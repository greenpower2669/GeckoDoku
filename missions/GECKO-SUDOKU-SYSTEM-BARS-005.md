# GECKO-SUDOKU-SYSTEM-BARS-005 — zone utile Android pour fenêtres flottantes

Date : 14/10/2026. Ordre Fab : GO correction suite à capture Samsung Android 16.
Branche : `feature/sudoku-visual-reasoning-v1` (aucun merge main).
Dépendance : `missions/GECKO-SUDOKU-FLOATING-LAYERS-004.md`.

## Constat confirmé dans le code

- Les panneaux Repères humains, Aide surhumaine et pavé Sudoku bornent leurs déplacements et redimensionnements à `screenRoot.width/height`, pas à la zone réellement utilisable.
- Sur l'écran Samsung, les boutons système en bas coupent l'espace disponible. La grille utilise déjà des insets, tandis que les PopupWindow ont d'autres calculs.
- `PopupWindow.showAtLocation(Gravity.TOP|START)` emploie des coordonnées de fenêtre ; le plateau était mesuré relativement au root, avec décalages possibles.
- Le défilement horizontal du ruban Repères (boutons Projection…Effacer) est intentionnel ; ne pas introduire un changement de gameplay.

## Changement de code

- `SudokuFloatingWindowGeometryPolicy.safeBounds()` : intersection de la surface réelle du root mesuré avec les insets `systemBars|displayCutout` du decor, sans double retrait si la vue est déjà inset.
- `clampToBounds()` : une politique pour x/y/w/h, limites utilisables, clamp en cas de rotation, et sauvegarde de géométrie Repères toujours préservée.
- `bottomCentered()` : clavier initialement placé et redimensionné au-dessus de la navigation Samsung.
- `MainActivity.sudokuPopupSafeBounds()` : conversion en coordonnées de fenêtre via `getLocationInWindow()`.
- Repères, Aide et pavé : placement initial, drag, redimensionnement, restauration et `WindowInsets` recalés avec le même système.
- `reconcileSudokuFloatingWindowsWithSafeBounds()` : fenêtres ouvertes recalées après insets sans dismissal ; hors écran temporaire et restitution Pierre intacts.
- `SudokuFloatingWindowGeometryTest` : edge-to-edge, navigation à trois touches, écran déjà inset, rotation/resize, clavier bas-centre, layout transitoire étroit.

## Vérifications avant livraison

- [x] Correctifs et tests unitaires écrits sur branche dédiée.
- [ ] CI `testDebugUnitTest + assemblePhone` GREEN à obtenir.
- [ ] Artefact APK phone contenant Pierre Sherpa/Piper et banque sprites 240p à vérifier.
- [ ] Recette Samsung Android 16 par Fab : descente maximum au-dessus des 3 boutons, aucun débordement; pavé redimensionnable jusqu'à la limite; rotation; position mémorisée; Pierre devant et restitution exacte des 3 panneaux.
- [ ] Vérifier le défilement horizontal intentionnel de Projection/Couple/Triplet/Candidat/Effacer.
- [ ] Aucun merge `main`, Release, Play, signature, package, contenu ou règle de jeu modifiés sans accord explicite.
