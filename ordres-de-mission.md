# GeckoDoku — ordre de mission courant

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
