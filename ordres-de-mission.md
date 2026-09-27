# GECKODOKU — ORDRE DE MISSION ACTIF
## GECKO-039 — CORRECTIONS SUDOKU + FUTUR MODE GOMOKU

Branche impérative :
`gecko-039-sudoku-tap-gecko-gomoku`

Base actuelle :
`d9cfeac9344072e6fba24bfdbdb87486dfcbfcd8`

IMPORTANT :
- intervenir uniquement sur cette branche ;
- ne pas repartir de `main` ;
- ne pas fusionner `main` ;
- ne pas publier de release sans GO explicite de Fab ;
- synchroniser `brain.md`, `brainmap.md`, `debughistorical.md` et `todo.md` avec chaque intervention ;
- téléphone Fab = autorité finale.

---

# 1 — SUDOKU : GECKO-REPÈRE — CORRECTION DES ANIMATIONS

STATUT :
Tap / retap déjà validés fonctionnellement.
Prof Sudoku déjà validé fonctionnellement.

## OBJECTIF

Le petit Gecko-repère du mode Sudoku ne doit PAS se contenter d'une animation procédurale simplifiée propre au Sudoku.

Fab demande explicitement de **réutiliser les vraies animations du Gecko du mode classique**.

Le Gecko-repère doit donc retrouver le comportement vivant et mignon déjà connu dans le mode GeckoDoku historique.

## CONTRAT

Tap simple sur une case Sudoku vide jouable :
- pose le petit Gecko-repère.

Retap sur cette même case :
- retire le Gecko-repère.

Ce comportement est déjà correct et ne doit pas être modifié.

## ANIMATIONS À RÉUTILISER

Réutiliser autant que possible l'infrastructure / les médias existants du mode classique, notamment les animations Gecko déjà présentes dans le projet.

Références existantes à auditer :
- `assets/gecko/Gecko_apparition.mp4`
- `assets/gecko/Gecko_disparition.mp4`
- `assets/gecko/Gecko_actions_plusieurs.mp4`
- autres mécanismes d'animation Gecko déjà réellement utilisés par le mode historique.

Objectif :
- apparition mignonne quand le repère est posé ;
- disparition mignonne quand il est retiré ;
- petites actions / attitudes aléatoires comme dans le mode classique ;
- comportement vivant et non mécanique ;
- ne pas recréer une seconde architecture d'animation si l'existante peut être mutualisée.

IMPORTANT :
- conserver la coexistence avec les candidats noirs ;
- ne pas gêner la lecture de la grille ;
- ne pas bloquer le tactile ;
- ne pas ralentir le jeu ;
- respecter le réglage Animations ON/OFF ;
- Animations OFF = repère visible sans animation ;
- ne pas transformer l'animation en effet décoratif permanent envahissant.

La petite animation procédurale actuelle peut servir de fallback si nécessaire, mais **la cible est bien la réutilisation des animations classiques**.

---

# 2 — BUG MINEUR / NETTOYAGE DU TITRE

Dépôt : GeckoDoku
Intervenir sur la branche actuellement en cours.

## OBJECTIF

Sur l'écran du mode Sudoku, le titre affiche actuellement :

`GeckoDoku · Sudoku 🦎`

Retirer uniquement la mention :

`· Sudoku`

afin que le titre devienne simplement :

`GeckoDoku 🦎`

## RAISON

Le mode de jeu est déjà parfaitement identifiable juste dessous grâce à :
- `Sudoku 9×9 • Facile • 40 cases données`
- `Mode Sudoku activé`

Répéter « Sudoku » dans le titre principal surcharge visuellement l'interface et affaiblit le nom de l'application.

## IMPORTANT

- conserver le nom `GeckoDoku` ;
- conserver le petit Gecko / l'icône présente à droite du titre ;
- ne pas modifier `Sudoku 9×9…` ;
- ne pas modifier `Mode Sudoku activé` ;
- ne pas modifier le fonctionnement du mode Sudoku ;
- ne pas modifier Prof Gecko ;
- ne pas modifier les autres modes ;
- cette correction de titre est purement visuelle.

## AUDIT RAPIDE

Vérifier également que les autres modes ne rajoutent pas leur nom directement dans le titre principal `GeckoDoku`.

Principe UI permanent :

