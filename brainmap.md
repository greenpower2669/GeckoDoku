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


<!-- GECKO-034-RED-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## GECKO-034
Vidéo
→ création alpha 0
→ prepared
→ start
→ SurfaceTexture frame
→ draw OpenGL réel
→ alpha 1

Grille
→ slot/ancre dans layout
→ grille réelle en couche flottante
→ capture rectangle initial
→ même fenêtre : rectangle figé
→ fenêtre réellement redimensionnée : nouvel ancrage autorisé


<!-- GECKO-034-GREEN-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## GECKO-034 implémenté
root → boardAnchor seulement
screenRoot → GeckoBoardView flottante → géométrie figée par fenêtre

vidéo non-intro
→ alpha 0
→ start
→ SurfaceTexture frame
→ updateTexImage
→ glDrawArrays
→ firstFrameRendered
→ alpha 1


<!-- GECKO-034-V01012-CANDIDATE-2026-09-26 -->
v0.10.12-dev
├── first-frame gate
├── board flottante immuable
├── intros inchangées
└── validation téléphone Fab


<!-- GECKO-035-RED-2026-09-26 -->
GECKO-035 RED
├── ProfessorSpeechLaunchPolicy
├── ProfessorSpeechVisualPolicy
├── ProfessorQuickTalkPolicy
├── SettingsMenuPolicy
└── PersistentMediaLog


<!-- GECKO-035-GREEN-POLICIES-2026-09-26 -->
GECKO-035 policies → Launch / Visual / Settings / QuickTalk.


<!-- GECKO-035-GREEN-LOG-2026-09-26 -->
MediaTrace → Logcat + PersistentMediaLog(geckodoku-media.log, 256 KiB).


<!-- GECKO-035-GREEN-SPEECH-GATE-2026-09-26 -->
SpeechOrigin + QUICK_TALK → ProfessorSpeechRequestPolicy → canAccept avant pré-roll.


<!-- GECKO-035-GREEN-FIRST-FRAME-HOLD-2026-09-26 -->
ProfParle → draw fresh frame → pause hidden → audio START → revealHeldFirstFrame → resume.


<!-- GECKO-035-GREEN-RUNTIME-2026-09-26 -->
UI row2 → Nouvelle | Stats | ! | ⚙️
⚙️ → Son / Animations / Journal vidéo
Pierre → canAccept → pre-roll ProfParle → frame held → speak → SPEAK_STARTED → reveal
video fail/timeout → PNG → speak
recorded encouragement → same visual pre-roll → audio onStarted → reveal.


<!-- GECKO-035-V01013-CANDIDATE-2026-09-26 -->
v0.10.13-dev → GECKO-035 candidate → CI finale → test téléphone Fab.


<!-- GECKO-035-CI115-GREEN-2026-09-26 -->
v0.10.13-dev → CI #115 GREEN → APK/AAB → validation téléphone en attente.


<!-- GECKO-036-PNG-CONTINUITY-MISSION-2026-09-27 -->
GECKO-036
Prof.png VISIBLE
→ ProfParle prepare alpha 0
→ first frame held
→ Pierre start
→ revealHeldFirstFrame()
   ├── success → masquer Prof.png
   └── failure/timeout/error → garder Prof.png


<!-- GECKO-036-QUICK-TALK-BUBBLE-2026-09-27 -->
! 
→ ProfessorQuickTalkPolicy
→ PierreSmallTalk[line]
├── ProfessorBubbleView.showMessage(line) [visuel seulement]
└── speakWithProfessorVisual(line, QUICK_TALK) [une seule voix]
    → ProfParle / PNG fallback

NE PAS utiliser le chemin showProfessorBubble qui reparle en PROF_BUTTON sans séparation visuel/voix.


<!-- GECKO-036-RED-2026-09-27 -->
GECKO-036 RED
├── ProfessorPortraitContinuityPolicy
├── FreshPlaybackFrameGate
└── QuickTalkPresentationPolicy


<!-- GECKO-036-GREEN-POLICIES-2026-09-27 -->
ProfessorPortraitContinuityPolicy + FreshPlaybackFrameGate + QuickTalkPresentationPolicy → garde-fous runtime GECKO-036.


<!-- GECKO-036-GREEN-RUNTIME-2026-09-27 -->
PNG visible → video alpha0 → START(gen) → RENDERING_START(gen) → arm renderer(gen, clear stale) → fresh frame(gen) → hold → Pierre START → reveal → hide PNG.
! → bubble visual-only + one QUICK_TALK speech.


<!-- GECKO-036-V01014-CANDIDATE-2026-09-27 -->
v0.10.14-dev → GECKO-036 candidate → CI finale → validation téléphone Fab.


<!-- GECKO-036-CI123-GREEN-2026-09-27 -->
v0.10.14-dev → CI #123 GREEN → APK/AAB → validation téléphone en attente.
