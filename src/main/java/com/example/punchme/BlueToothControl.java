package com.example.punchme;

import android.app.Application;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.Nullable;


public class BlueToothControl extends Service {

    Handler handler = new Handler();
    IBinder mBinder = new LocalBinder();

    //Bluetooth controls
    private BluetoothAdapter bAdapt;
    BluetoothManager bMan;
    public BluetoothSocket btSocket;
    public InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private Set<BluetoothDevice> pairedDevs;
    private ArrayList<String> messages;

    private ArrayList<String> collection = new ArrayList(); //final list of strings
    private int workTime = 0;
    public static final String passColl = "wO2rS"; //needed for passing the numbe

    @Override
    public void onCreate(){
        super.onCreate();

        bMan = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bAdapt = BluetoothAdapter.getDefaultAdapter();

        messages = new ArrayList<>();

        if (bAdapt == null){
            String toast = "Buletooth Not Available.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }
        else {
            String toast = "Buletooth Available.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY; //keeps bluetooth running
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder{
        public BlueToothControl getServerIntance(){
            return BlueToothControl.this;
        }
    }

    //gets the paired devices on the phone and adds them to the list
    public ArrayAdapter getPair(){
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
        return adapter;
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

    //listens for data
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
                                            resultPage();
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

    //checks if bt is connected
    public boolean btConnected(){ return isBtConnected; }

    public void sendMes(String num){
        try {
            btSocket.getOutputStream().write(num.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //allows workout time to be changed
    public void setWorkTime(int seconds){workTime = seconds;}

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

    //moves to the results page
    private void resultPage(){
        Intent nextPage = new Intent(this, ResultScreen.class);
        nextPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        nextPage.putExtra(passColl, collection);
        startActivity(nextPage);
    }
}
