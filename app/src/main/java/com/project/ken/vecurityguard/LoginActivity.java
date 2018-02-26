package com.project.ken.vecurityguard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.project.ken.vecurityguard.constants.AppData;
import com.project.ken.vecurityguard.sessions.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {
    private static String TAG = LoginActivity.class.getSimpleName();
    EditText mEmailEt;
    EditText mPasswordEt;
    Button mLoginBtn;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(LoginActivity.this);

        if(sessionManager.isLoggedIn()){
            Intent i = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }

        initializeViews();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProcess();
            }
        });
    }

    private void initializeViews(){
        mEmailEt = findViewById(R.id.email);
        mPasswordEt = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.email_sign_in_button);
    }

    private void loginProcess(){
        String email = mEmailEt.getText().toString();
        String password = mPasswordEt.getText().toString();

        processLogin(email, password);
    }

    private void processLogin(String email, String password){
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);

        final ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mProgressDialog.setMessage("Logging in........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(AppData.LoginGuard(), params, new AsyncHttpResponseHandler() {

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

                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    int success = jsonObject.getInt("success");
                    int error = jsonObject.getInt("error");
                    int id = jsonObject.getInt("id");

                    Log.d("Success: ", String.valueOf(success));
                    Log.d("Error: ", String.valueOf(error));

                    if (error == 0 && success == 1 && id != 0) {

                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();

                        sessionManager.createLoginSession(String.valueOf(id));

                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        // Closing all the Activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        // Add new Flag to start new Activity
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(i);

                        finish();

                    } else if (error == 1 && success == 0) {
                        Toast.makeText(LoginActivity.this, "Login unsuccessful", Toast.LENGTH_LONG).show();

                    } else if (error == 2 && success == 0) {
                        Toast.makeText(LoginActivity.this, "No credentials", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Unknown error", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d(TAG, "failed " + statusCode);
                mProgressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                Log.d(TAG, "retryNO: " + retryNo);
                Toast.makeText(LoginActivity.this, "Taking too long", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
