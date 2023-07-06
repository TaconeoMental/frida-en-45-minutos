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
import com.nivel4.fridaen45minutos.FeedActivity;
import com.nivel4.fridaen45minutos.ProfileActivity;
import com.nivel4.fridaen45minutos.R;
import com.nivel4.httpClient.RequestPost;

import org.json.JSONException;

public class SendPost extends DialogFragment implements ApiMethods.SendPostCallback {
    private EditText newPostEditText;
    private RequestPost requestPost;
    private String newPost;
    private SendPostListener sendPostListener;

    public interface SendPostListener {
        void onPostSent() throws JSONException;
        void onCancel();
    }

    public SendPost(SendPostListener sendPostListener) {
        this.sendPostListener = sendPostListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_send_post, null);

        TextView labelTextView = view.findViewById(R.id.labelTextView);
        newPostEditText = view.findViewById(R.id.newPostEditText);
        Button sendButton = view.findViewById(R.id.sendButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        labelTextView.setText("New Post");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiMethods apiMethods = new ApiMethods(getActivity());
                newPost = newPostEditText.getText().toString();
                apiMethods.postSendPost(getActivity(), FeedActivity.username, newPost, ProfileActivity.token, SendPost.this);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                sendPostListener.onCancel();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SendPostListener) {
            sendPostListener = (SendPostListener) context;
        } else {
            throw new IllegalArgumentException("Activity must implement SendPostListener");
        }
        requestPost = new RequestPost();
    }

    private void showMessage(CharSequence toastText) {
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostSentSuccess() throws JSONException {
        showMessage("Post sent");
        dismiss();
        sendPostListener.onPostSent();
    }

    @Override
    public void onPostSentFailure(String errorMessage) {
        showMessage(errorMessage);
        dismiss();
    }

}
