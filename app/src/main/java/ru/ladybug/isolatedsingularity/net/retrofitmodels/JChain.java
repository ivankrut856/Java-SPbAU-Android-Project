package ru.ladybug.isolatedsingularity.net.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Retrofit model for get responses of fetching chain view */
public class JChain {
    @SerializedName("id")
    @Expose
    private String chainId;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("desc")
    @Expose
    private String description;

    @SerializedName("pos")
    @Expose
    private JPos position;

    public String getChainId() {
        return chainId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public JPos getPosition() {
        return position;
    }

    public static class JPos {
        @SerializedName("lat")
        @Expose
        private String lat;

        @SerializedName("lon")
        @Expose
        private String lon;

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }
    }

}
