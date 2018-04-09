package edu.rit.ad8454.capstonerit;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by Ajinkya on 3/28/2018.
 */

public class Sensors implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor rotationSensor;
    private float[] sensorRotationMatrix = new float[16];
    private float[] temp = new float[16];
    private int worldAxisForDeviceAxisX;
    private int worldAxisForDeviceAxisY;

    public Sensors (Context context, WindowManager windowManager) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Log.e("is null?", (mSensorManager == null) + "");
        Log.e("is null?", (rotationSensor == null) + "");
        setWorldAxis(windowManager);
        // initialize the rotation matrix to identity
        temp[ 0] = 1;
        temp[ 4] = 1;
        temp[ 8] = 1;
        temp[12] = 1;
    }

    private void setWorldAxis(WindowManager windowManager) {
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Y;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Y;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Y;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Y;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }
    }

    public float[] getSensorRotationMatrix() {
        return sensorRotationMatrix;
    }

    public void startListener() {
        Log.e("set vals", "start listener");
        mSensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopListener() {
        mSensorManager.unregisterListener(this);
        Log.e("set vals", "stop listener");
    }

    private void setSensorRotationValues(float[] vector) {
        SensorManager.getRotationMatrixFromVector(temp, vector);
        SensorManager.remapCoordinateSystem(temp, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, sensorRotationMatrix);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.e("sensor", "set vals " + event.values[0] + "  " + event.accuracy);
            setSensorRotationValues(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
