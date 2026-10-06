# GeckoDoku — FAB Copilot brainmap

> Carte technique courte. Mission : `GECKO-HOF-SYNC-001` sur `feature/gecko-hof-sync-v1`.

## Flux produit

```text
MainActivity
├─ Classic / Sudoku / Gomoku / BeeGecko
├─ stats locales → PlayerStatsStore
├─ Hall local → HallOfFameStore
└─ fin de partie déjà éligible localement
   → GlobalScoreCompletionBridge
      → mapping spécifique du mode
      → GlobalScoreCompletionPublisher
         1. génère runId
         2. construit GlobalScorePayload
         3. sérialise une fois
         4. écrit PendingScoreStore
         5. déclenche seulement ensuite la synchro
```

## Synchronisation sortante

```text
PendingScoreStore (fichiers JSON atomiques)
→ GlobalScoreSyncCoordinator mono-worker
→ GeckoDokuHallApiClient / HttpURLConnection
   ├─ 201 accepted → retire pending
   ├─ 200 duplicate accepted → retire pending
   ├─ 429 → Retry-After
   ├─ 5xx / réseau → backoff 5/15/30/60 puis plafond 5 min
   └─ 400/409/413/415 → BLOCKED, entrée conservée
```

Même `payloadJson` et même `runId` à chaque retry.

## Synchronisation entrante

```text
GET /api/v1/games/geckodoku/sync?cursor=...&limit=100
→ HallSyncPage
→ GlobalScoreCacheStore.applyPage
   ├─ fusion scoreId
   └─ entrées + nextCursor écrits atomiquement ensemble
→ hasMore=true ? page suivante : fin
→ runId global confirmé ? réconciliation pending
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
GECKODOKU
→ size réel + puzzle.id + puzzle.seed + Classic

SUDOKU
→ size 9 + sudokuPuzzle.seed + SudokuVisualStyle

GOMOKU
→ GomokuMatchMode + snapshot winner/draw/moveCount
→ uniquement quand le chemin local actuel considère déjà la complétion comme score

BEES_GECKOS
→ puzzle.id + seed + radius + regionCount
```

Aucune nouvelle règle de victoire/statistique n’est introduite par le HOF global.

## Persistance indépendante

- stats/progression/Hall local : inchangés ;
- pending global : `PendingScoreStore` ;
- cache global + curseur : `GlobalScoreCacheStore` ;
- aucun flux global ne nourrit `PlayerStatsStore`.

## Références

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- code HOF raccordé : `d6d25e051c0a4723844df51c21aa99c15cd35c08`
- tests + `assembleDebug` du raccord : run `37508648842` GREEN
- validation restante : téléphone online/offline/restart/dédoublonnage.
