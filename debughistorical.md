# GeckoDoku — debughistorical actif

Aucun incident actif à conserver.

Les états durables utiles sont dans `brain.md` et `brainmap.md`.
L’archive froide `sauvegarde.md` reste disponible uniquement si une régression exige une recherche historique.


## GECKO-050 — CI #242 — TEST DE COMPTEUR CORRIGÉ

Symptôme :
un seul test AliveAnimator échouait ; Kotlin et les variantes Debug/Phone compilaient.

Cause :
le test comptait la transition APPEARING → premier IDLE comme une attente déjà terminée.

Correction :
le test consomme d'abord la fin de l'apparition, puis compte quatre fins réelles d'IDLE avant d'attendre la réinterrogation générale.

Aucun changement de règle fonctionnelle du moteur.


## GECKO-050 — sémantique d'attente affinée avant test téléphone

Relecture de la demande Fab :
une attente sélectionnée doit rester calme et se répéter jusqu'à quatre cycles, puis seulement update() effectue un nouveau choix.

Correction :
- IDLE 1/2/3/4 choisi → même clip jusqu'à quatre lectures ;
- quatrième fin → reroll + signal aux autres mascottes ;
- nouveau choix différent du précédent lorsqu'une alternative existe ;
- fin de mignonnerie → reroll immédiat + signal groupe ;
- une mignonnerie unique ne peut pas se resélectionner immédiatement si des attentes existent.

But :
éviter un changement visuel à chaque boucle et conserver la sérénité voulue.


## GECKO-050 — CI #246 — VERTE

Commit applicatif testé :
92eed1b0f273a1be069994d61f91f75ab3a5bc7f

Résultat :
- tests unitaires OK ;
- compilation Debug/Phone OK ;
- APK 0.15.7-dev produit ;
- aucune prerelease/release déclenchée.

Reste :
validation téléphone Fab de la continuité vidéo/PNG, des échelles, des attentes 4 cycles, des key colors, de la Plante et des non-régressions Prof/Pierre.


## GECKO-050 — 0.15.7-dev — COUPURE ENTRE DEUX VIDÉOS

Observation téléphone Fab :
parfois la mascotte disparaît brièvement entre deux vidéos, comme une coupure.

Cause confirmée dans l'architecture :
ChromaKeyVideoView.play() appelle stopPlayback(), arme revealOnFirstFrame et place alpha=0 jusqu'à la première frame du nouveau clip.
Le masque GECKO-050 reste actif pour cacher le PNG du plateau.
Entre les deux, on peut donc voir uniquement le fond.

Correctif 0.15.8-dev :
- si une première frame a déjà été rendue (maskLatched=true), afficher le PNG canonique dans AliveMascotOverlayView pendant le first-frame gate du clip suivant ;
- conserver exactement la même géométrie ;
- cacher le PNG dans onFirstFrameRendered ;
- ne jamais utiliser ce bridge avant la première apparition.

Résultat attendu :
vidéo A → image fixe cohérente très brève → vidéo B, sans phase vide.


## GECKO-050 — CI #247 — VERTE

Commit applicatif :
d9a744fc96814ada0dda5077363c18e4a6d7517a

Résultat :
- tests OK ;
- compilation Debug/Phone OK ;
- APK 0.15.8-dev produit ;
- prerelease/release non déclenchée.

À valider sur téléphone :
absence du vide entre deux clips grâce au bridge PNG, sans PNG prématuré avant la première apparition.


## GECKO-050 — carrés de fond visibles — CAUSE ET REFACTOR 0.15.9-dev

Observation Fab :
carrés de couleur visibles derrière certains Gecko jaunes et Abeilles alors que le chroma-key lui-même fonctionne.

Cause :
la première implémentation AliveMascotOverlayView conservait l'ancienne technique maskColorProvider et peignait la couleur de la case sous la vidéo.

Décision :
supprimer totalement cette technique dans la nouvelle classe.

Correction :
- aucun fond artificiel ;
- chaque instance vivante possède PNG transparent + vidéo ;
- le plateau masque seulement son ancien PNG à la cellule concernée ;
- plusieurs slots vivants simultanés pour augmenter la présence d'animations ;
- Plante draggable avec position persistée.

Commit applicatif :
b6c7f5001b412112c6011882cb540b079db8b9a9

CI et téléphone à valider.


## GECKO-050 — CI #248 — VERTE

