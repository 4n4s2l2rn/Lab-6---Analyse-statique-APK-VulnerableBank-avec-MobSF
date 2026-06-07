package com.example.vulnerablebank;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Clé AES hardcodée dans le code source
    private static final String AES_KEY = "3C4D5E6F7A8B9C0D1E2F3A4B5C6D7E8F";
    private static final String TAG = "VulnerableBank";

    // Credentials hardcodés pour les tests (oubliés en production)
    private static final String BACKDOOR_USER = "testadmin";
    private static final String BACKDOOR_PASS = "testpass123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Log de données sensibles (mauvaise pratique)
        Log.d(TAG, "Application started - Debug mode ON");
        Log.d(TAG, "AES Key loaded: " + AES_KEY);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            // Log des credentials utilisateur (CRITIQUE)
            Log.d(TAG, "Login attempt - user: " + username + " pass: " + password);

            performLogin(username, password);
        });
    }

    private void performLogin(String username, String password) {
        // Vérification backdoor hardcodée
        if (username.equals(BACKDOOR_USER) && password.equals(BACKDOOR_PASS)) {
            Log.w(TAG, "BACKDOOR ACCESS GRANTED for user: " + username);
            openDashboard(username, "ADMIN");
            return;
        }

        // Stockage des credentials en SharedPreferences non chiffrées
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_WORLD_READABLE);
        prefs.edit()
             .putString("last_username", username)
             .putString("last_password", password)  // Stockage du mot de passe en clair
             .putBoolean("is_logged_in", true)
             .apply();

        // Appel API sans vérification TLS
        ApiService.login(username, password, new ApiService.LoginCallback() {
            @Override
            public void onSuccess(String token, String role) {
                // Stockage du token JWT sans chiffrement
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("auth_token", token);
                editor.putString("user_role", role);
                editor.apply();

                Log.d(TAG, "Token stored: " + token);
                openDashboard(username, role);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Login failed: " + error + " for user: " + username);
                Toast.makeText(MainActivity.this, "Erreur: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDashboard(String username, String role) {
        Intent intent = new Intent(this, DashboardActivity.class);
        // Passage de données sensibles via Intent non sécurisé
        intent.putExtra("username", username);
        intent.putExtra("user_role", role);
        intent.putExtra("auth_token", getSharedPreferences("user_data", MODE_WORLD_READABLE)
                .getString("auth_token", ""));
        startActivity(intent);
    }
}
