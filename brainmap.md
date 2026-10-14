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


Preuve CI : run `38436224654` / #450 GREEN. La chaîne compile et l'APK phone est produit ; seule l'écoute Samsung peut confirmer que le premier phonème n'est plus tronqué.


## Sudoku visuel calculé — flux Binaire (14/10/2026)

```text
SudokuSnapshot.values
→ SudokuVisualReasoning.logicalCandidates()
   → SudokuSolver.candidateMask()
   → Map<Cell, Set<digit>> read-only
      ├─ Lignes → projectionSegments()
      ├─ Couples → nakedGroups(size=2)
      ├─ Triplets → nakedGroups(size=3)
      ├─ Binaire → binaryGroups()
      │   ├─ scan 9 lignes
      │   ├─ scan 9 colonnes
      │   ├─ scan 9 blocs
      │   └─ garder uniquement count == 2
      └─ Entourer → validation du candidat choisi

Checkbox
→ flag overlay
→ Prévisu.setPreviewMessage()
→ Pierre.speakSimpleProfessorBubble()
```

Invariant : cette carte n'est jamais recopiée dans `SudokuGameEngine.notes`. Les annotations disparaissent/recalculent avec la grille, sans mutation de gameplay.


Preuve build Binaire : Run `38504109994` / #464 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11867790810`, APK SHA-256 `356cb9973995d453d6e82e865b959f4648502624705322502e23d711ec52cb5e`.


## Projection Sudoku complète — 14/10/2026

```text
Settings
├─ Pseudo → editPlayerName() → même profil / nouveau nom public
└─ Profils locaux → choosePlayerProfile() → identité stats séparée

SudokuSnapshot.values
→ logicalCandidates()
→ projectionSegments()
   ├─ ligne : count(digit) == 2
   │  → axe horizontal board.left → board.right
   └─ colonne : count(digit) == 2
      → axe vertical board.top → board.bottom
→ alpha faible + deux anneaux de repère

Pierre Sudoku
→ éliminations
→ regrouper ROW / COLUMN / BOX
→ narration courte ordonnée
→ conclusion
```


Preuve build projection complète : Run `38519610024` / #472 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11873097206`, APK SHA-256 `562218c0d5cc2622e106a582bd5ba8a9bdec667fbec84fc72d0bedb3052ec4b7`.

## Sudoku — architecture repères humains / aide surhumaine (14/10/2026)

```text
Pavé Sudoku
├─ saisie valeur / note / hypothèse
└─ ?
   ├─ 2e clic sur zone du pavé
   │  → aide locale Pierre
   └─ 2e clic sur plateau
      → confirmation « aide de sur-humain ? »
      → Oui
         → popup Aide surhumaine
            ├─ Projection auto
            ├─ Couples auto
            ├─ Triplets auto
            ├─ Candidats auto
            ├─ Binaire auto
            └─ Pulsation

Bouton ✎ Repères
→ popup Repères humains
   ├─ Projection
   │  → sélectionner cellule
   │  → chiffre via zone Candidats
   │  → petite pièce + croix ligne/colonne
   ├─ Couple
   │  → 2 taps cellules
   ├─ Triplet
   │  → 3 taps cellules
   ├─ Candidat
   │  → cellule + chiffre
   └─ Effacer

SudokuValueOverlayView
├─ auto: logicalCandidates() (read-only)
└─ manuel: manual* (aucun appel solveur)
```

Invariant : `manual*` ne devient jamais une preuve logique. L'auto reste explicitement opt-in.


Preuve build repères humains : Run `38522492488` / #486 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11875030286`, APK SHA-256 `793a3878817fa04f9b2096d3ac032c8f235c98df50f6a7f56df0139dda062d15`.

## Carte — Hall comparatif / progression personnelle (14/10/2026)

```text
PlayerProfileStore
├─ local id → PlayerStatsStore(profile) → progression/stats personnelles
└─ progressionId UUID opaque
   ├─ HallOfFameEntry.playerProfileId
   └─ GlobalScore metadata.playerProfileId

Partie terminée
→ score local Hall (pseudo + progressionId)
→ payload global metadata.playerProfileId
→ /sync
→ GlobalScoreCacheStore
→ GlobalHallProjection
   ├─ classement global : TOUS les scores
   ├─ remote id connu → conserve id
   ├─ remote id absent + copie locale exacte non ambiguë → hydrate
   └─ ambigu / historique → id null, classement seulement

Hall niveau mode+difficulté
├─ classement global complet
└─ HallPlayerProgressionProjection
   └─ GROUP BY playerProfileId uniquement
      ├─ pseudo courant / anciens pseudos
      ├─ historique chronologique
      ├─ tendance temps
      └─ tendance étoiles

Pierre
→ profil actif
→ statsStore actif uniquement
→ changement profil : stop voix + nouveau ProfessorLife + reset ambient
→ jamais d'analyse parlée des autres sous-progressions
```


