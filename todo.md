# GeckoDoku — TODO actif

## GECKO-045 — finition Abeilles & Geckos

### Validation précédente
- [x] plateau logique validé par Fab ;
- [x] zones / axes / règles validés ;
- [x] keycolor vert validé ;
- [x] ailes bleues préservées ;
- [x] candidate 0.15.1 globalement jugée très bonne.

### Mission active
- [ ] réduire le sprite Abeille à environ 50 % ;
- [ ] réduire aussi la cible vidéo Abeille à environ 50 % ;
- [ ] conserver le centrage exact sur l'hexagone ;
- [ ] créer une fenêtre carrée fixe de visualisation ;
- [ ] dimensionner le carré avec l'espace réellement disponible ;
- [ ] clipper strictement le plateau dans ce carré ;
- [ ] déplacer uniquement le contenu pendant le drag ;
- [ ] zoomer uniquement le contenu pendant le pinch ;
- [ ] conserver le point visé pendant le zoom ;
- [ ] empêcher la grille de devenir irrécupérable ;
- [ ] recentrer le contenu dans le carré ;
- [ ] clipper les overlays Prof ;
- [ ] clipper / borner l'animation Abeille ;
- [ ] garantir que HUD / boutons / Prof restent hors du plan transformable ;
- [ ] tests gestes / clipping / conversion coordonnées ;
- [ ] CI GREEN ;
- [ ] publier APK téléphone direct ;
- [ ] validation finale Fab sur navigation et taille des Abeilles.

### Interdictions de cette passe
- [ ] ne pas modifier solveur / générateur / règles ;
- [ ] ne pas modifier Classic ;
- [ ] ne pas modifier Sudoku ;
- [ ] ne pas modifier Gomoku sauf code partagé strictement nécessaire ;
- [ ] ne pas modifier le keycolor vert déjà validé.
### GECKO-045 — code 0.15.2 préparé
- [x] `BeeGeckoVisualPolicy.BEE_SCALE = 0.50` ;
- [x] sprite Abeille réduit à 50 % ;
- [x] cible vidéo Abeille réduite à 50 % ;
- [x] viewport carré calculé par policy pure ;
- [x] clipping Canvas du plateau et aides Prof ;
- [x] caméra centrée / clampée dans le carré ;
- [x] tap interdit hors fenêtre ;
- [x] vidéo masquée si sa cible sortirait du carré ;
- [x] tests policy : carré centré, caméra centrée, échelle Bee 50 % ;
- [ ] CI GREEN ;
- [ ] publier APK téléphone 0.15.2 ;
- [ ] Fab valide taille Bee et navigation interne.
- [x] aligner scale initial et zoom minimum pour supprimer le saut au premier pinch ;
### GECKO-045 — après CI #223
- [x] CI #222 GREEN ;
- [x] CI #223 GREEN après correction du premier pinch ;
- [ ] publier prerelease téléphone 0.15.2 ;
- [ ] Fab valide la fenêtre carrée, navigation et Abeilles 50 %.
