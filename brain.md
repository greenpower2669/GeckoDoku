# GeckoDoku — brain

> Contrat fonctionnel courant, compact et reconstructible.
> Documentation détaillée : docs/GECKODOKU-FONCTIONNEMENT.md.
> Cartographie technique : brainmap.md.
> Archive froide avant restructuration : sauvegarde.md — ne pas lire par défaut.

## État de référence

- Branche : gecko-039-sudoku-tap-gecko-gomoku
- Version : 0.15.15-dev / versionCode 50
- GECKO-047 : VALIDÉ FAB — grandes barres d’axes, drag/suppression hors plateau, Prof utilisant les axes, audio Android/Pierre capturable.
- GECKO-048 : CODE + CI VERTE, validation téléphone encore attendue — prononciation Pierre et couleurs d’axes.
- GECKO-049 : VALIDÉ FAB — géométrie canonique des axes Abeilles & Geckos.
- GECKO-050 : correctif continuité CODE + CI #247 VERTE, validation téléphone attendue — pont PNG entre clips du moteur Gecko/Abeille/Plante.
- Titre visible dans tous les modes : GeckoDoku 🦎.

## 1 — Contrat transversal

GeckoDoku regroupe quatre jeux dans une interface Android commune :
1. GeckoDoku Classic — puzzle logique carré.
2. Sudoku — Sudoku 9×9 avec candidats, repères et Prof.
3. Gomoku — plateau 19×19, contre Prof ou humain contre humain.
4. Abeilles & Geckos — puzzle hexagonal avec zones, axes Q/R/S et appariement Gecko ↔ Abeille.

Commun à tous les modes :
- Prof Gecko = présence pédagogique ;
- Pierre = voix locale du Prof ;
- difficultés quand pertinentes ;
- statistiques, étoiles, profil joueur et Hall of Fame ;
- son et animations optionnels ;
- export/import utilisateur ;
- overlays pédagogiques/médias sans effet sur la logique ni la taille logique du plateau.

Les préférences de mode, difficulté, taille Classic, type de match Gomoku et style Sudoku sont locales et persistantes.

## 2 — Difficultés

Gamme commune :
Découverte → Facile → Réflexion → Difficile → Expert → Démentiel → Mission Impossible → Infernal.

Classic et Abeilles & Geckos :
- le niveau demandé est une vraie contrainte de génération ;
- aucun fallback silencieux vers un autre niveau ;
- la recherche peut continuer jusqu’à trouver un puzzle conforme ;
- elle reste annulable.

Gomoku contre Prof :
- la difficulté règle profondeur, largeur d’exploration, pièges, rayon tactique et fiabilité défensive.

Gomoku humain contre humain :
- pas de difficulté IA ; sélecteur masqué.

## 3 — GeckoDoku Classic

### Règle
Grille 5×5 à 12×12, avec N zones pour N×N.

Une solution respecte :
- exactement un Gecko par ligne ;
- exactement un Gecko par colonne ;
- exactement un Gecko par zone ;
- aucun contact entre Geckos horizontal, vertical ou diagonal ;
- givens verrouillés ;
- solution unique.

Un Gecko confirmé exclut automatiquement sa ligne, sa colonne, sa zone et les cases voisines.

### Interaction
- tap simple : croix manuelle ;
- double-clic : palette Gecko / Hypothèse / Axe ;
- appui long : évolution de l’hypothèse ;
- repères personnels indépendants disponibles.

### Axes joueur
Double-clic → Axe → Horizontal/Vertical → Jaune/Vert/Rouge → pose.

Une barre :
- est globale sur l’axe choisi ;
- reste semi-transparente ;
- conserve type et couleur pendant le drag ;
- disparaît hors plateau.

### Solveur / Prof
La vérité pédagogique vient du solveur logique, jamais d’un coup inventé.
Le solveur couvre singles, règles de région, projections, Gecko X-Wing et hypothèses bornées par contradiction pour les hauts niveaux.
Le Prof travaille depuis l’état réel et ne mutera la grille que par une étape logique permise.
Ses repères pédagogiques sont séparés des repères personnels du joueur.

## 4 — Sudoku

Sudoku 9×9, givens verrouillés.

Interaction :
- tap simple : sélection / marqueur Gecko selon contexte ;
- double-clic : repères personnels ;
- appui long : palette de saisie.

Palette :
- chiffres 1–9 ;
- candidats ;
- marqueur Gecko ;
- effacement.

Un chiffre incorrect est refusé et compte comme erreur.
Un chiffre valide retire cette valeur des candidats de ses pairs ligne/colonne/bloc.

Historique local :
- Annuler ;
- Refaire.

Styles :
- chiffres classiques ;
- Gecko noir et blanc ;
- Gecko couleur.

Prof Sudoku :
- appui normal = explication / prochaine étape ;
- appui long = intervention directe possible ;
- les explications peuvent montrer candidats, projections et raisonnement ;
- l’intervention directe coûte davantage en assistance.

## 5 — Gomoku

Plateau 19×19. Cinq pièces alignées horizontalement, verticalement ou diagonalement gagnent.

Navigation :
- tap = jouer ;
- drag = déplacer la vue ;
- pincement = zoom.

Vue initiale autour de 12×12 cellules ; zoom minimal autour de 5×5.

Contre Prof Gecko :
- humain = vert ;
- Pierre joue l’autre camp ;
- après le coup humain, le Prof joue son tour ;
- le bouton Prof conseille pendant le tour humain ;
- un appui long peut appliquer le coup conseillé pour l’humain puis lancer le tour Prof.

Humain contre humain :
- deux camps humains ;
- Prof conseiller uniquement.

Pédagogie :
- ligne étudiée ;
- menaces ;
- coup conseillé ;
- projection de suite.

## 6 — Abeilles & Geckos

Plateau hexagonal pointy-top, source canonique BeeGeckoAxisGeometry :
- Q = +60° = ↖↘
- S = -60° = ↙↗
- R = 0° = ←→

La légende, le double-clic Axe, la projection réelle et les libellés du Prof doivent tous dériver de cette même géométrie.

Carte panoramique/zoomable et recentrable.

Règle :
- exactement 1 Gecko et 1 Abeille par zone ;
- la paire de zone est voisine ;
- chaque Abeille de solution touche exactement un Gecko de solution ;
- chaque Gecko de solution touche exactement une Abeille de solution ;
- pour chaque type de pièce, au plus une pièce sur une même ligne Q, R ou S.

Interaction :
- tap simple = cycle croix jaune hypothèse → verte sûre → rouge impossible → retrait ;
- double-clic = Gecko / Abeille / Axe ;
- appui long = repère personnel.

Axes :
Axe → Q/S/R → Jaune/Vert/Rouge.
Ils sont globaux, draggables, supprimables hors plateau et persistés.

Persistance :
- schema 4 avec couleur ;
- anciennes données sans couleur → rouge par défaut.

Prof :
- appui normal = indice ;
- appui long = application possible ;
- repères Prof indépendants des couleurs personnelles.

