package onetoone.FriendRequest;

import jakarta.persistence.*;
import onetoone.users.User;

import java.util.Calendar;


/**
 * FriendRequest class which includes the sender, reciever, date/time sent, and the current status
 * of a friend request.
 *
 * @author Dawud Benedict
 */
@Entity
public class FriendRequest {

    final static int PENDING = 0;
    final static int ACCEPTED = 1;
    final static int IGNORED = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long friendRequestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    private Calendar date;

    private int status;

    public FriendRequest(User sender, User receiver, Calendar date) {
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
        this.status = PENDING;
    }

    public FriendRequest() {}

    public long getFriendRequestId() {
        return friendRequestId;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {return receiver;}

    public Calendar getDate() {return date;}
    public int getYear() { return (date != null) ? date.get(Calendar.YEAR) : 0; }
    public int getMonth() { return (date != null) ? date.get(Calendar.MONTH) : 0; }
    public int getDay() { return (date != null) ? date.get(Calendar.DAY_OF_MONTH) : 0; }
    public int getHour() { return (date != null) ? date.get(Calendar.HOUR_OF_DAY) : 0; }
    public int getMinute() { return (date != null) ? date.get(Calendar.MINUTE) : 0; }

    // month from 1 to 12.
    public void setDate(int year, int month, int day, int hour, int minute) {
        this.date = Calendar.getInstance();
        this.date.set(year, (month-1), day, hour, minute);
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public int getStatus() {return status;}

}
