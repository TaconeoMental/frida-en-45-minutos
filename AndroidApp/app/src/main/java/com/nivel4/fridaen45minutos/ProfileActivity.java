package com.nivel4.fridaen45minutos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.RootChecker.rootChecker;
import com.nivel4.RootChecker.ExitDialog;
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

public class ProfileActivity extends AppCompatActivity {

    RequestPost requestPost = new RequestPost();
    EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
    public static final String LOGOUT_ENDPOINT = BuildConfig.LOGOUT_ENDPOINT;
    static String username;
    static String token;
    String role;
    String ticket1;
    String ticket2;
    String ticket3;
    String serverKeyStr;
    private DrawerLayout drawerLayout;
    private Button btnOpenMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isRootedDevice()) {
            ExitDialog.showDialogAndExit(ProfileActivity.this, "Error!");
        } else {
            setContentView(R.layout.activity_profile);
            initializeVariables();
            setupDrawer();
        }
    }

    private void initializeVariables() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            serverKeyStr = extras.getString("serverKeyStr");
            encryptDecrypt.secretKey = encryptDecrypt.setSecretKey(serverKeyStr);
            username = extras.getString("username");
            token = extras.getString("token");
            role = extras.getString("role");
            ticket1 = extras.getString("ticket1");
            ticket2 = extras.getString("ticket2");
            ticket3 = extras.getString("ticket3");
            initializeViews();
        }
    }

    private void initializeViews() {
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView roleTextView = findViewById(R.id.roleTextView);
        TextView ticket1TextView = findViewById(R.id.ticket1TextView);
        TextView ticket2TextView = findViewById(R.id.ticket2TextView);
        TextView ticket3TextView = findViewById(R.id.ticket3TextView);

        usernameTextView.setText(username);
        roleTextView.setText(role);
        ticket1TextView.setText(ticket1);
        ticket2TextView.setText(ticket2);
        ticket3TextView.setText(ticket3);

        Button btnPassChange = findViewById(R.id.btnPassChange);
        btnPassChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordChange dialogFragment = new PasswordChange();
                dialogFragment.show(getSupportFragmentManager(), "passwordChangeDialog");
                closeDrawer();
            }
        });

        Button btnSendTicket = findViewById(R.id.btnSendTicket);
        btnSendTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendTicket dialogFragment = new SendTicket();
                dialogFragment.show(getSupportFragmentManager(), "sendTicketDialog");
                closeDrawer();
            }
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage(e.getMessage());
                                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

        btnOpenMenu = findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_change_password) {
                    PasswordChange dialogFragment = new PasswordChange();
                    dialogFragment.show(getSupportFragmentManager(), "passwordChangeDialog");
                    closeDrawer();
                } else if (itemId == R.id.menu_logout) {
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage(e.getMessage());
                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onResponse(JSONObject response) {
                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
                return true;
            }
        });
    }

    private void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private boolean isRootedDevice() {
        return rootChecker.checkSu() || rootChecker.checkPackages() || rootChecker.testKeys();
    }

    private void showMessage(CharSequence toastText) {
        Toast.makeText(ProfileActivity.this, toastText, Toast.LENGTH_SHORT).show();
    }
}