## 7 — Prof Gecko / Pierre

Le personnage UI est Prof Gecko. La voix locale est Pierre.

Contrat :
- explication cohérente avec l’état réel ;
- conseil ≠ coup direct sauf action explicitement prévue ;
- parole pédagogique prioritaire sur animation décorative ;
- paroles automatiques non comptées comme aide demandée ;
- bulles/animations sans reflow du plateau.

Flux vocal :
texte logique → ProfessorSpeech → PierrePiperSpeechEngine → PierrePronunciationPolicy → Sherpa/Piper → VoicePcmPlayer.

Le texte affiché reste orthographiquement correct.
La couche de prononciation ne transforme que le texte vocal.

GECKO-048 :
- mot entier église → eglize pour Pierre ;
- texte UI inchangé.

Pierre dispose aussi du bouton !, de phrases d’ambiance, d’un contexte joueur et d’une mémoire anti-répétition.

## 8 — Médias et audio

Les vidéos/animations sont décoratives :
- aucune influence sur moteur, solution, difficulté, stats ou validation ;
- pas de reflow du plateau ;
- échec média = jeu toujours fonctionnel ;
- désactivables.

GECKO-050 :
- AliveAnimator centralise Gecko, Abeille et Plante ;
- chaque profil possède son PNG transparent + ses vidéos ;
- aucune couleur de fond de case et aucun masque coloré dans la classe vivante ;
- le plateau suspend seulement son ancien PNG statique pendant qu'un slot vivant possède la mascotte ;
- PNG interne utilisé en STATIC_PNG et comme pont inter-clips ;
- pool vivant borné : 3 Gecko + 2 Abeilles + 1 Plante ;
- Plante draggable, position persistée ;
- quatre attentes Gecko et quatre attentes Abeille ; Plante quatre attentes + animation longue ;
- Prof/Pierre reste séparé ;
- animations OFF : la classe vivante affiche directement son PNG transparent ;
- comportement actuel des petits cycles encore trop mécanique : le prochain travail est un update() autonome avec séries complètes mémorisées, séries successives différentes, désynchronisation, et fin de grand cycle déclenchant une mignonnerie chez une copine visible différente.

Vidéo muted :
- la piste audio est réellement désélectionnée.

Audio Pierre validé GECKO-047 :
- Android MEDIA ;
- contenu SPEECH ;
- capture de lecture autorisée ;
- manifeste compatible capture.

Journal vidéo = diagnostic média.

## 9 — Étoiles, erreurs et assistance

Base : 5 étoiles sans aide.

Assistance :
- conseil = 1 point ;
- coup direct = 2 points.

Avant erreurs :
- 0 → 5 étoiles ;
- 1 → 4 ;
- 2–3 → 3 ;
- 4–5 → 2 ;
- plus → 1.

Chaque erreur retire 3 étoiles.
Minimum final : 1 étoile.
Animations et paroles automatiques ne comptent pas comme aide demandée.

## 10 — Données utilisateur

Statistiques locales, globales et par mode/difficulté :
- parties commencées ;
- terminées ;
- terminées avec Prof ;
- temps ;
- erreurs ;
- étoiles ;
- meilleurs résultats.

Profil / Hall of Fame :
- nom joueur modifiable ;
- Hall par mode/difficulté avec étoiles ;
- vider l’historique efface le Hall sans effacer stats agrégées, réglages ou journal Classic.

Journal Classic :
- sauvegarde définition de grille, pas progression ;
- charger = repartir d’une grille propre ;
- sauver, lister, charger, supprimer, vider.

Export/import :
- JSON via sélecteur Android ;
- validation avant application ;
- erreur d’import → restauration de l’état précédent.

## 11 — Invariants à ne pas casser

1. La logique de jeu ne dépend jamais des médias.
2. Prof et solveurs n’inventent jamais une preuve.
3. Les overlays ne redimensionnent pas les plateaux.
4. Les givens restent verrouillés.
5. Difficultés Classic/Bee sans fallback silencieux.
6. Couleurs/repères joueur séparés du rendu pédagogique Prof.
7. Import invalide ne détruit jamais l’état utilisateur.
8. brain.md garde le contrat fonctionnel ; brainmap.md porte les détails techniques.
9. sauvegarde.md est froide et n’est ouverte que sur besoin précis.

## 12 — Delta actif GECKO-048

Présent dans le code et couvert par CI :
- PierrePronunciationPolicy ;
- église → eglize au bord vocal seulement ;
- AxisGuideColor jaune / vert / rouge ;
- ClassicAxisGuide porte la couleur ;
- BeeGeckoLogicalMarks.axisColors ;
- drag conserve couleur ;
- Bee session schema 4, compat ancienne → rouge.

Reste à Fab :
validation téléphone de la prononciation, du rendu des couleurs, du drag/persistance et des non-régressions audio/axes.


## 13 — Delta actif GECKO-049

Cause confirmée sur téléphone :
les barres Bee suivaient la bonne géométrie du plateau, mais les symboles UI hérités annonçaient S vertical et R diagonal.

Correction commit 8ccc0385c8314239976368811dab93808970e35a :
- BeeGeckoAxisGeometry centralise centre écran, angle et symbole ;
- Q ↖↘ / +60°, S ↙↗ / -60°, R ←→ / 0° ;
- MainActivity ne possède plus sa propre table Q/S/R ;
- BeeGeckoRules.axisLabel utilise la même source pour le Prof ;
- aucun changement de règles, solveur ou persistance.

Preuve technique :
CI #236 verte, APK GeckoDoku-v0.15.6-dev publié en prerelease.

Reste :
validation téléphone Fab, notamment la popup du double-clic.


## 14 — État test GECKO-050

CI #246 verte sur commit applicatif 92eed1b0f273a1be069994d61f91f75ab3a5bc7f.
APK de test : GeckoDoku-v0.15.7-dev.apk.
Aucune release publique avant validation téléphone Fab.


## 15 — GECKO-050 continuité inter-clips

Commit applicatif : d9a744fc96814ada0dda5077363c18e4a6d7517a.
CI #247 verte.
APK de test : GeckoDoku-v0.15.8-dev.apk.
Aucune release publique avant validation téléphone Fab.


## 16 — GECKO-050 architecture autonome 0.15.9-dev

Règle canonique :
AliveAnimator possède le PNG transparent et les vidéos de la mascotte. Il ne demande jamais la couleur de fond au plateau et ne peint aucun masque de case.

Pendant la prise en charge d'une pièce :
- le plateau suspend uniquement son PNG statique ;
- AliveMascotOverlayView affiche son PNG transparent ou sa vidéo ;
- à la libération du slot, le plateau reprend son PNG statique.

Activité simultanée bornée :
- 3 Gecko ;
- 2 Abeilles ;
- 1 Plante.

Ce pool rend le plateau visiblement vivant tout en bornant les lecteurs vidéo.

Plante :
- drag direct ;
- position mémorisée en fractions de l'écran ;
- reste décorative et sans impact gameplay.


## 17 — GECKO-050 test 0.15.9-dev

