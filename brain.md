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
