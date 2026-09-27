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


<!-- GECKO-038-TACTILE-STYLED-CANDIDATES-2026-09-27 -->
- [x] Candidats Classic/NB/Color dans la grille.
- [x] Prof candidates via même renderer.
- [ ] MainActivity : fournir les candidats Prof et popup long press.


<!-- GECKO-038-TACTILE-MAIN-WIRING-2026-09-27 -->
- [x] Géométrie séparée par GameMode.
- [x] Difficulté déplacée vers ⚙️ en Sudoku.
- [x] Gros bouton Difficulté masqué en Sudoku.
- [x] Appui long → palette locale.
- [x] Valeur/candidat/effacer dans palette.
- [x] Candidats Prof transitoires via même renderer.
- [ ] CI GREEN.
- [ ] APK/AAB.
- [ ] Validation téléphone : pavé/selector visibles et popup confortable.


<!-- GECKO-038-TACTILE-RECT-IMPORT-2026-09-27 -->
- [x] Import Rect du sheet layout restauré.


<!-- GECKO-038-TACTILE-GEOMETRY-RESET-2026-09-27 -->
- [x] Compatibilité reset géométrie après insets.


<!-- GECKO-038-TACTILE-CANDIDATE-0111-2026-09-27 -->
- [x] CI #163 GREEN complet avant version bump.
- [x] Candidate 0.11.1-dev / code 29 préparée.
- [ ] CI candidate versionnée GREEN.
- [ ] Télécharger APK/AAB candidate.
- [ ] Test téléphone Fab : pavé visible.
- [ ] Test téléphone Fab : sélecteur visible.
- [ ] Test téléphone Fab : appui long/palette.
- [ ] Test téléphone Fab : candidats Classic/NB/Color.
- [ ] Test téléphone Fab : Prof candidats.


<!-- GECKO-038-TACTILE-CI164-GREEN-2026-09-27 -->
## Validation téléphone candidate 0.11.1-dev
- [x] CI #164 GREEN complet.
- [x] APK produit.
- [x] AAB produit.
- [ ] Vérifier que le pavé 1–9 est visible.
- [ ] Vérifier que le sélecteur 3 états est visible.
- [ ] Vérifier que le bouton Difficulté principal a disparu en Sudoku.
- [ ] Vérifier Difficulté dans ⚙️.
- [ ] Appui long sur case vide → palette locale.
- [ ] Tester valeur 1..9 depuis la palette.
- [ ] Tester plusieurs candidats depuis la palette sans fermer entre chaque candidat.
- [ ] Tester Effacer.
- [ ] Vérifier candidats Classic.
- [ ] Vérifier candidats Gecko N/B.
- [ ] Vérifier candidats Gecko couleur.
- [ ] Vérifier appui long sur given = blocage propre.
- [ ] Vérifier Prof Gecko : candidats affichés dans la case sans modifier Undo joueur.
- [ ] Vérifier GeckoDoku historique sans régression.

<!-- GECKO-038-GRID-READABILITY-REVISION-2026-09-27 -->
## Révision après test téléphone 0.11.1-dev
- [x] Constater : grille trop petite malgré contrôles visibles.
- [x] Décider : grille = priorité absolue de surface.
- [x] Décider : marge horizontale volontaire <= 3 px par côté.
- [x] Décider : mini-candidats en noir.
- [x] Annuler l'obligation de rendre les mini-candidats en Gecko/couleur.
- [ ] RED : géométrie Sudoku largeur utile - 6 px maximum.
- [ ] Corriger layout pour agrandir la grille avant les contrôles.
- [ ] Compacter les commandes qui consomment trop de hauteur.
- [ ] Rendre les candidats joueur en petits chiffres noirs.
- [ ] Rendre les candidats Prof en petits chiffres noirs.
- [ ] Préserver positions 3×3 et logique candidats.
- [ ] Vérifier long press/palette sans réduction de grille.
- [ ] CI GREEN.
- [ ] APK/AAB.
- [ ] Validation téléphone Fab : grille quasi plein écran en largeur.

