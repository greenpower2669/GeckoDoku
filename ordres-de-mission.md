# GECKODOKU — ORDRES DE MISSION ACTIFS

## GECKO-050 — SÉRÉNITÉ / MASCOTTES VIVANTES HOMOGÈNES

Date : 2026-09-29
Branche de développement : gecko-039-sudoku-tap-gecko-gomoku

### Intention de Fab

Les mascottes, Prof Gecko et petits personnages participent directement au côté calme, mignon et rassurant de GeckoDoku.

Le but de GECKO-050 est d'augmenter cette sensation de sérénité en rendant Gecko, Abeille et à terme les décorations vivantes beaucoup plus homogènes :
- même échelle entre PNG et vidéo ;
- transitions sans saut visuel ;
- attente discrète et continue ;
- variété sans répétition mécanique ;
- comportement commun dans tous les modes concernés ;
- fallback PNG propre lorsque les animations sont désactivées.

Les médias restent décoratifs et ne doivent jamais modifier la logique d'un jeu.

---

## 1 — Problèmes constatés

### Gecko
- certaines apparitions/disparitions ne sont pas homogènes selon le mode ;
- le PNG peut réapparaître avant la fin réelle de la vidéo d'apparition ;
- l'échelle du Gecko vidéo peut différer légèrement de celle du PNG ;
- le Gecko n'a pas encore partout un mouvement d'attente vivant et discret ;
- le comportement varie selon les écrans alors qu'il devrait être centralisé.

### Abeille
- même besoin de présence vivante et homogène ;
- animations d'attente nouvellement fournies ;
- apparition/disparition dédiées non encore identifiées dans les assets actuels.

### Plante carnivore décorative
- doit pouvoir vivre en bas à droite comme décoration sereine ;
- animations d'attente disponibles ;
- une animation longue existe ;
- elle ne doit jamais gêner le plateau, le Prof ou les interactions.

---

## 2 — Architecture demandée

Créer une architecture générique de type :

### AliveAnimator
Responsable du cycle de vie visuel d'une mascotte :
- apparition ;
- attente ;
- mignonnerie / animation longue ;
- disparition ;
- fallback PNG ;
- position ;
- échelle ;
- key color ;
- mémoire des dernières animations ;
- choix aléatoire contrôlé.

Nom final libre si l'architecture exige un nom plus précis, par exemple :
- AliveAnimator ;
- MascotAliveAnimator ;
- MascotAnimationProfile ;
- MascotLifeCoordinator.

Le principe compte plus que le nom.

### États minimaux
- HIDDEN ;
- APPEARING ;
- IDLE ;
- CUTE / LONG_ACTION ;
- DISAPPEARING ;
- STATIC_PNG.

---

## 3 — Profil d'une mascotte

Chaque type doit pouvoir définir :
- PNG transparent de référence ;
- vidéo apparition éventuelle ;
- vidéo disparition éventuelle ;
- liste des attentes disponibles ;
- liste des mignonneries / animations longues éventuelles ;
- key color vidéo ;
- scale de référence ;
- anchor ;
- offset X/Y ;
- politique de transition.

La géométrie visuelle doit être unique :
la vidéo et le PNG d'une même mascotte doivent occuper la même zone et avoir la même taille apparente.

---

## 4 — Règle animations ON / OFF

### Animations ON
La mascotte utilise les vidéos chroma-key selon son état.

### Animations OFF
- aucune vidéo ;
- aucun fond key color ;
- affichage direct du PNG transparent ;
- mêmes coordonnées et même scale logique que le mode vidéo.

Le changement animations ON/OFF ne doit jamais changer la logique du jeu ni provoquer de reflow du plateau.

---

## 5 — Apparition et disparition

### Apparition
Règle stricte :
1. démarrer la vidéo d'apparition ;
2. ne jamais afficher le PNG par-dessus avant la vraie fin de la vidéo ;
3. à la fin, passer directement à l'attente choisie ou au PNG si nécessaire ;
4. conserver position et échelle entre les deux représentations.

### Disparition
1. jouer la disparition si elle existe ;
2. attendre sa fin ;
3. masquer ensuite la mascotte ;
4. ne pas faire clignoter le PNG pendant la transition.

Si un asset manque, fallback propre sans faux flash.

---

## 6 — Attente vivante

Chaque mascotte possède plusieurs animations d'attente.

