# Lab 6 — Analyse Statique d'un APK avec MobSF

> **Contexte académique** — Cours de Cybersécurité Mobile | VM Mobexler  
> Analyse statique d'une application Android volontairement vulnérable avec l'outil MobSF (Mobile Security Framework)

---
<img width="1440" height="1040" alt="image" src="https://github.com/user-attachments/assets/ce5f8062-a931-4c42-b0ec-4066331da3df" />

## ⚠️ Avertissement

L'application **VulnerableBank** contenue dans ce dépôt est **intentionnellement vulnérable** à des fins pédagogiques. Elle ne doit **jamais être déployée en production**. Les clés API, credentials et secrets présents dans le code sont **fictifs** et créés uniquement pour illustrer les vulnérabilités étudiées en cours.

---

## Structure du projet

```
lab6_mobsf/
├── VulnerableBank/                  # Projet Android Studio (APK source)
│   └── app/src/main/
│       ├── AndroidManifest.xml      # Manifeste avec vulnérabilités
│       ├── java/com/example/vulnerablebank/
│       │   ├── MainActivity.java        # Backdoor + logging credentials
│       │   ├── LoginActivity.java       # Injection SQL + MD5 faible
│       │   ├── ApiService.java          # TLS désactivé + HTTP en clair
│       │   ├── DatabaseHelper.java      # Stockage non chiffré
│       │   ├── SmsReceiver.java         # Exfiltration SMS
│       │   ├── DashboardActivity.java   # Composant exporté non protégé
│       │   ├── TransferActivity.java    # Intent implicite non sécurisé
│       │   ├── SyncService.java         # Service exporté sans permission
│       │   └── UserDataProvider.java    # ContentProvider sans permission
│       └── res/values/strings.xml   # Secrets hardcodés
│
└── analysis/                        # Fichiers d'analyse MobSF
    ├── analyse_info.txt             # Traçabilité de l'analyse
    ├── apk_hash.txt                 # Hash SHA-256 de l'APK
    ├── app_info.txt                 # Informations générales
    ├── permissions.txt              # Analyse des permissions
    ├── composants_exportes.txt      # Composants Android exposés
    ├── config_reseau.txt            # Configuration réseau
    ├── endpoints.txt                # URLs et endpoints détectés
    ├── vulnerabilites.txt           # Liste complète des vulnérabilités
    ├── correlation_masvs.txt        # Mapping OWASP MASVS / MASTG
    ├── top_vulnerabilites.txt       # Synthèse par sévérité + faux positifs
    └── rapport_final.md             # Rapport d'audit complet
```

---

## Résumé des vulnérabilités

| # | Vulnérabilité | Sévérité | MASVS |
|---|---|---|---|
| V-01 | Secrets hardcodés (Stripe, JWT, AES, BDD) | 🔴 CRITIQUE | MASVS-STORAGE-2 |
| V-02 | Vérification TLS désactivée par code | 🔴 CRITIQUE | MASVS-NETWORK-1 |
| V-03 | ContentProvider sans permission | 🔴 CRITIQUE | MASVS-PLATFORM-1 |
| V-04 | Exfiltration de SMS vers serveur externe | 🔴 CRITIQUE | MASVS-PLATFORM-2 |
| V-05 | Injection SQL dans l'authentification | 🔴 CRITIQUE | MASVS-CODE-1 |
| V-06 | Mode debug activé en production | 🟠 ÉLEVÉ | MASVS-RESILIENCE-2 |
| V-07 | Credentials loggés dans Logcat | 🟠 ÉLEVÉ | MASVS-STORAGE-3 |
| V-08 | Chiffrement faible (MD5 + AES/ECB) | 🟠 ÉLEVÉ | MASVS-CRYPTO-1 |
| V-09 | Composants exportés sans protection (6) | 🟠 ÉLEVÉ | MASVS-PLATFORM-1 |
| V-10 | Stockage SQLite non chiffré | 🟠 ÉLEVÉ | MASVS-STORAGE-1 |
| V-11 | android:allowBackup="true" | 🟡 MOYEN | MASVS-STORAGE-8 |
| V-12 | Bibliothèques avec CVE connues | 🟡 MOYEN | MASVS-CODE-3 |

**Score MobSF : 12/100 — CRITIQUE**

---

## Outils utilisés

| Outil | Version | Rôle |
|---|---|---|
| MobSF | v3.7.6 | Analyse statique automatisée |
| VM Mobexler | v2.0 | Environnement d'analyse sécurisé |
| Android Studio | Référence | Lecture du code source |
| jadx | Référence | Décompilation APK |

---

## Références

- [OWASP MASVS](https://mas.owasp.org/MASVS/) — Mobile Application Security Verification Standard
- [OWASP MASTG](https://mas.owasp.org/MASTG/) — Mobile Application Security Testing Guide
- [MobSF GitHub](https://github.com/MobSF/Mobile-Security-Framework-MobSF)
- [Android Security Best Practices](https://developer.android.com/privacy-and-security/security-best-practices)
- [Network Security Configuration](https://developer.android.com/privacy-and-security/security-config)

---

## Rapport complet

Le rapport d'audit complet est disponible dans [`analysis/rapport_final.md`](analysis/rapport_final.md).

---

*Lab 6 — Cybersécurité Mobile | MobSF Static Analysis*
