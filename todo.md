# TODO — GeckoDoku

## PRIORITÉ — finir le passage 100 % sprites préfabriqués

- [x] Brancher la release `PackageSprites` dans la CI et extraire seulement 60p/120p/240p.
- [x] Masquer l’option « Banques sprites / export » du menu utilisateur.
- [ ] Étendre le lot préfabriqué aux 9 MP4 encore non couverts : 2 intros Gecko, 5 Plante, 2 Prof.
- [ ] Vérifier sur téléphone que ces 9 médias jouent depuis les banques sans fallback vidéo.
- [ ] Supprimer alors tous les `.mp4` du paquet et retirer définitivement le code de fabrication/export devenu mort.

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
- [x] CI #286 verte ;
- [x] APK Phone produit ;
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

### GECKO-062 build
- [x] CI #280 verte ;
- [x] APK Phone 0.15.22-dev produit ;
- [x] SHA-256 APK : 70b8cb93a180b2e0612bd445d035650537b52ae8640d46f503293ad5a3bca891.


## GECKO-063 — passe cumulée Gomoku / intro / surface / plante
- [x] supprimer le contournement Gomoku : GECKO_APPEARANCE ne crée plus directement une Presence ;
- [x] le sélecteur 3 PLAYER + 3 PROFESSOR est l'unique autorité de création des Presence Gomoku ;
- [x] les actions longues Gomoku sont routées vers requestCute() d'une Presence déjà sélectionnée ;
- [x] trace GOMOKU_LIVING_SELECTION avec compte par camp ;
- [x] ajouter un rideau noir topmost avant la première intro, présent avant setContentView ;
- [x] retirer le rideau seulement quand l'overlay intro a pris le relais ou si l'intro est indisponible ;
- [x] empêcher ChromaRenderer d'échantillonner la texture externe tant qu'aucune frame fraîche du playback courant n'a été consommée ;
- [x] conserver un clear alpha=0 avant la première frame pour éliminer le carré noir ;
- [x] réduire uniquement le PNG fallback de la plante à 95 %, sans modifier sa vidéo ni son centre ;
- [x] version 0.15.23-dev / versionCode 58 ;
- [ ] CI verte ;
- [ ] APK Phone produit ;
- [ ] téléphone Gomoku : jamais plus de 3 verts + 3 jaunes vivants ;
- [ ] téléphone Gomoku : redistribution au grand cycle sans création parasite ;
- [ ] lancement : aucun plateau visible avant l'intro ;
- [ ] aucun carré noir avant la première frame Gecko/Abeille/Plante/Prof ;
- [ ] plante : transition PNG/vidéo alignée à ~95 % ;
- [ ] aucune release ni merge main avant validation Fab.

## Validation téléphone 0.15.23-dev — 29/09/2026 23:21
- [x] intro : plus de plateau visible 1–2 s avant la vidéo ;
- [x] régression carré noir avant première frame : corrigée sur les modes validés ;
- [x] plante : PNG réduit de ~5 %, transition validée ;
- [ ] Gomoku : reste à corriger, ne pas considérer GECKO-062/063 validés ;
- [ ] Abeilles & Geckos : géométrie de visibilité encore incorrecte ; un Gecko du haut déborde du plateau et un Gecko du bas peut disparaître alors que sa cellule est encore partiellement visible ;
- [ ] prochaine stratégie géométrique à valider avec Fab avant code : conserver target centré sur cellule + existence si cellule intersecte le viewport + clipping visuel strict au rectangle du plateau, au lieu du seuil sur le centre de cellule.


