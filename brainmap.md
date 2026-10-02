# GeckoDoku — brainmap

> Cartographie technique active : responsabilités, dépendances et flux.
> Usage détaillé : docs/GECKODOKU-FONCTIONNEMENT.md.
> Contrat fonctionnel : brain.md.
> Archive froide : sauvegarde.md — ne pas lire par défaut.

## 0 — Vue générale

~~~mermaid
flowchart TD
    UI[MainActivity / UI commune] --> MODE{GameMode}
    MODE --> C[GeckoDoku Classic]
    MODE --> S[Sudoku]
    MODE --> G[Gomoku]
    MODE --> B[Abeilles & Geckos]

    C --> CE[GameEngine]
    C --> CS[PuzzleGenerator + HumanSolver + HypothesisSolver]
    C --> CV[GeckoBoardView]

    S --> SE[SudokuGameEngine]
    S --> SS[SudokuGenerator + SudokuSolver + HintEngine]
    S --> SV[SudokuBoardView + overlays]

    G --> GE[GomokuGameEngine]
    G --> GA[GomokuAi]
    G --> GV[GomokuBoardView + Viewport]

    B --> BE[BeeGeckoGameEngine]
    B --> BS[BeeGecko generator / solver]
    B --> BV[BeeGeckoBoardView + Viewport]

    UI --> P[Prof Gecko]
    P --> SP[ProfessorSpeech]
    SP --> PP[PierrePiperSpeechEngine]
    PP --> PR[PierrePronunciationPolicy]
    PR --> VO[VoicePcmPlayer / Android audio]

    UI --> RM[Rich media / ChromaKey overlays]
    UI --> DATA[Stats / Hall / Journal / Backup]
~~~

MainActivity.kt orchestre mode, moteurs, gestes, Prof, overlays, statistiques et menus.
Chaque moteur reste source de vérité de son mode.

## 1 — Mode, préférences et navigation

Fichiers :
- GameMode.kt : GameMode, GomokuMatchMode, SudokuVisualStyle, GameModePreferences ;
- SettingsMenuPolicy.kt ;
- AppTitlePolicy.kt ;
- MainActivity.kt.

~~~mermaid
flowchart LR
    Settings[Réglages] --> Chooser[Mode de jeu]
    Chooser --> Prefs[GameModePreferences]
    Prefs --> Switch[MainActivity.setGameMode]
    Switch --> Cleanup[nettoyage ancien mode / Prof / overlays]
    Cleanup --> Start[démarrage ou restauration moteur cible]
    Start --> Visibility[applyGameModeVisibility]
    Visibility --> Layout[positionFloatingBoard]
~~~

Invariants :
- titre toujours GeckoDoku 🦎 ;
- changement de mode nettoie les états temporaires incompatibles ;
- préférences persistées ;
- overlays non participants à la géométrie logique du plateau.

## 2 — GeckoDoku Classic

Modèle / moteur :
- GameModel.kt : Cell, GameDifficulty, Puzzle, GameSnapshot, hypothèses, repères ;
- GameEngine.kt : confirmed, manualCrosses, autoCrosses, hypotheses, customMarkers, axisGuides, mistakes.

Génération / résolution :
- PuzzleGenerator.kt ;
- HumanSolver.kt ;
- HypothesisSolver.kt ;
- DifficultyIndex.kt.

~~~mermaid
flowchart TD
    Req[Nouvelle + taille + difficulté] --> Gen[PuzzleGenerator]
    Gen --> Unique[unicité + contraintes]
    Unique --> Human[HumanSolver]
    Human -->|résolu| Rate[DifficultyIndexer]
    Human -->|bloqué haut niveau| Hyp[HypothesisSolver borné]
    Hyp --> Rate
    Rate --> Exact{niveau demandé ?}
    Exact -->|non| Gen
    Exact -->|oui| Puzzle[Puzzle accepté + solverTrace]
    Puzzle --> Engine[GameEngine]
    Engine --> View[GeckoBoardView]
    Engine --> Prof[ProfessorGecko]
~~~

GameEngine.toggleGecko :
- given → refus ;
- auto-cross → refus ;
- Gecko déjà présent → retrait ;
- faux Gecko → erreur + croix ;
- Gecko valide → confirmation ;
- dernier Gecko → complétion.

computeAutoCrosses :
Gecko confirmé → même ligne + même colonne + même région + voisinage 8 directions.

Axes Classic :
- AxisGuide.kt : AxisGuideColor, ClassicAxisGuideKind, ClassicAxisGuide ;
- GeckoBoardView.kt : rendu + sélection/drag.

~~~mermaid
flowchart LR
    D[Double-clic] --> LP[palette logique]
    LP --> A[Axe]
    A --> HV[Horizontal / Vertical]
    HV --> C[Jaune / Vert / Rouge]
    C --> E[GameEngine.toggleAxisGuide]
    E --> V[GeckoBoardView]
    V --> Drag[drag]
    Drag -->|dedans| Move[moveAxisGuide avec couleur]
    Drag -->|hors plateau| Delete[suppression]
~~~

ClassicProfessorAxisGuidePolicy produit les axes pédagogiques du Prof, séparés des couleurs joueur.

## 3 — Sudoku

Fichiers principaux :
- SudokuModel.kt ;
- SudokuGenerator.kt ;
- SudokuGameEngine.kt ;
- SudokuSolver.kt ;
- SudokuHintEngine.kt ;
- SudokuBoardView.kt ;
- SudokuValueOverlayView.kt ;
- SudokuQuickPaletteView.kt ;
- SudokuCandidateLayout.kt ;
- SudokuGesturePolicy.kt ;
- SudokuProfessorInteractionPolicy.kt ;
- SudokuProfessorCandidatePolicy.kt ;
- SudokuReasoningTrace.kt.

SudokuGameEngine maintient :
values, notes, geckoMarkers, customMarkers, givenMask, undoStack, redoStack, mistakes, lastMoveOrigin.

