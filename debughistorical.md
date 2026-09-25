# Debug historical — GeckoDoku

## 2026-09-25 — Initialisation
Dépôt initialement presque vide. Mise en place Android, moteur, FX et mémoires FAB Copilot.

## 2026-09-25 — Grille démo non unique
### Symptôme
La première carte 5×5 admettait plusieurs solutions.
### Cause
Unicité non vérifiée avant intégration.
### Correction
Énumération exhaustive et remplacement par une carte unique.
### Non-régression
Toute grille passe countSolutions avec arrêt à 2.

## 2026-09-25 — Workflow APK absent
### Symptôme
Aucune Action visible.
### Cause
La tentative de création du workflow avait été bloquée avant commit.
### Correction
Workflow GitHub Actions ajouté ; run v0.1 terminé avec succès et APK produit.

## 2026-09-25 — Geste croix → gecko incorrect
### Symptôme observé sur téléphone
Un premier clic posait une croix, puis un second clic normal sur la même case pouvait poser un gecko, même sans vrai double-clic.
### Cause racine
GameEngine.tap() codait volontairement la séquence croix puis gecko. La vue utilisait onSingleTapUp et ne distinguait pas une intention double-clic.
### Correction v0.2
- suppression du comportement tap séquentiel ;
- toggleCross() séparé de toggleGecko() ;
- onSingleTapConfirmed pour la croix ;
- onDoubleTap pour le gecko.
### À valider
Tempo réel du double-clic sur téléphone et confort avec déficience visuelle.

## 2026-09-25 — Unique ne veut pas dire humainement résoluble
### Symptôme observé
Première grille ressentie comme insoluble et sans point d'entrée évident.
### Cause
Le validateur testait seulement l'unicité mathématique, pas le chemin de déduction.
### Correction v0.2
Ajout HumanSolver et sélection de givens jusqu'à ce que la grille soit résoluble avec les techniques autorisées. Techniques : singles, interactions ligne/zone, X-Wing.
### Vigilance
Le solveur humain est une première formalisation. Les tests de Fab restent la référence pour le ressenti réel de difficulté.

## 2026-09-25 — Taille/difficulté/stats
Ajout tailles 5..8, quatre difficultés et stats SharedPreferences locales. Aucune synchronisation réseau.

## Toujours à vérifier v0.2
- compilation ;
- performance génération surtout 8×8 ;
- distinction clic simple / double-clic ;
- difficulté vécue vs index ;
- qualité des givens de départ ;
- stats après plusieurs parties ;
- volume FX et fatigue visuelle.
