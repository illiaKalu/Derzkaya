package com.dev.illiakaliuzhnyi.derzkaya;

/**
 * Created by illiakaliuzhnyi on 6/11/15.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.io.IOException;


public class AddAudioToVideoAsyncTask extends AsyncTask<Void, Void, Void> {


    int framesnumber = 0;
    String videoFromFilePath;
    String videoToFilePath;

    FFmpegFrameRecorder recorder;
    FFmpegFrameGrabber grabberFirst;
    FFmpegFrameGrabber grabberSecond;

    public AddAudioToVideoAsyncTask(String audioName){

        Log.d("MY", "WORKING FINE ON THE BEGINNING!");

        File workingFile = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        videoFromFilePath = workingFile + "/myvideo.3gp";
        videoToFilePath = workingFile + "/output.mp4";

        Log.d("MY_TAG", "result video - " + videoToFilePath);
        Log.d("MY_TAG", "source video - " + videoFromFilePath);

        grabberFirst = new FFmpegFrameGrabber(videoFromFilePath);
        grabberSecond = new FFmpegFrameGrabber(audioName);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


        try {
            grabberFirst.start();
        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        }
        try {
            grabberSecond.start();
        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        }

        Log.d("MY", "STILL ALIVE AFTER GRABBERFIRST.START");


        //frameRate = grabberFirst.getFrameRate();
        recorder = new FFmpegFrameRecorder(videoToFilePath, grabberFirst.getImageWidth(), grabberFirst.getImageHeight(), grabberSecond.getAudioChannels());

       // recorder.setVideoCodec(grabberFirst.getVideoCodec());
       // recorder.setFrameRate(grabberFirst.getFrameRate());
       // recorder.setSampleRate(grabberSecond.getSampleRate());
       // recorder.setFormat("mp4");


        Log.d("MY", "FrameRate - " + grabberFirst.getFrameRate());

        try {
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            recorder.release();
            grabberFirst.release();
            grabberSecond.release();
        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        } catch (FrameRecorder.Exception fre) {
            fre.printStackTrace();
        }

        //ButtonClickActions.returnFromAddMusicFragment((MainActivity)act);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("MY", "DOING GREAT! DOINBACKGROUND!");

        Frame frame1 = null;
        Frame frame2 = null;

        try {

            while(true)
            {

                frame1 = grabberFirst.grabFrame();
                frame2 = grabberSecond.grabFrame();

                if(frame1 == null || frame2 == null) break;

                frame1.samples = frame2.samples;

                recorder.record(frame1);


            }



            recorder.stop();
            grabberSecond.stop();
            grabberFirst.stop();

        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        } catch (FrameRecorder.Exception fre) {
            fre.printStackTrace();
        }

        Log.d("MY", "I am done with merging and frames are - " + framesnumber);

        return null;
    }
}