Commit applicatif : b6c7f5001b412112c6011882cb540b079db8b9a9.
CI #248 verte.
APK de test : GeckoDoku-v0.15.9-dev.apk.
Aucune release publique avant validation téléphone Fab.


## 18 — Nuages des placements donnés 0.15.10-dev

Classic et Abeilles & Geckos utilisent GivenFogVisualPolicy.
Le brouillard gris des pièces données n'est plus constitué de quatre grands ovales réguliers :
- 7 bouffées irrégulières ;
- alpha 8..21 ;
- tailles et offsets variés ;
- dérive douce basée sur seed + phase ;
- rendu décoratif seulement, sans effet logique.


## 19 — Validation téléphone 0.15.10-dev

Commit applicatif : 266a0efcd471f90b09f17341b5c2958121b80498.
CI #249 verte.
APK téléphone produit.
Fab valide le nouveau rendu des nuages : « Parfait ».
Ce point est clos sauf régression.

Reste actif :
le contrat des grands cycles autonomes des mascottes est enregistré dans l'ordre de mission et le TODO mais n'est pas encore implémenté.


## 20 — Release publique 0.15.10-dev

Version téléphone validée et publiée publiquement.
Tag : phone-0.15.10-dev-run-250.
CI #250 a créé la prerelease ; CI #251 l'a promue en release normale.
APK public : GeckoDoku-v0.15.10-dev.apk.
SHA-256 : 684a40dbdc8ab91a8ca4ccdb5d4d904907279b9ad9c05c35603ae0b253ac9b46.

Prochaine évolution active : grand cycle autonome des mascottes.


## 21 — GECKO-051 présence vivante ≠ lecteur vidéo

Architecture canonique :
- chaque mascotte visible possède une Presence légère dans AliveMascotOverlayView ;
- Presence possède PNG transparent, géométrie, suppression du PNG historique du plateau et son AliveAnimator ;
- les lecteurs vidéo sont un pool séparé et borné : 3 Gecko, 2 Abeilles, 1 Plante ;
- une Presence sans lecteur reste affichée par son PNG interne ;
- update() attribue les lecteurs aux Presence les moins récemment animées ;
- toutes les présences finissent donc par s'animer sans multiplier les GLSurfaceView.

Synchronisation complète :
- Classic : snapshot.confirmed ;
- Sudoku : snapshot.geckoMarkers ;
- Gomoku : snapshot.stones ;
- Abeilles & Geckos : snapshot.confirmedGeckos + confirmedBees.

Cycle :
- changement de stay à chaque clip ;
- grand cycle de 3 à 5 stays ;
- séries successives non identiques ;
- fin de grand cycle → choix d'une copine visible disposant d'une animation cute ;
- autres Presence repartent de manière désynchronisée.

Pierre reste volontairement hors de ce moteur.


## 22 — Test GECKO-051 0.15.11-dev

Commit applicatif : 616b0dcdb808735ca0a396091aa2428ed5192034.
CI #252 : verte.
APK téléphone : GeckoDoku-v0.15.11-dev.apk.
SHA-256 : 9d8de7bd06a5defcbef0523cbdae293f125c077f9a628bfdb0b37357cfbf709f.

Aucune release publique : validation téléphone Fab nécessaire, en particulier :
- les Gecko déjà présents en Classic s'animent à tour de rôle ;
- Sudoku/Gomoku/Bee utilisent la même rotation ;
- aucun retour de carré de fond ;
- pas de surcharge visible avec le pool vidéo borné ;
- Plante drag et Pierre inchangés.


## 23 — GECKO-052 diagnostic ALL ANIMATED

0.15.12-dev désactive volontairement le plafond runtime des lecteurs vidéo.
AliveMascotOverlayView.videoSlots est dynamique : il alloue autant de VideoSlot que de Presence visibles pour chaque type.

But :
tester directement l'hypothèse que le pool borné / ordonnanceur est la raison pour laquelle les Gecko cessent de s'animer sur téléphone.

Ce choix est DIAGNOSTIC, pas encore l'architecture finale optimisée.

Intro :
applyProfessorIntroVisibility() pilote désormais AliveMascotOverlayView.setIntroSuppressed().
Pendant FIRST/SECOND, les lecteurs mascottes sont arrêtés et la couche est invisible.
À DONE/skip, la couche vivante reprend et les cycles repartent.


## 24 — Test GECKO-052 0.15.12-dev

Commit applicatif : f90f41a4d6c65d952538a1cbad266e7b2f0c0abd.
CI #253 : verte.
APK : GeckoDoku-v0.15.12-dev.apk.
SHA-256 : a50bffee172b944dfe1e40d977b6eccc5b3383722aed7c4ff1d2e643615fc1c3.

Build diagnostic :
- pas de plafond runtime des lecteurs mascottes ;
- toutes les Presence visibles peuvent être animées simultanément ;
- AliveMascotOverlayView supprimé pendant INTRO FIRST/SECOND ;
- reprise après DONE/skip ;
- aucune release publique.


## 25 — GECKO-053 reprise lifecycle Android

Invariance lifecycle :
- onPause() conserve stopAll() : aucun lecteur mascotte ne reste actif en arrière-plan ;
- onResume() doit reconstruire l'état visuel vivant depuis la vérité des moteurs ;
- ordre de reprise : positionTitleIdentity → ensurePlantMascot → syncLivingMascotsForCurrentMode.

syncLivingMascotsForCurrentMode restaure :
- Classic : confirmed ;
- Sudoku : geckoMarkers ;
- Gomoku : stones ;
- Abeilles & Geckos : confirmedGeckos + confirmedBees.

Ainsi un aller-retour Mail/Messages ne transforme plus les mascottes du plateau en PNG statiques permanents.
Le diagnostic ALL ANIMATED reste actif en 0.15.13-dev.


## 26 — Test GECKO-053 0.15.13-dev

Commit : 173bf0ed9e4431b3e6efe13de44c4374cc055bcb.
CI #254 verte.
APK : GeckoDoku-v0.15.13-dev.apk.
SHA-256 : 4bee54cfdc1261adf75559635c8749ade82133a8383cc34e1ddbea4019d13937.

À valider :
après ouverture de Mail/Messages puis retour, toutes les mascottes du mode courant doivent être reconstruites et recommencer leurs cycles sans relancer l'intro.


## 26 — GECKO-054 canon : une mascotte = un lecteur

Décision définitive Fab : plus jamais d’ordonnanceur de mascottes.

AliveMascotOverlayView :
- chaque Presence possède directement son ImageView, son ChromaKeyVideoView et son AliveAnimator ;
- aucun VideoSlot partagé, aucun pool, aucune capacity(), aucune rotation ;
- chaque Presence programme uniquement sa propre prochaine étape ;
- target temporairement null : retry local 240 ms, sans supprimer la mascotte ;
- échec média : PNG fallback puis nouvel ambient après 900 ms.

Apparition :
tant que Gecko_apparition n’a pas fini, ni PNG interne ni PNG historique du plateau. Ensuite seulement PNG/idle vivant.

