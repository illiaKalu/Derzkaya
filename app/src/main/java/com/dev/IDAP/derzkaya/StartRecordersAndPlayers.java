package com.dev.IDAP.derzkaya;

import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.Chronometer;

/**
 * Created by illiakaliuzhnyi on 7/3/15.
 */
public class StartRecordersAndPlayers extends AsyncTask{

    private Chronometer chronometer;
    private MediaPlayer mediaPlayer;
    private Thread progressThread;

    StartRecordersAndPlayers(Chronometer chronometer, MediaPlayer mediaPlayer, Thread progressThread){

        this.mediaPlayer = mediaPlayer;
        this.chronometer = chronometer;
        this.progressThread = progressThread;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        progressThread.start();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        mediaPlayer.start();
        return null;
    }
}
