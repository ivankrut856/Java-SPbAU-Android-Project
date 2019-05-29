package ru.ladybug.isolatedsingularity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.Stateful;
import ru.ladybug.isolatedsingularity.StatefulActivity;

public class UserFragment extends Fragment implements Stateful {
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
        state = (LocalState) ((StatefulActivity) getActivity()).getState();
        state.addListener(this);

        return view;
    }

    @Override
    public void initStatic() {
        welcomeText.setText(String.format("The Great Conqueror: %s", state.getUserData().getName()));
        moneyText.setText(String.format("Your money: %s", state.getUserData().getMoney()));
    }

    @Override
    public void updateDynamic() {
        // TODO
    }
}
