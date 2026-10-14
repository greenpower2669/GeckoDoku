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
