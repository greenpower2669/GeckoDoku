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


## GECKO-049 — symboles d’axes Bee décalés — CORRECTION INTÉGRÉE

Symptôme confirmé par capture téléphone :
les barres réelles du plateau hexagonal ne suivent pas les orientations annoncées par la légende et la popup du double-clic.

Cause :
la projection pointy-top réelle est Q +60°, R 0°, S -60°, tandis que l’UI avait une table héritée Q ↖↘ / S ↑↓ / R ↗↙.

Correction :
BeeGeckoAxisGeometry devient la source unique pour la projection des centres, les symboles UI et les libellés du Prof :
Q ↖↘, R ←→, S ↙↗.

Commit code :
8ccc0385c8314239976368811dab93808970e35a

Règles, solveur, schema 4, couleurs et drag ne sont pas modifiés.

Preuve attendue :
tests + CI, puis validation téléphone Fab.
