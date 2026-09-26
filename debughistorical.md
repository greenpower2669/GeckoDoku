# Debug historical — GeckoDoku

## 2026-09-25 — Grille initiale non unique
Correction : validation d'unicité puis génération sécurisée.

## 2026-09-25 — Workflow APK absent
Correction : GitHub Actions ajouté.

## 2026-09-25 — Deux clics simples pouvaient créer un gecko
Correction v0.2 : intentions séparées + GestureDetector.

## 2026-09-25 — Unique ne signifie pas humainement résoluble
Correction : HumanSolver explicable.

## 2026-09-25 — Difficulté peu fidèle
Correction v0.3 : classement par techniques réellement indispensables.

## 2026-09-25 — Projection de zone
Ajout REGION_TOUCH_PROJECTION pour les zones à 2–4 candidats partageant une interdiction de voisinage.

## 2026-09-25 — Gecko X-Wing
Ajout lignes, colonnes et paires de zones.

## 2026-09-25 — Professeur absent alors que le solveur connaît le chemin
### Symptôme
Une grille peut être logiquement résoluble mais le joueur ne sait pas quelle déduction le solveur a utilisée. Exemple téléphone : 12×12 Difficile, projection requise ; une exclusion oubliée masquait la suite.
### Cause
SolveAnalysis gardait seulement technique + action, sans preuve visuelle ni état avant l'étape. L'UI ne consommait pas la trace.
### Correction v0.4
- SolveStep enrichi avec sources, zones, axe et préconditions ;
- Puzzle conserve solverTrace ;
- ProfessorGecko réutilise cette trace ;
- fallback nextStep sur l'état réel du joueur ;
- révélation progressive 1/3, 2/3, 3/3 ;
- surlignage pédagogique.
### Non-régression
Le Prof ne joue pas automatiquement. Si aucune déduction sûre n'est trouvée, il l'annonce.

## Vigilances v0.4
- vérifier que le cache choisit toujours une étape encore valide ;
- tester les chemins humains différents de la trace ;
- tester projection et X-Wing visuellement ;
- surveiller la lisibilité du surlignage sur 12×12 ;
- éviter que les explications deviennent trop longues sur petit écran.


## 2026-09-25 — Prof Gecko masqué par la barre Android
### Symptôme
Sur téléphone 12×12, le bouton Prof Gecko apparaît derrière la barre de navigation Android et n'est pas utilisable normalement. Le haut de l'interface empiète également sur la barre d'état.
### Cause
L'activité cible Android API 36 et l'interface programmatique n'appliquait aucun WindowInsets système.
### Correction v0.4.1
Application des insets systemBars haut, bas, gauche et droite au conteneur racine, en plus du padding de base. Compatibilité API 26–36.
### Non-régression
Prof Gecko doit rester entièrement visible et cliquable avec navigation Android par boutons comme par gestes.

## 2026-09-25 — Facile nécessitant deux X-Wing
### Symptôme
Test humain sur grille 12×12 affichée « Facile • zones 1 • trace 7 » : deux Gecko X-Wing ont été nécessaires pour terminer.
### Interprétation
Si ces deux X-Wing sont logiquement indispensables, le classement Facile est faux malgré l'analyse actuelle.
### Décision
Bug enregistré comme GECKO-012. Ne pas le mélanger au correctif UI urgent ; auditer ensuite DifficultyIndexer, solverTrace et la notion de technique réellement nécessaire.


## 2026-09-25 — Impossible de rejouer/conserver une bonne grille
### Besoin
Une grille appréciée devait pouvoir être rejouée à l'identique et conservée pour être testée de nouveau par le joueur ou une autre personne.
### Correction v0.5
Ajout d'un journal local persistant et de l'action Rejouer. Le journal stocke la définition originale et non la progression afin de garantir un redémarrage propre.

## 2026-09-25 — Stats sans réussite par difficulté
### Symptôme
L'application comptait les terminées par difficulté mais n'affichait pas le nombre de tentatives ni le taux de réussite par niveau.
### Correction v0.5
Exposition de started_diff + completed_diff et affichage completionRate pour chaque GameDifficulty.

