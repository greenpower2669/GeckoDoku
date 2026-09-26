# Brain — GeckoDoku

## Contrat produit
Jeu Android de logique accessible, hors ligne et sans publicité. Toute difficulté et tout conseil doivent pouvoir être expliqués par une preuve logique.

## Règles
Un gecko exactement par ligne, colonne et zone. Aucun contact horizontal, vertical ou diagonal. Solution unique compte tenu des givens. Pas de guessing dans le solveur humain actuel.

## Gestes
Simple : croix. Vrai double-clic : gecko. Long : hypothèse. Givens verrouillés.

## Tailles et rendu
5 à 12. N zones pour NxN. 12 teintes pastel et frontières fortes.

## Solveur
Techniques :
- ROW_SINGLE ;
- COLUMN_SINGLE ;
- REGION_SINGLE ;
- REGION_LOCKED ;
- REGION_TOUCH_PROJECTION ;
- GECKO_X_WING.

## Trace pédagogique v0.4
SolveStep contient :
- technique ;
- cell forcée ou eliminated ;
- sourceCells ;
- sourceRegions ;
- axis ;
- beforeConfirmed ;
- beforeExcluded.

HumanSolver.analyze() produit une trace ordonnée complète. Puzzle.solverTrace conserve cette trace calculée une fois au moment où le générateur accepte la grille.

Cette trace devient une ressource commune :
- calcul de difficulté ;
- Professeur Gecko ;
- debug ;
- futur dataset IA.

## Professeur Gecko
ProfessorGecko.nextHint() reçoit le snapshot actuel.
Il tente d'abord de réutiliser une étape de solverTrace dont les préconditions sont satisfaites et dont l'action n'est pas déjà faite.
Si aucune étape pré-calculée ne correspond, HumanSolver.nextStep() recalcule uniquement la prochaine déduction depuis confirmed + manualCrosses + autoCrosses.

Le Prof fonctionne désormais en pas-à-pas actif :
- chaque pression demande une seule prochaine déduction ;
- une étape déterministe est expliquée puis appliquée au GameEngine ;
- croix et geckos posés par le Prof deviennent de vrais éléments de partie ;
- après chaque étape, le Prof attend explicitement une nouvelle pression ;
- toute action manuelle du joueur efface l'overlay pédagogique courant.

Pour une bifurcation à deux candidats, le Prof ne confirme rien immédiatement :
- il affiche d'abord les deux candidats comme geckos semi-transparents dans un overlay distinct ;
- une seconde pression résout la preuve de contradiction ;
- le candidat rejeté devient une croix et le survivant devient un gecko confirmé.

La bulle BD accompagne chaque étape. Les sources restent encadrées en ambre et l'action appliquée est encadrée en rouge.

## Difficulté
Mesurée par solveurs d'ablation. Expert nécessite réellement X-Wing. Démentiel nécessite X-Wing + projection.

## IA future
L'IA pourra prédire le niveau et sélectionner des explications, mais la trace logique reste la vérité terrain.

## Stats/média
Stats SharedPreferences locales. Audio léger. Médias riches futurs séparés du moteur.


## Barres système Android
MainActivity doit toujours respecter les WindowInsets des systemBars. Le padding de base d'accessibilité est additionné aux insets haut/bas/gauche/droite afin qu'aucun contrôle, notamment Prof Gecko, ne soit masqué par la barre de navigation ou la barre d'état.

## Anomalie difficulté à auditer
Un test réel 12×12 étiqueté Facile a nécessité deux Gecko X-Wing selon le joueur. Cette observation invalide le classement si les X-Wing étaient effectivement indispensables. Ne pas considérer le libellé Facile comme fiable pour ce cas avant audit. La correction doit comparer la trace réellement nécessaire et les alternatives logiques, puis garantir qu'un niveau inférieur ne dépend d'aucun X-Wing.


