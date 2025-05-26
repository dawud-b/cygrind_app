package com.example.androidexample.Payment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.R;
import com.example.androidexample.URLManager;
import com.example.androidexample.VolleySingleton;

import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.PaymentMethodsActivityStarter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PaymentActivity extends AppCompatActivity {

    private Stripe stripe;
    private CardInputWidget cardInputWidget;
    private Button payButton;
    private Button cancelSubscriptionButton;
    private TextView subscriptionStatusText, subscriptionTimeRemaining;
    private LinearLayout paymentSection, subscriptionStatusSection;

    private String username;
    private String email;
    private Long userId;
    private String subscriptionId; // stores subscription id, -1 if not subbed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Load username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "");
        email = prefs.getString("email", "");
        userId = prefs.getLong("id", -1);

        // Stripe setup
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51RKkboPEN0w34OOG7QFc57aKfK0Gwj9wmBo7fNvJIPsJom8uH33zswroG90YRNQSKbdTQhPaZV98J95rvFsNo99J00WydOD6um"
        );

        stripe = new Stripe(
                getApplicationContext(),
                PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey()
        );

        // UI bindings
        cardInputWidget = findViewById(R.id.cardInputWidget);
        payButton = findViewById(R.id.payButton);
        cancelSubscriptionButton = findViewById(R.id.cancelSubscriptionButton);
        subscriptionStatusText = findViewById(R.id.subscriptionStatusText);
        paymentSection = findViewById(R.id.paymentSection);
        subscriptionStatusSection = findViewById(R.id.subscriptionStatusSection);

        // Initial state
        paymentSection.setVisibility(View.GONE);
        subscriptionStatusSection.setVisibility(View.GONE);

        // Check subscription status from backend
        checkSubscriptionStatus();

        payButton.setOnClickListener(v -> {
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                stripe.createPaymentMethod(
                        params,
                        new ApiResultCallback<PaymentMethod>() {
                            @Override
                            public void onSuccess(@NonNull PaymentMethod paymentMethod) {
                                String paymentMethodId = paymentMethod.id;
                                fetchClientSecretAndPay(paymentMethodId); // Call updated method with ID
                            }

                            @Override
                            public void onError(@NonNull Exception e) {
                                Toast.makeText(PaymentActivity.this, "Failed to create payment method: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Invalid card data.", Toast.LENGTH_SHORT).show();
            }

            // Delay the method call by 5 seconds (5000 milliseconds)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Call the method after the delay
                    checkSubscriptionStatus();
                }
            }, 5000); // Delay time in milliseconds
        });

        cancelSubscriptionButton.setOnClickListener(v -> cancelSubscription(subscriptionId));
    }

    private void checkSubscriptionStatus() {
        String url = "http://coms-3090-035.class.las.iastate.edu:8080/users/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("PaymentActivity", "SubStatusResponse:" + response);
                        boolean isActive = response.getBoolean("subscribed");
                        if (isActive) {
                            subscriptionStatusText.setText("You are subscribed.");
                            subscriptionId = response.getString("subscriptionId");
                            Toast.makeText(this, "You are subscribed!", Toast.LENGTH_SHORT).show();
                            subscriptionStatusSection.setVisibility(View.VISIBLE);
                            paymentSection.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(this, "You are not subscribed.", Toast.LENGTH_SHORT).show();
                            paymentSection.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        paymentSection.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    error.printStackTrace();
                    paymentSection.setVisibility(View.VISIBLE);
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void fetchClientSecretAndPay(String paymentMethodId) {
        if (paymentMethodId == null || paymentMethodId.isEmpty() || email == null || email.isEmpty()) {
            Toast.makeText(this, "Missing payment method or email", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("paymentMethodId", paymentMethodId);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    URLManager.getSubscriptionCreateURL(),
                    body,
                    response -> {
                        String clientSecret = null;
                        try {
                            checkSubscriptionStatus();
                            clientSecret = response.getString("clientSecret");
                            confirmPaymentIntent(clientSecret);
                        } catch (JSONException e) {
                            // do nothing
                        }
                    },
                    error -> {
                        String errorMessage = "An unexpected error occurred";

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                JSONObject errorObj = new JSONObject(responseBody);
                                if (errorObj.has("error")) {
                                    errorMessage = errorObj.getString("error");
                                    Log.d("PaymentActivity", "Error: " + errorMessage);
                                } else {
                                    errorMessage = "Server error: " + statusCode;
                                }
                            } catch (Exception e) {
                                errorMessage = "Failed to parse error response";
                            }

                            if (statusCode == 400) {
                                errorMessage = "Invalid payment method or user info. Please check and try again.";
                            } else if (statusCode == 500) {
                                errorMessage = "Server error. Please try again later.";
                            }
                        } else {
                            errorMessage = "Network error. Please check your internet connection.";
                        }

                        Log.d("PaymentActivity", "Error message: " + errorMessage);

                        checkSubscriptionStatus();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmPaymentIntent(String clientSecret) {
        ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams.create(clientSecret);
        stripe.confirmPayment(this, confirmParams);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stripe.onPaymentResult(requestCode, data, new ApiResultCallback<PaymentIntentResult>() {
            @Override
            public void onSuccess(@NonNull PaymentIntentResult result) {
                PaymentIntent paymentIntent = result.getIntent();
                Log.d("PaymentActivity", "Payment status: " + paymentIntent.getStatus());
                if (paymentIntent.getStatus() == PaymentIntent.Status.Succeeded) {
                    Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PaymentActivity.this, "Payment failed: " + paymentIntent.getStatus(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(@NonNull Exception e) {
                Toast.makeText(PaymentActivity.this, "Payment error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("PaymentActivity", "Payment error: " + e.getMessage());
            }
        });
    }

    public void cancelSubscription(String subscriptionId) {
        String url = URLManager.getSubscriptionCancelURL(subscriptionId); // e.g., https://your-backend.com/subscription/{subscriptionId}

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    // Success response
                    Toast.makeText(this, "Subscription cancelled", Toast.LENGTH_SHORT).show();
                    paymentSection.setVisibility(View.VISIBLE);
                    subscriptionStatusSection.setVisibility(View.GONE);
                },
                error -> {
                    // Error response
                    String errorMessage = "An error occurred";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            JSONObject errorJson = new JSONObject(body);
                            if (errorJson.has("error")) {
                                errorMessage = errorJson.getString("error");
                            }
                        } catch (Exception e) {
                            errorMessage = "Failed to parse error response";
                        }
                    } else {
                        errorMessage = "Network error or server unreachable";
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        // Set headers if needed (e.g., authentication)
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}
