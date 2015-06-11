package com.dev.illiakaliuzhnyi.derzkaya;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;

import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;


public class RecordActivity extends Activity {

    Chronometer chronometer;
    Boolean isRecording = false;
    SurfaceView surfaceView;
    Camera camera;
    MediaRecorder mediaRecorder;
    Thread progressThread;
    private ProgressBar mProgress;
    private Handler mHandler = new Handler();
    MediaPlayer mediaPlayer;
    File videoFile;
    ImageButton microphone_button;
    AudioManager audioManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);



        // !!
        mediaPlayer = MediaPlayer.create(RecordActivity.this, R.raw.derzkaya);

        File DCIMdir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        videoFile = new File(DCIMdir, "myvideo.mp4");
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

                //camera.setDisplayOrientation(90);

                microphone_button = (ImageButton) findViewById(R.id.microphoneAction);

                microphone_button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            Log.d("MY_TAG", "LOL i am pressed");

                            audioManager.setMicrophoneMute(true);

                            mediaPlayer.setVolume(0, 0);


                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("MY_TAG", "LOL i am released");

                            audioManager.setMicrophoneMute(false);

                            mediaPlayer.setVolume(1, 1);

                        }

                        return true;
                    }
                });


                try {

                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                    camera.setDisplayOrientation(90);
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
//                camera.release();
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
        mediaPlayer.release();
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

        if(isRecording) {

            mediaPlayer.stop();
            mediaPlayer.release();
            chronometer.stop();
            //progressThread.destroy();

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
            mediaPlayer.start();
            isRecording = true;
        }



    }
    private boolean prepareVideoRecorder() {

        camera.unlock();

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setCamera(camera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mediaRecorder.setOrientationHint(90);

        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());


        mediaRecorder.setMaxDuration(23000); // delayed for 1 sec because of SMTH!

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){

                    if (mediaRecorder != null) {
                        mediaRecorder.stop();
                        releaseMediaRecorder();
                    }

                    mediaPlayer.stop();
                    mediaPlayer.release();

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
