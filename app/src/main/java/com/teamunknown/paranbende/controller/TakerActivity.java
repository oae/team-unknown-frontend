package com.teamunknown.paranbende.controller;


import android.graphics.Color;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.teamunknown.paranbende.BaseMapActivity;
import com.teamunknown.paranbende.R;

public class TakerActivity extends BaseMapActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_taker);

        setMapFragment();

        setMyLocationButton();

    }

    @Override
    protected void updateObjectsOnMap(double latitude,double longitude,int zoomLevel)
    {
        CircleOptions circle = new CircleOptions().center(new LatLng(latitude,longitude))
                .strokeColor(Color.RED)
                .radius(500); // In meters

        mMap.addCircle(circle);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Prompt the user for permission.
        getLocationPermission();
        getDeviceLocation();

    }

}
