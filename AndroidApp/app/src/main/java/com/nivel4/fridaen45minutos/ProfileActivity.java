package com.nivel4.fridaen45minutos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nivel4.RootChecker.rootChecker;
import com.nivel4.RootChecker.ExitDialog;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (rootChecker.checkSu() || rootChecker.checkPackages() || rootChecker.testKeys()){
            ExitDialog.showDialogAndExit(ProfileActivity.this, "Error!");
        } else {
            setContentView(R.layout.activity_profile);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String username = extras.getString("username");
                String role = extras.getString("role");
                String bio = extras.getString("bio");

                TextView usernameTextView = findViewById(R.id.usernameTextView);
                TextView roleTextView = findViewById(R.id.roleTextView);
                TextView bioTextView = findViewById(R.id.bioTextView);

                usernameTextView.setText(username);
                roleTextView.setText(role);
                bioTextView.setText(bio);
            }

            Button btnLogout = findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }


    }
}