## Journal de grilles v0.5
PuzzleJournalStore est séparé du moteur. Il utilise SharedPreferences + JSON et conserve uniquement la définition nécessaire :
- id ;
- size ;
- difficulty ;
- seed ;
- regions ;
- solutionCols ;
- givens ;
- savedAt.

La progression du joueur n'est pas sauvegardée dans le journal. Recharger signifie refaire la grille proprement. HumanSolver reconstruit solverTrace au chargement, ce qui garde les entrées légères et indépendantes des évolutions internes de la trace.

Opérations : save sans doublon d'id, list, load, delete, clear, contains.

## Rejouer
Rejouer ne génère rien : GameEngine est recréé sur le même Puzzle. Hypothèses, croix, geckos joueurs, temps, état de fin et Prof courant sont réinitialisés. Cela compte comme une nouvelle tentative statistique.

## Statistiques par difficulté
PlayerStatsStore conserve déjà started_diff_* et completed_diff_*. v0.5 expose DifficultyStats(started, completed, completionRate). L'écran Stats affiche pour chaque difficulté pourcentage et fraction completed/started. Un niveau sans tentative affiche « — » plutôt qu'un faux 0 %.


## Hypothèses logiques v0.6
HypothesisSolver intervient uniquement lorsque HumanSolver FULL ne fournit plus de déduction.
Il construit des BinaryChoice uniquement sur :
- une ligne non résolue avec exactement 2 candidats ;
- une colonne non résolue avec exactement 2 candidats ;
- une zone non résolue avec exactement 2 candidats.

Pour chaque candidat, il simule les conséquences déterministes. Une branche est rejetée seulement si elle mène à une contradiction : conflit entre geckos, ligne sans candidat, colonne sans candidat ou zone sans candidat.

maxDepth=1 + maxHypothesisSteps=1 définit Mission Impossible.
maxDepth=2 + maxHypothesisSteps=2 définit Infernal.
Une étape transporte hypothesisRejected et hypothesisDepth afin que le Prof explique la preuve.

DifficultyIndexer essaie d'abord HumanSolver FULL. Seulement s'il bloque, il tente Mission puis Infernal. Une grille qui échoue encore à profondeur 2 n'est pas logicallySolvable dans le contrat actuel.

## Professeur en bulle BD
ProfessorBubbleView est une couche UI distincte. Elle affiche un fond clair, contour vert, petite queue de bulle, en-tête « Prof Gecko » et texte lisible. MainActivity affiche dans cette bulle focusText, explanationText puis actionText. Toute action joueur réinitialise et masque la bulle.

## Célébration
VictoryCelebrationView est un overlay procédural sans asset : fond translucide, confettis et particules radiales. Le nombre de salves et particules dépend de l'ordinal de GameDifficulty. Pas de stroboscope ; l'alpha des particules décroît progressivement. Durée environ 2,2 s + 0,32 s par niveau, toucher pour fermer.


## Application des étapes du Prof v0.7
GameEngine.applyProfessorStep(SolveStep) applique les eliminations, hypothesisRejected et cell. Les geckos posés déclenchent ensuite computeAutoCrosses comme un coup joueur normal.

La sélection du coup reste entièrement issue de HumanSolver/HypothesisSolver. Avant de confirmer un gecko, GameEngine utilise puzzle.isSolution uniquement comme garde-fou de cohérence. Si la conclusion diverge de la solution unique, aucune mutation n'est appliquée et l'UI signale l'incohérence.

GeckoBoardView.professorGhosts affiche les deux possibilités à alpha 0,24 sans modifier GameSnapshot.hypotheses.

## Stats assistance
MainActivity garde professorUsed pour toute la tentative. PlayerStatsStore.recordComplete(..., usedProfessor) incrémente completed_with_prof et completed_with_prof_diff_<difficulty>. Le taux de réussite principal reste completed/started ; l'aide est un attribut de la réussite, pas une erreur.


