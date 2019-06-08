package ru.ladybug.isolatedsingularity.net.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Retrofit model for get responses to login attempt */
public class AuthReportResponse {
    @SerializedName("response")
    @Expose
    private String response;

    @SerializedName("user_id")
    @Expose
    private int userId;

    @SerializedName("token")
    @Expose
    private String token;

    public String getResponse() {
        return response;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }
}
