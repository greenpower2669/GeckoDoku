# TODO — GeckoDoku

## Fait v0.3.0-dev
- [x] Étendre tailles de 5×5 à 12×12.
- [x] Étendre palette à 12 couleurs.
- [x] Ajouter Difficile et Démentiel.
- [x] Séparer score numérique et niveau logique prouvé.
- [x] Ajouter solveurs d'ablation pour mesurer les techniques indispensables.
- [x] Facile : au moins 1 déduction de zone nécessaire.
- [x] Réflexion : 2 à 3 déductions de zone.
- [x] Difficile : 4+ déductions de zone.
- [x] Expert : Gecko X-Wing nécessaire.
- [x] Démentiel : X-Wing + projection de zone nécessaires.
- [x] Ajouter REGION_TOUCH_PROJECTION sur 2..4 candidats de zone.
- [x] Étendre Gecko X-Wing aux paires de zones.
- [x] Remplacer cache exhaustif de permutations par backtracking compatible 12×12.
- [x] Vérifier unicité avec givens par compteur borné à 2.
- [x] Afficher demandé vs mesuré si le niveau exact n'est pas trouvé.
- [x] Stats basées sur difficulté mesurée.
- [x] Conserver gestes v0.2, FX et repères.
- [x] Synchroniser ordre + 4 mémoires dans le cycle v0.3.

## Validation immédiate
- [ ] Commit v0.3.
- [ ] Vérifier démarrage GitHub Action.
- [ ] Corriger toute erreur de compilation.
- [ ] Récupérer APK v0.3.
- [ ] Tester génération 5×5 à 12×12, surtout temps 10–12.
- [ ] Vérifier que Expert affiche X-Wing requis.
- [ ] Vérifier que Démentiel affiche X-Wing + projection requise.
- [ ] Tester que Facile/Réflexion/Difficile correspondent mieux au ressenti.
- [ ] Vérifier couleurs et lisibilité.
- [ ] Vérifier double-clic et stats.

## Futur logique
- [ ] Professeur Gecko : expliquer chaque SolveStep visuellement.
- [ ] Montrer le rectangle/axes Gecko X-Wing.
- [ ] Montrer la zone et la projection commune.
- [ ] Ajouter ressenti local trop facile / bien / trop dur.
- [ ] Entraîner IA locale de prédiction à partir des preuves + retours humains.
- [ ] Explorer Gecko Swordfish (N geckos / N axes) uniquement si explicable.

## Habillage futur
- [ ] Switch procédural / assets riches.
- [ ] AssetGeckoRenderer.
- [ ] AssetFxFeedback.
- [ ] Voix d'encouragement.
- [ ] Animations / FX visuels.
- [ ] Images / éventuelles vidéos.
- [ ] Toujours conserver un mode léger.
