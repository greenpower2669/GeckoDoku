# GeckoDoku — sauvegarde froide

> **ARCHIVE / EXPIRÉ — NE PAS LIRE PAR DÉFAUT**
>
> Ce fichier est une sauvegarde de sécurité avant restructuration des mémoires vivantes.
> Il ne remplace ni les quatre mémoires actives ni `ordres-de-mission.md`.
> Ne l'ouvrir que sur demande de Fab, régression, indice historique précis ou besoin de restauration.

- Date de capture : 2026-09-28
- Branche : `gecko-039-sudoku-tap-gecko-gomoku`
- HEAD au moment de la capture : `81675c42a1f73108daf2c64e667ea3088f847608`
- Nature : copie intégrale, sans nettoyage ni réécriture, de l'état alors présent des mémoires et du registre de mission.
- Ordre de capture : `todo.md` → `brain.md` → `brainmap.md` → `debughistorical.md` → `ordres-de-mission.md`.



---

## SNAPSHOT — todo.md

```markdown
# GeckoDoku — TODO actif

## GECKO-047
- [x] axes validés Fab ;
- [x] audio validé Fab ;
- [x] mission close.

## GECKO-048

### Pierre
- [x] créer PierrePronunciationPolicy ;
- [x] église → eglize ;
- [x] appliquer uniquement avant Piper ;
- [x] préserver texte UI ;
- [x] tests prononciation.

### Axes Classic
- [x] AxisGuideColor jaune / vert / rouge ;
- [x] ClassicAxisGuide porte la couleur ;
- [x] rendu couleur semi-transparente ;
- [x] drag conserve couleur ;
- [x] remplacement couleur sur même axe.

### Axes GeckoBee
- [x] axisColors ajouté aux LogicalMarks ;
- [x] rendu jaune / vert / rouge ;
- [x] drag conserve couleur ;
- [x] popup couleur après axe ;
- [x] persistance schema 4 ;
- [x] compat anciennes sauvegardes → rouge.

### Livraison
- [ ] commit code + 5 fichiers vivants ;
- [ ] CI GREEN ;
- [ ] APK téléphone 0.15.5-dev ;
- [ ] validation Fab prononciation + couleurs.
### GECKO-048 — correctif CI
- [x] identifier échec CI #229 ;
- [x] corriger argument couleur manquant pendant drag Classic ;
- [ ] CI suivante GREEN ;
- [ ] APK téléphone 0.15.5-dev ;
- [ ] validation Fab prononciation « église » et couleurs d'axes.

```

---

## SNAPSHOT — brain.md

```markdown
# GeckoDoku — brain

## État stable

Branche :
`gecko-039-sudoku-tap-gecko-gomoku`

### GECKO-047 — CLOS / VALIDÉ FAB
Validé :
- axes globaux ;
- drag axes ;
- suppression hors plateau ;
- audio Android capturable ;
- Pierre capturable ;
- policy MEDIA/SPEECH ;
- vidéos muted sans piste fantôme.

Ne pas rouvrir sans régression.

## Mission active GECKO-048

### Prononciation Pierre
Créer une couche dédiée juste avant Piper :
`PierrePronunciationPolicy.forSpeech(text)`

Premier correctif :
- mot entier « église » → « eglize ».

Le texte UI reste inchangé.

### Couleur axes
Créer / utiliser :
`AxisGuideColor.YELLOW`
`AxisGuideColor.GREEN`
`AxisGuideColor.RED`

Classic :
`ClassicAxisGuide(kind,index,color)`

GeckoBee :
`BeeGeckoLogicalMarks.axisColors`

Flux UI :
axe → popup couleur → pose.

Drag conserve la couleur.

Bee session :
schema 4 ;
anciennes données axes string → RED ;
nouvelles données axes objet {axis,color}.
## GECKO-048 — correctif compilation

CI #229 : échec Kotlin localisé à `GeckoBoardView.kt`.
Cause : signature `guideAtPointer(kind,x,y,color)` mise à jour, mais l'appel ACTION_MOVE utilisait encore l'ancienne signature.

Correctif : ACTION_MOVE transmet `active.color`.
La couleur reste donc stable pendant tout le drag.

```

---

## SNAPSHOT — brainmap.md

```markdown
# GeckoDoku — brainmap

## GECKO-048

### Pierre
UI text
→ ProfessorSpeech
→ PierrePiperSpeechEngine
→ PierrePronunciationPolicy
→ « église » → « eglize »
→ Sherpa/Piper

Affichage jamais modifié.

### Classic axes
double tap
→ Axe
→ Horizontal / Vertical
→ Couleur
   → Jaune
   → Vert
   → Rouge
→ ClassicAxisGuide(kind,index,color)
→ rendu
→ drag conserve color

### GeckoBee axes
double tap
→ Axe
→ Q / S / R
→ Couleur
→ BeeGeckoLogicalMarks
   → excludedAxes
   → axisColors
→ rendu
→ drag conserve color
→ session schema 4

### Compatibilité Bee
schema 3 axes = string
→ color RED

schema 4 axes = object(axis,color)
→ couleur restaurée.
### Correctif CI #229

Classic Axis drag
→ ACTION_DOWN : sélection guide
→ ACTION_MOVE : guideAtPointer(..., active.color)
→ ACTION_UP : guideAtPointer(..., active.color)
→ couleur conservée.

```

---

## SNAPSHOT — debughistorical.md

```markdown
# GeckoDoku — debug historical condensé

## GECKO-047
Validé par Fab le 2026-09-28 :
- axes parfaits ;
- audio parfait ;
- mission close.

Cause audio historique :
Pierre passait par AudioTrack ACCESSIBILITY.
Corrigé en MEDIA + SPEECH + ALLOW_CAPTURE_BY_ALL.
Les vidéos muted désélectionnent leurs pistes audio.

## GECKO-048

Retour Fab :
- conserver totalement les axes actuels ;
- ajouter choix de couleur jaune / vert / rouge après choix axe ;
- améliorer Pierre : « église » doit être envoyé au moteur comme « eglize ».

Choix d’architecture :
- correction prononciation uniquement au bord Piper ;
- UI non modifiée ;
- enum AxisGuideColor partagé ;
- ClassicAxisGuide porte sa couleur ;
- GeckoBee conserve excludedAxes et ajoute axisColors pour compatibilité ;
- Bee session passe schema 4 avec lecture schema 2/3 conservée.

Version cible :
0.15.5-dev / versionCode 40.
## GECKO-048 — CI #229

Échec de compilation, pas un défaut fonctionnel :
`GeckoBoardView.kt:309` → argument `color` manquant.

Origine :
migration de `ClassicAxisGuide` vers un guide coloré, un appel ACTION_MOVE resté sur l'ancienne signature.

Correction appliquée :
`active.color` transmis dans ACTION_MOVE.

```

---

## SNAPSHOT — ordres-de-mission.md

```markdown
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

```
