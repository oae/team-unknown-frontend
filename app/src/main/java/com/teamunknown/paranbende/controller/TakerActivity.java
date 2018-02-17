package com.teamunknown.paranbende.controller;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.teamunknown.paranbende.BaseMapActivity;
import com.teamunknown.paranbende.constants.GeneralValues;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.helpers.DialogHelper;
import com.teamunknown.paranbende.helpers.RequestHelper;
import com.teamunknown.paranbende.model.WithdrawalDataModel;
import com.teamunknown.paranbende.model.WithdrawalModel;
import com.teamunknown.paranbende.model.WithdrawalTakerModel;
import com.teamunknown.paranbende.util.Helper;
import com.teamunknown.paranbende.util.PreferencesPB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    protected void updateObjectsOnMap(double latitude, double longitude, int zoomLevel)
    {
        return;
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
                createWithdrawal(moneyAmountEditText.getText().toString().equals("") ? 0 : Integer.parseInt(moneyAmountEditText.getText().toString()));
                break;
        }
    }

    private JSONObject createWithdrawalRequestBody(int moneyAmount)
    {
        JSONObject json = null;
        try
        {
            json = new JSONObject();
            json.put("amount", moneyAmount);

            JSONArray locationArray = new JSONArray();
            locationArray.put(mLastKnownLocation.getLatitude());
            locationArray.put(mLastKnownLocation.getLongitude());

            json.put("location", locationArray);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception");
        }

        return json;
    }

    private void createWithdrawal(int moneyAmount)
    {
        serviceAPI = RequestHelper.createServiceAPI();
        progressDialog = DialogHelper.show(this);

        requestBody = createWithdrawalRequestBody(moneyAmount);

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
