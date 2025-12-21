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

    private static final String TAG = "TiltSensor";

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;

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
        tiltListener.onTilt(rollDeg);
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
