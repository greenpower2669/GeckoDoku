# GECKODOKU — ORDRE DE MISSION ACTIF
## GECKO-039 — CLASSIC → SUDOKU → GOMOKU

Branche impérative :
`gecko-039-sudoku-tap-gecko-gomoku`

HEAD documentaire de restructuration :
`3b86582010d3693bd21deb05d9e2d676d981dc60`

## RÈGLES GÉNÉRALES

- travailler uniquement sur cette branche ;
- ne pas repartir de `main` ;
- ne pas fusionner `main` ;
- aucune release sans GO explicite de Fab ;
- TDD : RED avant correction structurante, puis GREEN ;
- synchroniser `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md`, `ordres-de-mission.md` à chaque intervention ;
- téléphone Fab = autorité finale ;
- chaque mode doit rester compartimenté afin de pouvoir être testé séparément ;
- ne pas casser un mode déjà validé en travaillant sur le suivant.

# ORDRE IMPÉRATIF D'EXÉCUTION

```text
PHASE 1 — GECKODOKU CLASSIC
        ↓ validation / candidate testable
PHASE 2 — SUDOKU
        ↓ validation / candidate testable
PHASE 3 — GOMOKU
        ↓ validation / candidate testable
```

Ne pas commencer la phase suivante tant que la précédente n'est pas techniquement GREEN et testable.

---

# PHASE 1 — MODE GECKODOKU CLASSIC

## 1.1 — GRILLE CLASSIQUE PLUS LARGE

### OBJECTIF

Agrandir la grille du mode GeckoDoku historique afin qu'elle utilise davantage la largeur utile du téléphone.

La grille doit devenir visiblement plus grande et plus confortable sans :
- chevaucher les contrôles ;
- masquer Prof Gecko ;
- casser les bulles ;
- réduire les cibles tactiles ;
- déformer la géométrie historique ;
- casser les tailles de grille existantes.

### PRINCIPE

Comme pour le Sudoku, la grille a priorité sur les commandes secondaires.

Objectif :
- largeur utile maximale raisonnable ;
- marges horizontales minimales ;
- aucune marge décorative inutile ;
- conserver un plateau parfaitement lisible.

La valeur exacte de la marge doit être déterminée après audit du layout historique et validée sur téléphone.

### NON-RÉGRESSION

Conserver :
- régions ;
- cellules ;
- gestes ;
- croix ;
- hypothèses ;
- repères personnels ;
- Prof ;
- animations ;
- accessibilité.

## 1.2 — AUDIT DU SYSTÈME D'ANIMATIONS CLASSIQUE

Le mode Classic devient la **référence canonique d'animation Gecko**.

Auditer réellement :
- déclenchement des animations ;
- sélection aléatoire ;
- apparition ;
- disparition ;
- actions longues ;
- règles de priorité ;
- coexistence avec le plateau ;
- respect de `Animations ON/OFF`.

Assets/références connus à inspecter :
- `assets/gecko/Gecko_apparition.mp4`
- `assets/gecko/Gecko_disparition.mp4`
- `assets/gecko/Gecko_actions_plusieurs.mp4`
- autres médias Gecko effectivement utilisés par le moteur historique.

### BUT

Éviter trois architectures séparées.

Créer ou dégager, si nécessaire, une couche commune réutilisable par :
- Classic ;
- Sudoku ;
- Gomoku.

Cette mutualisation doit préserver le comportement historique avant de l'étendre.

## 1.3 — REPÈRES PERSONNELS CLASSIQUES

Auditer le clavier / la palette des repères personnels déjà existants dans le mode Classic.

Documenter :
- données manipulées ;
- gestes d'ouverture ;
- modèle de stockage ;
- rendu ;
- Undo/Redo éventuel ;
- distinction entre repère visuel et logique de jeu.

Le Sudoku devra réutiliser ce système autant que possible au lieu d'en recréer un autre.

## 1.4 — JALON PHASE 1

Avant Sudoku :
- tests Classic GREEN ;
- grille Classic plus large ;
- animations historiques toujours fonctionnelles ;
- repères personnels non régressés ;
- APK/AAB candidate ;
- test téléphone Fab.

