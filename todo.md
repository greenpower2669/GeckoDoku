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


## Fait v0.6.0-dev
- [x] Ajouter Mission Impossible.
- [x] Ajouter Infernal.
- [x] Ajouter SolveTechnique hypothèse simple et double.
- [x] Brancher uniquement sur des unités à exactement 2 candidats.
- [x] Détecter contradictions structurelles.
- [x] Borner Mission à 1 hypothèse.
- [x] Borner Infernal à 2 hypothèses/profondeur 2.
- [x] Enregistrer candidat rejeté et profondeur dans SolveStep.
- [x] Intégrer hypothèses au DifficultyIndexer.
- [x] Intégrer hypothèses aux solverTrace.
- [x] Reconstruire traces d'hypothèses depuis le journal.
- [x] Prof Gecko explique les hypothèses.
- [x] Ajouter ProfessorBubbleView.
- [x] Ajouter VictoryCelebrationView.
- [x] Intensité de célébration selon difficulté.
- [x] Messages spéciaux Mission Impossible / Infernal.
- [x] Célébration sans stroboscope et interrompable.
- [x] Synchroniser ordre + 4 mémoires.

## Validation v0.6
- [ ] Compiler GitHub Actions.
- [ ] Installer APK v0.6.
- [ ] Vérifier les 8 niveaux dans le sélecteur et les stats.
- [ ] Générer Mission Impossible et confirmer qu'une seule hypothèse est nécessaire.
- [ ] Générer Infernal et confirmer deux hypothèses/profondeur 2.
- [ ] Tester Prof 1/3, 2/3, 3/3 sur une hypothèse.
- [ ] Vérifier bulle BD sur 5×5 et 12×12.
- [ ] Terminer plusieurs difficultés et comparer les célébrations.
- [ ] Vérifier absence de flash agressif.
- [ ] Tester sauvegarde/rechargement d'une grille Mission/Infernal.
- [ ] Mesurer temps de génération 12×12 haut niveau.


## Fait v0.7.0-dev
- [x] Prof applique réellement une étape déterministe par pression.
- [x] Prof pose les croix d'exclusion.
- [x] Prof pose les geckos forcés.
- [x] Conserver les exclusions automatiques après gecko du Prof.
- [x] Afficher deux geckos fantômes lors d'une bifurcation.
- [x] Ne pas mélanger fantômes Prof et hypothèses joueur.
- [x] Deuxième pression d'hypothèse : contradiction, croix rejetée, gecko survivant.
- [x] Garde-fou avant mutation d'un gecko.
- [x] Conserver bulle BD et surlignages.
- [x] Enregistrer les victoires assistées globalement.
- [x] Enregistrer les victoires assistées par difficulté.
- [x] Synchroniser ordre + 4 mémoires.

## Validation v0.7
- [ ] Compiler GitHub Actions.
- [ ] Tester Prof sur une déduction de ligne/colonne.
- [ ] Tester Prof sur REGION_LOCKED / projection.
- [ ] Tester Prof sur Gecko X-Wing.
- [ ] Tester première pression d'hypothèse : deux fantômes seulement.
- [ ] Tester seconde pression : mauvaise branche barrée, bonne branche solide.
- [ ] Vérifier qu'une action manuelle annule les fantômes.
- [ ] Vérifier qu'une grille terminée par le Prof déclenche le feu d'artifice.
- [ ] Vérifier « Terminées avec Prof » dans Stats.
- [ ] Vérifier que le Prof n'incrémente jamais le compteur d'erreurs.


## Fait v0.8.0-dev
- [x] Sortir la bulle Prof du LinearLayout principal.
- [x] Ajouter screenRoot FrameLayout.
- [x] Positionner la bulle juste au-dessus du bouton Prof.
- [x] Ne plus réduire la grille lors de l'affichage de la bulle.
- [x] Regrouper les trois lignes de commandes dans controlsPanel.
- [x] Masquer controlsPanel tant que la bulle est ouverte.
- [x] Garder le bouton Prof visible.
- [x] Dessiner une grosse croix × dans la bulle.
- [x] Restaurer les commandes à la fermeture.
- [x] Ne pas annuler pendingProfessorHypothesis en fermant seulement la bulle.
- [x] Émettre un événement à chaque salve visuelle.
- [x] Ajouter lancement/explosion sonore stylisés.
- [x] Augmenter naturellement les salves avec la difficulté.
- [x] Ajouter accent final sur niveaux élevés.
- [x] Lier les sons de célébration au bouton FX.
- [x] Arrêter les sons différés lorsque la célébration est fermée.
- [x] Synchroniser ordre + 4 mémoires.

## Validation v0.8
- [ ] Compiler GitHub Actions.
- [ ] Tester 12×12 avec une longue bulle Prof : grille non réduite.
- [ ] Vérifier que la bulle reste juste au-dessus du bouton Prof.
- [ ] Vérifier × suffisamment grande et cliquable.
- [ ] Vérifier retour immédiat des autres boutons.
- [ ] Fermer la bulle pendant une hypothèse puis reprendre le Prof.
- [ ] Tester FX ON sur plusieurs difficultés.
- [ ] Tester FX OFF : célébration visuelle silencieuse.
- [ ] Fermer la célébration pendant les salves : plus aucun son retardé.
- [ ] Vérifier Mission Impossible et Infernal : montée sonore plus riche.