État initial :
syncLivingMascotsForCurrentMode reste commun aux quatre modes, complété par refresh post-layout.

Lifecycle :
onPause stopAll/release ; onResume ensurePlantMascot + syncLivingMascotsForCurrentMode.

Pierre :
pipeline visuel/parole séparé.
RETURN_AFTER_PAUSE = 300 phrases dédiées, cooldown 48 h, 48 IDs récents persistants, anti-famille et anti-similarité.


## 27 — Test GECKO-054 0.15.14-dev

Commits :
- 302debfdc4fba4e9a9bdc483f0fbf7a19c5d8484 : suppression définitive du lecteur partagé ;
- ab82f3d338cef1a8338d70d41fb2e70267fdc90d : 300 retours Pierre + anti-répétition + mémoires.

CI #255 entièrement verte.
APK : GeckoDoku-v0.15.14-dev.apk.
SHA-256 : 67cb5429d2ae188e8a5a58f09f43afa2032a96e43f1998f1265bafe96eed8abb.
Aucune release publique avant validation téléphone.


## 27 — GECKO-055 composition interne canonique

Une Presence possède :
- pngContainer + ImageView transparent ;
- videoContainer + ChromaKeyVideoView ;
- AliveAnimator ;
- target/ownerKey.

pngContainer et videoContainer sont deux siblings du AliveMascotOverlayView, pas deux enfants empilés dans le même FrameLayout.
Raison : ChromaKeyVideoView est un GLSurfaceView/surface Android séparée ; le garder comme sibling restaure une composition fiable avec le PNG normal.

Le plateau ne gère plus les transitions vidéo :
- target invalide → ancien PNG plateau reste disponible ;
- target valide → transfert de propriété à la Presence, ancien PNG plateau supprimé ;
- ensuite PNG interne ↔ vidéo uniquement ;
- playDecision ne touche plus au masquage plateau.

Invariant durable :
aucun ordonnanceur, aucun pool, aucun lecteur partagé.


### GECKO-055 — build
CI #257 verte sur 135ec82888e8d01494f74bd56adcea86f5dae490.
APK Phone 0.15.15-dev SHA-256 671776186328f53df577599e45d2cb85ea8bc6a8ca8daf5d54bf0721c71dabf5.
Validation téléphone encore requise.


## 28 — GECKO-056 first-frame Gecko/Abeille

Observation téléphone : Gecko et Abeilles restent en PNG fixe alors que les animations sont ON.

Cause code ciblée :
FreshPlaybackFrameGate rejetait MEDIA_INFO_VIDEO_RENDERING_START si onPlayerStarted() n'avait pas encore été appelé. Android peut délivrer le signal de rendu pendant MediaPlayer.start(), donc avant le retour local de start() et avant onPlayerStarted().

Correction 0.15.16-dev :
- onRenderingStart() mémorise le signal pour la génération courante sans exiger playerStarted à cet instant ;
- onFrameRendered() conserve la barrière stricte playerStarted && renderingStarted ;
- une génération périmée ne déverrouille jamais la suivante ;
- cancel() reste bloquant ;
- test unitaire dédié ajouté.

Portée commune : Classic, Sudoku, Gomoku, Abeilles & Geckos ; Gecko et Abeille partagent ChromaKeyVideoView/FreshPlaybackFrameGate.

Invariant inchangé : 1 Presence = 1 PNG + 1 ChromaKeyVideoView + 1 AliveAnimator ; zéro ordonnanceur, zéro pool, zéro lecteur partagé.

### GECKO-056 — build
CI #260 verte sur a43ef730ec77475e08b3ab7c4b897a023977057c. Artifact : GeckoDoku-v0.15.16-dev-phone. SHA-256 archive : 4fde649f9492e89ba01e12e62a4094e454e229008c930c35837b200c94308894. Validation téléphone Gecko + Abeilles encore requise.


## 29 — GECKO-057 : famine des callbacks Presence

Le log téléphone de 0.15.16-dev est décisif : Pierre produit PLAY_REQUEST / VIDEO_FIRST_FRAME / VIDEO_VISIBLE, tandis que les ChromaKey supplémentaires des mascottes de plateau sont bien construits mais ne produisent aucun PLAY_REQUEST. GECKO-056 avait donc corrigé un gate situé après play(), pas la cause empêchant play() d'être appelé.

Cause retenue dans AliveMascotOverlayView :
- refreshDynamicTargets() rappelle refreshPresenceTarget() ;
- refreshPresenceTarget() remplaçait systématiquement les LayoutParams des deux siblings PNG/vidéo, même à géométrie identique ;
- chaque refresh d'une Presence encore pending rappelait schedulePresence() ;
- schedulePresence() faisait removeCallbacks() puis postDelayed() ;
- les refresh/layout pouvaient ainsi repousser continuellement le même cycle avant son exécution.

GECKO-057 :
- LayoutParams appliqués seulement si taille/position changent ;
- une Presence déjà armée garde son callback local : un refresh ne peut plus le repousser ;
- le callback se désarme lui-même juste avant advancePresence ;
- cancel/remove désarment explicitement ;
- ALIVE_PLAY est journalisé avant ChromaKeyVideoView.play ;
- le dernier Gecko Classic, joueur ou Prof, est aussi enregistré dans la couche vivante avant completeGame.

Invariant : aucune coordination globale, aucun pool, aucun ordonnanceur. Une Presence possède son unique callback local.

### GECKO-057 — build
CI #263 verte sur 352c046bcc6e1af6d26794782f14189054ddcae2. APK Phone 0.15.17-dev produit. SHA-256 APK : cb50cb5e97ed9cc33ab7285982a6da7fda5e6439040fa2a195b7c59bb6fa8577. Validation téléphone Gecko + Abeilles encore requise.

### GECKO-057 — validation téléphone partielle
Fab confirme que le mode 1 / Classic est corrigé sur 0.15.17-dev : les Gecko vivants s'animent de nouveau. Les validations Sudoku, Gomoku et Abeilles & Geckos restent à faire.


## 30 — GECKO-058 : cibles de bord hexagonal conservées

Le défaut Abeilles & Geckos venait de beeGeckoAliveTarget(). Le rectangle animé d'un Gecko de bord pouvait dépasser légèrement viewportRectOnScreen(); l'ancien code retournait alors null, ce qui empêchait toute Presence vidéo pour ce Gecko. Les Abeilles, plus petites et souvent intérieures, restaient animables.

Correction 0.15.18-dev :
- si la cible n'intersecte pas du tout le viewport : null conservé ;
- si elle intersecte mais dépasse un bord : le rectangle garde sa taille et est décalé du minimum nécessaire pour rentrer dans le viewport ;
- si sa taille dépasse le viewport, son centre est aligné sur celui du viewport ;
- aucun changement du pipeline AliveAnimator/ChromaKey ;
- trace BEE_GECKO_ALIVE_TARGET_CLAMPED pour vérifier les Gecko de bord sur téléphone.

