package com.teamunknown.paranbende.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.teamunknown.paranbende.GeneralValues;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.util.PreferencesPB;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mSignInBtn;
    private ProgressDialog progressDialog;
    View focusView = null;
    private TextView mAccountRegisterTextBtn;

    private RestInterfaceController serviceAPI;
    private UserLoginTokenModel userLoginTokenModel;

    private static String grantType = "password";
    private String usernameStr, passwordStr;
    private String error = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {

        if (isDeviceOnline()){
            checkUserLogInStatus();
        }else{

        }
        }

    private void checkUserLogInStatus() {
        if (PreferencesPB.checkPreferencesWhetherTheValueisExistorNot(GeneralValues.LOGIN_DEVICE_ID)){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            LoginActivity.this.finish();
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        else {

        }

    }


    private boolean isDeviceOnline(){

        ConnectivityManager connectivityManager = (ConnectivityManager)LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

    }
}

