package com.example.androidexample.Nutrition;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.androidexample.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Main activity for the Nutrition tracking feature.
 * Contains three tabs:
 * 1. Recently stored foods
 * 2. Create new food
 * 3. Daily tracker
 */
public class NutritionActivity extends AppCompatActivity {

    private static final String TAG = "NutritionActivity";
    private static final int NUM_PAGES = 3;
    private static final int RECENT_FOODS_PAGE = 0;
    private static final int ADD_FOOD_PAGE = 1;
    private static final int DAILY_TRACKER_PAGE = 2;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private NutritionPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_nutrition);

            // Set up back button in action bar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Nutrition Tracker");
            }

            // Initialize ViewPager and TabLayout
            viewPager = findViewById(R.id.nutrition_view_pager);
            tabLayout = findViewById(R.id.nutrition_tab_layout);

            if (viewPager == null || tabLayout == null) {
                Log.e(TAG, "ViewPager or TabLayout not found in layout");
                Toast.makeText(this, "Error initializing layout", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set up the adapter for the ViewPager
            try {
                pagerAdapter = new NutritionPagerAdapter(this);
                viewPager.setAdapter(pagerAdapter);

                // Connect TabLayout with ViewPager
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case RECENT_FOODS_PAGE:
                            tab.setText("Recent Foods");
                            break;
                        case ADD_FOOD_PAGE:
                            tab.setText("Add Food");
                            break;
                        case DAILY_TRACKER_PAGE:
                            tab.setText("Daily Tracker");
                            break;
                    }
                }).attach();

                // Start with Daily Tracker tab by default
                // (since it's likely the most commonly used)
                viewPager.setCurrentItem(DAILY_TRACKER_PAGE);

            } catch (Exception e) {
                Log.e(TAG, "Error setting up ViewPager and tabs", e);
                Toast.makeText(this, "Error initializing tabs", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing nutrition tracker", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Refresh the Daily Tracker fragment when a food item is added
     */
    public void refreshDailyTracker() {
        Log.d(TAG, "Refreshing daily tracker");
        try {
            // First try to find the fragment using the fragment manager
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + DAILY_TRACKER_PAGE);
            if (fragment instanceof DailyTrackerFragment) {
                Log.d(TAG, "Found daily tracker fragment by tag, refreshing");
                DailyTrackerFragment dailyTrackerFragment = (DailyTrackerFragment) fragment;
                dailyTrackerFragment.loadDailyFoodItems();
                dailyTrackerFragment.loadNutritionalSummary();

                // Switch to the Daily Tracker tab
                viewPager.setCurrentItem(DAILY_TRACKER_PAGE);
                return;
            }

            // If we couldn't find the fragment by tag, try from fragments list
            if (getSupportFragmentManager().getFragments().size() > DAILY_TRACKER_PAGE) {
                fragment = getSupportFragmentManager().getFragments().get(DAILY_TRACKER_PAGE);
                if (fragment instanceof DailyTrackerFragment) {
                    Log.d(TAG, "Found daily tracker fragment in fragments list, refreshing");
                    DailyTrackerFragment dailyTrackerFragment = (DailyTrackerFragment) fragment;
                    dailyTrackerFragment.loadDailyFoodItems();
                    dailyTrackerFragment.loadNutritionalSummary();

                    // Switch to the Daily Tracker tab
                    viewPager.setCurrentItem(DAILY_TRACKER_PAGE);
                    return;
                }
            }

            // If we still couldn't find the fragment, simply switch to the Daily Tracker tab
            // The fragment will reload its data when it becomes visible
            Log.d(TAG, "Could not find daily tracker fragment, switching tab");
            viewPager.setCurrentItem(DAILY_TRACKER_PAGE);

        } catch (Exception e) {
            Log.e(TAG, "Error refreshing daily tracker", e);
            // Still try to switch to the Daily Tracker tab
            try {
                viewPager.setCurrentItem(DAILY_TRACKER_PAGE);
            } catch (Exception ex) {
                Log.e(TAG, "Error switching to daily tracker tab", ex);
            }
        }
    }

    /**
     * Adapter for the ViewPager that hosts the three tabs
     */
    private static class NutritionPagerAdapter extends FragmentStateAdapter {

        public NutritionPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.d(TAG, "Creating fragment for position: " + position);
            try {
                switch (position) {
                    case RECENT_FOODS_PAGE:
                        return new RecentFoodsFragment();
                    case ADD_FOOD_PAGE:
                        return new AddFoodFragment();
                    case DAILY_TRACKER_PAGE:
                        return new DailyTrackerFragment();
                    default:
                        return new RecentFoodsFragment();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating fragment for position: " + position, e);
                // Return an empty fragment as fallback
                return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}