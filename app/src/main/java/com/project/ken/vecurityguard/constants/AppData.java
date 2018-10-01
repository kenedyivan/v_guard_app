package com.project.ken.vecurityguard.constants;

public class AppData {
    private static final String HOST = "http://10.0.3.2:8000";

    //Guard pool server
    private static final String SERVER_HOST = "http://10.0.2.2:3000";
    //private static final String SERVER_HOST = "http://vehcurity.com";

    public static String createGuardProcess() {
        return SERVER_HOST + "/guard-sessions-api/create-guarding-session";
    }
}
