package com.project.ken.vecurityguard.Common;

import android.location.Location;

import com.project.ken.vecurityguard.Remote.FCMRetrofitClient;
import com.project.ken.vecurityguard.Remote.IFCMService;
import com.project.ken.vecurityguard.Remote.IGoogleAPI;
import com.project.ken.vecurityguard.Remote.RetrofitClient;

public class Common {
    public static String currentToken = "";

    public static final String guards_tbl = "Guards";
    public static final String user_guard_tbl = "GuardsInformation";
    public static final String user_owner_tbl = "OwnersInformation";
    public static final String guard_request_tbl = "GuardRequest";
    public static final String fcm_tokens_tbl = "FcmTokens";

    public static Location mLastLocation = null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmBaseURL = "https://fcm.googleapis.com";

    public static IGoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);

    }

    public static IFCMService getFCMService(){
        return FCMRetrofitClient.getClient(fcmBaseURL).create(IFCMService.class);
    }
}
