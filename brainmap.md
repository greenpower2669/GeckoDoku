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


## Chantier ouvert — Sudoku accessible (12/10/2026)

`GECKO-SUDOKU-VISUAL-001` → [`missions/GECKO-SUDOKU-VISUAL-001.md`](missions/GECKO-SUDOKU-VISUAL-001.md) ; `feature/sudoku-visual-reasoning-v1` ; **mission déposée, implémentation à faire**. Consulter `todo.md` pour les étapes et `ordres-de-mission.md` pour les interdictions de merge/release. La référence de publication HOF ci-dessus reste inchangée.


## Hall global : identités/progressions — chantier à part (12/10/2026)

`GECKO-HOF-PLAYER-ISOLATION-002` → [`missions/GECKO-HOF-PLAYER-ISOLATION-002.md`](missions/GECKO-HOF-PLAYER-ISOLATION-002.md), même branche temporaire que le chantier Sudoku. Objectif : isoler chaque profil dans les statistiques/progressions, conserver le classement global multi-joueurs, couvrir les quatre modes. **Signalement à auditer ; non codé.** Voir `todo.md`.


## Code ajouté aux deux missions — 12/10/2026

`GECKO-SUDOKU-VISUAL-001` → `SudokuVisualReasoning.kt`, `SudokuValueOverlayView.kt`, options/pavé dans `MainActivity.kt`. `GECKO-HOF-PLAYER-ISOLATION-002` → `PlayerProfileStore.kt`, `PlayerStats.kt`, `UserDataBackup.kt`, `GlobalHallProjection.kt`, interface Réglages/Hall dans `MainActivity.kt`. Version test 0.15.45-dev/code80 sur `feature/sudoku-visual-reasoning-v1`. **CI finale et validation Samsung en attente ; pas de merge/release**. Consulter `todo.md` pour les preuves et défauts restants.


## Validation finale CI du lot 0.15.45-dev — 12/10/2026

- Workflow `Build GeckoDoku phone APK`, run [38314483379](https://github.com/greenpower2669/GeckoDoku/actions/runs/38314483379) : **GREEN**, `gradle :app:testDebugUnitTest :app:assemblePhone` → `BUILD SUCCESSFUL`.
- Étapes Pierre, banque sprites 240p (source 1 305 fichiers, modèle et manifest dans l'APK) et publication de l'artefact : SUCCESS.
- Artefact Actions : `GeckoDoku-v0.15.45-dev-phone` (ID `11775463777`) contenant `GeckoDoku-v0.15.45-dev.apk` et `GeckoDoku-v0.15.45-dev.apk.sha256` ; APK SHA-256 : `ddff9b30a3267573cbf0a1cecad98fa6c76ca7aee94f670533a2169e1701d586`.
- Cette version est le **build de test phone signé avec la clé debug existante**, PAS une publication Google Play. La signature Google Play n'est pas modifiée.
- PR brouillon #4 : https://github.com/greenpower2669/GeckoDoku/pull/4 ; ne pas merger ni publier avant accord explicite.
- **À faire** : recette physique Samsung par Fab (UI, Pierre, quatre modes, deux profils/homonymes, restauration des données). Les anciens résultats localement mélangés, sans identité d'origine, ne peuvent être reclassés automatiquement.


## Chaîne Pierre préchauffée — 13/10/2026

```text
MainActivity.onCreate
→ ProfessorSpeech
→ PierrePiperSpeechEngine
   └─ worker mono-thread
      → ensureTts()
      → generate("Bonjour, je suis prêt.")
      → VoicePcmPlayer persistent STREAM
         ├─ volume = 0 (amorce inaudible)
         ├─ write PCM float
         ├─ attendre playbackHeadPosition >= frames écrites
         └─ pause sans release
      → WARMUP_COMPLETE
      → demande réelle suivante
         → generate(vraie phrase)
         → même AudioTrack (TRACK_REUSE)
         → volume = 1
         → fin réelle par playback head
```

Invariant : aucun timer de durée ne décide de la fin TTS. Le petit polling ne sert qu'à relire le curseur matériel sans boucle CPU agressive. `stop()` vide la file sans libérer la piste ; `release()` seul détruit l'AudioTrack. En cas d'échec de la voie Piper, `ProfessorSpeech` conserve le fallback Android.
