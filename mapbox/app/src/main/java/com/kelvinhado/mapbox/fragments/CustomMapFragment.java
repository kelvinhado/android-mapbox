package com.kelvinhado.mapbox.fragments;

/**
 * Created by kelvin on 31/07/2017.
 */

public interface CustomMapFragment {
    void changeMarkerPosition(double latitude, double longitude, String title, boolean animateCamera);
}
