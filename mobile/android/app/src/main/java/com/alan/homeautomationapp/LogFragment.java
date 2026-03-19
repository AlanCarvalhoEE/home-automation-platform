package com.alan.homeautomationapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

// Class responsible for running the rooms fragment
public class LogFragment extends Fragment {

    private LinearLayout logLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log, container, false);

        logLayout = view.findViewById(R.id.logLayout);

        refreshLog();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLog();
    }

    // Method to refresh log entries on screen
    private void refreshLog() {

        logLayout.removeAllViews();

        DatabaseManager db = DatabaseManager.getInstance(requireContext());
        List<LogData> logList = db.getEvents();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (LogData logEntry : logList) {

            View logView = inflater.inflate(R.layout.log_info, logLayout, false);

            TextView logTimestampTextView = logView.findViewById(R.id.logTimestampTextView);
            TextView logTypeTextView = logView.findViewById(R.id.logTypeTextView);
            TextView logMessageTextView = logView.findViewById(R.id.logMessageTextView);

            logTimestampTextView.setText(logEntry.getFormattedTime());
            logTypeTextView.setText(logEntry.getType());
            logMessageTextView.setText(logEntry.getMessage());

            logLayout.addView(logView);
        }
    }
}