## Vigilances v0.5
- journal toujours relisible après fermeture/réouverture de l'app ;
- supprimer une entrée sans affecter la partie courante ;
- vider le journal sans supprimer les stats ;
- rejouer doit conserver exactement la même définition de grille ;
- vérifier que le Prof reconstruit correctement sa trace au chargement ;
- vérifier que chaque replay/load compte bien comme nouvelle tentative ;
- GECKO-012 difficulté reste ouvert et non corrigé par cette version.


## 2026-09-25 — Niveaux au-delà du déterministe
### Besoin
Certaines fins de grille peuvent rester à deux possibilités après toutes les techniques, X-Wing compris. Le joueur souhaite tester les hypothèses plutôt que déclarer la grille insoluble.
### Correction v0.6
Ajout HypothesisSolver borné. Il ne brute-force pas la solution : il ne branche que sur des paires de deux candidats et exige une contradiction démontrable avant de forcer l'autre choix.
Mission Impossible = une hypothèse.
Infernal = deux hypothèses ou profondeur 2.
### Vigilance
Valider humainement que les contradictions et les textes du Prof sont naturels. Refuser les grilles nécessitant plus de profondeur.

## 2026-09-25 — Prof sans bulle visuelle
### Besoin
Le texte d'indice devait ressembler à une parole du Prof en plus des repères sur la grille.
### Correction v0.6
ProfessorBubbleView, bulle BD accessible et temporaire, masquée dès que le joueur agit.

## 2026-09-25 — Fin de partie trop sobre
### Besoin
Récompense visuelle proportionnelle à la difficulté.
### Correction v0.6
VictoryCelebrationView procédural, sans asset lourd. Confettis et feux d'artifice augmentent avec le niveau, sans stroboscope. Toucher ferme l'overlay.

## Vigilances v0.6
- génération Mission Impossible / Infernal peut être plus coûteuse, surtout 12×12 ;
- tester que le Prof retrouve une hypothèse sur un état réellement bloqué ;
- vérifier qu'aucune branche n'est rejetée sans contradiction ;
- vérifier lisibilité de la bulle sur petit écran ;
- vérifier overlay de victoire avec barres Android ;
- GECKO-012 reste ouvert : classification Facile/X-Wing à auditer séparément.


## 2026-09-25 — Prof montre mais ne joue pas
### Besoin
Le joueur souhaite pouvoir demander au Prof de poursuivre réellement la résolution, une étape à la fois, au lieu de devoir recopier chaque exclusion/gecko indiqué.
### Correction v0.7
Ajout GameEngine.applyProfessorStep. Chaque pression applique une seule déduction déterministe puis s'arrête.
### Hypothèses
Une bifurcation à deux candidats utilise deux phases : deux geckos fantômes d'abord, contradiction et mutation seulement à la pression suivante. L'overlay fantôme ne touche pas aux hypothèses personnelles.
### Garde-fou
Un gecko forcé est contrôlé contre la solution unique avant toute mutation. Une divergence devient une alerte de debug et non une erreur joueur.

## 2026-09-25 — Réussites assistées indifférenciées
Correction v0.7 : stats globales et par difficulté conservent le nombre de grilles terminées avec Prof, tout en gardant le taux de réussite standard.


## 2026-09-25 — Bulle Prof écrase la grille
### Symptôme
Capture téléphone v0.7 : en 12×12, la grande bulle BD apparaît entre l'en-tête et le plateau et force la grille à devenir minuscule.
### Cause
ProfessorBubbleView était un enfant normal du LinearLayout vertical.
### Correction v0.8
La bulle est sortie du flux de layout et dessinée en overlay dans screenRoot. Les commandes secondaires sont temporairement masquées pour dégager l'espace visuel ; le bouton Prof reste toujours accessible. Une croix × ferme la bulle.
### Non-régression
La taille du plateau ne doit plus diminuer à cause de la longueur du texte du Prof.

## 2026-09-25 — Célébration sans FX dédiés
### Besoin
Les feux d'artifice et confettis doivent être accompagnés de sons cohérents et gradués.
### Correction v0.8
VictoryCelebrationView émet un callback par salve ; ToneFxFeedback synchronise lancement/explosion et accent final. Les callbacks différés sont annulables.