## Bulle Prof overlay v0.8
L'activité utilise désormais un FrameLayout screenRoot contenant :
1. le LinearLayout principal du jeu ;
2. ProfessorBubbleView en overlay ;
3. VictoryCelebrationView au premier plan lorsqu'il est actif.

ProfessorBubbleView ne participe donc plus à la mesure verticale du plateau. À l'ouverture, controlsPanel (les trois lignes de boutons secondaires) passe en GONE ; le bouton Prof reste visible. La bulle est mesurée puis positionnée relativement au haut réel de professorButton.

ProfessorBubbleView dessine sa propre cible × de fermeture et expose onClose. closeProfessorBubble() ne réinitialise pas pendingProfessorHypothesis : fermer le dialogue visuel n'annule pas le raisonnement en cours.

## Audio célébration v0.8
VictoryCelebrationView émet onFireworkBurst(level, burstIndex, isLast) exactement lorsque chaque salve devient active. ToneFxFeedback.celebrationBurst() crée une séquence courte lancement/explosion avec ToneGenerator. Les sons sont donc alignés sur l'animation sans fichier audio externe.

ToneFxFeedback possède un Handler dédié aux sons différés de célébration. stopCelebration() supprime ces callbacks. enabled=false et release() appellent également stopCelebration().


## Contrat média riche GECKO-022
L'habillage riche est strictement décoratif et superposé. Il ne remplace jamais le rendu procédural ni le moteur logique.

Switch local indépendant : Habillage animé ON/OFF.
FX contrôle le volume des bandes son des vidéos, pas leur activation.

Catalogue de vérité :
- assets/gecko/Gecko_Intro.mp4 = intro uniquement ;
- assets/gecko/Gecko_apparition.mp4 = apparition d'un gecko confirmé uniquement ;
- assets/gecko/Gecko_disparition.mp4 = disparition d'un gecko retiré uniquement ;
- assets/gecko/Gecko_actions_plusieurs.mp4 = séquence longue mignonne occasionnelle entière ;
- assets/prof/Prof.png = portrait normal Prof ;
- assets/prof/Prof_fb.png = source fond bleu pour futur keycolor ;
- assets/prof/Prof_actions.mp4 = séquence longue Prof occasionnelle entière.

Les deux vidéos longues (~30,07 s) ne sont PAS découpées dans la phase actuelle : lecture 0→EOF, une fois, sans boucle. Le futur découpage en micro-actions est explicitement différé.

Le futur runtime devra utiliser une couche overlay dédiée avec fallback systématique vers le rendu normal. Une défaillance média ne peut jamais modifier ou invalider l'état de partie.


## Keycolor bleu immédiat — amendement GECKO-022
Décision Fab du 26/09/2026 : le fond bleu des médias riches doit être rendu transparent dès ce lot. Le lecteur vidéo applique un chroma-key GPU OpenGL ES avant composition : dominance bleue → alpha, bord adouci et réduction de frange bleue. Les seuils vivent dans AssetMediaCatalog et restent réglables. Une erreur shader/décodage déclenche un fallback vers le jeu procédural ; le média ne peut jamais modifier GameEngine.

Les médias racine `assets/` sont empaquetés par le sourceSet Android sans duplication binaire. Habillage animé est persistant, désactivable à chaud et indépendant de FX ; FX coupe uniquement l'audio des vidéos. `Prof.png` reste le portrait normal déjà transparent ; `Prof_fb.png` reste une source bleue disponible sans remplacer automatiquement le portrait normal.


## Voix d'encouragement — mission GECKO-023
Le master `assets/audio/encouragements/master/Voix_encouragements.mp3` contient 13 phrases courtes. La mission GECKO-023 prévoit de le conserver intact et de générer 13 clips séparés sous `assets/audio/encouragements/clips/`. Une voix est déclenchée uniquement après confirmation correcte d'un **nouveau gecko par le joueur**, jamais pour une action Prof, une erreur, une croix ou un retrait. Le même gecko ne doit pas être félicité deux fois dans une tentative après retrait/repose.

