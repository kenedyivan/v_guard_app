package com.project.ken.vecurityguard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.ken.vecurityguard.Common.Common;
import com.project.ken.vecurityguard.Models.Guard;
import com.project.ken.vecurityguard.Models.Guarding;
import com.project.ken.vecurityguard.sessions.SessionManager;

import java.util.Calendar;
import java.util.Date;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {
    private static String TAG = LoginActivity.class.getSimpleName();
    EditText mEmailEt;
    EditText mPasswordEt;
    Button mLoginBtn;
    LinearLayout mRootLayout;
    TextView mSignupTx;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    //Presence System
    DatabaseReference onlineRef, currentUserRef;

    SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        sessionManager = new SessionManager(LoginActivity.this);


        if (sessionManager.isLoggedIn()) {
            Intent i;
            i = new Intent(LoginActivity.this, GuardHomeActivity.class);
            startActivity(i);
            // close this activity
            finish();
        }



        if (getIntent().getBooleanExtra("EXIT", false)) {
            SessionManager sessionManager = new SessionManager(LoginActivity.this);
            sessionManager.setIsAcceptedTracking(false);
            //Presence System
            onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
            currentUserRef = FirebaseDatabase.getInstance().getReference(Common.guards_tbl)
                    .child(sessionManager.getUserID());
            onlineRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Remove value from Guard table when guard disconnects
                    currentUserRef.onDisconnect().removeValue();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            FirebaseDatabase.getInstance().goOffline();
            FirebaseAuth.getInstance().signOut();
            //finish();
        }

        //Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_guard_tbl);


        initializeViews();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginProcess();

            }
        });

        mSignupTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(i);
            }
        });
    }

    private void initializeViews() {
        mEmailEt = findViewById(R.id.email);
        mPasswordEt = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.email_sign_in_button);
        mRootLayout = findViewById(R.id.rootLayoutLogin);
        mSignupTx = findViewById(R.id.sign_up);

        /*mEmailEt.setText("kenedyakena@gmail.com");
        mPasswordEt.setText("123456");*/
    }

    private void loginProcess() {
        final String email = mEmailEt.getText().toString();
        final String password = mPasswordEt.getText().toString();

        final AlertDialog waitingDialog = new SpotsDialog(LoginActivity.this, R.style.CustomLoginDialog);
        waitingDialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Snackbar.make(mRootLayout, "Login successful", Snackbar.LENGTH_SHORT)
                                .show();
                        waitingDialog.dismiss();


                        DatabaseReference guardInfo = FirebaseDatabase.getInstance().getReference(Common.user_guard_tbl);
                        guardInfo.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Common.currentGuard = dataSnapshot.getValue(Guard.class);
                                        sessionManager.createLoginSession(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        sessionManager.guardData(Common.currentGuard.getName(),
                                                Common.currentGuard.getEmail(),
                                                Common.currentGuard.getPhone(),
                                                Common.currentGuard.getPassword(),
                                                Common.currentGuard.getAvatar());

                                        startActivity(new Intent(LoginActivity.this, GuardHomeActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        databaseError.getMessage();
                                    }
                                });
                        guardInfo.keepSynced(true);


                    }


                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(mRootLayout, "Login failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
                        .show();
                waitingDialog.dismiss();
            }
        });


    }


}
