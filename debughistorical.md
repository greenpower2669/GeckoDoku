# GeckoDoku — debug historical condensé

## GECKO-047
Validé par Fab le 2026-09-28 :
- axes parfaits ;
- audio parfait ;
- mission close.

Cause audio historique :
Pierre passait par AudioTrack ACCESSIBILITY.
Corrigé en MEDIA + SPEECH + ALLOW_CAPTURE_BY_ALL.
Les vidéos muted désélectionnent leurs pistes audio.

## GECKO-048

Retour Fab :
- conserver totalement les axes actuels ;
- ajouter choix de couleur jaune / vert / rouge après choix axe ;
- améliorer Pierre : « église » doit être envoyé au moteur comme « eglize ».

Choix d’architecture :
- correction prononciation uniquement au bord Piper ;
- UI non modifiée ;
- enum AxisGuideColor partagé ;
- ClassicAxisGuide porte sa couleur ;
- GeckoBee conserve excludedAxes et ajoute axisColors pour compatibilité ;
- Bee session passe schema 4 avec lecture schema 2/3 conservée.

Version cible :
0.15.5-dev / versionCode 40.
