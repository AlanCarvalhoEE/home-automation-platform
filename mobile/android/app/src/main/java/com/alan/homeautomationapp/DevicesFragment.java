package com.alan.homeautomationapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

public class DevicesFragment extends Fragment {

    private DBhandler dbHandler;        // Database handler instance

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        dbHandler = DBhandler.getInstance(requireContext());

        Utils.updateConfigurationDevices(view, dbHandler);

        return view;
    }
}
