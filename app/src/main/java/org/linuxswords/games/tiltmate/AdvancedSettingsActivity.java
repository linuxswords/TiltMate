package org.linuxswords.games.tiltmate;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.WindowManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.linuxswords.games.tiltmate.preferences.AppPreferences;

public class AdvancedSettingsActivity extends Activity
{
    private AppPreferences preferences;
    private MaterialButton lowButton;
    private MaterialButton mediumButton;
    private MaterialButton highButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_settings);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        preferences = new AppPreferences(this);

        setupTickingSwitch();
        setupSensitivityButtons();
        setupShowMovesSwitch();

        // Back button returns to settings screen
        findViewById(R.id.advancedSettingsBackButton).setOnClickListener(v ->
            startActivity(new Intent(this, SettingsActivity.class))
        );
    }

    private void setupTickingSwitch()
    {
        SwitchMaterial tickingSwitch = findViewById(R.id.tickingSwitch);

        // Load saved setting
        tickingSwitch.setChecked(preferences.isTickingEnabled());
        updateSwitchStyle(tickingSwitch, preferences.isTickingEnabled());

        // Save when changed
        tickingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setTickingEnabled(isChecked);
            updateSwitchStyle(tickingSwitch, isChecked);
        });
    }

    private void setupShowMovesSwitch()
    {
        SwitchMaterial showMovesSwitch = findViewById(R.id.showMovesSwitch);

        // Load saved setting
        showMovesSwitch.setChecked(preferences.isShowMovesEnabled());
        updateSwitchStyle(showMovesSwitch, preferences.isShowMovesEnabled());

        // Save when changed
        showMovesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setShowMovesEnabled(isChecked);
            updateSwitchStyle(showMovesSwitch, isChecked);
        });
    }

    private void setupSensitivityButtons()
    {
        lowButton = findViewById(R.id.sensitivityLowButton);
        mediumButton = findViewById(R.id.sensitivityMediumButton);
        highButton = findViewById(R.id.sensitivityHighButton);

        // Load saved setting and highlight the selected button
        int currentSensitivity = preferences.getTiltSensitivity();
        updateSensitivityButtonHighlight(currentSensitivity);

        // Set click listeners
        lowButton.setOnClickListener(v -> {
            preferences.setTiltSensitivity(0);
            updateSensitivityButtonHighlight(0);
        });

        mediumButton.setOnClickListener(v -> {
            preferences.setTiltSensitivity(1);
            updateSensitivityButtonHighlight(1);
        });

        highButton.setOnClickListener(v -> {
            preferences.setTiltSensitivity(2);
            updateSensitivityButtonHighlight(2);
        });
    }

    private void updateSensitivityButtonHighlight(int selected)
    {
        // Reset all buttons to default style
        resetButtonStyle(lowButton);
        resetButtonStyle(mediumButton);
        resetButtonStyle(highButton);

        // Highlight the selected button
        MaterialButton selectedButton = null;
        switch (selected) {
            case 0: selectedButton = lowButton; break;
            case 1: selectedButton = mediumButton; break;
            case 2: selectedButton = highButton; break;
        }

        if (selectedButton != null) {
            // Use bright white background and border to match switch active state
            int densityDp = (int) getResources().getDisplayMetrics().density;
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(0xFFBBBBBB)); // Light gray like switch track
            selectedButton.setStrokeWidth(2 * densityDp);
            selectedButton.setStrokeColor(ColorStateList.valueOf(0xFFFFFFFF)); // White border
            selectedButton.setTextColor(0xFF000000); // Black text for contrast
        }
    }

    private void resetButtonStyle(MaterialButton button)
    {
        int densityDp = (int) getResources().getDisplayMetrics().density;
        button.setBackgroundTintList(ColorStateList.valueOf(0xFF333333)); // Very dark gray like inactive switch
        button.setStrokeWidth(2 * densityDp);
        button.setStrokeColor(ColorStateList.valueOf(0xFF666666)); // Dark gray border
        button.setTextColor(0xFFFFFFFF); // White text
    }

    private void updateSwitchStyle(SwitchMaterial switchMaterial, boolean isChecked)
    {
        if (isChecked) {
            // Active state: bright white track and thumb
            switchMaterial.setThumbTintList(ColorStateList.valueOf(0xFFFFFFFF)); // White
            switchMaterial.setTrackTintList(ColorStateList.valueOf(0xFFBBBBBB)); // Light gray
        } else {
            // Inactive state: dark gray track and thumb
            switchMaterial.setThumbTintList(ColorStateList.valueOf(0xFF666666)); // Dark gray
            switchMaterial.setTrackTintList(ColorStateList.valueOf(0xFF333333)); // Very dark gray
        }
    }
}
