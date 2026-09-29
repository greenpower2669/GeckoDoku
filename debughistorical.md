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
