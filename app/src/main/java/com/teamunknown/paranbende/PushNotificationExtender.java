package com.teamunknown.paranbende;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import com.teamunknown.paranbende.constants.CommonConstants;
import com.teamunknown.paranbende.constants.GeneralValues;
import com.teamunknown.paranbende.util.PreferencesPB;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by msalihkarakasli on 17.02.2018.
 */

public class PushNotificationExtender extends NotificationExtenderService
{
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult)
    {
        String userId = PreferencesPB.getValue(GeneralValues.LOGIN_USER_ID);
        try
        {
            if(userId.equals(receivedResult.payload.additionalData.getString("userId")) || true)
            {
                JSONObject withdrawObj = receivedResult.payload.additionalData.getJSONObject("withdrawal");

                if (receivedResult.isAppInFocus)
                {
                    Intent intent = new Intent(CommonConstants.WITHDRAW_MATCH_EVENT);
                    intent.putExtra(CommonConstants.MESSAGE, receivedResult.payload.body);
                    intent.putExtra(CommonConstants.WITHDRAWAL, withdrawObj.getString(CommonConstants.WITHDRAWAL_ID));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                    return true;
                }

                return false;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return true;
    }
}
