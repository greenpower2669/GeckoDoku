# GeckoDoku — FAB Copilot debug historical

> Historique condensé. La vérité courante reste `brain.md` + `ordres-de-mission.md`.

## Baseline avant HOF

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- version `0.15.43-dev`, code 78
- téléphone validé avant HOF : `phone-0.15.43-dev-run-403`
- quatre modes, Pierre, médias 240p, stats et Hall local déjà fonctionnels.

## GECKO-HOF-SYNC-001 — 06/10/2026

Implémenté en TDD : payload 4 modes, pending crash-safe, client ACK, backoff, cache `/sync`, mono-worker, reprise réseau, runtime Android, mapping des complétions et timestamp local/global unique.

Invariants : pending avant réseau, retry exact, erreurs permanentes BLOCKED, `/sync` transactionnel, seed décimale 64 bits, aucun secret APK, aucune donnée globale injectée dans stats/progression.

## Régressions téléphone du premier APK de validation

Fab a fourni capture + journal.

### Médias

Symptômes : Gecko invisibles/empreintes vides et Pierre muet.

Preuves journal :
- `SpriteBankFactory ... SPRITE_BANK_ERROR ... FileNotFoundException` sur `gecko/alive/stay*.mp4`, `Gecko_apparition.mp4`, etc. ;
- `ProfessorSpeech ... SPEAK_STARTED ... engine=android`.

Cause : l’APK de validation HOF avait été construit via une chaîne légère `assembleDebug` sans le packaging téléphone complet. Le code gameplay n’était pas en cause.

Correction build :
- Piper UPMC Medium téléchargé avec checksum canonique ;
- ancienne Release technique `PackageSprites` constatée absente ;
- banque 240p restaurée depuis l’APK téléphone validé `phone-0.15.43-dev-run-403`, SHA connu `5b4f38049c3c7e8d115b78bef573784084ddec85cd4774d620dd0d76a4ba7a94` ;
- `assemblePhone` ;
- contrôle APK : modèle Pierre présent, index sprites présent, bank-manifest 240p présent, 1 305 fichiers 240p, entrées Gecko signalées par le journal présentes.

### Hall après réinstallation

Symptôme : un score global Facile existait mais, après désinstallation/réinstallation, l’UI disait « aucune partie terminée » ; une nouvelle partie recréait ensuite 1 résultat.

Cause : `/sync` alimentait bien `GlobalScoreCacheStore`, mais `showHallOfFame()` lisait seulement `HallOfFameStore.entries()` local. La désinstallation effaçait donc la seule source affichée.

Correction TDD :
- `GlobalHallProjectionTest` RED ;
- `GlobalHallProjection` convertit les entrées cache valides en `HallOfFameEntry` ;
- `HallOfFameStore.entries()` fusionne local + cache global en lecture seule ;
- même complétion locale/globale dédoublonnée grâce au `completedAt` commun ;
- entrées invalides/inachevées ignorées ;
- tests HOF GREEN.

## État actuel

- code fonctionnel : `1e54fe8156bc04de2470456423ec62f125856083`
- build téléphone complet : run `37526965060` GREEN
- artifact : `GeckoDoku-HOF-v1-phone-validation`
- SHA-256 : `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf`
- workflow temporaire de build supprimé après génération.

Reste : validation réelle animations/Pierre, restauration Hall après clean install, puis scénarios offline/retry. Aucun merge/release avant ordre de Fab.


## Validation finale et préparation Release — 06/10/2026

Fab a effectué une désinstallation/réinstallation sur téléphone. Le Hall global a été restauré après clean install et l’UI affiche `GeckoDoku Classic · Facile · 3 résultats`. Le journal montre les banques 240p chargées et les animations Gecko relancées sans l’ancienne erreur de fichiers absents.

Fab a ensuite donné l’ordre explicite de merger `feature/gecko-hof-sync-v1` dans `main` et de publier la Release.

Avant publication, contrôle du workflow canonique :
- la Release `PackageSprites` référencée par `.github/workflows/build.yml` n’existe plus (404 GitHub) ;
- la Release téléphone validée `phone-0.15.43-dev-run-403` existe et son APK a le SHA-256 `5b4f38049c3c7e8d115b78bef573784084ddec85cd4774d620dd0d76a4ba7a94` ;
- le workflow canonique a donc été aligné sur la méthode déjà validée par le run `37526965060` : restauration de `assets/sprites` depuis cet APK connu bon, puis vérification de Pierre et de la banque 240p dans l’APK produit ;
- version de publication portée à `0.15.44-dev`, code 79.

Les tests offline/retry/doublons exhaustifs n’ont pas tous été rejoués manuellement après ce clean install ; Fab accepte explicitement ce reliquat de validation pour cette publication.


## Merge et Release finale HOF

- PR #3 mergée dans `main` par le commit `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`.
- Le merge a déclenché le workflow téléphone canonique avec `[phone-release] [phone-publish]`.
- Run `37531494180` / #432 : GREEN.
- Étapes GREEN : restauration sprites 240p depuis APK validé, tests JVM, `assemblePhone`, vérification Pierre + banque sprites, création prerelease puis promotion.
- Release finale : `phone-0.15.44-dev-run-432`.
- APK : `GeckoDoku-v0.15.44-dev.apk`.
- SHA-256 : `555cce892d573aa5d5bd794b78254ce315e789726a474f149546c11e096d108c`.


## 12/10/2026 — préparation GECKO-SUDOKU-VISUAL-001

Création de `feature/sudoku-visual-reasoning-v1` depuis `main` et dépôt de `missions/GECKO-SUDOKU-VISUAL-001.md`. **Aucune modification de code, aucun test ni APK nouveau à ce stade** ; aucune régression liée à cette mission observée ou corrigée. Historique HOF précédent conservé.


## 12/10/2026 — retour utilisateur : Hall/progression mêlant plusieurs pseudonymes

Fab rapporte que l'affichage de scores/progressions du Hall semble associer plusieurs pseudos à une même personne. **Symptôme déclaré, origine technique non prouvée et non reproduite à cette date.** Ne pas accuser le serveur avant audit API/cache/projection/statistiques locales/UI. Mission de diagnostic et correction : `missions/GECKO-HOF-PLAYER-ISOLATION-002.md`. Aucun correctif/code/build effectué lors de cette documentation.


## 12/10/2026 — diagnostic et corrections des deux missions

