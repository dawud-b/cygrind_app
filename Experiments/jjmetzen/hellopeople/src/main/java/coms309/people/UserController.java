package coms309.people;

import org.springframework.web.bind.annotation.*;


import java.util.HashMap;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre, Joseph Metzen
 */

@RestController
public class UserController {

    // Note that there is only ONE instance of UserController in
    // Springboot system.

    // Note that there is only ONE instance of UserController in
    // Springboot system.
    HashMap<String, User> userList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input.
    // Springboot automatically converts the list to JSON format
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method


    @GetMapping("/users")
    public  HashMap<String,User> getAllUsers() {
        return userList;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // in this case because of @ResponseBody
    // Note: To CREATE we use POST method


    @PostMapping("/users")
    public  String createUser(@RequestBody User user) {
        System.out.println(user);
        userList.put(user.getUsername(), user);
        return "New User Saved: "+ user.getUsername() + " " + user.getEmail();
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // in this case because of @ResponseBody
    // Note: To READ we use GET method


    @GetMapping("/users/{username}")
    public User getUser(@PathVariable String username) {
        User u = userList.get(username);
        return u;
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // in this case because of @ResponseBody
    // Note: To UPDATE we use PUT method

    @PutMapping("/users/{username}")
    public User updateUser(@PathVariable String username, @RequestBody User u) {
        userList.replace(username, u);
        return userList.get(username);
    }


    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // in this case because of @ResponseBody
    // Note: To DELETE we use delete method

    @DeleteMapping("/users/{username}")
    public HashMap<String, User> deleteUser(@PathVariable String username) {
        userList.remove(username);
        return userList;
    }

    @PutMapping("/{username}/friends/add")
    public String addFriend(@PathVariable String username, @RequestBody String friend_username) {
        // Check if the user is trying to add themselves as a friend
        if (username.equals(friend_username)) {
            //throw new RuntimeException("Error: Stop adding yourself");
           return username + " stop adding yourself";

        }

        User my_user = userList.get(username);
        User friend = userList.get(friend_username);

        // Check if the friend is already in the user's friend list
        if (my_user.friendsList.containsKey(friend_username)) {
            throw new RuntimeException("Error: " + friend_username + " is already your friend.");
        }

        // Add the friend to the user's friend list
        my_user.friendsList.put(friend_username, friend);
        my_user.incrementFriendsCount();

        // Add the user to the friend's friend list
        friend.friendsList.put(username, my_user);
        friend.incrementFriendsCount();

        return "New Friend Added: " + friend_username;
    }




    // DELETE request will remove the RequestBody user as the path users friend and vise versa.
    @DeleteMapping("/{username}/friends/remove")
    public  String removeFriend(@PathVariable String username, @RequestBody String friend_username) {
        System.out.println(friend_username);
        User my_user = userList.get(username);
        User friend = userList.get(friend_username);
        my_user.friendsList.remove(friend_username);
        my_user.decrementFriendsCount();
        friend.friendsList.remove(username);
        friend.decrementFriendsCount();
        return "Friend Removed: "+ friend_username;
    }

    // GET request will return the list of friends for the passed user
    @GetMapping("/{username}/friends")
    public  HashMap<String,User> getFriends(@PathVariable String username) {
        User my_user = userList.get(username);
        return my_user.friendsList;
    }

}

