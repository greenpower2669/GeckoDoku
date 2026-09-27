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


<!-- GECKO-038-CI177-GREEN-2026-09-27 -->
- [x] CI #177 GREEN complet.
- [x] APK 0.11.2-dev produit.
- [x] AAB 0.11.2-dev produit.
- [ ] Fab : test grille 3 px.
- [ ] Fab : test candidats noirs.
- [ ] Fab : test Prof tap / retap / long press.
- [ ] Fab : test Undo coup Prof.
- [ ] Fab : test Gecko-repère, animation et suppression.
- [ ] Fab : test Animations OFF = Gecko-repère statique.
- [ ] Fab : contrôle régression GeckoDoku historique.

<!-- GECKO-039-MESSAGE-INVARIANT-2026-09-27 -->
- [x] Retirer `Prof Gecko :` des explications Sudoku.
- [x] Retirer `Prof Gecko •` des status Sudoku concernés.
- [x] Invariant multi-mode documenté.
- [ ] Ajouter test source/contrat empêchant un futur préfixe redondant.
- [ ] Appliquer le même invariant à toute future banque de phrases Gomoku.



<!-- GECKO-039-TAP-GECKO-RED-2026-09-27 -->
- [x] RED tap simple case vide → Gecko-repère.
- [x] RED given/filled → sélection seule.
- [x] RED suppression préfixe Prof en tête seulement.
- [ ] Confirmer RED CI.
- [ ] Implémenter policies.
- [ ] Câbler tap simple MainActivity.
- [ ] Normaliser tous les chemins de parole Prof sans casser bulle↔voix.
- [ ] CI/APK/AAB.


<!-- GECKO-039-TAP-GECKO-GREEN-2026-09-27 -->
- [x] SudokuCellTapPolicy implémentée.
- [x] Tap simple case vide → toggle Gecko-repère.
- [x] Given / case remplie → sélection seule.
- [x] Long press case → palette inchangée.
- [x] ProfessorDialogTextPolicy centralisée.
- [x] Même texte normalisé pour bulle et voix.
- [x] Branche GECKO-039 ajoutée à la CI.
- [ ] CI GREEN.
- [ ] Version candidate + APK/AAB.
- [ ] Test téléphone Fab.


<!-- GECKO-039-COMPILE-FIX-2026-09-27 -->
- [x] Corriger compilation status « N posé » après retrait du préfixe Prof.
- [ ] CI suivante GREEN.


<!-- GECKO-039-CANDIDATE-0113-2026-09-27 -->
## Validation téléphone 0.11.3-dev
- [x] CI #179 GREEN avant bump version.
- [x] Candidate 0.11.3-dev / code 31 préparée.
- [ ] CI candidate versionnée GREEN.
- [ ] Tap case vide → Gecko apparaît.
- [ ] Retap même case → Gecko disparaît.
- [ ] Tap given → aucun Gecko.
- [ ] Tap case remplie → aucun Gecko.
- [ ] Long press → palette toujours disponible.
- [ ] Gecko animé si Animations ON, statique si OFF.
- [ ] Bulbe Prof : aucun préfixe « Prof Gecko : » redondant.
- [ ] Status Sudoku : aucun préfixe « Prof Gecko • » redondant.
- [ ] Aucun code Gomoku avant GO distinct.


<!-- GECKO-039-CI180-GREEN-2026-09-27 -->
- [x] CI candidate #180 GREEN.
- [x] APK 0.11.3-dev produit.
- [x] AAB 0.11.3-dev produit.
- [ ] Test téléphone : tap vide pose Gecko.
- [ ] Test téléphone : retap retire Gecko.
- [ ] Test téléphone : given/remplie protégées.
- [ ] Test téléphone : long press palette inchangé.
- [ ] Test téléphone : bulle Prof sans préfixe redondant.
- [ ] Après validation Fab : clôturer PARTIE A/B.
- [ ] Gomoku : attendre GO distinct avant tout code.

