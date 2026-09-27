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


<!-- GECKO-033-PHONE-FEEDBACK-INTRO-2026-09-26 -->
## 2026-09-26 — v0.10.9-dev téléphone : régression intro
CI #75 était GREEN côté tests/build/APK/AAB, mais la validation téléphone révèle deux régressions fonctionnelles :
1. **Audio intro 1 coupé** : `IntroGeckoGD.mp4` a été absorbé par la politique « toutes vidéos Gecko muettes ». C’est incorrect pour cette intro : Fab veut son audio embarqué.
2. **Intro 2 disparue** : `Gecko_Intro.mp4` ne se lance pas après `IntroGeckoGD.mp4` sur téléphone malgré le contrat de séquence.

Important : ne pas conclure la cause racine de l’intro 2 avant audit. Le build GREEN ne valide pas la chaîne comportementale réelle sur appareil.

Décision : aucun code maintenant ; accumuler les autres retours téléphone de Fab, puis ouvrir un nouveau cycle RED ciblé.


<!-- GECKO-033-PHONE-FEEDBACK-ICON-PROF-ARBITRATION-2026-09-26 -->
## 2026-09-26 — téléphone : icônes validées, conflit d’arbitrage média non résolu
Fab valide les deux livraisons icône de v0.10.9-dev :
- launcher Android ;
- médaillon/icon in-app près du titre.

Nouveau symptôme observé : le Prof semble interrompre une animation, ou l’inverse. La capture et l’observation ne permettent pas encore d’attribuer la cause avec certitude.

Ne pas corriger à l’aveugle. Lors du prochain cycle :
1. instrumenter les demandes de lecture/arrêt ;
2. identifier le média propriétaire du host partagé ;
3. reproduire le conflit ;
4. seulement ensuite ajuster la priorité/arbitrage.

Vigilance : préserver les deux icônes validées et les règles de cycle de vie `ProfParle.mp4` déjà contractualisées.


<!-- GECKO-033-PHONE-FEEDBACK-GRID-GECKO-ANIM-2026-09-26 -->
## 2026-09-26 — v0.10.9-dev : régression animations Gecko de grille
Retour téléphone Fab : les animations Gecko sur la grille ne sont plus présentes comme attendu.

La demande GECKO-033 était seulement de **muter leur audio**, jamais de supprimer leur rendu ou leur déclenchement.

Hypothèse à auditer au prochain cycle, sans la considérer encore comme cause prouvée :
- la nouvelle politique de mute ou l’arbitrage média peut avoir affecté le chemin de lecture lui-même ;
- vérifier séparément `play`, visibilité, busy state, callback de fin et volume du player.

Référence stable à retrouver : comportement visuel pré-GECKO-033 des animations apparition/disparition/action longue, avec uniquement l’audio vidéo forcé à zéro.

Ne pas corriger avant la fin des retours téléphone en cours.


<!-- GECKO-033-MEDIA-TRACE-PROF-AFTER-INTRO1-2026-09-26 -->
## 2026-09-26 — instrumentation média avant correction
Fab constate que le Prof apparaît trop tôt et demande de conserver des logs détaillés de lancement/arrêt des vidéos pour diagnostiquer le conflit même si une cause semble rapidement trouvée.

Ce cycle est volontairement **diagnostic seulement** :
- aucune correction de priorité ;
- aucune correction de mute ;
- aucune correction d’enchaînement intro ;
- aucune correction de visibilité Prof ;
- aucune correction des animations Gecko grille.

Le tag `GeckoDokuMediaTrace` enregistre l’ordre réel des événements afin d’identifier les conflits entre INTRO, GECKO_ACTION, PROF_ACTION et PROF_SPEECH.

Nouvelle chronologie cible à respecter ensuite : Prof absent pendant la première intro ; première intro avec son ; Prof seulement après sa fin ; pas de chevauchement audio intro 1 → intro 2.


<!-- GECKO-033-PHONE-FEEDBACK-ANIM-OFF-UNEXPECTED-2026-09-26 -->
## 2026-09-26 — Anim passé OFF sans cause identifiée
Fab observe que l’habillage animé s’est retrouvé désactivé pendant les tests téléphone, sans avoir identifié l’action déclenchante.

Ne pas attribuer prématurément ce symptôme au conflit Prof/intro/Gecko. Les causes possibles incluent préférence persistée, valeur par défaut, lifecycle Android, action utilisateur involontaire ou écriture indésirable depuis un chemin d’erreur.

Prochain diagnostic :
- inventorier toutes les écritures de `RichMediaSettings.enabled` ;
- tracer chargement initial + source ;
- vérifier qu’aucun échec vidéo ne modifie ce réglage ;
- vérifier comportement après mise à jour APK et recréation Activity.
<!-- GECKO-033-AUDIT-PROF-SPEECH-GECKO-FLOW-2026-09-26 -->
## 2026-09-26 — parole Prof semblant coupée au lancement des animations
Retour initial : Pierre semble parfois se couper/se vider lorsqu'une animation démarre. Hypothèse Fab : canal son partagé.

Précision ultérieure décisive : l'animation Gecko sur sa case finit bien par arriver.

Audit :
- aucun mécanisme queue/retry dans `RichMediaOverlayView` ; si busy, la demande est perdue ;
- animation visible plus tard = demande initialement acceptée, démarrage réel possiblement retardé par `prepareAsync()` ;
- animations Gecko forcées muettes ;
- Pierre joue via `AudioTrack`, distinct du `MediaPlayer` vidéo ;
- sur action manuelle, `clearProfessorSession()` coupe explicitement `ProfessorSpeech` avant la demande apparition/disparition ;
- une nouvelle `ProfessorSpeech.speak()` coupe aussi la phrase précédente ;
- `Prof_actions` et `ProfParle` partagent bien un player, mais ce conflit est vidéo.

Particularité à ne pas mélanger :
- apparition joueur peut déclencher ensuite une action longue Gecko ;
- disparition ne déclenche pas d'action longue ;
- apparition posée par le Prof ne déclenche pas d'action longue.

Cause racine non encore déclarée tant que le scénario téléphone exact n'est pas corrélé aux logs.

<!-- GECKO-033-AUDIT-PRIORITY-ARBITRATION-2026-09-26 -->
## 2026-09-26 — nouvelle cause probable : Prof idle concurrent avec les intros
Audit du code GECKO-033 après retour Fab.

Découverte :
- l'idle Prof est planifié dans `onCreate` avant l'appel différé à `playIntroIfEnabled()` ;
- il déclenche rapidement (~2–3 s) ;
- `runProfessorIdleAnimation()` ne considère pas `richMediaOverlay.isBusy` comme blocage ;
- `playProfessorButtonVideo()` peut donc lancer `Prof_actions.mp4` pendant l'intro ;
- vidéo Prof et intro utilisent deux `ChromaKeyVideoView`, donc deux `MediaPlayer`/GLSurfaceView ;
- les deux vues utilisent `setZOrderOnTop(true)`.

Cette concurrence est plus cohérente avec « Prof apparaît trop tôt » et « intro 2 disparue » qu'une Intro 2 volontairement jouée en arrière-plan. Intro 2 est appelée seulement après le callback de fin d'Intro 1.

