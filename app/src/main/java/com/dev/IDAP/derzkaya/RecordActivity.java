package com.dev.IDAP.derzkaya;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


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
    ImageButton recordButton;
    ImageButton backToMain;




    // names of the resourses must be lower case !
    String recorderedVoice = MainActivity.baseExternalDirectory.getAbsolutePath() + "/recorderedvoice.wav";
    String capturedVideo = "capturedvideo.3gp";

    String MY_LOG_TAG = "MY";

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;

    boolean microphoneDown = false;

    // optimal buffer size
    private int voiceBufferSize = 8192;
    byte[] voiceBuffer = new byte[voiceBufferSize];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        recordButton = (ImageButton) findViewById(R.id.record_button);
        backToMain = (ImageButton) findViewById(R.id.back_to_main);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        // !!
        mediaPlayer = MediaPlayer.create(RecordActivity.this, R.raw.derzkaya);

        progressThread = new Thread(new Runnable() {

            int mProgressStatus = 0;
            public void run() {
                while (true) {
                    android.os.SystemClock.sleep(50); // Thread.sleep() doesn't guarantee 50 msec sleep, it can be interrupted before! 1000 / 50 = 20; 20 * 23 = 460 (!) - max progress bar size
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

        microphone_button = (ImageButton) findViewById(R.id.microphoneAction);

        microphone_button.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                microphone_button.setBackgroundResource(R.drawable.micro_on);


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    microphoneDown = true;
                    mediaPlayer.setVolume(0, 0);


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    microphoneDown = false;
                    microphone_button.setBackgroundResource(R.drawable.micro_off);
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

        File workingDir = MainActivity.baseExternalDirectory;

        videoFile = new File(workingDir, capturedVideo); //
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

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

                //camera.setDisplayOrientation(90);


            }


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        finish();
    }


    private void startRecord(View view) throws IOException {

         recordButton.setBackgroundResource(R.drawable.stop);

         audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
                 RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, voiceBufferSize);

        if(isRecording) {

            isRecording = false;

            chronometer.stop();
            mediaPlayer.stop();
            audioRecord.stop();
//            progressThread.destroy();

            new AddAudioToVideoAsyncTask(recorderedVoice).execute();

            Intent intent = new Intent(this, ResultActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

            if (mediaRecorder != null) {
                mediaRecorder.stop();
                releaseMediaRecorder();
            }

            mediaPlayer.release();
            audioRecord.release();
            finish();

        }else{

            isRecording = true;

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            progressThread.start();

            audioRecord.startRecording();
            startReadAudioRecord();


            if (prepareVideoRecorder()) {
                mediaRecorder.start();
            }else{
                releaseMediaRecorder();
            }
            mediaPlayer.start();

        }

  }

    private void startReadAudioRecord() throws IOException {

       final InputStream fin = getResources().openRawResource(R.raw.derzkaya);
       final FileOutputStream fos = new FileOutputStream(recorderedVoice);

        // do not need AsyncTask, just a simple Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (audioRecord == null)
                    return;

                while (isRecording) {
                    if(microphoneDown) {
                        try {
                            fin.read(voiceBuffer,0,voiceBufferSize);
                            audioRecord.read(voiceBuffer, 0, voiceBufferSize);
                            fos.write(voiceBuffer, 0, voiceBufferSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else{
                        try {
                            audioRecord.read(voiceBuffer, 0, voiceBufferSize);
                            fin.read(voiceBuffer,0,voiceBufferSize);
                            fos.write(voiceBuffer,0,voiceBufferSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                try {

                    fin.close();
                    fos.flush();
                    fos.close();
                    Log.d("MY", " CLOSED !");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }



    private boolean prepareVideoRecorder() {

        camera.unlock();

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setCamera(camera);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // every phone has LOW profile.
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH)) {
            mediaRecorder.setProfile(CamcorderProfile
                    .get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        }else{
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_LOW));
        }


        mediaRecorder.setOrientationHint(90);

        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());


        mediaRecorder.setMaxDuration(23000);

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){

                    chronometer.stop();

                    if (mediaRecorder != null) {
                        mediaRecorder.stop();
                        releaseMediaRecorder();
                    }

                    mediaPlayer.stop();
                    mediaPlayer.release();

                    new AddAudioToVideoAsyncTask(recorderedVoice).execute();



                    Intent intent = new Intent(RecordActivity.this, ResultActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);


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
