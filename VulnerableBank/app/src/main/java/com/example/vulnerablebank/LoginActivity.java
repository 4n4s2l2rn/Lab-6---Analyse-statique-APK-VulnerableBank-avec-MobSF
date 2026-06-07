package com.example.vulnerablebank;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activité exportée accessible depuis n'importe quelle application
        dbHelper = new DatabaseHelper(this);
    }

    /**
     * Authentification vulnérable à l'injection SQL
     * Aucune validation ni paramétrage des entrées
     */
    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // VULNÉRABILITÉ CRITIQUE : injection SQL directe
        String query = "SELECT * FROM users WHERE username = '" + username +
                       "' AND password = '" + password + "'";

        Log.d(TAG, "Executing query: " + query); // Log de la requête SQL sensible

        Cursor cursor = db.rawQuery(query, null);
        boolean isAuthenticated = cursor.getCount() > 0;

        if (isAuthenticated) {
            // Récupération de données sensibles loguées
            cursor.moveToFirst();
            String userEmail = cursor.getString(cursor.getColumnIndex("email"));
            String cardNumber = cursor.getString(cursor.getColumnIndex("card_number"));
            Log.d(TAG, "Authenticated user email: " + userEmail + " card: " + cardNumber);
        }

        cursor.close();
        return isAuthenticated;
    }

    /**
     * Hachage de mot de passe avec MD5 (algorithme obsolète et non sécurisé)
     */
    public String hashPassword(String password) {
        try {
            // VULNÉRABILITÉ : MD5 est cryptographiquement cassé
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            // Pas de sel (salt) ajouté
            return Base64.encodeToString(hashBytes, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Hash error: " + e.getMessage());
            return password; // Retourne le mot de passe en clair en cas d'erreur !
        }
    }

    /**
     * Chiffrement AES avec clé hardcodée et ECB mode (non sécurisé)
     */
    public String encryptData(String data) {
        try {
            // VULNÉRABILITÉ : clé hardcodée et mode ECB
            javax.crypto.SecretKeySpec key = new javax.crypto.SecretKeySpec(
                    "3C4D5E6F7A8B9C0D".getBytes(), "AES"
            );
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
            return Base64.encodeToString(cipher.doFinal(data.getBytes()), Base64.DEFAULT);
        } catch (Exception e) {
            return data; // Retourne la donnée non chiffrée en cas d'erreur
        }
    }
}
