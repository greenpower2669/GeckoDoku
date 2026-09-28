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
