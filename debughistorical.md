# GeckoDoku — FAB Copilot debug historical

> Historique condensé. Pour le détail ancien : `sauvegarde.md`.
> Version + SHA + date priment sur les anciens identifiants GECKO.

## Baseline avant HOF

Au 2 octobre 2026 :
- GeckoDoku multimode : Classic, Sudoku, Gomoku, Abeilles & Geckos ;
- merge code précédent : `17c6de0186c745c15fc042971eb09b4fe19a6299` ;
- main documentaire utilisé pour HOF : `1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675` ;
- version `0.15.43-dev`, code 78 ;
- release téléphone `phone-0.15.43-dev-run-403` ;
- sprites Gecko/Abeille 240p préconstruits ; Plante, Prof/Pierre et intros conservés ;
- stats : terminée enregistrée, abandon 0 erreur ignoré, abandon avec erreur enregistré ;
- hypothèses colorées parent/enfant dans les modes concernés ;
- pavé Sudoku persistant 2×2 et aide `?` documentaire.

## GECKO-HOF-SYNC-001 — 6 octobre 2026

Branche créée depuis main : `feature/gecko-hof-sync-v1`.

Contrat serveur déjà publié par Fab : 4 modes, `runId` idempotent, champs nullable, metadata libre, seed 64 bits décimale, aucun secret APK, POST + `/sync`.

### TDD

Implémentation par lots RED → GREEN :

1. `GlobalScorePayload` + codec + factory quatre modes.
2. `AtomicJsonFileStore` + `PendingScoreStore` crash-safe.
3. `GeckoDokuHallApiClient` + `GlobalScoreRetryPolicy`.
4. `GlobalScoreCacheStore` avec page et curseur atomiques.
5. `GlobalScoreSyncCoordinator` mono-worker + `AndroidNetworkMonitor`.
6. `GlobalScoreCompletionPublisher`, `GlobalScoreCompletionBridge`, `GlobalScoreRuntime` et raccord `MainActivity`.

Run de référence Task 5 : `37500492900` GREEN.

### Incidents de développement utiles

- Le workflow téléphone historique dépendait du package `PackageSprites`; un workflow unitaire HOF dédié a été utilisé pour isoler les tests du chantier.
- Premier client HTTP : erreur Kotlin `return` dans un corps d’expression ; corrigée sans changer le contrat.
- Un test publisher contenait un mauvais cast du retour de `assertNotNull`; le code produit compilait, test corrigé.
- Premier patch `MainActivity` trop sensible à l’indentation : échec avant toute écriture produit.
- Deuxième patch compilait jusqu’à `BuildConfig.VERSION_NAME`, non généré dans ce projet. Remplacement par `PackageManager.versionName`.
- Le patch corrigé a passé `testDebugUnitTest` + `assembleDebug` dans le run `37508648842`, puis a été poussé au commit produit `d6d25e051c0a4723844df51c21aa99c15cd35c08`.
- Les workflows temporaires de chirurgie ont ensuite été supprimés et le scope du workflow téléphone historique restauré.

### Invariants confirmés

- pending écrit avant réseau ;
- retry exact même JSON + même runId ;
- ACK invalide ou refusé ne supprime pas pending ;
- erreurs permanentes gardées BLOCKED ;
- retry réseau/5xx/429 ;
- `/sync` continue avec `hasMore=true` même page vide ;
- cache global séparé des stats/progression ;
- mapping quatre modes sans inventer de nouvelle règle de gameplay ;
- Gomoku non éligible au Hall local reste seulement représentable dans le protocole.

## État suivant

Code Android HOF techniquement prêt pour validation téléphone réelle.

À vérifier sur téléphone :
- online → une seule entrée ;
- offline → pending conservé ;
- kill/restart offline → pending survit ;
- retour réseau → envoi automatique ;
- doublon/retry → une seule entrée serveur ;
- catégories et champs spécifiques corrects.

Aucun merge `main` ni Release avant ordre explicite de Fab.
