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

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.ladybug.isolatedsingularity.net.RetrofitService;
import ru.ladybug.isolatedsingularity.net.StatefulFragment;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.ActionReportResponse;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JChain;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JContrib;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JUser;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.MakeContribBody;

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
    private List<StatefulFragment> listeners = new ArrayList<>();
    private GeoPoint internalLocation;

    // Rx
    private Observable<Long> updateObservable;
    private Observable<Long> staticObservable;


    @SuppressLint("MissingPermission")
    public LocalState(Context context, UserIdentity user) {

        this.user = user;
        noStateFill();

        LocationManager manager = getSystemService(context, LocationManager.class);
        Location lastLocation = Objects.requireNonNull(manager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
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

        initUpdateCycle();
    }

    public void initStatic() throws IOException, NetworkErrorException {
        try {
            staticLock.lock();
            markers = new ArrayList<>();
            Response<List<JChain>> response = RetrofitService.getInstance().getServerApi().getChains(user.getToken()).execute();
            if (!response.isSuccessful())
                throw new NetworkErrorException("Bad response");
            for (JChain chain : Objects.requireNonNull(response.body())) {
                markers.add(new ChainView(chain));
            }

            userData = fetchUserData();
        }
        catch (Exception e) {
            throw e;
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

        updateLock.unlock();
    }

    public List<ChainView> getMarkers() {
        staticLock.lock();
        List<ChainView> result = markers;
        staticLock.unlock();
        return result;
    }

    public UserIdentity getUser() {
        staticLock.lock();
        UserIdentity result = user;
        staticLock.unlock();
        return result;
    }

    public int getCurrentChainId() {
        updateLock.lock();
        int result = currentChainId;
        updateLock.unlock();
        return result;
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
        updateLock.lock();
        GeoPoint result = location;
        updateLock.unlock();
        return result;
    }

    public ChainData getCurrentChain() {
        updateLock.lock();
        ChainData result = currentChain;
        updateLock.unlock();
        return result;
    }

    public void makeContrib(final Consumer<String> contribCallBack) {
        updateLock.lock();
        RetrofitService.getInstance().getServerApi().makeContrib(new MakeContribBody(currentChain.getView().getChainId(), String.valueOf(1), user.getToken()))
                .enqueue(new Callback<ActionReportResponse>() {
                    @Override
                    public void onResponse(Call<ActionReportResponse> call, Response<ActionReportResponse> response) {
                        if (!response.isSuccessful())
                            onFailure(call, new RuntimeException("Bad response"));

                        if (response.body().getResponse().equals("ok")) {
                            AsyncTask.execute(() -> update());
                        }
                        contribCallBack.accept(response.body().getMessage());
                    }

                    @Override
                    public void onFailure(Call<ActionReportResponse> call, Throwable t) {
                    }
                });
        updateLock.unlock();
    }

    public UserData getUserData() {
        updateLock.lock();
        UserData result = userData;
        updateLock.unlock();
        return result;
    }

    private void initUpdateCycle() {
        staticObservable = Observable.fromCallable(() -> {
            initStatic();
            return 0L;
        }).subscribeOn(Schedulers.io()).doOnNext(tick -> Log.d("Stateful", "staticObservable:")).cache();

        updateObservable = Observable.interval(0, 3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(tick -> {
                    update();
                    return tick;
                }).doOnNext(tick -> Log.d("Stateful", "updateObservable:")).share().doOnSubscribe(tick -> Log.d("Stateful", "subscribed"));
    }

    public Observable<Long> getUpdates() {
        return updateObservable;
    }

    public Observable<Long> getStatics() {
        return staticObservable;
    }
}
