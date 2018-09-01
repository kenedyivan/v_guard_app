package com.project.ken.vecurityguard;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.ken.vecurityguard.Common.Common;
import com.project.ken.vecurityguard.Models.FCMResponse;
import com.project.ken.vecurityguard.Models.Notification;
import com.project.ken.vecurityguard.Models.Sender;
import com.project.ken.vecurityguard.Models.Token;
import com.project.ken.vecurityguard.Remote.IFCMService;
import com.project.ken.vecurityguard.Remote.IGoogleAPI;
import com.project.ken.vecurityguard.sessions.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarOwnerCallActivity extends AppCompatActivity {

    TextView txtTime, txtAddress, txtDistance, txtGuardingTime;

    Button btnCancel, btnAccept;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;

    String carOwnerId;

    double lat, lng;
    String duration;
    double totalCost;
    String ownerDBId;
    private String requestKey;

    //Presence System
    DatabaseReference searchableRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_owner_call);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        //Init views
        txtAddress = findViewById(R.id.txtAddress);
        txtDistance = findViewById(R.id.txtDistance);
        txtTime = findViewById(R.id.txtTime);
        txtGuardingTime = findViewById(R.id.txtGuardingTime);

        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(carOwnerId)) {
                    cancelBooking(carOwnerId);
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptRequest(carOwnerId);
            }
        });


        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);
            duration = getIntent().getStringExtra("duration");
            totalCost = getIntent().getDoubleExtra("total_cost", 0);
            ownerDBId = getIntent().getStringExtra("owner_id");
            carOwnerId = getIntent().getStringExtra("car_owner_id");
            requestKey = getIntent().getStringExtra("request_key");

            getDirection(lat, lng);
        }

        txtGuardingTime.setText("Guarding time: "+duration);

    }

    private void acceptRequest(final String carOwnerId) {
        SessionManager sessionManager = new SessionManager(CarOwnerCallActivity.this);
        sessionManager.setIsAcceptedTracking(true);
        //Makes guard unsearchable when busy
        searchableRef = FirebaseDatabase.getInstance().getReference("SearchableGuards")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        searchableRef.removeValue();

        Token token = new Token(carOwnerId);

        Notification notification = new Notification("Accept", FirebaseAuth.getInstance().getCurrentUser().getUid() + ":" + ownerDBId);
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success == 1) {
                    Toast.makeText(CarOwnerCallActivity.this, "Accepted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CarOwnerCallActivity.this, GuardTrackingActivity.class);
                    //Sends car owner location to GuardTrackingActivity
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    intent.putExtra("owner_id", ownerDBId);
                    intent.putExtra("duration", duration);
                    intent.putExtra("total_cost", totalCost);
                    intent.putExtra("request_key", requestKey);
                    intent.putExtra("car_owner_id", carOwnerId);

                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void cancelBooking(String carOwnerId) {
        Token token = new Token(carOwnerId);

        Notification notification = new Notification("Cancel", "Guard has cancelled your request");
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success == 1) {
                    Toast.makeText(CarOwnerCallActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void getDirection(double lat, double lng) {

        String destination = lat + "," + lng;

        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() + "&" +
                    "destination=" + destination + "&" +
                    "keys=" + getResources().getString(R.string.google_direction_api);

            Log.d("EDMTDEV", requestApi); // Print URL for debugging

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.d("Response", response.body());
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                JSONArray routes = jsonObject.getJSONArray("routes");

                                //After getting routes, get first element of routes
                                JSONObject object = routes.getJSONObject(0);

                                //After getting first element, we need get array with name "legs"
                                JSONArray legs = object.getJSONArray("legs");

                                //and get first element of legs array
                                JSONObject legsObject = legs.getJSONObject(0);

                                //Now, get Distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                //txtDistance.setText(distance.getString("text")+" away");

                                //Now, get Time
                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text")+" away");

                                //Now, get address
                                String address = legsObject.getString("end_address");
                                txtAddress.setText(address);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CarOwnerCallActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }
}
