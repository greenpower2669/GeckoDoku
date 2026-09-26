# GeckoDoku — GECKO-034
# MISSION ACTIVE — SUPPRIMER LE FLASH NOIR AU DÉMARRAGE DES ANIMATIONS

Date : 26/09/2026
Branche : `gecko-033-identity-prof-life`
Base fonctionnelle : v0.10.11-dev

## Constat téléphone

Le rendu chroma est désormais correct une fois la vidéo lancée, mais il reste un **très court flash/cadre noir au démarrage** de certaines animations.

Le symptôme apparaît avant que le contenu vidéo transparent ne soit effectivement rendu :
- le conteneur / Surface existe déjà ;
- il devient visible ;
- la première frame vidéo n'est pas encore affichée ;
- Android montre brièvement la Surface vide/noire ;
- dès que la première frame arrive, le chroma transparent devient correct.

Ce correctif doit rester **mineur et local**.

## Objectif

Ne jamais afficher une surface vidéo avant que sa première frame réelle soit disponible.

Séquence cible :

`création cachée → prepareAsync → start caché → première frame réellement rendue → affichage`

## Règle technique

Lors de la création d'un `ChromaKeyVideoView` / session vidéo :

1. créer le conteneur et le player normalement ;
2. garder la vue vidéo cachée (`INVISIBLE` ou `alpha=0f`) ;
3. préparer puis démarrer la lecture sans rendre la Surface visible ;
4. attendre un signal équivalent à **MEDIA_INFO_VIDEO_RENDERING_START** / première frame rendue ;
5. seulement alors rendre la vidéo visible ;
6. si erreur avant première frame, fermer/libérer la session sans jamais montrer le rectangle noir.

Important :
- ne pas se contenter de `onPrepared()` ;
- ne pas se contenter de `MediaPlayer.start()` ;
- ne pas afficher sur simple demande de lecture ;
- le déclencheur de visibilité doit correspondre à une frame effectivement rendue.

## Non-régressions

Ne pas modifier :
- Intro 1 / Intro 2 ;
- ordre des intros ;
- audio des intros ;
- logique de Pierre ;
- priorité PROF_SPEECH > PROF_ACTION ;
- retry ProfParle ;
- architecture multi-sessions ;
- mute des animations Gecko gameplay ;
- ZOrderOnTop / transparence chroma validée en v0.10.11-dev ;
- géométrie de grille ;
- moteur logique.

## TDD attendu

Ajouter un contrat testable autour d'un petit état/policy de visibilité :

- initialement : vidéo cachée ;
- prepared : toujours cachée ;
- start appelé : toujours cachée ;
- première frame rendue : visible ;
- erreur avant première frame : jamais visible ;
- complete avant visibilité : jamais visible ;
- chaque session gère son propre état indépendamment.

## Logs

Ajouter/maintenir des traces utiles :
- `VIDEO_VISIBILITY_ARMED`
- `VIDEO_FIRST_FRAME`
- `VIDEO_VISIBLE`
- `VIDEO_ABORT_BEFORE_FIRST_FRAME`

Inclure l'id de session / logicalLayer / asset.

## Critère de réussite téléphone

Au déclenchement d'une animation :
- aucun flash noir ;
- rien n'est visible tant que la première frame n'est pas prête ;
- l'animation apparaît directement avec son chroma transparent ;
- aucun délai artificiel perceptible ;
- coexistence avec les autres vidéos inchangée.

## Contrat court

**Créer la vidéo cachée, la démarrer cachée, attendre la première frame réellement rendue, puis seulement l'afficher.**


<!-- GECKO-034-GRID-GEOMETRY-GUARD-2026-09-26 -->
## Garde-fou supplémentaire — grille géométriquement immuable

Retour Fab : selon certains affichages, un objet relatif peut perturber la grille ou donner l'impression qu'elle flotte/se décale.

Pour GECKO-034, le gate de première frame ne doit **jamais** modifier le layout de la grille.

Règles strictes :
- aucun `View.GONE` sur un élément dont la présence influence la mesure du plateau ;
- aucun changement de `layout_weight`, marge, padding, hauteur ou largeur du parent de la grille ;
- aucun conteneur média ajouté dans le flux vertical/relatif du plateau ;
- les vidéos restent dans un overlay indépendant au-dessus du rendu normal ;
- masquer une vidéo avant sa première frame doit agir uniquement sur la couche média (`alpha=0f` ou visibilité sans reflow), pas sur la géométrie du parent ;
- passage à visible après première frame = aucun `requestLayout()` volontaire sur la grille ;
- le rectangle réel du plateau avant/durant/après animation doit rester identique.

Test/non-régression attendu :
`boardRectBefore == boardRectDuringHiddenVideo == boardRectAfterFirstFrame == boardRectAfterVideo`.

Si une solution de visibilité provoque un re-layout même sur certains appareils/taille d'écran, elle est rejetée.


<!-- GECKO-034-RED-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## Cycle TDD RED ouvert
Deux causes sont désormais séparées :
1. la Surface vidéo est présentée avant sa première frame OpenGL réellement consommée ;
2. la grille reste enfant pondéré du LinearLayout, donc sa géométrie peut encore suivre les variations de hauteur des éléments voisins.

RED :
- `FirstFrameVisibilityGate` : invisible jusqu'à la première frame rendue, abort irréversible pour la tentative ;
- `BoardGeometryPolicy` : fige le rectangle de la grille tant que les dimensions de fenêtre restent identiques, et autorise un nouvel ancrage uniquement lors d'un vrai changement de fenêtre.


<!-- GECKO-034-GREEN-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## GREEN implémenté — à valider CI/téléphone
- vidéos non-intro alpha 0 jusqu'à une frame SurfaceTexture fraîche réellement dessinée par OpenGL ;
- stop/error/complete avant la première frame = aucune révélation ;
- mask Gecko et portrait Prof suivent la première frame ;
- chemin des intros laissé inchangé ;
- la vraie GeckoBoardView sort du LinearLayout pondéré ;
- un boardAnchor transparent réserve l'espace ;
- BoardGeometryPolicy fige le rectangle dans la même fenêtre et réancre seulement sur resize/insets.


<!-- GECKO-034-V01012-CANDIDATE-2026-09-26 -->
## Candidate téléphone v0.10.12-dev
Preuves :
- #103 RED attendu sur les deux nouvelles policies ;
- #104 GREEN complet : tests, APK, AAB et artifact.

La candidate téléphone GECKO-034 est versionCode 23 / versionName 0.10.12-dev.
