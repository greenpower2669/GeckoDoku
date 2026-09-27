# GeckoDoku — todo.md
## ÉTAT APRÈS v0.10.11-dev — 26/09/2026

### Validé / acquis
- [x] Moteur de jeu principal actif.
- [x] Génération à solution unique + validation logique.
- [x] HumanSolver actif.
- [x] Projection de zone active.
- [x] Gecko X-Wing actif.
- [x] Hypothèses Mission Impossible / Infernal actives.
- [x] 8 difficultés actives.
- [x] Prof pédagogique actif.
- [x] Journal / Sauver / Rejouer actifs.
- [x] Stats locales actives.
- [x] Bulle Prof overlay sans reflow.
- [x] Pierre + priorités de parole.
- [x] Encouragements enregistrés retirés ; Pierre + moteur contextuel 309.
- [x] Célébration.
- [x] Icône launcher validée téléphone.
- [x] Médaillon titre validé téléphone.
- [x] Intro 1 + son validés téléphone.
- [x] Intro 2 + enchaînement validés téléphone.
- [x] Multi-sessions média.
- [x] Retry ProfParle après erreur transitoire.
- [x] CI #97 GREEN complet v0.10.11-dev.

### Vigilances, pas missions actives
- [ ] Surveiller toute régression de transparence/chroma sur d'autres appareils.
- [ ] Surveiller l'état Anim ON/OFF via logs si le symptôme réapparaît.
- [ ] Surveiller que ProfParle reste synchronisé avec la parole réelle.
- [ ] Surveiller la coexistence Prof/Gecko sous forte concurrence média.

### Mission
Aucun ordre de mission actif actuellement.
`ordres-de-mission.md` doit rester vide jusqu'au prochain ordre explicite de Fab.


<!-- GECKO-034-FIRST-FRAME-GATE-2026-09-26 -->
## GECKO-034 — supprimer le flash noir de première frame
- [ ] RED : une nouvelle session vidéo démarre cachée.
- [ ] RED : `onPrepared` ne rend pas la vidéo visible.
- [ ] RED : appel de `start()` ne rend pas la vidéo visible.
- [ ] RED : première frame réellement rendue → vidéo visible.
- [ ] RED : erreur avant première frame → session fermée sans affichage noir.
- [ ] RED : complete avant première frame → jamais visible.
- [ ] RED : état de visibilité indépendant pour chaque session.
- [ ] Brancher le callback `MEDIA_INFO_VIDEO_RENDERING_START` ou équivalent fiable.
- [ ] Garder `ChromaKeyVideoView` / Surface caché tant que la première frame n'est pas rendue.
- [ ] Ajouter logs `VIDEO_VISIBILITY_ARMED`, `VIDEO_FIRST_FRAME`, `VIDEO_VISIBLE`, `VIDEO_ABORT_BEFORE_FIRST_FRAME`.
- [ ] Ne pas introduire de temporisation arbitraire.
- [ ] Ne pas toucher aux intros validées.
- [ ] Ne pas modifier ZOrderOnTop / alpha / shader chroma hors nécessité démontrée.
- [ ] Ne pas modifier la coexistence multi-sessions.
- [ ] Build APK/AAB après GREEN.
- [ ] Validation téléphone : aucun flash noir au démarrage Prof.
- [ ] Validation téléphone : aucun flash noir au démarrage Gecko.


<!-- GECKO-034-GRID-GEOMETRY-GUARD-2026-09-26 -->
## GECKO-034 — garde-fou géométrie grille
- [ ] RED : rectangle de la grille identique avant création de la session vidéo.
- [ ] RED : rectangle identique pendant vidéo cachée en attente de première frame.
- [ ] RED : rectangle identique lors du passage alpha 0 → 1.
- [ ] RED : rectangle identique après fin/erreur vidéo.
- [ ] Ne jamais utiliser `GONE` si cela peut provoquer un reflow.
- [ ] Préférer `alpha=0f` / couche overlay pour cacher la surface sans modifier le layout.
- [ ] Vérifier aucun changement de weight/marges/padding/taille du parent plateau.
- [ ] Vérifier petits/grands écrans et orientation/ratios différents.
- [ ] Validation téléphone : aucune grille qui flotte, saute, se redimensionne ou se décale pendant le lancement d'une animation.


