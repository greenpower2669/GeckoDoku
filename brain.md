# GeckoDoku — FAB Copilot brain

> Mémoire fonctionnelle courte. Historique : `debughistorical.md`; `sauvegarde.md` reste archive froide.

## Référence

- canonique : `main`
- mission `GECKO-HOF-SYNC-001` : close après validation téléphone, merge et Release
- merge final : `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`
- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- code HOF + restauration Hall : `1e54fe8156bc04de2470456423ec62f125856083`
- version de publication : `0.15.44-dev`, code 79
- ordre explicite reçu de Fab le 06/10/2026 : merger `main` et publier la Release HOF.

## Hall global v1

API : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`.

Flux sortant :
`GlobalScoreCompletionBridge → GlobalScoreCompletionPublisher → PendingScoreStore → GlobalScoreSyncCoordinator → GeckoDokuHallApiClient`.

Flux entrant :
`GET /sync → GlobalScoreCacheStore → GlobalHallProjection → HallOfFameStore.entries()`.

Invariants :
- un seul `completedAt` local/global ;
- pending écrit avant réseau ;
- retry = même JSON + même `runId` ;
- retrait seulement après ACK accepté ou `/sync` confirmé ;
- seed 64 bits en chaîne décimale ;
- aucun secret dans l’APK ;
- global ne modifie jamais stats/progression personnelles ;
- Hall affiché fusionne local + cache global et dédoublonne la même complétion.

## Validation téléphone du 06/10/2026

Trois écarts trouvés :
1. Gecko animé vide : banque SpriteRGBA 240p absente du premier APK HOF (`FileNotFoundException`).
2. Pierre absent : modèle Piper non embarqué, fallback `engine=android`.
3. Après désinstallation/réinstallation, le bouton Hall lisait seulement le Hall local et ignorait le cache `/sync`.

Corrections :
- restauration Hall par `GlobalHallProjection` + fusion lecture-only dans `HallOfFameStore` ;
- tests HOF GREEN ;
- nouvel APK construit en variante `phone` avec Pierre UPMC Medium + banque 240p restaurée depuis l’APK téléphone validé `phone-0.15.43-dev-run-403` ;
- APK vérifié : 1 305 fichiers 240p, index Gecko attendu et modèle Pierre présents.

Nouvel APK validation :
- run : `37526965060` GREEN ;
- artifact : `GeckoDoku-HOF-v1-phone-validation` ;
- SHA-256 APK : `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf`.

Le premier APK HOF SHA `926058...` est à considérer invalide pour toute validation média.

## Validation finale téléphone et publication

- désinstallation/réinstallation effectuée par Fab ;
- le Hall global est restauré après clean install : l’UI affiche de nouveau les résultats synchronisés ;
- la capture de validation montre `GeckoDoku Classic · Facile · 3 résultats` ;
- le journal téléphone montre le backend SpriteRGBA actif, les banques 240p chargées en mémoire et les animations Gecko relancées sans `FileNotFoundException` ;
- Fab autorise explicitement le merge `main` et la Release malgré les scénarios offline/retry exhaustifs encore non rejoués dans cette session.

Blocage de publication trouvé puis corrigé avant merge :
- le workflow canonique `build.yml` référençait encore la Release supprimée `PackageSprites` ;
- il restaure désormais la banque 240p depuis l’APK téléphone validé `phone-0.15.43-dev-run-403`, avec SHA-256 figé ;
- il vérifie aussi dans l’APK produit la présence de Pierre et des assets sprites 240p.

Cible de publication : `0.15.44-dev` / code 79.


## Publication finale

- workflow Release : run `37531494180` / #432 GREEN ;
- Release : `phone-0.15.44-dev-run-432` ;
- titre : `GeckoDoku 0.15.44-dev • téléphone validé` ;
- APK : `GeckoDoku-v0.15.44-dev.apk` ;
- SHA-256 : `555cce892d573aa5d5bd794b78254ce315e789726a474f149546c11e096d108c` ;
- Release finale non prerelease, ciblée sur le merge `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`.


## Projet ouvert — raisonnement visuel Sudoku (12/10/2026)

Mission `GECKO-SUDOKU-VISUAL-001` : conception validée, **documentation poussée seulement** sur `feature/sudoku-visual-reasoning-v1`. Nouveau pavé flottant bas-centre avec priorité après parole de Pierre ; cases Projection, Couples, Triplets, Entourer ; lignes dès deux candidats identiques ; paires/triplets dans une unité Sudoku ; liens/cercle pour X-Wing visuel sans preuve automatique ; animation lente facultative et affichage accessible. Mission détaillée : `missions/GECKO-SUDOKU-VISUAL-001.md`. Aucune modification du code, de `main`, de la signature ou de la Release à ce stade.


## Correction à réaliser — identité Hall et progression (12/10/2026)

Signalement de Fab : scores Hall / courbes de progression paraissent réunir les résultats de plusieurs pseudonymes sous un même joueur. Mission autonome `GECKO-HOF-PLAYER-ISOLATION-002`, `missions/GECKO-HOF-PLAYER-ISOLATION-002.md`, même branche de travail `feature/sudoku-visual-reasoning-v1`. **Hall global = multi-profils ; progression personnelle = un seul profil identifié ; filtrage par mode pour Classique, Sudoku, Gomoku, Abeilles & Geckos.** Pseudo ≠ identité stable. Ne pas inférer l'origine des scores historiques inconnus. Signalement **non reproduit, non corrigé** à ce stade.


## Implémentation Sudoku accessible et profils (12/10/2026, branche)

Code poussé sur `feature/sudoku-visual-reasoning-v1` : `SudokuVisualReasoning.kt` (projections ligne/candidats, paires et triplets nus dans une unité, vérificateur géométrique X-Wing), `SudokuValueOverlayView.kt` (repères visuels semi-transparents, cercles et pulsation lente facultative), `MainActivity.kt` (pavé PopupWindow bas-centre, options, sélection manuelle de cercles). Le mode Entourer intercepte volontairement la touche candidats et n'édite pas les notes. Référence : `missions/GECKO-SUDOKU-VISUAL-001.md`.

Identité : `PlayerProfileStore.kt` fournit un profil historique `legacy` conservant son ancien stockage et des profils supplémentaires à UUID ; `PlayerStats.kt` les sépare par fichier de préférences ; `UserDataBackup.kt` exporte/importe les fichiers additionnels ; `MainActivity.kt` autorise le choix/création de profil et exige abandon/relance si partie active. `GlobalHallProjection.merge` préserve deux scores globaux distincts même avec pseudo identique, tandis que la courbe locale n'est plus accolée au classement mondial. Les scores historiques sans identité fiable restent historiques, pas reventilés. Référence : `missions/GECKO-HOF-PLAYER-ISOLATION-002.md`.

Cible branche : `0.15.45-dev` / code 80, mêmes package et configuration de signature. CI phone activée + artefact APK/sha256 ; **preuve finale GREEN/validation Samsung encore à obtenir**.


## Validation finale CI du lot 0.15.45-dev — 12/10/2026

- Workflow `Build GeckoDoku phone APK`, run [38314483379](https://github.com/greenpower2669/GeckoDoku/actions/runs/38314483379) : **GREEN**, `gradle :app:testDebugUnitTest :app:assemblePhone` → `BUILD SUCCESSFUL`.
- Étapes Pierre, banque sprites 240p (source 1 305 fichiers, modèle et manifest dans l'APK) et publication de l'artefact : SUCCESS.
- Artefact Actions : `GeckoDoku-v0.15.45-dev-phone` (ID `11775463777`) contenant `GeckoDoku-v0.15.45-dev.apk` et `GeckoDoku-v0.15.45-dev.apk.sha256` ; APK SHA-256 : `ddff9b30a3267573cbf0a1cecad98fa6c76ca7aee94f670533a2169e1701d586`.
- Cette version est le **build de test phone signé avec la clé debug existante**, PAS une publication Google Play. La signature Google Play n'est pas modifiée.
- PR brouillon #4 : https://github.com/greenpower2669/GeckoDoku/pull/4 ; ne pas merger ni publier avant accord explicite.
- **À faire** : recette physique Samsung par Fab (UI, Pierre, quatre modes, deux profils/homonymes, restauration des données). Les anciens résultats localement mélangés, sans identité d'origine, ne peuvent être reclassés automatiquement.


### Ajustement visuel Sudoku Samsung — 13/10/2026
Le pavé Sudoku est calculé à 75 % de sa hauteur précédente, avec borne tactile minimale, et reste ancré bas-centre même après redimensionnement. Les projections horizontales dépassent légèrement le candidat à chaque extrémité pour mieux matérialiser la ligne sans modifier les notes ni le solveur.


## Amorçage première phrase Pierre — 13/10/2026

Mission `GECKO-PIERRE-WARMUP-003`. Le premier début de mot Piper/Sherpa pouvait être mangé/déformé à froid alors qu'une seconde lecture était correcte. La chaîne Pierre est désormais préchauffée dès la construction : chargement Sherpa/Piper, synthèse interne `Bonjour, je suis prêt.`, lecture réelle mais volume nul, attente de la consommation des frames audio, puis disponibilité de la première vraie phrase.

`VoicePcmPlayer` n'alloue plus un AudioTrack STATIC pour chaque phrase : il maintient un `AudioTrack.MODE_STREAM` persistant, réutilisé à sample rate identique. La fin est fondée sur `playbackHeadPosition`, pas sur `duration + délai`. Après drainage, la piste reste allouée en pause. La première vraie demande est sérialisée derrière l'amorce sur l'executor mono-thread de `PierrePiperSpeechEngine`. Fallback Android inchangé. Validation Samsung encore requise.


Validation CI de l'amorçage : run `38436224654` / #450 GREEN ; build phone, tests, packaging Pierre et sprites réussis. La résolution acoustique reste conditionnée au test réel Samsung.


## Sudoku visuel — candidats calculés et Binaire (14/10/2026)

L'audit Samsung a montré que les anciens outils paraissaient morts car `SudokuValueOverlayView` ne raisonnait que sur `snapshot.notesAt(cell)`. Désormais `SudokuVisualReasoning.logicalCandidates(values)` calcule les candidats possibles via `SudokuSolver.candidateMask`. Cette carte est purement visuelle et ne modifie ni les notes du joueur ni le moteur.

Lignes, Couples et Triplets utilisent la carte logique. Quand un outil analytique est actif, les candidats calculés non saisis manuellement sont rendus plus légers pour que les repères correspondent à des chiffres visibles. Entourer valide aussi contre cette carte et peut donc fonctionner sans préparation manuelle des notes.

Nouvelle option **Binaire** : recherche, pour chaque chiffre et chaque ligne/colonne/bloc, d'exactement deux occurrences candidates. Même paire multi-unité dédoublonnée. Les deux chiffres reçoivent deux anneaux et une pulsation lente (2,4 s, jamais disparition complète). Plusieurs binaires simultanés sont autorisés.

Le panneau Prévisu reçoit maintenant un texte d'aide multi-ligne sur l'option togglée ; Pierre lit la même explication. Le pavé est compacté : header à 7,5 %, titre de sous-panneau à 12 %, 1 px d'espace visuel entre tuiles, barre d'options 46 dp et padding 1 px.


Preuve CI Binaire : Run `38504109994` / #464 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11867790810`, APK SHA-256 `356cb9973995d453d6e82e865b959f4648502624705322502e23d711ec52cb5e`. La logique compile et les tests passent ; l'ergonomie réelle reste à confirmer sur Samsung.


