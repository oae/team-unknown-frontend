package com.teamunknown.paranbende;

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
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.teamunknown.paranbende.constants.CommonConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MakerActivity extends BaseMapActivity
{
    DrawerLayout drawer;
    WebSocket mWebSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maker);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        String whereFrom = "";
        if (getIntent() != null && getIntent().getExtras() != null)
        {
             whereFrom = getIntent().getExtras().getString(CommonConstants.WHERE_FROM);
        }

        setMapFragment();
        setMyLocationButton();

        drawer  = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ImageView iToggleBtn = findViewById(R.id.iMenuToggle);

        iToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (!drawer.isDrawerOpen(GravityCompat.START))
                {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        Button bSettingsOk =  findViewById(R.id.b_settings_ok);

        bSettingsOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (view.getId() == R.id.b_settings_ok) {
                    drawer.closeDrawer(GravityCompat.START);

                    Toast.makeText(MakerActivity.this, "Settings are updated.", Toast.LENGTH_LONG).show();
                }
            }
        });

        Switch sIsOnline =  findViewById(R.id.s_is_active);
        sIsOnline.setChecked(true);
        sIsOnline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                TextView tIsOnline = findViewById(R.id.tIsOnline);
                TextView tIsOffline = findViewById(R.id.tIsOffline);

                if (isChecked) {
                    tIsOffline.setVisibility(View.GONE);
                    tIsOnline.setVisibility(View.VISIBLE);
                }
                else {
                    tIsOffline.setVisibility(View.VISIBLE);
                    tIsOnline.setVisibility(View.GONE);
                }
            }
        });

        if (CommonConstants.FROM_PUSH_NOTIFICATION.equals(whereFrom)) {
            String message = getIntent().getExtras().getString("message");
            createWitdrawEventDialog(message);
        }
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
    public void onBackPressed()
    {

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
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
        public void onReceive(Context context, Intent intent)
        {
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