---

# PHASE 2 — MODE SUDOKU

## 2.1 — ÉTAT DÉJÀ VALIDÉ / À CONSERVER

Déjà en place :
- grille Sudoku quasi pleine largeur ;
- mini-candidats noirs ;
- tap simple case vide → Gecko-repère ;
- retap simple → retire le Gecko-repère ;
- case donnée/remplie protégée ;
- appui long → palette locale Sudoku ;
- Undo/Redo du Gecko-repère ;
- Prof : premier tap explique ;
- second tap joue la même déduction encore valide ;
- long press Prof joue directement une déduction sûre ;
- provenance `PROFESSOR` ;
- suppression des préfixes redondants `Prof Gecko :` / `Prof Gecko •`.

Ne pas refaire ces fonctions.

## 2.2 — GECKO-REPÈRE : UTILISER LES ANIMATIONS CLASSIQUES

### PROBLÈME

Le Gecko-repère Sudoku possède actuellement une animation procédurale locale.

Fab demande les **vraies animations du mode Classic**.

### OBJECTIF

Brancher le Gecko-repère Sudoku sur le pipeline d'animations mutualisé issu de la phase 1.

Comportement souhaité :
- pose → apparition mignonne ;
- retrait → disparition mignonne ;
- petites actions aléatoires ;
- personnalité visuelle identique au Gecko historique ;
- aucune animation mécanique uniforme.

### CONTRAINTES

- candidats noirs toujours lisibles ;
- Gecko en arrière-plan / filigrane si nécessaire ;
- tactile jamais bloqué ;
- animation courte ou non intrusive ;
- `Animations OFF` = Gecko visible mais statique ;
- animation procédurale actuelle seulement comme fallback si nécessaire.

## 2.3 — TITRE SUDOKU : NETTOYAGE MINEUR

Actuellement :
`GeckoDoku · Sudoku 🦎`

Cible :
`GeckoDoku 🦎`

Ne modifier que le titre principal.

Conserver :
- `Sudoku 9×9 • Facile • ...` ;
- indication de mode sous le titre ;
- Gecko/icône ;
- fonctionnement Sudoku.

### INVARIANT MULTI-MODE

```text
TITRE PRINCIPAL = GeckoDoku 🦎
MODE ACTIF = zone dédiée sous le titre
```

Auditer Classic et futur Gomoku pour ne jamais ajouter leur nom au titre principal.

## 2.4 — DOUBLE TAP = CLAVIER DES REPÈRES PERSONNELS

En Sudoku :

```text
TAP SIMPLE case vide
→ toggle Gecko-repère

DOUBLE TAP case
→ clavier / palette des repères personnels

APPUI LONG case
→ palette Sudoku
   - valeur 1..9
   - candidats 1..9
   - effacer
```

### IMPORTANT

- double tap ≠ deux taps simples ;
- ne pas poser puis retirer instantanément le Gecko ;
- ne pas saisir de valeur automatiquement ;
- réutiliser le système de repères personnels audité en phase 1 ;
- repères personnels purement visuels ;
- aucune influence sur `SudokuSolver` ;
- aucune influence sur `SudokuHintEngine`.

## 2.5 — PROF GECKO : RAISONNEMENT VISUEL ET DÉTAILLÉ

### PROBLÈME

Une phrase du type :

`Dans cette ligne, le 6 ne peut aller qu'à cet endroit.`

donne la conclusion mais pas le raisonnement.

### PRINCIPE

Prof Gecko est un professeur.

Son rôle n'est pas :
`Voici la réponse.`

Son rôle est :
`Je vais te montrer pourquoi cette réponse est forcément correcte.`

## 2.6 — S'INSPIRER DU MODE CLASSIC

Auditer la façon dont le Prof Classic :
- met en évidence des sources ;
- projette une logique ;
- montre des hypothèses ;
- garde un état visuel ;
- synchronise interaction et explication.

Réutiliser les composants/patterns pertinents au lieu de dupliquer inutilement la pédagogie.

