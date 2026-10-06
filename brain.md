# GeckoDoku — FAB Copilot brain

> Mémoire fonctionnelle courte. Historique : `debughistorical.md`, puis `sauvegarde.md` seulement si nécessaire.

## Référence

- canonique : `main`
- mission : `GECKO-HOF-SYNC-001`
- branche : `feature/gecko-hof-sync-v1`
- base : `1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- code HOF final vérifié : `9ff902bbe778a2202bdda3ca715e2bbce7cb06dd`
- version : `0.15.43-dev`, code 78
- aucun merge `main` / Release HOF sans ordre explicite de Fab.

## Hall of Fame global v1

API : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`

Architecture réelle :
`GlobalScoreCompletionBridge → GlobalScoreCompletionPublisher → PendingScoreStore → GlobalScoreSyncCoordinator → GeckoDokuHallApiClient`.

Flux inverse : `GET /sync → GlobalScoreCacheStore`.

Invariants :
- un seul `completedAt` figé pour Hall local + payload global ;
- payload et `runId` figés avant réseau ;
- retry sans recalcul, même JSON + même `runId` ;
- retrait pending seulement après ACK accepté ou confirmation `/sync` ;
- 429 respecte `Retry-After`, 5xx/réseau retry ;
- 400/409/413/415 restent BLOCKED pour diagnostic ;
- seed 64 bits = chaîne décimale ;
- aucun secret dans l’APK ;
- aucun résultat global ne modifie stats/progression/Hall local.

Modes mappés :
- Classic : taille + puzzle id/seed ;
- Sudoku : 9×9 + seed + style visuel ;
- Gomoku : mode de match + winner/draw/moveCount pour les complétions déjà éligibles localement ;
- Abeilles & Geckos : puzzle id/seed + rayon + nombre de paires.

Runtime Android : lancement `MainActivity`, reprise au retour réseau, arrêt au destroy, permissions INTERNET + ACCESS_NETWORK_STATE.

## Vérification technique finale

- correctif timestamp unique : tests + `assembleDebug` GREEN, run `37518234014` ;
- workflow HOF canonique propre : GREEN, run `37519024806` ;
- APK de validation HOF : GREEN, run `37519024909` ;
- artifact : `GeckoDoku-HOF-v1-validation` ;
- SHA-256 APK : `926058461bb214fbe9e8abcf4825902b8a58129169907a1a4d9749a79c2dd49a` ;
- workflow temporaire APK supprimé après génération.

## Produit à préserver

Ne pas modifier pendant la validation HOF : gameplay, étoiles/erreurs/aides, stats locales, Hall local, géométrie/gestes, Prof/Pierre et médias.

## Prochaine validation

Téléphone réel :
1. finir une partie online → une seule entrée serveur ;
2. finir offline → score pending conservé ;
3. tuer/redémarrer offline → pending survit ;
4. remettre le réseau → envoi automatique ;
5. vérifier retry/doublon et champs/catégories.

Après validation téléphone seulement : décider avec Fab du merge/release.
