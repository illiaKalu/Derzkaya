package com.dev.illiakaliuzhnyi.derzkaya;

import android.app.Activity;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;


public class RecordActivity extends Activity {

    Chronometer chronometer;
    Boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void backToMainActivity(View view) {
        finish();
    }

    public void startRecord(View view) {

        if(isRecording){
            chronometer.stop();
            isRecording = false;

            Intent startActivityResult = new Intent(this, ResultActivity.class);
            startActivity(startActivityResult);
        }else{
            chronometer = (Chronometer) findViewById(R.id.chronometer);
            chronometer.start();
            isRecording = true;
        }


    }
}