<!-- GECKO-034-RED-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## GECKO-034 — RED posé
- [x] Ajouter RED gate première frame.
- [x] Ajouter RED sessions indépendantes.
- [x] Ajouter RED géométrie figée dans une même fenêtre.
- [x] Ajouter RED réancrage uniquement sur changement de fenêtre.
- [ ] Vérifier que CI échoue pour classes de policy absentes.
- [ ] Implémenter gate dans ChromaKeyVideoView.
- [ ] Révéler mask/portrait seulement à la première frame.
- [ ] Sortir la grille réelle du LinearLayout pondéré vers une couche flottante ancrée.
- [ ] Conserver un slot de réservation sans déplacer la grille réelle.


<!-- GECKO-034-GREEN-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## GECKO-034 — GREEN implémenté
- [x] FirstFrameVisibilityGate.
- [x] Révélation après frame OpenGL fraîche.
- [x] Abort sans flash avant première frame.
- [x] Mask Gecko synchronisé.
- [x] Portrait Prof conservé jusqu'à première frame.
- [x] Intro non modifiée par le gate.
- [x] BoardGeometryPolicy.
- [x] Board réelle flottante + anchor de réservation.
- [x] Rectangle figé dans une même fenêtre.
- [ ] CI GREEN.
- [ ] APK/AAB.
- [ ] Validation téléphone flash noir.
- [ ] Validation téléphone géométrie grille.


<!-- GECKO-034-V01012-CANDIDATE-2026-09-26 -->
## Candidate v0.10.12-dev
- [x] #103 RED attendu.
- [x] #104 GREEN complet.
- [x] versionCode 23.
- [x] versionName 0.10.12-dev.
- [ ] CI finale versionnée GREEN.
- [ ] Test téléphone : aucun flash noir.
- [ ] Test téléphone : grille immobile malgré textes/overlays.


<!-- GECKO-035-RED-2026-09-26 -->
## GECKO-035
- [x] Poser RED priorité Pierre / panne vidéo.
- [x] Poser RED toutes SpeechOrigin + encouragement enregistré.
- [x] Poser RED bouton ! / 100 phrases.
- [x] Poser RED ⚙️ Son / Anim / Journal.
- [x] Poser RED journal persistant borné / clear.
- [ ] Vérifier RED CI.
- [ ] Implémenter pré-roll ProfParle caché avant Pierre avec fallback.
- [ ] Ajouter timeout de sécurité sans couper la voix.
- [ ] Animer encouragement enregistré.
- [ ] Ajouter bouton !.
- [ ] Ajouter ⚙️ et déplacer Son/Anim.
- [ ] Ajouter journal persistant consultable/vidable.
- [ ] Synchroniser les 5 fichiers au GREEN.
- [ ] CI GREEN + APK/AAB.


<!-- GECKO-035-GREEN-POLICIES-2026-09-26 -->
## GECKO-035 étape 1
- [x] ProfessorSpeechLaunchPolicy.
- [x] ProfessorSpeechVisualPolicy.
- [x] SettingsMenuPolicy.
- [x] ProfessorQuickTalkPolicy.
- [ ] PersistentMediaLog / MediaTrace.
- [ ] Câblage voix/vidéo/UI.


<!-- GECKO-035-GREEN-LOG-2026-09-26 -->
## GECKO-035 étape 2
- [x] PersistentMediaLog.
- [x] Rotation 256 KiB.
- [x] MediaTrace Logcat + fichier.
- [ ] Installer MediaTrace au démarrage UI.
- [ ] Consultation/copie/clear via ⚙️.


<!-- GECKO-035-GREEN-SPEECH-GATE-2026-09-26 -->
## GECKO-035 étape 3
- [x] QUICK_TALK non préemptif.
- [x] ProfessorSpeech.canAccept.
- [ ] Première frame tenue / release synchronisée.


<!-- GECKO-035-GREEN-FIRST-FRAME-HOLD-2026-09-26 -->
## GECKO-035 étape 4
- [x] holdOnFirstFrame.
- [x] revealHeldFirstFrame.
- [x] AssetAudioPlayer.onStarted.
- [ ] Câbler MainActivity et UI.


