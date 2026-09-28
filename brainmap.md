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
