package com.example.androidexample.Friends;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter class for managing fragments in a ViewPager.
 * This adapter is used to switch between different tabs (Friends, Search, Requests).
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructor for the ViewPagerAdapter.
     *
     * @param fa The FragmentActivity that hosts the ViewPager.
     */
    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    /**
     * Returns the Fragment associated with a specified position.
     *
     * @param position The position of the tab in the ViewPager.
     * @return Fragment instance corresponding to the tab position.
     */
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FriendsFragment();  // First tab: Friends list
            case 1:
                return new SearchFragment();   // Second tab: Search users
            case 2:
                return new RequestsFragment(); // Third tab: Friend requests
            default:
                return new FriendsFragment();  // Default fallback
        }
    }

    /**
     * Returns the total number of tabs (fragments).
     *
     * @return The number of items managed by the adapter.
     */
    @Override
    public int getItemCount() {
        return 3; // Total number of tabs
    }
}
