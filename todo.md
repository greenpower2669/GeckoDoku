# GeckoDoku — TODO actif

> Uniquement les travaux réellement ouverts.
> Tout ce qui est terminé est dans `debughistorical.md`.
> Référence courante : `main` — 0.15.43-dev / code 78.

## Validation téléphone 0.15.43-dev

- [ ] Vérifier que le pavé Sudoku descend confortablement jusqu'à ~140×160 dp.
- [ ] Vérifier qu'à taille minimale les zones Choix / Candidats / Hypothèse / Prévisu restent utilisables.
- [ ] Vérifier que la poignée de resize n'empiète pas trop sur Prévisu.
- [ ] Vérifier que simple et double clic reciblent le pavé sans flicker ni fermeture.
- [ ] Vérifier que le pavé reste ouvert après Oui et après Non.
- [ ] Vérifier que les clics hors popup atteignent correctement la grille sur Android 16.
- [ ] Vérifier `?` puis les quatre zones : bonne explication de Pierre.
- [ ] Vérifier que l'aide `?` ne réduit pas les étoiles.

## Hypothèses

- [ ] Test téléphone Classic : suppression parent retire croix/aura des enfants.
- [ ] Test téléphone Classic : contradiction transforme visuellement la branche en sens interdit et nettoie ses descendants.
- [ ] Test téléphone Abeilles & Geckos : même comportement parent/enfant.
- [ ] Test téléphone Sudoku : même comportement parent/enfant.
- [ ] Test de changement de sous-branche : aucun descendant visuel de l'ancienne branche ne reste.

## Statistiques

- [ ] Annuler une partie à 0 erreur → aucune nouvelle statistique.
- [ ] Annuler après erreur → tentative + erreurs visibles au bon niveau.
- [ ] Deux parties terminées du même niveau → tendance temps/étoiles correcte.
- [ ] Stats → niveau → graphe lisible.
- [ ] Hall of Fame → niveau → graphe et retour vers stats.

## Médias / mémoire

- [ ] Vérifier en usage réel que Gecko/Abeille chargent les banques 240p sans reconstruction MP4.
- [ ] Vérifier qu'aucun log 60p/120p n'apparaît.
- [ ] Mesurer RAM/FPS sur une session longue ; ne pas conclure à un gain mémoire sans mesure.

## Git

- [x] Release 0.15.43-dev produite — run #403.
- [x] Réconciliation avec les 8 commits historiques de main — CI #404 verte.
- [x] Merge PR #2 dans main — `17c6de0186c745c15fc042971eb09b4fe19a6299`.
- [x] CI finale main #405 verte.

## Plus tard

- [ ] Décider d'un éventuel chantier iOS uniquement sur demande explicite ; `ios.md` reste documentaire.