## 2026-09-26 — Risque de mélanger les rôles des nouveaux médias
### Constat
Plusieurs médias Gecko et Prof ont été ajoutés successivement. Une interprétation trop rapide avait commencé à confondre « banque d'actions », apparition, disparition, intro et portrait Prof.
### Décision GECKO-022
Figer un catalogue à rôle unique par fichier. Aucun fichier ne doit être réutilisé pour un autre rôle sans nouvel ordre de mission.
### Décision vidéos 30 s
Ne pas découper Gecko_actions_plusieurs.mp4 ni Prof_actions.mp4 maintenant. Les lire occasionnellement en totalité, 0→EOF, sans boucle.
### Décision rendu
L'habillage riche doit toujours être un overlay désactivable au-dessus du rendu normal. Aucune dépendance du moteur logique.
### Décision assets
Sortir les médias de la racine du dépôt et les ranger sous assets/gecko et assets/prof sans réencodage.


## 2026-09-26 — Keycolor bleu avancé dans GECKO-022
### Changement de mission
Fab demande finalement le keycolor bleu immédiatement, uniquement pour transformer le fond bleu en transparence. L'ancien point « PAS MAINTENANT » est remplacé par cet amendement explicite.

### Choix technique
Les MP4 H.264 n'emportent pas d'alpha fiable. Le rendu utilise MediaPlayer vers SurfaceTexture externe OES puis un fragment shader OpenGL ES 2.0 : dominance bleue, transition douce et despill. Cette voie couvre minSdk 26 sans dépendre de RuntimeShader API 33.

### Protection
Le média reste décoratif. Erreur d'asset, de MediaPlayer ou de shader : log + fermeture overlay, sans rollback logique. Une seule vidéo riche à la fois ; victoire, nouvelle grille, pause et désactivation arrêtent la vidéo.

### TDD
Le test scheduler a été lancé seul avant implémentation et a échoué sur `Unresolved reference RichMediaScheduler`, RED attendu.


### Preuve build v0.9
GitHub Actions run #15 (`36203684907`) sur commit `e9dbfa394246f6ba0f552ef69ff3d1da847ede76` : succès de `:app:testDebugUnitTest`, `:app:assembleDebug` et `:app:bundleDebug`. APK et AAB renommés puis regroupés dans l'artefact `GeckoDoku-v0.9.0-dev-Android` (id `10893390568`, digest `sha256:77d4dddc2db8433a33ca7689da8c3b02ef9d3959fed435dc033731f596842882`). Le rendu chroma-key lui-même nécessite encore validation visuelle sur téléphone.


## 2026-09-26 — Banque vocale d'encouragement
### Source observée
`assets/audio/encouragements/master/Voix_encouragements.mp3`, durée mesurée 14,441 s, contient 13 phrases séparées par des silences nets. La dernière phrase contient une pause interne volontaire après « Ça » et ne doit pas être scindée.

### Décision GECKO-023
Ne pas piloter un MP3 long avec des seeks/timers à chaque coup. Conserver le master et produire 13 clips courts déterministes à partir des timecodes canoniques de l'ordre de mission. Lecture seulement après un nouveau gecko correctement confirmé par le joueur, avec anti-répétition et garde contextuelle pour « Tu y es presque ».

### Vigilances
- éviter les coupes trop serrées : les timecodes gardent une marge autour de la parole ;
- ne pas féliciter un gecko placé par le Prof ;
- ne pas rejouer une phrase si la même cellule est retirée puis reposée ;
- ne pas mélanger voix d'encouragement et futur son de victoire ;
- FX OFF doit rester réellement silencieux ;
- audio absent = aucun impact logique.


### Rangement physique des MP3 — sans code
Pendant le test téléphone v0.9, Fab demande uniquement de préparer GECKO-023 et de ranger les assets audio. Les trois blobs MP3 sont déplacés sans réencodage ni changement de contenu vers `assets/audio/`, classés en `intro/`, `celebration/` et `encouragements/master/`. Aucun fichier Kotlin/Gradle n'est modifié pour ce rangement.


## 2026-09-26 — Vidéos inversées et grille mobile pendant médias
### Retour téléphone
Les vidéos riches apparaissent tête en bas et l'animation de case donne une composition trop générale. Fab exige une grille immuable et un cache blanc local avant PNG/vidéo.

