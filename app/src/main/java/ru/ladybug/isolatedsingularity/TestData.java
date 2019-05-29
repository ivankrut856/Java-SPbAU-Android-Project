package ru.ladybug.isolatedsingularity;

import org.osmdroid.util.GeoPoint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class TestData {
    public static List<ChainView> chains = Arrays.asList(
            new ChainView("SPb Governor", "Go away! It can tax you", new GeoPoint(59.940266, 30.313810), 0)
    );

    public static List<ChainData> chainData = Arrays.asList(
            new ChainData(chains.get(0), Arrays.asList(
                    new ChainData.Contributor(BigInteger.valueOf(100), "John, the Living, the Emperor of the Terra"),
                    new ChainData.Contributor(BigInteger.valueOf(15), "Petr")
            ), BigInteger.valueOf(12))
    );
}