## Projection complète et pseudo direct — 14/10/2026

Régression UI corrigée : `SettingsEntry.PLAYER_NAME` redevient un accès direct au pseudo public (`editPlayerName`), tandis qu'un nouveau `PLAYER_PROFILE` ouvre la gestion des identités locales séparées. Ainsi modifier son pseudo n'efface ni ne fusionne les statistiques du profil.

Projection Sudoku : `projectionSegments` détecte désormais exactement deux occurrences candidates d'un chiffre dans chaque ligne et chaque colonne. L'overlay prolonge l'axe correspondant sur toute la grille avec faible alpha et faible épaisseur, plus deux anneaux discrets sur les occurrences. « Lignes » devient « Projection ». Les blocs 3×3 restent couverts par Binaire et les repères du Prof, pas par une ligne artificielle.

Pierre : les traces Sudoku sont regroupées par type de projection (ligne, colonne, bloc), au maximum quelques étapes courtes au lieu d'une phrase par source. Vocabulaire plus humain et ordre calme. Les tracés orange du raisonnement sont semi-transparents et plus fins.


Preuve CI projection complète : Run `38519610024` / #472 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11873097206`, APK SHA-256 `562218c0d5cc2622e106a582bd5ba8a9bdec667fbec84fc72d0bedb3052ec4b7`. La compilation et les tests passent ; la validation d'usage reste le test Samsung.

