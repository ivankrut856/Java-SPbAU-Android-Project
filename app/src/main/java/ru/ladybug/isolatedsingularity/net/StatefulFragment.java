package ru.ladybug.isolatedsingularity.net;

import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;

public abstract class StatefulFragment extends Fragment {
    public abstract void initStatic();
    public abstract void updateDynamic();

    @CallSuper
    public void onUpdateError(Throwable throwable) {
        Toast.makeText(getContext(), "Recent server update failed", Toast.LENGTH_SHORT).show();
    }

    private Observable<Long> stateUpdate = null;
    private Observable<Long> stateStatic = null;

    private Disposable stateUpdateSubscription = null;
    public Disposable stateStaticSubscription = null;

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
            stateStaticSubscription = stateStatic.observeOn(AndroidSchedulers.mainThread()).subscribe(tick -> initStatic(), throwable -> {
                Toast.makeText(getContext(), getString(R.string.server_error_text), Toast.LENGTH_LONG).show();
                Objects.requireNonNull(getActivity()).finish();
            });
        }
    }

    private void onResumeSubscribe() {
        if (stateUpdate != null && stateUpdateSubscription == null) {
            stateUpdateSubscription = stateUpdate.observeOn(AndroidSchedulers.mainThread()).subscribe(tick -> updateDynamic(), this::onUpdateError);
        }
    }

    private void onPauseUnsubscribe() {
        if (stateUpdateSubscription != null) {
            stateUpdateSubscription.dispose();
            stateUpdateSubscription = null;
        }
    }

    private void onStopUnsubscribe() {
        if (stateStaticSubscription != null) {
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
        resumed = true;
        onResumeSubscribe();
    }

    @Override
    public void onPause() {
        onPauseUnsubscribe();
        resumed = false;
        super.onPause();
    }

    public void unsubscribe() {
        stateUpdate = null;
        stateStatic = null;
        onPauseUnsubscribe();
        onStopUnsubscribe();
    }
}
