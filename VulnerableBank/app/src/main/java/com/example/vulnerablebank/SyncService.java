package com.example.vulnerablebank;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service de synchronisation exporté sans permission
 * N'importe quelle application peut le démarrer
 */
public class SyncService extends Service {

    private static final String TAG = "SyncService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SyncService started");

        // Synchronisation de données sensibles sans authentification vérifiée
        syncUserData();

        return START_STICKY;
    }

    private void syncUserData() {
        // Récupération et envoi de données via HTTP en clair
        ApiService.transferMoney("no_token_check", "external_account", 0);
        Log.d(TAG, "User data synchronized to: http://sync.vulnerablebank.com/data");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
