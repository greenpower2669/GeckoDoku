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
