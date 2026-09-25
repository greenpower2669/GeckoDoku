# TODO — GeckoDoku

## Fait v0.4.0-dev
- [x] Enrichir SolveStep avec preuve visuelle.
- [x] Enregistrer beforeConfirmed / beforeExcluded.
- [x] Stocker la trace complète dans Puzzle.solverTrace.
- [x] Calculer la trace une seule fois lors de l'acceptation de la grille.
- [x] Ajouter HumanSolver.nextStep() pour un état courant.
- [x] Ajouter ProfessorGecko.
- [x] Chemin rapide : réutilisation de la trace.
- [x] Fallback : recalcul d'une seule prochaine étape.
- [x] Aide 1/3 où regarder.
- [x] Aide 2/3 pourquoi.
- [x] Aide 3/3 action.
- [x] Surlignage ambre des sources.
- [x] Surlignage rouge des cibles au niveau 3.
- [x] Ne jamais jouer l'action automatiquement.
- [x] Reset du Prof après action joueur.
- [x] Maintenir logique v0.3 et stats locales.
- [x] Synchroniser ordre + 4 mémoires.

## Validation immédiate
- [ ] Commit v0.4.
- [ ] Compiler GitHub Actions.
- [ ] Récupérer APK.
- [ ] Tester Prof sur 12×12 Difficile.
- [ ] Tester une projection de zone.
- [ ] Tester un Gecko X-Wing.
- [ ] Tester le joueur qui suit un autre chemin.
- [ ] Tester une croix manuelle erronée : aucune invention du Prof.
- [ ] Vérifier TalkBack/annonce du texte d'aide.
- [ ] Vérifier lisibilité du surlignage sur petites cases.

## Futur
- [ ] Historique consultable de toutes les étapes.
- [ ] Boutons précédent/suivant pour revoir le cours après partie.
- [ ] Ton du Prof configurable : sérieux / chaleureux / taquin.
- [ ] Enregistrer ressenti trop facile / bien / trop dur.
- [ ] Dataset local opt-in basé sur traces + ressenti.
- [ ] IA locale de prédiction.
- [ ] Explorer Gecko Swordfish si explicable.

## Habillage futur
- [ ] Switch procédural / assets riches.
- [ ] AssetGeckoRenderer.
- [ ] AssetFxFeedback.
- [ ] Voix d'encouragement.
- [ ] Animations / FX visuels.
- [ ] Images / éventuelles vidéos.


## Correctif urgent v0.4.1
- [x] Enregistrer le bug Facile nécessitant 2 X-Wing comme GECKO-012.
- [x] Diagnostiquer Prof Gecko derrière la barre Android.
- [x] Ajouter WindowInsets systemBars haut/bas/gauche/droite.
- [x] Préserver un padding de base en plus des insets.
- [x] Monter version 0.4.1-dev.
- [ ] Compiler v0.4.1.
- [ ] Tester sur téléphone que Prof Gecko est entièrement visible et cliquable.
- [ ] Vérifier le titre sous la barre d'état.

## Priorité logique suivante
- [ ] Reproduire le 12×12 Facile ayant nécessité deux X-Wing.
- [ ] Comparer solverTrace stockée au chemin humain.
- [ ] Vérifier si DifficultyIndexer masque un X-Wing par une autre déduction admise.
- [ ] Interdire définitivement Facile/Réflexion/Difficile si X-Wing est réellement indispensable.


## Fait v0.5.0-dev
- [x] ↺ Rejouer la grille courante à zéro.
- [x] ⭐ Sauvegarder une grille dans un journal local.
- [x] Dédupliquer le journal par id de grille.
- [x] 📚 Lister les grilles sauvegardées.
- [x] Recharger/rejouer une grille du journal.
- [x] Supprimer une grille du journal.
- [x] Vider tout le journal avec confirmation.
- [x] Conserver stats séparées du journal.
- [x] Reconstruire solverTrace lors du chargement.
- [x] Afficher taux de réussite par difficulté.
- [x] Afficher terminées/lancées par difficulté.
- [x] Compter Rejouer et Journal load comme nouvelles tentatives.
- [x] Synchroniser ordre + 4 mémoires.

## Validation v0.5
- [ ] Compiler GitHub Actions.
- [ ] Installer APK v0.5.
- [ ] Tester Rejouer avant et après fin de partie.
- [ ] Sauver plusieurs grilles et redémarrer l'application.
- [ ] Charger une grille et vérifier qu'elle est identique.
- [ ] Vérifier Prof Gecko après chargement.
- [ ] Supprimer une entrée.
- [ ] Vider tout et vérifier que les stats restent présentes.
- [ ] Vérifier taux de réussite par difficulté avec plusieurs tentatives.
- [ ] Vérifier que les nouveaux boutons restent au-dessus de la barre Android.

## Futur journal
- [ ] Export/import d'une grille pour un autre appareil.
- [ ] Petit code de partage compact et vérifié.
- [ ] Nom/note personnalisée d'une grille favorite.
- [ ] Tags : pédagogique, X-Wing, projection, test.
