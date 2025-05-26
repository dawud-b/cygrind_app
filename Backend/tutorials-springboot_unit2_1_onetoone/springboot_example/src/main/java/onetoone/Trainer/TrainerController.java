package onetoone.Trainer;

import onetoone.Workout_Template.WorkoutTemplate;
import onetoone.Workout_Template.WorkoutTemplateRepo;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * REST controller for trainer-related operations.
 * Handles all endpoints for trainer profiles, followers, and workout templates.
 */
@RestController
@Tag(name = "Trainer Controller", description = "REST APIs related to Trainer Entity and operations")
public class TrainerController {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutTemplateRepo workoutTemplateRepo;

    @Autowired
    private TrainerService trainerService;

    @Operation(summary = "Get all active trainers", description = "Retrieve a list of all trainers that are currently active in the system.")
    @GetMapping("/trainers")
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findByIsActiveTrue();
    }

    @Operation(summary = "Get trainer by ID", description = "Retrieve a specific trainer's profile using their unique identifier.")
    @GetMapping("/trainers/{id}")
    public Trainer getTrainerById(@PathVariable int id) {
        Trainer trainer = trainerRepository.findById(id);
        if (trainer == null || !trainer.isActive()) {
            return null; // Return null to match existing API pattern
        }
        return trainer;
    }

    @Operation(summary = "Get trainer followers", description = "Retrieve the list of users following a specific trainer.")
    @GetMapping("/trainers/{id}/followers")
    public Set<User> getTrainerFollowers(@PathVariable int id) {
        Trainer trainer = trainerRepository.findById(id);
        if (trainer == null || !trainer.isActive()) {
            return null;
        }
        return trainer.getFollowers();
    }

    @Operation(summary = "Get user's followed trainers", description = "Retrieve the list of trainers that a specific user is following.")
    @GetMapping("/users/{username}/followed-trainers")
    public List<Trainer> getFollowedTrainers(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Collections.emptyList(); // Return empty list if user not found
        }

        // Use the repository method to find all trainers followed by this user
        return trainerRepository.findByFollowersContaining(user);
    }

    @Operation(summary = "Get trainer's workout templates", description = "Retrieve all workout templates created by a specific trainer.")
    @GetMapping("/trainers/{id}/templates")
    public List<WorkoutTemplate> getTrainerWorkoutTemplates(@PathVariable int id) {
        Trainer trainer = trainerRepository.findById(id);
        if (trainer == null || !trainer.isActive()) {
            return null;
        }
        return trainer.getTrainerTemplates();
    }

    @Operation(summary = "Add workout template", description = "Create a new workout template associated with a specific trainer.")
    @PostMapping("/trainers/{trainerId}/templates")
    public WorkoutTemplate addWorkoutTemplate(@PathVariable int trainerId, @RequestBody WorkoutTemplate template) {
        Trainer trainer = trainerRepository.findById(trainerId);
        if (trainer == null || !trainer.isActive()) {
            return null;
        }

        // Set the trainer for this template
        template.setTrainer(trainer);

        // Save the template
        WorkoutTemplate savedTemplate = workoutTemplateRepo.save(template);

        // Add to trainer's templates
        trainer.addWorkoutTemplate(savedTemplate);
        trainerRepository.save(trainer);

        return savedTemplate;
    }

    @Operation(summary = "Follow trainer", description = "Allow a user to follow a specific trainer.")
    @PostMapping("/trainers/{trainerId}/followers/{username}")
    public String followTrainer(@PathVariable int trainerId, @PathVariable String username) {
        Trainer trainer = trainerRepository.findById(trainerId);
        if (trainer == null || !trainer.isActive()) {
            return "Trainer not found or not active";
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }

        // Check if user is already following this trainer
        if (trainer.getFollowers().contains(user)) {
            return "User is already following this trainer";
        }

        // Add user to trainer's followers
        trainer.addFollower(user);
        trainerRepository.save(trainer);

        return "Successfully followed trainer";
    }

    @Operation(summary = "Update trainer profile", description = "Modify a trainer's profile information.")
    @PutMapping("/trainers/{id}")
    public Trainer updateTrainer(@PathVariable int id, @RequestBody Trainer trainerDetails) {
        Trainer trainer = trainerRepository.findById(id);
        if (trainer == null) {
            return null;
        }

        // Update fields if provided
        if (trainerDetails.getSpecialization() != null) {
            trainer.setSpecialization(trainerDetails.getSpecialization());
        }

        if (trainerDetails.getBio() != null) {
            trainer.setBio(trainerDetails.getBio());
        }

        return trainerRepository.save(trainer);
    }

    @Operation(summary = "Update workout template", description = "Modify a workout template that belongs to a specific trainer.")
    @PutMapping("/trainers/{trainerId}/templates/{templateId}")
    public WorkoutTemplate updateWorkoutTemplate(
            @PathVariable int trainerId,
            @PathVariable int templateId,
            @RequestBody WorkoutTemplate templateDetails) {

        Trainer trainer = trainerRepository.findById(trainerId);
        if (trainer == null || !trainer.isActive()) {
            return null;
        }

        WorkoutTemplate template = workoutTemplateRepo.findById(templateId);
        if (template == null) {
            return null;
        }

        // Verify this template belongs to the trainer
        if (template.getTrainer() == null || template.getTrainer().getId() != trainer.getId()) {
            return null;
        }

        // Update fields
        if (templateDetails.getTemplateName() != null) {
            template.setTemplateName(templateDetails.getTemplateName());
        }

        if (templateDetails.getDescription() != null) {
            template.setDescription(templateDetails.getDescription());
        }

        return workoutTemplateRepo.save(template);
    }

    @Operation(summary = "Delete trainer", description = "Remove a trainer's profile from the system and handle related entities.")
    @DeleteMapping("/trainers/{id}")
    public String deleteTrainer(@PathVariable int id) {
        Trainer trainer = trainerRepository.findById(id);
        if (trainer == null) {
            return "Trainer not found";
        }

        // Remove all templates created by this trainer or set them to null
        List<WorkoutTemplate> templates = trainer.getTrainerTemplates();
        for (WorkoutTemplate template : templates) {
            template.setTrainer(null);
            workoutTemplateRepo.save(template);
        }

        // Remove trainer
        trainerRepository.delete(trainer);
        return "Trainer deleted successfully";
    }

    @Operation(summary = "Unfollow trainer", description = "Remove a user from a trainer's followers list.")
    @DeleteMapping("/trainers/{trainerId}/followers/{username}")
    public String unfollowTrainer(@PathVariable int trainerId, @PathVariable String username) {
        Trainer trainer = trainerRepository.findById(trainerId);
        User user = userRepository.findByUsername(username);

        if (trainer == null) {
            return "Trainer not found";
        }

        if (user == null) {
            return "User not found";
        }

        if (!trainer.getFollowers().contains(user)) {
            return "User is not following this trainer";
        }

        trainer.removeFollower(user);
        trainerRepository.save(trainer);

        return "Unfollowed successfully";
    }

    @Operation(summary = "Delete workout template", description = "Remove a workout template created by a specific trainer.")
    @DeleteMapping("/trainers/{trainerId}/templates/{templateId}")
    public String deleteWorkoutTemplate(@PathVariable int trainerId, @PathVariable int templateId) {
        Trainer trainer = trainerRepository.findById(trainerId);
        if (trainer == null) {
            return "Trainer not found";
        }

        WorkoutTemplate template = workoutTemplateRepo.findById(templateId);
        if (template == null) {
            return "Template not found";
        }

        // Verify this template belongs to the trainer
        if (template.getTrainer() == null || template.getTrainer().getId() != trainer.getId()) {
            return "This template doesn't belong to the trainer";
        }

        // Remove template from trainer
        trainer.removeWorkoutTemplate(template);
        trainerRepository.save(trainer);

        // Delete template
        workoutTemplateRepo.delete(template);

        return "Template deleted successfully";
    }

    @Operation(summary = "Check trainer status", description = "Verify if a user is registered as a trainer.")
    @GetMapping("/users/{username}/is-trainer")
    public boolean isTrainer(@PathVariable String username) {
        return trainerService.isTrainer(username);
    }

    @Operation(summary = "Register trainer", description = "Create both a user and trainer profile in a single operation.")
    @PostMapping("/register/trainer")
    public Trainer registerTrainer(@RequestBody TrainerRegistrationDTO registrationDTO) {
        try {
            // First check if username already exists
            if (userRepository.findByUsername(registrationDTO.getUsername()) != null) {
                return null; // Username already exists
            }

            // Check if email already exists
            if (userRepository.findByEmail(registrationDTO.getEmail()) != null) {
                return null; // Email already exists
            }

            // Create the user first
            User user = new User();
            user.setUsername(registrationDTO.getUsername());
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setEmail(registrationDTO.getEmail());
            user.setPassword(registrationDTO.getPassword());
            user.setTelephone(registrationDTO.getTelephone());

            // Save the user
            User savedUser = userRepository.save(user);

            // Now create the trainer profile
            Trainer trainer = new Trainer(savedUser);
            trainer.setSpecialization(registrationDTO.getSpecialization());
            trainer.setBio(registrationDTO.getBio());

            // Save and return the trainer
            return trainerRepository.save(trainer);
        } catch (Exception e) {
            System.err.println("Error creating trainer: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Data transfer object for combined user and trainer registration
     */
    public static class TrainerRegistrationDTO {
        // User fields
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String telephone;

        // Trainer fields
        private String specialization;
        private String bio;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTelephone() {
            return telephone;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }
    }
}