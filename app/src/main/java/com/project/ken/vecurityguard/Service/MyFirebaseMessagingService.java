package com.project.ken.vecurityguard.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.project.ken.vecurityguard.CarOwnerCallActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Firebase message contains lat and lng from owner app
        LatLng carOwnerLocation = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

        Intent intent = new Intent(getBaseContext(), CarOwnerCallActivity.class);
        intent.putExtra("lat", carOwnerLocation.latitude);
        intent.putExtra("lng", carOwnerLocation.longitude);


        startActivity(intent);

    }
}