<!-- GECKO-035-GREEN-RUNTIME-2026-09-26 -->
## GECKO-035 étape 5
- [x] MediaTrace.install au boot.
- [x] Wrapper commun des paroles Pierre.
- [x] Pré-roll + timeout + fallback PNG.
- [x] ! / QUICK_TALK.
- [x] ⚙️ avec Son/Anim/Journal.
- [x] Son/Anim retirés de row2.
- [x] Consultation/copie/vidage journal.
- [x] Encouragement enregistré animé + cleanup erreur.
- [ ] CI GREEN runtime.
- [ ] Bump v0.10.13-dev.
- [ ] Test téléphone.


<!-- GECKO-035-V01013-CANDIDATE-2026-09-26 -->
## Candidate v0.10.13-dev
- [x] #108 RED attendu.
- [x] #114 GREEN runtime.
- [x] versionCode 24 / versionName 0.10.13-dev.
- [ ] CI finale versionnée GREEN.
- [ ] Test téléphone : synchro Pierre/ProfParle.
- [ ] Test téléphone : panne vidéo n'empêche jamais Pierre.
- [ ] Test téléphone : ! / ⚙️ / journal.
- [ ] Test téléphone : grille toujours immuable.


<!-- GECKO-035-CI115-GREEN-2026-09-26 -->
## GECKO-035 après #115
- [x] CI finale versionnée GREEN.
- [x] APK/AAB produits.
- [ ] Fab : tester synchro Pierre / ProfParle.
- [ ] Fab : tester ! avec plusieurs phrases.
- [ ] Fab : tester ⚙️ Son / Animations / Journal.
- [ ] Fab : tester consultation / copie / vidage du journal.
- [ ] Fab : confirmer que Pierre continue si ProfParle échoue.
- [ ] Fab : confirmer grille immuable.


<!-- GECKO-036-PNG-CONTINUITY-MISSION-2026-09-27 -->
## GECKO-036 — continuité PNG → ProfParle
- [ ] RED : PNG visible au début du pré-roll.
- [ ] RED : PNG visible pendant prepare.
- [ ] RED : PNG visible après première frame tenue.
- [ ] RED : reveal réussi → masquer PNG seulement après succès.
- [ ] RED : reveal échoué → PNG reste visible.
- [ ] RED : timeout → PNG reste visible.
- [ ] RED : erreur vidéo avant reveal → PNG reste visible.
- [ ] RED : erreur vidéo après reveal → retour PNG immédiat.
- [ ] RED : rectangle de grille inchangé pendant toute la transition.
- [ ] GREEN minimal sans toucher à la priorité Pierre.
- [ ] CI GREEN + APK/AAB.
- [ ] Validation téléphone : aucun trou visuel.


<!-- GECKO-036-QUICK-TALK-BUBBLE-2026-09-27 -->
## GECKO-036 — bouton ! / bulle
- [ ] RED : phrase QUICK_TALK affichée dans ProfessorBubbleView.
- [ ] RED : phrase complète non affichée dans status.
- [ ] RED : une seule requête de parole par appui sur !.
- [ ] RED : origin conservée QUICK_TALK.
- [ ] Séparer helper bulle visuel et helper bulle + parole si nécessaire.
- [ ] Conserver anti-répétition des 100 phrases.
- [ ] Conserver fermeture bulle sans arrêt voix.
- [ ] Conserver géométrie de grille immuable.
- [ ] Validation téléphone : phrase des 100 visible dans la bulle.


<!-- GECKO-036-RED-2026-09-27 -->
## GECKO-036 — RED
- [x] RED continuité Prof.png.
- [x] RED restauration PNG après erreur.
- [x] RED frame résiduelle / génération playback.
- [x] RED ! → bulle + une seule QUICK_TALK.
- [ ] Vérifier RED CI.
- [ ] GREEN policies.
- [ ] Câbler MainActivity.
- [ ] Câbler ChromaKeyVideoView avec génération fraîche.
- [ ] CI GREEN.
- [ ] Candidate téléphone.


<!-- GECKO-036-GREEN-POLICIES-2026-09-27 -->
## GECKO-036 étape 1
- [x] ProfessorPortraitContinuityPolicy.
- [x] FreshPlaybackFrameGate.
- [x] QuickTalkPresentationPolicy.
- [ ] Câbler ChromaKeyVideoView.
- [ ] Câbler MainActivity.


