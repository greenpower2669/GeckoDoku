# GeckoDoku — TODO actif

## GECKO-047 — REPÈRES

- [ ] simplifier double clic à 3 choix ;
- [ ] vert = repère Gecko ;
- [ ] jaune = repère Abeille ;
- [ ] rouge = repère Axe ;
- [ ] seconde popup uniquement pour l’axe ;
- [ ] Classic : barres horizontale / verticale ;
- [ ] GeckoBee : barres Q / R / S ;
- [ ] barre semi-transparente pleine longueur ;
- [ ] drag parallèle de la barre ;
- [ ] suppression si barre complètement hors plateau ;
- [ ] conserver caméra/drag distinct du drag de barre ;
- [ ] retirer les grosses croix du Prof ;
- [ ] faire utiliser les AxisBars au Prof si pédagogiquement utile ;
- [ ] améliorer le texte des projections deux zones / deux axes ;
- [ ] ne pas appeler X-Wing une simple projection.

## GECKO-047 — AUDIO

### Manifest / policy
- [ ] auditer AndroidManifest ;
- [ ] vérifier / ajouter allowAudioPlaybackCapture=true ;
- [ ] ne pas ajouter RECORD_AUDIO ;
- [ ] auditer AudioManager capture policy ;
- [ ] appliquer ALLOW_CAPTURE_BY_ALL sur API 29+ si pertinent ;
- [ ] log Android version + capture policy.

### Pierre
- [ ] tracer le chemin exact Sherpa/Piper/Pierre ;
- [ ] identifier lecteur final ;
- [ ] auditer AudioAttributes ;
- [ ] USAGE_MEDIA ;
- [ ] CONTENT_TYPE_SPEECH ;
- [ ] ALLOW_CAPTURE_BY_ALL API 29+ ;
- [ ] éliminer usages non capturables.

### Autres sons
- [ ] MediaPlayer ;
- [ ] SoundPool ;
- [ ] AudioTrack ;
- [ ] vidéos ;
- [ ] Media3 / ExoPlayer si présents ;
- [ ] WebView audio si présent.

### Sons fantômes
- [ ] chercher anciens encouragements ;
- [ ] chercher anciennes voix ;
- [ ] chercher lecteurs mute volume=0 ;
- [ ] remplacer faux mute par STOP / RELEASE lorsque approprié ;
- [ ] identifier précisément le son fantôme des captures.

### Logs
- [ ] [AUDIO] source / usage / content / capture ;
- [ ] [AUDIO] STOP ;
- [ ] [AUDIO] RELEASE.

### Tests
- [ ] test sans recorder ;
- [ ] test Samsung Sons multimédia ;
- [ ] Pierre présent dans vidéo ;
- [ ] vieux son supprimé absent de vidéo ;
- [ ] Pierre + vidéo sans régression ;
- [ ] volume perçu inchangé ;
- [ ] animations inchangées.

## Documentation / livraison

- [x] mission GECKO-047 documentée avant code ;
- [ ] audit ;
- [ ] code + 5 fichiers vivants même commit ;
- [ ] tests unitaires ;
- [ ] CI GREEN ;
- [ ] APK/AAB de test ;
- [ ] validation Fab ;
- [ ] pas de release définitive sans validation.
