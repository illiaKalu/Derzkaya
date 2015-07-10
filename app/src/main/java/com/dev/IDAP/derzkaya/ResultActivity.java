package com.dev.IDAP.derzkaya;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;


import java.io.File;
import java.io.IOException;


public class ResultActivity extends Activity {

    VideoView videoView;
    Uri videoFileUri;

    ImageButton save_button;
    ImageButton backToMain;

    CallbackManager callbackManager;
    ShareDialog shareDialog;
    private String errorMsg = "whooops! Internet connection failed OR you do NOT have facebook app.";

    private String videoSavedMsg = "Video saved to SD card";
    private int pausedPossition;
    private boolean off;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // init facebook SDK, callback manager and share dialog

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(ResultActivity.this);
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

                // changing buttons background
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    save_button.setBackgroundResource(R.drawable.save_off);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    save_button.setBackgroundResource(R.drawable.save_on);

                    // preventing multiple click situation
                    save_button.setEnabled(false);

                    Toast.makeText(ResultActivity.this, videoSavedMsg, Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
        backToMain = (ImageButton) findViewById(R.id.back_to_main_button);

        // back to main activity button
        backToMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // changing background of a back button
                    backToMain.setBackgroundResource(R.drawable.back_off);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    backToMain.setBackgroundResource(R.drawable.back_on);
                    onBackPressed();
                }

                return true;
            }
        });


        videoView = (VideoView) findViewById(R.id.videoView);

        // video show logic
        videoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // decided from witch file grub a video source to show
                // depends on was Save method called or not
                if (!off) videoView.setVideoPath(AddAudioToVideoAsyncTask.resultVideo);

                if(!videoView.isPlaying()) {
                     videoView.start();
                     off = true;
                }else {
                    if (videoView.isPlaying())
                        videoView.pause();

                }

                return false;
            }
        });

    }

    // sharing logic
    public void shareOnFacebook(View view) throws IOException {

        // decided from witch file grub a video source to share
        // depends on was Save method called or not
        videoFileUri = Uri.parse("file://" + AddAudioToVideoAsyncTask.resultVideo);

        Log.d("MY", "FACEBOOK PATH - " + videoFileUri);

        if(checkFacebookAppAvailability() & isNetworkConnected()){
            if (ShareDialog.canShow(ShareVideoContent.class)) {
                ShareVideo videoToShare = new ShareVideo.Builder()
                        .setLocalUrl(videoFileUri)
                        .build();

                ShareVideoContent content = new ShareVideoContent.Builder()
                        .setVideo(videoToShare)
                        .build();

                shareDialog.show(content);
            }
        }else{
            view.setEnabled(false);
            Toast.makeText(ResultActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    // checking facebook application availability
    // facebook app on android called katana
    private boolean checkFacebookAppAvailability() {
        try{
            ApplicationInfo info = getPackageManager().
                    getApplicationInfo("com.facebook.katana", 0 );
            return true;
        } catch( PackageManager.NameNotFoundException e ){
            return false;
        }
    }

    // network availability check
    private boolean isNetworkConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return (networkInfo != null ) ? true : false;
    }

    // deleting temp files
    private void deleteTempFiles(String directoryWithTempFiles){

//         getting directory tree for deleting all sub files for sure. Clean up whole app folder
//         !important: recursive call ( infinity loop danger )
        File dir = new File(directoryWithTempFiles);
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory()){
                deleteTempFiles(children[i].getAbsolutePath());
            }else{
                if (children[i].exists()) children[i].delete();
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteTempFiles(MainActivity.baseExternalDirectory.getAbsolutePath());
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
    protected void onStop() {
        super.onStop();
        deleteTempFiles(MainActivity.baseExternalDirectory.getAbsolutePath());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // delete temp files from application folder ~ 50 MB
        deleteTempFiles(MainActivity.baseExternalDirectory.getAbsolutePath());
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

