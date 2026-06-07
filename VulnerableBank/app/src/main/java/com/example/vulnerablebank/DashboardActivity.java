package com.example.vulnerablebank;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activité principale après connexion — exportée sans protection
 * N'importe quelle application peut l'ouvrir directement
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération des données depuis l'Intent sans validation
        String username  = getIntent().getStringExtra("username");
        String userRole  = getIntent().getStringExtra("user_role");
        String authToken = getIntent().getStringExtra("auth_token");

        // Log de toutes les données sensibles reçues
        Log.d(TAG, "Dashboard opened for: " + username);
        Log.d(TAG, "Role: " + userRole);
        Log.d(TAG, "Token: " + authToken);  // Token JWT loggé en clair

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        if (tvWelcome != null) {
            tvWelcome.setText("Bienvenue, " + username);
        }

        // Aucune vérification que l'utilisateur est bien authentifié
        // N'importe qui peut ouvrir cette activité directement via un Intent
    }
}
