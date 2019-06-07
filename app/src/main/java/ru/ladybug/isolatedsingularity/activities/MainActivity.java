package ru.ladybug.isolatedsingularity.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import io.reactivex.Observable;
import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.net.StatefulActivity;
import ru.ladybug.isolatedsingularity.UserIdentity;
import ru.ladybug.isolatedsingularity.adapters.ViewPagerAdapter;
import ru.ladybug.isolatedsingularity.fragments.ChainFragment;
import ru.ladybug.isolatedsingularity.fragments.MapFragment;
import ru.ladybug.isolatedsingularity.fragments.UserFragment;

public class MainActivity extends AppCompatActivity implements StatefulActivity {

    private TabLayout tabsMenu;
    private ViewPager tabsViewPager;

    private MapFragment cityMapFragment;
    private ChainFragment chainFragment;
    private UserFragment userFragment;

    private LocalState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInterface();

        UserIdentity currentUser = UserIdentity.fromData(getIntent().getIntExtra("user_id", -1), getIntent().getStringExtra("token"));
        state = new LocalState(getApplicationContext(), currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();

        cityMapFragment.subscribe(state);
        chainFragment.subscribe(state);
        userFragment.subscribe(state);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initInterface() {
//        try {
            tabsMenu = findViewById(R.id.topMenu);
            tabsViewPager = findViewById(R.id.mainContentPager);
            tabsViewPager.setOffscreenPageLimit(3);
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

            cityMapFragment = new MapFragment();
            adapter.addFragment(cityMapFragment, "Map");

            chainFragment = new ChainFragment();
            adapter.addFragment(chainFragment, "Chain View");

            userFragment = new UserFragment();
            adapter.addFragment(userFragment, "User");

            tabsViewPager.setAdapter(adapter);
            tabsMenu.setupWithViewPager(tabsViewPager, true);
//        }
//        catch (Exception e) {
//            // TODO Retry
//        }
    }

//    private void startUpdateCycle() {
//        Timer timer = new Timer(false);
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                update();
//            }
//        }, 0, 3000);
//    }
//
//    private void update() {
//        state.update();
//    }

    @Override
    public LocalState getState() {
        return state;
    }
}
