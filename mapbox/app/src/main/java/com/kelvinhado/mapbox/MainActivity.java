package com.kelvinhado.mapbox;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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
import com.kelvinhado.mapbox.addresses.Constants;
import com.kelvinhado.mapbox.addresses.FetchAddressIntentService;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, MapFragment.OnNewPositionSelectedListener {
    private MapFragment mMapFragment;
    private AddressResultReceiver mAddressResultReceiver;
    private Location lastSelectedLocation;
    private String mAddressOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mAddressResultReceiver = new AddressResultReceiver(new Handler());

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
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(position.latitude);
        loc.setLongitude(position.longitude);
        lastSelectedLocation = loc;
        fetchAddress();
    }

    private void fetchAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(MainActivity.this,
                    R.string.no_geocoder_available,
                    Toast.LENGTH_LONG).show();
            return;
        }
        startIntentService();
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mAddressResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastSelectedLocation);
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(MainActivity.this, mAddressOutput, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "epic fail", Toast.LENGTH_SHORT).show();
            }

        }
    }

}