AliveAnimator doit mémoriser au minimum :
- animation courante ;
- dernière animation ;
- petit historique récent ;
- nombre d'utilisations de l'attente actuelle ;
- dernière animation longue éventuelle.

### Règles
- un **nouveau choix** d'animation ne doit jamais sélectionner le même clip que le choix précédent si une alternative existe ;
- une attente choisie reste volontairement stable et peut boucler jusqu'à 4 fois : c'est le mouvement discret et répétitif demandé par Fab ;
- si plusieurs nouveaux choix sont disponibles, éviter aussi les animations récentes ;
- après la 4e boucle de l'attente, lancer une réinterrogation générale puis choisir un autre clip ;
- au terme d'une mignonnerie / animation longue, forcer une réinterrogation ;
- le choix initial se fait lors de init() ;
- la sélection aléatoire doit rester douce et non frénétique.

L'objectif n'est pas une animation permanente spectaculaire :
les mouvements doivent rester discrets et reposants.

---

## 7 — update() et coordination

Avant de lancer une nouvelle séquence, l'instance passe par une méthode globale de mise à jour, par exemple update().

Cette méthode :
- regarde son historique ;
- regarde combien de fois l'attente a été utilisée ;
- choisit une autre attente ou une mignonnerie ;
- refuse la répétition immédiate ;
- remet à jour les paramètres nécessaires.

Quand :
- une mignonnerie se termine ;
OU
- la même attente a été utilisée 4 fois,

l'instance déclenche une réinterrogation générale.

Un coordinateur léger peut alors inviter les autres mascottes visibles à réévaluer leur prochain choix, sans les synchroniser brutalement ni créer un ballet mécanique.

---

## 8 — Inventaire réel des assets au 29/09/2026

### Gecko — déjà présent sur la branche active
- assets/gecko/Gecko_tr.png
- assets/gecko/Gecko_apparition.mp4
- assets/gecko/Gecko_disparition.mp4
- assets/gecko/Gecko_actions_plusieurs.mp4
- assets/gecko/Gecko_Intro.mp4
- assets/gecko/IntroGeckoGD.mp4

### Gecko — nouveaux fichiers actuellement sur main, racine assets/
- Geckostay1.mp4
- Gekostay2.mp4
- Geckostay3.mp4
- Geckostay4.mp4

Note :
`Gekostay2.mp4` a une orthographe différente ; normaliser lors de l'intégration sans perdre l'asset.

### Abeille — déjà présent sur la branche active
- assets/abeille/AbeilleTr.png
- assets/abeille/Abeillefondvert.mp4

### Abeille — nouveaux fichiers actuellement sur main, racine assets/
- Beestay1.mp4
- Beestay2.mp4
- Beestay3.mp4
- Beestay4.mp4

### Abeille — manque à confirmer / fournir
Aucune vidéo clairement nommée apparition/disparition Abeille n'a été trouvée dans l'inventaire actuel.

Ne pas inventer ces assets.
Si Fab les fournit plus tard, le profil AliveAnimator doit pouvoir les adopter sans refonte.

### Plante — nouveaux fichiers actuellement sur main, racine assets/
- Plantestay1.mp4
- Plantestay2.mp4
- Plantesyay3.mp4
- Plante stay4.mp4
- Planteannime10s.mp4

Notes :
- `Plantesyay3.mp4` semble être le stay3 malgré le nom ;
- `Plante stay4.mp4` contient un espace ;
- normaliser les noms lors de l'intégration ;
- aucun PNG transparent Plante clairement identifié ;
- aucune apparition/disparition Plante clairement identifiée.

Ne pas inventer les médias manquants.

---

## 9 — Organisation des assets

Les nouveaux fichiers ont été uploadés sur `main/assets/` mais ne sont pas encore présents sur la branche de développement active.

Avant codage effectif :
- importer explicitement les assets utiles dans la branche active ;
- les ranger dans des dossiers cohérents ;
- éviter les noms avec espaces ou fautes ;
- conserver le contenu binaire intact ;
- mettre à jour AssetMediaCatalog ou équivalent ;
- ne jamais utiliser Base64.

Organisation cible suggérée :

assets/gecko/alive/
assets/abeille/alive/
assets/plante/alive/

ou toute organisation équivalente claire.

---

## 10 — Intégration multi-mode

Le même moteur vivant doit être réutilisable dans tous les modes qui montrent Gecko/Abeille.

Ne pas créer une implémentation différente dans chaque mode.

Le moteur reçoit son profil + sa géométrie d'affichage et doit produire le même comportement visuel partout.

