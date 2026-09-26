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


Build GECKO-026
└── run #37 vert
    ├── ProfessorUiPolicyTest
    ├── suite existante
    ├── APK v0.10.2-dev
    └── AAB v0.10.2-dev


## GECKO-027
professorButtonHost
├── Button
│   ├── elevation = 0 dp
│   ├── stateListAnimator = null
│   └── surface tactile + texte
└── Prof.png
    ├── elevation = 18 dp
    ├── bringToFront après ajout
    └── bringToFront avant chaque micro-animation
        └── scale/translationY uniquement

Résultat attendu
└── Button jamais devant Prof, même pendant press/animation


Build GECKO-027
└── run #41 vert
    ├── ProfessorUiPolicyTest
    ├── suite existante
    ├── APK v0.10.3-dev
    └── AAB v0.10.3-dev


## GECKO-028
GeckoBoardPalette
├── couleur région utilisée par GeckoBoardView
└── même couleur → masque animation cellule
    ├── inset 4 %
    └── vidéo = rectangle complet

ProfessorSpeech
└── ProfessorVoicePolicy
    ├── voix FR explicitement masculine si identifiable
    └── sinon voix FR locale + pitch 0,78

Prof.png bouton
├── clic → action aléatoire BOUNCE/TILT/NOD
└── idle → timer 10–20 s
    ├── action aléatoire
    ├── replanification automatique
    └── callbacks retirés pause/destroy/Anim OFF


Build GECKO-028
└── run #45 vert
    ├── CellAnimationStyleTest
    ├── ProfessorVoicePolicyTest
    ├── ProfessorIdleAnimationPolicyTest
    ├── APK v0.10.4-dev
    └── AAB v0.10.4-dev


## GECKO-029
Prof.png idle
└── délai 2–3 s
    ├── bulle ouverte : autorisé
    ├── clic : immédiat + replanification
    └── pause / Anim OFF / victoire : bloqué

RichMediaScheduler Gecko
├── chance = 45 %
└── cooldown = 45 s

Victoire
├── VictoryCelebrationView finit
│   └── stop FX procéduraux
└── MP3 celebration continue
    └── fin naturelle MediaPlayer


Build GECKO-029
└── run #49 vert
    ├── Prof idle 2–3 s
    ├── Gecko scheduler 45 % / 45 s
    ├── celebration audio policy
    ├── APK v0.10.5-dev
    └── AAB v0.10.5-dev


## GECKO-030
professorButtonHost (géométrie fixe)
├── Button Prof (Z 0)
├── Prof.png (repos/fallback)
└── ChromaKeyVideoView dédié (premier plan)
    └── assets/prof/Prof_actions.mp4
        ├── t=0 → EOF ~30,070 s
        ├── chroma bleu → transparent
        └── audio embarqué muet

Déclencheurs
├── clic Prof
└── idle 2–3 s
    └── même si bulle ouverte

Pendant playback
├── PNG masqué après MediaPlayer prepared
├── vidéo locale au bouton
└── aucune mutation de layout/grille

Fin / erreur / pause / Anim OFF
└── stop vidéo → PNG restauré


Build GECKO-030
└── run #53 vert
    ├── vraie vidéo Prof restaurée
    ├── lecture complète dans le bouton
    ├── PNG fallback
    ├── APK v0.10.6-dev
    └── AAB v0.10.6-dev


## GECKO-031 — architecture prévue
MainActivity
└── bouton temporaire 🧪 Voix A/B
    └── VoiceBenchmarkDialog
        ├── VoiceBenchmarkEngine (contrat unique)
        │   ├── AndroidTtsBenchmarkEngine
        │   └── PiperVoiceBenchmarkEngine
        │       ├── LOW fr_FR-siwis-low
        │       └── MEDIUM fr_FR-siwis-medium
        └── métriques

PiperVoiceBenchmarkEngine
└── PiperSingleModelSlot<OfflineTts>
    ├── active = LOW ou MEDIUM ou null
    ├── switch : release(active)
    └── puis create(target)

CI
├── récupère les archives Piper officielles
├── extrait dans assets/tts/piper/{low,medium}
└── build APK/AAB contenant les deux modèles


