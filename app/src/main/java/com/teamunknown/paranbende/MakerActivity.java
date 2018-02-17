package com.teamunknown.paranbende;

import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
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
import com.teamunknown.paranbende.constants.CommonConstants;

public class MakerActivity extends BaseMapActivity
{
    // Keys for storing activity state.
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maker);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

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
    }

    @Override
    protected void updateObjectsOnMap(double latitude,double longitude,int zoomLevel)
    {
        return;
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("amount");

            Toast.makeText(MakerActivity.this, "Bilgi:" + message, Toast.LENGTH_LONG).show();

            Log.d("receiver", "Got message: " + message);
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
