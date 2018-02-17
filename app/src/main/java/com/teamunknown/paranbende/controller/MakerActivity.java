package com.teamunknown.paranbende.controller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.teamunknown.paranbende.BaseMapActivity;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.constants.CommonConstants;
import com.teamunknown.paranbende.constants.GeneralValues;
import com.teamunknown.paranbende.helpers.RequestHelper;
import com.teamunknown.paranbende.model.Settings.SettingsDataModel;
import com.teamunknown.paranbende.model.Settings.SettingsMaker;
import com.teamunknown.paranbende.model.Settings.SettingsModel;
import com.teamunknown.paranbende.model.Settings.UserSettings.UserSettingsData;
import com.teamunknown.paranbende.model.Settings.UserSettings.UserSettingsMaker;
import com.teamunknown.paranbende.model.Settings.UserSettings.UserSettingsModel;
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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MakerActivity extends BaseMapActivity {
    // Keys for storing activity state.
    DrawerLayout drawer;
    private Toolbar toolbar;

    WebSocket mWebSocket;

    private SettingsDataModel mSettingsDataModel;
    private SettingsMaker mSettingsMaker;
    private SettingsModel mSettingsModel;
    private UserSettingsModel mUserSettingsModel;
    private UserSettingsMaker mUserSettingsMaker;
    private UserSettingsData mUserSettingsData;

    private ProgressDialog progressDialog;

    private RestInterfaceController serviceAPI;

    private JSONObject requestBody;

    private Switch sIsOnline;
    public EditText mMinAmountEditText, mMaxAmountEditText, mDistanceEditText;


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
                    getUserSettings();
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
                TextView tIsOffline = findViewById(R.id.tIsOffline);

                if (isChecked) {
                    tIsOffline.setVisibility(View.GONE);
                    tIsOnline.setVisibility(View.VISIBLE);
                } else {
                    tIsOffline.setVisibility(View.VISIBLE);
                    tIsOnline.setVisibility(View.GONE);
                }
            }
        });

        mMinAmountEditText = findViewById(R.id.e_min_amount);
        mMaxAmountEditText = findViewById(R.id.e_max_amount);
        mDistanceEditText = findViewById(R.id.e_distance);



        if (CommonConstants.FROM_PUSH_NOTIFICATION.equals(whereFrom)) {
            String message = getIntent().getExtras().getString("message");
            createWitdrawEventDialog(message);
        }
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

        retrofit2.Call<SettingsModel> call = serviceAPI.saveUserSettings("Bearer " + PreferencesPB.getValue(GeneralValues.LOGIN_ACCESS_TOKEN), requestBody.toString());
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
                            }
                            progressDialog.cancel();
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Response body is null");
                }
            }

            @Override
            public void onFailure(Call<SettingsModel> call, Throwable t) {
                Log.e(TAG, "onFailure()");
            }
        });

    }

    private void getUserSettings() {

        serviceAPI = RequestHelper.createServiceAPI();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        retrofit2.Call<UserSettingsModel> call = serviceAPI.getUserSettings("Bearer " + PreferencesPB.getValue(GeneralValues.LOGIN_ACCESS_TOKEN));
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
                                // Picasso.with(getApplicationContext()).load(mUserSettingsData.getAvatar()).into((Target) toolbar.getNavigationIcon());
                                mMinAmountEditText.setText(String.valueOf(mUserSettingsMaker.getMinAmount()));
                                mMaxAmountEditText.setText(String.valueOf(mUserSettingsMaker.getMaxAmount()));
                                mDistanceEditText.setText(String.valueOf(mUserSettingsMaker.getRange()));
                            } else {
                                Helper.createSnackbar(MakerActivity.this, response.body().getMessage());
                            }
                            progressDialog.cancel();

                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Response body is null");
                    progressDialog.cancel();
                }
            }

            @Override
            public void onFailure(Call<UserSettingsModel> call, Throwable t) {

            }
        });

    }

    @Override
    protected void updateObjectsOnMap(double latitude,double longitude,int zoomLevel)
    {
        if (mWebSocket != null)
        {
            try
            {
                mWebSocket.send(createUpdateLocationMessage());
            }
            catch (JSONException e)
            {
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
    }

    private void createWitdrawEventDialog(String message)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MakerActivity.this);
        dialogBuilder.setTitle("Yeni bir işlem için onayınız bekleniyor!");
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(false);

        dialogBuilder.setPositiveButton(
                getResources().getString(R.string.accept),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // TODO: Sent accept request to for withdrawal.
                        // Start to web socket connection to sent own location.
                        connectWebSocket();
                    }
                });

        dialogBuilder.setNegativeButton(
                getResources().getString(R.string.reject),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // TODO: Sent reject request to for withdrawal.
                        dialog.cancel();
                    }
                });

        AlertDialog dialogBox = dialogBuilder.create();
        dialogBox.show();
    }

    private void connectWebSocket()
    {
        AsyncHttpClient.getDefaultInstance().websocket("ws://lab.nepjua.org:23000", null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                mWebSocket = webSocket;

                try
                {
                    webSocket.send(createUpdateLocationMessage());
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    private String createUpdateLocationMessage() throws JSONException
    {
        if (mLastKnownLocation == null)
        {
            return createLocationUpdateErrorMessage();
        }
        JSONObject mainRequestObject = new JSONObject();

        mainRequestObject.put("type", "method");
        mainRequestObject.put("method", "update-location");

        JSONObject payloadObject = new JSONObject();
        payloadObject.put("id", "test1");

        JSONArray locationArray = new JSONArray();
        locationArray.put(mLastKnownLocation.getLatitude());
        locationArray.put(mLastKnownLocation.getLongitude());

        payloadObject.put("loc", locationArray);

        mainRequestObject.put("payload", payloadObject);

        return mainRequestObject.toString();
    }

    private String createLocationUpdateErrorMessage() throws JSONException
    {
        JSONObject mainRequestObject = new JSONObject();

        mainRequestObject.put("type", "error");

        JSONObject payloadObject = new JSONObject();
        payloadObject.put("name", "LocationUpdateFail");

        mainRequestObject.put("payload", payloadObject);

        return mainRequestObject.toString();
    }

    private boolean parseSocketMessage(String s) throws JSONException
    {
        if ("".equals(s))
        {
            return false;
        }

        JSONObject mainObject = new JSONObject(s);

        String actionType = mainObject.getString("type");
        JSONObject payloadObject = mainObject.getJSONObject("payload");

        if (CommonConstants.ACTION_START.equals(actionType))
        {

        }
        else if (CommonConstants.ACTION_LOCATION_UPDATE.equals(actionType))
        {

        }
        else if (CommonConstants.ACTION_END.equals(actionType))
        {

        }
        else
        {

        }

        return true;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getExtras().getString(CommonConstants.MESSAGE);
            createWitdrawEventDialog(message);
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
