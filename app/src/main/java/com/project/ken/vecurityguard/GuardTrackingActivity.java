package com.project.ken.vecurityguard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.project.ken.vecurityguard.Common.Common;
import com.project.ken.vecurityguard.Helper.DirectionsJSONParser;
import com.project.ken.vecurityguard.Models.FCMResponse;
import com.project.ken.vecurityguard.Models.Guarding;
import com.project.ken.vecurityguard.Models.Notification;
import com.project.ken.vecurityguard.Models.Sender;
import com.project.ken.vecurityguard.Models.Token;
import com.project.ken.vecurityguard.Remote.IFCMService;
import com.project.ken.vecurityguard.Remote.IGoogleAPI;
import com.project.ken.vecurityguard.constants.AppData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuardTrackingActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = GuardTrackingActivity.class.getSimpleName();

    Button btnStartGuarding;

    private GoogleMap mMap;

    double ownerLat, ownerLng;
    int duration;
    double totalCost;
    String ownerId;

    //Play services
    private static final int PLAY_SERVICE_RES_REQUEST = 6001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle ownerMaker;
    private Marker guardMarker;

    private Polyline direction;

    private IGoogleAPI mService;

    IFCMService mFCMService;

    GeoFire geoFire;

    String carOwnerId;

    //Views
    TextView mCounterTv;
    TextView mDoneTv;
    CardView crdCounter;
    ImageView imgShield;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_tracking);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Init views
        btnStartGuarding = findViewById(R.id.btnStartGuarding);
        btnStartGuarding.setVisibility(View.GONE);
        mCounterTv = findViewById(R.id.counter);
        mDoneTv = findViewById(R.id.done);
        crdCounter = findViewById(R.id.crdCounter);
        imgShield = findViewById(R.id.shield);

        btnStartGuarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGuarding(carOwnerId);
            }
        });

        if (getIntent() != null) {
            ownerLat = getIntent().getDoubleExtra("lat", -1.0);
            ownerLng = getIntent().getDoubleExtra("lng", -1.0);
            duration = getIntent().getIntExtra("duration", 0);
            totalCost = getIntent().getDoubleExtra("total_cost", 0);
            ownerId = getIntent().getStringExtra("owner_id");
            carOwnerId = getIntent().getStringExtra("car_owner_id");
        }

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        setUpLocation();
    }

    private void startGuarding(String carOwnerId) {
        Token token = new Token(carOwnerId);

        Notification notification = new Notification("Guarding", "Guarding has started");
        Sender sender = new Sender(token.getToken(), notification);


        saveGuardingToDB();


        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success == 1) {
                    Toast.makeText(GuardTrackingActivity.this, "Started guarding", Toast.LENGTH_SHORT).show();
                    runCounter();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void runCounter() {
        btnStartGuarding.setEnabled(false);
        btnStartGuarding.setText("Guarding...");
        crdCounter.setVisibility(View.VISIBLE);
        new CountDownTimer(10 * 1000, 1000) {

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
                mCounterTv.setVisibility(View.GONE);
                mDoneTv.setVisibility(View.VISIBLE);
                mDoneTv.setText("done!");
                imgShield.setImageResource(R.drawable.protected_shield_blue);
                btnStartGuarding.setText("Guarding complete...");
                counterFinished();
            }

        }.start();
    }

    private void counterFinished() {
        Log.d("Counter","Counter finished");
    }


    private void saveGuardingToDB() {
        DatabaseReference mGuarding = FirebaseDatabase.getInstance().getReference("Guardings");
        Date d = new Date();
        //CharSequence s = DateFormat.format("yyyy-MM-dd hh:mm:ss", d.getTime());
        long millis = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        String start_time = cal.getTime().toString();
        cal.add(Calendar.HOUR_OF_DAY, duration);
        String end_time = cal.getTime().toString();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        /*Guarding guarding = new Guarding();
        guarding.setGuard(FirebaseAuth.getInstance().getCurrentUser().getUid());
        guarding.setOwner(ownerId);
        guarding.setDuration(String.valueOf(duration));
        guarding.setStart_time(start_time);
        guarding.setEnd_time(end_time);
        guarding.setTotalCost(String.valueOf(totalCost));
        guarding.setStatus("0");
        String key = mGuarding.child("guard_time").push().getKey();
        mGuarding.child(key)
                .setValue(guarding);*/
        
        createGuardingTask(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                ownerId,String.valueOf(duration),start_time,end_time,
                String.valueOf(totalCost),"0",minutes,hours,day,month);

    }

    private void createGuardingTask(String guardId, String ownerId, String duration,
                                    String startTime, String endTime, String totalCost, String status,
                                    int minute, int hour, int date, int month) {
        RequestParams params = new RequestParams();
        params.put("guardId", guardId);
        params.put("ownerId", ownerId);
        params.put("duration", duration);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("totalCost", totalCost);
        params.put("status", status );
        params.put("minute", minute );
        params.put("hour", hour );
        params.put("date", date );
        params.put("month", month );

        StringBuilder sb = new StringBuilder();
        sb.append("/");
        sb.append(guardId);
        sb.append("/");
        sb.append(ownerId);
        sb.append("/");
        sb.append(duration);
        sb.append("/");
        sb.append(startTime);
        sb.append("/");
        sb.append(endTime);
        sb.append("/");
        sb.append(totalCost);
        sb.append("/");
        sb.append(status);
        sb.append("/");
        sb.append(minute);
        sb.append("/");
        sb.append(hour);
        sb.append("/");
        sb.append(date);
        sb.append("/");
        sb.append(month);

        Log.d("Params", String.valueOf(sb));

        final ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(GuardTrackingActivity.this);
        mProgressDialog.setMessage("Creating guard session........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(AppData.createGuardProcess()+String.valueOf(sb), params, new AsyncHttpResponseHandler() {

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

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d(TAG, "failed " + statusCode);
                mProgressDialog.dismiss();
                Toast.makeText(GuardTrackingActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                Log.d(TAG, "retryNO: " + retryNo);
                Toast.makeText(GuardTrackingActivity.this, "Taking too long", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ownerMaker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(ownerLat, ownerLng))
                .radius(50) //50 => radius set to 50m
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));

        //Create Geo fencing with radius of 50m
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.guards_tbl));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(ownerLat, ownerLng), 0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(carOwnerId);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void sendArrivedNotification(String carOwnerId) {
        Token token = new Token(carOwnerId);
        Notification notification = new Notification("Arrived",
                String.format("The guard %s has arrived at your location", Common.currentGuard.getName()));
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(GuardTrackingActivity.this, "Failed", Toast.LENGTH_SHORT)
                            .show();
                } else if (response.body().success == 1) {
                    btnStartGuarding.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    private void setUpLocation() {

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();

        }

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }

        return true;
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                this);

    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocation != null) {

            final double latitude = Common.mLastLocation.getLatitude();
            final double longitude = Common.mLastLocation.getLongitude();

            if (guardMarker != null)
                guardMarker.remove();
            guardMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker()));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));


            if (direction != null)
                direction.remove();
            getDirection();


        } else {
            Log.d("ERROR", "Cannot get your location");
        }
    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + ownerLat + "," + ownerLng + "&" +
                    "keys=" + getResources().getString(R.string.google_direction_api);
            Log.d("EDMTDEV", requestApi); // Print URL for debugging

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.d("Response", response.body());
                            try {

                                new ParserTask().execute(response.body());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(GuardTrackingActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog mDialog = new ProgressDialog(GuardTrackingActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please wait...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {

                jsonObject = new JSONObject(strings[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }

            if (direction != null)
                direction = mMap.addPolyline(polylineOptions);
        }
    }
}
