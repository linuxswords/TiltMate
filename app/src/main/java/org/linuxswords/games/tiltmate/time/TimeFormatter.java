package org.linuxswords.games.tiltmate.time;

import java.util.concurrent.TimeUnit;

public class TimeFormatter
{
    private static final String DISPLAY_TIME_FORMAT = "%d:%02d";
    private static final String LOW_TIME_FORMAT = "%d.%d";
    private static final long LOW_TIME_THRESHOLD_MS = 10000;

    public static String convertMillisIntoDisplayableTime(Long millisUntilFinished)
    {
        if (millisUntilFinished < LOW_TIME_THRESHOLD_MS) {
            long seconds = millisUntilFinished / 1000;
            long tenths = (millisUntilFinished % 1000) / 100;
            return String.format(LOW_TIME_FORMAT, seconds, tenths);
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
        return String.format(DISPLAY_TIME_FORMAT,
            minutes,
            seconds % 60
        );
    }

}
