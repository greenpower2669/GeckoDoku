# GECKODOKU — ORDRE DE MISSION ACTIF

## GECKO-045 — FINITION ABEILLES & GECKOS : ÉCHELLE + FENÊTRE DE VISUALISATION

Date : 2026-09-28  
Branche : `gecko-039-sudoku-tap-gecko-gomoku`

## ÉTAT VALIDÉ PAR FAB

La candidate téléphone 0.15.1-dev est globalement validée.

Considérer comme **VERT / ACQUIS** et ne pas régresser :

- logique générale Abeilles & Geckos ;
- plateau hexagonal ;
- zones colorées ;
- règles de zones / axes / voisinage ;
- solveur et génération ;
- keycolor VERT de l'animation Abeille ;
- fond vert supprimé ;
- ailes bleues conservées ;
- fonctionnement général du mode ;
- Prof, statistiques, étoiles, sauvegardes et export/import sauf régression révélée ultérieurement.

Le nouveau travail est une **passe de finition visuelle et de navigation**.

---

## 1 — ABEILLES DEUX FOIS PLUS PETITES

Les Abeilles sont actuellement trop grandes visuellement.

### Objectif

Réduire leur taille apparente d'environ **50 %**.

### Contraintes

- conserver le centre de l'Abeille sur sa case ;
- conserver le Gecko à sa taille actuelle ;
- ne pas changer la taille logique des hexagones ;
- ne pas réduire la zone tactile du plateau ;
- conserver une bonne lisibilité ;
- l'Abeille doit rester immédiatement identifiable ;
- le rendu doit être plus léger et plus esthétique.

La réduction concerne :

- le sprite statique `AbeilleTr.png` ;
- l'animation Abeille quand elle est affichée sur le plateau ;
- toute représentation Abeille dérivée utilisant la même géométrie de cible.

L'animation ne doit plus donner l'impression de recouvrir plusieurs hexagones.

---

## 2 — FENÊTRE CARRÉE DE VISUALISATION

La grille elle-même est validée.

Le défaut restant est la manière dont elle se déplace / se présente pendant la navigation.

### Principe recherché

Créer une **fenêtre carrée fixe de visualisation**.

Le joueur doit avoir l'impression de regarder le plateau à travers un carré stable :

- le carré reste fixe dans l'interface ;
- la grille se déplace **à l'intérieur** ;
- ce qui dépasse du carré est **clippé / masqué** ;
- le plateau ne doit jamais déborder visuellement sur le HUD, le Prof ou les boutons.

L'idée est celle d'une **fenêtre dans la fenêtre**.

---

## 3 — DEUX ARCHITECTURES ACCEPTABLES

Choisir l'implémentation la plus propre après audit du code.

### Option A — Viewport / clipping réel

Créer un conteneur carré fixe :

`SquareViewport`
→ clip du contenu
→ `BeeGeckoBoardView` transformable à l'intérieur.

Le plateau peut alors être :

- translaté ;
- zoomé ;
- recentré ;

sans jamais dessiner hors du carré.

### Option B — Recalcul de la zone visible

Garder la vue actuelle mais calculer explicitement la partie de grille visible à l'intérieur d'un carré logique fixe.

Le rendu et les interactions sont convertis dans ce viewport.

### Critère de choix

Préférer la solution :

- la plus simple ;
- la plus robuste ;
- la moins risquée pour les gestes ;
- la plus proche de la méthode éprouvée du Gomoku ;
- qui évite les coordonnées dupliquées ou les hacks de translation.

---

## 4 — DIMENSION ET POSITION DU CARRÉ

La fenêtre doit être **carrée**, centrée horizontalement et utiliser le maximum de place disponible sans recouvrir :

- le titre ;
- les informations ;
- le statut ;
- les boutons ;
- la zone Prof Gecko.

Sa taille doit être calculée à partir de l'espace réellement disponible.

Conceptuellement :

`side = min(availableWidth, availableHeight)`

Le plateau est ensuite rendu et manipulé uniquement dans ce carré.

---

## 5 — NAVIGATION INTERNE

Le comportement recherché est celui d'un viewport de carte.

### Drag

- un doigt déplace la grille à l'intérieur du carré ;
- aucun faux tap après un drag ;
- mouvement fluide ;
- le déplacement ne doit jamais déplacer le carré lui-même ;
- les bords du plateau doivent rester récupérables.

### Pinch / zoom

- deux doigts zooment la grille dans le carré ;
- le point visé reste stable autant que possible ;
- aucun conflit avec le drag ;
- ne jamais laisser le plateau disparaître entièrement ;
- le zoom minimum doit permettre une lecture globale utile.

### Recentrage

Le recentrage doit agir sur **le contenu interne**, pas sur le carré.

---

## 6 — CLIPPING ABSOLU

Règle importante :

**RIEN appartenant au plateau transformable ne doit être dessiné hors de la fenêtre carrée.**

Cela inclut :

- hexagones ;
- Geckos ;
- Abeilles ;
- croix ;
- repères ;
- surbrillances ;
- lignes du Prof ;
- projections ;
- animation Abeille ciblée sur une case.

Si une animation ou une aide sort du carré, elle doit être clipée proprement.

---