~~~mermaid
flowchart TD
    Touch[SudokuBoardView] --> GP[SudokuGesturePolicy]
    GP -->|simple| Select[sélection / Gecko]
    GP -->|double| Mark[repères personnels]
    GP -->|long| Palette[SudokuQuickPaletteView]
    Palette --> Digit[chiffre]
    Palette --> Note[candidat]
    Palette --> Gecko[marqueur Gecko]
    Palette --> Erase[effacer]
    Digit --> Engine[SudokuGameEngine]
    Note --> Engine
    Gecko --> Engine
    Erase --> Engine
    Engine --> View[Board + ValueOverlay]
~~~

Un chiffre accepté retire la même note des pairs ligne/colonne/bloc.

Prof Sudoku :
~~~mermaid
flowchart LR
    Prof[Prof Gecko] --> Policy[SudokuProfessorInteractionPolicy]
    Policy --> Explain[Explain]
    Policy --> Apply[Apply]
    Explain --> Hint[Hint + reasoning + overlays]
    Apply --> Engine[SudokuGameEngine]
~~~

Tap Prof = conseil/exposé.
Appui long = demande directe.

## 4 — Gomoku

Fichiers :
- GomokuModel.kt ;
- GomokuGameEngine.kt ;
- GomokuAi.kt ;
- GomokuBoardView.kt ;
- GomokuViewportPolicy.kt ;
- GomokuUiPolicy.kt ;
- GomokuMatchPolicy.kt ;
- GomokuProfessorPersona.kt.

GomokuGameEngine maintient stones, currentPlayer, winner, winningLine, draw.
GomokuWinDetector teste quatre directions et gagne à partir de 5 alignés.

~~~mermaid
flowchart LR
    Pointer[entrée tactile] --> Policy[GomokuGesturePolicy]
    Policy -->|tap| Play[GomokuGameEngine.play]
    Policy -->|drag| Pan[GomokuViewportPolicy.pan]
    Policy -->|2 doigts / scale| Zoom[GomokuViewportPolicy.zoom]
    Pan --> Board[GomokuBoardView]
    Zoom --> Board
    Play --> Board
~~~

Viewport :
- initial ≈ 12 ;
- minimum ≈ 5 ;
- clamp dans le plateau ;
- zoom centré sur le focus.

Contre Prof :
~~~mermaid
flowchart TD
    Human[coup humain vert] --> Engine[GomokuGameEngine]
    Engine --> State{fin ?}
    State -->|non| AI[GomokuAi + profil difficulté]
    AI --> Decision[GomokuAiDecision + reasoning]
    Decision --> Engine
    Decision --> Overlay[lignes / menaces / projection]
~~~

GomokuMatchPolicy sépare :
- tour contrôlé par Prof ;
- conseil disponible ;
- appui long pouvant appliquer un coup humain conseillé.

HUMAN_VS_HUMAN : aucun camp contrôlé par IA.

## 5 — Abeilles & Geckos

Fichiers :
- BeeGeckoModel.kt : HexCoord, HexAxis, BeeGeckoPiece, BeeGeckoPuzzle, BeeGeckoGameEngine, génération/solveur/difficulté ;
- BeeGeckoAxisGeometry.kt : projection pointy-top et présentation canonique Q/R/S ;
- BeeGeckoBoardView.kt ;
- BeeGeckoViewport.kt ;
- BeeGeckoLogicalMarkers.kt ;
- BeeGeckoSessionStore.kt.

BeeGeckoPuzzle impose :
- paire Gecko/Abeille par zone ;
- voisinage de la paire ;
- 1 Gecko + 1 Abeille par zone ;
- exclusivité globale 1↔1 ;
- au plus une pièce de chaque type par axe Q/R/S.

~~~mermaid
flowchart TD
    Req[difficulté] --> Search[BeeGeckoGenerator.generateExact]
    Search --> Candidate[puzzle candidat]
    Candidate --> Rate[difficulty report]
    Rate --> Exact{niveau exact ?}
    Exact -->|non| Search
    Exact -->|oui| Start[startBeeGeckoGame]
    Start --> Engine[BeeGeckoGameEngine]
    Start --> Session[BeeGeckoSessionStore]
    Engine --> Board[BeeGeckoBoardView]
~~~

La recherche est hors thread UI et annulable par token.

BeeGeckoLogicalMarks contient :
- geckoCandidate ;
- beeCandidate ;
- excludedAxes ;
- axisColors.

Géométrie canonique écran :
- Q constant → +60° → ↖↘ ;
- R constant → 0° → ←→ ;
- S constant → -60° → ↙↗.

BeeGeckoAxisGeometry est la source commune de BeeGeckoBoardView, de la légende UI, de la popup du double-clic et des libellés du Prof.

~~~mermaid
flowchart LR
    Double[Double-clic] --> Piece[Gecko / Abeille / Axe]
    Piece -->|Axe| QSR[Q / S / R]
    QSR --> Color[Jaune / Vert / Rouge]
    Color --> Marks[BeeGeckoLogicalMarks]
    Marks --> Render[BeeGeckoBoardView]
    Render --> Drag[drag axe]
    Drag --> Session[session persistée]
~~~

Schema courant 4.
Ancien axe sans couleur → rouge.

## 6 — Prof Gecko / Pierre

Blocs principaux :
- ProfessorGecko.kt ;
- ProfessorSpeech.kt ;
- ProfessorBubbleView.kt ;
- ProfessorLifeController.kt ;
- ProfessorPlayerContext.kt ;
- ProfessorPhraseCatalog.kt ;
- ProfessorPhraseSelector.kt ;
- ProfessorPhraseHistory.kt ;
- politiques Professor...Policy ;
- PierrePiperSpeechEngine.kt ;
- PierrePronunciationPolicy.kt ;
- VoicePcmPlayer.kt ;
- AudioCapturePolicy.kt.