<!-- GECKO-036-GREEN-RUNTIME-2026-09-27 -->
## GECKO-036 étape 2
- [x] PNG visible avant stop/remplacement vidéo.
- [x] PNG conservé pendant prepare/hold.
- [x] PNG masqué seulement après reveal/fresh frame réussi.
- [x] FreshPlaybackFrameGate câblé.
- [x] Renderer armé après START + VIDEO_RENDERING_START.
- [x] ! → bulle visuelle-only + une QUICK_TALK.
- [ ] Vérifier CI GREEN.
- [ ] Bump v0.10.14-dev après GREEN.
- [ ] Validation téléphone : zéro trou, zéro -38, bulle QUICK_TALK.


<!-- GECKO-036-V01014-CANDIDATE-2026-09-27 -->
## Candidate v0.10.14-dev
- [x] #120 RED attendu.
- [x] #121 GREEN policies.
- [x] #122 GREEN runtime.
- [x] versionCode 25 / versionName 0.10.14-dev.
- [ ] CI finale versionnée GREEN.
- [ ] Fab : zéro trou visuel pendant pré-roll.
- [ ] Fab : vérifier disparition du MediaPlayer -38.
- [ ] Fab : phrases ! dans bulle, une seule voix.
- [ ] Fab : grille toujours immuable.


<!-- GECKO-036-CI123-GREEN-2026-09-27 -->
## GECKO-036 après #123
- [x] CI finale versionnée GREEN.
- [x] APK/AAB produits.
- [ ] Fab : confirmer Prof.png visible pendant toute l'attente.
- [ ] Fab : confirmer absence d'erreur MediaPlayer -38 lors des répétitions/remplacements.
- [ ] Fab : confirmer les 100 phrases dans la bulle.
- [ ] Fab : confirmer une seule voix QUICK_TALK par appui.
- [ ] Fab : confirmer grille immuable.


<!-- GECKO-036-FRAME-SERIAL-RED-2026-09-27 -->
## GECKO-036 — serial de frame
- [x] RED stale serial consommé mais non validant.
- [x] RED serial suivant validant.
- [x] RED ancienne génération rejetée.
- [x] RED intro hors gate.
- [ ] Vérifier RED CI.
- [ ] Remplacer bool frameAvailable par compteur produit/consommé sûr.
- [ ] Ne jamais clear une frame à l'armement.
- [ ] Ne pas armer INTRO.
- [ ] CI GREEN.
- [ ] v0.10.15-dev.
- [ ] Test téléphone Intro + Prof répété.


<!-- GECKO-036-FRAME-SERIAL-GREEN-2026-09-27 -->
## GECKO-036 — GREEN frame serial
- [x] #125 RED attendu.
- [x] FreshFrameSerialGate.
- [x] FirstFrameGateActivationPolicy.
- [x] Compteur produced/consumed serial.
- [x] Aucune purge booléenne de frame.
- [x] Stale frame consommée mais non validante.
- [x] Intro hors gate.
- [ ] CI GREEN.
- [ ] v0.10.15-dev.
- [ ] Fab : Intro 1 fluide.
- [ ] Fab : ProfParle sans timeouts répétés.


<!-- GECKO-036-V01015-CANDIDATE-2026-09-27 -->
## Candidate v0.10.15-dev
- [x] #125 RED attendu.
- [x] #126 GREEN complet.
- [x] versionCode 26 / versionName 0.10.15-dev.
- [ ] CI finale versionnée GREEN.
- [ ] Fab : Intro 1 fluide et non figée.
- [ ] Fab : Prof_actions fluide.
- [ ] Fab : ProfParle obtient VIDEO_FIRST_FRAME sans timeout répété.
- [ ] Fab : Pierre reste prioritaire.
- [ ] Fab : bulle QUICK_TALK toujours correcte.


<!-- GECKO-036-CI127-GREEN-2026-09-27 -->
## GECKO-036 après #127
- [x] CI candidate v0.10.15-dev GREEN.
- [x] APK/AAB produits.
- [ ] Fab : vérifier Intro 1 non figée.
- [ ] Fab : vérifier Prof_actions non figé.
- [ ] Fab : vérifier ProfParle sans timeout répété.
- [ ] Fab : vérifier aucune régression PNG/bulle/grille.


<!-- GECKO-036-FINAL-VALIDATION-2026-09-27 -->
# ÉTAT FINAL APRÈS GECKO-036

