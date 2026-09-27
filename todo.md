# TODO — GeckoDoku

## État actuel

Aucune mission de code active.

GECKO-038 est techniquement gelé sur la candidate **0.11.0-dev / code 28**, CI **#154 GREEN**, en attente du retour téléphone de Fab.

## Validation téléphone à faire

- [ ] Vérifier que le mode GeckoDoku historique est inchangé.
- [ ] Ouvrir Réglages → Mode de jeu → Sudoku.
- [ ] Vérifier la taille et la lisibilité de la grille 9×9.
- [ ] Tester sélection de case + saisie 1..9.
- [ ] Tester Notes ✏️.
- [ ] Tester Effacer.
- [ ] Tester Undo / Redo.
- [ ] Tester Prof Gecko en Sudoku.
- [ ] Vérifier Pierre / bulle / ProfParle.
- [ ] Glisser le sélecteur gauche ↔ milieu ↔ droite sans relever le doigt.
- [ ] Vérifier que la grille change en direct pendant le glissement.
- [ ] Vérifier le style Classic.
- [ ] Vérifier le cadrage Gecko N/B.
- [ ] Vérifier le cadrage Gecko couleur.
- [ ] Vérifier qu'aucune couche Gecko ne bloque les touches.
- [ ] Vérifier qu'aucune régression Intro / audio / célébration n'apparaît.

## Après le retour Fab

- [ ] Corriger uniquement les défauts réellement observés.
- [ ] Revalider CI après toute correction.
- [ ] Décider ensuite seulement d'une nouvelle mission : pédagogie Sudoku avancée, animations Gecko, ergonomie ou autre.

## Interdictions actuelles

- [ ] Ne pas fusionner dans `main` sans validation explicite de Fab.
- [ ] Ne pas publier de release sans validation explicite de Fab.
- [ ] Ne pas empiler de nouvelles fonctionnalités avant le retour téléphone.

Le détail fonctionnel complet de GECKO-038 est désormais canonique dans `brain.md`.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-RED-2026-09-27 -->
## Garde-fou permanent — dialogue Prof
- [ ] RED confirmé pour le contrat parole simple.
- [ ] Factoriser AMBIENT fixe + STATS + moteur 309 vers un chemin simple partagé.
- [ ] Garantir bubbleText == speechText.
- [ ] Garantir status court.
- [ ] Garantir origine SpeechOrigin conservée.
- [ ] Garantir aucune bulle orpheline sur refus.
- [ ] Garantir callback réel + ~1 s + token générationnel.
- [ ] Réauditer tous les chemins de parole après correction.
- [ ] CI tests/APK/AAB GREEN.
- [x] Validation téléphone Fab — issue Pierre ↔ bulle : OK.
- [ ] **Permanent : toute nouvelle parole de Pierre doit passer par ProfessorBubbleView.**

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-GREEN-CODE-2026-09-27 -->
- [x] RED #155 confirmé.
- [x] Coordinator pur parole simple.
- [x] AMBIENT fixe → bulle.
- [x] STATS → bulle.
- [x] moteur 309 → même helper commun.
- [x] Refus tardif → fermeture sûre de la bulle courante seulement.
- [x] Prof pédagogique laissé hors auto-close.
- [ ] CI GREEN du correctif.
- [ ] Audit final exhaustif après CI.
- [ ] APK/AAB GREEN.
- [ ] Validation téléphone Fab.
- [ ] **Permanent : surveiller toute nouvelle parole de Pierre qui contournerait `speakSimpleProfessorBubble()` ou `showProfessorBubble()`.**

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-CI156-GREEN-2026-09-27 -->
## État issue intemporelle
- [x] Tests RED ajoutés.
- [x] RED #155 confirmé.
- [x] AMBIENT aide automatique synchronisé avec la bulle.
- [x] AMBIENT Sauver synchronisé avec la bulle.
- [x] STATS synchronisé avec la bulle.
- [x] Moteur 309 factorisé sur le même chemin simple.
- [x] Refus tardif nettoie seulement la bulle du token courant.
- [x] Callback obsolète protégé.
- [x] Délai de fermeture = vraie fin + 1 s.
- [x] Prof pédagogique hors auto-close.
- [x] Aucun reflow de plateau.
- [x] Audit final des chemins runtime.
- [x] CI #156 GREEN.
- [x] APK/AAB produits.
- [ ] Validation téléphone Fab.
- [ ] **GARDE-FOU PERMANENT : toute nouvelle parole de Pierre doit afficher exactement son texte dans `ProfessorBubbleView`.**

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-PHONE-VALIDATED-2026-09-27 -->
## Garde-fou permanent validé
- [x] Test téléphone Pierre ↔ bulle validé par Fab.
- [x] AMBIENT aide affiché dans la vraie bulle.
- [x] AMBIENT Sauver affiché dans la vraie bulle.
- [x] STATS affiché dans la vraie bulle.
- [x] Fermeture naturelle après fin réelle de Pierre.
- [ ] **Permanent : toute nouvelle parole de Pierre doit continuer à afficher exactement son texte dans `ProfessorBubbleView`.**

Aucune mission active pour cette issue.

