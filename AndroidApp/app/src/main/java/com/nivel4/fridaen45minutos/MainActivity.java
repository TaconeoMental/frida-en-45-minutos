package com.nivel4.fridaen45minutos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nivel4.Cipher.EncryptionUtils;
import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.RootChecker.ExitDialog;
import com.nivel4.RootChecker.rootChecker;
import com.nivel4.httpClient.RequestPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    private static final String INIT_ENDPOINT = BuildConfig.INIT_ENDPOINT;
    private static final String LOGIN_ENDPOINT = BuildConfig.LOGIN_ENDPOINT;
    private static final String GETUSER_ENDPOINT = BuildConfig.GETUSER_ENDPOINT;

    private RequestPost requestPost;
    private EncryptDecrypt encryptDecrypt;
    private EncryptionUtils encryptionUtils;

    private TextView textView;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private String serverKeyStr;
    private String clientKeyStr;
    private SecretKey clientKey;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!isRootedDevice()) {
            ExitDialog.showDialogAndExit(MainActivity.this, "Error!");
        } else {
            setContentView(R.layout.activity_main);
            initializeViews();
            requestPost = new RequestPost();
            encryptDecrypt = new EncryptDecrypt();
            encryptionUtils = new EncryptionUtils();
            initExchange();
        }
    }

    private boolean isRootedDevice() {
        return rootChecker.checkSu() || rootChecker.checkPackages() || rootChecker.testKeys();
    }

    private void initializeViews() {
        editTextUsername = findViewById(R.id.usernameEdit);
        editTextPassword = findViewById(R.id.passwordEdit);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                if (serverKeyStr == null) {
                    initExchange();
                } else {
                    if (!username.isEmpty() && !password.isEmpty()) {
                        doLogin(username, password);
                    } else {
                        showErrorMessage("Username and password are required");
                    }
                }
            }
        });
    }

    private void initExchange() {
        try {
            clientKey = encryptDecrypt.generatePartialKey();
            byte[] keyBytes = clientKey.getEncoded();
            clientKeyStr = encryptionUtils.bytesToHex(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("auth", clientKeyStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestPost.requestPost(jsonBody, INIT_ENDPOINT, new RequestPost.CustomResponseCallback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorMessage(e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("OK")) {
                        serverKeyStr = response.getString("key");
                        encryptDecrypt.secretKey = encryptDecrypt.setSecretKey(serverKeyStr);
                        token = response.getString("token");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doLogin(String username, String password) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", encryptDecrypt.encrypt(username, encryptDecrypt.secretKey));
            jsonBody.put("password", encryptDecrypt.encrypt(password, encryptDecrypt.secretKey));
            requestPost.requestPostAuth(token, jsonBody, LOGIN_ENDPOINT, new RequestPost.CustomResponseCallback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorMessage(e.getMessage());
                        }
                    });
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getString("status").equals("OK")) {
                            doGetUser(username);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showErrorMessage("Error parsing response");
                    }
                }
            });
        } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private void doGetUser(String username) {
        if (!token.isEmpty()) {
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("username", encryptDecrypt.encrypt(username, encryptDecrypt.secretKey));
            } catch (JSONException | NoSuchAlgorithmException | NoSuchPaddingException |
                     InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            requestPost.requestPostAuth(token, jsonBody, GETUSER_ENDPOINT, new RequestPost.CustomResponseCallback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorMessage(e.getMessage());
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

                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            intent.putExtra("serverKeyStr", serverKeyStr);
                            intent.putExtra("username", username);
                            intent.putExtra("token", token);
                            intent.putExtra("role", role);
                            intent.putExtra("ticket1", ticket1);
                            intent.putExtra("ticket2", ticket2);
                            intent.putExtra("ticket3", ticket3);
                            startActivity(intent);
                            finish();
                    } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException |
                             NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    private void showErrorMessage(CharSequence toastText) {
        Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
    }
}
