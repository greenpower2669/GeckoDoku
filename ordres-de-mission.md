# GeckoDoku — ordre de mission courant

## PROTOCOLE PERMANENT — SIGNATURE ANDROID / GOOGLE PLAY (À PRÉSERVER)

> Contrat de travail figé **par convention**, pas verrou GitHub technique. Ne jamais supprimer, remplacer ni réécrire cette procédure sans validation explicite de Fab ; tout incident ou changement de clé se consigne dans un avenant daté plus bas. **Aucun secret ni clé privée dans ce document.**

1. **Séparer les deux clés.** La **clé de signature de l'application** (App Signing Key) est gérée par Google Play App Signing. La **clé d'importation** (Upload Key) privée sert seulement à signer les AAB transmis à Google. Ne jamais demander le changement de la première lorsqu'on a perdu la seconde.
2. **Signature dédiée à GeckoDoku.** Package `com.greenpower2669.geckodoku`. Clé d'importation RSA 4096, magasin JKS, alias attendu `geckodoku-upload` (par défaut dans le workflow). Conserver le certificat public `.pem` et son empreinte SHA-256 ; ne pas confondre avec le certificat de signature de l'application distribué par Google.
3. **Création sans ordinateur via GitHub Actions.** Workflow historique `.github/workflows/setup-play-upload-key.yml` sur `feature/play-upload-signing-v1`. Son bootstrap `[bootstrap-play-once]` génère le JKS dans le runner temporaire, exporte le certificat public, chiffre le JKS par AES-256-CBC/PBKDF2 (300 000 itérations), émet une remise (handoff) **éphémère de 1 jour**, puis compile/teste/signe le premier AAB. Le bootstrap doit rester protégé contre la régénération accidentelle.
4. **Quatre noms de secrets canoniques du dépôt** (noms seulement, jamais valeurs) :
   - `ANDROID_UPLOAD_STORE_PASSWORD` — mot de passe du magasin et du chiffrement de remise ;
   - `ANDROID_UPLOAD_KEY_PASSWORD` — mot de passe de la clé ;
   - `ANDROID_UPLOAD_KEY_ALIAS` — alias, facultatif si le défaut `geckodoku-upload` est conservé ;
   - `ANDROID_UPLOAD_KEYSTORE_BASE64` — Base64 du **JKS déjà chiffré** AES-256-CBC/PBKDF2, **pas** Base64 du JKS nu. Source : `ANDROID_UPLOAD_KEYSTORE_BASE64.txt` de la remise.
5. **Obligation de sauvegarde avant de terminer le bootstrap.** Copier le contenu chiffré dans le secret `ANDROID_UPLOAD_KEYSTORE_BASE64` **et** en conserver une copie durable hors GitHub, sécurisée, avec les informations de déchiffrement conservées séparément. Le handoff d'un jour n'est **jamais** une sauvegarde. Vérifier une reconstruction réelle du JKS, la correspondance du certificat et la capacité à signer un AAB avant de considérer le dispositif fiable.
6. **AAB ultérieurs.** Workflow `.github/workflows/build-play-aab.yml` : restituer le JKS temporairement depuis les Secrets, préparer Pierre + sprites 240p, lancer tests et `bundleRelease`, signer avec `jarsigner`, vérifier la signature, publier uniquement l'artefact AAB et supprimer les fichiers privés temporaires. Les APK téléphone de test restent signés par leur clé de test : **ne jamais les utiliser comme AAB Google Play**.
7. **Perte de l'Upload Key.** Examiner sauvegarde hors dépôt, Secrets et éventuel artefact encore valide. Ni les mots de passe, ni un AAB signé, ni le certificat public `.pem` ne permettent de reconstituer la clé privée. Si elle est introuvable, créer **une nouvelle Upload Key**, en sauvegarder immédiatement le JKS chiffré, exporter le **nouveau** certificat public `.pem`, puis demander la **réinitialisation de la clé d'importation** dans Play Console. Attendre la confirmation Google avant d'envoyer un AAB signé avec la nouvelle clé. Vérifier que Play App Signing est activé.
8. **Interdictions.** Jamais de JKS (même chiffré), Base64 privé, mots de passe ou clés dans le dépôt, les logs, les mémoires ou un artefact public non maîtrisé ; jamais de merge `main`, Release GitHub, publication Play ni changement irréversible sans ordre explicite de Fab. Ne pas exposer les secrets dans le chat.

