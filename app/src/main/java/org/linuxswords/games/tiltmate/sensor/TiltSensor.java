package org.linuxswords.games.tiltmate.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TiltSensor implements SensorEventListener
{
    public interface TiltListener
    {
        // create method with all 3
        // axis translation as argument
        void onTilt(int degree);
    }

    private TiltListener tiltListener;

    public void setListener(TiltListener l)
    {
        tiltListener = l;
    }

    /**
     * Set tilt sensitivity level
     * @param level 0 = Low (20°), 1 = Medium (10°), 2 = High (5°)
     */
    public void setSensitivity(int level)
    {
        switch (level) {
            case 0:
                sensitivityThreshold = THRESHOLD_LOW;
                break;
            case 1:
                sensitivityThreshold = THRESHOLD_MEDIUM;
                break;
            case 2:
                sensitivityThreshold = THRESHOLD_HIGH;
                break;
            default:
                Log.w(TAG, "Invalid sensitivity level " + level + ", using medium");
                sensitivityThreshold = THRESHOLD_MEDIUM;
        }
        Log.d(TAG, "Sensitivity set to " + level + " (threshold: " + sensitivityThreshold + "°)");
    }

    private static final String TAG = "TiltSensor";

    // Sensitivity thresholds in degrees
    private static final int THRESHOLD_LOW = 12;      // Low sensitivity (0)
    private static final int THRESHOLD_MEDIUM = 6;    // Medium sensitivity (1)
    private static final int THRESHOLD_HIGH = 3;      // High sensitivity (2)

    // Low-pass filter coefficient (0-1, higher = faster response)
    private static final float ALPHA = 0.8f;

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private float[] mGravity;
    private int sensitivityThreshold = THRESHOLD_MEDIUM; // Default: medium

    public TiltSensor(Context context)
    {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Log.w(TAG, "accelerometer is null");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.values == null) {
            Log.w(TAG, "event.values is null");
            return;
        }
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        mGravity = applyLowPassFilter(mGravity, event.values);

        // Calculate tilt using accelerometer only (no magnetometer needed)
        // Phone is on its side (X dominant), Y changes when seesaw tilts
        double tilt = Math.atan2(-mGravity[1], Math.abs(mGravity[0]));
        int tiltDeg = (int) Math.round(Math.toDegrees(tilt));

        // Only notify listener if tilt exceeds the sensitivity threshold
        if (Math.abs(tiltDeg) >= sensitivityThreshold) {
            tiltListener.onTilt(tiltDeg);
        } else {
            // Send 0 when below threshold (device is level)
            tiltListener.onTilt(0);
        }
    }

    private float[] applyLowPassFilter(float[] previous, float[] current) {
        if (previous == null) {
            return current.clone();
        }
        for (int i = 0; i < previous.length; i++) {
            previous[i] = previous[i] + ALPHA * (current[i] - previous[i]);
        }
        return previous;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no need to implement this atm
    }

    public void register()
    {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister()
    {
        sensorManager.unregisterListener(this);
    }
}
