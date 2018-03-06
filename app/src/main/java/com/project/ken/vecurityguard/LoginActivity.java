package com.project.ken.vecurityguard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.ken.vecurityguard.Common.Common;

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

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        progress();

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
    }

    private void loginProcess() {
        final String email = mEmailEt.getText().toString();
        final String password = mPasswordEt.getText().toString();
        mProgressDialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Snackbar.make(mRootLayout, "Login successful", Snackbar.LENGTH_SHORT)
                                .show();
                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                        finish();
                    }


                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(mRootLayout, "Login failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
        mProgressDialog.dismiss();


    }

    private void progress() {
        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mProgressDialog.setMessage("Logging in........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }

}
