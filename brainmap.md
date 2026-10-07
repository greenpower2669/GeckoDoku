# GeckoDoku — FAB Copilot brainmap

Mission `GECKO-HOF-SYNC-001` — terminée, intégrée dans `main`.

## Fin de partie

```text
MainActivity
├─ stats personnelles → PlayerStatsStore
├─ Hall local → HallOfFameStore
└─ complétion éligible
   → completedAt unique
   → GlobalScoreCompletionBridge
   → GlobalScoreCompletionPublisher
   → PendingScoreStore (avant réseau)
   → GlobalScoreSyncCoordinator
   → POST /scores
```

## Retry / ACK

```text
201 accepted                 → retire pending
200 duplicate:true           → retire pending
429                          → Retry-After
5xx / réseau                 → backoff + pending conservé
400/409/413/415              → BLOCKED conservé
```

Toujours même JSON + même `runId`.

## Sync entrant et Hall affiché

```text
GET /sync(cursor)
→ GlobalScoreCacheStore
   ├─ fusion par scoreId
   └─ page + nextCursor persistés ensemble
→ GlobalHallProjection
→ fusion cache global + Hall local
→ dédoublonnage par complétion
→ UI Hall existante
```

Le cache global reste séparé de `PlayerStatsStore`; aucune progression personnelle n’est reconstruite depuis le serveur.

## Médias téléphone

```text
APK phone
├─ Sherpa-ONNX AAR
├─ Piper Pierre UPMC Medium
└─ assets/sprites
   ├─ index.json
   └─ banks/240p (1 305 fichiers vérifiés)
```

La première validation HOF avait utilisé un APK incomplet : Pierre et sprites 240p manquaient. Le build corrigé récupère la banque depuis l’APK téléphone connu bon `phone-0.15.43-dev-run-403` et vérifie son SHA avant extraction.

## Références

- code fonctionnel HOF + restauration Hall : `1e54fe8156bc04de2470456423ec62f125856083`
- run APK téléphone complet : `37526965060` GREEN
- SHA APK validé : `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf`
- clean install Fab : Hall global restauré, 3 résultats visibles
- pipeline Release : `build.yml` restaure désormais la banque 240p depuis la Release validée `phone-0.15.43-dev-run-403` et vérifie Pierre + sprites dans l’APK
- publication autorisée : `0.15.44-dev` / code 79, merge `main` + Release explicitement ordonnés par Fab.


## Publication finale

```text
main
└─ merge c1a32bf4736894f87ac45f1c626f72a111cf1c2e
   └─ workflow phone #432 / run 37531494180 GREEN
      ├─ tests JVM GREEN
      ├─ assemblePhone GREEN
      ├─ Pierre vérifié dans APK
      ├─ sprites 240p vérifiés dans APK
      └─ Release phone-0.15.44-dev-run-432 publiée
```

APK final : `GeckoDoku-v0.15.44-dev.apk`  
SHA-256 : `555cce892d573aa5d5bd794b78254ce315e789726a474f149546c11e096d108c`.


## Google Play — flux de signature

```text
Fab crée ANDROID_UPLOAD_STORE_PASSWORD dans GitHub Actions Secrets
        ↓
setup-play-upload-key.yml
        ├─ keytool RSA 4096 / alias geckodoku-upload
        ├─ certificat public PEM
        ├─ JKS chiffré AES-256-CBC + PBKDF2
        └─ commit du .jks.enc + PEM, jamais du JKS clair
        ↓
build-play-aab.yml
        ├─ Pierre + sprites 240p validés
        ├─ tests JVM
        ├─ bundleRelease
        ├─ déchiffrement temporaire du JKS
        ├─ vérification certificat
        ├─ jarsigner + vérification
        └─ artifact AAB signé
        ↓
Upload manuel Play Console depuis le téléphone
```

Garde-fous : génération one-shot, pas de secret dans le dépôt, pas de remplacement silencieux de clé, pas de merge/main ni publication Play automatique.
