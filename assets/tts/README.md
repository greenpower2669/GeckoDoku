# Voix locale GeckoDoku

Le laboratoire A/B GECKO-031 est terminé.

La voix locale retenue est :
- modèle Piper : `fr_FR-upmc-medium`
- locuteur : **Pierre**
- `sid = 1`
- `sid = 0` correspond à Jessica
- runtime : Sherpa-ONNX v1.13.8

Le gros modèle n'est pas commité dans Git. La CI télécharge l'archive officielle, vérifie son SHA-256 puis l'embarque sous `assets/tts/piper/pierre/`.

Archive :
`vits-piper-fr_FR-upmc-medium.tar.bz2`

SHA-256 :
`e9830a331a16f6cc5ef3116a287065e015d3495c3f56b974889a266da7f89a7f`

Le TTS Android reste uniquement un fallback en cas d'échec de Piper.
