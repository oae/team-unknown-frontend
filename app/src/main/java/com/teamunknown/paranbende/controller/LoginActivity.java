package com.teamunknown.paranbende.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.teamunknown.paranbende.GeneralValues;
import com.teamunknown.paranbende.MakerActivity;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.model.Data;
import com.teamunknown.paranbende.model.User;
import com.teamunknown.paranbende.model.UserLoginModel;
import com.teamunknown.paranbende.util.Helper;
import com.teamunknown.paranbende.util.PreferencesPB;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mSignInBtn;
    private Spinner mUserTypeSpinner;
    private ProgressDialog progressDialog;
    View focusView = null;
    private TextView mAccountRegisterTextBtn;

    private RestInterfaceController serviceAPI;
    private Data mData;
    private User mUser;
    private UserLoginModel mUserLoginModel;

    private String usernameStr, passwordStr;
    private String selectedItem = null;


    private JSONObject requestBody;


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkUserLogInStatus();

        mEmailView = findViewById(R.id.email);

        mAccountRegisterTextBtn = findViewById(R.id.account_register_text);

        mUserTypeSpinner = findViewById(R.id.user_type_spinner);

        List list = new ArrayList();
        list.add("Maker");
        list.add("Taker");


        mAccountRegisterTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, AccountRegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        mPasswordView = findViewById(R.id.password);

        mSignInBtn = findViewById(R.id.email_sign_in_button);
        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    attemptLogin();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        ArrayAdapter dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUserTypeSpinner.setAdapter(dataAdapter);


    }


    private void attemptLogin() throws JSONException {
        boolean mCancel = this.logInValidation();
        if (mCancel) {
            focusView.requestFocus();
        } else {
            logIn();
        }
    }

    private void logIn() {
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
        try {
            JSONObject json = new JSONObject();
            json.put("email", usernameStr);
            json.put("password", passwordStr);
            requestBody = json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception");
        }

        retrofit2.Call<UserLoginModel> call = serviceAPI.userLogin(requestBody.toString());
        call.enqueue(new Callback<UserLoginModel>() {
            @Override
            public void onResponse(retrofit2.Call<UserLoginModel> call, Response<UserLoginModel> response) {
                response.body();
                try {
                    int code = response.code();
                    mUserLoginModel = new UserLoginModel();
                    mData = new Data();
                    mUser = new User();
                    if (code == 200) {

                        if (!(response.body() == null)) {
                            mUserLoginModel.setError(response.body().getError());

                            if (!mUserLoginModel.getError()) {
                                mUserLoginModel.setData(response.body().getData());
                                mData = mUserLoginModel.getData();
                                mUser.setEmail(mData.getUser().getEmail());

                                PreferencesPB.setValue(GeneralValues.LOGIN_USER_NAME, mUser.getEmail());
                                PreferencesPB.setValue(GeneralValues.LOGIN_ACCESS_TOKEN, mData.getToken());
                                PreferencesPB.setValue(GeneralValues.LOGIN_USER_ID, mUser.getId());
                                Intent intent = null;
                                if (PreferencesPB.checkPreferencesWhetherTheValueisExistorNot(GeneralValues.LOGIN_USER_TYPE)){
                                    if (PreferencesPB.getValue(GeneralValues.LOGIN_USER_TYPE) == "Taker") {
                                        intent = new Intent(LoginActivity.this, TakerActivity.class);
                                    } else {
                                        intent = new Intent(LoginActivity.this, MakerActivity.class);
                                    }
                                }
                                else {
                                    intent = new Intent(LoginActivity.this, MakerActivity.class);
                                }
                                startActivity(intent);
                                LoginActivity.this.finish();
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                            } else {
                                Helper.createSnackbar(LoginActivity.this, response.body().getMessage());
                            }

                        } else {
                            Helper.createAlertDialog(LoginActivity.this, "Username or password is wrong.", false);
                        }

                        progressDialog.cancel();
                    } else {
                        progressDialog.cancel();
                        Helper.createSnackbar(LoginActivity.this, getString(R.string.internet_connection_problem));
                    }
                } catch (Exception e) {
                    progressDialog.cancel();
                    Helper.createSnackbar(LoginActivity.this, getString(R.string.something_going_wrong));
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(retrofit2.Call<UserLoginModel> call, Throwable t) {
                progressDialog.cancel();
                Helper.createSnackbar(LoginActivity.this, getString(R.string.internet_connection_problem));
            }
        });


    }

    private boolean logInValidation() {
        //default values
        boolean cancel = false;
        mEmailView.setError(null);
        ;
        mPasswordView.setError(null);

        // Store values at the time of the sign up attempt.
        usernameStr = mEmailView.getText().toString();
        passwordStr = mPasswordView.getText().toString();

        if (passwordStr.length() < 6) {
            Helper.createSnackbar(this, getString(R.string.password_length_failed));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(passwordStr)) {
            Helper.createSnackbar(this, getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(usernameStr)) {
            Helper.createSnackbar(this, getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        return cancel;
    }


    private void checkUserLogInStatus() {
        if (PreferencesPB.checkPreferencesWhetherTheValueisExistorNot(GeneralValues.LOGIN_USER_NAME)) {
            Intent intent;
            if (PreferencesPB.checkPreferencesWhetherTheValueisExistorNot(GeneralValues.LOGIN_USER_TYPE) &&
                    PreferencesPB.getValue(GeneralValues.LOGIN_USER_TYPE)=="Taker"){
                intent = new Intent(LoginActivity.this, TakerActivity.class);
            }
            else {
                intent = new Intent(LoginActivity.this, MakerActivity.class);
            }
            LoginActivity.this.finish();
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else
            return;

    }


    private boolean isDeviceOnline() {

        ConnectivityManager connectivityManager = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        selectedItem = parent.getItemAtPosition(position).toString();
        PreferencesPB.setValue(GeneralValues.LOGIN_USER_TYPE, selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

