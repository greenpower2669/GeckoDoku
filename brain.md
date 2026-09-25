# Brain — GeckoDoku

## Contrat produit
Jeu Android de logique accessible, hors ligne et sans publicité. Toute difficulté et tout conseil doivent pouvoir être expliqués par une preuve logique.

## Règles
Un gecko exactement par ligne, colonne et zone. Aucun contact horizontal, vertical ou diagonal. Solution unique compte tenu des givens. Pas de guessing dans le solveur humain actuel.

## Gestes
Simple : croix. Vrai double-clic : gecko. Long : hypothèse. Givens verrouillés.

## Tailles et rendu
5 à 12. N zones pour NxN. 12 teintes pastel et frontières fortes.

## Solveur
Techniques :
- ROW_SINGLE ;
- COLUMN_SINGLE ;
- REGION_SINGLE ;
- REGION_LOCKED ;
- REGION_TOUCH_PROJECTION ;
- GECKO_X_WING.

## Trace pédagogique v0.4
SolveStep contient :
- technique ;
- cell forcée ou eliminated ;
- sourceCells ;
- sourceRegions ;
- axis ;
- beforeConfirmed ;
- beforeExcluded.

HumanSolver.analyze() produit une trace ordonnée complète. Puzzle.solverTrace conserve cette trace calculée une fois au moment où le générateur accepte la grille.

Cette trace devient une ressource commune :
- calcul de difficulté ;
- Professeur Gecko ;
- debug ;
- futur dataset IA.

## Professeur Gecko
ProfessorGecko.nextHint() reçoit le snapshot actuel.
Il tente d'abord de réutiliser une étape de solverTrace dont les préconditions sont satisfaites et dont l'action n'est pas déjà faite.
Si aucune étape pré-calculée ne correspond, HumanSolver.nextStep() recalcule uniquement la prochaine déduction depuis confirmed + manualCrosses + autoCrosses.

Trois niveaux :
1. focusText : zone/ligne/structure à regarder ;
2. explanationText : preuve logique ;
3. actionText : case gecko ou cases à barrer.

La vue affiche les sources en ambre et les cibles en rouge seulement au niveau 3. Toute action du joueur efface l'indice courant.

Le Prof ne modifie pas le moteur et ne joue pas automatiquement.

## Difficulté
Mesurée par solveurs d'ablation. Expert nécessite réellement X-Wing. Démentiel nécessite X-Wing + projection.

## IA future
L'IA pourra prédire le niveau et sélectionner des explications, mais la trace logique reste la vérité terrain.

## Stats/média
Stats SharedPreferences locales. Audio léger. Médias riches futurs séparés du moteur.


## Barres système Android
MainActivity doit toujours respecter les WindowInsets des systemBars. Le padding de base d'accessibilité est additionné aux insets haut/bas/gauche/droite afin qu'aucun contrôle, notamment Prof Gecko, ne soit masqué par la barre de navigation ou la barre d'état.

## Anomalie difficulté à auditer
Un test réel 12×12 étiqueté Facile a nécessité deux Gecko X-Wing selon le joueur. Cette observation invalide le classement si les X-Wing étaient effectivement indispensables. Ne pas considérer le libellé Facile comme fiable pour ce cas avant audit. La correction doit comparer la trace réellement nécessaire et les alternatives logiques, puis garantir qu'un niveau inférieur ne dépend d'aucun X-Wing.


## Journal de grilles v0.5
PuzzleJournalStore est séparé du moteur. Il utilise SharedPreferences + JSON et conserve uniquement la définition nécessaire :
- id ;
- size ;
- difficulty ;
- seed ;
- regions ;
- solutionCols ;
- givens ;
- savedAt.

La progression du joueur n'est pas sauvegardée dans le journal. Recharger signifie refaire la grille proprement. HumanSolver reconstruit solverTrace au chargement, ce qui garde les entrées légères et indépendantes des évolutions internes de la trace.

Opérations : save sans doublon d'id, list, load, delete, clear, contains.

## Rejouer
Rejouer ne génère rien : GameEngine est recréé sur le même Puzzle. Hypothèses, croix, geckos joueurs, temps, état de fin et Prof courant sont réinitialisés. Cela compte comme une nouvelle tentative statistique.

## Statistiques par difficulté
PlayerStatsStore conserve déjà started_diff_* et completed_diff_*. v0.5 expose DifficultyStats(started, completed, completionRate). L'écran Stats affiche pour chaque difficulté pourcentage et fraction completed/started. Un niveau sans tentative affiche « — » plutôt qu'un faux 0 %.


## Hypothèses logiques v0.6
HypothesisSolver intervient uniquement lorsque HumanSolver FULL ne fournit plus de déduction.
Il construit des BinaryChoice uniquement sur :
- une ligne non résolue avec exactement 2 candidats ;
- une colonne non résolue avec exactement 2 candidats ;
- une zone non résolue avec exactement 2 candidats.

Pour chaque candidat, il simule les conséquences déterministes. Une branche est rejetée seulement si elle mène à une contradiction : conflit entre geckos, ligne sans candidat, colonne sans candidat ou zone sans candidat.

maxDepth=1 + maxHypothesisSteps=1 définit Mission Impossible.
maxDepth=2 + maxHypothesisSteps=2 définit Infernal.
Une étape transporte hypothesisRejected et hypothesisDepth afin que le Prof explique la preuve.

DifficultyIndexer essaie d'abord HumanSolver FULL. Seulement s'il bloque, il tente Mission puis Infernal. Une grille qui échoue encore à profondeur 2 n'est pas logicallySolvable dans le contrat actuel.

## Professeur en bulle BD
ProfessorBubbleView est une couche UI distincte. Elle affiche un fond clair, contour vert, petite queue de bulle, en-tête « Prof Gecko » et texte lisible. MainActivity affiche dans cette bulle focusText, explanationText puis actionText. Toute action joueur réinitialise et masque la bulle.

## Célébration
VictoryCelebrationView est un overlay procédural sans asset : fond translucide, confettis et particules radiales. Le nombre de salves et particules dépend de l'ordinal de GameDifficulty. Pas de stroboscope ; l'alpha des particules décroît progressivement. Durée environ 2,2 s + 0,32 s par niveau, toucher pour fermer.
