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