### Hypothèse racine orientation
Le renderer utilise déjà `SurfaceTexture.getTransformMatrix()` mais son buffer UV est lui-même verticalement inversé. Cela peut appliquer deux corrections verticales et produire le rendu tête en bas. Le correctif sera précédé d'un test RED sur les UV canoniques.

### Décision géométrique
La grille ne participe jamais au layout des médias. Les médias utilisent le rectangle réel de case et un cache blanc local dans l'overlay. Tout débordement visuel reste hors layout.


### Correctif implémenté GECKO-024
Le test RED run #25 échoue exactement sur les deux classes absentes attendues. Le correctif remplace les UV inversés par des UV canoniques, ajoute le cache blanc de case, localise les animations Gecko/Prof et branche musique/voix/TTS sans modifier GameEngine. Les 13 clips physiques restent à versionner ; le build téléphone lit temporairement les fenêtres canoniques directement dans le master.


### PNG Gecko statique
`assets/gecko/Gecko.png` est un raster transparent léger dérivé du Gecko vectoriel/procédural déjà présent dans l'application. Il devient le sprite normal de la grille ; le rendu procédural reste le fallback si l'asset ne peut pas être lu.


### Échec compilation run #26 — portrait Prof
Le premier GREEN a atteint la compilation Kotlin puis s'est arrêté sur `ProfessorBubbleView.kt` : `Unresolved reference 'paint'`. Cause racine : le nouveau rendu bitmap du portrait utilisait un nom de Paint qui n'existait pas dans cette View. Correction minimale : ajout d'un `imagePaint` dédié et usage exclusif pour `drawBitmap`. Aucun changement de logique ou de géométrie.


### Preuve GREEN run #28
Après le correctif `imagePaint`, GitHub Actions run #28 (`36209229581`) termine avec succès : `:app:testDebugUnitTest`, `:app:assembleDebug` et `:app:bundleDebug`. L'artefact Android v0.10.0-dev est produit. Les symptômes réels (orientation, ancrage, audio/TTS) doivent encore être contrôlés sur téléphone : CI ne valide pas le rendu visuel ni la perception audio.


## 2026-09-26 — Conflit Gecko.png / Gecko_tr.png
Fab signale que le bon sprite avait déjà été uploadé sous `Gecko_tr.png`. Le `Gecko.png` généré pendant GECKO-024 crée donc un conflit d'identité visuelle. Décision : Gecko_tr devient canonique et le doublon sera supprimé.

Même retour : le Prof doit être davantage animé. Le scheduler partagé 12 % / 180 s n'est pas adapté à ce besoin ; l'animation Prof devient systématique à chaque intervention éligible, tout en restant locale au portrait et non bloquante.


### TDD GECKO-025
Run #32 : RED attendu. `AssetMediaCatalogTest` impose `gecko/Gecko_tr.png` et `ProfessorAnimationPolicyTest` échoue sur la classe absente. Le correctif introduit cette politique pure puis retire le gate aléatoire 12 % / 180 s du Prof. Le scheduler reste utilisé uniquement pour les actions Gecko occasionnelles.


### GREEN GECKO-025
Run #33 (`36210582662`) : succès complet après passage de Gecko_tr en sprite canonique et suppression du gate aléatoire du Prof. Les tests imposent le chemin `gecko/Gecko_tr.png` et la politique d'animation systématique sous conditions de priorité.


## 2026-09-26 — GECKO-026 cadrage grille / bulle Prof
### Symptôme
Sur téléphone, ouverture de la bulle Prof → recadrage visible de la grille.

### Cause racine trouvée
`showProfessorBubble()` exécute `controlsPanel.visibility = View.GONE`. Le plateau est un enfant de hauteur `0` avec `weight=1f` dans le même `LinearLayout`. En supprimant les trois rangées de contrôles du flux, Android augmente mécaniquement la hauteur disponible pour le plateau, qui se remesure et change son rectangle. À la fermeture, le phénomène inverse se produit.

### Décision
Ne plus changer le flux de layout pour une bulle. La bulle reste enfant overlay de `screenRoot`. Prof.png sort de la bulle et devient décor du bouton Prof à taille de host fixe ; la vidéo Prof ne joue plus dans la bulle.