~~~mermaid
flowchart LR
    Logic[texte pédagogique / ambiance] --> Speech[ProfessorSpeech]
    Speech --> Piper[PierrePiperSpeechEngine]
    Piper --> Pron[PierrePronunciationPolicy]
    Pron --> Sherpa[Sherpa/Piper local]
    Sherpa --> PCM[VoicePcmPlayer]
    PCM --> Android[AudioTrack MEDIA + SPEECH]
~~~

Règles :
- UI non mutée par la prononciation ;
- parole pédagogique prioritaire ;
- action joueur peut invalider un hint temporaire ;
- le Prof n’applique que ce que le mode autorise ;
- couleurs joueur ≠ rendu Prof.

GECKO-048 :
mot entier église, quelle que soit la casse détectée → eglize dans le flux vocal.

## 7 — Rich media / rendu

Fichiers :
- RichMediaOverlayView.kt ;
- AliveAnimator.kt ;
- AliveMascotOverlayView.kt ;
- MascotAnimationProfiles.kt ;
- MascotRenderPolicy.kt ;
- ChromaKeyVideoView.kt ;
- RichMediaPlaybackRegistry.kt ;
- RichMediaScheduler.kt ;
- RichMediaSettings.kt ;
- AssetMediaCatalog.kt ;
- GeckoMediaAudioPolicy.kt ;
- MediaRenderGeometry.kt ;
- PersistentMediaLog.kt ;
- VictoryCelebrationView.kt ;
- AssetAudioPlayer.kt.

~~~mermaid
flowchart TD
    Event[événement jeu/Prof] --> Policy{animations ON ?}
    Policy -->|non| Normal[rendu normal]
    Policy -->|oui| Overlay[RichMediaOverlayView]
    Overlay --> Video[ChromaKeyVideoView]
    Video --> Frame[frame transparente]
    Frame --> Compose[composition au-dessus du plateau]
    Video -->|erreur| Fallback[rendu normal conservé]
~~~

Invariants :
- aucune dépendance logique vers média ;
- géométrie média dérivée de la cible réelle ;
- pas de reflow ;
- muted = piste audio désélectionnée ;
- logs média séparés du gameplay.

## 8 — Score / stats / profil / Hall

Fichiers :
- CompletionRating.kt ;
- PlayerStats.kt ;
- PlayerProfileStore.kt ;
- HallOfFameStore.kt.

~~~mermaid
flowchart LR
    Start[début] --> Stats[recordStart]
    Help[aide] --> Assist[assistancePoints]
    Error[erreur] --> Mistakes[mistakes]
    End[victoire] --> Stars[CompletionRatingPolicy]
    Assist --> Stars
    Mistakes --> Stars
    Stars --> Complete[recordComplete]
    Complete --> Hall[Hall of Fame]
~~~

Score final borné 1..5.

## 9 — Persistance / sauvegardes

Stores :
- GameModePreferences : mode, difficulté, taille, style, match ;
- PuzzleJournalStore : grilles Classic ;
- BeeGeckoSessionStore : puzzle/caméra/repères Bee ;
- ProfessorPhraseHistory : anti-répétition ;
- PlayerStatsStore : stats ;
- PlayerProfileStore : nom ;
- HallOfFameStore : résultats ;
- RichMediaSettings : animations ;
- UserDataBackup.kt : export/import transversal.

~~~mermaid
flowchart TD
    Stores[stores locaux] --> Export[UserDataBackup.exportJson]
    Export --> SAF[Android create document]
    File[JSON choisi] --> Read[lecture]
    Read --> Validate[validation]
    Validate -->|OK| Apply[écriture stores]
    Validate -->|échec| Keep[état précédent]
    Apply -->|exception| Rollback[restauration snapshot]
~~~

## 10 — Géométrie / couches UI

Composants :
- BoardGeometryPolicy.kt ;
- GameModeBoardGeometryPolicy.kt ;
- vues de plateau ;
- ProfessorBubbleView ;
- RichMediaOverlayView ;
- VictoryCelebrationView.

Ordre conceptuel :
jeu → médias décoratifs → bulle Prof → célébration/feedback selon contexte.

Règle : le plateau est la référence géométrique ; les couches flottantes s’y adaptent, jamais l’inverse.

## 11 — Propagation d’un changement

~~~mermaid
flowchart TD
    Req[demande Fab] --> Contract[brain.md]
    Contract --> Locate[brainmap.md]
    Locate --> Code[code / policies / stores / views]
    Code --> Tests[tests + CI]
    Tests --> Human{validation appareil ?}
    Human -->|oui| Todo[todo.md : à valider]
    Human -->|non| Stable[fonction stable]
    Bug[incident] --> Hist[debughistorical.md]
    Hist --> Locate
~~~

Règles :
- changement de logique → modèle + moteur + solveur + Prof + tests à vérifier ;
- changement visuel → ne doit pas dériver dans la logique ;
- changement de persistance → migration/compatibilité explicite.

## 12 — GECKO-048 : delta actif

Prononciation :
ProfessorSpeech → PierrePiperSpeechEngine → PierrePronunciationPolicy → eglize → Sherpa/Piper.
Affichage conservé.

Axes Classic :
palette axe → ClassicAxisGuide(kind,index,color) → GeckoBoardView → ACTION_DOWN → ACTION_MOVE/UP avec active.color.

Axes Bee :
Q/S/R + couleur → BeeGeckoLogicalMarks.excludedAxes + axisColors → BeeGeckoBoardView → BeeGeckoSessionStore schema 4.

Ancien axe sans couleur → RED.

CI : verte.
Validation téléphone Fab encore requise avant de classer GECKO-048 stable.

## 13 — Frontières documentaires

- brain.md = vérité fonctionnelle compacte.
- brainmap.md = carte technique active.
- docs/GECKODOKU-FONCTIONNEMENT.md = usage et comportement détaillés.
- debughistorical.md = incidents récents/pertinents.
- todo.md = travail agent restant.
- ordres-de-mission.md = objectifs explicites de Fab.
- sauvegarde.md = archive froide avant restructuration ; ne pas lire par défaut.