## Validé téléphone par Fab
- [x] Intro 1 non figée.
- [x] Prof_actions non figé.
- [x] ProfParle fonctionne sans la régression de timeout répétitif observée.
- [x] Prof.png reste présent pendant les attentes/fallbacks.
- [x] Pierre reste prioritaire et continue même si la vidéo échoue.
- [x] Les 100 phrases du bouton ! apparaissent dans la bulle Prof.
- [x] Une seule parole QUICK_TALK par appui.
- [x] Grille flottante/immuable préservée.
- [x] Menu ⚙️ et journal média conservés.
- [x] CI #128 GREEN sur le HEAD final.
- [x] v0.10.15-dev / versionCode 26 devient la base de référence.

## Mission active
Aucune mission active.
`ordres-de-mission.md` doit rester vide jusqu'au prochain ordre explicite de Fab.


<!-- GECKO-037-LIVING-PROF-MISSION-2026-09-27 -->
# GECKO-037 — PROF GECKO VIVANT

## Étape 0 — audit/TDD
- [ ] Vérifier branche/HEAD avant code.
- [ ] Auditer `PierreSmallTalk.kt`, sélecteur actuel, événements joueur, difficulté et bulle.
- [ ] Poser RED corpus 309 / IDs uniques / textes uniques.
- [ ] Poser RED clock 48 h et persistence.
- [ ] Poser RED lastPhraseId.
- [ ] Poser RED fallback oldest-first.
- [ ] Poser RED random injecté.
- [ ] Poser RED context/mood/décroissance.
- [ ] Poser RED difficulté / anti-moquerie injuste.
- [ ] Poser RED rare ≤5%.
- [ ] Poser RED bulle +1s après vraie fin Pierre.
- [ ] Poser RED token/generation anti-fermeture obsolète.
- [ ] Poser RED bulles pédagogiques non auto-fermées.

## Architecture
- [ ] ProfessorPhrase.
- [ ] PhraseCategory.
- [ ] PhraseRarity.
- [ ] ProfessorPhraseCatalog.
- [ ] ProfessorPhraseHistory.
- [ ] ProfessorPhraseSelector.
- [ ] ProfessorPlayerContext.
- [ ] ProfessorMood.
- [ ] ProfessorMoodPolicy.
- [ ] Clock injectable.
- [ ] Random injectable.
- [ ] Persistent store léger.

## Corpus
- [ ] Migrer 100 historiques avec IDs legacy stables.
- [ ] Ajouter 200 V2 exactes.
- [ ] Ajouter 4 FAB.
- [ ] Ajouter 5 TAQUIN.
- [ ] Total 309.
- [ ] Diagnostic proximité texte.

## Runtime
- [ ] Brancher GameEvent haut niveau.
- [ ] Calculer mastery.
- [ ] Calculer impulsivity.
- [ ] Calculer momentum.
- [ ] Pondérer mood/catégories.
- [ ] Cooldown 48 h.
- [ ] lastPhraseId.
- [ ] Fallback neighbor/GENERAL/oldest-first.
- [ ] Logs PROF_CONTEXT / POOL / SELECTED / FALLBACK.
- [ ] Bulle simple = texte exact de Pierre.
- [ ] Auto-close 1 s après callback réel.
- [ ] Annulation timer obsolète.
- [ ] Aucune fermeture auto pédagogie.
- [ ] Aucune préemption small talk.
- [ ] Préserver ProfParle/Pierre/grille/vidéo validés.

## Validation
- [ ] CI GREEN.
- [ ] APK/AAB.
- [ ] Test téléphone Fab.


<!-- GECKO-037-REMOVE-RECORDED-ENCOURAGEMENTS-2026-09-27 -->
## GECKO-037 — retirer encouragements enregistrés
- [x] Audit chemin RECORDED / PIERRE.
- [x] RED : encouragement delivery = PIERRE uniquement.
- [ ] Vérifier RED CI.
- [ ] Supprimer EncourgementSource.RECORDED / policy alternée.
- [ ] Supprimer EncouragementSelector des segments MP3.
- [ ] Supprimer EncouragementSegment + catalogue des segments.
- [ ] Supprimer lecture playVoiceSegment liée aux encouragements.
- [ ] Supprimer asset Voix_encouragements.mp3.
- [ ] Conserver PierreEncouragements provisoirement jusqu’au moteur 309.
- [ ] Préserver onFinished / célébration.
- [ ] CI GREEN.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-GREEN-2026-09-27 -->
## GECKO-037 — encouragements enregistrés : état
- [x] #131 RED attendu.
- [x] Delivery PIERRE uniquement.
- [x] Source RECORDED supprimée.
- [x] EncouragementSelector enregistré supprimé.
- [x] Catalogue/segments MP3 supprimés.
- [x] playVoiceSegment enregistré retiré du chemin encouragement.
- [x] Voix_encouragements.mp3 supprimé.
- [x] Anciens tests RECORDED supprimés/actualisés.
- [x] onFinished / célébration préservés.
- [ ] CI GREEN du retrait.
- [ ] Plus tard : remplacer PierreEncouragements provisoire par selector 309 contextuel.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-CI132-2026-09-27 -->
## GECKO-037 — encouragements enregistrés après #132
- [x] CI #132 GREEN complet.
- [x] APK/AAB produits.
- [x] Ancien MP3 d'encouragement hors architecture.
- [ ] Étape suivante GECKO-037 : moteur 309 phrases + contexte/mood/history.