Commit applicatif :
b6c7f5001b412112c6011882cb540b079db8b9a9

Résultat :
- tests unitaires OK ;
- compilation Debug/Phone OK ;
- APK 0.15.9-dev produit ;
- aucune prerelease/release déclenchée.

À valider sur téléphone :
- disparition complète des carrés de couleur ;
- 3 Gecko / 2 Abeilles suffisamment vivants sans surcharge ;
- PNG pont propre ;
- Gecko Prof jaune sans carré ;
- drag et mémorisation de la Plante.


## GECKO-050 — nuages trop géométriques

Observation téléphone Fab :
les nuages gris des placements de départ sont agréables mais encore trop opaques et géométriques.

Correction 0.15.10-dev :
- remplacement des 4 ovales réguliers par 7 petites bouffées irrégulières ;
- alpha réduit de l'ancien ordre 38..66 vers 8..21 ;
- tailles et positions variées par seed ;
- mouvement conservé mais plus discret ;
- même politique visuelle en Classic et Abeilles & Geckos.


## GECKO-050 — CI #249 — VERTE

Commit applicatif :
266a0efcd471f90b09f17341b5c2958121b80498

Résultat :
- tests unitaires OK ;
- compilation Debug/Phone OK ;
- APK 0.15.10-dev produit ;
- aucune prerelease/release déclenchée.

À valider sur téléphone :
nuages plus transparents, moins géométriques, toujours lisibles sur les placements donnés.


## GECKO-050 — validations téléphone après CI #249

0.15.9-dev :
Fab juge le rendu visuel « propre » après suppression des anciens maskColorProvider et rectangles de couleur. Le défaut restant identifié n'est plus un incident de chroma-key : la vie des mascottes paraît encore trop mécanique.

Décision fonctionnelle suivante :
update() devra gérer des grands cycles autonomes et non répétitifs, mémoriser les séries complètes et transmettre une mignonnerie à une copine visible en fin de cycle.

0.15.10-dev :
Fab valide « Parfait » le nouveau brouillard des placements donnés :
- opacité réduite ;
- forme moins géométrique ;
- petites bouffées irrégulières.
Ce point est clos sauf régression.

Note historique :
les mentions plus haut de « masque continu » ou maskColorProvider décrivent les anciennes versions 0.15.7/0.15.8. Elles ne décrivent plus l'architecture courante depuis 0.15.9.


## GECKO-050 — publication 0.15.10-dev

Après validation téléphone Fab :
- CI #250 : build vert + création de la prerelease phone-0.15.10-dev-run-250 ;
- CI #251 : build vert + promotion réussie ;
- release finale non-prerelease ;
- APK public confirmé ;
- SHA-256 : 684a40dbdc8ab91a8ca4ccdb5d4d904907279b9ad9c05c35603ae0b253ac9b46.

Aucun incident de publication.


## GECKO-051 — seuls Plante et Pierre semblaient vivants

Observation téléphone Fab sur la release 0.15.10-dev :
les Gecko visibles en Classic restent statiques ; visuellement seuls la Plante et Pierre s'animent.

Cause :
le pool 3 Gecko / 2 Abeilles / 1 Plante était utilisé comme pool d'instances vivantes au lieu d'être uniquement un pool de lecteurs vidéo. De plus, les pièces déjà présentes au chargement n'étaient pas synchronisées dans AliveMascotOverlayView.

Correction 0.15.11-dev :
- registre léger de Presence pour toutes les mascottes visibles ;
- PNG interne par Presence ;
- ancien PNG du plateau supprimé tant que Presence existe ;
- pool vidéo séparé et borné ;
- rotation update() vers les présences les moins récemment animées ;
- synchronisation snapshot dans les quatre modes ;
- série 3..5 stays différents + mémoire anti-série identique ;
- grand cycle déclenche une cute chez une autre mascotte compatible.


## GECKO-051 — CI #252 — VERTE

Commit :
616b0dcdb808735ca0a396091aa2428ed5192034

Résultat :
- tests unitaires OK ;
- compilation Debug/Phone OK ;
- APK 0.15.11-dev produit ;
- aucune prerelease/release déclenchée ;
- SHA-256 APK : 9d8de7bd06a5defcbef0523cbdae293f125c077f9a628bfdb0b37357cfbf709f.

Validation appareil encore requise pour confirmer la rotation réelle entre toutes les Presence visibles dans les quatre modes.


