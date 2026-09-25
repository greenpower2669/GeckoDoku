# Brain — GeckoDoku

## Contrat produit
GeckoDoku est un jeu Android de logique accessible, hors ligne et sans publicité. Le moteur doit pouvoir expliquer pourquoi chaque grille est résoluble et pourquoi elle reçoit son niveau.

## Règles
- exactement un gecko par ligne ;
- exactement un gecko par colonne ;
- exactement un gecko par zone ;
- aucun contact horizontal, vertical ou diagonal ;
- solution unique compte tenu des givens ;
- pas de guessing dans les niveaux proposés par le solveur actuel.

## Gestes
- clic simple confirmé : croix ;
- vrai double-clic : gecko ;
- appui long : hypothèse faible → forte → aucune ;
- appui long hors grille : repères personnels ;
- givens verrouillés.

## Tailles et zones
Tailles : 5 à 12.
Chaque grille NxN contient N zones.
Rendu v0.3 : 12 teintes pastel, frontières noires fortes. La taille ne définit pas la difficulté.

## Solveur humain v0.3
Techniques :
1. ROW_SINGLE ;
2. COLUMN_SINGLE ;
3. REGION_SINGLE ;
4. REGION_LOCKED : toutes les possibilités d'une zone sur une même ligne/colonne ou inverse ;
5. REGION_TOUCH_PROJECTION : si toutes les 2 à 4 possibilités restantes d'une zone touchent une même case extérieure, cette case est exclue ;
6. GECKO_X_WING :
   - deux lignes → mêmes deux colonnes ;
   - deux colonnes → mêmes deux lignes ;
   - deux zones confinées à deux lignes ou deux colonnes.

La projection exploite directement la règle de non-contact, ce que le Sudoku classique n'a pas.

## Difficulté mesurée
DifficultyIndexer exécute plusieurs solveurs par ablation :
- singles seuls ;
- logique de zone complète sans X-Wing ;
- X-Wing sans projection ;
- toutes les techniques.

Classement :
- Découverte si singles seuls suffisent ;
- Facile si logique de zone nécessaire avec 1 étape ;
- Réflexion avec 2–3 étapes de zone ;
- Difficile avec au moins 4 étapes de zone ;
- Expert si l'analyse complète réussit mais échoue sans X-Wing ;
- Démentiel si elle échoue sans X-Wing ET échoue sans projection : combinaison des deux indispensable.

Le score 0–100 reste secondaire. Le niveau nominal provient d'abord de la preuve logique.

## Génération
Pour 5..12, ne jamais pré-calculer toutes les permutations : le volume devient trop grand.
Pipeline v0.3 :
- construire une solution valide par backtracking aléatoire ;
- faire croître N zones connectées depuis les N geckos solution ;
- partir de givens sûrs puis les retirer ;
- après chaque retrait, vérifier unicité par backtracking borné à 2 solutions ;
- vérifier la résolubilité HumanSolver ;
- comparer le niveau mesuré au niveau demandé ;
- essayer plusieurs cartes/ordres ;
- si l'exact n'est pas trouvé dans le budget, retourner le meilleur candidat sûr et afficher demandé vs mesuré.

## IA future
Une IA locale pourra prédire difficulté et guider la génération plus vite, mais elle ne remplace pas les preuves logiques. Le solveur sert de vérité terrain et produit les futures données d'apprentissage.

## Stats locales
SharedPreferences : lancées, terminées, taux de réussite, erreurs, temps, tailles et difficulté mesurée. Aucun envoi réseau.

## Audio et média
ToneGenerator actuellement. Moteur indépendant des médias ; futurs AssetRenderer, AssetFxFeedback, voix et animations restent des couches remplaçables.
