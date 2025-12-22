package org.linuxswords.games.tiltmate.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import org.linuxswords.games.tiltmate.time.TimeSettings;

public class AppPreferences
{
    private static final String PREFS_NAME = "TiltMatePrefs";
    private static final String KEY_TIME_SETTING = "selected_time_setting";
    private static final String KEY_TICKING_ENABLED = "ticking_enabled";
    private static final String KEY_TILT_SENSITIVITY = "tilt_sensitivity";

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
        prefs.edit().putString(KEY_TIME_SETTING, setting.name()).apply();
    }

    /**
     * Load the saved time setting from persistent storage
     * @return The saved TimeSettings, or TEN_PLUS_FIVE as default if none exists
     */
    public TimeSettings getTimeSetting()
    {
        String savedName = prefs.getString(KEY_TIME_SETTING, TimeSettings.TEN_PLUS_FIVE.name());
        try {
            return TimeSettings.valueOf(savedName);
        } catch (IllegalArgumentException e) {
            // If saved value is invalid, return default
            return TimeSettings.TEN_PLUS_FIVE;
        }
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
}
