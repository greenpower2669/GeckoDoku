# GeckoDoku Global Hall of Fame Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Relier l’APK GeckoDoku au Hall of Fame global v1 avec persistance offline, retry idempotent, ACK serveur et synchronisation incrémentale, sans modifier gameplay, statistiques locales, progression ni Hall local.

**Architecture:** Le score final est figé en `GlobalScorePayload`, sérialisé une seule fois puis persisté localement avant toute tentative réseau. Un client HTTP minimal basé sur `HttpURLConnection` parle au protocole public, tandis qu’un coordinateur mono-worker vide la file, applique le backoff et fusionne `/sync` dans un cache global séparé.

**Tech Stack:** Kotlin/JVM + Android SDK 26–36, `org.json`, `HttpURLConnection`, `ConnectivityManager`, fichiers JSON atomiques sous `filesDir`, JUnit 4.13.2.

**Spec:** `docs/superpowers/specs/2026-10-06-geckodoku-global-hof-sync-design.md`

## Global Constraints

- Branche de travail : `feature/gecko-hof-sync-v1`, base initiale `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`.
- API publique : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`.
- POST : `/api/v1/games/geckodoku/scores`; GET sync : `/api/v1/games/geckodoku/sync?cursor=<cursor>&limit=100`.
- Aucun header Authorization, aucun secret/API key dans l’APK.
- `schemaVersion=1`, `scoreVersion=1`, `completed=true`.
- `seed` non null toujours envoyée sous forme de chaîne décimale exacte.
- Le payload persisté est immuable au retry : même JSON, même `runId`.
- Retrait de pending uniquement après HTTP 201/200 avec `accepted:true` ou réconciliation `/sync` confirmant le même `runId`.
- 400/409/413/415 : conserver et bloquer pour diagnostic; 429/503/5xx/réseau : conserver et retry.
- Backoff : 5 s, 15 s, 30 s, 60 s, puis plafond 5 min; `Retry-After` prévaut pour 429.
- `/sync` fusionne par `scoreId`; `nextCursor` n’avance qu’avec la persistance de la page; continuer tant que `hasMore=true`, même page vide.
- Le cache global n’alimente jamais `PlayerStatsStore`, progression ou Hall local.
- Aucun merge `main`, aucune release/prerelease sans ordre explicite de Fab.

## Review Focus

- Mort du processus juste après la fin de partie : le score doit déjà être relisible depuis pending au prochain lancement.
- Deux callbacks réseau / deux triggers quasi simultanés : un seul drain doit s’exécuter et aucun double POST concurrent ne doit partir.
- HTTP 200/201 avec JSON invalide ou `accepted:false` : ne jamais supprimer l’entrée pending.
- Échec d’écriture disque / file pleine : ne jamais supprimer une entrée existante ni prétendre que le score a été mis en attente.
- `/sync` avec doublon `scoreId`, page vide `hasMore=true` ou crash pendant fusion : fusion idempotente et curseur non avancé tant que l’état n’est pas écrit.

---

### Task 1: Modèle de score et codec protocole v1

**Files:**
- Create: `app/src/main/java/com/greenpower2669/geckodoku/GlobalScorePayload.kt`
- Create: `app/src/main/java/com/greenpower2669/geckodoku/GlobalScorePayloadFactory.kt`
- Test: `app/src/test/java/com/greenpower2669/geckodoku/GlobalScorePayloadTest.kt`
- Modify: `app/build.gradle.kts` uniquement si `org.json` doit être fourni au classpath des tests JVM.

**Interfaces:**
- Produces: `data class GlobalScorePayload(...)`, `object GlobalScorePayloadCodec { fun encode(payload: GlobalScorePayload): String }`.
- Produces: `sealed interface GlobalScoreModeDetails` avec détails Sudoku/Gomoku/Bee et `object GlobalScorePayloadFactory { fun create(common: GlobalScoreCommon, details: GlobalScoreModeDetails): GlobalScorePayload }`.
- `GlobalScoreCommon` reçoit les valeurs finales déjà calculées : `runId`, `playerName`, `mode`, `difficulty`, `size`, `stars`, `elapsedSeconds`, `mistakes`, `assistancePoints`, `usedProfessor`, `completedAt`, `appVersion`.

- [ ] **Step 1: Écrire les tests rouges du payload commun et des quatre modes**
  - `commonPayloadContainsAllRequiredFields`
  - `classicLeavesForeignModeFieldsNull`
  - `sudokuUsesSize9SeedAndVisualStyle`
  - `gomokuProfessorCarriesMatchWinnerDrawAndMoveCount`
  - `gomokuHumanModeIsRepresentableWithoutChangingGameRules`
  - `beeCarriesPuzzleIdRadiusPairsAndSeed`
  - `longMinAndMaxSeedsAreDecimalStrings`
  - assertions exactes : `schemaVersion=1`, `scoreVersion=1`, `completed=true`, metadata objet `{}`, seed JSON de type String.

- [ ] **Step 2: Lancer `gradle :app:testDebugUnitTest --tests '*GlobalScorePayloadTest' --no-daemon` et vérifier l’échec attendu.**

- [ ] **Step 3: Implémenter le modèle et le codec minimal** sans logique réseau ni Android; utiliser les noms techniques existants `GameMode.name`, `GameDifficulty.name`, `SudokuVisualStyle.name`, `GomokuMatchMode.name`.

- [ ] **Step 4: Relancer le test ciblé; attendu PASS.**

- [ ] **Step 5: Commit `feat: add GeckoDoku global score payload v1`.**

### Task 2: Persistance crash-safe de `pendingScores`

**Files:**
- Create: `app/src/main/java/com/greenpower2669/geckodoku/AtomicJsonFileStore.kt`
- Create: `app/src/main/java/com/greenpower2669/geckodoku/PendingScoreStore.kt`
- Test: `app/src/test/java/com/greenpower2669/geckodoku/PendingScoreStoreTest.kt`

**Interfaces:**
- Produces: `class AtomicJsonFileStore(file: File) { fun read(): String?; fun writeAtomically(content: String): Boolean }`.
- Produces: `data class PendingScoreEntry(runId: String, payloadJson: String, addedAt: Long, attempts: Int, nextAttemptAt: Long, state: PendingScoreState, lastHttpStatus: Int?, lastErrorCode: String?)`.
- Produces: `class PendingScoreStore(directory: File, maxEntries: Int = 1000)` avec `enqueue(runId,payloadJson,now)`, `entries()`, `markRetry(...)`, `markBlocked(...)`, `remove(runId)`, `find(runId)`.

- [ ] **Step 1: Écrire les tests rouges** pour enqueue→nouvelle instance→reload, conservation byte-for-byte de `payloadJson`, même `runId`, duplicate runId identique = no-op, duplicate runId avec JSON différent = refus explicite, entrée bloquée conservée, maxEntries refuse l’ajout sans retirer l’existant, fichier temporaire incomplet n’écrase pas le snapshot valide.

- [ ] **Step 2: Lancer le test ciblé et vérifier FAIL.**

- [ ] **Step 3: Implémenter l’écriture atomique** dans le même dossier (`.tmp`, flush + `fd.sync()`, remplacement atomique/fallback sûr) et le store synchronisé; ne jamais recalculer le payload au chargement.

- [ ] **Step 4: Relancer; attendu PASS.**

- [ ] **Step 5: Commit `feat: persist pending Hall of Fame scores`.**

### Task 3: Client HTTP, ACK et politique de retry

**Files:**
- Create: `app/src/main/java/com/greenpower2669/geckodoku/GeckoDokuHallApiClient.kt`
- Create: `app/src/main/java/com/greenpower2669/geckodoku/GlobalScoreRetryPolicy.kt`
- Test: `app/src/test/java/com/greenpower2669/geckodoku/GeckoDokuHallApiClientTest.kt`
- Test: `app/src/test/java/com/greenpower2669/geckodoku/GlobalScoreRetryPolicyTest.kt`

**Interfaces:**
- Produces: `interface HallHttpTransport { fun execute(request: HallHttpRequest): HallHttpResponse }` et implémentation `UrlConnectionHallHttpTransport`.
- Produces: `class GeckoDokuHallApiClient(transport: HallHttpTransport, baseUrl: String = HALL_BASE_URL)` avec `postScore(payloadJson: String): ScorePostResult` et `fetchSync(cursor: String, limit: Int = 100): SyncPageResult`.
- Produces: `object GlobalScoreRetryPolicy { fun delayMs(attemptNumber: Int, retryAfterSeconds: Long?): Long }`.

- [ ] **Step 1: Écrire les tests rouges** : 201 accepted, 200 duplicate accepted, 200/201 `accepted:false` conservé, JSON ACK invalide conservé, 400/409/413/415 classés BLOCKED, 429 lit `Retry-After`, 503 et autres 5xx RETRY, IOException RETRY; backoff exact 5/15/30/60/300 secondes max.

- [ ] **Step 2: Lancer les deux classes de tests; vérifier FAIL.**

- [ ] **Step 3: Implémenter le transport `HttpURLConnection`** avec `Content-Type: application/json; charset=utf-8`, timeouts bornés, aucune Authorization, lecture du corps erreur/succès sans log du payload.

- [ ] **Step 4: Relancer; attendu PASS.**

- [ ] **Step 5: Commit `feat: add GeckoDoku Hall API client and retry policy`.**

### Task 4: Cache global `/sync` et curseur transactionnel

**Files:**
- Create: `app/src/main/java/com/greenpower2669/geckodoku/GlobalScoreCacheStore.kt`
- Test: `app/src/test/java/com/greenpower2669/geckodoku/GlobalScoreCacheStoreTest.kt`

**Interfaces:**
- Produces: `data class GlobalScoreCacheEntry(scoreId: String, runId: String, sequence: Long, receivedAt: Long, normalizedJson: String)`.
- Produces: `class GlobalScoreCacheStore(directory: File)` avec `state()`, `applyPage(entries: List<GlobalScoreCacheEntry>, nextCursor: String): Boolean`, `entry(scoreId)`, `cursor()`.
- Une seule écriture atomique contient à la fois entrées fusionnées et cursor afin qu’un échec n’avance jamais seulement le curseur.

- [ ] **Step 1: Écrire les tests rouges** : fusion par `scoreId`, replay même page idempotent, remplacement contrôlé d’un même scoreId, `nextCursor` modifié seulement si snapshot écrit, page vide peut avancer le cursor, corruption du nouveau `.tmp` laisse l’ancien état relisible.

- [ ] **Step 2: Lancer le test ciblé; vérifier FAIL.**

- [ ] **Step 3: Implémenter le cache avec `AtomicJsonFileStore`**; conserver le JSON normalisé serveur et indexer par `scoreId`.

- [ ] **Step 4: Relancer; attendu PASS.**

- [ ] **Step 5: Commit `feat: cache global GeckoDoku score sync`.**

### Task 5: Coordinateur mono-worker, retry et retour réseau

**Files:**
- Create: `app/src/main/java/com/greenpower2669/geckodoku/GlobalScoreSyncCoordinator.kt`
- Create: `app/src/main/java/com/greenpower2669/geckodoku/AndroidNetworkMonitor.kt`
- Test: `app/src/test/java/com/greenpower2669/geckodoku/GlobalScoreSyncCoordinatorTest.kt`

**Interfaces:**
- Consumes: `PendingScoreStore`, `GlobalScoreCacheStore`, `GeckoDokuHallApiClient`, `GlobalScoreRetryPolicy`.
- Produces: `class GlobalScoreSyncCoordinator(...){ fun start(); fun stop(); fun enqueue(runId:String,payloadJson:String,now:Long): Boolean; fun triggerNow(); fun syncGlobalNow() }`.
- Produces: `class AndroidNetworkMonitor(context: Context, onAvailable: () -> Unit) { fun start(); fun stop() }` utilisant `ConnectivityManager.registerDefaultNetworkCallback`.

- [ ] **Step 1: Écrire les tests rouges** : deux triggers concurrents => un seul drain, reboot simulé avec pending existant => envoi, succès 201/duplicate retire, 400/409 bloquent sans retirer, 429 planifie selon Retry-After, 503 conserve, payload/runId identiques à chaque retry, `/sync` boucle sur `hasMore=true` y compris page vide, réconciliation d’un runId vu dans `/sync` retire son pending confirmé.

- [ ] **Step 2: Lancer le test ciblé; vérifier FAIL.**

- [ ] **Step 3: Implémenter le coordinateur sur un executor mono-thread**; aucun accès réseau sur UI; scheduler annulable au `stop()`; `AtomicBoolean`/état sérialisé pour empêcher les drains concurrents.

- [ ] **Step 4: Relancer; attendu PASS.**

- [ ] **Step 5: Commit `feat: coordinate offline Hall score synchronization`.**

### Task 6: Raccorder les fins de partie réelles sans changer les règles

**Files:**
- Modify: `app/src/main/java/com/greenpower2669/geckodoku/MainActivity.kt` autour de `onCreate`, lifecycle et `recordRatedCompletionIfNeeded`, plus les appels Classic/Sudoku/Gomoku/Bee existants.
- Modify: `app/src/main/AndroidManifest.xml`
- Test: `app/src/test/java/com/greenpower2669/geckodoku/GlobalScoreCompletionWiringTest.kt` pour la logique extraite testable si nécessaire.

**Interfaces:**
- Consumes: factory, coordinator, stores.
- Ajouter une structure locale/testable `GlobalScoreCompletionContext` ou helper équivalent afin de ne pas enfouir la construction JSON dans `MainActivity`.

- [ ] **Step 1: Écrire les tests rouges du mapping des fins de partie** : Classic taille réelle; Sudoku `size=9`, seed et `gameModePreferences.sudokuVisualStyle`; Bee `puzzle.id`, `seed`, `radius`, `regionCount`; Gomoku snapshot `winner/draw/moveCount` et `gomokuMatchMode` lorsqu’une complétion déjà considérée score local est enregistrée.

- [ ] **Step 2: Vérifier FAIL.**

- [ ] **Step 3: Ajouter `INTERNET` et `ACCESS_NETWORK_STATE` au manifest**, initialiser le coordinateur avec `filesDir`, démarrer/reprendre la synchronisation au lifecycle et arrêter le monitor au destroy.

- [ ] **Step 4: Raccorder l’enqueue exactement dans le chemin de complétion locale existant** : capturer une fois `completedAt`, temps, erreurs, étoiles et assistance; générer `UUID.randomUUID().toString()` une seule fois; écrire pending avant `triggerNow()`. Ne pas créer de nouvelle règle de victoire, d’étoiles ou de statistiques; les cas Gomoku non actuellement éligibles au Hall local restent seulement représentables par le protocole, pas artificiellement ajoutés au classement.

- [ ] **Step 5: Relancer tests ciblés + `gradle :app:testDebugUnitTest --no-daemon`; attendu PASS.**

- [ ] **Step 6: Commit `feat: enqueue global scores from GeckoDoku completions`.**

### Task 7: Vérification complète, FAB Copilot et branche prête téléphone

**Files:**
- Modify: `brain.md`
- Modify: `brainmap.md`
- Modify: `debughistorical.md`
- Modify: `todo.md`
- Modify: `ordres-de-mission.md`
- No release files; no merge.

**Interfaces:**
- Aucun nouvel API produit; cette tâche prouve le lot et documente son état exact.

- [ ] **Step 1: Lancer les 18 scénarios minimum de la spec** en s’assurant que les noms/assertions correspondants sont présents dans les suites des Tasks 1–6.

- [ ] **Step 2: Lancer la commande canonique du workflow : `gradle :app:testDebugUnitTest :app:assemblePhone --no-daemon`** dans un environnement où l’AAR Sherpa et les assets préparés par le workflow sont présents; attendu BUILD SUCCESSFUL.

- [ ] **Step 3: Vérifier le diff complet** : aucun secret, aucune Authorization, aucun changement gameplay/calcul étoiles, aucune réintroduction média.

- [ ] **Step 4: Mettre à jour les cinq mémoires Fab Copilot** avec SHA, tests/build réels et checklist téléphone : online, offline→reconnect, duplicate, restart pending, seed 64 bits, stats locales inchangées.

- [ ] **Step 5: Commit `docs: record GECKO-HOF-SYNC-001 technical validation`.**

- [ ] **Step 6: STOP** : laisser `feature/gecko-hof-sync-v1` prête pour validation téléphone. Ne pas merger `main`, ne pas créer release/prerelease sans ordre explicite de Fab.
