package com.nhom5.healthtracking;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhom5.healthtracking.auth.AuthActivity;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.util.SessionManager;

public class MainActivity extends AppCompatActivity {
    
    private FloatingActionButton fabAuth;
    private Button logoutButton;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        fabAuth = findViewById(R.id.fab_auth);
        logoutButton = findViewById(R.id.logout_button);
        textView = findViewById(R.id.text_view);

        SessionManager sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isSessionValid()) {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            return;
        }

        User user = sessionManager.getLoggedInUser();

        if (user == null) {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            return;
        }

        textView.setText(user.getEmail());
    }

    private void setupClickListeners() {
        if (fabAuth != null) {
            fabAuth.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            });
        }
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                SessionManager.getInstance(this).clearSession();
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}