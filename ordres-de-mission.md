# GeckoDoku — ordre de mission courant

## Mission active

`GECKO-HOF-SYNC-001` — Hall of Fame global v1.

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- branche : `feature/gecko-hof-sync-v1`
- code HOF final vérifié : `9ff902bbe778a2202bdda3ca715e2bbce7cb06dd`
- version : `0.15.43-dev` / code 78
- référence téléphone précédente : `phone-0.15.43-dev-run-403`
- aucune fusion `main` / Release HOF sans ordre explicite de Fab.

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
- retrait pending seulement après ACK accepté ou confirmation `/sync` ;
- `seed` 64 bits en chaîne décimale ;
- champs spécifiques nullable ; `metadata` extensible ;
- 400/409/413/415 bloqués ; 429/5xx/réseau réessayés ;
- `/sync` fusion par `scoreId`, `nextCursor` persisté avec la page ;
- Hall global indépendant des stats/progression/Hall local ;
- `completedAt` est calculé une seule fois pour Hall local + payload global.

## État d’implémentation

Implémenté : payload quatre modes, pending crash-safe, client/ACK, retry/backoff, cache `/sync`, mono-worker, reprise réseau, runtime Android, permissions, raccord `MainActivity`, mapping des quatre modes.

Important Gomoku : seuls les cas déjà considérés comme score local sont publiés ; aucune nouvelle règle de classement n’est inventée.

## Vérification technique

- run `37518234014` : test du timestamp unique + tests JVM + `assembleDebug` GREEN ;
- run `37519024806` : workflow HOF canonique GREEN ;
- run `37519024909` : APK de validation GREEN ;
- artifact : `GeckoDoku-HOF-v1-validation` ;
- SHA-256 : `926058461bb214fbe9e8abcf4825902b8a58129169907a1a4d9749a79c2dd49a` ;
- workflows temporaires de patch/build supprimés après usage.

## Reste autorisé / attendu

- validation téléphone : online, offline→online, redémarrage pending, absence de doublon, champs/catégories ;
- corriger seulement les bugs découverts par cette validation ;
- maintenir les cinq mémoires synchronisées.

Aucun merge `main`, aucune Release avant ordre explicite de Fab.
