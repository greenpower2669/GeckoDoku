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


## État produit le 07/10/2026

Le bootstrap a été exécuté avec succès :

- run `37632351393` GREEN ;
- Upload Key : `geckodoku-upload`, RSA 4096 ;
- certificat SHA-256 : `8E:A9:D0:C7:33:0F:ED:0E:B4:FF:04:1E:BC:9C:EB:E0:93:09:B8:36:44:C7:39:8C:FB:AE:E8:E4:E4:13:43:5F` ;
- handoff chiffré : artifact `11486976777`, rétention 1 jour ;
- AAB signé : `GeckoDoku-v0.15.44-dev-code79-signed.aab` ;
- SHA-256 AAB : `cc904dab3c0170f522507af2c5805e5d00cde22bf920bc53537a2e1a1e726a16` ;
- artifact AAB : `11486204826` ;
- signature vérifiée par `jarsigner`.

Le handoff doit maintenant être copié dans le Secret `ANDROID_UPLOAD_KEYSTORE_BASE64` pour rendre les prochains builds reproductibles sans régénérer la clé.