<!-- GECKO-038-PROF-PLAY-GESTURE-2026-09-27 -->
## Prof Sudoku — explique puis joue
- [x] Premier tap = expliquer.
- [x] Second tap = jouer la même déduction encore valide.
- [x] Long press Prof = jouer directement une déduction sûre.
- [x] Long press case = palette locale, inchangé.
- [ ] Ajouter PendingProfessorSudokuMove ou abstraction équivalente.
- [ ] Ajouter provenance PLAYER / PROFESSOR.
- [ ] Invalider / revalider le pending sur mutations pertinentes.
- [ ] Second tap : revalider avant application.
- [ ] Long press Prof : recalculer avant application.
- [ ] Coup Prof dans Undo/Redo.
- [ ] Coup Prof non compté comme réussite autonome joueur.
- [ ] Nettoyer overlay Prof après application.
- [ ] Aucune déduction sûre → aucune mutation.
- [ ] Préserver invariant Pierre ↔ bulle.
- [ ] CI + APK/AAB + téléphone Fab.


<!-- GECKO-038-FULLWIDTH-PROF-RED-2026-09-27 -->
- [x] RED pleine largeur 3 px réel.
- [x] RED candidats noirs.
- [x] RED Prof tap/tap/long press.
- [x] RED provenance PROFESSOR + Undo/Redo.
- [ ] Confirmer RED CI.
- [ ] Implémenter policies + moteur.
- [ ] Câbler Activity + long press Prof.
- [ ] Compacter contrôles pour laisser la grille prioritaire.
- [ ] CI GREEN + APK/AAB.


<!-- GECKO-038-GREEN-POLICIES-2026-09-27 -->
- [x] Policies pleine largeur / candidats noirs / pending Prof ajoutées.
- [ ] Provenance moteur PLAYER/PROFESSOR.
- [ ] Renderer/vues.
- [ ] Activity.


<!-- GECKO-038-GREEN-MOVE-ORIGIN-2026-09-27 -->
- [x] Provenance PLAYER/PROFESSOR dans moteur.
- [x] Provenance conservée par Undo/Redo.
- [ ] Renderer/vues candidats noirs et marge 0.
- [ ] Activity Prof.


<!-- GECKO-038-GREEN-BLACK-CANDIDATES-2026-09-27 -->
- [x] Mini-candidats joueur/Prof noirs.
- [x] Overlay marge interne 0 px.
- [ ] SudokuBoardView marge interne 0.
- [ ] Palette candidats noirs.
- [ ] Activity/layout.


<!-- GECKO-038-GREEN-BOARD-PALETTE-2026-09-27 -->
- [x] BoardView marge interne 0.
- [x] Palette candidats noirs.
- [ ] Activity : plein largeur + contrôles compacts + Prof tap/tap/longpress.


<!-- GECKO-038-GREEN-FULLWIDTH-LAYOUT-2026-09-27 -->
- [x] MainActivity plein largeur 3px.
- [x] Anchor réserve le carré Sudoku.
- [x] Pavé permanent retiré au profit du long press.
- [x] Undo/Redo compacts + selector48.
- [ ] Câbler Prof tap/tap/longpress.
- [ ] CI GREEN.


<!-- GECKO-038-CANDIDATE-0112-2026-09-27 -->
## Validation téléphone 0.11.2-dev
- [x] Core grille pleine largeur GREEN.
- [x] Mini-candidats noirs GREEN.
- [x] Prof tap1 explique / tap2 joue GREEN.
- [x] Prof long press joue direct GREEN.
- [x] Provenance PROFESSOR + Undo/Redo GREEN.
- [x] Gecko-repère joueur Undo/Redo GREEN.
- [x] Gecko-repère animé si Animations ON, statique si OFF.
- [x] Palette Repère / Effacer testée par policy.
- [x] CI #176 GREEN complet.
- [ ] CI candidate 0.11.2-dev GREEN.
- [ ] Fab : vérifier marge grille ≈ 3 px par côté.
- [ ] Fab : vérifier candidats noirs lisibles.
- [ ] Fab : tester tap Prof puis retap.
- [ ] Fab : tester long press Prof.
- [ ] Fab : Undo après coup Prof.
- [ ] Fab : poser / retirer petit gecko-repère.
- [ ] Fab : vérifier son animation mignonne.
- [ ] Fab : vérifier Animations OFF = repère statique.
