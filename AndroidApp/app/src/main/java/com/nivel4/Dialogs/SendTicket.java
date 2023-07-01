package com.nivel4.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import com.nivel4.fridaen45minutos.BuildConfig;
import com.nivel4.fridaen45minutos.ProfileActivity;
import com.nivel4.fridaen45minutos.R;
import com.nivel4.HttpClient.RequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Call;

public class SendTicket extends DialogFragment {
    private EditText newTicketEditText;
    private RequestPost requestPost;
    private static final String SENDTICKET_ENDPOINT = BuildConfig.SENDTICKET_ENDPOINT;
    private String newTicket;
    private SendTicketListener sendTicketListener;

    public interface SendTicketListener {
        void onTicketSent() throws JSONException;
        void onCancel();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_send_ticket, null);

        TextView labelTextView = view.findViewById(R.id.labelTextView);
        newTicketEditText = view.findViewById(R.id.newTicketEditText);
        Button sendButton = view.findViewById(R.id.sendButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        labelTextView.setText("New ticket");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTicket = newTicketEditText.getText().toString();
                JSONObject jsonBody = new JSONObject();
                try {
                    String encryptedUsername = EncryptDecrypt.encrypt(ProfileActivity.username, EncryptDecrypt.secretKey);
                    String encryptedTicket = EncryptDecrypt.encrypt(newTicket, EncryptDecrypt.secretKey);
                    jsonBody.put("username", encryptedUsername);
                    jsonBody.put("ticket", encryptedTicket);
                } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                }

                requestPost.requestPostAuth(ProfileActivity.token, jsonBody, SENDTICKET_ENDPOINT, new RequestPost.CustomResponseCallback() {
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
                                showMessage("Ticket sent");
                                dismiss();
                                try {
                                    sendTicketListener.onTicketSent();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
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
                sendTicketListener.onCancel();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SendTicketListener) {
            sendTicketListener = (SendTicketListener) context;
        } else {
            throw new IllegalArgumentException("Activity must implement SendTicketListener");
        }
        requestPost = new RequestPost();
    }

    private void showMessage(CharSequence toastText) {
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }
}
