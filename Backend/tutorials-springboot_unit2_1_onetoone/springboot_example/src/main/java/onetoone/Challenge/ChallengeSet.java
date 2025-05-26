package onetoone.Challenge;


import jakarta.persistence.*;
import onetoone.users.User;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ChallengeSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private User user;

    @OneToMany(mappedBy = "challengeSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Challenge> challenges;

    private String title;
    private int challengesCompleted;

    public ChallengeSet(User user, String title) {
        this.title = title;
        this.user = user;
        this.challenges = new ArrayList<>();
        this.challengesCompleted = 0;
    }

    public ChallengeSet() {
        this.challenges = new ArrayList<>();
        this.challengesCompleted = 0;
    }

    public long getId() {return id;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    public List<Challenge> getChallenges() {return challenges;}
    public void setChallenges(List<Challenge> challenges) {this.challenges = challenges;}
    public void addChallenge(Challenge challenge) {this.challenges.add(challenge);}
    public void removeChallenge(Challenge challenge) {this.challenges.remove(challenge);}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public int getStages() {return challenges.size();}

    //public int getChallengesCompleted() {return challengesCompleted;}
    //public void incrementChallengesCompleted() {challengesCompleted++;}
    public int getChallengesCompleted() {
        return (int) challenges.stream().filter(Challenge::isCompleted).count();
    }

    public void setChallengesCompleted() {
        challengesCompleted = (int)challenges.stream().filter(Challenge::isCompleted).count();
    }

    public double getProgress() {
        if (challengesCompleted == getStages()) {
            return 1;
        }
        if (challengesCompleted == 0 || getStages() == 0) {
            return 0;
        }
        return challengesCompleted/(double)getStages();
    }

    // 0 indexed
    public Challenge getChallengeByStage(int stage) {
        if (stage < 0 || stage >= challenges.size()) {
            return null;
        }
        return challenges.get(stage);
    }

    public List<Challenge> getByChallengesCompleteness(boolean completeness) {
        List<Challenge> completedChallenges = new ArrayList<>();
        for (Challenge challenge : challenges) {
            if (challenge.isCompleted() == completeness) {
                completedChallenges.add(challenge);
            }
        }
        return completedChallenges;
    }
}
