package ru.ladybug.isolatedsingularity.net.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Retrofit model for responses of making an action */
public class ActionReportResponse {
    @SerializedName("response")
    @Expose
    private String response;

    @SerializedName("message")
    @Expose
    private String message;

    public String getResponse() {
        return response;
    }

    public String getMessage() {
        return message;
    }
}