## Repères humains et aide surhumaine séparés — 14/10/2026

Le pavé Sudoku n'embarque plus la barre d'analyse automatique.

Deux fenêtres indépendantes :
- `sudokuManualToolsPopup` : repères graphiques du joueur. `SudokuVisualToolMode` = NONE / PROJECTION / PAIR / TRIPLET / CANDIDATE. Les données sont stockées séparément dans `sudokuManualProjectionRefs`, `sudokuManualCandidateRefs`, `sudokuManualPairGroups`, `sudokuManualTripletGroups`; elles sont envoyées à `SudokuValueOverlayView` sans toucher à `SudokuGameEngine.notes`.
- `sudokuSuperHelpPopup` : aides calculées. Elle n'est accessible qu'après `?` puis tap plateau + confirmation explicite. Options auto OFF au démarrage d'une nouvelle grille.

Projection humaine : case sélectionnée + chiffre choisi dans Candidats → `SudokuCandidateRef`, rendu du petit candidat + croix horizontale et verticale sur tout le plateau, semi-transparente. Couple/triplet sont des ensembles de cellules choisis manuellement. Le candidat manuel est une pièce visuelle seulement.

Aide automatique : `logicalCandidates(values)` reste le moteur de lecture seule. `showVisualCandidates` est maintenant distinct des autres flags, donc Projection/Couples/Triplets/Binaire n'affichent plus tout le tableau de candidats par effet de bord. Chaque aide rend seulement ses candidats concernés ; la checkbox Candidats seule révèle l'ensemble des candidats possibles.

