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
- [x] connecter la géométrie exacte PNG/vidéo de chaque mode ;
- [x] fallback PNG animations OFF prévu par profil.

### Intégration
- [x] Gecko : apparition / idle1..4 / disparition ;
- [x] éliminer le flash PNG prématuré par masque continu entre clips ;
- [x] Abeille : idle1..4 + fallback transitions si média de disparition absent ;
- [x] Plante : attente décorative bas droite + PlanteTr fallback ;
- [x] intégration Classic / Sudoku / Gomoku / Abeilles & Geckos via le même moteur ;
- [x] un retrait ancien ne peut pas interrompre la mascotte vivante plus récente du même type ;
- [x] tests unitaires sélection/anti-répétition + géométrie de taille ;
- [x] CI #246 intégration complète verte ;
- [x] APK téléphone 0.15.7-dev produit en artifact privé de test ;
- [x] test téléphone Fab : coupure visuelle intermittente entre deux vidéos reproduite conceptuellement ;
- [x] cause : nouveau lecteur transparent jusqu'à sa première frame alors que le PNG plateau reste masqué ;
- [x] correction : PNG transparent canonique utilisé uniquement comme pont entre deux clips déjà commencés ;
- [x] CI #247 0.15.8-dev verte ;
- [x] APK téléphone 0.15.8-dev produit ;
- [ ] validation téléphone Fab du pont sans coupure.

Voir `ordres-de-mission.md` pour le contrat complet.


### CI fondation
- [x] compilation Kotlin Debug/Phone atteinte en CI #242 ;
- [x] défaut du test de compteur identifié et corrigé ;
- [ ] nouvelle CI verte après correction.


### Correctif téléphone 0.15.9-dev
- [x] supprimer maskColorProvider de la nouvelle classe ;
- [x] supprimer le rectangle de couleur derrière Gecko/Abeille ;
- [x] AliveAnimator possède toujours son PNG transparent ;
- [x] masquer seulement le PNG statique du plateau pour les instances prises en charge ;
- [x] 3 Gecko vivants simultanés ;
- [x] 2 Abeilles vivantes simultanées ;
- [x] 1 Plante vivante ;
- [x] PNG pont jaune cohérent avec le Gecko Prof ;
- [x] Plante déplaçable par drag ;
- [x] position Plante mémorisée ;
- [ ] CI 0.15.9-dev ;
- [ ] validation téléphone Fab : aucun carré de fond + plateau suffisamment vivant.