Autre anomalie : le × de l'overlay appelle `stop()` avec `invokeCompletion=false`; sauter Intro 1 arrête donc toute la chaîne au lieu de lancer Intro 2.

Côté voix, aucune gestion de focus audio n'a été trouvée dans `VoicePcmPlayer`, `AssetAudioPlayer` ou `ChromaKeyVideoView`. La coupure observée reste d'abord expliquée par nos `professorSpeech.stop()` explicites et la préemption de `ProfessorSpeech.speak()`.

Décision fonctionnelle Fab : Pierre ne doit jamais être interrompu par une action normale ou un média. Seuls un nouvel appui Prof et la séquence finale peuvent volontairement remplacer sa phrase, hors arrêt utilisateur/lifecycle.



<!-- GECKO-033-COEXISTENCE-RED-2026-09-26 -->
## 2026-09-26 — correction de direction avant code
L’audit précédent proposait un arbitre « une vidéo à la fois » et le différé des animations Gecko. Fab rejette explicitement cette solution car elle masque le défaut de coexistence.

Cycle RED ouvert avant toute production :
- politique de parole par origine ;
- actions normales sans arrêt voix ;
- registre de sessions vidéo indépendantes ;
- Intro 1 sonore et Prof non éligible pendant Intro 1 ;
- politique SurfaceView sans `setZOrderOnTop(true)` global.


<!-- GECKO-033-COEXISTENCE-POLICY-GREEN-2026-09-26 -->
## 2026-09-26 — GREEN policies après RED #83
Les politiques pures de coexistence et priorité parole ont été introduites après l’échec attendu de #83. Aucun arbitre mono-vidéo n’est ajouté. L’intégration runtime reste volontairement séparée pour pouvoir isoler les régressions.


<!-- GECKO-033-POLICY-COMPILE-FIX-2026-09-26 -->
## 2026-09-26 — #84 échec compile ciblé
Cause : suppression de INTRO de la branche mute sans branche explicite false dans le `when`. Correctif minimal : INTRO rejoint PROF_LONG_ACTION côté `false`. Ce changement correspond au RED « Intro 1 audible si FX ON ».


<!-- GECKO-033-SPEECH-RUNTIME-GREEN-2026-09-26 -->
## 2026-09-26 — séparation état pédagogique / bulle / transport voix
Cause racine de la coupure sur action normale : `clearProfessorSession → closeProfessorBubble → professorSpeech.stop`.
Correction : `closeProfessorBubble` n’arrête plus le moteur vocal. Les arrêts transport sont maintenant explicites et tracés par raison/caller. Les requêtes basses priorité sont rejetées plutôt que préempter la parole.


<!-- GECKO-033-MULTISESSION-RUNTIME-2026-09-26 -->
## 2026-09-26 — suppression du verrou mono-session au niveau overlay
Cause architecturale identifiée : `RichMediaOverlayView` possédait un unique `videoView`, un unique `activeKind` et rejetait tout nouveau `play()` dès `isBusy`. Cela empêchait structurellement la coexistence de plusieurs animations.

Correction du composant :
- map de sessions indépendantes ;
- vue/player par session ;
- terminaison isolée ;
- INTRO seulement séquentielle avec INTRO ;
- Z-order SurfaceView passé de OnTop global à MediaOverlay ;
- logs par couche logique.

La suppression des gardes `MainActivity.richMediaOverlay.isBusy` reste une étape séparée pour limiter le rayon de changement.


<!-- GECKO-033-COEXISTENCE-MAIN-WIRING-2026-09-26 -->
## 2026-09-26 — retrait des verrous MainActivity
Après passage de l’overlay en multi-sessions, plusieurs gardes `richMediaOverlay.isBusy` continuaient à reproduire artificiellement l’ancien modèle mono-vidéo au niveau de l’Activity.

Correction :
- suppression des guards busy pour Gecko de case et action longue ;
- suppression du blocage « Prof/bulle active » sur action longue ;
- maintien uniquement des interdictions fonctionnelles légitimes (Anim OFF, célébration, partie terminée) ;
- intro gérée par phase narrative, et non par verrou vidéo global.

La disparition précédente de `Gecko_Intro.mp4` est également couverte par un enchaînement explicite via `IntroLifecyclePolicy` et callbacks de session indépendants.


<!-- GECKO-033-INTRO-BOOT-FIX-V01010-2026-09-26 -->
## 2026-09-26 — cause subtile Prof trop tôt au boot
Le premier `createPuzzle(recordStart=true)` est appelé avant construction de `richMediaOverlay`. Le câblage initial avait placé `introPhase = DONE` dans `startPuzzle()` sans distinguer ce bootstrap d’une vraie nouvelle grille utilisateur.

Correction : terminer l’intro dans `startPuzzle()` uniquement lorsque l’overlay existe déjà. Cette condition différencie proprement bootstrap et navigation runtime.


<!-- GECKO-033-PROF-LOCAL-PREEMPTION-2026-09-26 -->
## 2026-09-26 — ProfParle absent pendant parole : exception locale clarifiée
Capture téléphone : le Prof affiche une animation visuelle dans son slot alors que la bulle pédagogique est active ; `ProfParle.mp4` n'est pas forcément celui affiché.

Fab précise que la seule préemption vidéo supplémentaire autorisée est locale :
- `ProfParle.mp4` peut interrompre `Prof_actions.mp4` au moment où Pierre commence réellement à parler ;
- cette règle ne s'étend pas aux animations Gecko.

À vérifier au prochain correctif :
- état `professorVideoMode` au `SPEAK_STARTED` ;
- si ACTION → STOP local puis START SPEECH ;
- jamais de stop sur `RichMediaOverlayView` ou sessions Gecko.


<!-- GECKO-033-PROFPARLE-LATCHED-FAILURE-HYPOTHESIS-2026-09-26 -->
## 2026-09-26 — ProfParle absent même depuis PNG
Nouvelle observation discriminante : Fab déclenche Prof depuis l'état PNG normal et `ProfParle.mp4` ne démarre toujours pas.

Audit code :
`startProfessorSpeechVideo()` contient une garde sur `professorSpeechVideoFailed`.
Le callback erreur de cette même vidéo met ce flag à true de manière persistante.

Conclusion de diagnostic : hypothèse forte d'un latch d'échec permanent après une première erreur vidéo. Cette hypothèse doit être confirmée par `GeckoDokuMediaTrace` / erreurs MediaPlayer avant correction.

Ne pas confondre avec la priorité locale PROF_SPEECH > PROF_ACTION, qui reste correcte mais n'explique pas le cas PNG.


<!-- GECKO-033-ACTIVE-MISSION-RESET-SPRITES-2026-09-26 -->
## 2026-09-26 — validation intros et réinitialisation de la mission active
Retour téléphone Fab sur v0.10.10-dev :
- Intro 1 : parfaite ;
- audio Intro 1 : correct ;
- Intro 2 : parfaite ;
- enchaînement Intro 1 → Intro 2 : correct.

Ces points sont désormais considérés VALIDÉS et GELÉS.

Le problème actif devient exclusivement la famille animations/sprites vidéo, jugée « quasi KO » :
- rectangle noir derrière vidéo Prof ;
- rectangle noir derrière vidéo Gecko de grille ;
- animation ProfParle non fiable ;
- animations Gecko de gameplay non fiables ;
- composition multi-surfaces/chroma/Z-order à auditer.

