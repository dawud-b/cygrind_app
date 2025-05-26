package onetoone.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import onetoone.users.User;


/**
 * Saves message information including the contents of the message, sender, chatSession it was sent in,
 * and the date/time it was sent.
 *
 * @author Dawud Benedict
 */
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who sent the message
    @ManyToOne
    private User user;

    // What the message is
    @Lob
    private String content;

    // The time the message was sent (server time)
    @Column(name = "date")
    private Date sent = new Date();

    // what chat the message belong to
    @ManyToOne
    private ChatSession chatSession;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Reaction> reactions;

	public Message() {};

	public Message(User user, String content, Date time, ChatSession chatSession) {
		this.user = user;
		this.content = content;
        this.sent = time;
        this.chatSession = chatSession;
        reactions = new ArrayList<>();
	}

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public Date getSent() {
        return sent;
    }
    public void setSent(Date sent) {
        this.sent = sent;
    }

    public void addReaction(Reaction reaction) {reactions.add(reaction);}
    public List<Reaction> getReactions() {return reactions;}

}
