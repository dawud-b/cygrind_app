package onetoone.calendar;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.users.User;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Calendar;

@Entity
public class WorkoutDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private WorkoutTemplate workout;

    @ManyToOne
    private User user;

    private boolean completed;

    private Calendar date;

    public WorkoutDate() {}

    public void setCompleted(boolean completed) {this.completed = completed;}
    public boolean isCompleted() {return this.completed;}

    public User getUser() {return this.user;}
    public void setUser(User user) {this.user = user;}

    public WorkoutTemplate getWorkout() {return this.workout;}
    public void setWorkout(WorkoutTemplate workout) {this.workout = workout;}

    public Calendar getDate() {return this.date;}
    public void setDate(Calendar date) {this.date = date;}

}