Pour réduire le bruit documentaire, les fichiers vivants `ordres-de-mission.md`, `brain.md`, `brainmap.md` et `todo.md` ont été réinitialisés autour de cet état courant. `debughistorical.md` conserve l'historique détaillé antérieur.

Aucun code de production modifié dans ce cycle.


<!-- GECKO-033-SPRITES-RED-TRANSPARENCY-RETRY-2026-09-26 -->
## 2026-09-26 — correction d'une hypothèse technique du brain
L'interdiction précédente de `setZOrderOnTop(true)` venait d'une hypothèse de conflit SurfaceView, pas d'une exigence utilisateur.

Après passage à `setZOrderMediaOverlay(true)`, le test téléphone v0.10.10-dev montre des rectangles noirs derrière les vidéos Prof et Gecko, alors que le shader clear alpha=0, EGL alpha=8 et PixelFormat.TRANSLUCENT sont déjà présents.

Ruling : le contrat durable est « transparence + coexistence », pas « MediaOverlay à tout prix ». Le RED exige maintenant une stratégie de composition transparente, tout en conservant les lecteurs/sessions indépendants.

Deuxième RED : un `professorSpeechVideoFailed` ancien ne doit pas bloquer la phrase suivante.


<!-- GECKO-033-PROFPARLE-RETRY-GREEN-2026-09-26 -->
## 2026-09-26 — correction du latch ProfParle
RED #94 : `ProfessorSpeechVideoStartPolicy` manquante.

Cause corrigée : `professorSpeechVideoFailed` était utilisé comme garde persistante dans `startProfessorSpeechVideo()`. Un seul onError condamnait donc toutes les phrases suivantes.

Correction : policy de démarrage fondée uniquement sur l'état courant ; ancien échec conservé pour log mais ignoré comme verrou. Le flag est réinitialisé au prochain démarrage vidéo réussi.


<!-- GECKO-033-TRANSPARENT-SURFACE-GREEN-2026-09-26 -->
## 2026-09-26 — RED #95 confirme la régression de composition
Après GREEN retry ProfParle, la suite compile et exécute 43 tests. Un seul échoue :
`transparentKeyedSurfaceUsesTopCompositionInsteadOfOpaqueMediaOverlay`.

Le code avant régression GECKO-033 utilisait directement `setZOrderOnTop(true)`, en plus de PixelFormat.TRANSLUCENT et EGL alpha. La refonte coexistence avait remplacé cela par MediaOverlay.

Correction : restaurer OnTop pour l'alpha tout en gardant la nouvelle architecture multi-sessions. Ceci sépare clairement deux dimensions qui avaient été confondues : composition SurfaceView et arbitrage des players.


<!-- GECKO-033-SPRITES-V01011-VERIFY-2026-09-26 -->
## 2026-09-26 — TDD sprites jusqu'au GREEN #96
- Run #94 : RED attendu, compilation test échoue sur `ProfessorSpeechVideoStartPolicy` absente.
- Après implémentation retry, run #95 : tests exécutés, 43 tests dont un seul RED sur la politique SurfaceView.
- Après restauration de la composition transparente, run #96 : SUCCESS complet, y compris tests, assembleDebug, bundleDebug et upload artifact.

Le rendu réel noir/transparence reste à confirmer sur téléphone.


<!-- GECKO-033-WORKING-STATE-CONSOLIDATED-2026-09-26 -->
## 2026-09-26 — consolidation de ce qui fonctionne

Cette entrée distingue volontairement trois niveaux : **validé téléphone**, **actif et couvert par le code/tests**, et **preuve CI**.

### Validé explicitement sur téléphone par Fab
- Intro 1 `IntroGeckoGD.mp4` : affichage correct.
- Son embarqué Intro 1 : correct.
- Intro 2 `Gecko_Intro.mp4` : correcte.
- Enchaînement Intro 1 → Intro 2 : correct.
- Icône launcher GeckoDoku : validée.
- Médaillon/icon près du titre : validé.

Après la livraison de v0.10.11-dev, Fab a répondu « Parfait » puis a demandé de vider l'ordre de mission. Ce retour est enregistré comme **retour global positif sur la candidate actuelle**, sans inventer de détail de test non formulé séparément.

### Bons fonctionnements toujours actifs dans le code
Vérification croisée sur le HEAD courant :
- moteur `GameEngine` : croix, Gecko, hypothèses, marqueurs, erreurs, auto-crosses ;
- génération 5×5 à 12×12 avec unicité et validation logique ;
- `HumanSolver` : singles, région verrouillée, projection, Gecko X-Wing ;
- `HypothesisSolver` : contradiction bornée pour Mission Impossible/Infernal ;
- 8 niveaux de difficulté actifs ;
- `ProfessorGecko` : trace cachée si encore valide, recalcul sinon, explication et étape appliquable ;
- sauvegarde/rejouer/journal persistant ;
- statistiques locales et par difficulté ;
- distinction des parties assistées par Prof ;
- bulle Prof overlay sans reflow de grille ;
- célébration procédurale + audio ;
- Pierre local + fallback Android ;
- priorité des origines de parole ;
- interventions ambiantes ;
- encouragements enregistrés + Pierre ;
- multi-sessions vidéo indépendantes ;
- animations Gecko gameplay forcées muettes ;
- retry `ProfParle.mp4` après erreur ponctuelle ;
- instrumentation `GeckoDokuMediaTrace`.

### Corrections GECKO-033 qui ont techniquement réussi
- séparation fermeture de bulle / arrêt transport de Pierre ;
- actions normales joueur ne coupent plus la voix ;
- suppression du verrou global mono-session vidéo ;
- intro 1 devenue sonore lorsque FX ON ;
- séquence Intro 1 → Intro 2 restaurée ;
- Prof non présenté pendant Intro 1 ;
- bootstrap initial corrigé pour ne pas annuler IntroPhase.FIRST ;
- latch permanent `professorSpeechVideoFailed` supprimé comme condition de refus ;
- retry vidéo ProfParle autorisé au speech suivant ;
- stratégie Surface chroma restaurée en `ZOrderOnTop` avec alpha/translucide ;
- multi-sessions conservées malgré ce changement de composition.

### Preuves CI
- #94 : RED attendu sur policy retry absente.
- #95 : 43 tests exécutés, un seul RED sur la Surface transparente.
- #96 : SUCCESS complet.
- #97 : SUCCESS complet v0.10.11-dev : tests unitaires, APK, AAB, artifact.

### Règle historique ajoutée
Un succès confirmé doit être conservé dans `brain.md` comme invariant actif. Un bug corrigé reste dans `debughistorical.md` avec sa cause et sa correction. Une future mission ne doit pas effacer ces acquis simplement parce qu'elle cible une autre zone du projet.


<!-- GECKO-034-RED-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## 2026-09-26 — GECKO-034 RED
Audit avant code :
- l'overlay passe VISIBLE avant la première frame ;
- `ChromaKeyVideoView.onPrepared` appelle le démarrage sans gate de première frame ;
- la grille est encore directement ajoutée au `LinearLayout` en `height=0, weight=1f`, donc une variation d'un sibling peut modifier sa hauteur.

