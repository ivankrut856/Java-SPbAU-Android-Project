package ru.ladybug.isolatedsingularity.net.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Retrofit model for post requests of making contribution
 */
public class MakeContribBody {
    /** Action code = 1 for this type of actions */
    @SerializedName("action_code")
    @Expose
    private int actionCode = 1;

    /** The id of the chain to which to contribute */
    @SerializedName("chain_id")
    @Expose
    private int chainId;

    /** The authorization token */
    @SerializedName("token")
    @Expose
    private String token;

    public MakeContribBody(int chainId, String token) {
        this.chainId = chainId;
        this.token = token;
    }
}
