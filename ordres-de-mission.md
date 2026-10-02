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

État GECKO-062 : CI #280 verte ; APK 0.15.22-dev produit ; aucune release ; validation téléphone requise sur cap 3+3 et redistribution de fin de cycle.


## GECKO-063 — LOT CUMULÉ VALIDÉ PAR « GOGOGO »

1. GOMOKU
- maximum strict 3 Gecko verts + 3 jaunes vivants, contre Prof et JcJ ;
- aucune création directe par le vieux chemin GECKO_APPEARANCE ;
- redistribution uniquement par le cycle Alive ;
- les autres pierres restent statiques ;
- long/cute = action de la Presence sélectionnée, jamais un deuxième objet vidéo parallèle.

2. INTRO
- aucun plateau visible 1–2 s avant IntroGeckoGD ;
- rideau noir présent avant l'affichage de l'UI ;
- overlay Intro noir prend le relais de façon atomique.

3. CARRÉ NOIR CHROMA
- ne jamais dessiner la texture externe avant une frame fraîche du playback courant ;
- clear transparent jusque-là ;
- conserver la règle PNG visible -> frame vidéo fraîche/keyée -> bascule.

4. PLANTE
- PNG fallback -5 % (95 %) autour du même centre ;
- vidéo et target inchangées.

Version cible 0.15.23-dev / versionCode 58.
Aucun merge main ni release sans validation explicite de Fab.

## ÉTAT APRÈS TEST 0.15.23-dev
Validé téléphone : intro sans flash plateau, suppression du carré noir avant première frame, plante PNG ~95 %.
Restent ouverts : Gomoku et Abeilles & Geckos.
Pour Abeilles & Geckos, ne pas coder de nouveau correctif géométrique avant validation de Fab. Proposition : Presence conservée tant que sa cellule intersecte le viewport, target toujours centrée sur cellule, puis clipping strict du rendu au rectangle réel du plateau. Ne plus utiliser le centre de cellule comme frontière d'affichage.


## GECKO-064 — CLIPPING BEE/GECKO VALIDÉ PAR FAB
Contrat :
- cellule qui touche encore le viewport = Presence conservée ;
- mascotte toujours centrée sur sa cellule ;
- rendu PNG et vidéo clipé strictement à la fenêtre réelle du plateau ;
- aucune translation ou recentrage artificiel ;
- cellule totalement hors viewport = Presence cachée ;
- zoom/drag doit faire glisser progressivement la mascotte derrière les bords.

Version cible 0.15.24-dev / versionCode 59.
Aucun merge main ni release avant validation téléphone.

État GECKO-064 : CI #289 verte ; APK 0.15.24-dev produit ; validation téléphone du clipping Bee/Gecko requise ; aucune release ni merge main.


## GECKO-065 — PASSAGE PERMANENT DERRIÈRE LE CADRE DU PLATEAU

Validation Fab : GO.

BUT

Pour Abeilles & Geckos et Gomoku, les mascottes vivantes doivent rester réellement vivantes même lorsqu'un zoom ou un drag les déplace complètement derrière le cadre blanc du plateau.

CONTRAT

- La Presence existe tant que la pièce logique existe dans la partie.
- La position cible reste calculée hors viewport.
- La fenêtre du plateau sert uniquement de clip visuel.
- Une mascotte entièrement hors fenêtre reste animée mais invisible.
- Au retour dans la fenêtre, elle glisse naturellement depuis le bord ; aucun spawn/restart artificiel.
- Aucun clamp, aucun recentrage.
- Abeilles & Geckos : appliquer à Gecko et Abeille.
- Gomoku : appliquer uniquement aux Presence sélectionnées par la règle 3+3.
- Conserver au maximum 3 PLAYER + 3 PROFESSOR en Gomoku.
- Titres, textes, boutons, commandes et Prof ne doivent jamais être recouverts par les mascottes du plateau.
- La plante reste indépendante de cette fenêtre de plateau.
- Ne pas modifier les règles de jeu, solveurs, IA ou difficulté.

