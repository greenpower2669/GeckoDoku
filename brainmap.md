# GeckoDoku — brainmap

## Architecture validée

`MainActivity`
→ Classic
→ Sudoku
→ Gomoku
→ Abeilles & Geckos

### Abeilles & Geckos — logique
`BeeGeckoPuzzle`
→ zones
→ axes Q/R/S
→ Gecko
→ Abeille
→ voisinage local exclusif
→ solveur / générateur

### Abeilles & Geckos — rendu actuel
`BeeGeckoBoardView`
→ caméra / zoom / pan
→ hexagones
→ sprites
→ Prof overlays
→ targets média

`RichMediaOverlayView`
→ `ChromaKeyVideoView`
→ keycolor GREEN pour `BEE_APPEARANCE`

## GECKO-045 — architecture cible

`HUD FIXE`
- titre
- info
- statut
- boutons
- Prof / bulle

`SQUARE VIEWPORT FIXE`
→ clipRect / clipChildren
→ contenu transformable

`CONTENU TRANSFORMABLE`
- BeeGeckoBoardView
- hexagones
- Geckos
- Abeilles
- croix / repères
- aides Prof
- animation Bee

### Navigation

Touch
→ TAP si sous seuil
→ PAN si 1 doigt + déplacement
→ ZOOM si 2 doigts

Transformations
→ appliquées au contenu
→ jamais au viewport carré

### Taille Abeille

Gecko = échelle actuelle  
Abeille = environ 0,5 × échelle visuelle actuelle

Animation Bee
→ cible ≈ 0,5 × ancienne taille
→ centrée sur la même case
→ keycolor GREEN inchangé
→ clip au viewport
