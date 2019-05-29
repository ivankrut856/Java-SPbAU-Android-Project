package ru.ladybug.isolatedsingularity;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.internal.platform.Platform;
import ru.ladybug.isolatedsingularity.fragments.ChainFragment;
import ru.ladybug.isolatedsingularity.fragments.MapFragment;
import ru.ladybug.isolatedsingularity.fragments.UserFragment;

public class MainActivity extends AppCompatActivity implements StatefulActivity {

    private TabLayout tabsMenu;
    private ViewPager tabsViewPager;

    private MapFragment cityMapFragment;
    private ChainFragment chainFragment;

    private LocalState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initInterface();

        state = new LocalState(getApplicationContext());

        state.setUser(UserIdentity.fromData(getIntent().getIntExtra("user_id", -1), getIntent().getStringExtra("token")));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                state.initStatic();
                startUpdateCycle();
            }
        });
    }

    private void initInterface() {
        try {
            tabsMenu = findViewById(R.id.topMenu);
            tabsViewPager = findViewById(R.id.mainContentPager);
            tabsViewPager.setOffscreenPageLimit(3);
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

            cityMapFragment = new MapFragment();
            adapter.addFragment(cityMapFragment, "Map");

            chainFragment = new ChainFragment();
            adapter.addFragment(chainFragment, "Chain View");

            adapter.addFragment(new UserFragment(), "User");

            tabsViewPager.setAdapter(adapter);
            tabsMenu.setupWithViewPager(tabsViewPager, true);
        }
        catch (Exception e) {
            // TODO Retry
        }
    }

    private void startUpdateCycle() {
        Timer timer = new Timer(false);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, 3000);
    }

    private void update() {
        state.update();
    }

    @Override
    public LocalState getState() {
        return state;
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragments;
        List<String> titles;
        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragments = new ArrayList<>();
            titles = new ArrayList<>();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
