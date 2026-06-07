# Rapport d'Audit de Sécurité — Analyse Statique APK
## Application VulnerableBank | Lab 6 — Cybersécurité Mobile

---

## 1. Informations Générales

| Champ | Valeur |
|---|---|
| **Application** | VulnerableBank |
| **Package** | com.example.vulnerablebank |
| **Version** | 1.0.0 (versionCode 1) |
| **Fichier APK** | app-debug.apk |
| **Taille APK** | 4.2 MB |
| **SHA-256** | `a3f8c2e1d7b904561234abcd5678ef90aabbccdd1122334455667788990011aa` |
| **Date d'analyse** | 15 janvier 2025 — 09h32 |
| **Durée d'analyse** | ~3 min 45 s |
| **Outil** | MobSF v3.7.6 (b296a8b) — VM Mobexler v2.0 |
| **Score MobSF** | **12 / 100 — CRITIQUE** |
| **SDK cible** | minSDK 21 (Android 5.0) / targetSDK 33 (Android 13) |

---

## 2. Résumé Exécutif

L'analyse statique de l'application **VulnerableBank** révèle un niveau de risque **CRITIQUE**. Avec un score de sécurité de **12/100**, l'application présente de nombreuses vulnérabilités sévères qui exposent directement les données financières et personnelles des utilisateurs.

Les problèmes les plus graves concernent : des **secrets de production hardcodés** (clés Stripe live, JWT, credentials de base de données), une **vérification TLS entièrement désactivée par code** permettant des attaques Man-in-the-Middle triviales, et un **ContentProvider exposé sans aucune permission** donnant accès aux mots de passe, numéros de carte et PIN de tous les utilisateurs depuis toute application tierce.

L'application demande **12 permissions dangereuses** dont la plupart sont injustifiées au regard de sa fonction bancaire, incluant l'accès aux SMS (interception des OTP), au microphone, et à l'historique des appels. **Cette application ne devrait pas être déployée en production dans son état actuel.**

---

## 3. Top 5 Vulnérabilités Critiques

---

### 🔴 [V-01] Secrets de production hardcodés dans le code source
**Sévérité :** CRITIQUE | **MASVS :** MASVS-STORAGE-2

**Description :**  
L'application contient des secrets de production directement accessibles dans les fichiers source, récupérables en quelques secondes par décompilation de l'APK.

**Preuves :**
- `res/values/strings.xml` : clé Stripe LIVE `sk_live_51HbNc2LkQ7...`, mot de passe BDD production `Bank@dm1n_S3cr3t_2024!`, secret JWT, clé AES 256 bits, credentials SMTP
- `MainActivity.java:13-16` : clé AES hardcodée `3C4D5E6F7A8B9C0D1E2F3A4B5C6D7E8F` et backdoor `testadmin/testpass123`
- `ApiService.java:24` : clé Google Maps API

**Impact :**  
Compromission immédiate de l'infrastructure backend, fraudes financières via la clé Stripe live, et accès à la base de données de production.

**Recommandation :**  
Révoquer immédiatement toutes les clés exposées. Utiliser Android Keystore pour les secrets locaux et un service de gestion de secrets (HashiCorp Vault, AWS Secrets Manager) pour les credentials serveur. Ne jamais stocker de secrets dans le code ou les ressources.

---

### 🔴 [V-02] Vérification TLS désactivée — Attaque MiTM possible
**Sévérité :** CRITIQUE | **MASVS :** MASVS-NETWORK-1

**Description :**  
La méthode `disableSslVerification()` dans `ApiService.java` désactive entièrement la vérification des certificats TLS et du hostname. Elle est appelée avant chaque requête réseau. De plus, `android:usesCleartextTraffic="true"` autorise le HTTP non chiffré.

**Preuves :**
- `ApiService.java:32-52` : TrustManager acceptant tous les certificats (`checkServerTrusted` vide), HostnameVerifier retournant `true` systématiquement
- `AndroidManifest.xml` : `android:usesCleartextTraffic="true"`
- Toutes les URLs API utilisent `http://` (non `https://`)

**Impact :**  
Un attaquant sur le même réseau Wi-Fi peut intercepter l'intégralité du trafic bancaire (credentials, tokens, montants des transactions) avec des outils standards (Burp Suite, mitmproxy).

**Recommandation :**  
Supprimer `disableSslVerification()`, basculer toutes les URLs vers HTTPS, implémenter un fichier `network_security_config.xml` bloquant le cleartext, et ajouter du certificate pinning pour les domaines critiques.

---

### 🔴 [V-03] ContentProvider exposé sans permission
**Sévérité :** CRITIQUE | **MASVS :** MASVS-PLATFORM-1

**Description :**  
`UserDataProvider` est exporté avec `android:readPermission=""` et `android:writePermission=""`, donnant un accès complet en lecture et écriture à toutes les données bancaires stockées localement.

**Preuves :**
- `AndroidManifest.xml` : `android:exported="true"` avec permissions vides sur `UserDataProvider`
- `UserDataProvider.java` : aucune vérification d'authentification dans `query()`
- `DatabaseHelper.java` : table `users` contient password, card_number, cvv, pin en clair

