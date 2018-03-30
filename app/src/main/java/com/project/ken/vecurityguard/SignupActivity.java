package com.project.ken.vecurityguard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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

import dmax.dialog.SpotsDialog;

public class SignupActivity extends AppCompatActivity {
    private static String TAG = SignupActivity.class.getSimpleName();
    EditText mNameEt;
    EditText mEmailEt;
    EditText mPhoneEt;
    EditText mPasswordEt;
    LinearLayout mRootLayout;
    Button mSignupBtn;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    //Presence System
    DatabaseReference onlineRef, currentUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //Sets actionbar back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_guard_tbl);

        initializeViews();
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerProcess();
            }
        });


    }


    private void initializeViews() {
        mNameEt = findViewById(R.id.name);
        mEmailEt = findViewById(R.id.email);
        mPhoneEt = findViewById(R.id.phone);
        mPasswordEt = findViewById(R.id.password);
        mSignupBtn = findViewById(R.id.email_sign_up_button);
        mRootLayout = findViewById(R.id.rootLayout);
    }

    private void registerProcess() {
        final String name = mNameEt.getText().toString();
        final String email = mEmailEt.getText().toString();
        final String phone = mPhoneEt.getText().toString();
        final String password = mPasswordEt.getText().toString();

        final AlertDialog waitingDialog = new SpotsDialog(SignupActivity.this, R.style.CustomSignUpDialog);
        waitingDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Guard user = new Guard();
                        user.setName(name);
                        user.setEmail(email);
                        user.setPhone(phone);
                        user.setPassword(password);

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(mRootLayout, "Sign up successful", Snackbar.LENGTH_SHORT)
                                                .show();
                                        waitingDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(mRootLayout, "Sign up failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                                .show();
                                        waitingDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(mRootLayout, "Sign up failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
                        waitingDialog.dismiss();
                    }
                });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
