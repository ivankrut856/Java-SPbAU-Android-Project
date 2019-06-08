package ru.ladybug.isolatedsingularity;

import java.io.IOException;

import retrofit2.Response;
import ru.ladybug.isolatedsingularity.net.RetrofitService;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.AuthReportResponse;

/** Class represents user credentials after login */
public class UserIdentity {
    private int userId;
    private String token;

    /** Empty constructor */
    public UserIdentity() {}

    /** Produces user identity from given id and authentication token */
    public static UserIdentity fromData(int userId, String token) {
        UserIdentity user = new UserIdentity();
        user.userId = userId;
        user.token = token;
        return user;
    }

    public int getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }
}
