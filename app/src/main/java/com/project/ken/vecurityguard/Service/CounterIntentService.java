package com.project.ken.vecurityguard.Service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CounterIntentService extends IntentService {

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
                    Toast.makeText(getApplicationContext(),"My Awesome service toast...",Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

}
