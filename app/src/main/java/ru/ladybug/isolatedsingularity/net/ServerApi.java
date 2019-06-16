package ru.ladybug.isolatedsingularity.net;

import java.util.List;

// ненужный импорт
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.ActionReportResponse;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.AuthReportResponse;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JChain;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JContrib;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JUser;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.MakeContribBody;

/** Retrofit flavour server api description */
public interface ServerApi {
    /**
     * Fetches chain view by given chain id
     * @param id the id of the chain
     * @param token the authorization token
     * @return api formatted chain view
     */
    @GET("chains/{id}")
    Call<JChain> getChainById(@Path("id") int id, @Query("token") String token);

    /**
     * Fetches all the chain view presented
     * @param token the authorization token
     * @return list of api formatted chain views
     */
    @GET("chains/")
    Call<List<JChain>> getChains(@Query("token") String token);

    /**
     * Fetches all contributor to chain with given id
     * @param id the id of the chain
     * @param token the authorization token
     * @return list of api formatted contributors
     */
    @GET("chains/contrib/{id}")
    Call<List<JContrib>> getContribsByChain(@Path("id") int id, @Query("token") String token);

    /**
     * Requests an action of contributing to the chain
     * @param body the parameters: the id of the chain to which to contribute, the authorization token
     * @return response in the api specified format
     */
    @POST("chains/action/")
    Call<ActionReportResponse> makeContrib(@Body MakeContribBody body);

    /**
     * Fetches user data by given token
     * @param token the authorization token
     * @return api formatted user data
     */
    @GET("user")
    Call<JUser> getUserData(@Query("token") String token);

    /**
     * Makes an attempt to login
     * @param username the username of the user
     * @param password the password of the user
     * @return the id of the user and the authorization token in api specified format
     */
    @GET("login")
    Call<AuthReportResponse> tryLogin(@Query("username") String username, @Query("password") String password);
}
