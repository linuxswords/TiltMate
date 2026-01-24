package org.linuxswords.games.tiltmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import org.linuxswords.games.tiltmate.R;
import org.linuxswords.games.tiltmate.listener.DoubleClickListener;
import org.linuxswords.games.tiltmate.preferences.AppPreferences;
import org.linuxswords.games.tiltmate.sensor.TiltSensor;
import org.linuxswords.games.tiltmate.sensor.TiltSensor.TiltListener;
import org.linuxswords.games.tiltmate.sound.TickingSoundManager;
import org.linuxswords.games.tiltmate.time.PlayerClock;
import org.linuxswords.games.tiltmate.time.TimeSettings;
import org.linuxswords.games.tiltmate.time.TimeSettingsManager;

public class MainActivity extends Activity implements TiltListener, PlayerClock.ClockFinishListener
{
    private static final String TAG = "MainActivity";
    private int currentTiltDegree = 0;
    private int moveCount = 0;
    private boolean gameStarted = false;

    private PlayerClock leftClock;
    private PlayerClock rightClock;
    private TiltSensor tiltSensor;
    private AppPreferences preferences;
    private TickingSoundManager tickingSound;
    private TextView moveCountDisplay;
    private TextView tapToStartIndicator;

    // Track which clock was running before pausing (for settings navigation)
    private enum ClockState { NONE, LEFT, RIGHT }
    private ClockState runningClockBeforePause = ClockState.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView leftClockView = findViewById(R.id.clockLeft);
        TextView rightClockView = findViewById(R.id.clockRight);

        TimeSettingsManager timeSettingsManager = TimeSettingsManager.instance(this);
        preferences = new AppPreferences(this);

        leftClock = new PlayerClock(leftClockView, timeSettingsManager);
        rightClock = new PlayerClock(rightClockView, timeSettingsManager);

        // Set finish listener on both clocks to stop ticking when time runs out
        leftClock.setFinishListener(this);
        rightClock.setFinishListener(this);

        tiltSensor = new TiltSensor(this);
        tiltSensor.setListener(this);
        // Load and apply sensitivity setting
        tiltSensor.setSensitivity(preferences.getTiltSensitivity());

        // Initialize ticking sound
        tickingSound = new TickingSoundManager(this);
        tickingSound.setEnabled(preferences.isTickingEnabled());

        this.<TextView>findViewById(R.id.timeSettingDisplay).setText(timeSettingsManager.getCurrent().getLabel());

        // Initialize move counter display (must be before restoreClockState)
        moveCountDisplay = findViewById(R.id.moveCountDisplay);
        updateMoveCounterVisibility();

        // Initialize tap to start indicator
        tapToStartIndicator = findViewById(R.id.tapToStartIndicator);

        // Restore saved state if available
        if (savedInstanceState != null) {
            long leftTime = savedInstanceState.getLong("leftClockTime", -1);
            long rightTime = savedInstanceState.getLong("rightClockTime", -1);
            int savedMoveCount = savedInstanceState.getInt("moveCount", 0);
            String runningClock = savedInstanceState.getString("runningClock", "NONE");
            boolean savedGameStarted = savedInstanceState.getBoolean("gameStarted", false);

            if (leftTime > 0 && rightTime > 0) {
                // Restore clock times
                restoreClockState(leftTime, rightTime, savedMoveCount, runningClock, savedGameStarted);
            } else {
                leftClock.showStartTime();
                rightClock.showStartTime();
            }
        } else {
            leftClock.showStartTime();
            rightClock.showStartTime();
        }

        // pause on a single tap, reset on double tap, show settings on a long tap
        findViewById(R.id.parent).setOnClickListener(new DoubleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                // At game start (neither clock running, game not started), single tap starts
                // the upward-facing clock (like black starting white's clock)
                if (!gameStarted && currentTiltDegree != 0) {
                    if (currentTiltDegree < 0) {
                        leftClock.start();
                    } else {
                        rightClock.start();
                    }
                    tickingSound.start();
                    gameStarted = true;
                    tapToStartIndicator.setVisibility(View.GONE);
                } else if (gameStarted) {
                    // Check if clocks are paused (both not running)
                    if (!leftClock.isRunning() && !rightClock.isRunning()) {
                        // Resume the clock based on current tilt direction
                        if (currentTiltDegree != 0) {
                            if (currentTiltDegree < 0) {
                                leftClock.start();
                            } else {
                                rightClock.start();
                            }
                            tickingSound.start();
                        }
                    } else {
                        // A clock is running, so pause
                        pauseAllClocks();
                    }
                }
            }

