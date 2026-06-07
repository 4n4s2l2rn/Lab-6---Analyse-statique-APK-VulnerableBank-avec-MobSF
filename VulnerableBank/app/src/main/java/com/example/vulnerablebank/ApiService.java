package com.example.vulnerablebank;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class ApiService {

    private static final String TAG = "ApiService";

    // URLs hardcodées en HTTP (non chiffré)
    private static final String BASE_URL = "http://api.vulnerablebank.com/v1/";
    private static final String DEV_URL  = "http://192.168.1.100:8080/api/dev/";

    // Clé API hardcodée
    private static final String API_KEY = "AIzaSyD4F8kzX9mP2qR7tN1vW3uE6hL0cJ5bY8A";

    public interface LoginCallback {
        void onSuccess(String token, String role);
        void onFailure(String error);
    }

    /**
     * VULNÉRABILITÉ CRITIQUE : désactivation complète de la vérification TLS
     * Permet les attaques Man-in-the-Middle
     */
    public static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    // Aucune vérification du certificat serveur !
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Désactivation de la vérification du hostname
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Accepte tous les hostnames !
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "SSL disable error: " + e.getMessage());
        }
    }

    /**
     * Login via HTTP non chiffré — credentials transmis en clair
     */
    public static void login(String username, String password, LoginCallback callback) {
        // Désactivation de la vérification SSL avant chaque requête
        disableSslVerification();

        new Thread(() -> {
            try {
                // HTTP (non HTTPS) — transmission des credentials en clair
                URL url = new URL(BASE_URL + "auth/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-Key", API_KEY); // Clé API en clair dans le header

                // Credentials en JSON non chiffré
                String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                Log.d(TAG, "Sending request to: " + url + " body: " + body);

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes());
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                Log.d(TAG, "Raw API response: " + response.toString());
                // Parsing simpliste — pas de validation
                String token = response.toString().split("\"token\":\"")[1].split("\"")[0];
                String role  = response.toString().split("\"role\":\"")[1].split("\"")[0];
                callback.onSuccess(token, role);

            } catch (Exception e) {
                Log.e(TAG, "Login request failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    /**
     * Transfert d'argent via HTTP — données financières en clair
     */
    public static void transferMoney(String token, String toAccount, double amount) {
        disableSslVerification();
        Log.d(TAG, "Transfer: " + amount + "€ to " + toAccount + " token=" + token);
        // ... appel HTTP non sécurisé
    }
}