<!-- GECKO-037-FULL-RED-2026-09-27 -->
## GECKO-037 — RED global
- [x] Audit branche/points d'accroche.
- [x] RED corpus 309/IDs/dédoublonnage.
- [x] RED proximité textuelle.
- [x] RED cooldown 48 h.
- [x] RED persistence + lastPhraseId.
- [x] RED répétition immédiate.
- [x] RED oldest-first.
- [x] RED mood/context/difficulté.
- [x] RED rareté.
- [x] RED auto-close + token obsolète + pédagogie.
- [ ] Vérifier CI RED ciblée.
- [ ] GREEN modèles/catalogue.
- [ ] GREEN history/selector.
- [ ] GREEN context/mood.
- [ ] GREEN bubble policy.
- [ ] Brancher runtime.


<!-- GECKO-037-GREEN-MODEL-MOOD-BUBBLE-2026-09-27 -->
## GECKO-037 GREEN 1
- [x] Modèles Phrase/Category/Rarity/Mood.
- [x] Clock/Random injectables.
- [x] Context tracker + difficulté + décroissance.
- [x] Mood policy / anti-moquerie réfléchie.
- [x] RARE cible 4 %.
- [x] Bubble close policy token +1s + pédagogie.
- [ ] Catalogue 309.
- [ ] History/selector.
- [ ] Runtime.


<!-- GECKO-037-GREEN-CATALOG-309-2026-09-27 -->
## GECKO-037 GREEN 2
- [x] Catalogue 309 exact.
- [x] 100 legacy conservées avec IDs stables.
- [x] 209 nouvelles exactes.
- [x] Déduplication normalisée.
- [x] Diagnostic similarité Jaccard.
- [x] PierreSmallTalk compatibilité 100.
- [ ] History/selector.
- [ ] Runtime.


<!-- GECKO-037-GREEN-HISTORY-SELECTOR-2026-09-27 -->
## GECKO-037 GREEN 3
- [x] Cooldown 48 h exact.
- [x] Storage SharedPreferences.
- [x] lastPhraseId persistant.
- [x] Selector pondéré.
- [x] Fallback voisin/GENERAL.
- [x] Oldest-first sans clear.
- [x] Anti-répétition immédiate.
- [ ] Vérifier RED global devenu GREEN côté cœur.
- [ ] Brancher runtime MainActivity.


<!-- GECKO-037-RUNTIME-GREEN-2026-09-27 -->
## GECKO-037 GREEN 4
- [x] Controller runtime + logs.
- [x] GAME_STARTED / CORRECT / WRONG / RAPID_WRONG / LONG_THINKING / HINT / LEVEL_COMPLETED / RETURN / AMBIENT.
- [x] ! via 309.
- [x] Smalltalk ambiant via 309.
- [x] Encouragements via 309.
- [x] Réactions erreur contextuelles via 309.
- [x] Bulle = texte Pierre.
- [x] +1s après vraie completion.
- [x] Timer obsolète protégé.
- [x] Pédagogie non auto-close.
- [ ] CI GREEN runtime.
- [ ] Nettoyage anciens sélecteurs devenus inutilisés.
- [ ] Version candidate + APK/AAB.


<!-- GECKO-037-RUNTIME-CLEANUP-2026-09-27 -->
## GECKO-037 GREEN 5
- [x] Aucun ancien selector smalltalk par index dans MainActivity.
- [x] Runtime petites phrases = moteur 309 uniquement.
- [ ] Versionner candidate.
- [ ] CI finale + APK/AAB.


