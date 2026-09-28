# GeckoDoku — brainmap

## GECKO-048

### Pierre
UI text
→ ProfessorSpeech
→ PierrePiperSpeechEngine
→ PierrePronunciationPolicy
→ « église » → « eglize »
→ Sherpa/Piper

Affichage jamais modifié.

### Classic axes
double tap
→ Axe
→ Horizontal / Vertical
→ Couleur
   → Jaune
   → Vert
   → Rouge
→ ClassicAxisGuide(kind,index,color)
→ rendu
→ drag conserve color

### GeckoBee axes
double tap
→ Axe
→ Q / S / R
→ Couleur
→ BeeGeckoLogicalMarks
   → excludedAxes
   → axisColors
→ rendu
→ drag conserve color
→ session schema 4

### Compatibilité Bee
schema 3 axes = string
→ color RED

schema 4 axes = object(axis,color)
→ couleur restaurée.
