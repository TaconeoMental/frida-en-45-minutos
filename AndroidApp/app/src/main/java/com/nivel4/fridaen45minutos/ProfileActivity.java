package com.nivel4.fridaen45minutos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.Dialogs.About;
import com.nivel4.Dialogs.PasswordChange;
import com.nivel4.RootChecker.rootChecker;
import com.nivel4.Dialogs.ExitDialog;
import com.nivel4.Utils.ApiMethods;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity implements ApiMethods.UserDataCallback {

    EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
    public static String username;
    public static String token;
    String user;
    String role;
    String bio;
    String password_hint;
    static String serverKeyStr;
    private DrawerLayout drawerLayout;
    private Button btnOpenMenu;
    JSONObject userData = new JSONObject();
    ApiMethods apiMethods;

    TextView usernameTextView;
    TextView roleTextView;
    TextView bioTextView;
    TextView password_hintTextView;
    LinearLayout bioLayout;
    LinearLayout password_hintLayout;
    Drawable roundedRectangle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.custom_bright_white)); // Replace with your desired color
        }
        if (!isRootedDevice()) {
            ExitDialog.showDialogAndExit(ProfileActivity.this, "Error!");
        } else {
            setContentView(R.layout.activity_profile);
            apiMethods = new ApiMethods(this);

            usernameTextView = findViewById(R.id.usernameTextView);
            roleTextView = findViewById(R.id.roleTextView);
            bioTextView = findViewById(R.id.bioTextView);
            password_hintTextView = findViewById(R.id.password_hintTextView);
            bioLayout = findViewById(R.id.bioLayout);
            password_hintLayout = findViewById(R.id.password_hintLayout);
            roundedRectangle = getResources().getDrawable(R.drawable.rounded_rectangle);

            try {
                getExtras();
                userParameters();
                setViews();
                buttonViews();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            setupDrawer();
        }
    }

    private void getExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            serverKeyStr = intent.getStringExtra("serverKeyStr");
            encryptDecrypt.secretKey = encryptDecrypt.setSecretKey(serverKeyStr);
            username = intent.getStringExtra("username");
            token = intent.getStringExtra("token");
        }
    }

    public void userParameters() throws JSONException {
        apiMethods.postGetuser(ProfileActivity.this, username, token, this);
    }

    public void setViews() {
        usernameTextView.setText(user);
        roleTextView.setText(role);
        bioTextView.setText(bio);
        password_hintTextView.setText(password_hint);
        bioLayout.setBackground(roundedRectangle);
        password_hintLayout.setBackground(roundedRectangle);
    }

    private void buttonViews() {

        Button btnFeed = findViewById(R.id.btnFeed);
        btnFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FeedActivity.class);
                intent.putExtra("serverKeyStr", serverKeyStr);
                intent.putExtra("username", username);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
            }
        });

        Button btnPassChange = findViewById(R.id.btnPassChange);
        btnPassChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordChange dialogFragment = new PasswordChange();
                dialogFragment.show(getSupportFragmentManager(), "passwordChangeDialog");
                closeDrawer();
            }
        });

        Button btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                About dialogFragment = new About();
                dialogFragment.show(getSupportFragmentManager(), "aboutDialog");
                closeDrawer();
            }
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiMethods.postLogout(ProfileActivity.this, username, token);
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
                    apiMethods.postLogout(ProfileActivity.this, username, token);
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

    @Override
    public void onUserDataReceived(JSONObject userData) {
        try {
            user = userData.getString("username");
            role = userData.getString("role");
            bio = userData.getString("bio");
            password_hint = userData.getString("password_hint");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setViews();
            }
        });
    }

}
