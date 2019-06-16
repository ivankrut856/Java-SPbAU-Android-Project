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

/** Simple slider fragment pager adapter with lifecycle events support and autosubscription on state's events */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    /*
     * Эти поля вообще не используются и могут быть спокойно удалены
     */
    private MapFragment mapFragment;
    private ChainFragment chainFragment;
    private UserFragment userFragment;

    private LocalState state;

    /**
     * Construct autosubscribing slider adapter with given state to listen
     * @param fragmentManager the fragment manager which is to rule over new fragments
     * @param state the state whose events are to be listen
     */
    public ViewPagerAdapter(FragmentManager fragmentManager, LocalState state) {
        super(fragmentManager);
        this.state = state;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc */
    @Override
    public Fragment getItem(int position) {
        /*
         * Кажется, что логичнее создать эти фрагменты по одному разу, после чего переиспользовать
         *
         * Возможно я ошибаюсь, т.к. не очень хорошо знаком с этой частью UI андроида
         */
        switch (position) {
            case 0:
                return new MapFragment();
            case 1:
                return new ChainFragment();
            case 2:
                return new UserFragment();
            default:
                /*
                 * Minor: в сообщение было бы неплохо добавить значение position
                 */
                throw new IndexOutOfBoundsException("Wrong position");
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public int getCount() {
        return 3;
    }
}