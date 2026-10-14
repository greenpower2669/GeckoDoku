# MISSION GECKO-SUDOKU-MANUAL-PROJECTION-006
## Projection humaine à axe unique, hypothèse à deux axes
Date : 14 octobre 2026.
Branche de travail : `feature/sudoku-visual-reasoning-v1`.
Ordre Fab : « Parfait, code ça ». Ne pas merger main, ne pas créer de Release, ne pas publier Play.

### Diagnostic (capture Samsung)
Le mode manuel PROJECTION dans `SudokuValueOverlayView.drawManualPlayerAnnotations()` appelait toujours les deux `canvas.drawLine` (ligne ET colonne) pour chaque chiffre choisi, quel que soit l'alignement réel des repères humains. Le statut annonçait erronément « croix ligne-colonne » ; cela confond observation d'un alignement et conséquence d'une hypothèse.

### Invariants fonctionnels
- PROJECTION MANUELLE : un SEUL axe pour un même chiffre.
  - Plusieurs repères humains du même chiffre placés sur une même ligne : projection sur cette ligne seulement.
  - Plusieurs repères du même chiffre placés sur une même colonne : projection sur cette colonne seulement.
  - Repère isolé : ligne par défaut ; jamais de croix.
  - Si un repère possède des voisins horizontaux et verticaux : choisir l'axe avec le plus de voisins de même chiffre ; égalité = ligne. Ne pas afficher de croix. Aucun autre chiffre ne doit influencer l'axe choisi.
- HYPOTHÈSE RÉELLEMENT POSÉE via le moteur Sudoku : afficher une croix discrète ligne + colonne, en conservant le marquage d'hypothèse existant.
- Les projections manuelles ne sont pas des éliminations ni des preuves. Ne pas affecter candidats, solveur, valeur, sauvegarde, aides automatiques, couples, triplets ou Hall.
- Lorsque plusieurs repères d'un chiffre sont ajoutés, la direction est recalculée au rendu sans modifier l'état du plateau.

### Modifications implémentées
1. `SudokuVisualReasoning.kt` : `SudokuManualProjectionPolicy.axesFor()`, politique pure testable avec `SudokuManualProjectionAxis`.
2. `SudokuValueOverlayView.kt` : plus de croix systématique des repères humains ; seule l'orientation renvoyée par la politique est tracée. Une hypothèse active `snapshot.hypothesisAt(cell)` projette les deux axes avec traits translucides sous les chiffres du plateau.
3. `MainActivity.kt` : messages Projection adaptés à la direction réelle, sans annoncer une croix.
4. `SudokuVisualReasoningTest.kt` : tests ligne, colonne, repère isolé, coexistence de chiffres, égalité d'axes, hypothèse et séparation des types de marqueurs.

### État et validation
- [x] Implémentation de la distinction repère humain / hypothèse.
- [x] Tests JVM unitaires écrits.
- [ ] CI `testDebugUnitTest + assemblePhone` à confirmer sur le dernier commit.
- [ ] Artefact APK téléphone, modèle Pierre et sprites 240p à confirmer.
- [ ] Sur Samsung : contrôler projection horizontale avec deux repères du même chiffre alignés ; colonne avec deux repères verticaux ; hypothèse réelle → croix ; outil Candidat n'active pas de projection ; retrait d'un repère met à jour l'affichage ; retour Pierre OK.
- [ ] Contrôler qu'aucun autre outil ni score n'a changé et que le correctif précédent de navigation Android reste fonctionnel.

**Interdits :** aucun merge main, aucune Release, aucun Play, aucune modification de signature, package, médias ni gameplay sans ordre explicite de Fab.
