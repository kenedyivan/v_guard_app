package com.project.ken.vecurityguard;

import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    TextView mCarOwnerName;
    TextView mLicenseNumber;
    TextView mVehicleType;
    TextView mDuration;
    TextView mResponseTime;
    TextView mRequestArea;
    LinearLayout mRequestLayout;
    Button mAcceptBtn;
    Button mDeclinetBtn;
    CountDownTimer mCountDownTimer;
    boolean timerRun;
    String oId;
    boolean accept = false;


    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://10.0.2.2:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCarOwnerName = findViewById(R.id.car_owner_name);
        mLicenseNumber = findViewById(R.id.license_number);
        mVehicleType = findViewById(R.id.vehicle_type);
        mDuration = findViewById(R.id.duration);
        mResponseTime = findViewById(R.id.response_time);
        mRequestArea = findViewById(R.id.request_area);
        mRequestLayout = findViewById(R.id.request_layout);
        mAcceptBtn = findViewById(R.id.accept);
        mDeclinetBtn = findViewById(R.id.decline);

        hideRequestLayout();

        mSocket.emit("disconn guard", "2");
        mSocket.emit("add guard", "2");
        mSocket.on("send request", onNewRequest);
        mSocket.connect();


    }

    private void hideRequestLayout() {
        mRequestLayout.setVisibility(View.GONE);
        mRequestArea.setVisibility(View.VISIBLE);
    }

    private void showRequestLayout() {
        mRequestLayout.setVisibility(View.VISIBLE);
        mRequestArea.setVisibility(View.GONE);
    }

    private Emitter.Listener onNewRequest = new Emitter.Listener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void call(final Object... args) {
            Log.d("Request", Arrays.toString(args));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parseRequest(args);
                }
            });

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new request", onNewRequest);
    }

    private void parseRequest(Object[] args) {

        JSONObject data = (JSONObject) args[0];
        String carOwnerId = null;
        String carOwnerName = null;
        String licenseNumber = null;
        String vehicleType = null;
        String duration = null;

        try {
            carOwnerId = data.getString("carOwnerId");
            carOwnerName = data.getString("carOwnerName");
            licenseNumber = data.getString("licenseNumber");
            vehicleType = data.getString("vehicleType");
            duration = data.getString("duration");

            oId = carOwnerId;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        processRequest(new RequestBody(carOwnerId, carOwnerName,
                licenseNumber, vehicleType, duration));
    }

    private void processRequest(final RequestBody requestBody) {

        showTimer();
        mCountDownTimer.start();
        timerRun = true;

        showRequestLayout();
        mCarOwnerName.setText(requestBody.ownerName);
        mLicenseNumber.setText(requestBody.licenseNumber);
        mVehicleType.setText(requestBody.vehicleType);
        mDuration.setText(requestBody.duration + " hr(s)");

        mAcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    acceptRequest(requestBody.ownerId, Long.parseLong(requestBody.duration));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mDeclinetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    declineRequest(requestBody.ownerId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void showTimer() {
        mCountDownTimer = new CountDownTimer(10 * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                mResponseTime.setText(String.format(Locale.ENGLISH, "%02d %s",
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished), "secs"));
            }

            public void onFinish() {
                mResponseTime.setText(R.string.time_done);
                try {
                    noResponse();
                    if(!accept){
                        showSnackbar();
                    }

                    hideRequestLayout();
                    timerRun = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };
    }

    private void showSnackbar() {
        View container = findViewById(R.id.activity_main);
        if (container != null) {
            Snackbar.make(container, "Request canceled", Snackbar.LENGTH_LONG).show();
        }
    }

    private void acceptRequest(String ownerId, long duration) throws JSONException {

        if(timerRun){
            mCountDownTimer.cancel();
            timerRun = false;
        }

        accept = true;

        mSocket.emit("accept", new JSONObject()
                .put("guardId", "2")
                .put("ownerId", ownerId)
                .put("status", "accepted"));

        Intent i = new Intent(MainActivity.this, CounterActivity.class);
        i.putExtra("durationMillis", TimeUnit.HOURS.toMillis(duration));
        startActivity(i);
        finish();
    }

    private void declineRequest(String ownerId) throws JSONException {
        if(timerRun){
            mCountDownTimer.cancel();
            timerRun = false;
        }

        mSocket.emit("decline", new JSONObject()
                .put("guardId", "2")
                .put("ownerId", ownerId)
                .put("status", "declined"));
        hideRequestLayout();
    }

    private void noResponse() throws JSONException {
        mSocket.emit("no response", new JSONObject()
                .put("guardId", "2")
                .put("ownerId", oId)
                .put("status", "No response"));
        hideRequestLayout();
    }

    class RequestBody {
        String ownerId;
        String ownerName;
        String licenseNumber;
        String vehicleType;
        String duration;

        RequestBody(String ownerId, String ownerName,
                    String licenseNumber, String vehicleType,
                    String duration) {
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            this.licenseNumber = licenseNumber;
            this.vehicleType = vehicleType;
            this.duration = duration;
        }

    }

}
