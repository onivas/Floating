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
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private TextView mScreenSizeTwxtView;
    private int[] viewCoords = new int[2];
    private FloatingActionButton fab;
    private boolean floatingOn = false;
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getScreenSize();

        mScreenSizeTwxtView = (TextView) findViewById(R.id.screen_size);
        mScreenSizeTwxtView.setText("screenWidth: " + screenWidth
                + " screenHeight: " + screenHeight);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Please, don't touch me!", Toast.LENGTH_LONG).show();
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

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    private void registerSensor() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Toast.makeText(this, "Sensor not available", Toast.LENGTH_LONG).show();
            return;
        }

        fab.getLocationOnScreen(viewCoords);

        // MAX VALUE: x  is 70,  y is 150
        int xAbs = (int) Math.abs(event.values[2]);
        int yAbs = (int) Math.abs(event.values[1]);

        int xCoord = (xAbs * screenWidth) / 70;
        int yCoord = (yAbs * screenHeight) / 150;

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
