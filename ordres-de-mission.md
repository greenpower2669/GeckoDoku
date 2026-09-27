# GECKODOKU — ORDRE DE MISSION ACTIF
## GECKO-039 — TAP GECKO SUDOKU + FUTUR MODE GOMOKU

Branche impérative :
`gecko-039-sudoku-tap-gecko-gomoku`

Base de départ :
`538e61560650c094db44e51c7b1bd6fba1f4eabf` (candidate 0.11.2-dev GREEN).

IMPORTANT :
- ne pas repartir de main ;
- ne pas fusionner main ;
- ne pas publier de release sans GO explicite de Fab ;
- relire le HEAD réel avant tout code ;
- synchroniser brain.md, brainmap.md, debughistorical.md et todo.md avec chaque intervention.

---

# PARTIE A — SUDOKU : TAP SIMPLE = PETIT GECKO REPÈRE

## A1. But

En mode Sudoku, un **tap simple sur une case vide jouable** pose un petit Gecko mignon comme repère joueur.

Ce Gecko signifie :
« trop de possibilités / pas encore exploitable logiquement / j'y reviendrai ».

Il a une fonction proche d'une croix/pense-bête, mais reste dans l'identité GeckoDoku.

## A2. Interaction

- tap simple sur case vide jouable → pose le Gecko-repère ;
- tap simple à nouveau sur la même case → retire le Gecko-repère ;
- appui long sur une case → conserve la palette locale valeur 1–9 / candidats 1–9 / effacer ;
- case given → aucun Gecko-repère ;
- case contenant une vraie valeur → aucun Gecko-repère.

Le tap peut aussi sélectionner visuellement la case, mais sa fonction principale devient le toggle du Gecko-repère.

## A3. Nature logique

Le Gecko-repère :
- n'est PAS un chiffre ;
- n'est PAS un candidat 1–9 ;
- n'entre PAS dans SudokuSolver ;
- n'influence PAS SudokuHintEngine ;
- n'est PAS une erreur ;
- reste Undo/Redo ;
- est retiré automatiquement si une vraie valeur est posée ;
- Effacer le retire également.

## A4. Animation

Réutiliser le comportement mignon déjà présent :
- mouvement doux ;
- léger bob / respiration / rotation discrète ;
- rythme déphasé selon la case pour éviter un mouvement militaire synchronisé ;
- animation légère, sans gêner les mini-candidats noirs ;
- si Animations = OFF → Gecko visible mais statique.

Le Gecko reste en filigrane derrière les candidats.

## A5. Lisibilité

Les mini-candidats restent des petits chiffres noirs.
Le Gecko-repère ne doit jamais rendre ces chiffres illisibles.

---

# PARTIE B — INVARIANT DIALOGUE PROF MULTI-MODE

ProfessorBubbleView affiche déjà l'en-tête « Prof Gecko ».

Donc :
- le corps ne commence jamais par `Prof Gecko :` ;
- le corps ne commence jamais par `Prof Gecko •` ;
- le status décrit uniquement l'action / technique / état.

Exemples Sudoku :
- corps : `Cette case n'a plus qu'un seul candidat possible. Le 1 est donc certain.`
- status : `Candidat unique`

Ce contrat devra être appliqué dès la création du futur Gomoku.

---

# PARTIE C — FUTUR MODE GOMOKU AVEC PROF GECKO

STATUT :
**BRAINSTORM / ORDRE DE MISSION FUTUR.**
**NE PAS CODER CE MODE TANT QUE FAB N'A PAS DONNÉ UN GO EXPLICITE.**

## C1. But général

Ajouter un troisième mode de jeu inspiré du Gomoku.

Le joueur affronte directement Prof Gecko / Pierre.

Principe :
- joueur pose un Gecko sur une case libre ;
- Prof joue son propre Gecko ;
- premier camp qui aligne au moins 5 Geckos gagne.

Conserver :
- humour ;
- Prof vivant ;
- interventions contextuelles ;
- interface claire ;
- animations ;
- accessibilité ;
- identité GeckoDoku.

## C2. Plateau

Moteur indépendant du Sudoku.

La taille n'est pas figée à 19×19.
Choisir plus tard une taille adaptée :
- téléphone ;
- lisibilité ;
- grandes cibles tactiles ;
- rythme GeckoDoku.

Toutes les cases libres sont légalement jouables.
Case occupée = coup interdit.

## C3. Pions

JOUEUR :
- Gecko vert original.

PROF :
- même sprite Gecko ;
- rendu jaune dynamique.

AUCUN nouvel asset Gecko jaune.

## C4. Filtre Gecko jaune

Partir du sprite vert existant.

Transformation couleur dynamique :
- conserver alpha ;
- conserver détails, relief, ombres, yeux et silhouette ;
- augmenter fortement R ;
- conserver G fort ;
- réduire B.

Ne pas faire un simple aplat jaune.
Ne pas dégrader le sprite.

## C5. Tour joueur

Tap case libre :
- pose Gecko vert ;
- animation courte ;
- vérifier immédiatement alignement de 5.

Tap case occupée :
- aucun pion ajouté ;
- remarque contextuelle possible, sans spam.

Exemples de ton :
- « Petit problème… je suis déjà là. »
- « Tu veux mettre deux Geckos dans la même case ? Ambitieux. »
- « Cette place est prise, jeune lézard. »

Les phrases sont stockées SANS préfixe « Prof Gecko : ».

## C6. Tour Prof

