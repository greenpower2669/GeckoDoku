# Brainmap — GeckoDoku v0.4

PuzzleGenerator
├── génère / ajuste la grille
├── vérifie unicité
├── DifficultyIndexer
└── attachTrace()
    └── HumanSolver.analyze().steps
        └── Puzzle.solverTrace

SolveStep
├── technique
├── cell / eliminated
├── sourceCells
├── sourceRegions
├── axis
├── beforeConfirmed
└── beforeExcluded

ProfessorGecko
├── reçoit GameSnapshot
├── cherche étape compatible dans Puzzle.solverTrace
│   └── chemin rapide
├── sinon HumanSolver.nextStep()
│   └── chemin de secours depuis état courant
└── ProfessorHint
    ├── focusText
    ├── explanationText
    └── actionText

MainActivity
├── bouton Prof Gecko
├── pression 1 = où regarder
├── pression 2 = pourquoi
├── pression 3 = action
└── toute action joueur → reset hint

GeckoBoardView
├── sources ambre
└── cibles rouges au niveau 3

HumanSolver
├── singles
├── region locked
├── projection
└── Gecko X-Wing

## Réutilisation de la trace
génération → difficulté → professeur → debug → futur apprentissage IA.

## Build
push main → GitHub Actions → GeckoDoku-v0.4.0-dev.apk.


MainActivity / WindowInsets
├── systemBars top
├── systemBars bottom
├── systemBars left/right
└── padding de base + insets
    ├── titre hors barre d'état
    └── Prof Gecko hors barre de navigation

KnownIssue GECKO-012
└── Facile 12×12 observé avec 2 X-Wing humains
    └── audit DifficultyIndexer + solverTrace à venir


PuzzleJournalStore
├── SharedPreferences / JSON
├── save(Puzzle)
│   ├── définition de grille uniquement
│   └── dédoublonnage par id
├── list()
├── load(id)
│   └── HumanSolver.analyze() → solverTrace reconstruite
├── delete(id)
└── clear()

MainActivity v0.5
├── ↺ Rejouer
│   └── même Puzzle → nouveau GameEngine
├── ⭐ Sauver
│   └── PuzzleJournalStore.save
├── 📚 Journal
│   ├── charger/rejouer
│   ├── supprimer
│   └── vider tout
└── Stats
    └── DifficultyStats par difficulté

Stats tentative
Nouvelle / Rejouer / Journal load
→ recordStart(difficulté mesurée)
→ fin réussie
→ recordComplete
→ taux = completed / started.