Décision : tester séparément le gate de frame et la géométrie de grille avant production.


<!-- GECKO-034-GREEN-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## 2026-09-26 — GECKO-034 après RED #103
RED #103 : échec attendu uniquement sur FirstFrameVisibilityGate / BoardGeometryPolicy absents.

Correction : gate de frame fraîche réellement dessinée OpenGL, abort avant révélation, masks/portrait synchronisés ; grille réelle sortie du LinearLayout weight=1 vers couche flottante screenRoot avec ancre de réservation et géométrie figée par fenêtre.


<!-- GECKO-034-V01012-CANDIDATE-2026-09-26 -->
## 2026-09-26 — preuve GREEN GECKO-034
Run #104 : SUCCESS complet après le correctif première frame + board flottante. Tests, assembleDebug, bundleDebug et upload artifact réussis.


<!-- GECKO-035-RED-2026-09-26 -->
## 2026-09-26 — GECKO-035 RED
Avant production, ouverture d'un RED sur priorité voix, habillage de toutes les origines, bouton !, menu ⚙️ et journal persistant. L'ancien verrou `previousAttemptFailed` reste explicitement interdit.


<!-- GECKO-035-GREEN-POLICIES-2026-09-26 -->
## 2026-09-26 — GECKO-035 policies
Après RED #108, ajout des policies pures avant câblage Android.


<!-- GECKO-035-GREEN-LOG-2026-09-26 -->
## 2026-09-26 — journal persistant
Ajout d'un sink fichier borné et tolérant aux erreurs en complément de Logcat.


<!-- GECKO-035-GREEN-SPEECH-GATE-2026-09-26 -->
## 2026-09-26 — admission voix avant vidéo
Ajout de canAccept pour éviter qu'un média se prépare pour une parole qui serait ensuite rejetée occupée.


<!-- GECKO-035-GREEN-FIRST-FRAME-HOLD-2026-09-26 -->
## 2026-09-26 — frame tenue
Le gate anti-flash est étendu avec un mode hold : première frame consommée OpenGL mais alpha 0 + player pause, puis révélation explicite au départ audio.


<!-- GECKO-035-GREEN-RUNTIME-2026-09-26 -->
## 2026-09-26 — GECKO-035 runtime câblé
Le démarrage antérieur (voix puis vidéo) est remplacé pour les nouvelles demandes MainActivity par un pré-roll de ProfParle. Le fallback est volontairement un échec visuel seulement. Aucune callback vidéo n'appelle ProfessorSpeech.stop().


<!-- GECKO-035-V01013-CANDIDATE-2026-09-26 -->
## 2026-09-26 — candidate GECKO-035
#108 RED attendu. Étapes intermédiaires et #114 GREEN complet avant bump. Candidate v0.10.13-dev code 24 préparée pour CI finale.


<!-- GECKO-035-CI115-GREEN-2026-09-26 -->
## 2026-09-26 — #115 GREEN
Candidate v0.10.13-dev : tests + APK + AAB + artifact réussis. Ne pas confondre cette preuve CI avec une validation téléphone de la synchro audio/vidéo.


<!-- GECKO-036-PNG-CONTINUITY-MISSION-2026-09-27 -->
## 2026-09-27 — trou visuel pendant pré-roll ProfParle
Retour téléphone Fab sur v0.10.13-dev : le délai de synchronisation est jugé correct, mais pendant ce délai le Prof peut parfois ne plus avoir aucune image visible.

Diagnostic fonctionnel : la transition PNG → vidéo masque parfois le fallback statique avant que la vidéo ne soit effectivement révélée.

Correction demandée : conserver `Prof.png` jusqu'à la réussite effective de `revealHeldFirstFrame()`.


<!-- GECKO-036-QUICK-TALK-BUBBLE-2026-09-27 -->
## 2026-09-27 — QUICK_TALK affiché au mauvais endroit
Captures téléphone : les phrases du bouton `!` apparaissent dans le texte de statut au-dessus de la grille au lieu de la bulle Prof.

Cause code vérifiée :
`speakQuickProfessorLine()` écrit actuellement
`status.text = "Prof Gecko • " + line`.

Piège identifié avant correction :
`showProfessorBubble(message)` ne fait pas qu'afficher ; elle appelle aussi `speakWithProfessorVisual(... PROF_BUTTON)`.
La réutiliser telle quelle pour QUICK_TALK provoquerait potentiellement une seconde demande de parole.

Correction attendue : séparer affichage de bulle et transport vocal, puis afficher la phrase dans la bulle tout en conservant une seule parole QUICK_TALK.


<!-- GECKO-036-RED-2026-09-27 -->
## 2026-09-27 — GECKO-036 RED
Le log téléphone a révélé des FIRST_FRAME_HELD possibles avant START, compatible avec une frame SurfaceTexture résiduelle. RED ajouté pour interdire toute frame antérieure au START + VIDEO_RENDERING_START du playback courant.


<!-- GECKO-036-GREEN-POLICIES-2026-09-27 -->
## 2026-09-27 — policies GREEN
Après RED #120, ajout des trois policies pures avant câblage Android.


<!-- GECKO-036-GREEN-RUNTIME-2026-09-27 -->
## 2026-09-27 — correction frame résiduelle / -38
Le renderer était armé dès PLAY_REQUEST et le log montrait parfois FIRST_FRAME_HELD avant START. Correction : génération par playback, armement seulement après START + VIDEO_RENDERING_START, purge de frameAvailable à l'armement, callback taggé génération. UI : PNG conservé pendant toutes les transitions et QUICK_TALK déplacé dans la bulle sans double parole.


<!-- GECKO-036-V01014-CANDIDATE-2026-09-27 -->
## 2026-09-27 — candidate GECKO-036
#120 RED attendu ; #121 GREEN policies ; #122 GREEN runtime. Candidate v0.10.14-dev code 25 préparée pour validation finale CI puis téléphone.


<!-- GECKO-036-CI123-GREEN-2026-09-27 -->
## 2026-09-27 — #123 GREEN
Candidate v0.10.14-dev : tests + APK + AAB + artifact réussis après correction continuité PNG, génération de frame et bulle QUICK_TALK.


<!-- GECKO-036-FRAME-SERIAL-RED-2026-09-27 -->
## 2026-09-27 — régression figée Intro/Prof
v0.10.14-dev téléphone : INTRO et ProfParle peuvent recevoir VIDEO_RENDERING_START sans VIDEO_FIRST_FRAME. Pour ProfParle, plusieurs générations finissent en timeout 900 ms. Cause probable localisée : `armFirstFrameNotification()` force `frameAvailable=false`, créant une course où une vraie frame déjà signalée est perdue avant `updateTexImage()`.


<!-- GECKO-036-FRAME-SERIAL-GREEN-2026-09-27 -->
## 2026-09-27 — cause affinée et correction du gel
RED #125 confirme le nouveau contrat absent.
La première correction GECKO-036 avait introduit une purge booléenne `frameAvailable=false` à l'armement. Les logs téléphone ont montré INTRO et ProfParle bloqués après VIDEO_RENDERING_START, avec timeouts répétés. La purge est supprimée : toutes les frames sont consommées, la fraîcheur utilise désormais un serial monotone. INTRO n'est plus armée par ce gate.