Preuve build HOF isolation : Run 38526555276 / #510 : GREEN. Commit code validé 29151fc63bd9f8b05d5ba188276f6cfa197e5998. Tests JVM + assemblePhone réussis, Pierre et banque sprites 240p vérifiés. Artefact GeckoDoku-v0.15.45-dev-phone ID 11876601009. APK SHA-256 eec808012b45e21a36a400672b1c50d7b2db41a27df9310db2df30eccef45981.

## Sudoku floating layers — 14/10/2026

```text
sudokuBoard geometry
→ compactAboveBoard()
→ Repères humains (78dp, largeur plateau)
→ drag
→ save x/y/w/h
→ reopen
→ clamp()

ProfessorSpeech speaking=true
→ record current geometry already tracked
→ PopupWindow clipping=false
→ move Repères / SuperHelp / Palette offscreen
→ Pierre views bringToFront()

ProfessorSpeech speaking=false
→ update popup x/y/w/h to previous geometry
→ clipping=true
→ previous functional layer arrangement restored
```


Preuve build Repères compact : Run `38537609942` / #522 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11880727507`, APK SHA-256 `3f6dbba028b4bbb793ed16cbf3a9bd3eb237f36963c4e9fd8a292da21fed911c`.

## Zone sûre pour les trois panneaux Sudoku — mission 005 (14/10/2026)

```text
window.decorView / screenRoot + WindowInsets(systemBars, displayCutout)
→ sudokuPopupSafeBounds() : coordonnées absolues de fenêtre
→ SudokuFloatingWindowGeometryPolicy.safeBounds()
  ├─ Repères humains : compactAboveBoard → clampToBounds → préférences x/y/w/h
  ├─ Aide surhumaine : boardRelativeTop → clampToBounds
  └─ Pavé : bottomCentered / drag / resize → clampToBounds
→ protectFromSystemBars : onInsets + recalage des PopupWindow visibles
→ Pierre speaking : suspend hors écran / restore puis clamp de sûreté
```

Référence : `missions/GECKO-SUDOKU-SYSTEM-BARS-005.md`. Aucun changement du moteur Sudoku ni de la signature. Preuve CI / APK et recette Samsung à compléter.

## Projection manuelle vs hypothèse — mission 006 (14/10/2026)

```text
SudokuValueOverlayView
├─ manualProjectionRefs -> SudokuManualProjectionPolicy.axesFor(ref, refs)
│  ├─ références même chiffre sur une ligne -> ROW seulement
│  ├─ références même chiffre sur une colonne -> COLUMN seulement
│  └─ isolé/égalité -> ROW seulement
├─ SudokuSnapshot.hypothesisAt(cell) -> axesFor(isHypothesis=true)
│  └─ ROW + COLUMN (traits translucides, sous les chiffres)
└─ autres visualisations (candidats/couples/triplets/prof) inchangées
```

`MainActivity.placeSudokuManualDigitMarker()` reflète la direction réelle dans son message. Tests dans `SudokuVisualReasoningTest.kt`. Mission `missions/GECKO-SUDOKU-MANUAL-PROJECTION-006.md`. CI phone à vérifier.

## Fenêtres Sudoku — correctifs intro et clavier (missions 007/008)

```text
refreshSudokuUi + applyGameModeVisibility + onLayout / intro terminée
→ canShowSudokuManualTools()
   → SudokuFloatingStartupPolicy.canOpen()
   → mode SUDOKU + intro DONE + vidéo absente
   → grille visible, mesurée + non fermé manuellement
   → ouverture de Repères autorisée
Pendant la parole Pierre → blocage ouverture; reprise après restitution

sudokuBoard.getLocationInWindow() + hauteur
→ SudokuFloatingWindowGeometryPolicy.dockBelowBoard(safe,inset,gap)
→ showSudokuCellPalette : bottomCentered(dock)
→ onDragDelta / onResizeDelta / reconcile : clamp(dock)
→ clavier sous la grille sans cacher la ligne 9 (portrait normal)
```

Références : `missions/GECKO-SUDOKU-INTRO-GATE-007.md` et `missions/GECKO-SUDOKU-KEYPAD-DOCK-008.md`. Ne pas modifier les règles de jeu ; CI et recette Samsung à enregistrer.