### TDD RED GECKO-026
Run #36 (`36211900132`) échoue volontairement sur `Unresolved reference 'ProfessorUiPolicy'`. Le test impose : contrôles visibles pendant la bulle, aucun portrait/vidéo dans la bulle, portrait visible et animé dans un host de bouton fixe 58 dp avec débordement 10 dp.

### Correctif GECKO-026
- suppression de `controlsPanel = GONE` à l'ouverture : cause racine du re-layout ;
- `ProfessorBubbleView` simplifiée : aucun bitmap, aucune géométrie portrait, élévation flottante ;
- suppression du lancement `Prof_actions.mp4` dans la bulle ;
- `Prof.png` placé dans un host fixe autour du bouton avec clip désactivé ;
- micro-animation locale par `scaleX/scaleY/translationY`, donc sans `requestLayout` ;
- retrait de `ProfessorAnimationPolicy` et de son test, supersédés par GECKO-026.


### GREEN GECKO-026
Run #37 (`36212085221`) termine en succès complet. Le build compile après suppression de la vidéo Prof dans la bulle, déplacement du PNG dans le bouton et maintien permanent du panneau de contrôles dans le layout. La CI valide la structure et les tests ; seul le téléphone peut confirmer le symptôme visuel initial.


## 2026-09-26 — GECKO-027 bouton devant Prof
### Symptôme
Sur téléphone, le rectangle du bouton passe visuellement devant le PNG Prof.

### Cause probable
Le PNG est ajouté après le Button dans le FrameLayout, mais un Button Android peut disposer d'une élévation/StateListAnimator qui modifie son Z lors des états pressés. L'ordre d'ajout seul n'est donc pas une garantie suffisante.

### Stratégie
Test RED sur une politique d'élévation explicite, puis Button à Z neutre et portrait avec Z supérieur + `bringToFront()` avant animation.


### TDD RED GECKO-027
Run #40 échoue comme prévu : `ProfessorUiPolicyTest` référence `buttonElevationDp` et `portraitElevationDp` absents. Cela verrouille le besoin de Z explicite avant implémentation.

### Correctif GECKO-027
- Button Prof : `stateListAnimator=null`, élévation 0 dp, translationZ 0 ;
- Prof.png : élévation 18 dp ;
- `bringToFront()` après insertion dans le host et avant chaque micro-animation ;
- aucune modification de taille/position du host ni de la grille.


### GREEN GECKO-027
Run #41 (`36212793683`) termine en succès complet. Le Button Prof a son StateListAnimator neutralisé et un Z nul ; Prof.png reçoit 18 dp d'élévation et est ramené au premier plan avant chaque animation. La CI valide le code et les tests ; le contrôle final de superposition reste visuel sur téléphone.


## 2026-09-26 — GECKO-028
Retour téléphone : le cache blanc d'animation Gecko tranche avec la couleur de région ; exemple observé : cache blanc sur une case vert pâle qui devait conserver RGB(232,248,232). Le Prof est aussi vocalisé avec une voix perçue féminine et paraît trop statique hors clic.

Décision : palette unique réutilisée par le plateau et les overlays ; cache inset 4 %. Pour le TTS, Android n'expose pas de propriété de genre normalisée sur tous les moteurs : préférence explicite pour les voix françaises dont nom/features signalent masculin, sinon voix française disponible avec pitch abaissé. Prof PNG : animations au clic + timer aléatoire 10–20 s.


### TDD RED GECKO-028
Run #44 échoue sur les nouvelles classes/politiques absentes, conformément au RED attendu.

### Correctif GECKO-028
- palette région centralisée, évitant toute divergence couleur grille/cache ;
- masque de cellule inset 4 %, couleur locale exacte ;
- stratégie TTS masculine avec fallback grave documenté ;
- trois micro-animations Prof et timer idle 10–20 s ;
- nettoyage des callbacks sur pause/destroy et désactivation animations.


### GREEN GECKO-028
Run #45 (`36213803733`) : succès complet. Palette/cache, stratégie TTS et timer Prof compilent avec la suite existante. Limite explicitement conservée : le genre des voix TTS dépend du moteur installé ; la sélection masculine est préférentielle, avec fallback vocal grave.


## 2026-09-26 — GECKO-029
Retour téléphone : Prof encore trop peu animé, Gecko long encore trop rare, et musique de félicitations tronquée.

