# GeckoDoku — TODO actif

## GECKO-042 — Abeilles & Geckos / 0.14

### Fondation codée dans la passe en cours
- [x] coordonnées hexagonales axiales et six voisins ;
- [x] modèle Gecko/Abeille/couple exclusif 1↔1 ;
- [x] validation anti-partage ;
- [x] solveur de matching + déductions forcées ;
- [x] générateur avec contrôle d'unicité ;
- [x] vue hexagonale exploratoire ;
- [x] pinch zoom + drag + seuil anti-faux-clic + clamp ;
- [x] recentrage dans les réglages ;
- [x] Prof sur demande avec projection source/candidats/impossibles/réservés/couple ;
- [x] session active : couples + caméra + temps + aides ;
- [x] intégration export/import ;
- [x] stats / étoiles / Hall par nouveau GameMode ;
- [x] assets `AbeilleTr.png` et `Abeillefondvert.mp4` intégrés au catalogue.

### À valider / enrichir
- [ ] CI compilée et tests GREEN ;
- [ ] test téléphone Fab : lisibilité hexagones / Gecko / Abeille ;
- [ ] test téléphone : tap vs drag vs pinch, limites caméra, recentrage ;
- [ ] test téléphone : animation Abeille fond vert correctement chroma-keyée et attachée au plateau ;
- [ ] test téléphone : fermeture/réouverture reprend couples + zoom + position + temps ;
- [ ] test téléphone : Prof explique correctement puis applique seulement en aide directe ;
- [ ] test téléphone : stats, étoiles, Hall, export/import ;
- [ ] enrichir les générateurs difficiles avec ambiguïtés et interactions régionales plus variées ;
- [ ] ajouter les notes / repères personnels propres aux cases hexagonales ;
- [ ] ajouter si utile un repère discret de zone non terminée / dernière zone Prof ;
- [ ] optimiser le rendu visible-only si les cartes finales deviennent beaucoup plus grandes ;
- [ ] non-régression complète Classic / Sudoku / Gomoku.

## Gomoku — correctif séparé connu
- [ ] Découverte : diminuer réellement la force du Prof pour qu'il ne gagne pas systématiquement ;
- [ ] explication stratégique uniquement si demandée ;
- [ ] quand demandée, exposer la vraie séquence anticipée avec repères graphiques multi-coups.

## Packaging téléphone
- [ ] après candidate validée par CI, fournir une GitHub Release avec APK directement téléchargeable plutôt qu'un ZIP d'artefact.
- [x] CI #212 a confirmé la compilation des nouvelles sources ;
- [x] corriger l'ancien test qui attendait seulement 3 GameMode ;
- [ ] relancer CI après correction du test de cardinalité.
### Jalon CI #213
- [x] CI #213 GREEN ;
- [x] APK/AAB 0.14.0-dev produits ;
- [x] repères personnels hexagonaux ajoutés après ce jalon ;
- [x] navigation vers prochaine zone non résolue ajoutée ;
- [ ] revalider ces ajouts sur la CI suivante ;
- [ ] validation téléphone.
- [x] CI #214 : diagnostiquer la collision `View.cancelLongPress()` ;
- [x] renommer le helper en `cancelPendingLongPress()` ;
- [ ] revalider sur CI suivante.
### Packaging téléphone après CI #215
- [x] CI #215 GREEN avec repères + navigation ;
- [x] ajouter une variante `phone` non débogable sans contaminer `release` ;
- [x] alléger le workflow téléphone à un APK seul ;
- [x] préparer la publication automatique en GitHub prerelease sur commit `[phone-release]` ;
- [ ] valider le build `assemblePhone` et la création réelle de la prerelease ;
- [ ] tester l'APK sur téléphone.
## GECKO-043 — mission active

