package com.nivel4.Utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.Cipher.EncryptionUtils;
import com.nivel4.fridaen45minutos.FeedActivity;
import com.nivel4.fridaen45minutos.MainActivity;
import com.nivel4.httpClient.RequestPost;


import org.json.JSONArray;
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
    private byte[] serverKey;
    private static String INIT_ENDPOINT;
    private static String LOGIN_ENDPOINT;

    public static String LOGOUT_ENDPOINT;
    private static String GETUSER_ENDPOINT;
    private static String GETFEED_ENDPOINT;
    private static String PASSCHANGE_ENDPOINT;
    private static String SENDPOST_ENDPOINT;



    static JSONObject userData = new JSONObject();
    static JSONObject feedData = new JSONObject();


    public ApiMethods(Context context) {
        Map<String, String> prefsMap = FileManager.readSharedPrefs(context);
        String hostStr = prefsMap.get("host");
        String portStr = prefsMap.get("port");
        String protocolStr = prefsMap.get("protocol");

        apiURL = protocolStr + "://" + hostStr + ":" + portStr;

        LOGOUT_ENDPOINT = apiURL + "/logout";
        GETUSER_ENDPOINT = apiURL + "/getuser";
        GETFEED_ENDPOINT = apiURL + "/getfeed";
        INIT_ENDPOINT = apiURL + "/init";
        LOGIN_ENDPOINT = apiURL + "/login";
        PASSCHANGE_ENDPOINT = apiURL + "/changepass";
        SENDPOST_ENDPOINT = apiURL + "/sendpost";


        serverKeyStr = MainActivity.serverKeyStr;
        encryptionUtils = new EncryptionUtils();
    }

    public void postInit(Context context){
        requestPost = new RequestPost();
        try {
            clientKey = encryptDecrypt.generatePartialKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
                        serverKey = encryptionUtils.hexToBytes(serverKeyStr);
                        byte[] concatedKeys = encryptionUtils.concatArrays(clientKey.getEncoded(), serverKey);
                        String secretKeyStr = encryptionUtils.bytesToHex(concatedKeys);
                        String key = encryptionUtils.bytesToHex(concatedKeys);
                        encryptDecrypt.secretKey = encryptDecrypt.setSecretKey(key);
                        MainActivity.serverKeyStr = secretKeyStr;
                        MainActivity.token = response.getString("token");
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
                Intent intent = new Intent(context, FeedActivity.class);
                intent.putExtra("serverKeyStr", serverKeyStr);
                intent.putExtra("username", username);
                intent.putExtra("token", token);
                context.startActivity(intent);
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

    public void postChangePass(Context context, String username, String newPassword, String token, PassChangeCallback callback) {
        requestPost = new RequestPost();
        encryptDecrypt = new EncryptDecrypt();
        JSONObject jsonBody = new JSONObject();
        try {
            String encryptedUsername = EncryptDecrypt.encrypt(username, EncryptDecrypt.secretKey);
            String encryptedPassword = EncryptDecrypt.encrypt(newPassword, EncryptDecrypt.secretKey);
            jsonBody.put("username", encryptedUsername);
            jsonBody.put("password", encryptedPassword);
        } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        requestPost.requestPostAuth(token, jsonBody, PASSCHANGE_ENDPOINT, new RequestPost.CustomResponseCallback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(context, e.getMessage());
                        callback.onPassChanged(); // Notify callback on failure
                    }
                });
            }

            @Override
            public void onResponse(JSONObject response) {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(context, "Password updated, please log in again");
                        callback.onPassChanged(); // Notify callback on success
                    }
                });
            }
        });
    }

    public void postSendPost(Context context, String username, String contents, String token, SendPostCallback callback) {
        requestPost = new RequestPost();
        encryptDecrypt = new EncryptDecrypt();
        JSONObject jsonBody = new JSONObject();
        try {
            String encryptedUsername = EncryptDecrypt.encrypt(FeedActivity.username, EncryptDecrypt.secretKey);
            String encryptedPost = EncryptDecrypt.encrypt(contents, EncryptDecrypt.secretKey);
            jsonBody.put("username", encryptedUsername);
            jsonBody.put("contents", encryptedPost);
        } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        requestPost.requestPostAuth(FeedActivity.token, jsonBody, SENDPOST_ENDPOINT, new RequestPost.CustomResponseCallback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(context, e.getMessage());
                        try {
                            callback.onPostSentSuccess();
                        } catch (JSONException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }

            @Override
            public void onResponse(JSONObject response) {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(context, "Post sent");
                        try {
                            callback.onPostSentSuccess();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
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
                    String bio = response.getString("bio");
                    String password_hint = response.getString("password_hint");


                    username = encryptDecrypt.decrypt(username, encryptDecrypt.secretKey);
                    role = encryptDecrypt.decrypt(role, encryptDecrypt.secretKey);
                    bio = encryptDecrypt.decrypt(bio, encryptDecrypt.secretKey);
                    password_hint = encryptDecrypt.decrypt(password_hint, encryptDecrypt.secretKey);

                    jsonResponse.put("username", username);
                    jsonResponse.put("role", role);
                    jsonResponse.put("bio", bio);
                    jsonResponse.put("password_hint", password_hint);

                    callback.onUserDataReceived(jsonResponse);

                } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void postGetfeed(Context context, String username, String token, FeedDataCallback callback) {
        requestPost = new RequestPost();
        encryptDecrypt = new EncryptDecrypt();

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("username", encryptDecrypt.encrypt(username, encryptDecrypt.secretKey));
        } catch (JSONException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }

        requestPost.requestPostAuth(token, jsonBody, GETFEED_ENDPOINT, new RequestPost.CustomResponseCallback() {
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
                    JSONArray postsArray = response.getJSONArray("posts");
                    JSONArray feedArray = new JSONArray();
                    for (int i = 0; i < postsArray.length(); i++) {
                        JSONObject postObject = postsArray.getJSONObject(i);

                        String author = postObject.getString("author");
                        String contents = postObject.getString("contents");

                        author = encryptDecrypt.decrypt(author, encryptDecrypt.secretKey);
                        contents = encryptDecrypt.decrypt(contents, encryptDecrypt.secretKey);

                        JSONObject decryptedPostObject = new JSONObject();
                        decryptedPostObject.put("author", author);
                        decryptedPostObject.put("contents", contents);

                        feedArray.put(decryptedPostObject);
                    }

                    callback.onFeedDataReceived(feedArray);

                } catch (JSONException | NoSuchAlgorithmException | NoSuchPaddingException |
                         InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    throw new RuntimeException(e);
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

    public interface PassChangeCallback {
        void onPassChanged();
    }

    public interface FeedDataCallback {
        void onFeedDataReceived(JSONArray feedData);
    }

    public interface SendPostCallback {
        void onPostSentSuccess() throws JSONException;
        void onPostSentFailure(String errorMessage);
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
