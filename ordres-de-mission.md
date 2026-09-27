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
4. tests + CI + APK/AAB ;
5. test téléphone Fab.

FUTUR :
- Gomoku complet uniquement après un GO distinct de Fab.
