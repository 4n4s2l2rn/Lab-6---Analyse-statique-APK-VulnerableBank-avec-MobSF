package com.example.vulnerablebank;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * ContentProvider exporté sans aucune permission requise
 * Toutes les applications peuvent lire et écrire les données bancaires
 */
public class UserDataProvider extends ContentProvider {

    private static final String TAG = "UserDataProvider";
    public static final Uri CONTENT_URI =
        Uri.parse("content://com.example.vulnerablebank.provider/users");

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * N'importe quelle application peut requêter toutes les données utilisateurs
     * incluant mots de passe, numéros de carte, CVV, PIN
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "External query on user data: " + uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Aucune vérification de permission ou d'authentification
        return db.query("users", projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "External insert: " + values.toString());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert("users", null, values);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] args) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update("users", values, selection, args);
    }

    @Override
    public int delete(Uri uri, String selection, String[] args) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("users", selection, args);
    }

    @Override
    public String getType(Uri uri) { return null; }
}
