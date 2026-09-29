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
- [x] chaque instance choisit une nouvelle petite animation différente à chaque fin ;
- [x] mémoriser les dernières séries complètes ;
- [x] interdire la répétition immédiate d'une série complète ;
- [x] varier longueur/ordre du cycle pour casser les motifs perceptibles ;
- [x] fin de grand cycle : choisir une copine visible différente de la précédente pour une mignonnerie ;
- [x] autres mascottes repartent sur des cycles désynchronisés ;
- [x] éviter répétition de la même copine ; variété mignonnerie limitée aux assets présents.

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
- [x] CI #252 entièrement verte ;
- [x] APK téléphone 0.15.11-dev produit ;
- [ ] validation téléphone Fab sur les 4 modes ;
- [ ] release seulement après validation explicite.


## GECKO-052 — diagnostic sans verrou
- [x] retirer le plafond runtime 3 Gecko / 2 Abeilles ;
- [x] créer un lecteur vidéo paresseux par Presence visible ;
- [x] permettre à toutes les mascottes visibles de s'animer simultanément ;
- [x] conserver les PNG internes et les grands cycles ;
- [x] intro FIRST/SECOND suspend totalement la couche mascottes ;
- [x] reprise des mascottes après fin/skip intro ;
- [x] CI #253 entièrement verte ;
- [x] APK téléphone 0.15.12-dev produit ;
- [ ] test téléphone : tous les Gecko visibles bougent-ils réellement ? ;
- [ ] test téléphone : Abeilles & Geckos tous animés ? ;
- [ ] test téléphone : intro réellement au-dessus de tout ? ;
- [ ] décider ensuite si le pool borné était la cause ;
- [ ] aucune release avant verdict Fab.


## GECKO-053 — retour Mail/Messages
- [x] confirmer que onPause() détruit les Presence via stopAll() ;
- [x] confirmer que onResume() ne restaurait que la Plante ;
- [x] conserver l'arrêt complet des lecteurs en arrière-plan ;
- [x] restaurer Plante + snapshot vivant du mode courant dans onResume() ;
- [x] correction commune Classic / Sudoku / Gomoku / Abeilles & Geckos ;
- [x] CI #254 verte ;
- [x] APK téléphone 0.15.13-dev produit ;
- [ ] test téléphone : ouvrir Mail/Messages puis revenir ;
- [ ] vérifier que toutes les mascottes repartent sans toucher au plateau ;
- [ ] vérifier que l'intro ne se relance pas au simple retour ;
- [ ] aucune release avant verdict Fab.


## GECKO-054 — zéro ordonnanceur
- [x] supprimer VideoSlot/pool partagé ;
- [x] un ChromaKeyVideoView par Presence ;
- [x] aucun plafond Gecko/Abeille/Plante ;
- [x] cycle autonome local par mascotte ;
- [x] target null au premier layout = retry local ;
- [x] refresh post-layout multi-mode ;
- [x] apparition sans PNG prématuré ;
- [x] lifecycle retour appli conservé ;
- [x] intro au-dessus conservée ;
- [x] Pierre visuel/parole laissé séparé ;
- [x] 300 RETURN dédiées ;
- [x] RETURN_AFTER_PAUSE = RETURN uniquement ;
- [x] cooldown 48 h + historique 48 IDs ;
- [x] anti-répétition famille + similarité ;
- [x] CI #255 0.15.14-dev entièrement verte ;
- [ ] validation téléphone sur les 4 modes ;
- [ ] validation pièces initiales Bee/Gecko ;
- [ ] validation apparition vide → vidéo → vivant ;
- [ ] validation retour Mail/Messages ;
- [ ] validation intros on top ;
- [ ] validation variété Pierre ;
- [ ] release seulement après validation explicite Fab.

- [x] APK 0.15.14-dev produit ; SHA-256 67cb5429d2ae188e8a5a58f09f43afa2032a96e43f1998f1265bafe96eed8abb ;


## GECKO-055 — PNG/vidéo détenus par la même Presence
- [x] conserver 1 Presence = 1 PNG + 1 vidéo + 1 AliveAnimator ;
- [x] conserver zéro ordonnanceur / zéro pool ;
- [x] sortir GLSurfaceView du même FrameLayout que l'ImageView ;
- [x] pngContainer et videoContainer deviennent siblings détenus par la même Presence ;
- [x] géométrie identique appliquée aux deux siblings ;
- [x] transfert plateau → Presence uniquement quand target valide ;
- [x] supprimer le setStaticSuppressed du cycle playDecision ;
- [x] entre clips : PNG interne, jamais l'ancien PNG du plateau ;
- [x] vraie apparition : PNG interne caché jusqu'à la vidéo ;
- [x] drag Plante déplace PNG + vidéo ensemble ;
- [x] CI #257 0.15.15-dev entièrement verte ;
- [ ] téléphone : vérifier qu'aucune grille n'est vide ;
- [ ] téléphone : Classic / Sudoku / Gomoku / Abeilles & Geckos ;
- [ ] téléphone : map initiale Bee/Gecko ;
- [ ] téléphone : apparition ;
- [ ] téléphone : retour Mail/Messages ;
- [ ] release seulement après validation Fab.

- [x] artifact Phone 0.15.15-dev produit ; SHA-256 APK 671776186328f53df577599e45d2cb85ea8bc6a8ca8daf5d54bf0721c71dabf5.


## GECKO-056 — Gecko + Abeilles figés malgré animations ON
- [x] auditer la chaîne commune Gecko/Abeille ;
- [x] isoler la course MEDIA_INFO_VIDEO_RENDERING_START / onPlayerStarted ;
- [x] latcher rendering-start pour la génération courante, quel que soit l'ordre des callbacks ;
- [x] conserver l'obligation playerStarted + renderingStarted avant révélation ;
- [x] protéger les générations anciennes et cancel() ;
- [x] ajouter FreshPlaybackFrameGateTest ;
- [x] version 0.15.16-dev / versionCode 51 ;
- [ ] CI verte ;
- [ ] APK Phone produit ;
- [ ] téléphone : Gecko Classic bougent ;
- [ ] téléphone : Gecko Sudoku bougent ;
- [ ] téléphone : Gecko Gomoku bougent ;
- [ ] téléphone : Gecko + Abeilles hexagonaux bougent ;
- [ ] téléphone : retour Mail/Messages conserve la reprise ;
- [ ] aucune release avant validation explicite Fab.
