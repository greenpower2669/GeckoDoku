# GeckoDoku — ordre de mission courant

## Mission active

`GECKO-HOF-SYNC-001` — Hall of Fame global v1.

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- branche : `feature/gecko-hof-sync-v1`
- dernier commit code HOF : `d6d25e051c0a4723844df51c21aa99c15cd35c08`
- version de départ : `0.15.43-dev` / code 78
- référence téléphone précédente : `phone-0.15.43-dev-run-403`

## Contrat FAB Copilot

1. Lire le code courant avant toute modification ; ne jamais recoder depuis une vieille mémoire.
2. Synchroniser après chaque geste significatif : `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md`, `ordres-de-mission.md`.
3. `sauvegarde.md` est archive froide, jamais vérité courante.
4. Aucun merge `main` ni Release/prerelease sans ordre explicite de Fab.
5. Ne pas restaurer d’anciens médias supprimés.

## Contrat HOF canonique

Hôte : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`

- POST `/api/v1/games/geckodoku/scores`
- GET `/api/v1/games/geckodoku/sync`
- aucun secret / Authorization dans l’APK ;
- `runId` idempotent ;
- payload figé et persisté avant réseau ;
- retry = même JSON + même `runId` ;
- retrait pending uniquement après ACK accepté ou confirmation `/sync` ;
- `seed` 64 bits envoyée en chaîne décimale ;
- champs propres aux modes nullable ; `metadata` extensible ;
- 400/409/413/415 bloqués pour diagnostic ; 429/5xx/réseau réessayés ;
- `/sync` fusion par `scoreId`, curseur `nextCursor` persisté avec la page ;
- le Hall global ne modifie jamais stats, progression ou Hall local.

## État d’implémentation

Implémenté et vérifié sur la branche :

- payload protocole v1 pour Classic, Sudoku, Gomoku et Abeilles & Geckos ;
- `PendingScoreStore` crash-safe ;
- client HTTP + ACK 200/201 + conflits/erreurs ;
- retry progressif + `Retry-After` ;
- cache global + `/sync` transactionnel ;
- coordinateur mono-worker + reprise au démarrage/retour réseau ;
- `GlobalScoreCompletionPublisher` : persistance avant déclenchement réseau ;
- `GlobalScoreCompletionBridge` : mapping des quatre modes sans nouvelle règle de gameplay ;
- `GlobalScoreRuntime` Android ;
- permissions `INTERNET` et `ACCESS_NETWORK_STATE` ;
- raccord aux complétions locales existantes dans `MainActivity` ;
- arrêt du runtime dans `onDestroy`.

Important Gomoku : conformément au plan, seuls les cas déjà considérés comme score local sont publiés. Les autres formats restent représentables par le protocole mais aucune nouvelle règle de classement n’a été inventée.

Vérification technique du raccord réel : workflow temporaire de validation `37508648842` — tests JVM + `assembleDebug` GREEN avant commit du code. Les workflows temporaires ont ensuite été supprimés.

## Reste autorisé / attendu

- finir la vérification CI dédiée HOF après nettoyage documentaire ;
- validation téléphone réelle : online, offline→online, redémarrage pending et absence de doublon ;
- corriger uniquement les bugs découverts par cette validation ;
- mettre à jour les cinq mémoires.

Aucun merge `main`, aucune Release avant ordre explicite de Fab.