ARCHITECTURE

- BeeGeckoBoardView : géométrie de cellule non bornée.
- GomokuBoardView : géométrie Gecko non bornée + rectangle de viewport exporté.
- MainActivity : targets non bornées.
- AliveMascotOverlayView : clipProvider reste l'autorité de visibilité.
- ChromaKeyVideoView : politique Surface historique conservée.
- Aucun masque Surface global, aucun nouvel ordonnanceur, pool ou lecteur partagé.

VERSION LIVRÉE POUR TEST

0.15.26-dev / versionCode 61.

État : CI #303 verte, artifact Phone produit, SHA-256 APK 1b25121080454d58934c20366073ad0a255324226759fe3baf22f2d4b1617323. Aucune release/prerelease, aucun merge main. Validation téléphone Fab requise.


## GECKO-066 — TEST BACKEND TEXTUREVIEW POUR LE PLATEAU

Validation Fab : « Ok on test ».

OBJECTIF
Vérifier si la divergence temporelle PNG/vidéo vient du SurfaceView historique en faisant passer uniquement les mascottes des plateaux exploratoires dans une vraie TextureView Android.

PORTÉE STRICTE
- Abeilles & Geckos : Gecko + Abeille.
- Gomoku : seulement les Presence choisies par le cap 3+3.
- Android API >= 33 uniquement.
- Classic, Sudoku, Plante, Prof, Intro et autres médias restent sur le backend historique.
- SDK < 33 : fallback GLSurfaceView.

CONTRAT VISUEL
- PNG et vidéo doivent suivre le même target, le même clip et le même Z-order.
- Une vidéo ne doit jamais apparaître au-dessus des boutons, titres ou Prof hors fenêtre du plateau.
- Une Presence qui passe derrière un bord continue son cycle et revient sans recréation.
- Aucun carré noir/keycolor avant première frame.
- YellowTint Gomoku conservé.
- Bleu et vert keycolor conservés.

CONTRAT ARCHITECTURE
- ChromaKeyPlayback est l'interface commune.
- TextureView utilise MediaPlayer + RuntimeShader RenderEffect.
- Aucun ordonnanceur global, pool, lecteur partagé ou changement de règles.
- Aucun merge main ni release/prerelease avant validation téléphone.

Version test : 0.15.27-dev / versionCode 62.

État GECKO-066 : CI #310 verte ; APK 0.15.27-dev produit ; SHA-256 APK e5de07cea97ca736108d644298ab56b2ad16689d695e357ef0a1d4cd280afe3f ; validation téléphone Bee/Gecko + Gomoku requise ; aucune release/prerelease ni merge main.


## GECKO-067 — STABILISER KEYCOLOR ET JAUNE SUR TEXTUREVIEW

Validation Fab : GO.

CONSTAT
TextureView est la bonne piste pour le clipping et le Z-order, mais le keycolor et le filtre jaune du test 0.15.27-dev sont instables.

MISSION
Garder TextureView comme sortie visuelle Android, mais retirer RuntimeShader/RenderEffect et remettre le traitement chroma dans OpenGL, avec le shader historique déjà validé.

PORTÉE
- Abeilles & Geckos ;
- Gomoku 3+3 ;
- aucun changement Classic/Sudoku/Plante/Prof/Intro ;
- aucun changement de règles, solveurs, IA ou sélection 3+3.

ARCHITECTURE
- 1 TextureView de sortie par Presence ;
- 1 thread GL/EGL local par Presence ;
- 1 SurfaceTexture OES d'entrée pour MediaPlayer ;
- shader GLSL historique bleu/vert + despill + yellowTint ;
- sortie EGL dans TextureView ;
- first-frame gate après rendu GL frais et swap réussi.

INTERDIT
- RuntimeShader/RenderEffect pour ce backend ;
- ordonnanceur global ;
- pool ou lecteur partagé ;
- merge main / release avant validation téléphone.

