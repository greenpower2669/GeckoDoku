# GeckoDoku — brain

> Contrat fonctionnel courant, compact et reconstructible.
> Documentation détaillée : docs/GECKODOKU-FONCTIONNEMENT.md.
> Cartographie technique : brainmap.md.
> Archive froide avant restructuration : sauvegarde.md — ne pas lire par défaut.

## État de référence

- Branche : gecko-039-sudoku-tap-gecko-gomoku
- Version : 0.15.13-dev / versionCode 48
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
