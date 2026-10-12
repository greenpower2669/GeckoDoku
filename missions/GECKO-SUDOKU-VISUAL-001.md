# MISSION ASTRA / SOL — GECKO-SUDOKU-VISUAL-001
**Date :** 12 octobre 2026
**Dépôt :** `greenpower2669/GeckoDoku`
**Branche dédiée :** `feature/sudoku-visual-reasoning-v1` (base `main`)
**Décision Fab :** concept fonctionnel validé ; implémentation et tests demandés. **Aucun merge sur main, aucune Release/Play Console sans validation explicite.**

## Intention
Faire du Sudoku un espace de raisonnement **visuellement accessible** pour une personne malvoyante : rendre les hypothèses, les paires, les triplets et les liens visibles, **sans résoudre le jeu à la place du joueur**. Conserver la forte lisibilité actuelle du plateau, les grandes séparations, les couleurs et tout l'existant.

## Préparation obligatoire — FAB Copilot
1. Lire le code réel de `main`, puis `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md`, `ordres-de-mission.md`. Chercher les implémentations réelles du pavé Sudoku, du panneau de confirmation, des candidats/hypothèses, du tap Gecko et des interventions de Pierre. Ne pas recoder de mémoire ; ancienne branche de référence potentielle : `gecko-045-sudoku-hypothesis-pad`, mais **travailler sur main courant**.
2. Respecter les autres modes et ne pas modifier la mécanique des notes, les sauvegardes, le Hall, les statistiques, les ressources, Piper/Pierre ou les sprites.
3. Invariants Android absolus : conserver l'`applicationId` / package `com.greenpower2669.geckodoku` **après vérification dans le code** ; ne jamais modifier les identités de signature, réinventer une clé JKS ni divulguer les secrets. Préserver les mises à jour de l'application et le pipeline `phone` (modèle Pierre + banque sprites 240p).
4. Construire des petits composants testables, avec tests RED→GREEN si pertinent, et documenter les décisions.

## A. Pavé numérique Sudoku flottant
- Le rendre **ancré en bas au centre**, légèrement plus compact tout en gardant les grandes zones tactiles accessibles.
- Le pavé est une fenêtre flottante **au premier plan** par défaut, jamais masquée durablement par le Prof.
- **Pendant que Pierre parle**, sa représentation peut temporairement passer devant, puis le pavé **reprend automatiquement la priorité** dès la fin de l'intervention. Respecter les mécanismes réels de lifecycle et z-order (pas de fenêtre système Android superposée aux autres applications).
- Une croix de fermeture identifiable et facilement activable ; pas de recouvrement incontrôlé avec la grille ou le bouton de validation. Préserver les gestes existants et le fonctionnement du mode hypothèses.
- Adapter au format/rotation/densité réels ; aucun contrôle ne doit sortir de l'écran.

## B. Panneau existant de validation : quatre cases à cocher indépendantes
Placer au voisinage de la validation Oui/Non et des choix de candidats :
1. **Projeter la ligne** (OFF initial).
2. **Couples** (OFF initial).
3. **Triplets** (OFF initial).
4. **Entourer** (OFF initial).
Cases cochables/décochables par tap successif, états et retours visuels évidents ; préserver la validation des notes. Les options doivent pouvoir être combinées. Choisir une disposition compacte mais lisible, sans mini-texte.

## C. Projection d'un chiffre sur une ligne
- Si l'option Projection est active et qu'un **même chiffre candidat** apparaît dans **au moins deux cases d'une même ligne horizontale**, tracer un ou plusieurs liens fins, transparents mais lisibles, entre les positions de ce chiffre. **Un seul candidat = aucun lien.**
- Faire apparaître le chiffre à chaque extrémité, et conserver la correspondance avec ses cases ; dès qu'un candidat change, recalculer la projection sans traînées.
- Les chiffres 1 à 9 doivent disposer de positions/pistes différenciées dans les cases ou sur les lignes afin de **limiter les chevauchements** ; gérer les neuf chiffres simultanés, avec marges et contraste suffisants.
- Teintes distinctes plus motifs/indices textuels : ne pas se fonder sur la couleur seule. Tout tracé est un repère visuel, **pas une preuve d'exclusion**.
- Ne pas inventer pour cette phase une projection automatique en colonne, non demandée.

