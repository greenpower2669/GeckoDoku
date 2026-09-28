# GeckoDoku — brainmap

## État stable
Classic / Sudoku / Gomoku / GeckoBeeDoku
→ logique validée
→ stats / Hall / sauvegardes / export-import

## GECKO-046

### Repères
`LogicalMarker`
→ GeckoCandidate
→ BeeCandidate
→ AxisExclusion(Q/R/S)
→ Cross(HYPOTHESIS / CONFIRMED / IMPOSSIBLE)

### Input
double tap
→ palette logique
→ placement pièce OU repère

single tap / action croix
→ cycle 3 états

### Solveur / Prof
`BeeGeckoSolveStep`
→ mêmes marqueurs
→ texte
→ rendu synchronisé

### Rendu
Gecko marker
→ vert + symbole

Bee marker
→ jaune + symbole
→ léger offset

Axis exclusion
→ barre orientée Q/R/S

Cross
→ jaune / vert / rouge
→ style différent en plus de la couleur

### Victoire
completion GeckoBee
→ musique Classic
→ animation tous Geckos
→ animation toutes Abeilles
→ viewport clipping conservé

### Départage visuel
Classic + GeckoBee
→ ancien cercle noir supprimé
→ `Mist/FogOverlay`
→ transparent
→ animé
→ attaché à la zone / cellule logique

### Persistance
session + export/import
→ markers
→ axis exclusions
→ cross state
## GECKO-046 — architecture implémentée

`BeeGeckoCrossState`
→ HYPOTHESIS / CONFIRMED / IMPOSSIBLE
→ rendu jaune / vert / rouge
→ solverCrosses = CONFIRMED + IMPOSSIBLE

`BeeGeckoLogicalMarks`
→ GeckoCandidate
→ BeeCandidate
→ excludedAxes Set<HexAxis>

`double tap Bee`
→ palette pièce + repères + axes + croix

`BeeGeckoProfessorMarkerPolicy`
→ BeeGeckoSolveStep
→ logicalMarkers
→ crossStates
→ BeeGeckoBoardView partage le même renderer que le joueur

`BeeGeckoSessionStore schema 3`
→ crossStates
→ logicalMarkers
→ compat schema 2

Victoire :
`completeBeeGeckoGame()`
→ startCelebrationMusic()
→ BeeGeckoBoardView.startVictoryAnimation()
→ celebrationView

Classic :
`completeGame()`
→ GeckoBoardView.startVictoryAnimation()

Givens Classic + Bee :
→ fog animation partagée par BeeGeckoFogPolicy
→ plus d'anneau sombre.
