package com.nivel4.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.nivel4.fridaen45minutos.R;

public class About extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_about, null);

        TextView labelTextView = view.findViewById(R.id.labelTextView);
        TextView aboutTextView = view.findViewById(R.id.aboutTextView);
        Button closeButton = view.findViewById(R.id.closeButton);

        labelTextView.setText("Frida en 45 minutos");
        String about = "Developed by p4ncontomat3 and 1nh4l3r\n\n\nHappy hooking <3";
        aboutTextView.setText(about);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }


}
