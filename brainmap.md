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


Rich Media GECKO-022
GameEngine / Solver / Stats
└── NE DÉPENDENT JAMAIS des médias

Rendu normal
└── RichMediaOverlay (optionnel)
    ├── Intro
    │   └── assets/gecko/Gecko_Intro.mp4
    ├── case gecko confirmée
    │   └── Gecko_apparition.mp4 complet
    ├── gecko retiré
    │   └── Gecko_disparition.mp4 complet
    ├── action mignonne occasionnelle
    │   └── Gecko_actions_plusieurs.mp4 complet ~30 s
    └── contexte Prof occasionnel
        └── Prof_actions.mp4 complet ~30 s

Assets
assets/
├── gecko/
│   ├── Gecko_Intro.mp4
│   ├── Gecko_apparition.mp4
│   ├── Gecko_disparition.mp4
│   └── Gecko_actions_plusieurs.mp4
└── prof/
    ├── Prof.png
    ├── Prof_fb.png
    └── Prof_actions.mp4

Interrupteurs
Habillage animé OFF
└── rendu normal uniquement

Habillage animé ON
├── FX ON → vidéo + audio embarqué
└── FX OFF → vidéo muette

Règle actuelle
vidéo longue
→ t=0
→ lecture entière
→ EOF
→ jamais de découpage / boucle


## Keycolor runtime GECKO-022
MainActivity
├── RichMediaSettings (SharedPreferences)
├── RichMediaScheduler (12 % test / cooldown partagé 180 s)
├── RichMediaOverlayView
│   └── ChromaKeyVideoView
│       ├── MediaPlayer → SurfaceTexture OES
│       ├── OpenGL ES 2 fragment shader
│       │   ├── dominance bleue → alpha
│       │   ├── bord smoothstep
│       │   └── despill bleu
│       └── erreur média/shader → overlay fermé, jeu inchangé
├── intro 0→EOF + titre GeckoDoku + skip
├── apparition/disparition → rectangle réel de la case
├── Gecko_actions_plusieurs.mp4 → entier, rare, cooldown
└── Prof_actions.mp4 → entier, rare, texte Prof prioritaire

Gradle app
└── sourceSets.main.assets = ../assets
    └── aucun doublon des MP4/PNG dans app/src/main/assets

Ordre visuel
screenRoot
├── jeu normal
├── RichMediaOverlayView
├── ProfessorBubbleView (texte logique au-dessus du média Prof)
└── VictoryCelebrationView (priorité la plus haute)


## Encouragement audio GECKO-023 — cible
assets/audio/encouragements/master/Voix_encouragements.mp3 (master 14,441 s)
└── découpage préparatoire, jamais au runtime
    └── assets/audio/encouragements/clips/
        └── 13 clips courts

GameEngine
└── confirme le coup joueur
    └── MainActivity / événement gecko confirmé
        └── EncouragementSelector
            ├── cellule nouvelle dans la tentative
            ├── hasard
            ├── anti-répétition immédiate
            └── « Tu y es presque » seulement à 1–2 restants
                └── EncouragementPlayer
                    ├── FX ON → lecture
                    ├── FX OFF → silence
                    └── erreur asset → ignore / jeu inchangé

Dernier gecko
├── voix courte
└── célébration visuelle immédiate
    └── futur audio victoire attend la fin de la voix

Habillage animé
└── sans effet sur les voix d'encouragement


Audio autonomes
assets/audio/
├── intro/
│   └── jungle intro GeckoD.mp3
├── celebration/
│   └── jungle cebration GeckoD.mp3
└── encouragements/
    ├── master/
    │   └── Voix_encouragements.mp3
    └── clips/
        └── futur : 13 clips GECKO-023


## GECKO-024 runtime
GeckoBoardView
├── Gecko_tr.png transparent
└── fallback procédural

Case immuable
├── masque blanc exact
└── ChromaKeyVideoView
    └── UV canoniques + matrice SurfaceTexture

ProfessorBubbleView
├── Prof.png statique
├── texte
└── portraitRectOnScreen
    └── Prof_actions.mp4 localisé
        └── TTS français prioritaire

AssetAudioPlayer
├── musique Nouvelle
├── musique victoire
└── voix encouragements par timecodes du master


Build v0.10
└── run #28 vert
    ├── tests unitaires
    ├── APK
    └── AAB
        └── validation téléphone encore ouverte


## GECKO-025
GeckoBoardView
└── AssetMediaCatalog.GECKO_PORTRAIT
    └── assets/gecko/Gecko_tr.png (canonique)

Intervention Prof
├── texte immédiat
├── TTS immédiat
└── ProfessorAnimationPolicy
    ├── Animations ON
    ├── média libre
    ├── pas d'hypothèse en attente
    └── pas de célébration
        └── Prof_actions.mp4 localisé dans le portrait
            └── aucune probabilité / aucun cooldown


Build GECKO-025
└── run #33 vert
    ├── AssetMediaCatalogTest
    ├── ProfessorAnimationPolicyTest
    ├── APK v0.10.1-dev
    └── AAB v0.10.1-dev


## GECKO-026
LinearLayout racine (dimensions stables)
├── titre / info / statut
├── GeckoBoardView (weight=1, ne change jamais pendant Prof)
├── controlsPanel (reste VISIBLE)
└── professorButtonHost (hauteur fixe 58 dp)
    ├── Button texte
    └── Prof.png décoratif
        ├── dépassement visuel 10 dp
        └── micro-animation transform uniquement

screenRoot overlay
├── jeu normal
├── RichMediaOverlayView
├── ProfessorBubbleView flottante
│   ├── titre
│   ├── texte
│   └── croix
│   (aucun PNG/MP4 Prof)
└── VictoryCelebrationView

showProfessorBubble()
├── ne touche jamais au flux de layout
├── TTS immédiat
└── positionProfessorBubble() seulement
