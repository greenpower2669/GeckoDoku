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


<!-- GECKO-037-FULL-RED-2026-09-27 -->
GECKO-037 RED
├── ProfessorPhraseCatalog = 309
├── ProfessorPhraseHistory = 48h + persistence + lastPhraseId
├── ProfessorPhraseSelector = weighted/fallback/oldest
├── ProfessorPlayerContextTracker
├── ProfessorMoodPolicy
└── ProfessorQuickBubbleClosePolicy = real completion + 1s + generation guard


<!-- GECKO-037-GREEN-MODEL-MOOD-BUBBLE-2026-09-27 -->
PlayerEvent → ProfessorPlayerContextTracker → ProfessorMoodPolicy → weighted categories
Speech completion → ProfessorQuickBubbleClosePolicy(token) → +1000ms close seulement si token courant/simple.


<!-- GECKO-037-GREEN-CATALOG-309-2026-09-27 -->
ProfessorPhraseCatalog = legacy(100) + modern(209) = 309
legacy IDs 001..100 immuables
modern IDs prof_* fournis par Fab
normalizeText + textSimilarity + nearDuplicatePairs.


<!-- GECKO-037-GREEN-HISTORY-SELECTOR-2026-09-27 -->
Selector → RARE(4% max) / FAB(3%) → weighted category → cooldown 48h → !lastPhraseId → fallback categories → GENERAL → forced oldest-first → history.markUsed(id).


<!-- GECKO-037-RUNTIME-GREEN-2026-09-27 -->
MainActivity events → ProfessorLifeController.observe → context/mood
canAccept(origin) → controller.choose → phrase ID/history → bubble(text) + Pierre(same text) → real completion → guarded +1000ms close
Prof button pedagogy → pedagogical bubble token → no auto-close.


<!-- GECKO-037-RUNTIME-CLEANUP-2026-09-27 -->
MainActivity small speech → ProfessorLifeController ONLY. Ancien PierreSmallTalkSelector/QuickTalkPresentationPolicy : hors runtime.


<!-- GECKO-037-V01016-CANDIDATE-2026-09-27 -->
v0.10.16-dev → GECKO-037 complete runtime → CI versionnée → validation téléphone Fab.


<!-- GECKO-037-CI140-GREEN-2026-09-27 -->
GECKO-037 v0.10.16-dev / CI #140 GREEN

player event
→ context tracker
→ mood
→ weighted category
→ catalog 309
→ 48h + lastPhraseId
→ fallback/oldest-first
→ selected phrase
→ bubble EXACT text
→ Pierre EXACT text
→ real completion
→ guarded 1000ms close

Runtime sources:
! / ambient smalltalk / encouragement / contextual reaction
→ same ProfessorLifeController path.

Pedagogy:
Prof button / solver explanation
→ persistent pedagogical bubble
→ no auto-close.

Phone validation Fab → pending.


<!-- GECKO-037-TECHNICAL-CLOSE-2026-09-27 -->
GECKO-037 — ARCHITECTURE CONSOLIDÉE

GameEvent
→ ProfessorPlayerContext
   ├── mastery
   ├── impulsivity
   ├── momentum
   └── difficulty
→ ProfessorMoodPolicy
→ weighted PhraseCategory
→ ProfessorPhraseCatalog[309]
→ ProfessorPhraseHistory
   ├── lastUsedAt[id] / 48h
   └── lastPhraseId
→ ProfessorPhraseSelector
   ├── contextual pool
   ├── neighbor fallback
   ├── GENERAL
   └── forced oldest-first
→ selected ProfessorPhrase
→ same text → ProfessorBubbleView + Pierre
→ real speech completion
→ +1s guarded close for simple reactions only

Pedagogical bubble path
→ no auto-close.

Recorded encouragement path
→ removed.

Media / grid / ProfParle pipeline
→ unchanged and protected.

<!-- GECKO-038-DESIGN-SUDOKU-2026-09-27 -->
# GECKO-038 — ORGANIGRAMMES SECOND MODE SUDOKU

STATUT : conception seulement, aucun code autorisé.

## Architecture produit

```text
                    GECKODOKU APP
                         |
                     GameMode
              +----------+----------+
              |                     |
         GECKODOKU                SUDOKU
              |                     |
   moteur historique          SudokuEngine dédié
              |                     |
              +----------+----------+
                         |
                 services partagés
      Pierre / 309 / mood / audio / settings / UI shell
```

## Entrées stables, routage par mode

```text
TOUCH / KEYBOARD
      |
 stable input binding
      |
   GameMode
   /      \
GECKODOKU  SUDOKU
   |         |
historical   sudoku
action       action
```

Règle : ne pas faire un cycle unbind/rebind complet à chaque bascule de mode.

## État Sudoku vs représentation

