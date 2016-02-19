package com.savinoordine.letsfloating;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

    public void setSensorCoords(View view) {
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

    public void setPercentageCoords(View view) {
        mFloatingView = view;
        mFloatingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                LayoutInflater inflater = mActivity.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_percentage, null);

                final EditText xTextValue = (EditText) dialogView.findViewById(R.id.x_percentage);
                final EditText yTextValue = (EditText) dialogView.findViewById(R.id.y_percentage);

                builder.setView(dialogView)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                int x = (mScreenInfo.getScreenWidth() * Integer.valueOf(xTextValue.getText().toString())) / 100;
                                int y = (mScreenInfo.getScreenHeight() * Integer.valueOf(yTextValue.getText().toString())) / 100;

                                animateView(x, y);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
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

        if (mCurrentTime + 500 < System.currentTimeMillis()) {
            float orientation[] = new float[3];
            if (mGravity != null && mGeomagnetic != null) {
                mCurrentTime = System.currentTimeMillis();
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    SensorManager.getOrientation(R, orientation);
                }
            }

            int xCoord = getXCoord(orientation[2]);
            int yCoord = getYCoord(orientation[1]);
            animateView(xCoord, yCoord);
        }
    }

    private void animateView(int x,int y) {
        mFloatingView.animate().x(x).y(y).setDuration(500);
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
