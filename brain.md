# GeckoDoku — brain

## État stable au 2026-09-28

Branche :
`gecko-039-sudoku-tap-gecko-gomoku`

### Validé / conservé
- Classic ;
- Sudoku ;
- Gomoku ;
- GeckoBeeDoku ;
- viewport carré Bee ;
- zoom / drag ;
- Bee scale 50 % ;
- keycolor vert ;
- logique / solveurs / générateurs ;
- stats / Hall / sauvegardes / export-import.

### GECKO-046
La 0.15.3-dev a passé la CI et a introduit :
- repères logiques ;
- croix 3 états ;
- victoire vivante ;
- brouillard ;
- persistance schema 3.

Mais Fab simplifie maintenant l’UX :
- trop de choix au double clic ;
- grosses croix du Prof jugées moches.

## Mission active : GECKO-047

### Repères
Le double clic doit devenir un sélecteur à 3 choix :
- vert = Gecko ;
- jaune = Abeille ;
- rouge = axe.

Rouge ouvre une seconde mini-popup pour choisir l’axe.

L’axe est représenté par une grande barre semi-transparente couvrant le plateau.
Cette barre est draggable parallèlement à elle-même.
Si elle est glissée hors plateau → suppression.

Le Prof ne doit plus utiliser de grosses croix.
Tester les grandes barres globales comme langage principal des exclusions d’axes.

Les croix joueur peuvent rester temporairement mais hors du langage Prof et hors de la popup double-clic.

### Pédagogie
Pour une projection de type deux zones / deux colonnes :
le Prof doit expliciter les couleurs/zones, les deux positions de chacune, les axes communs, puis seulement les exclusions.
Ne pas appeler X-Wing une simple projection.

## Mission audio active

Symptôme réel :
- Pierre audible par Fab ;
- Pierre absent de la capture Samsung / AZ en mode sons multimédia ;
- certains anciens sons inaudibles dans le jeu restent présents dans la capture.

Hypothèse :
routage / AudioAttributes / lecteurs fantômes.

Objectif :
`heard_in_game == captured_by_screen_recorder`

Et :
`disabled_sound == stopped_and_not_captured`

Audit requis :
- manifest `allowAudioPlaybackCapture` ;
- policy globale API 29+ ;
- Pierre / Sherpa / Piper ;
- MediaPlayer ;
- SoundPool ;
- AudioTrack ;
- vidéo ;
- anciens encouragements / sons remplacés.

Pierre cible :
- USAGE_MEDIA ;
- CONTENT_TYPE_SPEECH ;
- ALLOW_CAPTURE_BY_ALL sur API 29+.

Ne pas ajouter RECORD_AUDIO.
## GECKO-047 — implémentation 0.15.4-dev

### Cause Pierre trouvée

`VoicePcmPlayer` créait l'AudioTrack avec :
- USAGE_ASSISTANCE_ACCESSIBILITY ;
- CONTENT_TYPE_SPEECH.

C'est incohérent avec l'objectif AudioPlaybackCapture multimédia.

Correction :
- Pierre → USAGE_MEDIA ;
- CONTENT_TYPE_SPEECH conservé ;
- ALLOW_CAPTURE_BY_ALL sur API 29+ ;
- même policy pour Android TTS fallback.

### Son fantôme : cause plausible concrète dans le code

`ChromaKeyVideoView` mutait les vidéos par :
`MediaPlayer.setVolume(0f,0f)`

Le décodeur audio restait néanmoins actif.

Correction 0.15.4 :
- lorsque vidéo muted, recherche des tracks audio ;
- `deselectTrack()` sur les tracks audio avant démarrage ;
- setVolume(0) reste une sécurité supplémentaire ;
- STOP/RELEASE loggés.

### Audio policy globale

`AudioCapturePolicy` centralise :
- speechAttributes() = MEDIA + SPEECH ;
- musicAttributes() = MEDIA + MUSIC ;
- videoAttributes() = MEDIA + MOVIE ;
- ALLOW_CAPTURE_BY_ALL API 29+ ;
- setAllowedCapturePolicy global.

Manifest :
`allowAudioPlaybackCapture=true`.

### Axes

Classic :
`ClassicAxisGuide(kind,index)`
→ rendu pleine longueur
→ drag
→ sortie plateau = delete.

Prof Classic :
`ClassicProfessorAxisGuidePolicy`
→ REGION_LOCKED / singles / X-Wing
→ bandes d'axe.

Bee :
les `excludedAxes` existants deviennent des bandes globales dédupliquées.
Leur cellule origine ne sert qu'à identifier la valeur Q/R/S.
Drag = déplacement de cette origine vers une cellule du nouvel axe.

Le Prof Bee ne dessine plus ses grosses croix.
## GECKO-047 — CI #227 GREEN

Le commit fonctionnel 0.15.4-dev passe la CI complète.

Cause Pierre confirmée dans le code :
`VoicePcmPlayer` utilisait `USAGE_ASSISTANCE_ACCESSIBILITY`.
Le chemin réel est Sherpa/Piper → PCM → AudioTrack ; il est maintenant MEDIA + SPEECH + ALLOW_CAPTURE_BY_ALL.

Cause plausible du son vidéo fantôme :
les vidéos forcées muettes restaient décodées et étaient seulement à volume 0.
La 0.15.4 désélectionne explicitement leurs pistes audio, tout en gardant volume 0 en sécurité.

Aucun appel actif aux anciens encouragements segmentés n’a été retrouvé dans le gameplay courant ; `playEncouragement()` appelle Pierre via `ProfessorSpeech`.
Le chemin legacy `AssetAudioPlayer.playVoiceSegment` reste instrumenté et correctement stoppé/released s’il est réutilisé.

UI Prof Bee :
l’ancienne légende de croix est supprimée ; les axes exclus sont décrits par les bandes semi-transparentes.
## GECKO-047 — candidate téléphone publiée

CI #228 GREEN.
Release : `phone-0.15.4-dev-run-228`.
APK direct : `GeckoDoku-v0.15.4-dev.apk`.
SHA-256 : `a879911c7abc141bf34ded2fde31fc94c94791c690da8077fbc9b71b307080f2`.

La mission n'est pas déclarée close tant que Fab n'a pas validé la capture Samsung réelle.
