package com.savinoordine.letsfloating;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LetsFloating implements SensorEventListener {

    private boolean mIsFloating = false;
    private Activity mActivity;
    private View mSensorFloatingView;
    private View mPercentageFloatingView;
    private ScreenInfo mScreenInfo;

    float[] mGravity = null;
    float[] mGeomagnetic = null;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private long mCurrentTime = System.currentTimeMillis();

    private static final String VIEW_X_COORD = "x";
    private static final String VIEW_Y_COORD = "y";

    public void init(Activity activity) {
        mActivity = activity;
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mScreenInfo = new ScreenInfo(mActivity);
    }

    public boolean setSensorCoords(final View view) {
        mSensorFloatingView = view;
        mSensorFloatingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mIsFloating) {
                    unregisterSensor();
                    saveNewPosition(mSensorFloatingView, view.getX(), view.getY());
                } else {
                    registerSensor();
                }
                mIsFloating = !mIsFloating;
                return true;
            }
        });

        return isOriginalPosition(view);
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

            mSensorFloatingView.animate().x(xCoord).y(yCoord).setDuration(500);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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

    public boolean setPercentageCoords(View view) {
        mPercentageFloatingView = view;
        mPercentageFloatingView.setOnLongClickListener(new View.OnLongClickListener() {
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
                                boolean xMove = false;
                                boolean yMove = false;
                                int x = 0;
                                int y = 0;

                                String xVal = xTextValue.getText().toString();
                                if (!xVal.isEmpty()) {
                                    Integer xInteger = Integer.valueOf(xVal);
                                    if (xInteger >= 0 && xInteger <= 100) {
                                        x = (mScreenInfo.getScreenWidth() * xInteger) / 100;
                                        xMove = true;
                                    }
                                }

                                String yVal = yTextValue.getText().toString();
                                if (!yVal.isEmpty()) {
                                    Integer yInteger = Integer.valueOf(yVal);
                                    if (yInteger >= 0 && yInteger <= 100) {
                                        y = (mScreenInfo.getScreenHeight() * yInteger) / 100;
                                        yMove = true;
                                    }
                                }

                                percentageViewAnimation(xMove, yMove, x, y);

                                saveNewPosition(mPercentageFloatingView, x, y);
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

        return isOriginalPosition(view);
    }

    private void percentageViewAnimation(boolean xMove, boolean yMove, int x, int y) {
        int xOffset = 0;
        int yOffset = 0;

        int viewWidth = mPercentageFloatingView.getWidth();
        int viewHeight = mPercentageFloatingView.getHeight();

        if (viewWidth + x > mScreenInfo.getScreenWidth()) {
            xOffset = viewWidth;
        }

        if (viewHeight > y ) {
            yOffset = -viewWidth;
        }

        if (viewHeight + y > mScreenInfo.getScreenHeight()) {
            yOffset = viewWidth;
        }

        x = x - xOffset;
        y = y - yOffset;

        if (xMove && yMove) {
            mPercentageFloatingView.animate().x(x).y(y);
            return;
        }

        if (xMove) {
            mPercentageFloatingView.animate().x(x);
            return;
        }

        if (yMove) {
            mPercentageFloatingView.animate().y(y);
            return;
        }
    }

    private void saveNewPosition(View view, float xCoord, float yCoord) {
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        int orientationValue = view.getContext().getResources().getConfiguration().orientation;

        String id = view.getResources().getResourceName(view.getId())
                .concat(String.valueOf(orientationValue));
        String x = id.concat(VIEW_X_COORD);
        String y = id.concat(VIEW_Y_COORD);

        editor.putString(id, id);
        editor.putFloat(x, xCoord);
        editor.putFloat(y, yCoord);
        editor.apply();
    }

    private boolean isOriginalPosition(View view) {
        int orientationValue = view.getContext().getResources().getConfiguration().orientation;

        String id = view.getResources().getResourceName(view.getId())
                .concat(String.valueOf(orientationValue));
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        String originalPosition = sharedPref.getString(id, null);

        if (originalPosition == null) {
            return true;
        }
        return false;
    }

    public void restoreLastCoords(View view) {
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

        int orientationValue = view.getContext().getResources().getConfiguration().orientation;

        String id = view.getResources().getResourceName(view.getId())
                .concat(String.valueOf(orientationValue));

        if (orientationValue != -1) {

            String x = id.concat(VIEW_X_COORD);
            String y = id.concat(VIEW_Y_COORD);

            float xCoord = sharedPref.getFloat(x, -1);
            float yCoord = sharedPref.getFloat(y, -1);

            if (xCoord != -1 && yCoord != -1) {
                if (orientationValue == 1 || orientationValue == 2) {
                    view.animate().x(xCoord).y(yCoord).start();
                }
            }
        }
    }
}
