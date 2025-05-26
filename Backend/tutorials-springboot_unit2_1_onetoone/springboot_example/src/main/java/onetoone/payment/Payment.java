package onetoone.payment;

import jakarta.persistence.*;
import onetoone.users.User;

import java.time.LocalDateTime;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long amountPaid; // in cents
    private String currency;
    private String paymentIntentId;
    private String status; // e.g., "succeeded", "failed"
    private LocalDateTime paymentDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Payment() {
        paymentDate = LocalDateTime.now();
    }

    public Long getId() {return id;}

    public Long getAmountPaid() {return amountPaid;}
    public void setAmountPaid(Long amountPaid) {this.amountPaid = amountPaid;}

    public String getCurrency() {return currency;}
    public void setCurrency(String currency) {this.currency = currency;}

    public String getPaymentIntentId() {return paymentIntentId;}
    public void setPaymentIntentId(String paymentIntentId) {this.paymentIntentId = paymentIntentId;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public LocalDateTime getPaymentDate() {return paymentDate;}
    public void setPaymentDate(LocalDateTime paymentDate) {this.paymentDate = paymentDate;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
}
