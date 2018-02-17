package com.teamunknown.paranbende.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.teamunknown.paranbende.GeneralValues;
import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.model.UserLoginModel;
import com.teamunknown.paranbende.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class AccountRegisterActivity extends AppCompatActivity {

    private static final String TAG = AccountRegisterActivity.class.getSimpleName();

    //UI references
    private EditText mUserEmail, mUserPassword, mUserConfirmationPassword;
    private Button mSubmitButton;
    private ProgressDialog progressDialog;
    private ActionMenuView amvMenu;
    View focusView = null;
    private RestInterfaceController serviceAPI;
    private String mUserEmailStr, mUserPasswordStr, mUserConfirmationPasswordStr;
    private JSONObject requestBody;

    private UserLoginModel mUserLoginModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_register);

        Toolbar toolbar = findViewById(R.id.toolbar);

        amvMenu = toolbar.findViewById(R.id.amvMenu);

        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mUserEmail = findViewById(R.id.UserEmail);
        mUserPassword = findViewById(R.id.UserPassword);
        mUserConfirmationPassword = findViewById(R.id.UserPasswordConfirmation);
        mSubmitButton = findViewById(R.id.submitBtn);


        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSingUp();
            }


        });


    }

    private void attemptSingUp() {
        boolean mCancel = this.signUpValidation();
        if (mCancel) {
            focusView.requestFocus();
        } else {
            signUp();
        }
    }

    private void signUp() {
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
            json.put("email", mUserEmailStr);
            json.put("password", mUserPasswordStr);
            requestBody = json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception");
        }

        Call<UserLoginModel> call = serviceAPI.userRegister(requestBody.toString());
        call.enqueue(new Callback<UserLoginModel>() {
            @Override
            public void onResponse(Call<UserLoginModel> call, Response<UserLoginModel> response) {


                response.body();
                try {
                    int code = response.code();
                    mUserLoginModel = new UserLoginModel();

                    if (code == 200) {

                        if (!(response.body() == null)) {
                            mUserLoginModel.setError(response.body().getError());

                            if (!mUserLoginModel.getError()) {
                                Helper.createAlertDialog(AccountRegisterActivity.this, getString(R.string.successfully_registration_message), true);

                            } else {
                                Helper.createSnackbar(AccountRegisterActivity.this, response.body().getMessage());
                            }
                            progressDialog.cancel();

                        } else {
                            Helper.createAlertDialog(AccountRegisterActivity.this, getString(R.string.something_going_wrong), false);
                        }

                        progressDialog.cancel();
                    } else {
                        progressDialog.cancel();
                        Helper.createSnackbar(AccountRegisterActivity.this, getString(R.string.internet_connection_problem));
                    }
                } catch (Exception e) {
                    progressDialog.cancel();
                    Helper.createSnackbar(AccountRegisterActivity.this, getString(R.string.something_going_wrong));
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<UserLoginModel> call, Throwable t) {
                progressDialog.cancel();
                Helper.createSnackbar(AccountRegisterActivity.this, getString(R.string.internet_connection_problem));
            }
        });


    }

    private boolean signUpValidation() {
        //default values
        boolean cancel = false;
        mUserEmail.setError(null);
        mUserPassword.setError(null);
        mUserConfirmationPassword.setError(null);

        // Store values at the time of the sign up attempt.
        mUserEmailStr = mUserEmail.getText().toString();
        mUserPasswordStr = mUserPassword.getText().toString();
        mUserConfirmationPasswordStr = mUserConfirmationPassword.getText().toString();

        if (mUserConfirmationPasswordStr.length() < 6) {
            mUserConfirmationPassword.setError(getString(R.string.password_length_failed));
            focusView = mUserConfirmationPassword;
            cancel = true;
        }
        if (mUserPasswordStr.length() < 6) {
            mUserPassword.setError(getString(R.string.password_length_failed));
            focusView = mUserPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(mUserConfirmationPasswordStr)) {
            mUserConfirmationPassword.setError(getString(R.string.error_field_required));
            focusView = mUserConfirmationPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(mUserPasswordStr)) {
            mUserPassword.setError(getString(R.string.error_field_required));
            focusView = mUserPassword;
            cancel = true;
        }

        if (!mUserPasswordStr.equals(mUserConfirmationPasswordStr)) {
            mUserConfirmationPassword.setError(getString(R.string.password_confirmation_failed));
            focusView = mUserConfirmationPassword;
            cancel = true;
        }


        if (TextUtils.isEmpty(mUserEmailStr)) {
            mUserEmail.setError(getString(R.string.error_field_required));
            focusView = mUserEmail;
            cancel = true;
        }

        return cancel;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.registration_menu, amvMenu.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backBtn:
                Intent intent = new Intent(AccountRegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
        }
        return true;
    }
}