### Preuve CI Samsung 007/008 — run #525
Validation finale après les missions 007 et 008 : run [#525](https://github.com/greenpower2669/GeckoDoku/actions/runs/38587086273), workflow 38587086273 **GREEN**, commit de build `eee5347f52602ccbc91d1359aeacb161c427d9f1`. `testDebugUnitTest + assemblePhone` réussis ; vérification Pierre Sherpa/Piper et banque sprites 240p réussie. Artefact `GeckoDoku-v0.15.45-dev-phone` ID `11905448115` ; APK `GeckoDoku-v0.15.45-dev.apk` SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`, contrôlé après extraction. La recette Samsung demeure nécessaire. Aucun merge main / Release / Play.

## Preuve GitHub — APK direct #525 (14/10/2026)
Livraison APK brut run #525 : prérelease temporaire `temp-geckodoku-0.15.45-dev-run-525` créée et vérifiée par l'API GitHub. [Téléchargement direct .apk](https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-0.15.45-dev-run-525/GeckoDoku-v0.15.45-dev.apk), 299496421 octets, SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`. Workflow de publication #527 GREEN. La première tentative #526 (403 GitHub) a été corrigée en reprenant la méthode qui fonctionnait : checkout de la branche et création du tag sur le commit déclencheur. Aucun nouveau build Android, aucun merge main ou Release stable. Règle permanente de livraison d'APK direct enregistrée dans la section `Contrat FAB Copilot` d'`ordres-de-mission.md`.

## Flux pavé Sudoku compact dès l'ouverture — mission 009

```text
showSudokuCellPalette(cell)
├─ palette existante → refreshSudokuPersistentPalette()
│                      → fitOpenSudokuPaletteToDock() (contrôle fenêtre réelle)
└─ nouvelle palette
   → sudokuKeyboardDockBounds() (bord inférieur du plateau + insets)
   → SudokuFloatingWindowGeometryPolicy.compactKeypadBelowBoard()
   → popupX/Y/Width/Height = géométrie finale
   → PopupWindow(palette, largeur FINALE, hauteur FINALE)
   → showAtLocation(x/y)
layout / insets
→ fitOpenSudokuPaletteToDock()
→ compare geometry ET popup.width/height
→ update(...) seulement si divergent
```

Conserver taille grille, repères au-dessus, fenêtre sous grille, calques Pierre, préférences de l'utilisateur. Mission `GECKO-SUDOKU-KEYPAD-OPEN-COMPACT-009`. Validation CI et recette Samsung non achevées à la rédaction.

## AAB signé et merge conditionnel — mission 010

```text
Secrets GitHub (chiffrés, jamais lus par l'assistant)
→ build-play-aab.yml : reconstruction JKS temporaire
→ certificat SHA1 = notification Google Play du 12/10 (bloquant)
→ préparation Pierre + sprites 240p → 299 tests
→ :app:bundleRelease → jarsigner -verify
→ avant merge : validation branch feature/sudoku-visual-reasoning-v1
→ si GREEN : merge PR #4 → main
→ nouveau build AAB signé sur main → Release AAB + SHA-256
→ Google Play : envoi ultérieur contrôlé (aucune autopublication)
```

Protocole immuable conservé en tête d'`ordres-de-mission.md`. Voir `missions/GECKO-PLAY-MERGE-SIGNED-RELEASE-010.md`.

## Mission 010 close — production AAB signé, GitHub Release (14/10/2026)

```text
PR #4 (Sudoku visuel + Hall isolé + signature Play) -> main f48dcd7
Secrets GitHub -> Upload Key reconstruite temporairement
-> SHA1 Google Play validé -> 299 tests + bundleRelease
-> Pierre + sprite bank 240p -> jarsigner verify
-> GitHub Release stable AAB code80 + SHA256
-> Play Console : envoi/acceptation restent à réaliser
```

**Clôture validée 14/10/2026 — mission 010.** Feu vert de Fab « merge release avec la secret key ». PR #4 mergée sur `main`, SHA merge `f48dcd7d6898793ccd6d63b22c0bc20644d6966a`. Contrôle pré-merge AAB sur branche : workflow `38636402228` GREEN (certificat Upload Key conforme au Play SHA-1 `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`, tests, Pierre et sprites 240p, `jarsigner -verify`). Build officiel depuis main : workflow `38636963727` GREEN, tests + `bundleRelease`, même contrôle certificat, `jar verified.`. GitHub Release **publique/stable** : https://github.com/greenpower2669/GeckoDoku/releases/tag/geckodoku-v0.15.45-dev-code80 ; AAB signé direct : https://github.com/greenpower2669/GeckoDoku/releases/download/geckodoku-v0.15.45-dev-code80/GeckoDoku-v0.15.45-dev-code80-signed.aab ; SHA-256 `08390c0e502380ee5d3b6e2e77e01d491ff2a9535c61524b9b5270eb92cd755c`. Application `com.greenpower2669.geckodoku`, versionName `0.15.45-dev`, versionCode `80`. L'APK téléphone précédent reste un build à signature test : NE PAS le présenter comme signé Play. Aucun envoi à Play Console réalisé ; l'acceptation de l'AAB par Google n'est pas encore démontrée. Aucune clé privée ni valeur de Secret dans Git.