Ne pas forcer une réutilisation si la logique Sudoku exige un composant spécifique.

## 2.7 — TRACE DE RAISONNEMENT UNIQUE

Créer un modèle pur, nom libre, équivalent à :

`SudokuReasoningTrace`

contenant au minimum :
- technique Sudoku ;
- valeur recherchée ;
- cellules/chiffres sources ;
- projections utilisées ;
- candidates initiales ;
- cases/candidats éliminés ;
- raison de chaque élimination ;
- case finale ;
- ordre des étapes.

### RÈGLE ABSOLUE

Cette même trace produit :
1. le graphique ;
2. le texte ;
3. éventuellement le texte vocal.

Interdit :
- calculer la solution ;
- puis inventer après coup une phrase générique.

## 2.8 — PROJECTION GRAPHIQUE

Étape par étape :
1. mettre en évidence le chiffre source ;
2. dessiner sa projection réelle :
   - ligne ;
   - colonne ;
   - bloc 3×3 ;
3. montrer les cases rendues impossibles ;
4. afficher la contrainte suivante ;
5. barrer/atténuer les possibilités éliminées ;
6. mettre en évidence la dernière case possible.

Exemple :

```text
6 source
   ↓
projection ─────────────── X X X

autre 6
   ↓
projection │
           X
           X

dernière case
     ↓
    [ 6 ]
```

## 2.9 — SYNCHRONISATION TEXTE / GRAPHISME

Exemple de séquence :

### Étape 1
Graphique :
premier 6 + projection.

Pierre :
`Ce 6 interdit toute cette ligne.`

### Étape 2
Graphique :
deuxième contrainte.

Pierre :
`Celui-ci élimine aussi cette possibilité.`

### Étape 3
Graphique :
possibilités supprimées.

Pierre :
`Il ne reste donc plus qu'une case possible.`

### Étape 4
Graphique :
case finale.

Pierre :
`Voilà ! Le 6 doit être ici.`

Le contenu exact dépend de la grille réelle.

## 2.10 — LAYOUT EXPLICATION PROF

Grand écran :
```text
[ projection / grille ] [ texte détaillé ]
```

Petit Android :
```text
[ projection ]
[ explication ]
```

Ne jamais rendre la projection trop petite pour comprendre la logique.

## 2.11 — ACCESSIBILITÉ PROF

Ne jamais encoder les projections par couleur uniquement.

Utiliser :
- traits ;
- flèches ;
- croix ;
- hachures ;
- contours ;
- variation de luminosité ;
- surbrillance ;
- animation discrète.

La case finale doit être immédiatement identifiable.

## 2.12 — TECHNIQUES À TRAITER D'ABORD

Commencer par les techniques déjà implémentées :
- naked single ;
- hidden single ligne ;
- hidden single colonne ;
- hidden single bloc.

Chaque technique doit produire sa vraie trace.

## 2.13 — INVARIANT DIALOGUE

La bulle a déjà un en-tête `Prof Gecko`.

Le corps :
- ne commence jamais par `Prof Gecko :` ;
- ne commence jamais par `Prof Gecko •`.

Le status :
- technique/action uniquement ;
- jamais une signature du locuteur.

## 2.14 — JALON PHASE 2

Avant Gomoku :
- titre corrigé ;
- animation Gecko-repère réutilise le Classic ;
- double tap repères personnels ;
- Prof détaillé et synchronisé ;
- tests texte/graphique ;
- accessibilité ;
- CI GREEN ;
- APK/AAB ;
- test téléphone Fab.

---

# PHASE 3 — TROISIÈME MODE : GOMOKU CONTRE PROF GECKO

## 3.1 — STATUT

Le Gomoku fait désormais partie du **même cycle de développement GECKO-039**.

Il doit commencer seulement après le jalon Sudoku GREEN/testable.

## 3.2 — PHILOSOPHIE

Le joueur affronte directement Pierre.

Il ne doit pas avoir l'impression de jouer contre une IA abstraite.

