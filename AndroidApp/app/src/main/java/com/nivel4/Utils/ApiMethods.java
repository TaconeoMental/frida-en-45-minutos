package com.nivel4.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.Cipher.EncryptionUtils;
import com.nivel4.fridaen45minutos.MainActivity;
import com.nivel4.fridaen45minutos.ProfileActivity;
import com.nivel4.HttpClient.RequestPost;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import okhttp3.Call;

public class ApiMethods {
    public static String apiURL;
    private RequestPost requestPost;
    private EncryptDecrypt encryptDecrypt;
    private EncryptionUtils encryptionUtils;
    private String serverKeyStr;
    private SecretKey clientKey;
    private String clientKeyStr;
    private static String INIT_ENDPOINT;
    private static String LOGIN_ENDPOINT;

    public static String LOGOUT_ENDPOINT;
    private static String GETUSER_ENDPOINT;
    static JSONObject userData = new JSONObject();

    public ApiMethods(Context context) {
        Map<String, String> prefsMap = FileManager.readSharedPrefs(context);
        String hostStr = prefsMap.get("host");
        String portStr = prefsMap.get("port");

        apiURL = "http://" + hostStr + ":" + portStr;

        LOGOUT_ENDPOINT = apiURL + "/logout";
        GETUSER_ENDPOINT = apiURL + "/getuser";
        INIT_ENDPOINT = apiURL + "/init";
        LOGIN_ENDPOINT = apiURL + "/login";

        serverKeyStr = MainActivity.serverKeyStr;
        encryptionUtils = new EncryptionUtils();
    }

    public void postInit(Context context, Callback callback) throws NoSuchAlgorithmException {
        requestPost = new RequestPost();
        clientKey = encryptDecrypt.generatePartialKey();
        byte[] keyBytes = clientKey.getEncoded();
        clientKeyStr = encryptionUtils.bytesToHex(keyBytes);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("auth", clientKeyStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestPost.requestPost(jsonBody, INIT_ENDPOINT, new RequestPost.CustomResponseCallback() {
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
                    if (response.getString("status").equals("OK")) {
                        serverKeyStr = response.getString("key");
                        encryptDecrypt.secretKey = encryptDecrypt.setSecretKey(serverKeyStr);
                        MainActivity.serverKeyStr = serverKeyStr;
                        MainActivity.token = response.getString("token");
                        MainActivity.initOK = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void postLogin(Context context, String username, String password, String token){
        requestPost = new RequestPost();
        encryptDecrypt = new EncryptDecrypt();
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("username", encryptDecrypt.encrypt(username, encryptDecrypt.secretKey));
            jsonBody.put("password", encryptDecrypt.encrypt(password, encryptDecrypt.secretKey));
        } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        requestPost.requestPostAuth(token, jsonBody, LOGIN_ENDPOINT, new RequestPost.CustomResponseCallback() {
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
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("serverKeyStr", serverKeyStr);
                intent.putExtra("username", username);
                intent.putExtra("token", token);
                context.startActivity(intent);
                ((AppCompatActivity) context).finish();
            }
        });
    }


    public void postLogout(Context context, String username, String token) {
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

    public void postGetuser(Context context, String username, String token, UserDataCallback callback) {
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
                    JSONObject jsonTicket1 = tickets.getJSONObject("ticket1");
                    JSONObject jsonTicket2 = tickets.getJSONObject("ticket2");
                    JSONObject jsonTicket3 = tickets.getJSONObject("ticket3");

                    username = encryptDecrypt.decrypt(username, encryptDecrypt.secretKey);
                    role = encryptDecrypt.decrypt(role, encryptDecrypt.secretKey);
                    String ticket1 = encryptDecrypt.decrypt(jsonTicket1.getString("desc"), encryptDecrypt.secretKey);
                    String ticket2 = encryptDecrypt.decrypt(jsonTicket2.getString("desc"), encryptDecrypt.secretKey);
                    String ticket3 = encryptDecrypt.decrypt(jsonTicket3.getString("desc"), encryptDecrypt.secretKey);
                    String author1 = encryptDecrypt.decrypt(jsonTicket1.getString("author"), encryptDecrypt.secretKey);
                    String author2 = encryptDecrypt.decrypt(jsonTicket2.getString("author"), encryptDecrypt.secretKey);
                    String author3 = encryptDecrypt.decrypt(jsonTicket3.getString("author"), encryptDecrypt.secretKey);

                    jsonResponse.put("username", username);
                    jsonResponse.put("role", role);
                    jsonResponse.put("ticket1", ticket1);
                    jsonResponse.put("ticket2", ticket2);
                    jsonResponse.put("ticket3", ticket3);
                    jsonResponse.put("author1", author1);
                    jsonResponse.put("author2", author2);
                    jsonResponse.put("author3", author3);


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

    public interface Callback {
        void onSuccess();
        void onFailure();
    }
}
