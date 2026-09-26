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


## GECKO-022 — Habillage riche : préparation
- [x] Figer le rôle exact de chaque asset.
- [x] Interdire le découpage actuel des deux vidéos longues.
- [x] Définir lecture complète 0→EOF sans boucle.
- [x] Définir overlay au-dessus du rendu normal.
- [x] Définir switch Habillage animé ON/OFF.
- [x] Définir relation audio vidéo ↔ FX.
- [x] Définir fallback obligatoire si média absent ou illisible.
- [x] Définir priorités entre intro, Prof, victoire, apparition et actions longues.
- [x] Définir le keycolor comme chantier futur séparé.
- [x] Définir règles d'accessibilité / skip.
- [x] Ranger les assets dans un dossier assets.
- [x] Implémenter AssetMediaCatalog.
- [x] Implémenter RichMediaSettings persistant.
- [x] Implémenter RichMediaOverlayView hors layout de grille.
- [x] Implémenter lecture Intro + texte stylisé GeckoDoku.
- [x] Implémenter apparition sur case après confirmation logique.
- [x] Implémenter disparition sur case après retrait logique.
- [x] Implémenter lecture occasionnelle ENTIÈRE de Gecko_actions_plusieurs.mp4.
- [x] Implémenter lecture occasionnelle ENTIÈRE de Prof_actions.mp4 en contexte Prof.
- [x] Respecter FX ON/OFF pour le son média.
- [ ] Tester arrêt propre Activity pause/destroy.
- [ ] Tester asset manquant → jeu inchangé.
- [ ] Tester qu'une seule vidéo peut être active.
- [ ] Mesurer impact APK / mémoire / décodage sur téléphone.
- [ ] FUTUR : découpage des clips par timecodes, uniquement après validation séparée.
- [x] Keycolor bleu GPU avancé dans ce lot par décision Fab du 26/09/2026.


## Exécution GECKO-022 — TDD
- [x] Créer la branche isolée `gecko-022-rich-media` depuis le HEAD stable `edbe4740d60716fa01a69c29ce8a51645e791abd`.
- [x] Ajouter le test RED du scheduler des animations longues avant son implémentation.
- [x] Vérifier que la CI échoue pour la raison attendue : `RichMediaScheduler` absent (run #14, `Unresolved reference`).
- [x] Implémenter le sous-système média riche + keycolor bleu transparent sur la branche isolée.
- [x] Faire passer tests + APK + AAB sur GitHub Actions — run #15 `36203684907`, succès ; artefact `GeckoDoku-v0.9.0-dev-Android` id `10893390568`.
- [x] Fusionner seulement après CI verte ; `main` = branche GECKO-022 au commit `13ed666a8d93057c5ebd25307b8651c849b01154`, run #21 vert ; validation visuelle Android reste humaine.


## Validation téléphone v0.9
- [ ] Vérifier intro complète + titre GeckoDoku + bouton ×.
- [ ] Vérifier que le bleu des vidéos devient transparent sans trou visible dans le Gecko/Prof.
- [ ] Vérifier bords doux et absence de frange bleue gênante ; ajuster seuil/softness/despill si besoin.
- [ ] Vérifier apparition/disparition exactement sur la case concernée.
- [ ] Vérifier que les séquences Gecko/Prof longues jouent 0→EOF sans boucle.
- [ ] Vérifier Habillage animé OFF immédiatement et après redémarrage.
- [ ] Vérifier FX OFF : vidéos silencieuses, visuel conservé.
- [ ] Vérifier pause/reprise Android et changement de grille sans vidéo orpheline.
- [ ] Vérifier Prof : texte logique immédiat, média jamais bloquant.
- [ ] Vérifier célébration prioritaire au-dessus du média.


## Synchronisation main pendant GECKO-022
- [x] Ranger les trois MP3 sans réencodage : `assets/audio/intro/jungle intro GeckoD.mp3`, `assets/audio/celebration/jungle cebration GeckoD.mp3`, `assets/audio/encouragements/master/Voix_encouragements.mp3` ; aucun rôle runtime ajouté pendant le test v0.9.


## GECKO-023 — Voix d'encouragement
- [x] Analyser le master `assets/audio/encouragements/master/Voix_encouragements.mp3` et mesurer sa durée : 14,441 s.
- [x] Identifier les 13 phrases et figer les timecodes sûrs dans `ordres-de-mission.md`.
- [ ] Découper les 13 clips sous `assets/audio/encouragements/clips/` en conservant le master intact.
- [ ] Ajouter EncouragementCatalog / Selector / Player (noms finaux à confirmer lors de l'implémentation).
- [ ] Déclencher uniquement après un nouveau gecko correctement confirmé par le joueur.
- [ ] Empêcher le redéclenchement sur la même cellule dans une tentative.
- [ ] Empêcher la répétition immédiate du même clip.
- [ ] Rendre « Tu y es presque » éligible seulement à 1–2 geckos restants.
- [ ] Faire suivre les voix par FX ON/OFF, indépendamment de Habillage animé.
- [ ] Séquencer le dernier encouragement avant un futur audio de victoire, sans bloquer la célébration visuelle.
- [ ] Ajouter tests unitaires de sélection/déclenchement/anti-répétition.
- [ ] Tester toutes les coupes sur téléphone : attaque et fin de mot intactes.
- [ ] Vérifier qu'un clip absent ou illisible ne change jamais la partie.


## GECKO-024 — grille immuable / médias / audio
- [x] Figer le retour téléphone et le contrat géométrique dans l'ordre de mission.
- [x] RED : verrouiller orientation UV sans double retournement — run #25, classe absente attendue.
- [x] RED : verrouiller sélection encouragements / anti-répétition / contexte fin de grille — run #25, classe absente attendue.
- [x] Corriger vidéo tête en bas par UV canoniques.
- [x] Ajouter cache blanc strictement borné aux animations de case.
- [x] Conserver grille immuable pendant tous les overlays.
- [x] Intro : fond noir.
- [x] Prof : afficher `Prof.png` statiquement et localiser son animation sur le portrait.
- [x] Gecko : ajouter `assets/gecko/Gecko.png` transparent dérivé de l'identité visuelle existante, avec fallback procédural.
- [ ] Découper les 13 encouragements depuis le master, sans le modifier.
- [x] Jouer musique intro sur bouton Nouvelle.
- [x] Jouer musique célébration à la victoire.
- [x] Implémenter sélection voix GECKO-023 et anti-répétition ; timecodes master pour build téléphone.
- [x] Ajouter TTS français non bloquant pour les textes du Prof, contrôlé par FX.
- [x] Cleanup pause/destroy/FX OFF implémenté ; validation téléphone requise.
- [ ] CI tests + APK + AAB.
- [ ] Validation téléphone Fab.


### Vérification intermédiaire GECKO-024
- [x] Run #26 analysé : compilation bloquée uniquement par `ProfessorBubbleView.paint` inexistant.
- [x] Corriger avec un `imagePaint` dédié, sans autre changement.
- [ ] Refaire tests + APK + AAB après ce correctif.
