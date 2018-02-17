package com.teamunknown.paranbende.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.teamunknown.paranbende.R;
import com.teamunknown.paranbende.controller.LoginActivity;

/**
 * Created by halitogunc on 17.02.2018.
 */

public class Helper {

    public static void createSnackbar(Activity activity, String message) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(activity, R.color.white));
        snackbar.show();

    }

    public static void createAlertDialog(final Activity activity, String message, final Boolean openLoginActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                if (openLoginActivity){
                    Intent intent=new Intent(activity,LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }

                dialog.cancel();
            }
        });


        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