Le choix est aléatoire sans répétition immédiate. « Tu y es presque » est conditionné à 1–2 geckos restants. Les voix suivent FX ON/OFF mais restent indépendantes du switch Habillage animé. Le dernier gecko reçoit lui aussi un encouragement ; un futur son de victoire est séquencé après la courte phrase pour éviter la cacophonie. L'audio reste décoratif et ne peut jamais influencer GameEngine, stats ou difficulté.

Les timecodes canoniques de découpe sont stockés dans `ordres-de-mission.md`. Statut : **à implémenter**.


## Organisation audio physique
Les fichiers audio autonomes sont classés sous `assets/audio/` :
- `intro/` pour le morceau d'introduction futur ;
- `celebration/` pour le morceau de célébration futur ;
- `encouragements/master/` pour le master vocal intact ;
- `encouragements/clips/` réservé aux 13 dérivés GECKO-023.

Ce rangement n'active aucun comportement à lui seul. Les vidéos MP4 conservent leur audio embarqué et restent sous `assets/gecko/` et `assets/prof/`.


## GECKO-024 — rendu fixe et audio gameplay
Le retour téléphone v0.9 a montré une inversion verticale des vidéos. Le buffer UV pré-appliquait un retournement alors que SurfaceTexture fournit déjà sa matrice : les UV sont désormais canoniques et la matrice SurfaceTexture reste la transformation de vérité.

La grille est immuable pendant les médias. RichMediaOverlayView porte un masque indépendant ; les animations de case utilisent exactement le rectangle réel de GeckoBoardView, avec cache blanc sous la vidéo et aucun impact sur la mesure du plateau. Les actions longues Gecko ne partent plus après une croix et restent attachées à la case du Gecko confirmé.

ProfessorBubbleView utilise Prof.png statiquement. Prof_actions.mp4 est localisé sur le portrait, avec audio vidéo muet pour laisser la voix TTS française du Prof prioritaire.

AssetAudioPlayer sépare musique et voix. Nouvelle → musique intro ; victoire → musique celebration. GECKO-023 utilise dans ce build les timecodes canoniques du master par seek Android 26+ ; le découpage physique en 13 clips reste une étape de durcissement ouverte.


### Preuve CI v0.10
GitHub Actions run #28 (`36209229581`) sur `f7fd2a703913e636cddaab4f7fed4951643146de` : tests unitaires + APK + AAB réussis. Artefact `GeckoDoku-v0.10.0-dev-Android` id `10894193799`. La validation visuelle/audio sur téléphone reste requise.


## GECKO-025 — sprite canonique et Prof systématique
Fab confirme que `assets/gecko/Gecko_tr.png` était déjà le sprite transparent officiel. `AssetMediaCatalog.GECKO_PORTRAIT` pointe exclusivement vers ce fichier ; le petit `Gecko.png` ajouté pendant GECKO-024 est supprimé pour éliminer toute ambiguïté.

L'animation du Prof n'utilise plus `RichMediaScheduler`. À chaque intervention Prof éligible, `ProfessorAnimationPolicy` autorise immédiatement `Prof_actions.mp4` dans le rectangle du portrait quand les animations sont ON, que le média n'est pas déjà occupé, qu'aucune hypothèse Prof n'attend sa seconde étape et qu'aucune célébration n'est visible. Texte et TTS restent immédiats et prioritaires.


### Preuve CI GECKO-025
GitHub Actions run #33 (`36210582662`) sur `8aaa06f95dd626a654a95a4b284280b43705a47c` : tests unitaires, APK et AAB réussis. Version produite : `0.10.1-dev`. Validation visuelle téléphone encore nécessaire pour confirmer le rendu réel de Gecko_tr et la fréquence d'animation Prof.


## GECKO-026 — Prof flottant sans reflow
Le bug de cadrage n'était pas causé par le fait que `ProfessorBubbleView` soit dans `screenRoot` : elle était déjà en overlay. La cause était `controlsPanel.visibility = GONE` lors de l'ouverture. Comme `GeckoBoardView` occupe la hauteur restante avec `weight=1f`, Android recalculait sa hauteur et donc le rectangle de la grille.

