# GeckoDoku — GECKO-036
# MISSION ACTIVE — ZÉRO TROU VISUEL ENTRE PROF.PNG ET PROF_PARLE

Date : 27/09/2026
Branche : `gecko-033-identity-prof-life`
Base : v0.10.13-dev

## Retour téléphone Fab

Le temps d'attente avant Pierre / ProfParle est désormais correct, mais pendant cette attente il arrive parfois qu'aucune image du Prof ne soit visible.

Le fallback statique `prof/Prof.png` doit rester affiché pendant toute la préparation de `ProfParle.mp4`.

## Contrat visuel cible

Séquence obligatoire :

`Prof.png visible → préparation ProfParle cachée → première frame tenue → démarrage Pierre → révélation ProfParle → seulement alors masquer Prof.png`

Il ne doit jamais exister un intervalle où :
- `Prof.png` est caché ;
- et `ProfParle.mp4` n'est pas encore réellement visible.

## Règles absolues

- `Prof.png` reste visible durant tout le pré-roll.
- La création de la Surface vidéo ne masque jamais le PNG.
- `onPrepared`, `start()`, `VIDEO_RENDERING_START` ou la simple disponibilité du player ne masquent jamais le PNG.
- La première frame tenue seule ne masque pas encore le PNG.
- Le PNG n'est masqué qu'au moment où `revealHeldFirstFrame()` réussit réellement.
- Si `revealHeldFirstFrame()` échoue : garder le PNG.
- Si timeout du pré-roll : garder le PNG.
- Si erreur MediaPlayer / Surface / shader : garder le PNG.
- Si la vidéo termine ou plante ensuite : restaurer immédiatement le PNG.
- La voix de Pierre garde sa priorité : ce correctif visuel ne doit jamais retarder davantage, couper ou bloquer Pierre.

## TDD RED avant production

Ajouter des tests ciblés pour garantir :
- état initial : PNG visible, vidéo cachée ;
- pendant prepare : PNG visible ;
- après première frame tenue : PNG toujours visible ;
- reveal réussi : vidéo visible puis PNG masqué ;
- reveal échoué : PNG reste visible ;
- timeout : PNG reste visible ;
- erreur avant première frame : PNG reste visible ;
- erreur après reveal : retour immédiat PNG ;
- aucune étape ne modifie le rectangle de la grille.

## Non-régressions

Préserver intégralement :
- grille flottante et géométriquement immuable ;
- anti-flash noir GECKO-034 ;
- pré-roll / timeout GECKO-035 ;
- Pierre prioritaire sur la vidéo ;
- bouton ! ;
- menu ⚙️ ;
- journal média persistant ;
- intros validées ;
- coexistence vidéo multi-session ;
- animations Gecko ;
- transparence chroma.

## Critère téléphone

Fab ne doit jamais voir :
- un trou vide ;
- un Prof qui disparaît pendant l'attente ;
- un clignotement noir.

Le Prof doit toujours être représenté visuellement :
- PNG pendant l'attente / fallback ;
- vidéo seulement lorsqu'elle est réellement prête et visible.

## Contrat court

**Prof.png reste visible jusqu'au moment exact où ProfParle est réellement révélé.**


<!-- GECKO-036-QUICK-TALK-BUBBLE-2026-09-27 -->
# Extension GECKO-036 — les 100 phrases doivent vivre dans la bulle Prof

Retour téléphone Fab confirmé par captures :
- le bouton `!` fait bien parler une phrase de `PierreSmallTalk` ;
- mais le texte complet apparaît actuellement dans la ligne `status` au-dessus de la grille ;
- la phrase doit apparaître dans la vraie bulle de dialogue `ProfessorBubbleView`.

## Comportement cible du bouton !

Séquence :
`! → choisir phrase → ouvrir/mettre à jour bulle Prof → préparer ProfParle → Pierre parle → animation synchronisée`

Règles :
- la bulle affiche exactement la phrase choisie ;
- la phrase complète ne doit plus être injectée dans `status.text` ;
- `status` peut rester générique (`Prof Gecko`) ou conserver son rôle d'état court ;
- la bulle est un overlay et ne doit jamais déplacer/redimensionner la grille ;
- la fermeture manuelle de la bulle ne coupe pas Pierre ;
- l'anti-répétition des 100 phrases reste inchangé.

## Garde-fou important : aucune double parole

La méthode actuelle `showProfessorBubble(message)` affiche la bulle **et appelle aussi la parole avec origin PROF_BUTTON**.

Le bouton `!` parle déjà via `speakWithProfessorVisual(..., QUICK_TALK)`.

Donc la correction ne doit **pas** appeler directement l'actuel `showProfessorBubble(message)` après avoir lancé QUICK_TALK, sinon Pierre risquerait de parler deux fois / de remplacer sa propre phrase.

Prévoir une séparation propre :
- affichage seul de la bulle (ex. helper visuel dédié) ;
- parole déclenchée exactement une seule fois avec `SpeechOrigin.QUICK_TALK`.

## RED supplémentaires
- `!` accepté → phrase visible dans ProfessorBubbleView ;
- phrase complète absente de `status.text` ;
- une seule demande de parole pour une pression sur `!` ;
- origin reste QUICK_TALK ;
- fermeture bulle ≠ stop Pierre ;
- rectangle de grille inchangé.


<!-- GECKO-036-RED-2026-09-27 -->
## TDD RED ouvert
Trois contrats RED sont ajoutés avant production :
- portrait PNG visible jusqu'à révélation vidéo réellement réussie et restauré sur toute erreur ;
- une frame résiduelle d'un playback précédent ne peut jamais valider le pré-roll du playback courant ;
- le bouton ! présente la phrase dans la bulle, garde un status court et n'émet qu'une seule requête QUICK_TALK.


<!-- GECKO-036-GREEN-POLICIES-2026-09-27 -->
## GREEN étape 1 — policies
Ajout des policies de continuité portrait, validation de frame fraîche par génération de playback, et présentation QUICK_TALK bulle/status/origin unique.
