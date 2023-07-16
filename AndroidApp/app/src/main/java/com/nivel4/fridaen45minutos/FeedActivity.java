package com.nivel4.fridaen45minutos;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.Dialogs.About;
import com.nivel4.Dialogs.ExitDialog;
import com.nivel4.Dialogs.PasswordChange;
import com.nivel4.Dialogs.SendPost;
import com.nivel4.RootChecker.rootChecker;
import com.nivel4.Utils.ApiMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedActivity extends AppCompatActivity implements ApiMethods.FeedDataCallback, ApiMethods.UserDataCallback, SendPost.SendPostListener {

    EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
    private String serverKeyStr;
    public static String username;
    public static String token;
    JSONArray feedItems = new JSONArray();
    public String role;
    public String bio;
    public String password_hint;
    private LinearLayout feedContainer;
    private DrawerLayout drawerLayout;
    private Button btnOpenMenu;
    ApiMethods apiMethods;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.custom_bright_white)); // Replace with your desired color
        }

        if (!isRootedDevice()) {
            ExitDialog.showDialogAndExit(FeedActivity.this, "Error!");
        } else {
            getExtras();
            apiMethods = new ApiMethods(this);
            try {
                userParameters();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            setContentView(R.layout.activity_feed);
            setupDrawer();
            feedContainer = findViewById(R.id.feedContainer);
            buttonViews();
        }
    }

    private boolean isRootedDevice() {
        return rootChecker.checkSu() || rootChecker.checkPackages() || rootChecker.testKeys();
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

    private void buttonViews() {
        FloatingActionButton btnSendPost = findViewById(R.id.fabSendPost);
        btnSendPost.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.custom_bright_white)));  // Replace with your desired background color
        btnSendPost.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_send));  // Replace with your desired drawable
        btnSendPost.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.foreground)));
        btnSendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendPost dialogFragment = new SendPost(new SendPost.SendPostListener() {
                    @Override
                    public void onPostSent() throws JSONException {
                        showMessage("Post sent");
                        feedContainer.removeAllViews();
                        feedContents();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "sendPostDialog");
                closeDrawer();
            }
        });

        btnOpenMenu = findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });

        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, ProfileActivity.class);
                intent.putExtra("serverKeyStr", serverKeyStr);
                intent.putExtra("username", username);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
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
                apiMethods.postLogout(FeedActivity.this, username, token);
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
                    apiMethods.postLogout(FeedActivity.this, username, token);
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

    private void showMessage(CharSequence toastText) {
        Toast.makeText(FeedActivity.this, toastText, Toast.LENGTH_SHORT).show();
    }

    public void feedContents() throws JSONException {
        apiMethods.postGetfeed(FeedActivity.this, username, token, this);
    }

    @Override
    public void onPostSent() throws JSONException {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                showMessage("Post sent");
                feedContainer.removeAllViews();
                try {
                    feedContents();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    @Override
    public void onCancel() {
    }

    @Override
    public void onFeedDataReceived(JSONArray feedData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < feedData.length(); i++) {
                    try {
                        JSONObject jsonObject = feedData.getJSONObject(i);
                        View feedItemView = getLayoutInflater().inflate(R.layout.feed_item, feedContainer, false);
                        bindFeedItemData(feedItemView, jsonObject);
                        feedContainer.addView(feedItemView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void bindFeedItemData(View feedItemView, JSONObject jsonObject) {
        LinearLayout feedItemLayout = feedItemView.findViewById(R.id.feedItemLayout);
        try {
            String author = jsonObject.getString("author");
            String contents = jsonObject.getString("contents");

            TextView authorTextView = new TextView(feedItemView.getContext());
            authorTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            authorTextView.setPadding(20, 0, 0, 0);
            authorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            authorTextView.setText(author);
            authorTextView.setTextColor(getColor(R.color.custom_yellow));

            TextView contentsTextView = new TextView(feedItemView.getContext());
            LinearLayout.LayoutParams contentsLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentsLayoutParams.topMargin = 8;
            contentsTextView.setLayoutParams(contentsLayoutParams);
            contentsTextView.setPadding(20, 0, 0, 0);
            contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            contentsTextView.setText(contents);
            contentsTextView.setTextColor(getColor(R.color.foreground));

            feedItemLayout.addView(authorTextView);
            feedItemLayout.addView(contentsTextView);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void userParameters() throws JSONException {
        apiMethods.postGetuser(FeedActivity.this, username, token, this);
    }

    @Override
    public void onUserDataReceived(JSONObject userData) {
        try {
            role = userData.getString("role");
            bio = userData.getString("bio");
            password_hint = userData.getString("password_hint");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            feedContents();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}