```text
TITRE PRINCIPAL = identité de l'application : GeckoDoku
INDICATION DE MODE = zone dédiée sous le titre
```

---

# 3 — INVARIANT MESSAGE PROF MULTI-MODE

STATUT :
Correction déjà appliquée en Sudoku.
À conserver comme invariant permanent.

`ProfessorBubbleView` affiche déjà l'en-tête « Prof Gecko ».

Donc :
- le corps ne commence jamais par `Prof Gecko :` ;
- le corps ne commence jamais par `Prof Gecko •` ;
- le status décrit uniquement l'action / technique / état.

Ce contrat devra être respecté dès le premier code du futur Gomoku.

---

# 3A — SUDOKU : DOUBLE TAP = CLAVIER DES REPÈRES PERSONNELS

## OBJECTIF

En mode Sudoku, ajouter un geste distinct :

**DOUBLE TAP SUR UNE CASE**
→ ouvrir le clavier / la palette des repères personnels.

Le but est de permettre au joueur d'ajouter rapidement ses propres repères visuels, en complément :
- du Gecko-repère ;
- des candidats 1–9 ;
- des valeurs Sudoku.

## CONTRAT DES GESTES

```text
TAP SIMPLE sur case vide
→ pose / retire le Gecko-repère

DOUBLE TAP sur case
→ ouvre le clavier / la palette des repères personnels

APPUI LONG sur case
→ ouvre la palette Sudoku locale
   - valeurs 1..9
   - candidats 1..9
   - effacer
```

IMPORTANT :
- ne pas confondre double tap et deux taps simples successifs ;
- un double tap ne doit pas poser puis retirer instantanément le Gecko-repère ;
- un double tap ne doit pas saisir automatiquement une valeur Sudoku ;
- réutiliser autant que possible le système de repères personnels du mode classique ;
- ne pas mélanger les repères personnels avec les notes/candidats Sudoku dans le moteur logique ;
- les repères personnels restent purement visuels ;
- ils ne doivent jamais influencer SudokuSolver ou SudokuHintEngine ;
- conserver l'accessibilité et les grandes cibles tactiles.

---

# 3B — CORRECTION PROF GECKO
## EXPLICATION VISUELLE ET RAISONNEMENT DÉTAILLÉ

## PROBLÈME CONSTATÉ

Actuellement Prof Gecko peut afficher par exemple :

`Dans cette ligne, le 6 ne peut aller qu'à cet endroit.`

C'est insuffisant.

Le joueur voit la réponse, mais ne comprend pas pourquoi les autres positions sont impossibles.

## OBJECTIF

Quand Prof Gecko donne une aide logique, l'écran doit présenter :

### À GAUCHE
La projection graphique du raisonnement directement sur la grille / mini-grille.

### À DROITE
L'explication détaillée de Pierre correspondant exactement à cette projection.

Sur petit écran Android, autoriser une disposition adaptative :

```text
[ projection ]
[ explication ]
```

si deux colonnes deviennent illisibles.

## RÈGLE FONDAMENTALE

Le Prof ne doit jamais se contenter de donner la conclusion.

Il doit expliquer les éliminations successives qui permettent d'y arriver.

Exemple de style attendu :

`Regarde le 6 déjà présent ici.`

`Il se projette sur cette ligne : toutes ces cases deviennent impossibles pour un autre 6.`

`Ensuite, ce deuxième 6 se projette dans cette direction.`

`Zut ! Cette deuxième possibilité est elle aussi éliminée.`

`Dans cette zone, il ne reste maintenant qu'une seule case possible pour le 6.`

`Donc le 6 doit forcément être placé ici.`

Le texte exact dépend obligatoirement de la grille réelle.

## PROJECTION GRAPHIQUE

Pendant chaque étape :

1. mettre en évidence le chiffre source utilisé ;
2. dessiner la projection réellement utilisée :
   - ligne ;
   - colonne ;
   - bloc 3×3 ;
3. montrer graphiquement les cases rendues impossibles ;
4. afficher une deuxième projection lorsqu'une seconde contrainte intervient ;
5. marquer clairement les possibilités éliminées ;
6. terminer par la seule case restante.

Exemple conceptuel :

