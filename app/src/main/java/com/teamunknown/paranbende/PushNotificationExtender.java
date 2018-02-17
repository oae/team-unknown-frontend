package com.teamunknown.paranbende;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import com.teamunknown.paranbende.constants.CommonConstants;
import com.teamunknown.paranbende.helpers.CheckAppIsRunning;
import com.teamunknown.paranbende.helpers.MapHelper;

/**
 * Created by msalihkarakasli on 17.02.2018.
 */

public class PushNotificationExtender extends NotificationExtenderService
{
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        if (receivedResult.isAppInFocus) {

            Intent intent = new Intent(CommonConstants.WITHDRAW_MATCH_EVENT);
            intent.putExtra("amount", receivedResult.payload.title);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            return true;
        }

        return false;
    }
}
