# GeckoDoku — brainmap

## Mission active GECKO-047

### Repères

Double tap
→ popup 3 choix

VERT
→ Gecko marker

JAUNE
→ Bee marker

ROUGE
→ popup axe
  → Classic : horizontal / vertical
  → GeckoBee : Q / R / S
→ création AxisBar

AxisBar
→ semi-transparent
→ traverse tout le plateau
→ draggable parallèlement à son axe
→ sortie complète du plateau = delete

Prof / Solver
→ pas de grosses croix
→ surbrillances + markers + AxisBar
→ texte synchronisé

Projection
→ zones/couleurs
→ positions candidates
→ axes communs
→ réservations
→ exclusions

### Audio

Application startup
→ Android version
→ AudioManager capture policy API 29+
→ log [AUDIO]

Manifest
→ allowAudioPlaybackCapture=true

Pierre
→ Sherpa/Piper path
→ AudioAttributes
  → USAGE_MEDIA
  → CONTENT_TYPE_SPEECH
  → ALLOW_CAPTURE_BY_ALL

Music / SFX / Video
→ usage MEDIA ou GAME
→ ALLOW_CAPTURE_BY_ALL

Disabled legacy sound
→ STOP / RELEASE
→ jamais simple volume=0

Diagnostics
→ [AUDIO] source=...
→ [AUDIO] STOP ...
→ [AUDIO] RELEASE ...

Validation
→ Samsung Multimedia capture
→ Pierre présent
→ anciens sons supprimés absents.
## GECKO-047 — architecture implémentée

### Axis guides Classic

`ClassicAxisGuideKind`
→ HORIZONTAL
→ VERTICAL

`GameEngine.axisGuides`
→ toggleAxisGuide
→ moveAxisGuide

`GeckoBoardView`
→ drawAxisGuides
→ hit-test bande
→ drag bande
→ outside = null/delete

`ClassicProfessorAxisGuidePolicy`
→ SolveStep
→ axes réellement réservés

### Axis guides Bee

`BeeGeckoLogicalMarks.excludedAxes`
→ désormais rendu pleine longueur

`BeeGeckoBoardView`
→ findAxisGuideAtScreen
→ onAxisGuideMoved
→ caméra non déplacée pendant drag bande

`BeeGeckoGameEngine.moveAxisMarker`
→ retire ancien axe
→ pose nouvel axe
→ null = suppression

### Audio

Manifest
→ allowAudioPlaybackCapture=true

MainActivity.onCreate
→ AudioCapturePolicy.applyApplicationPolicy()

Pierre/Piper
→ OfflineTts
→ PCM
→ VoicePcmPlayer
→ AudioTrack
→ MEDIA + SPEECH + ALLOW_ALL

Android fallback
→ TextToSpeech.setAudioAttributes(MEDIA/SPEECH/ALLOW_ALL)

Music
→ AssetAudioPlayer
→ MediaPlayer MEDIA/MUSIC/ALLOW_ALL

Video
→ ChromaKeyVideoView
→ MediaPlayer MEDIA/MOVIE/ALLOW_ALL
→ muted ?
   → volume 0
   → deselect audio tracks
→ unmuted
   → select first audio track

Logs
→ [AUDIO] source=...
## GECKO-047 — état CI

0.15.4-dev
→ commit fonctionnel 5ea050d
→ CI #227 GREEN
→ nettoyage légende Prof
→ commit phone-release
→ CI publication
→ test Samsung Fab.

Audio final :
Pierre PCM
→ AudioTrack MEDIA/SPEECH/ALLOW_ALL

Muted video
→ deselectTrack(audio)
→ volume 0 sécurité
→ STOP/RELEASE.

Repères :
double tap
→ 3 choix
→ axe si rouge
→ grande bande
→ drag
→ dehors = suppression.
### Livraison GECKO-047

commit 9b73f6f
→ CI #228 GREEN
→ prerelease phone-0.15.4-dev-run-228
→ APK direct
→ test réel Samsung / barres Fab.