Le Prof Gecko garde sa propre logique vidéo/parole existante :
GECKO-050 ne doit pas casser Pierre, ProfParle, les bulles, ni les politiques de priorité de parole.

---

## 11 — Plante décorative

Première intention :
- décoration bas droite ;
- vivante mais discrète ;
- animations d'attente + animation longue ;
- pas de collision ;
- pas d'interaction gameplay ;
- pas de reflow ;
- ne doit pas masquer les boutons ou zones tactiles utiles.

Son intégration peut être faite après Gecko/Abeille si nécessaire.

---

## 12 — Ordre de réalisation

### Phase A — audit / architecture
- inventorier tous les appels actuels aux vidéos Gecko/Abeille ;
- repérer les endroits où PNG et vidéo n'utilisent pas la même géométrie ;
- identifier les anciennes transitions qui doivent être remplacées ;
- construire le profil générique et l'automate AliveAnimator.

### Phase B — Gecko
- importer stay1..4 ;
- homogénéiser apparition → attente → disparition ;
- corriger le flash PNG ;
- unifier scale / position ;
- mémoire anti-répétition.

### Phase C — Abeille
- importer Beestay1..4 ;
- brancher le même moteur ;
- fallback si apparition/disparition absentes ;
- ne pas inventer de média manquant.

### Phase D — Plante
- importer/ranger ses médias ;
- l'ancrer bas droite ;
- utiliser le même moteur ;
- commencer par attente / animation longue si aucun PNG/apparition/disparition n'existe.

### Phase E — intégration globale
- remplacer les comportements locaux redondants ;
- vérifier tous les modes ;
- animations OFF = PNG stable ;
- CI + test téléphone.

---

## 13 — Critères de validation téléphone

- [ ] Gecko ne flashe plus en PNG avant la fin de son apparition ;
- [ ] Gecko PNG et vidéo ont la même taille apparente ;
- [ ] Gecko garde une présence discrète en attente ;
- [ ] les attentes Gecko ne se répètent jamais deux fois de suite ;
- [ ] Abeille utilise ses 4 attentes avec la même logique ;
- [ ] animations OFF affiche des PNG propres sans key color ;
- [ ] aucune mascotte ne redimensionne un plateau ;
- [ ] aucune mascotte ne modifie la logique d'un jeu ;
- [ ] changement de mode ne laisse aucune vidéo fantôme ;
- [ ] Prof Gecko / Pierre restent inchangés fonctionnellement ;
- [ ] Plante, si activée dans cette passe, reste en bas à droite sans gêner l'UI ;
- [ ] mémoire de choix donne une impression variée et non mécanique.

---

## 14 — Règles de sécurité de mission

- ne pas modifier la logique des jeux ;
- ne pas rouvrir les missions closes ;
- ne pas remplacer les médias du Prof par ce système ;
- ne pas lire sauvegarde.md sans besoin de régression ;
- ne pas publier de release avant validation téléphone Fab ;
- maintenir les mémoires concernées dans le même cycle de code ;
- conserver brain.md et brainmap.md comme état durable, à mettre à jour uniquement lorsque le comportement est réellement intégré.

### État
ORDRE DE MISSION OUVERT.

Socle actuel validé techniquement :
- AliveAnimator + profils Gecko / Abeille / Plante ;
- AliveMascotOverlayView autonome visuellement ;
- aucun maskColorProvider et aucun rectangle de couleur de fond ;
- chaque mascotte possède son PNG transparent et ses vidéos ;
- le plateau suspend uniquement son ancien PNG statique pendant la prise en charge par la classe vivante ;
- PNG transparent interne utilisé comme pont entre deux clips ;
- pool borné actuel : 3 Gecko + 2 Abeilles + 1 Plante ;
- Plante draggable avec position mémorisée ;
- Classic / Sudoku / Gomoku / Abeilles & Geckos branchés sur le même moteur ;
- Pierre / ProfParle restent hors de cette architecture.

Retours téléphone Fab :
- 0.15.9-dev : rendu visuel jugé propre après suppression des anciens masques ; le point restant n'est plus le chroma-key mais le comportement trop mécanique des cycles ;
- 0.15.10-dev : nuages des placements donnés validés « parfait » après réduction d'opacité et forme plus organique.

PROCHAINE ÉTAPE ACTIVE :
implémenter le vrai update() autonome des mascottes avec mémoire des séries complètes, grand cycle non répétitif, désynchronisation et déclenchement d'une mignonnerie chez une copine visible en fin de grand cycle.


