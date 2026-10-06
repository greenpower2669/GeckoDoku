# GeckoDoku — FAB Copilot brain

> Mémoire fonctionnelle courte. Historique : `debughistorical.md` puis `sauvegarde.md` si nécessaire.

## Référence

- canonique : `main`
- mission : `GECKO-HOF-SYNC-001`
- branche : `feature/gecko-hof-sync-v1`
- base : `1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- dernier code HOF : `d6d25e051c0a4723844df51c21aa99c15cd35c08`
- version : `0.15.43-dev`, code 78
- aucune fusion `main` / Release HOF autorisée sans Fab.

## Hall of Fame global v1

API : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`

Architecture réelle :
`GlobalScoreCompletionBridge → GlobalScoreCompletionPublisher → PendingScoreStore → GlobalScoreSyncCoordinator → GeckoDokuHallApiClient`

Flux inverse :
`GET /sync → GlobalScoreCacheStore`.

Invariants :
- payload et `runId` figés avant réseau ;
- retry sans recalcul ;
- ACK accepté ou `/sync` confirmé avant retrait pending ;
- 429 respecte `Retry-After`, 5xx/réseau retry ;
- 400/409/413/415 restent bloqués pour diagnostic ;
- seed 64 bits = chaîne décimale ;
- aucun secret dans l’APK ;
- aucun résultat global ne modifie stats/progression/Hall local.

Les quatre modes sont mappés :
- Classic : taille + puzzle id/seed ;
- Sudoku : 9×9 + seed + style visuel ;
- Gomoku : mode de match + winner/draw/moveCount pour les complétions déjà éligibles localement ;
- Abeilles & Geckos : puzzle id/seed + rayon + nombre de paires.

Runtime Android :
- démarrage au lancement de `MainActivity` ;
- reprise sur retour réseau ;
- arrêt monitor/coordinator au destroy ;
- permissions INTERNET + ACCESS_NETWORK_STATE.

Tests JVM et `assembleDebug` du raccord réel : GREEN dans le run `37508648842` avant commit produit.

## Produit à préserver

Quatre modes : Classic, Sudoku, Gomoku, Abeilles & Geckos.

Ne pas modifier pendant la validation HOF :
- gameplay ;
- calcul étoiles/erreurs/aides ;
- stats locales et progression ;
- Hall local ;
- géométrie/gestes ;
- Prof/Pierre ;
- médias.

Sudoku reste : pavé 2×2 persistant, Choix/Candidats/Hypothèse/Prévisu, drag/resize, aide `?` documentaire sans coût.

Hypothèses Classic/Sudoku/Bee restent parent/enfant colorées avec prune des descendants/croix/aura.

Médias : Gecko/Abeille SpriteRGBA 240p ; Plante, Prof/Pierre et intros conservés.

## Prochaine validation

Téléphone réel :
1. terminer une partie online → une seule entrée serveur ;
2. terminer offline → score conservé ;
3. relancer l’app offline → pending toujours présent ;
4. remettre le réseau → envoi automatique ;
5. vérifier absence de doublon et cohérence catégorie/champs.

Après validation téléphone seulement : décider avec Fab du merge/release.
