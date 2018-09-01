package com.project.ken.vecurityguard.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.project.ken.vecurityguard.CancelRequestActivity;
import com.project.ken.vecurityguard.CarOwnerCallActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    LocalBroadcastManager localBroadcastManager;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());

        if (remoteMessage.getNotification() != null &&
                Objects.equals(remoteMessage.getNotification().getTitle(), "starting")) {
            Log.d("Body", "" + remoteMessage.getNotification().getBody());
            String duration = null;
            try {
                JSONObject data = new JSONObject(remoteMessage.getNotification().getBody());
                duration = data.getString("duration");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(MyFirebaseMessagingService.this, CounterIntentService.class);
            intent.putExtra("duration", duration);
            startService(intent);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("counterStart"));

        } else if (remoteMessage.getNotification() != null &&
                Objects.equals(remoteMessage.getNotification().getTitle(), "ending")) {
            Log.d("Body", "" + remoteMessage.getNotification().getBody());
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("counterEnd"));
        } else if (remoteMessage.getNotification() != null &&
                Objects.equals(remoteMessage.getNotification().getTitle(), "Cancel")) {
            //Log.d("Body", "" + remoteMessage.getNotification().getBody());
            //LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("cancelRequest"));
            Intent intent = new Intent(getBaseContext(), CancelRequestActivity.class);
            startActivity(intent);
        } else if(remoteMessage.getNotification() != null &&
                Objects.equals(remoteMessage.getNotification().getTitle(), "Payment")){
            JSONObject data;
            String type = null;
            String requestKey = null;

            try {
                data = new JSONObject(remoteMessage.getNotification().getBody());
                type = data.getString("type");
                requestKey = data.getString("request_key");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent localIntent = new Intent("paymentBroadcast");
            localIntent.putExtra("broadcast", "Payment");
            localIntent.putExtra("payment_type", type);
            localIntent.putExtra("request_key", requestKey);
            // Send local broadcast
            localBroadcastManager.sendBroadcast(localIntent);

        } else {
            //Firebase message contains lat and lng from owner app
            JSONObject data;
            String ownerId = null;
            String duration = null;
            double totalCost = 0;
            String requestKey = null;
            LatLng carOwnerLocation = null;

            try {
                data = new JSONObject(remoteMessage.getNotification().getBody());
                ownerId = data.getString("owner_id");
                duration = data.getString("duration");
                totalCost = data.getDouble("total_cost");
                requestKey = data.getString("request_key");
                JSONObject location = data.getJSONObject("location");
                carOwnerLocation = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                //Log.e("LatLng", "" + location.getDouble("lat") + " " + location.getDouble("lng"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //LatLng carOwnerLocation = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

            Intent intent = new Intent(getBaseContext(), CarOwnerCallActivity.class);
            assert carOwnerLocation != null;
            intent.putExtra("lat", carOwnerLocation.latitude);
            intent.putExtra("lng", carOwnerLocation.longitude);
            intent.putExtra("owner_id", ownerId);
            intent.putExtra("duration", duration);
            intent.putExtra("total_cost", totalCost);
            intent.putExtra("request_key", requestKey);
            intent.putExtra("car_owner_id", remoteMessage.getNotification().getTitle());


            startActivity(intent);
        }


    }
}
