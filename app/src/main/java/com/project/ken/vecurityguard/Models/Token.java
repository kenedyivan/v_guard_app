package com.project.ken.vecurityguard.Models;

/**
 * Created by ken on 3/6/18.
 */

public class Token {
    private String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
