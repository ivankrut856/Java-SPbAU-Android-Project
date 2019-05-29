package ru.ladybug.isolatedsingularity;

import org.osmdroid.util.GeoPoint;

import ru.ladybug.isolatedsingularity.retrofitmodels.JChain;

public class ChainView {
    private String title;
    private String description;
    private GeoPoint position;
    private int chainId;

    public ChainView(String title, String description, GeoPoint position, int chainId) {
        this.title = title;
        this.description = description;
        this.position = position;
        this.chainId = chainId;
    }

    public ChainView(JChain jchain) {
        title = jchain.getTitle();
        description = jchain.getDescription();
        JChain.JPos jpos = jchain.getPosition();
        position = new GeoPoint(Double.parseDouble(jpos.getLat()), Double.parseDouble(jpos.getLon()));
        chainId = Integer.parseInt(jchain.getChainId());
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public GeoPoint getPosition() {
        return position;
    }

    public int getChainId() {
        return chainId;
    }
}
