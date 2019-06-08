package ru.ladybug.isolatedsingularity.net;

import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;

/** Class providing smart update subscription agreeing with lifecycle fragment and activity events */
public abstract class StatefulFragment extends Fragment {
    /** The method initialises the data that should be initialized once and for all lifetime */
    public abstract void initStatic();
    /** The method initialises and updates the data that should be updated periodically */
    public abstract void updateDynamic();

    /** The method deals with throwables thrown during an update */
    @CallSuper
    public void onUpdateError(Throwable throwable) {
        Toast.makeText(getContext(), "Recent server update failed", Toast.LENGTH_SHORT).show();
    }

    private Observable<Long> stateUpdate = null;
    private Observable<Long> stateStatic = null;

    private Disposable stateUpdateSubscription = null;
    private Disposable stateStaticSubscription = null;

    private boolean resumed = false;
    private boolean started = false;

    /** Asks the fragment to subscribe on state's events, i.e. static initialisation and updates
     * Exact behavior depends on lifecycle status, i.e. initialisation and updates subscription will be active only in started or resumed status respectively
     * @param state the state which is to be listen for events
     */
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

    /** {@inheritDoc} */
    @Override
    public void onStart() {
        super.onStart();
        started = true;
        onStartSubscribe();
    }

    /** {@inheritDoc} */
    @Override
    public void onStop() {
        onStopUnsubscribe();
        started = false;
        super.onStop();
    }

    /** {@inheritDoc} */
    @Override
    public void onResume() {
        super.onResume();
        resumed = true;
        onResumeSubscribe();
    }

    /** {@inheritDoc} */
    @Override
    public void onPause() {
        onPauseUnsubscribe();
        resumed = false;
        super.onPause();
    }

}
