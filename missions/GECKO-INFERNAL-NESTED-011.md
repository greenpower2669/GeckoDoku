# Mission 011 — Infernal = trois imbrications logiques

Date : 15/10/2026. Départ : main. Branche : feature/infernal-nested-depth3-v1.

## Invariants
1. GeckoDoku CLASSIC uniquement : aucune refonte du Sudoku, Gomoku ou Abeilles & Geckos.
2. Épuiser les déductions HumanSolver.FULL avant chaque hypothèse.
3. Mission Impossible : une hypothèse (profondeur = 1), suivie de contradiction prouvée.
4. Infernal : au moins trois niveaux imbriqués H1 → H2 → H3 ; plusieurs hypothèses à la suite ne suffisent pas.
5. Branche indécise (limite de calcul) ≠ contradiction ≠ solution complète. Si deux solutions complètes distinctes sont possibles, grille rejetée.
6. PuzzleGenerator.generateExact continue à exiger la solution complète unique avec countSolutions(..., limit = 2) == 1 et un profil de profondeur ≥ 3.

## Code
- HypothesisSolver.kt : issues explicites, propagation réelle de la profondeur.
- InfernalDepthPolicy.kt : minimum 3, recherche bornée à 3 dans cette version.
- DifficultyIndex.kt : recherche profondeur 1, élimination profondeur 2, profondeur 3.
- PuzzleGenerator.kt : profil Infernal strict et trace Professeur cohérente.
- ProfessorGecko.kt : recherche à trois niveaux et annonce de la profondeur mesurée.
- InfernalDepthPolicyTest.kt : bornes profondeur, grille terminée, état indécis.
- .github/workflows/build.yml : build de branche, pas de publication automatique.

## Validation à compléter
CI Android + JVM, APK de test, seed certifiée profondeur 3, recette Samsung, mesure du temps de recherche et test complet de la démonstration. La trace contient actuellement l'hypothèse externe et la profondeur, pas tout l'arbre explicatif H1/H2/H3. Aucune fusion main ni Release sans feu vert distinct.

## Preuve CI initiale
Run 38800346305 / #533 GREEN sur commit d394a65 ; tests JVM + assemblePhone, Pierre et sprites 240p. APK test SHA-256 ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3, artifact 12003858392. Sans certificat de puzzle triple imbriqué, ne pas annoncer validation gameplay complète.
