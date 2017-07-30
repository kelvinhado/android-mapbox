package com.kelvinhado.mapbox;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, MapFragment.OnNewPositionSelectedListener {
    private MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);

        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //address filter
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        //boundaries FR
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(51.391494, 8.481445));
        builder.include(new LatLng(42.023793, -6.020508));

        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setBoundsBias(builder.build());
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMapFragment.changeMarkerPosition(Utils.toMapBoxLatLng(place.getLatLng()), place.getAddress().toString(), true);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.error_connexion_places, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNewPositionSelected(LatLng position) {
        Toast.makeText(this, "" + position.latitude, Toast.LENGTH_SHORT).show();
    }
}