Cause musique confirmée dans `MainActivity` : callback `VictoryCelebrationView.onCelebrationStopped` appelle `gameAudio.stopMusic()`, ce qui couple la durée visuelle à la durée du MP3. Décision : supprimer ce couplage ; seul le lecteur audio décide de la fin naturelle de la piste.


### TDD RED GECKO-029
Run #48 : RED attendu. Les nouveaux tests imposent Prof 2–3 s, Gecko 45 % / 45 s et une politique de célébration qui ne stoppe pas la musique à la fin visuelle. Échec observé sur `CelebrationAudioPolicy` absent.

### Correctif GECKO-029
- Prof idle : 2–3 s ;
- aucune condition de blocage liée à la bulle Prof ;
- scheduler Gecko par défaut : 45 % / 45 s ;
- `onCelebrationStopped` ne coupe plus le MP3 lorsque la politique vaut false ;
- la fin naturelle du MediaPlayer devient la fin normale de la musique de victoire.


### GREEN GECKO-029
Run #49 (`36215814330`) : succès complet. Le test de régression confirme la politique de musique indépendante de la fin visuelle ; les tests de cadence Prof et Gecko passent avec les nouvelles valeurs.


## 2026-09-26 — GECKO-030 régression vraie vidéo Prof
### Preuve asset
Le fichier ré-envoyé par Fab `14126.mp4` a le git blob SHA-1 `87c72def4f7c79b7c7c35ed254e3380ac5a7c0db`, exactement égal au blob de `assets/prof/Prof_actions.mp4` sur main. L'asset n'a donc jamais été perdu.

### Root cause historique
Le commit GECKO-025 `8aaa06f...` lançait `Prof_actions.mp4` à chaque intervention éligible. Le commit GECKO-026 `f555102...` a explicitement supprimé `maybePlayProfessorLongAction()`, retiré `ProfessorAnimationPolicy` et remplacé la vidéo par une micro-animation PNG. Cette décision a satisfait le déplacement hors bulle mais a supprimé, à tort, les vraies animations demandées.

### Correctif visé
Restaurer exactement le MP4 complet mais dans le bouton, via un ChromaKeyVideoView local et indépendant de la bulle/grille.


### TDD RED GECKO-030
Run #52 échoue uniquement parce que `ProfessorUiPolicy` ne contient pas encore `playVideoInButton`, `playProfVideoFromStartToEnd` et `muteProfVideoEmbeddedAudio`. Le RED verrouille donc bien la restauration de la vraie vidéo.

### Correctif GECKO-030
- `ChromaKeyVideoView.play()` reçoit un callback `onStarted`, afin de ne masquer le PNG qu'au démarrage réel ;
- un ChromaKeyVideoView dédié est ajouté dans `professorButtonHost` au même rectangle que le portrait ;
- `Prof_actions.mp4` complet est lu au clic et à l'idle 2–3 s ;
- aucun restart/empilement si déjà en cours ;
- bulle ouverte autorisée ;
- audio vidéo muet ;
- PNG restauré à EOF/erreur/stop ;
- fallback micro-animation PNG uniquement si vidéo indisponible.


### GREEN GECKO-030
Run #53 (`36224509248`) : succès complet. Le build compile avec le ChromaKeyVideoView local au bouton, le callback onStarted et la restauration du PNG à EOF/erreur/stop. L'asset vidéo utilisé est toujours le blob exact `87c72def...`.


## 2026-09-26 — GECKO-031 préparation expérience TTS
Fab demande un A/B local sans remplacement du Prof : Android TTS vs Piper LOW vs Piper MEDIUM.

Recherche amont :
- sherpa-onnx latest vérifié : v1.13.8, release 10/09/2026 ;
- API Kotlin officielle confirme `OfflineTts.release()` et la config VITS Piper ;
- archives officielles disponibles pour `vits-piper-fr_FR-siwis-low` et `...-medium` ;
- LOW/MEDIUM siwis utilisent le même speaker/famille, ce qui évite de confondre qualité et timbre.

Risque principal : charger simultanément deux modèles natifs ONNX. Garde-fou imposé par contrat + test : release de l'ancien avant création du suivant.


