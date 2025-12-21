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
import org.linuxswords.games.tiltmate.sensor.TiltSensor;
import org.linuxswords.games.tiltmate.sensor.TiltSensor.TiltListener;
import org.linuxswords.games.tiltmate.time.PlayerClock;
import org.linuxswords.games.tiltmate.time.TimeSettingsManager;

public class MainActivity extends Activity implements TiltListener
{
    private static final String TAG = "MainActivity";
    private int currentTiltDegree = 0;

    private PlayerClock leftClock;
    private PlayerClock rightClock;
    private TiltSensor tiltSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView leftClockView = findViewById(R.id.clockLeft);
        TextView rightClockView = findViewById(R.id.clockRight);

        leftClock = new PlayerClock(leftClockView).showStartTime();
        rightClock = new PlayerClock(rightClockView).showStartTime();

        tiltSensor = new TiltSensor(this);
        tiltSensor.setListener(this);

        this.<TextView>findViewById(R.id.timeSettingDisplay).setText(TimeSettingsManager.instance().getCurrent().getLabel());

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
    }


    private void pauseAllClocks()
    {
        leftClock.pause();
        rightClock.pause();
    }

    private void restartAllClocks()
    {
        leftClock.restart().showStartTime();
        rightClock.restart().showStartTime();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        tiltSensor.register();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        tiltSensor.unregister();
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
}
