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


## 12/10/2026 — préparation GECKO-SUDOKU-VISUAL-001

Création de `feature/sudoku-visual-reasoning-v1` depuis `main` et dépôt de `missions/GECKO-SUDOKU-VISUAL-001.md`. **Aucune modification de code, aucun test ni APK nouveau à ce stade** ; aucune régression liée à cette mission observée ou corrigée. Historique HOF précédent conservé.


## 12/10/2026 — retour utilisateur : Hall/progression mêlant plusieurs pseudonymes

Fab rapporte que l'affichage de scores/progressions du Hall semble associer plusieurs pseudos à une même personne. **Symptôme déclaré, origine technique non prouvée et non reproduite à cette date.** Ne pas accuser le serveur avant audit API/cache/projection/statistiques locales/UI. Mission de diagnostic et correction : `missions/GECKO-HOF-PLAYER-ISOLATION-002.md`. Aucun correctif/code/build effectué lors de cette documentation.


## 12/10/2026 — diagnostic et corrections des deux missions

- Hall : `MainActivity.showHallOfFameLevel` posait sous un classement mondial multi-pseudonymes la courbe produite par `statsStore.eventsForModeAndDifficulty`, qui correspondait uniquement au téléphone ; fausse attribution visuelle. Courbe retirée du Hall mondial, accès maintenu via Stats du niveau du profil actif.
- `GlobalHallProjection.merge` supprimait par `HallIdentity` les doublons composites, risquant de fusionner deux scores réellement différents de joueurs homonymes aux mêmes métriques. Nouvelle fusion : conserver chaque score du cache déjà unique par `scoreId`, retirer tout au plus UNE copie locale par score mondial correspondant.
- Stockage personnel ancien : les préférences stats étaient communes à l'appareil et au pseudo modifiable ; introduction de profils locaux stables, séparation des stores par profil, conservation intégrale du store historique (pas de réattribution rétroactive sans identité).
- Sudoku : surimpressions distinctes des notes et du solveur, aucun effet sur la grille ; pulsation lente facultative pour éviter les flashs rapides.
- Première CI a trouvé `PlayerProfileStore.kt:51 Unresolved reference id` : corrigé par `buildList<GeckoPlayerProfile>` explicite.
- Deuxième CI a trouvé un test X-Wing incorrect : un candidat ajouté dans une ligne n'invalidait pas l'autre orientation par colonnes. Le test a été corrigé en invalidant les deux orientations, sans dégrader l'algorithme mathématique.
- **Statut à cet instant** : code poussé, test/build final en cours ; ne pas annoncer livraison APK sans passage GREEN + hachage. Aucun test Samsung effectué.


## Validation finale CI du lot 0.15.45-dev — 12/10/2026

- Workflow `Build GeckoDoku phone APK`, run [38314483379](https://github.com/greenpower2669/GeckoDoku/actions/runs/38314483379) : **GREEN**, `gradle :app:testDebugUnitTest :app:assemblePhone` → `BUILD SUCCESSFUL`.
- Étapes Pierre, banque sprites 240p (source 1 305 fichiers, modèle et manifest dans l'APK) et publication de l'artefact : SUCCESS.
- Artefact Actions : `GeckoDoku-v0.15.45-dev-phone` (ID `11775463777`) contenant `GeckoDoku-v0.15.45-dev.apk` et `GeckoDoku-v0.15.45-dev.apk.sha256` ; APK SHA-256 : `ddff9b30a3267573cbf0a1cecad98fa6c76ca7aee94f670533a2169e1701d586`.
- Cette version est le **build de test phone signé avec la clé debug existante**, PAS une publication Google Play. La signature Google Play n'est pas modifiée.
- PR brouillon #4 : https://github.com/greenpower2669/GeckoDoku/pull/4 ; ne pas merger ni publier avant accord explicite.
- **À faire** : recette physique Samsung par Fab (UI, Pierre, quatre modes, deux profils/homonymes, restauration des données). Les anciens résultats localement mélangés, sans identité d'origine, ne peuvent être reclassés automatiquement.
