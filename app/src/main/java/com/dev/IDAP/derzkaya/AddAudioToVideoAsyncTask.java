package com.dev.IDAP.derzkaya;

/**
 * Created by illiakaliuzhnyi on 6/24/15.
 */

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.WritableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class AddAudioToVideoAsyncTask extends AsyncTask< Void, Void, Void> {

    private FFmpegFrameRecorder recorder;

    private FrameGrabber grabberRecordedAudio;
    private FrameGrabber grabberMusicAudio;
    private FrameGrabber grabberVideo;

    TimeStamps timeStamp;

    ArrayList<TimeStamps> timeStampses;

    static String resultVideo;

    Iterator timeSamplesIterator;
    private int counter;
    private long offset;
    private boolean recordMicro;
    private float alpha = 1.0f;

    public AddAudioToVideoAsyncTask(File recordedAudioFile, File recordedVideoFile, File musicFile, ArrayList<TimeStamps> timeStampses){

    grabberMusicAudio = new FFmpegFrameGrabber(musicFile.getAbsolutePath()); // replace with file in RAW folder.
    grabberRecordedAudio = new FFmpegFrameGrabber(recordedAudioFile.getAbsolutePath());
    grabberVideo = new FFmpegFrameGrabber(recordedVideoFile.getAbsolutePath());
    this.timeStampses = timeStampses;
    timeSamplesIterator = timeStampses.iterator();

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // generating date as a specific name for each video
        // some media players can't play videos with ":" in name
        // use "_" instead of ":"
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss");
        String date = dateFormat.format(new Date());
        String outputFileName = "/DerzkayaVID" + date + ".mp4";

        resultVideo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + outputFileName;

        try {
            grabberMusicAudio.start();
            grabberRecordedAudio.start();
            grabberVideo.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }



        recorder = new FFmpegFrameRecorder(MainActivity.baseExternalDirectory + "/FFMpegMergeResultMicrophoneAndVoice.m4a", grabberRecordedAudio.getAudioChannels());

        recorder.setAudioCodec(grabberRecordedAudio.getAudioCodec());
        recorder.setAudioBitrate(grabberRecordedAudio.getAudioBitrate());
        //recorder.setFrameNumber(grabberAud.getFrameNumber());
        recorder.setFormat(grabberRecordedAudio.getFormat());
        //recorder.setTimestamp(grabberAud.getTimestamp());
        recorder.setSampleRate(grabberRecordedAudio.getSampleRate());


        Log.d("MY", "STILL ALIVE AFTER GRABBERFIRST.START");

        try {
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);


        if (RecordActivity.dialog != null) RecordActivity.dialog.dismiss();

        try {
            grabberRecordedAudio.release();
            grabberVideo.release();
        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        }

    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("MY", "DOING GREAT! DOINBACKGROUND!");

        Frame audioFrame = null;
        Frame musicFrame = null;

        if (timeSamplesIterator.hasNext()) timeStamp = (TimeStamps) timeSamplesIterator.next();

        try {

            while ((audioFrame = grabberRecordedAudio.grabFrame()) != null) {

                musicFrame = grabberMusicAudio.grabFrame();

                Buffer musicBuffer = musicFrame.samples[0];
                Buffer microphoneBuffer = audioFrame.samples[0];


                for (int t = 0; t < musicBuffer.capacity(); t++) {
                    counter++;

                    if (timeStamp == null) break;


                    if ( ((counter / 43)) == ( ((timeStamp.getOffsetInMs())) - 1000) & timeStamp.getTimeStampType().equals("M")) {

                        offset = timeStamp.getOffsetInMs() + 1000;
                        recordMicro = true;
                        if (timeSamplesIterator.hasNext()) timeStamp = (TimeStamps)timeSamplesIterator.next();

                    }

                    if ( ((counter / 43)) == ( (timeStamp.getOffsetInMs())) & timeStamp.getTimeStampType().equals("T")) {

                        recordMicro = false;
                        if (timeSamplesIterator.hasNext()) timeStamp = (TimeStamps)timeSamplesIterator.next();
                    }

                    if ( ((counter / 43 )) >= ((offset - 1000)) & (((counter / 43)) <= (offset)) & recordMicro) {
                        if (alpha > 0.002f) alpha -= 0.000022f;
                        ((FloatBuffer) musicBuffer).put(t, ((FloatBuffer) musicBuffer).get(t) * alpha + (1 - alpha) * ((FloatBuffer) microphoneBuffer).get(t));
                    }

                    if ( ((counter / 43)) > (offset) & (((counter / 43)) < (timeStamp.getOffsetInMs() - 1000)) & recordMicro) {
                        musicBuffer = audioFrame.samples[0];
                    }

                    if ( ((counter / 43)) >= ( timeStamp.getOffsetInMs() - 1000 ) & (((counter / 43)) <= ((timeStamp.getOffsetInMs())) & recordMicro)) {
                        if (alpha < 1) alpha += 0.000022f;
                        ((FloatBuffer) musicBuffer).put(t, ((FloatBuffer) musicBuffer).get(t) * alpha + (1 - alpha) * ((FloatBuffer) microphoneBuffer).get(t));
                    }

                }

                audioFrame.samples[0] = musicBuffer;

              //  videoFrame = grabberVideo.grabFrame();
                recorder.record(audioFrame);
              //  recorder.record(videoFrame);

            }

            recorder.stop();
            grabberMusicAudio.stop();
            grabberRecordedAudio.stop();


        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        } catch (FrameRecorder.Exception fre) {
            fre.printStackTrace();
        }

        Log.d("MY", "I am done with merging and frames are - " + 0);




        Movie video = null;
        try {
            video = new MovieCreator().build(MainActivity.baseExternalDirectory + "/VideoResultFromCamera.mp4");
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Movie audio = null;
        try {
            audio = new MovieCreator().build(MainActivity.baseExternalDirectory + "/FFMpegMergeResultMicrophoneAndVoice.m4a");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Track audioTrack = audio.getTracks().get(0);
        Track videoTrack = video.getTracks().get(0);
        Log.d("TAG","audioDuration = "+audioTrack.getDuration());
        Log.d("TAG","videoDuration = "+videoTrack.getDuration());

        Movie mov = new Movie();
        mov.addTrack(videoTrack);
        mov.addTrack(audioTrack);


        Container out = new DefaultMp4Builder().build(mov);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(resultVideo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class BufferedWritableFileByteChannel implements WritableByteChannel {
        private static final int BUFFER_CAPACITY = 1000000;

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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

