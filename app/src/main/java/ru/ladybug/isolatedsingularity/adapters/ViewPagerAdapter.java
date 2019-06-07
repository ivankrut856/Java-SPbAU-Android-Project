package ru.ladybug.isolatedsingularity.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.fragments.ChainFragment;
import ru.ladybug.isolatedsingularity.fragments.MapFragment;
import ru.ladybug.isolatedsingularity.fragments.UserFragment;
import ru.ladybug.isolatedsingularity.net.StatefulFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private MapFragment mapFragment;
    private ChainFragment chainFragment;
    private UserFragment userFragment;

    private LocalState state;

    public ViewPagerAdapter(FragmentManager fragmentManager, LocalState state) {
        super(fragmentManager);
        this.state = state;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Map";
            case 1:
                return "Chain";
            case 2:
                return "User";
            default:
                throw new IndexOutOfBoundsException("Wrong position");
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MapFragment();
            case 1:
                return new ChainFragment();
            case 2:
                return new UserFragment();
            default:
                throw new IndexOutOfBoundsException("Wrong position");
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        StatefulFragment createdFragment = (StatefulFragment) super.instantiateItem(container, position);
        switch (position) {
            case 0:
                mapFragment = (MapFragment) createdFragment;
                break;
            case 1:
                chainFragment = (ChainFragment) createdFragment;
                break;
            case 2:
                userFragment = (UserFragment) createdFragment;
                break;
            default:
                throw new IndexOutOfBoundsException("Wrong position");
        }
        createdFragment.subscribe(state);
        return createdFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}