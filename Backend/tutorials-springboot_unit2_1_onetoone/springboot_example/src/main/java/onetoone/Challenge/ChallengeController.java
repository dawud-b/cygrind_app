package onetoone.Challenge;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Tag(name = "Challenge Controller", description = "Endpoints for creating and updating Challenges")
@RestController
public class ChallengeController {

    @Autowired
    ChallengeRepository challengeRepository;
    @Autowired
    ChallengeSetRepo challengeSetRepo;
    @Autowired
    private UserRepository userRepository;


    @Operation(summary = "Get challenge sets by User", description = "Returns all challenge sets assigned to the user in the path.")
    @GetMapping("/challenges/user/{username}")
    public List<ChallengeSet> getChallengesByUser(@Parameter(description = "Username of the user of the challenges to return") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return null;}
        return challengeSetRepo.findByUser(user);
    }

    @Operation(summary = "Get completed challenges by User", description = "Returns all completed challenges assigned to the user in the path.")
    @GetMapping("/challenges/{username}/completed")
    public List<Challenge> getCompletedChallengesByUser(@Parameter(description = "Username of the user of the challenges to return") @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {return null;}
        List<Challenge> returnList = new ArrayList<>();
        List<ChallengeSet> challengeSets = challengeSetRepo.findByUser(user);
        for (ChallengeSet challengeSet : challengeSets) {
            List<Challenge> challenges = challengeSet.getChallenges();
            for (Challenge challenge : challenges) {
                if (challenge.isCompleted()) {
                    returnList.add(challenge);
                }
            }
        }
        return returnList;
    }

    @Operation(summary = "Get completed challenges by set", description = "Returns all completed challenges in the challenge set assigned to the path id.")
    @GetMapping("/challenges/completed/{id}")
    public List<Challenge> getCompletedChallengesBySet(@Parameter(description = "ID of the challengeSet of the challenges to return") @PathVariable long id) {
        ChallengeSet challenges = challengeSetRepo.findById(id);
        if (challenges == null) {return null;}
        return challenges.getByChallengesCompleteness(true);
    }

    @Operation(summary = "Get incomplete challenges in challenge set", description = "Returns all incomplete challenges within the challenge set in the path.")
    @GetMapping("/challenges/incomplete/{id}")
    public List<Challenge> getIncompleteChallengesBySet(@Parameter(description = "ID of the challengeSet of the challenges to return") @PathVariable long id) {
        ChallengeSet challenges = challengeSetRepo.findById(id);
        if (challenges == null) {return null;}
        return challenges.getByChallengesCompleteness(false);
    }

    @Operation(summary = "Get Challenge Set by ID", description = "Returns the Challenge object with the path id.")
    @GetMapping("/challenges/{id}")
    public ChallengeSet getChallengeSetById(@Parameter(description = "ID of the challenge set to return") @PathVariable Long id) {
        return challengeSetRepo.findById(id).orElse(null);
    }

    @Operation(summary = "Create a challenge set", description = "Creates and Returns a new ChallengeSet with the details given in the body. Pass user id.")
    @PostMapping("/challenges")
    public ChallengeSet createChallengeSet(@RequestBody ChallengeSet challenges) {
        User user = userRepository.findById(challenges.getUser().getId());
        if (user == null) {return null;}

        ChallengeSet newSet = new ChallengeSet();
        for (Challenge challenge : challenges.getChallenges()) {
            if (challenge.getPoints() <= 0) {return null;}

            Challenge newChallenge = new Challenge(challenge.getTitle(), challenge.getDescription(), challenge.getPoints());
            newChallenge.setChallengeSet(newSet);
            newSet.addChallenge(newChallenge);
        }
        newSet.setUser(user);
        newSet.setTitle(challenges.getTitle());
        userRepository.save(user);
        return challengeSetRepo.save(newSet);
    }

    @Operation(summary = "Create a challenge set for all users", description = "Creates a new challenge set with details from the body and assigns it to all users. Ignores if the body has a user assigned.")
    @PostMapping("/challenges/assignToAll")
    public String createChallengeSetForAllUsers(@RequestBody ChallengeSet challenges) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            ChallengeSet newSet = new ChallengeSet();
            for (Challenge challenge : challenges.getChallenges()) {
                if (challenge.getPoints() <= 0) {
                    return "Failed. Reward points must be greater than 0.";
                }
                Challenge newChallenge = new Challenge(challenge.getTitle(), challenge.getDescription(), challenge.getPoints());
                newChallenge.setChallengeSet(newSet);
                newSet.addChallenge(newChallenge);
            }
            newSet.setUser(user);
            newSet.setTitle(challenges.getTitle());
            challengeSetRepo.save(newSet);
            userRepository.save(user);
        }
        return "Challenge Set created successfully";
    }

    @Operation(summary = "Set a challenge stage as complete", description = "Sets the next stage of a challenge set as complete and adds its points to the user.")
    @PutMapping("/challenges/{id}/complete")
    public ChallengeSet setChallengeAsComplete(@Parameter(description = "ID of challenge set to mark next stage complete") @PathVariable long id) {
        ChallengeSet challengeSet = challengeSetRepo.findById(id);
        if (challengeSet == null) {return null;}

        // if no challenges in set return
        if (challengeSet.getChallenges() == null) {return null;}

        // if all stages complete, just return the challengeSet
        if (challengeSet.getProgress() == 1) {return challengeSet;}

        Challenge challenge = challengeSet.getChallengeByStage(challengeSet.getChallengesCompleted());
        challenge.setCompleted();
        challenge.setCompletedDate(getNow());
        challengeRepository.save(challenge);

        User user = challengeSet.getUser();
        user.addPoints(challenge.getPoints());
        userRepository.save(user);

        challengeSet.setChallengesCompleted();
        return challengeSetRepo.save(challengeSet);
    }

    @Operation(summary = "Delete Challege Set", description = "Deletes the challenge set and challenges of the set assigned to the path id.")
    @DeleteMapping("/challenges/{id}")
    public void deleteChallengeSet(@Parameter(description = "ID of the challenge set to delete") @PathVariable long id) {
        ChallengeSet challengeSet = challengeSetRepo.findById(id);
        if (challengeSet == null) {
            return;
        }
        for (Challenge challenge : challengeSet.getChallenges()) {
            challengeRepository.delete(challenge);
        }
        challengeSetRepo.delete(challengeSet);
    }

    private Date getNow(){
        LocalDateTime now = LocalDateTime.now();
        Calendar calendar = Calendar.getInstance();
        calendar.set(now.getYear(), now.getMonthValue()-1, now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond());
        return calendar.getTime();
    }
}
