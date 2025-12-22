package org.linuxswords.games.tiltmate.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import org.linuxswords.games.tiltmate.time.TimeSettings;

public class AppPreferences
{
    private static final String PREFS_NAME = "TiltMatePrefs";
    private static final String KEY_TIME_SETTING = "selected_time_setting";

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

    // Future settings methods can be added here:
    // public void setSoundEnabled(boolean enabled) { ... }
    // public boolean isSoundEnabled() { ... }
    // etc.
}
