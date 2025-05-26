package coms309.people;

import java.util.HashMap;

/**
 * Provides the Definition/Structure for user information
 *
 * @author Joseph Metzen
 */

public class User {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private int friendsCount;

    HashMap<String, User> friendsList = new HashMap<>();



    public User(String username, String firstName, String lastName, String email, String telephone){
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telephone = telephone;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String firstName) {
        this.username = username;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String address) {
        this.email = email;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getFriendsCount() {return this.friendsCount; }

    public void incrementFriendsCount() { this.friendsCount++; }

    public void decrementFriendsCount() { this.friendsCount--; }

    @Override
    public String toString() {
        return username + " "
                + firstName + " "
                + lastName + " "
                + email + " "
                + telephone;
    }

}
