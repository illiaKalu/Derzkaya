package com.dev.illiakaliuzhnyi.derzkaya;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;

import android.widget.ProgressBar;


import java.io.File;



public class RecordActivity extends Activity {

    Chronometer chronometer;
    Boolean isRecording = false;
    SurfaceView surfaceView;
    Camera camera;
    MediaRecorder mediaRecorder;
    Thread progressThread;
    private ProgressBar mProgress;
    private Handler mHandler = new Handler();

    File videoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);




        File DCIMdir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        videoFile = new File(DCIMdir, "myvideo.3gp");
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                progressThread = new Thread(new Runnable() {
                    int mProgressStatus = 0;
                    public void run() {
                        while (mProgressStatus < 24) {
                            android.os.SystemClock.sleep(1000); // Thread.sleep() doesn't guarantee 1000 msec sleep, it can be interrupted before
                            // Update the progress bar
                            mHandler.post(new Runnable() {
                                public void run() {
                                    mProgress.setProgress(mProgressStatus);
                                }
                            });
                            mProgressStatus ++;
                        }
                    }
                });

                camera.setDisplayOrientation(90);


                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //camera.release();
            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
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

            if (mediaRecorder != null) {
                mediaRecorder.stop();
                releaseMediaRecorder();
            }

            Intent startActivityResult = new Intent(this, ResultActivity.class);
            startActivity(startActivityResult);
            finish();
        }else{

            if (prepareVideoRecorder()) {
                mediaRecorder.start();
            } else {
                releaseMediaRecorder();
            }

            chronometer = (Chronometer) findViewById(R.id.chronometer);
            mProgress = (ProgressBar) findViewById(R.id.progressBar);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            progressThread.start();
            isRecording = true;
        }



    }
    private boolean prepareVideoRecorder() {

        camera.unlock();

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

        mediaRecorder.setMaxDuration(23000);
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){

                    if (mediaRecorder != null) {
                        mediaRecorder.stop();
                        releaseMediaRecorder();
                    }

                    Intent startActivityResult = new Intent(RecordActivity.this, ResultActivity.class);
                    startActivity(startActivityResult);

                    finish();

                }
            }
        });

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

}
