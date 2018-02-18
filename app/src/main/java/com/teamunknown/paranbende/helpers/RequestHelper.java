package com.teamunknown.paranbende.helpers;

import com.teamunknown.paranbende.RestInterfaceController;
import com.teamunknown.paranbende.constants.CommonConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by msalihkarakasli on 18.02.2018.
 */

public class RequestHelper
{
    public static RestInterfaceController createServiceAPI()
    {
        Retrofit retrofitObj = new Retrofit.Builder()
                    .baseUrl(CommonConstants.GeneralValues.BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        if (retrofitObj == null)
        {
            return null;
        }

        return retrofitObj.create(RestInterfaceController.class);
    }
}