Il joue contre Prof Gecko :
- il réfléchit ;
- bloque ;
- attaque ;
- plaisante ;
- explique parfois ses choix.

## 3.3 — PLATEAU LOGIQUE + VIEWPORT 12×12

### EXIGENCE PRINCIPALE

Ne pas miniaturiser tout le plateau pour le faire tenir à l'écran.

Afficher une **fenêtre visible de 12 × 12 positions**.

Le plateau logique peut être plus grand que 12×12.

Architecture :
```text
PLATEAU LOGIQUE PLUS GRAND
        ↓
VIEWPORT MOBILE 12 × 12
        ↓
DRAG / PAN
        ↓
exploration des autres zones du plateau
```

### DRAG / PAN

Le joueur doit pouvoir :
- poser le doigt sur le plateau ;
- faire glisser ;
- déplacer la fenêtre visible ;
- explorer naturellement le plateau.

Le déplacement :
- ne doit pas poser accidentellement un Gecko ;
- doit avoir un seuil de drag clair ;
- doit rester fluide ;
- doit être borné aux limites du plateau ;
- doit conserver la taille lisible des intersections/cases.

### TAP VS DRAG

```text
TAP court sans déplacement significatif
→ jouer sur la position

DRAG dépassant le seuil
→ déplacer le viewport
→ aucun pion posé
```

### TAILLE DU PLATEAU LOGIQUE

Ne pas lier le moteur obligatoirement à 19×19.

Prévoir une taille paramétrable.

Une taille plus grande que le viewport 12×12 est obligatoire pour que l'exploration par drag ait un sens.

La taille initiale exacte sera choisie lors de l'implémentation après test de jouabilité, sans casser l'architecture.

## 3.3A — ZOOM IN / ZOOM OUT

Le viewport 12×12 est le **niveau de zoom de référence**, pas une limite fixe d'affichage.

Le joueur doit pouvoir zoomer par geste tactile, de préférence pinch-to-zoom.

### ZOOM-IN

En zoomant :
- moins de positions logiques sont visibles ;
- les cases/intersections deviennent plus grandes ;
- la précision tactile augmente ;
- idéal pour les personnes ayant besoin d'un affichage agrandi.

### ZOOM-OUT

En dézoomant :
- davantage de positions du plateau logique deviennent visibles ;
- le joueur peut mieux lire les menaces globales ;
- la taille minimale doit rester raisonnablement lisible.

### CONTRAT GESTUEL

```text
TAP court
→ jouer un Gecko

DRAG
→ déplacer le viewport

PINCH
→ zoom in / zoom out

PINCH + translation naturelle
→ conserver autant que possible le point du plateau situé entre les doigts
```

Le zoom ne doit jamais poser de pion accidentellement.

### BORNES

Définir :
- un zoom minimum ;
- un zoom maximum ;
- un zoom par défaut correspondant à environ 12×12 positions visibles.

Ne pas autoriser :
- un zoom-out rendant les Geckos inutilisables ;
- un zoom-in où quelques intersections géantes rendent la navigation incompréhensible.

### STABILITÉ SPATIALE

Pendant un zoom :
- conserver le centre logique sous les doigts autant que possible ;
- éviter les sauts de viewport ;
- borner correctement aux bords du plateau ;
- préserver la position logique des pions.

Le zoom est une propriété de la **vue**, jamais de l'état logique du Gomoku.

### ACCESSIBILITÉ

Le zoom manuel complète le viewport 12×12 :
- déficience visuelle → zoom-in ;
- vision tactique globale → zoom-out.

La logique IA et la détection de victoire restent totalement indépendantes du niveau de zoom.

## 3.4 — APPARENCE TYPE PLATEAU DE GO

Le rendu doit évoquer un plateau de Go/Gomoku :
- grille régulière ;
- intersections ou positions très lisibles ;
- contraste fort ;
- repérage spatial stable pendant le pan.

Les Geckos remplacent les pierres.

## 3.5 — PIONS

JOUEUR :
- Gecko vert original.

PROF :
- même sprite Gecko ;
- filtre jaune dynamique.