## 14 — GECKO-049 : axes Bee synchronisés

~~~mermaid
flowchart LR
    Geo[BeeGeckoAxisGeometry] --> Center[cellCenter / plateau réel]
    Geo --> Legend[légende Q/S/R]
    Geo --> Popup[double-clic → Axe]
    Geo --> Prof[BeeGeckoRules.axisLabel]
    Center --> Bars[barres globales + drag]
~~~

Invariants :
- Q = ↖↘, R = ←→, S = ↙↗ ;
- aucune seconde table de symboles d’axe dans MainActivity ;
- drag/persistance continuent d’utiliser HexAxis + axisValue ;
- changer un symbole n’altère jamais les règles du solveur.


## 15 — GECKO-050 : moteur de mascottes vivantes — architecture courante

~~~mermaid
flowchart LR
    Event[pièce / repère / décoration] --> Layer[AliveMascotOverlayView]
    Layer --> Slot[pool vivant borné]
    Slot --> A[AliveAnimator par slot]
    A --> PNG[PNG transparent interne]
    A --> Video[ChromaKeyVideoView]
    PNG --> Bridge[pont pendant first-frame gate]
    Board[Plateau] -->|suspend seulement son PNG statique| Slot
    Plant[Plante] --> Drag[drag + position persistée]
~~~

Capacités :
- 3 Gecko ;
- 2 Abeilles ;
- 1 Plante.

Géométrie :
- Classic Gecko = 80 % de la cellule ;
- Sudoku Gecko = base 66 % ;
- Gomoku = geckoRectOnScreen ;
- Bee/Gecko hex = 0,61 × scale de type ;
- Plante = décor indépendante, déplaçable par drag.

Suppression statique par vue :
- GeckoBoardView.mediaSuppressedGeckos ;
- SudokuValueOverlayView.mediaSuppressedGeckos ;
- GomokuBoardView.mediaSuppressedCells ;
- BeeGeckoBoardView.mediaSuppressedPieces.

Invariants :
- aucun maskColorProvider dans AliveMascotOverlayView ;
- aucune couleur de case dans le pipeline vivant ;
- chaque slot possède son PNG transparent et sa vidéo ;
- libérer/remplacer un slot restaure le PNG statique du plateau concerné ;
- le PNG interne sert de pont inter-clips et reçoit aussi la teinte jaune pour le Gecko Prof ;
- Prof Gecko / Pierre n'utilisent pas ce moteur.

### Prochaine évolution demandée

AliveAnimator.update() doit devenir autonome :
- choix différent à chaque petite animation ;
- mémorisation des séries complètes ;
- jamais la même série juste après ;
- longueur/ordre variables pour éviter les motifs humains ;
- fin de grand cycle : une copine visible différente joue une mignonnerie ;
- autres mascottes repartent sur des cycles désynchronisés ;
- éviter répétition de la même copine et de la même mignonnerie.

Cette évolution est contractuelle mais pas encore codée dans 0.15.10-dev.


## 17 — GivenFogVisualPolicy

~~~mermaid
flowchart LR
    Given[placement donné / grisé] --> Policy[GivenFogVisualPolicy]
    Policy --> Puffs[7 bouffées irrégulières alpha 8..21]
    Puffs --> Classic[GeckoBoardView]
    Puffs --> Hex[BeeGeckoBoardView]
~~~

Le brouillard reste déterministe pour une cellule donnée, avec mouvement doux par phase.
Aucune géométrie de jeu n'en dépend.


## 18 — GECKO-051 : Presence légère + pool vidéo

~~~mermaid
flowchart TD
    Snapshot[état du mode] --> Sync[syncLivingMascotsForCurrentMode]
    Sync --> P[Presence par mascotte visible]
    P --> PNG[PNG transparent interne]
    P --> A[AliveAnimator individuel]
    Pool[3 Gecko / 2 Bee / 1 Plant lecteurs vidéo] --> Scheduler[AliveMascotOverlayView.update]
    Scheduler --> P
    A --> Series[cycle 3..5 stays différents]
    Series --> Friend[mignonnerie chez une copine]
~~~

Le plafond de MascotActivityPolicy est désormais un plafond de lecteurs vidéo simultanés, pas un plafond de mascottes vivantes.

Les quatre modes alimentent le même registre de Presence à partir de leur snapshot courant.


## 19 — GECKO-052 : diagnostic sans pool borné

~~~mermaid
flowchart LR
    P[Presence visibles] --> Count[compter par type]
    Count --> Slots[créer 1 VideoSlot par Presence]
    Slots --> All[ALL ANIMATED simultanément]
    Intro[INTRO FIRST/SECOND] --> Suppress[setIntroSuppressed true]
    Suppress --> Stop[stop lecteurs mascottes + overlay invisible]
    Done[INTRO DONE/skip] --> Resume[reprendre PNG + cycles]
~~~

MascotActivityPolicy conserve les anciens nombres 3/2/1 uniquement comme référence historique.
AliveMascotOverlayView ne les utilise pas dans 0.15.12-dev.


## 20 — GECKO-053 lifecycle reprise

~~~mermaid
flowchart LR
    App[GeckoDoku] --> Pause[onPause]
    Pause --> Stop[AliveMascotOverlay.stopAll]
    Stop --> BG[aucun lecteur en arrière-plan]
    BG --> Resume[onResume]
    Resume --> Plant[ensurePlantMascot]
    Plant --> Sync[syncLivingMascotsForCurrentMode]
    Sync --> Modes[Classic / Sudoku / Gomoku / Bee]
    Modes --> Update[update ALL ANIMATED]
~~~

Le snapshot de jeu est la source de vérité après reprise ; les Presence sont reconstructibles et ne doivent pas être conservées comme état métier.


## 21 — GECKO-054 : zéro ordonnanceur