## D. Couples et triplets
- Supporter les **paires nues** (ex. {1,3} dans exactement deux cases d'une même unité) et les **triplets nus** (ex. {1,2}, {1,3}, {2,3} dans trois cases d'une même unité), que les cases soient ou non alignées horizontalement.
- Par « unité », entendre **ligne, colonne ou bloc 3×3** : un groupe visuellement proche dans des unités différentes n'autorise **aucune élimination**.
- Proposer un marquage très clair des groupes que le joueur identifie ; validation logique facultative, mais ne **jamais** annoncer ou exécuter une élimination non démontrée. Ne pas confondre double choix dans une seule case avec une paire nue et ne pas confondre deux hypothèses binaires sans unité commune avec une paire Sudoku.
- Le joueur peut combiner le marquage avec « Projeter la ligne » quand le groupe possède des candidats alignés.
- Effet demandé « clignotant » : appliquer **une pulsation lente, douce, désactivable**, ou un surlignage fixe (option « réduire les animations »). Aucun flash rapide ; garder tous les candidats lisibles même sans animation.

## E. Entourer / liaison visuelle / préparation du X-Wing
- Activation « Entourer » : après une sélection initiale d'un candidat, le tap sur un **second candidat de même valeur** marque ce second candidat d'un cercle fin, semi-transparent, coloré et lisible.
- Conserver visuellement la relation (au besoin cercler aussi l'origine ou tracer un trait discret), autoriser plusieurs relations de couleurs différenciées. Prévoir annulation/suppression d'un lien sans effacer la note Sudoku.
- Pouvoir matérialiser un rectangle de **quatre occurrences** de même chiffre ; c'est un **outil de dessin logique** et non une reconnaissance automatique de X-Wing.
- Un X-Wing véritable nécessite deux lignes avec **exactement les mêmes deux colonnes possibles pour ce chiffre** (ou l'inverse en inversant lignes/colonnes). N'autoriser une indication d'exclusion que si les contraintes sont vérifiées ; sinon ne montrer que l'annotation.
- Garder les cercles devant les candidats mais derrière les contrôles tactiles, ne jamais obstruer un chiffre.

## F. Accessibilité et comportement
- Cibles tactiles généreuses, contraste lisible, graduations et marqueurs assez épais, taille adaptable à la grille, accessibilité sans dépendre de la seule couleur.
- Privilégier une **alternative statique** aux animations. OFF par défaut pour les effets.
- Les projections/circles sont des **annotations distinctes des candidats véritables** : ne pas altérer le solveur, les règles du jeu, le score, les aides existantes ni la sauvegarde des parties.
- Documenter explicitement la persistance retenue : pour un retour à la partie, conserver si possible les préférences de visualisation sans faire croire que les annotations changent l'état logique du Sudoku.

## G. Vérifications / recette
- Tests de superposition du pavé avant / pendant / après parole de Pierre et en fermeture/réouverture ; compatibilité taille d'écran et touches de validation.
- Ligne avec 1, 2, 3 candidats identiques ; cas de 1 à 9 en parallèle ; ajout/retrait de note.
- Paires/triplets valides et contre-exemples invalides (case isolée, groupes hors unité) ; projection simultanée.
- Plusieurs cercles ; suppression ciblée ; test faux X-Wing et vrai X-Wing ; aucune élimination fausse.
- Vérifier 4 modes, gestes Sudoku tap/long press, TTS Pierre, Hall, progression, imports et sauvegardes.
- Tests CI puis construction d'un **APK Android variante phone testable**, si la chaîne GitHub le permet ; vérifier que le modèle Piper et les 1 305 sprites 240p attendus sont bien embarqués. AAB uniquement si build fiable et signature légitime disponible ; ne jamais publier automatiquement.
- Fournir branche, commits, état des tests, workflow/run, lien artifact APK s'il existe, SHA-256 vérifié et points restant à tester sur Samsung.

## H. Livrables et protocole d'arrêt
1. Commits/push **uniquement sur la branche dédiée**, jamais sur main sans nouvelle décision Fab.
2. Synchroniser les cinq fichiers de mémoire vivante propres à GeckoDoku selon FAB Copilot après implémentation : `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md`, `ordres-de-mission.md`. Archiver/synthétiser les longues parties, ne jamais écraser l'historique technique utile.
3. **Si tout ne tient pas en une session** : committer ce qui est prêt sans prétendre que c'est terminé, laisser dans `todo.md` les points exacts à reprendre et les preuves manquantes, puis demander les validations nécessaires.
4. **Aucun merge main, aucune Release, aucun changement d'applicationId ou de signature** sans instruction explicite du propriétaire.

**Critère de succès utilisateur :** avec une basse vision, Fab suit ses propres raisonnements Sudoku à l'écran grâce au pavé toujours accessible, aux projections, aux paires/triplets et aux cercles, sans modification des règles ni perte des fonctions historiques.
