# PASSATION — GECKODOKU
## FENÊTRE REPÈRES COMPACTE + MÉMOIRE DE POSITION + PIERRE AU PREMIER PLAN

Dépôt :
`greenpower2669/GeckoDoku`

Branche :
`feature/sudoku-visual-reasoning-v1`

RÈGLES
- Ne pas merger `main`.
- Ne pas créer de Release.
- Ne pas publier Google Play.
- Conserver package, signature, Pierre Sherpa/Piper et banque sprites 240p.
- Mettre à jour les cinq mémoires FAB Copilot.
- Produire un APK seulement après CI GREEN.

==================================================
1. FENÊTRE « REPÈRES HUMAINS »
==================================================

Objectif validé par Fab :
la disposition par défaut doit être directement exploitable, sans devoir déplacer ou réduire la fenêtre à chaque ouverture.

Disposition canonique :
- fenêtre au-dessus du plateau Sudoku ;
- largeur alignée autant que possible sur la largeur réelle du plateau ;
- hauteur compacte au strict nécessaire ;
- deux rangées seulement :
  1. barre de titre ;
  2. boutons Projection / Couple / Triplet / Candidat / Effacer ;
- quasiment aucune marge verticale inutile ;
- barre haute raccourcie : « ✎ Repères humains » ;
- la barre entière reste la poignée de déplacement ;
- bouton fermer compact à droite.

Placement initial :
- calculer après layout réel du plateau ;
- attendre que `sudokuBoard.width/height > 0` ;
- position = immédiatement au-dessus du plateau ;
- clamp dans la zone écran utile.

Mémoire utilisateur :
- après déplacement, mémoriser x/y ;
- mémoriser aussi width/height pour compatibilité avec futurs redimensionnements ;
- à l’ouverture suivante, restaurer la géométrie mémorisée ;
- si dimensions écran changent, re-clamper proprement ;
- si aucune mémoire, utiliser le placement compact canonique.

Le stockage de géométrie ne doit jamais modifier la grille ni les préférences de gameplay.

==================================================
2. PIERRE ET ORDRE DES CALQUES
==================================================

Règle Fab :
si Pierre parle, Pierre passe TEMPORAIREMENT au premier plan.

Pendant toute la durée `speaking=true` :
- conserver l’état exact des fenêtres Sudoku actuellement visibles ;
- Repères humains ;
- Aide surhumaine ;
- clavier/pavé Sudoku ;
- les éloigner temporairement de la zone visible sans les détruire ;
- ne pas perdre leur position, taille, outil actif ou contenu ;
- Pierre (portrait/vidéo/bulle) devient visible au premier plan.

À `speaking=false` :
- restaurer immédiatement les fenêtres qui étaient présentes ;
- exactement à leur géométrie précédente ;
- conserver leur ordre logique et leur contenu ;
- Pierre revient donc à sa place de calque normale.

IMPORTANT :
- ne pas dismiss/recréer inutilement les popups pendant la parole ;
- ne pas déclencher les listeners de fermeture ;
- ne pas perdre le clavier sélectionné ;
- ne pas remettre à zéro Repères humains ;
- ne pas réinitialiser Aide surhumaine ;
- ne pas modifier la grille.

Approche actuelle :
les `PopupWindow` restent vivants mais sont déplacés hors écran pendant la parole, avec clipping temporairement désactivé, puis replacés avec leurs géométries mémorisées.

==================================================
3. TESTS
==================================================

Vérifier au minimum :
1. première ouverture Repères = compacte et juste au-dessus du plateau ;
2. déplacement puis fermeture/réouverture = même position ;
3. rotation/changement de taille = géométrie re-clampée à l’écran ;
4. Pierre parle avec Repères ouvert = Pierre visible devant ;
5. fin de parole = Repères revient exactement où il était ;
6. même test avec clavier Sudoku ouvert ;
7. même test avec Aide surhumaine ;
8. aucun outil manuel, candidat, hypothèse ou sélection perdu ;
9. aucun changement de gameplay ;
10. CI `testDebugUnitTest + assemblePhone` GREEN.

==================================================
ÉTAT D’IMPLÉMENTATION
==================================================

- `SudokuFloatingWindowGeometry.kt` ajouté.
- Politique compacte + clamp ajoutée.
- Tests géométrie ajoutés.
- Repères réduit à 78 dp de hauteur nominale.
- Barre titre réduite à « ✎ Repères humains ».
- Position/largeur calculées depuis le plateau réel.
- Position mémorisée après drag.
- Géométries runtime suivies pour Repères, Aide surhumaine et clavier.
- Pierre suspend temporairement ces fenêtres pendant la parole et les restaure ensuite.
- Validation téléphone encore requise.
