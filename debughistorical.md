# GeckoDoku — FAB Copilot debug historical

> Historique condensé. La vérité courante reste `brain.md` + `ordres-de-mission.md`.

## Baseline avant HOF

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- version `0.15.43-dev`, code 78
- téléphone validé avant HOF : `phone-0.15.43-dev-run-403`
- quatre modes, Pierre, médias 240p, stats et Hall local déjà fonctionnels.

## GECKO-HOF-SYNC-001 — 06/10/2026

Implémenté en TDD : payload 4 modes, pending crash-safe, client ACK, backoff, cache `/sync`, mono-worker, reprise réseau, runtime Android, mapping des complétions et timestamp local/global unique.

Invariants : pending avant réseau, retry exact, erreurs permanentes BLOCKED, `/sync` transactionnel, seed décimale 64 bits, aucun secret APK, aucune donnée globale injectée dans stats/progression.

## Régressions téléphone du premier APK de validation

Fab a fourni capture + journal.

### Médias

Symptômes : Gecko invisibles/empreintes vides et Pierre muet.

Preuves journal :
- `SpriteBankFactory ... SPRITE_BANK_ERROR ... FileNotFoundException` sur `gecko/alive/stay*.mp4`, `Gecko_apparition.mp4`, etc. ;
- `ProfessorSpeech ... SPEAK_STARTED ... engine=android`.

Cause : l’APK de validation HOF avait été construit via une chaîne légère `assembleDebug` sans le packaging téléphone complet. Le code gameplay n’était pas en cause.

Correction build :
- Piper UPMC Medium téléchargé avec checksum canonique ;
- ancienne Release technique `PackageSprites` constatée absente ;
- banque 240p restaurée depuis l’APK téléphone validé `phone-0.15.43-dev-run-403`, SHA connu `5b4f38049c3c7e8d115b78bef573784084ddec85cd4774d620dd0d76a4ba7a94` ;
- `assemblePhone` ;
- contrôle APK : modèle Pierre présent, index sprites présent, bank-manifest 240p présent, 1 305 fichiers 240p, entrées Gecko signalées par le journal présentes.

### Hall après réinstallation

Symptôme : un score global Facile existait mais, après désinstallation/réinstallation, l’UI disait « aucune partie terminée » ; une nouvelle partie recréait ensuite 1 résultat.

Cause : `/sync` alimentait bien `GlobalScoreCacheStore`, mais `showHallOfFame()` lisait seulement `HallOfFameStore.entries()` local. La désinstallation effaçait donc la seule source affichée.

Correction TDD :
- `GlobalHallProjectionTest` RED ;
- `GlobalHallProjection` convertit les entrées cache valides en `HallOfFameEntry` ;
- `HallOfFameStore.entries()` fusionne local + cache global en lecture seule ;
- même complétion locale/globale dédoublonnée grâce au `completedAt` commun ;
- entrées invalides/inachevées ignorées ;
- tests HOF GREEN.

## État actuel

- code fonctionnel : `1e54fe8156bc04de2470456423ec62f125856083`
- build téléphone complet : run `37526965060` GREEN
- artifact : `GeckoDoku-HOF-v1-phone-validation`
- SHA-256 : `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf`
- workflow temporaire de build supprimé après génération.

Reste : validation réelle animations/Pierre, restauration Hall après clean install, puis scénarios offline/retry. Aucun merge/release avant ordre de Fab.


## Validation finale et préparation Release — 06/10/2026

Fab a effectué une désinstallation/réinstallation sur téléphone. Le Hall global a été restauré après clean install et l’UI affiche `GeckoDoku Classic · Facile · 3 résultats`. Le journal montre les banques 240p chargées et les animations Gecko relancées sans l’ancienne erreur de fichiers absents.

Fab a ensuite donné l’ordre explicite de merger `feature/gecko-hof-sync-v1` dans `main` et de publier la Release.

Avant publication, contrôle du workflow canonique :
- la Release `PackageSprites` référencée par `.github/workflows/build.yml` n’existe plus (404 GitHub) ;
- la Release téléphone validée `phone-0.15.43-dev-run-403` existe et son APK a le SHA-256 `5b4f38049c3c7e8d115b78bef573784084ddec85cd4774d620dd0d76a4ba7a94` ;
- le workflow canonique a donc été aligné sur la méthode déjà validée par le run `37526965060` : restauration de `assets/sprites` depuis cet APK connu bon, puis vérification de Pierre et de la banque 240p dans l’APK produit ;
- version de publication portée à `0.15.44-dev`, code 79.