### GECKO-031 — implémentation
row3 existante
├── Rejouer
├── Sauver
├── Journal
└── 🧪 Voix A/B
    └── AlertDialog
        ├── même phrase
        ├── AndroidTtsBenchmarkEngine
        ├── PiperVoiceBenchmarkEngine(LOW)
        └── PiperVoiceBenchmarkEngine(MEDIUM)

Piper partagé
└── single-thread executor
    └── PiperModelManager
        └── PiperSingleModelSlot<OfflineTts>
            ├── LOW
            └── MEDIUM
            switch = release ancien → create nouveau

Audio
├── Android → WAV cache + MediaPlayer
└── Piper → GeneratedAudio + AudioTrack PCM float

Lifecycle
├── dismiss/pause → stop + release Piper programmé
└── destroy → shutdown ressources benchmark


GECKO-031 dependency path
GitHub release v1.13.8
└── sherpa-onnx-1.13.8.aar
    ├── SHA-256 vérifié en CI
    └── app/libs/sherpa-onnx-1.13.8.aar
        └── Gradle implementation(files(...))

Aucun sherpa-onnx-jvm.jar dans l'APK Android.


Build GECKO-031
└── branche gecko-031-voice-ab-experiment
    └── run #58 SUCCESS
        ├── AAR Sherpa SHA vérifié
        ├── Piper LOW SHA vérifié
        ├── Piper MEDIUM SHA vérifié
        ├── tests
        ├── APK
        └── AAB
main
└── reste v0.10.6-dev stable pendant le test humain


## GECKO-032
ProfessorSpeech
├── PierrePiperSpeechEngine
│   ├── modèle unique fr_FR-upmc-medium
│   ├── sid = 1
│   └── OfflineTts singleton paresseux
└── AndroidProfessorSpeech fallback

Encouragement
├── Recorded → AssetAudioPlayer segment
└── Pierre → ProfessorSpeech phrase complémentaire

Nouvelle grille
├── recordStart
├── musique LEVEL_START
└── onCompletion
    └── PlayerStatsNarration
        └── ProfessorSpeech(Pierre)

CI
├── Sherpa AAR
└── UPMC Medium seulement


### GECKO-032 — implémentation
Prof Gecko texte
└── ProfessorSpeech
    ├── PierrePiperSpeechEngine
    │   └── UPMC Medium / sid 1
    └── AndroidProfessorSpeech fallback

Gecko confirmé
└── EncouragementSourcePolicy 50/50
    ├── RECORDED → master MP3 segment
    └── PIERRE → phrase TTS locale

Nouvelle grille
└── LEVEL_START MediaPlayer
    └── completion callback
        └── PlayerStatsNarration
            └── ProfessorSpeech/Pierre


Build GECKO-032
└── run #61 SUCCESS
    ├── Sherpa AAR SHA OK
    ├── UPMC Medium SHA OK
    ├── Pierre sid 1 test
    ├── encouragement source policy test
    ├── stats narration test
    ├── APK v0.10.8-dev
    └── AAB v0.10.8-dev


Main GECKO-032
└── run #63 SUCCESS
    ├── Pierre UPMC Medium sid 1
    ├── encouragements enregistrés + Pierre
    ├── stats après musique d'ouverture
    ├── APK v0.10.8-dev
    └── AAB v0.10.8-dev


## GECKO-033 — cible future (non codée)
Ouverture application
├── IntroGeckoGD.mp4
├── Gecko_Intro.mp4
└── jeu déjà prêt derrière

Titre haut écran
├── IconGeckoGD.png
└── texte GeckoDoku
    └── aucun impact sur taille de grille

ProfessorSpeech / Pierre sid 1
└── avant parole → ProfParle.mp4
    ├── déjà en cours ? réutiliser
    ├── terminé ? relancer t=0
    └── audio vidéo muet
        └── voix = Piper Pierre

Prof_actions.mp4
└── reste réservé aux actions générales du Prof