Le `?` de `SudokuQuickPaletteView` expose maintenant `onHelpModeChanged`. Si le prochain clic touche le plateau, `MainActivity` désarme l'aide et ouvre la confirmation surhumaine ; si le clic touche une zone du pavé, l'aide locale courte de Pierre est jouée et le mode aide est désarmé.


Preuve CI repères humains / aide surhumaine : Run `38522492488` / #486 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11875030286`, APK SHA-256 `793a3878817fa04f9b2096d3ac032c8f235c98df50f6a7f56df0139dda062d15`. La compilation et les tests passent ; la validation d'ergonomie reste à faire sur Samsung.

## HOF global complet / progression mono-joueur (14/10/2026)

Règle canonique : le Hall est un comparatif global ; la progression est une analyse personnelle. Le pseudo est seulement un libellé public, jamais une clé d'identité.

`PlayerProfileStore` possède désormais deux notions : `id` pour le stockage local des stats et `progressionId` opaque pour relier les scores Hall d'un même joueur. Le profil historique reçoit un UUID persistant `legacy_progression_id_v1`; les profils UUID existants réutilisent leur UUID comme progressionId. Renommer un profil ne change pas progressionId.

`HallOfFameEntry.playerProfileId` est nullable. Les nouvelles lignes locales le reçoivent ; `GlobalScoreCompletionBridge` publie `metadata.playerProfileId`; `GlobalHallProjection` le relit. Les anciennes lignes sans preuve restent null et ne nourrissent aucune sous-progression.

Le merge global ne confond jamais deux IDs connus différents. Un score distant sans metadata ne peut emprunter l'identité locale que sur correspondance sportive exacte et non ambiguë ; en cas d'ambiguïté, aucune identité n'est inventée.

`HallPlayerProgressionProjection` regroupe uniquement par playerProfileId, conserve tous les scores chronologiquement, les changements de pseudo et calcule tendances temps/étoiles. Le Hall de niveau n'a plus `.take(10)` et le store local n'a plus de limite 200 : classement complet + boutons de sous-progressions séparées.

Pierre : `statsStore` reste lié à `activeProfileId`. Au changement/création de profil, la parole en cours est stoppée, `ProfessorLifeController` est recréé, la session et l'état ambiant sont réinitialisés. `announcePlayerStats()` utilise exclusivement `statsStore.professorLevels()` et préfixe par le pseudo actif. Les progressions d'autres joueurs dans le Hall sont silencieuses.


Preuve CI HOF global / progression : Run 38526555276 / #510 : GREEN. Commit code validé 29151fc63bd9f8b05d5ba188276f6cfa197e5998. Tests JVM + assemblePhone réussis, Pierre et banque sprites 240p vérifiés. Artefact GeckoDoku-v0.15.45-dev-phone ID 11876601009. APK SHA-256 eec808012b45e21a36a400672b1c50d7b2db41a27df9310db2df30eccef45981. Fonctionnellement, la recette Samsung doit encore confirmer l'ergonomie des sous-progressions et la synchronisation réelle de metadata.

## Repères compact + foreground lease Pierre — 14/10/2026

`SudokuFloatingWindowGeometryPolicy` calcule un placement compact au-dessus du plateau et reclamp toute géométrie sauvegardée à la taille écran courante.

`showSudokuManualToolsPopup()` attend désormais un plateau réellement mesuré, ouvre Repères sur la largeur du plateau et une hauteur nominale de 78 dp (barre 34 dp + outils 44 dp), titre raccourci `✎ Repères humains`. Le drag mémorise x/y ; width/height sont aussi persistés pour compatibilité avec tout redimensionnement futur.

Les géométries runtime des trois PopupWindow Sudoku sont suivies : Repères humains, Aide surhumaine, pavé/clavier. Quand `ProfessorSpeech.onSpeakingChanged(true)` arrive, les PopupWindow restent vivants mais sont déplacés hors écran avec clipping désactivé ; Pierre (portrait/vidéo/bulle) devient visuellement prioritaire. Quand `speaking=false`, les PopupWindow sont replacés à leurs coordonnées/taille précédentes et le clipping est restauré. Aucun dismiss/recreate, donc aucun listener de fermeture ni état outil/clavier perdu.