Version cible : 0.15.28-dev / versionCode 63.

État GECKO-067 : CI #312 verte ; APK 0.15.28-dev produit ; SHA-256 APK 296240aa14d866f101ac921887ca30c46c1eebbde946d2f7dc5797ae70d1ce79 ; validation téléphone requise ; aucune release/prerelease ni merge main.


# GECKO-068 — TEST SPRITES RGBA / PERFORMANCE

STATUT : implémentation expérimentale, à valider sur téléphone.

OBJECTIFS :
1. Produire hors écran des sprites RGBA à 12 i/s depuis les MP4, chroma-key inclus.
2. Conserver le filtre jaune du Prof dynamique.
3. Ajouter 240p/360p/480p dans les réglages, 480p par défaut.
4. Rendre la limite 3 animations par camp optionnelle, OFF par défaut.
5. Mesurer FPS UI, temps de frame, mémoire, nombre d'animations simultanées et pic.
6. Ne pas fusionner main ni publier de release avant validation téléphone.


# GECKO-069 — CACHE CHAUD SPRITERGBA SANS NOUVEL ORDONNANCEUR

STATUT : implémentation autorisée par Fab le 2026-09-30, validation téléphone requise.

## Décision

Optimiser la création/lecture des sprites à partir du constat téléphone : 240p est visuellement suffisant et la lecture simultanée est fluide ; le coût principal reste la fabrication initiale des banques.

Contrat :
- conserver trois rôles logiques chauds : Gecko vert, Gecko jaune et Abeille ;
- Gecko vert + Gecko jaune partagent physiquement la même banque RGBA ; le jaune reste un filtre ColorMatrix au dessin ;
- conserver une banque Gecko et une banque Abeille épinglées en mémoire, sans créer de lecteurs/mascottes cachés ;
- différer le warmup après les premières demandes visibles afin que le jeu réel passe avant le chauffage du cache ;
- remplir ensuite progressivement uniquement les autres clips IDLE Gecko/Abeille ;
- ne plus préchauffer au démarrage apparition/disparition ni Gecko_actions_plusieurs.mp4 ;
- aucune modification d'AliveAnimator, aucune réintroduction d'ordonnanceur de mascottes, aucun pool de lecteurs vidéo ;
- changement de résolution invalide seulement les pins chauds de l'ancienne résolution ;
- journaliser PREWARM_ARMED / PREWARM_REQUEST / HOT_BANK_PINNED / PREWARM_IDLE_DONE.

Version test : 0.15.30-dev / versionCode 65.

Aucun merge main, aucune prerelease/release avant validation téléphone Fab.


État GECKO-069 : commit applicatif 522802e5712ee5005850a033577f5fc143d00bb8 ; CI #314 entièrement verte ; artifact GeckoDoku-v0.15.30-dev-phone produit ; digest artifact sha256:3245421691fdc4857ee7d1dee1978cb9ff198a2d21380fbece526630b6ca795b ; aucune prerelease/release ; validation téléphone Fab requise.


# GECKO-070 — QUICK-START SPRITERGBA + RÉSOLUTIONS SOUS 240P

STATUT : GO Fab, implémenté ; validation CI/téléphone requise.

CONTRAT
- Ajouter 120p et 180p au sélecteur existant.
- Ordre du sélecteur : 120p, 180p, 240p, 360p, 480p.
- 240p = valeur par défaut si aucune préférence n'est encore enregistrée.
- Une animation SpriteRGBA ne doit plus attendre la fabrication complète de sa banque.
- Dès 4 frames RGBA valides, autoriser l'animation.
- Pendant la fabrication restante, le préfixe peut boucler localement.
- Dès que la banque complète est prête, la même Presence adopte la séquence complète sans recréation.
- Appliquer au moteur SpriteRGBA générique, donc Gecko et Abeille.
- Conserver filtre jaune dynamique, keycolor, AliveAnimator local, warmup chaud et limite Gomoku optionnelle.
- Aucun ordonnanceur global, aucun pool vidéo, aucune modification solveur/gameplay.
- Aucun merge main ni release/prerelease sans validation explicite Fab.

