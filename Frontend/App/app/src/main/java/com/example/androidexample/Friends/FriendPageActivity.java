package com.example.androidexample.Friends;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.androidexample.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity that provides a tabbed interface for managing friends.
 * <p>
 * Contains three sections represented by tabs:
 * <ul>
 *     <li><b>Friends</b> - List of current friends</li>
 *     <li><b>Search</b> - Search for new friends</li>
 *     <li><b>Requests</b> - View and manage friend requests</li>
 * </ul>
 * <p>
 * Uses {@link ViewPager2} and {@link TabLayout} with {@link ViewPagerAdapter}
 * to handle page navigation.
 */
public class FriendPageActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;

    /**
     * Initializes the activity, sets the layout, and configures the tabbed interface
     * using {@link ViewPager2} and {@link TabLayout}.
     *
     * @param savedInstanceState The previously saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Set up ViewPager2 with the adapter
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Friends");
                            break;
                        case 1:
                            tab.setText("Search");
                            break;
                        case 2:
                            tab.setText("Requests");
                            break;
                    }
                }).attach();
    }
}
