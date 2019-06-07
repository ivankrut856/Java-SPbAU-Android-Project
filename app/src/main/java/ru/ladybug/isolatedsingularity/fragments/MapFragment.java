package ru.ladybug.isolatedsingularity.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import ru.ladybug.isolatedsingularity.ChainView;
import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.net.StatefulFragment;
import ru.ladybug.isolatedsingularity.net.StatefulActivity;

public class MapFragment extends StatefulFragment {
    private List<ChainView> mapChains;

    private View view;
    private Context context;
    private Button locationButton;
    private MapView cityMap;
    private IMapController mapController;

    private LocalState state;

    public MapFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.map_fragment, container, false);

        context = getContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        cityMap = view.findViewById(R.id.cityMap);
        cityMap.setTileSource(TileSourceFactory.MAPNIK);

        locationButton = view.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(v -> {
            if (state.getCurrentChainId() != -1) {
                cityMap.getController().animateTo(state.getCurrentChain().getView().getPosition());
            }
        });

        state = ((StatefulActivity) getActivity()).getState();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        cityMap.onResume();
    }

    @Override
    public void onPause() {
        cityMap.onPause();

        super.onPause();
    }

    @Override
    public void initStatic() {
        Log.d("Stateful fragment", "initStatic: happening");
        cityMap.post(() -> {
            mapChains = state.getMarkers();
            Log.d("Stateful fragment", "initStatic: post with " + (mapChains != null ? mapChains.size() : "null"));
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
        });
    }

    public void selectChain() {
        Log.d("Stateful fragment", "selectChain: " + Thread.currentThread().getName());
        Log.d("Stateful fragment", "selectChain: " + state.getCurrentChainId());
        getActivity().runOnUiThread(() -> {
            if (state.getCurrentChainId() != -1) {
                locationButton.setText(state.getCurrentChain().getView().getTitle());
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("Stateful fragment", "onAttach: " + getActivity());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void updateDynamic() {
        selectChain();
        // TODO Dynamic
    }

    @Override
    public void onUpdateError(Throwable throwable) {
        throw new RuntimeException(throwable);
    }
}
