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


### Validation Google Play du 12/10/2026 — rappel
Notification Play Console : nouvelle Upload Key prévue active le 14/10/2026 à **20:49 UTC** (22:49 à Lyon). Empreinte SHA-1 publique du nouveau certificat : `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`. Une empreinte historique du 07/10 ne suffit pas ; **une comparaison calculée dans le runner depuis le JKS restauré** et l'empreinte Play doit réussir AVANT merge/release. Ne jamais exposer la clé privée, les mots de passe ou le Base64.


## Mission active

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
6. **Règle immuable — lien APK direct GitHub :** lorsque Fab demande « APK décompressé sur GitHub » après CI verte, ne **jamais** proposer un lien Actions/Artifacts ZIP comme s'il s'agissait du fichier APK. Fournir un lien HTTPS finissant par `.apk`, hébergé comme asset d'une **prérelease TEMP** propre au run concerné (et son `.sha256`), sans dépôt Git de gros binaire, merge `main`, Release stable ni publication Play. Une demande explicite d'APK direct vaut autorisation de créer uniquement cette prérelease TEMP de test ; aucune autorisation générale pour les Releases stables. Vérifier l'existence du lien avant d'annoncer la réussite.
7. **Réponses brèves :** dire ce qui est fait, GREEN ou non, et donner le lien direct ; éviter les explications répétitives.

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


## Nouvelle mission ouverte — 12/10/2026

**GECKO-SUDOKU-VISUAL-001** — accessibilité Sudoku : pavé flottant bas-centre au premier plan, projection de candidats identiques par ligne, marquage couples/triplets, cercles/liens et visualisation manuelle du X-Wing.

- Ordre fonctionnel complet : [`missions/GECKO-SUDOKU-VISUAL-001.md`](missions/GECKO-SUDOKU-VISUAL-001.md).
- Branche de travail : `feature/sudoku-visual-reasoning-v1`, créée depuis `main`.
- Au dépôt de cette mission : **documentation seule, code non modifié, APK non construit**.
- Aucune fusion `main`, aucune Release ou publication sans validation explicite de Fab. Préserver l'identifiant Android, la signature, Pierre et les assets 240p.


## Avenant Fab — identité joueur dans Hall/progression (12/10/2026)

**GECKO-HOF-PLAYER-ISOLATION-002**, mission distincte à réaliser dans la même branche : [`missions/GECKO-HOF-PLAYER-ISOLATION-002.md`](missions/GECKO-HOF-PLAYER-ISOLATION-002.md). Retour utilisateur : des progressions associées à plusieurs pseudonymes semblent interprétées comme une seule personne. Objectif **quatre modes** : Hall global multi-joueurs conservé ; **aucune fusion des statistiques personnelles entre profils**. Même pseudo ≠ identité ; une identité stable vaut mieux qu'un nom affiché ; provenance incertaine ≠ attribution automatique. Audit/reproduction avant correction. **À cet instant : spécification poussée, code non corrigé, aucun nouveau build.** Maintenir les interdictions de merge, release, publication et changement de signature.


## Exécution du lot GECKO-SUDOKU-VISUAL-001 + GECKO-HOF-PLAYER-ISOLATION-002 — 12/10/2026

Les deux missions ont été **implémentées et poussées sur `feature/sudoku-visual-reasoning-v1`**, sans merge `main` ni Release. Version de test : `0.15.45-dev`, code 80 ; `applicationId` et configuration de signature inchangés. Sudoku : pavé bas-centre (PopupWindow), cases Lignes/Couples/Triplets/Entourer, option Pulsation douce, projections et cercles, groupes Sudoku ; pas d'élimination automatique. Hall : suppression de la courbe locale trompeuse sous le classement global, conservation des scores mondiaux distincts même si pseudo/métriques identiques, profils locaux et préférences statistiques séparés, import/export des préférences propres aux profils. Un changement de joueur en pleine partie demande la confirmation d'abandon et recommence la grille.

CI branche activée avec build `phone` et SHA-256 de l'APK dans l'artefact. **À la rédaction : run final en cours ; ne pas prétendre tests GREEN ni APK livré tant que non confirmé.** Contrôle visuel Samsung et scénario multi-profils réel encore nécessaires. Aucun merge/release/publication sans feu vert complémentaire.