GECKO-026 interdit désormais tout retrait du layout lors d'une bulle Prof : les contrôles restent visibles, la bulle flotte au-dessus, et la grille conserve ses dimensions. `ProfessorBubbleView` ne contient plus aucun PNG ni animation Prof : seulement titre, texte, croix et TTS externe.

`Prof.png` vit dans un `FrameLayout` de bouton à hauteur fixe (58 dp), avec 10 dp de débordement visuel autorisé vers le haut. Une micro-animation de scale/lift est appliquée au PNG à chaque interaction Prof via propriétés visuelles uniquement ; aucune mesure/layout n'est modifiée. `Prof_actions.mp4` n'est plus lancé depuis la bulle. La précédente `ProfessorAnimationPolicy` GECKO-025 est retirée du runtime.


### Preuve CI GECKO-026
GitHub Actions run #37 (`36212085221`) sur `f555102094cda6a21ed7812dae247748329ea62d` : tests unitaires + APK + AAB réussis. Artefact : `GeckoDoku-v0.10.2-dev-Android` id `10896251661`. La validation téléphone doit encore confirmer visuellement que la grille ne change plus de cadre à l'ouverture/fermeture de la bulle.


## GECKO-027 — Prof réellement au premier plan
Le simple ordre d'ajout dans le FrameLayout ne suffit pas avec un `Button` Android, car son StateListAnimator peut modifier son Z lors des états pressés. Le host Prof neutralise désormais le Z du bouton (`stateListAnimator=null`, élévation 0 dp) et impose au PNG une élévation dédiée de 18 dp.

Après insertion du portrait, puis avant chaque micro-animation, `bringToFront()` est appelé. L'animation continue d'utiliser uniquement scale/translationY ; l'élévation du portrait est réaffirmée avant animation. Aucun paramètre de layout n'est modifié.


### Preuve CI GECKO-027
GitHub Actions run #41 (`36212793683`) sur `df8ebb93b43cd5b5c69bcdf382c7d5b1e23b8b7c` : tests unitaires + APK + AAB réussis. Version `0.10.3-dev`. La vérification téléphone doit confirmer que la surface du bouton ne recouvre plus jamais le Prof, y compris pendant press et micro-animation.


## GECKO-028 — cache harmonisé, voix masculine et vie idle du Prof
La palette de régions est centralisée dans `GeckoBoardPalette`. Le même entier ARGB sert au dessin de la case et au cache d'overlay. Le cache est inset de 4 % sur chaque bord ; la vidéo conserve le rectangle intégral de la case.

`ProfessorVoicePolicy` préfère une voix française dont nom/features signalent explicitement male/masculin/homme. Android ne normalisant pas le genre sur tous les moteurs TTS, le fallback choisit une voix française locale disponible et abaisse le pitch à 0,78 ; une voix explicitement masculine utilise 0,94. Débit 0,93.

`ProfessorIdleAnimationPolicy` fournit trois actions (BOUNCE/TILT/NOD) et un délai pseudo-aléatoire borné à 10–20 s. Le clic Prof anime immédiatement puis reprogramme le timer. Les animations idle utilisent uniquement transform ; pause/destroy et Anim OFF retirent le callback.


### Preuve CI GECKO-028
GitHub Actions run #45 (`36213803733`) : tests unitaires + APK + AAB réussis sur le commit `6c0e602b607c352bb29bbf4aeca228d38ee4939b`. Version `0.10.4-dev`. La validation téléphone reste nécessaire pour juger la perception masculine du TTS installé et l'intégration visuelle du cache coloré.


