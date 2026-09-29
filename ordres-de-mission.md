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

Fondation technique intégrée :
- AliveAnimator + MascotLifeCoordinator ;
- profils Gecko / Abeille / Plante ;
- couche AliveMascotOverlayView maintenant le masque entre deux clips pour supprimer le flash PNG ;
- mémoire anti-répétition ;
- réinterrogation générale après 4 attentes ou une mignonnerie ;
- nouveaux stay rangés sur la branche active ;
- PlanteTr.png fourni par Fab et intégré comme fallback PNG ;
- version de travail 0.15.7-dev / versionCode 42.

Intégration multi-mode effectuée :
- Classic : apparition / attente continue / mignonnerie / disparition ;
- Sudoku : repère Gecko via le même moteur ;
- Gomoku : dernier Gecko vivant, avec teinte jaune conservée pour le Prof ;
- Abeilles & Geckos : Gecko ET Abeille passent par le même moteur ;
- Plante : profil vivant décoratif en bas à droite, PNG PlanteTr si animations OFF ;
- géométrie vidéo calée sur la taille réelle des PNG de chaque mode ;
- le masque reste actif entre apparition et attente : pas de réapparition PNG intermédiaire ;
- changement de mode / nouvelle partie / pause nettoient les instances de plateau ;
- Pierre et les vidéos du Prof restent hors de cette architecture.

État : test téléphone 0.15.7-dev effectué. Fab observe parfois un bref vide entre deux vidéos. Diagnostic : ChromaKeyVideoView rend le nouveau clip transparent jusqu'à sa première frame après arrêt du clip précédent. Correctif 0.15.8-dev : le PNG transparent canonique, à géométrie identique, sert uniquement de pont entre deux clips déjà visibles puis disparaît sur la première frame du clip suivant. CI #247 verte ; revalidation téléphone Fab requise avant toute release publique.


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
- validation Fab requise.