```text
SudokuCellState
(empty / given / player value / notes)
          |
      VisualStyle
   +------+------+ 
   |      |      |
CLASSIC  NB   COLORED
   |      |      |
 text   source region from canonical PNG
          |
  click-through visual layer
          |
 historical/stable hitbox below
```

Le visuel ne devient jamais la donnée.

## Sélecteur magique 3 états

```text
POINTER DOWN
    |
previewStyle = zone courante
    |
POINTER MOVE -------------------------------+
    |                                      |
gauche / milieu / droite                    |
    |                                      |
style ciblé TRÈS GRAND                      |
autres styles petits                        |
    |                                      |
GRID LIVE PREVIEW                           |
    +-------------- boucle tant que doigt --+
    |
POINTER UP
    |
selectedStyle = previewStyle
    |
persist selection
```

## Assets visuels

```text
PlancheGeckoDeNombreNB.png ------> GECKO_NB
PlancheGeckoDeNombreColored.png -> GECKO_COLORED
chiffres natifs -----------------> CLASSIC_NUMBERS
```

Aucune conversion / recompression / génération de sprites dérivés requise.

## Protection case vide

```text
cell empty
   |
GameMode?
 /      \
GECKO   SUDOKU
 |        |
historique  véritable état vide
inchangé    (pas de fuite du renderer Gecko)
```

## Prof partagé

```text
Game event
   |
GameMode adapter
   |
ProfessorPlayerContext
   |
309 / mood / 48h / Pierre / bubble
   |
si pédagogie demandée
   +--> GeckoDoku solver explanation
   |
   +--> Sudoku technique explanation
```



<!-- GECKO-038-RED-START-2026-09-27 -->
GECKO-038 GO → branch isolated → RED engine/generator/hints/styles → GREEN core → UI routing → phone validation.


<!-- GECKO-038-CORE-GREEN-2026-09-27 -->
SudokuGenerator → unique puzzle → SudokuGameEngine → values/notes/history
SudokuSnapshot → SudokuHintEngine → naked single / hidden single row/column/box
GameMode ⟂ SudokuVisualStyle.


<!-- GECKO-038-CORE-COMPILE-FIX-2026-09-27 -->
notes : MutableList<MutableSet<Int>> → capture Set immuable → restore MutableSet.


<!-- GECKO-038-UI-SURFACE-2026-09-27 -->
SudokuBoardView (touch + grid + notes) → SudokuValueOverlayView (click-through values) → CLASSIC / NB / COLOR
SudokuStyleSelectorView DOWN/MOVE → previewStyle → overlay.invalidate ; UP → committed style + persistence (au câblage MainActivity).


<!-- GECKO-038-ASSET-CATALOG-2026-09-27 -->
AssetMediaCatalog.GECKO_NUMBER_NB / GECKO_NUMBER_COLORED → PNG canoniques assets/gecko.


<!-- GECKO-038-MAIN-WIRING-2026-09-27 -->
Settings GAME_MODE → setGameMode → visibility router
GECKODOKU → GeckoBoardView + GameEngine historique
SUDOKU → SudokuBoardView(touch) + SudokuValueOverlay(click-through) + SudokuGameEngine
Selector MOVE → preview style → overlay ; UP → persist style
Prof button(SUDOKU) → SudokuHintEngine → pedagogical bubble + Pierre.


<!-- GECKO-038-CI150-CONTRACT-2026-09-27 -->
SettingsMenuPolicy = GAME_MODE + SOUND + ANIMATIONS + MEDIA_LOG ; affectsBoardLayout=false.


<!-- GECKO-038-CANDIDATE-0110-2026-09-27 -->
GECKO-038 candidate: 0.11.0-dev code 28 → CI candidate → APK/AAB → validation téléphone Fab → seulement ensuite décision suite/merge/release.


<!-- GECKO-038-SPRITE-CROP-2026-09-27 -->
canonical PNG → sourceRect only at draw time → cell target. NB uses per-digit normalized bounds; Colored uses 5×2 card geometry with label exclusion.


<!-- GECKO-038-CROP-SYNTAX-FIX-2026-09-27 -->
SudokuNumberSheetLayout : structure Kotlin corrigée, logique sourceRect inchangée.


<!-- GECKO-038-CI154-GREEN-2026-09-27 -->
GECKO-038 0.11.0-dev code28 → CI #154 GREEN → APK/AAB → TEST FAB
TEST FAB doit couvrir : GECKODOKU historique ; SUDOKU tactile ; 3-state selector live ; CLASSIC/NB/COLOR crops ; Prof/Pierre ; notes/undo/redo.

<!-- GECKO-038-CANONICAL-CLOSE-2026-09-27 -->
# GECKO-038 — CARTE CANONIQUE FINALE

