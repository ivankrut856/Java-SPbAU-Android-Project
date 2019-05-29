package ru.ladybug.isolatedsingularity;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.ladybug.isolatedsingularity.retrofitmodels.ActionReportResponse;
import ru.ladybug.isolatedsingularity.retrofitmodels.JChain;
import ru.ladybug.isolatedsingularity.retrofitmodels.JContrib;
import ru.ladybug.isolatedsingularity.retrofitmodels.JUser;
import ru.ladybug.isolatedsingularity.retrofitmodels.MakeContribBody;

import static android.support.v4.content.ContextCompat.getSystemService;

public class LocalState {

    //Finals
    private ChainData NO_CHAIN = new ChainData(new ChainView("No chain", "", null, -1), Collections.<ChainData.Contributor>emptyList(), BigInteger.ZERO);

    // Static
    private List<ChainView> markers;
    private UserIdentity user;

    // Dynamic
    private int currentChainId;
    private GeoPoint location;
    private ChainData currentChain;
    private UserData userData;

    // Service
    private Lock updateLock = new ReentrantLock();
    private Lock staticLock = new ReentrantLock();
    private boolean staticHasFinished = false;
    private List<Stateful> listeners = new ArrayList<>();
    private GeoPoint internalLocation;
//    private LocationManager locationManager;


    @SuppressLint("MissingPermission")
    public LocalState(Context context) {
        LocationManager manager = (LocationManager) getSystemService(context, LocationManager.class);
        Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        internalLocation = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                internalLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

    }

    public void addListener(Stateful listener) {
        staticLock.lock();
        listeners.add(listener);
        if (staticHasFinished)
            listener.initStatic();
        staticLock.unlock();
    }

    public void initStatic() {
        try {
            staticLock.lock();
            Log.d("map", "initStatic in state");
            markers = new ArrayList<>();
            Response<List<JChain>> response = RetrofitService.getInstance().getServerApi().getChains(user.getToken()).execute();
            if (!response.isSuccessful())
                throw new NetworkErrorException("Bad response");
            for (JChain chain : Objects.requireNonNull(response.body())) {
                markers.add(new ChainView(chain));
            }
            noStateFill();
            Log.d("map", "initStatic just before calls");

            for (Stateful listener : listeners) {
                listener.initStatic();
            }
            staticHasFinished = true;

            userData = fetchUserData();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Not implemented");
            // TODO Retry
        }
        finally {
            staticLock.unlock();
        }

    }

    private UserData fetchUserData() throws IOException {
        Response<JUser> response = RetrofitService.getInstance().getServerApi().getUserData(user.getToken()).execute();
        if (!response.isSuccessful())
            throw new IOException("Bad response");
        return new UserData(Objects.requireNonNull(response.body()));
    }

    private void noStateFill() {
        currentChainId = -1;
        location = internalLocation;
        currentChain = null;
    }

    public void update() {
        updateLock.lock();

        location = internalLocation;

        double minDistance = 100_000;
        ChainView nearestChain = null;
        for (ChainView view : markers) {
            double distance = view.getPosition().distanceToAsDouble(location);
            if (distance < minDistance) {
                minDistance = distance;
                nearestChain = view;
            }
        }
        Log.d("location", "updatePosition: " + minDistance);
        if (minDistance < 2_000) {
            currentChainId = nearestChain.getChainId();
        }
        else {
            currentChainId = -1;
        }

        try {
            currentChain = getChainById(currentChainId);
            userData = fetchUserData();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO Wtf
        }



        for (Stateful listener : listeners) {
            listener.updateDynamic();
        }
        updateLock.unlock();
    }

    public List<ChainView> getMarkers() {
        return markers;
    }

    public UserIdentity getUser() {
        return user;
    }

    public int getCurrentChainId() {
        return currentChainId;
    }

    public void setCurrentChainId(int id) throws IOException {
        updateLock.lock();

        currentChainId = id;
        currentChain = getChainById(currentChainId);

        updateLock.unlock();
    }

    private ChainData getChainById(int chainId) throws IOException {
        if (chainId == -1) {
            return NO_CHAIN;
        }

        UserIdentity user = getUser();
        Response<JChain> chainResponse = RetrofitService.getInstance().getServerApi().getChainById(chainId, user.getToken()).execute();
        Response<List<JContrib>> contribResponse = RetrofitService.getInstance().getServerApi().getContribsByChain(chainId, user.getToken())
                .execute();

        if (!chainResponse.isSuccessful() || !contribResponse.isSuccessful()) {
            throw new IOException("Request failed");
        }

        ChainView view = new ChainView(chainResponse.body());

        List<JContrib> contribs = contribResponse.body();
        contribs.sort(new Comparator<JContrib>() {
            @Override
            public int compare(JContrib o1, JContrib o2) {
                return new BigInteger(o2.getValue()).compareTo(new BigInteger(o1.getValue()));
            }
        });

        BigInteger myContribution = BigInteger.ZERO;
        List<ChainData.Contributor> contributors = new ArrayList<>();
        for (JContrib contrib : contribResponse.body()) {
            if (contrib.getUserId() == user.getUserId()) {
                myContribution = new BigInteger(contrib.getValue());
            }
            else
                contributors.add(new ChainData.Contributor(new BigInteger(contrib.getValue()), contrib.getUserName()));
        }

        return new ChainData(view, contributors, myContribution);
    }

    public GeoPoint getLocation() {
        return location;
    }

    public ChainData getCurrentChain() {
        return currentChain;
    }

    public void makeContrib(final Consumer<String> contribCallBack) {
        RetrofitService.getInstance().getServerApi().makeContrib(new MakeContribBody(currentChain.getView().getChainId(), String.valueOf(1), user.getToken()))
                .enqueue(new Callback<ActionReportResponse>() {
                    @Override
                    public void onResponse(Call<ActionReportResponse> call, Response<ActionReportResponse> response) {
                        if (!response.isSuccessful())
                            onFailure(call, new RuntimeException("Bad response"));

                        if (response.body().getResponse().equals("ok")) {
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    update();
                                }
                            });
                        }
                        contribCallBack.accept(response.body().getMessage());
                    }

                    @Override
                    public void onFailure(Call<ActionReportResponse> call, Throwable t) {
                    }
                });
    }

    public void setUser(UserIdentity user) {
        this.user = user;
    }

    public Lock getUpdateLock() {
        return updateLock;
    }

    public UserData getUserData() {
        return userData;
    }
}
