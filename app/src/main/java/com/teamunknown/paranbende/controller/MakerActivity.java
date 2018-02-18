package com.teamunknown.paranbende.controller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.squareup.picasso.Picasso;
import com.teamunknown.paranbende.BaseMapActivity;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.constants.CommonConstants;
import com.teamunknown.paranbende.helpers.RequestHelper;
import com.teamunknown.paranbende.model.Settings.SettingsDataModel;
import com.teamunknown.paranbende.model.Settings.SettingsMaker;
import com.teamunknown.paranbende.model.Settings.SettingsModel;
import com.teamunknown.paranbende.model.Settings.UserSettings.UserSettingsData;
import com.teamunknown.paranbende.model.Settings.UserSettings.UserSettingsMaker;
import com.teamunknown.paranbende.model.Settings.UserSettings.UserSettingsModel;
import com.teamunknown.paranbende.model.ToggleOnlineModel;
import com.teamunknown.paranbende.model.WithdrawalDataModel;
import com.teamunknown.paranbende.model.WithdrawalModel;
import com.teamunknown.paranbende.util.Helper;
import com.teamunknown.paranbende.util.PreferencesPB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MakerActivity extends BaseMapActivity {

    private static final String TAG = MakerActivity.class.getSimpleName();

    // Keys for storing activity state.
    DrawerLayout drawer;
    private Toolbar toolbar;

    WebSocket mWebSocket;

    private SettingsDataModel mSettingsDataModel;
    private SettingsMaker mSettingsMaker;
    private SettingsModel mSettingsModel;
    private ToggleOnlineModel mToggleOnlineModel;
    private UserSettingsModel mUserSettingsModel;
    private UserSettingsMaker mUserSettingsMaker;
    private UserSettingsData mUserSettingsData;
    private WithdrawalModel mWithdrawalModel;
    private WithdrawalDataModel mWithdrawalDataModel;

    private ProgressDialog progressDialog;

    private RestInterfaceController serviceAPI;

    private JSONObject requestBody;

    private Switch sIsOnline;
    private EditText mMinAmountEditText, mMaxAmountEditText, mDistanceEditText;
    private TextView mLogOutText, mUserNameTextView;

    private ImageView avatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maker);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        String whereFrom = "";
        if (getIntent() != null && getIntent().getExtras() != null) {
            whereFrom = getIntent().getExtras().getString(CommonConstants.WHERE_FROM);
        }

        avatar = findViewById(R.id.headerImage);


        setMapFragment();
        setMyLocationButton();

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ImageView iToggleBtn = findViewById(R.id.iMenuToggle);

        iToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        Button bSettingsOk = findViewById(R.id.b_settings_ok);

        bSettingsOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.b_settings_ok) {
                    drawer.closeDrawer(GravityCompat.START);
                    saveSettings();

                }
            }
        });

        sIsOnline = findViewById(R.id.s_is_active);
        sIsOnline.setChecked(true);
        sIsOnline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                TextView tIsOnline = findViewById(R.id.tIsOnline);
                updateToggleOnline(isChecked);
                if (isChecked) {
                    tIsOnline.setTextColor(getColor(R.color.white));
                    tIsOnline.setText(R.string.online);
                } else {
                    tIsOnline.setTextColor(getColor(R.color.black));
                    tIsOnline.setText(R.string.offline);
                }
            }
        });

        mMinAmountEditText = findViewById(R.id.e_min_amount);
        mMaxAmountEditText = findViewById(R.id.e_max_amount);
        mDistanceEditText = findViewById(R.id.e_distance);
        mUserNameTextView = findViewById(R.id.t_username);

        mLogOutText = findViewById(R.id.logOut);

        mLogOutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        if (CommonConstants.FROM_PUSH_NOTIFICATION.equals(whereFrom)) {
            String message = getIntent().getExtras().getString(CommonConstants.MESSAGE);
            String withdrawalId = getIntent().getExtras().getString(CommonConstants.WITHDRAWAL);

            createWithdrawEventDialog(message, withdrawalId);

        }
        getUserSettings();
    }

    private void logOut() {
        PreferencesPB.removeValue(CommonConstants.GeneralValues.LOGIN_USER_TYPE);
        PreferencesPB.removeValue(CommonConstants.GeneralValues.LOGIN_ACCESS_TOKEN);
        PreferencesPB.removeValue(CommonConstants.GeneralValues.LOGIN_USER_ID);
        PreferencesPB.removeValue(CommonConstants.GeneralValues.LOGIN_USER_NAME);

        Intent intent = new Intent(MakerActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void updateToggleOnline(boolean isChecked) {
        serviceAPI = RequestHelper.createServiceAPI();

        try {
            JSONObject json = new JSONObject();
            json.put("online", isChecked);
            requestBody = json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        retrofit2.Call<ToggleOnlineModel> call = serviceAPI.toogleOnline("Bearer " + PreferencesPB.getValue(CommonConstants.GeneralValues.LOGIN_ACCESS_TOKEN), requestBody.toString());
        call.enqueue(new Callback<ToggleOnlineModel>() {
            @Override
            public void onResponse(Call<ToggleOnlineModel> call, Response<ToggleOnlineModel> response) {
                try {
                    int code = response.code();
                    mToggleOnlineModel = new ToggleOnlineModel();

                    if (code == 200) {
                        if (!(response.body() == null)) {
                            mToggleOnlineModel.setError(response.body().getError());

                            if (!mSettingsModel.getError()) {

                                Helper.createSnackbar(MakerActivity.this, getString(R.string.isOnlineChanged));

                            } else {
                                Helper.createSnackbar(MakerActivity.this, response.body().getMessage());
                                Log.i(TAG, "SettingsModel getError()");
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ToggleOnlineModel> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });

    }


    private void saveSettings() {

        serviceAPI = RequestHelper.createServiceAPI();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("minAmount", Integer.parseInt(mMinAmountEditText.getText().toString()));
            json.put("maxAmount", Integer.parseInt(mMaxAmountEditText.getText().toString()));
            json.put("range", Integer.parseInt(mDistanceEditText.getText().toString()));
            requestBody = json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception");
        }

        retrofit2.Call<SettingsModel> call = serviceAPI.saveUserSettings("Bearer " + PreferencesPB.getValue(CommonConstants.GeneralValues.LOGIN_ACCESS_TOKEN), requestBody.toString());
        call.enqueue(new Callback<SettingsModel>() {
            @Override
            public void onResponse(Call<SettingsModel> call, Response<SettingsModel> response) {
                try {
                    int code = response.code();
                    mSettingsModel = new SettingsModel();
                    mSettingsDataModel = new SettingsDataModel();
                    mSettingsMaker = new SettingsMaker();

                    if (code == 200) {
                        if (!(response.body() == null)) {
                            mSettingsModel.setError(response.body().getError());

                            if (!mSettingsModel.getError()) {

                                Helper.createSnackbar(MakerActivity.this, getString(R.string.save_settings));

                            } else {
                                Helper.createSnackbar(MakerActivity.this, response.body().getMessage());
                                Log.i(TAG, "SettingsModel getError()");
                            }
                            progressDialog.cancel();
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    progressDialog.cancel();
                }
            }

            @Override
            public void onFailure(Call<SettingsModel> call, Throwable t) {
                Log.e(TAG, t.getMessage());
                progressDialog.cancel();
            }
        });

    }

    private void getUserSettings() {

        serviceAPI = RequestHelper.createServiceAPI();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        retrofit2.Call<UserSettingsModel> call = serviceAPI.getUserSettings("Bearer " + PreferencesPB.getValue(CommonConstants.GeneralValues.LOGIN_ACCESS_TOKEN));
        call.enqueue(new Callback<UserSettingsModel>() {
            @Override
            public void onResponse(Call<UserSettingsModel> call, Response<UserSettingsModel> response) {
                try {
                    int code = response.code();
                    mUserSettingsModel = new UserSettingsModel();
                    mUserSettingsMaker = new UserSettingsMaker();
                    mUserSettingsData = new UserSettingsData();

                    if (code == 200) {
                        if (!(response.body() == null)) {
                            mUserSettingsModel.setError(response.body().getError());
                            mUserSettingsModel.setData(response.body().getData());
                            mUserSettingsData = mUserSettingsModel.getData();
                            mUserSettingsMaker = mUserSettingsData.getMaker();
                            if (!mUserSettingsModel.getError()) {
                                mMinAmountEditText.setText(String.valueOf(mUserSettingsMaker.getMinAmount()));
                                mMaxAmountEditText.setText(String.valueOf(mUserSettingsMaker.getMaxAmount()));
                                mDistanceEditText.setText(String.valueOf(mUserSettingsMaker.getRange()));
                                Picasso.with(MakerActivity.this).load("http:" + mUserSettingsData.getAvatar()).into(avatar);
                                toolbar.setNavigationIcon(R.drawable.ic_man_money);

                                mUserNameTextView.setText(mUserSettingsModel.getData().getEmail());

                                sIsOnline.setChecked(mUserSettingsMaker.getOnline());
                            } else {
                                Helper.createSnackbar(MakerActivity.this, response.body().getMessage());
                                Log.i(TAG, "userSettingsModel getError()");
                            }
                            progressDialog.cancel();

                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    progressDialog.cancel();
                }
            }

            @Override
            public void onFailure(Call<UserSettingsModel> call, Throwable t) {
                progressDialog.cancel();
                Log.e(TAG, t.getMessage());
            }
        });

    }

    @Override
    protected void updateObjectsOnMap(double latitude, double longitude, int zoomLevel) {
        if (mWebSocket != null) {
            try {
                mWebSocket.send(createUpdateLocationMessage());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        Log.i(TAG, "onBackPressed()");
    }

    private void createWithdrawEventDialog(String message, final String withdrawalId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MakerActivity.this);
        dialogBuilder.setTitle("We need your permission to keep going!");
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(false);

        dialogBuilder.setPositiveButton(
                getResources().getString(R.string.accept),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createConfirmWithdrawalRequest(true, withdrawalId);
                        // Start to web socket connection to sent own location.
                        connectWebSocket();
                    }
                });

        dialogBuilder.setNegativeButton(
                getResources().getString(R.string.reject),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createConfirmWithdrawalRequest(false, withdrawalId);
                        dialog.cancel();
                    }
                });

        AlertDialog dialogBox = dialogBuilder.create();
        dialogBox.show();
    }

    private void createConfirmWithdrawalRequest(Boolean isApproved, String withdrawalId) {
        serviceAPI = RequestHelper.createServiceAPI();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            JSONObject json = new JSONObject();
            json.put("isApproved", isApproved);
            json.put("withdrawalId", withdrawalId);
            requestBody = json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception");
        }

        retrofit2.Call<WithdrawalModel> call = serviceAPI.confirmWithdrawal("Bearer " + PreferencesPB.getValue(CommonConstants.GeneralValues.LOGIN_ACCESS_TOKEN), requestBody.toString());
        call.enqueue(new Callback<WithdrawalModel>() {
            @Override
            public void onResponse(Call<WithdrawalModel> call, Response<WithdrawalModel> response) {
                try {
                    int code = response.code();

                    progressDialog.cancel();

                    mWithdrawalModel = new WithdrawalModel();
                    mWithdrawalDataModel = new WithdrawalDataModel();

                    if (code == 200) {
                        if (!(response.body() == null)) {
                            mWithdrawalModel.setError(response.body().getError());

                            if (!mWithdrawalModel.getError()) {

                                mWithdrawalModel.setData(response.body().getData());
                                mWithdrawalDataModel = mWithdrawalModel.getData();
                                updateUIForTakerInformation();

                            } else {
                                Helper.createSnackbar(MakerActivity.this, response.body().getMessage());
                                Log.i(TAG, "WithDrawalModel getError()");
                            }
                            progressDialog.cancel();
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

    private void updateUIForTakerInformation() {
        try {
            List<Double> takerLocation = mWithdrawalDataModel.getTakerLocation();
            addTakerMarker(takerLocation.get(1), takerLocation.get(0));

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

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
                    webSocket.send(createUpdateLocationMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    return;
                }
            }
        });
    }

    private void updateCameraView()
    {
        ArrayList<Marker> markers = new ArrayList<>();
        markers.add(currentMarker);
        markers.add(takerMarker);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300);
        mMap.moveCamera(cu);
    }

    private String createUpdateLocationMessage() throws JSONException {
        if (mLastKnownLocation == null) {
            return createLocationUpdateErrorMessage();
        }
        JSONObject mainRequestObject = new JSONObject();

        mainRequestObject.put("type", "method");
        mainRequestObject.put("method", "update-location");

        JSONObject payloadObject = new JSONObject();
        payloadObject.put("id", mWithdrawalModel.getData().getId());

        JSONArray locationArray = new JSONArray();
        locationArray.put(mLastKnownLocation.getLatitude());
        locationArray.put(mLastKnownLocation.getLongitude());

        payloadObject.put("loc", locationArray);

        mainRequestObject.put("payload", payloadObject);

        return mainRequestObject.toString();
    }

    private String createLocationUpdateErrorMessage() throws JSONException {
        JSONObject mainRequestObject = new JSONObject();

        mainRequestObject.put("type", "error");

        JSONObject payloadObject = new JSONObject();
        payloadObject.put("name", "LocationUpdateFail");

        mainRequestObject.put("payload", payloadObject);

        return mainRequestObject.toString();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getExtras().getString(CommonConstants.MESSAGE);
            String withdrawalId = intent.getExtras().getString(CommonConstants.WITHDRAWAL);

            createWithdrawEventDialog(message, withdrawalId);
        }
    };

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
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(CommonConstants.WITHDRAW_MATCH_EVENT));
    }
}