<!-- GECKO-039-MISSION-CLEANUP-ANIM-TITLE-2026-09-27 -->
## GECKO-039 — reste à faire
- [x] Tap case vide → Gecko-repère.
- [x] Retap → retrait Gecko-repère.
- [x] Prof Sudoku tap/retap/long press.
- [x] Préfixes Prof redondants corrigés.
- [ ] Auditer le pipeline d'animations Gecko du mode classique.
- [ ] Réutiliser les animations classiques pour le Gecko-repère Sudoku.
- [ ] Préserver candidats noirs / tactile / Animations OFF.
- [ ] Corriger titre Sudoku : `GeckoDoku 🦎`.
- [ ] Auditer que les autres modes n'ajoutent pas leur nom dans le titre principal.
- [ ] Tests / CI / APK / AAB.
- [ ] Test téléphone Fab.
- [ ] Gomoku : NE PAS CODER avant GO distinct.

<!-- GECKO-039-DOUBLE-TAP-MARKERS-PROF-REASONING-2026-09-27 -->
## GECKO-039 — nouveaux travaux
- [ ] Double tap case Sudoku → ouvrir clavier / palette des repères personnels.
- [ ] Vérifier qu'un double tap ne déclenche pas deux toggles Gecko.
- [ ] Conserver tap simple Gecko-repère et long press palette Sudoku.
- [ ] Auditer/réutiliser le système de repères personnels du mode classique.
- [ ] Définir un modèle pur de ReasoningTrace Sudoku.
- [ ] Enrichir les hints pour conserver sources, projections et éliminations réelles.
- [ ] Créer la projection graphique synchronisée avec ReasoningTrace.
- [ ] Créer la narration détaillée à partir de la même ReasoningTrace.
- [ ] Layout large : projection à gauche / texte à droite.
- [ ] Layout petit écran : projection au-dessus / texte en dessous.
- [ ] Accessibilité projections : traits/flèches/croix/hachures, pas couleur seule.
- [ ] Tester que texte et graphique décrivent exactement les mêmes étapes.
- [ ] Tester que le Prof ne saute jamais directement à la conclusion quand une explication détaillée est disponible.

<!-- GECKO-039-THREE-PHASE-PLAN-2026-09-27 -->
# GECKO-039 — TODO restructuré

## Phase 1 Classic
- [ ] Auditer géométrie actuelle Classic.
- [ ] RED grille Classic plus large sans chevauchement.
- [ ] Agrandir la grille Classic.
- [ ] Auditer animations Gecko historiques.
- [ ] Extraire/mutualiser pipeline animation sans régression.
- [ ] Auditer repères personnels Classic.
- [ ] CI GREEN + candidate A.
- [ ] Test téléphone Fab.

## Phase 2 Sudoku
- [x] Tap / retap Gecko-repère.
- [x] Prof tap / retap / long press.
- [x] Préfixes Prof redondants nettoyés.
- [ ] Réutiliser pipeline animation Classic pour Gecko-repère.
- [ ] Corriger titre en `GeckoDoku 🦎`.
- [ ] Double tap → clavier/palette repères personnels.
- [ ] Garder long press palette Sudoku.
- [ ] Construire `SudokuReasoningTrace`.
- [ ] Produire graphique et narration depuis la même trace.
- [ ] Layout horizontal/vertical adaptatif.
- [ ] Accessibilité projections.
- [ ] CI GREEN + candidate B.
- [ ] Test téléphone Fab.

