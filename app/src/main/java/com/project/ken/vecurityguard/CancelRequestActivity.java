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

public class CancelRequestActivity extends AppCompatActivity {
    Button btnApprove;

    MediaPlayer mediaPlayer;

    int duration;
    //Presence System
    DatabaseReference searchableRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_request);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        btnApprove = findViewById(R.id.btnApprove);
        btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequest();
            }
        });

    }

    private void cancelRequest() {
        Intent intent = new Intent(CancelRequestActivity.this, GuardHomeActivity.class);
        startActivity(intent);
        finish();
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
