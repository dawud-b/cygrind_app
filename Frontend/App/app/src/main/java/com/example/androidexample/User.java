package com.example.androidexample;

import org.json.JSONObject;

/**
 * The User class represents a user profile in the system. It stores user-related information such as their personal details,
 * contact information, and other relevant attributes. This class provides constructors for creating a User from a JSON object
 * as well as getter and setter methods for accessing and modifying the user’s attributes.
 */
public class User {

    // User's personal and contact information
    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;

    // User's physical attributes
    private int friendsCount;
    private int height; // height in inches
    private int weight; // weight in pounds
    private int age;

    // User's account credentials
    private String password;

    /**
     * Default constructor for creating an empty User object.
     */
    public User() {};

    /**
     * Constructs a User object from the provided JSON object. This constructor is useful for parsing user data from a JSON response.
     *
     * @param jsonObject A JSON object containing the user's data.
     */
    public User(JSONObject jsonObject) {
        try {
            this.id = jsonObject.optLong("id", 0);  // Use optLong to avoid JSONException if field is missing
            this.username = jsonObject.optString("username", "");
            this.firstName = jsonObject.optString("firstName", "");
            this.lastName = jsonObject.optString("lastName", "");
            this.email = jsonObject.optString("email", "");
            this.telephone = jsonObject.optString("telephone", "");
            this.friendsCount = jsonObject.optInt("friendsCount", 0);
            this.height = jsonObject.optInt("height", 0);
            this.weight = jsonObject.optInt("weight", 0);
            this.age = jsonObject.optInt("age", 0);
            this.password = jsonObject.optString("password", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the username of the user.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the unique ID of the user.
     *
     * @return The ID of the user.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the user.
     *
     * @param id The ID to set for the user.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The username to set for the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the first name of the user.
     *
     * @return The first name of the user.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName The first name to set for the user.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the user.
     *
     * @return The last name of the user.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName The last name to set for the user.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the email address of the user.
     *
     * @return The email address of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The email address to set for the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the telephone number of the user.
     *
     * @return The telephone number of the user.
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Sets the telephone number of the user.
     *
     * @param telephone The telephone number to set for the user.
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Returns the height of the user in inches.
     *
     * @return The height of the user in inches.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the user in inches.
     *
     * @param height The height to set for the user (in inches).
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the weight of the user in pounds.
     *
     * @return The weight of the user in pounds.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Sets the weight of the user in pounds.
     *
     * @param weight The weight to set for the user (in pounds).
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Returns the age of the user.
     *
     * @return The age of the user.
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the age of the user.
     *
     * @param age The age to set for the user.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Returns the password of the user.
     *
     * @return The password of the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the user.
     *
     * @param password The password to set for the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the number of friends the user has.
     *
     * @return The number of friends of the user.
     */
    public int getFriendsCount() {
        return friendsCount;
    }

    /**
     * Sets the number of friends for the user.
     *
     * @param friendsCount The number of friends to set for the user.
     */
    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }
}