## Phase 3 Gomoku
- [ ] Ajouter `GameMode.GOMOKU`.
- [ ] Moteur Gomoku indépendant.
- [ ] Plateau logique paramétrable et > 12×12.
- [ ] Viewport visible exactement 12×12.
- [ ] Drag/pan borné sur plateau.
- [ ] Policy tap vs drag.
- [ ] Gecko vert joueur.
- [ ] Même asset filtré jaune pour Pierre.
- [ ] Détection 5 alignés dans 4 directions.
- [ ] IA priorités gagner/bloquer/4/3/proximité.
- [ ] Tour joueur ↔ Prof.
- [ ] Recentrage sur coup Prof si hors viewport utile.
- [ ] Recentrage sur alignement gagnant hors viewport.
- [ ] Pipeline animations partagé.
- [ ] Personnalité / dialogue Prof.
- [ ] Accessibilité vert/jaune + grandes cibles.
- [ ] CI GREEN + candidate C.
- [ ] Test téléphone Fab.

<!-- GECKO-039-GOMOKU-ZOOM-DIFFICULTY-2026-09-27 -->
## Phase 3 Gomoku — zoom
- [ ] Viewport 12×12 comme zoom de référence.
- [ ] Pinch zoom-in / zoom-out.
- [ ] Bornes min/max de zoom.
- [ ] Zoom autour du point focal sous les doigts.
- [ ] Pinch ne pose jamais de Gecko.
- [ ] Pan + zoom compatibles.
- [ ] IA indépendante du viewport/zoom.
- [ ] Accessibilité zoom testée.

## Phase 3 Gomoku — difficulté stratégique
- [ ] Définir abstraction de profondeur/horizon IA.
- [ ] Niveau bas : tactique immédiate.
- [ ] Niveau intermédiaire : projection courte.
- [ ] Niveau fort : séquences multi-coups / doubles menaces.
- [ ] Niveau très fort : pièges / appâts / réponses forcées / contre-pièges.
- [ ] Aucun niveau ne triche.
- [ ] Mapper les difficultés existantes de l'app vers ces comportements.
- [ ] Mesurer performances Android avant choix final minimax/negamax/alpha-beta.
- [ ] Produire des explications Prof à partir de la vraie analyse tactique lorsque possible.



<!-- GECKO-039-PHASE1-RED-2026-09-27 -->
- [x] RED grille Classic plus large.
- [ ] Confirmer RED CI.
- [ ] Implémenter policy et GeckoBoardView.
- [ ] Mutualiser pipeline animation sans régression.
- [ ] CI GREEN Phase 1.


<!-- GECKO-039-PHASE1-GREEN-2026-09-27 -->
- [x] Policy grille Classic plus large.
- [x] GeckoBoardView marges compactes.
- [x] Pipeline animations Classic mutualisé.
- [ ] CI GREEN Phase 1.
- [ ] Candidate/test téléphone après jalon global si nécessaire.


<!-- GECKO-039-PHASE2-RED-2026-09-27 -->
- [x] RED repères personnels Sudoku.
- [x] RED double-tap vs long press.
- [x] RED titre multi-mode.
- [x] RED trace raisonnement Prof.
- [ ] Confirmer RED CI.
- [ ] GREEN Phase 2.


<!-- GECKO-039-PHASE2-MODELS-2026-09-27 -->
- [x] Modèles/policies Phase 2.
- [ ] Moteur repères personnels.
- [ ] HintEngine produit ReasoningTrace.
- [ ] UI Phase 2.


<!-- GECKO-039-PHASE2-ENGINE-TRACE-2026-09-27 -->
- [x] Moteur repères personnels.
- [x] HintEngine ReasoningTrace réelle.
- [ ] UI double tap/rendu repères.
- [ ] UI projection/speech sequence.
- [ ] Animation Classic sur Gecko-repère.


<!-- GECKO-039-PHASE2-VIEWS-2026-09-27 -->
- [x] Gestes view single/double/long distincts.
- [x] Rendu repères personnels.
- [x] Overlay raisonnement accessible par traits/croix.
- [ ] Câblage MainActivity + animation Classic + séquence voix/bulle.


<!-- GECKO-039-PHASE2-MAIN-WIRING-2026-09-27 -->
- [x] Double tap repères personnels.
- [x] Animation Classic sur Gecko Sudoku.
- [x] Titre invariant.
- [x] Séquence raisonnement overlay+bulle+voix.
- [ ] CI GREEN Phase 2.


