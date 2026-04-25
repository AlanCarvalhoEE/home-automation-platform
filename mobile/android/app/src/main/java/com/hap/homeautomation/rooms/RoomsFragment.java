package com.hap.homeautomation.rooms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hap.homeautomation.core.DatabaseManager;
import com.hap.homeautomation.core.DialogManager;
import com.hap.homeautomation.R;
import com.hap.homeautomation.core.MQTTclient;

import java.util.List;

// Class responsible for running the rooms fragment
public class RoomsFragment extends Fragment {

    private DatabaseManager dbHandler;
    private MQTTclient mqttClient;
    private LinearLayout roomsLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        dbHandler = DatabaseManager.getInstance(requireContext());
        mqttClient = MQTTclient.getInstance();
        roomsLayout = view.findViewById(R.id.roomsLayout);

        ImageButton roomAddImageButton = view.findViewById(R.id.roomAddImageButton);

        // Room add button listener
        roomAddImageButton.setOnClickListener(v ->
                DialogManager.openAddRoomDialog(requireActivity(), roomData -> {

                            dbHandler.addRoom(roomData.getId(), roomData.getName());

                            RoomManager.getInstance(requireContext()).addRoom(
                                    new RoomData(roomData.getId(), roomData.getName()), true);

                            String payload = roomData.getId() + "," + roomData.getName();
                            mqttClient.publish("hap/main/database/add_room", payload);

                            refreshRooms();
                        }));

        refreshRooms();

        return view;
    }

    // Method to refresh rooms on screen
    private void refreshRooms() {

        roomsLayout.removeAllViews();

        List<String> roomsList = RoomManager.getInstance(requireContext()).getAllRoomNames();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String roomName : roomsList) {

            RoomData room = RoomManager.getInstance(requireContext()).getRoomByName(roomName);
            String roomID = room.getId();

            View roomView = inflater.inflate(R.layout.room_info, roomsLayout, false);

            TextView roomNameTextView = roomView.findViewById(R.id.roomNameTextView);
            ImageButton roomConfigImageButton = roomView.findViewById(R.id.roomConfigImageButton);
            ImageButton roomDeleteImageButton = roomView.findViewById(R.id.roomDeleteImageButton);

            roomNameTextView.setText(roomName);

            //Room configure button listener
            roomConfigImageButton.setOnClickListener(v ->
                    DialogManager.openConfigureRoomDialog(
                            requireActivity(), room, roomData -> {

                                dbHandler.configureRoom(roomData.getId(), roomData.getName());

                                RoomManager.getInstance(requireContext()).configureRoom(
                                        roomData.getId(), roomData.getName(), true);

                                String payload = roomData.getId() + "," + roomData.getName();
                                mqttClient.publish("hap/main/database/update_room", payload);

                                refreshRooms();
                            }));

            // Room delete button listener
            roomDeleteImageButton.setOnClickListener(v -> DialogManager.openDeleteRoomDialog(
                            requireActivity(), roomID, roomName, roomData -> {

                                dbHandler.deleteRoom(roomData.getId());

                                RoomManager.getInstance(requireContext()).deleteRoom(
                                        roomData.getId(), true);

                                String payload = roomData.getId();
                                mqttClient.publish("hap/main/database/delete_room", payload);

                                refreshRooms();
                            }));

            roomsLayout.addView(roomView);
        }
    }
}
