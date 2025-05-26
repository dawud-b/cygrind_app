package onetoone.Workout_Template;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Exercise_Template.ExerciseTemplate;
import onetoone.Exercise_Template.ExerciseTemplateRepo;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Workout Template Controller", description = "Endpoints for managing workout templates and assigning exercises")
@RestController
public class WorkoutTemplateController {

    @Autowired
    WorkoutTemplateRepo templateRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseTemplateRepo exerciseTemplateRepo;

    @Operation(summary = "Get all workout templates", description = "Returns a list of all workout templates in the database")
    @GetMapping(path = "/templates")
    List<WorkoutTemplate> getAllTemplates() {
        return templateRepo.findAll();
    }

    @Operation(summary = "Get workout template by ID", description = "Returns a specific workout template by its ID")
    @GetMapping(path = "/templates/{id}")
    WorkoutTemplate getTemplateById(@Parameter(description = "ID of the workout template") @PathVariable int id) {
        return templateRepo.findById(id);
    }

    @Operation(summary = "Get exercise templates for a workout", description = "Returns all exercise templates associated with a specific workout template")
    @GetMapping("/templates/{id}/exerciseTemplates")
    List<ExerciseTemplate> getAllExerciseTemplates(@Parameter(description = "Workout template ID") @PathVariable int id) {
        WorkoutTemplate template = templateRepo.findById(id);
        return template.getExerciseTemplates();
    }

    @Operation(summary = "Create a new workout template", description = "Creates a new workout template. Requires a user ID in the nested user object.")
    @PostMapping("/templates")
    WorkoutTemplate createTemplate(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Workout Template to create", required = true, content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ExerciseTemplate.class),
            examples = @ExampleObject(value = "{ \"templateName\": \"string\", \"user\": {\"id\": user_id}, \"description\": \"description of workout\"}")))
                                   @RequestBody WorkoutTemplate template) {
        if (template.getTemplateName() == null) {
            return null;
        }
        User u = template.getUser();
        if (u == null) {
            return null;
        }
        u.addWorkoutTemplate(template);
        return templateRepo.save(template);
    }

    @Operation(summary = "Delete a workout template", description = "Deletes a workout template and all associated exercise templates")
    @DeleteMapping("/templates/{template_id}")
    String deleteTemplate(@Parameter(description = "ID of the workout template to delete") @PathVariable int template_id) {
        WorkoutTemplate template = templateRepo.findById(template_id);
        if (template == null) {
            return "Failed. Template not found";
        }
        User u = template.getUser();
        if (u == null) {
            return "Failed. User not found";
        }
        u.removeWorkoutTemplate(template);
        List<ExerciseTemplate> exercises = exerciseTemplateRepo.findByWorkoutTemplate(template);
        for (ExerciseTemplate e : exercises) {
            exerciseTemplateRepo.delete(e);
        }
        templateRepo.delete(template);
        return "Deleted";
    }

    @Operation(summary = "Update a workout template", description = "Updates the name and/or description of a workout template. Cannot change user.")
    @PutMapping("/templates/{template_id}")
    WorkoutTemplate updateTemplate(@Parameter(description = "ID of the workout template to update") @PathVariable int template_id, @RequestBody WorkoutTemplate template) {
        WorkoutTemplate templateToUpdate = templateRepo.findById(template_id);
        if (templateToUpdate.getUser() == null) {
            return null;
        }

        if (template.getDescription() != null) {
            templateToUpdate.setDescription(template.getDescription());
        }

        if (template.getTemplateName() != null) {
            templateToUpdate.setTemplateName(template.getTemplateName());
        }

        templateRepo.save(templateToUpdate);
        return templateToUpdate;
    }

    @Operation(summary = "Get templates by username", description = "Returns a list of workout templates created by a specific user")
    @GetMapping("/{username}/templates")
    List<WorkoutTemplate> getTemplatesByUser(@Parameter(description = "Username of the user") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        return templateRepo.findByUser(user);
    }

//    @Operation(summary = "Add an exercise to a workout template", description = "Adds a new exercise template to the workout template's list")
//    @PutMapping("/templates/{workoutTemp_id}/add")
//    WorkoutTemplate addExercise(@Parameter(description = "Workout template ID") @PathVariable int workoutTemp_id, @RequestBody ExerciseTemplate exercise) {
//        WorkoutTemplate temp = templateRepo.findById(workoutTemp_id);
//        temp.addExerciseTemplate(exercise);
//        return temp;
//    }

}