Preuve CI Repères compact / foreground Pierre : Run `38537609942` / #522 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11880727507`, APK SHA-256 `3f6dbba028b4bbb793ed16cbf3a9bd3eb237f36963c4e9fd8a292da21fed911c`.

## Correctif zone sûre PopupWindow Sudoku — 14/10/2026

Voir `missions/GECKO-SUDOKU-SYSTEM-BARS-005.md`. `sudokuPopupSafeBounds()` déduit les bornes en coordonnées de fenêtre par intersection root mesuré / insets `systemBars|displayCutout`, sans retrancher deux fois les barres déjà appliquées au root. `SudokuFloatingWindowGeometryPolicy.clampToBounds()` et `bottomCentered()` gouvernent désormais Repères, Aide et pavé pour drag / reprise de géométrie / resize / placement par défaut. Les popups ouverts sont reconciliés après changement des insets ; le système de calques Pierre ne ferme toujours rien. Tests géométrie de navigation Android, edge-to-edge, rotation ajoutés ; compilation CI et validation Samsung en attente.

## Projection humaine / hypothèse : axes distincts — 14/10/2026

`GECKO-SUDOKU-MANUAL-PROJECTION-006` corrige la croix systématique des repères humains. `SudokuManualProjectionPolicy.axesFor()` retient une ligne si les marques du même chiffre sont alignées en ligne, une colonne si elles sont verticales ; repère isolé / égalité = ligne (jamais les deux axes). Une hypothèse réellement présente dans `SudokuSnapshot.hypothesisAt` projette ligne+colonne, très discrètement sous les chiffres. `SudokuValueOverlayView` ne change pas les notes/valeurs. Messages MainActivity corrigés ; tests unitaires ajoutés. CI et validation Samsung à confirmer.

## Correctifs Samsung : intro + pavé sous la grille (14/10/2026) — missions 007/008

PUSH 1 : `SudokuFloatingStartupPolicy` et `canShowSudokuManualTools()` empêchent Repères humains d'apparaître pendant la vidéo d'introduction / rideau de lancement et avant la présence du plateau Sudoku mesuré, ou pendant la parole de Pierre. Les rappels d'ouverture sont différés après fin/saut d'introduction et à la fin de parole. Réouverture manuelle et sauvegarde de position conservées.

PUSH 2 : `SudokuFloatingWindowGeometryPolicy.dockBelowBoard()` établit la zone du pavé à partir du bas réel du plateau et des insets Android ; placement initial, drag, resize et recalage à l'écran exploitent cette zone. La hauteur du pavé s'adapte pour ne pas recouvrir les dernières lignes du Sudoku sur un écran disposant de l'espace requis. Tests JVM ajoutés. Petits écrans/paysage à vérifier sur téléphone avant toute garantie générale.

