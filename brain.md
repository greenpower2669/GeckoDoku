# GeckoDoku — FAB Copilot brain

> Mémoire fonctionnelle courte. Historique : `debughistorical.md`; `sauvegarde.md` reste archive froide.

## Référence

- canonique : `main`
- mission `GECKO-HOF-SYNC-001` : close après validation téléphone, merge et Release
- merge final : `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`
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


## Publication finale

- workflow Release : run `37531494180` / #432 GREEN ;
- Release : `phone-0.15.44-dev-run-432` ;
- titre : `GeckoDoku 0.15.44-dev • téléphone validé` ;
- APK : `GeckoDoku-v0.15.44-dev.apk` ;
- SHA-256 : `555cce892d573aa5d5bd794b78254ce315e789726a474f149546c11e096d108c` ;
- Release finale non prerelease, ciblée sur le merge `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`.


## Google Play / AAB — mission active

- branche : `feature/play-upload-signing-v1`.
- aucun secret ni keystore en clair dans Git.
- secret GitHub attendu : `ANDROID_UPLOAD_STORE_PASSWORD` (24 caractères minimum ; valeur connue uniquement de Fab/GitHub).
- workflow setup : génère une seule fois une clé d'upload RSA 4096 `geckodoku-upload`, exporte le PEM public, chiffre le JKS avec AES-256-CBC/PBKDF2 et refuse tout remplacement.
- workflow AAB : restaure Pierre + banque sprites 240p validée, lance les tests JVM + `bundleRelease`, déchiffre temporairement le JKS, vérifie le certificat, signe l'AAB puis supprime le JKS clair.
- sortie prévue : `GeckoDoku-v<version>-code<versionCode>-signed.aab`.
- Play App Signing : Google pourra conserver la clé de signature finale ; notre clé sert uniquement à l'importation.
- pas de merge `main` ni publication Play sans ordre explicite.