Version cible : 0.15.31-dev / versionCode 66.


# GECKO-071 — STABLE_FRAME issue de stay1/frame 1

STATUT : GO Fab, implémenté ; validation CI et téléphone requise.

OBJECTIF
Supprimer le petit saut visuel PNG historique → animation et rendre Gecko/Abeille visibles immédiatement pendant la préparation SpriteRGBA.

CONTRAT CANONIQUE
- GECKO_STABLE_FRAME = frame 1 de gecko/alive/stay1.mp4 uniquement.
- BEE_STABLE_FRAME = frame 1 de abeille/alive/stay1.mp4 uniquement.
- Gecko jaune = exactement la même STABLE_FRAME Gecko physique, avec le tint jaune existant au rendu.
- Ne jamais chercher la STABLE_FRAME dans stay2/stay3/stay4, apparition ou disparition.
- La STABLE_FRAME est un Bitmap statique simple : aucune boucle, playhead, timer, lecteur vidéo ou instance SpriteRGBA active.
- Animations ON : STABLE_FRAME → SpriteRGBA dès QUICK_READY/first frame.
- Animations OFF : PNG historique.
- Conserver intégralement les PNG historiques, leur chargement et les structures existantes.
- La plante est hors mission ; Pierre, gameplay, probabilités idle, apparition/disparition restent inchangés.
- Résolutions restent 120p/180p/240p/360p/480p ; 240p reste le défaut neuf.

CACHE
- extraction indépendante d'une seule frame à t=0 depuis stay1 ;
- même scaling et même chroma-key que SpriteFrameCache ;
- cache mémoire + cache disque léger ;
- si frame-00000 SpriteRGBA existe déjà, elle peut être réutilisée directement ;
- aucun build de séquence complète n'est nécessaire pour obtenir la STABLE_FRAME.

LOGS
- STABLE_FRAME_REQUEST
- STABLE_FRAME_READY
- STABLE_FRAME_SHOW
- STABLE_FRAME_TO_SPRITE
- LEGACY_PNG_SHOW reason=ANIMATIONS_DISABLED

Version cible : 0.15.32-dev / versionCode 67.
Aucun merge main ni release/prerelease sans validation explicite Fab.


# GECKO-072 — SPRITE BANK FACTORY / CACHE PERSISTANT / QUALITÉ PROGRESSIVE / EXPORT

Date : 2026-09-30
Statut : implémenté sur branche active ; CI + validation téléphone en cours.
Version cible : 0.15.33-dev / versionCode 68.

## Contrat
- STABLE_FRAME reste la priorité absolue et est désormais canonique en 240p, frame 1 de stay1 pour Gecko/Abeille.
- La Sprite Factory ne couvre que le pipeline réellement SpriteRGBA : catalogue Gecko + Abeille. Prof/Pierre et Plante restent sur leurs backends vidéo actuels ; aucune conversion artificielle.
- Aucun ordonnanceur global de mascottes n’est ajouté. Le pool/scheduler appartient uniquement à la fabrique de banques.
- 3 workers maximum.
- 60p est interne au moteur, jamais ajouté au sélecteur utilisateur.
- 240p reste le défaut utilisateur et la cible normale du bootstrap.
- 360p/480p restent on-demand et passent après la préparation essentielle.
- Aucun merge main ni release/prerelease avant validation explicite Fab.

## Pipeline visible
Animations ON :
STABLE_FRAME 240p
→ banque 60p QUICK_READY
→ 120p
→ 240p
→ résolution utilisateur >240p seulement si demandée.

Le changement de banque conserve le temps logique de l’animation et ne repart pas volontairement de la frame zéro.
L’upscale basse résolution utilise le filtrage bitmap du rendu ; les fichiers natifs ne sont pas floutés.

