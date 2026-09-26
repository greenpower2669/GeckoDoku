# TTS expérimental GeckoDoku

Ce dossier ne contient pas les gros modèles dans Git.

La CI expérimentale GECKO-031 télécharge et vérifie :
- `vits-piper-fr_FR-siwis-low`
- `vits-piper-fr_FR-siwis-medium`

Puis elle les extrait dans :
- `assets/tts/piper/low/`
- `assets/tts/piper/medium/`

Les deux modèles sont embarqués dans l'APK/AAB expérimental et fonctionnent hors ligne à l'exécution.

Sherpa-ONNX : v1.13.8, Apache-2.0.
Voix Piper Siwis : français fr_FR ; dataset indiqué CC-BY 4.0 dans les model cards Piper.

Aucun de ces moteurs ne remplace encore la voix runtime de Prof Gecko.
