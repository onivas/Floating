package com.savinoordine.floating;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private boolean floatingOn = false;

    private int screenWidth;
    private int screenHeight;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    int xCoord;
    int yCoord;

    private FloatingActionButton fab;
    private TextView mViewcoords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getScreenSize();

        mViewcoords = (TextView) findViewById(R.id.view_coords);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Please, don't touch me!", Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (floatingOn) {
                    unregisterSensor();
                } else {
                    registerSensor();
                }
                floatingOn = !floatingOn;
                return true;
            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    private void registerSensor() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // @// TODO: 2/6/2016 reset default button coords
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    float[] mGravity = null;
    float[] mGeomagnetic = null;

    @Override
    public void onSensorChanged(SensorEvent event) {

        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Toast.makeText(this, "Sensor not available", Toast.LENGTH_LONG).show();
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

        float z = orientation[2];
        float y = orientation[1];
        if (z <= 0) {
            xCoord = (int) ((( screenWidth/2 ) / (-1.5) ) * z);
        } else {
            xCoord = (int) ((( screenWidth ) / (1.5) ) * z);
        }

        if (y <= 0) {
            yCoord = (int) ((( screenHeight ) / (-1.5))  * y);
        } else {
            yCoord = (int) ((( screenHeight/2 ) / (1.5) ) * y);
        }

        mViewcoords.setText("x: " + xCoord + " y: " + yCoord);
        fab.animate().x(xCoord).y(yCoord);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        unregisterSensor();
        super.onPause();
    }

    private void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }
}
