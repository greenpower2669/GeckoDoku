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


<!-- GECKO-036-FRAME-SERIAL-RED-2026-09-27 -->
SurfaceTexture callback → producedSerial++ → requestRender
GL draw → updateTexImage() de toute frame disponible → consumedSerial
gate arm → baselineSerial = producedSerial
validation → consumedSerial > baselineSerial ET bonne génération
INTRO revealOnFirstFrame=false → aucun armement gate.


<!-- GECKO-036-FRAME-SERIAL-GREEN-2026-09-27 -->
onFrameAvailable → producedSerial++
onDrawFrame si producedSerial > consumedSerial → updateTexImage → consumedSerial=producedSerial
gate arm(gen) → baseline=producedSerial
stale consumedSerial<=baseline → consommée, non validante
fresh consumedSerial>baseline + bonne gen → VIDEO_FIRST_FRAME
INTRO gate=false → aucun arm.


<!-- GECKO-036-V01015-CANDIDATE-2026-09-27 -->
v0.10.15-dev → frame serial safe → INTRO hors gate + ProfParle gate sans perte de frame → CI finale → téléphone Fab.


<!-- GECKO-036-CI127-GREEN-2026-09-27 -->
v0.10.15-dev → CI #127 GREEN → APK/AAB → test téléphone Intro + Prof.


<!-- GECKO-036-FINAL-VALIDATION-2026-09-27 -->
GECKO-036 FINAL
v0.10.15-dev / code 26
├── SurfaceTexture
│   ├── onFrameAvailable → producedSerial++
│   ├── updateTexImage sur toute frame disponible
│   ├── stale serial consommé mais non validant
│   └── fresh serial + bonne génération → validation
├── INTRO
│   └── revealOnFirstFrame=false → hors gate
├── Prof
│   ├── Prof.png visible pendant préparation
│   ├── ProfParle reveal réel → PNG masqué
│   └── erreur/timeout → PNG restauré
├── Pierre
│   └── prioritaire, jamais bloqué par la vidéo
├── QUICK_TALK
│   ├── phrase → bulle Prof
│   └── une seule requête vocale
├── Grille
│   └── rectangle immuable sous overlays
└── VALIDATION TÉLÉPHONE FAB : OK


<!-- GECKO-037-LIVING-PROF-MISSION-2026-09-27 -->
GECKO-037
GameEvent
→ ProfessorPlayerContext
   ├── mastery
   ├── impulsivity
   └── momentum
→ ProfessorMoodPolicy
→ weighted categories
→ ProfessorPhraseCatalog (309)
→ ProfessorPhraseHistory
   ├── lastUsedAt[id] 48h
   └── lastPhraseId
→ ProfessorPhraseSelector
   ├── contextual pool
   ├── neighbor fallback
   ├── GENERAL
   └── forced oldest-first
→ one selected ProfessorPhrase
→ bubble(text)
→ Pierre(same text)
→ real speech completion
→ +1s guarded close for QUICK/simple only

Pedagogical Professor path remains separate and persistent on-screen.


<!-- GECKO-037-REMOVE-RECORDED-ENCOURAGEMENTS-2026-09-27 -->
Ancien :
GECKO_CONFIRMED → random RECORDED | PIERRE
RECORDED → Voix_encouragements.mp3 segment

Cible :
GECKO_CONFIRMED → PIERRE ENCOURAGEMENT uniquement
→ speakWithProfessorVisual(ENCOURAGEMENT)
→ onCompletion
→ celebration music si fin

GECKO-037 futur :
ENCOURAGEMENT event → context/mood → ProfessorPhraseSelector → Pierre.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-GREEN-2026-09-27 -->
GECKO_CONFIRMED
→ playEncouragement
→ EncouragementDeliveryPolicy = PIERRE
→ PierreEncouragements (provisoire)
→ SpeechOrigin.ENCOURAGEMENT
→ ProfParle/PNG pipeline
→ onCompletion
→ celebration flow

RECORDED / Voix_encouragements.mp3 : SUPPRIMÉ.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-CI132-2026-09-27 -->
Encouragements enregistrés → RETIRÉS → CI #132 GREEN.
Chemin actif provisoire : succès joueur → PierreEncouragements → ENCOURAGEMENT → ProfParle/PNG.
