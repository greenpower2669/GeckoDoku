# GeckoDoku — signature Google Play sans PC

Mission : `GECKO-PLAY-SIGNING-001`.

## Architecture retenue

Google Play App Signing conserve la clé de signature finale distribuée aux utilisateurs.

GeckoDoku possède séparément une **Upload Key pérenne** qui sert uniquement à signer les AAB envoyés à Play Console.

La clé privée d'upload ne doit jamais être commitée dans Git, même chiffrée. Aucun mot de passe ne doit apparaître dans le dépôt, les logs ou le chat.

## Secrets GitHub

Dans `Settings → Secrets and variables → Actions` :

- `ANDROID_UPLOAD_STORE_PASSWORD` : mot de passe du keystore ;
- `ANDROID_UPLOAD_KEY_PASSWORD` : mot de passe de la clé ; il peut être identique au précédent ;
- `ANDROID_UPLOAD_KEY_ALIAS` : alias, normalement `geckodoku-upload` ;
- `ANDROID_UPLOAD_KEYSTORE_BASE64` : Base64 du JKS chiffré AES produit par le bootstrap.

GitHub ne permet pas de relire ensuite la valeur en clair d'un Secret. Les mots de passe restent donc également sauvegardés dans le gestionnaire de mots de passe de Fab.

## Bootstrap sans PC

Le workflow `.github/workflows/setup-play-upload-key.yml` est strictement un bootstrap :

1. il utilise les mots de passe déjà présents dans GitHub Secrets ;
2. il refuse de générer une nouvelle clé si `ANDROID_UPLOAD_KEYSTORE_BASE64` existe déjà ;
3. il génère `geckodoku-upload` en RSA 4096 / JKS ;
4. il exporte le certificat public ;
5. il chiffre le JKS en AES-256-CBC avec PBKDF2 300000 ;
6. il produit `ANDROID_UPLOAD_KEYSTORE_BASE64.txt`, qui contient uniquement le Base64 du JKS déjà chiffré ;
7. il publie ce handoff chiffré en artifact à rétention 1 jour ;
8. dans le même run, il construit, signe et vérifie le premier AAB ;
9. il efface les fichiers privés temporaires du runner.

Le bootstrap n'est déclenché que par un commit contenant `[bootstrap-play-once]`, et le job refuse les re-runs (`github.run_attempt != 1`).

Dès que le handoff existe, Fab copie le contenu de `ANDROID_UPLOAD_KEYSTORE_BASE64.txt` dans le Secret GitHub du même nom. Le fichier handoff est ensuite seulement une sauvegarde/transfert temporaire.

## Builds AAB suivants

Le workflow `.github/workflows/build-play-aab.yml` :

- reçoit le JKS chiffré via `ANDROID_UPLOAD_KEYSTORE_BASE64` ;
- le décode et le déchiffre uniquement dans le runner temporaire ;
- valide l'alias ;
- restaure Pierre et la banque sprites 240p validée ;
- exécute les tests JVM ;
- construit `:app:bundleRelease` ;
- signe avec `jarsigner` ;
- vérifie la signature ;
- publie uniquement l'AAB signé comme artifact ;
- détruit la copie temporaire du keystore.

Sortie : `GeckoDoku-v<version>-code<versionCode>-signed.aab`.

## Google Play

Pour une nouvelle application :

- utiliser Play App Signing ;
- laisser Google générer et conserver l'App Signing Key ;
- ne jamais créer cette App Signing Key dans nos workflows ;
- envoyer uniquement l'AAB signé avec notre Upload Key.

## Interdictions

- aucun `.jks`, `.keystore`, `.p12` ou `.jks.enc` dans Git ;
- aucun mot de passe dans une issue, un commit, un log ou un chat ;
- ne jamais régénérer silencieusement l'Upload Key ;
- ne pas confondre Upload Key et App Signing Key Google ;
- aucun merge `main`, aucune Release GitHub et aucun envoi Play Console sans validation explicite de Fab.
