# GeckoDoku — FAB Copilot brain

> Mémoire fonctionnelle courte. Historique : `debughistorical.md`; `sauvegarde.md` reste archive froide.

## Référence

- canonique : `main`
- mission : `GECKO-HOF-SYNC-001`
- branche : `feature/gecko-hof-sync-v1`
- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- code HOF + restauration Hall : `1e54fe8156bc04de2470456423ec62f125856083`
- version de publication : `0.15.44-dev`, code 79
- ordre explicite reçu de Fab le 06/10/2026 : merger `main` et publier la Release HOF.

## Hall global v1

API : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`.

Flux sortant :
`GlobalScoreCompletionBridge → GlobalScoreCompletionPublisher → PendingScoreStore → GlobalScoreSyncCoordinator → GeckoDokuHallApiClient`.

Flux entrant :
`GET /sync → GlobalScoreCacheStore → GlobalHallProjection → HallOfFameStore.entries()`.

Invariants :
- un seul `completedAt` local/global ;
- pending écrit avant réseau ;
- retry = même JSON + même `runId` ;
- retrait seulement après ACK accepté ou `/sync` confirmé ;
- seed 64 bits en chaîne décimale ;
- aucun secret dans l’APK ;
- global ne modifie jamais stats/progression personnelles ;
- Hall affiché fusionne local + cache global et dédoublonne la même complétion.

## Validation téléphone du 06/10/2026

Trois écarts trouvés :
1. Gecko animé vide : banque SpriteRGBA 240p absente du premier APK HOF (`FileNotFoundException`).
2. Pierre absent : modèle Piper non embarqué, fallback `engine=android`.
3. Après désinstallation/réinstallation, le bouton Hall lisait seulement le Hall local et ignorait le cache `/sync`.

Corrections :
- restauration Hall par `GlobalHallProjection` + fusion lecture-only dans `HallOfFameStore` ;
- tests HOF GREEN ;
- nouvel APK construit en variante `phone` avec Pierre UPMC Medium + banque 240p restaurée depuis l’APK téléphone validé `phone-0.15.43-dev-run-403` ;
- APK vérifié : 1 305 fichiers 240p, index Gecko attendu et modèle Pierre présents.

Nouvel APK validation :
- run : `37526965060` GREEN ;
- artifact : `GeckoDoku-HOF-v1-phone-validation` ;
- SHA-256 APK : `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf`.

Le premier APK HOF SHA `926058...` est à considérer invalide pour toute validation média.

## Validation finale téléphone et publication

- désinstallation/réinstallation effectuée par Fab ;
- le Hall global est restauré après clean install : l’UI affiche de nouveau les résultats synchronisés ;
- la capture de validation montre `GeckoDoku Classic · Facile · 3 résultats` ;
- le journal téléphone montre le backend SpriteRGBA actif, les banques 240p chargées en mémoire et les animations Gecko relancées sans `FileNotFoundException` ;
- Fab autorise explicitement le merge `main` et la Release malgré les scénarios offline/retry exhaustifs encore non rejoués dans cette session.

Blocage de publication trouvé puis corrigé avant merge :
- le workflow canonique `build.yml` référençait encore la Release supprimée `PackageSprites` ;
- il restaure désormais la banque 240p depuis l’APK téléphone validé `phone-0.15.43-dev-run-403`, avec SHA-256 figé ;
- il vérifie aussi dans l’APK produit la présence de Pierre et des assets sprites 240p.

Cible de publication : `0.15.44-dev` / code 79.
