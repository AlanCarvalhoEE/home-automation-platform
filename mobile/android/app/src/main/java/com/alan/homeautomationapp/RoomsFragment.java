package com.alan.homeautomationapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

public class RoomsFragment extends Fragment {

    private DBhandler dbHandler;        // Database handler instance

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        dbHandler = DBhandler.getInstance(requireContext());

        ImageButton roomAddImageButton = view.findViewById(R.id.roomAddImageButton);

        Utils.updateConfigurationRooms(view, dbHandler);

        roomAddImageButton.setOnClickListener(v ->
                Utils.openDialog(view, view.getContext(), dbHandler, "dialog_add_room", null));

        return view;
    }
}
