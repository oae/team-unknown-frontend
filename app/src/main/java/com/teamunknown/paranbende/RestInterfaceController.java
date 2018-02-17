package com.teamunknown.paranbende;

import com.teamunknown.paranbende.model.UserLoginModel;
import com.teamunknown.paranbende.model.WithdrawalModel;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by halitogunc on 17.02.2018.
 */

public interface RestInterfaceController {

    /*  login    */
    @Headers("Content-Type: application/json")
    @POST("/user/login")
    Call<UserLoginModel> userLogin(@Body String body);

    /*  register    */
    @Headers("Content-Type: application/json")
    @POST("/user/register")
    Call<UserLoginModel> userRegister(@Body String body);

    /*  create withdrawal    */
    @Headers("Content-Type: application/json")
    @POST("/taker/create-withdrawal")
    Call<WithdrawalModel> createWithdrawal(@Header("Authorization") String authorization, @Body String body);






}
