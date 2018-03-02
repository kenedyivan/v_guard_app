package com.project.ken.vecurityguard.Common;

import com.project.ken.vecurityguard.Remote.IGoogleAPI;
import com.project.ken.vecurityguard.Remote.RetrofitClient;

public class Common {
    public static final String baseURL ="https://maps.googleapis.com";
    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);

    }
}
