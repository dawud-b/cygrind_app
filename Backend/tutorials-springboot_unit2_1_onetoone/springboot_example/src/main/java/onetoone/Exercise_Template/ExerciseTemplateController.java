package onetoone.Exercise_Template;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import onetoone.FriendRequest.FriendRequest;
import onetoone.WorkoutExercises.Exercise;
import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.Workout_Template.WorkoutTemplateRepo;
import onetoone.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Exercise Template Controller", description = "Controller used to manage Exercise Templates")
@RestController
public class ExerciseTemplateController {

    @Autowired
    ExerciseTemplateRepo exerciseTemplateRepo;

    @Autowired
    private WorkoutTemplateRepo workoutTemplateRepo;

    @Operation(summary = "Get Exercise Template by its id", description = "Returns the Exercise Template assigned to the path variable id")
    @GetMapping("/exerciseTemplate/{id}")
    public ExerciseTemplate getExerciseTemplate(@Parameter(description = "Id of the exerciseTemplate needed") @PathVariable int id) {
        exerciseTemplateRepo.findById(id);
        return exerciseTemplateRepo.findById(id);
    }

    @Operation(summary = "Create new Exercise Template", description = "Creates a new exerciseTemplate with the information passed in the Request Body")
    @PostMapping("/exerciseTemplate")
    public ExerciseTemplate addExerciseTemplate(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Exercise Template to create", required = true, content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ExerciseTemplate.class),
            examples = @ExampleObject(value = "{ \"workoutTemplate\": {\"id\": workout_template_id}, \"exercise\": {\"id\": exercise_id}, \"repCount\": num_reps, \"setCount\": num_sets}")))
                                                    @RequestBody ExerciseTemplate exerciseTemplate) {
        WorkoutTemplate worktemp = exerciseTemplate.getWorkoutTemplate();
        if (worktemp == null) {
            return null;
        }
        worktemp.addExerciseTemplate(exerciseTemplate);
        return exerciseTemplateRepo.save(exerciseTemplate);
    }

    @Operation(summary = "Delete a Exercise Template", description = "Deletes the exerciseTemplate with the path variable id")
    @DeleteMapping("/exerciseTemplate/{id}")
    public String deleteExerciseTemplate(@Parameter(description = "Id of ExerciseTemplate to delete") @PathVariable int id) {
        ExerciseTemplate exTemp = exerciseTemplateRepo.findById(id);
        if (exTemp == null) {
             return "ExerciseTemplate not found.";
        }
        exerciseTemplateRepo.delete(exTemp);
        return "Deleted.";
    }

    @Operation(summary = "Edit a Exercise Template", description = "Updates a Exercise Template with the information in the request body object. Cannot update workoutTemplate")
    @PutMapping("/exerciseTemplate/{id}")
    public ExerciseTemplate updateExerciseTemplate(@PathVariable int id, @RequestBody ExerciseTemplate exerciseTemplate) {
        ExerciseTemplate existingExerciseTemplate = exerciseTemplateRepo.findById(id);
        if (existingExerciseTemplate.getWorkoutTemplate() == null) {
            return null;
        }

        if (exerciseTemplate.getRepCount() != 0) {
            existingExerciseTemplate.setRepCount(exerciseTemplate.getRepCount());
        }

        if (exerciseTemplate.getSetCount() != 0) {
            existingExerciseTemplate.setSetCount(exerciseTemplate.getSetCount());
        }

        if (exerciseTemplate.getDuration() != 0) {
            existingExerciseTemplate.setDuration(exerciseTemplate.getDuration());
        }

        if (exerciseTemplate.getWeight() != 0) {
            existingExerciseTemplate.setWeight(exerciseTemplate.getWeight());
        }
        exerciseTemplateRepo.save(existingExerciseTemplate);
        return existingExerciseTemplate;
    }

    // GET the ExerciseTemplates from a WorkoutTemplate
    @Operation(summary = "Get all Exercise Templates from WorkoutTemplate", description = "Returns all the Exercise Templates that are assigned to the workoutTemplate assigned to the path variable id")
    @GetMapping("/templates/{workoutTemplate_id}/exercises")
    List<ExerciseTemplate> getTemplatesByUser(@PathVariable int workoutTemplate_id) {
        WorkoutTemplate workoutTemp = workoutTemplateRepo.findById(workoutTemplate_id);
        if (workoutTemp == null) {
            return null;
        }
        return exerciseTemplateRepo.findByWorkoutTemplate(workoutTemp);
    }
}