<!-- GECKO-036-V01015-CANDIDATE-2026-09-27 -->
## 2026-09-27 — candidate v0.10.15-dev
#125 RED attendu sur FreshFrameSerialGate / FirstFrameGateActivationPolicy. #126 GREEN complet après suppression de la purge frameAvailable et passage au serial monotone. Candidate code 26 préparée.


<!-- GECKO-036-CI127-GREEN-2026-09-27 -->
## 2026-09-27 — #127 GREEN
Le remplacement de la purge booléenne par serial produit/consommé et l'exclusion des intros du gate passent la CI complète en v0.10.15-dev.


<!-- GECKO-036-FINAL-VALIDATION-2026-09-27 -->
## 2026-09-27 — clôture GECKO-036 validée téléphone

Historique final :
- v0.10.14-dev avait introduit une régression de gel possible sur Intro/Prof en purgeant `frameAvailable=false` au moment d'armer le gate.
- Les logs téléphone montraient `VIDEO_RENDERING_START` sans `VIDEO_FIRST_FRAME`, puis des timeouts ProfParle répétés.
- Le correctif v0.10.15-dev remplace cette purge par un serial monotone produit/consommé et laisse les intros hors gate.
- Les transitions Prof conservent le PNG jusqu'à révélation réelle.
- QUICK_TALK a été déplacé dans la bulle sans double parole.
- CI #126, #127 et #128 : GREEN.
- Fab confirme ensuite que, pour lui, tout est corrigé.

Conclusion : GECKO-036 est clôturé. Les comportements ci-dessus deviennent des non-régressions permanentes.


<!-- GECKO-037-LIVING-PROF-MISSION-2026-09-27 -->
## 2026-09-27 — ouverture GECKO-037 « Prof Gecko vivant »

Fab fournit un ordre complet pour remplacer la sélection quasi-aléatoire du small talk par :
- catalogue structuré 309 phrases ;
- IDs stables ;
- mémoire individuelle 48 h persistante ;
- lastPhraseId persistant ;
- sélection contextuelle pondérée ;
- contexte mastery/impulsivity/momentum ;
- mood caché et décroissant ;
- fallback oldest-first ;
- auto-fermeture sûre des petites bulles 1 s après vraie fin de parole.

Le corpus fourni est cohérent :
200 V2 + 4 FAB + 5 TAQUIN = 209 nouvelles ; avec 100 historiques = 309.

Aucun code n'est modifié lors de cette ouverture de mission.


<!-- GECKO-037-REMOVE-RECORDED-ENCOURAGEMENTS-2026-09-27 -->
## 2026-09-27 — retrait demandé des encouragements MP3
Audit : le runtime possède encore une alternance via `EncouragementSourcePolicy` entre RECORDED et PIERRE. La branche RECORDED choisit un des 13 segments de `Voix_encouragements.mp3` via `EncouragementSelector` puis `AssetAudioPlayer.playVoiceSegment()`.

Fab demande la suppression complète de ce système enregistré afin de converger vers Pierre + le nouveau moteur contextuel GECKO-037.

RED ajouté avant suppression.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-GREEN-2026-09-27 -->
## 2026-09-27 — encouragements enregistrés supprimés
RED #131 a confirmé le contrat absent.
L'ancien chemin alternait aléatoirement entre 13 segments du master `Voix_encouragements.mp3` et Pierre. Ce doublon est retiré : plus de source RECORDED ni de selector de segments. Le runtime utilise Pierre uniquement, sans modifier les autres musiques.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-CI132-2026-09-27 -->
## 2026-09-27 — #132 GREEN
Suppression complète du chemin enregistré d'encouragement validée par CI : tests, APK, AAB et artifact réussis.


<!-- GECKO-037-FULL-RED-2026-09-27 -->
## 2026-09-27 — RED global Prof vivant
Audit confirmé : smalltalk actuel utilise encore PierreSmallTalkSelector/Random ; SharedPreferences est déjà le pattern local du projet ; ProfessorSpeech expose un callback onCompletion réel ; SpeechOrigin QUICK_TALK/AMBIENT/ENCOURAGEMENT sont non préemptifs. RED global posé avant migration.


<!-- GECKO-037-GREEN-MODEL-MOOD-BUBBLE-2026-09-27 -->
## 2026-09-27 — GREEN 1 GECKO-037
Après RED #134, première tranche pure : modèles, context tracker, mood policy et close policy. Aucun câblage MainActivity dans ce commit.


<!-- GECKO-037-GREEN-CATALOG-309-2026-09-27 -->
## 2026-09-27 — catalogue 309 créé
Le corpus a été généré depuis les 100 chaînes historiques du dépôt et les 209 lignes ID/text de l'ordre GECKO-037. Vérification pré-écriture : 100 + 209, 209 IDs modernes uniques, 309 textes uniques après normalisation.


<!-- GECKO-037-GREEN-HISTORY-SELECTOR-2026-09-27 -->
## 2026-09-27 — GREEN 3 history/selector
Le vieux anti-repeat par index est remplacé fonctionnellement par lastPhraseId persistant + cooldown par ID. Le fallback n'efface jamais la mémoire globale et force la phrase compatible la plus ancienne.


<!-- GECKO-037-RUNTIME-GREEN-2026-09-27 -->
## 2026-09-27 — GREEN 4 runtime GECKO-037
Remplacement runtime des anciens Random.nextInt smalltalk/PierreEncouragements par le selector 309. Ajout classification légère de l'erreur par délai de réflexion, logs PROF_CONTEXT/POOL/SELECTED/FALLBACK/FORCED_OLDEST et fermeture de bulle basée sur vraie completion Pierre.


<!-- GECKO-037-RUNTIME-CLEANUP-2026-09-27 -->
## 2026-09-27 — nettoyage runtime GECKO-037
Après #138 GREEN, suppression des champs morts issus du système quicktalk historique afin d'éviter une réactivation accidentelle du tirage par index.


<!-- GECKO-037-V01016-CANDIDATE-2026-09-27 -->
## 2026-09-27 — candidate GECKO-037
#134 RED global attendu, #137 GREEN cœur, #138 GREEN runtime. Candidate v0.10.16-dev / code 27 préparée après nettoyage des anciens selectors runtime.


<!-- GECKO-037-CI140-GREEN-2026-09-27 -->
## 2026-09-27 — GECKO-037 candidate CI #140 GREEN

La candidate v0.10.16-dev / code 27 passe la CI complète après :
- RED global #134 ;
- cœur 309/history/selector/context/bubble GREEN #137 ;
- runtime vivant GREEN #138 ;
- retrait des anciens selectors runtime ;
- versionnage candidate.

Tests, APK, AAB et artifact sont produits. Aucun changement du pipeline vidéo GECKO-036 n'a été requis pour GECKO-037.

Correction documentaire : après le câblage GREEN 4, les encouragements ne passent plus par `PierreEncouragements` mais par le selector contextuel 309.


<!-- GECKO-037-TECHNICAL-CLOSE-2026-09-27 -->
## 2026-09-27 — clôture technique GECKO-037

