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
- [x] CI #260 verte ;
- [x] APK Phone 0.15.16-dev produit ;
- [x] téléphone : Gecko Classic bougent — VALIDÉ Fab ;
- [ ] téléphone : Gecko Sudoku bougent ;
- [ ] téléphone : Gecko Gomoku bougent ;
- [ ] téléphone : Gecko + Abeilles hexagonaux bougent ;
- [ ] téléphone : retour Mail/Messages conserve la reprise ;
- [ ] aucune release avant validation explicite Fab.

- [x] artifact CI #260 : GeckoDoku-v0.15.16-dev-phone ; SHA-256 archive 4fde649f9492e89ba01e12e62a4094e454e229008c930c35837b200c94308894.


## GECKO-057 — mascottes créées mais cycles jamais lancés
- [x] analyser le log téléphone 0.15.16-dev ;
- [x] constater les créations ChromaKey des Presence mais zéro PLAY_REQUEST / ALIVE_PLAY Gecko-Abeille ;
- [x] identifier la famine locale : refreshDynamicTargets reprogrammait le callback avant son exécution ;
- [x] rendre refreshPresenceTarget idempotent sur les LayoutParams ;
- [x] ne plus repousser un callback de Presence déjà armé ;
- [x] conserver un callback local par Presence, zéro ordonnanceur/pool partagé ;
- [x] tracer ALIVE_PLAY pour le prochain test téléphone ;
- [x] faire entrer aussi le dernier Gecko de fin de partie dans la couche vivante ;
- [x] version 0.15.17-dev / versionCode 52 ;
- [x] CI #263 verte ;
- [x] APK Phone 0.15.17-dev produit ;
- [ ] téléphone : voir ALIVE_PLAY pour Gecko ;
- [ ] téléphone : voir ALIVE_PLAY pour Abeille ;
- [ ] téléphone : confirmer les stay1..4 visibles ;
- [ ] aucune release avant validation explicite Fab.

- [x] artifact CI #263 : GeckoDoku-v0.15.17-dev-phone ; APK SHA-256 cb50cb5e97ed9cc33ab7285982a6da7fda5e6439040fa2a195b7c59bb6fa8577.


## GECKO-058 — Abeilles & Geckos : seules les Abeilles s'animent en bordure
- [x] confirmer par log : BEE produit ALIVE_PLAY / VIDEO_VISIBLE ;
- [x] confirmer l'absence de ALIVE_PLAY pour les Gecko du plateau hexagonal ;
- [x] corréler avec la capture : les 3 Gecko visibles sont placés sur des cellules de bord ;
- [x] isoler la cause dans beeGeckoAliveTarget() : rejet total si le rect animé dépasse même légèrement viewportRectOnScreen() ;
- [ ] remplacer le rejet par une cible visible/clippée ou recentrée dans le viewport, sans casser zoom/drag ;
- [ ] tester Gecko bord gauche, bord droit et bord bas ;
- [ ] vérifier Abeille intérieure inchangée ;
- [ ] vérifier Gecko/Abeille après zoom et drag ;
- [ ] aucune release avant validation Fab.


## GECKO-058 — correctif appliqué
- [x] remplacer le rejet total des cibles de bord par un maintien dans le viewport ;
- [x] conserver la taille/aspect de la mascotte ;
- [x] recentrer uniquement du minimum nécessaire quand la cible dépasse ;
- [x] continuer à retourner null si la mascotte est totalement hors écran ;
- [x] ajouter la trace BEE_GECKO_ALIVE_TARGET_CLAMPED ;
- [x] version 0.15.18-dev / versionCode 53 ;
- [x] CI #265 verte ;
- [x] APK Phone 0.15.18-dev produit ;
- [ ] téléphone : Gecko bord gauche animé ;
- [ ] téléphone : Gecko bord droit animé ;
- [ ] téléphone : Gecko bord bas animé ;
- [ ] téléphone : Abeille intérieure inchangée ;
- [ ] téléphone : zoom/drag conservent les animations ;
- [ ] aucune release avant validation explicite Fab.