```text
6 source
   ↓
projection ─────────────── X X X

autre 6
   ↓
projection │
           X
           X

une seule case encore possible
           ↓
          [ 6 ]
```

## SYNCHRONISATION TEXTE / GRAPHISME

Les explications apparaissent étape par étape.

### ÉTAPE 1
Graphique :
projection du premier 6.

Texte :
`Ce 6 interdit toute cette ligne.`

### ÉTAPE 2
Graphique :
projection du deuxième 6.

Texte :
`Celui-ci élimine aussi cette possibilité.`

### ÉTAPE 3
Graphique :
cases impossibles barrées / atténuées.

Texte :
`Il ne reste donc plus qu'une case possible.`

### ÉTAPE 4
Graphique :
case solution très clairement mise en évidence.

Texte :
`Voilà ! Le 6 doit être ici.`

## SOURCE UNIQUE DE VÉRITÉ : TRACE DE RAISONNEMENT

Le moteur doit expliquer **le raisonnement réel qui a conduit à la suggestion**.

Interdit :
- obtenir une case solution ;
- puis fabriquer après coup une phrase générique qui semble expliquer cette solution.

Le système d'aide doit conserver une structure de preuve, nom libre, équivalente à :

`SudokuReasoningTrace`

contenant au minimum :
- technique utilisée ;
- valeur recherchée ;
- chiffres/cases sources de contraintes ;
- type de chaque projection ;
- cases candidates initiales ;
- cases/candidats éliminés ;
- raison de chaque élimination ;
- dernière case restante ;
- ordre logique des étapes.

Cette même trace sert ensuite à générer :
1. l'animation / projection graphique ;
2. l'explication du Prof.

**Le dessin et le texte ne doivent jamais provenir de deux calculs indépendants.**

## TECHNIQUES CONCERNÉES

Commencer par les techniques déjà réellement implémentées :
- candidat unique / naked single ;
- chiffre unique dans une ligne ;
- chiffre unique dans une colonne ;
- chiffre unique dans un bloc 3×3.

Pour chacune :
- produire une trace exacte ;
- n'afficher que les contraintes réellement nécessaires ;
- ne pas inventer une projection qui n'a pas servi au raisonnement.

Les techniques futures devront adopter la même architecture.

## ERGONOMIE

Zone d'explication :

```text
[ projection / mini-grille ] [ raisonnement détaillé ]
```

La projection doit rester suffisamment grande pour être comprise.

Le texte doit rester lisible et suivre exactement l'étape graphique courante.

Prévoir :
- étape suivante automatique lente ou pilotable ;
- possibilité de revoir une étape si nécessaire ;
- aucune animation trop rapide ;
- aucune projection qui masque définitivement la grille.

## ACCESSIBILITÉ

Les projections ne doivent jamais être indiquées uniquement par la couleur.

Utiliser aussi :
- traits ;
- flèches ;
- hachures ;
- croix ;
- contours ;
- variation de luminosité ;
- surbrillance ;
- petite animation si utile.

La case finale doit être extrêmement facile à identifier.

## INVARIANT PIERRE ↔ BULLE

Le contrat existant reste obligatoire :
tout texte réellement prononcé par Pierre apparaît dans ProfessorBubbleView.

L'en-tête visuel contient déjà `Prof Gecko`, donc le corps ne répète pas ce préfixe.

## CRITÈRES DE SUCCÈS

Une aide Prof est validée seulement si le joueur peut répondre à la question :

**« Pourquoi cette case et pas les autres ? »**

en regardant la projection et en lisant/écoutant Pierre.

Le Prof est un professeur.

Son rôle n'est pas :
`Voici la réponse.`

Son rôle est :
`Je vais te montrer pourquoi cette réponse est forcément correcte.`

Le joueur doit pouvoir reproduire ensuite le raisonnement seul.

---

# 4 — FUTUR MODE GOMOKU AVEC PROF GECKO

STATUT :
**BRAINSTORM / ORDRE DE MISSION FUTUR.**
**NE PAS CODER CE MODE TANT QUE FAB N'A PAS DONNÉ UN GO EXPLICITE.**

## 4.1 But général

Ajouter un troisième mode de jeu inspiré du Gomoku.

Le joueur affronte directement Prof Gecko / Pierre.

