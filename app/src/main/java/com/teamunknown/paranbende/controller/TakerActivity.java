package com.teamunknown.paranbende.controller;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.teamunknown.paranbende.BaseMapActivity;
import com.teamunknown.paranbende.GeneralValues;
import com.teamunknown.paranbende.MakerActivity;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.model.UserLoginModel;
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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class TakerActivity extends BaseMapActivity implements View.OnClickListener {
    EditText moneyAmountEditText;
    Button searchButton;
    private ProgressDialog progressDialog;

    private RestInterfaceController serviceAPI;

    private WithdrawalTakerModel mWithdrawalTakerModel;
    private WithdrawalModel mWithdrawalModel;
    private WithdrawalDataModel mWithDrawalDataModel;

    private JSONObject requestBody;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_taker);

        initUI();

        setMapFragment();

        setMyLocationButton();

    }

    private void initUI() {
        moneyAmountEditText = findViewById(R.id.moneyAmountEditText);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
    }

    @Override
    protected void updateObjectsOnMap(double latitude, double longitude, int zoomLevel) {
        CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude))
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchButton:
                createWithDrawal(Integer.parseInt(moneyAmountEditText.getText().toString()));
                break;
        }


    }

    private void createWithDrawal(int moneyAmount) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GeneralValues.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serviceAPI = retrofit.create(RestInterfaceController.class);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.searching));
        progressDialog.setCancelable(false);
        progressDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.dismiss();
            }
        });
        progressDialog.show();



        try {
            JSONObject json = new JSONObject();
            json.put("amount", moneyAmount);
            requestBody = json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception");
        }

        retrofit2.Call<WithdrawalModel> call = serviceAPI.createWithdrawal("Bearer " + PreferencesPB.getValue(GeneralValues.LOGIN_ACCESS_TOKEN),
                requestBody.toString());

        call.enqueue(new Callback<WithdrawalModel>() {
            @Override
            public void onResponse(Call<WithdrawalModel> call, Response<WithdrawalModel> response) {
                try {
                    int code = response.code();
                    mWithdrawalModel = new WithdrawalModel();
                    mWithDrawalDataModel = new WithdrawalDataModel();
                    mWithdrawalTakerModel = new WithdrawalTakerModel();

                    if (code == 200) {
                        if (!(response.body() == null)) {
                            mWithdrawalModel.setError(response.body().getError());

                            if (!mWithdrawalModel.getError()) {


                            } else {
                                Helper.createSnackbar(TakerActivity.this, response.body().getMessage());
                                progressDialog.cancel();
                            }

                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Response body is null");
                }
            }

            @Override
            public void onFailure(Call<WithdrawalModel> call, Throwable t) {

            }
        });

    }
}