AUCUN asset Gecko jaune supplémentaire.

## 3.6 — FILTRE JAUNE

À partir du Gecko vert :
- R fortement augmenté ;
- G conservé fort ;
- B réduit ;
- alpha intact ;
- détails intactes ;
- yeux, ombres, relief conservés ;
- silhouette inchangée.

Ne pas faire un aplat jaune.

## 3.7 — ANIMATIONS

Le Gomoku doit réutiliser le pipeline commun issu du Classic.

Placement joueur :
- apparition courte / rebond Gecko.

Placement Prof :
- apparition courte adaptée au Gecko jaune.

Actions aléatoires éventuelles :
- discrètes ;
- jamais au détriment du rythme.

`Animations OFF` :
- pions visibles ;
- pas d'animation.

## 3.8 — TOUR JOUEUR

Tap valide sur position libre :
- poser Gecko vert ;
- vérifier immédiatement victoire.

Position occupée :
- aucun pion ajouté ;
- remarque contextuelle possible.

Exemples :
- `Petit problème… je suis déjà là.`
- `Tu veux mettre deux Geckos dans la même case ? Ambitieux.`
- `Cette place est prise, jeune lézard.`

Une position libre reste toujours légalement jouable.

## 3.9 — TOUR PROF

Séquence :
1. joueur joue ;
2. interaction Prof disponible ;
3. joueur demande à Pierre de jouer ;
4. IA analyse le plateau logique complet, pas seulement le viewport ;
5. Prof choisit une position ;
6. si nécessaire, le viewport peut se recentrer / guider vers son coup ;
7. Gecko jaune posé ;
8. commentaire éventuel ;
9. retour joueur.

Le moteur ne doit jamais dépendre de ce qui est actuellement visible à l'écran pour calculer.

## 3.10 — IA INITIALE

Priorités :
1. gagner immédiatement ;
2. bloquer victoire immédiate joueur ;
3. créer ligne de 4 menaçante ;
4. bloquer ligne de 4 ;
5. créer / bloquer lignes de 3 ;
6. privilégier zones proches des pions existants ;
7. aléatoire léger entre coups de score similaire.

Commencer déterministe et explicable.

## 3.10A — DIFFICULTÉ = PROFONDEUR STRATÉGIQUE

La difficulté du Gomoku ne doit pas être un simple coefficient arbitraire.

Elle représente principalement **jusqu'où Pierre projette les conséquences futures d'un coup**.

Principe :

```text
DIFFICULTÉ BASSE
→ réaction locale / immédiate

DIFFICULTÉ PLUS ÉLEVÉE
→ projection sur plusieurs réponses possibles

DIFFICULTÉ FORTE
→ préparation de menaces en plusieurs temps

DIFFICULTÉ TRÈS FORTE
→ pièges, doubles menaces, sacrifices tactiques et contre-pièges
```

### NIVEAU 1 — LECTURE IMMÉDIATE

Pierre regarde surtout :
- gagner au prochain coup ;
- bloquer une victoire immédiate ;
- créer une menace simple ;
- proximité des pions.

Horizon stratégique court.

Il peut laisser passer des plans à plusieurs coups.

### NIVEAU 2 — PROJECTION COURTE

Pierre anticipe plusieurs échanges probables.

Il reconnaît mieux :
- ligne de 4 future ;
- blocage préparatoire ;
- intersections créant plusieurs possibilités ;
- réponses naturelles du joueur.

Il commence à jouer pour **préparer le coup suivant**, pas seulement pour répondre au dernier.

### NIVEAU 3 — STRATÉGIE PROJETÉE

Pierre étudie des séquences plus longues et compare plusieurs branches.

Il peut :
- créer une menace qui force une réponse ;
- exploiter la réponse forcée pour construire une deuxième menace ;
- éviter de bloquer naïvement s'il existe une défense plus active ;
- préparer des doubles menaces.

Le joueur doit commencer à penser plusieurs coups à l'avance.

### NIVEAU 4 — PIÈGES ET CONTRE-PIÈGES

Pierre projette encore plus loin.

