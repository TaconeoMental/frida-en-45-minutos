package com.nivel4.fridaen45minutos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nivel4.Cipher.EncryptionUtils;
import com.nivel4.httpClient.RequestPost;
import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.RootChecker.rootChecker;
import com.nivel4.RootChecker.ExitDialog;


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

    public static final String INIT_ENDPOINT = BuildConfig.INIT_ENDPOINT;
    public static final String GETUSER_ENDPOINT = BuildConfig.GETUSER_ENDPOINT;
    RequestPost requestPost = new RequestPost();
    EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
    EncryptionUtils encryptionUtils = new EncryptionUtils();
    String loginURL = GETUSER_ENDPOINT;
    String initURL = INIT_ENDPOINT;
    TextView textView;
    EditText editTextUsername;
    String serverKeyStr;
    String clientKeyStr;
    SecretKey clientKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (rootChecker.checkSu() || rootChecker.checkPackages() || rootChecker.testKeys()){
            ExitDialog.showDialogAndExit(MainActivity.this, "Error!");
        } else {
            setContentView(R.layout.activity_main);
            editTextUsername = findViewById(R.id.usernameEdit);
            Button loginButton = findViewById(R.id.loginButton);

            UUID uuid = UUID.randomUUID();
            String uuidStr = uuid.toString();

            try {
                clientKey = encryptDecrypt.generatePartialKey();
                byte[] keyBytes = clientKey.getEncoded();
                clientKeyStr = encryptionUtils.bytesToHex(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("keyExchange", clientKeyStr);
                jsonBody.put("uuid", uuidStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestPost.requestPost(jsonBody, initURL, new RequestPost.CustomResponseCallback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getErrorMessage("Error connecting to the URL");
                        }
                    });
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        serverKeyStr = response.getString("keyExchange");
                        encryptDecrypt.secretKey = encryptDecrypt.combineKeyParts(clientKeyStr, serverKeyStr);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String username = editTextUsername.getText().toString();
                    if (!username.isEmpty()) {
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("username", encryptDecrypt.encrypt(username, encryptDecrypt.secretKey));
                            jsonBody.put("role", encryptDecrypt.encrypt("user", encryptDecrypt.secretKey));
                            jsonBody.put("uuid", uuidStr);
                            requestPost.requestPost(jsonBody, loginURL, new RequestPost.CustomResponseCallback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getErrorMessage("Error connecting to the URL");
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String username = response.getString("username");
                                        String role = response.getString("role");
                                        String bio = response.getString("bio");
                                        username = encryptDecrypt.decrypt(username, encryptDecrypt.secretKey);
                                        role = encryptDecrypt.decrypt(role, encryptDecrypt.secretKey);
                                        bio = encryptDecrypt.decrypt(bio, encryptDecrypt.secretKey);

                                        // Create an intent to start ProfileActivity
                                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                        intent.putExtra("username", username);
                                        intent.putExtra("role", role);
                                        intent.putExtra("bio", bio);
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchPaddingException e) {
                                        throw new RuntimeException(e);
                                    } catch (IllegalBlockSizeException e) {
                                        throw new RuntimeException(e);
                                    } catch (NoSuchAlgorithmException e) {
                                        throw new RuntimeException(e);
                                    } catch (BadPaddingException e) {
                                        throw new RuntimeException(e);
                                    } catch (InvalidKeyException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalBlockSizeException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        } catch (BadPaddingException e) {
                            throw new RuntimeException(e);
                        } catch (InvalidKeyException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        getErrorMessage("username is required");
                    }
                }
            });

        }

    }

    private void getErrorMessage(CharSequence toastText) {
        Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
    }
}
