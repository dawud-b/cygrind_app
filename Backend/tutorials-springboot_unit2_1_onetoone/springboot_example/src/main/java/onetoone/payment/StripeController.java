package onetoone.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import jakarta.servlet.http.HttpServletRequest;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Scanner;

@RestController
@RequestMapping("/api/stripe/webhook")
public class StripeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        String payload;
        try (Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A")) {
            payload = s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }

        Event event;
        try {
            event = objectMapper.readValue(payload, Event.class);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        // Handle successful payment
        if ("invoice.paid".equals(event.getType())) {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            if (invoice != null) {
                String stripeCustomerId = invoice.getCustomer();

                Optional<User> optionalUser = userRepository.findByStripeCustomerId(stripeCustomerId);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();

                    Payment payment = new Payment();
                    payment.setAmountPaid(invoice.getAmountPaid());
                    payment.setCurrency(invoice.getCurrency());
                    payment.setPaymentIntentId(invoice.getPaymentIntent());
                    payment.setStatus("succeeded");
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setUser(user);
                    paymentRepository.save(payment);

                    user.setSubscription(true);
                    user.setSubscriptionId(invoice.getSubscription());
                    userRepository.save(user);
                }
            }
        }

        // Handle subscription cancellation
        if ("customer.subscription.deleted".equals(event.getType())) {
            Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
            if (subscription != null) {
                Optional<User> optionalUser = userRepository.findBySubscriptionId(subscription.getId());
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setSubscription(false);
                    user.setSubscriptionId(null);
                    userRepository.save(user);
                }
            }
        }

        return ResponseEntity.ok("Received");
    }
}