<!-- GECKO-039-PHASE2-COMPILE-FIX-2026-09-27 -->
- [x] Corriger customMarkerAt dans SudokuSnapshot.
- [ ] CI Phase 2 GREEN.


<!-- GECKO-039-PHASE3-RED-2026-09-27 -->
- [x] RED moteur Gomoku.
- [x] RED viewport 12/pan/zoom.
- [x] RED difficulté stratégique/IA.
- [ ] Confirmer RED CI.
- [ ] GREEN core Gomoku.
- [ ] Ajouter mode/UI/rendu jaune.


<!-- GECKO-039-GOMOKU-CORE-GREEN-2026-09-27 -->
- [x] GameMode.GOMOKU.
- [x] GomokuGameEngine + snapshot + builder.
- [x] WinDetector 5+.
- [x] Viewport 12×12 + pan + zoom.
- [x] Profils difficulté / profondeur / traps.
- [x] IA victoire/blocage/menaces + alpha-beta borné.
- [ ] CI core GREEN.
- [ ] Vue Android Gomoku.
- [ ] Câblage MainActivity / mode chooser.
- [ ] Gecko jaune dynamique.
- [ ] Gestes pan/pinch/tap.
- [ ] Dialogues Prof Gomoku.


<!-- GECKO-039-GOMOKU-GEOMETRY-COMPILE-FIX-2026-09-27 -->
- [x] Ajouter branche géométrie GOMOKU après CI #190.
- [ ] Revalider core Gomoku GREEN.


<!-- GECKO-039-THREE-MODE-TEST-FIX-2026-09-27 -->
- [x] Mettre à jour le test historique de 2 à 3 modes après CI #191.
- [ ] Revalider core Gomoku.


<!-- GECKO-039-GOMOKU-UI-RED-2026-09-27 -->
- [x] Core Gomoku CI #192 GREEN.
- [x] RED gesture tap/pan/pinch.
- [x] RED filtre jaune/alpha.
- [x] RED difficulté réglages Gomoku.
- [ ] Implémenter policies UI.
- [ ] Créer GomokuBoardView.
- [ ] Câbler MainActivity.


<!-- GECKO-039-GOMOKU-UI-POLICIES-GREEN-2026-09-27 -->
- [x] GomokuGesturePolicy.
- [x] GomokuYellowFilterPolicy.
- [x] GomokuBoardLayoutPolicy.
- [x] Difficulté settings Gomoku.
- [ ] CI policies GREEN.
- [ ] GomokuBoardView + MainActivity.

<!-- GECKO-039-GOMOKU-BOARDVIEW-2026-09-27 -->
- [x] GomokuBoardView rendu plateau.
- [x] Pan + pinch zoom + tap séparés dans la vue.
- [x] Même sprite filtré jaune pour Prof.
- [x] Highlight victoire + animation légère de pose.
- [ ] Câbler vue dans MainActivity.
- [ ] Tour joueur / bouton Prof / recentrage.
- [ ] Shared rich-media animation joueur.
- [ ] CI de cette brique.

<!-- GECKO-039-GOMOKU-MAINACTIVITY-2026-09-27 -->
- [x] GomokuBoardView CI #195 GREEN.
- [x] Sélecteur 3 modes.
- [x] Nouvelle/Rejouer Gomoku.
- [x] Visibilité/layout Gomoku séparés.
- [x] Tap joueur vert.
- [x] Bouton Prof → IA asynchrone → Gecko jaune.
- [x] Recentrage coup Prof hors écran.
- [x] Victoire/draw + ligne gagnante.
- [ ] CI intégration MainActivity.
- [ ] Vérifier performances IA haut niveau.
- [ ] Candidate APK/AAB et test téléphone.

