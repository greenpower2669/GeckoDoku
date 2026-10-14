# GECKO-PLAY-MERGE-SIGNED-RELEASE-010

## Autorisation
Le 14/10/2026, Fab : « Ok, merge release avec la secret key ». Signature à réaliser exclusivement avec la clé d'importation pérenne stockée dans **GitHub Secrets**, après validation du certificat sur Play Console. Aucun secret privé ni mot de passe dans Git, artefacts publics, logs ou conversation.

## Références immuables
- Protocole recopié sans modification dans le début de `ordres-de-mission.md`, source `feature/play-upload-signing-v1`.
- Clé de signature finale de l'app = **Google Play App Signing** ; clé d'importation = keystore reconstruit en runner depuis `ANDROID_UPLOAD_KEYSTORE_BASE64` + passwords, alias `geckodoku-upload`.
- Notification Google Play du 12/10 : activation prévue 14/10 à 20:49 UTC ; empreinte nouvelle Upload Key SHA-1 : `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`. Il faut comparer le certificat reconstruit à cette valeur et échouer si différent.
- AAB cible : package `com.greenpower2669.geckodoku`, version `0.15.45-dev`, `versionCode=80`. Tests phone #529 GREEN ; recette Samsung du pavé compact explicitement validée par Fab.

## Garde-fous et procédure
1. Restaurer le protocole immuable et le workflow signé `.github/workflows/build-play-aab.yml` sur la branche Sudoku, sans régénérer ni copier de JKS.
2. **Avant merge** : lancer workflow signé sur la branche Sudoku, vérifier présence des secrets, restitution du keystore, certificat SHA-1 EXACT, tests, Pierre/240p et signature `jarsigner -verify`. Échec => STOP, pas de merge/release.
3. Vérifier l'état de la PR #4 et du build téléphone GREEN. Finaliser PR #4 sur `main` uniquement après signature validée.
4. **Après merge** : sur `main`, reconstruire et vérifier l'AAB depuis secrets avec contrôle SHA-1 et `jarsigner`. La commande `[play-aab]` peut publier la Release GitHub seulement après réussite de toutes les étapes, avec fichier AAB signé et son SHA-256.
5. Aucun upload automatique Google Play ; Google peut demander d'autres vérifications. Ne jamais prétendre que Play accepte l'AAB tant qu'il n'a pas répondu positivement.
6. Conserver les cinq mémoires vivantes et références à l'AAB et au run, sans informations secrètes.

## Statut
- [x] Contrat immuable recopié et workflow AAB ajouté.
- [x] Contrôle SHA-1 Play attendu et publication de Release conditionnelle au GREEN sur `main`.
- [ ] Validation workflow signé PR branche.
- [ ] Merge PR #4 sur `main`.
- [ ] Workflow signé sur `main`, AAB et Release GitHub vérifiés.

## Clôture vérifiée (14/10/2026)

**Clôture validée 14/10/2026 — mission 010.** Feu vert de Fab « merge release avec la secret key ». PR #4 mergée sur `main`, SHA merge `f48dcd7d6898793ccd6d63b22c0bc20644d6966a`. Contrôle pré-merge AAB sur branche : workflow `38636402228` GREEN (certificat Upload Key conforme au Play SHA-1 `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`, tests, Pierre et sprites 240p, `jarsigner -verify`). Build officiel depuis main : workflow `38636963727` GREEN, tests + `bundleRelease`, même contrôle certificat, `jar verified.`. GitHub Release **publique/stable** : https://github.com/greenpower2669/GeckoDoku/releases/tag/geckodoku-v0.15.45-dev-code80 ; AAB signé direct : https://github.com/greenpower2669/GeckoDoku/releases/download/geckodoku-v0.15.45-dev-code80/GeckoDoku-v0.15.45-dev-code80-signed.aab ; SHA-256 `08390c0e502380ee5d3b6e2e77e01d491ff2a9535c61524b9b5270eb92cd755c`. Application `com.greenpower2669.geckodoku`, versionName `0.15.45-dev`, versionCode `80`. L'APK téléphone précédent reste un build à signature test : NE PAS le présenter comme signé Play. Aucun envoi à Play Console réalisé ; l'acceptation de l'AAB par Google n'est pas encore démontrée. Aucune clé privée ni valeur de Secret dans Git.

- [x] Workflow signé pré-merge GREEN.
- [x] PR #4 mergée.
- [x] AAB signé reconstruit sur main et Release GitHub publique validée.
- [ ] Déploiement Play Console : pas encore effectué.