## GECKO-050 — correction téléphone 0.15.9-dev

Décision Fab :
- supprimer totalement l'ancien masquage par couleur de fond dans AliveAnimator ;
- Gecko, Abeille et Plante possèdent leur PNG transparent dans la classe vivante ;
- le plateau ne fournit plus de couleur de case ;
- lorsqu'une mascotte est prise en charge par AliveAnimator, le plateau suspend uniquement son ancien PNG statique à cette position ;
- AliveAnimator affiche son propre PNG ou sa vidéo ;
- plusieurs mascottes doivent rester vivantes simultanément afin que le plateau paraisse suffisamment animé ;
- la Plante carnivore doit être déplaçable par drag.

Implémentation :
- aucun maskColorProvider dans AliveMascotOverlayView ;
- aucun rectangle de fond ajouté derrière les mascottes ;
- pool borné : 3 Gecko + 2 Abeilles + 1 Plante ;
- quand le pool est plein, la mascotte vivante la plus ancienne rend sa place et son PNG de plateau réapparaît ;
- PNG interne utilisé en mode animations OFF et comme pont entre clips ;
- PNG pont du Gecko Prof reçoit la même teinte jaune que sa vidéo ;
- Plante draggable, position mémorisée en coordonnées normalisées ;
- aucune modification des règles de jeu.

État : code 0.15.9-dev intégré au commit b6c7f5001b412112c6011882cb540b079db8b9a9 ; CI #248 entièrement verte ; APK téléphone produit. Validation Fab requise avant toute release publique.


## GECKO-050 — comportement autonome des grands cycles + nuages doux

Décision Fab sur AliveAnimator :
- update() doit gérer automatiquement la vie visuelle sans intervention du jeu ;
- chaque fin de petite animation choisit une attente différente de la précédente ;
- le moteur doit mémoriser les séries complètes, pas seulement le dernier clip ;
- un nouveau grand cycle ne doit pas reproduire la même série que le précédent, car l'œil humain repère vite les motifs ;
- fin de grand cycle : choisir une autre mascotte visible et lui proposer une animation mignonne/longue ;
- les autres mascottes réinitialisent leur propre cycle de manière désynchronisée ;
- éviter aussi de sélectionner toujours la même copine ou la même mignonnerie.

État de ce point : CONTRAT ENREGISTRÉ, PAS ENCORE IMPLÉMENTÉ dans 0.15.10-dev.

Décision Fab sur les nuages des placements donnés/grisés :
- conserver l'idée du nuage ;
- beaucoup plus transparent ;
- moins géométrique ;
- forme organique faite de petites bouffées irrégulières ;
- ne jamais masquer la lisibilité de la pièce ou de la case.

Implémentation 0.15.10-dev :
- GivenFogVisualPolicy commun aux grilles Classic et Abeilles & Geckos ;
- 7 bouffées de tailles/positions différentes au lieu de 4 grands ovales réguliers ;
- alpha abaissé à 8..21 ;
- légère dérive déterministe pour rester douce et stable.


État nuages 0.15.10-dev :
- commit applicatif 266a0efcd471f90b09f17341b5c2958121b80498 ;
- CI #249 verte ;
- APK téléphone produit ;
- validation téléphone Fab : PARFAIT / VALIDÉ ;
- ne pas rouvrir ce rendu sauf régression.


## Publication 0.15.10-dev

Fab a validé la version pour publication publique.

Publication effectuée :
- prerelease créée par CI #250 via [phone-release] ;
- tag : phone-0.15.10-dev-run-250 ;
- promotion en release publique normale par CI #251 via [phone-publish] ;
- titre final : GeckoDoku 0.15.10-dev • téléphone validé ;
- prerelease = false ;
- release publique confirmée sur GitHub ;
- APK : GeckoDoku-v0.15.10-dev.apk ;
- SHA-256 GitHub : 684a40dbdc8ab91a8ca4ccdb5d4d904907279b9ad9c05c35603ae0b253ac9b46.

La prochaine mission active reste le grand cycle autonome des mascottes.


## GECKO-051 — toutes les mascottes visibles doivent vivre

Observation téléphone Fab après publication 0.15.10-dev :
en mode Classic, les Gecko présents sur la grille restent statiques ; seuls la Plante et Pierre donnent une impression de vie.