Il peut construire de vrais pièges tactiques :
- coup d'appât ;
- menace volontairement visible cachant une seconde menace ;
- double menace ;
- séquence de réponses forcées ;
- sacrifice local pour obtenir un alignement ailleurs ;
- contre-piège si le joueur prépare lui-même une séquence.

IMPORTANT :
un « piège » doit provenir de l'analyse réelle du plateau.

Ne jamais simuler l'intelligence avec un coup arbitraire ou une triche sur les règles.

### HORIZON / BUDGET DE RECHERCHE

L'architecture IA doit permettre d'associer la difficulté à :
- profondeur de recherche ;
- nombre de branches évaluées ;
- qualité de l'évaluation positionnelle ;
- reconnaissance de motifs tactiques ;
- éventuelle réduction ou augmentation de l'aléatoire.

Le niveau le plus faible peut utiliser essentiellement les priorités locales.

Les niveaux élevés peuvent employer :
- recherche multi-coups ;
- minimax / negamax ou architecture équivalente ;
- alpha-beta si pertinent ;
- heuristiques de menaces ;
- motifs Gomoku.

Ne pas choisir l'algorithme final avant audit/performance Android.

### PAS DE TRICHE

Quel que soit le niveau :
- Pierre ne voit aucune information cachée inexistante ;
- mêmes règles que le joueur ;
- aucune case illégale ;
- pas de bonus artificiel.

La difficulté vient uniquement d'une meilleure anticipation.

### EXPLICABILITÉ

Même à haut niveau, Pierre doit pouvoir expliquer certains plans.

Exemples :
- `Je t'oblige à répondre ici, parce qu'après je pourrai attaquer de l'autre côté.`
- `Cette menace n'était pas la vraie. Je préparais surtout cette intersection.`
- `Si je bloque directement, tu gagnes ailleurs. Je dois couper la séquence plus tôt.`

Les explications doivent être dérivées autant que possible de la vraie analyse IA.

### MAPPING AUX NIVEAUX DE L'APPLICATION

Au moment du code, mapper les niveaux de difficulté existants de GeckoDoku à ces quatre comportements stratégiques sans multiplier inutilement les systèmes de difficulté.

Si l'application n'expose pas exactement quatre niveaux, conserver le principe :
**plus difficile = horizon plus long + meilleure reconnaissance de pièges + meilleure sélection entre branches.**

## 3.11 — PERSONNALITÉ DU PROF

Réutiliser :
- mémoire anti-répétition ;
- humeur ;
- phrases contextuelles ;
- bulle ;
- voix Pierre ;
- invariant sans préfixe redondant.

Pas de phrase après chaque coup.

Événements intéressants :
- blocage critique ;
- menace ;
- victoire ;
- danger ;
- case occupée ;
- explication pédagogique.

## 3.12 — PROF = ADVERSAIRE + PROFESSEUR

Pierre peut expliquer :
- pourquoi il a bloqué ;
- pourquoi une ligne est dangereuse ;
- pourquoi une intersection est forte ;
- comment repérer une double menace.

Le système pédagogique peut réutiliser les principes de trace/visualisation du Sudoku lorsque pertinent, sans mélanger les moteurs.

## 3.13 — VALIDATION DES COUPS

Toutes les positions libres sont légales.

Ne jamais dire qu'un coup libre est `incorrect`.

Il peut être :
- excellent ;
- moyen ;
- faible ;
- dangereux ;

mais légal.

## 3.14 — VICTOIRE

Après chaque coup, rechercher au moins 5 Geckos identiques :
- horizontal ;
- vertical ;
- diagonale descendante ;
- diagonale montante.

Victoire :
- verrouiller nouveaux coups ;
- mettre en évidence les Geckos gagnants ;
- réaction du Prof ;
- proposer Rejouer.

Égalité possible si plateau plein.

## 3.15 — VIEWPORT ET VICTOIRE

Un alignement gagnant peut traverser une zone actuellement hors écran.

Le moteur vérifie donc toujours le plateau logique complet.

