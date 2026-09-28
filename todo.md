# GeckoDoku — TODO actif

## GECKO-045
- [x] viewport carré ;
- [x] navigation interne ;
- [x] Abeilles 50 % ;
- [x] keycolor vert ;
- [x] CI / APK ;
- [x] validation téléphone Fab.

## GECKO-046 — mission active

### Repères joueur
- [ ] auditer la palette double clic actuelle ;
- [ ] intégrer placement Gecko / Abeille + repères dans une palette commune ;
- [ ] ajouter repère Gecko vert ;
- [ ] ajouter repère Abeille jaune avec offset ;
- [ ] ajouter barres d'exclusion Q/R/S ;
- [ ] ajouter suppression / modification des repères.

### Croix
- [ ] définir structure cross state ;
- [ ] jaune = hypothèse ;
- [ ] vert = déduction confirmée ;
- [ ] rouge = impossible ;
- [ ] cycle ergonomique ;
- [ ] rendu accessible hors couleur.

### Prof / solveur
- [ ] faire produire les repères par le solveur ;
- [ ] rendre les mêmes structures côté Prof ;
- [ ] synchroniser texte + graphisme.

### Victoire
- [ ] identifier musique de victoire Classic ;
- [ ] réutiliser cette musique dans GeckoBeeDoku ;
- [ ] animer tous les Geckos ;
- [ ] animer toutes les Abeilles ;
- [ ] préserver performance / clipping.

### Brouillard
- [ ] identifier précisément le cercle noir de départage Classic ;
- [ ] identifier son équivalent GeckoBeeDoku ;
- [ ] remplacer par brouillard transparent animé ;
- [ ] partager le même composant / policy entre les deux modes.

### Persistance
- [ ] sauvegarder repères Gecko/Bee ;
- [ ] sauvegarder exclusions axes ;
- [ ] sauvegarder cross states ;
- [ ] export/import.

### Validation
- [ ] tests unitaires ;
- [ ] CI GREEN ;
- [ ] APK téléphone direct ;
- [ ] validation Fab.
## GECKO-046 — code 0.15.3 préparé

### Repères
- [x] cross state jaune / vert / rouge ;
- [x] repère Gecko vert ;
- [x] repère Abeille jaune excentré ;
- [x] barres Q/R/S ;
- [x] palette double clic enrichie ;
- [x] renderer partagé Prof / joueur.

### Solveur / Prof
- [x] mapping solve step → repères communs ;
- [x] exclusions Prof rendues avec les mêmes croix / barres.

### Persistance
- [x] session schema 3 ;
- [x] compat schema 2 ;
- [x] cross states persistés ;
- [x] logical markers persistés.

### Victoire
- [x] musique Classic dans GeckoBeeDoku ;
- [x] animation de toutes les pièces Bee ;
- [x] animation de tous les Geckos Classic.

### Brouillard
- [x] anneau donné Classic remplacé ;
- [x] anneau donné Bee remplacé ;
- [x] brouillard animé semi-transparent.

### Validation
- [x] tests unitaires marqueurs / axes / cycle croix / fog ;
- [ ] CI GREEN ;
- [ ] APK téléphone ;
- [ ] validation Fab.
### GECKO-046 — après CI #225
- [x] tests GREEN ;
- [x] build phone GREEN ;
- [x] code repères / croix / Prof / victoire / brouillard validé CI ;
- [ ] publier prerelease téléphone 0.15.3 ;
- [ ] Fab valide double clic / repères ;
- [ ] Fab valide croix 3 états ;
- [ ] Fab valide victoire vivante ;
- [ ] Fab valide brouillard Classic + GeckoBeeDoku.
