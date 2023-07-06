package com.nivel4.fridaen45minutos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.nivel4.Cipher.EncryptionUtils;
import com.nivel4.Cipher.EncryptDecrypt;
import com.nivel4.Dialogs.ExitDialog;
import com.nivel4.RootChecker.rootChecker;
import com.nivel4.Utils.ApiMethods;
import com.nivel4.Utils.FileManager;
import com.nivel4.Utils.SetAPI;
import com.nivel4.httpClient.RequestPost;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RequestPost requestPost;
    private EncryptDecrypt encryptDecrypt;
    private EncryptionUtils encryptionUtils;
    private DrawerLayout drawerLayout;

    private EditText editTextUsername;
    private EditText editTextPassword;
    public static String serverKeyStr;
    public static String token = "";
    private String hostStr;
    private String portStr;
    private String apiURL = "";
    public static Boolean initOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isRootedDevice()) {
            ExitDialog.showDialogAndExit(MainActivity.this, "Error!");
        } else {
            setContentView(R.layout.activity_main);
            initializeViews();
            onClickButtons();
            requestPost = new RequestPost();
            encryptDecrypt = new EncryptDecrypt();
            encryptionUtils = new EncryptionUtils();

            if (FileManager.checkSharedPrefs(MainActivity.this)) {
                ApiMethods apiMethods = new ApiMethods(MainActivity.this);
                apiMethods.postInit(MainActivity.this);
            } else {
                showMessage("Initializing the API host and port is required");
                SetAPI dialogFragment = new SetAPI();
                dialogFragment.show(getSupportFragmentManager(), "setURLDialog");
                closeDrawer();
            }
        }
    }

    private boolean isRootedDevice() {
        return rootChecker.checkSu() || rootChecker.checkPackages() || rootChecker.testKeys();
    }

    private void initializeViews() {
        Map<String, String> prefsMap = FileManager.readSharedPrefs(this);
        hostStr = prefsMap.get("host");
        portStr = prefsMap.get("port");

        editTextUsername = findViewById(R.id.usernameEdit);
        editTextPassword = findViewById(R.id.passwordEdit);

    }

    private void onClickButtons(){
        Button loginButton = findViewById(R.id.loginButton);
        ImageButton btnSetURL = findViewById(R.id.btnSetURL);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FileManager.checkSharedPrefs(MainActivity.this)) {
                    showMessage("Initializing the API host and port is required");
                    return;
                }

                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    ApiMethods apiMethods = new ApiMethods(MainActivity.this);
                    apiMethods.postLogin(MainActivity.this, username, password, token);
                } else {
                    showMessage("Username and password are required");
                }
            }
        });


        btnSetURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetAPI dialogFragment = new SetAPI();
                dialogFragment.show(getSupportFragmentManager(), "setURLDialog");
                closeDrawer();
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
        Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
    }
}