### Preuve CI Samsung 007/008 — run #525
Validation finale après les missions 007 et 008 : run [#525](https://github.com/greenpower2669/GeckoDoku/actions/runs/38587086273), workflow 38587086273 **GREEN**, commit de build `eee5347f52602ccbc91d1359aeacb161c427d9f1`. `testDebugUnitTest + assemblePhone` réussis ; vérification Pierre Sherpa/Piper et banque sprites 240p réussie. Artefact `GeckoDoku-v0.15.45-dev-phone` ID `11905448115` ; APK `GeckoDoku-v0.15.45-dev.apk` SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`, contrôlé après extraction. La recette Samsung demeure nécessaire. Aucun merge main / Release / Play.

## Preuve GitHub — APK direct #525 (14/10/2026)
Livraison APK brut run #525 : prérelease temporaire `temp-geckodoku-0.15.45-dev-run-525` créée et vérifiée par l'API GitHub. [Téléchargement direct .apk](https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-0.15.45-dev-run-525/GeckoDoku-v0.15.45-dev.apk), 299496421 octets, SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`. Workflow de publication #527 GREEN. La première tentative #526 (403 GitHub) a été corrigée en reprenant la méthode qui fonctionnait : checkout de la branche et création du tag sur le commit déclencheur. Aucun nouveau build Android, aucun merge main ou Release stable. Règle permanente de livraison d'APK direct enregistrée dans la section `Contrat FAB Copilot` d'`ordres-de-mission.md`.

## Correctif affichage pavé Sudoku au premier lancement — mission 009 (14/10/2026)

Cause prouvée : `MainActivity.showSudokuCellPalette` construisait `PopupWindow` aux anciennes dimensions puis ne modifiait que les variables x/y/w/h après `bottomCentered(dock)`. L'Android PopupWindow gardait physiquement sa grande hauteur et recouvrait la grille. `compactKeypadBelowBoard` calcule maintenant avant construction le format compact cible (environ 315×225 dp, aligné à droite sous la ligne 9) ; même largeur/hauteur envoyées au constructeur. `fitOpenSudokuPaletteToDock` contrôle également une fenêtre déjà ouverte et la réalité de `popup.width/height`, conserve positions manuelles valides. Trois tests JVM ciblés. Réf : `missions/GECKO-SUDOKU-KEYPAD-OPEN-COMPACT-009.md`. CI/recette Samsung à suivre.

## Publication AAB Google Play sous contrôle — mission 010 (14/10/2026)

La procédure immuable de signature de la branche `feature/play-upload-signing-v1` est intégrée dans `ordres-de-mission.md` sans clé privée ni secret. `.github/workflows/build-play-aab.yml` ajouté à la branche Sudoku ; JKS recréé uniquement en RAM/disque temporaire runner depuis GitHub Secrets chiffrés ; SHA-1 de l'Upload Key vérifié contre le nouveau certificat Play (`27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`) avant `bundleRelease`. Publication de l'AAB signé uniquement après merge `main`, avec re-build et `jarsigner`. Pas d'envoi automatisé Google Play. Ordre explicite Fab « merge release avec la secret key ». Mission `GECKO-PLAY-MERGE-SIGNED-RELEASE-010`. STOP si Secrets absents ou empreinte divergente.

## Publication 0.15.45-dev signée Google Play (14/10/2026)

**Clôture validée 14/10/2026 — mission 010.** Feu vert de Fab « merge release avec la secret key ». PR #4 mergée sur `main`, SHA merge `f48dcd7d6898793ccd6d63b22c0bc20644d6966a`. Contrôle pré-merge AAB sur branche : workflow `38636402228` GREEN (certificat Upload Key conforme au Play SHA-1 `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`, tests, Pierre et sprites 240p, `jarsigner -verify`). Build officiel depuis main : workflow `38636963727` GREEN, tests + `bundleRelease`, même contrôle certificat, `jar verified.`. GitHub Release **publique/stable** : https://github.com/greenpower2669/GeckoDoku/releases/tag/geckodoku-v0.15.45-dev-code80 ; AAB signé direct : https://github.com/greenpower2669/GeckoDoku/releases/download/geckodoku-v0.15.45-dev-code80/GeckoDoku-v0.15.45-dev-code80-signed.aab ; SHA-256 `08390c0e502380ee5d3b6e2e77e01d491ff2a9535c61524b9b5270eb92cd755c`. Application `com.greenpower2669.geckodoku`, versionName `0.15.45-dev`, versionCode `80`. L'APK téléphone précédent reste un build à signature test : NE PAS le présenter comme signé Play. Aucun envoi à Play Console réalisé ; l'acceptation de l'AAB par Google n'est pas encore démontrée. Aucune clé privée ni valeur de Secret dans Git.


## 15/10/2026 — GECKO-INFERNAL-NESTED-011
Ordre explicite Fab « Ok, code ça entre à 3 imbrication ou plus ». En mode GeckoDoku CLASSIC uniquement, épuiser les techniques déterministes avant chaque hypothèse. Mission Impossible = une hypothèse (profondeur 1). Infernal = contradiction démontrée à au moins trois niveaux H1 → H2 → H3 imbriqués, et non par trois hypothèses successives indépendantes. Une branche indécise à la limite de recherche n'est pas une solution ni une contradiction. Unicité indépendante de toute grille : exactement une solution complète. Recherche bornée à profondeur 3 dans cette première réalisation. Branche feature/infernal-nested-depth3-v1 ; aucun merge, Release ni Play autorisé.

Preuve CI du commit fonctionnel d394a65 : run 38800346305 / #533 GREEN, tests JVM + assemblePhone + vérifications Pierre/sprites. Artefact GeckoDoku-v0.15.45-dev-phone (ID 12003858392), APK de test SHA-256 ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3. Validation profondeurs sur une grille réelle et Samsung encore à faire.


## Livraison APK temporaire Infernal #533 — 15/10/2026
La CI de livraison #534 (run 38802231998) est GREEN. Artefact source #533 (12003858392) téléchargé et extrait dans le dossier temporaire GitHub Actions `temp/GeckoDoku-infernal-depth3-run533`. SHA-256 APK vérifié : `ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3`. APK brut de test : https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-infernal-depth3-run533/GeckoDoku-v0.15.45-dev.apk. Prérelease TEMP uniquement, sans fichier binaire ajouté au Git tree ; aucun merge, Release stable, AAB ni publication Play.


## GECKO-INFERNAL-PROOF-SEARCH-012 — 16/10/2026
Suite de 011, ordre explicite Fab « Ok corrige ça ». Branche feature/infernal-proof-search-v2 créée depuis 011 : correction du moteur CLASSIC de génération Infernal profondeur ≥3. HypothesisSolver choisit toutes les bifurcations disponibles (paires et candidats vrai/faux), mémoïse les états (placements/exclusions/profondeur), distingue Contradiction/Solved/Inconclusive et arrête les preuves selon limites de temps/nœuds/annulation. Les hypothèses de profondeur inférieure sont testées AVANT plus profond. Une recherche superficielle interrompue interdit de certifier la nécessité de profondeur 3. PuzzleGenerator.generateExact a une recherche finie (~60 s et 8 séries max pour Infernal), annulation transmise aux analyses et au décompte des solutions, aucun downgrade silencieux. MainActivity annonce absence de grille plutôt que chercher sans fin. Unicité de solution contrôlée indépendamment. Les budgets peuvent empêcher de trouver une grille pourtant existante ; ne pas promettre de génération réussie tant qu'une graine de profondeur 3 n'est pas certifiée.

**CI finale code 012** : run #539 / 38859558302 GREEN sur ce6d855db774734793b2f811b6e112c56f019bd5 ; Gradle BUILD SUCCESSFUL, tests JVM + assemblePhone, Pierre et banque sprites 240p. Artefact Actions GeckoDoku-v0.15.45-dev-phone (ID 12028672971), APK test SHA-256 ba4acee944de16aa34951cd7f22a3ed3a8423e1e71e58e3b75a866086549fbd5. CI ne certifie pas encore une graine Infernal profondeur 3 produite.


## GECKO-CLASSIC-WORKSHOP-013 — moteur différé de grilles exigeantes (16/10/2026)
Ordre explicite de Fab : « Parfait, code ça » après validation d'un atelier de génération de grilles CLASSIC avec ≥1 Gecko X-Wing et ≥2 branches d'hypothèses dépendantes. Travail isolé sur feature/classic-infernal-workshop-v1, issu de feature/infernal-proof-search-v2, sans main/release/AAB. Génération candidate avec contrôle d'unicité indépendant, essai de preuves profondeur H2/H3, comparaison avec et sans X-Wing, puis persistance atomique individuelle dans filesDir/infernal_workshop/records. Tâches de fond Android WorkManager par petits lots, périodiques à l'électricité/batterie non faible, lot immédiat batterie non faible, pausable et annulable, suivi tentatives. Atelier dans les réglages Classic : essai réel, note, statuts attente/test/validé/rejeté, export ZIP JSON (solution complète et trace) ou import qui remet toujours à l'état « candidat ». Les grilles de test n'émettent aucun score Hall local/global ni statistiques. Celles validées manuellement après test rejoignent la sélection Infernal avec métadonnées H2/H3 explicites. Leurs preuves requièrent une revue humaine : les budgets du solveur ne permettent pas encore une preuve exhaustive d'indispensabilité de X-Wing pour toutes les résolutions mathématiques. ZIP privé car solutions intégrales.

Amélioration rendement mission 013 : analyser d'abord la profondeur 2 puis la profondeur 3 si besoin ; lorsqu'une comparaison sans X-Wing ou profondeur1 dépasse son budget, conserver la grille comme CANDIDATE avec les drapeaux explicites `xWingExclusionModelComplete` / `shallowExclusionModelComplete` au lieu de la perdre ; ne jamais annoncer que l'absence de X-Wing est mathématiquement impossible. La promotion manuelle reste soumise à essai préalable.

**CI finale mission 013** : workflow build phone #545 / run 38894030798 GREEN, code commit fcc114e4b5e3739117ad160cb3ac6abd429f365e, tests JVM + assemblePhone + contrôles Pierre/sprites 240p SUCCESS. Artifact Actions GeckoDoku-v0.15.45-dev-phone ID 12043056881 ; APK SHA-256 `1e49ba40e09e316d6857a12df33bda533cb5fa9c2c3aa0e4fd9892f9f0162e78`. Aucune recette de génération réelle Samsung ni benchmark de rendement à ce stade ; aucun merge ni Release.

## GECKO-CLASSIC-WORKSHOP-MULTISIZE-014 — autorisation de Fab du 16/10/2026
À la suite de l'audit confirmant Classic 5..12 et 12 couleurs, Fab ordonne « Ok corrige ça ». Branche `feature/classic-workshop-multisize-h4-v2` basée sur l'atelier 013. `InfernalWorkshopPolicy` centralise tailles 5 à 12, 2 niveaux imbriqués minimum, 4 maximum, X-Wing facultatif 5..7 et obligatoire 8..12. `InfernalWorkshopScheduler` utilise un curseur persistant `next_size_v2` (5→6→...→12→5), compte tentatives `attempts_v2_N` et dernières recherches par taille, n'oublie pas les travaux précédents à l'installation de la mise à jour ; active périodique existante KEEP, lots unitaires opportunistes, verrou inter-worker. `PuzzleGenerator.generateWorkshopCandidate` essaie successivement profondeur H2/H3/H4 et adapte condition X-Wing. `InfernalWorkshopStore` accepte désormais H2..H4 sans X-Wing pour petites tailles, vérifie le contrat à l'import et à la promotion. Interface atelier : statut de la rotation, bilan détaillé des huit tailles, filtres de grilles par taille. Les 12 couleurs et une région/Gecko par taille existaient déjà et restent inchangés. Aucune garantie de trouver un candidat de chaque taille ni de recherche continue en arrière-plan. La notion « X-Wing nécessaire » reste bornée par les modèles de résolution, drapeaux d'incertitude conservés.

**CI finale atelier 014 (16/10/2026)** : run GitHub Actions #550 `38921744094` GREEN, commit code `b56fce177805e103028c09bdfd6d9772ad7e8bc0`, Gradle `BUILD SUCCESSFUL in 2m 4s`, tests JVM, APK phone, Pierre/banque 240p vérifiés. Artefact Actions `GeckoDoku-v0.15.45-dev-phone` ID `12055281956`, APK SHA-256 `d879a10ea9ece31dcb866fadb83ea438826f52439c9a0881baf4664d569b966d`. Les #547–#549 avaient échoué pendant l'intégration : #547 ancien appel `start(context,size)`, puis #548/#549 ancien test exigeant un X-Wing sur 5×5 ; API et test corrigés avant #550. Pas de merge main, de Release ni de Google Play. Essais réels sur Samsung / démonstration de grilles H2–H4 restent à effectuer.

## GECKO-CLASSIC-WORKSHOP-CONTINUOUS-015 — Recherche continue (16/10/2026)
Fab signale sur le Samsung Android 16 : « Pas de candidats, normal ? », journal média de 17:47 à 17:49 sans aucun évènement du moteur ; puis « recherche en continue », compteurs essais tous à zéro. Réalisation sur `feature/classic-workshop-continuous-fgs-v3` depuis l'atelier 014 : service Android en premier plan `InfernalWorkshopService` démarré explicitement depuis le menu Atelier, notification persistante « GeckoDoku • recherche continue » avec Arrêter, FGS `specialUse`/permissions dans `AndroidManifest.xml` (validation Google Play du type non acquise, test seulement). L'ancien WorkManager périodique avec contrainte de chargeur est annulé et son `Worker` conservé comme stub afin de ne pas casser les tâches anciennes. Recherche séquentielle et continue en rotation 5×5→12×12, boucle tant qu'activée, recherche ≤20s par taille, repos 250ms entre tailles, pause de sécurité si chauffe sévère ou économie de batterie, l'OS peut malgré tout interrompre le service. Compteurs persistants de recherches lancées/réellement terminées/grilles sauvegardées/erreurs par taille, dernière activité, taille courante, prochaine taille, phase, dernier échec ; message de l'atelier actualisé toutes les 1,5s sans fermer le dialogue ; fonction de redémarrage si interrompu. Journal média existant (MediaTrace) reçoit START_REQUEST, SERVICE_ACTIVE, SEARCH_START, SEARCH_END, THERMAL_PAUSE, BATTERY_SAVER_PAUSE, ERROR, STOP_NOTIFICATION et SERVICE_STOPPED. Règles X-Wing 5–7 facultatif / 8–12 requis, H2–H4, bibliothèque JSON et exports ZIP conservés sans réinitialiser la progression. Aucun résultat réel confirmé sur le Samsung ; comparer compteur/notification/log après installation. Aucune fusion main, Release ni Play.

### CI atelier 015 — 16/10/2026
Run téléphone #557 `38929818514` GREEN sur commit `ad65fab01d05d0ec3751246d8d8446d6a64fabe7`. `BUILD SUCCESSFUL in 2m 33s`, tests JVM, `assemblePhone`, contrôles de Pierre et sprites 240p OK. Artefact Actions `GeckoDoku-v0.15.45-dev-phone` id `12058666334` ; SHA-256 APK `da540f8e19007d7d4fe932df13d30c00a09160958560adbe5f15b6d26dede8af`. Les anciennes CI #552, #554, #555 étaient aussi vertes. APK brut non publié : seul l'artefact ZIP Actions existe ; ne pas inventer un lien direct .apk ni créer de prérelease sans nouvelle demande. Aucun test Android 16 réel effectué. Aucun merge main, aucune Release/Play. Un foreground service est susceptible d'être interrompu par Android ; `specialUse` nécessite examen Play avant diffusion publique.
