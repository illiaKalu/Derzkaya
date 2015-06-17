package com.dev.illiakaliuzhnyi.derzkaya;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.coremedia.iso.boxes.Container;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookDialog;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;



public class ResultActivity extends Activity {

    VideoView videoView;

    File DCIMdir;

    Uri videoFileUri;
    
    AudioManager audioManager;

    ImageButton save_button;

    ImageButton backToMain;

    CallbackManager callbackManager;
    ShareDialog shareDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        DCIMdir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File file = new File(DCIMdir + "/myvideo.3gp");
        videoFileUri = Uri.fromFile(file);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        // this part is optional
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });


        save_button = (ImageButton) findViewById(R.id.save_button);

        save_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    save_button.setBackground(getDrawable(R.drawable.save_off));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    save_button.setBackground(getDrawable(R.drawable.save_on));
                    // Do save actions
                }

                return true;
            }
        });
        backToMain = (ImageButton) findViewById(R.id.back_to_main_button);

        backToMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    backToMain.setBackground(getDrawable(R.drawable.back_off));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    backToMain.setBackground(getDrawable(R.drawable.back_on));
                    onBackPressed();
                }

                return true;
            }
        });


        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoPath(DCIMdir + "/myvideo.3gp");


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!videoView.isPlaying()) {

                    videoView.start();

                } else {

                    videoView.pause();

                }


                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
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

    public void shareOnFacebook(View view) throws IOException {

        // share on facebook button realization

        // TEMPORARY!

   /*     File DCIMdir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String audiom4a = DCIMdir + "/" + "derzkaya1.m4a";
        String video = DCIMdir + "/" + "myvideo.3gp";
        String output3gp = DCIMdir + "/" + "ouput.3gp";



        new AddAudioToVideoAsyncTask(audiom4a).execute();

        //mergeVideoAndAudio();
    */

        if (ShareDialog.canShow(ShareVideoContent.class)) {
            ShareVideo video = new ShareVideo.Builder()
                    .setLocalUrl(videoFileUri)
                    .build();

            ShareVideoContent content = new ShareVideoContent.Builder()
                    .setVideo(video)
                    .build();

            shareDialog.show(content);
        }


    }

    private void mergeVideoAndAudio() throws IOException {

        Log.d("My_TAG", "START MUX");
        Mp4ParserAudioMuxer mp4parser = new Mp4ParserAudioMuxer();

        String audioPath = "/storage/emulated/0/DCIM/derzkayaaac.aac";
        String videoPath = "/storage/emulated/0/DCIM/myvideo.3gp";

        mp4parser.mux(videoPath, audioPath, DCIMdir + "/videowithsound.mp4");

        Log.d("My_TAG", audioPath + " - working with this audio");
        Log.d("My_TAG", videoPath + " - working with this video");
        Log.d("My_TAG", DCIMdir + "/videowithsound.mp4" + "   - Directory of output");

        Log.d("My_TAG", "I AM DONE");

    }

    private boolean mux(String videoFile, String audioFile, String outputFile) {
        Movie video;


        try {
            video = new MovieCreator().build(videoFile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Movie audio;
        try {
            audio = new MovieCreator().build(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        Log.d("MY", "done with init");


        Track audioTrack = audio.getTracks().get(0);
        Track videoTrack = video.getTracks().get(0);



        Movie movie = new Movie();
        movie.addTrack(audioTrack);
        movie.addTrack(videoTrack);


        Container out = new DefaultMp4Builder().build(video);


        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Log.d("MY", "done all");
        return true;
    }

    private static class BufferedWritableFileByteChannel implements WritableByteChannel {
        private static final int BUFFER_CAPACITY = 100000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) throws IOException {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);


            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() throws IOException {
            dumpToFile();
            isOpen = false;
        }
        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



}