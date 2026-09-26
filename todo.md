# GeckoDoku — mission active — correction vidéo Prof

## Validation Fab — 26 septembre 2026

Fab valide le diagnostic et le correctif minimal suivant.

Le keycolor bleu fonctionne : si le chroma key était cassé, le bleu source resterait visible. Le rectangle noir doit donc être recherché dans la composition Android de la surface vidéo, pas dans le shader ni dans les seuils de keycolor.

Deux causes distinctes sont traitées :

1. rectangle noir autour de la vidéo du Prof ;
2. une erreur vidéo empêchait ensuite toute nouvelle lecture du Prof.

## Correctif validé

- [x] Ne pas modifier le shader chroma key ni ses seuils.
- [x] Conserver EGL RGBA 8/8/8/8, `PixelFormat.TRANSLUCENT`, alpha transparent et blending existants.
- [x] Remplacer `setZOrderOnTop(true)` par `setZOrderMediaOverlay(true)`.
- [x] Forcer le fond de `ChromaKeyVideoView` à `Color.TRANSPARENT`.
- [x] Supprimer le verrou permanent `professorVideoFailed`.
- [x] Après une erreur `MediaPlayer` : release du player, restauration du portrait et nouvelle tentative autorisée au prochain clic.
- [x] Après une erreur renderer/shader/texture externe : recréer uniquement `ChromaKeyVideoView`, puis autoriser une nouvelle tentative.
- [x] Écrire les erreurs dans `cacheDir/temp/video-error-log.txt`.
- [x] Écrire aussi dans `cacheDir/temp/log.txt` une ligne `[VIDEO ERROR] Voir temp/video-error-log.txt`.
- [x] Ajouter un test unitaire de classification erreur MediaPlayer vs erreur renderer.
- [ ] Exécuter les tests unitaires Android sur le commit de correction.
- [ ] Construire APK/AAB.
- [ ] Test téléphone Fab : vérifier disparition du rectangle noir.
- [ ] Test téléphone Fab : provoquer/observer une erreur vidéo puis vérifier que le Prof repart au clic suivant.
- [ ] Si `setZOrderMediaOverlay(true)` laisse encore un rectangle noir sur Samsung, ne pas retoucher le shader : préparer alors une migration ciblée du Prof intégré vers `TextureView/EGL`.

## Contraintes

- Préserver les vidéos et assets existants.
- Préserver le keycolor bleu actuel.
- Ne pas interrompre la parole du Prof par une animation secondaire.
- Ne pas introduire de verrou permanent après une erreur vidéo.
- Pas de refonte du lecteur tant que le correctif de composition n'a pas été testé sur téléphone.
- FAB Copilot : toute intervention de code synchronise `brain.md`, `brainmap.md`, `debughistorical.md` et `todo.md`.