~~~mermaid
flowchart TD
    Snapshot[Snapshot du mode] --> Sync[syncLivingMascotsForCurrentMode]
    Sync --> P[Presence]
    P --> PNG[ImageView propre]
    P --> Video[ChromaKeyVideoView propre]
    P --> Anim[AliveAnimator propre]
    Anim --> Local[cycleRunnable local]
    Local --> Video
    Target[target null] --> Retry[retry local 240 ms]
    Retry --> P
    Intro[INTRO FIRST/SECOND] --> Hide[coupe/cache toutes les Presence]
    Resume[onResume] --> Sync
~~~

Il n’existe plus de lecteur partagé, pool, capacité ou scheduler global.
La coordination grand cycle reste un simple signal entre instances déjà autonomes.

Pierre :
RETURN_AFTER_PAUSE → 300 RETURN → cooldown 48 h → historique 48 IDs → filtre famille récente → filtre similarité → pipeline Pierre existant.


## 22 — GECKO-055 : Presence propriétaire, couches sœurs

~~~mermaid
flowchart TD
    Board[plateau / ancien PNG] -->|target valide : handoff unique| P[Presence]
    P --> PNG[pngContainer sibling]
    P --> Video[videoContainer sibling / GLSurfaceView]
    P --> A[AliveAnimator]
    A --> Wait[attente : PNG visible]
    Wait --> Prepare[prépare vidéo en gardant PNG]
    Prepare --> First[première frame réelle]
    First --> HidePNG[cacher PNG interne]
    HidePNG --> Playing[vidéo]
    Playing --> End[fin clip]
    End --> Wait
~~~

Le GLSurfaceView n'est plus enfant du même FrameLayout que le PNG.
Les deux couches restent détenues par la même Presence et reçoivent la même géométrie.


GECKO-055 validé techniquement par CI #257 ; validation composition réelle reste téléphone.


## 24 — GECKO-056 gate première frame partagé Gecko/Abeille

~~~mermaid
flowchart LR
    P[Presence Gecko ou Abeille] --> V[ChromaKeyVideoView]
    V --> S[MediaPlayer.start]
    S --> R[MEDIA_INFO_VIDEO_RENDERING_START]
    S --> PS[onPlayerStarted]
    R --> G[FreshPlaybackFrameGate]
    PS --> G
    G -->|2 signaux reçus, ordre libre| F[frame fraîche acceptée]
    F --> A[alpha vidéo = 1]
    F --> H[PNG interne caché]
~~~

Avant 0.15.16-dev, R reçu avant PS était perdu. GECKO-056 mémorise R par génération ; la révélation reste impossible tant que PS n'est pas également arrivé.

GECKO-056 : CI #260 verte ; APK Phone 0.15.16-dev construit ; prochain nœud = validation téléphone des Gecko et Abeilles dans les quatre modes.


## 25 — GECKO-057 cycle local non repoussable

~~~mermaid
flowchart LR
    S[show Presence] --> A[callback local armé]
    R[refreshDynamicTargets] --> G[maj géométrie si réellement changée]
    G --> Q{callback déjà armé ?}
    Q -->|oui| K[ne pas le repousser]
    Q -->|non| A
    A --> C[cycleRunnable]
    C --> D[désarme localement]
    D --> P[advancePresence]
    P --> V[ALIVE_PLAY / ChromaKeyVideoView.play]
~~~

Le refresh de géométrie n'est plus autorisé à réinitialiser l'horloge d'une Presence.

GECKO-057 : CI #263 verte ; APK Phone 0.15.17-dev prêt ; prochain test = ALIVE_PLAY visible puis stay1..4 Gecko/Abeille à l'écran.


## 26 — GECKO-058 bord du viewport hexagonal

~~~mermaid
flowchart LR
    C[Cellule Gecko/Abeille] --> T[cible centrée]
    T --> I{intersection viewport ?}
    I -->|non| N[target null]
    I -->|oui| B{dépasse un bord ?}
    B -->|non| R[cible inchangée]
    B -->|oui| K[clamp / recentrage minimal]
    K --> V[Presence animable]
    R --> V
~~~

La bordure ne supprime plus une mascotte encore visible.

GECKO-058 : CI #265 verte ; APK Phone 0.15.18-dev prêt ; prochain test = Gecko gauche/droite/bas animés + Abeille intérieure inchangée + zoom/drag.


## 27 — GECKO-059
cellule -> centeredScaledRect -> screenRectToRoot -> Presence Gecko/Abeille.
Aucune contrainte viewport sur le cadre média.

GECKO-059 : CI #267 verte ; APK Phone 0.15.19-dev prêt ; prochain test = Gecko de bord animés sans clamp ni décalage artificiel.


## 28 — GECKO-060 ownership visuel unique
Snapshot Bee/Gecko -> Alive Presence -> PNG fallback OU vidéo.
BeeGeckoBoardView garde grille, axes, croix et marqueurs mais ne dessine plus les mascottes.
La visibilité hors-écran dépend de la cellule du plateau, pas du cadre média transparent.


## 29 — GECKO-061 visibilité hexagonale
cellule -> centre écran -> centre dans viewport ? -> oui : Presence PNG/vidéo ; non : Presence cachée.
Le cadre média transparent/key-color n'est jamais utilisé pour décider la visibilité.

GECKO-061 : CI #275 verte ; APK 0.15.21-dev prêt ; test attendu = drag : cellule hors viewport => Presence cachée.


## 30 — GECKO-062 Gomoku vivant
stones -> séparation PLAYER / PROFESSOR -> max 3 aléatoires par camp -> Alive Presence.
Toutes les autres pierres restent statiques.
Grand cycle Alive terminé -> callback -> nouvelle sélection 3+3 -> anciens owners rendus au board, nouveaux owners promus.

GECKO-062 : CI #280 verte ; APK 0.15.22-dev prêt ; test téléphone = maximum 3 verts + 3 jaunes vivants, redistribution au grand cycle.


