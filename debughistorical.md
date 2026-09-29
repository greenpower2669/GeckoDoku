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
