package com.dev.IDAP.derzkaya;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends Activity {

    Button toRecordActivityButton;

    static File baseExternalDirectory;

    String MY_LOG_TAG = "MY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // creating static base dir
        if(isExternalStorageWritable()){

            baseExternalDirectory = getExternalFilesDir(null);
            Log.d(MY_LOG_TAG, "DIR - " + baseExternalDirectory.getAbsolutePath());

        }else{
            Toast.makeText(this, "SDCard is not available, application will NOT work properly!", Toast.LENGTH_SHORT).show();
        }

        // setting animation effects
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

        // finding action button and creating listener for it
        toRecordActivityButton = (Button) findViewById(R.id.to_recordActivity_button);

        toRecordActivityButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    toRecordActivityButton.setBackgroundResource((R.drawable.record_)); // find out methods, which works properly on old APIs!!
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    toRecordActivityButton.setBackgroundResource(R.drawable.record);
                    Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                }
                return true;
            }
        });

    }

    // check external storage availability
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

}
