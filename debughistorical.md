# GeckoDoku — debug historical condensé

## Jalons conservés

### 0.15
Abeilles & Geckos reconstruit selon l'esprit Classic :
zones + axes + Gecko + Abeille + voisinage local.

### 0.15.1
Keycolor vert corrigé ; ailes bleues conservées.

### 0.15.2 / GECKO-045
- Abeilles réduites à 50 % ;
- viewport carré ;
- navigation interne ;
- clipping ;
- CI #224 GREEN ;
- validation téléphone Fab : **parfait / all clear**.

GECKO-045 est clos fonctionnellement.

## GECKO-046 — nouvelle mission

Retour Fab :
- apprécie particulièrement les repères graphiques du Prof ;
- veut pouvoir utiliser des repères comparables lui-même via double clic ;
- demande repère Gecko vert, Abeille jaune légèrement excentrée et barres d'exclusion d'axe ;
- veut que le solveur / Prof exploite ces repères dans les explications ;
- veut trois états de croix : jaune / vert / rouge ;
- victoire GeckoBeeDoku avec musique du Classic + animation de tous les Geckos et Abeilles ;
- remplacer dans Classic et GeckoBeeDoku le cercle noir de départage par un nuage de fumée / brouillard transparent animé.

État : mission documentée avant code, conformément à la demande Fab.
## GECKO-046 — code préparé

Le système précédent Bee utilisait :
- un Set binaire de croix ;
- des CustomMarker génériques ;
- un anneau sombre autour des givens ;
- une célébration visuelle Bee sans démarrage explicite de la musique Classic.

La passe 0.15.3 remplace / étend ces points sans toucher aux règles du puzzle.

Décision importante :
- croix jaune = note personnelle, ne ferme pas une possibilité pour le solveur ;
- croix verte / rouge = exclusion logique effective.
Cela évite qu'une simple hypothèse du joueur modifie silencieusement le raisonnement du solveur.

Le brouillard est rendu procéduralement dans le Canvas afin d'éviter un nouvel asset lourd et de partager la métaphore entre Classic et GeckoBeeDoku.

État : code prêt à commit, CI pas encore exécutée.
### CI #225 GREEN

Run `36409465578` terminé avec succès. Étapes tests + build APK + upload artefact toutes GREEN. La publication prerelease était skipped car le commit fonctionnel ne portait pas encore `[phone-release]`.
