# Mission 012 — Déblocage recherche Infernal

Date : 16/10/2026.
Base : feature/infernal-nested-depth3-v1.
Branche : feature/infernal-proof-search-v2.

## Symptôme
L'option Infernal CLASSIC exige trois hypothèses imbriquées, mais le solveur ne trouve pas ; generateExact continue ses séries tant que l'utilisateur n'annule pas.

## Contrat
- Techniques déterministes HumanSolver.FULL à chaque état.
- Mission Impossible : preuve par une hypothèse depth1. Infernal : contradiction nécessitant une chaîne imbriquée depth3 minimum, et pas trois guesses indépendantes.
- Toutes les bifurcations possibles doivent être essayées jusqu'au plafond borné. Une paire de candidats se branche de façon exclusive ; un candidat isolé se branche sur présent ou absent.
- Enregistrer les états déjà explorés dans chaque recherche pour éviter les preuves identiques.
- Contradiction prouvée, résolution complète et indécision sont trois résultats distincts. Une recherche interrompue n'est pas une preuve d'impossibilité, ni une preuve de profondeur nécessaire.
- Unicité mathématique maintenue par countSolutions(..., limit=2)==1 indépendante du solveur pédagogique.
- Timeout de génération (~60 s) et au maximum 8 lots Infernal ; contrôles d'annulation dans le solveur et dans le compteur d'unicité. Ne jamais tourner sans fin et ne jamais rétrograder la grille.

## Modifications
HypothesisSolver : exploration des paires + test vrai/faux, memo d'état, plafond nœuds/horloge, choix de preuve la moins profonde, annulation, flag budget épuisé.
DifficultyIndexer : refuse de qualifier H3 comme nécessaire si H1/H2 ont épuisé leur budget.
PuzzleGenerator : recherche finie, annulation coopérative jusqu'au compte de solutions, génération réduite par lots.
MainActivity : information claire si aucune grille qualifiée n'est produite.
InfernalSearchRegressionTest : tests annulation, aucune fausse résolution et plateau déjà fini.
Workflow build.yml : CI phone de branche feature/infernal-proof-search-v2.

## Limites et preuves attendues
Même après cette correction, aucune grille trois fois imbriquée générée à partir d'une graine précise n'est encore certifiée ; quantifier et améliorer les candidats en fonction de ce résultat. CI green = compilation + tests, pas validation statistique du générateur. L'APK phone est signé Android test, pas Play ; aucun merge main ou Release sans accord explicite de Fab.
