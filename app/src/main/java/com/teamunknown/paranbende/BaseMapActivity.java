package com.teamunknown.paranbende;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.teamunknown.paranbende.constants.GeneralValues;
import com.teamunknown.paranbende.constants.MapConstants;
import com.teamunknown.paranbende.controller.MakerActivity;
import com.teamunknown.paranbende.controller.TakerActivity;
import com.teamunknown.paranbende.helpers.MapHelper;
import com.teamunknown.paranbende.helpers.RequestHelper;
import com.teamunknown.paranbende.model.User;
import com.teamunknown.paranbende.model.WithdrawalDataModel;
import com.teamunknown.paranbende.model.WithdrawalModel;
import com.teamunknown.paranbende.model.WithdrawalTakerModel;
import com.teamunknown.paranbende.util.Helper;
import com.teamunknown.paranbende.util.PreferencesPB;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by msalihkarakasli on 17.02.2018.
 */

public abstract class BaseMapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    protected GoogleMap mMap;
    protected CameraPosition mCameraPosition;
    protected Location mLastKnownLocation;
    private ImageView locationUpdateIV;
    protected Marker currentMarker;
    protected Marker takerMarker;
    protected Marker makerMarker;
    LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    protected static final String KEY_CAMERA_POSITION = MapConstants.KEY_CAMERA_POSITION;
    protected static final String KEY_LOCATION = MapConstants.KEY_LOCATION;
    protected static final int DEFAULT_ZOOM = MapConstants.DEFAULT_ZOOM;
    protected static final String TAG = MakerActivity.class.getSimpleName();
    protected static final int ACCESS_FINE_LOCATION = 1;

    protected final LatLng mDefaultLocation = new LatLng(41.0288058, 29.1154325);

    protected boolean mLocationPermissionGranted;
    protected FusedLocationProviderClient mFusedLocationProviderClient;

    protected SupportMapFragment mapFragment;

    private ActionBar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                mLastKnownLocation = locationResult.getLastLocation();

                if (null == currentMarker)
                {
                    return;
                }

                currentMarker.remove();

                currentMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(MapHelper.getMarkerBitmapFromView(BaseMapActivity.this, R.drawable.ic_men_web))).position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                updateObjectsOnMap(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), DEFAULT_ZOOM);
            }

            ;
        };


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        toolbar = getSupportActionBar();

        mLocationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                mLastKnownLocation = locationResult.getLastLocation();
                updateLocationOnMap(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), DEFAULT_ZOOM);
            }

            ;
        };

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
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

    private void createLocationUpdateRequest()
    {
        RestInterfaceController serviceAPI = RequestHelper.createServiceAPI();
        JSONObject requestBody = null;

        try
        {
            JSONObject json = new JSONObject();
            json.put("lat", mLastKnownLocation.getLatitude());
            json.put("lng", mLastKnownLocation.getLongitude());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("location", json);

            requestBody = jsonObj;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception");
        }

        retrofit2.Call<User> call = serviceAPI.updateLocation("Bearer " + PreferencesPB.getValue(GeneralValues.LOGIN_ACCESS_TOKEN), requestBody.toString());

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                try {
                    int code = response.code();

                    if (code == 200) {
                        if (!(response.body() == null))
                        {
                            Log.d("paranbende", response.toString());
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Response body is null");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
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
                            if (currentMarker != null)
                            {
                                currentMarker.remove();
                            }
                            currentMarker = mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromBitmap(MapHelper.getMarkerBitmapFromView(BaseMapActivity.this, R.drawable.ic_men_web)))
                                    .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude())));

                            createLocationUpdateRequest();

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

    protected void updateLocationOnMap(double latitude,double longitude,int zoomLevel)
    {
        if (null != currentMarker)
        {
            currentMarker.remove();
        }

        currentMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(MapHelper.getMarkerBitmapFromView(BaseMapActivity.this, R.drawable.ic_men_web))).position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), DEFAULT_ZOOM));
        updateObjectsOnMap(latitude, longitude, DEFAULT_ZOOM);
    }

    protected abstract void updateObjectsOnMap(double latitude,double longitude,int zoomLevel);

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

    public void changeLocation(Location location)
    {
        //updateLocationOnMap(location.getLatitude(),location.getLongitude(), DEFAULT_ZOOM);

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

    protected void addTakerMarker(double lat, double lng)
    {
        takerMarker = mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(MapHelper.getMarkerBitmapFromView(BaseMapActivity.this, R.drawable.ic_men_web)))
                        .position(new LatLng(lat, lng)));
    }

    protected void addMakerMarker(double lat, double lng)
    {
        makerMarker = mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(MapHelper.getMarkerBitmapFromView(BaseMapActivity.this, R.drawable.ic_man_money)))
                        .position(new LatLng(lat, lng)));
    }
}