Cause :
AliveMascotOverlayView limitait le nombre de mascottes vivantes au nombre de lecteurs vidéo (3 Gecko / 2 Abeilles / 1 Plante). Les pièces déjà présentes au chargement n'étaient en plus enregistrées qu'au fil des événements d'apparition.

Décision Fab :
- corriger POUR TOUS LES MODES ;
- toutes les mascottes visibles doivent appartenir à la classe vivante et posséder leur PNG interne ;
- le plafond 3 Gecko / 2 Abeilles / 1 Plante concerne uniquement les vidéos simultanées ;
- les lecteurs vidéo doivent tourner entre les mascottes visibles ;
- une mascotte qui n'a pas le lecteur reste un PNG interne vivant, pas un retour au vieux rendu du plateau ;
- Classique, Sudoku, Gomoku et Abeilles & Geckos doivent synchroniser les mascottes déjà présentes au chargement ;
- update() distribue les animations de manière autonome ;
- petites animations successives différentes ;
- séries complètes différentes ;
- fin de grand cycle : une autre mascotte visible avec animation cute reçoit une mignonnerie ;
- les autres repartent sur des cycles désynchronisés.

Version de travail : 0.15.11-dev / versionCode 46.
Aucune publication publique avant validation téléphone Fab.


État GECKO-051 :
- commit applicatif : 616b0dcdb808735ca0a396091aa2428ed5192034 ;
- version : 0.15.11-dev / versionCode 46 ;
- CI #252 entièrement verte ;
- tests + build Phone OK ;
- APK téléphone produit ;
- SHA-256 APK : 9d8de7bd06a5defcbef0523cbdae293f125c077f9a628bfdb0b37357cfbf709f ;
- aucune prerelease/release déclenchée ;
- validation téléphone Fab requise sur les 4 modes avant publication.


## GECKO-052 — diagnostic ALL ANIMATED + intros réellement au-dessus

Retour téléphone Fab sur 0.15.11-dev :
- la grille est visuellement propre ;
- les Gecko restent cependant statiques après un moment ;
- en pratique seuls Pierre et la Plante donnent encore une impression d'animation ;
- hypothèse à tester : le verrou/pool des lecteurs vidéo empêche la vie des mascottes.

Décision Fab :
RETIRER TEMPORAIREMENT TOUTE LIMITATION DE CONCURRENCE.
Toutes les mascottes visibles doivent pouvoir jouer leur vidéo simultanément afin d'isoler le rôle du verrou.

Implémentation diagnostic 0.15.12-dev :
- videoSlots devient dynamique ;
- un slot ChromaKeyVideoView est créé paresseusement pour chaque Presence visible ;
- aucune limite 3 Gecko / 2 Abeilles n'est appliquée dans ce build ;
- toutes les Presence éligibles peuvent donc être animées en même temps ;
- le mécanisme de grands cycles reste actif ;
- ce mode est volontairement diagnostic : performances/batterie ne sont pas encore le critère.

Correction jointe demandée par Fab :
les deux vidéos d'introduction doivent être ON TOP.
Pendant INTRO_FIRST / INTRO_SECOND :
- AliveMascotOverlayView arrête ses lecteurs actifs et devient invisible ;
- aucune mascotte ni Plante ne peut passer devant l'intro ;
- à la fin ou au skip de l'intro, les Presence reprennent leurs PNG et leurs cycles.

Version : 0.15.12-dev / versionCode 47.
Pas de release publique avant test téléphone.


État GECKO-052 :
- commit applicatif : f90f41a4d6c65d952538a1cbad266e7b2f0c0abd ;
- CI #253 entièrement verte ;
- tests + build Phone OK ;
- APK téléphone 0.15.12-dev produit ;
- SHA-256 APK : a50bffee172b944dfe1e40d977b6eccc5b3383722aed7c4ff1d2e643615fc1c3 ;
- aucune prerelease/release déclenchée ;
- verdict téléphone attendu sur ALL ANIMATED + intro on top.


## GECKO-053 — reprise après changement d'application

Observation téléphone Fab sur le build ALL ANIMATED :
- sans limite d'ordonnanceur, les mascottes s'animent correctement ;
- mais après bascule vers Mail / Messages / autre application puis retour, seuls Pierre et la Plante repartent ;
- les Gecko/Abeilles du plateau restent statiques.

