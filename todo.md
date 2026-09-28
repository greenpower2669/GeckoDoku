# GeckoDoku — TODO actif

## GECKO-047
- [x] axes validés Fab ;
- [x] audio validé Fab ;
- [x] mission close.

## GECKO-048

### Pierre
- [x] créer PierrePronunciationPolicy ;
- [x] église → eglize ;
- [x] appliquer uniquement avant Piper ;
- [x] préserver texte UI ;
- [x] tests prononciation.

### Axes Classic
- [x] AxisGuideColor jaune / vert / rouge ;
- [x] ClassicAxisGuide porte la couleur ;
- [x] rendu couleur semi-transparente ;
- [x] drag conserve couleur ;
- [x] remplacement couleur sur même axe.

### Axes GeckoBee
- [x] axisColors ajouté aux LogicalMarks ;
- [x] rendu jaune / vert / rouge ;
- [x] drag conserve couleur ;
- [x] popup couleur après axe ;
- [x] persistance schema 4 ;
- [x] compat anciennes sauvegardes → rouge.

### Livraison
- [ ] commit code + 5 fichiers vivants ;
- [ ] CI GREEN ;
- [ ] APK téléphone 0.15.5-dev ;
- [ ] validation Fab prononciation + couleurs.
### GECKO-048 — correctif CI
- [x] identifier échec CI #229 ;
- [x] corriger argument couleur manquant pendant drag Classic ;
- [ ] CI suivante GREEN ;
- [ ] APK téléphone 0.15.5-dev ;
- [ ] validation Fab prononciation « église » et couleurs d'axes.


## GECKO-MEM-001 — Préparation restructuration des mémoires
- [x] créer `sauvegarde.md` comme snapshot froid avant restructuration — commit `fd230d996adc0f3083ab25d1dbc4e583aaebd6de` ;
- [x] ne pas modifier `brain.md` ni `brainmap.md` pendant le snapshot ;
- [x] créer une documentation fonctionnelle détaillée de l'état actuel avant restructuration ;
- [ ] relire avec Fab la structure cible de `brain.md` ;
- [ ] relire avec Fab la structure cible de `brainmap.md` ;
- [ ] seulement après validation de Fab : restructurer les mémoires actives ;
- [ ] délester l'ancien `debughistorical.md` vers `sauvegarde.md` en gardant le récent/utile.
