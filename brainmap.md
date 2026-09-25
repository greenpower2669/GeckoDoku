# Brainmap — GeckoDoku v0.2

MainActivity
├── réglage taille 5..8
├── réglage GameDifficulty
├── PuzzleGenerator.generate()
├── GameEngine
├── GeckoBoardView
├── PlayerStatsStore
├── FxFeedback
└── DifficultyIndexer

PuzzleGenerator
├── validPermutations()
├── growRegions()
├── countSolutions() == 1
├── HumanSolver.selectGivens()
└── fallback 5..8

HumanSolver
├── candidates
├── ROW_SINGLE
├── COLUMN_SINGLE
├── REGION_SINGLE
├── LOCKED_CANDIDATE
└── X_WING

GameEngine
├── givens verrouillés
├── confirmed
├── manualCrosses
├── autoCrosses
├── hypotheses
└── customMarkers

GeckoBoardView
├── onSingleTapConfirmed → croix
├── onDoubleTap → gecko
├── onLongPress → hypothèse/palette
├── givens entourés
└── rendu procédural

PlayerStatsStore
└── SharedPreferences local uniquement

## Flux nouvelle grille
taille + difficulté
→ générateur
→ unicité exhaustive
→ sélection de givens
→ validation HumanSolver
→ GameEngine
→ partie
→ stats locales.

## Séparation critique
Logique = PuzzleGenerator + HumanSolver + GameEngine.
Rendu = GeckoBoardView.
Audio = FxFeedback.
Stats = PlayerStatsStore.
Scoring = DifficultyModel.
Les futurs AssetRenderer / AssetFx / VoiceFeedback ne doivent pas pénétrer le moteur logique.

## Build CI
push main → GitHub Actions → JDK17 + Gradle9.6 → assembleDebug → GeckoDoku-v0.2.0-dev.apk.
