package onetoone.Workout_Template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.Exercise_Template.ExerciseTemplate;
import onetoone.Trainer.Trainer;
import onetoone.WorkoutExercises.Exercise;
import onetoone.calendar.WorkoutDate;
import onetoone.users.User;
import onetoone.Trainer.Trainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * WorkoutTemplates are planned workouts with various Exercises which the user plans on completing later.
 * Contains a list of ExerciseTemplates which include the exercise with the users set weight, reps, sets.
 *
 * @author Dawud Benedict
 */
@Entity
public class WorkoutTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String templateName;
    private String description;
    private Date creationDate;

    // User linked to the WorkoutTemplate.
    // Each Template can only have one associated user.
    // TODO: Possibly add template sharing with friends
    @ManyToOne
    private User user;

    // list of exercises a user wants to complete within one session.
    // This is expected/goal not finished workouts.
    @JsonIgnore
    @OneToMany(mappedBy = "workoutTemplate")
    private List<ExerciseTemplate> exerciseTemplates;

    @JsonIgnore
    @OneToMany(mappedBy = "workout")
    private List<WorkoutDate> workoutDates;

    public WorkoutTemplate(String name, User user) {
        this.templateName = name;
        this.user = user;
        this.exerciseTemplates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        this.creationDate = new GregorianCalendar(
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond()).getTime();
        workoutDates = new ArrayList<>();
    }

    public WorkoutTemplate() {
        this.exerciseTemplates = new ArrayList<>();
    }

    public long getId() {return this.id;}

    public void setUser(User u) {this.user = u;}
    public User getUser() {return this.user;}

    public String getTemplateName() {return this.templateName;}
    public void setTemplateName(String templateName) {this.templateName = templateName;}

    public String getDescription() {return this.description;}
    public void setDescription(String description) {this.description = description;}

    public Date getCreationDate() {return this.creationDate;}
    public void setCreationDate(Date creationDate) {this.creationDate = creationDate;}

    public List<ExerciseTemplate> getExerciseTemplates() {return this.exerciseTemplates;}

    public void addExerciseTemplate(ExerciseTemplate exerciseTemplate) {this.exerciseTemplates.add(exerciseTemplate);}

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

}
