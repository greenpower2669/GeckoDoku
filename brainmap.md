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


## 15 — GECKO-050 : moteur de mascottes vivantes

~~~mermaid
flowchart LR
    Event[apparition / repère / pièce] --> Layer[AliveMascotOverlayView]
    Layer --> Coord[MascotLifeCoordinator]
    Coord --> G[AliveAnimator Gecko]
    Coord --> B[AliveAnimator Abeille]
    Coord --> P[AliveAnimator Plante]
    G --> GV[appear / stay1..4 / cute / disappear]
    B --> BV[appear / stay1..4 / fallback hide]
    P --> PV[stay1..4 / cute / PlanteTr fallback]
    Layer --> CK[ChromaKeyVideoView]
    CK --> Mask[masque maintenu entre clips]
~~~

Géométrie :
- Classic Gecko = 80 % de la cellule, identique au PNG dessiné avec inset 10 % ;
- Sudoku Gecko = base 66 % de la cellule ;
- Gomoku utilise geckoRectOnScreen, déjà calé sur le PNG ;
- Bee/Gecko hex = 0,61 × scale de type, donc Abeille = 0,61 × BEE_SCALE ;
- Plante = décor bas-droite, indépendante du gameplay.

Une seule instance vivante de chaque type est active à la fois ; quand une nouvelle pièce du même type apparaît, l'ancienne redevient son PNG statique. Cela évite une multiplication de lecteurs vidéo sur les grands plateaux.

Invariant de propriété : retirer une ancienne pièce statique ne reprend pas le slot vivant à une pièce plus récente du même type.

Prof Gecko / Pierre n'utilisent pas AliveAnimator.