Référence Google : https://developer.android.com/studio/publish/app-signing?hl=fr — rubrique « Réinitialiser une clé d'importation privée perdue ou compromise ».

## Mission active

`GECKO-PLAY-SIGNING-001` — Upload Key Google Play pérenne + AAB signé sans PC.

- base : `main@908df36a4e26e79d714967ff9fb7948759cdd9ee`
- branche : `feature/play-upload-signing-v1`
- objectif immédiat autorisé par Fab le 07/10/2026 : générer UNE Upload Key pérenne hors dépôt et produire le premier AAB signé.
- Play App Signing : Google générera/conservera l'App Signing Key ; GeckoDoku ne crée que l'Upload Key.
- alias : `geckodoku-upload`, RSA 4096, JKS.
- secrets : `ANDROID_UPLOAD_STORE_PASSWORD`, `ANDROID_UPLOAD_KEY_PASSWORD`, `ANDROID_UPLOAD_KEY_ALIAS`, puis `ANDROID_UPLOAD_KEYSTORE_BASE64`.
- `ANDROID_UPLOAD_KEYSTORE_BASE64` contient le Base64 du JKS déjà chiffré AES-256-CBC/PBKDF2 ; aucun keystore, même chiffré, n'est stocké dans Git.
- bootstrap one-shot : génération dans le runner, handoff chiffré 1 jour, build/tests/package, signature + vérification du premier AAB, nettoyage.
- builds suivants : reconstruction du keystore uniquement depuis GitHub Secrets.
- chaîne média AAB : Pierre + banque sprites 240p validée.
- aucun changement gameplay.
- aucun merge `main`, aucune Release GitHub et aucun envoi Play Console sans nouvel ordre explicite de Fab.


## Résultat bootstrap du 07/10/2026

- commit de préparation : `93dc77b6b175790ab392f731a86be712da3970dd` ;
- run GitHub Actions : `37632351393` / #2 — GREEN ;
- Upload Key générée : alias `geckodoku-upload`, RSA 4096 ;
- certificat upload SHA-256 : `8E:A9:D0:C7:33:0F:ED:0E:B4:FF:04:1E:BC:9C:EB:E0:93:09:B8:36:44:C7:39:8C:FB:AE:E8:E4:E4:13:43:5F` ;
- handoff chiffré : artifact `geckodoku-upload-key-handoff`, ID `11486976777`, rétention 1 jour ;
- AAB signé : artifact `GeckoDoku-v0.15.44-dev-code79-GooglePlay`, ID `11486204826` ;
- fichier : `GeckoDoku-v0.15.44-dev-code79-signed.aab` ;
- SHA-256 AAB : `cc904dab3c0170f522507af2c5805e5d00cde22bf920bc53537a2e1a1e726a16` ;
- tests JVM + `bundleRelease` : GREEN ;
- Pierre + sprites 240p : vérifiés dans l'AAB ;
- `jarsigner -verify` : `jar verified.` ;
- aucun keystore privé committé dans Git ;
- prochain geste obligatoire : placer le contenu de `ANDROID_UPLOAD_KEYSTORE_BASE64.txt` dans le Secret GitHub `ANDROID_UPLOAD_KEYSTORE_BASE64`, puis conserver une sauvegarde hors dépôt.

## Mission close précédente

`GECKO-HOF-SYNC-001` — Hall of Fame global v1.

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- branche : `feature/gecko-hof-sync-v1`
- code fonctionnel HOF + restauration Hall : `1e54fe8156bc04de2470456423ec62f125856083`
- version de publication : `0.15.44-dev` / code 79
- téléphone de référence avant HOF : `phone-0.15.43-dev-run-403`
- ordre explicite de Fab reçu le 06/10/2026 : merge `main` + Release autorisés.