## GECKO-064 — clipping strict Bee/Gecko aux limites du plateau
- [x] abandonner le seuil basé sur le centre de cellule ;
- [x] conserver la Presence tant que la cellule logique intersecte le viewport ;
- [x] ne jamais recentrer/décaler la mascotte ;
- [x] ajouter un clipProvider optionnel aux Presence Alive ;
- [x] appliquer le même clip local au conteneur PNG et au conteneur vidéo ;
- [x] clip = intersection entre target rendu et viewport réel du plateau ;
- [x] supprimer le handoff legacy setMediaPieceSuppressed sur disparition Bee/Gecko ;
- [x] version 0.15.24-dev / versionCode 59 ;
- [ ] CI verte ;
- [ ] APK Phone produit ;
- [ ] téléphone : Gecko haut coupé proprement à la bordure ;
- [ ] téléphone : Gecko bas encore partiellement visible tant que sa cellule touche le plateau ;
- [ ] zoom/drag : glissement progressif derrière les quatre bords ;
- [ ] aucune release avant validation Fab.

### GECKO-064 build
- [x] CI #289 verte ;
- [x] APK Phone 0.15.24-dev produit ;
- [x] SHA-256 APK : 0792b38bc21fc8dc9f705220f2d3a11dd16a8c91ebd39a44fcacdfc2dd059e67.


## GECKO-065 — mascottes toujours vivantes derrière le cadre du plateau
- [x] séparer définitivement existence de la Presence et visibilité dans la fenêtre du plateau ;
- [x] Abeilles & Geckos : target géométrique non bornée, même quand la cellule est entièrement derrière le cadre ;
- [x] Abeilles & Geckos : clipProvider reste l'unique fenêtre visuelle du plateau ;
- [x] Gomoku : même stratégie target non bornée + clipProvider ;
- [x] préserver le cap Gomoku 3 PLAYER + 3 PROFESSOR et sa redistribution de grand cycle ;
- [x] conserver le ChromaKeyVideoView/SurfaceView historique ; ne pas intercaler une politique Surface globale fragile ;
- [x] le cadre blanc, titres, textes, boutons et Prof restent hors de la fenêtre où les mascottes peuvent être visibles ;
- [x] aucune destruction/recréation d'une Presence uniquement parce qu'elle passe derrière un bord pendant zoom/drag ;
- [x] version 0.15.26-dev / versionCode 61 ;
- [x] CI #303 verte ;
- [x] APK Phone 0.15.26-dev produit ;
- [ ] téléphone Abeilles & Geckos : glissement derrière les 4 bords sans pop/disparition prématurée ;
- [ ] téléphone Abeilles & Geckos : au zoom maximal, retour dans le plateau sans redémarrage visible du cycle ;
- [ ] téléphone Gomoku : même glissement derrière le cadre, avec maximum 3+3 vivants ;
- [ ] téléphone : aucune mascotte de plateau ne recouvre titres, textes, boutons ou Prof ;
- [ ] aucune release ni merge main avant validation explicite Fab.

### GECKO-065 build
- [x] run CI #303 / 36638744397 SUCCESS sur 1cffec0bbf88148c210fad545e1a3ad4a2f56ad4 ;
- [x] artifact GeckoDoku-v0.15.26-dev-phone ;
- [x] SHA-256 archive : e5f33774d3bd0ab3af52bee00b25c1b75d5ba4a4d70b22ca09067e1b4f1092dd ;
- [x] SHA-256 APK : 1b25121080454d58934c20366073ad0a255324226759fe3baf22f2d4b1617323 ;
- [x] prerelease/release : non déclenchées.


