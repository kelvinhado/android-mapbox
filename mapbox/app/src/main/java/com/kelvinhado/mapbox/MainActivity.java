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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, MapBoxFragment.OnNewPositionSelectedListener {

    public static final String MAP_EXTRA = "map_extra";
    public static final int MAP_MAPBOX = 0;
    public static final int MAP_GMAPS = 1;

    private CustomMapFragment mMapFragment;
    private PlaceAutocompleteFragment autocompleteFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private AddressesAdapter mAdapter;

    private AddressResultReceiver mAddressResultReceiver;
    private Location lastSelectedLocation;
    private String mAddressOutput;
    private List<Address> addressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int mapMode = getIntent().getIntExtra(MAP_EXTRA, -1);


        mMapFragment = new MapBoxFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout_map, (MapBoxFragment) mMapFragment);
        ft.commit();



        // init auto-complete fragment
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        initialiseAutocompleteFragment();

        // init drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        addressList = getSavedAddresses();
        mAdapter = new AddressesAdapter(this, addressList);
        mDrawerList.setAdapter(mAdapter);
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
                mMapFragment.changeMarkerPosition(
                        place.getLatLng().latitude,
                        place.getLatLng().longitude,
                        place.getAddress().toString(),
                        true);
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
    public void onNewPositionSelected(double latitude, double longitude) {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
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
            mDrawerList.setItemChecked(position, true);
            Address selectedAddress = (Address) mDrawerList.getItemAtPosition(position);
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            mMapFragment.changeMarkerPosition(
                    selectedAddress.getLatitude(),
                    selectedAddress.getLongitude(),
                    selectedAddress.getPlaceName(),
                    true);
        }
    }

    // used shared preferences because of the small amount of data stored
    public void saveNewAddress(Address address) {
        ArrayList<Address> savedAddresses = getSavedAddresses();
        if(savedAddresses.size() >= 15) {
            savedAddresses.remove(0);
        }
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

        //notify the adapter
        addressList.clear();
        addressList.addAll(savedAddresses);
        mAdapter.notifyDataSetChanged();
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
            Log.d("Address", ">" +  data[0] + "/" + Double.parseDouble(data[1]) + "/" + Double.parseDouble(data[2]));
        }
        return addressList;
    }

}
