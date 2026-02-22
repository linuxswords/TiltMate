package org.linuxswords.games.tiltmate.time;

import android.graphics.Color;
import android.widget.TextView;
import org.linuxswords.games.tiltmate.R;

public class PlayerClock
{
    public interface ClockFinishListener
    {
        void onClockFinished(PlayerClock clock);
    }

    private static final int WARN_COLOR = Color.RED;
    private static final long WARN_THRESH_HOLD_IN_MILLIS = 10_000L;  // 10 seconds

    private final TimeSettingsManager timeSettingsManager;
    private final PausableCountDownTimer countDownTimer;
    private final TextView view;
    private final long startTimeInMillis;
    private ClockFinishListener finishListener;

    public PlayerClock(TextView view, TimeSettingsManager timeSettingsManager)
    {
        this.timeSettingsManager = timeSettingsManager;
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
                // Notify listener that time has run out
                if (finishListener != null) {
                    finishListener.onClockFinished(PlayerClock.this);
                }
            }
        };
    }

    public void setFinishListener(ClockFinishListener listener)
    {
        this.finishListener = listener;
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

    public void addIncrement()
    {
        long increment = timeSettingsManager.getCurrent().getIncrement() * 1_000L;
        if (increment > 0) {
            this.countDownTimer.increaseTime(increment);
            this.view.setText(TimeFormatter.convertMillisIntoDisplayableTime(this.countDownTimer.getRemainingTime()));
        }
    }

    public void pause()
    {
        this.countDownTimer.pause();
        this.view.setAlpha(0.5F);
        this.view.setTextColor(getDynamicTextColor(this.countDownTimer.getRemainingTime()));
    }

    public PlayerClock restart()
    {
        this.countDownTimer.restart();
        return this;
    }

    public void setActiveDisplay(boolean active)
    {
        this.view.setAlpha(active ? 1.0F : 0.5F);
    }

    public boolean isRunning()
    {
        return !this.countDownTimer.isPaused();
    }

    public long getRemainingTime()
    {
        return this.countDownTimer.getRemainingTime();
    }

    public void setRemainingTime(long timeInMillis)
    {
        this.countDownTimer.pause();
        this.countDownTimer.setRemainingTime(timeInMillis);
        this.view.setText(TimeFormatter.convertMillisIntoDisplayableTime(timeInMillis));
        this.view.setTextColor(getDynamicTextColor(timeInMillis));
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
