# GECKODOKU — ORDRE DE MISSION ACTIF

## GECKO-048 — PRONONCIATION PIERRE + COULEURS DES BARRES D’AXES

Date : 2026-09-28  
Branche : `gecko-039-sudoku-tap-gecko-gomoku`

---

# ÉTAT VALIDÉ / AU VERT

GECKO-047 est **clos et validé par Fab**.

Validé sur téléphone :
- grandes barres d’axes ;
- drag / suppression hors plateau ;
- Prof utilisant les axes ;
- audio capturable ;
- Pierre présent dans la capture ;
- ancien routage audio corrigé.

Ne pas rouvrir GECKO-047 sauf régression.

---

# 1 — PRONONCIATION PIERRE : « ÉGLISE » → « EGLIZE »

Le texte affiché dans les bulles et l’interface doit rester correctement orthographié :

`église`

Mais juste avant synthèse par Pierre / Sherpa-Piper, transmettre :

`eglize`

Objectif :
obtenir une prononciation naturelle de « église ».

Règles :
- ne pas modifier le texte affiché ;
- correction uniquement dans le flux vocal Pierre ;
- respecter la casse d’entrée sans importance pour la détection ;
- remplacement sur mot entier ;
- ne pas remplacer les sous-chaînes accidentelles.

Exemple :

Affichage :
`Cette église est dans la zone verte.`

Pierre reçoit :
`Cette eglize est dans la zone verte.`

Prévoir une policy dédiée afin d’ajouter plus tard d’autres corrections de prononciation sans polluer le code TTS.

---

# 2 — COULEUR DES BARRES D’AXES

Les grandes barres sont validées.

Nouvelle option :
après le choix de l’axe, ouvrir une petite popup de couleur.

Choix :
- JAUNE ;
- VERT ;
- ROUGE.

Séquence :

Double clic
→ Axe
→ choix orientation
→ choix couleur
→ barre posée.

Cette règle s’applique :
- Classic ;
- GeckoBeeDoku.

---

# 3 — RENDU

Les couleurs doivent rester semi-transparentes.

### Jaune
Repère / hypothèse / attention.

### Vert
Repère validé / logique positive.

### Rouge
Exclusion / impossibilité.

Ne pas modifier la largeur, le drag, le clipping ou la suppression hors plateau déjà validés.

---

# 4 — DRAG ET COULEUR

Quand une barre est déplacée :
- elle conserve sa couleur ;
- son type d’axe reste identique ;
- seule sa position change.

Si elle sort du plateau :
- suppression identique à GECKO-047.

---

# 5 — PERSISTANCE GECKOBEEDOKU

Les barres GeckoBeeDoku sont sauvegardées.

Ajouter la couleur à leur persistance.

Compatibilité :
- anciennes sauvegardes sans couleur → ROUGE par défaut ;
- nouvelles sauvegardes → couleur restaurée.

---

# 6 — PROF

Le Prof conserve son propre rendu pédagogique déjà validé.

La popup de couleur concerne les barres posées par le joueur.

Ne pas transformer automatiquement les axes du Prof selon le choix personnel du joueur.

---

# 7 — TESTS

Tester :
- église → eglize pour Pierre ;
- texte original inchangé ;
- axe jaune ;
- axe vert ;
- axe rouge ;
- drag conserve couleur ;
- sauvegarde Bee conserve couleur ;
- ancienne sauvegarde Bee sans couleur → rouge ;
- non-régression audio GECKO-047 ;
- non-régression axes GECKO-047.

---

# CRITÈRE DE VALIDATION

- [ ] Pierre prononce correctement « église » ;
- [ ] affichage reste « église » ;
- [ ] popup couleur après choix axe ;
- [ ] jaune / vert / rouge visibles ;
- [ ] couleur conservée pendant drag ;
- [ ] couleur sauvegardée dans GeckoBeeDoku ;
- [ ] audio toujours capturable ;
- [ ] axes toujours aussi agréables qu’en 0.15.4.
---

## Correctif CI GECKO-048

CI #229 a échoué sur une erreur de compilation unique dans `GeckoBoardView` :
le drag ACTION_MOVE appelait `guideAtPointer()` sans transmettre la nouvelle couleur de barre.

Correction :
- transmettre `active.color` pendant le drag Classic ;
- conserver la couleur du guide pendant MOVE et UP.

Aucune logique de jeu modifiée.
