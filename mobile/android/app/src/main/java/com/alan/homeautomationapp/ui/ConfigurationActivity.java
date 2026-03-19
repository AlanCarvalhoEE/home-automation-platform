package com.alan.homeautomationapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.alan.homeautomationapp.core.DialogManager;
import com.alan.homeautomationapp.core.LanguageManager;
import com.alan.homeautomationapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class ConfigurationActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout
        setContentView(R.layout.activity_configuration);

        // Setup the StrictMode tool
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Configure the action bar
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        View actionBarView = getSupportActionBar().getCustomView();
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) actionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0,0);

        // Component references
        ImageButton configurationImageButton = actionBarView.findViewById(R.id.configurationImageButton);
        ImageButton languageImageButton = actionBarView.findViewById(R.id.languageImageButton);
        TabLayout tabLayout = findViewById(R.id.configTabLayout);
        ViewPager2 viewPager = findViewById(R.id.configViewPager);

        // Change the configuration button icon
        configurationImageButton.setImageResource(R.drawable.ic_return);

        // Configure the language button
        String currentLanguage = LanguageManager.getLanguage();
        switch (currentLanguage) {
            case "pt": languageImageButton.setImageDrawable(getDrawable(R.drawable.portuguese_image));
                break;
            default: languageImageButton.setImageDrawable(getDrawable(R.drawable.english_image));
        }

        viewPager.setAdapter(new ConfigPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText(getString(R.string.rooms_tab_label));
                    else if (position == 1) tab.setText(getString(R.string.devices_tab_label));
                    else tab.setText(getString(R.string.log_tab_label));
                }
        ).attach();

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> finish());

        // Language button listener
        languageImageButton.setOnClickListener(v -> {
            DialogManager.openLanguageSelectionDialog(this, currentLanguage);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setAlpha(1f);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}