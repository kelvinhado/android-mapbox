package com.kelvinhado.mapbox;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by kelvin on 29/07/2017.
 */

public class Utils {

    public static LatLng fromGoogleLatLng(com.google.android.gms.maps.model.LatLng latlng) {
        return new LatLng(latlng.latitude, latlng.longitude);
    }

    public static com.google.android.gms.maps.model.LatLng fromMapBoxLatLng(LatLng latlng) {
        return new com.google.android.gms.maps.model.LatLng(latlng.getLatitude(), latlng.getLongitude());
    }
}