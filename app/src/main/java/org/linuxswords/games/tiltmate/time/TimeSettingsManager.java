package org.linuxswords.games.tiltmate.time;

public class TimeSettingsManager
{
    private TimeSettings current = TimeSettings.TEN_PLUS_FIVE;
    private static final TimeSettingsManager instance = new TimeSettingsManager();
    private TimeSettingsManager(){}
    public TimeSettings getCurrent()
    {
        return current;
    }

    public void setCurrent(TimeSettings current)
    {
        this.current = current;
    }

    public static TimeSettingsManager instance(){
        return instance;
    }

}
