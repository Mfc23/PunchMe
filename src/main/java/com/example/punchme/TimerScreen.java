package com.example.punchme;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class TimerScreen extends AppCompatActivity {

    //variables needed to make the buzzer
    private SoundPool buzzer;
    private int soundbuz;

    private TextView countDown; //the clock that displays on screen
    private CountDownTimer countTimer; //the count down
    private long milliLeft; //keeps track of the milliseconds left
    private int workTime; //the original time in seconds

    public BlueToothControl bCon;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_screen);

        Intent intent = new Intent(this, BlueToothControl.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        //gets the seconds the user wanted for the timer
        Intent theTime = getIntent();
        workTime = theTime.getIntExtra(startWorkout.passNum, 0);
        milliLeft = workTime * 1000;
        countDown = findViewById(R.id.timeLeft);

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

        //starts up the timer
        theTimer();
    }

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
                playSound();

                //end taking in signals
                bCon.sendMes("3");
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


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connected = true;
            BlueToothControl.LocalBinder mLocalbinder = (BlueToothControl.LocalBinder) service;
            bCon = mLocalbinder.getServerIntance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bCon = null;
            connected = false;
        }
    };

    //ends the sound so the resources are saved
    @Override
    protected void onDestroy() {
        super.onDestroy();
        buzzer.release();
        buzzer = null;
    }
}

