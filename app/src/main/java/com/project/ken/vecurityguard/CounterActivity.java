package com.project.ken.vecurityguard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.project.ken.vecurityguard.constants.AppData;
import com.project.ken.vecurityguard.sessions.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class CounterActivity extends AppCompatActivity {

    TextView mCounterTv;
    SessionManager sessionManager;
    private static final String TAG = CounterActivity.class.getSimpleName();

    String ownerId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        sessionManager = new SessionManager(CounterActivity.this);

        mCounterTv = findViewById(R.id.counter);
        final long[] secondsCount = new long[1];

        long time = getIntent().getLongExtra("durationMillis",0);
         ownerId = getIntent().getStringExtra("ownerId");

        new CountDownTimer(10*1000, 1000) {

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
                finishedGuarding();
            }

        }.start();
    }

    private void finishedGuarding(){
        RequestParams params = new RequestParams();
        params.put("ownerId", ownerId);
        params.put("guardId", sessionManager.getUserID());

        final ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(CounterActivity.this);
        mProgressDialog.setMessage("Finishing guard process........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(AppData.finishGuardProcess(), params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Log.d(TAG, "Started request");
                mProgressDialog.show();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                Log.d(TAG, "Status: " + statusCode);
                String resp = new String(response);
                Log.d(TAG, "Response: " + resp);
                mProgressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    int success = jsonObject.getInt("success");

                    Log.d("Success: ", String.valueOf(success));

                    if (success == 1) {
                        Toast.makeText(CounterActivity.this, "Guarding finished", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(CounterActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(CounterActivity.this, "Failed finishing guard", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d(TAG, "failed " + statusCode);
                mProgressDialog.dismiss();
                Toast.makeText(CounterActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                Log.d(TAG, "retryNO: " + retryNo);
                Toast.makeText(CounterActivity.this, "Taking too long", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
