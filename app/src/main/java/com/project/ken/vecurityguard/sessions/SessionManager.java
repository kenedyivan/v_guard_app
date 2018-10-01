package com.project.ken.vecurityguard.sessions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.project.ken.vecurityguard.LoginActivity;

/**
 * Created by ken on 2/17/18.
 */

public class SessionManager {
    private static final String KEY_NAME = "NAME";
    private static final String KEY_EMAIL = "EMAIL";
    private static final String KEY_PHONE = "PHONE";
    private static final String KEY_PASSWORD = "PASSWORD";
    private static final String KEY_AVATAR = "AVATAR";
    private static final String IS_ONLINE = "IsOnline";
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "com.magicdigital.ken.vecurityguard.vecuritypref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // All Shared Preferences Keys
    private static final String IS_ACCEPTED_TRACKING = "IsAcceptedTracking";

    // Guard id (make variable public to access from outside)
    public static final String KEY_ID = "id";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public  String getKeyName() {
        return pref.getString(KEY_NAME, null);
    }

    public String getKeyEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getKeyPhone() {
        return pref.getString(KEY_PHONE, null);
    }

    public String getKeyPassword() {
        return pref.getString(KEY_PASSWORD, null);
    }

    public String getKeyAvatar() {
        return pref.getString(KEY_AVATAR, null);
    }

    public void setIsAcceptedTracking(Boolean accepted){
        // Storing login value as TRUE
        editor.putBoolean(IS_ACCEPTED_TRACKING, accepted);
        // commit changes
        editor.commit();
    }

    public boolean isTracking(){
        return pref.getBoolean(IS_ACCEPTED_TRACKING, false);
    }

    /**
     * Create login session
     */
    public void createLoginSession(String id) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing id in pref
        editor.putString(KEY_ID, id);

        // commit changes
        editor.commit();
    }


    public void guardData(String name, String email, String phone, String password, String avatar){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_AVATAR, avatar);

        // commit changes
        editor.commit();
    }

    public void guard(String name, String email){

        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);

        // commit changes
        editor.commit();
    }

    public void guardAvatarImage(String avatar){

        editor.putString(KEY_AVATAR, avatar);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Starting Login Activity
            _context.startActivity(i);
        }

    }

    public String getUserID() {
        return pref.getString(KEY_ID, null);
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    // Get Login State
    public boolean isOnline() {
        return pref.getBoolean(IS_ONLINE, false);
    }

    public void setIsOnline() {
        editor.putBoolean(IS_ONLINE, true);
        editor.commit();
    }

    public void setIsOffline() {
        editor.putBoolean(IS_ONLINE, false);
        editor.commit();
    }
}
