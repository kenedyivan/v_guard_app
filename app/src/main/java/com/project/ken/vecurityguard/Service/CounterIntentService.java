package com.project.ken.vecurityguard.Service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import java.util.Random;

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


    public CounterIntentService() {
        super("CounterIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(),"My Awesome service toast...",Toast.LENGTH_SHORT).show();
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



}