### TDD RED GECKO-031
Run #56 échoue comme prévu à la compilation des tests : `VoiceBenchmarkVariant`, `VoiceBenchmarkCatalog` et `PiperSingleModelSlot` sont absents. Le test prouve donc que le contrat A/B et le garde-fou mono-modèle ne préexistaient pas.

### Implémentation GREEN GECKO-031
- Sherpa-ONNX v1.13.8 via JitPack ;
- CI télécharge les deux archives officielles Piper et vérifie leurs SHA-256 avant extraction ;
- les modèles sont injectés sous `assets/tts/piper/low` et `medium` uniquement pendant la construction de l'artefact ;
- interface Kotlin unique pour le benchmark ;
- slot mono-modèle : release ancien avant create nouveau ;
- Android TTS reste séparé de `ProfessorSpeech` ;
- UI A/B ne modifie pas le flux normal du Prof.


### Run #57 — échec GREEN analysé
L'étape `Prepare Piper LOW + MEDIUM assets` est verte : archives téléchargées, checksums vérifiés et chemins modèle/tokens/espeak-ng-data confirmés.

La compilation échoue ensuite sur :
- classes Sherpa dupliquées entre `sherpa-onnx-jvm-v1.13.8.jar` et `sherpa-onnx-v1.13.8.aar` apportés ensemble par JitPack ;
- quatre incompatibilités `Long -> Int` autour de `Debug.getPss()`.

Cause racine dépendance : mauvais mode d'intégration Android, pas défaut Piper. Correctif : AAR officiel seul + PSS en Long.


### GREEN GECKO-031 — run #58
Après remplacement de JitPack par l'AAR Android officiel et passage des métriques PSS en Long, le run #58 (`36229808317`) termine SUCCESS. Les deux étapes de préparation native/modèles, les tests, l'APK et l'AAB sont verts. Artefact final : `GeckoDoku-v0.10.7-voice-ab-exp-Android` id `10902486871`.

La taille importante de l'artefact (~317,1 Mo ZIP APK+AAB) est attendue pour cette expérience : deux modèles Piper + runtime Sherpa sont embarqués. Ce poids ne préjuge pas du choix final ; il sert précisément à comparer LOW/MEDIUM avant de ne conserver qu'une option éventuelle.


## 2026-09-26 — GECKO-032 choix final de voix
Fab choisit la voix masculine Pierre. Vérification externe : le catalogue Piper `fr_FR-upmc-medium` déclare 2 speakers, `jessica:0` et `pierre:1`. Le modèle Siwis du benchmark précédent n'était donc pas le bon modèle final pour Pierre.

Décision : repartir de la branche A/B verte #59, supprimer tout l'UI/labo de benchmark et réduire l'artefact à Sherpa + UPMC Medium. Ajouter un test explicite sid=1 pour éviter une régression silencieuse vers Jessica.


### GECKO-032 — activation CI
La branche GECKO-032 a été ajoutée explicitement au déclencheur GitHub Actions afin que le RED TDD puisse s'exécuter avant le code.


### TDD RED GECKO-032
Run #60 échoue comme prévu sur les symboles absents `PierreVoiceConfig`, `EncouragementSourcePolicy`, `EncouragementSource`, `PierreEncouragements` et `PlayerStatsNarration`. Les tests verrouillent donc avant implémentation le sid Pierre, la coexistence des sources d'encouragement et le résumé statistiques.

### Correctif GECKO-032
- retrait UI/classes/tests du laboratoire A/B ;
- UPMC Medium seul, sid 1 ;
- ProfessorSpeech Piper-first avec fallback Android ;
- VoicePcmPlayer supporte un callback de fin ;
- encouragements enregistrés conservés + Pierre ajouté ;
- musique de niveau suivie d'une annonce stats, jamais superposée.


### GREEN GECKO-032 — run #61
Le premier GREEN complet de Pierre termine en SUCCESS. Aucun modèle LOW/Siwis n'est préparé par la CI. L'artefact double APK+AAB passe d'environ 317,1 Mo (A/B) à 286,5 Mo. Le test `PierreVoiceConfigTest` verrouille UPMC Medium et `sid=1`.


### MAIN GREEN GECKO-032
Run #63 (`36233134204`) sur main : SUCCESS complet. La branche n'avait aucun commit concurrent sur main (fast-forward). Le runtime final n'embarque qu'UPMC Medium ; les modèles du laboratoire A/B ne sont plus préparés.


