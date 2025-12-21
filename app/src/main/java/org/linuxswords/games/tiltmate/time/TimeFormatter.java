package org.linuxswords.games.tiltmate.time;

import java.util.concurrent.TimeUnit;

public class TimeFormatter
{
    public static final String DISPLAY_TIME_FORMAT = "%d:%02d";  // make this configurable


    public static String convertMillisIntoDisplayableTime(Long millisUntilFinished)
    {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
        return String.format(DISPLAY_TIME_FORMAT,
            minutes,
            seconds % 60
        );
    }

}