- Hall : `MainActivity.showHallOfFameLevel` posait sous un classement mondial multi-pseudonymes la courbe produite par `statsStore.eventsForModeAndDifficulty`, qui correspondait uniquement au téléphone ; fausse attribution visuelle. Courbe retirée du Hall mondial, accès maintenu via Stats du niveau du profil actif.
- `GlobalHallProjection.merge` supprimait par `HallIdentity` les doublons composites, risquant de fusionner deux scores réellement différents de joueurs homonymes aux mêmes métriques. Nouvelle fusion : conserver chaque score du cache déjà unique par `scoreId`, retirer tout au plus UNE copie locale par score mondial correspondant.
- Stockage personnel ancien : les préférences stats étaient communes à l'appareil et au pseudo modifiable ; introduction de profils locaux stables, séparation des stores par profil, conservation intégrale du store historique (pas de réattribution rétroactive sans identité).
- Sudoku : surimpressions distinctes des notes et du solveur, aucun effet sur la grille ; pulsation lente facultative pour éviter les flashs rapides.
- Première CI a trouvé `PlayerProfileStore.kt:51 Unresolved reference id` : corrigé par `buildList<GeckoPlayerProfile>` explicite.
- Deuxième CI a trouvé un test X-Wing incorrect : un candidat ajouté dans une ligne n'invalidait pas l'autre orientation par colonnes. Le test a été corrigé en invalidant les deux orientations, sans dégrader l'algorithme mathématique.
- **Statut à cet instant** : code poussé, test/build final en cours ; ne pas annoncer livraison APK sans passage GREEN + hachage. Aucun test Samsung effectué.


## Validation finale CI du lot 0.15.45-dev — 12/10/2026