## GECKO-066 — test TextureView pour les mascottes de plateau
- [x] ajouter une abstraction ChromaKeyPlayback commune ;
- [x] conserver ChromaKeyVideoView/GLSurfaceView comme backend historique ;
- [x] créer ChromaKeyTextureView pour Android 13+ avec MediaPlayer -> TextureView ;
- [x] reproduire le keycolor bleu/vert et le jaune Gomoku via RuntimeShader/RenderEffect ;
- [x] conserver la garde première frame : VIDEO_RENDERING_START + onSurfaceTextureUpdated fraîche avant alpha=1 ;
- [x] appliquer TextureView uniquement aux owners bee:* et gomoku:* ;
- [x] conserver Classic, Sudoku et Plante sur le backend GLSurface historique ;
- [x] fallback automatique GLSurface pour bee/gomoku si SDK < 33 ;
- [x] tests unitaires AliveVideoBackendPolicy ;
- [x] compilation fonctionnelle confirmée par CI #309 avant bump ;
- [x] version 0.15.27-dev / versionCode 62 ;
- [ ] CI finale 0.15.27 verte ;
- [ ] APK Phone 0.15.27-dev produit ;
- [ ] téléphone : PNG et vidéo Bee/Gecko obéissent exactement au même clip pendant tout le cycle ;
- [ ] téléphone : Gomoku vidéo ne déborde jamais sur boutons/titres/Prof ;
- [ ] téléphone : pas de carré noir ni flash keycolor avant première frame ;
- [ ] téléphone : zoom/drag garde la même Presence et glisse proprement derrière les bords ;
- [ ] aucune release/prerelease ni merge main avant validation explicite Fab.

### GECKO-066 build final
- [x] CI #310 verte ;
- [x] run 36641153646 SUCCESS ;
- [x] APK Phone 0.15.27-dev produit ;
- [x] taille APK : 248574081 octets ;
- [x] SHA-256 APK : e5de07cea97ca736108d644298ab56b2ad16689d695e357ef0a1d4cd280afe3f ;
- [x] artifact archive SHA-256 : e3cc39a3d9821b4894cd5244f7c0994e0b8fc09c2c45fa2c82723beaaf0479ac ;
- [x] release/prerelease : non déclenchées.


## GECKO-067 — TextureView + OpenGL pour stabiliser keycolor et jaune
- [x] conserver TextureView pour le bon clipping/Z-order Android ;
- [x] retirer le post-filtre RuntimeShader/RenderEffect instable ;
- [x] créer un pipeline OpenGL ES 2 dédié par Presence TextureView ;
- [x] MediaPlayer décode vers une SurfaceTexture d'entrée OES ;
- [x] réutiliser exactement le shader GLSL historique pour keycolor bleu/vert, despill et yellowTint ;
- [x] rendre le résultat OpenGL dans la SurfaceTexture de sortie du TextureView via EGL ;
- [x] conserver la première frame cachée jusqu'à MEDIA_INFO_VIDEO_RENDERING_START + frame GL fraîche réellement swapée ;
- [x] conserver holdOnFirstFrame ;
- [x] garder l'expérience ciblée sur bee:* et gomoku:* uniquement ;
- [x] CI intermédiaire #311 verte avant bump ;
- [x] version 0.15.28-dev / versionCode 63 ;
- [ ] CI finale 0.15.28 verte ;
- [ ] APK Phone produit ;
- [ ] téléphone : keycolor stable pendant tout le cycle ;
- [ ] téléphone : jaune Gomoku stable pendant tout le cycle ;
- [ ] téléphone : aucun débordement vidéo hors cadre/boutons ;
- [ ] téléphone : aucun carré noir avant première frame ;
- [ ] aucune release/prerelease ni merge main avant validation Fab.

### GECKO-067 build final
- [x] CI #312 verte ;
- [x] run 36643601567 SUCCESS ;
- [x] APK Phone 0.15.28-dev produit ;
- [x] taille APK : 248574081 octets ;
- [x] SHA-256 APK : 296240aa14d866f101ac921887ca30c46c1eebbde946d2f7dc5797ae70d1ce79 ;
- [x] SHA-256 archive : 2633430128a4fcd465bea4a559770ec3810d0f305c20eddafe30ced7b74585ae ;
- [x] release/prerelease : non déclenchées.