But : garder les Gecko gauche/droite/bas vivants sans casser zoom, drag ni les Abeilles.

### GECKO-058 — build
CI #265 verte sur 2c20b06d2a0f699c8811cdf7cd37871a4b712f45. APK Phone 0.15.18-dev produit. SHA-256 APK : 9e0ad087bc2f641ecd80010bb162cf57739ac0b0a236d2dc21cbe5d321311b77. Validation téléphone des Gecko de bord encore requise.


## 31 — GECKO-059 : suppression de la contrainte viewport

Fab précise que la règle de rejet/clamp du cadre média n'avait jamais été demandée et qu'elle est conceptuellement fausse ici : les Gecko/Abeilles sont déjà centrés dans un cadre transparent/key-color plus large que le personnage visible.

Décision : beeGeckoAliveTarget() n'applique plus aucun test viewport, aucun rejet partiel et aucun recentrage. La cible centrée sur la cellule est utilisée directement.

### GECKO-059 — build
CI #267 verte sur baf9743f71821032fbf88480462554eddeade1d5. APK Phone 0.15.19-dev produit. SHA-256 APK : 97e3876d853dd1701f3e7f552fe38d289bf4d5344278416a1e409d7817d6ebcf. Validation téléphone requise.


## 32 — GECKO-060 : une seule source visuelle par mascotte

Retour Fab : la nouvelle classe vivante contient déjà son PNG fallback et choisit elle-même ses animations. L'ancien rendu PNG du BeeGeckoBoard ne doit donc plus coexister avec elle.

Modèle canonique :
- une Presence Alive possède le PNG fallback ET les vidéos ;
- BeeGeckoBoardView ne dessine plus de seconde représentation Gecko/Abeille ;
- target temporairement absente : PNG+vidéo Alive sont cachés sans rendre l'ancien PNG du board ;
- la règle hors-écran reste utile mais teste l'intersection de la cellule avec le viewport, jamais les bords du cadre transparent/key-color ;
- cellule visible : le cadre média peut dépasser naturellement ;
- cellule complètement sortie par zoom/drag : Presence cachée jusqu'au retour.

Version 0.15.20-dev / versionCode 55.


## 33 — GECKO-061 : hors-écran = centre de cellule hors viewport

Le retour téléphone 0.15.20-dev montre un Gecko encore dessiné au-dessus du plateau après drag. L'ownership unique est correct, mais la garde géométrique basée sur une simple intersection de cellule est trop permissive : une cellule presque entièrement sortie garde encore une petite intersection et sa Presence reste visible.

Règle canonique :
- la Presence reste propriétaire unique du PNG fallback et des vidéos ;
- le cadre média transparent/key-color n'entre jamais dans le test de visibilité ;
- la cellule fournit la position logique ;
- si le centre de la cellule est dans le viewport, la Presence est autorisée ;
- dès que le centre de la cellule sort du viewport, target=null et PNG+vidéo Alive sont cachés ;
- aucun retour au rendu legacy du board.

Version 0.15.21-dev / versionCode 56.

### GECKO-061 — build
CI #275 verte sur 94eed6fd4e718d8d5aaeed6350e816709dd45327. APK Phone 0.15.21-dev produit. SHA-256 : 1f65452b0e8b98670eaf27ba9e321e997dfad13c2bfdbf36b2fd17523877da19. Validation téléphone requise.


## 34 — GECKO-062 : Gomoku limité à 3 Presence par équipe

Retour Fab : en Gomoku (contre ordinateur/Prof comme en JcJ), animer chaque pierre produit trop de mouvement.

Règle canonique :
- les pierres du plateau restent toutes visibles ;
- au maximum 3 pierres PLAYER (vertes) et 3 pierres PROFESSOR (jaunes) sont promues en Presence Alive ;
- les autres restent le rendu PNG statique du GomokuBoardView ;
- la sélection est aléatoire mais stable pendant un grand cycle ;
- lorsqu'un AliveAnimator termine une série et demande le grand rafraîchissement de groupe, MainActivity redistribue la sélection 3+3 ;
- s'il existe plus de 3 candidats dans une équipe, une redistribution évite de conserver exactement le même trio ;
- cette règle vaut pour VS_PROFESSOR et HUMAN_VS_HUMAN.

Implémentation : GomokuLivingSelectionPolicy + callback onGroupCycleCompleted de AliveMascotOverlayView.
Version 0.15.22-dev / 57.

### GECKO-062 — build
CI #280 verte sur 3187c512c3155581daa3ba24002faada566a7c20. APK Phone 0.15.22-dev produit. SHA-256 : 70b8cb93a180b2e0612bd445d035650537b52ae8640d46f503293ad5a3bca891. Validation téléphone requise.


## 35 — GECKO-063 : autorité Alive unique + garde première frame

Le journal téléphone de 0.15.22-dev a prouvé que la limite Gomoku 3+3 était contournée par l'ancien chemin playGomokuPieceAnimation(GECKO_APPEARANCE) -> showGomokuLivingGecko(). Chaque nouveau coup créait ainsi une nouvelle Presence malgré GomokuLivingSelectionPolicy.

Correction canonique :
- GECKO_APPEARANCE ne crée plus de Presence et force seulement la synchronisation du sélecteur 3+3 ;
- les longs gestes Gomoku ne passent plus par un overlay de pierre indépendant : ils deviennent requestCute() sur une Presence déjà sélectionnée ;
- GOMOKU_LIVING_SELECTION journalise player/professor/total pour preuve téléphone ;
- l'architecture reste 1 Presence = son PNG + son ChromaKeyVideoView + son AliveAnimator.

Démarrage :
- un launchCurtain noir est ajouté au-dessus de toute l'UI avant setContentView sur le premier lancement avec médias actifs ;
- dès que la session Intro est acceptée, RichMediaOverlay est déjà noir et prend le relais ; sinon le rideau est retiré pour ne jamais bloquer l'application.

Carré noir :
- ChromaRenderer ne dessine plus l'external texture tant qu'une frame fraîche appartenant au playback courant n'a pas été consommée ;
- jusque-là il ne publie que le clear transparent alpha=0 ;
- pour les playbacks gate-first-frame, le rendu n'est armé qu'au VIDEO_RENDERING_START_SIGNAL puis attend une frame postérieure ;
- cela protège Gecko, Abeille, Plante et Prof sans cacher artificiellement leurs conteneurs.

Plante :
- pngScale=0.95 est distinct de renderScale ; seule l'ImageView PNG est réduite autour de son centre, vidéo et target inchangés.

Version 0.15.23-dev / versionCode 58.

Build GECKO-063 : CI #286 / run 36630368939 SUCCESS. Artifact GeckoDoku-v0.15.23-dev-phone, APK SHA-256 ae704b70c968030b33389d6296332c3c9d7dbe63b9479e993092f84d5b0e0661. Validation téléphone encore requise ; aucune release.

## 36 — Retour téléphone 0.15.23-dev : géométrie Bee/Gecko à revoir
Fab valide le lot cumulé hors Gomoku et Abeilles & Geckos : intro sans flash du plateau, disparition du carré noir avant première frame et ajustement PNG plante ~95 % fonctionnent.

