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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();


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

        mEmailEt.setText("kenedyguard@gmail.com");
        mPasswordEt.setText("123456");
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

                        FirebaseDatabase.getInstance().getReference(Common.user_guard_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Common.currentGuard = dataSnapshot.getValue(Guard.class);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                        finish();
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
