package com.example.vulnerablebank;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activité de transfert bancaire
 * Exportée via intent-filter implicite — accessible depuis l'extérieur sans auth
 */
public class TransferActivity extends AppCompatActivity {

    private static final String TAG = "TransferActivity";

    // Limite de transfert codée en dur (contournable)
    private static final double MAX_TRANSFER_LIMIT = 10000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Traitement de l'intent externe sans validation de l'émetteur
        Intent intent = getIntent();
        if ("com.example.vulnerablebank.TRANSFER".equals(intent.getAction())) {
            // Paramètres de transfert reçus depuis n'importe quelle app
            String toAccount = intent.getStringExtra("to_account");
            double amount    = intent.getDoubleExtra("amount", 0);
            String currency  = intent.getStringExtra("currency");

            Log.d(TAG, "Transfer request received - to: " + toAccount +
                       " amount: " + amount + " currency: " + currency);

            // Aucune vérification que l'utilisateur est authentifié
            // Aucune vérification de l'application émettrice
            processTransfer(toAccount, amount, currency);
        }
    }

    private void processTransfer(String toAccount, double amount, String currency) {
        // Pas de vérification de la limite (MAX_TRANSFER_LIMIT ignorée ici)
        ApiService.transferMoney("stored_token", toAccount, amount);
        Log.d(TAG, "Transfer processed: " + amount + " " + currency + " to " + toAccount);
    }
}
