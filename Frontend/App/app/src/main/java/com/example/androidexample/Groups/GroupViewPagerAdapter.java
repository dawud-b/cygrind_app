
package com.example.androidexample.Groups;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.androidexample.Friends.FriendsFragment;
import com.example.androidexample.Friends.RequestsFragment;
import com.example.androidexample.Friends.SearchFragment;
import com.example.androidexample.Messaging.ChatFragment;

/**
 * A {@link FragmentStateAdapter} subclass that provides fragments to be displayed in a ViewPager2.
 * This adapter handles the dynamic creation and management of different fragments based on the current position.
 * It is used for displaying various views in a tab layout, such as group search, group members, chat, and group requests.
 */
public class GroupViewPagerAdapter extends FragmentStateAdapter {

    private Bundle bundle;  // A bundle to pass arguments (e.g., WebSocket connection details) to the ChatFragment

    /**
     * Constructor for the adapter.
     *
     * @param fa The FragmentActivity that the adapter is associated with
     */
    public GroupViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    /**
     * Creates and returns a fragment based on the given position.
     * The position corresponds to a tab in the ViewPager. Each position corresponds to a different fragment:
     * <ul>
     *   <li>0: {@link GroupSearchFragment}</li>
     *   <li>1: {@link GroupMembersFragment}</li>
     *   <li>2: {@link ChatFragment}</li>
     *   <li>3: {@link GroupRequestsFragment}</li>
     * </ul>
     *
     * @param position The position of the tab (0-based index)
     * @return The fragment associated with the given position
     */
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new GroupSearchFragment();
                break;
            case 1:
                fragment = new GroupMembersFragment();
                break;
            case 2:
                fragment = new ChatFragment();
                break;
            case 3:
                fragment = new GroupRequestsFragment();
                break;
            default:
                fragment = new GroupSearchFragment();
                break;
        }

        // If the fragment is a ChatFragment, set its arguments (e.g., WebSocket connection info)
        if (fragment instanceof ChatFragment) {
            if (bundle == null) {
                Log.e("GroupChat", "Bundle for websocket connection not found");
            } else {
                fragment.setArguments(bundle);
            }
        }

        return fragment;
    }

    /**
     * Sets the bundle containing additional data (e.g., WebSocket connection details)
     * that can be passed to a fragment, particularly used in the {@link ChatFragment}.
     *
     * @param bundle The bundle containing data to be passed to the fragment
     */
    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Returns the total number of tabs (fragments) available in the ViewPager.
     * In this case, there are 4 tabs: Group Search, Group Members, Chat, and Group Requests.
     *
     * @return The number of tabs (fragments) to display in the ViewPager
     */
    @Override
    public int getItemCount() {
        return 4;  // 4 tabs
    }
}
