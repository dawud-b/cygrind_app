package com.example.androidexample.Trainers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

/**
 * Adapter class for managing fragments within the {@link ViewPager2} used in {@link TrainerPageActivity}.
 * <p>
 * This adapter supplies two fragments:
 * <ul>
 *     <li>{@link FollowedTrainersFragment} - Displays a list of trainers the user is currently following.</li>
 *     <li>{@link SearchTrainersFragment} - Provides a searchable list of all available trainers.</li>
 * </ul>
 * It is used to connect with a {@link TabLayout} via {@link com.google.android.material.tabs.TabLayoutMediator}
 * for navigation between the two sections.
 */
public class TrainerViewPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructor that receives the parent activity where the ViewPager is hosted.
     *
     * @param fa The activity that hosts this adapter, typically {@link TrainerPageActivity}.
     */
    public TrainerViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    /**
     * Returns the fragment associated with a specific position.
     *
     * @param position The index of the tab selected.
     * @return The corresponding fragment:
     *         - index 0 returns {@link FollowedTrainersFragment}
     *         - index 1 returns {@link SearchTrainersFragment}
     *         - default also returns {@link FollowedTrainersFragment}
     */
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FollowedTrainersFragment();
            case 1:
                return new SearchTrainersFragment();
            default:
                return new FollowedTrainersFragment();
        }
    }

    /**
     * Returns the total number of fragments handled by the adapter.
     *
     * @return 2, corresponding to the two tabs: Followed and Search.
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}
