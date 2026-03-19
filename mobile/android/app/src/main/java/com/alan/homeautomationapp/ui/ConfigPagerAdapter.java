package com.alan.homeautomationapp.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alan.homeautomationapp.devices.DevicesFragment;
import com.alan.homeautomationapp.log.LogFragment;
import com.alan.homeautomationapp.rooms.RoomsFragment;

public class ConfigPagerAdapter extends FragmentStateAdapter {

    public ConfigPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RoomsFragment();

            case 1:
                return new DevicesFragment();

            case 2:
                return new LogFragment();

            default:
                return new RoomsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