Sur Abeilles & Geckos, la capture Expert 6 zones montre l'échec du critère centre-de-cellule : un Gecko dont le centre reste dans le viewport peut déborder au-dessus de la bordure ; inversement une cellule encore partiellement visible en bas peut avoir son centre hors viewport et sa Presence est alors supprimée trop tôt.

Le bon modèle proposé pour la suite est de séparer deux notions :
1. existence logique de la Presence : la cellule existe tant qu'elle intersecte le viewport ;
2. visibilité physique : le rendu PNG/vidéo reste centré sur la cellule mais doit être clipé par le rectangle réel du plateau.

Ainsi aucune translation/recentrage artificiel, aucun test sur le grand cadre média, et aucun seuil brutal sur le centre de cellule. Cette stratégie doit être confirmée par Fab avant code.


## 37 — GECKO-064 : existence par cellule, visibilité par clipping

Après validation de Fab, la géométrie Bee/Gecko est corrigée selon deux responsabilités séparées :
- existence logique : beeGeckoAliveTarget() retourne une target tant que cellRectOnScreen intersecte viewportRectOnScreen ;
- visibilité physique : AliveMascotOverlayView reçoit un clipProvider optionnel et applique exactement la même clipBounds aux conteneurs PNG et vidéo.

Le target reste centré sur la cellule sans translation ni clamp. Le clip est calculé dans l'espace root puis converti dans les coordonnées locales du conteneur : intersection(target, viewport), offsetée de -target.left/-target.top. Ainsi le sprite glisse réellement derrière la bordure du plateau pendant zoom/drag.

Le grand cadre transparent/key-color ne décide jamais de l'existence de la mascotte ; il est simplement découpé au viewport comme le PNG. Version 0.15.24-dev / 59.

### GECKO-064 — build
CI #289 / run 36633302195 SUCCESS sur 65bf2f5016f9373d5804d7ece962f1c17522f2c3. APK Phone 0.15.24-dev produit, 248557697 octets, SHA-256 0792b38bc21fc8dc9f705220f2d3a11dd16a8c91ebd39a44fcacdfc2dd059e67. Validation téléphone requise ; aucune release.


## 37 — GECKO-065 : existence vivante indépendante de la fenêtre visible

Décision Fab : en Abeilles & Geckos comme en Gomoku, une mascotte ne doit plus être détruite, suspendue ou perdre sa géométrie simplement parce que le zoom/drag la fait passer derrière le cadre blanc du plateau.

Modèle canonique final :
- le plateau reste en dessous ;
- la Presence Alive reste au-dessus du plateau ;
- la cible géométrique de la mascotte reste calculable même quand sa cellule est entièrement hors de la fenêtre visible ;
- le clip du plateau décide seul de ce qui est visible ;
- clip vide = mascotte entièrement derrière le cadre, mais son AliveAnimator local continue son cycle ;
- au retour par drag/zoom, la même Presence réapparaît progressivement, sans recentrage, sans pop et sans recréation ;
- titres, textes, boutons et Prof sont hors de cette fenêtre visuelle et ne peuvent donc pas être recouverts par une mascotte de plateau.

Abeilles & Geckos :
BeeGeckoBoardView expose désormais cellRectOnScreenUnbounded(). MainActivity.beeGeckoAliveTarget() s'appuie dessus. Le clipProvider existant reste viewportRectOnScreen() converti dans screenRoot. Il n'existe plus de condition viewport qui transforme la cible en null.

Gomoku :
GomokuBoardView expose geckoRectOnScreenUnbounded() et viewportRectOnScreen(). Les 3+3 Presence sélectionnées utilisent une cible non bornée et un gomokuAliveClipRect(). Le cap 3 PLAYER + 3 PROFESSOR et la redistribution par grand cycle restent inchangés.

Point d'architecture important :
une tentative intermédiaire de masque global + changement de couche SurfaceView a été abandonnée avant livraison téléphone. Le code final 0.15.26-dev restaure la politique ChromaKeyVideoView historique. L'effet « derrière le cadre » est obtenu par clip local de chaque Presence, pas par un nouvel ordre global de SurfaceView.

Invariant toujours absolu :
1 Presence = 1 PNG fallback + 1 ChromaKeyVideoView + 1 AliveAnimator + 1 callback local. Aucun ordonnanceur global, pool ou lecteur partagé.

### GECKO-065 — build
CI #303 (run 36638744397) verte sur 1cffec0bbf88148c210fad545e1a3ad4a2f56ad4. Artifact GeckoDoku-v0.15.26-dev-phone. SHA-256 archive e5f33774d3bd0ab3af52bee00b25c1b75d5ba4a4d70b22ca09067e1b4f1092dd. APK 248557697 octets, SHA-256 1b25121080454d58934c20366073ad0a255324226759fe3baf22f2d4b1617323. Aucune prerelease/release.


## 38 — GECKO-066 : TextureView ciblé pour Abeilles/Geckos et Gomoku

Observation téléphone 0.15.26-dev : la géométrie et le clipping PNG sont bons, mais le rendu peut sembler changer dans le temps parce que GLSurfaceView/SurfaceView est composé séparément du reste de la hiérarchie Android. Une vidéo Alive peut donc ne pas respecter exactement le même clip/Z-order que son PNG fallback.

Test architectural autorisé par Fab :
- ne toucher ni Pierre, ni intro, ni Classic, ni Sudoku, ni plante ;
- uniquement owners bee:* et gomoku:* sur Android API >= 33 utilisent ChromaKeyTextureView ;
- les autres Presence gardent ChromaKeyVideoView historique ;
- Android < 33 garde aussi le GLSurfaceView pour compatibilité.

ChromaKeyTextureView :
- MediaPlayer décode directement vers la SurfaceTexture du TextureView ;
- TextureView est une vraie View Android, donc clipBounds, alpha, parent clipping et ordre visuel sont composés avec le PNG ;
- RuntimeShader + RenderEffect reproduisent l'algorithme keycolor actuel : dominance bleu/vert, seuil/softness/despill, puis yellowTint Gomoku ;
- sortie AGSL prémultipliée pour conserver des bords transparents propres ;
- isOpaque=false ;
- fit-center via TextureView.setTransform pour conserver le ratio vidéo.

Garde première frame :
- alpha=0 au play ;
- baseline de onSurfaceTextureUpdated enregistrée ;
- MediaPlayer doit avoir démarré ;
- MEDIA_INFO_VIDEO_RENDERING_START doit être reçu ;
- une mise à jour SurfaceTexture postérieure au baseline doit arriver ;
- seulement alors VIDEO_FIRST_FRAME puis VIDEO_VISIBLE ;
- holdOnFirstFrame reste supporté.

L'interface ChromaKeyPlayback permet à AliveMascotOverlayView de piloter indifféremment l'ancien backend ou le TextureView sans modifier AliveAnimator ni les callbacks locaux.

