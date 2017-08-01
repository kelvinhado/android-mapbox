package com.kelvinhado.mapbox;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1993;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // check location permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonMapBox:
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.MAP_EXTRA, MainActivity.MAP_MAPBOX);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in, R.anim.activty_slide_out);
                break;
            case R.id.buttonMaps:
                Intent intent2 = new Intent(SplashActivity.this, MainActivity.class);
                intent2.putExtra(MainActivity.MAP_EXTRA, MainActivity.MAP_GMAPS);
                startActivity(intent2);
                overridePendingTransition(R.anim.activity_slide_in, R.anim.activty_slide_out);
                break;
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_alert_title)
                        .setMessage(R.string.permission_alert_location)
                        .setPositiveButton(R.string.permission_alert_button_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ActivityCompat.requestPermissions(SplashActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                MY_PERMISSIONS_REQUEST_LOCATION );
                                    }
                                })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
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
                    if (ContextCompat.checkSelfPermission(this,
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
}
