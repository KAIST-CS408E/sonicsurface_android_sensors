package DataStructure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.LinearLayout;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import edu.kaist.cps.ubitap.chessclient.Main;
import edu.kaist.cps.ubitap.chessclient.R;

public class Utils {
	public static int i = 0;
	private static int counter = 0;

	public static long getCurrentTime() {
		String str = String.valueOf(System.nanoTime()) + String.valueOf(i++)+"00";
		if (i > 9) {
			i = 0;
		}
		return Long.parseLong(str, 10);
	}

	public static byte[] getMockData(int size) throws InterruptedException {
		// Let the THREAD sleep for x milliseconds. This mimics the time taken to read
		// the buffer of the microphone.

		int i = 0;
		int max =32000;
		short[] data = new short[size];

		for (i =0; i < size; i++) {
			if(Utils.counter ==max){
				Utils.counter=0;
			}
			data[i] = ((short) (Utils.counter));

		}
		return ShortToByte_ByteBuffer_Method(data);
	}
	public static short getAbsByteMax(byte[] inputArray) {

		ShortBuffer buf1 = ByteBuffer.wrap(inputArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		short[] shortData=new short[buf1.remaining()];
		buf1.get(shortData);

		short maxValue = inputArray[0];

		for (int i = 1; i < shortData.length; i++) {
			if (Math.abs(shortData[i]) > maxValue) {
				maxValue = (short)Math.abs(shortData[i]);
			}
		}
	/*	Arrays.sort(shortData);
		return (short)(shortData[shortData.length-1] );*/
		return maxValue;
	}
	public static short getMin(short[] inputArray) {
		short maxValue = inputArray[0];
		for (int i = 1; i < inputArray.length; i++) {
			if (inputArray[i] < maxValue) {
				maxValue = inputArray[i];
			}
		}
		return maxValue;
	}
	public static void DisplayAlert(Context context,String title, String message){
		AlertDialog.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
		} else {
			builder = new AlertDialog.Builder(context);
		}
		builder.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// continue with delete
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();

	}
	public static void Alert(Context context,String title, String message){
		AlertDialog.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
		} else {
			builder = new AlertDialog.Builder(context);
		}
		builder.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// continue with delete
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();

	}


	public static  void sendData(AudioChunk c) throws IOException {
		byte[] data1 = new byte[0];
		try {
			data1=Utils.getMockData(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


//		AudioChunk c1 = new AudioChunk(data1,Utils.getCurrentTime());


		//TCP
        //Main.oos.writeObject(c);

		//UDP
		Utils.sendTo(c,Main.IP,Main.SERVERPORT);
//		oos.writeObject(c1);


//        oos.close();




	}
    public static byte [] ShortToByte_ByteBuffer_Method(short[] input)
    {
        int index;
        int iterations = input.length;

        ByteBuffer bb = ByteBuffer.allocate(input.length * 2);

        for(index = 0; index != iterations; ++index)
        {
            bb.putShort(input[index]);
        }

        return bb.array();
    }

	public static void sendTo(Object o, String hostName, int desPort) {
		try {


			InetAddress address = InetAddress.getByName(hostName);
			ByteArrayOutputStream byteStream = new
					ByteArrayOutputStream(5000);
			ObjectOutputStream os = new ObjectOutputStream(new
					BufferedOutputStream(byteStream));
			os.flush();
			os.writeObject(o);
			os.flush();
			//retrieves byte array
			byte[] sendBuf = byteStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(
					sendBuf, sendBuf.length, address, desPort);
			int byteCount = packet.getLength();
			Main.dSock.send(packet);
			os.close();
		} catch (UnknownHostException e) {
			System.err.println("Exception:  " + e);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
