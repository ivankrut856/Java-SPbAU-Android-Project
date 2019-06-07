package ru.ladybug.isolatedsingularity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ru.ladybug.isolatedsingularity.ChainView;
import ru.ladybug.isolatedsingularity.LocalState;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.net.StatefulActivity;
import ru.ladybug.isolatedsingularity.net.StatefulFragment;

public class MapFragment extends StatefulFragment {

    private Context context;
    private Button locationButton;

    private MapView cityMap;
    private int myPositionOverlayIndex = -1;

    private LocalState state;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.map_fragment, container, false);

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
        Button positionButton = view.findViewById(R.id.position);
        positionButton.setOnClickListener(v -> {
            cityMap.getController().animateTo(state.getLocation());
        });

        state = ((StatefulActivity) Objects.requireNonNull(getActivity())).getState();

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
        IMapController mapController = cityMap.getController();
        mapController.setZoom(17f);

        List<ChainView> mapChains = state.getMarkers();
        GeoPoint startPoint = state.getLocation();
        mapController.animateTo(startPoint);

        List<OverlayItem> markers = new ArrayList<>();
        for (ChainView chain : mapChains) {
            markers.add(new OverlayItem(chain.getTitle(), chain.getDescription(), chain.getPosition()));
        }

        cityMap.getOverlays().clear();

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
                },
                context);
        markersOverlay.setFocusItemsOnTap(true);
        cityMap.getOverlays().add(markersOverlay);
        cityMap.getOverlays().add(getPositionOverlay());
        myPositionOverlayIndex = cityMap.getOverlays().size() - 1;
    }

    private void selectChain() {
        if (state.getCurrentChainId() == -1) {
            locationButton.setText(getString(R.string.no_chain_text));
        }
        else {
            locationButton.setText(state.getCurrentChain().getView().getTitle());
        }

        if (myPositionOverlayIndex != -1) {
            cityMap.getOverlayManager().set(myPositionOverlayIndex, getPositionOverlay());
        }
    }

    private Overlay getPositionOverlay() {
        OverlayItem myPositionOverlayItem = new OverlayItem("You", "Smart player", state.getLocation());
        return new ItemizedIconOverlay<>(new ArrayList<>(Collections.singleton(myPositionOverlayItem)),
                Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.person)),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                },
                context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void updateDynamic() {
        selectChain();
    }
}
