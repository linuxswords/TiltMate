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
    private static final int THRESHOLD_LOW = 20;      // Low sensitivity (0)
    private static final int THRESHOLD_MEDIUM = 10;   // Medium sensitivity (1)
    private static final int THRESHOLD_HIGH = 5;      // High sensitivity (2)

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private int sensitivityThreshold = THRESHOLD_MEDIUM; // Default: medium

    // create constructor with context as argument
    public TiltSensor(Context context)
    {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Log.d(TAG, "accelerometer is null");
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
            Log.d(TAG, "magnetometer is null");
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.values == null) {
            Log.w(TAG, "event.values is null");
            return;
        }
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                break;
            default:
                Log.w(TAG, "Unknown sensor type " + sensorType);
                return;
        }
        if (mGravity == null) {
            Log.w(TAG, "mGravity is null");
            return;
        }
        if (mGeomagnetic == null) {
            Log.w(TAG, "mGeomagnetic is null");
            return;
        }
        float[] r = new float[9];
        if (!SensorManager.getRotationMatrix(r, null, mGravity, mGeomagnetic)) {
            Log.w(TAG, "getRotationMatrix() failed");
            return;
        }

        float[] orientation = new float[9];
        SensorManager.getOrientation(r, orientation);
        // Orientation contains: azimuth, pitch and roll - we'll use pitch
        float roll = orientation[1];
        int rollDeg = (int) Math.round(Math.toDegrees(roll));

        // Only notify listener if tilt exceeds the sensitivity threshold
        if (Math.abs(rollDeg) >= sensitivityThreshold) {
            tiltListener.onTilt(rollDeg);
        } else {
            // Send 0 when below threshold (device is flat)
            tiltListener.onTilt(0);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no need to implement this atm
    }

    public void register()
    {
        // call sensor manger's register listener and pass the required arguments
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // create method to unregister
    // from sensor notifications
    public void unregister()
    {
        // call sensor manger's unregister listener
        // and pass the required arguments
        sensorManager.unregisterListener(this);
    }
}