En cas de victoire hors viewport courant :
- recentrer intelligemment sur l'alignement gagnant ;
- afficher les 5 Geckos concernés ;
- ne jamais forcer le joueur à les chercher manuellement.

## 3.16 — ACCESSIBILITÉ

Le viewport 12×12 sert précisément à préserver de grandes cibles.

Vert vs jaune :
- contraste fort ;
- ne pas dépendre uniquement de la couleur ;
- prévoir contour / luminosité / marqueur secondaire si nécessaire.

Le pan doit être compatible avec :
- grossissement ;
- gestes tactiles ;
- lecture visuelle ;
- annonces d'accessibilité.

## 3.17 — ARCHITECTURE

Créer un moteur Gomoku totalement séparé du Sudoku.

Composants conceptuels :
- `GomokuBoardState`
- `GomokuMove`
- `GomokuPlayer`
- `GomokuWinDetector`
- `GomokuAi`
- `GomokuViewport`
- `GomokuGesturePolicy`
- rendu Gecko partagé ;
- pipeline animation partagé.

Ne jamais mettre les règles Gomoku dans `SudokuGameEngine`.

## 3.18 — ÉVOLUTIONS PRÉPARÉES

Sans forcément tout coder maintenant :
- tailles logiques différentes ;
- difficultés Prof ;
- humain vs humain ;
- pédagogique ;
- analyse après partie ;
- défis tactiques ;
- statistiques ;
- séries de victoires ;
- personnalités Prof.

## 3.19 — JALON PHASE 3

Tests obligatoires :
- viewport par défaut montre environ 12×12 positions ;
- zoom-in réduit le nombre de positions visibles et agrandit les cibles ;
- zoom-out augmente le nombre de positions visibles sans devenir illisible ;
- pinch ne joue aucun coup ;
- zoom conserve autant que possible le point logique sous les doigts ;
- pan explore tout le plateau ;
- tap ne devient pas drag ;
- drag ne joue aucun coup ;
- IA analyse le plateau complet ;
- difficulté modifie réellement l'horizon stratégique ;
- niveau bas privilégie réaction immédiate ;
- niveaux élevés savent préparer doubles menaces / pièges ;
- aucun niveau ne triche ;
- Gecko vert/jaune partagent le même asset ;
- filtre jaune préserve alpha/détails ;
- victoire 5 directions requises ;
- victoire hors écran recentrée ;
- animations partagées ;
- dialogues sans préfixe redondant ;
- accessibilité ;
- CI GREEN ;
- APK/AAB ;
- test téléphone Fab.

---

# STRATÉGIE DE TEST FAB

Chaque phase produit une candidate testable séparément.

## Candidate A — Classic
Fab vérifie :
- grille plus large ;
- aucune régression ;
- animations Gecko historiques.

## Candidate B — Sudoku
Fab vérifie :
- titre ;
- animation Gecko classique dans Sudoku ;
- tap / retap ;
- double tap repères ;
- long press ;
- Prof détaillé visuel + texte.

## Candidate C — Gomoku
Fab vérifie :
- apparition du 3e mode ;
- viewport 12×12 au zoom de référence ;
- drag/pan ;
- zoom-in / zoom-out ;
- difficulté stratégique et pièges ;
- pose Gecko vert ;
- coup Pierre Gecko jaune ;
- alignements ;
- accessibilité ;
- humour / pédagogie.

---

# RÉSUMÉ FINAL

```text
1. CLASSIC
   → grille plus large
   → pipeline animation canonique
   → candidate testable

2. SUDOKU
   → animation Classic mutualisée
   → titre nettoyé
   → double tap repères
   → Prof raisonnement détaillé
   → candidate testable

3. GOMOKU
   → moteur séparé
   → plateau logique plus grand
   → viewport 12×12 par défaut
   → drag/pan
   → zoom-in / zoom-out
   → difficulté = profondeur de projection + pièges
   → Gecko vert vs Gecko jaune dynamique
   → IA Pierre
   → candidate testable
```

Le travail est compartimenté pour permettre à Fab de tester chaque mode simplement avant de poursuivre.
