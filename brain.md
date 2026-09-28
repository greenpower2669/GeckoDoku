# GeckoDoku — brain

## État de référence au 2026-09-28

Branche active : `gecko-039-sudoku-tap-gecko-gomoku`.

Dernière candidate téléphone validée techniquement :
- 0.15.1-dev ;
- CI #221 GREEN ;
- keycolor Abeille VERT fonctionnel ;
- ailes bleues conservées.

## Éléments désormais considérés comme acquis

### Classic
- grille et ergonomie historiques conservées ;
- difficulté exacte générée par solveur ;
- statistiques / étoiles / Hall ;
- sauvegardes et export/import.

### Sudoku
- mode fonctionnel ;
- Prof et statistiques séparés ;
- pas de mélange avec les autres modes.

### Gomoku
- zoom / drag de référence pour les gestes ;
- difficulté IA réellement différenciée ;
- pédagogie / projection sur demande ;
- persona non agressive.

### Abeilles & Geckos
- conception Classic-like validée ;
- grille hexagonale ;
- zones colorées ;
- exactement 1 Gecko + 1 Abeille par zone ;
- trois familles d'axes logiques ;
- voisinage Abeille ↔ Gecko local et exclusif ;
- solveur / unicité / génération exacte ;
- Prof visuel ;
- keycolor vert de la vidéo Abeille ;
- fond vert supprimé.

## Mission active GECKO-045

Passe uniquement visuelle / ergonomique :

1. Abeilles à environ 50 % de leur taille actuelle.
2. Créer un viewport carré fixe pour Abeilles & Geckos.
3. Le plateau se déplace et zoome à l'intérieur de ce carré.
4. Tout dépassement du plateau / Prof / animation est clippé.
5. HUD, boutons et Prof restent fixes hors du viewport.
6. Ne toucher à aucune logique métier validée.

### Invariant UX

**Le carré est fixe ; le contenu bouge dedans.**

La méthode Gomoku reste la référence de comportement tactile.
## GECKO-045 — implémentation 0.15.2-dev

Architecture choisie : viewport carré **interne** à `BeeGeckoBoardView`, plutôt qu'un nouveau ViewGroup externe. Cette solution conserve les coordonnées existantes, limite le risque sur les gestes et permet un vrai `canvas.clipRect()` commun aux hexagones, pièces, croix, repères et aides Prof.

`BeeGeckoSquareViewportPolicy` calcule un carré centré à partir de la plus petite dimension disponible. `BeeGeckoViewportPolicy.centeredInViewport()` et `clampInViewport()` transforment la caméra dans ce repère carré.

`BeeGeckoVisualPolicy.BEE_SCALE = 0.50f` devient la source unique de la réduction Abeille. Le sprite statique et la cible vidéo l'utilisent tous deux.

Pour éviter qu'une vidéo Bee déborde du carré alors qu'elle vit dans `RichMediaOverlayView`, son target dynamique devient nul dès que le rectangle réduit n'est plus entièrement contenu dans le viewport : la vidéo est alors cachée au bord au lieu de déborder sur le HUD.
