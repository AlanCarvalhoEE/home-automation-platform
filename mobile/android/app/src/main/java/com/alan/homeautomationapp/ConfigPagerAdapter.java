package com.alan.homeautomationapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ConfigPagerAdapter extends FragmentStateAdapter {

    public ConfigPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0
                ? new RoomsFragment()
                : new DevicesFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
