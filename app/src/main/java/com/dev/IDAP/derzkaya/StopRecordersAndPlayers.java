package com.dev.IDAP.derzkaya;

import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.Chronometer;

/**
 * Created by illiakaliuzhnyi on 7/3/15.
 */
public class StopRecordersAndPlayers extends AsyncTask {

    private Chronometer chronometer;
    private MediaPlayer mediaPlayer;

    public StopRecordersAndPlayers(Chronometer chronometer, MediaPlayer mediaPlayer) {

        this.mediaPlayer = mediaPlayer;
        this.chronometer = chronometer;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (chronometer != null) chronometer.stop();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if (mediaPlayer != null || !mediaPlayer.isPlaying()) mediaPlayer.stop();
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
