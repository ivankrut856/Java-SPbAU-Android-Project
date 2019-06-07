package ru.ladybug.isolatedsingularity.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.net.StatefulActivity;
import ru.ladybug.isolatedsingularity.net.StatefulFragment;

public class UserFragment extends StatefulFragment {
    private LocalState state;

    private TextView welcomeText;
    private TextView moneyText;

    public UserFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.user_fragment, container, false);

        welcomeText = view.findViewById(R.id.welcomeText);
        moneyText = view.findViewById(R.id.moneyText);

        state = ((StatefulActivity) Objects.requireNonNull(getActivity())).getState();

        return view;
    }

    @Override
    public void initStatic() {
        welcomeText.setText(String.format("The Great Conqueror: %s", state.getUserData().getName()));
    }

    @Override
    public void updateDynamic() {
        moneyText.setText(String.format("Your money: %s", state.getUserData().getMoney()));
    }
}