<!-- GECKO-033-ADDENDUM-FAB-2026-09-26 -->
## GECKO-033 — cible enrichie (non codée)
Identité
├── assets/gecko/IconGeckoGD.png
│   ├── launcher Android APK/AAB
│   └── médaillon rond à gauche du titre
│       ├── micro-animation locale
│       ├── jamais IntroGeckoGD.mp4
│       └── zéro reflow / grille immuable
│
Audio Gecko
├── vidéos Gecko = volume 0 systématique
└── musiques jeu/intro/victoire = flux séparés
│
Pierre / ProfessorSpeech sid=1
├── parole utile / stats / encouragement
│   └── ProfParle.mp4 juste avant audio
│       ├── déjà actif → réutiliser
│       ├── terminé → restart t=0
│       ├── aucune seconde instance
│       └── vidéo muette
├── Prof_actions.mp4 = général/idle, séparé
└── parole spontanée basse priorité
    ├── idle long → proposer bouton Prof
    ├── partie longue → proposer sauvegarde
    ├── check-in joueur
    └── banalités Gecko
        ├── catalogue 100
        ├── anti-répétition
        └── cooldown variable
│
Intro
└── IntroGeckoGD.mp4 → Gecko_Intro.mp4 → jeu déjà prêt


<!-- GECKO-033-PROFPARLE-SPEECH-LIFECYCLE-2026-09-26 -->
## GECKO-033 — ProfParle lifecycle
ProfessorSpeech / Sherpa Pierre
├── speechStart
│   └── ProfParle inactive → play t=0
├── speechContinues / phrase suivante sans silence
│   └── ProfParle active → reuse current playback
├── speechEnd / cancel / error
│   └── stop ProfParle immediately
│       └── restore Prof.png
└── next speech after stop
    └── play ProfParle from t=0


## GECKO-033 — RED TDD démarré sur branche isolée
Branche : `gecko-033-identity-prof-life`.
Le cycle RED ajoute uniquement les tests de contrat GECKO-033 et l’activation CI de cette branche. Les tests exigent les constantes assets, le mute Gecko, le cycle ProfParle ↔ parole réelle, l’identité du titre, le catalogue 100 phrases et les seuils du Prof ambiant avant toute implémentation.


GECKO-033 RED complémentaire : l’ordre `IntroGeckoGD.mp4` → `Gecko_Intro.mp4` est désormais verrouillé par test avant code.


## GECKO-033 — implémentation GREEN poussée, CI à confirmer
Le code GECKO-033 est branché sur `gecko-033-identity-prof-life` :
- callbacks réels début/fin de parole Pierre sur Piper/Sherpa et fallback Android ;
- `ProfParle.mp4` réservé à la parole, toujours muet, stoppé dès le silence et relancé depuis t=0 si Pierre reparle ; si une parole dépasse ~30 s, le clip repart tant que la voix est active ;
- `Prof_actions.mp4` reste l’animation action/idle et partage une seule vue vidéo avec arbitrage exclusif ;
- intro `IntroGeckoGD.mp4` → `Gecko_Intro.mp4`, skip interrompant la chaîne sans recréer la grille ;
- vidéos Gecko forcées muettes au niveau du player, indépendamment de FX ;
- `IconGeckoGD.png` affiché en médaillon rond superposé à gauche du titre et micro-animé par transform, sans participer au layout ;
- launcher Android adaptatif utilisant le blob exact de `IconGeckoGD.png` ;
- Prof ambiant : aide à 90 s d’inactivité, proposition de sauvegarde à 10 min ou 25 actions, catalogue de 100 banalités à 4–8 min avec anti-répétition ;
- version cible `0.10.9-dev`.

La mission reste en validation CI : aucune déclaration GREEN finale avant tests + APK + AAB.


<!-- GECKO-033-PHONE-FEEDBACK-INTRO-2026-09-26 -->
## GECKO-033 — retour téléphone intros
Intro chain
├── IntroGeckoGD.mp4
│   ├── visible : OUI
│   └── audio embarqué : DOIT ÊTRE AUDIBLE si audio actif
├── fin naturelle
│   └── Gecko_Intro.mp4 : DOIT SE LANCER
└── jeu déjà prêt derrière

Audio policy
├── intro 1 Gecko : exception → son conservé
├── animations Gecko gameplay : muettes
└── ProfParle.mp4 : muet

État : bugs constatés sur v0.10.9-dev ; correction gelée pendant poursuite des tests Fab.


