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
- [x] couche AliveMascotOverlayView autonome, sans masque de couleur de fond ;
- [x] connecter la géométrie exacte PNG/vidéo de chaque mode ;
- [x] fallback PNG animations OFF prévu par profil.

### Intégration
- [x] Gecko : apparition / idle1..4 / disparition ;
- [x] continuité inter-clips via PNG transparent interne, sans masque de fond ;
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
- [x] continuité visuelle reprise dans le refactor autonome 0.15.9-dev ; rendu déclaré propre sur téléphone.

Voir `ordres-de-mission.md` pour le contrat complet.


### CI fondation
- [x] compilation Kotlin Debug/Phone atteinte en CI #242 ;
- [x] défaut du test de compteur identifié et corrigé ;
- [x] CI suivantes vertes jusqu'à #249.


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
- [x] CI #248 0.15.9-dev verte ;
- [x] APK téléphone 0.15.9-dev produit ;
- [x] rendu téléphone sans anciens carrés de fond jugé propre ;
- [ ] fréquence/sémantique des animations à reprendre via le grand cycle autonome ;
- [ ] revalider le drag Plante si modifié par la prochaine passe.


### Grand cycle autonome demandé par Fab
- [ ] update() choisit une nouvelle petite animation différente à chaque fin ;
- [ ] mémoriser les dernières séries complètes ;
- [ ] interdire la répétition immédiate d'une série complète ;
- [ ] varier longueur/ordre du cycle pour casser les motifs perceptibles ;
- [ ] fin de grand cycle : choisir une copine visible différente de la précédente pour une mignonnerie ;
- [ ] autres mascottes repartent sur des cycles désynchronisés ;
- [ ] éviter répétition de la même mignonnerie et de la même copine.

### Nuages placements donnés — 0.15.10-dev
- [x] rendu partagé Classic + Abeilles & Geckos ;
- [x] opacité fortement réduite ;
- [x] 7 bouffées irrégulières plutôt que 4 ovales géométriques ;
- [x] dérive douce et déterministe ;
- [x] test de politique visuelle ;
- [x] CI #249 verte ;
- [x] APK téléphone 0.15.10-dev produit ;
- [x] validation téléphone Fab : « Parfait » ; ne pas rouvrir sauf régression.


### Publication 0.15.10-dev
- [x] validation Fab ;
- [x] CI #250 : prerelease créée ;
- [x] CI #251 : release promue en publique ;
- [x] tag phone-0.15.10-dev-run-250 ;
- [x] APK public disponible ;
- [ ] grand cycle autonome des mascottes reste la prochaine tâche active.


## GECKO-051 — toutes les mascottes vivantes, tous modes
- [x] séparer présence vivante et lecteur vidéo ;
- [x] présence légère PNG interne pour chaque Gecko/Abeille visible ;
- [x] conserver seulement 3 lecteurs Gecko + 2 Abeille + 1 Plante ;
- [x] rotation automatique des lecteurs entre présences ;
- [x] synchroniser les Gecko Classic déjà présents ;
- [x] synchroniser les marqueurs Gecko Sudoku déjà présents ;
- [x] synchroniser toutes les pierres Gecko Gomoku déjà présentes ;
- [x] synchroniser Gecko + Abeilles du mode hexagonal déjà présents ;
- [x] retirer les présences devenues absentes sans fantôme ;
- [x] update() autonome et pauses légèrement désynchronisées ;
- [x] petites attentes différentes à chaque clip ;
- [x] mémoire des séries complètes ;
- [x] grand cycle → mignonnerie chez une copine compatible ;
- [x] animations OFF = toutes les présences restent PNG internes ;
- [ ] CI ;
- [ ] validation téléphone Fab sur les 4 modes ;
- [ ] release seulement après validation explicite.
