package onetoone.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stripe.model.Account;
import jakarta.persistence.*;

import onetoone.Challenge.Challenge;
import onetoone.Challenge.ChallengeSet;
import onetoone.WorkoutGroups.WorkoutGroup;
import onetoone.FriendRequest.FriendRequest;
import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.calendar.WorkoutDate;
import onetoone.chat.ChatSession;
import onetoone.payment.Payment;
import onetoone.points.PointTransaction;
import onetoone.points.UserPremiumReaction;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Provides the Definition/Structure for user information
 *
 * @author Dawud Benedict, Joey
 */
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private int height; // inches
    private int weight; // lbs
    private int age;
    private String password;
    private String UserRole;
    private int totalPoints; // Total points earned by the user
    private boolean activeSubscription;
    private String stripeCustomerId;
    private String subscriptionId;
    private String paymentMethodId;

    // ordered WorkoutTemplate list, should list in order created
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    List<WorkoutTemplate> workoutTemplateList;

    // New field for workout groups
    @JsonIgnore
    @ManyToMany(mappedBy = "members")
    private Set<WorkoutGroup> workoutGroups = new HashSet<>();

    @JsonIgnore
    @OneToMany
    List<WorkoutTemplate> Workout_Template;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "users_friends",
            joinColumns = @JoinColumn(name = "User_id"),
            inverseJoinColumns = @JoinColumn(name = "Friend_id"))
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<User> friendsList;

    @OneToMany(mappedBy = "sender")
    @JsonIgnore
    private Set<FriendRequest> sentRequests;

    @OneToMany(mappedBy = "receiver")
    @JsonIgnore
    private Set<FriendRequest> receivedRequests;

    @ManyToMany(mappedBy = "users")
    @JsonIgnore
    private List<ChatSession> chatSessions;

    @OneToMany
    private List<WorkoutDate> plannedWorkouts;

    @OneToMany
    private List<WorkoutDate> completedWorkouts;

    // Points system - user's point transaction history
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointTransaction> pointTransactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPremiumReaction> premiumReactions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeSet> challenges;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> paymentHistory;

    public User() {
        this.workoutTemplateList = new ArrayList<>();
        this.friendsList = new ArrayList<>();
        this.receivedRequests = new HashSet<>();
        this.sentRequests = new HashSet<>();
        this.workoutGroups = new HashSet<>();
        this.chatSessions = new ArrayList<>();
        this.plannedWorkouts = new ArrayList<>();
        this.completedWorkouts = new ArrayList<>();
        this.totalPoints = 0;
        this.pointTransactions = new ArrayList<>();
        this.challenges = new ArrayList<>();
        this.paymentHistory = new ArrayList<>();
    }

    public Set<WorkoutGroup> getWorkoutGroups() {
        return workoutGroups;
    }

    public void setWorkoutGroups(Set<WorkoutGroup> workoutGroups) {
        this.workoutGroups = workoutGroups;
    }

    public void addWorkoutGroup(WorkoutGroup group) {
        this.workoutGroups.add(group);
    }

    public void removeWorkoutGroup(WorkoutGroup group) {
        this.workoutGroups.remove(group);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getUserRole() {
        return this.UserRole;
    }
    public void setUserRole(String UserRole){
        this.UserRole = UserRole;
    }

    public int getFriendsCount() {return friendsList.size(); }

    public int getHeight() {return this.height;}
    public void setHeight(int height) {this.height = height;}

    public int getWeight() {return this.weight;}
    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getAge() {return this.age;}
    public void setAge(int age) {this.age = age;}

    public String getPassword() {return this.password;}
    public void setPassword(String password) {this.password = password;}

    public List<WorkoutTemplate> getWorkoutTemplateList() { return this.workoutTemplateList; }
    public void setWorkoutTemplateList(List<WorkoutTemplate> workoutTemplateList) {
        this.workoutTemplateList = workoutTemplateList;
    }

    public void addWorkoutTemplate(WorkoutTemplate workoutTemplate) {
        this.workoutTemplateList.add(workoutTemplate);
    }

    public void removeWorkoutTemplate(WorkoutTemplate workoutTemplate) {
        this.workoutTemplateList.remove(workoutTemplate);
    }

    public void addFriend(User user) {friendsList.add(user);}
    public void removeFriend(User user) {friendsList.remove(user);}
    public List<User> getFriendsList() {return this.friendsList;}

    public User getFriendByUsername(String friendUsername) {
        if (this.friendsList == null) {
            return null;
        }
        for (User friend : this.friendsList) {
            if (friend.getUsername().equals(friendUsername)) {
                return friend;
            }
        }
        return null;
    }

    public boolean isFriend(User user) {
        return this.friendsList.contains(user);
    }

    public int getFriendCount() {return this.friendsList.size();}

    public List<ChatSession> getChatSessions() {return this.chatSessions;}
    public void addChatSessions(ChatSession chatSession) {
        this.chatSessions.add(chatSession);
    }
    public void removeChatSession(ChatSession chatSession) {
        this.chatSessions.remove(chatSession);
    }

    public void addPlannedWorkout(WorkoutDate plannedWorkout) {
        this.plannedWorkouts.add(plannedWorkout);
    }

    public void removePlannedWorkout(WorkoutDate plannedWorkout) {
        this.plannedWorkouts.remove(plannedWorkout);
    }

    public void addCompletedWorkout(WorkoutDate completedWorkout) {
        this.completedWorkouts.add(completedWorkout);
        // Note: Points for completed workouts should be awarded through the PointsController
    }

    public void removeCompletedWorkout(WorkoutDate completedWorkout) {
        this.completedWorkouts.remove(completedWorkout);
    }

    // Points system methods
    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void addPoints(int points) {
        this.totalPoints += points;
    }

    public List<PointTransaction> getPointTransactions() {
        return pointTransactions;
    }

    public void setPointTransactions(List<PointTransaction> pointTransactions) {
        this.pointTransactions = pointTransactions;
    }

    public void addChallenge(ChallengeSet challengeSet) {this.challenges.add(challengeSet);}
    public void removeChallenge(ChallengeSet challengeSet) {this.challenges.remove(challengeSet);}
    public List<ChallengeSet> getChallenges() {return this.challenges;}

    public boolean isSubscribed() {return this.activeSubscription;}
    public void setSubscription(boolean paid) {this.activeSubscription = paid;}

    public String getStripeCustomerId() {return this.stripeCustomerId;}
    public void setStripeCustomerId(String stripeCustomerId) {this.stripeCustomerId = stripeCustomerId;}

    public String getSubscriptionId() {return this.subscriptionId;}
    public void setSubscriptionId(String subscriptionId) {this.subscriptionId = subscriptionId;}

    public String getPaymentMethodId() {return this.paymentMethodId;}
    public void setPaymentMethodId(String paymentMethodId) {this.paymentMethodId = paymentMethodId;}

    public List<Payment> getPaymentHistory() {return this.paymentHistory;}
    public void clearPremiumReactions() {this.premiumReactions.clear();}

    public void removePointTransaction(PointTransaction transaction) {
        if (pointTransactions != null && pointTransactions.remove(transaction)) {
            transaction.setUser(null);
        }
    }

    public void setPaymentHistory(List<Payment> paymentHistory) {this.paymentHistory = paymentHistory;}


    @Override
    public String toString() {
        return username + " "
                + firstName + " "
                + lastName + " "
                + email + " "
                + telephone;
    }
}