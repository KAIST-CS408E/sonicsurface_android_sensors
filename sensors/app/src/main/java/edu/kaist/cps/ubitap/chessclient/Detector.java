package edu.kaist.cps.ubitap.chessclient;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;

import DataStructure.AudioChunk;
import DataStructure.CircularQueue;
import DataStructure.TSafeQueue;

public class Detector extends Thread {
	FileOutputStream os = null;
	Context c;
	CircularQueue circularQueue;

	public Detector(Context context){
		this.c=context;
		circularQueue = new CircularQueue(30,this.c);
	}

	@Override
	public void run() {
		try {
			while (Wrapper.isRecording) {

				AudioChunk chunk = TSafeQueue.removeFromINbufferQueue();

				this.circularQueue.insert(new AudioChunk(chunk.data,chunk.arrivalTime));

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
				Log.d(Main.tag,"Detector Thread END");
		}
	}

}
