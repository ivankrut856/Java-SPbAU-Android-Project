package ru.ladybug.isolatedsingularity.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JContrib {
    @SerializedName("user_id")
    @Expose
    private int userId;

    @SerializedName("chain_id")
    @Expose
    private int chainId;

    @SerializedName("user_name")
    @Expose
    private String userName;

    private String value;

    public int getUserId() {
        return userId;
    }

    public int getChainId() {
        return chainId;
    }

    public String getValue() {
        return value;
    }

    public String getUserName() {
        return userName;
    }
}