Cause confirmée dans le cycle Android :
- onPause() appelle aliveMascotOverlay.stopAll() pour arrêter proprement les lecteurs en arrière-plan ;
- onResume() reconstruisait seulement la Plante avec ensurePlantMascot() ;
- le snapshot du mode courant n'était pas resynchronisé.

Décision / correction :
- conserver stopAll() dans onPause() pour ne laisser aucun MediaPlayer/GLSurface actif en arrière-plan ;
- dans le screenRoot.post de onResume(), appeler successivement :
  1. positionTitleIdentity()
  2. ensurePlantMascot()
  3. syncLivingMascotsForCurrentMode()
- la reconstruction couvre donc Classic, Sudoku, Gomoku et Abeilles & Geckos.

Version de test : 0.15.13-dev / versionCode 48.
Le mode ALL ANIMATED sans plafond reste actif pour ne pas mélanger les diagnostics.
Aucune release publique avant validation téléphone.


État GECKO-053 :
- commit applicatif : 173bf0ed9e4431b3e6efe13de44c4374cc055bcb ;
- CI #254 verte ;
- APK téléphone 0.15.13-dev produit ;
- SHA-256 APK : 4bee54cfdc1261adf75559635c8749ade82133a8383cc34e1ddbea4019d13937 ;
- ALL ANIMATED reste actif ;
- aucune prerelease/release ;
- validation téléphone attendue sur aller-retour Mail/Messages.


## GECKO-054 — architecture définitive : PLUS JAMAIS D’ORDONNANCEUR

Décision explicite Fab après tests téléphone :
« Plus jamais d’ordonnanceur ».

Règle canonique à partir de 0.15.14-dev :
- 1 mascotte visible = 1 Presence = 1 AliveAnimator = 1 ChromaKeyVideoView ;
- aucun pool partagé ;
- aucune capacité 3/2/1 ;
- aucun prêt ou rotation de lecteur ;
- chaque Presence gère seule son propre cycle ;
- la coordination de fin de grand cycle ne fait qu’envoyer un signal refresh/cute à une copine, sans posséder de lecteur ni de planning global.

Correctifs inclus :
- mascottes issues de la map initiale : une target encore indisponible au premier layout ne tue plus la Presence ; retry local jusqu’à géométrie valide ;
- refresh post-layout pour les quatre modes ;
- apparition Gecko stricte : aucun PNG avant la vraie vidéo d’apparition ; ordre vide → vidéo → vivant ;
- retour Mail/Messages : onPause libère, onResume reconstruit Plante + snapshot complet ;
- intros FIRST/SECOND restent au-dessus de toute la couche vivante.

Pierre :
- pipeline animation/parole inchangé ;
- RETURN passe de 10 à 300 phrases dédiées ;
- RETURN_AFTER_PAUSE choisit exclusivement RETURN ;
- cooldown exact 48 h par phrase ;
- historique persistant des 48 derniers IDs ;
- éviter les familles d’ouverture récemment utilisées ;
- éviter aussi les textes trop similaires aux derniers retours ;
- si les 300 sont épuisées, prendre la RETURN la plus ancienne.

Version : 0.15.14-dev / versionCode 49.
Aucune publication publique avant validation téléphone.


État GECKO-054 :
- commit architecture sans ordonnanceur : 302debfdc4fba4e9a9bdc483f0fbf7a19c5d8484 ;
- commit final mascottes + Pierre : ab82f3d338cef1a8338d70d41fb2e70267fdc90d ;
- CI #255 entièrement verte ;
- tests + build Phone OK ;
- APK 0.15.14-dev produit ;
- SHA-256 APK : 67cb5429d2ae188e8a5a58f09f43afa2032a96e43f1998f1265bafe96eed8abb ;
- aucune prerelease/release déclenchée ;
- validation téléphone Fab requise avant publication.


## GECKO-055 — retirer la vieille procédure de composition PNG/vidéo

Retour téléphone Fab sur 0.15.14-dev :
la logique fonctionne et peut déclarer la victoire, mais les pièces deviennent visuellement vides.

Décision Fab :
le concept Presence autonome est conservé, mais l'ancienne procédure de composition ne doit plus piloter PNG et vidéo comme deux systèmes concurrents.

Cause architecturale ciblée :
- 0.15.14-dev avait placé ImageView PNG et ChromaKeyVideoView dans le MÊME FrameLayout enfant ;
- ChromaKeyVideoView est un GLSurfaceView avec surface Android séparée et z-order on-top ;
- ce type de surface ne se compose pas comme un ImageView enfant ordinaire ;
- le PNG interne pouvait donc ne pas jouer correctement son rôle de fond/fallback dans ce conteneur commun.

