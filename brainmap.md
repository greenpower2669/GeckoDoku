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


HypothesisSolver
├── HumanSolver FULL bloqué
├── binaryChoices()
│   ├── ligne = 2 candidats
│   ├── colonne = 2 candidats
│   └── zone = 2 candidats
├── proveContradiction()
│   ├── conflits de geckos
│   ├── ligne sans candidat
│   ├── colonne sans candidat
│   └── zone sans candidat
├── profondeur 1 → Mission Impossible
└── profondeur/compte 2 → Infernal

SolveStep v0.6
├── hypothesisRejected
└── hypothesisDepth

DifficultyIndexer v0.6
HumanSolver FULL
├── solved → niveaux Découverte..Démentiel
└── blocked
    ├── HypothesisSolver(1,1) solved → Mission Impossible
    ├── HypothesisSolver(2,2) solved → Infernal
    └── sinon → hors contrat / rejet génération

ProfessorGecko
├── trace déterministe ou hypothèse
├── ProfessorBubbleView
└── surlignage GeckoBoardView

Victoire
completeGame()
└── VictoryCelebrationView.start(difficulty)
    ├── intensité croissante
    ├── Mission accomplie
    └── Infernal vaincu


Prof Gecko v0.7
bouton Prof
├── étape déterministe
│   ├── ProfessorGecko.nextHint()
│   ├── GameEngine.applyProfessorStep()
│   ├── croix / gecko réels
│   ├── bulle explicative
│   └── arrêt → attend pression suivante
└── étape hypothèse
    ├── pression 1
    │   ├── professorGhosts = 2 candidats
    │   └── aucune mutation
    └── pression 2
        ├── croix sur hypothesisRejected
        ├── gecko solide sur cell
        └── fantômes supprimés

GameEngine.applyProfessorStep
├── garde-fou solution unique
├── eliminated → manualCrosses
├── hypothesisRejected → manualCrosses
└── cell → confirmed → autoCrosses

Stats
professorUsed pendant tentative
└── victoire
    └── completed_with_prof + completed_with_prof_diff.


UI Prof v0.8
screenRoot : FrameLayout
├── contenu principal : LinearLayout
│   ├── titre/info/status
│   ├── grille
│   ├── controlsPanel
│   │   ├── taille/difficulté
│   │   ├── nouvelle/stats/FX
│   │   └── rejouer/sauver/journal
│   └── bouton Prof
├── ProfessorBubbleView overlay
│   ├── position = juste au-dessus bouton Prof
│   ├── × → closeProfessorBubble()
│   └── ouverture → controlsPanel GONE
└── VictoryCelebrationView overlay

Célébration sonore
VictoryCelebrationView
└── onFireworkBurst(level,index,last)
    └── FxFeedback.celebrationBurst()
        ├── ton lancement
        ├── ton explosion différé
        └── accent final haut niveau

Arrêt
├── bouton FX OFF
├── toucher célébration
└── release
→ FxFeedback.stopCelebration().
