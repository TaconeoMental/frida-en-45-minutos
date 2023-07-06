package com.nivel4.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.nivel4.fridaen45minutos.R;

public class ExitDialog {

    public static void showDialogAndExit(Context context, String title) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(title);
        ImageView gifImageView = new ImageView(context);
        gifImageView.setImageResource(R.drawable.sad);
        gifImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        alertDialog.setView(gifImageView);
        alertDialog.setMessage("This application can't be used on rooted devices uwu");

        alertDialog.setButton(alertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

}
