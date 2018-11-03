package DataStructure;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.LinearLayout;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.kaist.cps.ubitap.chessclient.Main;
import edu.kaist.cps.ubitap.chessclient.R;
import edu.kaist.cps.ubitap.chessclient.Wrapper;

public class CircularQueue {

    int size;
    ArrayList<AudioChunk> cQueue;
    boolean flag;
    int offset=7;
    int offsetCount=0;
    Context c;
//    int peakPost;//Defines the no of data chunks to throw after detection of a peak.

    int waitBlocks=13;

    public CircularQueue(int size,Context c){
        this.c=c;
        this.size=size;
        cQueue = new ArrayList<AudioChunk>(size);
        this.flag=false;
//        this.peakPost=size;
        int milliSecondToWait=60;
        this.waitBlocks = (int) Math.ceil((milliSecondToWait*Wrapper.RECORDER_SAMPLERATE)/(Wrapper.bufferSize*1000) );

    }

    public boolean insert(AudioChunk chunk)  {

        if((!flag)  ) {
            if (Utils.getAbsByteMax(chunk.data) > Wrapper.THRESHOLD) {
//                try {
//                    Utils.sendData(new byte[1]);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                this.flag = true;
            }
            this.offsetCount=0;
        }
        else if(flag ) {

            if(this.offsetCount == this.waitBlocks && this.cQueue.size() == this.size){
                ArrayList<AudioChunk> dataBulk =new ArrayList<AudioChunk>( cQueue.subList(0,this.waitBlocks + 1));

                byte[] b = new byte[0];

                for(int i = 0 ; i< dataBulk.size() ;i++) {
//                    AudioChunk n = dataBulk.remove(dataBulk.size()-1);
//                    b= ArrayUtils.addAll(b,n.data);
                    b= ArrayUtils.addAll(b,dataBulk.remove(dataBulk.size()-1).data);

                }

                try {
                    Utils.sendData(new AudioChunk(b,Utils.getCurrentTime()));
//                    Wrapper.writeByteToFile(Arrays.copyOf(b,b.length), dataBulk.get(0).arrivalTime+"MONO.wav");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.flag = false;
//                this.peakPost=10;
                this.offsetCount=0;
            }

        }
        this.offsetCount++;

//        this.peakPost--;

        //insert Code
        if(this.cQueue.size() == this.size){
            this.cQueue.remove(this.cQueue.size()-1);
        }
        this.cQueue.add(0,chunk);

        return true;
    }

    public int size(){
        return this.cQueue.size();
    }
}
