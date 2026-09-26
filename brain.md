# GeckoDoku — brain.md
## État fonctionnel actif — 26/09/2026

### Validé téléphone — gelé
- Intro 1 `IntroGeckoGD.mp4` : parfaite, son inclus.
- Intro 2 `Gecko_Intro.mp4` : parfaite, enchaînement correct.
- Icône launcher : validée.
- Médaillon près du titre : validé.

Ces éléments ne font plus partie du chantier actif.

### Chantier actif
Restauration des animations/sprites vidéo.

Symptômes :
- fond noir derrière Prof vidéo ;
- fond noir derrière Gecko vidéo sur grille ;
- animations Gecko quasi KO ;
- `ProfParle.mp4` pas toujours visible alors que Pierre parle ;
- suspicion de problème commun Surface/GL/chroma/Z-order ;
- suspicion de latch persistant `professorSpeechVideoFailed`.

### Contrats essentiels
- Gecko gameplay : vidéo visible, audio embarqué muet.
- Pierre ne doit pas être coupé par une action joueur normale.
- `ProfParle.mp4` suit exactement l'état réel de parole.
- Priorité locale slot Prof : PROF_SPEECH > PROF_ACTION.
- Cette priorité ne touche aucune vidéo Gecko.
- Plusieurs sessions vidéo doivent réellement coexister.
- Aucun arbitre global mono-vidéo.
- Une erreur ProfParle ponctuelle ne doit pas le désactiver définitivement.
- Les intros sont hors périmètre du correctif sauf régression démontrée.

### Diagnostic prioritaire
Auditer :
`ChromaKeyVideoView` + renderer OpenGL + Surface/SurfaceTexture + Z-order + blending + alpha + cycle de vie des sessions.

Conserver `GeckoDokuMediaTrace`.
