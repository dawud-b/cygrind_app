package onetoone.Trainer;

import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for trainer-related business logic.
 * Handles creation and management of trainer profiles.
 */
@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Checks if a user has trainer status
     *
     * @param username The username to check
     * @return true if the user is an active trainer, false otherwise
     */
    public boolean isTrainer(String username) {
        // First find the user
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }

        // Then check if they have an active trainer profile
        Trainer trainer = trainerRepository.findByUser(user);
        return trainer != null && trainer.isActive();
    }

    /**
     * Find a trainer by their username
     *
     * @param username The username to look up
     * @return The Trainer object or null if not found
     */
    public Trainer getTrainerByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        return trainerRepository.findByUser(user);
    }

    /**
     * Creates a new trainer profile for an existing user
     *
     * @param username The username of the existing user
     * @param specialization The trainer's area of expertise
     * @param bio Brief description of the trainer
     * @return The created Trainer or null if user doesn't exist
     */
    @Transactional
    public Trainer createTrainerProfile(String username, String specialization, String bio) {
        // Find the user
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        // Check if trainer profile already exists for this user
        Trainer existingTrainer = trainerRepository.findByUser(user);
        if (existingTrainer != null) {
            return null;
        }

        // Create new trainer profile
        Trainer trainer = new Trainer(user);
        trainer.setSpecialization(specialization);
        trainer.setBio(bio);

        return trainerRepository.save(trainer);
    }
}