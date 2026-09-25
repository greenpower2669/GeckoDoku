# Brain — GeckoDoku

## Contrat produit
GeckoDoku est un jeu de logique Android accessible, mignon, hors ligne et sans publicité. Les retours audio complètent le visuel.

## Règles cœur
- exactement un gecko par ligne ;
- exactement un gecko par colonne ;
- exactement un gecko par zone ;
- aucun contact entre geckos, diagonales comprises ;
- toute grille jouable doit avoir une solution unique ;
- les modes normaux doivent fournir un chemin de résolution logique compatible avec leur niveau.

## Gestes v0.2
- clic simple confirmé : poser/enlever une croix manuelle ;
- vrai double-clic : poser/enlever un gecko ;
- appui long : hypothèse discrète → alerte clignotante → aucun ;
- appui long hors grille : palette de repères ;
- un gecko donné est verrouillé.

La séparation single/double est assurée par GestureDetector.onSingleTapConfirmed et onDoubleTap. Deux clics simples séparés ne doivent plus produire un gecko.

## Taille et difficulté
Tailles initiales : 5, 6, 7, 8.
Difficultés :
- Découverte : davantage de geckos donnés, singles ;
- Facile : moins de donnés, interactions simples admises ;
- Réflexion : peu de donnés, interactions ligne/zone ;
- Expert : peut utiliser X-Wing.

Taille et difficulté sont indépendantes.

## Génération
PuzzleGenerator :
1. génère les permutations respectant ligne/colonne/non-contact ;
2. choisit une solution ;
3. fait croître des zones connectées autour des cellules solution ;
4. rejette la carte si elle n'a pas exactement une solution ;
5. sélectionne les geckos donnés ;
6. vérifie que HumanSolver résout selon les techniques autorisées ;
7. possède un fallback vérifié 5×5 à 8×8.

## Solveur humain
Techniques v0.2 :
- ligne forcée ;
- colonne forcée ;
- zone forcée ;
- interaction ligne/zone ;
- X-Wing lignes et colonnes.

HumanSolver ne lit pas solutionCols pendant la résolution : il raisonne uniquement à partir des règles, givens et éliminations.

## Difficulté
DifficultyIndexer utilise la taille, le nombre de givens, le nombre d'étapes, la technique maximale et le nombre de X-Wings. Le score actuel est heuristique et explicable ; ce n'est pas encore l'IA entraînée future.

## Stats locales
SharedPreferences uniquement sur le téléphone :
- parties lancées ;
- parties terminées ;
- taux de réussite ;
- erreurs ;
- temps total et moyen ;
- terminées par taille ;
- terminées par difficulté.

Architecture prête à évoluer vers profils multiples sans réseau.

## Audio
ToneGenerator léger : croix, blocage, gecko, erreur, hypothèse, repère, fin. FX désactivables. Futur AssetFxFeedback prévu.

## Architecture média
GameEngine ne dépend pas des médias. Les futurs sons, voix, images, animations, vidéos et FX riches devront rester interchangeables.

## Validation
v0.1 APK a compilé avec succès. v0.2.0-dev doit être compilé puis testé sur téléphone, notamment gestes, génération, difficulté et stats.
