# GECKO-SUDOKU-KEYPAD-DOCK-008 — pavé sous la grille et plus de recouvrement

Retour Samsung de Fab (14 octobre 2026) : avec « Repères humains » placé correctement au-dessus du Sudoku, le clavier flottant bas-centre cache encore les dernières lignes de la grille.

## Cause constatée
Le pavé était borné à la fenêtre Android sûre (`systemBars`) et non à l'espace libre **sous** `sudokuBoard`. La hauteur initiale (~75 % de l'ancien pavé) était conservée même quand il ne restait pas assez de place ; les dernières rangées du plateau étaient masquées.

## Correction (push 2)
- `SudokuFloatingWindowGeometryPolicy.dockBelowBoard()` déduit une zone dédiée à partir du bas réel du plateau mesuré en coordonnées de fenêtre et de `sudokuPopupSafeBounds()`.
- `sudokuKeyboardDockBounds()` : la palette se place en bas-centre sous le Sudoku, avec écart, au-dessus des boutons de navigation Android ; la hauteur diminue avant tout chevauchement.
- Le geste de déplacement, le redimensionnement et le recalage d'une palette déjà ouverte respectent cette zone, y compris après modification des dimensions d'écran.
- Repères humains reste au-dessus, plateau inchangé et lisible. Les quatre zones du clavier (Choix, Candidats, Hypothèse, Prévisu) conservent leurs interactions ; calques Pierre inchangés.

## Recette
- [x] Politique de docking et tests JVM de non-chevauchement écrits.
- [ ] CI complète GREEN du dernier code, puis APK de test.
- [ ] Samsung en portrait : case de la 9e ligne entièrement visible, clavier dessous, boutons système non recouverts.
- [ ] Valider visuellement tailles tactiles et redimensionnement ; vérifier petites fenêtres / rotation. Si la hauteur libre est physiquement insuffisante en paysage, examiner un aménagement spécifique sans prétendre que grille et clavier peuvent rester grands simultanément.
- [ ] Contrôler Repères, Pierre, hypothèses, notes, autres modes.

Aucun merge main / Release / Play. Ne pas modifier package, signature, sprites 240p ou moteur de Sudoku.

### Preuve CI Samsung 007/008 — run #525
Validation finale après les missions 007 et 008 : run [#525](https://github.com/greenpower2669/GeckoDoku/actions/runs/38587086273), workflow 38587086273 **GREEN**, commit de build `eee5347f52602ccbc91d1359aeacb161c427d9f1`. `testDebugUnitTest + assemblePhone` réussis ; vérification Pierre Sherpa/Piper et banque sprites 240p réussie. Artefact `GeckoDoku-v0.15.45-dev-phone` ID `11905448115` ; APK `GeckoDoku-v0.15.45-dev.apk` SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`, contrôlé après extraction. La recette Samsung demeure nécessaire. Aucun merge main / Release / Play.
