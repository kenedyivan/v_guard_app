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
import com.project.ken.vecurityguard.CarOwnerCallActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification() != null &&
                Objects.equals(remoteMessage.getNotification().getTitle(), "starting")) {
            Log.d("Body", "" + remoteMessage.getNotification().getBody());
            Intent intent = new Intent(MyFirebaseMessagingService.this,CounterIntentService.class);
            startService(intent);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("counterStart"));
        } else {
            //Firebase message contains lat and lng from owner app
            JSONObject data;
            String ownerId = null;
            int duration = 0;
            double totalCost = 0;
            LatLng carOwnerLocation = null;

            try {
                data = new JSONObject(remoteMessage.getNotification().getBody());
                ownerId = data.getString("owner_id");
                duration = data.getInt("duration");
                totalCost = data.getDouble("total_cost");
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
            intent.putExtra("car_owner_id", remoteMessage.getNotification().getTitle());


            startActivity(intent);
        }


    }
}
