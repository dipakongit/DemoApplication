package com.example.demoapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient mFusedLocationClient;
    TextView textLocation,textAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textLocation=findViewById(R.id.textLocation);
        textAddress=findViewById(R.id.textAddress);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return false;

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if(!checkPermissions())
        {
            return;
        }
        if(!isLocationEnabled())
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            textLocation.setText("Latitude : "+location.getLatitude()+" Longitude : "+location.getLongitude());
                            try {
                                getAddress(location.getLatitude(),location.getLongitude());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            try {
                getAddress(location.getLatitude(),location.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            textLocation.setText("Latitude : "+location.getLatitude()+" Longitude : "+location.getLongitude());
        }
    };
    @Override
    public void onResume(){
        super.onResume();
        getLastLocation();
    }

    private void getAddress(double lat,double lon) throws IOException {
        Geocoder geocoder=new Geocoder(this, Locale.getDefault());
        List<Address> addresses=geocoder.getFromLocation(lat,lon,1);
        Address address=addresses.get(0);
        textAddress.setText(address.getAddressLine(0));
    }


}
