# GeckoDoku — TODO actif

> Uniquement les travaux réellement ouverts.
> Tout ce qui est terminé est dans `debughistorical.md`.
> Baseline : `main` — 0.15.43-dev / code 78.
> Mission active : `GECKO-HOF-SYNC-001` — `feature/gecko-hof-sync-v1`.

## Hall of Fame global v1

- [x] Contrat SITE v1 publié et testé côté serveur.
- [x] Design Android approuvé en conversation.
- [x] Spec écrite : `docs/superpowers/specs/2026-10-06-geckodoku-global-hof-sync-design.md`.
- [ ] Revue explicite de la spec par Fab.
- [ ] Écrire le plan d’implémentation après validation de la spec.
- [ ] Ajouter permission INTERNET et configuration URL publique.
- [ ] Implémenter `GlobalScorePayload` pour les 4 modes.
- [ ] Implémenter `PendingScoreStore` persistant et crash-safe au niveau app.
- [ ] Implémenter client POST score et ACK idempotent.
- [ ] Implémenter retry/backoff + `Retry-After`.
- [ ] Implémenter cache global + `/sync` par `scoreId` et `nextCursor`.
- [ ] Déclencher reprise au lancement et retour réseau sans concurrence multiple.
- [ ] Ajouter les 18 tests ciblés du contrat Android.
- [ ] Lancer tous les tests existants.
- [ ] Build Android vert.
- [ ] Validation téléphone : online, offline→online, doublon, redémarrage pending.
- [ ] Aucun merge main / release avant ordre explicite de Fab.

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
- [x] Branche mission HOF créée depuis le HEAD documentaire de main.

## Plus tard

- [ ] Décider d'un éventuel chantier iOS uniquement sur demande explicite ; `ios.md` reste documentaire.
