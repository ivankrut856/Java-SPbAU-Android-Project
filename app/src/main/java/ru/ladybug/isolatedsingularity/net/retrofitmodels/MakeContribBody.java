package ru.ladybug.isolatedsingularity.net.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MakeContribBody {
    @SerializedName("action_code")
    @Expose
    private int actionCode = 1;

    @SerializedName("chain_id")
    @Expose
    private int chainId;

    @SerializedName("contrib")
    @Expose
    private String contrib;

    @SerializedName("token")
    @Expose
    private String token;

    public MakeContribBody(int chainId, String contrib, String token) {
        this.chainId = chainId;
        this.contrib = contrib;
        this.token = token;
    }
}
