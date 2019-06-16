package ru.ladybug.isolatedsingularity;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.ladybug.isolatedsingularity.net.RetrofitService;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.ActionReportResponse;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JChain;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JContrib;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.JUser;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.MakeContribBody;

import static android.support.v4.content.ContextCompat.getSystemService;

/** Main state of the client relevant game world */
public class LocalState {

    //Finals
    /** Fake chain using when no other chains around or an exception occurred during nearest chain update*/
    public final ChainData NO_CHAIN = new ChainData(new ChainView("No chain around", "", null, -1), Collections.emptyList(), BigInteger.ZERO);
    /** Fake user using when an exception occurred during user data update */
    public final UserData NO_USER = new UserData("No user", 0);
    /** Default location using when it is not possible to determine real user location */
    /*
     * Отсутствие геокоординат лучше обрабатывать отдельно, и не разрешать пользователю ничего делать,
     *   пока его координаты не будут определены
     */
    private GeoPoint DEFAULT_LOCATION = new GeoPoint(59.9342802, 30.3350986);

    // Static
    // По смыслу, тут не static, а constant или final
    private volatile List<ChainView> markers;
    // user должен быть final, т.к. вы присваиваете его только в конструкторе
    // volatile здесь не нужен совсем
    private volatile UserIdentity user;

    // Dynamic
    private volatile int currentChainId;
    private volatile GeoPoint location;
    private volatile ChainData currentChain;
    private volatile UserData userData;

    /*
     * read markers -> read location
     * У вас в коде есть только такая связи между чтением переменных
     *   (за пределами класса LocalState), поэтому можно было не лепить volatile вообще
     *   везде, а грамотно расставить только в нужных местах
     */

    // Service
    private GeoPoint internalLocation;

    // Rx
    private Observable<Long> updateObservable;
    private Observable<Long> staticObservable;

    /** Construct state initializing just instant data and fill other with defaults and also starts update cycle */
    @SuppressLint("MissingPermission")
    public LocalState(Context context, UserIdentity user) {

        this.user = user;

        LocationManager manager = getSystemService(context, LocationManager.class);
        Location lastLocation = Objects.requireNonNull(manager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation == null) {
            internalLocation = DEFAULT_LOCATION;
        }
        else {
            internalLocation = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
        }

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

        noStateFill();
        initUpdateCycle();
    }

    private void initStatic() throws IOException, NetworkErrorException {
        // если при таком коде убрать volatile с markers, то ничего не изменится
        // с точки зрения потокобезопасности этот код выглядит ошибочным, т.к.
        // вы публикуете непотокобезопасный список перед тем, как начинаете его заполнять

        // я правильно понимаю, что если на сервере поменяются маркеры, то пользователь этого
        // не увидит до тех пор, пока не перезапустит приложение?
        markers = new ArrayList<>();
        Response<List<JChain>> response = RetrofitService.getInstance().getServerApi().getChains(user.getToken()).execute();
        if (!response.isSuccessful() || response.body() == null)
            throw new NetworkErrorException("Bad response");

        for (JChain chain : response.body()) {
            markers.add(new ChainView(chain));
        }

        userData = fetchUserData();
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
        currentChain = NO_CHAIN;
        userData = NO_USER;
    }

    private void update() {

        location = internalLocation;

        // все также захардкоженные константы, что очень плохо
        double minDistance = 100_000;
        ChainView nearestChain = null;
        // что, если markers ещё не проинициализировался?
        for (ChainView view : markers) {
            double distance = view.getPosition().distanceToAsDouble(location);
            if (distance < minDistance) {
                minDistance = distance;
                nearestChain = view;
            }
        }
        if (minDistance < 2_000) {
            currentChainId = nearestChain.getChainId();
        }
        else {
            currentChainId = NO_CHAIN.getView().getChainId();
        }

        try {
            currentChain = getChainById(currentChainId);
        } catch (IOException ignore) {
            currentChain = NO_CHAIN;
        }

        try {
            userData = fetchUserData();
        } catch (IOException ignore) {
            userData = NO_USER;
        }

    }

    public List<ChainView> getMarkers() {
        return markers;
    }

    // ненужный геттер
    public UserIdentity getUser() {
        return user;
    }

    // ненужный геттер
    public int getCurrentChainId() {
        return currentChainId;
    }

    private ChainData getChainById(int chainId) throws IOException {
        if (chainId == -1) {
            return NO_CHAIN;
        }

        UserIdentity user = getUser();
        Response<JChain> chainResponse = RetrofitService.getInstance().getServerApi().getChainById(chainId, user.getToken()).execute();
        Response<List<JContrib>> contribResponse = RetrofitService.getInstance().getServerApi().getContribsByChain(chainId, user.getToken())
                .execute();

        if (!chainResponse.isSuccessful() || !contribResponse.isSuccessful() || chainResponse.body() == null || contribResponse.body() == null) {
            return NO_CHAIN;
        }

        ChainView view = new ChainView(chainResponse.body());

        List<JContrib> contribs = contribResponse.body();
        contribs.sort((o1, o2) -> new BigInteger(o2.getValue()).compareTo(new BigInteger(o1.getValue())));

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

    public @NonNull ChainData getCurrentChain() {
        return currentChain;
    }

    /**
     * Makes an attempt to contribute to the current chain
     * @param contribCallBack the message callback accepts message which is a result of an attempt to contribute
     */
    public void makeContrib(final Consumer<String> contribCallBack) {
        RetrofitService.getInstance().getServerApi().makeContrib(new MakeContribBody(currentChain.getView().getChainId(), user.getToken()))
                .enqueue(new Callback<ActionReportResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ActionReportResponse> call, @NonNull Response<ActionReportResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            onFailure(call, new RuntimeException("Bad response"));
                            return;
                        }

                        contribCallBack.accept(response.body().getMessage());
                    }

                    @Override
                    public void onFailure(@NonNull Call<ActionReportResponse> call, @NonNull Throwable t) {
                        contribCallBack.accept(t.getMessage());
                    }
                });
    }

    public UserData getUserData() {
        return userData;
    }

    private void initUpdateCycle() {
        staticObservable = Observable.fromCallable(() -> {
            initStatic();
            return 0L;
        }).subscribeOn(Schedulers.io()).cache();

        updateObservable = Observable.interval(0, 3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(tick -> {
                    update();
                    return tick;
                }).share();
    }

    /** Returns observable related to update event */
    public Observable<Long> getUpdates() {
        return updateObservable;
    }

    /** Returns observable related to static initialization */
    public Observable<Long> getStatics() {
        return staticObservable;
    }
}