Architecture corrigée 0.15.15-dev :
- chaque Presence possède toujours exactement SON PNG et SA vidéo ;
- mais le PNG vit dans pngContainer, vue Android normale ;
- la vidéo vit dans videoContainer, sibling séparé, comme dans l'architecture de surface qui avait déjà fonctionné ;
- aucun lecteur n'est partagé ;
- aucun pool ;
- aucun ordonnanceur ;
- les deux siblings utilisent toujours exactement la même target et la même taille.

Propriété visuelle :
- dès qu'une Presence a une target valide, le plateau cesse UNE FOIS de dessiner son ancien PNG ;
- ensuite le plateau ne participe plus aux transitions ;
- attente : PNG interne visible ;
- préparation vidéo : PNG interne reste visible ;
- première vraie frame : vidéo visible et PNG interne caché ;
- fin vidéo : videoContainer invisible et PNG interne visible immédiatement ;
- apparition réelle : PNG interne caché jusqu'à la vidéo d'apparition, puis cycle normal ;
- si aucune target n'est encore disponible, le plateau conserve son PNG historique.

Version : 0.15.15-dev / versionCode 50.
Aucune release publique avant validation téléphone.


État GECKO-055 :
- commit architecture : 4d6731523990ecc80810207259b2282757011862 ;
- correctif compilation listener : 135ec82888e8d01494f74bd56adcea86f5dae490 ;
- CI #257 entièrement verte ;
- tests + build Phone OK ;
- artifact GeckoDoku-v0.15.15-dev-phone produit ;
- APK SHA-256 : 671776186328f53df577599e45d2cb85ea8bc6a8ca8daf5d54bf0721c71dabf5 ;
- aucune prerelease/release ;
- validation téléphone Fab requise.


## GECKO-056 — CORRIGER LES ANIMATIONS GECKO ET ABEILLE QUI NE DÉMARRENT PAS

Validation Fab : correction autorisée.

Diagnostic :
- Gecko et Abeille utilisent le même pipeline AliveMascotOverlayView → ChromaKeyVideoView ;
- revealOnFirstFrame garde alpha=0 jusqu'à validation d'une frame fraîche ;
- FreshPlaybackFrameGate exigeait playerStarted au moment de onRenderingStart ;
- Android peut livrer MEDIA_INFO_VIDEO_RENDERING_START pendant MediaPlayer.start(), avant le retour local puis onPlayerStarted ;
- le signal pouvait donc être perdu pour toute la génération.

Correction :
- latcher rendering-start pour la génération courante sans dépendre de l'ordre des callbacks ;
- ne révéler une frame que lorsque playerStarted ET renderingStarted sont vrais ;
- ignorer strictement les anciennes générations ;
- conserver cancel() ;
- ajouter les tests ordre inversé / génération périmée / cancel ;
- ne réintroduire aucun ordonnanceur, pool ou lecteur partagé.

Version cible : 0.15.16-dev / versionCode 51.
Aucun merge main et aucune release avant validation téléphone.

État GECKO-056 : CI #260 entièrement verte sur a43ef730ec77475e08b3ab7c4b897a023977057c ; artifact GeckoDoku-v0.15.16-dev-phone produit ; aucune release ; validation téléphone Gecko + Abeilles requise.


## GECKO-057 — CYCLE LOCAL GECKO/ABEILLE QUI NE DOIT PLUS ÊTRE AFFAMÉ

Retour téléphone : 0.15.16-dev affiche encore des Gecko statiques / absents. Le journal montre que Pierre atteint normalement ChromaKeyVideoView.play(), alors que les Presence Gecko/Abeille sont construites sans aucun PLAY_REQUEST.

Cause corrigée :
- refreshPresenceTarget réécrivait des LayoutParams identiques ;
- refreshDynamicTargets pouvait alors reprogrammer une Presence pending ;
- schedulePresence annulait le callback précédent avant de le repousser ;
- le cycle pouvait ne jamais atteindre advancePresence/playDecision.

Règles 0.15.17-dev :
- une Presence n'a qu'un callback local armé à la fois ;
- un refresh de géométrie ne repousse jamais un callback déjà armé ;
- les LayoutParams ne sont remplacés que si la géométrie a réellement changé ;
- cancel/remove libèrent le flag local ;
- ALIVE_PLAY doit être visible dans le journal dès qu'une animation Gecko/Abeille part ;
- le dernier Gecko d'une partie Classic doit aussi être vivant avant completeGame.

