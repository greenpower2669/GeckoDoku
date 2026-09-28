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


---

# GECKO-MEM-001 — DOCUMENTATION AVANT RESTRUCTURATION DES MÉMOIRES

Date : 2026-09-28

Demande de Fab :
- sauvegarder l'état actuel avant toute restructuration ;
- ne jamais supprimer `brain.md` ni `brainmap.md` ;
- créer d'abord une documentation détaillée décrivant le fonctionnement actuel du programme, l'usage des boutons, gestes, modes et fonctions ;
- utiliser cette documentation comme filet de compréhension avant de restructurer `brain.md` et `brainmap.md` ;
- `debughistorical.md` pourra ensuite être délesté franchement : conserver le récent/utile dans le fichier actif et déplacer l'ancien dans `sauvegarde.md`.

État :
- [x] snapshot froid créé dans `sauvegarde.md` ;
- [x] documentation fonctionnelle créée dans `docs/GECKODOKU-FONCTIONNEMENT.md` ;
- [ ] structure future de `brain.md` à discuter avec Fab ;
- [ ] structure future de `brainmap.md` à discuter avec Fab ;
- [ ] aucune restructuration des deux fichiers avant cette discussion.

Règle de sécurité :
`sauvegarde.md` est une archive froide. Ne pas la lire par défaut.
