package com.project.ken.vecurityguard.constants;

/**
 * Created by ken on 2/17/18.
 */

public class AppData {
    private static final String HOST = "http://10.0.3.2:8000";

    //Guard pool server
    public static final String POOLHOST = "http://10.0.3.2:3000";

    public static String getHost() {
        return HOST;
    }

    public static String LoginGuard() {
        return HOST + "/guard-login/";
    }

    public static String findGuard() {
        return POOLHOST + "/find-guard";
    }

    public static String finishGuardProcess() {
        return POOLHOST + "/finish-guard";
    }
}