## GECKO-052 — test de l'hypothèse « verrou vidéo »

Observation Fab, 0.15.11-dev :
sur une grille Classic terminée, les Gecko sont présents et propres mais l'animation s'essouffle ; seuls Pierre et la Plante semblent continuer à vivre.

Hypothèse diagnostique :
le pool borné des ChromaKeyVideoView ou son ordonnanceur empêche les Presence Gecko d'obtenir/reprendre un lecteur.

Test 0.15.12-dev :
- suppression du plafond runtime ;
- allocation lazy d'un VideoSlot par Presence visible ;
- toutes les mascottes éligibles peuvent jouer simultanément.

Autre défaut observé sur capture :
Gecko/Plante passaient devant les deux vidéos intro.
Correction : suspension complète de AliveMascotOverlayView pendant les phases INTRO FIRST/SECOND, reprise à DONE/skip.


## GECKO-052 — CI #253 — VERTE

Commit :
f90f41a4d6c65d952538a1cbad266e7b2f0c0abd

Résultat :
- tests unitaires OK ;
- compilation Phone OK ;
- artifact 0.15.12-dev produit ;
- aucune release/prerelease ;
- SHA-256 APK : a50bffee172b944dfe1e40d977b6eccc5b3383722aed7c4ff1d2e643615fc1c3.

À observer sur téléphone :
1. tous les Gecko visibles doivent s'animer sans attendre un slot ;
2. Abeilles et Gecko du mode hexagonal doivent tous pouvoir s'animer ;
3. les deux vidéos intro doivent masquer totalement Gecko/Plante ;
4. surveiller volontairement fluidité, chauffe et stabilité car ce build est sans limitation.


## GECKO-053 — animations perdues après changement d'application

Observation Fab :
le build 0.15.12-dev sans verrou fonctionne tant que GeckoDoku reste au premier plan. Après passage par Mail/Messages puis retour, Pierre et la Plante repartent mais les mascottes de plateau restent statiques.

Cause code :
onPause() stopAll() supprime volontairement toutes les Presence. onResume() rappelait ensurePlantMascot() mais pas syncLivingMascotsForCurrentMode().

Correction 0.15.13-dev :
ajout de syncLivingMascotsForCurrentMode() juste après ensurePlantMascot() dans le screenRoot.post de onResume().

Choix conservé :
stopAll() reste dans onPause() pour éviter lectures vidéo/GLSurfaceView en arrière-plan. La reprise reconstruit depuis le snapshot, ce qui est plus sûr que garder les lecteurs actifs.


## GECKO-053 — CI #254 — VERTE

Commit :
173bf0ed9e4431b3e6efe13de44c4374cc055bcb

Résultat :
- tests unitaires OK ;
- build Phone OK ;
- APK 0.15.13-dev produit ;
- aucune release/prerelease ;
- SHA-256 APK : 4bee54cfdc1261adf75559635c8749ade82133a8383cc34e1ddbea4019d13937.

Test appareil attendu :
basculer vers Mail/Messages puis revenir et vérifier que Gecko/Abeille/Plante reprennent tous sans action utilisateur.


## GECKO-054 — ordonnanceur supprimé définitivement

Verdict téléphone Fab :
le mode sans limitation a confirmé que l’ancien ordonnanceur/pool participait à l’essoufflement des animations. Décision : « plus jamais d’ordonnanceur ».

Le correctif 0.15.14-dev regroupe aussi les autres observations :
- pièces initiales de map pouvant rester mortes, surtout Bee/Gecko ;
- target parfois null avant layout ;
- PNG visible avant vidéo d’apparition ;
- reconstruction nécessaire après Mail/Messages ;
- Pierre fonctionne sur son pipeline autonome et ne doit pas être fusionné avec AliveMascotOverlayView.

Correction :
- 1 Presence = 1 lecteur vidéo ;
- retry local target ;
- refresh post-layout ;
- apparition stricte ;
- lifecycle snapshot conservé ;
- intro prioritaire ;
- Pierre inchangé visuellement ;
- 300 phrases RETURN avec anti-répétition renforcée.


## GECKO-054 — CI #255 — VERTE

Résultat :
- tests unitaires OK ;
- compilation Phone OK ;
- artifact GeckoDoku-v0.15.14-dev-phone produit ;
- aucune prerelease/release ;
- SHA-256 APK : 67cb5429d2ae188e8a5a58f09f43afa2032a96e43f1998f1265bafe96eed8abb.

