# Debug historical — GeckoDoku

## 2026-09-25 — Grille initiale non unique
Cause : absence de validateur exhaustif. Correction : unicité vérifiée, puis pipeline de génération sécurisé.

## 2026-09-25 — Workflow APK absent
Cause : workflow non commité. Correction : GitHub Actions ajouté ; v0.1 et v0.2 ont construit avec succès.

## 2026-09-25 — Deux clics simples transformaient une croix en gecko
Cause : GameEngine.tap mélangeait les intentions. Correction v0.2 : toggleCross/toggleGecko séparés + onSingleTapConfirmed/onDoubleTap.

## 2026-09-25 — Unique ≠ humainement résoluble
Cause : seul le nombre de solutions était contrôlé. Correction : HumanSolver explicable.

## 2026-09-25 — Difficultés v0.2 peu fidèles au ressenti
### Symptôme
Le niveau affiché reposait encore trop sur une heuristique et sur la technique maximale autorisée, pas sur la preuve qu'une technique était nécessaire.
### Retour humain
Expert doit signifier : impossible sans X-Wing.
Démentiel doit demander une combinaison de raisonnements, notamment X-Wing et déduction/projection de zone.
Le nombre de déductions de couleur doit augmenter avec la difficulté.

### Correction v0.3
DifficultyIndexer compare plusieurs résolutions où certaines familles de techniques sont volontairement désactivées.
- Expert seulement si sans X-Wing le solveur bloque.
- Démentiel seulement si sans X-Wing il bloque ET si sans projection de zone il bloque.
- Facile/Réflexion/Difficile séparés par le nombre de déductions de zone nécessaires dans le chemin trouvé.

## 2026-09-25 — Déduction de projection de zone manquante
### Exemple
Une zone n'a plus que 2 cases possibles sur une ligne. Le gecko sera forcément dans l'une d'elles. Une case située au-dessus ou dessous qui toucherait les deux possibilités est donc impossible, même sans connaître laquelle contient le gecko.
### Correction
REGION_TOUCH_PROJECTION généralise ce raisonnement à 2..4 candidats : une case extérieure touchant toutes les possibilités de la zone est éliminée.

## 2026-09-25 — Gecko X-Wing enrichi
Ajout du X-Wing lignes/colonnes et d'un équivalent par paires de zones confinées dans exactement deux lignes ou colonnes.

## 2026-09-25 — Passage 5..12
Le pré-calcul de toutes les permutations ne convient plus aux grandes tailles. v0.3 passe à un backtracking aléatoire pour créer une solution et à un comptage de solutions borné à 2.

## Vigilances v0.3
- temps de génération 10×10 à 12×12 sur téléphone ;
- rareté possible d'Expert/Démentiel sur certaines petites tailles ;
- si niveau exact non trouvé, l'UI doit annoncer le niveau réellement mesuré ;
- vérifier visuellement les 12 couleurs ;
- valider que les nouvelles déductions correspondent au raisonnement humain ;
- garder le double-clic fiable.