<!-- GECKO-037-V01016-CANDIDATE-2026-09-27 -->
## Candidate v0.10.16-dev
- [x] #134 RED global attendu.
- [x] #137 cœur GREEN complet.
- [x] #138 runtime GREEN complet.
- [x] versionCode 27 / versionName 0.10.16-dev.
- [ ] CI finale versionnée GREEN.
- [ ] APK/AAB candidate.
- [ ] Fab : valider comportement vivant sur téléphone.


<!-- GECKO-037-CI140-GREEN-2026-09-27 -->
# GECKO-037 — ÉTAT APRÈS CI #140

## Critères techniques implémentés
- [x] 100 phrases historiques conservées.
- [x] 200 V2.
- [x] 4 FAB.
- [x] 5 TAQUIN.
- [x] Total 309.
- [x] IDs stables.
- [x] Aucun doublon exact après normalisation.
- [x] Diagnostic de proximité textuelle.
- [x] Cooldown individuel 48 h.
- [x] Cooldown persistant SharedPreferences.
- [x] lastPhraseId persistant.
- [x] Aucune répétition immédiate.
- [x] Fallback voisin / GENERAL.
- [x] Fallback forcé oldest-first sans clear.
- [x] RARE limitée à 4 %.
- [x] FAB semi-rare.
- [x] Contexte joueur fonctionnel.
- [x] mastery.
- [x] impulsivity.
- [x] momentum.
- [x] mood adaptatif.
- [x] décroissance vers NEUTRAL.
- [x] difficulté prise en compte.
- [x] erreur réfléchie/difficile protégée contre TAQUIN immédiat.
- [x] erreurs rapides répétées peuvent mener à TAQUIN/PEDAGOGICAL.
- [x] réussite/streak difficile peut mener à IMPRESSED.
- [x] domination répétée de niveaux faciles peut permettre TAQUIN.
- [x] ! via moteur 309.
- [x] smalltalk ambiant via moteur 309.
- [x] encouragements via moteur 309.
- [x] réactions d'erreur via moteur 309.
- [x] bulle simple = texte exact prononcé.
- [x] fermeture 1 s après vraie completion Pierre.
- [x] timer obsolète protégé par token.
- [x] pédagogie non auto-fermée.
- [x] smalltalk non préemptif.
- [x] ProfParle/pipeline vidéo non refondu.
- [x] tests unitaires GREEN.
- [x] CI #140 GREEN complet.
- [x] APK/AAB produits.
- [x] cinq fichiers FAB Copilot synchronisés.

## Validation téléphone restante
- [ ] Fab : naturel des réactions/contextes.
- [ ] Fab : humeur perçue comme variée et bienveillante.
- [ ] Fab : aucune moquerie injuste après erreur réfléchie/difficile.
- [ ] Fab : bulle simple ferme naturellement ~1 s après fin réelle.
- [ ] Fab : pédagogie reste lisible/ouverte.
- [ ] Fab : aucune régression Intro / ProfParle / grille.
- [ ] Fab : confirmer GECKO-037 comme validé.

Mission active jusqu'à cette validation.


<!-- GECKO-037-TECHNICAL-CLOSE-2026-09-27 -->
# ÉTAT CANONIQUE APRÈS GECKO-037

## Acquis techniques
- [x] Catalogue 309.
- [x] IDs stables.
- [x] Cooldown persistant 48 h.
- [x] lastPhraseId persistant.
- [x] Anti-répétition immédiate.
- [x] Fallback oldest-first.
- [x] RARE contrôlée.
- [x] Contexte mastery / impulsivity / momentum.
- [x] Mood adaptatif + retour au neutre.
- [x] Difficulté prise en compte.
- [x] Anti-moquerie injuste.
- [x] ! / ambient / encouragement / erreur via moteur 309.
- [x] Bulle = texte prononcé.
- [x] Auto-close +1 s après vraie fin Pierre.
- [x] Protection timer obsolète.
- [x] Pédagogie non auto-fermée.
- [x] Ancien encouragement MP3 supprimé.
- [x] ProfParle / grille / intro préservés.
- [x] CI #140 GREEN + APK/AAB.
- [x] CI #141 GREEN documentation.