- [x] artifact CI #265 : GeckoDoku-v0.15.18-dev-phone ; APK SHA-256 9e0ad087bc2f641ecd80010bb162cf57739ac0b0a236d2dc21cbe5d321311b77.


## GECKO-059 — retirer la contrainte viewport non demandée
- [x] supprimer le rejet des cadres Gecko/Abeille hors viewport ;
- [x] supprimer le clamp/recentrage ajouté en GECKO-058 ;
- [x] utiliser directement la cible centrée sur la cellule ;
- [x] version 0.15.19-dev / versionCode 54 ;
- [x] CI #267 verte ;
- [x] APK Phone 0.15.19-dev produit ;
- [ ] téléphone : Gecko de bord animés sans décalage ;
- [ ] Abeilles inchangées ;
- [ ] zoom/drag cohérents ;
- [ ] aucune release avant validation Fab.

- [x] APK SHA-256 : 97e3876d853dd1701f3e7f552fe38d289bf4d5344278416a1e409d7817d6ebcf.


## GECKO-060 — ownership visuel unique Bee/Gecko
- [x] Alive Presence possède PNG fallback + vidéos ;
- [x] BeeGeckoBoardView ne rend plus une seconde mascotte legacy ;
- [x] conserver l'ownership Alive quand la target sort temporairement ;
- [x] restaurer la règle hors-écran sur la cellule du plateau, pas sur le cadre média ;
- [x] version 0.15.20-dev / versionCode 55 ;
- [ ] CI verte ;
- [ ] APK Phone produit ;
- [ ] téléphone : toutes les Abeilles et tous les Gecko passent par leur Presence ;
- [ ] drag : une cellule sortie du viewport cache sa mascotte ;
- [ ] drag retour : la Presence reprend sans PNG legacy ;
- [ ] aucune release avant validation Fab.


## GECKO-061 — règle hors-écran sur le centre de cellule
- [x] conserver l'ownership unique Alive (PNG fallback + vidéo) ;
- [x] ne pas tester les bords du cadre média transparent/key-color ;
- [x] masquer la Presence dès que le centre logique de sa cellule sort du viewport ;
- [x] version 0.15.21-dev / versionCode 56 ;
- [ ] CI verte ;
- [ ] APK Phone produit ;
- [ ] drag haut : le Gecko hors plateau disparaît ;
- [ ] drag gauche/droite/bas : même comportement ;
- [ ] cellule visible : PNG/vidéo restent centrés naturellement ;
- [ ] aucune release avant validation Fab.


### GECKO-061 build
- [x] CI #275 verte ;
- [x] APK Phone 0.15.21-dev produit ;
- [x] SHA-256 APK : 1f65452b0e8b98670eaf27ba9e321e997dfad13c2bfdbf36b2fd17523877da19.


## GECKO-062 — Gomoku : 3 mascottes vivantes par équipe
- [x] appliquer au Gomoku contre Prof et au Gomoku JcJ ;
- [x] conserver tous les autres pions Gecko en PNG statique ;
- [x] sélectionner aléatoirement au maximum 3 Gecko verts et 3 jaunes pour Alive ;
- [x] garder la sélection stable pendant un grand cycle ;
- [x] redistribuer les 3+3 à la fin d'un grand cycle Alive ;
- [x] garantir un changement de sélection quand une alternative existe ;
- [x] tests unitaires de la politique 3 par équipe ;
- [x] version 0.15.22-dev / versionCode 57 ;
- [ ] CI verte ;
- [ ] APK Phone produit ;
- [ ] téléphone : jamais plus de 3 Gecko animés par équipe ;
- [ ] téléphone : redistribution visible en fin de cycle ;
- [ ] aucune release avant validation Fab.
