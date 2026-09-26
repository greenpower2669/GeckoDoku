# Assets GeckoDoku

Ce dossier contient uniquement les médias riches optionnels.
Ils ne doivent jamais devenir une dépendance de la logique du jeu.

## gecko/

- `Gecko_Intro.mp4`
  - intro d'ouverture uniquement ;
  - lecture complète ;
  - texte stylisé GeckoDoku ajouté par l'application.

- `Gecko_apparition.mp4`
  - apparition d'un gecko confirmé uniquement ;
  - lecture complète.

- `Gecko_disparition.mp4`
  - disparition d'un gecko retiré uniquement ;
  - lecture complète.

- `Gecko_actions_plusieurs.mp4`
  - séquence longue d'actions mignonnes ;
  - ~30 s ;
  - PHASE ACTUELLE : lecture complète occasionnelle ;
  - aucun timecode / aucun découpage / aucune boucle.

## prof/

- `Prof.png`
  - portrait principal du Prof.

- `Prof_fb.png`
  - source fond bleu disponible pour le keycolor ;
  - ne remplace pas automatiquement `Prof.png`.

- `Prof_actions.mp4`
  - séquence longue du Prof ;
  - ~30 s ;
  - PHASE ACTUELLE : lecture complète occasionnelle en contexte Prof ;
  - aucun timecode / aucun découpage / aucune boucle.

## Règle commune

Les médias sont un habillage désactivable en overlay.
Le jeu normal doit fonctionner parfaitement sans eux.
Le son embarqué des vidéos respecte le réglage FX.


## Keycolor bleu
Le fond bleu des vidéos riches est supprimé au runtime par shader GPU OpenGL ES : dominance bleue → transparence, transition douce et réduction des franges bleues. Les seuils sont réglables dans `AssetMediaCatalog`. En cas d'échec du shader ou du décodage, l'overlay disparaît et le jeu procédural continue.


## audio/

Les MP3 autonomes sont rangés par rôle sans réencodage :
- `audio/intro/jungle intro GeckoD.mp3` : source musicale d'introduction, rôle runtime encore non activé ;
- `audio/celebration/jungle cebration GeckoD.mp3` : source musicale de célébration, rôle runtime encore non activé ;
- `audio/encouragements/master/Voix_encouragements.mp3` : master vocal intact de 14,441 s ;
- `audio/encouragements/clips/` : réservé aux 13 clips courts GECKO-023 à produire ultérieurement.

Le rangement des MP3 n'ajoute aucun code et ne modifie pas le comportement de la v0.9 en cours de test.


## Gecko statique
- `gecko/Gecko_tr.png` : **sprite Gecko canonique transparent** hors animation, fourni par Fab.
- `gecko/Gecko.png` : supprimé, ancien doublon généré par erreur.
- les MP4 Gecko restent des overlays temporaires au-dessus du rendu normal.
