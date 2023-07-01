package com.nivel4.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.nivel4.fridaen45minutos.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetAPI extends DialogFragment {
    private EditText hostEditText;
    private EditText portEditText;

    private String hostStr;
    private String portStr;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_api, null);

        TextView labelTextView = view.findViewById(R.id.labelTextView);
        hostEditText = view.findViewById(R.id.hostEditText);
        portEditText = view.findViewById(R.id.portEditText);
        Button setButton = view.findViewById(R.id.setButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        labelTextView.setText("API host:port");

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostStr = hostEditText.getText().toString();
                portStr = portEditText.getText().toString();

                if (isValidIPAddress(hostStr) && isValidPort(portStr)) {
                    FileManager.generateSharedPrefs(getActivity(), hostStr, portStr);
                    dismiss();
                } else {
                    if (!isValidIPAddress(hostStr)) {
                        showMessage("Invalid IP address");
                    } else if (!isValidPort(portStr)) {
                        showMessage("Invalid port number. Port must be between 1 and 65535");
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void showMessage(CharSequence toastText) {
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidIPAddress(String ipAddress) {
        // Regular expression to validate IP address
        String ipPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    private boolean isValidPort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            return portNumber >= 1 && portNumber <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
