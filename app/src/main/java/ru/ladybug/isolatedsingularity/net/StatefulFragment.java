package ru.ladybug.isolatedsingularity.net;

import android.support.v4.app.Fragment;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import ru.ladybug.isolatedsingularity.LocalState;

public abstract class StatefulFragment extends Fragment {
    public abstract void initStatic();
    public abstract void updateDynamic();

    public abstract void onUpdateError(Throwable throwable);

    private Observable<Long> stateUpdate = null;
    private Observable<Long> stateStatic = null;

    private Disposable stateUpdateSubscription = null;
    private Disposable stateStaticSubscription = null;

    private boolean resumed = false;
    private boolean started = false;

    public void subscribe(LocalState state) {
        stateStatic = state.getStatics();
        stateUpdate = state.getUpdates();
        if (started)
            onStartSubscribe();
        if (resumed)
            onResumeSubscribe();
    }

    private void onStartSubscribe() {
        if (stateStatic != null && stateStaticSubscription == null) {
            stateStaticSubscription = stateStatic.observeOn(AndroidSchedulers.mainThread()).subscribe(tick -> initStatic(), Functions.ERROR_CONSUMER);
        }
    }

    private void onResumeSubscribe() {
        if (stateUpdate != null && stateUpdateSubscription == null) {
            stateStaticSubscription = stateUpdate.observeOn(AndroidSchedulers.mainThread()).subscribe(tick -> updateDynamic(), this::onUpdateError);
        }
    }

    private void onPauseUnsubscribe() {
        if (stateUpdateSubscription != null && !stateUpdateSubscription.isDisposed()) {
            stateUpdateSubscription.dispose();
            stateUpdateSubscription = null;
        }
    }

    private void onStopUnsubscribe() {
        if (stateStaticSubscription != null && !stateStaticSubscription.isDisposed()) {
            stateStaticSubscription.dispose();
            stateStaticSubscription = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        started = true;
        onStartSubscribe();
    }

    @Override
    public void onStop() {
        onStopUnsubscribe();
        started = false;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Stateful fragment", "onResume: " + this.getClass().getName());
        resumed = true;
        onResumeSubscribe();
    }

    @Override
    public void onPause() {
        onPauseUnsubscribe();
        resumed = false;
        Log.d("Stateful fragment", "onPause: " + this.getClass().getName());
        super.onPause();
    }

    public void unsubscribe() {
        stateUpdate = null;
        stateStatic = null;
        onPauseUnsubscribe();
        onStopUnsubscribe();
    }
}