## Vigilance téléphone, pas mission active
- [ ] Observer le naturel des réactions/contextes.
- [ ] Observer la variété/bienveillance du mood.
- [ ] Vérifier qu’une erreur réfléchie/difficile ne provoque pas de pique injuste.
- [ ] Vérifier fermeture naturelle ~1 s des petites bulles.
- [ ] Vérifier que la pédagogie reste ouverte.
- [ ] Vérifier absence de régression Intro / ProfParle / grille.

## Mission active
Aucune mission active.
`ordres-de-mission.md` reste vide jusqu’au prochain ordre explicite de Fab.

<!-- GECKO-038-DESIGN-SUDOKU-2026-09-27 -->
# GECKO-038 — SECOND MODE SUDOKU

## Statut
- [x] Intention fonctionnelle documentée.
- [x] Architecture GameMode / VisualStyle documentée.
- [x] Sélecteur 3 états documenté.
- [x] Stratégie on-top anti-régression documentée.
- [x] Assets NB / couleur vérifiés sur main.
- [x] Workflow futur consigné dans ordres-de-mission.md.
- [ ] **AUCUN CODE avant GO explicite de Fab.**

## À faire seulement après GO
- [ ] Geler SHA de base + comportement téléphone de référence.
- [ ] Intégrer les deux PNG canoniques depuis main sans conversion.
- [ ] Audit des zones de MainActivity / GeckoBoardView sensibles au mode.
- [ ] RED tests de non-régression GeckoDoku avant abstraction.
- [ ] Introduire GameMode sans changer le comportement historique.
- [ ] Router les événements stables par mode, sans rebinding global.
- [ ] Construire SudokuEngine pur 9×9.
- [ ] Ajouter modèle case Sudoku / notes / givens / undo-redo.
- [ ] Ajouter contrôles tactile + clavier partageant les mêmes actions métier.
- [ ] Ajouter renderer Sudoku indépendant.
- [ ] Ajouter couche visuelle click-through.
- [ ] Ajouter VisualStyle Classic / Gecko NB / Gecko Coloré.
- [ ] Ajouter sélecteur magique 3 états avec preview live et commit au relâchement.
- [ ] Vérifier qu'une case vide Sudoku n'hérite pas du gecko historique.
- [ ] Adapter les événements du moteur Prof vivant au Sudoku.
- [ ] Concevoir ensuite seulement la pédagogie Sudoku humaine.
- [ ] CI complète + APK/AAB.
- [ ] Validation téléphone Fab : GeckoDoku historique inchangé.
- [ ] Validation téléphone Fab : tactile Sudoku.
- [ ] Validation téléphone Fab : styles et sélecteur.



<!-- GECKO-038-RED-START-2026-09-27 -->
## GECKO-038 — exécution
- [x] GO explicite Fab.
- [x] Branche isolée créée.
- [x] RED moteur / génération / hint / styles posé.
- [ ] Constater RED CI.
- [ ] GREEN cœur Sudoku.
- [ ] Intégrer assets canoniques sans conversion.
- [ ] UI Sudoku + routeur de mode.
- [ ] Sélecteur 3 états live.
- [ ] Prof Sudoku.
- [ ] CI finale APK/AAB.
- [ ] Validation téléphone Fab.


<!-- GECKO-038-CORE-GREEN-2026-09-27 -->
- [x] CI #145 RED attendu confirmé.
- [x] GameMode / VisualStyle séparés.
- [x] SudokuEngine pur.
- [x] Génération 9×9 avec unicité vérifiée.
- [x] Notes + undo/redo.
- [x] Hints single nu / caché.
- [ ] Confirmer CI cœur GREEN.
- [ ] UI / assets / sélecteur.


<!-- GECKO-038-CORE-COMPILE-FIX-2026-09-27 -->
- [x] Diagnostiquer CI #146 : mismatch Set notes.
- [x] Corriger le contrat de collection.
- [ ] Reconfirmer GREEN cœur.


<!-- GECKO-038-UI-SURFACE-2026-09-27 -->
- [x] CI #147 cœur GREEN.
- [x] Surface Sudoku séparée.
- [x] Overlay valeur click-through.
- [x] Sélecteur 3 états DOWN/MOVE/UP.
- [x] PNG NB/couleur copiés bit-identiques depuis main.
- [ ] Câbler MainActivity et mode switch.
- [ ] CI surface UI.


<!-- GECKO-038-ASSET-CATALOG-2026-09-27 -->
- [x] Références AssetMediaCatalog des planches Sudoku.
