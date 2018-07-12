package com.project.ken.vecurityguard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.project.ken.vecurityguard.Common.Common;
import com.project.ken.vecurityguard.Helper.DirectionsJSONParser;
import com.project.ken.vecurityguard.Models.FCMResponse;
import com.project.ken.vecurityguard.Models.Notification;
import com.project.ken.vecurityguard.Models.Owner;
import com.project.ken.vecurityguard.Models.Sender;
import com.project.ken.vecurityguard.Models.Token;
import com.project.ken.vecurityguard.Remote.IFCMService;
import com.project.ken.vecurityguard.Remote.IGoogleAPI;
import com.project.ken.vecurityguard.Service.CounterIntentService;
import com.project.ken.vecurityguard.constants.AppData;
import com.project.ken.vecurityguard.sessions.SessionManager;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import at.markushi.ui.CircleButton;
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
    CircleButton btnCall;
    //ProgressBar progressBar;
    TextView prompt;

    private GoogleMap mMap;

    double ownerLat, ownerLng;
    int duration;
    double totalCost;
    String ownerId;

    //Ending dialog
    private View dialogView;
    AlertDialog dialog;

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

    CounterIntentService mCService;
    boolean mBound = false;

    ImageView imgExpandable;
    BottomSheetOwnerFragment mBottomSheet;

    TextView gOwnerName;
    TextView gCarName;
    TextView gLicenseNumber;
    ImageView gAvatar;

    SessionManager sessionManager;

    boolean doubleTap = false;

    MediaPlayer mediaPlayer;


    //Presence System
    DatabaseReference onlineRef, currentUserRef;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CounterIntentService.CounterBinder binder = (CounterIntentService.CounterBinder) service;
            mCService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    public void onBackPressed() {
        if(doubleTap){
            super.onBackPressed();
        }else{
            Toast.makeText(this,"Press Back again to exit!", Toast.LENGTH_SHORT)
                    .show();
            doubleTap = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleTap = false;
                }
            }, 500);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_tracking);

        btnCall = findViewById(R.id.btn_call);


        sessionManager = new SessionManager(GuardTrackingActivity.this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Init views
        btnStartGuarding = findViewById(R.id.btnStartGuarding);
        //progressBar = findViewById(R.id.progressBar);
        prompt = findViewById(R.id.prompt);
        //btnStartGuarding.setVisibility(View.GONE);
        //progressBar.setVisibility(View.VISIBLE);
        prompt.setVisibility(View.VISIBLE);
        prompt.setText("Get to the car location!");
        mCounterTv = findViewById(R.id.counter);
        crdCounter = findViewById(R.id.crdCounter);
        imgShield = findViewById(R.id.shield);

        //Owner
        gOwnerName = findViewById(R.id.owner_name);
        gCarName = findViewById(R.id.car_name);
        gLicenseNumber = findViewById(R.id.license_number);
        gAvatar = findViewById(R.id.avatar);

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

        FirebaseDatabase.getInstance().getReference(Common.user_owner_tbl)
                .child(ownerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Owner owner = dataSnapshot.getValue(Owner.class);
                        setBottomSheetFields(owner);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    private void openDialer(String phone_number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+phone_number));
        startActivity(intent);
    }

    private void setBottomSheetFields(final Owner owner) {

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialer(owner.getPhone());
            }
        });

        gOwnerName.setText(owner.getFirstName() + " " + owner.getLastName());
        gCarName.setText(owner.getBrand() + ", " + owner.getModel());
        gLicenseNumber.setText(owner.getLicenseNumber());
        if (owner.getAvatar() != null
                && !TextUtils.isEmpty(owner.getAvatar())) {
            Picasso.with(this)
                    .load(owner.getAvatar())
                    .into(gAvatar);
        }

        imgExpandable = findViewById(R.id.imageExpandable);
        mBottomSheet = BottomSheetOwnerFragment.newInstance("Owner bottom sheet");
        Bundle data = new Bundle();//create bundle instance
        data.putString("owner_name", owner.getFirstName() + " " + owner.getLastName());//put string to pass with a key value
        data.putString("car_name", owner.getBrand() + ", " + owner.getModel());//put string to pass with a key value
        data.putString("license_number", owner.getLicenseNumber());//put string to pass with a key value
        data.putString("avatar", owner.getAvatar());//put string to pass with a key value
        data.putString("car_image", owner.getCarImage());//put string to pass with a key value
        data.putString("duration", String.valueOf(duration));//put string to pass with a key value
        data.putString("total_cost", String.valueOf(totalCost));//put string to pass with a key value
        mBottomSheet.setArguments(data);
        mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
        imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });
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
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }


    private void saveGuardingToDB() {
        DatabaseReference mGuarding = FirebaseDatabase.getInstance().getReference("Guardings");
        Date d = new Date();
        //CharSequence s = DateFormat.format("yyyy-MM-dd hh:mm:ss", d.getTime());
        long millis = System.currentTimeMillis();

        int calcHours = duration / 60; //since both are ints, you get an int
        int calMinutes = duration % 60;

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        String start_time = cal.getTime().toString();
        cal.add(Calendar.HOUR_OF_DAY, calcHours);
        cal.add(Calendar.MINUTE, calMinutes);
        String end_time = cal.getTime().toString();
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        /*Log.d("Future Time", end_time);
        Log.d("Hour Time", ""+hours);
        Log.d("Minute Time", ""+minutes);
        Log.d("Month Time", ""+month);
        Log.d("Day Time", ""+day);*/

        createGuardingTask(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                ownerId, String.valueOf(duration), start_time, end_time,
                String.valueOf(totalCost), "0", minutes, hours, day, month);

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
        params.put("status", status);
        params.put("minute", minute);
        params.put("hour", hour);
        params.put("date", date);
        params.put("month", month);

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
        client.get(AppData.createGuardProcess() + String.valueOf(sb), params, new AsyncHttpResponseHandler() {

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
        final boolean[] isNoticeDispatched = {false};
        mMap = googleMap;

        ownerMaker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(ownerLat, ownerLng))
                .radius(50) //50 => radius set to 50m
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.destination_marker);
        mMap.addMarker(new MarkerOptions().position(new LatLng(ownerLat, ownerLng))
                .title("Destination"));
                //.icon(icon));

        //Create Geo fencing with radius of 50m
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.guards_tbl));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(ownerLat, ownerLng), 0.05f);

        //geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!isNoticeDispatched[0]){
                    Log.d("Notice Dispatch", String.valueOf(isNoticeDispatched[0]));
                    sendArrivedNotification(carOwnerId);
                    isNoticeDispatched[0] = true;
                }else{
                    Log.d("Notice Dispatch", String.valueOf(isNoticeDispatched[0]));
                }

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
                String.format("The guard %s has arrived at your location", sessionManager.getKeyName()));
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(GuardTrackingActivity.this, "Failed", Toast.LENGTH_SHORT)
                            .show();
                } else if (response.body().success == 1) {
                    btnStartGuarding.setVisibility(View.VISIBLE);
                    //progressBar.setVisibility(View.GONE);
                    prompt.setVisibility(View.GONE);
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
            /*guardMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker()));*/

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.guard);
            guardMarker = mMap.addMarker(new MarkerOptions()
                    .icon(icon)
                    .position(new LatLng(latitude, longitude))
                    .title("You"));

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

            //if (direction != null)
            try {
                direction = mMap.addPolyline(polylineOptions);
            } catch (NullPointerException e) {
                Toast.makeText(GuardTrackingActivity.this, "Can't get routes at this time", Toast.LENGTH_SHORT)
                        .show();
            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mMessageReceiver, new IntentFilter("counterStart"));
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mCounterUpdatesReceiver, new IntentFilter("counterUpdates"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).
                unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).
                unregisterReceiver(mCounterUpdatesReceiver);
        if (mConnection != null) {
            try {
                unbindService(mConnection);
            }catch (RuntimeException e){
                Log.w("Activity", e.getMessage());
            }

            mBound = false;
        }

    }

    /*@Override
    protected void onStop() {
        super.onStop();
        if(mConnection != null){
            unbindService(mConnection);
            mBound = false;
        }

    }*/

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {

            Log.d("BroadCast", "Start counter");
            bindCounterService();
            btnStartGuarding.setEnabled(false);
            btnStartGuarding.setText("Guarding");
            btnStartGuarding.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }

    };

    private void bindCounterService() {
        Intent i = new Intent(GuardTrackingActivity.this, CounterIntentService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        if (mBound) {
            int num = mCService.getRandomNumber();
            Toast.makeText(GuardTrackingActivity.this, "number: " + num, Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver mCounterUpdatesReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override

        public void onReceive(Context context, Intent intent) {

            String updates = intent.getStringExtra("counterUpdate");
            mCounterTv.setText(updates);
            if (Objects.equals(updates, "Done!")) {
                guardEndedDialog();
            }
        }

    };

    private void guardEndedDialog() {

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_guarding_ended, null);
        Button mBtnStopGuarding = dialogView.findViewById(R.id.btnStopGuarding);
        Button mBtnGuardAgain = dialogView.findViewById(R.id.btnGuardAgain);

        mBtnStopGuarding.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                mediaPlayer.release();
                dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("EXIT", true);
                finishAffinity();
                ActivityCompat.finishAffinity(GuardTrackingActivity.this);
                startActivity(intent);
                finish();
            }
        });

        mBtnGuardAgain.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                mediaPlayer.release();
                dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), GuardHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("AGAIN", true);
                finishAffinity();
                ActivityCompat.finishAffinity(GuardTrackingActivity.this);
                startActivity(intent);
                finish();
            }
        });

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GuardTrackingActivity.this);

        builder.setView(dialogView);
        // Set other dialog properties
        // Create the AlertDialog
        dialog = builder.create();

        dialog.setCancelable(false);

        /*dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {

            }
        });*/


        dialog.show();
    }

    @Override
    protected void onStop() {
        try{
            mediaPlayer.release();
        }catch (RuntimeException e){
            Log.d("MediaPlayer", e.getMessage());
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        try{
            mediaPlayer.release();
        }catch (RuntimeException e){
            Log.d("MediaPlayer", e.getMessage());
        }

        super.onPause();
    }


}
