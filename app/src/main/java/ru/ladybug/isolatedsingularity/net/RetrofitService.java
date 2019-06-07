package ru.ladybug.isolatedsingularity.net;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static RetrofitService instance;
    private static final String BASE_URL = "http://chain-srv.herokuapp.com:80/";
    private Retrofit retrofit;

    private RetrofitService() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static RetrofitService getInstance() {
        if (instance == null) {
            instance = new RetrofitService();
        }
        return instance;
    }

    public ServerApi getServerApi() {
        return retrofit.create(ServerApi.class);
    }
}
