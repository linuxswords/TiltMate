package org.linuxswords.games.chessclock.time;

import android.graphics.Color;
import android.widget.TextView;
import org.linuxswords.games.tiltmate.R;

public class PlayerClock
{
    private static final int WARN_COLOR = Color.RED;
    private static final long WARN_THRESH_HOLD_IN_MILLIS = 60L * 1_000L;  // one minute

    private static final TimeSettingsManager timeSettingsManager = TimeSettingsManager.instance();
    private final PausableCountDownTimer countDownTimer;
    private final TextView view;
    private final long startTimeInMillis;

    public PlayerClock(TextView view)
    {
        this.startTimeInMillis = timeSettingsManager.getCurrent().minutesAsMilliSeconds();
        this.view = view;
        countDownTimer = new PausableCountDownTimer(startTimeInMillis)
        {

            @Override
            public void onTimerTick(Long millisUntilFinished)
            {
                String timeText = TimeFormatter.convertMillisIntoDisplayableTime(millisUntilFinished);
                if(millisUntilFinished<WARN_THRESH_HOLD_IN_MILLIS) {
                    view.setTextColor(WARN_COLOR);
                }

                view.setText(timeText);
            }

            @Override
            public void onTimerFinish()
            {
                view.setTextColor(WARN_COLOR);
                view.setText(R.string.lostMessage);
            }
        };
    }


    public PlayerClock showStartTime()
    {
        this.view.setText(TimeFormatter.convertMillisIntoDisplayableTime(this.startTimeInMillis));
        return this;
    }

    public void start()
    {
        this.countDownTimer.start();
        this.view.setAlpha(1.0F);
        this.view.setTextColor(getDynamicTextColor(this.countDownTimer.getRemainingTime()));
    }

    public void pause()
    {
        // add increment here
        if (!this.countDownTimer.isPaused()) {
            this.countDownTimer.increaseTime(timeSettingsManager.getCurrent().getIncrement() * 1_000L);
        }
        this.countDownTimer.pause();
        this.view.setAlpha(0.5F);
        this.view.setTextColor(getDynamicTextColor(this.countDownTimer.getRemainingTime()));
    }

    public PlayerClock restart()
    {
        this.countDownTimer.restart();
        return this;
    }

    private int getDynamicTextColor(long currentTimeInMillis)
    {
        int result = Color.WHITE;
        if (currentTimeInMillis <= WARN_THRESH_HOLD_IN_MILLIS) {
            result = WARN_COLOR;
        }
        return result;
    }
}
