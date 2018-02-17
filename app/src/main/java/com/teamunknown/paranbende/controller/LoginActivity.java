package com.teamunknown.paranbende;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.teamunknown.paranbende.util.PreferencesPB;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class SplashScreenActivity extends AppCompatActivity {

    private String error="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();
    }

    private void init() {

        if (isDeviceOnline()){
            checkUserLogInStatus();
        }else{
            // Device has not a network connection. Toast a message and close application.
            Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.splash_screen_online_error), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SplashScreenActivity.this.finish();
                }
            }, GeneralValues.SPLASH_TIME_OUT);
        }
        }

    private void checkUserLogInStatus() {
        if (PreferencesPB.checkPreferencesWhetherTheValueisExistorNot(GeneralValues.LOGIN_DEVICE_ID)){
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            SplashScreenActivity.this.finish();
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        else {

        }

    }


    private boolean isDeviceOnline(){

        ConnectivityManager connectivityManager = (ConnectivityManager)SplashScreenActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

    }
}

