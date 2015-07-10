package com.dev.IDAP.derzkaya;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;

import android.widget.ImageButton;
import android.widget.ProgressBar;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class RecordActivity extends Activity {

    private Boolean isRecording = false;

    private Chronometer chronometer;

    private SurfaceView surfaceView;
    private Camera camera;

    private MediaRecorder videoMediaRecorder;
    private MediaRecorder audioMediaRecorder;
    private MediaPlayer mediaPlayer;

    private Thread progressThread;
    private ProgressBar mProgress;
    private Handler mHandler = new Handler();
    private boolean progressBarUpdate = false;

    private ImageButton microphone_button;
    private ImageButton recordButton;
    private ImageButton backToMain;

    private File recordedVideoFile;
    private File recordedAudioFile;
    private String recordedAudioFileName = "/AudioResultFromMic.m4a";
    private String recordedVideoFileName = "/VideoResultFromCamera.mp4";


    private String MY_LOG_TAG = "MY";

    static ProgressDialog dialog ;


    ArrayList<TimeStamps> timeStampses = new ArrayList<TimeStamps>();
    private long currentTimeInMillis;
    private String musicRawFileName = "/derzkayam4a.m4a";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        recordButton = (ImageButton) findViewById(R.id.record_button);
        backToMain = (ImageButton) findViewById(R.id.back_to_main);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        camera = Camera.open();

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

        });

        // !!
        mediaPlayer = MediaPlayer.create(RecordActivity.this, R.raw.derzkayam4a);

        progressThread = new Thread(new Runnable() {

            int mProgressStatus = 0;

            public void run() {
                while (progressBarUpdate) {
                    android.os.SystemClock.sleep(50); // Thread.sleep() doesn't guarantee 50 msec sleep, it can be interrupted before! 1000 / 50 = 20; 20 * 23 = 460 (!) - max progress bar size
                    // Update progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgress.setProgress(mProgressStatus);
                        }
                    });
                    mProgressStatus ++;
                }
            }
        });

        microphone_button = (ImageButton) findViewById(R.id.microphoneAction);

        microphone_button.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                microphone_button.setBackgroundResource(R.drawable.micro_on);


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    timeStampses.add(new TimeStamps("M", System.currentTimeMillis() - currentTimeInMillis));
                    mediaPlayer.setVolume(0, 0);


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    microphone_button.setBackgroundResource(R.drawable.micro_off);
                    timeStampses.add(new TimeStamps("T", System.currentTimeMillis() - currentTimeInMillis));
                    mediaPlayer.setVolume(1, 1);
                }

                return true;
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startRecord(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        backToMain.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    backToMain.setBackgroundResource(R.drawable.back_off);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    backToMain.setBackgroundResource(R.drawable.back_on);
                    onBackPressed();
                }


                return true;
            }
        });


        recordedVideoFile = new File(MainActivity.baseExternalDirectory, recordedVideoFileName);

        recordedAudioFile = new File (MainActivity.baseExternalDirectory, recordedAudioFileName);

    }


    private void startRecord(View view) throws IOException {

        recordButton.setBackgroundResource(R.drawable.stop);


        if(isRecording) {

            isRecording = false;
            progressBarUpdate = false;

            // create and show progress dialog, which will wait for merging stuff done
            // will be dissmissed by addAudioToVideoAsyncTask class

            if (chronometer != null & mediaPlayer != null)
            new StopRecordersAndPlayers(chronometer, mediaPlayer).execute();


            if (videoMediaRecorder != null) {
                videoMediaRecorder.stop();
                audioMediaRecorder.stop();
            }


            new AddAudioToVideoAsyncTask(recordedAudioFile, recordedVideoFile, loadMusicFileFromRawFolder(), timeStampses).execute();


            dialog = new ProgressDialog(this);
            dialog.setMessage(" Prepearing your video ! It could take a while . . .");
            dialog.show();

            Intent intent = new Intent(this, ResultActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

            releaseVideoAudioMediaRecorders();

        }else{

            isRecording = true;
            progressBarUpdate = true;


            if (chronometer != null & mediaPlayer != null)
            new StartRecordersAndPlayers(chronometer, mediaPlayer, progressThread).execute();


            if (prepareVideoAudioRecorders()) {
                videoMediaRecorder.start();
                currentTimeInMillis = System.currentTimeMillis();
                audioMediaRecorder.start();
            }else{
                releaseVideoAudioMediaRecorders();
            }

             mediaPlayer.start();

        }

  }

    private File loadMusicFileFromRawFolder() {
        InputStream in = getResources().openRawResource(R.raw.derzkayam4a);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(MainActivity.baseExternalDirectory + musicRawFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return new File(MainActivity.baseExternalDirectory + musicRawFileName);
    }

    private boolean prepareVideoAudioRecorders() {

        camera.unlock();

            videoMediaRecorder = new MediaRecorder();
            audioMediaRecorder = new MediaRecorder();

            videoMediaRecorder.setCamera(camera);

            videoMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // every phone has LOW profile.
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH)) {
            videoMediaRecorder.setProfile(CamcorderProfile
                    .get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        }else{
            videoMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_LOW));
        }

            videoMediaRecorder.setOrientationHint(90);
            videoMediaRecorder.setOutputFile(recordedVideoFile.getAbsolutePath());
            videoMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

            videoMediaRecorder.setMaxDuration(23000);

            videoMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {

                        isRecording = false;
                        progressBarUpdate = false;

//                        Intent intent = new Intent(RecordActivity.this, ResultActivity.class);
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

                        //new StopRecordersAndPlayers(chronometer, audioRecord, mediaPlayer).execute();


                        if (videoMediaRecorder != null & audioMediaRecorder != null) {
                            videoMediaRecorder.stop();
                            audioMediaRecorder.stop();
                            releaseVideoAudioMediaRecorders();
                        }

                        Log.d("MY", "STATUS 23 REACHED !");
                        //new AddAudioToVideoAsyncTask(recorderedAudioFile).execute();

                        finish();

                    }
                }
            });

            audioMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            audioMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            audioMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            audioMediaRecorder.setAudioSamplingRate(44100);
            audioMediaRecorder.setAudioEncodingBitRate(256000);
            audioMediaRecorder.setOutputFile(recordedAudioFile.getAbsolutePath());

        try {
            videoMediaRecorder.prepare();
            audioMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseVideoAudioMediaRecorders();
            return false;
        }
        return true;
    }

    private void releaseVideoAudioMediaRecorders() {
        if (videoMediaRecorder != null) {
            videoMediaRecorder.reset();
            videoMediaRecorder.release();
            videoMediaRecorder = null;
            camera.lock();
        }
        if (audioMediaRecorder != null){
            audioMediaRecorder.reset();
            audioMediaRecorder.release();
            audioMediaRecorder = null;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MY", "ON STOP !");
        //finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseVideoAudioMediaRecorders();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        finish();
    }

}
