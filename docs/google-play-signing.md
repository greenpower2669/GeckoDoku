# GeckoDoku — signature Google Play sans PC

Mission : GECKO-PLAY-SIGNING-001.

## Principe

Google Play App Signing conserve la clé de signature finale distribuée aux utilisateurs. GeckoDoku utilise séparément une clé d'importation (upload key) pour signer chaque AAB envoyé à Play Console.

Aucun mot de passe ni JKS en clair n'est stocké dans le dépôt.

## Secret GitHub

Créer dans le dépôt GitHub :

`Settings → Secrets and variables → Actions → New repository secret`

Nom exact :

`ANDROID_UPLOAD_STORE_PASSWORD`

Choisir une valeur aléatoire forte d'au moins 24 caractères. Ne pas la poster dans une issue, un commit, un log ou un chat.

## Génération one-shot

Le workflow `.github/workflows/setup-play-upload-key.yml` :

1. génère `geckodoku-upload` en RSA 4096 ;
2. exporte le certificat public PEM ;
3. chiffre le JKS en AES-256-CBC avec PBKDF2 300000 ;
4. vérifie le déchiffrement et le certificat ;
5. commit uniquement le JKS chiffré, le certificat public et son empreinte ;
6. refuse de remplacer une clé existante.

Sur la branche de préparation, la génération est déclenchée par un commit contenant `[generate-upload-key]`.

## AAB signé

Le workflow `.github/workflows/build-play-aab.yml` :

- restaure le modèle Pierre et la banque sprites 240p validée ;
- exécute les tests JVM ;
- construit `:app:bundleRelease` ;
- déchiffre la clé uniquement dans le runner temporaire ;
- vérifie que le certificat correspond ;
- signe avec `jarsigner` ;
- vérifie la signature ;
- publie un artifact `GeckoDoku-v<version>-code<versionCode>-signed.aab`.

Sur la branche de préparation, un commit contenant `[play-aab]` lance le build. Une fois le workflow intégré à `main`, il peut aussi être lancé manuellement depuis l'onglet Actions.

## Interdictions

- ne jamais committer un fichier `.jks`, `.keystore` ou `.p12` en clair ;
- ne jamais afficher ou recopier le secret ;
- ne jamais régénérer une autre upload key par-dessus celle-ci ;
- ne pas confondre upload key et clé de signature finale Google Play.
