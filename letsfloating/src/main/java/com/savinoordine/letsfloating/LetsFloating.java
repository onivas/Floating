package com.savinoordine.letsfloating;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class LetsFloating implements SensorEventListener {

    private boolean mIsFloating = false;
    private Activity mActivity;
    private View mFloatingView;
    private ScreenInfo mScreenInfo;

    float[] mGravity = null;
    float[] mGeomagnetic = null;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    public void init(Activity activity) {
        mActivity = activity;
        mSensorManager = (SensorManager)mActivity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mScreenInfo = new ScreenInfo(mActivity);
    }

    public void setListener(View view) {
        mFloatingView = view;
        mFloatingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mIsFloating) {
                    unregisterSensor();
                } else {
                    registerSensor();
                }
                mIsFloating = !mIsFloating;
                return true;
            }
        });
    }

    public void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    private void registerSensor() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int xCoord;
        int yCoord;

        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Toast.makeText(mActivity, "Sensor not available", Toast.LENGTH_LONG).show();
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        float orientation[] = new float[3];

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(R, orientation);
            }
        }

        /*
        aumenta z a destra,
        diminuisce z a sx

        diminuisce y sotto
        aumenta y sopra
        */


        float y = orientation[2];
        float z = orientation[1];
        String s = "z: " + z + " - y: " + y;
        Log.d(">>>>>" ,s);
        if (z <= 0) {
            xCoord = (int) (( mScreenInfo.getScreenWidth() * z) / (-1.5));
        } else {
            xCoord = (int) ((( mScreenInfo.getScreenWidth() ) / (1.5) ) * z);
        }

        if (y <= 0) {
            yCoord = (int) ((( mScreenInfo.getScreenHeight() ) / (-1.5))  * y);
        } else {
            yCoord = (int) ((( mScreenInfo.getScreenHeight() ) / (1.5) ) * y);
        }

        s = "H:" + mScreenInfo.getScreenHeight()+ " W:" + mScreenInfo.getScreenWidth() +
                "xCoord: " + xCoord + " - yCoord: " + yCoord;
        Log.d(">>>>>" ,s);
        mFloatingView.animate().x(xCoord).y(yCoord);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