Invariant : 1 Presence = 1 PNG + 1 backend vidéo + 1 AliveAnimator + 1 callback local. Aucun ordonnanceur, pool ou lecteur partagé.

CI #309 a déjà confirmé compilation/tests du nouveau backend avant bump. Version de test : 0.15.27-dev / 62.

### GECKO-066 — build final
CI #310 / run 36641153646 SUCCESS sur 4bf3c696a8dbee1d90f3136c8594684025e4a381. APK Phone 0.15.27-dev produit, 248574081 octets, SHA-256 e5de07cea97ca736108d644298ab56b2ad16689d695e357ef0a1d4cd280afe3f. Archive artifact SHA-256 e3cc39a3d9821b4894cd5244f7c0994e0b8fc09c2c45fa2c82723beaaf0479ac. Aucune release/prerelease. Validation téléphone ciblée Bee/Gecko + Gomoku requise.


## 39 — GECKO-067 : TextureView pour la couche, OpenGL pour la couleur

Retour téléphone 0.15.27-dev : l'intuition TextureView est bonne pour la hiérarchie et le clipping, mais le keycolor et le filtre jaune sont visuellement instables. Le point suspect est le RuntimeShader/RenderEffect appliqué comme post-effet Android sur la TextureView.

Décision : conserver TextureView comme surface de composition Android, mais déplacer tout le traitement couleur dans un vrai pipeline OpenGL ES 2, identique conceptuellement au ChromaKeyVideoView historique.

Pipeline par Presence :
- TextureView fournit la SurfaceTexture de sortie ;
- un thread GL local crée EGLDisplay/EGLContext/EGLSurface sur cette SurfaceTexture ;
- un texture OES externe + SurfaceTexture d'entrée reçoit le décodage MediaPlayer ;
- à chaque frame disponible : updateTexImage(), matrice SurfaceTexture, shader GLSL historique, eglSwapBuffers() vers la TextureView ;
- le shader reprend exactement KEY_THRESHOLD, KEY_SOFTNESS, KEY_DESPILL, choix bleu/vert et yellowTint historique ;
- fit-center reste géré par les sommets OpenGL, comme dans ChromaKeyVideoView.

Première frame :
- play -> alpha TextureView 0 ;
- beginPlayback mémorise la génération ;
- MEDIA_INFO_VIDEO_RENDERING_START arme la garde et fixe un baseline de frame ;
- seule une frame OES postérieure au baseline, réellement dessinée puis eglSwapBuffers(), déclenche VIDEO_FIRST_FRAME ;
- ensuite alpha=1, ou pause si holdOnFirstFrame.

Ainsi :
- géométrie/clipping/Z-order = TextureView Android ;
- couleur/keycolor/jaune = OpenGL historique ;
- aucune dépendance à RenderEffect pour les mascottes de plateau.

Invariant inchangé : chaque Presence possède son propre PNG, son propre backend vidéo, son propre AliveAnimator et son callback local. Aucun ordonnanceur global ni partage de lecteur.

Version test : 0.15.28-dev / versionCode 63.

### GECKO-067 — build final
CI #312 / run 36643601567 SUCCESS sur 075160c5bd45157d7c818e2e996e1518554efe95. APK Phone 0.15.28-dev produit, 248574081 octets, SHA-256 296240aa14d866f101ac921887ca30c46c1eebbde946d2f7dc5797ae70d1ce79. Archive artifact SHA-256 2633430128a4fcd465bea4a559770ec3810d0f305c20eddafe30ced7b74585ae. Aucune release/prerelease. Validation téléphone keycolor/jaune/clipping requise.


## GECKO-068 — Sprites RGBA expérimentaux pour Gomoku / Abeilles (2026-09-30)

- Remplacement expérimental du backend TextureView des mascottes de plateau par un backend SpriteRGBA.
- Les MP4 restent les sources maîtres. Un cache hors écran extrait 12 images/s, applique le chroma-key bleu/vert et stocke des frames RGBA transparentes.
- Le filtre jaune du Prof reste dynamique au rendu : aucun doublon de sprites jaunes.
- Résolution réglable : 240p / 360p / 480p, défaut 480p.
- Limite Gomoku 3 animations par camp désormais optionnelle et désactivée par défaut pour le test de charge.
- Journal performance toutes les ~5 s : FPS UI, moyenne/max frame UI, animations sprite actives, pic simultané, coût moyen de décodage frame, mémoire bitmap sprite et mémoire JVM.
- Le cache 480p est volontairement un test de qualité/performance ; surveiller taille cache, mémoire et temps de préchauffage avant toute généralisation.


## 2026-09-30 — GECKO-069 : banques SpriteRGBA chaudes

