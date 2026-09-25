# Brainmap — GeckoDoku

MainActivity
├── Puzzle.demo5x5()
│   └── carte de régions 5×5 à solution unique vérifiée
├── GameEngine
│   ├── confirmed
│   ├── manualCrosses
│   ├── autoCrosses calculées
│   ├── hypotheses
│   └── customMarkers
├── GeckoBoardView
│   ├── zones
│   ├── frontières fortes
│   ├── croix manuelles / auto
│   ├── gecko procédural
│   ├── gecko fantôme
│   ├── gecko alerte
│   └── repères personnels
├── FxFeedback
│   └── ToneFxFeedback
└── DifficultyIndexer
    ├── DifficultyFeatures
    ├── DifficultyModel
    └── HeuristicDifficultyModel

## Build CI
push main / workflow_dispatch
→ GitHub Actions ubuntu-latest
→ JDK 17
→ Gradle 9.6.0
→ :app:assembleDebug
→ GeckoDoku-v0.1.0-dev.apk
→ artefact GeckoDoku-v0.1.0-dev-APK

## Séparation critique
Puzzle + GameEngine = logique pure.
GeckoBoardView = rendu léger actuel.
FxFeedback = port audio interchangeable.
DifficultyModel = port de scoring interchangeable.

## Flux
Touch → GeckoBoardView → MainActivity → GameEngine → ActionFeedback → FX + message accessible → rendu.
Gecko confirmé → recalcul autoCrosses → ligne + colonne + zone + voisinage exclus.
Long press hors grille → palette → sélection → prochain tap → customMarkers, sans effet logique.

## Validation puzzle
Énumérer les permutations d'une position par ligne et colonne → filtrer non-contact diagonal/adjacent → filtrer exactement un gecko par région → exiger exactement une solution.

## Futur
Generator → UniqueSolutionValidator → SolverTrace → DifficultyFeatures → DifficultyModel.
SkinManager → ProceduralRenderer / AssetRenderer.
Audio → ToneFxFeedback / AssetFxFeedback.
EncouragementEngine → texte/TTS léger / voix enregistrées.
