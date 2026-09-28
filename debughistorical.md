# GeckoDoku — debug historical condensé

## Jalons utiles conservés

### 0.13
- stats / Hall / étoiles / export-import consolidés ;
- Gomoku intégré aux résultats ;
- ancien malentendu « Prof méchant » corrigé ensuite : le problème était la force, pas le ton.

### 0.14
- première tentative Abeilles & Geckos trop orientée matching / exploration ;
- abandonnée après retour Fab.

### 0.15
- Abeilles & Geckos reconstruit selon l'esprit Classic :
  zones + axes + Gecko + Abeille + voisinage local ;
- solveur et générateur vérifiés ;
- difficulté Gomoku réellement différenciée ;
- Prof Gomoku avec projection graphique ;
- 1 erreur = -3 étoiles.

### 0.15.1 / GECKO-044
Retour téléphone :
- plateau validé ;
- logique validée ;
- rectangle vert de la vidéo Abeille corrigé par keycolor VERT ;
- ailes bleues préservées ;
- CI #221 GREEN ;
- candidate téléphone publiée.

## Nouveau retour Fab — GECKO-045

Fab juge le test **très bon**.

Validation :
- animation fond vert : OK ;
- plateau : OK ;
- fonctionnement général : OK.

Reste à corriger :
1. Abeilles visuellement trop grosses → environ 50 %.
2. Navigation encore insatisfaisante.
3. La bonne solution conceptuelle est une **fenêtre carrée fixe** avec le plateau déplaçable à l'intérieur.

Hypothèse d'architecture privilégiée :
- un viewport carré réel avec clipping est probablement plus robuste qu'un faux clamp de caméra ;
- le contenu (board + overlays + média) doit partager le même repère transformable ou la même conversion logique→écran ;
- aucun rendu transformable ne doit sortir du carré.

Ne pas rouvrir les sujets déjà validés tant qu'aucune régression n'est observée.
## GECKO-045 — passe code 0.15.2

Implémentation retenue après audit : ne pas créer un deuxième arbre de vues qui dupliquerait les conversions écran↔hexagone. Le carré est calculé et clippé directement dans `BeeGeckoBoardView`, ce qui garde une seule source de coordonnées.

Le drag/zoom agit désormais par rapport au carré. Les événements ACTION_DOWN hors carré sont refusés, ce qui évite d'interagir avec une zone invisible du board.

Le média Bee reste dans l'overlay global pour préserver l'architecture vidéo existante. Pour empêcher tout débordement, sa cible dynamique est réduite à 50 % puis désactivée si elle franchit le carré.

État : code préparé, CI non encore exécutée.
### GECKO-045 — prévention saut premier pinch

Audit statique post-commit : `centered()` appliquait un fit à 92 % alors que la borne de zoom utilisait le fit complet. Correction avant validation téléphone : le centrage reçoit directement `minimumScale()` comme minimum.
### CI #223 GREEN

Run `36401929872` terminé avec succès. Toutes les étapes tests/build/nommage/upload passent. `Publish phone prerelease` est volontairement skipped car le commit n'avait pas encore le marqueur `[phone-release]`.
### CI #224 / release GECKO-045

Run #224 terminé GREEN, y compris `Publish phone prerelease`. Release : `phone-0.15.2-dev-run-224`. APK 236668609 octets, SHA-256 `19394e87c34d0a14f074732ed43c3f96026db4cc9c2f9d24cff7368c155b79b2`.