```text
                    GeckoDoku App
                         |
                     GameMode
              +----------+----------+
              |                     |
         GECKODOKU                SUDOKU
              |                     |
   GeckoBoardView intact     SudokuBoardView
   GameEngine historique      + touch selection
              |               + notes grid
              |                     |
              |              SudokuGameEngine
              |                     |
              |              SudokuValueOverlay
              |               (click-through)
              |                     |
              |          +----------+----------+
              |          |          |          |
              |       CLASSIC      NB       COLOR
              |                     |
              +----------- services partagés -----------+
                          Pierre / Prof 309
                          mood / 48h / bubble
                          audio / celebration
```

```text
SudokuStyleSelector
DOWN → preview
MOVE → slot → live redraw → haptic
UP   → persist selected style
CANCEL → revert committed style
```

```text
APK 0.11.0-dev / code 28
        |
     CI #154
        |
      GREEN
        |
 validation téléphone Fab
        |
   retour utilisateur
        |
correction ciblée OU prochaine mission
```

État : aucune mission active ; ordre de mission vidé.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-RED-2026-09-27 -->
# Invariant dialogue Prof

```text
source événement
   ↓
parole simple acceptée ?
   ↓
armement token petite bulle
   ↓
ProfessorBubbleView = texte exact
   ↓
status = "Prof Gecko"
   ↓
ProfessorSpeech / SpeechOrigin conservée
   ↓
ProfParle
   ↓
callback FIN RÉELLE
   ↓
ProfessorQuickBubbleClosePolicy
   ↓
+ ~1 seconde
   ↓
fermeture si token/generation toujours valide
```

PROF_BUTTON pédagogique reste hors de ce flux auto-close.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-GREEN-CODE-2026-09-27 -->
# Chemin canonique runtime

```text
AMBIENT fixe / STATS / moteur 309
            ↓
speakSimpleProfessorBubble(text, origin)
            ↓
ProfessorSimpleSpeechCoordinator.begin
            ↓
canAccept ? ── non → rien à afficher
            ↓ oui
token simple + bubbleText == speechText
            ↓
showProfessorBubbleVisualOnly
status = "Prof Gecko"
            ↓
speakWithProfessorVisual(origin)
            ↓
ProfessorSpeech.speak (une requête)
     ↓ accepté       ↓ refus tardif
 fin réelle          token courant ?
     ↓                    ↓ oui
+1s policy           fermer bulle
     ↓
fermer si token encore courant
```

`showProfessorBubble()/PROF_BUTTON` reste le chemin pédagogique distinct.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-CI156-GREEN-2026-09-27 -->
# Audit final GREEN

```text
PROF_BUTTON pédagogique
    → showProfessorBubble
    → bulle persistante
    → speakWithProfessorVisual
    → ProfessorSpeech

Toutes les autres paroles du Prof
    → speakSimpleProfessorBubble
    → même texte bulle/voix
    → status court
    → SpeechOrigin conservée
    → speakWithProfessorVisual
    → ProfessorSpeech
    → vraie fin
    → +1 s
    → fermeture tokenisée
```

Aucun événement métier n'appelle directement `ProfessorSpeech.speak()`.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-PHONE-VALIDATED-2026-09-27 -->
# Invariant téléphone validé

```text
événement Prof
   ↓
texte unique
   ↓
ProfessorBubbleView = texte exact
   ↓
ProfessorSpeech / SpeechOrigin
   ↓
ProfParle / Pierre
   ↓
fin réelle de parole
   ↓
+ ~1 s
   ↓
fermeture si token courant
```

Validation Fab téléphone : OK.

<!-- GECKO-038-SUDOKU-TACTILE-UX-MISSION-2026-09-27 -->
# GECKO-038 — UX Sudoku tactile cible

```text
ÉCRAN SUDOKU
    |
    +-- grille 9×9
    |
    +-- selector CLASSIC / NB / COLOR
    |
    +-- pavé 1..9
    |
    +-- Notes / Effacer / Undo / Redo
    |
    +-- Nouvelle / ! / ⚙️
             |
             +-- Mode
             +-- Difficulté
             +-- Son / Animations / Journal
```

```text
TAP CASE
   → sélection
   → pavé 1..9 / Notes

LONG PRESS CASE
   → palette locale tactile
   → valeur 1..9
   → candidats 1..9
   → effacer
```

```text
Candidates = positions fixes 3×3 dans la case

1 2 3
4 5 6
7 8 9

        ↓ VisualStyle
CLASSIC / GECKO_NB / GECKO_COLORED
        ↓
même style pour valeur principale ET mini-candidats
```

```text
Player candidate action ─┐
                         ├→ même candidate model → même renderer
Professor hint action ───┘
```

Overlay local / bulle / selector :
**aucun reflow du plateau**.



