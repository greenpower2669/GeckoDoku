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
## GECKO-045 — implémentation

`BeeGeckoSquareViewportPolicy.bounds(viewW, viewH, inset)`
→ carré centré fixe

`BeeGeckoBoardView.onDraw()`
→ dessine fond de fenêtre
→ `clipRect(square)`
→ translate/scale caméra
→ grille + pièces + aides
→ restore
→ dessine bordure carrée

`BeeGeckoViewportPolicy`
→ `centeredInViewport()`
→ `clampInViewport()`
→ `fitScaleInViewport()`

`BeeGeckoVisualPolicy.BEE_SCALE = 0.5`
→ sprite Bee 50 %
→ target vidéo Bee 50 %

`beeGeckoOverlayTarget()`
→ rect case
→ réduction 50 %
→ vérification `viewport.contains(reducedRect)`
→ sinon null / vidéo masquée.
### Caméra

`minimumScale(viewport)` → source commune pour centrage initial et limite zoom-out. Pas de seuil différent entre ouverture et pinch.
