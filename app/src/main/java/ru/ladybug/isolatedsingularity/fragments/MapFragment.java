package ru.ladybug.isolatedsingularity.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import ru.ladybug.isolatedsingularity.ChainView;
import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.MainActivity;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.RetrofitService;
import ru.ladybug.isolatedsingularity.ServerApi;
import ru.ladybug.isolatedsingularity.Stateful;
import ru.ladybug.isolatedsingularity.StatefulActivity;
import ru.ladybug.isolatedsingularity.UserIdentity;
import ru.ladybug.isolatedsingularity.retrofitmodels.JChain;

public class MapFragment extends Fragment implements Stateful {
    private List<ChainView> mapChains;

    private View view;
    private Context context;
    private Button locationButton;
    private MapView cityMap;
    private IMapController mapController;

    private LocalState state;
//    private boolean fullyCreated = false;

//    public MapView getMap() {
//        return cityMap;
//    }

    public MapFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = (FrameLayout) inflater.inflate(R.layout.map_fragment, container, false);

        context = getContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        cityMap = view.findViewById(R.id.cityMap);
        cityMap.setTileSource(TileSourceFactory.MAPNIK);

        locationButton = view.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state.getCurrentChainId() != -1) {
                    cityMap.getController().animateTo(state.getCurrentChain().getView().getPosition());
                }
            }
        });

        state = (LocalState) ((StatefulActivity) getActivity()).getState();
        state.addListener(this);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("osm", "destroyed");
    }

    @Override
    public void onResume() {
        super.onResume();
        cityMap.onResume();
        Log.d("osm", "resumed");
    }

    @Override
    public void onPause() {
        super.onPause();
        cityMap.onPause();
        Log.d("osm", "paused");
    }

    @Override
    public void initStatic() {
        Log.d("map", "init static");
        cityMap.post(new Runnable() {
            @Override
            public void run() {
                Log.d("map", "post map");
                mapChains = state.getMarkers();
                mapController = cityMap.getController();
                mapController.setZoom(17f);
                GeoPoint startPoint = state.getLocation();
                mapController.animateTo(startPoint);

                List<OverlayItem> markers = new ArrayList<>();
                for (ChainView chain : mapChains) {
                    markers.add(new OverlayItem(chain.getTitle(), chain.getDescription(), chain.getPosition()));
                }

                ItemizedOverlayWithFocus<OverlayItem> markersOverlay = new ItemizedOverlayWithFocus<>(markers,
                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                return true;
                            }

                            @Override
                            public boolean onItemLongPress(final int index, final OverlayItem item) {
                                return false;
                            }
                        }, context);
                markersOverlay.setFocusItemsOnTap(true);
                cityMap.getOverlays().add(markersOverlay);
            }
        });
    }

    public void selectChain(int chainId) {
        Log.d("no chain", "selectChain: post sent");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("no chain", "run: The action");
                locationButton.setText(state.getCurrentChain().getView().getTitle());
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("map", "onDetach: ");
    }

    @Override
    public void updateDynamic() {
        selectChain(state.getCurrentChainId());
        // TODO Dynamic
    }
}
