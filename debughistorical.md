# GeckoDoku — debughistorical actif

> Historique court utile au diagnostic courant.
> L’état complet avant restructuration est dans sauvegarde.md, archive froide à ne pas lire par défaut.

## GECKO-047 — Pierre non capturable — CLOS / VALIDÉ

Symptôme :
Pierre pouvait être audible localement mais absent d’une capture configurée sur les sons multimédia.

Cause démontrée :
le PCM de Pierre utilisait un AudioTrack classé ACCESSIBILITY.

Correction :
- usage MEDIA ;
- contenu SPEECH ;
- capture autorisée ;
- manifeste compatible capture ;
- vidéos muted sans piste audio fantôme.

Preuve :
Fab a validé sur téléphone le 28/09/2026 l’audio capturable, Pierre dans la capture et les axes GECKO-047.

Ne rouvrir que sur régression.

## GECKO-048 — CI #229 — CLOS

Symptôme :
échec compilation Kotlin dans GeckoBoardView après ajout des couleurs d’axes.

Cause :
guideAtPointer demandait désormais color mais ACTION_MOVE utilisait encore l’ancienne signature.

Correction :
ACTION_MOVE et ACTION_UP propagent active.color.

Preuve :
les CI suivantes de la branche sont vertes.

## GECKO-048 — validation appareil encore ouverte

Ce n’est pas un bug confirmé.

À valider :
- prononciation réelle de église via eglize ;
- UI restant église ;
- rendu jaune/vert/rouge ;
- couleur conservée au drag ;
- restauration couleur Bee ;
- non-régression capture audio.

Le reste vit dans todo.md.
