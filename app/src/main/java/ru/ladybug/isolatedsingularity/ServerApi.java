package ru.ladybug.isolatedsingularity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.ladybug.isolatedsingularity.retrofitmodels.ActionReportResponse;
import ru.ladybug.isolatedsingularity.retrofitmodels.AuthReportResponse;
import ru.ladybug.isolatedsingularity.retrofitmodels.JChain;
import ru.ladybug.isolatedsingularity.retrofitmodels.JContrib;
import ru.ladybug.isolatedsingularity.retrofitmodels.JUser;
import ru.ladybug.isolatedsingularity.retrofitmodels.MakeContribBody;

public interface ServerApi {
    @GET("chains/{id}")
    Call<JChain> getChainById(@Path("id") int id, @Query("token") String token);
    @GET("chains/")
    Call<List<JChain>> getChains(@Query("token") String token);
    @GET("chains/contrib/{id}")
    Call<List<JContrib>> getContribsByChain(@Path("id") int id, @Query("token") String token);
    @POST("chains/action/")
    Call<ActionReportResponse> makeContrib(@Body MakeContribBody body);
    @GET("login")
    Call<AuthReportResponse> tryLogin(@Query("username") String username, @Query("password") String password);
    @GET("user")
    Call<JUser> getUserData(@Query("token") String token);


    @GET("register")
    Call<AuthReportResponse> tryRegister(@Query("username") String username, @Query("password") String password);
}
