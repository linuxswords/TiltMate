package org.linuxswords.games.tiltmate.time;

import android.os.CountDownTimer;

public abstract class PausableCountDownTimer
{
    private static final long LOW_TIME_THRESHOLD = 10_000L;
    private static final long NORMAL_INTERVAL = 1_000L;
    private static final long FAST_INTERVAL = 100L;

    private CountDownTimer countDownTimer;
    private long remainingTime = 0L;
    private long originalStartTime = 0L;
    private long currentInterval = NORMAL_INTERVAL;

    public PausableCountDownTimer(long millisUntilFinished)
    {
        this.originalStartTime = millisUntilFinished;
        this.remainingTime = millisUntilFinished;
    }

    private boolean isPaused = true;

    public abstract void onTimerTick(Long millisUntilFinished);

    public abstract void onTimerFinish();

    public synchronized void start()
    {
        if (isPaused) {
            currentInterval = remainingTime <= LOW_TIME_THRESHOLD ? FAST_INTERVAL : NORMAL_INTERVAL;
            startTimer();
            isPaused = false;
        }
    }

    private void startTimer()
    {
        this.countDownTimer = new CountDownTimer(remainingTime, currentInterval)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                remainingTime = millisUntilFinished;

                // Switch to fast interval when crossing threshold
                if (currentInterval == NORMAL_INTERVAL && millisUntilFinished <= LOW_TIME_THRESHOLD) {
                    countDownTimer.cancel();
                    currentInterval = FAST_INTERVAL;
                    startTimer();
                    return;
                }

                onTimerTick(millisUntilFinished);
            }

            @Override
            public void onFinish()
            {
                onTimerFinish();
                restart();
            }
        };
        this.countDownTimer.start();
    }

    public void pause()
    {
        if (!isPaused) {
            countDownTimer.cancel();
        }
        isPaused = true;
    }

    public void restart()
    {
        countDownTimer.cancel();
        remainingTime = originalStartTime;
        currentInterval = NORMAL_INTERVAL;
        isPaused = true;
    }

    public long getRemainingTime()
    {
        return remainingTime;
    }

    public void increaseTime(long timeInMillis)
    {
        this.remainingTime += timeInMillis;
    }

    public void setRemainingTime(long timeInMillis)
    {
        this.remainingTime = timeInMillis;
        this.currentInterval = timeInMillis <= LOW_TIME_THRESHOLD ? FAST_INTERVAL : NORMAL_INTERVAL;
    }

    public boolean isPaused()
    {
        return isPaused;
    }
}