## Contrat FAB Copilot

1. Lire le code courant avant modification ; jamais recoder depuis mémoire.
2. Synchroniser `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md`, `ordres-de-mission.md` après geste significatif.
3. `sauvegarde.md` = archive froide.
4. Aucun merge `main`, Release ou prerelease sans ordre explicite de Fab.
5. Ne pas modifier gameplay/médias source hors cause démontrée.

## Contrat HOF

Hôte : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`.

- POST `/api/v1/games/geckodoku/scores` ; GET `/api/v1/games/geckodoku/sync` ;
- aucun secret/Authorization dans APK ;
- `runId` idempotent ; payload persisté avant réseau ; retry exact ;
- retrait pending seulement sur confirmation serveur ;
- seed 64 bits en chaîne décimale ;
- 400/409/413/415 BLOCKED, 429/5xx/réseau retry ;
- `/sync` fusionne `scoreId` et persiste page + `nextCursor` ensemble ;
- global ne modifie jamais stats/progression personnelles ;
- Hall UI peut afficher le cache global restauré et le fusionne au Hall local sans réinjecter ces résultats dans les stats ;
- `completedAt` unique local/global sert aussi au dédoublonnage d’affichage.

## Avenant validation téléphone 06/10

Le premier APK HOF était invalide pour médias : absence banque sprites 240p + modèle Pierre. Journal Fab : `FileNotFoundException` SpriteRGBA et `engine=android`.

Le score global absent après réinstallation venait d’un raccord UI incomplet : cache `/sync` séparé mais UI Hall local-only. Corrigé par `GlobalHallProjection` + fusion dans `HallOfFameStore.entries()`.

Nouvel APK téléphone :
- run `37526965060` GREEN ;
- artifact `GeckoDoku-HOF-v1-phone-validation` ;
- SHA-256 `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf` ;
- Pierre UPMC Medium présent ;
- banque 240p présente, 1 305 fichiers vérifiés ;
- workflow temporaire supprimé après génération.

## Décision finale Fab — 06/10/2026

Fab a effectué une désinstallation/réinstallation puis constaté que le Hall global revient correctement ; la capture montre `GeckoDoku Classic · Facile · 3 résultats`. Le journal montre la banque 240p active et les animations Gecko sans l’ancienne erreur de fichiers manquants.

Fab autorise explicitement :
1. le merge de `feature/gecko-hof-sync-v1` vers `main` ;
2. la publication de la Release HOF.

Avant merge, le pipeline canonique doit être publiable. La Release historique `PackageSprites` est absente ; `build.yml` doit utiliser la méthode validée du run `37526965060` : extraire les sprites 240p depuis l’APK téléphone validé `phone-0.15.43-dev-run-403`, SHA-256 `5b4f38049c3c7e8d115b78bef573784084ddec85cd4774d620dd0d76a4ba7a94`, puis vérifier Pierre + sprites dans l’APK construit.

Cible : `0.15.44-dev` / code 79.

Les scénarios offline/restart/retry/doublons encore non rejoués exhaustivement restent du suivi produit et ne bloquent plus la Release sur décision explicite de Fab.


## Clôture — merge et Release effectués

Ordre Fab exécuté.

- PR #3 : mergée.
- merge `main` : `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`.
- workflow téléphone canonique : run `37531494180`, #432, GREEN.
- Release finale : `phone-0.15.44-dev-run-432`.
- APK : `GeckoDoku-v0.15.44-dev.apk`.
- SHA-256 : `555cce892d573aa5d5bd794b78254ce315e789726a474f149546c11e096d108c`.
- statut Release : publiée, non prerelease, latest au moment de la publication.

La mission `GECKO-HOF-SYNC-001` est close. Les tests offline/restart/retry/doublons exhaustifs restant non rejoués restent des vérifications produit futures et ne remettent pas en cause l’ordre de publication déjà donné par Fab.
