package com.teamunknown.paranbende.helpers;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.teamunknown.paranbende.R;

/**
 * Created by msalihkarakasli on 18.02.2018.
 */

public class DialogHelper
{
    public static ProgressDialog show(Context context) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.searching));
        progressDialog.setCancelable(false);
        progressDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.dismiss();
            }
        });
        progressDialog.show();

        return progressDialog;
    }
}
