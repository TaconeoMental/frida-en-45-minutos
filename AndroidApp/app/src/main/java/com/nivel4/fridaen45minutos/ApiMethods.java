package com.nivel4.fridaen45minutos;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class ApiMethods {
    private RequestPost requestPost;
    private EncryptDecrypt encryptDecrypt;
    public static final String LOGOUT_ENDPOINT = BuildConfig.LOGOUT_ENDPOINT;
    private static final String GETUSER_ENDPOINT = BuildConfig.GETUSER_ENDPOINT;
    static JSONObject userData = new JSONObject();



    public void logOut(Context context, String username, String token) {
        requestPost = new RequestPost();
        encryptDecrypt = new EncryptDecrypt();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", encryptDecrypt.encrypt(username, encryptDecrypt.secretKey));
        } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        requestPost.requestPostAuth(token, jsonBody, LOGOUT_ENDPOINT, new RequestPost.CustomResponseCallback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(context, e.getMessage());
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                        ((AppCompatActivity) context).finish();
                    }
                });
            }

            @Override
            public void onResponse(JSONObject response) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                ((AppCompatActivity) context).finish();
            }
        });
    }

    public void getUserData(Context context, String username, String token, UserDataCallback callback) {
        requestPost = new RequestPost();
        encryptDecrypt = new EncryptDecrypt();

        JSONObject jsonBody = new JSONObject();
        JSONObject jsonResponse = new JSONObject();

        try {
            jsonBody.put("username", encryptDecrypt.encrypt(username, encryptDecrypt.secretKey));
        } catch (JSONException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }

        requestPost.requestPostAuth(token, jsonBody, GETUSER_ENDPOINT, new RequestPost.CustomResponseCallback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(context, e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String username = response.getString("username");
                    String role = response.getString("role");
                    JSONObject tickets = response.getJSONObject("tickets");

                    username = encryptDecrypt.decrypt(username, encryptDecrypt.secretKey);
                    role = encryptDecrypt.decrypt(role, encryptDecrypt.secretKey);
                    String ticket1 = encryptDecrypt.decrypt(tickets.getString("ticket1"), encryptDecrypt.secretKey);
                    String ticket2 = encryptDecrypt.decrypt(tickets.getString("ticket2"), encryptDecrypt.secretKey);
                    String ticket3 = encryptDecrypt.decrypt(tickets.getString("ticket3"), encryptDecrypt.secretKey);

                    jsonResponse.put("username", username);
                    jsonResponse.put("role", role);
                    jsonResponse.put("ticket1", ticket1);
                    jsonResponse.put("ticket2", ticket2);
                    jsonResponse.put("ticket3", ticket3);

                    callback.onUserDataReceived(jsonResponse);


                    Log.d("apimethods userdata", userData.toString());
                } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showMessage(Context context, CharSequence toastText) {
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
    }

    public interface UserDataCallback {
        void onUserDataReceived(JSONObject userData);
    }

    public interface LogoutCallback {
        void onLogoutSuccess();
        void onLogoutFailure(Call call, Exception e);
    }
}
