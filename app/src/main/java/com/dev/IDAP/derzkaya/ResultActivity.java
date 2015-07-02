package com.dev.IDAP.derzkaya;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ResultActivity extends Activity {

    VideoView videoView;
    boolean videoViewPathExist = false;
    Uri videoFileUri;

    ImageButton save_button;
    ImageButton backToMain;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    static ProgressDialog dialog ;

    String resultFileStringName = "/resultVideo.mp4";
    File resultVideoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        dialog = new ProgressDialog(this);
        dialog.setMessage(" Prepearing your video ! It could take a while . . .");
        dialog.show();

        resultVideoFile = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + resultFileStringName);

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

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    save_button.setBackgroundResource(R.drawable.save_off);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    save_button.setBackgroundResource(R.drawable.save_on);
                    // Do save actions

                    save_button.setEnabled(false);
                    // some media players can't play videos with ":" in name
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss");
                    String date = dateFormat.format(new Date());
                    String outputFileName = "/DerzkayaVID" + date + ".mp4";
                    File videoFileWithDateName = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + outputFileName);
                    renameFile(resultVideoFile, videoFileWithDateName);
                    videoView.setVideoPath(videoFileWithDateName.getAbsolutePath());
                    videoViewPathExist = true;
                }

                return true;
            }
        });
        backToMain = (ImageButton) findViewById(R.id.back_to_main_button);

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


        videoView = (VideoView) findViewById(R.id.videoView);


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!videoViewPathExist) videoView.setVideoPath(resultVideoFile.getAbsolutePath());

                if(!videoView.isPlaying()) {
                        videoView.start();
                }else {
                    if(videoView.isPlaying()){
                        videoView.pause();
                    }

                }

                return false;
            }
        });

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

    public void shareOnFacebook(View view) throws IOException {

        if(checkFacebookAppAvailability()){

            if (ShareDialog.canShow(ShareVideoContent.class)) {
                ShareVideo video = new ShareVideo.Builder()
                        .setLocalUrl(videoFileUri)
                        .build();
                Log.d("MY", "facebook");

                ShareVideoContent content = new ShareVideoContent.Builder()
                        .setVideo(video)
                        .build();

                shareDialog.show(content);
            }

        }else{
            Toast.makeText(ResultActivity.this, "whooops! Looks like you haven't facebook app.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFacebookAppAvailability() {

        try{
            ApplicationInfo info = getPackageManager().
                    getApplicationInfo("com.facebook.katana", 0 );
            return true;
        } catch( PackageManager.NameNotFoundException e ){
            return false;
        }
    }

    private void renameFile(File inputFile, File outputFile) {

        inputFile.renameTo(outputFile);
        Toast.makeText(ResultActivity.this, "Video saved to SD card", Toast.LENGTH_SHORT).show();

    }

    private void deleteTempFiles(String directoryWithTempFiles){

        File dir = new File(directoryWithTempFiles);
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory()){
                deleteTempFiles(children[i].getAbsolutePath());
            }else{
                if (children[i].exists()) children[i].delete();
            }
        }

        // Delete special resultVideo if Exist !
        File resultVideo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + resultFileStringName);
        if (resultVideo.exists()) resultVideo.delete();

        Log.d("MY", "FILES DELETED!");

    }

}

