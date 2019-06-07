package ru.ladybug.isolatedsingularity.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.net.StatefulActivity;
import ru.ladybug.isolatedsingularity.net.StatefulFragment;

public class UserFragment extends StatefulFragment {
    private View view;
    private LocalState state;

    private TextView welcomeText;
    private TextView moneyText;

    public UserFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.user_fragment, container, false);

        welcomeText = view.findViewById(R.id.welcomeText);
        moneyText = view.findViewById(R.id.moneyText);

        state = ((StatefulActivity) getActivity()).getState();

        return view;
    }

    @Override
    public void initStatic() {
        welcomeText.setText(String.format("The Great Conqueror: %s", state.getUserData().getName()));
        moneyText.setText(String.format("Your money: %s", state.getUserData().getMoney()));
    }

    @Override
    public void updateDynamic() {
        getActivity().runOnUiThread(() -> moneyText.setText(String.format("Your money: %s", state.getUserData().getMoney())));
    }

    @Override
    public void onUpdateError(Throwable throwable) {

    }
}
