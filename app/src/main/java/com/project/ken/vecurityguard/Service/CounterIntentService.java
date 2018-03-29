package com.project.ken.vecurityguard.Service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.project.ken.vecurityguard.R;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CounterIntentService extends IntentService {



    // Binder given to clients
    private final IBinder mBinder = new CounterBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    private String counterUpdate;
    private CountDownTimer countDownTimer;


    public CounterIntentService() {
        super("CounterIntentService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d("Counter", "Howdy from counter");
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mCounterStopReceiver, new IntentFilter("counterEnd"));
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).
                unregisterReceiver(mCounterStopReceiver);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String duration = intent.getStringExtra("duration");
            long d = Long.parseLong(duration);
            Log.d("D", ""+d);
            //final long dura = TimeUnit.HOURS.toMillis(d);
            final long dura = TimeUnit.MINUTES.toMillis(d);
            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(),"My Awesome service toast...",Toast.LENGTH_SHORT).show();
                    Log.d("Counter", "Counter started");
                    runCounter(dura);
                }
            });

        }
    }

    public class CounterBinder extends Binder {
        public CounterIntentService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CounterIntentService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

    private void runCounter(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {

            public void onTick(long millisUntilFinished) {
                //mCounterTv.setText(String.valueOf(millisUntilFinished / 1000));
                //here you can have your logic to set text to edittext

                counterUpdate = String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                Intent counterIntent = new Intent("counterUpdates");
                counterIntent.putExtra("counterUpdate",counterUpdate);
                LocalBroadcastManager.getInstance(CounterIntentService.this).sendBroadcast(counterIntent);

            }

            public void onFinish() {
                counterUpdate = "Done!";
                Intent counterIntent = new Intent("counterUpdates");
                counterIntent.putExtra("counterUpdate",counterUpdate);
                LocalBroadcastManager.getInstance(CounterIntentService.this).sendBroadcast(counterIntent);

            }

        }.start();
    }

    private BroadcastReceiver mCounterStopReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {

            Log.d("Stop", "Stop");
            counterUpdate = "Done!";
            Intent counterIntent = new Intent("counterUpdates");
            counterIntent.putExtra("counterUpdate",counterUpdate);
            LocalBroadcastManager.getInstance(CounterIntentService.this).sendBroadcast(counterIntent);
            countDownTimer.cancel();
        }

    };



}