<!-- GECKO-039-CANDIDATE-0120-2026-09-27 -->
## Validation téléphone 0.12.0-dev
- [x] Classic Phase 1 GREEN.
- [x] Sudoku Phase 2 GREEN.
- [x] Gomoku core GREEN.
- [x] Gomoku vue GREEN #195.
- [x] Gomoku intégration GREEN #196.
- [x] Candidate 0.12.0-dev / code32 préparée.
- [ ] CI candidate versionnée GREEN.
- [ ] Fab : Classic grille plus large.
- [ ] Fab : Sudoku animations Gecko Classic.
- [ ] Fab : Sudoku double tap repères personnels.
- [ ] Fab : Prof Sudoku projection + explication synchronisées.
- [ ] Fab : choisir le 3e mode Gomoku.
- [ ] Fab : vérifier vue ~12×12.
- [ ] Fab : drag/pan.
- [ ] Fab : pinch zoom-in / zoom-out.
- [ ] Fab : tap pose Gecko vert sans conflit avec drag.
- [ ] Fab : bouton Prof pose Gecko jaune.
- [ ] Fab : vérifier difficulté basse vs haute.
- [ ] Fab : vérifier fluidité IA niveau Infernal.
- [ ] Fab : victoire 5 alignés + recentrage.

<!-- GECKO-039-CI197-GREEN-2026-09-27 -->
- [x] CI candidate #197 GREEN.
- [x] APK 0.12.0-dev produit.
- [x] AAB 0.12.0-dev produit.
- [ ] Test téléphone Fab des trois modes.


<!-- GECKO-039-POST-TEST-TODO-2026-09-27 -->
# TODO actif — passe debug téléphone

- [ ] Ajouter configuration Gomoku VS_PROFESSOR / HUMAN_VS_HUMAN au choix de mode sans dupliquer le moteur.
- [ ] Remplacer les chaînes UI Gomoku « Pierre » par « Prof Gecko ».
- [ ] VS_PROFESSOR : jouer automatiquement le tour jaune après le vert.
- [ ] Prof clic court : conseil sans coup.
- [ ] Prof long VS_PROFESSOR : analyse vert + joue vert pour humain + enchaîne jaune automatique.
- [ ] Prof long HUMAN_VS_HUMAN : analyse approfondie seulement, aucun coup.
- [ ] HUMAN_VS_HUMAN : alternance vert/jaune humaine et aucune IA automatique.
- [ ] Ne pas griser Prof quand il peut conseiller.
- [ ] Rendre les targets vidéo Gomoku dynamiques pendant pan/zoom.
- [ ] Supprimer le rectangle de masquage en Gomoku en masquant temporairement le PNG statique.
- [ ] Teinter la vidéo du camp jaune.
- [ ] Ajouter tests unitaires pour match mode, analyse par camp et non-régression.
- [ ] CI GREEN.
- [ ] APK/AAB candidate puis test téléphone Fab.
- [ ] Après validation téléphone : nettoyer l'ordre de mission et réduire ce TODO aux restes réels.


<!-- GECKO-039-CORE-MATCH-MODES-TODO-2026-09-27 -->
- [x] Modèle persistant VS_PROFESSOR / HUMAN_VS_HUMAN sans dupliquer le moteur.
- [x] IA capable d'analyser explicitement le camp vert ou jaune.
- [x] Tests de politique des deux variantes.
- [ ] Câbler les variantes et interactions Prof dans MainActivity.
- [ ] Corriger le suivi dynamique des vidéos Gomoku + teinte jaune.
- [ ] CI GREEN + APK/AAB + test téléphone.


<!-- GECKO-039-MEDIA-DYNAMIC-TODO-2026-09-27 -->
- [x] Infrastructure target vidéo dynamique.
- [x] Rect vidéo calé sur le rect PNG réel.
- [x] Masquage ciblé du PNG sans rectangle de fond.
- [x] Teinte vidéo jaune optionnelle.
- [ ] Câbler ces fonctions dans MainActivity Gomoku.
- [ ] Vérifier CI puis test téléphone pan/zoom/keycolor/jaune.