## 2026-09-26 — GECKO-033 assets uploadés / rôles figés
Fab a uploadé sur `main` :
- `assets/prof/ProfParle.mp4` ;
- `assets/gecko/IntroGeckoGD.mp4` ;
- `assets/gecko/IconGeckoGD.png`.

Risque identifié : confusion entre `ProfParle.mp4` et `Prof_actions.mp4`, ou redémarrage de la vidéo de parole à chaque phrase. Décision figée : `ProfParle.mp4` accompagne uniquement la parole de Pierre et, si elle est déjà active pendant sa durée ~30 s, la nouvelle phrase réutilise la lecture courante au lieu de repartir de zéro.

Aucun code n'a été modifié dans ce cycle ; il s'agit uniquement d'une préparation de mission et de passation.


<!-- GECKO-033-ADDENDUM-FAB-2026-09-26 -->
## 2026-09-26 — GECKO-033 addendum : risques et décisions
Nouvelles décisions Fab, documentaires uniquement :
1. **Risque dérive d’identité** : launcher Android et visuel in-app pourraient diverger. Décision : une seule source `IconGeckoGD.png`; le visuel in-app est un médaillon rond à gauche du titre, micro-animé localement, jamais par `IntroGeckoGD.mp4`.
2. **Risque audio indésirable** : les vidéos Gecko pouvaient historiquement suivre FX. Nouvelle décision prioritaire : tout Gecko animé est muet au player. Les musiques dédiées restent séparées.
3. **Risque confusion Prof** : `Prof_actions.mp4` et `ProfParle.mp4` ne doivent jamais être mélangés. ProfParle accompagne uniquement la parole Pierre et réutilise sa lecture si active.
4. **Risque voix trop bavarde** : les interventions spontanées sont basse priorité, avec cooldown, anti-répétition et blocage pendant contextes critiques.
5. **Risque information scientifique fausse** : ne pas attribuer l’adhérence des geckos à l’effet Casimir ; utiliser van der Waals + setae/spatulae.
6. **Risque sauvegarde intrusive** : Pierre peut seulement proposer une sauvegarde, jamais la déclencher sans action utilisateur.
7. **Risque régression géométrique** : médaillon, vidéo ProfParle et intros sont overlays/transforms ; aucune mesure de grille ne doit changer.

Aucun bug n’est déclaré corrigé par ce commit : il s’agit d’un verrouillage de contrat avant RED TDD.


<!-- GECKO-033-PROFPARLE-SPEECH-LIFECYCLE-2026-09-26 -->
## 2026-09-26 — GECKO-033 précision ProfParle : fin liée à Sherpa
Risque identifié : laisser `ProfParle.mp4` continuer ~30 s après une phrase courte donnerait l’impression que Pierre parle encore alors que Sherpa est silencieux.

Décision Fab : la vidéo de parole suit le **cycle réel de la voix**. Elle démarre avec la parole, peut être réutilisée tant que la parole reste active, mais doit être stoppée dès la fin/annulation/échec de la synthèse. Une parole ultérieure relance le média depuis t=0.

Cette décision remplace explicitement l’ancienne formulation qui laissait la vidéo aller à sa fin naturelle après la fin de la voix.


## GECKO-033 — RED attendu
Les tests de contrat sont volontairement ajoutés avant les classes de production. Échec attendu : références GECKO-033 non résolues, prouvant que les tests pincent bien le comportement nouveau avant implémentation.


GECKO-033 RED complémentaire : l’ordre `IntroGeckoGD.mp4` → `Gecko_Intro.mp4` est désormais verrouillé par test avant code.


## 2026-09-26 — GECKO-033 RED validé, implémentation poussée
Runs #73 (`36237890947`) et #74 (`36238120422`) ont échoué à `:app:compileDebugUnitTestKotlin` exactement sur les références GECKO-033 volontairement absentes : constantes nouveaux assets, politiques audio/ProfParle/intro/titre, Prof ambiant et catalogue 100 phrases. Le code de production est ajouté seulement après cette preuve RED. Prochaine étape : observer le run GREEN complet ; en cas d’échec, corriger la cause racine et synchroniser les cinq fichiers dans le même cycle.