<!-- GECKO-038-TACTILE-RED-2026-09-27 -->
GECKO geometry freeze ─┐
                       ├→ GameModeBoardGeometryPolicy → overlay board
SUDOKU geometry freeze ┘

Prof candidate hint → transient candidate overlay → SAME candidate renderer → no player undo mutation.


<!-- GECKO-038-TACTILE-GREEN-CORE-2026-09-27 -->
Policies pures → GameModeBoardGeometryPolicy / SudokuCandidateLayout / SudokuPaletteLayoutPolicy / SudokuPopupPlacementPolicy / SudokuProfessorCandidatePolicy.
Renderer commun → SudokuDigitRenderer → grille + palette + futurs candidats Prof.


<!-- GECKO-038-TACTILE-INPUT-SETTINGS-2026-09-27 -->
SudokuBoardView → tap / long-press callbacks. Settings entriesFor(SUDOKU) → GAME_MODE + DIFFICULTY + réglages historiques.


<!-- GECKO-038-TACTILE-STYLED-CANDIDATES-2026-09-27 -->
snapshot.notes + transient Prof candidates → SudokuCandidateLayout(3×3) → SudokuDigitRenderer(style courant).


<!-- GECKO-038-TACTILE-MAIN-WIRING-2026-09-27 -->
GameMode → geometry freeze distinct → board overlay ends at boardAnchor actuel.
Long press cell → popup placement policy → QuickPalette → value(false notes) / candidate(true notes) / erase → SudokuGameEngine.
Prof hint → logical candidate mask → transient overlay → same renderer.


<!-- GECKO-038-TACTILE-RECT-IMPORT-2026-09-27 -->
Renderer contract inchangé ; Rect import only.


<!-- GECKO-038-TACTILE-GEOMETRY-RESET-2026-09-27 -->
system insets change → geometryPolicy.reset() → reset GECKODOKU + SUDOKU freezes.


<!-- GECKO-038-TACTILE-CANDIDATE-0111-2026-09-27 -->
0.11.1-dev/code29 → CI candidate → APK/AAB → validation téléphone Fab : pavé + selector + long press + candidats 3 styles.


<!-- GECKO-038-TACTILE-CI164-GREEN-2026-09-27 -->
0.11.1-dev/code29 → CI #164 GREEN → APK/AAB → TEST FAB
TEST FAB : selector visible + pad visible + long press + palette + candidats 3 styles + Prof candidats + GeckoDoku historique.

<!-- GECKO-038-GRID-READABILITY-REVISION-2026-09-27 -->
# Priorité écran révisée

```text
largeur utile écran
      ↓
- 3 px gauche
- 3 px droite
      ↓
GRILLE SUDOKU CARRÉE = priorité #1
      ↓
espace vertical restant
      ↓
commandes compactes / overlays / long press
```

```text
Cellule vide
   ↓
candidates 1..9
   ↓
positions fixes 3×3
   ↓
PETITS CHIFFRES NOIRS
(indépendants du VisualStyle principal)
```

```text
Valeur principale → CLASSIC / GECKO_NB / GECKO_COLORED
Mini-candidats    → NOIR, lisibilité prioritaire
```

<!-- GECKO-038-PROF-PLAY-GESTURE-2026-09-27 -->
# Prof Sudoku — interaction canonique

```text
TAP PROF
  ↓
chercher déduction sûre
  ↓
expliquer + montrer candidats
  ↓
PendingProfessorSudokuMove
  ↓
TAP PROF à nouveau
  ↓
pending encore valide ?
  ├─ non → recalculer / expliquer
  └─ oui → jouer origin=PROFESSOR
```

```text
LONG PRESS PROF
  ↓
recalculer déduction sûre
  ↓
valide ?
  ├─ non → aucune mutation
  └─ oui → jouer origin=PROFESSOR
```

```text
mutation grille / undo / redo / new / replay / mode / difficulté
  ↓
invalider ou revalider le pending
```


<!-- GECKO-038-FULLWIDTH-PROF-RED-2026-09-27 -->
RED contracts → FullWidthBoard(3px) + CandidateVisual(black) + ProfessorInteraction(pending) + MoveOrigin(PLAYER/PROFESSOR).


<!-- GECKO-038-GREEN-POLICIES-2026-09-27 -->
FullWidthBoardPolicy(3px) | CandidateVisualPolicy(BLACK) | ProfessorInteractionPolicy(pending fingerprint).


<!-- GECKO-038-GREEN-MOVE-ORIGIN-2026-09-27 -->
enterDigit(origin=PLAYER default | PROFESSOR) → EngineState.lastMoveOrigin → undo/redo round-trip.


<!-- GECKO-038-GREEN-BLACK-CANDIDATES-2026-09-27 -->
candidate mini → CandidateVisualPolicy → CLASSIC + mini=true → BLACK. Overlay inner margin=0px.


