# GeckoDoku — debug historical condensé

## Jalons

### 0.15
GeckoBeeDoku reconstruit selon logique Classic + hexagones.

### 0.15.1
Keycolor Bee vert corrigé.

### 0.15.2
Viewport carré + Bee scale 50 % validés par Fab.

### 0.15.3 / GECKO-046
Repères logiques, croix 3 états, victoire vivante et brouillard introduits.
CI GREEN.

## Retour Fab après 0.15.3

Deux corrections de direction importantes.

### 1 — Repères / Prof
La palette double-clic est trop chargée.

Nouvelle UX :
- 3 choix seulement : Gecko vert / Abeille jaune / Axe rouge ;
- si Axe : seconde popup uniquement pour l’orientation ;
- exclusion d’axe = grande barre semi-transparente traversant tout le plateau ;
- barre draggable ;
- sortie du plateau = suppression.

Les grosses croix affichées par le solveur / Prof sont jugées moches.
Le Prof doit préférer les barres globales, à tester.

Les croix joueur peuvent rester temporairement mais ne sont plus le vocabulaire principal du Prof.

### 2 — Audio capture écran
Tests réels Samsung + AZ :
- Pierre audible localement ;
- Pierre absent de la vidéo enregistrée ;
- certains vieux sons inaudibles restent capturés.

Conclusion de travail :
auditer en priorité les politiques AudioPlaybackCapture et les lecteurs restés actifs.

Points à vérifier :
- AndroidManifest allowAudioPlaybackCapture ;
- AudioManager.setAllowedCapturePolicy API 29+ ;
- AudioAttributes Pierre ;
- Sherpa/Piper/AudioTrack ;
- MediaPlayer / SoundPool / vidéo ;
- différence MUTE vs STOP/RELEASE.

Aucune cause racine n’est encore déclarée tant que l’audit code n’a pas été fait.
## GECKO-047 — audit + code 0.15.4

### Audio

Cause racine très probable de Pierre absent des captures :
`VoicePcmPlayer.kt` utilisait explicitement `AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY`.

Pierre n'est pas joué via le TextToSpeech Android principal : le chemin normal est :
Sherpa OfflineTts/Piper → FloatArray PCM → `VoicePcmPlayer` → AudioTrack.

Donc modifier seulement le fallback Android TTS aurait été insuffisant.

Correction appliquée au vrai chemin PCM :
MEDIA + SPEECH + ALLOW_CAPTURE_BY_ALL.

Deuxième défaut concret :
`ChromaKeyVideoView.setMuted()` et le démarrage vidéo utilisaient uniquement `setVolume(0,0)`.
La piste audio restait décodée.
La 0.15.4 désélectionne réellement les tracks audio des vidéos muted.

Les anciens encouragements du gameplay actuel passent déjà par `ProfessorSpeech` / Pierre.
`AssetAudioPlayer.playVoiceSegment` existe encore comme chemin legacy mais reçoit désormais lui aussi des AudioAttributes capturables et des logs STOP/RELEASE.

### Axes

La petite barre locale de GECKO-046 est abandonnée visuellement.
L'axe est désormais une bande semi-transparente globale.

Classic :
- état dans GameEngine ;
- drag dans GeckoBoardView.

Bee :
- réutilisation de la persistance `logicalMarkers/excludedAxes` schema 3 ;
- aucun nouveau format de sauvegarde requis ;
- rendu global + drag.

Prof :
- GeckoBeeDoku : grosses croix retirées ;
- Classic : axes affichés selon le SolveStep ;
- X-Wing : policy vérifie la structure 2×2 des sourceCells avant de produire deux bandes réservées.

État : code préparé, CI non encore exécutée.
