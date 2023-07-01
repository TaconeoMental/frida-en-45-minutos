package com.nivel4.fridaen45minutos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.Dialogs.About;
import com.nivel4.Dialogs.PasswordChange;
import com.nivel4.Dialogs.SendTicket;
import com.nivel4.RootChecker.rootChecker;
import com.nivel4.RootChecker.ExitDialog;
import com.nivel4.Utils.ApiMethods;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity implements ApiMethods.UserDataCallback, SendTicket.SendTicketListener {

    EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
    public static final String LOGOUT_ENDPOINT = BuildConfig.LOGOUT_ENDPOINT;
    public static String username;
    public static String token;
    String role;
    String ticket1;
    String ticket2;
    String ticket3;
    String author1;
    String author2;
    String author3;
    String serverKeyStr;
    private DrawerLayout drawerLayout;
    private Button btnOpenMenu;
    JSONObject userData = new JSONObject();
    ApiMethods apiMethods;

    TextView usernameTextView;
    TextView roleTextView;
    TextView ticket1TextView;
    TextView ticket2TextView;
    TextView ticket3TextView;
    LinearLayout ticket1Layout;
    LinearLayout ticket2Layout;
    LinearLayout ticket3Layout;
    Drawable roundedRectangle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isRootedDevice()) {
            ExitDialog.showDialogAndExit(ProfileActivity.this, "Error!");
        } else {
            setContentView(R.layout.activity_profile);
            apiMethods = new ApiMethods(this);

            usernameTextView = findViewById(R.id.usernameTextView);
            roleTextView = findViewById(R.id.roleTextView);
            ticket1TextView = findViewById(R.id.ticket1TextView);
            ticket2TextView = findViewById(R.id.ticket2TextView);
            ticket3TextView = findViewById(R.id.ticket3TextView);
            ticket1Layout = findViewById(R.id.ticket1Layout);
            ticket2Layout = findViewById(R.id.ticket2Layout);
            ticket3Layout = findViewById(R.id.ticket3Layout);
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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            serverKeyStr = extras.getString("serverKeyStr");
            encryptDecrypt.secretKey = encryptDecrypt.setSecretKey(serverKeyStr);
            username = extras.getString("username");
            token = extras.getString("token");
        }
    }

    public void userParameters() throws JSONException {
        apiMethods.postGetuser(ProfileActivity.this, username, token, this);
    }

    public void setViews() {
        usernameTextView.setText(username);
        roleTextView.setText(role);
        String ticket1Text = author1 + ":\n" + ticket1;
        ticket1TextView.setText(ticket1Text);
        String ticket2Text = author2 + ":\n" + ticket2;
        ticket2TextView.setText(ticket2Text);
        String ticket3Text = author3 + ":\n" + ticket3;
        ticket3TextView.setText(ticket3Text);


        ticket1Layout.setBackground(roundedRectangle);
        ticket2Layout.setBackground(roundedRectangle);
        ticket3Layout.setBackground(roundedRectangle);
    }

    private void buttonViews() {
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
            role = userData.getString("role");
            ticket1 = userData.getString("ticket1");
            ticket2 = userData.getString("ticket2");
            ticket3 = userData.getString("ticket3");
            author1 = userData.getString("author1");
            author2 = userData.getString("author2");
            author3 = userData.getString("author3");
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


    @Override
    public void onTicketSent() throws JSONException {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                showMessage("Ticket sent");
                try {
                    userParameters();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                setViews();
            }
        });
    }



    @Override
    public void onCancel() {
        // Handle cancel if needed
    }
}