<!-- GECKO-038-GREEN-BOARD-PALETTE-2026-09-27 -->
BoardView inner margin=0px. QuickPalette: Value→VisualStyle ; Candidate→BLACK mini number.


<!-- GECKO-038-GREEN-FULLWIDTH-LAYOUT-2026-09-27 -->
SUDOKU → reserve boardAnchor(screenWidth-6, weight0) → overlay left3 square → selector48 → undo/redo40. GECKODOKU → boardAnchor height0 weight1 historique.


<!-- GECKO-038-CANDIDATE-0112-2026-09-27 -->
0.11.2-dev/code30
├─ Grid: width useful - 6px, inner margin 0
├─ Candidate: black mini numbers
├─ Prof tap → Explain(pending)
│   └─ tap again → Apply(PROFESSOR)
├─ Prof long press → recompute → Apply(PROFESSOR)
└─ Cell long press → QuickPalette
    ├─ Value 1..9
    ├─ Candidate 1..9
    ├─ 🦎 Gecko marker
    └─ Erase

Gecko marker → engine annotation → Undo/Redo → overlay watermark → animated iff Animations ON.


<!-- GECKO-038-CI177-GREEN-2026-09-27 -->
CI #177 GREEN → APK/AAB 0.11.2-dev → test téléphone Fab → corrections perceptuelles ciblées seulement.

<!-- GECKO-039-MESSAGE-INVARIANT-2026-09-27 -->
Professor event → raw natural phrase → ProfessorBubbleView(header="Prof Gecko", body=phrase) → Pierre voice.
Status → action/technique only, never speaker-prefix duplication.
Invariant shared by GECKODOKU / SUDOKU / future GOMOKU.



<!-- GECKO-039-TAP-GECKO-RED-2026-09-27 -->
Sudoku tap → SudokuCellTapPolicy → empty playable=TOGGLE_GECKO_MARKER | given/filled=SELECT_ONLY.
Professor text → ProfessorDialogTextPolicy.normalize → same normalized body for bubble + Pierre.


<!-- GECKO-039-TAP-GECKO-GREEN-2026-09-27 -->
tap Sudoku → select cell → CellTapPolicy → empty=toggle marker | given/filled=select only.
Professor message → normalize once → identical normalized text to Bubble + Pierre.
CI push includes gecko-039-sudoku-tap-gecko-gomoku.


<!-- GECKO-039-COMPILE-FIX-2026-09-27 -->
status Professor move → digit.toString() + " posé".


<!-- GECKO-039-CANDIDATE-0113-2026-09-27 -->
0.11.3-dev/code31 → CI candidate → APK/AAB → test téléphone Fab.
Tap empty Sudoku → toggle Gecko marker.
Tap given/filled → select only.
Long press → palette.
Professor raw text → normalize leading speaker → identical Bubble/Pierre text.
Gomoku → future only / no runtime code.


<!-- GECKO-039-CI180-GREEN-2026-09-27 -->
0.11.3-dev/code31 → CI #180 GREEN → APK/AAB → validation téléphone Fab.
PARTIE C Gomoku remains future-only.

<!-- GECKO-039-MISSION-CLEANUP-ANIM-TITLE-2026-09-27 -->
GECKO-039 remaining:
Sudoku Gecko marker → reuse CLASSIC Gecko animation pipeline/media → keep tap/retap semantics unchanged.
Title → always "GeckoDoku 🦎" → mode identity below title.
Gomoku → mission only / no code without explicit GO.

<!-- GECKO-039-DOUBLE-TAP-MARKERS-PROF-REASONING-2026-09-27 -->
# Sudoku — gestes + aide Prof

```text
SINGLE TAP empty cell
  → toggle Gecko-repère

DOUBLE TAP cell
  → open personal-marker keyboard/palette

LONG PRESS cell
  → Sudoku local palette
    → value 1..9
    → candidates 1..9
    → erase
```

```text
SudokuHintEngine / reasoning engine
  ↓
ReasoningTrace
  ├─ technique
  ├─ target digit
  ├─ source cells
  ├─ projections
  ├─ eliminated cells/candidates
  ├─ surviving target
  └─ ordered steps
       ↓
       ├────────→ GraphicReasoningOverlay
       └────────→ DetailedProfessorNarration

Same trace = no text/graphic contradiction.
```

<!-- GECKO-039-THREE-PHASE-PLAN-2026-09-27 -->
GECKO-039 execution order:
CLASSIC
  → wider board
  → audit/extract shared Gecko animation pipeline
  → candidate A
SUDOKU
  → shared Classic animations
  → clean title
  → double tap personal markers
  → ReasoningTrace → graphic + text
  → candidate B
