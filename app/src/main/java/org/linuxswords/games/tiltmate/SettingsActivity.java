package org.linuxswords.games.tiltmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
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
        setupCustomTimeButton();

        // advanced settings
        android.view.View advancedButton = findViewById(R.id.advancedSettingsButton);
        Log.d(TAG, "advancedSettingsButton found: " + (advancedButton != null));
        if (advancedButton != null) {
            advancedButton.setOnClickListener(v -> {
                Log.d(TAG, "Advanced settings button clicked");
                startActivity(new Intent(this, AdvancedSettingsActivity.class));
            });
        }

        // exit (now returns to game - has revert icon)
        android.view.View exitButton = findViewById(R.id.exitButton);
        Log.d(TAG, "exitButton found: " + (exitButton != null));
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                Log.d(TAG, "Exit button clicked - returning to game");
                finish();  // Return to existing MainActivity
            });
        }

        // cancel (now quits app - has power icon)
        android.view.View cancelButton = findViewById(R.id.settingsCancelButton);
        Log.d(TAG, "settingsCancelButton found: " + (cancelButton != null));
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                Log.d(TAG, "Cancel button clicked - quitting app");
                this.finishAffinity();  // Quit the app
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
    }

    private void setupCustomTimeButton()
    {
        Button customButton = findViewById(R.id.timeSettingCustom);

        // Update button text to show current custom time if set
        TimeSettings current = TimeSettingsManager.instance(this).getCurrent();
        if (current.isCustom()) {
            customButton.setText(current.getLabel());
        } else {
            customButton.setText("Custom");
        }

        customButton.setOnClickListener(v -> this.showCustomTimeDialog());
    }

    private void showCustomTimeDialog()
    {
        Log.d(TAG, "showCustomTimeDialog called");
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom_time, null);

        // Get references to UI elements
        TextView minutesValue = dialogView.findViewById(R.id.minutesValue);
        TextView incrementValue = dialogView.findViewById(R.id.incrementValue);
        Button minutesDecrement = dialogView.findViewById(R.id.minutesDecrement);
        Button minutesIncrement = dialogView.findViewById(R.id.minutesIncrement);
        Button incrementDecrement = dialogView.findViewById(R.id.incrementDecrement);
        Button incrementIncrement = dialogView.findViewById(R.id.incrementIncrement);

        // Initialize with current custom time if set, otherwise use defaults
        TimeSettings current = TimeSettingsManager.instance(this).getCurrent();
        final int[] currentMinutes = {current.isCustom() ? current.getMinutes() : 10};
        final int[] currentIncrement = {current.isCustom() ? current.getIncrement() : 5};

        // Update display
        minutesValue.setText(String.valueOf(currentMinutes[0]));
        incrementValue.setText(String.valueOf(currentIncrement[0]));

        // Setup hold-to-repeat for minutes decrement
        setupRepeatButton(minutesDecrement, () -> {
            if (currentMinutes[0] > 1) {
                currentMinutes[0]--;
                minutesValue.setText(String.valueOf(currentMinutes[0]));
                return true;
            }
            return false;
        });

        // Setup hold-to-repeat for minutes increment
        setupRepeatButton(minutesIncrement, () -> {
            if (currentMinutes[0] < 180) {
                currentMinutes[0]++;
                minutesValue.setText(String.valueOf(currentMinutes[0]));
                return true;
            }
            return false;
        });

        // Setup hold-to-repeat for increment decrement
        setupRepeatButton(incrementDecrement, () -> {
            if (currentIncrement[0] > 0) {
                currentIncrement[0]--;
                incrementValue.setText(String.valueOf(currentIncrement[0]));
                return true;
            }
            return false;
        });

        // Setup hold-to-repeat for increment increment
        setupRepeatButton(incrementIncrement, () -> {
            if (currentIncrement[0] < 60) {
                currentIncrement[0]++;
                incrementValue.setText(String.valueOf(currentIncrement[0]));
                return true;
            }
            return false;
        });

        // Create and show dialog
        new AlertDialog.Builder(this)
            .setTitle("Custom Time Control")
            .setView(dialogView)
            .setPositiveButton("OK", (dialog, which) -> {
                Log.d(TAG, "Custom time selected: " + currentMinutes[0] + "+" + currentIncrement[0]);
                try {
                    TimeSettings custom = TimeSettings.createCustom(currentMinutes[0], currentIncrement[0]);
                    Log.d(TAG, "Created custom TimeSettings: " + custom.getLabel());
                    setTimeAndCloseView(custom);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid custom time values", e);
                }
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                Log.d(TAG, "Custom time dialog cancelled");
            })
            .show();
    }

    /**
     * Sets up a button to repeat its action when held down
     * @param button The button to configure
     * @param action The action to repeat (returns true if action succeeded, false if at limit)
     */
    private void setupRepeatButton(Button button, RepeatAction action)
    {
        final Handler handler = new Handler();
        final boolean[] isPressed = {false};

        final Runnable repeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPressed[0]) {
                    if (action.execute()) {
                        handler.postDelayed(this, 100); // Repeat every 100ms
                    }
                }
            }
        };

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isPressed[0] = true;
                    action.execute(); // Execute immediately on press
                    handler.postDelayed(repeatRunnable, 500); // Start repeating after 500ms delay
                    v.setPressed(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isPressed[0] = false;
                    handler.removeCallbacks(repeatRunnable);
                    v.setPressed(false);
                    v.performClick(); // Accessibility
                    return true;
            }
            return false;
        });
    }

    /**
     * Functional interface for repeat actions
     */
    private interface RepeatAction {
        boolean execute();
    }

    private void setTimeAndCloseView(TimeSettings timeSetting)
    {
        Log.d(TAG, "setTimeAndCloseView called with: " + timeSetting.getLabel());
        TimeSettingsManager timeSettingsManager = TimeSettingsManager.instance(this);
        timeSettingsManager.setCurrent(timeSetting);
        Log.d(TAG, "TimeSettings saved, current is now: " + timeSettingsManager.getCurrent().getLabel());
        finish();  // Return to existing MainActivity instead of creating new one
    }
}