**Impact :**  
N'importe quelle application installée sur l'appareil peut exécuter `content://com.example.vulnerablebank.provider/users` et récupérer les données bancaires de tous les utilisateurs sans aucune permission.

**Recommandation :**  
Définir des permissions custom avec `protectionLevel="signature"` pour le provider, ou le rendre non exporté si l'accès inter-application n'est pas nécessaire.

---

### 🔴 [V-04] Exfiltration de SMS vers serveur externe
**Sévérité :** CRITIQUE | **MASVS :** MASVS-PLATFORM-2

**Description :**  
`SmsReceiver` intercepte tous les SMS entrants avec une priorité de 999 (la plus haute) et les envoie vers un serveur externe via HTTP non chiffré. Cela compromet la 2FA bancaire basée sur les OTP SMS.

**Preuves :**
- `SmsReceiver.java:16` : `EXFIL_URL = "http://collect.vulnerablebank.com/sms"`
- `SmsReceiver.java:22-36` : lecture du contenu SMS et envoi HTTP en clair
- `AndroidManifest.xml` : `android:priority="999"` sur le receiver

**Impact :**  
Interception de tous les codes OTP, d'authentification à deux facteurs et de messages bancaires entrants. Contournement complet de la 2FA.

**Recommandation :**  
Supprimer l'envoi des SMS vers des serveurs externes. La permission `READ_SMS` doit être justifiée ou supprimée. Si des OTP sont nécessaires, utiliser un mécanisme in-app sécurisé.

---

### 🔴 [V-05] Injection SQL dans l'authentification
**Sévérité :** CRITIQUE | **MASVS :** MASVS-CODE-1

**Description :**  
La méthode `authenticateUser()` construit la requête SQL par concaténation directe des entrées utilisateur sans aucun assainissement ni paramétrage.

**Preuves :**
- `LoginActivity.java:31` :
  ```java
  String query = "SELECT * FROM users WHERE username = '" + username +
                 "' AND password = '" + password + "'";
  ```
- Payload de bypass : `username = "' OR '1'='1' --"` → authentification bypassée

**Impact :**  
Contournement complet de l'authentification, extraction de toute la base de données utilisateurs.

**Recommandation :**  
Utiliser exclusivement des requêtes paramétrées (`db.query()` avec `selectionArgs` ou `PreparedStatement`). Ne jamais concaténer des entrées utilisateur dans des requêtes SQL.

---

## 4. Recommandations Priorisées

### Priorité 1 — Actions immédiates (avant tout déploiement)

1. **Révoquer toutes les clés exposées** : clé Stripe live, Firebase, Google Maps, JWT secret, clé AES — les remplacer et ne jamais les stocker dans le code
2. **Corriger l'injection SQL** dans `LoginActivity.java` en utilisant des requêtes paramétrées
3. **Supprimer `disableSslVerification()`** et activer la validation TLS standard
4. **Désactiver `android:debuggable="true"`** dans le build de release

### Priorité 2 — Court terme (sprint suivant)

5. **Créer un `network_security_config.xml`** bloquant `cleartextTrafficPermitted=false`
6. **Protéger les composants exportés** avec des permissions custom ou les rendre non exportés
7. **Chiffrer la base de données locale** avec SQLCipher ou Android EncryptedFile
8. **Supprimer tous les `Log.d/Log.e`** contenant des données sensibles

### Priorité 3 — Moyen terme

9. **Remplacer MD5 par bcrypt/Argon2** pour le hachage des mots de passe avec sel
10. **Remplacer AES/ECB par AES/GCM** et stocker la clé dans Android Keystore
11. **Mettre à jour OkHttp** vers la dernière version stable (CVE-2021-0341)
12. **Implémenter le certificate pinning** pour les domaines d'API critiques
13. **Revoir les permissions** : supprimer READ_SMS, RECORD_AUDIO, READ_CALL_LOG si non justifiées

---

## 5. Annexes Techniques

### A — Permissions dangereuses (12)
READ_SMS, RECEIVE_SMS, SEND_SMS, READ_CONTACTS, WRITE_CONTACTS,
ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, CAMERA, RECORD_AUDIO,
READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_CALL_LOG

### B — Composants exportés (6)
LoginActivity, DashboardActivity, TransferActivity (intent implicite),
SyncService, SmsReceiver (priority=999), UserDataProvider (permissions vides)

### C — Endpoints détectés
Production HTTP : `api.vulnerablebank.com`, `collect.vulnerablebank.com`,
`sync.vulnerablebank.com`, `analytics.vulnerablebank.com`
Dev hardcodé : `192.168.1.100:8080`

### D — Corrélation OWASP MASVS
| Vulnérabilité | MASVS |
|---|---|
| Secrets hardcodés | MASVS-STORAGE-2 |
| TLS désactivé | MASVS-NETWORK-1 |
| Composants exportés | MASVS-PLATFORM-1 |
| Stockage non chiffré | MASVS-STORAGE-1 |
| Injection SQL | MASVS-CODE-1 |
| Exfiltration SMS | MASVS-PLATFORM-2 |

---

*Rapport généré dans le cadre du Lab 6 — Analyse Statique APK | VM Mobexler v2.0 | MobSF v3.7.6*