## GECKO-029 — fréquence accrue et musique de victoire indépendante
Le Prof passe d'un idle 10–20 s à 2–3 s. Le timer reste volontairement indépendant de la visibilité de `ProfessorBubbleView`, donc le PNG du bouton continue ses micro-animations lorsque la bulle est ouverte. Seuls Anim OFF, pause/destroy, portrait invisible et célébration bloquent les animations idle.

Le scheduler des longues actions Gecko conserve la même logique de sécurité mais change ses valeurs par défaut : 45 % de probabilité par événement éligible et cooldown 45 s au lieu de 12 % / 180 s.

`CelebrationAudioPolicy` formalise le découplage son/visuel : la fin de `VictoryCelebrationView` ne stoppe plus le MP3 de célébration. Le lecteur MediaPlayer le laisse aller jusqu'à sa fin naturelle ; Nouvelle, FX OFF, pause ou destroy restent autorisés à l'arrêter.


### Preuve CI GECKO-029
GitHub Actions run #49 (`36215814330`) sur `f2ea4b0da5935dc1760831229ba8bd9540678c16` : suite de tests, APK et AAB réussis. Version `0.10.5-dev`. Validation téléphone encore requise pour la cadence ressentie des animations et la lecture complète du MP3 de victoire.


## GECKO-030 — restauration du vrai Prof_actions.mp4
L'investigation Git confirme que `14126.mp4` et `assets/prof/Prof_actions.mp4` sont le même blob `87c72def...`. La régression était uniquement runtime : GECKO-026 avait supprimé `maybePlayProfessorLongAction()`.

Le MP4 complet est restauré sans revenir à l'ancien emplacement dans la bulle. `professorButtonHost` possède maintenant un `ChromaKeyVideoView` dédié, de même rectangle que le portrait, ajouté au-dessus du Button et du PNG. Le PNG reste visible tant que MediaPlayer n'a pas réellement démarré, puis est masqué pendant le playback et restauré à EOF/erreur/stop.

Le fichier joue depuis t=0 jusqu'à EOF naturel, sans découpage ni boucle, audio embarqué muet. Clic Prof et idle 2–3 s démarrent le vrai MP4 si aucune lecture n'est en cours. La bulle ouverte n'est pas une condition de blocage. Les micro-animations PNG sont conservées uniquement comme fallback si la vidéo ne peut pas être jouée.


### Preuve CI GECKO-030
GitHub Actions run #53 (`36224509248`) sur `09e475d897f308e68150e0228f50605f094d1f21` : tests unitaires + APK + AAB réussis. Artefact `GeckoDoku-v0.10.6-dev-Android` id `10900510994`. Version `0.10.6-dev`. La validation téléphone doit confirmer le retour visuel exact des animations du MP4 fourni.


## GECKO-031 — expérience TTS locale isolée
Référence stable avant expérience : v0.10.6-dev. Le runtime normal du Prof conserve `ProfessorSpeech` basé sur Android TextToSpeech. Le benchmark TTS est un sous-système séparé, non appelé par `showProfessorBubble()` ou `showProfessorHint()`.

Choix expérimental : Sherpa-ONNX v1.13.8 + Piper `fr_FR-siwis-low` / `fr_FR-siwis-medium`, même famille de voix. Les modèles sont préparés par CI et embarqués dans l'artefact Android, mais ne sont pas stockés en gros blobs Git.

Architecture prévue :
- `VoiceBenchmarkEngine` : API Kotlin commune ;
- `VoiceBenchmarkVariant` / catalogue : Android, Piper LOW, Piper MEDIUM ;
- `PiperVoiceBenchmarkEngine` : une implémentation unique pour LOW/MEDIUM ;
- `PiperSingleModelSlot` : garantit release avant changement de modèle ;
- `AndroidTtsBenchmarkEngine` : référence Android séparée du Prof ;
- dialogue temporaire dans MainActivity ;
- métriques génération / modèle / PSS approximatif.


### Implémentation GECKO-031
Le benchmark dispose d'un contrat `VoiceBenchmarkEngine.synthesizeAndPlay(text, callback)` commun. Android TTS et les deux profils Piper sont sélectionnés par `VoiceBenchmarkVariant`.

