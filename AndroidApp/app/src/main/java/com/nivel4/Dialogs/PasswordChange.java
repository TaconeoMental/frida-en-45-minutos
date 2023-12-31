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

import com.nivel4.Utils.ApiMethods;
import com.nivel4.fridaen45minutos.ProfileActivity;
import com.nivel4.fridaen45minutos.R;
import com.nivel4.httpClient.RequestPost;

public class PasswordChange extends DialogFragment implements ApiMethods.PassChangeCallback {
    private EditText newPasswordEditText;
    private RequestPost requestPost;
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

        labelTextView.setText("Password update");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiMethods apiMethods = new ApiMethods(getActivity());
                newPassword = newPasswordEditText.getText().toString();
                apiMethods.postChangePass(getActivity(), ProfileActivity.username, newPassword, ProfileActivity.token, PasswordChange.this);
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

    @Override
    public void onPassChanged() {
        ApiMethods apiMethods = new ApiMethods(getActivity());
        apiMethods.postLogout(getActivity(), ProfileActivity.username, ProfileActivity.token);
    }
}