- Workflow `Build GeckoDoku phone APK`, run [38314483379](https://github.com/greenpower2669/GeckoDoku/actions/runs/38314483379) : **GREEN**, `gradle :app:testDebugUnitTest :app:assemblePhone` → `BUILD SUCCESSFUL`.
- Étapes Pierre, banque sprites 240p (source 1 305 fichiers, modèle et manifest dans l'APK) et publication de l'artefact : SUCCESS.
- Artefact Actions : `GeckoDoku-v0.15.45-dev-phone` (ID `11775463777`) contenant `GeckoDoku-v0.15.45-dev.apk` et `GeckoDoku-v0.15.45-dev.apk.sha256` ; APK SHA-256 : `ddff9b30a3267573cbf0a1cecad98fa6c76ca7aee94f670533a2169e1701d586`.
- Cette version est le **build de test phone signé avec la clé debug existante**, PAS une publication Google Play. La signature Google Play n'est pas modifiée.
- PR brouillon #4 : https://github.com/greenpower2669/GeckoDoku/pull/4 ; ne pas merger ni publier avant accord explicite.
- **À faire** : recette physique Samsung par Fab (UI, Pierre, quatre modes, deux profils/homonymes, restauration des données). Les anciens résultats localement mélangés, sans identité d'origine, ne peuvent être reclassés automatiquement.


## 13/10/2026 — première phrase Piper/Sherpa mangée ou déformée

Symptôme Fab : sur la première lecture, le début de certains mots peut être mangé/déformé ; rejouer exactement la même phrase donne généralement un rendu correct.

Audit du code avant correction :
- `PierrePiperSpeechEngine.ensureTts()` était paresseux : le modèle n'était chargé qu'à la première demande réelle ;
- `VoicePcmPlayer` recréait un `AudioTrack.MODE_STATIC` à chaque phrase ;
- le callback de fin reposait sur `samples/sampleRate + 350 ms`, donc sur une estimation temporelle et non sur la consommation audio réelle ;
- la première phrase cumulait donc démarrage modèle + création AudioTrack + démarrage sortie Android.

Correction : préchauffage asynchrone dès construction du moteur, phrase d'amorce synthétisée et réellement lue à volume nul, AudioTrack STREAM persistant, fin déterminée par les frames réellement consommées via `playbackHeadPosition`, puis réutilisation de la même piste pour la phrase réelle. Traces dédiées `WARMUP_*`, `TRACK_CREATE/REUSE`, `PLAYBACK_DRAINED`.

Cause finale du défaut acoustique à confirmer par test Samsung : l'hypothèse de chaîne audio froide est maintenant testée directement, sans déclarer le défaut résolu avant écoute réelle.


Validation compilation : run `38436224654` / #450 GREEN après intégration complète du nouveau `VoicePcmPlayer` et de l'amorçage. Le run #449 rouge correspondait à l'état intermédiaire où le lecteur avait été remplacé avant l'adaptation de `PierrePiperSpeechEngine`; il n'est pas une régression finale.


## 14/10/2026 — outils visuels Sudoku paraissent inactifs

Signalement Fab : en recette téléphone, Entourer / Lignes / Couples / Triplets ne donnent aucun effet visible.

Audit sans code :
- checkboxes correctement branchées ;
- overlay visible et invalidate appelé ;
- cause commune : `drawPlayerReasoningAnnotations` construisait sa preuve uniquement depuis les **notes personnelles** ;
- Lignes exigeait donc deux mêmes notes déjà saisies dans la même ligne ;
- Couples/Triplets exigeaient des ensembles manuels exacts ;
- Entourer interceptait la touche candidat mais refusait le chiffre s'il n'était pas déjà présent dans `snapshot.notesAt(cell)` ;
- tests existants injectaient directement des cartes artificielles de notes et ne couvraient pas l'usage bout-en-bout.

Correction autorisée ensuite par Fab :
- candidats logiques calculés en lecture seule depuis les valeurs confirmées ;
- Lignes/Couples/Triplets branchés dessus ;
- Entourer branché sur la validité logique du candidat ;
- Binaire exact-2 ajouté ligne/colonne/bloc ;
- explication Prévisu + Pierre ;
- compaction du pavé.

Ne pas déclarer la correction visuelle validée avant recette Samsung, même si la CI devient GREEN.


### Validation finale du correctif visuel
Run `38504109994` / #464 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11867790810`, APK SHA-256 `356cb9973995d453d6e82e865b959f4648502624705322502e23d711ec52cb5e`.
Le défaut d'usage n'est pas déclaré résolu tant que Fab n'a pas validé visuellement Lignes/Couples/Triplets/Binaire/Entourer sur Samsung.


## 14/10/2026 — retour Samsung projection/pseudo/Pierre

Retour Fab après APK run #464 :
1. choix du pseudo perçu comme perdu depuis l'introduction des profils ;
2. narrations Sudoku de Pierre jugées « imbuvables » ;
3. « Lignes » ne projette que l'horizontal et manque la colonne / l'axe complet.

Audit :
- le renommage existait encore mais était enfoui dans le dialogue Profils, d'où la régression d'usage ;
- `SudokuHintEngine` produisait une étape parlée par source de conflit, donc trop de phrases ;
- `projectionSegments` ne parcourait que les lignes et l'overlay reliait uniquement les deux points.

Correction :
- pseudo direct + profils séparés ;
- narrations regroupées par ligne/colonne/bloc ;
- projection exact-2 horizontale ou verticale, prolongée sur tout le plateau, discrète et semi-transparente ;
- tests adaptés.

Les runs intermédiaires #465 et #467 peuvent être rouges car ils correspondent à des commits partiels (nouvel enum avant adaptation exhaustive de MainActivity, puis nouvelle règle projection avant mise à jour du test historique). Seul le run final du commit complet fait foi.


### Validation finale du lot projection/pseudo/Pierre
Run `38519610024` / #472 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11873097206`, APK SHA-256 `562218c0d5cc2622e106a582bd5ba8a9bdec667fbec84fc72d0bedb3052ec4b7`.
Les runs intermédiaires rouges correspondaient aux commits partiels décrits plus haut ; #472 est la preuve finale du lot complet.

## 14/10/2026 — ambiguïté outils humains / solutions automatiques

Retour Fab : après le correctif projection/Binaire, les checkboxes sont techniquement efficaces mais elles calculent les candidats et motifs à la place du joueur. C'est intéressant pédagogiquement, mais l'UI les présentait encore comme de simples repères personnels, créant une ambiguïté « j'ai dessiné » vs « le solveur m'a montré ».

Correction :
- retrait de la barre auto du clavier ;
- couche manuelle autonome et purement graphique ;
- couche automatique renommée implicitement Aide surhumaine, protégée par confirmation explicite ;
- point d'interrogation utilisé comme geste en deux temps : `?` puis zone du pavé = explication ; `?` puis plateau = demande d'aide calculée ;
- candidats auto découplés : les autres modes n'affichent plus tous les candidats par défaut ;
- nouvelle grille remet les flags auto OFF.

Les runs rouges #478–#480 correspondent à des commits intermédiaires où MainActivity référençait des fonctions ajoutées dans les commits suivants ; ne pas les considérer comme état final. Run #481 a déjà validé la création complète des deux fenêtres. La preuve finale doit être prise sur le dernier commit fonctionnel.


### Validation finale repères humains / aide surhumaine
Run `38522492488` / #486 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11875030286`, APK SHA-256 `793a3878817fa04f9b2096d3ac032c8f235c98df50f6a7f56df0139dda062d15`.
Le run #486 est la preuve finale du lot complet ; les runs rouges précédents restent des états intermédiaires partiels.

## 14/10/2026 — Hall global vs progression : cause et correction

Signalement Fab : le Hall doit comparer tous les joueurs, mais les progressions ne doivent jamais ranger plusieurs pseudos/joueurs comme une seule personne ; Pierre doit rester attaché au joueur sélectionné.

Audit prouvé :
- `showHallOfFameLevel()` tronquait les résultats avec `.take(10)` ;
- `HallOfFameEntry` ne portait pas d'identité stable, seulement `playerName` + métriques ;
- le protocole global possède déjà un objet `metadata`, mais l'APK n'y publiait pas l'identité locale ;
- `PlayerStatsStore` était déjà correctement séparé par `activeProfileId` ;
- `ProfessorLifeController` était créé une seule fois au lancement : lors d'un switch de profil, `statsStore` changeait mais le contexte vivant (mastery, momentum, streak, impulsivity...) pouvait continuer depuis le joueur précédent ;
- la courbe personnelle avait déjà été retirée du classement global, mais aucune sous-progression distante fiable ne pouvait être créée sans identité.

Correction :
- identité opaque `progressionId` indépendante du pseudo ;
- metadata globale + Hall local enrichis ;
- regroupement de progression uniquement par id ;
- historiques inconnus conservés au classement et exclus des progressions ;
- aucune déduplication entre deux ids connus différents ;
- hydratation locale seulement si preuve exacte non ambiguë ;
- suppression du Top 10 UI et de la limite locale 200 ;
- sous-progressions séparées par joueur ;
- stop/recréation/reset de Pierre lors d'une vraie bascule de profil.

Point restant à valider en recette réseau réelle : le contrat de documentation accepte `metadata`; confirmer qu'un nouveau score publié revient bien avec `metadata.playerProfileId` dans `/sync`. Même si un serveur ancien omet metadata, les propres scores exacts du téléphone restent hydratables localement ; les scores distants d'autres joueurs sans id restent volontairement non attribués.


### Validation finale technique Hall/progression
Run 38526555276 / #510 : GREEN. Commit code validé 29151fc63bd9f8b05d5ba188276f6cfa197e5998. Tests JVM + assemblePhone réussis, Pierre et banque sprites 240p vérifiés. Artefact GeckoDoku-v0.15.45-dev-phone ID 11876601009. APK SHA-256 eec808012b45e21a36a400672b1c50d7b2db41a27df9310db2df30eccef45981.
La CI valide le code et l'APK. La seule inconnue externe restante est la conservation effective de metadata.playerProfileId par le serveur public réel ; le fallback local non ambigu protège les scores propres au téléphone si un ancien serveur omet ce champ.

## 14/10/2026 — Repères humains trop grand et Pierre masqué par PopupWindow

Retour Fab : à chaque ouverture de Repères humains, nécessité de déplacer/recompacter la fenêtre ; la zone titre + outils comporte trop de hauteur. Autre règle : quand Pierre parle, il doit passer momentanément devant puis rendre le premier plan aux outils précédemment visibles.

Audit :
- Repères avait une hauteur fixe 92 dp ; header 36 dp + zone outils 54 dp ;
- position recalculée à chaque création mais jamais persistée ;
- clavier, Repères et Aide sont des `PopupWindow`, donc ils peuvent masquer les vues Pierre de l'Activity même si `bringToFront()` est appelé dans la hiérarchie normale.

Correction :
- hauteur canonique 78 dp ; header 34 dp, outils 44 dp ;
- placement calculé depuis `sudokuBoard` mesuré ;
- géométrie Repères persistée ;
- stratégie de foreground sans destruction : pendant la parole, PopupWindow déplacés hors écran avec clipping temporairement désactivé ; à la fin, restauration exacte.

Risque à valider téléphone : comportement constructeur Android du déplacement offscreen des PopupWindow. La CI ne peut valider que compilation/tests ; la recette Samsung doit confirmer visuellement l'absence de clignotement et le retour exact.


### Validation finale Repères compact / calques Pierre
Run `38537609942` / #522 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11880727507`, APK SHA-256 `3f6dbba028b4bbb793ed16cbf3a9bd3eb237f36963c4e9fd8a292da21fed911c`.
La CI valide code/tests/build ; la recette Samsung doit confirmer que Pierre apparaît réellement devant puis que les trois fenêtres reprennent exactement leur place.

## 14/10/2026 — Clavier/Repères flottants bornés hors zone navigation Samsung

Capture Fab Android 16 : barre de navigation à trois boutons au bas ; pavé Sudoku redimensionnable et fenêtre Repères glissable ne tenaient pas compte des bornes réellement accessibles ; une partie des outils Repères était horizontalement défilée. Cause relue dans `MainActivity.kt` : coordonnées `PopupWindow` calculées avec `screenRoot.height`, sans intersection explicite avec `WindowInsets` de navigation (alors que la grille utilise `protectFromSystemBars`). En outre certaines positions plateau étaient dérivées de coordonnées root-local plutôt que fenêtre.

Correction mission `GECKO-SUDOKU-SYSTEM-BARS-005` : borne commune decor/root/insets, positions `getLocationInWindow`, drag+resize+placement bas-centre re-clampés, suivi des fenêtres déjà ouvertes lors d'un changement d'insets, reprise de Pierre intacte. Tests JVM ajoutés, **validation CI et Samsung non encore effectuées à cette note**. La ligne d'outils Repères reste horizontalement scrollable. Aucun merge ni Release.

## 14/10/2026 — Projection humaine dessine une croix à tort (mission 006)

Capture Fab : sur un 1 marqué dans la grille, la projection manuelle trace ligne et colonne alors que l'observation d'un alignement horizontal n'impose qu'une ligne ; vertical → colonne. La croix n'est légitime que pour une hypothèse posée. Audit : `SudokuValueOverlayView.drawManualPlayerAnnotations()` avait deux `canvas.drawLine` inconditionnels ; MainActivity annonçait explicitement « croix ligne-colonne ».

Correction : politique pure `SudokuManualProjectionPolicy.axesFor` séparant repères d'un même chiffre (1 axe selon voisins, horizontal par défaut) des `hypothesisAt` (2 axes), dessin discret, messages conformes. Six tests spécialisés ajoutés. Aucun changement des notes ni du solveur. CI et recette réelle encore à valider ; ne pas confondre code poussé avec APK testé.

## 14/10/2026 — Intro masquée par Repères + pavé sur lignes du bas (PUSH 007/008)

Deux captures Samsung successives :
1. Une vidéo d'introduction Gecko affichée plein écran est recouverte par le PopupWindow « Repères humains ». Source : `refreshSudokuUi()` postait l'ouverture sans vérifier la phase de l'intro ; le mode Sudoku sauvegardé présentait déjà un plateau sous la vidéo. Fix 007 : nouvelle politique d'admissibilité et vérification au moment de l'ouverture ; relance après intro/saut et quand Pierre cesse de parler.
2. En jeu, Repères est au-dessus du plateau mais le pavé Sudoku bas-centre en masque la dernière rangée. Source : limitation du PopupWindow aux boutons Android uniquement, sans considérer le bas de la grille. Fix 008 : dock sous le plateau mesuré, réduction de la hauteur, déplacement et resize bornés à ce dock, recalage lors du layout.

Tests purs ajoutés. Pas de refonte du gameplay, de Pierre ou du rendu du Sudoku. **La preuve CI et les essais réels sur Samsung restent à confirmer** ; attention à la place physiquement insuffisante en paysage ou très petit écran.

### Preuve CI Samsung 007/008 — run #525
Validation finale après les missions 007 et 008 : run [#525](https://github.com/greenpower2669/GeckoDoku/actions/runs/38587086273), workflow 38587086273 **GREEN**, commit de build `eee5347f52602ccbc91d1359aeacb161c427d9f1`. `testDebugUnitTest + assemblePhone` réussis ; vérification Pierre Sherpa/Piper et banque sprites 240p réussie. Artefact `GeckoDoku-v0.15.45-dev-phone` ID `11905448115` ; APK `GeckoDoku-v0.15.45-dev.apk` SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`, contrôlé après extraction. La recette Samsung demeure nécessaire. Aucun merge main / Release / Play.

## Preuve GitHub — APK direct #525 (14/10/2026)
Livraison APK brut run #525 : prérelease temporaire `temp-geckodoku-0.15.45-dev-run-525` créée et vérifiée par l'API GitHub. [Téléchargement direct .apk](https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-0.15.45-dev-run-525/GeckoDoku-v0.15.45-dev.apk), 299496421 octets, SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`. Workflow de publication #527 GREEN. La première tentative #526 (403 GitHub) a été corrigée en reprenant la méthode qui fonctionnait : checkout de la branche et création du tag sur le commit déclencheur. Aucun nouveau build Android, aucun merge main ou Release stable. Règle permanente de livraison d'APK direct enregistrée dans la section `Contrat FAB Copilot` d'`ordres-de-mission.md`.

## 14/10/2026 — Pourquoi l'ancien dock ne réduisait pas le pavé (mission 009)

Deux captures Fab : 16:45 grande palette chevauchant lignes 8/9 ; 18:40 disposition manuellement corrigée, palette compacte alignée à droite sous la grille. Erreur vérifiée : la palette était physiquement construite `PopupWindow(..., popupHeight)` AVANT le recalage géométrique `bottomCentered(dock)`. Seules les variables `popupWidth` et `popupHeight` étaient ensuite modifiées, pas l'objet PopupWindow ; `showAtLocation` ouvrait donc sa grande hauteur initiale. De plus, la branche palette déjà ouverte quittait la fonction sans vérifier taille réelle ni position. Fix : calculer compactKeypadBelowBoard avant constructeur, comparer taille physique et rect enregistrés au réemploi et après layout/insets, mise à jour réelle. Tests portrait capture, divergence physique, préservation position. Ne pas confondre GREEN CI avec validation visuelle Samsung.

### CI #528 — test de géométrie ajusté (14/10/2026)

Le premier run de vérification `38603247697` / #528 a compilé le Kotlin puis échoué sur **un unique test parmi 299** : `SudokuFloatingWindowGeometryTest.samsungScreenshotPortraitOpensCompactAtRightBelowCompleteGrid` (hauteur attendue 416 px, ligne 255). Cause précise : `compactKeypadBelowBoard` soustrayait deux fois la marge du bas (une fois comme `margin`, une seconde comme `bottomGap`) et ramenait la hauteur à 415 px. Correction du calcul : une marge haute + le plus grand de la marge basse et du bottomGap ; hauteur attendue 416 avec clavier entièrement sous le plateau. Aucun changement de gameplay.

## 14/10/2026 — Signature Google Play et clé réinitialisée : merge protégé (mission 010)

Deux branches historiques : `feature/play-upload-signing-v1` contient le protocole permanent et `build-play-aab.yml` ; `feature/sudoku-visual-reasoning-v1` est la version jouable 0.15.45-dev/code80 validée Samsung (run #529 GREEN) mais ne possédait pas de chaîne AAB dédiée. Notification Play reçue le 12/10 : nouvelle Upload Key active prévue 20:49 UTC le 14/10, SHA1 `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`. Risque : utiliser l'ancien certificat du 07/10 et produire un AAB rejeté. Fix : réintégration du workflow et comparaison SHA1 publique, CI stop avant merge/release en cas de mismatch ; le keystore n'est jamais exposé. Vérifier l'acceptation effective côté Google Play séparément.

## 14/10/2026 — Publication AAB via Secrets après rotation de clé : GREEN

**Clôture validée 14/10/2026 — mission 010.** Feu vert de Fab « merge release avec la secret key ». PR #4 mergée sur `main`, SHA merge `f48dcd7d6898793ccd6d63b22c0bc20644d6966a`. Contrôle pré-merge AAB sur branche : workflow `38636402228` GREEN (certificat Upload Key conforme au Play SHA-1 `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`, tests, Pierre et sprites 240p, `jarsigner -verify`). Build officiel depuis main : workflow `38636963727` GREEN, tests + `bundleRelease`, même contrôle certificat, `jar verified.`. GitHub Release **publique/stable** : https://github.com/greenpower2669/GeckoDoku/releases/tag/geckodoku-v0.15.45-dev-code80 ; AAB signé direct : https://github.com/greenpower2669/GeckoDoku/releases/download/geckodoku-v0.15.45-dev-code80/GeckoDoku-v0.15.45-dev-code80-signed.aab ; SHA-256 `08390c0e502380ee5d3b6e2e77e01d491ff2a9535c61524b9b5270eb92cd755c`. Application `com.greenpower2669.geckodoku`, versionName `0.15.45-dev`, versionCode `80`. L'APK téléphone précédent reste un build à signature test : NE PAS le présenter comme signé Play. Aucun envoi à Play Console réalisé ; l'acceptation de l'AAB par Google n'est pas encore démontrée. Aucune clé privée ni valeur de Secret dans Git.

**Point sécurité :** l'ancienne empreinte Upload Key du 07/10 ne fait pas foi après la réinitialisation. La comparaison impérative avec l'empreinte Play reçue le 12/10 est désormais incluse au workflow, puis exécutée de nouveau après merge depuis main. Ne pas régénérer de keystore ; la Release n'implique pas une publication Play.


## 15/10/2026 — Audit erreur de paradigme Infernal
Ancienne condition (PuzzleGenerator.meetsProfile) : hypothesisCount >= 2 OU hypothesisDepth >= 2. Elle confondait des hypothèses indépendantes avec une imbrication nécessaire. HypothesisSolver utilisait null aussi bien pour branche terminée que pour branche indécise, et oubliait la profondeur de preuves internes lorsqu'une déduction intervenait entre elles. Correction mission 011 : BranchOutcome.Contradiction/Solved/Inconclusive ; propagation de nestedDepthUsed ; depth-2 analysée mais jamais acceptée comme Infernal ; depth-3 exigée ; génération unique par recherche indépendante préservée. Limites non dissimulées : le solveur recherche au plus 3 niveaux ; Pierre connaît la profondeur mais ne récite pas encore tout l'arbre de preuve. Tester performance, graine réellement infernale et restitution sur Samsung.

CI #533 run 38800346305 : BUILD SUCCESSFUL en 1m49, testDebugUnitTest/assemblePhone et vérification asset Pierre/sprites GREEN, APK test SHA-256 ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3. La CI ne démontre pas encore qu'un seed Infernal réel est obtenu en temps raisonnable.


## 15/10/2026 — Extraction ZIP Actions vers APK direct #534
Source #533 (run 38800346305, artifact 12003858392). Le fichier livré par GitHub Actions était un ZIP contenant `GeckoDoku-v0.15.45-dev.apk` + `.sha256`. Le job publication `publish_temp_apk_533` de `.github/workflows/build.yml` effectue `gh run download`, extrait dans `temp/GeckoDoku-infernal-depth3-run533` (runner temporaire), vérifie l'empreinte `ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3` puis publie les deux fichiers bruts comme assets dans la prérelease TEMP `temp-geckodoku-infernal-depth3-run533`. Run 38802231998 / #534 SUCCESS, asset APK taille 299496421 octets et SHA publié. Le dossier runner n'est pas un répertoire persistant du dépôt ; ne pas y versionner un APK ~300 Mo.


## 16/10/2026 — Infernal recherche bloquée : diagnostic et correctif 012
Symptôme : après le passage au contrat 3 imbrications, le générateur CLASSIC affiche des séries indéfiniment et ne trouve pas de grille. Causes : HypothesisSolver.proveContradiction ne testait que binaryChoices(...).firstOrNull(), ne revisitait pas d'autres choix ; aucun mémo des états (confirmés/exclus/profondeur) ; aucun budget total generateExact(); l'échec non distingué d'une impossibilité. Correctif : recherche sur toutes les paires et également branche gécko posé / candidat exclu, répétition évitée par memo, horloge et plafond de nœuds, validation des contradictions et solutions complètes, retour Inconclusive en budget épuisé. Recherche profonde ne peut être certifiée si recherche courte a épuisé son budget. Génération maintenant limitée à 60 s / séries finies, annulation coopérative des calculs et unicité ; erreur explicite UI, aucun faux Infernal. Erreur compilation transitoire de la mission : annotation @Volatile doublée sur classicGenerationToken ; correction ciblée à un seul @Volatile. Ne pas confondre compilation GREEN et preuve d'existence d'une grille de triple imbrication. Pas de modification des autres modes ni de la chaîne AAB.

## CI finale mission 012 — run #539 GREEN (16/10/2026)
Après l'échec #536-538 pour double @Volatile (annotation initiale déjà présente), correction commit ce6d855. Run 38859558302 / #539 SUCCESS, BUILD SUCCESSFUL in 2m 16s ; tests JVM et assemblePhone exécutés, vérification empaquetage Pierre/Piper et sprites 240p succès. APK phone artifact ID 12028672971, SHA-256 ba4acee944de16aa34951cd7f22a3ed3a8423e1e71e58e3b75a866086549fbd5. Les tests de non-régression annulation passent ; absence de seed triple prouvé dans le rapport, donc ne pas annoncer réussite fonctionnelle exhaustive.


## 16/10/2026 — Refus d'une génération Infernal synchrone trop lente
Cause : recherche synchrone de grille à trois hypothèses imbriquées et X-Wing, trop rare et trop coûteuse. Contrat modifié par Fab : atelier séparé, collecte de grilles avec au moins 1 X-Wing observé, 2 décisions imbriquées dépendantes, unicité exacte. Générateur spécialisé `generateWorkshopCandidate` part de solutions complètes légales, retire les givens en conservant countSolutions==1, évalue hypothèses et comparaison du modèle sans X-Wing. Ce dernier test est borné : « X-Wing requis dans le modèle essayé » n'est PAS preuve mathématique universelle ; une validation humaine reste indispensable. `WorkManager` a été introduit pour lots répétés sans blocage UI (périodique selon OS, chargeur et batterie suffisante) avec interrupteur persistant. Données et exports atomiques, anti-doublon par hash, import sous quota remettant le statut à CANDIDATE. H2/H3 distincts. Aperçu exclu de `recordRatedCompletionIfNeeded` : aucune écriture statistiques/Hall/publisher. Validation manuelle possible seulement après aperçu/test, sélection Infernal peut puiser dans grilles validées. Premier build CI #541 a échoué car gradle.properties désactivait AndroidX ; changer uniquement android.useAndroidX=true, pas d'autre refonte.

Optimisation de la collecte : l'ancien filtre rejetait tout puzzle si la recherche alternative sans X-Wing expirait, recréant un goulot d'étranglement malgré l'atelier. Nouveau filtrage : profondeur 2 testée avant 3, exclusion du puzzle si le modèle sans X-Wing le résout ; si le modèle est indécis faute de budget, garder le puzzle CANDIDATE avec marqueurs d'incertitude. UI expose l'état de la recherche sans X-Wing. Un X-Wing observé dans une trace n'atteste pas sa nécessité absolue ; l'export conserve le drapeau. Aucune candidature n'est validée automatiquement.

## 16/10/2026 — Validation compilation Atelier (#545)
Après activation AndroidX nécessaire à WorkManager (CI #541 échouée sur config uniquement), #543 et #544 GREEN, puis optimisation de collecte avec flags d'indécision et run #545 (38894030798) **GREEN**, Gradle BUILD SUCCESSFUL (2m26s), tests + assemblePhone + assets Pierre/sprites, commit fcc114e4. Artefact téléphone 12043056881, SHA-256 APK `1e49ba40e09e316d6857a12df33bda533cb5fa9c2c3aa0e4fd9892f9f0162e78`. Tests actuels n'apportent PAS la preuve qu'une grille H2/X-Wing a déjà été réellement produite ; mesurer rendement in situ.

## 16/10/2026 — Atelier 014 : monodimensionnel → toutes tailles
Le WorkManager de la mission 013 stockait et exploitait seulement la taille courante `size`, ce qui créait des années de recherche mono-taille si laissé seul. Correction 014 : curseur persistant réclamé AVANT chaque tentative, rotation équitable 5..12 indépendante du résultat, stockage compteurs `attempts_v2_N`, verrous pour éviter périodique + manuel en parallèle. Le 5×5 était filtré injustement si sa trace n'avait pas de X-Wing ; seuil de X-Wing levé pour 5–7, maintenu pour 8–12. Le solveur d'atelier pouvait atteindre H3, limite étendue à H4 avec budgets bornés (recherche coûteuse sur grosses grilles). Les comparaisons de preuves restent d'une portée bornée ; ne jamais conclure que H4 ou X-Wing sont nécessaires lorsqu'une recherche plus simple a expiré. Couleurs Classic et règles un Gecko par ligne/colonne/région inchangées. Tests CI et Samsung indispensables.

**CI finale atelier 014 (16/10/2026)** : run GitHub Actions #550 `38921744094` GREEN, commit code `b56fce177805e103028c09bdfd6d9772ad7e8bc0`, Gradle `BUILD SUCCESSFUL in 2m 4s`, tests JVM, APK phone, Pierre/banque 240p vérifiés. Artefact Actions `GeckoDoku-v0.15.45-dev-phone` ID `12055281956`, APK SHA-256 `d879a10ea9ece31dcb866fadb83ea438826f52439c9a0881baf4664d569b966d`. Les #547–#549 avaient échoué pendant l'intégration : #547 ancien appel `start(context,size)`, puis #548/#549 ancien test exigeant un X-Wing sur 5×5 ; API et test corrigés avant #550. Pas de merge main, de Release ni de Google Play. Essais réels sur Samsung / démonstration de grilles H2–H4 restent à effectuer.

## 16/10/2026 — 0 tentative / absence de preuve d'exécution du générateur (mission 015)
Le journal utilisateur Android 16 entre 17:47 et 17:49 ne comporte que des événements d'interface/médias (Pierre/Sherpa, ChromaKey, SpriteFactory, FPS) ; aucune trace explicite du moteur Infernal. Ce journal ne suffit pas à inférer si WorkManager a tenté quelque chose : le moteur 014 utilisait Logcat et comptait les tentatives uniquement à l'entrée de doWork, avec planification périodique 15 min et contraintes batterie/chargeur. « 0 partout » indique qu'aucune tentative n'a été persistée. Solution : lancement de FGS explicite depuis l'écran, enregistrement en entrée avant la recherche, collecte des événements dans le journal in-app MediaTrace, UI 1,5s live et statut d'interruption détecté grâce au heartbeat. La notification peut arrêter immédiatement et désactive le flag enabled. Recherche sans pause fixe programmée entre tâches, mais batterie économie/thermique sévère suspendent le calcul pour la sécurité. Attention FGS specialUse : exigences Android 14–16 et revue Play ; l'OS peut fermer un FGS. CI compile mais ne vérifie pas l'activation réelle sur le Samsung.

### CI atelier 015 — 16/10/2026
Run téléphone #557 `38929818514` GREEN sur commit `ad65fab01d05d0ec3751246d8d8446d6a64fabe7`. `BUILD SUCCESSFUL in 2m 33s`, tests JVM, `assemblePhone`, contrôles de Pierre et sprites 240p OK. Artefact Actions `GeckoDoku-v0.15.45-dev-phone` id `12058666334` ; SHA-256 APK `da540f8e19007d7d4fe932df13d30c00a09160958560adbe5f15b6d26dede8af`. Les anciennes CI #552, #554, #555 étaient aussi vertes. APK brut non publié : seul l'artefact ZIP Actions existe ; ne pas inventer un lien direct .apk ni créer de prérelease sans nouvelle demande. Aucun test Android 16 réel effectué. Aucun merge main, aucune Release/Play. Un foreground service est susceptible d'être interrompu par Android ; `specialUse` nécessite examen Play avant diffusion publique.

## GECKO-CLASSIC-WORKSHOP-DIALOG-016 — menu masqué sur Samsung (16/10/2026)
L'utilisateur fournit une capture à 18h34 : popup « Atelier Infernal • 0 grilles » avec « RECHERCHE ARRÊTÉE », « MOTEUR : INACTIF », « LANCÉES : 0 », « Dernier signal : jamais », affichage uniquement d'un texte de statut et bouton FERMER — aucune ligne « ▶ DÉMARRER ». Le journal média montre des événements audio/vidéo (Pierre/sprites), aucun SEARCH_START. **Cause racine identifiée dans MainActivity.kt** : les AlertDialog.Builder de l'atelier appelaient à la fois `.setMessage(...)` et `.setItems(...)`; Android privilégie le message et n'affiche pas la liste d'actions. Le service continu n'était donc jamais lancé par ce menu ; ce n'est pas une preuve de génération infructueuse. Correction dans la branche existante `feature/classic-workshop-continuous-fgs-v3` : fenêtre principale commandes setItems SEULEMENT, en tête DÉMARRER/ARRÊTER puis COMPTEURS EN DIRECT ; écran compteurs dédié avec message mis à jour toutes les 1,5 s et boutons Commandes/ARRÊTER ; fenêtre Bilan 8 tailles = items seuls ; fenêtre Détails candidat = items visibles (Tester, notes, Valider, Rejeter, Remettre) et bouton Détails séparé pour les preuves. Tests de régression source `InfernalWorkshopDialogVisibilityTest.kt` (quatre vérifications empêchant message+items dans un même menu). Zéro autre changement au moteur ; conserver données, préf et validations. Vérification restante : test visuel + compteur augmente après appui sur DÉMARRER sur Samsung ; notification et SEARCH_START/SEARCH_END dans Journal vidéo. Aucun merge main, aucune Release/Play sans ordre.

### Livraison correction popup masquée — 16/10/2026
CI #559 GREEN pour correctif des fenêtres, CI #560 run `38933645991` GREEN incluant quatre tests `InfernalWorkshopDialogVisibilityTest`. Artefact phone #560 ID `12059284259`, APK `GeckoDoku-v0.15.45-dev.apk` SHA-256 `e04f89f783c187cae19eb015f6fc5c870cd6401ff2b3b4fd5cb58520abbfbfb1`. À la demande implicite de poursuivre les essais avec des APK GitHub directs (dernière livraison #557), prérelease **temporaire** #561 run `38934166078` GREEN, tag `temp-geckodoku-workshop-dialog-fix-run560` ; asset exact APK brut vérifié (300771417 octets) : https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-workshop-dialog-fix-run560/GeckoDoku-v0.15.45-dev.apk . Ce nouveau fichier remplace le test #557 qui affichait uniquement FERMER. Aucune fusion main ni Release stable ni Play. Tester sur Samsung : appuyer « ▶ DÉMARRER / REPRENDRE », la notification doit apparaître et les compteurs LANCÉES/TERMINÉES monter même sans grille. Si l'état reste ARRÊTÉ après appui, contrôler dernière erreur et SEARCH_START dans Journal vidéo. L'installation seule ne démarre pas la recherche.

## GECKO-CLASSIC-WORKSHOP-NO-XWING-017 — expérience sans exigence de X-Wing (16/10/2026)
L'utilisateur, après une capture prouvant moteur continu actif (1194 essais, 6×6 en cours), confirme que tous les essais sont terminés sans candidat et ordonne « retire [le X-Wing] de mes équations, on va tester comme ça ». Expérience A/B sur branche `feature/classic-workshop-no-xwing-v4` fondée sur 016 : le X-Wing n'est plus un critère de qualification pour AUCUNE taille (5×5→12×12), et l'analyse supplémentaire `HypothesisSolver.analyze(... rules = SolverRules.NO_X_WING)` est totalement retirée du générateur atelier. **Conserver la technique X-Wing dans HumanSolver**, sans l'exiger ni empêcher qu'elle figure naturellement dans une démonstration. Conserver solution unique vérifiée séparément, logique H2–H4 par contradictions réellement imbriquées, anti-classement trompeur, bibliothèque et ZIP, exploration continue en rotation et validation humaine. `InfernalWorkshopPolicy.qualifies(size, depth, xWingCount)` ne vérifie désormais que taille, profondeur 2..4, métadonnée xWingCount ≥0. `InfernalWorkshopStore` utilise cette policy pour créations, imports et promotions (compatibilité JSON anciens conservée). Statistiques UI indiquent « SANS exigence X-Wing ». Nouveau compteur expérimental `attempts_no_xwing_v4` global et `attempts_no_xwing_v4_N` par taille, `saved_no_xwing_v4` pour candidates générées et enregistrées ; les anciens 1194+ essais restent comptabilisés dans les totaux historiques. L'installation ne doit pas effacer les candidats ni les préférences du moteur. Faire un test comparatif avec les nouveaux compteurs : 100+ recherches terminées par taille sans grille reste un signal d'algorithme trop restrictif, PAS une justification pour déclarer H2/H3 prouvé.

### Résultat et APK expérimental sans X-Wing (16/10/2026)
Après échec du premier CI #562 (1 test d'un Puzzle artificiel 12×12 mal construit avec tableaux 5×5, **pas** un bogue générateur), le test fut corrigé. CI #563 run `38957467342` GREEN, 323 tests JVM/compilation téléphone/ressources OK, commit code `811da1b90303764862db671f669544855ea60771`. APK phone brut SHA-256 `1b42e0575c6f18d9294ed85853dd16e4915157cd47f0c1d7c89c3a3a18715bb3`, artifact GitHub Actions `12072180081`. Livraison vérifiée CI #564 `38957908216` GREEN, prérelease TEMP `temp-geckodoku-workshop-no-xwing-run563` contenant l'asset APK brut 300771417 octets et fichier .sha256 ; lien direct : https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-workshop-no-xwing-run563/GeckoDoku-v0.15.45-dev.apk . GitHub API confirme asset réel. Aucun merge main, aucune Release stable, aucun Play. Après installation Samsung, appuyer DÉMARRER/REPRENDRE, relever le compteur **NOUVEAUX ESSAIS** et les candidats du test A/B, sans prendre les compteurs historiques comme résultat expérimental.

## GECKO-CLASSIC-WORKSHOP-FORWARD-XWING-018 — expérimentation additive (16/10/2026)
Après le test sans X-Wing (v4, zéro candidat) Fab propose d'inverser le sens de la construction : « On va poser les g. Code pas ok? », puis donne l'ordre nouveau « On tente et tu remet le xwing non? ». Ordre interprété comme autorisation de développer **sur une nouvelle branche** `feature/classic-workshop-forward-geckos-xwing-v5` (basée sur v4, main inchangée). Le moteur d'atelier conserve une solution complète SECRÈTE légale pour construire les régions colorées, mais construit désormais les **indices visibles en partant de zéro et en POSANT un Gecko à la fois**, exclusivement dans les cases de la solution secrète, sans retirer d'indice. `WorkshopForwardPath.stages(solution, random)` produit 0,1,...,N indices et explore quatre ordres aléatoires par géométrie, jusqu'à 12 géométries par invocation sous la limite/annulation de 20 s du service FGS. À la **première frontière de solution unique** indépendamment prouvée par `countSolutions`, analyse H2→H3→H4, rejette si H1 suffit, exige X-Wing observé pour 8×8..12×12 (facultatif 5..7), rejette si solveur SANS X-Wing prouve la grille sur tailles 8+, sinon conserve une incertitude quand le budget comparatif s'épuise. Le solveur habituel et ses techniques ne changent pas. En cas d'échec, essayer un autre ordre/geometrie, pas de retrait des G. Pas de garantie que cette méthode produise des grilles H2/X-Wing (cela reste expérimental). Données anciennes/v4 toujours lisibles : `InfernalWorkshopStore.fromJson` accepte une archive v4 de structure valide même si la règle X-Wing v5 ne permettrait pas sa promotion. Promotion Infernal reste soumise à validation explicite + profil en vigueur. Journal/compteurs A/B de v4 préservés ; nouveau compteur `attempts_forward_v5`, `saved_forward_v5`, `unique_forward_v5` (frontières uniques rencontrées, pas distinctes) et compteurs par taille, sans effacer les historiques. UI atelier affiche « TEST POSE PROGRESSIVE DES GECKO », compteurs V5, frontières uniques et règle X-Wing par taille. Nouveau test Kotlin `WorkshopForwardPathTest` (étapes 0→N monotones, déterminisme par seed, frontière réelle d'unicité, règle X-Wing) ; anciens tests de contrat mis à jour. CI Android/phone et test Samsung à effectuer. NE PAS merger main ni publier de release/AAB/Play sans nouvelle permission.

## CI finale mission 018 — vérifiée le 16/10/2026
GitHub Actions workflow `Build GeckoDoku phone APK` run **#568** id `38969577659`, commit source `2ba3dc5daf4021376003b8376a0834b3725e1453` **GREEN** : Gradle `BUILD SUCCESSFUL in 2m 36s`, tests JVM, APK Android phone, vérification Pierre Piper + banque sprites 240p réussies. Artefact GitHub Actions `GeckoDoku-v0.15.45-dev-phone` id `12076173884`, contenant `GeckoDoku-v0.15.45-dev.apk` SHA-256 `4f65aa420229e041fdf533f6e867713cd7ae155624623cc934bf854ae875763d`. Ci intermédiaire #565-#567 a échoué à cause du type `List<Cell>` vs `Set<Cell>` dans `WorkshopForwardPath.stages` ; correctif accepté avec `Collection<Cell>`, tests comparaison `toSet()`, #568 vert. Étapes workflow « Publish phone prerelease » et « Promote validated phone release » **skipped**. **Pas de lien direct .apk publié** ni main fusionné, Release/Play en attente d'autorisation. Tests Samsung de la génération réelle et du nombre de frontières uniques encore ouverts.

## Livraison APK direct mission 018 — 17/10/2026
À la demande « Ok je l attends », publication de l'APK téléphone de test construit en CI #568 (source `2ba3dc5daf4021376003b8376a0834b3725e1453`). GitHub Actions #569 run `38977217042` GREEN, job `publish_temp_apk_568` SUCCESS ; SHA256 validé `4f65aa420229e041fdf533f6e867713cd7ae155624623cc934bf854ae875763d`, APK brut vérifié comme asset GitHub Release temporaire `temp-geckodoku-workshop-forward-run568`, taille 300771417 octets : https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-workshop-forward-run568/GeckoDoku-v0.15.45-dev.apk (à ne pas confondre avec ZIP Actions). Fichier .sha256 dans la même prerelease. Le nouveau workflow GitHub ne recompile pas le code sur le commit de publication `[temp-apk-run568]` et ne touche pas `main`. Aucune Release stable, Play ou merge ; validation de la recherche par pose réelle sur Samsung encore attendue.

## 17/10/2026 — cause des recherches Infernal infructueuses et correction v6
Constat sur `feature/classic-workshop-forward-geckos-xwing-v5` : `InfernalWorkshopService.kt` annulait chaque essai après 20 s ; `HypothesisSolver.kt` imposait aussi 850 ms / 1,5 s / 2,5 s et quotas 2 500/4 000/8 000 nœuds aux preuves ; seuls les compteurs étaient durables, pas la grille/progression ni les sous-preuves. Une recherche inachevée pouvait être rejetée à tort et perdre ses résultats. Nouvelle branche `feature/classic-workshop-resumable-deletion-v6` : retrait restauré, points de sauvegarde de curseur et mémo des sous-branches prouvées, différenciation annulation/inconclusion, heartbeat sans cutoff. Risque résiduel : la pile DFS active est reconstruite au redémarrage, puis les sous-arbres déjà mis en cache sont évités ; tester les limites de mémoire et de stockage sur tailles 8–12. Tests Android/JVM et Samsung requis ; pas de merge ni Release.

## 17/10/2026 — Séparation découverte X-Wing / certification Infernal v7
Avant v7, toute grille unique traversait une démonstration H2–H4 sur place, même lorsqu'un X-Wing prometteur pouvait être conservé. Risque : travail précieux perdu entre deux expériences, coûts de preuve bloquants et absence de banque de pistes. V7 détecte X-Wing observable, archive et déduplique les grilles avec figure éliminatrice, puis les examine ultérieurement. Les fiches indécises survivent aux pauses et erreurs. Risques encore en audit : X-Wing dévoilé *seulement après une hypothèse*, non visible au détecteur léger ; exploration et certification alternées dans un seul thread, donc une preuve interminable peut retarder le producteur ; essais thermiques/longue durée Samsung non faits.

## 17/10/2026 — zéro candidat malgré 3 770 X-Wing détectés en v7
Capture Samsung : recherche active, 3 770 X-Wing détectés, 3 659 en attente, 111 explorés, 0 prometteur, 0 candidat stocké ; les compteurs ne prouvent pas qu'aucune grille ne peut être trouvée. Contrat original exigeait H2–H4 imbriquées et X-Wing à partir de 8 ; assouplir seulement la profondeur dans le solveur sans changer `InfernalWorkshopPolicy` et `InfernalWorkshopStore` n'aurait pas produit de candidats acceptés. Expérience v8 distincte : H1 + X-Wing versus H2 sous règles NO_X_WING, preuves conservées par grille et mode dans l'archive X-Wing, aucun seuil de temps artificiel. Critère scientifique : H2 exige profondeur réellement utilisée 2 et échec exhaustif de H1 sans X-Wing. Garder les résultats de laboratoire séparés et visibles, sans fausse homologation.
