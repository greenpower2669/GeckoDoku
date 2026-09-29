# GeckoDoku — TODO actif

## GECKO-050 — sérénité / mascottes vivantes

### Préparation
- [ ] auditer tous les usages actuels Gecko/Abeille vidéo + PNG ;
- [ ] importer/ranger sur la branche active les nouveaux assets actuellement sur main ;
- [ ] normaliser les noms de fichiers sans modifier les contenus ;
- [ ] confirmer les médias manquants : apparition/disparition Abeille, PNG/apparition/disparition Plante.

### Architecture
- [ ] définir MascotAnimationProfile ;
- [ ] implémenter AliveAnimator / MascotAliveAnimator ;
- [ ] états HIDDEN / APPEARING / IDLE / CUTE / DISAPPEARING / STATIC_PNG ;
- [ ] mémoire anti-répétition ;
- [ ] update() après 4 attentes identiques ou fin d'animation longue ;
- [ ] géométrie unique PNG/vidéo ;
- [ ] fallback PNG animations OFF.

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
