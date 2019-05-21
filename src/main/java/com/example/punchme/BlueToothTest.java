package com.example.punchme;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BlueToothTest extends AppCompatActivity {

    public static final String passColl = "wO2rS"; //needed for passing the number

    //the list view inputs
    ListView spinner;
    private Set<BluetoothDevice> pairedDevs;

    //Bluetooth controls
    BluetoothAdapter bAdapt;
    BluetoothManager bMan;
    BluetoothSocket btSocket;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //the messages
    ArrayList<String> messages;
    ArrayList<String> collection = new ArrayList(); //final list of strings

    //clock controls
    private int workTime = 0;
    private long milliLeft = 0;
    private TextView countDown; //the clock that displays on screen
    private CountDownTimer countTimer; //the count down
    private boolean ready = false;

    TextView fir;
    TextView sec;
    TextView thi;
    TextView fort;
    TextView fit;

    //variables needed to make the buzzer
    private SoundPool buzzer;
    private int soundbuz;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_test);

        messages = new ArrayList<>();
        countDown = findViewById(R.id.timeLeft2);

        fir = findViewById(R.id.textView6);
        sec = findViewById(R.id.textView7);
        thi = findViewById(R.id.textView8);
        fort = findViewById(R.id.textView9);
        fit = findViewById(R.id.textView10);

        //sets up the adapter\
        bMan = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bAdapt = BluetoothAdapter.getDefaultAdapter();

        //shows if the system has access to bluetooth
        if (bAdapt == null){
            String toast = "Buletooth Not Available.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }
        else {
            String toast = "Buletooth Available.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }


        //sets up buzzer file depending on version of phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            buzzer = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }else{
            buzzer = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        }

        //allocates the file to the buzzer
        soundbuz = buzzer.load(this, R.raw.sound1, 1);


        ///buttons based on names see below for details
        Button first = (Button) findViewById(R.id.OBut);
        first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startWorkout();
            }
        });

        //connects to the board when the button is clicked
        spinner = (ListView) findViewById(R.id.devs);
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                pairUp(info, address);
            }
        });

        getPair();
    }




    public void startWorkout(){
        if(isReady()){
            //sendNum("1");
            //milliLeft = workTime * 1000;

            //String toast = "Workout Started";
            //Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

            //theTimer();

            sendNum("3");
            //while(!ready){}
            //getValues();
            //nextPage();
        }
        else{
            Toast.makeText(this, "Make sure bag is connected and length is chosen first!", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean isReady(){
        EditText editNum = (EditText) findViewById(R.id.timeL);
        workTime = Integer.parseInt(editNum.getText().toString());

        if((workTime != 0) && (isBtConnected))
            return true;
        else
            return false;
    }

    //moves to the next page
    private void nextPage(){
        Intent nextPage = new Intent(this, ResultScreen.class);
        nextPage.putExtra(passColl, collection);
        startActivity(nextPage);
    }


    //gets the values from the array and turns them into strings for passing
    private void getValues(){
        //the data from the microcontroler
        int numHits = Integer.valueOf(messages.get(0));
        double total = Double.valueOf(messages.get(1));
        double max = Double.valueOf(messages.get(2));;

        //calculated values
        double hitPsec = 0;
        double average = 0;

        //calculates the average energy
        average =  total / numHits;

        //calculates the hits per second
        hitPsec = (double) numHits / workTime;

        //puts max, average, and hits per second into two decimal format
        String allHit = new DecimalFormat("#.##").format(numHits);
        String maxStr = new DecimalFormat("#.##").format(max);
        String ave = new DecimalFormat("#.##").format(average);
        String HPS = new DecimalFormat("#.##").format(hitPsec);
        String timeSpent = new DecimalFormat("#.##").format(workTime);


        //puts all the final values into the collection to be shown off
        collection.add(0, allHit);
        collection.add(1, maxStr);
        collection.add(2, ave);
        collection.add(3, HPS);
        collection.add(4, timeSpent);
    }


    //Clock section ************************************************************************
    public void theTimer(){
        countTimer = new CountDownTimer(milliLeft, 1000){
            //every second, the time is updated
            @Override
            public void onTick(long l){
                milliLeft = l;
                updateTimer();
            }

            //happens at the end of the timer
            @Override
            public void onFinish(){
                //make sound play
                //playSound();

                //end taking in signals
                sendNum("3");
            }
        }.start(); //makes sure the timer starts when the aplication opens
    }

    //updates the clock evvery second
    public void updateTimer()
    {
        int sec = (int) milliLeft / 1000;
        String timeLeftText;

        timeLeftText = "";
        if(sec < 10) timeLeftText += "0";
        timeLeftText += sec;

        countDown.setText(timeLeftText);
    }

    //plays the sound of the buzzer
    private void playSound(){
        buzzer.play(soundbuz, 1,1,0,0,1);
    }
    //ends the sound so the resources are saved
    @Override
    protected void onDestroy() {
        super.onDestroy();
        buzzer.release();
        buzzer = null;
    }

    //End Clock section ********************************************************************

    //Bluetooth section ********************************************************************

    //gets the paired devices on the phone and adds them to the list
    public void getPair(){
        //needed array and adapter to use
        pairedDevs = bAdapt.getBondedDevices();
        ArrayList devices = new ArrayList();

        //makes sure there is at least one device on the list
        if (pairedDevs.size() > 0 ){
            //adds the devices to a list
            for(BluetoothDevice bt : pairedDevs)
            {
                devices.add(bt.getName() + '\n' + bt.getAddress());
            }
        }
        else{
            //if there is no paired devices then tells the user
            String toast = "None found";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }

        //sets the list to the clickable list
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, devices);
        spinner.setAdapter(adapter);
    }

    //connects the user to the bag
    public void pairUp(String info, String address){
        //checks if it was successful
        boolean test = true;

        //tries to connect to the address given
        try {
            if (btSocket == null || !isBtConnected) {
                BluetoothDevice bag = bAdapt.getRemoteDevice(address);
                btSocket = bag.createInsecureRfcommSocketToServiceRecord(myUUID);
                btSocket.connect();
            }
        }
        catch (IOException e){
            test = false;
        }

        //displays if the connection succeeded or failed
        if (!test)
        {
            String toast = "Connection Failed. Is it a SPP Bluetooth? Try again.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }
        else
        {
            String toast = "Connected.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

            //lets the code know we are connected
            isBtConnected = true;

            //starts up the reading of the input
            try{
                mmInputStream = btSocket.getInputStream();
                beginListenForData();
            }
            catch (IOException e){

            }
        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            String mess = data.replaceAll("x","");
                                            int firstS = mess.indexOf(" ");
                                            int secondS = mess.lastIndexOf(" ");
                                            String first = mess.substring(0, firstS);
                                            String sec =  mess.substring(firstS+1, secondS);
                                            String third = mess.substring(secondS+1, mess.length());

                                            messages.add(0,first);
                                            messages.add(1,sec);
                                            messages.add(2,third);

                                            getValues();
                                            nextPage();
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    public void sendNum(String num){
        try {
            btSocket.getOutputStream().write(num.toString().getBytes());
        }
        catch (IOException e){
            String toast = "Error";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //end blueTooth section ***************************************************************************
}