## Catalogue canonique SpriteRGBA
12 assets connus :
- Gecko : stay1..4, apparition, disparition, action longue ;
- Abeille : stay1..4, apparition.

Chaque banque est produite en fond pour 60p, 120p, 240p.
Les demandes hors catalogue journalisent SPRITE_CATALOG_MISS.

## Scheduling
Priorités de la fabrique :
1. visible ;
2. bootstrap visible ;
3. upgrade visible ;
4. catalogue 60p ;
5. catalogue 120p ;
6. catalogue 240p ;
7. >240p secondaire.

Chaque job cède après un chunk de 4 frames. Les chunks sont réinsérés dans une PriorityBlockingQueue, ce qui permet à plusieurs assets visibles d’obtenir QUICK_READY avant qu’un seul asset soit entièrement terminé.
Une demande visible peut promouvoir un job catalogue déjà en attente ; les tâches de priorité précédente deviennent obsolètes via epoch de scheduling.
Le worker Android tourne en priorité BACKGROUND.

## Cache sûr
Racine persistante : filesDir/sprite-banks-v2.
État disque :
BUILDING → frames .tmp atomiques → digest banque → manifest.tmp → manifest READY.
Une banque interrompue peut reprendre depuis la suite contiguë de frames si building.json correspond toujours à la source et au générateur.
Le manifest READY contient notamment :
assetPath, SHA-256 source, keyColor, résolution, frameCount, FPS, frameDuration, dimensions, format, version générateur, paramètres chroma-key, SHA-256 banque, temps de génération.
Une banque obsolète/manifest incompatible est INVALID et reconstruite.

## Coalescence
Une seule BuildSession physique par asset × keycolor × résolution.
Les callbacks supplémentaires rejoignent la session existante.
La STABLE_FRAME conserve aussi sa coalescence dédiée.

## Ordre futur APK / local / génération
Le moteur tente :
1. assets/sprites/<bankKey>/manifest.json dans l’APK ;
2. banque persistante locale ;
3. session de construction déjà active ;
4. génération MP4.

Une banque embarquée valide est matérialisée dans le stockage persistant une fois afin de conserver le contrat actuel SpriteSequence<File>.

## Export développeur
Réglages → « 🧪 Exporter les banques sprites ».
Sortie ZIP :
- sprites/index.json ;
- sprites/sprite-factory-report.json ;
- toutes les banques READY + manifests + frames ;
- sprites/stable-frames/ ;
- sprites/export-manifest.json.

Le dossier sprites/ exporté est conçu pour être réintégré ultérieurement sous assets/sprites/.

## Reporting
Traces principales :
SPRITE_FACTORY_START
SPRITE_FACTORY_STABLE_DONE
SPRITE_BANK_BUILD_START / RESUME / READY
SPRITE_QUICK_READY
SPRITE_BANK_MEMORY_HIT / DISK_HIT / APK_HIT
SPRITE_BANK_COALESCED
SPRITE_BANK_INVALID
SPRITE_CATALOG_MISS
QUALITY_STAGE_REQUEST / QUALITY_STAGE_SWAP
SPRITE_FACTORY_STAGE_COMPLETE
SPRITE_FACTORY_FIRST_VISIBLE
SPRITE_FACTORY_REPORT.

Rapport JSON : progression stable/60/120/240, expected/missing, temps par étage, premier QUICK_READY, première animation visible, temps total, tailles disque, RAM pic, workers peak + temps occupé par worker, FPS UI min/moyen, hits, générations, coalescences, invalides et misses.

## Validation téléphone requise
- démarrage froid en Abeilles & Geckos ;
- STABLE_FRAME visible avant toute animation lourde ;
- 60p QUICK_READY puis 120p puis 240p sans redémarrage apparent de l’animation ;
- au plus 3 workers ;
- plusieurs assets visibles obtiennent leurs 4 frames sans attendre qu’un clip de 102/360 frames se termine ;
- après préparation complète et relance : DISK_HIT/APK_HIT, aucune reconstruction des banques READY ;
- export ZIP récupérable et exhaustif ;
- vérifier FPS et absence de jank pendant intro/Pierre/plateau.

