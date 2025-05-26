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

import java.util.UUID;


@RunWith(AndroidJUnit4.class)
public class LukeTest {

    private String userAname;
    private String userBname;
    private String testPwd = "password123!";

    @Rule
    public ActivityScenarioRule<SignupActivity> activityScenarioRule
            = new ActivityScenarioRule<>(SignupActivity.class);

    /*
    @Test
    public void testNavigateToDashboard() {
        // click on the Dashboard navigation item
        onView(withId(R.id.navigation_dashboard))
                .perform(click());

        // check if the text is correct (same as in DashboardViewModel)
        ViewInteraction dashboard = onView(withText("This is dashboard fragment")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
     */

    /*
    Creates 2 new users and stores their names for use in other tests.
     */
    @Before
    public void setUp() throws InterruptedException {
        ActivityScenario.launch(LoginActivity.class);

        String suffix1 = String.valueOf((int)(Math.random() * 9000 + 1000));
        String suffix2 = String.valueOf((int)(Math.random() * 9000 + 1000));

        userAname = "userA" + suffix1;
        userBname = "userB" + suffix2;

        onView(withId(R.id.login_signup_btn)).perform(click());
        signUp(userAname, userAname + "@test.com", testPwd);
        Thread.sleep(500);

        onView(withId(R.id.login_layout))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_signup_btn)).perform(click());
        signUp(userBname, userBname + "@test.com", testPwd);
    }

    @Test
    public void testCreatingWorkoutTemplate() throws InterruptedException {
        // start in login
        ActivityScenario.launch(LoginActivity.class);
        login(userAname, testPwd);
        onView(withId(R.id.dashboard_layout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.home_workout)).perform(click());
        onView(withId(R.id.workout_templates_layout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.add_template_btn)).perform(click());
        Thread.sleep(250); // wait for prompt to show up

        // Enter random data into the EditText field
        String randomTemplateName = "Template " + UUID.randomUUID().toString().substring(0, 8);
        onView(isAssignableFrom(EditText.class))
                .inRoot(isDialog())
                .perform(typeText(randomTemplateName), closeSoftKeyboard());

        // Click the "Add" button
        onView(withText("Add")).perform(click());

        // check that template persists by leaving and returning to workout templates
        onView(withId(R.id.workout_template_back_btn)).perform(click());
        onView(withId(R.id.dashboard_layout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.home_workout)).perform(click());
        onView(withId(R.id.workout_templates_layout))
                .check(matches(isDisplayed()));

        onView(withId(R.id.templateList))
                .check(hasMinimumItemCount(1)); // checks if there's at least 1 item
    }

    /*
    Tests the signup activity by using random username + email.
    Verifies that app transitions to login page where the entries are pre-filled.
     */
    @Test
    public void testUserSignup() throws InterruptedException {
        ActivityScenario.launch(SignupActivity.class);
        // Generate short random suffix (3-4 chars)
        String suffix = String.valueOf((int)(Math.random() * 9000 + 1000)); // e.g., "4723"

        // Base username must be short enough to stay under 12 characters
        String testUsername = "test" + suffix; // e.g., "test4723"
        String testEmail = testUsername + "@example.com";
        String testPassword = "password1!";

        signUp(testUsername, testEmail, testPassword);

        // Check if the login screen is displayed
        onView(withId(R.id.login_layout))
                .check(matches(isDisplayed()));

        // Check that email is prefilled
        onView(withId(R.id.login_username_edt))
                .check(matches(withText(testUsername)));

        // Check that username is prefilled (if applicable)
        onView(withId(R.id.login_password_edt))
                .check(matches(withText(testPassword)));

        login(testUsername, testPassword);

        onView(withId(R.id.home_groups))
                .perform(click());
        Thread.sleep(500);

        onView(withId(R.id.group_page_viewPager))
                .perform(swipeLeft());
        Thread.sleep(500);
        onView(withId(R.id.group_page_viewPager))
                .perform(swipeLeft());
        Thread.sleep(500);
        onView(withId(R.id.group_page_viewPager))
                .perform(swipeLeft());
        Thread.sleep(500);
    }

    /*
    Creates 2 new users.
    User A sends friend request to user B.
    User B sends accepts friend request.
    Test checks that the friend list has 1 friend.
    User B opens a chat with User A and sends message.
    User A logs in and checks that the chat has 1 message.
     */
    @Test
    public void testFriendRequestAndMessagingFlow() throws InterruptedException {
        ActivityScenario.launch(LoginActivity.class);
        login(userAname, testPwd);

        sendFriendRequest(userBname);

        pressBack();

        onView(withId(R.id.dashboard_layout)).check(matches(isDisplayed()));

        onView(withId(R.id.home_logout))
                .perform(click());

        onView(withId(R.id.login_layout)).check(matches(isDisplayed()));

        // Log in as User B and check for the request
        login(userBname, testPwd);
        acceptFriendRequestFrom(userAname);
        // or declineFriendRequest(userAUsername);

        onView(withId(R.id.viewPager))
                .perform(swipeRight());
        onView(withId(R.id.viewPager))
                .perform(swipeRight());

        onView(withId(R.id.friend_list_layout)).check(matches(isDisplayed()));

        // wait for friend request accept to go through
        Thread.sleep(1000);

        onView(withId(R.id.recyclerViewFriends))
                .perform(swipeDown());
        Thread.sleep(500); // wait for adapter to refresh

        onView(withId(R.id.recyclerViewFriends))
                .check(hasMinimumItemCount(1)); // checks if there's at least 1 item

        // click on message button for User A
        onView(withId(R.id.recyclerViewFriends))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(allOf(withId(R.id.friendName), withText(userAname))),
                        clickChildViewWithId(R.id.member_messageButton)
                ));

        Thread.sleep(1000);

        onView(withId(R.id.messageEditText)).perform(typeText("hello"), closeSoftKeyboard());
        onView(withId(R.id.buttonSend)).perform(click());
        Thread.sleep(500);

        ActivityScenario.launch(LoginActivity.class);
        login(userAname, testPwd);
        onView(withId(R.id.dashboard_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.home_messages)).perform(click());

        onView(withId(R.id.message_threads_layout)).check(matches(isDisplayed()));

        // Click the first item in the RecyclerView
        onView(withId(R.id.recyclerViewThreads))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        Thread.sleep(1000);
        onView(withId(R.id.recyclerViewMessage))
                .check(hasMinimumItemCount(1)); // checks if there's at least 1 item
    }

    public void signUp(String username, String email, String password) throws InterruptedException {
        // Check if the signup screen is displayed
        onView(withId(R.id.signup_layout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.signup_email_edt)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.signup_username_edt)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.signup_password_edt)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.signup_confirm_edt)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.signup_signup_btn)).perform(click());
        Thread.sleep(500); // wait for htpp request
    }

    public void login(String username, String password) {
        // Check if the login screen is displayed
        onView(withId(R.id.login_layout))
                .check(matches(isDisplayed()));

        // Clear and enter username
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), typeText(username), closeSoftKeyboard());

        // Clear and enter password
        onView(withId(R.id.login_password_edt))
                .perform(clearText(), typeText(password), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.login_login_btn))
                .perform(click());
    }

    private ViewAssertion hasMinimumItemCount(int minCount) {
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

    public void acceptFriendRequestFrom(String targetUsername) throws InterruptedException {
        // 1. Click button to go to Friend Activity
        onView(withId(R.id.home_friends)) // Replace with actual button ID to open the friend activity
                .perform(click());

        // 2. Swipe to Friend Search Fragment
        onView(withId(R.id.viewPager))
                .perform(swipeLeft());

        // 3. Swipe to Friend Request Fragment
        onView(withId(R.id.viewPager))
                .perform(swipeLeft());

        onView(withId(R.id.friend_search_layout)).check(matches(isDisplayed()));

        onView(withId(R.id.recyclerViewRequests))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(allOf(withId(R.id.fromUserTextView), withText("From: " + targetUsername))),
                        clickChildViewWithId(R.id.acceptButton)
                ));
    }

    public void sendFriendRequest(String targetUsername) throws InterruptedException {
        // 1. Click button to go to Friend Activity
        onView(withId(R.id.home_friends)) // Replace with actual button ID to open the friend activity
                .perform(click());

        // 2. Swipe to Friend Search Fragment (if applicable)
        onView(withId(R.id.viewPager)) // Replace with ID of your ViewPager2
                .perform(swipeLeft()); // Or swipeRight() if needed

        onView(withId(R.id.friend_search_layout)).check(matches(isDisplayed()));

        // 3. Type the friend's username into the search field
        onView(withId(R.id.searchBar)) // Replace with actual ID of the search input field
                .perform(clearText(), typeText(targetUsername), closeSoftKeyboard());

        // 4. Scroll through the RecyclerView and find the target user
        onView(withId(R.id.recyclerViewSearchResults)) // Replace with the RecyclerView ID
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(targetUsername))));

        // 5. Click the "Send Request" button for that user
        onView(withId(R.id.recyclerViewSearchResults)) // Replace with RecyclerView ID
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(targetUsername)),
                        clickChildViewWithId(R.id.friendRequestButton) // Replace with the actual ID of the button
                ));
    }
    public static ViewAction clickChildViewWithId(final int childViewId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class); // Be more flexible here
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
