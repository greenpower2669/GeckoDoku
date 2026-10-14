# GECKO-SUDOKU-INTRO-GATE-007 — repères cachés pendant l'introduction

Retour Samsung de Fab (14 octobre 2026) : la fenêtre « Repères humains » apparaît par-dessus la vidéo d'introduction, bien que la partie ne soit pas encore visible.

## Cause constatée
`refreshSudokuUi()` programmait systématiquement `showSudokuManualToolsPopup()` dès que le mode mémorisé était Sudoku ; le jeu et sa fenêtre étaient présents derrière l'introduction. Un callback différé pouvait rouvrir ce PopupWindow pendant la vidéo.

## Correction (push 1)
- `SudokuFloatingStartupPolicy.canOpen()` : garde pure sur mode Sudoku, `IntroPhase.DONE`, absence de vidéo `RichMediaKind.INTRO`, rideau de lancement masqué, plateau réellement visible et mesuré, absence de fermeture volontaire.
- `canShowSudokuManualTools()` vérifie aussi l'absence de parole Pierre et de suspension de fenêtres.
- Contrôle refait **au moment** de créer la fenêtre, pas seulement au moment de programmer le callback.
- À la fin d'introduction (fin naturelle ou saut), nouvelles tentatives différées après la fermeture effective de la couche vidéo ; layout/fin de parole peuvent relancer l'ouverture uniquement quand le plateau est jouable.
- `✎ Repères` conserve la réouverture manuelle, la position mémorisée et le fonctionnement de Pierre.

## Recette
- [x] Garde et test JVM écrits.
- [ ] CI complète GREEN avec le dernier code.
- [ ] Démarrage Samsung en mode Sudoku : intro sans fenêtres ; fin/saut intro : grille affichée puis Repères si non fermé.
- [ ] Reprise sans intro, changement de mode et fermeture/réouverture manuelle ; Pierre ne doit pas être masqué pendant sa parole.

Aucun merge main / Release / Play. Ne pas modifier les médias, sprites 240p, Pierre ni signature.
