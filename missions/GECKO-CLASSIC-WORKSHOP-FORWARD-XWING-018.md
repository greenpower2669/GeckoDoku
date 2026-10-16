# Mission 018 — GeckoDoku Atelier : construction par pose progressive et X-Wing restauré

## Autorisation
Fab valide « On tente et tu remet le xwing non? » après avoir demandé une construction en sens inverse (poser les Gecko au lieu de supprimer des indices). Ne pas confondre avec la précédente consigne « code pas » qui concernait uniquement la réflexion antérieure. Nouveau travail dans `feature/classic-workshop-forward-geckos-xwing-v5`, base `feature/classic-workshop-no-xwing-v4`, sans merge main.

## Architecture
- Conserver des **solutions secrètes complètes et légales** ainsi que les régions colorées déjà connues (un Gecko par ligne/colonne/région, sans contact).
- Débuter la **grille d'indices à zéro Gecko**. Pour un ordre aléatoire de positions de la solution secrète, poser 1 puis 2... puis N Gecko, sans jamais retirer l'un d'eux.
- Après chaque pose, vérifier de manière indépendante s'il existe **exactement une solution**. Quand l'unicité apparaît pour la première fois, tester la difficulté réelle. Si H2–H4 + règle X-Wing ne passent pas, explorer un autre ordre aléatoire ou une autre géométrie. Ne pas diffuser une grille facile/ambiguë comme Infernal.
- Recherche H2, puis H3 et H4 si besoin, preuves imbriquées, test H1 (si résolu à H1 -> rejet).
- Contrat X-Wing rétabli pour **8×8–12×12** (≥1 X-Wing présent dans la trace), facultatif **5×5–7×7**. Si modèle sans X-Wing résout une grande grille au même budget, rejeter ; si modèle comparatif indécis faute de temps, signaler l'incertitude, jamais prétendre une nécessité mathématique prouvée.
- Conserver la boucle FGS en continu, rotation 5..12, annulation et protections batterie/température, pas de promesse de recherche infinie Android.
- Statistiques distinctes v5 : recherches `attempts_forward_v5`, grilles `saved_forward_v5`, frontières d'unicité `unique_forward_v5`, plus détail par taille. Ne pas réinitialiser anciens compteurs ni bibliothèque/ZIP.
- Rétrocompatibilité : les candidats v4 restent lisibles dans l'atelier, même s'ils ne satisfont pas le nouveau contrat X-Wing pour promotion.
- Tests unitaires `WorkshopForwardPathTest.kt`, `InfernalWorkshopRotationTest.kt`, `InfernalWorkshopContractTest.kt`, suite JVM et assemblePhone (Pierre/sprites 240p).
- Pas de changement au solveur normal, Sudoku/Gomoku/Abeilles, statistiques Hall ni publication Play.

## Recette et limite
La pose additive explore d'autres chemins mais n'est pas une preuve qu'il existe des grilles correspondant au contrat. Après l'installation d'un APK **de test**, ouvrir Atelier, lancer recherche et regarder « ESSAIS V5 », « FRONTIÈRES UNIQUES », « CANDIDATS V5 ». Si essais augmentent, frontières uniques augmentent, mais candidats restent 0, examiner H2 et X-Wing et les budgets. Vérifier écran éteint, notification Android, export/import ZIP, données préexistantes. Tout merge main, Release stable/prérelease ou publication Play reste soumis à accord distinct.