<!-- GECKO-033-PHONE-FEEDBACK-ICON-PROF-ARBITRATION-2026-09-26 -->
## GECKO-033 — validation / conflit média
IconGeckoGD.png
├── launcher Android → VALIDÉ téléphone ✅
└── médaillon in-app gauche du titre → VALIDÉ téléphone ✅

Arbitrage média à auditer
├── INTRO
├── PROF_ACTION = Prof_actions.mp4
├── PROF_SPEECH = ProfParle.mp4
└── GECKO_ACTION
    └── symptôme : une transition semble interrompre une autre
        └── cause inconnue → tracer play/stop avant correction


<!-- GECKO-033-PHONE-FEEDBACK-GRID-GECKO-ANIM-2026-09-26 -->
## GECKO-033 — Gecko grille : image OUI, son NON
Animation Gecko gameplay
├── play vidéo : OUI
├── rendu sur bonne case : OUI
├── masque/cadrage/couleur validés : À PRÉSERVER
├── apparition : OUI
├── disparition : OUI
├── action longue : OUI
└── audio embarqué : TOUJOURS MUET

Important
├── mute ≠ stop
├── mute ≠ skip
├── mute ≠ hide
└── mute ≠ suppression du callback completion

IntroGeckoGD.mp4
└── exception : audio embarqué autorisé


<!-- GECKO-033-MEDIA-TRACE-PROF-AFTER-INTRO1-2026-09-26 -->
## GECKO-033 — MediaTrace
GeckoDokuMediaTrace
├── ChromaKeyVideoView
│   ├── PLAY_REQUEST / START
│   ├── STOP / COMPLETE
│   ├── SET_MUTED
│   └── ERROR / EXCEPTION / RELEASE
├── RichMediaOverlay
│   ├── PLAY_ACCEPT
│   ├── PLAY_REJECT_BUSY
│   └── FINISH_COMPLETE / FINISH_STOPPED
└── MainActivity
    ├── INTRO_STEP_REQUEST / ACCEPTED / REJECTED / FINISHED
    ├── GECKO_CELL_VIDEO_ACCEPTED / REJECTED / SKIPPED
    ├── PROF_ACTION_REQUEST
    ├── PROF_SPEECH_STATE
    └── PROF_SPEECH_VIDEO_REQUEST / PROF_VIDEO_STOP_ALL

Chronologie cible :
IntroGeckoGD visible + audible + Prof caché
→ fin propre audio intro 1
→ Gecko_Intro
→ Prof éligible après intro 1.


<!-- GECKO-033-PHONE-FEEDBACK-ANIM-OFF-UNEXPECTED-2026-09-26 -->
## GECKO-033 — état Anim inattendu
RichMediaSettings.enabled
├── démarrage
│   ├── préférence existante → restaurer choix utilisateur
│   └── aucune préférence → ON par défaut
├── bouton Anim
│   └── seul chemin normal de bascule ON/OFF
└── erreurs/conflits média
    └── NE DOIVENT PAS forcer OFF

Symptôme téléphone : Anim observé OFF sans origine connue → tracer lecture/écriture + lifecycle.
<!-- GECKO-033-AUDIT-PROF-SPEECH-GECKO-FLOW-2026-09-26 -->
## GECKO-033 — chronologie affinée

Action joueur (tap/double tap)
→ clearProfessorSession
→ closeProfessorBubble
→ professorSpeech.stop
→ mutation GameEngine
→ playCellAnimation
   ├── APPEARANCE
   │   └── EOF → maybePlayGeckoLongAction (45% / 45 s)
   └── DISAPPEARANCE
       └── fin, pas d'action longue

playCellAnimation
├── overlay busy → SKIP définitif, aucun retry
└── accepté → MediaPlayer prepareAsync → premières frames éventuellement plus tard

Étape Prof
→ showProfessorBubble
→ ProfessorSpeech.speak
→ apply step
→ GECKO_APPEARANCE sur richMediaOverlay
→ pas de stop voix dans playCellAnimation

Prof visuel
professorVideo unique
├── ACTION = Prof_actions
└── SPEECH = ProfParle muet

Pierre audio
→ VoicePcmPlayer
→ AudioTrack séparé

Donc : arrivée tardive animation Gecko ≠ remplacement différé ; rechercher d'abord stop explicite voix ou préemption par nouveau speak.