### Abeilles & Geckos
- [x] abandonner le matching libre / grande carte ;
- [x] nouveau modèle solution cachée Gecko + Abeille ;
- [x] zones colorées, exactement 1 Gecko + 1 Abeille ;
- [x] contraintes trois axes hexagonaux ;
- [x] voisinage local Abeille↔Gecko dans la zone ;
- [x] solveur zone + axes + voisinage + projection ;
- [x] générateur solution → retrait givens → unicité → difficulté ;
- [x] `generateExact()` jusqu'au niveau demandé ;
- [x] tap croix / double tap choix pièce / appui long repère ;
- [x] Prof visuel bleu/orange/rouge/vert + symboles ;
- [x] compteur des couples confirmés uniquement ;
- [x] persistance du nouvel état + caméra ;
- [ ] CI de la refonte Bee GREEN ;
- [ ] test téléphone mise en page compacte / zones / gestes / Prof.

### Étoiles
- [x] ajouter `mistakes` à la note ;
- [x] 1 erreur = −3 étoiles ;
- [ ] CI + test Classic/Sudoku/Abeilles.

### Gomoku — même mission
- [ ] vraie politique de force par niveau ;
- [ ] Découverte/Facile cohérents mais non optimaux ;
- [ ] niveaux hauts gardent win/block/traps ;
- [ ] trace de raisonnement concrète ;
- [ ] overlay graphique ligne / projection / blocage ;
- [ ] explication seulement sur demande ;
- [ ] supprimer persona agressive de la 0.13.

### Livraison
- [ ] CI complète GREEN ;
- [ ] GitHub prerelease téléphone APK direct 0.15.
### GECKO-043 — Gomoku implémenté, à valider CI
- [x] vraie politique de force par niveau ;
- [x] Découverte/Facile cohérents mais parfois non optimaux ;
- [x] Hard+ garde win/block et pièges ;
- [x] trace de raisonnement concrète ;
- [x] overlay graphique ligne / menace / coup / projection ;
- [x] explication stratégique uniquement sur demande ;
- [x] persona agressive supprimée ;
- [x] tests de différenciation IA et pédagogie réécrits ;
- [ ] CI GREEN de l'ensemble GECKO-043 ;
- [ ] validation téléphone des niveaux et projections.
### GECKO-043 — contrôle post-CI #218
- [x] CI #217 GREEN : refonte Bee + étoiles ;
- [x] CI #218 GREEN : Gomoku difficulté + pédagogie graphique ;
- [x] interdire les contacts Abeille↔plusieurs Geckos et Gecko↔plusieurs Abeilles entre zones ;
- [x] appliquer cette exclusivité au générateur, solveur et test d'unicité ;
- [x] recalibrer l'acceptation de difficulté sur la mesure du solveur ;
- [ ] CI suivante GREEN ;
- [ ] publier prerelease téléphone 0.15 après GREEN final.
### GECKO-043 — état après CI #219
- [x] CI #219 GREEN ;
- [x] moteur Bee Classic-like compilé/testé ;
- [x] Gomoku difficulté/pédagogie compilé/testé ;
- [x] pénalité −3 étoiles compilée/testée ;
- [ ] publier prerelease APK 0.15 ;
- [ ] Fab : valider visuellement zones, taille, gestes et lisibilité ;
- [ ] Fab : tester Prof Bee et projection Gomoku ;
- [ ] Fab : tester les écarts de force Gomoku, surtout Découverte vs niveaux hauts ;
- [ ] ajuster ensuite uniquement les points révélés par le test téléphone.
## GECKO-044 — test téléphone 0.15.1
- [x] audit du rectangle vert Abeille ;
- [x] keycolor paramétrable BLUE/GREEN ;
- [x] Abeille forcée au keycolor VERT ;
- [x] préserver les ailes bleues ;
- [x] aligner gestes Bee sur sémantique Gomoku ;
- [x] empêcher le zoom-out au-delà du fit complet ;
- [x] recentrer automatiquement une grille plus petite que le viewport ;
- [x] clamp strict quand la grille est zoomée ;
- [x] tests viewport / gestes / sélection keycolor ;
- [ ] CI GREEN ;
- [ ] publier prerelease téléphone directe 0.15.1 ;
- [ ] Fab valide : fond vert disparu + drag/pinch satisfaisants.