LOW et MEDIUM instancient la même classe `PiperVoiceBenchmarkEngine` et partagent un seul `PiperModelManager` + un seul executor série. `PiperSingleModelSlot` libère explicitement l'OfflineTts courant avant création d'un profil différent. La fermeture du dialogue, pause et destroy demandent la libération du modèle expérimental.

Piper génère en mémoire puis joue via AudioTrack PCM float. Android TTS synthétise la même phrase vers un WAV temporaire puis la joue via MediaPlayer. Les métriques incluent temps de génération ; Piper ajoute temps de chargement, taille ONNX, fréquence/durée et ΔPSS approximatif.

Le bouton temporaire est ajouté à row3 comme quatrième enfant afin de ne créer aucune nouvelle rangée et de ne pas réduire la hauteur de grille par ajout vertical.


### GECKO-031 — correction intégration native
La dépendance Sherpa-ONNX expérimentale n'utilise finalement pas JitPack : cette coordonnée apporte le JAR JVM en plus de l'AAR Android et produit des classes dupliquées. La CI récupère désormais uniquement l'AAR Android officiel v1.13.8, vérifie son SHA-256 et le place dans `app/libs` avant Gradle. `app/build.gradle.kts` référence ce fichier local temporaire.

Les métriques PSS utilisent `Long`, conformément au type Android actuel de `Debug.getPss()`.


### Preuve CI GECKO-031
GitHub Actions run #58 (`36229808317`) sur `2adf97bc49453b929a2a9e15ddb3042c0a950631` :
- AAR officiel Sherpa-ONNX v1.13.8 téléchargé et SHA-256 vérifié ;
- archives Piper LOW/MEDIUM téléchargées et SHA-256 vérifiés ;
- tests unitaires réussis ;
- APK debug réussi ;
- AAB debug réussi ;
- artefact `GeckoDoku-v0.10.7-voice-ab-exp-Android`, id `10902486871`, taille archive ~317,1 Mo.

Cette preuve porte uniquement sur la build expérimentale de branche. La référence stable `main` reste v0.10.6-dev tant que Fabrice n'a pas comparé les voix sur téléphone.


## GECKO-032 — décision Pierre UPMC Medium
La phase A/B est terminée. Le modèle runtime retenu devient `fr_FR-upmc-medium`, modèle Piper français à deux locuteurs. GeckoDoku fixe `sid=1` pour Pierre ; `sid=0` correspond à Jessica.

Le laboratoire A/B, LOW et Siwis MEDIUM sont retirés. `ProfessorSpeech` devient une façade : Piper Pierre en chemin nominal, Android TTS en secours seulement si le moteur local échoue.

Les encouragements existants restent disponibles. Une politique de source choisit entre audio enregistré et encouragement synthétisé par Pierre. La voix de Pierre ne remplace donc pas les clips existants.

La musique de début de niveau devient séquencée avec l'annonce stats : MediaPlayer termine d'abord la musique, puis `ProfessorSpeech` prononce un résumé généré depuis `PlayerStatsStore.read()`.


### Implémentation GECKO-032
`ProfessorSpeech` est désormais une façade avec chemin nominal `PierrePiperSpeechEngine` et secours `AndroidProfessorSpeech`. Pierre utilise toujours `sid=1`. Le modèle est chargé paresseusement sur un executor dédié et conservé tant que l'Activity vit ; aucun second modèle Piper n'existe.

Les encouragements alternent aléatoirement à parts égales entre les 13 segments enregistrés et une liste complémentaire de phrases Pierre. Le cas 1–2 geckos restants bénéficie d'une phrase Pierre dédiée.

`AssetAudioPlayer.playMusic()` accepte un callback de fin. Pour `LEVEL_START`, ce callback construit `PlayerStatsNarration` depuis les stats locales et fait parler Pierre. Le callback est retiré si la musique est interrompue par une nouvelle grille, empêchant une annonce obsolète.


