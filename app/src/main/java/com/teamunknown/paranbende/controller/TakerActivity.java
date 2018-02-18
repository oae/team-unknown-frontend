package com.teamunknown.paranbende.controller;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.teamunknown.paranbende.constants.CommonConstants;
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

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TakerActivity extends BaseMapActivity implements View.OnClickListener {
    private static final String TAG = TakerActivity.class.getSimpleName();

    EditText moneyAmountEditText;
    Button searchButton;
    private ProgressDialog progressDialog;

    WebSocket mWebSocket;

    private RestInterfaceController serviceAPI;

    private WithdrawalTakerModel mWithdrawalTakerModel;
    private WithdrawalModel mWithdrawalModel;
    private WithdrawalDataModel mWithDrawalDataModel;
    private JSONObject withdrawalObj;

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
        Log.i(TAG, "Map is ready.");
        getLocationPermission();
        getDeviceLocation();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchButton:
                createWithdrawal(moneyAmountEditText.getText().toString().equals("") ? 0 : Integer.parseInt(moneyAmountEditText.getText().toString()));
                Helper.closeVirtualKeyboard(this);
                break;
        }
    }

    private JSONObject createWithdrawalRequestBody(int moneyAmount) {
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("amount", moneyAmount);

            JSONArray locationArray = new JSONArray();
            locationArray.put(mLastKnownLocation.getLatitude());
            locationArray.put(mLastKnownLocation.getLongitude());

            json.put("location", locationArray);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        return json;
    }

    private void createWithdrawal(int moneyAmount) {
        serviceAPI = RequestHelper.createServiceAPI();
        progressDialog = DialogHelper.show(this);

        requestBody = createWithdrawalRequestBody(moneyAmount);

        retrofit2.Call<WithdrawalModel> call = serviceAPI.createWithdrawal("Bearer " + PreferencesPB.getValue(CommonConstants.GeneralValues.LOGIN_ACCESS_TOKEN),
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
                                Log.i(TAG, "WithdrawalModel getError()");
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<WithdrawalModel> call, Throwable t) {
                Log.e(TAG, t.getMessage());

            }
        });

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            try {
                progressDialog.cancel();

                withdrawalObj = new JSONObject(intent.getExtras().getString(CommonConstants.WITHDRAWAL));
                if ("cancelled".equals(withdrawalObj.getString("status"))) {
                    Helper.createSnackbar(TakerActivity.this, "Withdrawal request cancelled by maker.");
                } else if ("matched".equals(withdrawalObj.getString("status"))) {
                    Helper.createSnackbar(TakerActivity.this, "Maker is bringing your money.");

                    connectWebSocket();

                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }


        }
    };

    private void connectWebSocket() {
        AsyncHttpClient.getDefaultInstance().websocket("ws://lab.nepjua.org:23000", null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                mWebSocket = webSocket;
                isMatched = true;
                try {
                    webSocket.send(subscriptionMessage());
                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        public void onStringAvailable(String s) {
                            try {
                                parseSocketMessage(s);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    private String subscriptionMessage() throws JSONException {
        if (mLastKnownLocation == null) {
            return subscriptionMessageError();
        }
        JSONObject mainRequestObject = new JSONObject();

        mainRequestObject.put("type", "subscribe");
        mainRequestObject.put("channel", "live-location");

        JSONObject payloadObject = new JSONObject();
        payloadObject.put("id", withdrawalObj.getString("_id"));

        mainRequestObject.put("payload", payloadObject);

        return mainRequestObject.toString();
    }

    private String subscriptionMessageError() throws JSONException {
        JSONObject mainRequestObject = new JSONObject();

        mainRequestObject.put("type", "error");

        JSONObject payloadObject = new JSONObject();
        payloadObject.put("name", "LocationUpdateFail");

        mainRequestObject.put("payload", payloadObject);

        return mainRequestObject.toString();
    }

    private boolean parseSocketMessage(String s) throws JSONException {
        if ("".equals(s)) {
            return false;
        }

        JSONObject mainObject = new JSONObject(s);

        String actionType = mainObject.getString("type");
        JSONObject payloadObject = mainObject.getJSONObject("payload");

        if (CommonConstants.ACTION_LOCATION_UPDATE.equals(actionType)) {
            final JSONArray location = payloadObject.getJSONArray("loc");


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (null != TakerActivity.this.makerMarker) {
                        TakerActivity.this.makerMarker.remove();
                    }

                    try {
                        TakerActivity.this.addMakerMarker(location.getDouble(0), location.getDouble(1));

                        updateCameraView();
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }

        return true;
    }

    private void updateCameraView()
    {
        ArrayList<Marker> markers = new ArrayList<>();
        markers.add(currentMarker);
        markers.add(makerMarker);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300);
        mMap.moveCamera(cu);
    }


    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(CommonConstants.TAKER_WITHDRAW_MATCH_EVENT));
    }

}
