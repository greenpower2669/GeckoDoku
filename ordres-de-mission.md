# GECKODOKU — ORDRES DE MISSION ACTIFS

## GECKO-049 — GÉOMÉTRIE RÉELLE DES AXES ABEILLES & GECKOS

Date : 2026-09-28
Branche : gecko-039-sudoku-tap-gecko-gomoku

### Constat téléphone de Fab
Dans Abeilles & Geckos, les grandes barres réelles suivent correctement la grille hexagonale, mais les repères affichés ne correspondent pas à leurs angles :
- l’interface annonçait S comme ↑↓ alors qu’aucun axe réel du plateau n’est vertical ;
- l’interface annonçait R comme ↗↙ alors que R est horizontal ;
- le même décalage apparaît dans le choix d’axe après double-clic ;
- les textes du Prof pouvaient reprendre ces symboles faux.

### Géométrie canonique pointy-top
Coordonnées écran : Y positif vers le bas.
- Q constant = +60° = ↖↘ ;
- R constant = 0° = ←→ ;
- S constant = -60° = ↙↗.

### Mission
Créer une source de vérité géométrique unique et l’utiliser pour :
- la projection des centres hexagonaux du plateau ;
- la légende visible en haut du mode ;
- la popup Axe du double-clic ;
- les libellés d’axes utilisés par le raisonnement du Prof.

Ne pas modifier :
- les règles Bee/Gecko ;
- le solveur ;
- la persistance schema 4 ;
- la couleur, le drag ou la suppression des barres validées précédemment.

### Critères téléphone
- [ ] légende : Q ↖↘, S ↙↗, R ←→ ;
- [ ] double-clic → Axe montre exactement les mêmes trois orientations ;
- [ ] la barre choisie suit l’orientation annoncée ;
- [ ] le Prof nomme le même axe que celui réellement projeté ;
- [ ] couleurs et drag GECKO-048 sans régression.

### Réalisation technique
- [x] BeeGeckoAxisGeometry devient la source canonique ;
- [x] BeeGeckoBoardView délègue sa projection des centres ;
- [x] MainActivity dérive légende + popup de cette géométrie ;
- [x] BeeGeckoRules.axisLabel dérive les textes Prof de cette géométrie ;
- [x] tests unitaires dédiés ajoutés ;
- [x] code : 8ccc0385c8314239976368811dab93808970e35a ;
- [ ] CI branche ;
- [ ] validation téléphone Fab.

---

## GECKO-048 — PRONONCIATION PIERRE + COULEURS DES BARRES D’AXES

Date : 2026-09-28
Branche : gecko-039-sudoku-tap-gecko-gomoku

### État validé avant mission
GECKO-047 est clos et validé par Fab :
- grandes barres d’axes ;
- drag / suppression hors plateau ;
- Prof utilisant les axes ;
- audio capturable ;
- Pierre présent dans la capture ;
- ancien routage audio corrigé.

Ne pas rouvrir GECKO-047 sauf régression.

### 1 — Prononciation Pierre : église → eglize
Le texte affiché reste église.
Juste avant Sherpa/Piper, Pierre reçoit eglize.

Contraintes :
- correction uniquement dans le flux vocal ;
- mot entier ;
- détection insensible à la casse ;
- aucune sous-chaîne accidentelle ;
- policy extensible.

### 2 — Couleurs des barres
Après le choix de l’axe : JAUNE / VERT / ROUGE.

Classic :
double clic → Axe → Horizontal/Vertical → couleur → pose.

Abeilles & Geckos :
double clic → Axe → Q/S/R → couleur → pose.

Sémantique :
- jaune = hypothèse / attention ;
- vert = logique positive / validée ;
- rouge = exclusion / impossibilité.

Conserver largeur, clipping, drag et suppression hors plateau.

### 3 — Drag / persistance
- type conservé ;
- couleur conservée ;
- seule position change ;
- hors plateau → suppression.

Bee :
- couleur persistée ;
- ancienne sauvegarde sans couleur → rouge ;
- nouvelle sauvegarde → couleur restaurée.

### 4 — Prof
Les couleurs joueur n’altèrent pas le rendu pédagogique du Prof.

### 5 — Critères téléphone
- [ ] Pierre prononce correctement église ;
- [ ] affichage reste église ;
- [ ] jaune / vert / rouge visibles ;
- [ ] couleur conservée pendant drag ;
- [ ] couleur sauvegardée/restaurée Bee ;
- [ ] audio toujours capturable ;
- [ ] axes toujours aussi agréables qu’en 0.15.4.

### Réalisation technique
- [x] code intégré ;
- [x] correctif compilation CI #229 ;
- [x] CI branche verte ;
- [x] artefact GeckoDoku-v0.15.5-dev-phone produit ;
- [ ] validation téléphone Fab.

---

## GECKO-MEM-001 — RESTRUCTURATION DES MÉMOIRES

Date : 2026-09-28

### Demande de Fab
- sauvegarder l’état courant avant restructuration ;
- ne jamais supprimer brain.md ni brainmap.md ;
- créer d’abord une documentation détaillée ;
- redistiller cette documentation dans brain.md et brainmap.md ;
- brain = fonctionnement/contrat compact ;
- brainmap = architecture + organigrammes ;
- conserver la documentation comme niveau intermédiaire ;
- délester franchement debughistorical ;
- ne pas charger l’archive froide par défaut.

### État
- [x] snapshot complet : sauvegarde.md ;
- [x] documentation : docs/GECKODOKU-FONCTIONNEMENT.md ;
- [x] brain.md restructuré ;
- [x] brainmap.md restructuré ;
- [x] debughistorical.md condensé ;
- [x] todo.md nettoyé ;
- [x] sauvegarde.md non modifié pendant la restructuration ;
- [ ] Fab relit/valide la nouvelle structure.

### Règle durable
documentation détaillée ↔ brain fonctionnel compact ↔ brainmap technique.

Si un niveau devient confus ou trop volumineux, il peut être reconstruit à partir des deux autres sans perdre l’archive froide ni mélanger les rôles.
