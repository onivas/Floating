package com.savinoordine.floating;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.savinoordine.letsfloating.LetsFloating;

public class MainActivity extends AppCompatActivity {
    private LetsFloating mLetsFloating = new LetsFloating();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLetsFloating.init(this);

        Button button = (Button) findViewById(R.id.button);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Please, don't touch me!", Toast.LENGTH_SHORT).show();
            }
        });

        boolean isOriginalFabCoords = mLetsFloating.setSensorCoords(fab);
        if (!isOriginalFabCoords) {
            mLetsFloating.restoreLastCoords(fab);
        }

        boolean isOriginalButtonCoords = mLetsFloating.setPercentageCoords(button);
        if (!isOriginalButtonCoords) {
            mLetsFloating.restoreLastCoords(button);
        }
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
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            preferences.edit().clear().apply();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        mLetsFloating.unregisterSensor();
        super.onPause();
    }
}
