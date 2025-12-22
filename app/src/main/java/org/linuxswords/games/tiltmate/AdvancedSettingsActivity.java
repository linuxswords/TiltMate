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

        // Save when changed
        tickingSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            preferences.setTickingEnabled(isChecked)
        );
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
            // Use filled background and thicker border to make selection clearly visible
            int densityDp = (int) getResources().getDisplayMetrics().density;
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(0xFF66AA66));
            selectedButton.setStrokeWidth(4 * densityDp);
            selectedButton.setStrokeColor(ColorStateList.valueOf(0xFF88EE88));
        }
    }

    private void resetButtonStyle(MaterialButton button)
    {
        int densityDp = (int) getResources().getDisplayMetrics().density;
        button.setBackgroundTintList(ColorStateList.valueOf(0x00000000)); // Transparent
        button.setStrokeWidth(2 * densityDp);
        button.setStrokeColor(ColorStateList.valueOf(0xFFFFFFFF));
    }
}
