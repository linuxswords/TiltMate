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
import org.linuxswords.games.tiltmate.time.TimeSettingsManager;

public class MainActivity extends Activity implements TiltListener, PlayerClock.ClockFinishListener
{
    private static final String TAG = "MainActivity";
    private int currentTiltDegree = 0;
    private int moveCount = 0;

    private PlayerClock leftClock;
    private PlayerClock rightClock;
    private TiltSensor tiltSensor;
    private AppPreferences preferences;
    private TickingSoundManager tickingSound;
    private TextView moveCountDisplay;

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

        leftClock = new PlayerClock(leftClockView, timeSettingsManager).showStartTime();
        rightClock = new PlayerClock(rightClockView, timeSettingsManager).showStartTime();

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

        // Initialize move counter display
        moveCountDisplay = findViewById(R.id.moveCountDisplay);
        updateMoveCounterVisibility();

        // pause on a single tap, reset on double tap, show settings on a long tap
        findViewById(R.id.parent).setOnClickListener(new DoubleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                pauseAllClocks();
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
        toActivate.start();
        toPause.pause();
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
        leftClock.restart().showStartTime();
        rightClock.restart().showStartTime();
        // Stop ticking on restart (clocks are reset but not running)
        tickingSound.stop();
        // Reset move counter
        moveCount = 0;
        updateMoveCountDisplay();
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
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        tiltSensor.unregister();
        // Stop ticking when app is paused
        tickingSound.stop();
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

    @Override
    public void onTilt(int degree)
    {
        if (degree != 0) {
            if (currentTiltDegree == 0) {
                currentTiltDegree = degree;
            }
            else if (Math.signum(currentTiltDegree) != Math.signum(degree)) {
                currentTiltDegree = degree;
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
