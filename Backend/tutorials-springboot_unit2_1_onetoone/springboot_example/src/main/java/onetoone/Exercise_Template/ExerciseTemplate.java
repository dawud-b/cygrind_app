package onetoone.Exercise_Template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.WorkoutExercises.Exercise;
import onetoone.Workout_Template.WorkoutTemplate;

@Entity
public class ExerciseTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int weight;
    private int repCount;
    private int setCount;
    private int duration; // seconds

    @ManyToOne
    private Exercise exercise; // many ExerciesTemplates can have the same Exercies.

    @ManyToOne
    private WorkoutTemplate workoutTemplate;

    public ExerciseTemplate(WorkoutTemplate workoutTemplate, Exercise exercise, int weight, int repCount, int setCount, int duration) {
        this.workoutTemplate = workoutTemplate;
        this.exercise = exercise;
        this.weight = weight;
        this.repCount = repCount;
        this.setCount = setCount;
        this.duration = duration;
    }

    public ExerciseTemplate() {}

    public WorkoutTemplate getWorkoutTemplate() {return workoutTemplate;}
    public void setWorkoutTemplate(WorkoutTemplate workoutTemplate) {this.workoutTemplate = workoutTemplate;}

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public int getWeight() {return weight;}
    public void setWeight(int weight) {this.weight = weight;}

    public int getRepCount() {return repCount;}
    public void setRepCount(int repCount) {this.repCount = repCount;}

    public int getSetCount() {return setCount;}
    public void setSetCount(int setCount) {this.setCount = setCount;}

    public int getDuration() {return duration;}
    public void setDuration(int duration) {this.duration = duration;}

    public Exercise getExercise() {return exercise;}
    public void setExercise(Exercise exercise) {this.exercise = exercise;}

}