GECKO-037 a été implémenté par étapes TDD :
- #134 : RED global attendu ;
- #137 : cœur GREEN ;
- #138 : runtime GREEN ;
- #139 : suppression des anciens sélecteurs aléatoires ;
- #140 : candidate v0.10.16-dev GREEN avec APK/AAB ;
- #141 : documentation candidate GREEN.

Le système 309 phrases, mémoire 48 h, lastPhraseId, contexte joueur, mood adaptatif, rareté, oldest-first, bulle synchronisée et auto-close sécurisé est présent.

L’ancien chemin d’encouragement enregistré MP3 a été supprimé auparavant avec #131 RED puis #132/#133 GREEN.

Aucun bug téléphone GECKO-037 n’est déclaré corrigé sans retour explicite de Fab. La mission de code est néanmoins techniquement close ; les observations téléphone futures deviennent des retours de validation ou de nouvelles missions.

<!-- GECKO-038-DESIGN-SUDOKU-2026-09-27 -->
## 2026-09-27 — conception documentée du second mode Sudoku

Décision produit de Fab :
- GeckoDoku doit pouvoir accueillir un vrai Sudoku 9×9 comme second mode ;
- aucun code n'est autorisé dans cette intervention ;
- la priorité est de préparer une architecture anti-régression avant toute implémentation.

Décisions documentées :
- séparation GameMode (GECKODOKU / SUDOKU) et VisualStyle ;
- trois styles Sudoku : Classic, Gecko NB, Gecko Coloré ;
- sélecteur tactile unique à 3 crans avec preview live pendant le glissement et validation au relâchement ;
- couche visuelle on-top click-through pour protéger les hitboxes / comportements historiques ;
- événements bindés de manière stable puis routés par GameMode plutôt qu'unbind/rebind global ;
- le renderer de case vide historique reste spécifique à GeckoDoku et ne fuit pas vers Sudoku ;
- moteur Sudoku séparé du moteur GeckoDoku ;
- Pierre / moteur 309 / mood restent des services communs ;
- le brain devient explicitement la mémoire fonctionnelle canonique du produit et doit pouvoir alimenter une future documentation.

Assets vérifiés présents sur `main` :
- `assets/gecko/PlancheGeckoDeNombreNB.png`
- `assets/gecko/PlancheGeckoDeNombreColored.png`

Ils ne sont pas encore présents sur la branche `gecko-033-identity-prof-life` au moment de cette décision.
Contrat : aucune conversion ni recompression de ces PNG lors de la future intégration.

Aucun fichier source applicatif n'a été modifié par cette intervention documentaire.



<!-- GECKO-038-RED-START-2026-09-27 -->
## 2026-09-27 — GECKO-038 lancé
GO explicite de Fab. Branche `gecko-038-sudoku-mode` créée depuis la spécification. RED ajouté avant toute classe Sudoku : moteur, génération unique, hint sans hasard, séparation GameMode/VisualStyle. Le workflow CI est étendu à la branche. Échec attendu avant GREEN.


<!-- GECKO-038-CORE-GREEN-2026-09-27 -->
## 2026-09-27 — RED #145 puis cœur GREEN candidat
CI #145 échoue volontairement après ajout des contrats RED. Implémentation du cœur pur sans toucher GeckoBoardView : moteur Sudoku distinct, génération unique via countSolutions(limit=2), notes et historique undo/redo, hints sans guessing. Prochaine étape : CI cœur puis UI isolée.


<!-- GECKO-038-CORE-COMPILE-FIX-2026-09-27 -->
CI #146 : compilation stoppée dans SudokuGameEngine car Kotlin avait inféré `MutableList<LinkedHashSet<Int>>`. Restore produisait `MutableList<MutableSet<Int>>`. Correction : typer explicitement sur l'interface mutable. Aucun code historique touché.


<!-- GECKO-038-UI-SURFACE-2026-09-27 -->
## 2026-09-27 — surface UI Sudoku isolée
CI cœur #147 GREEN confirmée. Ajout d'une vue Sudoku séparée, sans modification de GeckoBoardView. Le rendu des valeurs est dans un overlay qui refuse les touches. Le sélecteur tactile 3 crans produit preview pendant MOVE et commit au UP. Les deux PNG sont repris via leurs blobs Git de main, donc octets inchangés.


<!-- GECKO-038-ASSET-CATALOG-2026-09-27 -->
Pré-CI surface : constantes AssetMediaCatalog ajoutées pour les deux PNG, correction de câblage uniquement.


<!-- GECKO-038-MAIN-WIRING-2026-09-27 -->
## 2026-09-27 — câblage MainActivity GECKO-038
Choix anti-régression : GeckoBoardView n'est pas modifié. MainActivity garde le puzzle/engine historiques et un puzzle/engine Sudoku parallèle. La bascule est un routeur de visibilité, pas un unbind/rebind des gestes Gecko. Stats/Sauver/Journal Gecko sont masqués en Sudoku ; l'offre ambient Sauver est neutralisée en Sudoku. La complétion et la difficulté Prof sont routées selon GameMode.


<!-- GECKO-038-CI150-CONTRACT-2026-09-27 -->
CI #150 : compilation Kotlin réussie, 77 tests exécutés, 1 échec de contrat historique Gecko035ContractTest car il comparait exactement 3 entrées Réglages. Ce test est mis à jour pour le nouveau contrat à 4 entrées ; aucun correctif runtime requis.


<!-- GECKO-038-CANDIDATE-0110-2026-09-27 -->
## 2026-09-27 — CI #151 GREEN et candidate 0.11.0-dev
Le câblage MainActivity, le sélecteur 3 états, les assets canoniques, les contrôles Sudoku et le Prof passent tests/build APK/AAB. Version candidate portée à 0.11.0-dev code 28. Ajustement mineur : chaque mode restaure sa difficulté active lors de la bascule.


<!-- GECKO-038-SPRITE-CROP-2026-09-27 -->
Inspection visuelle des sources : découpe 5 colonnes égales sur NB coupait/contaminait notamment 3/4/7. Correction uniquement dans SudokuNumberSheetLayout. Planche couleur : bottom inset augmenté pour exclure les chiffres imprimés sous les geckos. Aucun octet asset modifié.


<!-- GECKO-038-CROP-SYNTAX-FIX-2026-09-27 -->
CI #153 : échec compilation ligne 515, accolade surnuméraire introduite dans le patch de crop. Diagnostic immédiat ; suppression de cette seule accolade.


<!-- GECKO-038-CI154-GREEN-2026-09-27 -->
## 2026-09-27 — GECKO-038 CI #154 GREEN
Après #153 (accolade locale du helper crop), #154 passe tests, assembleDebug, bundleDebug et artifact upload. Les blobs des deux planches restent exactement ceux de main : Colored 188cb2cf8a7a4c43b5118337bcfad3d969a34040 ; NB d507a670299733ff0b6859774cd1d5c2e334ecd5. Candidate 0.11.0-dev prête pour validation téléphone, sans merge ni release.

<!-- GECKO-038-CANONICAL-CLOSE-2026-09-27 -->
## 2026-09-27 — consolidation GECKO-038 et vidage ordre de mission

À la demande de Fab, l'ordre de mission GECKO-038 est clôturé et vidé après transfert de son contenu utile dans `brain.md`.