## Validation finale CI du lot 0.15.45-dev — 12/10/2026

- Workflow `Build GeckoDoku phone APK`, run [38314483379](https://github.com/greenpower2669/GeckoDoku/actions/runs/38314483379) : **GREEN**, `gradle :app:testDebugUnitTest :app:assemblePhone` → `BUILD SUCCESSFUL`.
- Étapes Pierre, banque sprites 240p (source 1 305 fichiers, modèle et manifest dans l'APK) et publication de l'artefact : SUCCESS.
- Artefact Actions : `GeckoDoku-v0.15.45-dev-phone` (ID `11775463777`) contenant `GeckoDoku-v0.15.45-dev.apk` et `GeckoDoku-v0.15.45-dev.apk.sha256` ; APK SHA-256 : `ddff9b30a3267573cbf0a1cecad98fa6c76ca7aee94f670533a2169e1701d586`.
- Cette version est le **build de test phone signé avec la clé debug existante**, PAS une publication Google Play. La signature Google Play n'est pas modifiée.
- PR brouillon #4 : https://github.com/greenpower2669/GeckoDoku/pull/4 ; ne pas merger ni publier avant accord explicite.
- **À faire** : recette physique Samsung par Fab (UI, Pierre, quatre modes, deux profils/homonymes, restauration des données). Les anciens résultats localement mélangés, sans identité d'origine, ne peuvent être reclassés automatiquement.


## Nouvelle mission exécutée — 13/10/2026

**GECKO-PIERRE-WARMUP-003** — amorçage réel de la première phrase Piper/Sherpa : [`missions/GECKO-PIERRE-WARMUP-003.md`](missions/GECKO-PIERRE-WARMUP-003.md).

Ordre Fab : charger Pierre au démarrage, synthétiser une courte amorce silencieuse dans la vraie chaîne audio, attendre sa fin réelle (pas un timer arbitraire), garder la chaîne chaude puis seulement lire la première vraie phrase. Priorité à un AudioTrack persistant/réutilisable.

Implémenté sur `feature/sudoku-visual-reasoning-v1` : `PierrePiperSpeechEngine` sérialise l'amorce et la première parole ; `VoicePcmPlayer` utilise un AudioTrack STREAM persistant et valide la fin via `playbackHeadPosition`. Validation CI et surtout test Samsung de la première phrase encore requis. Aucun merge/release/publication autorisé par cet ordre.


### Validation CI GECKO-PIERRE-WARMUP-003

Run `38436224654` / #450 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque 240p vérifiés, APK et SHA-256 déposés en artefact. Le défaut acoustique de première syllabe reste à valider à l'oreille sur Samsung ; CI ne peut pas le prouver.


## Avenant Binaire et candidats réels — 14/10/2026

Fab autorise le correctif complet du pavé Sudoku après audit de l'impression « rien ne fonctionne ».

- Lignes / Couples / Triplets basculent des seules notes manuelles vers les candidats logiquement possibles calculés depuis les valeurs confirmées, **sans écrire ces candidats dans le moteur**.
- Entourer peut désormais lier un candidat possible sans exiger une note préexistante.
- Nouvelle checkbox **Binaire** : exactement deux occurrences d'un même chiffre candidat dans une ligne, colonne ou bloc ; double anneau + pulsation lente ; disparition automatique si l'occurrence n'est plus exactement deux.
- Prévisu explique chaque option ; Pierre prononce la même aide.
- Pavé compacté : titres et espaces visuels réduits, barre d'options abaissée à 46 dp avec marges internes d'1 px, tuiles séparées visuellement d'1 px.
- Tests unitaires ajoutés pour candidats calculés et Binaire.
- Branche uniquement : `feature/sudoku-visual-reasoning-v1`. Aucun merge main / Release / Play sans nouvel ordre.
- Run final attendu : #464, à valider avant annonce de livraison.


### Validation CI Binaire / candidats réels
Run `38504109994` / #464 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11867790810`, APK SHA-256 `356cb9973995d453d6e82e865b959f4648502624705322502e23d711ec52cb5e`.
Recette Samsung encore requise avant toute validation fonctionnelle finale. Aucun merge/release/publication.


## Avenant projection complète / pseudo / Pierre — 14/10/2026

Fab autorise la correction du retour Samsung :
- accès direct **✏️ Pseudo : <nom>** dans Réglages ;
- gestion **👥 Profils locaux** conservée séparément ;
- Pierre Sudoku reformulé avec phrases courtes et ordre de repères ligne → colonne → bloc ;
- checkbox **Projection** (ancien « Lignes ») : exactement deux candidats d'un même chiffre dans une ligne OU colonne, trait discret semi-transparent traversant toute la grille ;
- deux petits anneaux repèrent les occurrences sans dépendre seulement de la couleur ;
- les outils de projection du Prof sont visuellement adoucis.

Branche uniquement `feature/sudoku-visual-reasoning-v1`. Aucun merge main / Release / Play sans ordre explicite.


### Validation CI projection complète / pseudo / Pierre
Run `38519610024` / #472 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11873097206`, APK SHA-256 `562218c0d5cc2622e106a582bd5ba8a9bdec667fbec84fc72d0bedb3052ec4b7`.
Recette Samsung encore requise. Aucun merge/release/publication.

## Avenant repères humains / aide surhumaine volontaire — 14/10/2026

Ordre Fab implémenté sur `feature/sudoku-visual-reasoning-v1`.

- Sortir la barre d'outils du clavier Sudoku.
- Fenêtre **Repères humains** : flottante, déplaçable, fermable, sans confirmation ; outils Projection / Couple / Triplet / Candidat / Effacer.
- Le joueur place lui-même tous les repères. Projection manuelle = croix ligne-colonne à travers le candidat choisi.
- `?` du clavier arme une aide contextuelle. Si le clic suivant est sur le plateau, demander : « Êtes-vous sûr de vouloir une aide de sur-humain ? » avec une courte blague de Pierre.
- Après Oui seulement : fenêtre **Aide surhumaine** flottante, déplaçable, fermable.
- Aide auto : Projection / Couples / Triplets / Candidats / Binaire / Pulsation, toutes OFF sur nouvelle grille.
- Le bouton Candidats auto est le seul qui révèle tous les candidats ; les autres outils ne rendent que leurs candidats utiles.
- Aucun merge main / Release / Play sans feu vert explicite.
- CI finale du commit de code complet à confirmer avant livraison.


### Validation CI repères humains / aide surhumaine
Run `38522492488` / #486 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11875030286`, APK SHA-256 `793a3878817fa04f9b2096d3ac032c8f235c98df50f6a7f56df0139dda062d15`.
Recette Samsung encore requise. Aucun merge/release/publication.

## GECKO-HOF-PLAYER-ISOLATION-002 — Hall comparatif / progression perso (14/10/2026)

Ordre Fab exécuté sur `feature/sudoku-visual-reasoning-v1` :
- Hall global = tous les joueurs et tous les scores accessibles ; aucun Top 10 fonctionnel ;
- progression = groupement exclusif par identité opaque `playerProfileId`, jamais par pseudo ;
- sous-progressions visuelles séparées dans le Hall ;
- vieux scores sans identité sûre visibles mais non attribués ;
- nouveaux scores publient `metadata.playerProfileId` ;
- Pierre ne commente que le profil actif et son contexte est réinitialisé au changement de joueur ;
- pas de merge main, pas de Release, pas de publication Play.

Le run de validation final du dernier commit de code doit être GREEN avant annonce de livraison.


### Validation CI GECKO-HOF-PLAYER-ISOLATION-002
Run 38526555276 / #510 : GREEN. Commit code validé 29151fc63bd9f8b05d5ba188276f6cfa197e5998. Tests JVM + assemblePhone réussis, Pierre et banque sprites 240p vérifiés. Artefact GeckoDoku-v0.15.45-dev-phone ID 11876601009. APK SHA-256 eec808012b45e21a36a400672b1c50d7b2db41a27df9310db2df30eccef45981.
Reste la recette Samsung et la vérification serveur metadata. Aucun merge main / Release / Play.

## Avenant fenêtre Repères compacte / Pierre premier plan — 14/10/2026

Fab valide la disposition : Repères humains au-dessus du plateau, plateau au centre, clavier Sudoku en bas.

Ordres :
- première ouverture Repères directement compacte et alignée au plateau ;
- plus de réduction verticale manuelle à refaire à chaque fois ;
- dernière position mémorisée ; géométrie restaurée et re-clampée ;
- barre haute raccourcie/optimisée ;
- si Pierre parle, Pierre passe temporairement devant les fenêtres Sudoku ;
- à la fin de la parole, les fenêtres reprennent exactement leur place/calque fonctionnel précédent, sans perte d’état.

Mission de passation dédiée : `missions/GECKO-SUDOKU-FLOATING-LAYERS-004.md`.
Aucun merge main / Release / Play sans ordre explicite.


### Validation CI Repères compact / calques Pierre
Run `38537609942` / #522 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11880727507`, APK SHA-256 `3f6dbba028b4bbb793ed16cbf3a9bd3eb237f36963c4e9fd8a292da21fed911c`.
Recette Samsung encore requise pour valider visuellement le déplacement hors écran/restauration des PopupWindow. Aucun merge/release/publication.

## Feu vert Fab — correctif navigation Android / panneaux Sudoku (14/10/2026)

À partir de la capture Samsung, Fab constate que les boutons de navigation Android bornent le mouvement et empêchent un bon redimensionnement des fenêtres flottantes. Ordre explicite « Ok, go » **pour coder le correctif**, pas pour merger, créer une Release ni publier.

Mission canonique : `missions/GECKO-SUDOKU-SYSTEM-BARS-005.md`. Conserver les modes de jeu, la signature, l'identifiant, les médias Pierre et sprites 240p ; appliquer la géométrie sûre aux fenêtres Repères humains, Aide surhumaine et pavé. Maintenir les positions sauvegardées et les calques pendant la parole de Pierre. CI + APK de test nécessaires ; recette Samsung avant toute publication. **Aucun merge main / Release / Play autorisé.**

## Ordre Fab — projection manuelle sur un seul axe (14/10/2026)

Fab a validé explicitement le code de la correction « Parfait, code ça » suite à la capture Sudoku : si les marques du même chiffre sont alignées horizontalement, projeter uniquement la ligne ; verticalement, uniquement la colonne. La croix ligne+colonne est réservée aux hypothèses posées, pas aux simples repères manuels. Corriger `SudokuValueOverlayView`, messages et tests, sans toucher aux notes, solveur, Hall ou Pierre.

Mission : `missions/GECKO-SUDOKU-MANUAL-PROJECTION-006.md`, branche `feature/sudoku-visual-reasoning-v1`. Les interdictions de merge main, Release, publication Play ou changement de signature restent en vigueur. Attendre preuve CI et essai Samsung avant livraison définitive.

## Validation Fab — deux corrections ergonomiques Samsung, 14/10/2026

Fab donne « Ok, fait les deux push » pour les deux problèmes apparus sur ses captures :
1. La fenêtre Repères humains ne doit pas apparaître par-dessus l'intro ni avant la grille jouable.
2. La grille Sudoku doit être entièrement visible, le pavé flottant occupant l'espace au-dessous sans empiéter sur les dernières lignes (le tout au-dessus des boutons Android).

Travail sur `feature/sudoku-visual-reasoning-v1`. Passations séparées : `missions/GECKO-SUDOKU-INTRO-GATE-007.md` et `missions/GECKO-SUDOKU-KEYPAD-DOCK-008.md`. Relecture du code existant effectuée ; tests de géométrie et de cycle de vie ajoutés. Privilégier la lisibilité, préserver hypothèses, candidats, Pierre, sprites 240p, Hall, données, signature, modes et code de publication. Ne pas merger main, ne pas créer de Release, ne pas publier Play. Build CI GREEN et validation Samsung nécessaires.

### Preuve CI Samsung 007/008 — run #525
Validation finale après les missions 007 et 008 : run [#525](https://github.com/greenpower2669/GeckoDoku/actions/runs/38587086273), workflow 38587086273 **GREEN**, commit de build `eee5347f52602ccbc91d1359aeacb161c427d9f1`. `testDebugUnitTest + assemblePhone` réussis ; vérification Pierre Sherpa/Piper et banque sprites 240p réussie. Artefact `GeckoDoku-v0.15.45-dev-phone` ID `11905448115` ; APK `GeckoDoku-v0.15.45-dev.apk` SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`, contrôlé après extraction. La recette Samsung demeure nécessaire. Aucun merge main / Release / Play.

## Autorisation ponctuelle — APK brut du run #525 (14/10/2026)

Fab confirme « Feu vert » pour placer **l'APK décompressé du run #525** directement sur GitHub, dans une prérelease TEMP, et inscrit la règle ci-dessus dans le contrat immuable. Binaire attendu : `GeckoDoku-v0.15.45-dev.apk` SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`. Artefact source : Actions `38587086273` / `11905448115`. Tag cible : `temp-geckodoku-0.15.45-dev-run-525`. Aucune modification de code, de signature ou de `main` ; prérelease TEMP seulement.

## Preuve GitHub — APK direct #525 (14/10/2026)
Livraison APK brut run #525 : prérelease temporaire `temp-geckodoku-0.15.45-dev-run-525` créée et vérifiée par l'API GitHub. [Téléchargement direct .apk](https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-0.15.45-dev-run-525/GeckoDoku-v0.15.45-dev.apk), 299496421 octets, SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`. Workflow de publication #527 GREEN. La première tentative #526 (403 GitHub) a été corrigée en reprenant la méthode qui fonctionnait : checkout de la branche et création du tag sur le commit déclencheur. Aucun nouveau build Android, aucun merge main ou Release stable. Règle permanente de livraison d'APK direct enregistrée dans la section `Contrat FAB Copilot` d'`ordres-de-mission.md`.

## Ordre Fab — pavé compact physique sous grille (14/10/2026)

Feu vert « Go » après comparaison des captures 16:45 (palette trop grande, masque la grille) et 18:40 (palette compactée manuellement sous la grille, à droite). Objectif : obtenir la seconde disposition automatiquement, sans déplacer/redimensionner à chaque fois, conserver le Sudoku pleine taille et toutes les cases visibles. Cause réelle auditée : le PopupWindow a été créé avec les grandes dimensions avant de calculer le dock compact ; ses dimensions physiques n'étaient jamais corrigées. Correctif dans `MainActivity` + `SudokuFloatingWindowGeometryPolicy`, tests JVM. Mission `missions/GECKO-SUDOKU-KEYPAD-OPEN-COMPACT-009.md`. CI puis recette téléphone avant déclaration de réussite. Toujours pas de merge main ni Release stable / Play.

### Mission 009 — correction après CI de contrôle

Le run #528 `38603247697` a révélé 1 échec sur 299 tests malgré compilation Kotlin réussie, dû à une marge inférieure comptée deux fois dans le calcul du pavé compact. Politique corrigée sur la même branche (cf. `debughistorical.md`). Une nouvelle CI est exigée avant livraison ; ne pas présenter le run #528 comme valide.

## Preuve CI / livraison du pavé compact — mission 009 (14/10/2026)

Run `#529` (`38603852806`) **GREEN** sur le commit `a699757ec88c15cecc0de24568b02037f45b17e5` : 299 tests JVM, build Android phone, contrôle embarquement Pierre + banque sprites 240p et contrôle SHA-256 réussis. Artefact `GeckoDoku-v0.15.45-dev-phone`, id `11913804772`. APK `GeckoDoku-v0.15.45-dev.apk`, SHA-256 `28d0995d5e4b1948ff2351b34d3b99638b225b2f755e856fd036d5238ea8a86e`. Publication du seul APK direct dans prérelease TEMP demandée par le contrat immuable, tag `temp-geckodoku-0.15.45-dev-run-529` ; **aucun merge `main`, aucune Release stable, aucun Play**. Recette visuelle Samsung encore nécessaire.

## Ordre Fab — fusion et Release AAB signée (14/10/2026)

Fab ordonne explicitement : « Ok, merge release avec la secret key ». Protocole permanent ci-dessus impératif : utiliser uniquement l'Upload Key stockée dans GitHub Secrets, jamais l'APK phone signée debug, ni nouveau keystore. La nouvelle clé annoncée par Play devait être active à 20:49 UTC ; sa SHA1 publique `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7` est le garde-fou avant signature et merge. Effectuer d'abord build AAB branch GREEN + vérification empreinte, puis merge PR #4 et nouveau build main AAB signé ; publier Release GitHub avec AAB & SHA256, **sans envoi automatique Play Console**. Refuser toute Release non signée, mismatch de certificat ou secret absent. Réf : `missions/GECKO-PLAY-MERGE-SIGNED-RELEASE-010.md`.

## Clôture — ordre Fab « merge release avec la secret key » (14/10/2026)

**Clôture validée 14/10/2026 — mission 010.** Feu vert de Fab « merge release avec la secret key ». PR #4 mergée sur `main`, SHA merge `f48dcd7d6898793ccd6d63b22c0bc20644d6966a`. Contrôle pré-merge AAB sur branche : workflow `38636402228` GREEN (certificat Upload Key conforme au Play SHA-1 `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`, tests, Pierre et sprites 240p, `jarsigner -verify`). Build officiel depuis main : workflow `38636963727` GREEN, tests + `bundleRelease`, même contrôle certificat, `jar verified.`. GitHub Release **publique/stable** : https://github.com/greenpower2669/GeckoDoku/releases/tag/geckodoku-v0.15.45-dev-code80 ; AAB signé direct : https://github.com/greenpower2669/GeckoDoku/releases/download/geckodoku-v0.15.45-dev-code80/GeckoDoku-v0.15.45-dev-code80-signed.aab ; SHA-256 `08390c0e502380ee5d3b6e2e77e01d491ff2a9535c61524b9b5270eb92cd755c`. Application `com.greenpower2669.geckodoku`, versionName `0.15.45-dev`, versionCode `80`. L'APK téléphone précédent reste un build à signature test : NE PAS le présenter comme signé Play. Aucun envoi à Play Console réalisé ; l'acceptation de l'AAB par Google n'est pas encore démontrée. Aucune clé privée ni valeur de Secret dans Git.

Le protocole permanent demeure immuable. **Prochaine action distincte :** import manuel (ou explicitement autorisé) de l'AAB dans Play Console, vérification par Google de la clé acceptée, puis choix du canal de publication. Ne jamais confondre Release GitHub et publication Play.


## Mission GECKO-INFERNAL-NESTED-011 — ordre Fab du 15/10/2026
« Ok, code ça entre à 3 imbrication ou plus ». Le contrat Classic doit faire passer toutes les techniques déterministes avant chaque hypothèse. Mission Impossible = une hypothèse par contradiction (profondeur 1). Infernal = au moins 3 hypothèses réellement imbriquées dans une preuve de contradiction, pas seulement 3 hypothèses indépendantes. L'épuisement d'une profondeur de recherche n'est jamais une preuve de seconde solution. Vérifier indépendamment une seule solution complète avec countSolutions == 1. Feu vert code, tests et mémoire sur branche dédiée feature/infernal-nested-depth3-v1 ; aucune permission de merge main, Release, Play ou changement de signature. Garder intacts les autres modes, assets et le protocole de signature permanent.

### Mission 011 — compilation vérifiée, sans publication
Run Actions #533 / 38800346305 GREEN pour commit d394a65 (tests + APK phone, Pierre, sprites). Artefact Actions 12003858392 ; APK de test SHA-256 ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3. Aucune Release, aucun merge main ni Play. Contrôle fonctionnel de graine à profondeur 3 non encore certifié.


### Livraison ponctuelle de l'APK brut de la mission Infernal (15/10/2026)
Ordre Fab : « Demande à git de décompresser apk en fichiers accessible décompresser dans un dossier git temp ». Exécution limitée à la branche `feature/infernal-nested-depth3-v1`, job `publish_temp_apk_533` (run 38802231998 / #534 GREEN). Extraction dans `temp/GeckoDoku-infernal-depth3-run533` du runner GitHub Actions (espace éphémère), sans commit du binaire. Publication de l'APK brut en prérelease TEMP tag `temp-geckodoku-infernal-depth3-run533`, contrôlé par API GitHub : https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-infernal-depth3-run533/GeckoDoku-v0.15.45-dev.apk, taille 299496421 octets, SHA-256 `ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3`, fichier `.sha256` également publié. Aucune Release stable, aucun merge main, aucun AAB, aucun Play. La livraison du fichier ne vaut pas validation de la difficulté Infernal en conditions réelles.
