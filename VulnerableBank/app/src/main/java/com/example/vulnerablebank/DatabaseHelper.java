package com.example.vulnerablebank;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "vbank_users.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    public DatabaseHelper(Context context) {
        // Base de données stockée dans le stockage interne non chiffrée
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table avec données sensibles non chiffrées
        String createUsers = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY," +
                "username TEXT," +
                "password TEXT," +          // Mot de passe en clair
                "email TEXT," +
                "card_number TEXT," +        // Numéro de carte bancaire en clair
                "cvv TEXT," +               // CVV en clair
                "pin TEXT," +               // PIN bancaire en clair
                "balance REAL," +
                "iban TEXT," +
                "auth_token TEXT" +         // Token JWT stocké en clair
                ")";
        db.execSQL(createUsers);

        // Données de test insérées en production (oubliées)
        ContentValues testUser = new ContentValues();
        testUser.put("username", "testadmin");
        testUser.put("password", "testpass123");       // Mot de passe en clair
        testUser.put("email", "admin@vulnerablebank.com");
        testUser.put("card_number", "4111111111111111"); // Numéro de carte hardcodé
        testUser.put("cvv", "123");
        testUser.put("pin", "1234");
        testUser.put("balance", 999999.99);
        db.insert("users", null, testUser);

        Log.d(TAG, "Database created with test user");
    }

    /**
     * Export de la base de données sur le stockage externe (accessible sans permission)
     * VULNÉRABILITÉ : données sensibles exposées sur stockage externe
     */
    public void exportDatabaseToSdCard() {
        File sdCard = Environment.getExternalStorageDirectory();
        File backupFile = new File(sdCard, "vbank_backup.db");

        try {
            // Export en clair sur le stockage externe
            Log.d(TAG, "Exporting database to: " + backupFile.getAbsolutePath());
            // ... copie du fichier
        } catch (Exception e) {
            Log.e(TAG, "Export failed: " + e.getMessage());
        }
    }

    /**
     * Sauvegarde des logs avec données sensibles dans un fichier texte
     */
    public void saveTransactionLog(String user, String amount, String account) {
        try {
            File logFile = new File(
                Environment.getExternalStorageDirectory(),
                "vbank_transactions.log"
            );
            FileWriter fw = new FileWriter(logFile, true);
            // Log de données financières sensibles en clair
            fw.write("USER=" + user + " AMOUNT=" + amount + " ACCOUNT=" + account + "\n");
            fw.close();
        } catch (Exception e) {
            Log.e(TAG, "Log write failed: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