## 31 — GECKO-063
Gomoku move -> syncLivingMascots -> GomokuLivingSelectionPolicy -> max 3 PLAYER + 3 PROFESSOR -> seules ces Presence existent.
GECKO_APPEARANCE legacy -X-> showGomokuLivingGecko direct.
Long action Gomoku -> requestCute(owner sélectionné) -> même Presence, aucun second lecteur.

Launch : UI construite -> launchCurtain noir topmost -> Intro acceptée -> overlay Intro noir prend le relais -> curtain GONE.

Chroma : beginPlayback -> clear transparent -> texture interdite -> VIDEO_RENDERING_START (si gate) -> frame fraîche -> shader/keycolor -> VIDEO_FIRST_FRAME -> visible.

Plant : target vidéo 100 % ; PNG interne 95 %, centre identique.

GECKO-063 build -> CI #286 SUCCESS -> 0.15.23-dev -> test téléphone requis -> pas de release.

## 32 — stratégie géométrique proposée Bee/Gecko
cellRect intersecte viewport ? -> non : Presence cachée ; oui : Presence conservée -> target centré sur cellule -> rendu clipé au viewport réel du plateau.
Remplace le seuil center-in-viewport, qui masque trop tôt en bas et laisse déborder en haut.


## 33 — GECKO-064
cellRect ∩ viewport ? -> non : target null / Presence cachée ; oui : target centrée sur cellule.
target + viewport root -> intersection -> clipBounds locale -> PNG et vidéo.
Aucun clamp, aucun recentrage, aucun seuil center-in-viewport.

GECKO-064 : CI #289 verte -> APK 0.15.24-dev -> validation téléphone clipping haut/bas/gauche/droite -> pas de release.


## GECKO-065 — carte de couche « derrière le cadre »

Abeilles & Geckos / Gomoku :
cellule logique
→ target non bornée
→ Presence Alive persistante
→ PNG fallback + ChromaKeyVideoView
→ clipProvider = fenêtre réelle du plateau
→ visible si intersection
→ clip vide si entièrement derrière le cadre
→ cycle AliveAnimator continue localement
→ retour drag/zoom = réapparition progressive de la même Presence.

Le viewport ne doit plus décider de l'existence de la Presence.

Gomoku conserve en amont :
snapshot pierres
→ GomokuLivingSelectionPolicy
→ max 3 PLAYER + 3 PROFESSOR
→ redistribution au grand cycle
→ seules ces Presence passent dans la chaîne non bornée + clip.

Hors chaîne :
titres / textes / boutons / Prof restent hors de la fenêtre de rendu des mascottes de plateau.

Ne pas réintroduire :
viewport -> target=null pour une simple sortie visuelle ;
clamp/recentrage de mascotte ;
masque SurfaceView global ;
setZOrderOnTop/mediaOverlay modifié pour contourner la géométrie ;
ordonnanceur/pool/lecteur partagé.


## 34 — GECKO-066 / pipeline vidéo de plateau

ownerKey
→ AliveVideoBackendPolicy(owner, SDK)
→ bee:* ou gomoku:* + SDK>=33
  → ChromaKeyTextureView
  → MediaPlayer -> TextureView.SurfaceTexture
  → RuntimeShader keycolor bleu/vert + despill + yellowTint
  → onSurfaceTextureUpdated fraîche + VIDEO_RENDERING_START
  → VIDEO_FIRST_FRAME
  → alpha=1
  → clipBounds Android identique au PNG
→ sinon
  → ChromaKeyVideoView GLSurface historique.

AliveMascotOverlayView
→ video: View pour visibilité/layout/clip
→ playback: ChromaKeyPlayback pour play/mute/keycolor/tint/release.

Ne pas étendre TextureView à Pierre/Intro/Classic/Sudoku/Plante avant validation téléphone.

GECKO-066 -> CI #310 SUCCESS -> APK 0.15.27-dev -> téléphone : chercher ALIVE_VIDEO_BACKEND backend=TextureView sur owner bee:/gomoku: puis vérifier clip identique PNG/vidéo -> aucune release.


## 35 — GECKO-067 / flux final expérimental plateau

MediaPlayer
→ Surface(input SurfaceTexture OES)
→ frame disponible sur thread GL local
→ updateTexImage + textureMatrix
→ GLSL historique keycolor bleu/vert + despill + yellowTint
→ EGLSurface liée à TextureView.SurfaceTexture
→ eglSwapBuffers
→ TextureView composée comme vraie View Android
→ clipBounds/Z-order identiques au PNG.

First frame :
PLAY alpha=0
→ MEDIA_INFO_VIDEO_RENDERING_START
→ arm baseline
→ frame OES fraîche
→ draw shader
→ eglSwapBuffers OK
→ VIDEO_FIRST_FRAME
→ alpha=1.

Portée : bee:* + gomoku:* seulement.

GECKO-067 -> CI #312 SUCCESS -> APK 0.15.28-dev -> téléphone : ChromaTextureGL@... + keycolor stable + yellowTint stable + clip identique PNG/vidéo -> aucune release.


## GECKO-068
MP4 480p @ 12 fps -> SpriteFrameCache -> chroma-key RGBA -> ChromaKeySpriteView -> AliveMascotOverlayView.
Options: résolution 240/360/480 (480 défaut), limite 3/camp OFF défaut. PerfMonitor mesure activeAnimations/peakAnimations + FPS/mémoire.


## GECKO-069 — carte cache chaud SpriteRGBA

MainActivity
→ PREWARM_ARMED (4 s au lancement ; 2,5 s résolution/reprise)
→ SpriteFrameCache.prewarmLivingCore()
→ SpriteWarmupPolicy
→ 3 rôles logiques = Gecko vert + Gecko jaune + Abeille
→ 2 banques physiques = GECKO_SHARED + BEE
→ jaune = même frame Gecko + ColorMatrix dynamique
→ pin asynchrone (max 16 MiB/rôle)
→ warmup secondaire = seulement stay2/stay3/stay4 Gecko/Abeille, un par un
→ apparition/disparition/CUTE = on-demand
→ aucun changement AliveAnimator / aucune coordination globale des mascottes.


