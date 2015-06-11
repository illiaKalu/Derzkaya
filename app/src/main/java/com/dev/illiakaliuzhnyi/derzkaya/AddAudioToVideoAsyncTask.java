package com.dev.illiakaliuzhnyi.derzkaya;

/**
 * Created by illiakaliuzhnyi on 6/11/15.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;


public class AddAudioToVideoAsyncTask extends AsyncTask<Void, Void, Void> {

    Activity act;
    String audio;
    double position;
    String videoFromFilePath;
    boolean hasAudio;
    String videoToFilePath;
    double frameRate;

    FFmpegFrameRecorder recorder;
    FFmpegFrameGrabber grabberFirst;
    FFmpegFrameGrabber grabberSecond;

    public AddAudioToVideoAsyncTask(Activity act, String audioName){
        audio = audioName;
        position = 0.0; //positionToAdd;

        Log.d("MY", "WORKING FINE ON THE BEGINNING!");

        // !
        hasAudio = true;

        File workingFile = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        videoFromFilePath = workingFile + "/myvideo.mp4";
        videoToFilePath = workingFile + "/output.mp4";

        Log.d("MY_TAG", "result video - " + videoToFilePath);
        Log.d("MY_TAG", "source video - " + videoFromFilePath);

        grabberFirst = new FFmpegFrameGrabber(videoFromFilePath);
        grabberSecond = new FFmpegFrameGrabber(audioName);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.d("MY", "ON THE PRE EXECUTE METHOD");

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
        frameRate = grabberFirst.getFrameRate();
        recorder = new FFmpegFrameRecorder(videoToFilePath, grabberFirst.getImageWidth(), grabberFirst.getImageHeight(), grabberSecond.getAudioChannels());

        recorder.setFrameRate(frameRate);
        recorder.setFormat(grabberFirst.getFormat());
        recorder.setSampleRate(grabberFirst.getSampleRate());


        recorder.setAudioChannels(2);

        recorder.setVideoCodec(86018);

        //CHECK AVAILABLE CODECS!

        Log.d("MY", "number of audio channels = " + grabberSecond.getAudioChannels());

        Log.d("MY", "video codec = " + String.valueOf(grabberFirst.getVideoCodec()));
        Log.d("MY", "audio codec = " + String.valueOf(grabberFirst.getAudioCodec()));

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
            while ((frame1 = grabberFirst.grabFrame()) != null ||
                    (frame2 = grabberSecond.grabFrame()) != null) {
                recorder.record(frame1);
                recorder.record(frame2);
            }
        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        } catch (FrameRecorder.Exception fre) {
            fre.printStackTrace();
        }


        return null;
    }
}
