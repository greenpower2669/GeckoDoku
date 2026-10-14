# GECKO-PIERRE-WARMUP-003 — amorçage réel de la première phrase TTS

Date : 13/10/2026  
Branche : `feature/sudoku-visual-reasoning-v1`

## Signalement Fab

Avec Piper/Sherpa, la première lecture peut manger ou déformer le début de certains mots. La deuxième lecture de la même phrase est généralement correcte.

## Ordre fonctionnel

Au démarrage du moteur Pierre :

1. charger Sherpa/Piper ;
2. générer une courte phrase d'amorce ;
3. faire passer l'amorce dans la vraie chaîne audio sans qu'elle soit audible ;
4. attendre la fin réelle du traitement et de la lecture, jamais un timer arbitraire de durée ;
5. conserver la chaîne audio chaude ;
6. seulement ensuite autoriser la première vraie phrase.

Priorité technique : tester un `AudioTrack` réutilisable/persistant.

## Implémentation choisie

- `PierrePiperSpeechEngine` lance l'amorçage sur son worker mono-thread dès sa construction.
- Phrase d'amorce interne : `Bonjour, je suis prêt.`
- La synthèse passe par le même Sherpa/Piper, même speaker et même `VoicePcmPlayer` que la voix réelle.
- L'amorce est lue avec volume `0f` ; elle n'est pas exposée à l'utilisateur.
- `VoicePcmPlayer` utilise un `AudioTrack.MODE_STREAM` persistant.
- L'AudioTrack est réutilisé entre amorce et phrases suivantes si le sample rate est inchangé.
- La fin de lecture est déterminée par `playbackHeadPosition` et le nombre réel de frames écrites. Un poll court peut dormir pour économiser le CPU, mais **ne décide jamais de la fin**.
- La piste est mise en pause après drainage mais non libérée : elle reste allouée/chaude.
- Une première demande de parole est sérialisée derrière l'amorce sur le même executor.
- En cas d'échec Piper/AudioTrack, le fallback Android historique reste disponible.

## Traces attendues

Journal nominal au premier démarrage :

`TTS_READY`  
→ `WARMUP_GENERATE_START`  
→ `WARMUP_PLAYBACK_START muted=true`  
→ `TRACK_CREATE ... persistent=true`  
→ `PLAYBACK_DRAINED ... muted=true`  
→ `WARMUP_COMPLETE persistentTrack=true`  
→ première vraie phrase  
→ `TRACK_REUSE ...`  
→ `PLAYBACK_DRAINED ... muted=false`

## Validation

À confirmer sur Samsung :

- aucun son d'amorce perceptible ;
- premier mot de la première vraie phrase intact ;
- seconde lecture toujours correcte ;
- pas de régression vidéo Prof / callbacks de parole ;
- arrêt, pause et relance sans blocage ;
- aucune hausse anormale CPU/mémoire ;
- fallback Android encore fonctionnel si Piper échoue.

Aucun merge `main`, aucune Release ou publication sans ordre explicite de Fab.
