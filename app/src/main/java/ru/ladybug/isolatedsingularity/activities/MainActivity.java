package ru.ladybug.isolatedsingularity.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

    private ViewPagerAdapter adapter;

    private LocalState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserIdentity currentUser = UserIdentity.fromData(getIntent().getIntExtra("user_id", -1), getIntent().getStringExtra("token"));
        state = new LocalState(getApplicationContext(), currentUser);
        FragmentManager manager = getSupportFragmentManager();
        adapter = new ViewPagerAdapter(manager, state);

        initInterface();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Stateful fragment", "onResumeActivity: ");
    }

    @Override
    protected void onPause() {
        Log.d("Stateful fragment", "onPauseActivity: ");
        super.onPause();
    }

    private void initInterface() {
        tabsMenu = findViewById(R.id.topMenu);
        tabsViewPager = findViewById(R.id.mainContentPager);
        tabsViewPager.setOffscreenPageLimit(3);

        tabsViewPager.setAdapter(adapter);
        tabsMenu.setupWithViewPager(tabsViewPager, true);
    }

    @Override
    public LocalState getState() {
        return state;
    }
}
