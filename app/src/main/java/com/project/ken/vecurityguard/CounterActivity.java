package com.project.ken.vecurityguard;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CounterActivity extends AppCompatActivity {

    TextView mCounterTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        mCounterTv = findViewById(R.id.counter);
        final long[] secondsCount = new long[1];

        long time = getIntent().getLongExtra("durationMillis",0);

        new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;
                //mCounterTv.setText(String.valueOf(millisUntilFinished / 1000));
                //here you can have your logic to set text to edittext

                mCounterTv.setText(String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
            }

            public void onFinish() {
                mCounterTv.setText("done!");
            }

        }.start();
    }
}
