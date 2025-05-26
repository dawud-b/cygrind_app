package onetoone.Challenge;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.users.User;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JsonIgnore
    private ChallengeSet challengeSet;

    private String title;       // Title of challenge
    private String description; // Description of Challenge
    private int points;         // Points earned if completed
    private boolean completed;
    private Date completedDate;
    private Date assignedDate;


    public Challenge(String title, String description, int points) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.completed = false;
        this.assignedDate = getNow();
        this.completedDate = null;
    }

    public Challenge() {
        this.completed = false;
        this.assignedDate = getNow();
        this.completedDate = null;
    }

    public long getId() {return id;}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public int getPoints() {return points;}
    public void setPoints(int points) {this.points = points;}

    public boolean isCompleted() {return completed;}
    public void setCompleted() {this.completed = true;}

    public Date getCompletedDate() {return completedDate;}
    public void setCompletedDate(Date completedDate) {this.completedDate = completedDate;}

    public Date getAssignedDate() {return assignedDate;}
    public void setAssignedDate(Date assignedDate) {this.assignedDate = assignedDate;}

    public ChallengeSet getChallengeSet() {return challengeSet;}
    public void setChallengeSet(ChallengeSet challengeSet) {this.challengeSet = challengeSet;}

    public Date getNow(){
        LocalDateTime now = LocalDateTime.now();
        Calendar calendar = Calendar.getInstance();
        calendar.set(now.getYear(), now.getMonthValue()-1, now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond());
        return calendar.getTime();
    }
}