Les tests offline/retry/doublons exhaustifs n’ont pas tous été rejoués manuellement après ce clean install ; Fab accepte explicitement ce reliquat de validation pour cette publication.


## Merge et Release finale HOF

- PR #3 mergée dans `main` par le commit `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`.
- Le merge a déclenché le workflow téléphone canonique avec `[phone-release] [phone-publish]`.
- Run `37531494180` / #432 : GREEN.
- Étapes GREEN : restauration sprites 240p depuis APK validé, tests JVM, `assemblePhone`, vérification Pierre + banque sprites, création prerelease puis promotion.
- Release finale : `phone-0.15.44-dev-run-432`.
- APK : `GeckoDoku-v0.15.44-dev.apk`.
- SHA-256 : `555cce892d573aa5d5bd794b78254ce315e789726a474f149546c11e096d108c`.


## GECKO-PLAY-SIGNING-001 — ouverture et correction 07/10/2026

Besoin Fab : produire un AAB Google Play signé sans PC, avec une vraie Upload Key pérenne.

État initial :
- `main@908df36a4e26e79d714967ff9fb7948759cdd9ee` ;
- APK téléphone historique signé avec la clé Android de test, impropre à Google Play ;
- `release` sans signingConfig de production ;
- chaîne Pierre + sprites 240p déjà connue et reproductible.

Première préparation abandonnée avant génération de clé : elle prévoyait de committer un JKS chiffré dans Git. Fab a précisé le contrat : la clé privée doit rester entièrement hors dépôt et être injectée via GitHub Actions Secrets. Aucun JKS chiffré n'a été généré ni committé avant cette correction.

Architecture corrigée :
- Play App Signing conserve l'App Signing Key chez Google ;
- GeckoDoku crée uniquement l'Upload Key `geckodoku-upload` ;
- bootstrap GitHub Actions à partir des mots de passe Secrets ;
- JKS chiffré AES-256-CBC/PBKDF2 puis transmis temporairement par artifact 1 jour sous forme Base64 ;
- Fab place ensuite ce Base64 dans `ANDROID_UPLOAD_KEYSTORE_BASE64` ;
- builds futurs : keystore reconstruit uniquement dans le runner depuis Secrets ;
- premier AAB signé dans le même bootstrap pour éviter de bloquer la publication sans PC ;
- aucun gameplay/Gradle produit modifié ;
- aucun merge `main`, Release GitHub ou envoi Play Console sans ordre explicite.


## Bootstrap exécuté — 07/10/2026

Commit `93dc77b6b175790ab392f731a86be712da3970dd`, run `37632351393` GREEN.

La validation des trois Secrets déjà créés par Fab a réussi. Une Upload Key pérenne `geckodoku-upload` RSA 4096 a été générée, vérifiée puis utilisée immédiatement pour signer le premier AAB.

Le JKS brut n'a jamais été committé. Le handoff est un Base64 du JKS chiffré AES-256-CBC/PBKDF2, artifact `11486976777` à rétention 1 jour. Certificat SHA-256 : `8E:A9:D0:C7:33:0F:ED:0E:B4:FF:04:1E:BC:9C:EB:E0:93:09:B8:36:44:C7:39:8C:FB:AE:E8:E4:E4:13:43:5F`.

Chaîne build : Sherpa AAR vérifié, Pierre UPMC Medium restauré, banque sprites 240p restaurée depuis l'APK de référence, tests JVM + `bundleRelease` GREEN, présence Pierre/sprites contrôlée, `jarsigner -verify` = `jar verified.`.

AAB : `GeckoDoku-v0.15.44-dev-code79-signed.aab`, SHA-256 `cc904dab3c0170f522507af2c5805e5d00cde22bf920bc53537a2e1a1e726a16`, artifact `11486204826`.

Reste obligatoire avant de considérer la clé réutilisable par CI : transférer `ANDROID_UPLOAD_KEYSTORE_BASE64.txt` dans le Secret GitHub `ANDROID_UPLOAD_KEYSTORE_BASE64` et conserver une sauvegarde hors dépôt.