            @Override
            public void onDoubleClick(View v)
            {
                restartAllClocks();
            }
        });
        findViewById(R.id.parent).setOnLongClickListener(v -> this.showSettingsScreen());
    }

    private boolean showSettingsScreen()
    {
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    private void toggleSwitch(PlayerClock toActivate, PlayerClock toPause)
    {
        toPause.addIncrement();
        toPause.pause();
        toActivate.start();
        // Ticking continues while one clock is running
        tickingSound.start();
        // Increment move counter
        moveCount++;
        updateMoveCountDisplay();
    }


    private void pauseAllClocks()
    {
        leftClock.pause();
        rightClock.pause();
        // Stop ticking when both clocks are paused
        tickingSound.stop();
    }

    private void restartAllClocks()
    {
        // Get the current time setting (in case it changed)
        TimeSettings current = TimeSettingsManager.instance(this).getCurrent();
        long currentTime = current.minutesAsMilliSeconds();

        // Reset clocks to current time setting
        leftClock.setRemainingTime(currentTime);
        rightClock.setRemainingTime(currentTime);

        // Stop ticking on restart (clocks are reset but not running)
        tickingSound.stop();
        // Reset move counter
        moveCount = 0;
        updateMoveCountDisplay();
        // Reset game started state and show indicator
        gameStarted = false;
        tapToStartIndicator.setVisibility(View.VISIBLE);

        Log.d(TAG, "Clocks restarted to current time setting: " + current.getLabel());
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        // Reload sensitivity in case it was changed in settings
        tiltSensor.setSensitivity(preferences.getTiltSensitivity());
        tiltSensor.register();

        // Reload ticking setting in case it was changed
        tickingSound.setEnabled(preferences.isTickingEnabled());

        // Update move counter visibility in case it was changed in settings
        updateMoveCounterVisibility();

        // Update time display label in case time setting was changed
        TimeSettings current = TimeSettingsManager.instance(this).getCurrent();
        Log.d(TAG, "onResume: Current time setting is: " + current.getLabel());
        ((TextView)findViewById(R.id.timeSettingDisplay)).setText(current.getLabel());

        // Reset clocks to new time if not running
        if (!leftClock.isRunning() && !rightClock.isRunning()) {
            long newTime = current.minutesAsMilliSeconds();
            leftClock.setRemainingTime(newTime);
            rightClock.setRemainingTime(newTime);
            Log.d(TAG, "Clocks reset to new time: " + newTime);
        }

        // Restore clock state if a clock was running before we paused
        if (runningClockBeforePause == ClockState.LEFT) {
            leftClock.start();
            tickingSound.start();
        } else if (runningClockBeforePause == ClockState.RIGHT) {
            rightClock.start();
            tickingSound.start();
        }
        // Reset state
        runningClockBeforePause = ClockState.NONE;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        tiltSensor.unregister();

        // Save which clock is running before pausing (for settings navigation)
        if (leftClock.isRunning()) {
            runningClockBeforePause = ClockState.LEFT;
        } else if (rightClock.isRunning()) {
            runningClockBeforePause = ClockState.RIGHT;
        } else {
            runningClockBeforePause = ClockState.NONE;
        }

        // Pause all clocks to preserve state when navigating to settings
        pauseAllClocks();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        // Save clock states
        outState.putLong("leftClockTime", leftClock.getRemainingTime());
        outState.putLong("rightClockTime", rightClock.getRemainingTime());
        outState.putInt("moveCount", moveCount);
        outState.putBoolean("gameStarted", gameStarted);

        // Save which clock is running
        if (leftClock.isRunning()) {
            outState.putString("runningClock", "LEFT");
        } else if (rightClock.isRunning()) {
            outState.putString("runningClock", "RIGHT");
        } else {
            outState.putString("runningClock", "NONE");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // Release sound resources
        if (tickingSound != null) {
            tickingSound.release();
        }
    }

    private void restoreClockState(long leftTime, long rightTime, int savedMoveCount, String runningClock, boolean savedGameStarted)
    {
        // Restore times
        leftClock.setRemainingTime(leftTime);
        rightClock.setRemainingTime(rightTime);

        // Restore move count
        moveCount = savedMoveCount;
        updateMoveCountDisplay();

        // Restore game started state and indicator
        gameStarted = savedGameStarted;
        tapToStartIndicator.setVisibility(gameStarted ? View.GONE : View.VISIBLE);

        // Restore running state
        if ("LEFT".equals(runningClock)) {
            runningClockBeforePause = ClockState.LEFT;
        } else if ("RIGHT".equals(runningClock)) {
            runningClockBeforePause = ClockState.RIGHT;
        } else {
            runningClockBeforePause = ClockState.NONE;
        }
    }

    @Override
    public void onTilt(int degree)
    {
        if (degree != 0) {
            if (currentTiltDegree == 0) {
                currentTiltDegree = degree;
            }
            else if (Math.signum(currentTiltDegree) != Math.signum(degree)) {
                currentTiltDegree = degree;
                // Only switch clocks if game has been started with a tap
                if (gameStarted) {
                    Log.d(TAG, "tilted from " + currentTiltDegree + " to " + degree);
                    if (Math.signum(currentTiltDegree) == -1) {
                        toggleSwitch(leftClock, rightClock);
                    }
                    else {
                        toggleSwitch(rightClock, leftClock);
                    }
                }
            }
        }
    }

    @Override
    public void onClockFinished(PlayerClock clock)
    {
        // Stop ticking sound when time runs out
        tickingSound.stop();
        Log.d(TAG, "Clock finished - ticking sound stopped");
    }

    private void updateMoveCounterVisibility()
    {
        if (preferences.isShowMovesEnabled()) {
            moveCountDisplay.setVisibility(View.VISIBLE);
            updateMoveCountDisplay();
        } else {
            moveCountDisplay.setVisibility(View.GONE);
        }
    }

    private void updateMoveCountDisplay()
    {
        moveCountDisplay.setText(moveCount + " moves");
    }
}
