package com.teamunknown.paranbende.controller;

import android.app.Dialog;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.teamunknown.paranbende.BaseMapActivity;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.constants.CommonConstants;
import com.teamunknown.paranbende.constants.GeneralValues;
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
    private EditText mMinAmountEditText, mMaxAmountEditText , mDistanceEditText;



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
                }
            }
        });

        Button bSettingsOk = findViewById(R.id.b_settings_ok);

        bSettingsOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.b_settings_ok) {
                    drawer.closeDrawer(GravityCompat.START);
                 //   saveSettings();
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

        getUserSettings();
        if (CommonConstants.FROM_PUSH_NOTIFICATION.equals(whereFrom)) {
            String message = getIntent().getExtras().getString("message");
            createWitdrawEventDialog(message);
        }
    }

    private void getUserSettings() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GeneralValues.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serviceAPI = retrofit.create(RestInterfaceController.class);

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
                            mUserSettingsData=mUserSettingsModel.getData();
                            mUserSettingsMaker=mUserSettingsData.getMaker();
                            if (!mUserSettingsModel.getError()) {
                               // Picasso.with(getApplicationContext()).load(mUserSettingsData.getAvatar()).into((Target) toolbar.getNavigationIcon());
                                mMinAmountEditText.setText(mUserSettingsMaker.getMinAmount());
                                mMaxAmountEditText.setText(mUserSettingsMaker.getMaxAmount());
                                mDistanceEditText.setText(mUserSettingsMaker.getRange());
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
    protected void updateObjectsOnMap(double latitude, double longitude, int zoomLevel) {
        return;
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void createWitdrawEventDialog(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MakerActivity.this);
        dialogBuilder.setTitle("Yeni bir işlem için onayınız bekleniyor!");
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(false);

        dialogBuilder.setPositiveButton(
                getResources().getString(R.string.accept),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        dialogBuilder.setNegativeButton(
                getResources().getString(R.string.reject),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialogBox = dialogBuilder.create();
        dialogBox.show();
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
