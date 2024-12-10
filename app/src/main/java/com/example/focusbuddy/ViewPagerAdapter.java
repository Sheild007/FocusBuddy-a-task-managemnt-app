package com.example.focusbuddy;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the correct fragment for each position
        switch (position) {
            case 0:
                return new MainFragment();
            case 1:
                return new AllTasksFragment();
            case 2:
                return new TodayFragment();
            default:
                return new MainFragment(); // Default to ProjectsFragment
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}
