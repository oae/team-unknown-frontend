package com.teamunknown.paranbende;

import android.content.Intent;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.teamunknown.paranbende.constants.CommonConstants;
import com.teamunknown.paranbende.controller.BaseApplication;

import org.json.JSONObject;

/**
 * Created by msalihkarakasli on 17.02.2018.
 */

public class PushNotificationHandler implements OneSignal.NotificationOpenedHandler {

    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        // The following can be used to open an Activity of your choice.
        // Replace - getApplicationContext() - with any Android Context.
        Intent intent = new Intent(BaseApplication.getAppContext(), MakerActivity.class);
        intent.putExtra(CommonConstants.WHERE_FROM, CommonConstants.FROM_PUSH_NOTIFICATION);
        intent.putExtra("message", result.notification.payload.body);

        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        BaseApplication.getAppContext().startActivity(intent);
    }
}