## GECKO-068 — TODO validation téléphone
- [ ] Vérifier génération du cache SpriteRGBA en 480p.
- [ ] Vérifier disparition complète du fond bleu/vert.
- [ ] Vérifier filtre jaune Prof sur sprites transparents.
- [ ] Tester Gomoku avec limite 3/camp OFF et relever activeAnimations / peakAnimations.
- [ ] Comparer FPS UI et mémoire en 240p, 360p, 480p.
- [ ] Vérifier qu'aucun petit PNG parasite ne réapparaît entre deux clips.
- [ ] Décider après mesures si le cache doit rester runtime ou être pré-généré en CI.
- [ ] Aucun merge main / aucune release avant validation Fab.


## GECKO-069 — cache chaud SpriteRGBA
- [x] remplacer le préwarm eager Gecko 7 assets par un plan vivant ciblé ;
- [x] conserver 3 rôles logiques mais seulement 2 banques physiques (Gecko vert+jaune partagés, Abeille) ;
- [x] conserver le filtre jaune au rendu, sans seconde copie RGBA ;
- [x] épingler les frames chaudes hors thread UI, plafond 16 MiB par rôle ;
- [x] différer le warmup après les premières demandes visibles ;
- [x] compléter progressivement uniquement les autres clips IDLE Gecko/Abeille ;
- [x] retirer apparition/disparition/Gecko_actions_plusieurs du préwarm de démarrage ;
- [x] ajouter tests de politique de warmup ;
- [x] version 0.15.30-dev / versionCode 65 ;
- [x] CI tests + assemblePhone verte — run #314 ;
- [ ] téléphone : confirmer HOT_BANK_PINNED GECKO_SHARED + BEE en 240p ;
- [ ] téléphone : confirmer que jaune partage la banque Gecko et reste visuellement correct ;
- [ ] téléphone : vérifier qu'un CUTE construit Gecko_actions_plusieurs uniquement à la première vraie demande ;
- [ ] téléphone : comparer temps de chauffe et FPS au démarrage avec 0.15.29-dev ;
- [ ] aucune prerelease/release ni merge main avant validation explicite Fab.

- [x] artifact GeckoDoku-v0.15.30-dev-phone produit ; digest sha256:3245421691fdc4857ee7d1dee1978cb9ff198a2d21380fbece526630b6ca795b ;


## GECKO-070 — quick-start + résolutions basses
- [x] ajouter 120p et 180p au sélecteur ;
- [x] garder 240p/360p/480p ;
- [x] passer le défaut neuf de 480p à 240p ;
- [x] publier une séquence partielle dès 4 frames prêtes ;
- [x] démarrer ChromaKeySpriteView sur le préfixe sans attendre la banque complète ;
- [x] boucler localement le préfixe pendant la fin de construction ;
- [x] basculer vers la séquence complète sans recréer la Presence ;
- [x] ajouter un test des résolutions exposées et du fallback 240p ;
- [x] version 0.15.31-dev / versionCode 66 ;
- [ ] CI tests + assemblePhone verte ;
- [ ] téléphone : mesurer délai Abeille jusqu'à QUICK_START en 120p, 180p et 240p ;
- [ ] téléphone : vérifier absence de saut visible au passage FULL_SEQUENCE_READY ;
- [ ] téléphone : comparer mémoire/FPS 120p vs 180p vs 240p ;
- [ ] aucune prerelease/release ni merge main avant validation explicite Fab.

### GECKO-069 — éléments confirmés par le test téléphone 240p
- [x] HOT_BANK_PINNED GECKO_SHARED observé : 61/61, ~13,40 MiB ;
- [x] HOT_BANK_PINNED BEE observé : 24/24, ~5,27 MiB ;
- [x] yellow=shared observé et rendu jaune visuellement propre ;
- [x] Gecko_actions_plusieurs reste on-demand : construction observée après demande CUTE ;