GOMOKU
  → independent logical board
  → viewport 12x12
  → drag/pan
  → tap plays / drag pans
  → green player Gecko / dynamically yellow Prof Gecko
  → AI uses whole logical board
  → candidate C

Each mode remains compartmentalized and independently testable.

<!-- GECKO-039-GOMOKU-ZOOM-DIFFICULTY-2026-09-27 -->
GOMOKU VIEW:
logical board
  → viewport transform
     ├─ pan(dx,dy)
     └─ pinch(scale around focal logical point)
default ≈ 12x12 visible
zoom does NOT mutate game state.

GOMOKU AI DIFFICULTY:
L1 immediate tactics
→ L2 short lookahead
→ L3 multi-move forcing sequences / double threats
→ L4 traps / bait / forced replies / counter-traps
Higher difficulty = deeper future projection, not cheating.



<!-- GECKO-039-PHASE1-RED-2026-09-27 -->
CLASSIC RED → ClassicBoardReadabilityPolicy(width,height,gutter) → max width / no overflow.


<!-- GECKO-039-PHASE1-GREEN-2026-09-27 -->
Classic board → full screenRoot width → ClassicBoardReadabilityPolicy(3px) → compact gutter. Classic animations → shared target-based pipeline → reusable by Sudoku/Gomoku.


<!-- GECKO-039-PHASE2-RED-2026-09-27 -->
SUDOKU RED → GesturePolicy + custom marker state + AppTitlePolicy + SudokuReasoningTrace.


<!-- GECKO-039-PHASE2-MODELS-2026-09-27 -->
Sudoku gestures → intent policy. ReasoningTrace → steps/eliminations. App title → mode independent.


<!-- GECKO-039-PHASE2-ENGINE-TRACE-2026-09-27 -->
Engine customMarkers ↔ Undo/Redo. HintEngine → conflictFor(row/col/box) → grouped eliminations → ordered ReasoningTrace.


<!-- GECKO-039-PHASE2-VIEWS-2026-09-27 -->
SudokuBoardView → single confirmed / double / long. Overlay → custom marker + cumulative reasoning projection.


<!-- GECKO-039-PHASE2-MAIN-WIRING-2026-09-27 -->
doubleTap→marker dialog. Gecko toggle→shared Classic video. Reasoning step→overlay + bubble + Pierre→next on speech completion.


<!-- GECKO-039-PHASE2-COMPILE-FIX-2026-09-27 -->
SudokuSnapshot.customMarkerAt(cell) → customMarkers map.


<!-- GECKO-039-PHASE3-RED-2026-09-27 -->
GOMOKU RED → Engine + ViewportPolicy + DifficultyProfile/AiDecision.


<!-- GECKO-039-GOMOKU-CORE-GREEN-2026-09-27 -->
GOMOKU core: Snapshot → Engine.play → WinDetector. ViewportPolicy → initial12 / pan / focal zoom. DifficultyProfile → depth/beam/trapAware. AI → immediate win → immediate block → ordered threats → bounded alpha-beta.


<!-- GECKO-039-GOMOKU-GEOMETRY-COMPILE-FIX-2026-09-27 -->
GameModeBoardGeometryPolicy: GECKODOKU→geckoPolicy | SUDOKU→sudokuPolicy | GOMOKU→gomokuPolicy.


<!-- GECKO-039-THREE-MODE-TEST-FIX-2026-09-27 -->
GameMode = GECKODOKU | SUDOKU | GOMOKU ; SudokuVisualStyle reste 3 styles indépendants.


<!-- GECKO-039-GOMOKU-UI-RED-2026-09-27 -->
Gomoku UI RED → GesturePolicy(PLAY|PAN|ZOOM) + YellowFilterPolicy + Settings difficulty + geometry isolation.


<!-- GECKO-039-GOMOKU-UI-POLICIES-GREEN-2026-09-27 -->
GesturePolicy: >=2 pointers|scale→ZOOM ; 1 pointer distance>=threshold→PAN ; sinon PLAY. Yellow filter: R<-R+G, G conservé, B réduit, A identité. Settings difficulty for non-Classic modes.

<!-- GECKO-039-GOMOKU-BOARDVIEW-2026-09-27 -->
GomokuBoardView → snapshotProvider → draw logical board through GomokuViewport. Touch: tap→onPlayCell | drag→pan | pinch→zoom. Professor sprite = same bitmap + yellow ColorMatrix.

<!-- GECKO-039-GOMOKU-MAINACTIVITY-2026-09-27 -->
Mode chooser → GOMOKU → startGomokuGame → GomokuBoardView. Player tap→engine.play→green animation→PROF turn. Prof button→background GomokuAi→UI apply yellow move→PLAYER turn. Win→center line→lock→reaction.

