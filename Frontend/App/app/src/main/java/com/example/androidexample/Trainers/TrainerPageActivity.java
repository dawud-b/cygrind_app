package com.example.androidexample.Trainers;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.androidexample.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
/**
 * Activity that displays a tab layout for viewing followed trainers and searching for trainers.
 * <p>
 * This activity uses a `ViewPager2` with two tabs:
 * 1. "Followed Trainers" tab, which shows a list of trainers the user is following.
 * 2. "Search Trainers" tab, which allows the user to search for trainers.
 * <p>
 * The activity uses a `TabLayout` to provide a tabbed interface, and a `ViewPager2` to allow the user to swipe between the tabs.
 */
public class TrainerPageActivity extends AppCompatActivity {

    private TabLayout tabLayout;       // TabLayout for the tabs
    private ViewPager2 viewPager;      // ViewPager2 for swipeable content
    private TrainerViewPagerAdapter adapter;  // Adapter for the ViewPager2

    /**
     * Called when the activity is created.
     * <p>
     * This method sets up the `TabLayout`, `ViewPager2`, and the `TrainerViewPagerAdapter` to display
     * the content for followed trainers and search trainers. The method also connects the `TabLayout`
     * with the `ViewPager2` using a `TabLayoutMediator`.
     *
     * @param savedInstanceState The saved instance state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainers);

        // Initialize the TabLayout and ViewPager2
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Set up ViewPager2 with the custom adapter
        adapter = new TrainerViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2 and set tab titles
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Followed Trainers");  // Title for the first tab
                            break;
                        case 1:
                            tab.setText("Search Trainers");   // Title for the second tab
                            break;
                    }
                }).attach();
    }
}