Validation appareil attendue : quatre modes, pièces initiales Bee/Gecko, apparition, retour Mail/Messages, intros on top et variété des 300 retours Pierre.


## GECKO-055 — 0.15.14-dev : logique gagnante, rendu vide

Observation téléphone Fab :
le jeu reste logiquement correct et peut gagner, mais les pièces disparaissent visuellement.

Analyse après échange :
le nouveau concept 1 Presence = 1 lecteur est conservé. Le défaut est dans la composition Android : 0.15.14-dev avait mis l'ImageView PNG et le ChromaKeyVideoView GLSurfaceView dans le même FrameLayout. Or le GLSurfaceView possède une surface/z-order distincts.

Correction 0.15.15-dev :
- la Presence garde son PNG et sa vidéo ;
- pngContainer et videoContainer deviennent siblings séparés ;
- même target/scale pour les deux ;
- le plateau ne fait plus de hide/show au rythme de playDecision ;
- setStaticSuppressed devient uniquement le handoff plateau → Presence lorsque la target existe ;
- fin vidéo = videoContainer caché + PNG interne immédiatement visible ;
- apparition = seule phase où PNG interne reste volontairement caché.

Aucun retour d'ordonnanceur.


## GECKO-055 — CI #257 VERTE

Commit testé :
135ec82888e8d01494f74bd56adcea86f5dae490

Résultat :
- tests unitaires OK ;
- compilation Phone OK ;
- artifact 0.15.15-dev produit ;
- aucune release/prerelease ;
- SHA-256 APK : 671776186328f53df577599e45d2cb85ea8bc6a8ca8daf5d54bf0721c71dabf5.

Le verdict téléphone doit confirmer que la séparation pngContainer / videoContainer empêche le plateau vide.


## GECKO-056 — Gecko et Abeilles figés : race première frame

Observation téléphone Fab :
les Gecko ne démarrent pas leurs animations, et les Abeilles non plus. Le PNG reste visible.

Diagnostic :
AliveMascotOverlayView lance bien une décision animée et ChromaKeyVideoView garde revealOnFirstFrame=true. FreshPlaybackFrameGate exigeait cependant playerStarted au moment exact où MEDIA_INFO_VIDEO_RENDERING_START était reçu. Ce signal peut arriver pendant MediaPlayer.start(), avant l'appel local onPlayerStarted() placé juste après start(). Dans cet ordre, renderingStarted restait faux pour toute la génération ; aucune frame ne pouvait être acceptée et la vidéo restait alpha=0.

Correction 0.15.16-dev :
- onRenderingStart() latche le signal pour la génération courante indépendamment de l'ordre ;
- onFrameRendered() exige toujours playerStarted + renderingStarted ;
- anciennes générations et cancel() restent bloquants ;
- FreshPlaybackFrameGateTest couvre l'ordre inversé, les générations périmées et cancel().

Aucun ordonnanceur/pool n'est réintroduit.

## GECKO-056 — CI #260 VERTE

Commit testé : a43ef730ec77475e08b3ab7c4b897a023977057c.
Tests unitaires + assemblePhone : succès. Artifact : GeckoDoku-v0.15.16-dev-phone. SHA-256 archive : 4fde649f9492e89ba01e12e62a4094e454e229008c930c35837b200c94308894. Aucune release/prerelease déclenchée.


## GECKO-057 — 0.15.16-dev : le log prouve que play() n'est jamais atteint

Observation du log téléphone fourni par Fab :
- Pierre : PLAY_REQUEST, VIDEO_RENDERING_START_SIGNAL, VIDEO_FIRST_FRAME et VIDEO_VISIBLE sont présents ;
- mascottes plateau : plusieurs ChromaKey sont créés (SURFACE_POLICY) mais aucun PLAY_REQUEST Gecko/Abeille n'apparaît ;
- la panne est donc avant ChromaKeyVideoView.play(), pas dans le first-frame gate corrigé par GECKO-056.

Cause code :
refreshDynamicTargets() pouvait rappeler schedulePresence() de façon répétée. Celui-ci supprimait systématiquement le callback existant puis le repostait. En parallèle refreshPresenceTarget() réassignait toujours les LayoutParams des deux siblings, même inchangés, ce qui entretenait les refresh/layout. Le cycle d'une Presence pouvait rester indéfiniment pending : PNG visible ou apparition bloquée, vidéo jamais demandée.