# GECKODOKU — GECKO-040 — BANQUES SPRITES PAR RÉSOLUTION

## Décision canonique

Une banque logique correspond à une résolution. Les animations restent des sous-banques/fichiers indépendants à l'intérieur ; aucun fichier géant monolithique.

Résolutions connues : 60p interne puis résolutions configurables 120p, 180p, 240p, 360p, 480p. La génération progressive suit uniquement les étapes nécessaires à la cible. Exemple cible 480p : 60p > 120p > 240p > 480p.

Cadence canonique : 12 images/s.

## Réglages

Ajouter « Banques sprites / export ». Pour chaque banque afficher :
- résolution ;
- pourcentage réel calculé sur les frames générées / frames attendues ;
- taille disque ;
- état Vide / En cours / Complète / Erreur.

Le détail d'une banque montre aussi les frames et le nombre d'animations complètes. Chaque banque dispose de son propre export.

## Export

Un ZIP par résolution, structure :
`sprites/banks/<resolution>p/bank-manifest.json`
`sprites/banks/<resolution>p/<bankKey>/...`

Le manifeste global de résolution contient état, pourcentage, frames attendues/générées, taille, inventaire des assets et hashes disponibles. Les stable frames restent exportées sous `sprites/stable-frames/`.

Le loader APK doit reconnaître cette structure tout en conservant la compatibilité avec l'ancien chemin plat.

## Correctif cache READY

Après un hit disque ou APK validé, la session est terminée. Le worker doit sortir immédiatement et ne jamais tenter d'accéder à `session.metadata` pour lancer une génération inutile.

## ADDENDUM GECKO-040B — EXPORT GLOBAL OBLIGATOIREMENT COMPLET

Quand Fab choisit l'export global des banques sprites :
1. ne pas exporter immédiatement une banque incomplète ;
2. terminer/résumer toutes les banques canoniques 60p, 120p, 240p et 480p ;
3. afficher une barre de progression réelle basée sur les frames générées / attendues ;
4. conserver les banques déjà présentes et reprendre les partielles ;
5. seulement à 100 %, ouvrir le choix de destination et écrire un ZIP global ;
6. le ZIP global contient les quatre répertoires `sprites/banks/<resolution>p/` avec leurs manifests.

Le rechargement d'une banque READY depuis DISK/APK est terminal : aucune génération ni validation de metadata ne doit continuer après le hit.

# ADDENDUM GECKO-041 — BANQUES SPRITES 240P MAXIMUM

Décision canonique Fab :

Le système de banques sprites ne maintient désormais que trois niveaux :
- 60p ;
- 120p ;
- 240p.

240p est la résolution maximale du pipeline. Les variantes 180p, 360p et 480p sont retirées du sélecteur, du gestionnaire de banques, de la préparation et de l'export.

L'export global suit exclusivement :
`60p → 120p → 240p → ZIP`.

Les anciennes banques générées en 180p, 360p et 480p sont considérées obsolètes et peuvent être supprimées automatiquement du stockage de l'application.

L'ancien export 60/120/240/480 reste archivé dans la release GitHub `PackageSprites` et sert uniquement de sauvegarde historique.



# GECKO-043 — ORDRE DE MISSION 240P FINAL

Fab valide un runtime préfabriqué 240p uniquement. Supprimer les banques 60p/120p du produit, supprimer les MP4 Gecko/Abeille couverts par les banques, et rendre le loader indépendant de ces sources. Gecko/Abeille doivent utiliser SpriteRGBA dans tous les modes. Plante, Pierre/Prof et intros restent vidéo. Les séquences 240p sont préchargées dans le cache logique en arrière-plan ; le cache bitmap reste borné. Aucun merge main ni release avant validation téléphone.

