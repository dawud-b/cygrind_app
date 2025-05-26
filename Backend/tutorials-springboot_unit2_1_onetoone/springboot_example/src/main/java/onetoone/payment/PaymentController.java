package onetoone.payment;

import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import onetoone.Challenge.Challenge;
import onetoone.Challenge.ChallengeRepository;
import onetoone.Challenge.ChallengeSet;
import onetoone.Challenge.ChallengeSetRepo;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class PaymentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeSetRepo challengeSetRepo;

    public PaymentController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/payment/{username}")
    public List<Payment> getPaymentHistory(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        return paymentRepository.findByUser(user);
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> data) {
        try {
            long amount = Long.parseLong(data.get("amount").toString());

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("usd")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("clientSecret", intent.getClientSecret());

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment/subscription")
    public ResponseEntity<Map<String, String>> createSubscription(@RequestBody User user) {
        try {
            String email = user.getEmail();
            String paymentMethodId = user.getPaymentMethodId(); // Assuming the user has a payment method
            User currentUser = userRepository.findByEmail(email);
            if (currentUser == null) {
                return null;
            }

            // Check if the user already has a Stripe customer ID
            Customer customer;
            if (currentUser.getStripeCustomerId() == null) {
                Map<String, Object> customerParams = new HashMap<>();
                customerParams.put("email", email);
                customer = Customer.create(customerParams);
                currentUser.setStripeCustomerId(customer.getId());
                userRepository.save(currentUser);
            }
            else {
                // Retrieve the existing customer if the user already has one
                customer = Customer.retrieve(currentUser.getStripeCustomerId());
            }

            try {
                PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
                Map<String, Object> attachParams = new HashMap<>();
                attachParams.put("customer", customer.getId());
                paymentMethod.attach(attachParams);
            } catch (InvalidRequestException e) {
                if (!e.getMessage().contains("already been attached")) {
                    throw e;
                }
            }

            customer.update(Map.of("invoice_settings", Map.of("default_payment_method", paymentMethodId)));

            // Now that we have a valid customer and payment method, create the subscription
            String priceId = "price_1RKlSgPEN0w34OOGDUxf1bLa"; // Your Stripe price ID

            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                    .setCustomer(customer.getId())
                    .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                    .addAllExpand(List.of("latest_invoice.payment_intent"))
                    .build();

            // Create the subscription
            Subscription subscription = Subscription.create(params);

            // Check if the subscription creation was successful (status should be "active")
            if (!"active".equals(subscription.getStatus())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Subscription creation failed"));
            }

            // Get client secret for front-end payment confirmation
            PaymentIntent paymentIntent = subscription.getLatestInvoiceObject().getPaymentIntentObject();
            String clientSecret = null;

            if("requires_action".equals(paymentIntent.getStatus()) ||
                "requires_confirmation".equals(paymentIntent.getStatus()) ||
                    "requires_payment_method".equals(paymentIntent.getStatus())) {
                clientSecret = paymentIntent.getClientSecret();
            }

            // Save subscriptionId to user (only after confirming the subscription is active)
            currentUser.setSubscriptionId(subscription.getId());
            userRepository.save(currentUser);

            Map<String, String> response = new HashMap<>();
            response.put("subscriptionId", subscription.getId());
            if (clientSecret != null) {
                response.put("clientSecret", clientSecret);
            }

            // After successfully creating a subscription
            Payment payment = new Payment();
            payment.setAmountPaid(subscription.getLatestInvoiceObject().getAmountPaid());
            payment.setCurrency("usd"); // Adjust this based on actual payment details
            payment.setPaymentIntentId(subscription.getLatestInvoiceObject().getPaymentIntentObject().getId());
            payment.setStatus("succeeded");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setUser(currentUser);

            // Save payment and update user's payment history
            paymentRepository.save(payment);
            currentUser.getPaymentHistory().add(payment);
            // Set that they now are active subscriber
            currentUser.setSubscription(true);
            userRepository.save(currentUser);

            // updates the challenge if wasn't completed yet
            checkChallenge(currentUser);
            userRepository.save(currentUser);

            return ResponseEntity.ok(response);

        } catch (InvalidRequestException e) {
            // Handle the specific exception for already attached payment methods
            String errorMessage = e.getMessage();
            if (errorMessage.contains("The payment method you provided has already been attached to a customer")) {
                // Return a custom response to the frontend
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Payment method already attached to a customer"));
            }
            // Catch other InvalidRequestException errors and return the message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMessage));
        } catch (Exception e) {
            // Catch any other exceptions and send a generic 500 error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/subscription/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(@PathVariable String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.cancel(); // Immediate cancellation

            Optional<User> optionalUser = userRepository.findBySubscriptionId(subscriptionId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setSubscription(false);
                user.setSubscriptionId(null);
                userRepository.save(user);
            }

            return ResponseEntity.ok(Map.of("message", "Subscription cancelled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private void checkChallenge(User user) {
        ChallengeSet userChallenges = challengeSetRepo.findByUserAndTitle(user, "Premium Account");
        if (userChallenges != null && userChallenges.getProgress() != 1) {
            if (user.isSubscribed() && userChallenges.getChallengesCompleted() == 0) {
                Challenge challenge = userChallenges.getChallengeByStage(userChallenges.getChallengesCompleted());
                challenge.setCompleted();
                challenge.setCompletedDate(challenge.getNow());
                challengeRepository.save(challenge);

                user.addPoints(challenge.getPoints());
                userRepository.save(user);

                userChallenges.setChallengesCompleted();
                challengeSetRepo.save(userChallenges);
                userRepository.save(user);
            }
        }
    }
}

