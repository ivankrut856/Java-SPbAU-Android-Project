package ru.ladybug.isolatedsingularity.net.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JUser {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("money")
    @Expose
    private int money;

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public int getId() {
        return id;
    }
}