### Preuve CI GECKO-032
GitHub Actions run #61 (`36232826836`) sur `8aae5460caebf5d1f6d25b023e7a5b698ce2f917` : AAR Sherpa vérifié, archive UPMC Medium vérifiée, tests unitaires réussis, APK et AAB construits. Artefact `GeckoDoku-v0.10.8-dev-Android` id `10903700375`, taille ZIP APK+AAB `286519230` octets.

Le laboratoire A/B précédent produisait ~317,1 Mo pour le même ZIP double-artifact ; la suppression de LOW + Siwis Medium réduit déjà la distribution de test tout en conservant uniquement Pierre UPMC Medium.


### Preuve finale main GECKO-032
Après fast-forward sans conflit, GitHub Actions run #63 (`36233134204`) sur `main` termine SUCCESS. Artefact `GeckoDoku-v0.10.8-dev-Android` id `10903207369`, taille ZIP APK+AAB `286519227` octets. La voix nominale est désormais UPMC Medium / Pierre sid 1 ; Android TTS n'est qu'un fallback.


## GECKO-033 — mission préparée, non implémentée
Trois assets uploadés par Fab sont désormais contractualisés sans changement de runtime :
- `ProfParle.mp4` : animation spécifique à la parole de Pierre ; lecture lancée juste avant la voix et réutilisée si déjà en cours pendant sa fenêtre ~30 s ;
- `IntroGeckoGD.mp4` : première intro, placée avant `Gecko_Intro.mp4` ;
- `IconGeckoGD.png` : source de l'icône Android et petit visuel près du titre.

Important : `Prof_actions.mp4` reste l'animation générale du Prof. `ProfParle.mp4` est exclusivement l'animation de parole.
Aucun code n'a été modifié dans le cycle de préparation GECKO-033.


<!-- GECKO-033-ADDENDUM-FAB-2026-09-26 -->
## GECKO-033 — addendum Fab, identité + Prof vivant (non codé)
Le contrat GECKO-033 est étendu sans modification runtime dans ce cycle.

- `IconGeckoGD.png` est l’unique source d’identité pour **launcher Android** et **médaillon rond à gauche du titre**. Le médaillon peut avoir une micro-animation locale mais ne doit jamais utiliser `IntroGeckoGD.mp4` ni affecter la grille.
- Toute vidéo d’animation Gecko devient **muette au player** ; les MP3/musiques du jeu restent séparés.
- `ProfParle.mp4` est exclusivement synchronisé au démarrage réel de la parole de Pierre, muet, réutilisé s’il tourne déjà. `Prof_actions.mp4` reste le canal général/idle du Prof.
- Pierre peut intervenir contextuellement : aide après inactivité, proposition de sauvegarde sur partie longue, prise de nouvelles, banalités Gecko.
- La source de vérité des banalités est la banque **exactement 100 répliques** ajoutée à `ordres-de-mission.md`, avec anti-répétition et cooldown.
- Référence scientifique des phrases : adhérence sèche des geckos principalement par forces de van der Waals via setae/spatulae ; ne pas présenter l’effet Casimir comme mécanisme d’adhérence.
- Tout reste **à implémenter** après RED TDD. Aucun code applicatif n’est modifié par ce commit documentaire.


<!-- GECKO-033-PROFPARLE-SPEECH-LIFECYCLE-2026-09-26 -->
### GECKO-033 — cycle de vie ProfParle piloté par la parole
`ProfParle.mp4` est désormais contractuellement lié à l’état réel de la synthèse vocale Pierre :
- début parole → démarrer/réutiliser la vidéo ;
- parole toujours active → conserver l’instance en cours ;
- fin/annulation/échec de parole → stopper la vidéo immédiatement et restaurer `Prof.png` ;
- nouvelle parole après arrêt → relancer la vidéo depuis t=0.
La durée nominale ~30 s du média n’est plus une durée d’affichage imposée.
