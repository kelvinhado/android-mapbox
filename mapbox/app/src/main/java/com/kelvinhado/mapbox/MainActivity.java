package com.kelvinhado.mapbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.kelvinhado.mapbox.model.Address;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, MapFragment.OnNewPositionSelectedListener {
    private MapFragment mMapFragment;
    private PlaceAutocompleteFragment autocompleteFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private AddressResultReceiver mAddressResultReceiver;
    private Location lastSelectedLocation;
    private String mAddressOutput;
    private String[] mPlanetTitles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        initialiseAutocompleteFragment();

        mPlanetTitles = new String[3];
        mPlanetTitles[0] = "hello";
        mPlanetTitles[2] = "hi";
        mPlanetTitles[1] = "zuof";
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }

    public void initialiseAutocompleteFragment() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
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
                mMapFragment.changeMarkerPosition(Utils.toMapBoxLatLng(place.getLatLng()),
                        place.getAddress().toString(), true);
                saveNewAddress(new Address(place.getAddress().toString(),
                        place.getLatLng().latitude, place.getLatLng().longitude));
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
                    R.string.error_no_geocoder_available,
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
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (resultCode == Constants.SUCCESS_RESULT) {
                autocompleteFragment.setText(mAddressOutput);
                saveNewAddress(new Address(mAddressOutput,
                        lastSelectedLocation.getLatitude(), lastSelectedLocation.getLongitude()));
            }
            else {
                autocompleteFragment.setText("");
            }
        }
    }

    public void openDrawer(View view){
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectAddress(position);
        }
    }

    private void selectAddress(int position) {
        mDrawerList.setItemChecked(position, true);
    }


    // used shared preferences because of the small amount of data stored
    public void saveNewAddress(Address address) {
        ArrayList<Address> savedAddresses = getSavedAddresses();
        savedAddresses.remove(0);
        savedAddresses.add(address);

        String data = "";
        for(Address tmpAddress : savedAddresses) {
            data += tmpAddress.getPlaceName() + ":";
            data += tmpAddress.getLatitude() + ":";
            data += tmpAddress.getLongitude() + ";";
        }
        data = data.substring(0, data.length() - 1); //remove last char

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.shared_pref_key), data);
        editor.apply();
    }

    public ArrayList<Address> getSavedAddresses() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String storedAddresses = sharedPref.getString(getString(R.string.shared_pref_key), "");

        ArrayList<Address> addressList = new ArrayList<>();
        String[] addresses = storedAddresses.split(";");
        for(String address : addresses) {
            String[] data = address.split(":");
            if(data.length == 3)
                addressList.add(new Address(data[0],Double.parseDouble(data[1]),
                        Double.parseDouble(data[2])));
        }
        return addressList;
    }

}
