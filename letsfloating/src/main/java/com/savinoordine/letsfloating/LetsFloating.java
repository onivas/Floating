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

    private long mCurrentTime = System.currentTimeMillis();

    public void init(Activity activity) {
        mActivity = activity;
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
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
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
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

        if (mCurrentTime + 500 < System.currentTimeMillis()) {
            if (mGravity != null && mGeomagnetic != null) {
                mCurrentTime = System.currentTimeMillis();
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    SensorManager.getOrientation(R, orientation);
                }
            }

            yCoord = getYCoord(orientation[1]);
            xCoord = getXCoord(orientation[2]);
            mFloatingView.animate().x(xCoord).y(yCoord).setDuration(500);
        }
    }

    private int getXCoord(float param) {
        int xCoord;
        if (param >= 0 && param < 1) {
            xCoord = (int) (mScreenInfo.getScreenWidth() * (param));
        } else if (param < 0) {
            xCoord = 0;
        } else {
            xCoord = mScreenInfo.getScreenWidth();
        }
        return xCoord;
    }

    private int getYCoord(float param) {
        int yCoord;
        if (param < -1) {
            yCoord = (int) mScreenInfo.getScreenHeight();
        } else if (param < 0) {
            yCoord = (int) (mScreenInfo.getScreenHeight() * (-param));
        } else {
            yCoord = 0;
        }
        return yCoord;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