GECKO-069 build
→ commit 522802e5712ee5005850a033577f5fc143d00bb8
→ CI #314 verte
→ GeckoDoku-v0.15.30-dev-phone
→ test téléphone : HOT_BANK_PINNED + stabilité FPS/mémoire.


## GECKO-070 — quick-start SpriteRGBA

MP4 source
→ SpriteFrameCache
→ 4 frames RGBA prêtes
→ SPRITE_QUICK_READY
→ ChromaKeySpriteView démarre
→ boucle locale du préfixe si nécessaire
→ fabrication continue en fond
→ manifest complet
→ FULL_SEQUENCE_READY
→ séquence complète.

Résolutions : 120p / 180p / 240p / 360p / 480p.
Défaut neuf : 240p.


## GECKO-071 — chemin visuel statique/animé

Animations ON
→ StableFramePolicy
→ stay1 / frame 1 uniquement
→ requestStableFrame()
→ cache mémoire HIT
   ou cache disque HIT
   ou sprite frame-00000 HIT
   ou extraction unique MISS
→ ImageView simple STABLE_FRAME
→ QUICK_READY / première frame animée
→ STABLE_FRAME_TO_SPRITE
→ SpriteRGBA / backend animé.

Animations OFF
→ PNG historique
→ LEGACY_PNG_SHOW.

Partage :
GECKO_STABLE_FRAME
├─ Gecko vert
└─ Gecko jaune + tint
BEE_STABLE_FRAME
└─ Abeille.


## GECKO-072 — Sprite Bank Factory

BOOT
→ STABLE_FRAME Gecko stay1/frame1 @240p
→ STABLE_FRAME Abeille stay1/frame1 @240p
→ stable barrier OPEN
→ SpriteCatalog (12 assets Gecko/Abeille)

VISIBLE REQUEST
→ catalog check
→ MEMORY HIT ?
→ coalesce session ?
→ APK HIT ?
→ DISK HIT ?
→ BUILDING resume ?
→ generation MP4

GENERATION
→ PriorityBlockingQueue
→ max 3 workers
→ 4 frames/chunk
→ QUICK_READY
→ requeue
→ READY manifest atomique.

QUALITY
60p internal
→ 120p
→ 240p normal
→ 360/480 only on demand.

Playback swap
→ logicalStartedAtMs conservé
→ frame équivalente calculée
→ qualité change, AliveAnimator ne redémarre pas.

Storage
filesDir/sprite-banks-v2/<bankKey>/
├─ building.json pendant génération
├─ frame-xxxxx.webp/png
└─ manifest.json READY + sourceSha256 + bankSha256.

APK future
assets/sprites/<bankKey>/
→ APK HIT
→ materialisation locale
→ lecture.

Developer export
Réglages
→ GeckoDoku-sprite-banks-*.zip
→ sprites/index.json
→ sprites/sprite-factory-report.json
→ banques READY
→ stable-frames.

## GECKO-040 — Sprite banks by resolution

`SpriteBankFactory`
→ stable barrier
→ progressive catalog plan according to target resolution
→ per-asset physical bank files
→ logical aggregation by resolution
→ frame-level progress + size + state
→ disk/APK ready hit exits worker immediately

`MainActivity > Réglages > Banques sprites / export`
→ list 60p / 120p / 180p / 240p / 360p / 480p
→ percentage + size + state
→ details: generated frames / expected frames, complete assets / expected assets
→ one export action per resolution

`SpriteBankExporter`
→ `sprites/banks/<height>p/bank-manifest.json`
→ asset-bank directories for that resolution
→ stable frames alongside export package
→ embedded loader accepts new grouped APK path and legacy flat path.

## GECKO-040B — Export button flow

Réglages > Banques de sprites
→ Tout préparer + exporter
→ SpriteBankFactory.prepareExportThrough480()
→ 60p → 120p → 240p → 480p
→ barre de progression = frames générées / frames attendues
→ 4/4 banques complètes
→ ACTION_CREATE_DOCUMENT
→ SpriteBankExporter.write()
→ ZIP unique structuré par résolution.

READY reload:
initializeSession() → READY | BUILD_REQUIRED
→ terminal=true sur completeSession
→ aucune tâche stale ne peut relancer requireNotNull(metadata)
→ erreurs de métriques post-completion = warning non fatal.

## GECKO-041 — Sprite banks canonical set

Banques canoniques :
60p → 120p → 240p STOP

`SpriteResolution`
→ 120p / 240p seulement côté utilisateur

`SpriteProgressivePolicy`
→ cible 120 : 60 > 120
→ cible 240 : 60 > 120 > 240
→ aucune résolution secondaire >240

`SpriteBankFactory`
→ prune anciens dossiers *-180, *-360, *-480
→ exportBankHeights = [60,120,240]
→ préparation export max 240p

Archive historique 480p :
GitHub Release `PackageSprites`, conservée hors pipeline actif.

GECKO-041 validation
→ old stored resolution 180/360/480 → fallback P240
→ tests selector [120,240]
→ CI run #388 PASS.



## GECKO-042 — banques préfabriquées depuis la release PackageSprites

- Branche : `gecko-042-prebuilt-sprite-runtime`.
- Référence immuable : `PackageSprites/GeckoDoku-sprite-banks-60-120-240-480-20260930-2058.zip`, SHA-256 `b108331d259d6d03aa6832028327a76fb31cf3bcb882738f759121cfeafad296`.
- La CI injecte uniquement `sprites/banks/60p`, `120p` et `240p` dans les assets de l’APK ; 480p n’est pas embarqué.
- L’entrée visible « Banques sprites / export » est retirée du menu ; la fabrication/export devient un outil transitoire non exposé.
- Important : le `SpriteCatalog` ayant servi à l’export ne couvre que 12 clips Gecko/Abeille. Ne pas supprimer encore les 9 MP4 non couverts : `gecko/IntroGeckoGD.mp4`, `gecko/Gecko_Intro.mp4`, les 5 clips Plante, `prof/Prof_actions.mp4` et `prof/ProfParle.mp4`.
- Étape suivante avant suppression totale des MP4 : fabriquer/valider les banques 60/120/240 de ces 9 clips, puis basculer leur lecture sur sprites embarqués.