# GECKO-044 — ORDRE DE MISSION HYPOTHÈSES RÉVERSIBLES

Pour GeckoDoku classique et Abeilles & Geckos, une hypothèse manuelle possède une identité, une couleur et un parent éventuel. La palette est jaune, vert, rouge, violet, bleu, orange. Les croix créées sous une hypothèse appartiennent à cette branche et reprennent sa couleur. Une sous-hypothèse devient enfant de la branche active.

Si une hypothèse mène à contradiction, elle et ses hypothèses descendantes passent visuellement en « sens interdit ». Si le joueur abandonne ou supprime une branche, toutes les marques qui lui appartiennent (croix, auras, hypothèses enfants, contradiction) sont annulées récursivement. Le retour à une étape de la frise conserve cette hypothèse et ses marques propres mais retire tous ses descendants, afin de permettre l’exploration d’une branche sœur.

La frise colorée doit apparaître sous le plateau uniquement dans ces deux modes. Aucun merge main ni release avant validation Fab.

GECKO-044 CI FINAL : run #397 SUCCESS sur commit `3f61e64c1ce5381710b2ece6ee0ccac570f26a9f`. Tests Kotlin + assemblePhone verts. Artifact `GeckoDoku-v0.15.39-dev-phone`, 219108667 octets, digest `sha256:f77cecba153d34bba27fd047309f9c77f138fd4e7f0d2beadfabb1a0f02f4882`. Les prerelease/release sont restées skipped. Le correctif final garantit qu’une croix préexistante hors hypothèse ne devient jamais rétroactivement fille/colorée d’une nouvelle branche. Validation téléphone reste à faire.

# GECKO-045 — ORDRE DE MISSION SUDOKU SIMPLIFIÉ

Le Sudoku doit proposer un unique pavé local, déplaçable, composé de quatre carrés : Choix, Candidats, Hypothèse, Prévisu. Les candidats sont des notes ; une hypothèse est une branche logique distincte portant une couleur et un lien parent/enfant. Le panneau Hypothèse montre la couleur de la prochaine branche avant même sa création.

Choisir une valeur ne modifie pas immédiatement la grille : la valeur passe en Prévisu, la croix de fermeture est cachée, puis l'utilisateur répond à « Êtes-vous sûr ? Oui / Non ». Oui valide par le moteur normal ; Non annule la prévisu sans modifier la grille.

Les hypothèses suivent le moteur GECKO-044 : couleur, aura, enfant, contradiction/sens-interdit, suppression récursive. Sudoku ne doit pas afficher H1/H2 en permanence. La frise GeckoDoku/Abeilles-Geckos conserve ses couleurs mais sans texte H1/H2 afin de maximiser la place. Aucun merge main ni release avant validation Fab.

GECKO-045 CI FINAL : run #398 SUCCESS sur commit `63e3aab45628c0e0ba9311ae3e521ded03973aa2`. Tests Kotlin + assemblePhone verts. Artifact `GeckoDoku-v0.15.40-dev-phone`, 219118896 octets, digest `sha256:7336ff3419c30de1f7f1ba11d10aa2e35472c8714a3ab3a76c5d5b79e5db3f4f`. Prerelease et release sont restées skipped. Validation téléphone requise pour drag du pavé, confirmation Oui/Non, indépendance candidats/hypothèses et rollback parent/enfant Sudoku.

# GECKO-046 — ORDRE DE MISSION STATS

Les statistiques doivent suivre les performances significatives, pas les simples lancements. Une partie terminée est enregistrée. Une partie annulée sans erreur ne laisse aucune trace statistique. Une partie annulée avec au moins une erreur est conservée avec son nombre d'erreurs. Les statistiques sont ventilées par difficulté et leur tendance temporelle compare les deux dernières parties terminées pour le temps et les étoiles.

