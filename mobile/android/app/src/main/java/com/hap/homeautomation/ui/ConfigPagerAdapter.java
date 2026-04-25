package com.hap.homeautomation.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hap.homeautomation.devices.DevicesFragment;
import com.hap.homeautomation.log.LogFragment;
import com.hap.homeautomation.rooms.RoomsFragment;

public class ConfigPagerAdapter extends FragmentStateAdapter {

    public ConfigPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 1 -> new DevicesFragment();
            case 2 -> new LogFragment();
            default -> new RoomsFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