## 7 — HUD ET PROF TOUJOURS AU-DESSUS

La fenêtre de jeu et son contenu ne doivent jamais passer devant :

- le titre ;
- le texte d'information ;
- le statut ;
- la difficulté ;
- Nouvelle / Rejouer ;
- réglages ;
- Prof Gecko ;
- bulle du Prof.

Séparer clairement :

### Interface fixe
HUD / boutons / Prof

### Interface transformable
contenu du carré de visualisation

---

## 8 — AIDES DU PROF

Les aides du Prof doivent utiliser les mêmes transformations que le plateau.

Quand le joueur :

- zoome ;
- dézoome ;
- déplace la vue ;

les aides restent collées à leur case logique.

Dans le carré :

- bleu = analysé ;
- orange = hypothèse ;
- rouge = éliminé ;
- vert = certain.

Hors du carré :

- rien ne doit être visible.

---

## 9 — ANIMATION ABEILLE

Le keycolor VERT est validé et ne doit pas être modifié.

Nouvelle contrainte :

- réduire la cible visuelle de l'animation Abeille à environ **50 %** ;
- garder l'animation centrée sur la case Abeille ;
- la cible vidéo doit suivre la transformation du plateau ;
- elle doit respecter le clipping du carré ;
- les ailes bleues restent intactes.

---

## 10 — NON-RÉGRESSION

Ne pas modifier la logique métier de cette passe.

Ne pas changer :

- règles Abeilles & Geckos ;
- solveur ;
- générateur ;
- règle 1 Gecko + 1 Abeille par zone ;
- axes hexagonaux ;
- voisinage exclusif ;
- difficulté ;
- statistiques ;
- Hall of Fame ;
- étoiles ;
- sauvegardes ;
- export/import ;
- Gomoku ;
- Classic ;
- Sudoku.

Cette mission est **visuelle / ergonomique**, pas une refonte logique.

---

## 11 — TESTS OBLIGATOIRES

Ajouter / maintenir des tests pour :

- carré calculé avec `min(width,height)` de la zone disponible ;
- plateau centré dans le carré au démarrage ;
- drag ne déplace que le contenu ;
- pinch ne déplace pas le HUD ;
- coordonnées tap correctes après pan ;
- coordonnées tap correctes après zoom ;
- clip des aides Prof ;
- clip de l'animation Abeille ;
- recentrage interne ;
- taille Abeille ≈ 50 % de l'ancienne ;
- keycolor vert inchangé ;
- non-régression Classic / Sudoku / Gomoku.

---

## 12 — CRITÈRES DE VALIDATION TÉLÉPHONE

La mission est terminée seulement si Fab valide :

- [ ] Abeilles environ deux fois plus petites ;
- [ ] esthétique améliorée ;
- [ ] fenêtre de jeu clairement carrée ;
- [ ] carré fixe pendant le drag ;
- [ ] grille déplaçable à l'intérieur du carré ;
- [ ] grille correctement zoomable dans le carré ;
- [ ] aucun débordement sur les boutons / Prof / HUD ;
- [ ] animation Abeille clipée et centrée ;
- [ ] aides Prof parfaitement alignées ;
- [ ] navigation nettement plus agréable que la 0.15.1 ;
- [ ] aucun élément déjà validé n'a régressé.

---

## PRINCIPE DIRECTEUR

**LE CARRÉ EST LA FENÊTRE.  
LA GRILLE EST LE CONTENU.  
ON DÉPLACE LA GRILLE, PAS LA FENÊTRE.**

Les Abeilles deviennent deux fois plus petites afin d'alléger le rendu.

Tout le reste déjà validé reste au vert.
---

## GECKO-045 — implémentation lancée

Version cible : **0.15.2-dev** / versionCode **37**.

Choix d'architecture appliqué : **viewport carré interne réel dans `BeeGeckoBoardView`**.

- carré fixe centré avec marge interne discrète ;
- clipping du Canvas au carré avant tout rendu du plateau ;
- caméra recentrée et clampée dans ce carré, et non dans toute la View ;
- tap ignoré hors du carré ;
- zoom autour du point visé conservé ;
- contenu seulement transformable ;
- bordure carrée dessinée au-dessus du contenu ;
- Abeille statique ramenée à 50 % ;
- cible vidéo Abeille ramenée à 50 % et masquée si elle sortirait du carré ;
- keycolor VERT inchangé.

Ne pas déclarer la mission terminée avant CI + validation téléphone Fab.
### Ajustement caméra GECKO-045

Le scale initial doit utiliser le même `minimumScale()` que la borne de pinch afin d'éviter tout saut de zoom au premier geste. Cette règle fait partie du critère « navigation fluide ».
### Jalon GECKO-045

CI #222 GREEN sur l'implémentation initiale. CI #223 GREEN après alignement du scale initial avec le zoom minimum. Candidate 0.15.2-dev techniquement validée ; publication téléphone demandée.
### Publication GECKO-045

CI #224 GREEN. Prerelease téléphone publiée : `phone-0.15.2-dev-run-224`. APK direct : `GeckoDoku-v0.15.2-dev.apk`. Reste uniquement la validation téléphone Fab de la fenêtre carrée, du drag/pinch et de l'échelle Abeille.
