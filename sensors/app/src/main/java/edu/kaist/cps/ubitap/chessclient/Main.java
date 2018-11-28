package edu.kaist.cps.ubitap.chessclient;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import DataStructure.AudioChunk;
import DataStructure.Utils;

public class Main extends AppCompatActivity implements SensorEventListener {

    Button connect,disconnect,startAudioStream,stopAudioStream;
    Context context = this;
    //Path for storing audio files that are recorderd. (This has no use in the actual application, i used this for testing.)
    Wrapper engine = new Wrapper("/storage/emulated/0/AudioRecorder/",this);
    public static final int RequestPermissionCode =1;

    //The following two values are just hardcoded here but will be replaced by the values provided from the UI.
    public static int SERVERPORT=3352;
    public static String IP ="192.168.0.4";

    public static OutputStream out;
    public static InputStream in ;
    public static Socket serverSocket = null;
    public static ObjectOutputStream oos=null;


    public static DatagramSocket dSock ;


    //Just the tag for Log
    public static String tag = "UbiTap";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float lastX=10000;
    private float lastY=10000;
    private float lastZ=10000;

    private long currentTime = System.currentTimeMillis();
    private long lastUpdate = System.currentTimeMillis();

    private static boolean send = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Reference to the buttons created of the UI
        connect = findViewById(R.id.connect);
        disconnect= findViewById(R.id.disConnect);
        startAudioStream= findViewById(R.id.startAudioStream);
        stopAudioStream=findViewById(R.id.stopAudioStream);

        //Button click trigger these function for connect button
        connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                connect.setEnabled(false);

                EditText HostIP=findViewById(R.id.hostNameE);
                EditText HostPort=findViewById(R.id.hostPortE);

                Main.IP= HostIP.getText().toString();
                Main.SERVERPORT = Integer.parseInt(HostPort.getText().toString());

                Wrapper.isRecording = true;

                try {
//                    Main.serverSocket = new Socket(Main.IP,Main.SERVERPORT);
//                    Main.serverSocket.setTcpNoDelay(true);
//                    Main.oos = new ObjectOutputStream(serverSocket.getOutputStream());
                    Main.dSock = new DatagramSocket();
                    InetAddress address = InetAddress.getByName(Main.IP);

                    byte[] b = new byte[1];

                    AudioChunk c = new AudioChunk(b,Utils.getCurrentTime());
                    c.dataType=1111;
                    Utils.sendData(c);
                } catch (IOException e) {
                    Log.d(tag,"Exception while creating the Sockets in the Main file.");
                    Utils.Alert(context,"Connection Error",e.getMessage());
                    connect.setEnabled(true);
                }

            }
        });

        //Button click trigger these function for disconnect button
        disconnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {


                    byte[] b = new byte[1];
                    AudioChunk c = new AudioChunk(b,Utils.getCurrentTime());
                    c.dataType=1010;
                    Utils.sendData(c);
                   // Main.dSock.close();

//                    Main.oos.writeObject(c);
//                    Main.oos.close();
//                    Main.serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                connect.setEnabled(true);
            }
        });

        startAudioStream.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                engine.startRecording();//Start the recording and start to get data from the microphone.
                try {
                    Thread.sleep(100);
                    Detector d = new Detector(context);
                    d.start();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startAudioStream.setEnabled(false);
            }
        });

        stopAudioStream.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                engine.stopRecording();
                try {
                    Main.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startAudioStream.setEnabled(true);
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        currentTime = System.currentTimeMillis();

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        if (lastX==10000){
            lastUpdate = currentTime;
            lastX = x;
            lastY = y;
            lastZ = z;
        }

        float diff = Math.abs(lastX-x)+Math.abs(lastY-y)+Math.abs(lastZ-z);
        //Log.e("Diff",String.valueOf(diff));

        Handler handler = new Handler();

        if (diff>0.07 && !send){
            send = true;
            Log.e("Diff",String.valueOf(diff));
            findViewById(R.id.imageLinearLayout).setBackgroundColor(getResources().getColor(R.color.colorAccent));

            handler.postDelayed(new Runnable() {
                public void run() {
                    findViewById(R.id.imageLinearLayout).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }, 500);

            handler.postDelayed(new Runnable() {
                public void run() {
                    send = false;
                }
            }, 1000);

            lastUpdate=currentTime;
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public static boolean isVibrationTap(){
        return send;
    }
}
