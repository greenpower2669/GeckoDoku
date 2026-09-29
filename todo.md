# GeckoDoku — TODO actif

## GECKO-050 — sérénité / mascottes vivantes

### Préparation
- [x] auditer les principaux usages Gecko/Abeille vidéo + PNG ;
- [x] importer/ranger sur la branche active les nouveaux assets actuellement sur main ;
- [x] normaliser les noms de fichiers sans modifier les contenus ;
- [x] PlanteTr.png confirmé et intégré ;
- [x] médias manquants confirmés : pas de disparition Abeille dédiée, pas d'apparition/disparition Plante dédiée ; fallback prévu.

### Architecture
- [x] définir MascotAnimationProfile ;
- [x] implémenter AliveAnimator + MascotLifeCoordinator ;
- [x] états HIDDEN / APPEARING / IDLE / CUTE / DISAPPEARING / STATIC_PNG ;
- [x] mémoire anti-répétition ;
- [x] réinterrogation après 4 attentes ou fin d'animation longue ;
- [x] couche AliveMascotOverlayView avec masque conservé entre clips ;
- [ ] connecter la géométrie exacte PNG/vidéo de chaque mode ;
- [x] fallback PNG animations OFF prévu par profil.

### Intégration
- [ ] Gecko : apparition / idle1..4 / disparition ;
- [ ] éliminer le flash PNG prématuré ;
- [ ] Abeille : idle1..4 + fallback transitions si médias manquants ;
- [ ] Plante : attente décorative bas droite ;
- [ ] intégration multi-mode sans duplication locale ;
- [ ] tests unitaires politiques de sélection/anti-répétition ;
- [ ] CI ;
- [ ] validation téléphone Fab.

Voir `ordres-de-mission.md` pour le contrat complet.


### CI fondation
- [x] compilation Kotlin Debug/Phone atteinte en CI #242 ;
- [x] défaut du test de compteur identifié et corrigé ;
- [ ] nouvelle CI verte après correction.