Au démarrage, Prof Gecko ne lit plus de statistiques globales : il évoque au maximum le niveau le plus difficile disposant de données et le niveau immédiatement précédent, en signalant uniquement les tendances de progression utiles (plus rapide, réussites plus étoilées).

Le menu Stats rend chaque niveau cliquable et affiche un graphe temporel. Le Hall of Fame est navigable par mode et niveau et réutilise le même graphe. Le pavé Sudoku 2×2 est déplaçable et redimensionnable. Aucun merge main ni release avant validation Fab.

GECKO-046 CI FINAL : run #399 SUCCESS sur commit `2f6ec3c75118d15c8492397098e88670e2d09bf5`. Tests Kotlin + assemblePhone verts. Artifact `GeckoDoku-v0.15.41-dev-phone`, 219132939 octets, digest `sha256:9532cb2ca9b7baa877a298cf2090d87c86aacf18ee0d906959cefef89d0e7bac`. Prerelease et release sont restées skipped. Validation téléphone requise pour le redimensionnement du pavé, les règles d'annulation/statistiques, les tendances Prof et la navigation Stats/Hall of Fame.

# GECKO-047 — ORDRE DE MISSION PAVÉ SUDOKU

Le pavé Sudoku doit être un outil flottant persistant de saisie rapide. Une fois ouvert, il reste visible jusqu'à fermeture explicite par sa croix ou changement de contexte. Toucher une autre case du plateau ne le ferme pas : il devient immédiatement le pavé de cette nouvelle case. Une validation Oui ne ferme pas le pavé ; Non annule seulement la prévisualisation.

Simple clic et double clic sur une case ouvrent ou reciblent le pavé. L'appui long conserve l'accès aux repères personnels.

Le bouton ? du pavé active une aide interactive. En mode aide, toucher une des zones Choix, Candidats, Hypothèse ou Prévisu déclenche une explication approfondie de Prof Gecko. Cette documentation n'est pas une aide de résolution et ne doit pas modifier les étoiles ni les points d'assistance.

Le pavé reste déplaçable et redimensionnable. Aucun merge main ni release avant validation Fab.

GECKO-047 CI FINAL : run #402 SUCCESS sur commit `7425eadb8385226075d0e5b5d224c0dcc72bf8aa`. Tests Kotlin + assemblePhone verts. Artifact `GeckoDoku-v0.15.42-dev-phone`, 219134167 octets, digest `sha256:c23ba189f1e7879e9e3d2389d0470fdd72bc87ed4b4e0b958400cf406e961a9b`. Prerelease et release sont restées skipped. Le PopupWindow Sudoku est non modal au toucher sur Android Q+ afin que les touches hors pavé traversent vers la grille pendant que le pavé reste visible. Validation téléphone requise pour confirmer le passage tactile réel sur Samsung/Android 16.

# GECKO-048 — ORDRE DE MISSION COMPACT + RELEASE

Le pavé Sudoku doit pouvoir être réduit au moins deux fois davantage que GECKO-047 sans blocage prématuré. Minimum : 140×160 dp ; bandeau minimum : 14 dp. Toutes les fonctions persistantes, drag, resize, aide ?, hypothèses, candidats, prévisualisation et passage tactile vers la grille restent actives.

Fab autorise explicitement la production de la release téléphone de cette version après CI verte. Aucun merge main.

GECKO-048 RELEASE FINALE : CI #403 SUCCESS sur commit `10da3008613c26973d7598211967ba64a0e18601`. Release finale publiée et promue : `phone-0.15.43-dev-run-403`, titre `GeckoDoku 0.15.43-dev • téléphone validé`. APK public : `GeckoDoku-v0.15.43-dev.apk`, 299414401 octets. Artifact CI : `GeckoDoku-v0.15.43-dev-phone`, digest `sha256:c11afe59f5ab784256edb2a39ad76dcf9b0c0d0ed93f4679bcece7c57847b661`. Aucun merge main.