Principe :
- le joueur pose un Gecko sur une case libre ;
- Prof Gecko joue ensuite son propre coup ;
- le premier camp qui aligne au moins 5 Geckos gagne.

Conserver toute l'identité GeckoDoku :
- humour ;
- Prof vivant ;
- petites interventions contextuelles ;
- interface claire ;
- animations ;
- accessibilité ;
- aucun aspect froid ou purement abstrait.

## 4.2 Plateau

Créer un plateau Gomoku indépendant du Sudoku.

La taille exacte sera décidée au moment de l'implémentation.

Le moteur ne doit pas dépendre obligatoirement d'un plateau de Go 19×19.

Prévoir une grille plus compacte si nécessaire pour :
- téléphone ;
- visibilité ;
- cibles tactiles ;
- identité GeckoDoku.

Toutes les cases libres sont jouables.
Une case occupée est interdite.

## 4.3 Pions = Geckos

JOUEUR :
- Gecko vert original.

PROF :
- Gecko jaune.

IMPORTANT :
AUCUN nouvel asset Gecko jaune.

Utiliser exactement le même sprite Gecko que pour le joueur.

## 4.4 Coloration dynamique du Gecko jaune

Partir du Gecko vert existant.

Transformation colorimétrique :
- conserver principalement le vert ;
- augmenter fortement le rouge ;
- réduire ou supprimer le bleu.

Principe :
```text
VERT :
R faible
G fort
B faible

→

JAUNE :
R fort
G fort
B faible / nul
```

Le filtre :
- conserve l'alpha ;
- conserve ombres, détails, yeux, nuances, transparence et relief ;
- ne change ni taille ni silhouette ;
- ne dégrade pas le sprite.

Ne pas faire un simple aplat jaune.

## 4.5 Tour du joueur

Le joueur touche une case libre.

Un Gecko vert est placé.

Case occupée :
- aucun pion ajouté ;
- remarque humoristique contextuelle possible.

Exemples :
- « Petit problème… je suis déjà là. »
- « Tu veux mettre deux Geckos dans la même case ? Ambitieux. »
- « Cette place est prise, jeune lézard. »

Après un coup valide :
- vérifier immédiatement l'alignement gagnant.

## 4.6 Tour du Prof

Fonctionnement envisagé :
1. joueur joue ;
2. interaction Prof devient disponible ;
3. joueur demande au Prof de jouer ;
4. moteur analyse la position ;
5. Prof choisit une case ;
6. Gecko jaune placé ;
7. remarque éventuelle ;
8. retour joueur.

Le coup du Prof est calculé dynamiquement depuis le plateau courant.

Interdit :
- préenregistrer toutes les parties ;
- créer un arbre géant exhaustif ;
- prévoir toutes les combinaisons à l'avance.

## 4.7 IA du Prof

Commencer par une IA déterministe et lisible.

Priorités :
1. gagner immédiatement ;
2. bloquer une victoire immédiate du joueur ;
3. créer une ligne de 4 menaçante ;
4. empêcher une ligne de 4 ;
5. créer / bloquer des lignes de 3 ;
6. privilégier une case tactique proche des pions ;
7. petit aléatoire seulement entre plusieurs coups de valeur comparable.

Le Prof ne doit pas être immédiatement imbattable.
Des difficultés pourront être ajoutées plus tard.

## 4.8 Personnalité du Prof

Commentaires contextuels seulement quand cela apporte quelque chose.

Exemples :
- blocage de victoire ;
- menace créée ;
- danger ;
- victoire ;
- case occupée ;
- explication pédagogique.

Pas de phrase après chaque coup.

Réutiliser :
- personnalité existante ;
- mémoire anti-répétition ;
- humeur ;
- pipeline Pierre ↔ bulle.

Exemples sans préfixe de locuteur :
- « Ah ah ! Celle-là, je l'avais vue. »
- « Pas si vite. »
- « Très jolie attaque… ce serait dommage si quelqu'un mettait un Gecko ici. »
- « Tu devrais peut-être regarder cette ligne. »
- « Je dis ça, je dis rien… mais ça commence à sentir le Gecko. »
- « Et de cinq ! »
- « Une magnifique ligne de Geckos. J'en suis presque ému. »

## 4.9 Prof adversaire, mais toujours professeur

