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
