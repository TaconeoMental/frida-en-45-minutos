package com.nivel4.fridaen45minutos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.httpClient.RequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Call;

public class PasswordChange extends DialogFragment {
    private EditText newPasswordEditText;
    private RequestPost requestPost;
    private static final String PASSCHANGE_ENDPOINT = BuildConfig.PASSCHANGE_ENDPOINT;
    private String newPassword;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_password_change, null);

        TextView labelTextView = view.findViewById(R.id.labelTextView);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        Button sendButton = view.findViewById(R.id.sendButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        labelTextView.setText("Password Change");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPassword = newPasswordEditText.getText().toString();
                JSONObject jsonBody = new JSONObject();
                try {
                    String encryptedUsername = EncryptDecrypt.encrypt(ProfileActivity.username, EncryptDecrypt.secretKey);
                    String encryptedPassword = EncryptDecrypt.encrypt(newPassword, EncryptDecrypt.secretKey);
                    jsonBody.put("username", encryptedUsername);
                    jsonBody.put("password", encryptedPassword);
                } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                }

                requestPost.requestPostAuth(ProfileActivity.token, jsonBody, PASSCHANGE_ENDPOINT, new RequestPost.CustomResponseCallback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage(e.getMessage());
                                dismiss();
                            }
                        });
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage("Password updated, please log in again");
                                dismiss();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }

                });
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requestPost = new RequestPost();
    }

    private void showMessage(CharSequence toastText) {
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }

}
