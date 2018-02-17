package com.teamunknown.paranbende;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.teamunknown.paranbende.constants.MapConstants;
import com.teamunknown.paranbende.helpers.MapHelper;

/**
 * Created by msalihkarakasli on 17.02.2018.
 */

public abstract class BaseMapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    protected GoogleMap mMap;
    protected CameraPosition mCameraPosition;
    protected Location mLastKnownLocation;
    private ImageView locationUpdateIV;

    protected static final String KEY_CAMERA_POSITION   = MapConstants.KEY_CAMERA_POSITION;
    protected static final String KEY_LOCATION          = MapConstants.KEY_LOCATION;
    protected static final int DEFAULT_ZOOM             = MapConstants.DEFAULT_ZOOM;
    protected static final String TAG                   = MakerActivity.class.getSimpleName();
    protected static final int ACCESS_FINE_LOCATION     = 1;

    protected final LatLng mDefaultLocation = new LatLng(41.0288058, 29.1154325);

    protected boolean mLocationPermissionGranted;
    protected FusedLocationProviderClient mFusedLocationProviderClient;

    protected SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public void setMapFragment()
    {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Prompt the user for permission.
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

    }

    protected void getDeviceLocation() {

        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            updateLocationOnMap(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude(),DEFAULT_ZOOM);
                            mMap.addMarker(new MarkerOptions()
                                    .title("Test")
                                    .icon(BitmapDescriptorFactory.fromBitmap(MapHelper.getMarkerBitmapFromView(BaseMapActivity.this, R.drawable.ic_men_web)))
                                    .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude())));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            updateLocationOnMap(mDefaultLocation.latitude,mDefaultLocation.longitude,DEFAULT_ZOOM);
                        }
                    }
                });


                // Prompt the user for permission.
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    protected abstract void updateObjectsOnMap(double latitude,double longitude,int zoomLevel);

    private void updateLocationOnMap(double latitude,double longitude,int zoomLevel) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude,longitude), zoomLevel));

        updateObjectsOnMap(latitude, longitude, zoomLevel);
    }

    protected void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    finishAffinity();
                }

            }
        }
        updateLocationUI();
    }

    protected void setMyLocationButton()
    {
        locationUpdateIV = findViewById(R.id.locationUpdateImageView);

        if (locationUpdateIV != null) {
            locationUpdateIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDeviceLocation();
                }
            });
        }

        @SuppressLint("ResourceType") View myLocationButton = mapFragment.getView().findViewById(0x2);
        //myLocationButton.setBackground(getDrawable(R.drawable.));
        if (myLocationButton != null && myLocationButton.getLayoutParams() instanceof RelativeLayout.LayoutParams)
        {
            // location button is inside of RelativeLayout
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();

            // Align it to - parent BOTTOM|LEFT
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
            params.setMargins(margin, margin, margin, margin);

            myLocationButton.setLayoutParams(params);
        }
    }
}