Interdictions inchangées : aucun ordonnanceur global, aucun pool, aucun merge main, aucune release avant validation téléphone.

État GECKO-057 : CI #263 verte sur 352c046bcc6e1af6d26794782f14189054ddcae2 ; APK 0.15.17-dev produit ; SHA-256 cb50cb5e97ed9cc33ab7285982a6da7fda5e6439040fa2a195b7c59bb6fa8577 ; aucune release ; validation téléphone requise.


## GECKO-058 — CORRIGER LES GECKO DE BORD DANS ABEILLES & GECKOS

Validation Fab : correction autorisée.

Cause confirmée :
beeGeckoAliveTarget() retournait null dès que la cible animée dépassait le viewport. Les Gecko, plus grands, étaient donc éliminés sur les cellules périphériques alors que les Abeilles intérieures restaient animées.

Correction 0.15.18-dev :
- conserver null uniquement pour une cible totalement hors viewport ;
- sinon préserver taille et proportions ;
- translater la cible du minimum nécessaire pour la garder visible ;
- recentrer uniquement si elle est plus grande que le viewport ;
- journaliser BEE_GECKO_ALIVE_TARGET_CLAMPED ;
- ne modifier ni AliveAnimator ni la logique de puzzle.

Interdictions inchangées : aucun ordonnanceur/pool partagé, aucun merge main, aucune release avant validation téléphone.

État GECKO-058 : CI #265 verte sur 2c20b06d2a0f699c8811cdf7cd37871a4b712f45 ; APK 0.15.18-dev produit ; SHA-256 9e0ad087bc2f641ecd80010bb162cf57739ac0b0a236d2dc21cbe5d321311b77 ; aucune release ; validation téléphone requise.


## GECKO-059 — RETIRER LA CONTRAINTE VIEWPORT NON DEMANDÉE

Validation Fab : supprimer cette règle. Les assets Gecko/Abeille sont déjà centrés dans leur cadre transparent/key-color. beeGeckoAliveTarget() doit simplement calculer la cible centrée et la convertir vers screenRoot, sans rejet ni clamp viewport.

Version cible : 0.15.19-dev / versionCode 54. Aucun merge main ni release avant validation téléphone.

État GECKO-059 : CI #267 verte ; APK 0.15.19-dev produit ; aucune release ; validation téléphone requise.

## GECKO-060 — OWNERSHIP UNIQUE
Décision Fab : en mode Abeilles & Geckos, la Presence Alive remplace l'ancien rendu de pièce et possède son PNG fallback et ses vidéos. La règle hors écran est conservée sur la cellule du plateau : cellule hors viewport = mascotte cachée ; cadre média débordant = autorisé. Version 0.15.20-dev / 55. Pas de merge main ni release avant validation téléphone.


## GECKO-061 — GARDE HORS ÉCRAN APRÈS DRAG

Décision Fab : conserver la règle utile qui masque une mascotte quand le drag sort réellement sa cellule du plateau.

Implémentation :
- ownership unique Alive conservé ;
- visibilité décidée par le centre de la cellule logique ;
- centre dans viewport => Presence visible et libre de dépasser avec son cadre média transparent ;
- centre hors viewport => Presence cachée ;
- ne pas restaurer l'ancien renderer PNG BeeGeckoBoardView.

Version cible : 0.15.21-dev / versionCode 56.
Aucun merge main ni release avant validation téléphone.

État GECKO-061 : CI #275 verte ; APK 0.15.21-dev produit ; règle hors-écran basée sur le centre de cellule ; aucune release ; validation téléphone requise.


## GECKO-062 — GOMOKU 3 ANIMATIONS PAR ÉQUIPE

Décision Fab :
dans les deux variantes Gomoku (contre Prof/ordinateur et JcJ), ne pas animer toutes les pierres.

Contrat :
- maximum 3 Gecko verts vivants ;
- maximum 3 Gecko jaunes vivants ;
- choix aléatoire ;
- autres pierres en PNG statique ;
- le groupe reste stable pendant son cycle ;
- à la fin d'un grand cycle, redistribuer aléatoirement les 3 par équipe ;
- ne pas modifier les règles de jeu, IA, zoom/drag ou couleurs ;
- aucune release ni merge main avant validation téléphone.

Version cible : 0.15.22-dev / versionCode 57.
