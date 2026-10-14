# GECKO-SUDOKU-KEYPAD-OPEN-COMPACT-009
## Ouverture automatique : la capture du soir comme référence

Date : 14 octobre 2026. Ordre Fab « Go » à la suite des captures 16:45 (pavé trop haut, grille recouverte) et 18:40 (pavé compact sous grille, toutes les lignes visibles).
Branche : `feature/sudoku-visual-reasoning-v1`. Ne pas merger main, aucune Release stable, aucune publication Play.

### Cause reproduite dans le code
Dans `MainActivity.showSudokuCellPalette()`, le `PopupWindow` était construit avec `popupWidth` / `popupHeight` non réduits. Ensuite seulement, `bottomCentered(dock)` calculait une géométrie compacte **sans modifier la taille physique du PopupWindow déjà construit**. Résultat : état géométrique cohérent sur le papier, mais fenêtre réelle conservant son ancienne hauteur. Pour une palette existante, la sélection suivante faisait `refreshSudokuPersistentPalette()` puis `return`, sans contrôler le placement ou la taille physiques.

### Correction ciblée
- `SudokuFloatingWindowGeometryPolicy.compactKeypadBelowBoard()` retourne dès le départ une géométrie compacte avec alignement à droite sous la grille, marges/barres Samsung respectées. Dimensions nominales portrait : 315 dp de large × 225 dp de haut, bornées par le dock ; ne jamais cacher la neuvième ligne.
- `MainActivity.showSudokuCellPalette()` calcule **avant** le constructeur `PopupWindow` x/y/w/h, puis passe les dimensions réellement finales au constructeur ; supprime les anciens calculs orphelins après construction.
- `fitOpenSudokuPaletteToDock()` recale à la réutilisation de la palette, à la modification de la géométrie du plateau, et lors des insets. Compare les dimensions matérielles du PopupWindow aux dimensions stockées, même si x/y/w/h déclarés sont identiques.
- Une position manuelle encore valide dans le dock est préservée ; seuls les débordements ou dimensions physiques incorrectes sont corrigés.
- Aucune modification des modes Sudoku, logique de candidats/hypothèses, Pierre, médias ou Hall of Fame.

### Tests de non-régression
- [x] Portrait Samsung inspiré de la 2e capture : panneau compact aligné à droite sous la ligne 9, hors navigation Android.
- [x] Fenêtre physique trop haute malgré une géométrie enregistrée correcte : nécessité de mettre à jour le PopupWindow détectée.
- [x] Clavier déjà ouvert : conserver placement manuel valable, recaler s'il masque la grille.
- [ ] Test JVM complet et `assemblePhone` GREEN sur nouveau commit.
- [ ] Vérification réelle Samsung : ouverture directe sans action utilisateur, neuf lignes visibles, quatre panneaux utilisables, taille visuelle proche de la 2e capture, y compris nouvelle case, rotation, Pierre.
- [ ] APK de test et SHA GitHub du nouveau build.

Ne pas annoncer « corrigé sur téléphone » avant la recette Fab.