<!-- GECKO-039-CANDIDATE-0120-2026-09-27 -->
0.12.0-dev/code32
├─ Classic GREEN
├─ Sudoku GREEN
└─ Gomoku GREEN integration #196
   ├─ 19x19 logical board
   ├─ viewport default ~12x12
   ├─ tap play / drag pan / pinch zoom
   ├─ PLAYER green Gecko
   ├─ PROFESSOR same sprite + yellow matrix
   └─ background bounded AI → Prof button move

Next → candidate CI → APK/AAB → Fab phone validation.

<!-- GECKO-039-CI197-GREEN-2026-09-27 -->
CI #197 GREEN → artifact 0.12.0-dev → test téléphone Fab → corrections perceptuelles ciblées seulement.


<!-- GECKO-039-POST-TEST-ARCH-2026-09-27 -->
# Architecture cible — Gomoku partagé + Prof conseiller

Mode chooser
  → GeckoDoku
  → Sudoku
  → Gomoku / VS_PROFESSOR
  → Gomoku / HUMAN_VS_HUMAN

GomokuMatchMode
  ├─ VS_PROFESSOR
  │    ├─ GREEN = HUMAN
  │    └─ YELLOW = PROF_AI (auto-turn)
  └─ HUMAN_VS_HUMAN
       ├─ GREEN = HUMAN
       └─ YELLOW = HUMAN

Les deux variantes
  → même GomokuGameEngine
  → même GomokuBoardView
  → même GomokuViewportPolicy
  → même pipeline média.

ProfAdvisor
  → analyse explicitement pour snapshot.currentPlayer / camp demandé
  → clic = explain only
  → long VS_PROFESSOR = apply GREEN for human
  → long HUMAN_VS_HUMAN = deep explain only.

GomokuBoardView PNG rect (source de vérité)
  → dynamic target provider
  → RichMediaOverlayView
  → ChromaKeyVideoView
  → refresh bounds à chaque onViewportChanged
  → suppress static stone while overlay active
  → restore PNG on completion.


<!-- GECKO-039-CORE-MATCH-MODES-2026-09-27 -->
GameMode.GOMOKU + GameModePreferences.gomokuMatchMode
  → GomokuMatchMode.VS_PROFESSOR | HUMAN_VS_HUMAN
  → même GomokuGameEngine.
GomokuAi.chooseMoveFor(snapshot, difficulty, player)
  → PROFESSOR : moteur historique
  → PLAYER : snapshot à rôles inversés → moteur historique.


<!-- GECKO-039-MEDIA-DYNAMIC-2026-09-27 -->
GomokuBoardView.geckoRectOnScreen(cell)
  → targetProvider
  → RichMediaOverlayView.refreshDynamicTargets()
  → ChromaKeyVideoView bounds.
Video shader : blue key/despill → optional yellow tint → alpha blend.
Static stone : setMediaStoneSuppressed(cell,true) après première frame visible → restore à la fin.


<!-- GECKO-039-MAIN-INTEGRATION-2026-09-27 -->
Mode chooser(4 choix UI)
  → GameMode.GOMOKU + GomokuMatchMode.
Board tap
  → VS_PROFESSOR : GREEN human → auto playGomokuProfessorTurn(YELLOW)
  → HUMAN_VS_HUMAN : currentPlayer human → alternate.
Prof button
  → short: showGomokuProfessorAdvice(apply=false)
  → long VS: advice + engine.play(GREEN) → auto YELLOW
  → long H2H: deep advice only.
Viewport change → RichMediaOverlayView.refreshDynamicTargets.


<!-- GECKO-039-POST-TEST-CI201-GREEN-2026-09-27 -->
HEAD code `f6aafece850adf1d7e094463655847764b3d91fd`
  → CI #201 GREEN
  → tests
  → APK `GeckoDoku-v0.12.0-dev.apk`
  → AAB `GeckoDoku-v0.12.0-dev.aab`
  → prochaine porte : test téléphone Fab.


<!-- GECKO-040-ARCH-MAP-2026-09-27 -->
# Architecture cible de la nouvelle mission

Classic screen
  → compact controls
  → Save + Journal déplacés dans Settings
  → available bounds
  → largest square board
  → horizontal centering.

Difficulty request
  → background generator loop
  → solve/rate candidate
  → accept only requested difficulty
  → continue until match or user cancel.

Completion event
  → AssistanceTracker
  → star rating 1..5
  → Stats by difficulty
  → HallOfFameEntry(playerName, mode, difficulty, stars, metadata).

PlayerProfile
  → default name "GeckoTétu"
  → editable
  → persistent.

UserDataBackup
  → versioned export package
  → settings + saves + progress + stats + history + hall of fame + profile
  → validation/migration
  → transactional import.


