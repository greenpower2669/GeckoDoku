# GeckoDoku — brain

## État validé au 2026-09-28

Branche : `gecko-039-sudoku-tap-gecko-gomoku`

### GECKO-045
Validé par Fab :
- viewport carré Bee ;
- navigation ;
- zoom / drag ;
- Bee scale 50 % ;
- keycolor vert ;
- plateau et logique.

Considérer GECKO-045 comme acquis.

## Mission active — GECKO-046

Axes :

1. Palette logique au double clic.
2. Repère Gecko vert.
3. Repère Abeille jaune, légèrement excentré.
4. Barres d'exclusion orientées sur les 3 axes hex.
5. Croix personnelles à 3 états :
   - jaune = hypothèse ;
   - vert = déduction sûre ;
   - rouge = impossible.
6. Prof / solveur utilisent ces mêmes structures graphiques.
7. Victoire GeckoBeeDoku :
   - musique de victoire Classic ;
   - tous les Geckos animés ;
   - toutes les Abeilles animées.
8. Classic + GeckoBeeDoku :
   - remplacer le cercle noir de départage par un brouillard transparent animé.
9. Persister / exporter tous les nouveaux repères.

### Invariant

Un seul langage graphique partagé entre joueur, solveur et Prof.

Ne pas refaire la logique du puzzle.
## GECKO-046 — implémentation 0.15.3-dev

Nouveau langage logique Bee :

- `BeeGeckoCrossState` :
  - HYPOTHESIS = jaune ;
  - CONFIRMED = vert ;
  - IMPOSSIBLE = rouge.
- `BeeGeckoLogicalMarks` :
  - geckoCandidate ;
  - beeCandidate ;
  - excludedAxes Q/R/S.
- `BeeGeckoProfessorMarkerPolicy` transforme un `BeeGeckoSolveStep` en ces mêmes repères.

Le moteur conserve `manualCrosses` comme vue logique dérivée : jaune n'exclut pas le solveur ; vert/rouge sont de vraies exclusions.

Persistance :
- Bee session schema 3 ;
- lecture de schema 2 conservée ;
- crossStates + logicalMarkers exportés dans les mêmes SharedPreferences, donc l'export/import global reste compatible.

Victoire :
- GeckoBeeDoku appelle la musique `AssetAudioCatalog.CELEBRATION` via la même méthode que Classic ;
- `BeeGeckoBoardView.startVictoryAnimation()` pulse toutes les pièces ;
- Classic reçoit aussi `GeckoBoardView.startVictoryAnimation()`.

Départage :
- ancien anneau autour des givens remplacé par un brouillard animé semi-transparent dans Classic et GeckoBeeDoku.
## GECKO-046 — CI #225 GREEN

La 0.15.3-dev passe la CI complète. Les changements sont maintenant techniquement validés :
- palette logique ;
- repères Gecko/Bee ;
- barres axes ;
- croix 3 états ;
- Prof utilisant le même langage visuel ;
- persistance schema 3 ;
- musique Classic + animation globale à la victoire ;
- brouillard à la place de l'anneau donné.

Prochaine porte : validation téléphone Fab.