# GECKO-043 — RUNTIME SPRITES 240P UNIQUEMENT / NETTOYAGE MP4

Décision Fab du 2026-09-30.

- Runtime SpriteRGBA fixé à 240p : suppression des étages 60p/120p et des swaps progressifs.
- Le build n'embarque plus que `sprites/banks/240p`; 60p/120p/180p/360p/480p sont obsolètes et les caches locaux correspondants sont purgés.
- Les 12 MP4 sources désormais couverts par SpriteCatalog sont supprimés : Gecko stay1..4 + apparition + disparition + action longue ; Abeille stay1..4 + apparition.
- Les intros Gecko, la Plante et Pierre/Prof restent en vidéo et ne sont pas touchés.
- Classic, Sudoku, Gomoku et Abeilles & Geckos routent désormais Gecko/Abeille vers SpriteRGBA.
- Le loader résout les banques 240p via `sprites/index.json` avant tout accès au MP4 source ; une installation neuve n'a donc plus besoin des vidéos supprimées.
- La STABLE_FRAME Gecko/Abeille peut être lue directement depuis `frame-00000` de la banque 240p embarquée.
- Le catalogue 240p est préparé en arrière-plan et conservé dans `readyMemory`; les frames restent sous LRU bitmap borné pour éviter une explosion RAM.
- Version test : 0.15.38-dev / versionCode 73.
- Aucun merge main ni release/prerelease avant validation téléphone Fab.

# GECKO-044 — ARBRE D’HYPOTHÈSES COLORÉES

Décision Fab du 2026-10-02.

- Branche : `gecko-044-hypothesis-branches`.
- Modes : GeckoDoku classique et Abeilles & Geckos.
- Palette canonique des branches : jaune → vert → rouge → violet → bleu → orange.
- Chaque Gecko hypothèse porte une aura de sa couleur ; les croix filles héritent exactement de cette couleur.
- Une nouvelle hypothèse sous une branche active devient son enfant logique.
- Une contradiction transforme l’hypothèse concernée et ses descendants en état visuel « sens interdit ».
- Supprimer une hypothèse déclenche un rollback récursif : ses croix, auras, sous-hypothèses et états contradiction sont supprimés, sans toucher au parent ni aux autres branches.
- La frise sous le plateau affiche la chronologie. Toucher une étape revient à cette hypothèse et détruit seulement ses descendants.
- Le moteur de dépendances est partagé par les deux modes via `HypothesisBranchTrace`.
- Abeilles & Geckos persiste/restaure l’arbre dans sa session (schema 5).
- Version de test : 0.15.39-dev / versionCode 74.
- Aucun merge main ni release/prerelease avant validation Fab.

GECKO-044 CI FINAL : run #397 SUCCESS sur commit `3f61e64c1ce5381710b2ece6ee0ccac570f26a9f`. Tests Kotlin + assemblePhone verts. Artifact `GeckoDoku-v0.15.39-dev-phone`, 219108667 octets, digest `sha256:f77cecba153d34bba27fd047309f9c77f138fd4e7f0d2beadfabb1a0f02f4882`. Les prerelease/release sont restées skipped. Le correctif final garantit qu’une croix préexistante hors hypothèse ne devient jamais rétroactivement fille/colorée d’une nouvelle branche. Validation téléphone reste à faire.

# GECKO-045 — SUDOKU : PAVÉ HYPOTHÈSE / PRÉVISU

Décision Fab du 2026-10-02.

- Branche : `gecko-045-sudoku-hypothesis-pad`.
- Sudoku distingue désormais strictement **candidats** et **hypothèses**.
- Le pavé flottant devient une grille 2×2 de quatre zones : **Choix**, **Candidats**, **Hypothèse**, **Prévisu**.
- Le pavé reste déplaçable par drag via son bandeau supérieur et possède une croix de fermeture intégrée.
- Le pavé Hypothèse annonce déjà la couleur de la prochaine branche : jaune → vert → rouge → violet → bleu → orange.
- Une hypothèse Sudoku est le couple (case, chiffre), avec la même logique parent/enfant, contradiction et rollback récursif que GECKO-044.
- Les candidats restent des notes indépendantes : ils ne sont ni transformés ni confondus avec les hypothèses.
- Le chiffre choisi n'est plus posé immédiatement. Il apparaît d'abord dans Prévisu ; la croix de fermeture disparaît et la question « Êtes-vous sûr ? Oui / Non » devient obligatoire.
- **Oui** valide ensuite par le moteur Sudoku normal ; **Non** efface uniquement la prévisu.
- Les hypothèses Sudoku sont visibles sur la grille par aura colorée et chiffre hypothétique ; contradiction = sens interdit.
- Sudoku n'affiche pas de frise H1/H2 permanente afin d'économiser la place.
- Les libellés H1/H2 sont retirés de la frise GECKO-044 ; les couleurs + bord actif + sens interdit suffisent.
- Version test : 0.15.40-dev / versionCode 75.
- Aucun merge main ni release/prerelease avant validation Fab.

GECKO-045 CI FINAL : run #398 SUCCESS sur commit `63e3aab45628c0e0ba9311ae3e521ded03973aa2`. Tests Kotlin + assemblePhone verts. Artifact `GeckoDoku-v0.15.40-dev-phone`, 219118896 octets, digest `sha256:7336ff3419c30de1f7f1ba11d10aa2e35472c8714a3ab3a76c5d5b79e5db3f4f`. Prerelease et release sont restées skipped. Validation téléphone requise pour drag du pavé, confirmation Oui/Non, indépendance candidats/hypothèses et rollback parent/enfant Sudoku.