Architecture retenue après test 240p :
- les Presence restent totalement autonomes ; aucun ordonnanceur de mascottes n'est réintroduit ;
- SpriteFrameCache possède deux banques physiques chaudes : GECKO_SHARED et BEE ;
- GECKO_SHARED sert à la fois le vert et le jaune ; le jaune continue d'être appliqué dynamiquement par ColorMatrix, donc zéro duplication des frames ;
- les bitmaps des banques chaudes sont gardés par références fortes dans une limite de 16 MiB par rôle, en plus du LRU existant ;
- le warmup n'est plus lancé avant la construction de l'UI : il est armé après 4 s (2,5 s après changement de résolution / retour d'application), afin que les demandes réellement visibles puissent entrer d'abord ;
- une fois les deux banques chaudes prêtes, les autres stay Gecko/Abeille sont produits un par un avec temporisation ;
- les clips apparition/disparition et surtout Gecko_actions_plusieurs.mp4 (360 frames) sont désormais strictement on-demand et ne font plus partie du préwarm de démarrage.

Indicateurs téléphone attendus : HOT_BANK_PINNED role=GECKO_SHARED/BEE, yellow=shared, absence de SPRITE_BUILD_START Gecko_actions_plusieurs sans demande CUTE, et maintien des FPS lors du remplissage progressif.


### GECKO-069 — build
Commit : 522802e5712ee5005850a033577f5fc143d00bb8.
CI #314 : SUCCESS (tests unitaires + assemblePhone).
Artifact : GeckoDoku-v0.15.30-dev-phone.
Digest archive artifact : sha256:3245421691fdc4857ee7d1dee1978cb9ff198a2d21380fbece526630b6ca795b.
Aucune publication ; prochaine étape = test téléphone 240p du warmup chaud.


## 2026-09-30 — GECKO-070 : quick-start SpriteRGBA + résolutions basses

Retour téléphone 0.15.30-dev :
- le rendu 240p est visuellement propre ;
- le cache chaud est bien épinglé : GECKO_SHARED 61/61 (~13,40 MiB) et BEE 24/24 (~5,27 MiB) ;
- le jaune continue de partager physiquement la banque Gecko ;
- le coût restant perceptible concerne surtout la première construction, notamment l'Abeille avant sa première animation.

Décision Fab :
- ajouter 120p et 180p sous 240p ; conserver 360p et 480p ;
- 240p devient la résolution par défaut pour une configuration neuve ;
- ne plus attendre la banque complète avant de démarrer une animation SpriteRGBA.

Implémentation :
- SpriteFrameCache publie un préfixe dès 4 frames RGBA prêtes (SPRITE_QUICK_READY) ;
- ChromaKeySpriteView démarre immédiatement sur ce préfixe (QUICK_START) ;
- si les 4 frames sont consommées avant la fin de fabrication, elles bouclent localement sans recréer la Presence ;
- quand la banque complète arrive, le renderer bascule sur la séquence complète (FULL_SEQUENCE_READY) sans second lecteur vidéo ni nouvel ordonnanceur ;
- la fabrication continue sur le worker existant ; les MP4 restent les sources maîtres.

Version test : 0.15.31-dev / versionCode 66.


## 2026-09-30 — GECKO-071 : STABLE_FRAME canonique

Décision Fab : quand les animations sont actives, ne plus utiliser le PNG historique comme placeholder Gecko/Abeille. Utiliser une image statique réellement issue de l'animation :
- Gecko : frame 1 de gecko/alive/stay1.mp4 ;
- Abeille : frame 1 de abeille/alive/stay1.mp4 ;
- Gecko jaune : même Bitmap Gecko, tint jaune dynamique.

Implémentation :
- StableFramePolicy définit uniquement ces deux sources canoniques ; PLANT retourne null ;
- SpriteFrameCache.requestStableFrame() extrait une seule frame à t=0 avec exactement le scaling/chroma-key SpriteRGBA ;
- cache mémoire + disque stable-frames/ ; réutilisation possible de sprites/<key>/frame-00000 ;
- AliveMascotOverlayView masque le PNG historique en mode animations ON, demande la STABLE_FRAME puis l'affiche comme ImageView simple ;
- aucune animation n'est comptée active tant que seule cette image est affichée ;
- au premier frame rendu par le backend animé, STABLE_FRAME_TO_SPRITE masque l'image ;
- entre deux clips, la STABLE_FRAME revient ;
- animations OFF restaure explicitement le PNG historique et journalise LEGACY_PNG_SHOW ;
- changement de résolution invalide seulement la référence locale de chaque Presence et redemande la frame de la nouvelle résolution.

Version : 0.15.32-dev / 67.


## 2026-09-30 — GECKO-072 Sprite Bank Factory

Architecture installée pour rendre la conversion MP4→RGBA exceptionnelle après une première préparation.

SpriteBankFactory :
- ThreadPoolExecutor borné à 3 workers et PriorityBlockingQueue ;
- sessions coopératives de 4 frames, requeue après chaque chunk ;
- promotion des demandes visibles par rapport au catalogue via priorité + scheduleEpoch ;
- coalescence par asset/keycolor/résolution ;
- barrière STABLE_FRAME : les banques lourdes ne démarrent qu’après préparation des STABLE_FRAME Gecko/Abeille en 240p ;
- workers en THREAD_PRIORITY_BACKGROUND ;
- cache persistant filesDir/sprite-banks-v2 ;
- reprise BUILDING sûre, publication atomique READY, source SHA-256 + bank SHA-256 ;
- catalogue exhaustif des 12 assets Gecko/Abeille réellement compatibles SpriteRGBA ;
- préfabrique 60p puis 120p puis 240p ; >240p uniquement on-demand.

ChromaKeySpriteView :
- lecture progressive 60→120→240→target ;
- le changement de banque rebascule sur la frame correspondant au temps logique écoulé au lieu de repartir de zéro ;
- filtre bitmap activé pour l’upscale des petites banques ;
- la première animation visible est comptée une seule fois par lecture et les changements de qualité ne créent pas une nouvelle instance AliveAnimator.

Persistance future APK :
- recherche d’une banque exportée sous assets/sprites/<bankKey>/manifest.json avant le disque/génération ;
- copie locale une fois pour conserver SpriteSequence<File>.

Export :
- nouvelle entrée réglages « Exporter les banques sprites » ;
- ZIP compatible future intégration assets/sprites ;
- index + manifests + frames + stable frames + rapport.

Prof/Pierre et Plante restent volontairement hors Sprite Factory car leurs backends actuels ne sont pas SpriteRGBA. Aucun ordonnanceur global n’a été recréé.
Version : 0.15.33-dev / 68.

## 2026-09-30 — GECKO-040 — Banques sprites par résolution

Règle canonique :
- une banque logique par résolution ;
- les animations restent séparées à l'intérieur de la banque ;
- lecture/génération à 12 images/s ;
- progression calculée sur les frames réellement présentes, pas seulement sur le nombre de clips ;
- états : EMPTY / IN_PROGRESS / COMPLETE / INVALID ;
- taille disque et pourcentage visibles depuis Réglages > Banques sprites / export ;
- export ZIP indépendant pour chaque résolution ;
- structure d'export : `sprites/banks/<resolution>p/<bankKey>/...` avec `bank-manifest.json` global ;
- les stable frames restent exportées séparément sous `sprites/stable-frames/`.

Correction importante : un `SPRITE_BANK_DISK_HIT` ou `APK_HIT` termine maintenant immédiatement la résolution de session. Le worker ne doit plus poursuivre jusqu'au `requireNotNull(session.metadata)` après un hit prêt.

Le catalogue de fond suit désormais les étapes progressives de la résolution cible. Exemple 480p : 60p > 120p > 240p > 480p.

## 2026-09-30 — GECKO-040B — Export complet jusqu'à 480p

Décision Fab : le bouton d'export global ne doit pas exporter des banques incomplètes. Il doit d'abord terminer les banques canoniques 60p, 120p, 240p et 480p, afficher une barre de progression calculée sur les frames réellement présentes, puis ouvrir la création du ZIP complet.

La préparation export donne priorité aux jobs EXPORT_480 sans annuler la génération déjà commencée. Les banques partielles sont reprises.

Le chemin READY a été durci : initialisation retourne explicitement READY ou BUILD_REQUIRED, les sessions terminées portent un drapeau terminal, et les tâches obsolètes quittent sans accéder à metadata. Les métriques post-completion ne peuvent plus transformer un hit disque réussi en erreur.

## 2026-09-30 — GECKO-041 — Trois banques seulement, maximum 240p

Décision canonique de Fab après validation téléphone et export complet :
- conserver uniquement les banques 60p, 120p et 240p ;
- 60p reste la banque interne rapide ;
- 120p est la banque intermédiaire ;
- 240p est la qualité maximale ;
- supprimer 180p, 360p et 480p de la sélection, de l'UI banques et de l'export ;
- au démarrage de la factory, supprimer les anciens répertoires générés suffixés -180, -360 ou -480 afin de récupérer l'espace disque ;
- l'export global prépare et exporte seulement 60p → 120p → 240p.

Le paquet historique complet 60/120/240/480 est archivé dans la release GitHub `PackageSprites`, asset `GeckoDoku-sprite-banks-60-120-240-480-20260930-2058.zip`, SHA-256 `b108331d259d6d03aa6832028327a76fb31cf3bcb882738f759121cfeafad296`.