## GECKO-071 — STABLE_FRAME stay1/frame 1
- [x] définir Gecko STABLE_FRAME = frame 1 de GECKO_IDLE[0] ;
- [x] définir Abeille STABLE_FRAME = frame 1 de BEE_IDLE[0] ;
- [x] partager physiquement la frame Gecko avec la variante jaune ;
- [x] ne pas utiliser stay2/stay3/stay4 ni apparition/disparition comme source statique ;
- [x] extraire une seule frame avec scaling/chroma-key identiques au SpriteRGBA ;
- [x] cache mémoire léger ;
- [x] cache disque léger ;
- [x] réutiliser frame-00000 SpriteRGBA déjà présente si possible ;
- [x] Animations ON : ne plus afficher le PNG historique comme placeholder Gecko/Abeille ;
- [x] STABLE_FRAME affichée via ImageView simple, sans SPRITE_ACTIVE ;
- [x] transition STABLE_FRAME_TO_SPRITE sur première frame animée ;
- [x] Animations OFF : restaurer le PNG historique ;
- [x] logs STABLE_FRAME_REQUEST/READY/SHOW/TO_SPRITE + LEGACY_PNG_SHOW ;
- [x] ne pas toucher la plante, Pierre, gameplay, apparition/disparition ;
- [x] rafraîchir la STABLE_FRAME lors d'un changement de résolution ;
- [x] tests StableFramePolicy ;
- [x] version 0.15.32-dev / versionCode 67 ;
- [ ] CI tests + assemblePhone verte ;
- [ ] téléphone 240p à froid : STABLE_FRAME_READY très rapide pour Gecko et Abeille ;
- [ ] téléphone : vérifier activeAnimations=0 tant que seule la STABLE_FRAME est visible ;
- [ ] téléphone : vérifier transition visuellement invisible STABLE_FRAME_TO_SPRITE ;
- [ ] téléphone : animations OFF → LEGACY_PNG_SHOW et PNG historiques visibles ;
- [ ] mesurer séparément la latence restante QUICK_READY des animations visibles ; si nécessaire, traiter ensuite la priorité de file de build sans confondre avec GECKO-071 ;
- [ ] aucune prerelease/release ni merge main avant validation explicite Fab.


## GECKO-072 — Sprite Bank Factory
- [x] STABLE_FRAME prioritaire et fixée à 240p ;
- [x] barrière empêchant les builds lourds avant les STABLE_FRAME ;
- [x] catalogue canonique des 12 assets Gecko/Abeille SpriteRGBA ;
- [x] SPRITE_CATALOG_MISS sur asset non prévu ;
- [x] 60p interne, absent du sélecteur ;
- [x] pipeline visible 60p → 120p → 240p → target >240 si demandé ;
- [x] 240p reste résolution utilisateur par défaut ;
- [x] aucune préfabrication automatique 360/480 ;
- [x] max 3 workers ;
- [x] workers Android en priorité background ;
- [x] file de priorité dédiée Sprite Factory uniquement ;
- [x] chunks coopératifs de 4 frames ;
- [x] promotion d’un job catalogue lorsqu’il devient visible ;
- [x] coalescence des demandes identiques ;
- [x] coalescence STABLE_FRAME conservée et mesurée ;
- [x] cache banques dans filesDir persistant ;
- [x] état BUILDING + reprise des frames contiguës ;
- [x] publication atomique manifest READY ;
- [x] SHA-256 asset source dans la clé/manifest ;
- [x] SHA-256 banque dans le manifest ;
- [x] invalidation version générateur/chroma-key/source/résolution ;
- [x] APK lookup assets/sprites/<bankKey>/ avant génération ;
- [x] DISK/MEMORY/APK hits ;
- [x] swap de qualité sans remise volontaire à frame zéro ;
- [x] upscale filtré au rendu sans flouter les banques ;
- [x] reporting stable/60/120/240, temps, workers, FPS, RAM, hits, misses ;
- [x] bouton développeur d’export ZIP ;
- [x] export index global + rapport + banques READY + STABLE_FRAME ;
- [x] tests catalogue et progression de résolution ;
- [x] version 0.15.33-dev / versionCode 68 ;
- [ ] CI finale de tous les commits GECKO-072 verte ;
- [ ] téléphone : démarrage froid Abeilles & Geckos, mesurer STABLE_FRAME puis premier 60p QUICK_READY ;
- [ ] téléphone : confirmer ordre de qualité 60→120→240 sans restart visuel ;
- [ ] téléphone : confirmer workersPeak <= 3 ;
- [ ] téléphone : provoquer plusieurs assets manquants et vérifier « tout le monde vit d’abord » ;
- [ ] téléphone : laisser la fabrique finir 100 % puis tuer/reprendre l’app ;
- [ ] téléphone : confirmer aucune reconstruction d’une banque READY, uniquement DISK_HIT/APK_HIT ;
- [ ] téléphone : contrôler uiMinFps/uiAvgFps pendant intro, Pierre et plateau ;
- [ ] exporter GeckoDoku-sprite-banks-*.zip et vérifier l’exhaustivité ;
- [ ] intégrer ultérieurement l’export sous assets/sprites/ puis valider APK_HIT réel ;
- [ ] aucune release/prerelease ni merge main avant validation explicite Fab.

