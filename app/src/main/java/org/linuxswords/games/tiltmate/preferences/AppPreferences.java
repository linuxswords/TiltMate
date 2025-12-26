package org.linuxswords.games.tiltmate.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import org.linuxswords.games.tiltmate.time.TimeSettings;

public class AppPreferences
{
    private static final String PREFS_NAME = "TiltMatePrefs";
    private static final String KEY_TIME_SETTING = "selected_time_setting";
    private static final String KEY_CUSTOM_MINUTES = "custom_time_minutes";
    private static final String KEY_CUSTOM_INCREMENT = "custom_time_increment";
    private static final String KEY_TICKING_ENABLED = "ticking_enabled";
    private static final String KEY_TILT_SENSITIVITY = "tilt_sensitivity";
    private static final String KEY_SHOW_MOVES = "show_moves_enabled";

    private final SharedPreferences prefs;

    public AppPreferences(Context context)
    {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save the selected time setting to persistent storage
     */
    public void setTimeSetting(TimeSettings setting)
    {
        SharedPreferences.Editor editor = prefs.edit();

        if (setting.isCustom()) {
            // Save custom time with separate keys
            editor.putString(KEY_TIME_SETTING, TimeSettings.CUSTOM_NAME);
            editor.putInt(KEY_CUSTOM_MINUTES, setting.getMinutes());
            editor.putInt(KEY_CUSTOM_INCREMENT, setting.getIncrement());
        } else {
            // Save preset by name
            editor.putString(KEY_TIME_SETTING, setting.getName());
            // Clear custom values to prevent stale data
            editor.remove(KEY_CUSTOM_MINUTES);
            editor.remove(KEY_CUSTOM_INCREMENT);
        }

        editor.apply();
    }

    /**
     * Load the saved time setting from persistent storage
     * @return The saved TimeSettings, or TEN_PLUS_FIVE as default if none exists
     */
    public TimeSettings getTimeSetting()
    {
        String savedName = prefs.getString(KEY_TIME_SETTING, TimeSettings.TEN_PLUS_FIVE.getName());

        // Check if it's a custom time
        if (TimeSettings.CUSTOM_NAME.equals(savedName)) {
            int minutes = prefs.getInt(KEY_CUSTOM_MINUTES, 10);
            int increment = prefs.getInt(KEY_CUSTOM_INCREMENT, 5);
            try {
                return TimeSettings.createCustom(minutes, increment);
            } catch (IllegalArgumentException e) {
                // If custom values are invalid, return default
                return TimeSettings.TEN_PLUS_FIVE;
            }
        }

        // Handle migration from old presets that were replaced with FIDE times
        if ("FIFTEEN_PLUS_FIVE".equals(savedName)) {
            return TimeSettings.createCustom(15, 5);
        }
        if ("THREE_PLUS_FIVE".equals(savedName)) {
            return TimeSettings.createCustom(3, 5);
        }
        if ("FIVE_PLUS_FIVE".equals(savedName)) {
            return TimeSettings.createCustom(5, 5);
        }
        if ("FIFTEEN_PLUS_ZERO".equals(savedName)) {
            return TimeSettings.createCustom(15, 0);
        }

        // Try to find matching preset
        for (TimeSettings preset : TimeSettings.getPresets()) {
            if (preset.getName().equals(savedName)) {
                return preset;
            }
        }

        // If not found, return default
        return TimeSettings.TEN_PLUS_FIVE;
    }

    /**
     * Enable or disable clock ticking sound
     */
    public void setTickingEnabled(boolean enabled)
    {
        prefs.edit().putBoolean(KEY_TICKING_ENABLED, enabled).apply();
    }

    /**
     * Check if clock ticking sound is enabled
     * @return true if ticking is enabled (default: false)
     */
    public boolean isTickingEnabled()
    {
        return prefs.getBoolean(KEY_TICKING_ENABLED, false);
    }

    /**
     * Set tilt sensitivity level
     * @param level 0 = Low, 1 = Medium, 2 = High
     */
    public void setTiltSensitivity(int level)
    {
        prefs.edit().putInt(KEY_TILT_SENSITIVITY, level).apply();
    }

    /**
     * Get tilt sensitivity level
     * @return 0 = Low, 1 = Medium (default), 2 = High
     */
    public int getTiltSensitivity()
    {
        return prefs.getInt(KEY_TILT_SENSITIVITY, 1);
    }

    /**
     * Enable or disable move counter display
     */
    public void setShowMovesEnabled(boolean enabled)
    {
        prefs.edit().putBoolean(KEY_SHOW_MOVES, enabled).apply();
    }

    /**
     * Check if move counter display is enabled
     * @return true if move counter is enabled (default: false)
     */
    public boolean isShowMovesEnabled()
    {
        return prefs.getBoolean(KEY_SHOW_MOVES, false);
    }
}