<!-- GECKO-040-PHASE1-2026-09-28 -->
Classic boardAnchor → GeckoBoardView → ClassicBoardReadabilityPolicy → side=min(width,height-gutter) → left=(width-side)/2.
Difficulty UI → requestClassicPuzzle(token) → background generateExact → repeated bounded batches → exact DifficultyIndexer profile → apply only if token still current.


<!-- GECKO-040-PHASE2-MAP-2026-09-28 -->
Professor help request → AssistanceKind points → assistancePoints(session).
Completion Classic/Sudoku → CompletionRatingPolicy → stars 1..5 → PlayerStatsStore + HallOfFameStore.
PlayerProfileStore → GeckoTétu + soundEnabled.
Settings → edit profile / Hall of Fame / clear result history.


<!-- GECKO-040-PHASE3-MAP-2026-09-28 -->
Settings → ACTION_CREATE_DOCUMENT/ACTION_OPEN_DOCUMENT → UserDataBackup(schema v1) → validate all → snapshot prefs → commits → rollback on failure → recreate on success.


<!-- GECKO-040-CI206-GREEN-2026-09-28 -->
GECKO-040 pipeline validé CI :
4912ff7 (layout/exact difficulty) → #204 GREEN
b72f149 (stars/Hall/profile) → #205 GREEN
7283802 (backup import/export) → #206 GREEN
→ artifact 0.12.0-dev
→ prochaine porte : validation téléphone Fab.

<!-- GECKO-041-V013-MAP-2026-09-28 -->
0.13
├─ PlayerStatsStore → global historique + mode/difficulté
├─ CompletionRating → Classic | Sudoku | victoire Gomoku VS Prof
├─ HallOfFame → mode → difficulté → étoiles/nom/temps/date
├─ Gomoku help → advice/direct move → assistancePoints
└─ GomokuProfessorPersona
   ├─ Découverte n'adoucit pas le ton
   ├─ conseils exacts
   └─ habillage taquin familial

<!-- GECKO-041-V013-FIX-MAP-2026-09-28 -->
Gomoku auto turn (Prof jaune) → aucune assistance.
Gomoku advice/long press demandé → assistancePoints selon ADVICE/DIRECT_MOVE.

<!-- GECKO-041-V013-WORKFLOW-MAP-2026-09-28 -->
app/build.gradle.kts versionName → Resolve app version → GITHUB_ENV APP_VERSION → nom APK + nom AAB + nom artifact.

<!-- GECKO-041-V013-GREEN-MAP-2026-09-28 -->
commit cae4ed4 → CI #210 GREEN → APK 0.13 + AAB 0.13 → artifact 10944486757 → test téléphone Fab.
## GECKO-042 — carte architecture Abeilles & Geckos

GameMode.BEES_GECKOS
→ MainActivity
  → BeeGeckoGameEngine
  → BeeGeckoBoardView
  → BeeGeckoSessionStore
  → PlayerStatsStore / HallOfFameStore / CompletionRatingPolicy
  → RichMediaOverlayView

BeeGeckoPuzzle
→ HexCoord(q,r)
→ BeeGeckoPiece { GECKO, BEE }
→ BeeGeckoPair 1 ↔ 1

BeeGeckoRules
→ getHexNeighbors()
→ candidateOpposites()
→ normalizePair()
→ validatePairs()
→ isComplete()

BeeGeckoSolver
→ candidat forcé
→ propagation des réservations
→ backtracking / unicité
→ BeeGeckoHint visuel + textuel

BeeGeckoGenerator
→ profil de difficulté
→ placement logique complet
→ contrôle d'unicité

BeeGeckoViewportPolicy
→ zoom autour du focus
→ pan
→ clamp
→ recentrage

Rendu : logique axiale indépendante des pixels ; `BeeGeckoBoardView` transforme axial → écran. Les projections Prof et targets vidéo utilisent les mêmes coordonnées du plateau. `AbeilleTr.png` est le sprite statique ; `Abeillefondvert.mp4` l'animation chroma ciblée sur une case Abeille.

Persistance : `BeeGeckoSessionStore` → SharedPreferences → `UserDataBackup.preferenceNames`.
Résultats : `GameMode.BEES_GECKOS` → stats mode+difficulté → étoiles → Hall of Fame.
### CI #212 — impact transversal

`GameMode.entries` contient désormais 4 valeurs. Les tests structurels qui comptaient les modes doivent évoluer sans toucher aux trois styles visuels Sudoku, qui restent exactement 3.
### Extension repères/navigation

`BeeGeckoGameEngine.markers` → `BeeGeckoSnapshot.markers` → `BeeGeckoBoardView.drawMarkers()`.
`BeeGeckoSessionStore` sérialise les repères avec q/r + nom de `CustomMarker`.
`SettingsEntry.NEXT_UNRESOLVED` centre la caméra sur la première pièce encore hors couple ; `RECENTER` reste le retour global.
