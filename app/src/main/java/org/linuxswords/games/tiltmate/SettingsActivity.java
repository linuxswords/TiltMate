package org.linuxswords.games.tiltmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import org.linuxswords.games.tiltmate.R;
import org.linuxswords.games.tiltmate.time.TimeSettings;
import org.linuxswords.games.tiltmate.time.TimeSettingsManager;

public class SettingsActivity extends Activity
{
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_settings);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupTimeSettingsButtons();

        // advanced settings
        android.view.View advancedButton = findViewById(R.id.advancedSettingsButton);
        Log.d(TAG, "advancedSettingsButton found: " + (advancedButton != null));
        if (advancedButton != null) {
            advancedButton.setOnClickListener(v -> {
                Log.d(TAG, "Advanced settings button clicked");
                startActivity(new Intent(this, AdvancedSettingsActivity.class));
            });
        }

        // cancel
        android.view.View cancelButton = findViewById(R.id.settingsCancelButton);
        Log.d(TAG, "settingsCancelButton found: " + (cancelButton != null));
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                Log.d(TAG, "Cancel button clicked");
                finish();  // Return to existing MainActivity instead of creating new one
            });
        }

        // exit
        android.view.View exitButton = findViewById(R.id.exitButton);
        Log.d(TAG, "exitButton found: " + (exitButton != null));
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                Log.d(TAG, "Exit button clicked");
                this.finishAffinity();
            });
        }
    }

    private void setupTimeSettingsButtons()
    {
        findViewById(R.id.timeSettingThreeZero).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.THREE_PLUS_ZERO));
        findViewById(R.id.timeSettingThreePlusFive).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.THREE_PLUS_FIVE));

        findViewById(R.id.timeSettingFiveZero).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.FIVE_PLUS_ZERO));
        findViewById(R.id.timeSettingFivePlusFive).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.FIVE_PLUS_FIVE));

        findViewById(R.id.timeSettingTenZero).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.TEN_PLUS_ZERO));
        findViewById(R.id.timeSettingTenPlusFive).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.TEN_PLUS_FIVE));

        findViewById(R.id.timeSettingFifteenZero).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.FIFTEEN_PLUS_ZERO));
        findViewById(R.id.timeSettingFifteenPlusFive).setOnClickListener(v -> this.setTimeAndCloseView(TimeSettings.FIFTEEN_PLUS_FIVE));
    }

    private void setTimeAndCloseView(TimeSettings timeSetting)
    {
        TimeSettingsManager timeSettingsManager = TimeSettingsManager.instance(this);
        timeSettingsManager.setCurrent(timeSetting);
        finish();  // Return to existing MainActivity instead of creating new one
    }
}
