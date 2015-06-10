package com.dev.illiakaliuzhnyi.derzkaya;

import android.app.Activity;
import android.graphics.Movie;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ResultActivity extends Activity {

    VideoView videoView;

    File DCIMdir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);



        DCIMdir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);



        videoView = (VideoView)findViewById(R.id.videoView);
        videoView.setVideoPath(DCIMdir + "/myvideo.mp4");


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

               if(!videoView.isPlaying()){

                   videoView.start();

               }else{

                   videoView.pause();

               }



                return false;
            }
        });

    }

    private void mergeVideoAndAudio() throws IOException {

        Log.d("My_TAG", "START MUX");
        Mp4ParserAudioMuxer mp4parser = new Mp4ParserAudioMuxer();

        String audioPath = "/storage/emulated/0/DCIM/derzkayaaac.aac";
        String videoPath = "/storage/emulated/0/DCIM/myvideo.mp4";

        mp4parser.mux(videoPath, audioPath, DCIMdir + "/videowithsound.mp4");

        Log.d("My_TAG", audioPath + " - working with this audio");
        Log.d("My_TAG", videoPath + " - working with this video");
        Log.d("My_TAG", DCIMdir + "/videowithsound.mp4" + "   - Directory of output");

        Log.d("My_TAG", "I AM DONE");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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

    public void saveResult(View view) {

        // create a new music file ( mix audio record + derzkaya.wav)
        // add new music file to tape and save
    }

    public void shareOnFacebook(View view) throws IOException {

        // share on facebook button realization

        // TEMPORARY!
       // mergeVideoAndAudio();


}
    }