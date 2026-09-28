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
