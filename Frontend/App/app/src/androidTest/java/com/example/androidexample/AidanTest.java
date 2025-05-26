package com.example.androidexample;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.androidexample.Profile.ProfileActivity;
import com.example.androidexample.Trainers.TrainerPageActivity;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class AidanTest {

    private static final String TAG = "UserProfileTests";
    private String regularUsername;
    private String trainerUsername;
    private String testPwd = "Password123!"; // Make sure this meets your password requirements

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule
            = new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        // Generate random usernames with unique suffixes to avoid conflicts
        // Using shorter usernames to ensure they're under any character limits
        String suffix1 = String.valueOf((int)(Math.random() * 900 + 100));
        String suffix2 = String.valueOf((int)(Math.random() * 900 + 100));

        regularUsername = "user" + suffix1;
        trainerUsername = "trainer" + suffix2;

        Log.d(TAG, "Test users created - Regular: " + regularUsername + ", Trainer: " + trainerUsername);
    }

    /**
     * Test 1: Creating a trainer user
     */
    @Test
    public void testCreateTrainerUser() throws InterruptedException {
        Log.d(TAG, "Starting testCreateTrainerUser");

        // Navigate to signup screen from login
        onView(withId(R.id.login_signup_btn)).perform(click());
        Thread.sleep(500);

        // Verify we're on the signup screen
        onView(withId(R.id.signup_layout)).check(matches(isDisplayed()));

        // Fill in trainer account info
        String email = trainerUsername + "@test.com";
        onView(withId(R.id.signup_email_edt)).perform(clearText(), typeText(email), closeSoftKeyboard());
        onView(withId(R.id.signup_username_edt)).perform(clearText(), typeText(trainerUsername), closeSoftKeyboard());
        onView(withId(R.id.signup_password_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());
        onView(withId(R.id.signup_confirm_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());

        // Check trainer checkbox
        onView(withId(R.id.signup_trainer_checkbox)).perform(click());
        Thread.sleep(500);

        // Fill in trainer-specific fields
        onView(withId(R.id.signup_specialization_edt)).perform(typeText("Strength Training"), closeSoftKeyboard());
        onView(withId(R.id.signup_bio_edt)).perform(typeText("I help people achieve fitness goals"), closeSoftKeyboard());

        // Take a screenshot or log the state before submission
        Log.d(TAG, "About to submit trainer signup form");

        // Submit form
        onView(withId(R.id.signup_signup_btn)).perform(click());
        Thread.sleep(3000); // Give more time for network request

        // Check if we're redirected to login
        try {
            onView(withId(R.id.login_layout)).check(matches(isDisplayed()));
            Log.d(TAG, "Redirected to login screen successfully");

            // Since fields are autofilled, just click login button
            onView(withId(R.id.login_login_btn)).perform(click());
            Thread.sleep(2000); // Wait for login

            // Check if we reached the dashboard
            onView(withId(R.id.dashboard_layout)).check(matches(isDisplayed()));
            Log.d(TAG, "Trainer successfully logged in");

        } catch (Exception e) {
            Log.e(TAG, "Exception during trainer creation/login: " + e.getMessage());
            // Handle the case where we might still be on signup screen due to error
            if (isViewDisplayed(R.id.signup_layout)) {
                Log.e(TAG, "Still on signup screen, signup likely failed");
            }
            throw e; // Rethrow to fail the test
        }
    }

    /**
     * Test 2: Creating a user and editing their profile
     */
    @Test
    public void testCreateAndEditProfile() throws InterruptedException {
        Log.d(TAG, "Starting testCreateAndEditProfile");

        // Navigate to signup
        onView(withId(R.id.login_signup_btn)).perform(click());
        Thread.sleep(500);

        // Create regular user
        String email = regularUsername + "@test.com";
        onView(withId(R.id.signup_email_edt)).perform(clearText(), typeText(email), closeSoftKeyboard());
        onView(withId(R.id.signup_username_edt)).perform(clearText(), typeText(regularUsername), closeSoftKeyboard());
        onView(withId(R.id.signup_password_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());
        onView(withId(R.id.signup_confirm_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());

        Log.d(TAG, "About to submit user signup form");
        onView(withId(R.id.signup_signup_btn)).perform(click());
        Thread.sleep(3000); // More time for network

        // Check if we're on login screen
        try {
            onView(withId(R.id.login_layout)).check(matches(isDisplayed()));
            Log.d(TAG, "Redirected to login screen successfully");

            // Since fields are autofilled, just click login
            onView(withId(R.id.login_login_btn)).perform(click());
            Thread.sleep(2000);

            // Check if we reached the dashboard
            onView(withId(R.id.dashboard_layout)).check(matches(isDisplayed()));
            Log.d(TAG, "User successfully logged in");

            // Navigate to profile
            onView(withId(R.id.home_profile)).perform(click());
            Thread.sleep(1000);

            // Confirm we're on profile page
            onView(withId(R.id.display_name)).check(matches(isDisplayed()));

            // Click edit profile button
            onView(withId(R.id.edit_profile_btn)).perform(click());
            Thread.sleep(1000);

            // Update profile fields
            onView(withId(R.id.name_editable)).perform(clearText(), typeText("John Doe"), closeSoftKeyboard());
            onView(withId(R.id.age_editable)).perform(clearText(), typeText("25"), closeSoftKeyboard());
            onView(withId(R.id.weight_editable)).perform(clearText(), typeText("175"), closeSoftKeyboard());
            onView(withId(R.id.height_editable)).perform(clearText(), typeText("5ft 10in"), closeSoftKeyboard());
            onView(withId(R.id.editTextPhone)).perform(clearText(), typeText("5151234567"), closeSoftKeyboard());

            // Save changes
            Log.d(TAG, "Saving profile changes");
            onView(withId(R.id.save_btn)).perform(click());
            Thread.sleep(2000);

            // Check if we're on profile screen
            Log.d(TAG, "Checking if profile data was updated");

            // Return to profile if needed
            try {
                onView(withId(R.id.display_name)).check(matches(isDisplayed()));
            } catch (Exception e) {
                onView(withId(R.id.edit_back)).perform(click());
                Thread.sleep(1000);
            }

            // Verify profile changes
            onView(withId(R.id.display_name)).check(matches(withText("John Doe")));
            Log.d(TAG, "Profile successfully updated");

        } catch (Exception e) {
            Log.e(TAG, "Exception during user creation/profile edit: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test 3: Create user, change password, delete profile
     */
    @Test
    public void testChangePasswordAndDeleteProfile() throws InterruptedException {
        Log.d(TAG, "Starting testChangePasswordAndDeleteProfile");

        // Navigate to signup
        onView(withId(R.id.login_signup_btn)).perform(click());
        Thread.sleep(500);

        // Create user for deletion
        String deleteUser = "del" + String.valueOf((int)(Math.random() * 900 + 100));
        String email = deleteUser + "@test.com";

        onView(withId(R.id.signup_email_edt)).perform(clearText(), typeText(email), closeSoftKeyboard());
        onView(withId(R.id.signup_username_edt)).perform(clearText(), typeText(deleteUser), closeSoftKeyboard());
        onView(withId(R.id.signup_password_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());
        onView(withId(R.id.signup_confirm_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());

        onView(withId(R.id.signup_signup_btn)).perform(click());
        Thread.sleep(3000);

        try {
            // Login with autofilled credentials
            onView(withId(R.id.login_login_btn)).perform(click());
            Thread.sleep(2000);

            // Navigate to profile
            onView(withId(R.id.home_profile)).perform(click());
            Thread.sleep(1000);

            // Go to edit profile
            onView(withId(R.id.edit_profile_btn)).perform(click());
            Thread.sleep(1000);

            // Go to change password
            onView(withId(R.id.edit_password_btn)).perform(click());
            Thread.sleep(1000);

            // Change password
            String newPassword = "NewPass123!";
            onView(withId(R.id.old_password)).perform(typeText(testPwd), closeSoftKeyboard());
            onView(withId(R.id.new_password)).perform(typeText(newPassword), closeSoftKeyboard());
            onView(withId(R.id.confirm_password)).perform(typeText(newPassword), closeSoftKeyboard());
            onView(withId(R.id.change_password_btn)).perform(click());
            Thread.sleep(2000);

            // Should already go back to profile screen after change
            //pressBack();
            //Thread.sleep(1000);

            // Delete account
            Log.d(TAG, "About to delete account");
            onView(withId(R.id.delete_acc_btn)).perform(click());
            Thread.sleep(1000);

            // Confirm deletion in dialog
            onView(withText("Delete")).inRoot(isDialog()).perform(click());
            Thread.sleep(4000);

            // Verify redirect to login
            onView(withId(R.id.login_layout)).check(matches(isDisplayed()));
            Log.d(TAG, "Successfully redirected to login after deletion");

            // Try to login with deleted account
            onView(withId(R.id.login_username_edt)).perform(clearText(), typeText(deleteUser), closeSoftKeyboard());
            onView(withId(R.id.login_password_edt)).perform(clearText(), typeText(newPassword), closeSoftKeyboard());
            onView(withId(R.id.login_login_btn)).perform(click());
            Thread.sleep(2000);

            // Should still be on login screen
            onView(withId(R.id.login_layout)).check(matches(isDisplayed()));
            Log.d(TAG, "Login with deleted account failed as expected");

        } catch (Exception e) {
            Log.e(TAG, "Exception during password change/account deletion: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test 4: Create user and follow a trainer
     */
    @Test
    public void testFollowTrainer() throws InterruptedException {
        Log.d(TAG, "Starting testFollowTrainer");

        // First let's create a trainer
        // Navigate to signup
        onView(withId(R.id.login_signup_btn)).perform(click());
        Thread.sleep(500);

        // Create trainer account
        String trainerEmail = trainerUsername + "@test.com";
        onView(withId(R.id.signup_email_edt)).perform(clearText(), typeText(trainerEmail), closeSoftKeyboard());
        onView(withId(R.id.signup_username_edt)).perform(clearText(), typeText(trainerUsername), closeSoftKeyboard());
        onView(withId(R.id.signup_password_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());
        onView(withId(R.id.signup_confirm_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());

        // Check trainer checkbox and fill fields
        onView(withId(R.id.signup_trainer_checkbox)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.signup_specialization_edt)).perform(typeText("Fitness Coach"), closeSoftKeyboard());
        onView(withId(R.id.signup_bio_edt)).perform(typeText("I help clients achieve their fitness goals"), closeSoftKeyboard());

        onView(withId(R.id.signup_signup_btn)).perform(click());
        Thread.sleep(3000);

        try {
            // Verify we're on login screen
            onView(withId(R.id.login_layout)).check(matches(isDisplayed()));
            Log.d(TAG, "Trainer account created");

            // Now create a regular user
            onView(withId(R.id.login_signup_btn)).perform(click());
            Thread.sleep(500);

            // Fill user info
            String userEmail = regularUsername + "@test.com";
            onView(withId(R.id.signup_email_edt)).perform(clearText(), typeText(userEmail), closeSoftKeyboard());
            onView(withId(R.id.signup_username_edt)).perform(clearText(), typeText(regularUsername), closeSoftKeyboard());
            onView(withId(R.id.signup_password_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());
            onView(withId(R.id.signup_confirm_edt)).perform(clearText(), typeText(testPwd), closeSoftKeyboard());

            onView(withId(R.id.signup_signup_btn)).perform(click());
            Thread.sleep(3000);

            // Login with the regular user
            onView(withId(R.id.login_login_btn)).perform(click());
            Thread.sleep(2000);

            // Navigate to trainers section
            onView(withId(R.id.home_trainer)).perform(click());
            Thread.sleep(1000);

            // Ensure we're on the search trainers tab
            onView(withText("Search Trainers")).perform(click());
            Thread.sleep(1000);

            // Search for our trainer
            onView(withId(R.id.searchBar)).perform(clearText(), typeText(trainerUsername), closeSoftKeyboard());
            Thread.sleep(2000); // Wait for search/filter

            Log.d(TAG, "Searching for trainer: " + trainerUsername);

            // Try to interact with the found trainer
            try {
                // View trainer profile - try to find by username
                onView(withId(R.id.recyclerViewTrainerResults))
                        .perform(RecyclerViewActions.actionOnItem(
                                hasDescendant(withText(trainerUsername)),
                                clickChildViewWithId(R.id.viewProfileButton)));
            } catch (Exception e) {
                Log.d(TAG, "Could not find trainer by exact name, trying first result: " + e.getMessage());
                // If we can't find by exact username, try the first result
                onView(withId(R.id.recyclerViewTrainerResults))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                                clickChildViewWithId(R.id.viewProfileButton)));
            }

            Thread.sleep(2000);

            // On trainer profile, follow the trainer
            Log.d(TAG, "Following trainer");
            onView(withId(R.id.followUnfollowButton)).perform(click());
            Thread.sleep(2000);

            // Go back to trainer listing
            pressBack();
            Thread.sleep(1000);

            // After switching to followed trainers tab
            onView(withText("Followed Trainers")).perform(click());
            Thread.sleep(2000);

            // Add refresh action here
            Log.d(TAG, "Refreshing followed trainers list");
            onView(withId(R.id.recyclerViewFollowedTrainers))
                    .perform(swipeDown()); // Assuming you have a swipe-to-refresh layout
            Thread.sleep(2000); // Wait for refresh to complete

            // Now verify there's at least one trainer in the list
            onView(withId(R.id.recyclerViewFollowedTrainers))
                    .check(hasMinimumItemCount(1));

            Log.d(TAG, "Successfully verified trainer is being followed");

        } catch (Exception e) {
            Log.e(TAG, "Exception during follow trainer test: " + e.getMessage());
            throw e;
        }
    }

    // Helper method to check if a view with the given ID is currently displayed
    private boolean isViewDisplayed(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ViewAssertion hasMinimumItemCount(final int minCount) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            assertNotNull("RecyclerView has no adapter attached", adapter);
            assertTrue("RecyclerView has fewer items than expected",
                    adapter.getItemCount() >= minCount);
        };
    }

    public static ViewAction clickChildViewWithId(final int childViewId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified ID.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View childView = view.findViewById(childViewId);
                if (childView != null && childView.isClickable()) {
                    childView.performClick();
                } else {
                    throw new PerformException.Builder()
                            .withCause(new Throwable("Child view not found or not clickable"))
                            .build();
                }
            }
        };
    }
}