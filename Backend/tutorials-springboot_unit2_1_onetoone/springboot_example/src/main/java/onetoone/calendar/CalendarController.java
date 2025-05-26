package onetoone.calendar;

import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.Workout_Template.WorkoutTemplateRepo;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Calender Controller", description = "Assign workouts to dates and view them in different ways.")
@RestController
public class CalendarController {

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutTemplateRepo templateRepo;

    // Returns the workout dates for a specific User
    @GetMapping("/{username}/calendar")
    public List<WorkoutDate> getUserCalendar(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        return calendarRepository.findByUser(user);
    }

    // Returns the workout dates that are completed for a specific User
    @GetMapping("/{username}/calendar/completed")
    public List<WorkoutDate> getCompletedWorkoutsByUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        List<WorkoutDate> returnList = new ArrayList<>();
        for (WorkoutDate workout : calendarRepository.findByUser(user)) {
            if (workout.isCompleted()) {
                returnList.add(workout);
            }
        }
        return returnList;
    }

    // Returns the workout dates that are not completed for a specific User
    @GetMapping("/{username}/calendar/planned")
    public List<WorkoutDate> getPlannedWorkoutsByUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        List<WorkoutDate> returnList = new ArrayList<>();
        for (WorkoutDate workout : calendarRepository.findByUser(user)) {
            if (!workout.isCompleted() ) {
                returnList.add(workout);
            }
        }
        return returnList;
    }

    // Returns the workout dates that were planned in the past but not completed
    @GetMapping("/{username}/calendar/overdue")
    public List<WorkoutDate> getOverdueWorkouts(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        List<WorkoutDate> returnList = new ArrayList<>();
        for (WorkoutDate workout : calendarRepository.findByUser(user)) {
            if (!workout.isCompleted() && workout.getDate().before(now)) {
                returnList.add(workout);
            }
        }
        return returnList;
    }

    // Returns all the completed workouts that use this workoutTemplate
    @GetMapping("/{workout_id}/completed")
    public List<WorkoutDate> getCompletedWorkoutsByWorkout(@PathVariable int workout_id) {
        WorkoutTemplate workout = templateRepo.findById(workout_id);
        if (workout == null) {return null;}

        List<WorkoutDate> workoutDates = calendarRepository.findByWorkout(workout);
        if (workoutDates == null) {return null;}

        List<WorkoutDate> returnList = new ArrayList<>();
        for (WorkoutDate workoutDate : workoutDates) {
            if (workoutDate.isCompleted()) {returnList.add(workoutDate);}
        }
        return returnList;
    }

    // Returns all the future planned workouts that use this workoutTemplate.
    @GetMapping("/{workout_id}/upcoming")
    public List<WorkoutDate> getPlannedWorkoutsByWorkout(@PathVariable int workout_id) {
        WorkoutTemplate workout = templateRepo.findById(workout_id);
        if (workout == null) {return null;}

        List<WorkoutDate> workoutDates = calendarRepository.findByWorkout(workout);
        if (workoutDates == null) {return null;}

        List<WorkoutDate> returnList = new ArrayList<>();
        for (WorkoutDate workoutDate : workoutDates) {
            if (!workoutDate.isCompleted() && workoutDate.getDate().after(LocalDateTime.now())) {returnList.add(workoutDate);}
        }
        return returnList;
    }

    // Assign a workout to a workoutDate. Auto sets it as uncompleted.
    @PostMapping("/{username}/calendar")
    public String createWorkoutDate(@PathVariable String username, @RequestBody WorkoutDate workoutDate) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return "User not found";}

        if (workoutDate.getDate() == null) {return "Invalid date";}

        if (workoutDate.getWorkout() == null) {return "Invalid workout";}

        workoutDate.setUser(user);
        user.addPlannedWorkout(workoutDate);
        userRepository.save(user);
        calendarRepository.save(workoutDate);
        return "Date set for workout";
    }

    // Marks workoutDate as a workout that has been completed. Checks that the date is not in the future.
    @PutMapping("/{username}/calendar/{id}/complete")
    public String setAsCompleted(@PathVariable long id, @PathVariable String username) {
        WorkoutDate workoutDate = calendarRepository.findById(id);
        User user = userRepository.findByUsername(username);
        if (user == null) {return "User not found";}

        if (workoutDate == null) {return "Workout not found";}

        if (workoutDate.getDate().after(LocalDateTime.now())) {return "Cannot complete a future workout";}

        if (workoutDate.isCompleted()) {return "Workout already completed";}

        workoutDate.setCompleted(true);
        user.removePlannedWorkout(workoutDate);
        user.addCompletedWorkout(workoutDate);
        userRepository.save(user);
        calendarRepository.save(workoutDate);
        return "Workout marked as completed";
    }

    // Marks workoutDate as a workout that is not completed.
    @PutMapping("/{username}/calendar/{id}/notComplete")
    public String setAsUncompleted(@PathVariable long id, @PathVariable String username) {
        WorkoutDate workoutDate = calendarRepository.findById(id);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }
        if (workoutDate == null) {
            return "Workout not found";
        }
        if (!workoutDate.isCompleted()) {
            return "Workout is already uncompleted";
        }
        workoutDate.setCompleted(false);
        user.removeCompletedWorkout(workoutDate);
        user.addPlannedWorkout(workoutDate);
        userRepository.save(user);
        calendarRepository.save(workoutDate);
        return "Workout marked as uncompleted";
    }

    // delete a workout assigned to a date.
    @DeleteMapping("/{username}/calendar/{id}")
    public String deleteWorkout(@PathVariable long id, @PathVariable String username) {
        WorkoutDate delete = calendarRepository.findById(id);
        User user = userRepository.findByUsername(username);
        if (delete == null) {
            return "Workout not found";
        }
        if (delete.isCompleted()) {
            user.removeCompletedWorkout(delete);
        }
        else {
            user.removePlannedWorkout(delete);
        }
        userRepository.save(user);
        calendarRepository.delete(delete);
        return "Workout deleted";
    }



}