## GECKO-040 — Banques par résolution

- [x] Corriger le faux échec après `SPRITE_BANK_DISK_HIT/APK_HIT`.
- [x] Regrouper logiquement les banques par résolution sans fusionner les animations dans un fichier monolithique.
- [x] Calculer progression par frames, taille, état et nombre d'animations complètes.
- [x] Afficher ces informations dans Réglages.
- [x] Exporter une résolution à la fois.
- [x] Produire un `bank-manifest.json` par résolution.
- [x] Rendre le loader APK compatible avec `sprites/banks/<resolution>p/<bankKey>` et l'ancien chemin.
- [x] Faire suivre au catalogue de fond la chaîne progressive jusqu'à la résolution cible, y compris 480p.
- [x] Valider CI Android GECKO-040 — run #362 vert (tests + assemblePhone).
- [ ] Test téléphone : redémarrage avec banques READY, vérifier absence totale de `Required value was null`.
- [ ] Test téléphone : vérifier pourcentages / tailles / exports 60p, 120p, 240p et 480p.

## GECKO-040B — Export global 480p

- [x] Bouton « Tout préparer + exporter ».
- [x] Forcer la préparation des banques 60p / 120p / 240p / 480p avant export.
- [x] Reprendre les banques partielles existantes.
- [x] Barre de progression sur frames générées / attendues.
- [x] Export ZIP global seulement après préparation complète.
- [x] Durcir le chemin DISK/APK READY avec état terminal explicite.
- [x] Valider CI 0.15.35-dev — run #372 vert (tests + assemblePhone).
- [ ] Test téléphone : vérifier absence de faux DISK_HIT → ERROR.
- [ ] Test téléphone : lancer export global et vérifier progression jusqu'à 100 % puis ZIP avec `sprites/banks/60p`, `120p`, `240p`, `480p`.

## GECKO-041 — 240p maximum

- [x] Archiver le paquet 60/120/240/480 dans la release `PackageSprites` avec SHA-256 vérifié.
- [x] Retirer 180p / 360p / 480p des résolutions utilisateur.
- [x] Réduire la chaîne progressive à 60p / 120p / 240p.
- [x] Réduire l'export global à 60p / 120p / 240p.
- [x] Supprimer automatiquement les anciennes banques locales 180p / 360p / 480p.
- [x] Mettre l'UI export à jour pour 240p maximum.
- [x] Valider CI 0.15.36-dev — run #388 vert (tests + assemblePhone).
- [ ] Test téléphone : vérifier affichage de seulement 60p / 120p / 240p.
- [ ] Test téléphone : vérifier récupération de l'espace disque après purge du 480p.

