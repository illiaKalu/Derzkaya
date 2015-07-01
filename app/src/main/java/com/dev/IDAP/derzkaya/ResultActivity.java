package com.dev.IDAP.derzkaya;

import android.app.Activity;
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

    File DCIMdir;

    Uri videoFileUri;

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

        final File f = new File(DCIMdir + "/myvideo.3gp");
        videoFileUri = Uri.fromFile(f);

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
                    save_button.setBackground(getResources().getDrawable(R.drawable.save_off));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    save_button.setBackground(getResources().getDrawable(R.drawable.save_on));
                    // Do save actions

                    save_button.setEnabled(false);
                    //File moviesDir = Environment
                      //      .getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES); where do I need to store video?


                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
                    String date = dateFormat.format(new Date());
                    String outputFileName = "/DerzkayaVID_" + date + ".3gp";
                    Log.d("MY TAG", "DATE IS - " + date);
                    moveFile(DCIMdir + "/myvideo.3gp", DCIMdir, outputFileName);
                }

                return true;
            }
        });
        backToMain = (ImageButton) findViewById(R.id.back_to_main_button);

        backToMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    backToMain.setBackground(getResources().getDrawable(R.drawable.back_off));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    backToMain.setBackground(getResources().getDrawable(R.drawable.back_on));
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

                if(!videoView.isPlaying()) {
                    Log.d("MY", "touched!");
                    videoView.start();


                }else {
                    if(videoView.isPlaying()){
                        Log.d("MY", "touched again!");
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

        String audiom4a = DCIMdir + "/" + "derzkaya1.m4a";

        //String audioaac = DCIMdir + "/" + "derzkayaaac.aac";
       // new AddAudioToVideoAsyncTask(audiom4a).execute();


        if(checkFacebookAppAvailability()){


            if (ShareDialog.canShow(ShareVideoContent.class)) {
                ShareVideo video = new ShareVideo.Builder()
                        .setLocalUrl(videoFileUri)
                        .build();



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

    private void moveFile(String inputFile, File outputPath, String outputFileName) {

        InputStream in = null;
        OutputStream out = null;
        try {

            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputPath + "/Camera" + outputFileName);


            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            //new File(inputPath + inputFile).delete();

            Toast.makeText(ResultActivity.this, "Video saved", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

