package com.dev.IDAP.derzkaya;

/**
 * Created by illiakaliuzhnyi on 6/24/15.
 */

import android.app.ProgressDialog;
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
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;


public class AddAudioToVideoAsyncTask extends AsyncTask< Void, Void, Void> {

    String videoToFilePath;
    String videoFromFilePath;



    private FFmpegFrameRecorder recorder;
    private FFmpegFrameGrabber grabberVideo;

    private FrameGrabber grabberAud;
    private String resultingFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/resultVideo.mp4";


    public AddAudioToVideoAsyncTask(String audioFile){


        File workingFile = MainActivity.baseExternalDirectory;

        videoFromFilePath = workingFile + "/capturedvideo.3gp";
        videoToFilePath = workingFile + "/outputFFMpeg.mp4";

        grabberVideo = new FFmpegFrameGrabber(videoFromFilePath);
        grabberAud = new FFmpegFrameGrabber(audioFile);


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        try {
            grabberVideo.start();
            grabberAud.start();
        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        }

        Log.d("MY", "STILL ALIVE AFTER GRABBERFIRST.START");

        recorder = new FFmpegFrameRecorder(videoToFilePath, grabberVideo.getImageWidth(), grabberVideo.getImageHeight(), grabberAud.getAudioChannels());

        recorder.setFormat(grabberVideo.getFormat());
        recorder.setVideoBitrate(grabberVideo.getVideoBitrate());
        recorder.setFrameRate(grabberVideo.getFrameRate());
        recorder.setVideoCodec(grabberVideo.getVideoCodec());


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
            grabberVideo.release();
            grabberAud.release();
        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        } catch (FrameRecorder.Exception fre) {
            fre.printStackTrace();
        }

        ResultActivity.dialog.dismiss();

    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("MY", "DOING GREAT! DOINBACKGROUND!");
        Frame derzkframe = null;

        // part 1
        try {

            while ( (derzkframe = grabberAud.grabFrame()) != null){

                recorder.record(derzkframe);
            }

            recorder.stop();
            grabberVideo.stop();
            grabberAud.stop();


        } catch (FrameGrabber.Exception fge) {
            fge.printStackTrace();
        } catch (FrameRecorder.Exception fre) {
            fre.printStackTrace();
        }

        Log.d("MY", "I am done with merging and frames are - " + 0);



        // important part 2 !


        Movie video = null;
        try {
            video = new MovieCreator().build(MainActivity.baseExternalDirectory + "/capturedvideo.3gp");
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Movie audio = null;
        try {
            audio = new MovieCreator().build(MainActivity.baseExternalDirectory + "/outputFFMPeg.mp4");
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
            fos = new FileOutputStream(resultingFile);
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