État conservé :
- branche `gecko-038-sudoku-mode` ;
- candidate 0.11.0-dev code 28 ;
- SHA code GREEN `50436317dcc4be3dadbede22931a1bf3eb40f936` ;
- CI #154 GREEN tests/APK/AAB/artifact ;
- aucun merge main ;
- aucune release ;
- validation téléphone encore en attente.

Cette intervention est strictement documentaire : aucun fichier source applicatif, asset, build ou test n'est modifié.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-RED-2026-09-27 -->
## 2026-09-27 — issue intemporelle Pierre ↔ bulle ouverte

Symptôme téléphone : les propositions automatiques d'aide/sauvegarde sont prononcées par Pierre mais apparaissent intégralement dans la ligne status, pas dans ProfessorBubbleView.

Cause confirmée : `speakProfessorAmbient()` appelle directement `speakWithProfessorVisual(... AMBIENT)` puis écrit le message dans `status.text`. Audit global : `announcePlayerStats()` présente le même défaut avec `SpeechOrigin.STATS`.

Les chemins via `speakLivingProfessor()` et `showProfessorBubble()` sont déjà conformes et doivent rester inchangés dans leur sémantique.

RED ajouté avant correction via `ProfessorSimpleSpeechCoordinatorTest`.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-GREEN-CODE-2026-09-27 -->
## 2026-09-27 — GREEN code de l'issue intemporelle

RED #155 confirmé : compilation des tests stoppée uniquement sur l'absence attendue de `ProfessorSimpleSpeechCoordinator`.

Correction :
- ajout du coordinator pur ;
- factorisation du chemin simple dans MainActivity ;
- `speakProfessorAmbient()` route désormais vers la vraie bulle ;
- `announcePlayerStats()` route désormais vers la vraie bulle ;
- `speakLivingProfessor()` réutilise le même chemin au lieu de dupliquer token/bulle/timer ;
- ajout de `onRejected` à `speakWithProfessorVisual()` pour couvrir le refus tardif après préparation ProfParle ;
- aucun changement des priorités `ProfessorSpeechRequestPolicy` ;
- aucun changement de `ProfessorAmbientPolicy` ;
- aucun changement de `GeckoBoardView`, SudokuEngine, géométrie ou médias.

Audit avant correction : 5 occurrences de `speakWithProfessorVisual(` dans MainActivity (appel pédagogique, ambient défectueux, living conforme, définition helper, stats défectueux). Après factorisation, les appels runtime se réduisent au pédagogique + chemin simple commun ; la définition helper reste unique.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-CI156-GREEN-2026-09-27 -->
## 2026-09-27 — issue intemporelle GREEN #156

Résultat :
- branche : `gecko-038-sudoku-mode` ;
- HEAD de départ : `e854a6d8096a5b16227f38e0d3f998075e9e5fe6` ;
- RED : `defaf7692d1b12a09001b3411fcb3e7d3ce5bbe6`, CI #155 failure attendue ;
- code corrigé : `2935bd8566d32650758937b1ece1693f2b5f5bd2` ;
- CI #156 : GREEN complet ;
- artifact Android produit : `GeckoDoku-v0.11.0-dev-Android` ;
- digest artifact : `sha256:85ed88767ce41cc5adf842d50cea5e8621801dc9f67fc758a4120d1cdbc6b4eb`.

Audit final :
A. `showProfessorBubble()/PROF_BUTTON` : conforme, pédagogique persistante.
A. `speakLivingProfessor()/QUICK_TALK` : conforme via helper simple.
A. `speakLivingProfessor()/AMBIENT 309` : conforme via helper simple.
A. `speakLivingProfessor()/ENCOURAGEMENT` : conforme via helper simple.
A. réactions d'erreur Gecko/Sudoku : conformes via helper simple.
A. `RETURN_AFTER_PAUSE` : conforme via helper simple.
A. `END_GAME` Sudoku : conforme via helper simple.
D corrigé. `speakProfessorAmbient()/AMBIENT` : aide + Sauver désormais dans la bulle.
D corrigé. `announcePlayerStats()/STATS` : narration désormais dans la bulle.
C. Les autres `status.text` audités sont des états UI non parlés ou des libellés courts, légitimes.

Aucun quatrième chemin de parole sans bulle n'a été identifié dans l'architecture runtime auditée.

<!-- ISSUE-INTEMPORELLE-PROF-BUBBLE-PHONE-VALIDATED-2026-09-27 -->
## 2026-09-27 — validation téléphone finale

Fab confirme le test téléphone : **OK**.

L'issue intemporelle Pierre ↔ bulle est considérée corrigée et validée en conditions réelles.

Aucune modification de code lors de cette clôture documentaire.
Aucun merge `main`.
Aucune release créée.

<!-- GECKO-038-SUDOKU-TACTILE-UX-MISSION-2026-09-27 -->
## 2026-09-27 — retour téléphone UX Sudoku après candidate

Observation Fab sur téléphone :
- Sudoku 9×9 visible et jouable visuellement ;
- Notes / Effacer / Undo / Redo visibles ;
- gros bouton `FACILE` visible ;
- pavé 1–9 non visible ;
- sélecteur 3 états non visible dans la zone utile.

Décision :
- déplacer le choix Difficulté dans ⚙️ ;
- ne pas considérer cette suppression comme suffisante : la géométrie doit réserver explicitement l'espace du pavé et du sélecteur ;
- ajouter à terme un appui long sur une case ouvrant une palette locale de saisie ;
- afficher jusqu'à 9 mini-candidats dans chaque case en 3×3 ;
- les mini-candidats suivent le VisualStyle actif ;
- Prof Gecko utilise le même système de candidats que le joueur, comme outil pédagogique ;
- aucune modification de code dans cette intervention documentaire.

Attention historique : une interprétation temporaire « appui long hors grille » a été écartée après clarification de Fab. La décision canonique concerne les **mini-candidats dans les cases de la grille** et l'appui long contextuel sur une case Sudoku.



<!-- GECKO-038-TACTILE-RED-2026-09-27 -->
## 2026-09-27 — lancement code UX tactile
GO Fab sur `gecko-038-sudoku-mode`, HEAD `baac7df072786bad770b1b8236aada091390744f`. Audit : le masquage pavé/selector provient du freeze géométrique partagé entre modes, pas du pavé lui-même. RED ajouté pour géométrie par mode, grille candidats 3×3, palette long press/placement, Prof candidats transitoires et Difficulté dans Settings.


<!-- GECKO-038-TACTILE-GREEN-CORE-2026-09-27 -->
Bloc GREEN core ajouté avant câblage Activity : aucune modification GeckoBoardView. Les nouvelles classes sont isolées et testables ; la palette ne contient ni difficulté ni réglages globaux.


<!-- GECKO-038-TACTILE-INPUT-SETTINGS-2026-09-27 -->
Premier sous-bloc runtime : long press détectable et difficulté déclarée dans les réglages Sudoku. Aucun câblage Activity/popup encore dans ce commit.


<!-- GECKO-038-TACTILE-STYLED-CANDIDATES-2026-09-27 -->
Suppression du rendu de notes Classic dans SudokuBoardView : une seule chaîne de rendu gère maintenant valeurs + candidats stylés. PNG canoniques inchangés.


