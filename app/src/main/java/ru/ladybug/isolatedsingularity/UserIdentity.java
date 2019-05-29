package ru.ladybug.isolatedsingularity;

import android.text.Editable;

import java.io.IOException;
import java.util.function.Consumer;

import retrofit2.Callback;
import retrofit2.Response;
import ru.ladybug.isolatedsingularity.retrofitmodels.ActionReportResponse;
import ru.ladybug.isolatedsingularity.retrofitmodels.AuthReportResponse;

public class UserIdentity {
    public UserIdentity() {};
    private int userId;
    private String token;
    private String name;

    public static UserIdentity login(String username, String password) {
        UserIdentity user = new UserIdentity();
        try {
            Response<AuthReportResponse> response = RetrofitService.getInstance().getServerApi().tryLogin(username, password).execute();
            if (!response.isSuccessful())
                return null;
            if (!response.body().getResponse().equals("ok"))
                return null;

            user.userId = response.body().getUserId();
            user.token = response.body().getToken();
            return user;


        } catch (IOException e) {
            return null;
        }
    }

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
