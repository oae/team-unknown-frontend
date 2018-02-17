package com.teamunknown.paranbende;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by halitogunc on 17.02.2018.
 */

public interface RestInterfaceController {

    /*  register    */
    @Headers("Content-Type: application/json")
    @POST("/api/Account/Register")
    Call<Void> userSignUp(@Body String body);




}
