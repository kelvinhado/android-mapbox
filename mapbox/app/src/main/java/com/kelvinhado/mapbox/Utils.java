package com.kelvinhado.mapbox;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by kelvin on 29/07/2017.
 */

public class Utils {

    public static LatLng toMapBoxLatLng(com.google.android.gms.maps.model.LatLng latlng) {
        return new LatLng(latlng.latitude, latlng.longitude);
    }

    public static LatLng toMapBoxLatLng(double latitude, double longitude) {
        return new LatLng(latitude, longitude);
    }

    public static com.google.android.gms.maps.model.LatLng toGoogleLatLng(LatLng latlng) {
        return new com.google.android.gms.maps.model.LatLng(latlng.getLatitude(), latlng.getLongitude());
    }
}
