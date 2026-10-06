# GeckoDoku — FAB Copilot brainmap

> Carte technique courte. Mission : `GECKO-HOF-SYNC-001` sur `feature/gecko-hof-sync-v1`.

## Flux produit

```text
MainActivity
├─ Classic / Sudoku / Gomoku / BeeGecko
├─ stats locales → PlayerStatsStore
├─ Hall local → HallOfFameStore
└─ fin de partie déjà éligible localement
   → completedAt calculé une seule fois
   → GlobalScoreCompletionBridge
      → mapping spécifique du mode
      → GlobalScoreCompletionPublisher
         1. génère runId
         2. construit GlobalScorePayload
         3. sérialise une fois
         4. écrit PendingScoreStore
         5. déclenche ensuite seulement la synchro
```

## Synchronisation sortante

```text
PendingScoreStore (JSON atomique)
→ GlobalScoreSyncCoordinator mono-worker
→ GeckoDokuHallApiClient / HttpURLConnection
   ├─ 201 accepted → retire pending
   ├─ 200 duplicate accepted → retire pending
   ├─ 429 → Retry-After
   ├─ 5xx / réseau → backoff
   └─ 400/409/413/415 → BLOCKED conservé
```

Même `payloadJson` et même `runId` à chaque retry.

## Synchronisation entrante

```text
GET /api/v1/games/geckodoku/sync?cursor=...&limit=100
→ HallSyncPage
→ GlobalScoreCacheStore.applyPage
   ├─ fusion scoreId
   └─ entrées + nextCursor écrits ensemble
→ hasMore=true ? page suivante : fin
→ runId confirmé ? réconciliation pending
```

Le `highWatermark` ou l’ACK POST ne font jamais avancer le curseur.

## Runtime Android

```text
MainActivity.onCreate
→ GlobalScoreRuntime.start
   ├─ GlobalScoreSyncCoordinator.start
   └─ AndroidNetworkMonitor.start

réseau disponible → coordinator.triggerNow
MainActivity.onDestroy → GlobalScoreRuntime.stop
```

Permissions : INTERNET + ACCESS_NETWORK_STATE.

## Mapping des modes

```text
GECKODOKU → size + puzzle.id + puzzle.seed
SUDOKU → size 9 + seed + SudokuVisualStyle
GOMOKU → GomokuMatchMode + winner/draw/moveCount
BEES_GECKOS → puzzle.id + seed + radius + regionCount
```

Aucune nouvelle règle de victoire/statistique n’est introduite par le HOF global.

## Persistance indépendante

- stats/progression/Hall local : inchangés ;
- pending global : `PendingScoreStore` ;
- cache global + curseur : `GlobalScoreCacheStore` ;
- aucun flux global ne nourrit `PlayerStatsStore`.

## Références finales avant téléphone

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- code HOF final : `9ff902bbe778a2202bdda3ca715e2bbce7cb06dd`
- test/build timestamp : run `37518234014` GREEN
- HOF canonique : run `37519024806` GREEN
- APK validation : run `37519024909` GREEN
- APK SHA-256 : `926058461bb214fbe9e8abcf4825902b8a58129169907a1a4d9749a79c2dd49a`
- validation restante : téléphone online/offline/restart/dédoublonnage.
