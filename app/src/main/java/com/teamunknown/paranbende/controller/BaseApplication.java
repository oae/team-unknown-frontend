package com.teamunknown.paranbende.controller;

import android.app.Application;
import android.content.Context;

import com.onesignal.OneSignal;
import com.teamunknown.paranbende.PushNotificationHandler;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class BaseApplication extends Application {

    private static Context context;
    public static Context getAppContext(){
        return BaseApplication.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler(new PushNotificationHandler())
                .init();
    }
}
