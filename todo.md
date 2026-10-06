# GeckoDoku — TODO actif

> Uniquement les travaux ouverts. Historique terminé : `debughistorical.md`.

Mission : `GECKO-HOF-SYNC-001` sur `feature/gecko-hof-sync-v1`.

## Hall of Fame global v1

- [x] Contrat SITE v1 publié et testé côté serveur.
- [x] Spec validée par Fab.
- [x] Plan Native validé par Fab.
- [x] Payload quatre modes + seed 64 bits décimale.
- [x] `PendingScoreStore` crash-safe.
- [x] POST + ACK idempotent.
- [x] retry/backoff + `Retry-After`.
- [x] cache global + `/sync` transactionnel.
- [x] mono-worker + reprise au lancement/retour réseau.
- [x] raccord aux complétions locales existantes.
- [x] INTERNET + ACCESS_NETWORK_STATE.
- [x] tests JVM GREEN.
- [x] `assembleDebug` GREEN sur le raccord réel — run `37508648842`.
- [x] workflows temporaires de développement supprimés.
- [ ] CI HOF finale GREEN après synchronisation des mémoires.
- [ ] Téléphone : terminer une partie online et vérifier une seule entrée serveur.
- [ ] Téléphone : terminer offline et vérifier que le score reste pending.
- [ ] Téléphone : tuer/redémarrer offline puis vérifier que pending survit.
- [ ] Téléphone : remettre le réseau et vérifier l’envoi automatique.
- [ ] Téléphone : vérifier qu’un retry/doublon ne crée qu’une seule entrée.
- [ ] Vérifier les champs/catégories d’au moins un score réel de chacun des modes actuellement éligibles localement.
- [ ] Corriger seulement les bugs constatés pendant cette validation.
- [ ] Décider merge `main` / Release uniquement sur ordre explicite de Fab.

## Baseline produit — tests téléphone encore ouverts hors HOF

- [ ] Pavé Sudoku ~140×160 dp et quatre zones utilisables.
- [ ] Simple/double clic reciblent sans fermeture ; Oui/Non gardent le pavé.
- [ ] Clic extérieur popup Android 16.
- [ ] Aide `?` correcte et sans perte d’étoile.
- [ ] Hypothèses parent/enfant Classic, Sudoku, Bee : prune/contradiction/sous-branche.
- [ ] Stats : abandon 0 erreur ignoré, abandon avec erreur enregistré, tendances et graphes.
- [ ] Médias 240p : absence de reconstruction MP4 et mesure RAM/FPS en session longue.