Architecture envisagée :
1. joueur joue ;
2. interaction Prof devient disponible ;
3. joueur demande au Prof de jouer ;
4. moteur analyse le plateau ;
5. Prof choisit une case ;
6. Gecko jaune placé ;
7. remarque éventuelle ;
8. retour joueur.

Le coup est calculé dynamiquement depuis l'état courant.

Interdit :
- arbre géant préenregistré ;
- liste exhaustive de toutes les parties ;
- réponses scriptées pour chaque combinaison.

## C7. IA déterministe initiale

Priorités :
1. gagner immédiatement si possible ;
2. bloquer victoire immédiate joueur ;
3. créer une ligne de 4 menaçante ;
4. bloquer création de 4 ;
5. créer / bloquer des lignes de 3 ;
6. privilégier zones tactiques proches des pions ;
7. léger aléatoire seulement entre coups de valeur comparable.

Commencer lisible et déterministe avant sophistication.

## C8. Personnalité Prof

Commentaires uniquement sur événements intéressants :
- blocage critique ;
- menace créée ;
- danger ;
- victoire ;
- faute tactile sur case occupée ;
- occasion pédagogique.

Pas de phrase après chaque coup.

Réutiliser autant que possible :
- personnalité existante ;
- mémoire anti-répétition ;
- humeur/contexte ;
- pipeline Pierre ↔ bulle.

Exemples sans préfixe :
- « Ah ah ! Celle-là, je l'avais vue. »
- « Pas si vite. »
- « Très jolie attaque… ce serait dommage si quelqu'un mettait un Gecko ici. »
- « Tu devrais peut-être regarder cette ligne. »
- « Et de cinq ! »

## C9. Prof adversaire et professeur

Pierre reste Prof Gecko même comme adversaire.

Il peut expliquer :
- pourquoi il bloque ;
- pourquoi une intersection est forte ;
- comment reconnaître une menace ;
- pourquoi une ligne devient dangereuse.

Mode à la fois :
- jeu ;
- défi ;
- apprentissage.

## C10. Validation des coups

Toutes les cases libres sont légales.

Ne jamais dire qu'un coup libre est « incorrect ».

Un coup peut être :
- bon ;
- moyen ;
- dangereux ;
- faible ;

mais reste légal.

## C11. Victoire

Après chaque coup, rechercher au moins 5 pions identiques :
- horizontal ;
- vertical ;
- diagonale descendante ;
- diagonale montante.

Victoire :
- verrouiller nouveaux coups ;
- mettre en évidence l'alignement ;
- réaction du Prof adaptée ;
- proposer Rejouer.

Prévoir égalité si plateau plein sans gagnant.

## C12. Animation

Placement :
- apparition / petit rebond ;
- bref et fluide.

Réflexion Prof :
- éventuel regard / mouvement / petite réaction ;
- ne jamais ralentir artificiellement le tour.

## C13. Accessibilité

Vert joueur et jaune Prof doivent être fortement différenciables.

Ne pas dépendre seulement d'une nuance de teinte.
Prévoir si nécessaire :
- contour ;
- différence de luminosité ;
- petit marqueur secondaire.

Conserver de grandes cases tactiles.

## C14. Architecture

Créer un moteur Gomoku séparé :
- état plateau ;
- current player ;
- validation case ;
- détection alignement ;
- AI scoring ;
- victoire ;
- égalité.

Ne jamais injecter les règles Gomoku dans SudokuGameEngine.

La personnalité / parole / animation du Prof peuvent être partagées.

## C15. Évolutions futures possibles

Préparer sans coder immédiatement :
- tailles de plateau variables ;
- difficultés Prof ;
- humain vs humain ;
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
= même sprite
+ filtre dynamique
R ↑
G conservé fort
B ↓
= Gecko jaune
```

AUCUN ASSET JAUNE SUPPLÉMENTAIRE.

---

# PHILOSOPHIE

Le joueur ne doit pas avoir l'impression de jouer contre une IA abstraite.

Il joue contre Pierre :
il réfléchit, attaque, bloque, plaisante et explique parfois pourquoi il vient de se faire avoir.

---

# STATUT DE CET ORDRE

PARTIE A — tap Gecko Sudoku :
**prochaine mission de code après GO/continuation explicite.**

PARTIE B — invariant message Prof :
**correction Sudoku appliquée sur cette branche ; invariant permanent.**

PARTIE C — Gomoku :
**brainstorm seulement. NE PAS CODER SANS GO EXPLICITE DE FAB.**


<!-- GECKO-039-TAP-GECKO-RED-2026-09-27 -->
## ÉTAT D'EXÉCUTION GECKO-039
GO reçu pour PARTIE A et garde-fou PARTIE B. RED posé. PARTIE C Gomoku reste explicitement NON CODÉE.


<!-- GECKO-039-TAP-GECKO-GREEN-2026-09-27 -->
## EXÉCUTION PARTIE A/B
Tap Gecko Sudoku et invariant dialogue central sont implémentés. Attente CI. PARTIE C GOMOKU reste NON CODÉE sans GO distinct.


<!-- GECKO-039-COMPILE-FIX-2026-09-27 -->
#178 compile fix local ; aucune modification du périmètre. Gomoku toujours non codé.


<!-- GECKO-039-CANDIDATE-0113-2026-09-27 -->
## STATUT CANDIDATE
PARTIE A + invariant PARTIE B sont GREEN sur #179 et figés en `0.11.3-dev / code31`.
Attente : CI candidate puis test téléphone Fab.
PARTIE C Gomoku reste BRAINSTORM UNIQUEMENT / NE PAS CODER SANS GO DISTINCT.
