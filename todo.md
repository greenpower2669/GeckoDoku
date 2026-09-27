# TODO — GeckoDoku

## Mission active documentaire

GECKO-038 — UX Sudoku tactile / candidats vivants.

STATUT : spécification prête, code non lancé dans cette intervention.

## Retour téléphone déjà acquis

- [x] Mode Sudoku visible sur téléphone.
- [x] Grille 9×9 visible.
- [x] Notes / Effacer / Undo / Redo visibles.
- [x] Défaut constaté : pavé 1–9 non visible.
- [x] Défaut constaté : sélecteur 3 états non visible.
- [x] Gros bouton Difficulté identifié comme consommation verticale inutile.
- [x] Issue intemporelle Pierre ↔ bulle validée téléphone.

## Prochaine mission de code — après GO Fab

- [ ] Relire HEAD réel de `gecko-038-sudoku-mode`.
- [ ] Audit géométrie et superposition téléphone.
- [ ] Ajouter RED garantissant pavé et sélecteur non recouverts.
- [ ] Déplacer Difficulté dans ⚙️.
- [ ] Retirer le gros bouton Difficulté de l'écran Sudoku.
- [ ] Réserver explicitement l'espace du sélecteur 3 états.
- [ ] Réserver explicitement l'espace du pavé 1–9.
- [ ] Conserver Notes / Effacer / Undo / Redo.
- [ ] Ajouter appui long sur case Sudoku.
- [ ] Ajouter palette locale tactile 1–9 / candidats / effacer.
- [ ] Afficher 9 mini-candidats maximum en grille 3×3 dans la case.
- [ ] Rendre les candidats selon CLASSIC / GECKO_NB / GECKO_COLORED.
- [ ] Réutiliser les PNG canoniques sans conversion.
- [ ] Faire utiliser le même modèle de candidats au joueur et au Prof.
- [ ] Définir explicitement la politique Undo/Redo des actions du Prof.
- [ ] Vérifier popup sans reflow et positionnement intelligent.
- [ ] Préserver clavier physique.
- [ ] Tests unitaires/policies.
- [ ] CI GREEN.
- [ ] APK/AAB.
- [ ] Validation téléphone Fab.

## Garde-fous permanents

- [ ] Toute nouvelle parole de Pierre affiche exactement son texte dans `ProfessorBubbleView`.
- [ ] Une anomalie Sudoku ne se corrige pas en régressant `GeckoBoardView`.
- [ ] VisualStyle ne modifie jamais les données Sudoku.
- [ ] Une couche visuelle ne bloque pas les touches.
- [ ] Une popup / bulle / overlay ne modifie pas la géométrie de la grille.
- [ ] Les candidats restent dans leur case.
- [ ] Les candidats suivent le style visuel actif.

## Interdictions

- [ ] Pas de merge `main` sans GO explicite Fab.
- [ ] Pas de release sans GO explicite Fab.


<!-- GECKO-038-TACTILE-RED-2026-09-27 -->
- [x] GO code reçu.
- [x] Cause géométrie identifiée.
- [x] Politique candidats Prof décidée : transitoire, même renderer, pas Undo joueur.
- [x] RED géométrie / candidats / popup / settings posé.
- [ ] Confirmer RED CI.
- [ ] GREEN géométrie + difficulté Settings.
- [ ] GREEN long press + palette.
- [ ] GREEN candidats 3 styles + Prof.
- [ ] CI/APK/AAB + téléphone Fab.


<!-- GECKO-038-TACTILE-GREEN-CORE-2026-09-27 -->
- [x] Policies géométrie/candidats/popup ajoutées.
- [x] Renderer de chiffres/geckos commun ajouté.
- [x] Vue palette long press ajoutée.
- [ ] Câbler SudokuBoardView long press.
- [ ] Déplacer rendu notes dans overlay stylé.
- [ ] Câbler Settings difficulté + géométrie par mode.


<!-- GECKO-038-TACTILE-INPUT-SETTINGS-2026-09-27 -->
- [x] SudokuBoardView tap + long press.
- [x] Difficulté disponible via policy Settings Sudoku.
- [ ] Overlay candidats stylés.
- [ ] MainActivity/popup/géométrie.