<!-- GECKO-038-TACTILE-MAIN-WIRING-2026-09-27 -->
Correction de la cause téléphone appliquée : le freeze géométrique n'est plus partagé entre GECKODOKU et SUDOKU. DifficultyButton GONE en Sudoku ; Settings ajoute Difficulty. Popup long press est un PopupWindow overlay et n'affecte pas le root layout. Given = blocage sûr. Les candidats Prof sont nettoyés dès qu'une action joueur modifie la grille.


<!-- GECKO-038-TACTILE-RECT-IMPORT-2026-09-27 -->
Pré-CI : import Rect manquant détecté après déplacement du renderer de candidats ; correction locale uniquement.


<!-- GECKO-038-TACTILE-GEOMETRY-RESET-2026-09-27 -->
CI #161 : deux appels historiques reset() ne correspondaient plus au routeur multi-mode ; méthode de compatibilité ajoutée, sans modifier MainActivity.


<!-- GECKO-038-TACTILE-CANDIDATE-0111-2026-09-27 -->
## 2026-09-27 — candidate tactile 0.11.1-dev
Après CI #163 GREEN complet, version bump 28→29 et 0.11.0-dev→0.11.1-dev pour distinguer clairement l'APK tactile. Aucun changement fonctionnel dans ce commit.


<!-- GECKO-038-TACTILE-CI164-GREEN-2026-09-27 -->
## 2026-09-27 — CI #164 GREEN
La candidate tactile 0.11.1-dev passe le workflow complet : tests unitaires, assembleDebug, bundleDebug, renommage APK/AAB, upload artifact. Aucun merge main, aucune release. Les failures #157-#162 correspondent au cycle RED et aux petites corrections de compilation (policies absentes attendues, import Rect, reset multi-mode) avant le GREEN #163 puis la candidate #164.

<!-- GECKO-038-GRID-READABILITY-REVISION-2026-09-27 -->
## 2026-09-27 — retour téléphone 0.11.1-dev : grille trop petite

Capture téléphone Fab après candidate 0.11.1-dev :
- pavé 1–9 désormais visible ;
- selector 3 états désormais visible ;
- difficulté principale supprimée comme prévu ;
- mais la grille 9×9 est devenue nettement trop petite.

Conclusion : la stratégie « réserver de la hauteur à tous les contrôles permanents » corrigeait le chevauchement mais créait une régression de lisibilité.

Décision corrective documentaire :
- grille = largeur utile maximale ;
- marge volontaire horizontale <= 3 px par côté ;
- commandes secondaires doivent se compacter plutôt que réduire la grille ;
- mini-candidats : chiffres noirs, car le vert utilisé dans le renderer actuel est moins lisible à petite taille ;
- la règle précédente « candidat suit obligatoirement le VisualStyle » est annulée pour les mini-candidats.

Aucun code modifié dans cette intervention.

<!-- GECKO-038-PROF-PLAY-GESTURE-2026-09-27 -->
## 2026-09-27 — décision UX Prof Sudoku « explique puis joue »

Observation téléphone : Prof sait déjà sélectionner une case, afficher un candidat pédagogique et expliquer un single.

Décision Fab :
- premier appui Prof = explication uniquement ;
- deuxième appui sur la même déduction encore valide = Prof pose le chiffre ;
- appui long sur Prof = Prof joue directement la prochaine déduction sûre ;
- long press case reste réservé à la palette locale Sudoku.

Décision d'architecture : pending avec garde d'état, provenance PLAYER/PROFESSOR, coup Prof Undo/Redo, candidats pédagogiques transitoires, aucune attribution erronée au joueur.

Aucun code modifié dans cette intervention.


<!-- GECKO-038-FULLWIDTH-PROF-RED-2026-09-27 -->
## 2026-09-27 — RED pleine largeur / Prof joue
Tests ajoutés avant correctif. Le test de marge vérifie aussi INNER_GRID_MARGIN_PX=0 afin d'éviter de respecter 3 px sur le conteneur tout en gardant les anciens 4dp internes. Les tests Prof couvrent stale pending après mutation de note et provenance Undo/Redo.


<!-- GECKO-038-GREEN-POLICIES-2026-09-27 -->
Premier bloc GREEN : uniquement policies pures, aucun câblage Activity. RED #165 reste la preuve de contrat initial.


<!-- GECKO-038-GREEN-MOVE-ORIGIN-2026-09-27 -->
Deuxième bloc GREEN : modèle + moteur seulement. Aucun comportement joueur existant changé car origin par défaut = PLAYER.


<!-- GECKO-038-GREEN-BLACK-CANDIDATES-2026-09-27 -->
Troisième bloc GREEN : renderer/overlay seulement. La valeur principale conserve VisualStyle ; seuls les mini-candidats basculent en noir.


<!-- GECKO-038-GREEN-BOARD-PALETTE-2026-09-27 -->
Quatrième bloc GREEN : alignement BoardView/Overlay sur marge interne zéro et cohérence palette candidats noirs.


<!-- GECKO-038-GREEN-FULLWIDTH-LAYOUT-2026-09-27 -->
Bloc layout GREEN : correction structurelle plutôt qu'overlay qui recouvre les boutons. La grille prend d'abord sa hauteur carrée pleine largeur, puis les contrôles compacts sont layoutés dessous.


<!-- GECKO-038-CANDIDATE-0112-2026-09-27 -->
## 2026-09-27 — candidate 0.11.2-dev

Après le retour 0.11.1-dev (grille trop petite), la géométrie a été corrigée vers une grille quasi pleine largeur. Les candidats sont devenus noirs pour la lisibilité. Le Prof a reçu le contrat explique/joue et la provenance PROFESSOR.

Ajout à la volée demandé par Fab : gecko-repère joueur, analogue fonctionnel d'une croix/pense-bête quand trop de possibilités restent ouvertes.
- RED #171 attendu ;
- #172 moteur marqueur GREEN ;
- #173 rendu/palette animé GREEN ;
- #174 Prof + marker MainActivity GREEN ;
- #175 garde-fou footer palette GREEN ;
- #176 respect du toggle Animations GREEN.

Le gecko-repère ne participe jamais à la logique Sudoku.


<!-- GECKO-038-CI177-GREEN-2026-09-27 -->
## 2026-09-27 — CI #177 GREEN
Le commit candidat 0.11.2-dev/code30 passe tests, assembleDebug, bundleDebug, renommage et upload artifact. Le cycle gecko-repère : #171 RED attendu, #172 moteur GREEN, #173 rendu/palette GREEN, #174 câblage Prof+repère GREEN, #175 footer palette GREEN, #176 animation setting GREEN, #177 candidate versionnée GREEN.

<!-- GECKO-039-MESSAGE-INVARIANT-2026-09-27 -->
## 2026-09-27 — préfixe Prof redondant en Sudoku

Capture téléphone : la bulle affichait l'en-tête « Prof Gecko » puis le corps « Prof Gecko : cette case... », et le status affichait « Prof Gecko • candidat unique ».

Cause : préfixe encodé dans certaines explications SudokuHintEngine + préfixe ajouté dans les status Sudoku.

Correction sur la branche GECKO-039 :
- retirer le préfixe du corps des hints Sudoku ;
- status Sudoku = technique/action uniquement ;
- invariant ajouté pour éviter la même erreur dans le futur mode Gomoku.

