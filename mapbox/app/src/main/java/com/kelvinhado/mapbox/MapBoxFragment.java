package com.kelvinhado.mapbox;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapBoxFragment extends Fragment implements CustomMapFragment {

    private static final String MAPBOX_API_KEY = BuildConfig.MAPBOX_API_KEY;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private boolean isBeingAnimated;
    private boolean firstPositionPlaced;
    private LatLng userPosition;
    private LatLng selectedPosition;
    private Marker marker;
    OnNewPositionSelectedListener mCallback;

    public MapBoxFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Mapbox.getInstance(getActivity(), MAPBOX_API_KEY);
        return inflater.inflate(R.layout.fragment_map_mapbox, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                MapBoxFragment.this.mapboxMap = mapboxMap;

                mapboxMap.setOnMyLocationChangeListener(new MapboxMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(@Nullable Location location) {
                        if(mapboxMap.getMyLocation() != null && !firstPositionPlaced) {
                            firstPositionPlaced = true;
                            userPosition = new LatLng(mapboxMap.getMyLocation().getLatitude(),
                                    mapboxMap.getMyLocation().getLongitude());
                            changeMarkerPosition(userPosition, getString(R.string.map_location_me), true);
                        }

                    }
                });
            }
        });

        mapView.addOnMapChangedListener(new MapView.OnMapChangedListener() {
            @Override
            public void onMapChanged(int change) {
                if(change == 3 && !isBeingAnimated) { // #REGION_DID_CHANGE
                    float centerX = mapView.getX() + mapView.getWidth()  / 2;
                    float centerY = mapView.getY() + mapView.getHeight()  / 2;
                    selectedPosition = mapboxMap.getProjection().fromScreenLocation(new PointF(centerX, centerY));
                    changeMarkerPosition(selectedPosition, "", false);
                }
                else if(change == 13 && !isBeingAnimated) { // #DID_FINISH_RENDERING_MAP_FULLY_RENDERED
                    mCallback.onNewPositionSelected(selectedPosition.getLatitude(),
                            selectedPosition.getLongitude());
                }
            }
        });

        // check location permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
        }
    }

    @Override
    public void changeMarkerPosition(double latitude, double longitude, String title, boolean animateCamera) {
        changeMarkerPosition(new LatLng(latitude, longitude), title, animateCamera);
    }

    public void changeMarkerPosition(LatLng position, String title, boolean animateCamera) {

        if(marker == null) {
            IconFactory iconFactory = IconFactory.getInstance(getActivity());
            Icon icon = iconFactory.fromResource(R.drawable.map_marker);
            marker = mapboxMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(title)
                            .icon(icon));
        } else {
            marker.setPosition(position);
            marker.setTitle(title);
        }

        if(animateCamera) {
            CameraPosition camPosition = new CameraPosition.Builder()
                    .zoom(13)
                    .target(position)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition), 2000);

            //lock address research during animation
            isBeingAnimated = true;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isBeingAnimated = false;
                }
            }, 2500);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1993;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.permission_alert_title)
                        .setMessage(R.string.permission_alert_location)
                        .setPositiveButton(R.string.permission_alert_button_ok,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // permission granted
                    }

                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    // Container Activity must implement this interface
    public interface OnNewPositionSelectedListener {
        void onNewPositionSelected(double latitude, double longitude);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity;

        if (context instanceof Activity){
            activity = (Activity) context;
        } else {
            activity = null;
        }
        try {
            mCallback = (OnNewPositionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNewPositionSelectedListener");
        }
    }
}