Pierre reste Prof Gecko.

Il peut expliquer :
- pourquoi une position était dangereuse ;
- pourquoi il a bloqué ;
- comment reconnaître un alignement ;
- pourquoi une intersection est forte.

Le mode reste à la fois :
- jeu ;
- défi ;
- apprentissage.

## 4.10 Validation des coups

Toutes les cases libres sont légalement jouables.

Ne jamais dire qu'une case libre est « incorrecte ».

Un coup libre peut être :
- excellent ;
- moyen ;
- dangereux ;
- mauvais stratégiquement ;

mais reste légal.

## 4.11 Victoire

Après chaque coup, rechercher au moins 5 Geckos identiques :
- horizontal ;
- vertical ;
- diagonale descendante ;
- diagonale montante.

Victoire :
- bloquer nouveaux coups ;
- mettre en valeur l'alignement gagnant ;
- réaction adaptée du Prof ;
- proposer Rejouer.

Prévoir égalité éventuelle si plateau plein sans gagnant.

## 4.12 Animation

Placement d'un Gecko :
- petite apparition / rebond ;
- bref ;
- fluide ;
- aucune animation ne doit ralentir artificiellement le tour.

Réflexion Prof :
- éventuel regard ;
- mouvement ;
- petite bulle ;
- réaction courte.

## 4.13 Accessibilité

Vert joueur et jaune Prof doivent être très faciles à distinguer.

Ne pas dépendre uniquement d'une nuance de teinte.

Prévoir si nécessaire :
- contour ;
- luminosité différente ;
- petit marqueur secondaire.

Les cases restent suffisamment grandes pour être touchées facilement.

## 4.14 Architecture

Le moteur Gomoku reste séparé du moteur Sudoku.

Créer une logique claire contenant :
- état plateau ;
- joueur courant ;
- validation case ;
- détection alignement ;
- choix IA ;
- victoire ;
- égalité éventuelle.

La personnalité du Prof peut être mutualisée.

Ne jamais introduire les règles Gomoku directement dans `SudokuGameEngine`.

## 4.15 Évolutions futures

Préparer sans forcément coder :
- plusieurs tailles ;
- plusieurs difficultés ;
- humain vs humain ;
- Prof facile / normal / expert ;
- partie pédagogique ;
- analyse après partie ;
- suggestions ;
- défis tactiques ;
- positions préconstruites ;
- statistiques ;
- séries de victoires ;
- variations de personnalité.

---

# RÉSUMÉ VISUEL GOMOKU

```text
JOUEUR
= Gecko vert original

PROF
= même sprite Gecko
+ filtre colorimétrique dynamique
+ rouge augmenté
+ vert conservé
+ bleu réduit / supprimé
= Gecko jaune
```

AUCUN ASSET GECKO JAUNE SUPPLÉMENTAIRE.

---

# PHILOSOPHIE

Un Gomoku simple à comprendre mais totalement intégré à l'univers GeckoDoku.

Le joueur ne doit pas avoir l'impression de jouer contre une IA abstraite.

Il doit avoir l'impression de jouer contre Pierre, qui réfléchit, bloque, attaque, plaisante et explique parfois pourquoi il vient de se faire avoir.

---

# ÉTAT ACTUEL DE LA MISSION

DÉJÀ FAIT / À NE PAS REFAIRE :
- tap simple Sudoku pose le Gecko-repère ;
- retap enlève le Gecko-repère ;
- appui long case ouvre la palette ;
- comportement Prof Sudoku tap / retap / long press ;
- nettoyage du préfixe redondant Prof dans les messages Sudoku.

À FAIRE :
1. remplacer / mutualiser l'animation du Gecko-repère Sudoku avec les animations du mode classique ;
2. corriger le titre Sudoku en `GeckoDoku 🦎` ;
3. audit rapide du principe de titre sur les autres modes ;
4. double tap Sudoku → clavier / palette des repères personnels ;
5. construire la trace de raisonnement réelle du Prof ;
6. projection graphique + narration détaillée synchronisées ;
7. tests accessibilité / cohérence texte-graphisme ;
8. tests + CI + APK/AAB ;
9. test téléphone Fab.

FUTUR :
- Gomoku complet uniquement après un GO distinct de Fab.
