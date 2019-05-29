package ru.ladybug.isolatedsingularity.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.ladybug.isolatedsingularity.ChainData;
import ru.ladybug.isolatedsingularity.ContributorsAdapter;
import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.RetrofitService;
import ru.ladybug.isolatedsingularity.Stateful;
import ru.ladybug.isolatedsingularity.StatefulActivity;
import ru.ladybug.isolatedsingularity.UserIdentity;
import ru.ladybug.isolatedsingularity.retrofitmodels.ActionReportResponse;
import ru.ladybug.isolatedsingularity.retrofitmodels.JContrib;
import ru.ladybug.isolatedsingularity.retrofitmodels.MakeContribBody;

public class ChainFragment extends Fragment implements Stateful {
    private View view;

    private RecyclerView contributorsList;
    private ContributorsAdapter contributorsAdapter;
    private TextView chainTitle;
    private TextView myContribution;
    private ProgressBar serverResponseBar;
    private Button smallBoostButton;

    private LocalState state;

    private View.OnClickListener smallBoostOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public ChainFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.chain_fragment, container, false);

        contributorsList = view.findViewById(R.id.contributorsView);
        contributorsList.setLayoutManager(new LinearLayoutManager(getContext()));
        contributorsAdapter = new ContributorsAdapter();
        contributorsList.setAdapter(contributorsAdapter);

        chainTitle = view.findViewById(R.id.chainTitle);
        myContribution = view.findViewById(R.id.myContribution);

        serverResponseBar = view.findViewById(R.id.serverResponseBar);
        serverResponseBar.setVisibility(View.INVISIBLE);

        smallBoostButton = view.findViewById(R.id.smallBoostButton);
        smallBoostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockUI();
                state.makeContrib(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        ContribCallback(s);
                    }
                });
            }
        });


        state = (LocalState) ((StatefulActivity) getActivity()).getState();
        state.addListener(this);

        return view;
    }

    public void ContribCallback(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                unblockUI();
            }
        });
    }


    private void blockUI() {
        smallBoostButton.setClickable(false);
        serverResponseBar.setVisibility(View.VISIBLE);
    }

    private void unblockUI() {
        smallBoostButton.setClickable(true);
        serverResponseBar.setVisibility(View.INVISIBLE);
    }


    @Override
    public void initStatic() {
        // No static
    }

    @Override
    public void updateDynamic() {
        final ChainData currentChain = state.getCurrentChain();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chainTitle.setText(currentChain.getView().getTitle());
                myContribution.setText(String.format(Locale.getDefault(),"My contribution to the chain: %d", currentChain.getMyContribution()));
                contributorsAdapter.setContributors(currentChain.getContributors());
            }
        });

        // TODO Dynamic
    }
}
