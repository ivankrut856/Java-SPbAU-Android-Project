package ru.ladybug.isolatedsingularity.net;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Retrofit settings class */
public class RetrofitService {
    private static RetrofitService instance;
    private Retrofit retrofit;

    /** The remote host */
    private static final String BASE_URL = "http://chain-srv.herokuapp.com:80/";

    private RetrofitService() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /** Singleton factory */
    public static RetrofitService getInstance() {
        // это непотокобезопасный синглтон
        if (instance == null) {
            instance = new RetrofitService();
        }
        return instance;
    }

    /** Server api factory */
    public ServerApi getServerApi() {
        return retrofit.create(ServerApi.class);
    }
}
