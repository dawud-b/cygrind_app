package onetoone.WorkoutGroups;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.users.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class WorkoutGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupName;

    @Column
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String groupType; // e.g., "Public", "Private", "Invite Only"

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @JsonIgnore
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JoinRequest> joinRequests = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "workout_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    // Constructors
    public WorkoutGroup() {
        this.createdAt = LocalDateTime.now();
    }

    public WorkoutGroup(String groupName, User leader, String description, String groupType) {
        this.groupName = groupName;
        this.leader = leader;
        this.description = description;
        this.groupType = groupType;
        this.createdAt = LocalDateTime.now();
        this.members.add(leader);  // Leader is automatically a member
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<JoinRequest> getJoinRequests() {
        return joinRequests;
    }

    public void setJoinRequests(List<JoinRequest> joinRequests) {
        this.joinRequests = joinRequests;
    }

    // Additional methods to support DTO-like functionality
    public void updateFromDTO(WorkoutGroup updatedGroup) {
        if (updatedGroup.getGroupName() != null) {
            this.groupName = updatedGroup.getGroupName();
        }
        if (updatedGroup.getDescription() != null) {
            this.description = updatedGroup.getDescription();
        }
        if (updatedGroup.getGroupType() != null) {
            this.groupType = updatedGroup.getGroupType();
        }
    }

    // Method to add a member
    public boolean addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
            return true;
        }
        return false;
    }

    // Method to remove a member
    public boolean removeMember(User user) {
        if (members.contains(user)) {
            members.remove(user);
            return true;
        }
        return false;
    }
}