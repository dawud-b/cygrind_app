package onetoone.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class DawudSystemTest {

    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        System.out.println("Test is running on port: " + port);

        // Get all users but don't try to delete them
        Response response = given()
                .when()
                .get("/users");

        System.out.println("Found " + response.asString().length() + " bytes of user data");
    }

    // Tests creating a new user and user with already existing email/username
    @Test
    public void NewUserCreationTest() {
        // Create user information
        JSONObject user1 = createUserJSON("Test1", "Test1@example.com", "Password1!");

        // call endpoint to create the user and get response
        Response response1 = given()
                .contentType(ContentType.JSON)
                .body(user1.toString())
                .when()
                .post("/users");

        // check status code
        assertEquals(200, response1.getStatusCode());
        System.out.println("User creation successful");

        // sign user in. Get user by username and check password.
        Response passcheckResponse = given()
                .contentType(ContentType.TEXT)
                .body("Password1!")
                .when()
                .put("/users/passcheck/Test1");

        assertEquals(200, passcheckResponse.getStatusCode());
        System.out.println("Password check successful");
    }

    @Test
    public void ExistingUsernameTest() {
        // First create the initial user
        JSONObject user1 = createUserJSON("Test1", "Test1@example.com", "Password1!");
        given()
                .contentType(ContentType.JSON)
                .body(user1.toString())
                .when()
                .post("/users");

        // Create user information with same username as another
        JSONObject user2 = createUserJSON("Test1", "Test2@example.com", "Password1!");

        // call endpoint to create the user and get response
        Response response2 = given()
                .contentType(ContentType.JSON)
                .body(user2.toString())
                .when()
                .post("/users");

        // check status code
        assertEquals(200, response2.getStatusCode());
        System.out.println("Duplicate username test successful");
    }

    @Test
    public void ExistingEmailTest() {
        // create first user
        JSONObject user1 = createUserJSON("Test1", "Test1@example.com", "Password1!");
        given()
                .contentType(ContentType.JSON)
                .body(user1.toString())
                .when()
                .post("/users");

        // Create user information with same email as another
        JSONObject user2 = createUserJSON("Test2", "Test1@example.com", "Password1!");

        // call endpoint to create the user and get response
        Response response = given()
                .contentType(ContentType.JSON)
                .body(user2.toString())
                .when()
                .post("/users");

        // check status code
        assertEquals(200, response.getStatusCode());

        System.out.println("Duplicate email test successful");
    }

    @Test
    public void getUserTest() {
        // Create test user first
        JSONObject user1 = createUserJSON("Test1", "Test1@example.com", "Password1!");
        given()
                .contentType(ContentType.JSON)
                .body(user1.toString())
                .when()
                .post("/users");

        // get user
        Response response = given()
                .when()
                .get("/users/Test1");

        assertEquals(200, response.getStatusCode());

        // Extract username from response
        String responseBody = response.asString();
        try {
            JSONObject user = new JSONObject(responseBody);
            String username = user.getString("username");
            assertEquals("Test1", username);
        } catch (Exception e) {
            System.out.println("Error parsing user response: " + e.getMessage());
        }

        System.out.println("Get user test successful");
    }

    @Test
    public void updateUserTest() {
        // create user
        JSONObject user1 = createUserJSON("Test1", "Test1@example.com", "Password1!");
        given()
                .contentType(ContentType.JSON)
                .body(user1.toString())
                .when()
                .post("/users");

        JSONObject updateUser = new JSONObject();
        try {
            updateUser.put("firstName", "newName");
            updateUser.put("lastName", "name");
            updateUser.put("height", 60);
            updateUser.put("weight", 200);
            updateUser.put("age", 22);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // update user
        Response putResponse = given()
                .contentType(ContentType.JSON)
                .body(updateUser.toString())
                .when()
                .put("/users/Test1");

        assertEquals(200, putResponse.getStatusCode());

        // check if updated the user properly
        Response getUserResponse = given()
                .when()
                .get("/users/Test1");

        assertEquals(200, getUserResponse.getStatusCode());

        try {
            JSONObject user = new JSONObject(getUserResponse.asString());
            String firstName = user.getString("firstName");
            assertEquals("newName", firstName);
        } catch (Exception e) {
            System.out.println("Error verifying user update: " + e.getMessage());
        }

        // get challengeSets by user
        Response getChallengesByUser = given().when().get("/challenges/user/Test1");
        assertEquals(200, getChallengesByUser.getStatusCode());
        int challengeId = getChallengesByUser.jsonPath().getInt("[0].id");

        // Check if challenge was completed
        Response getChallengesCompleted = given().when().get("/challenges/Test1/completed");
        assertEquals(200, getChallengesCompleted.getStatusCode());

        // get stages completed
        Response getChallengeStagesComplete = given().when().get("/challenges/completed/1");
        assertEquals(200, getChallengeStagesComplete.getStatusCode());

        // get stages incomplete
        Response getChallengeStagesIncomplete = given().when().get("/challenges/incomplete/1");
        assertEquals(200, getChallengeStagesIncomplete.getStatusCode());

        // manually set complete
        Response setChallengeComplete = given().when().put("/challenges/" + challengeId + "/complete");
        assertEquals(200, setChallengeComplete.getStatusCode());

        //check all challenges
        Response getAllChallenges = given().when().get("/challenges/user/Test1");
        assertEquals(200, getAllChallenges.getStatusCode());

        // create a challenge
        JSONObject newChallenge = new JSONObject();
        try {
            JSONObject user1Obj = new JSONObject();
            user1Obj.put("id", getUserResponse.jsonPath().getInt("id"));
            newChallenge.put("user", user1Obj);

            newChallenge.put("title", "title");

            // Create the list of challenges
            JSONArray challengesArray = new JSONArray();

            // Create individual challenge
            JSONObject challenge1 = new JSONObject();
            challenge1.put("title", "title");
            challenge1.put("description", "description");
            challenge1.put("points", 10);

            // Add to the array
            challengesArray.put(challenge1);

            // Add array to the main object
            newChallenge.put("challenges", challengesArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // send to user
        Response createChallenge = given().contentType(ContentType.JSON).body(newChallenge.toString())
                .when().post("/challenges");
        assertEquals(200, createChallenge.getStatusCode());

        // also send to all
        Response createChallengeForAll = given().contentType(ContentType.JSON).body(newChallenge.toString())
                .when().post("/challenges/assignToAll");
        assertEquals(200, createChallengeForAll.getStatusCode());

        // delete challenge
        Response deleteChallenge = given().when().delete("/challenges/" + createChallenge.jsonPath().getInt("id"));

        System.out.println("Update user and user update challenges test successful");
    }

    @Test
    public void FriendRequestTest() {
        // create users
        JSONObject user1 = createUserJSON("Test1", "Test1@example.com", "Password1!");
        JSONObject user2 = createUserJSON("Test2", "Test2@example.com", "Password1!");

        given()
                .contentType(ContentType.JSON)
                .body(user1.toString())
                .when()
                .post("/users");

        given()
                .contentType(ContentType.JSON)
                .body(user2.toString())
                .when()
                .post("/users");

        // Get users ID
        Response user1Response = given()
                .when()
                .get("/users/Test1");

        Long user1Id = user1Response.jsonPath().getLong("id");

        Response user2Response = given()
                .when()
                .get("/users/Test2");

        Long user2Id = user2Response.jsonPath().getLong("id");

        // Create friend request. Test1 sends to Test2
        JSONObject requestJson = new JSONObject();
        try {
            JSONObject user1Obj = new JSONObject();
            user1Obj.put("id", user1Id);
            requestJson.put("sender", user1Obj);

            JSONObject user2Obj = new JSONObject();
            user2Obj.put("id", user2Id);
            requestJson.put("receiver", user2Obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Response requestResponse = given()
                .contentType(ContentType.JSON)
                .body(requestJson.toString())
                .when()
                .post("/friends/request");

        assertEquals(200, requestResponse.getStatusCode());
        System.out.println("Friend request created successfully");

        // get Test2 requests
        Response getRequestResponse = given()
                .when()
                .get("/friends/requests/Test2/received");

        assertEquals(200, getRequestResponse.getStatusCode());

        try {
            JSONArray requests = new JSONArray(getRequestResponse.asString());
            long requestId = requests.getJSONObject(0).getLong("friendRequestId");
            String senderUsername = requests.getJSONObject(0).getJSONObject("sender").getString("username");
            assertEquals("Test1", senderUsername);

            // get Test1 sent requests
            Response getSentResponse = given()
                    .when()
                    .get("/friends/requests/Test1/sent");

            assertEquals(200, getSentResponse.getStatusCode());

            // accept the request
            Response acceptResponse = given()
                    .when()
                    .post("/friends/request/accept/" + requestId);

            assertEquals(200, acceptResponse.getStatusCode());
            System.out.println("Friend request accepted successfully");

            Response makeSureSentRequestAccepted = given().when().get("/friends/requests/Test1/sent/accepted");
            assertEquals(200, makeSureSentRequestAccepted.getStatusCode());

            Response makeSureReceivedRequestAccepted = given().when().get("/friends/requests/Test2/received/accepted");
            assertEquals(200, makeSureReceivedRequestAccepted.getStatusCode());

            // Verify friendship was created
            Response getFriendsResponse = given().when()
                    .get("/users/Test1/friends");
            assertEquals(200, getFriendsResponse.getStatusCode());

            JSONArray friends = new JSONArray(getFriendsResponse.asString());
            assertEquals(1, friends.length());

            //create chat
            JSONArray usersArray = new JSONArray();
            try {
                JSONObject user1Obj = new JSONObject();
                user1Obj.put("id", user1Id);

                JSONObject user2Obj = new JSONObject();
                user2Obj.put("id", user2Id);

                usersArray.put(user1Obj);
                usersArray.put(user2Obj);

            } catch (Exception e) {
                e.printStackTrace();
            }
            Response postChat = given().contentType(ContentType.JSON).body(usersArray.toString())
                    .when().post("/chatSession");
            assertEquals(200, postChat.getStatusCode());

            // get chat sesstion
            Response getChat = given().when()
                    .get("/users/Test1/chatSessions");
            assertEquals(200, getChat.getStatusCode());

            // get chat by id
            Response getChatId = given().when()
                    .get("/chatSessions" + getChat.getBody().jsonPath().getInt("[0].id"));
            assertEquals(200, getChatId.getStatusCode());

        } catch (Exception e) {
            System.out.println("Error in friend request testing: " + e.getMessage());
        }
    }

    @Test
    public void exerciseTemplatesTest() {
        System.out.println("Running comprehensive exercise template test");

        // 1. Save an exercise first
        JSONObject exerciseJSON = new JSONObject();
        try {
            exerciseJSON.put("name", "Rows");
            exerciseJSON.put("type", "back");
            exerciseJSON.put("muscle", "lats");
            exerciseJSON.put("difficulty", "intermediate");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Response createExerciseResponse = given()
                .contentType(ContentType.JSON)
                .body(exerciseJSON.toString())
                .when()
                .post("/workouts/database/exercises");

        assertEquals(201, createExerciseResponse.getStatusCode());

        Long exerciseId = createExerciseResponse.jsonPath().getLong("id");
        System.out.println("Created exercise with ID: " + exerciseId);

        // 2. Create test user
        JSONObject user1 = createUserJSON("Test1", "Test1@example.com", "Password1!");
        given()
                .contentType(ContentType.JSON)
                .body(user1.toString())
                .when()
                .post("/users");

        Response userResponse = given()
                .when()
                .get("/users/Test1");

        Long userId = userResponse.jsonPath().getLong("id");

        // 3. Create workout template
        JSONObject templateJson = new JSONObject();
        try {
            templateJson.put("templateName", "Test Template");
            templateJson.put("description", "Test template description");
            JSONObject userObj = new JSONObject();
            userObj.put("id", userId);
            templateJson.put("user", userObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Response templateResponse = given()
                .contentType(ContentType.JSON)
                .body(templateJson.toString())
                .when()
                .post("/templates");

        assertEquals(200, templateResponse.getStatusCode());

        Long templateId = templateResponse.jsonPath().getLong("id");
        System.out.println("Created template with ID: " + templateId);

        // 4. Verify template was created
        given()
                .when()
                .get("/templates/" + templateId)
                .then()
                .statusCode(200)
                .body("templateName", equalTo("Test Template"));

        // 5. Create an exercise template
        JSONObject exerciseTemplateJSON = new JSONObject();
        try {
            exerciseTemplateJSON.put("repCount", 10);
            exerciseTemplateJSON.put("setCount", 3);
            exerciseTemplateJSON.put("weight", 100);
            exerciseTemplateJSON.put("duration", 30);

            JSONObject exerciseObj = new JSONObject();
            exerciseObj.put("id", exerciseId);
            exerciseTemplateJSON.put("exercise", exerciseObj);

            JSONObject templateObj = new JSONObject();
            templateObj.put("id", templateId);
            exerciseTemplateJSON.put("workoutTemplate", templateObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 6. Add exercise template
        Response exerciseTemplateResponse = given()
                .contentType(ContentType.JSON)
                .body(exerciseTemplateJSON.toString())
                .when()
                .post("/exerciseTemplate");

        assertEquals(200, exerciseTemplateResponse.getStatusCode());

        Long exerciseTemplateId = exerciseTemplateResponse.jsonPath().getLong("id");
        System.out.println("Created exercise template with ID: " + exerciseTemplateId);

        // 7. Get exercise template by ID
        Response getTemplateResponse = given()
                .when()
                .get("/exerciseTemplate/" + exerciseTemplateId);

        getTemplateResponse
                .then()
                .statusCode(200)
                .body("repCount", equalTo(10))
                .body("setCount", equalTo(3))
                .body("weight", equalTo(100))
                .body("duration", equalTo(30));

        System.out.println("Get exercise template successful");

        // 8. Test negative case - get non-existent exercise template
        Response nonExistentResponse = given()
                .when()
                .get("/exerciseTemplate/99999");

        System.out.println("Non-existent template status: " + nonExistentResponse.getStatusCode());

        // 9. Update the exercise template
        JSONObject updateTemplate = new JSONObject();
        try {
            updateTemplate.put("weight", 150);
            updateTemplate.put("repCount", 5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 10. Update exercise template
        given()
                .contentType(ContentType.JSON)
                .body(updateTemplate.toString())
                .when()
                .put("/exerciseTemplate/" + exerciseTemplateId)
                .then()
                .statusCode(200);

        // 11. Check if updated correctly
        given()
                .when()
                .get("/exerciseTemplate/" + exerciseTemplateId)
                .then()
                .statusCode(200)
                .body("weight", equalTo(150))
                .body("repCount", equalTo(5));

        // 12. Test partial update (only update duration)
        JSONObject partialUpdateTemplate = new JSONObject();
        try {
            partialUpdateTemplate.put("duration", 45);
        } catch (Exception e) {
            e.printStackTrace();
        }

        given()
                .contentType(ContentType.JSON)
                .body(partialUpdateTemplate.toString())
                .when()
                .put("/exerciseTemplate/" + exerciseTemplateId)
                .then()
                .statusCode(200);

        // 13. Verify partial update worked
        given()
                .when()
                .get("/exerciseTemplate/" + exerciseTemplateId)
                .then()
                .statusCode(200)
                .body("weight", equalTo(150))  // Should remain unchanged
                .body("repCount", equalTo(5))  // Should remain unchanged
                .body("duration", equalTo(45)); // Should be updated

        // 14. Test getting all exercise templates for a workout template
        Response allExerciseTemplatesResponse = given()
                .when()
                .get("/templates/" + templateId + "/exercises");

        allExerciseTemplatesResponse.then().statusCode(200);
        List<Map<String, Object>> exerciseTemplates = allExerciseTemplatesResponse.jsonPath().getList("$");
        assertEquals(1, exerciseTemplates.size());
        System.out.println("Found " + exerciseTemplates.size() + " exercise templates for workout template");

        // 15. Create a second exercise template for the same workout
        JSONObject secondExerciseTemplateJSON = new JSONObject();
        try {
            secondExerciseTemplateJSON.put("repCount", 15);
            secondExerciseTemplateJSON.put("setCount", 4);
            secondExerciseTemplateJSON.put("weight", 80);
            secondExerciseTemplateJSON.put("duration", 60);

            JSONObject exerciseObj = new JSONObject();
            exerciseObj.put("id", exerciseId);
            secondExerciseTemplateJSON.put("exercise", exerciseObj);

            JSONObject templateObj = new JSONObject();
            templateObj.put("id", templateId);
            secondExerciseTemplateJSON.put("workoutTemplate", templateObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Response secondExerciseTemplateResponse = given()
                .contentType(ContentType.JSON)
                .body(secondExerciseTemplateJSON.toString())
                .when()
                .post("/exerciseTemplate");

        Long secondExerciseTemplateId = secondExerciseTemplateResponse.jsonPath().getLong("id");
        System.out.println("Created second exercise template with ID: " + secondExerciseTemplateId);

        // 16. Verify we now have two exercise templates
        Response verifyCountResponse = given()
                .when()
                .get("/templates/" + templateId + "/exercises");

        List<Map<String, Object>> updatedExerciseTemplates = verifyCountResponse.jsonPath().getList("$");
        assertEquals(2, updatedExerciseTemplates.size());

        // 17. Test error case - try to create an exercise template with non-existent workout template
        JSONObject invalidWorkoutTemplateJSON = new JSONObject();
        try {
            invalidWorkoutTemplateJSON.put("repCount", 10);
            invalidWorkoutTemplateJSON.put("setCount", 3);
            invalidWorkoutTemplateJSON.put("weight", 100);
            invalidWorkoutTemplateJSON.put("duration", 30);

            JSONObject exerciseObj = new JSONObject();
            exerciseObj.put("id", exerciseId);
            invalidWorkoutTemplateJSON.put("exercise", exerciseObj);

            JSONObject templateObj = new JSONObject();
            templateObj.put("id", 99999); // Non-existent ID
            invalidWorkoutTemplateJSON.put("workoutTemplate", templateObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Response invalidTemplateResponse = given()
                .contentType(ContentType.JSON)
                .body(invalidWorkoutTemplateJSON.toString())
                .when()
                .post("/exerciseTemplate");

        System.out.println("Invalid workout template response: " + invalidTemplateResponse.asString());


        // Add workout in calendar
        JSONObject workoutDate = new JSONObject();
        try {
            JSONObject userObj = new JSONObject();
            userObj.put("id", userId)  ;
            workoutDate.put("user", userObj);

            JSONObject templateObj = new JSONObject();
            templateObj.put("id", templateId);
            workoutDate.put("workout", templateObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Response postCalendar = given().contentType(ContentType.JSON).body(workoutDate.toString())
                .when().post("/Test1/calendar");
        assertEquals(200, postCalendar.getStatusCode());

        // get calendar
        Response getCalendar = given().when().get("/Test1/calendar");
        assertEquals(200, getCalendar.getStatusCode());

        // check if its overdue
        Response getOverdueCalendar = given().when().get("/Test1/calendar/overdue");
        assertEquals(200, getOverdueCalendar.getStatusCode());

        // check if its complete by workout
        Response getWorkoutCalendar = given().when().get(templateId + "/completed");
        assertEquals(200, getWorkoutCalendar.getStatusCode());

        // check if its overdue
        Response getWorkoutCalendarPlanned = given().when().get(templateId + "/upcoming");
        assertEquals(200, getWorkoutCalendarPlanned.getStatusCode());

        // Complete workout
        Response completeCalendar = given().when().put("/Test1/calendar/1/complete");
        assertEquals(200, completeCalendar.getStatusCode());

        // check complete workouts
        Response getCompletedCalendar = given().when().get("/Test1/calendar/completed");
        assertEquals(200, getCompletedCalendar.getStatusCode());

        // not complete
        Response nonComplete = given().when().put("/Test1/calendar/1/notComplete");
        assertEquals(200, nonComplete.getStatusCode());

        // delete
        Response deleteDate = given().when().delete("/Test1/calendar/1");
        assertEquals(200, deleteDate.getStatusCode());

        // 18. Test deleting an exercise template
        Response deleteResponse = given()
                .when()
                .delete("/exerciseTemplate/" + exerciseTemplateId);

        assertEquals(200, deleteResponse.getStatusCode());
        assertEquals("Deleted.", deleteResponse.asString());
        System.out.println("Successfully deleted exercise template");

        // 19. Verify exercise template was deleted
        Response verifyDeletedResponse = given()
                .when()
                .get("/exerciseTemplate/" + exerciseTemplateId);

        System.out.println("Get deleted template status: " + verifyDeletedResponse.getStatusCode());

        // 20. Test deleting non-existent exercise template
        Response deleteNonExistentResponse = given()
                .when()
                .delete("/exerciseTemplate/99999");

        assertEquals(200, deleteNonExistentResponse.getStatusCode());
        assertEquals("ExerciseTemplate not found.", deleteNonExistentResponse.asString());

        // Clean up - delete the remaining exercise template and workout template
        given()
                .when()
                .delete("/exerciseTemplate/" + secondExerciseTemplateId);

        given()
                .when()
                .delete("/templates/" + templateId);

        given()
                .when()
                .delete("/workouts/database/exercises/" + exerciseId);

        System.out.println("Exercise template test completed successfully");
    }

    @After
    public void cleanUp() {
        // Use try-catch blocks to prevent test failures during cleanup
        try {
            given().when().delete("/users/Test1");
        } catch (Exception e) {
            System.out.println("Error deleting Test1: " + e.getMessage());
        }

        try {
            given().when().delete("/users/Test2");
        } catch (Exception e) {
            System.out.println("Error deleting Test2: " + e.getMessage());
        }

        try {
            given().when().delete("/users/Test3");
        } catch (Exception e) {
            System.out.println("Error deleting Test3: " + e.getMessage());
        }
    }

    private JSONObject createUserJSON(String username, String email, String password) {
        JSONObject userJson = new JSONObject();
        try {
            userJson.put("username", username);
            userJson.put("firstName", "test");
            userJson.put("lastName", "test");
            userJson.put("email", email);
            userJson.put("password", password);
            userJson.put("telephone", "5555555555");
            userJson.put("userRole", "USER");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userJson;
    }
}