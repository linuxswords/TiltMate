package org.linuxswords.games.tiltmate.time;

import android.content.Context;
import org.linuxswords.games.tiltmate.preferences.AppPreferences;

public class TimeSettingsManager
{
    private TimeSettings current;
    private AppPreferences preferences;
    private static TimeSettingsManager instance;

    private TimeSettingsManager(Context context)
    {
        preferences = new AppPreferences(context.getApplicationContext());
        // Load the saved setting from SharedPreferences
        current = preferences.getTimeSetting();
    }

    public TimeSettings getCurrent()
    {
        return current;
    }

    public void setCurrent(TimeSettings current)
    {
        this.current = current;
        // Persist the setting immediately when changed
        preferences.setTimeSetting(current);
    }

    public static TimeSettingsManager instance(Context context)
    {
        if (instance == null) {
            instance = new TimeSettingsManager(context);
        }
        return instance;
    }

}
