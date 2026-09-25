# Brainmap — GeckoDoku v0.3

MainActivity
├── taille 5..12
├── difficulté demandée
├── PuzzleGenerator
├── DifficultyIndexer → difficulté mesurée
├── GameEngine
├── GeckoBoardView (12 couleurs)
├── PlayerStatsStore
└── FxFeedback

PuzzleGenerator
├── randomSolution() backtracking
├── growRegions()
├── tuneGivens()
├── countSolutions(limit=2)
├── DifficultyIndexer.analyze()
└── meilleur candidat sûr si exact indisponible

HumanSolver
├── singles
│   ├── ROW_SINGLE
│   ├── COLUMN_SINGLE
│   └── REGION_SINGLE
├── REGION_LOCKED
├── REGION_TOUCH_PROJECTION
└── GECKO_X_WING
    ├── paire de lignes
    ├── paire de colonnes
    └── paire de zones sur 2 axes

DifficultyIndexer
├── solve Singles
├── solve Zone sans X-Wing
├── solve X-Wing sans projection
├── solve Full
└── classifie par nécessité réelle

Difficulté
Découverte → singles
Facile → ≥1 zone
Réflexion → 2..3 zones
Difficile → ≥4 zones
Expert → X-Wing indispensable
Démentiel → X-Wing + projection indispensables

GameEngine
├── givens
├── confirmed
├── crosses
├── autoCrosses
├── hypotheses
└── markers

## Flux
demande taille+niveau
→ génération
→ unicité
→ solveurs d'ablation
→ niveau mesuré
→ jeu
→ stats locales du niveau mesuré.

## Build
push main → GitHub Actions → assembleDebug → GeckoDoku-v0.3.0-dev.apk.
