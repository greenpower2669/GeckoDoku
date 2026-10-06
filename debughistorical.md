# GeckoDoku — FAB Copilot debug historical

> Historique condensé. Pour le détail ancien : `sauvegarde.md`.
> Version + SHA + date priment sur les anciens identifiants GECKO.

## Baseline avant HOF

Au 2 octobre 2026 :
- quatre modes : Classic, Sudoku, Gomoku, Abeilles & Geckos ;
- base HOF : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675` ;
- version `0.15.43-dev`, code 78 ;
- release téléphone précédente `phone-0.15.43-dev-run-403` ;
- stats, hypothèses, pavé Sudoku et médias 240p déjà en place.

## GECKO-HOF-SYNC-001 — 6 octobre 2026

Contrat serveur : 4 modes, `runId` idempotent, champs spécifiques nullable, metadata libre, seed 64 bits décimale, aucun secret APK, POST + `/sync`.

### TDD par lots

1. `GlobalScorePayload` + codec/factory quatre modes.
2. `AtomicJsonFileStore` + `PendingScoreStore` crash-safe.
3. `GeckoDokuHallApiClient` + `GlobalScoreRetryPolicy`.
4. `GlobalScoreCacheStore` avec page + curseur atomiques.
5. `GlobalScoreSyncCoordinator` mono-worker + `AndroidNetworkMonitor`.
6. `GlobalScoreCompletionPublisher`, `GlobalScoreCompletionBridge`, `GlobalScoreRuntime` + raccord `MainActivity`.

### Incidents utiles

- Pipeline téléphone historique `PackageSprites` séparé du banc HOF.
- Erreur Kotlin initiale du parseur ACK corrigée sans changer le contrat.
- Test publisher corrigé après mauvais usage de `assertNotNull`.
- Patches `MainActivity` trop sensibles à l’indentation abandonnés au profit d’un patch borné à la méthode.
- `BuildConfig.VERSION_NAME` non généré : version récupérée via `PackageManager`.
- Revue finale : Hall local et global pouvaient prendre deux timestamps distincts. Test RED ajouté, puis correction : un seul `completedAt` est calculé et réutilisé partout.
- Une première transformation timestamp a produit `val completedAt = completedAt`; le compilateur l’a refusée, aucun commit produit n’a été effectué. L’ordre du patch a été corrigé.

### Vérification finale

- commit code HOF final : `9ff902bbe778a2202bdda3ca715e2bbce7cb06dd` ;
- run timestamp `37518234014` : tests JVM + `assembleDebug` GREEN ;
- run HOF canonique `37519024806` : GREEN ;
- run APK validation `37519024909` : tests + build + artifact GREEN ;
- artifact `GeckoDoku-HOF-v1-validation` ;
- SHA-256 APK : `926058461bb214fbe9e8abcf4825902b8a58129169907a1a4d9749a79c2dd49a` ;
- workflows temporaires timestamp/APK supprimés après usage.

### Invariants confirmés

- pending écrit avant réseau ;
- retry exact même JSON + même runId ;
- ACK invalide/refusé ne supprime pas pending ;
- erreurs permanentes gardées BLOCKED ;
- retry réseau/5xx/429 ;
- `/sync` continue si `hasMore=true`, même page vide ;
- cache global séparé des stats/progression ;
- quatre modes mappés sans inventer de règle gameplay ;
- un seul `completedAt` local/global.

## État suivant

Code Android HOF prêt pour validation téléphone réelle : online, offline, kill/restart, retour réseau, dédoublonnage et contrôle des catégories/champs.

Aucun merge `main` ni Release avant ordre explicite de Fab.
