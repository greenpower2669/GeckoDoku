# GeckoDoku — brainmap.md
## CARTE DES FONCTIONNALITÉS ACTIVES — 26/09/2026

GECKODOKU
│
├── MOTEUR DE JEU
│   ├── tailles 5..12
│   ├── Gecko confirmés / donnés
│   ├── croix manuelles + automatiques
│   ├── hypothèses
│   ├── marqueurs personnalisés
│   └── règles ligne / colonne / région / voisinage
│
├── GÉNÉRATION
│   ├── solution unique
│   ├── validation logique
│   ├── trace solveur
│   └── fallback sûr
│
├── SOLVEUR HUMAIN
│   ├── singles
│   ├── région verrouillée
│   ├── projection
│   ├── Gecko X-Wing
│   └── hypothèses contradiction
│
├── DIFFICULTÉ
│   └── 8 niveaux
│       ├── Découverte
│       ├── Facile
│       ├── Réflexion
│       ├── Difficile
│       ├── Expert
│       ├── Démentiel
│       ├── Mission Impossible
│       └── Infernal
│
├── PROF GECKO
│   ├── trace solveur / recalcul live
│   ├── explication pédagogique
│   ├── une étape appliquée à la fois
│   ├── bulle overlay sans reflow
│   ├── Pierre local
│   ├── priorités parole
│   ├── ambient
│   └── slot visuel
│       ├── Prof.png
│       ├── Prof_actions.mp4
│       └── ProfParle.mp4
│           └── SPEECH > ACTION localement
│
├── PERSISTENCE
│   ├── Rejouer
│   ├── Sauver
│   ├── Journal
│   └── stats
│
├── AUDIO
│   ├── Pierre
│   ├── encouragements enregistrés
│   ├── encouragements Pierre
│   └── célébration
│
├── IDENTITÉ / INTRO — VALIDÉ TÉLÉPHONE
│   ├── launcher ✅
│   ├── médaillon titre ✅
│   ├── IntroGeckoGD + son ✅
│   └── Gecko_Intro après Intro 1 ✅
│
└── MÉDIAS / SPRITES
    ├── Gecko_tr.png canonique
    ├── apparition / disparition / actions longues
    │   └── visibles + audio vidéo muet
    ├── multi-sessions indépendantes
    ├── chroma bleu OpenGL
    ├── alpha/translucide
    ├── ZOrderOnTop candidat v0.10.11
    └── GeckoDokuMediaTrace

Règle centrale :
brain.md = contrat durable.
ordre de mission = tâche temporaire.
debughistorical = mémoire des causes/corrections.
