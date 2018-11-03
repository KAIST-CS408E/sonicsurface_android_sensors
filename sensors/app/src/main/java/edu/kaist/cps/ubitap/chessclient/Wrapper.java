package edu.kaist.cps.ubitap.chessclient;

/**
 * Created by root on 18. 5. 18.
 * <p>
 * Created by user on 1/2/2018.
 * This code is NOT my work. It is COPIED from the following source.
 * Source: http://selvaline.blogspot.kr/2016/04/record-audio-wav-format-android-how-to.html
 * <p>
 * Created by user on 1/2/2018.
 * This code is NOT my work. It is COPIED from the following source.
 * Source: http://selvaline.blogspot.kr/2016/04/record-audio-wav-format-android-how-to.html
 */

/**
 * Created by user on 1/2/2018.
 * This code is NOT my work. It is COPIED from the following source.
 * Source: http://selvaline.blogspot.kr/2016/04/record-audio-wav-format-android-how-to.html
 */

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import DataStructure.AudioChunk;
import DataStructure.TSafeQueue;
import DataStructure.Utils;

public class Wrapper {

    public static int RECORDER_SAMPLERATE = 192000;
    public static int THRESHOLD=2500;

    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    short[] audioData;

    public static AudioRecord recorder = null;
    public static int bufferSize = 0;
    private Thread pipeMicrophoneData = null;
    public static boolean isRecording = false;
    int[] bufferData;
    int bytesRecorded;
    Context  context;


    public static  String output;

    public Wrapper(String path, Context context) {


        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) ;
        this.context = context;
        audioData = new short[bufferSize]; // short array that pcm data is put
        // into.
        output = path;

    }


    public void startRecording() {

        pipeMicrophoneData = new Thread(new Runnable() {
            @Override
            public void run() {
                startPipeMicData();
            }
        }, "Audio Stream Thread");

        pipeMicrophoneData.start();//Thread that starts capturing the audio and pushes it to the buffer
    }

    public void startPipeMicData() {

        EditText thresholdE = (EditText) ((Activity) this.context).findViewById(R.id.thresholdE);
        THRESHOLD= Integer.parseInt(thresholdE.getText().toString());
        recorder = new AudioRecord(MediaRecorder.AudioSource.UNPROCESSED, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
        isRecording=true;


        int i = recorder.getState();
        if (i == 1) {
            recorder.startRecording();
        }
        else{
            Log.d(Main.tag,"MIC NOT READY");
        }


        //bufferSize=2;
        byte data[] = new byte[bufferSize/2];
        byte stereoData[] = new byte[bufferSize];


        int j=0;
        int index=0;
        while (isRecording) {
            j=0;


            recorder.read(stereoData, 0, bufferSize);
            for( j = 0; 2*(j+1)+1 <= stereoData.length; j+= 2) {
                //System.out.println(stereoData[index]);
                //data[j++]=stereoData[index];
                //data[j++]=stereoData[index];

                data[j] = stereoData[2*(j+1)];
                data[j+1] = stereoData[2*(j+1)+1];

            }


//            try {
//                data=Utils.getMockData(bufferSize);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            AudioChunk c = new AudioChunk(Arrays.copyOf(data,data.length), Utils.getCurrentTime());
            //Clear the arrays

            try {
                TSafeQueue.addToINbufferQueue(c);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    public void stopRecording() {
        if (null != recorder) {
            isRecording = false;

            int i = recorder.getState();
            if (i == 1)
                recorder.stop();
            recorder.release();

            recorder = null;
           // pipeMicrophoneData = null;
        }


    }






































    public static String getFilename() {

        String nameOfTheFile = "Recorder"+ ".wav";

        return (output + nameOfTheFile);
    }

    public static String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }


    //PURANO MA WRITE GARNA LAI USE GAREKO FILE.


    private static void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    public static void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;

        int channels = ((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1 : 2);
        channels=1;//Wrtie in MONO ALWAYS
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2) * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }



    public static  void writeByteToFile(final byte[] data1, String fileName) throws IOException, InterruptedException {
        FileOutputStream ds1 = null;


        try {
            ds1 = new FileOutputStream(new File(getTempFilename()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        final FileOutputStream finalDs = ds1;

        Thread s1t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    finalDs.write(data1,0,data1.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });


        s1t.start();


        s1t.join();

        finalDs.close();

        copyWaveFile(getTempFilename(), getFilename()+fileName+".wav");

        deleteTempFile();

    }



}