Correction 0.15.17-dev :
- callback déjà armé = jamais replanifié par un refresh ;
- désarmement uniquement au départ réel du runnable, cancel ou remove ;
- géométrie idempotente : pas de layoutParams réassignés si rect identique ;
- trace ALIVE_PLAY juste avant play() ;
- dernier Gecko de partie inclus dans la couche vivante.

Aucun ordonnanceur ou pool réintroduit.

## GECKO-057 — CI #263 VERTE

Commit testé : 352c046bcc6e1af6d26794782f14189054ddcae2. Tests + assemblePhone : succès. APK : GeckoDoku-v0.15.17-dev.apk. SHA-256 : cb50cb5e97ed9cc33ab7285982a6da7fda5e6439040fa2a195b7c59bb6fa8577. Aucune release/prerelease.

## GECKO-057 — validation téléphone mode 1
Fab confirme : mode 1 / Classic corrigé sur 0.15.17-dev. Les Gecko du mode Classic s'animent de nouveau. Les autres modes restent à valider séparément.


## GECKO-058 — mode Abeilles & Geckos : Bee OK, Gecko de bord rejetés

Preuve téléphone 0.15.17-dev :
- Abeille : ALIVE_PLAY -> PLAY_REQUEST -> START -> VIDEO_RENDERING_START_SIGNAL -> VIDEO_FIRST_FRAME -> VIDEO_VISIBLE, donc pipeline vivant fonctionnel ;
- aucun ALIVE_PLAY kind=GECKO avec owner bee:GECKO:* pendant la séquence du mode hexagonal ;
- la capture montre les trois Gecko confirmés sur des cellules de bord (gauche, droite, bas), alors que l'Abeille animée est intérieure.

Cause code :
MainActivity.beeGeckoAliveTarget() construit un rect centré puis retourne null si un seul bord du rect sort de viewportRectOnScreen().
Les Gecko utilisent une cible plus grande que les Abeilles, donc les Gecko situés sur le pourtour sont rejetés alors que l'Abeille intérieure reste admissible.

Correctif attendu :
ne plus transformer une légère intersection de bord en target=null ; conserver une cible animable visible, clippée/recentrée dans le viewport et compatible avec zoom/drag.


## GECKO-058 — correction cibles de bord hexagonal

Correctif appliqué dans MainActivity.beeGeckoAliveTarget().

Avant :
toute cible débordant d'un seul pixel du viewport renvoyait null.

Après 0.15.18-dev :
- null uniquement si aucune intersection avec le viewport ;
- sinon maintien de la taille du rectangle ;
- décalage X/Y minimal pour remettre la cible dans le viewport ;
- recentrage si une dimension de cible est plus grande que le viewport ;
- trace BEE_GECKO_ALIVE_TARGET_CLAMPED avec piece/q/r/dx/dy.

Ce changement vise les Gecko de bord observés sur la capture (gauche, droite, bas) sans modifier les Abeilles déjà fonctionnelles.

## GECKO-058 — CI #265 VERTE

Commit testé : 2c20b06d2a0f699c8811cdf7cd37871a4b712f45. Tests + assemblePhone : succès. APK : GeckoDoku-v0.15.18-dev.apk. SHA-256 : 9e0ad087bc2f641ecd80010bb162cf57739ac0b0a236d2dc21cbe5d321311b77. Aucune release/prerelease.


## GECKO-059 — règle viewport supprimée

Le cadre média transparent/key-color est plus large que le personnage visible et celui-ci est déjà centré. Rejeter ou déplacer le média parce que le cadre dépasse le viewport était donc une erreur de modèle et une règle non demandée.

Correction 0.15.19-dev : suppression du test viewport, du clamp et de la trace BEE_GECKO_ALIVE_TARGET_CLAMPED.

## GECKO-059 — CI #267 VERTE

Commit testé : baf9743f71821032fbf88480462554eddeade1d5. Tests + assemblePhone : succès. APK SHA-256 : 97e3876d853dd1701f3e7f552fe38d289bf4d5344278416a1e409d7817d6ebcf. Aucune release/prerelease.

## GECKO-060
0.15.20-dev : BeeGeckoBoardView ne dessine plus les pièces. AliveMascotOverlayView possède seul le PNG et la vidéo. La visibilité hors écran est décidée par la cellule du plateau.
