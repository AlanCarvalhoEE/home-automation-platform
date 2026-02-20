package com.alan.homeautomationapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

public class RoomsFragment extends Fragment {

    private DBhandler dbHandler;
    private MQTTclient mqttClient;
    private LinearLayout roomsLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        dbHandler = DBhandler.getInstance(requireContext());
        mqttClient = MQTTclient.getInstance();
        roomsLayout = view.findViewById(R.id.roomsLayout);

        ImageButton roomAddImageButton = view.findViewById(R.id.roomAddImageButton);

        roomAddImageButton.setOnClickListener(v ->
                DialogManager.openAddRoomDialog(
                        requireActivity(),
                        roomData -> {
                            dbHandler.addRoom(roomData.id, roomData.name);
                            String payload = roomData.id + "," + roomData.name;
                            mqttClient.publish("hap/main/database/add_room", payload);
                            refreshRooms();
                        }));

        refreshRooms();

        return view;
    }

    private void refreshRooms() {

        roomsLayout.removeAllViews();

        List<String> roomsList = dbHandler.getRoomsList();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String roomName : roomsList) {

            String roomID = dbHandler.getRoomID(roomName);

            View roomView = inflater.inflate(R.layout.room_info, roomsLayout, false);

            TextView roomNameTextView = roomView.findViewById(R.id.roomNameTextView);
            ImageButton roomConfigImageButton = roomView.findViewById(R.id.roomConfigImageButton);
            ImageButton roomDeleteImageButton = roomView.findViewById(R.id.roomDeleteImageButton);

            roomNameTextView.setText(roomName);

            roomConfigImageButton.setOnClickListener(v ->
                    DialogManager.openUpdateRoomDialog(
                            requireActivity(), roomID, roomData -> {
                                dbHandler.updateRoom(roomData.id, roomData.name);
                                String payload = roomData.id + "," + roomData.name;
                                mqttClient.publish("hap/main/database/update_room", payload);
                                refreshRooms();
                            }));

            roomDeleteImageButton.setOnClickListener(v ->
                    DialogManager.openDeleteRoomDialog(
                            requireActivity(), roomID, roomName, roomData -> {
                                dbHandler.deleteRoom(roomData.id);
                                String payload = roomData.id;
                                mqttClient.publish("hap/main/database/delete_room", payload);
                                refreshRooms();
                            }));

            roomsLayout.addView(roomView);
        }
    }
}
