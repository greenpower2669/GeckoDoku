# GeckoDoku — TODO actif

> Seulement le travail encore utile. Les commandes de Fab restent dans ordres-de-mission.md.

## GECKO-049 — axes hexagonaux cohérents avec le plateau

### Réalisé
- [x] diagnostic : barres réelles correctes, symboles S/R faux ;
- [x] BeeGeckoAxisGeometry centralise projection, angle et symboles ;
- [x] Q = ↖↘ / +60° ;
- [x] R = ←→ / 0° ;
- [x] S = ↙↗ / -60° ;
- [x] légende mode dérivée de la géométrie ;
- [x] popup du double-clic dérivée de la géométrie ;
- [x] libellés du Prof dérivés de la géométrie ;
- [x] tests unitaires de projection / symboles / Prof ;
- [x] version préparée : 0.15.6-dev / versionCode 41 ;
- [x] commit code : 8ccc0385c8314239976368811dab93808970e35a.

### Reste
- [x] CI #236 branche verte ;
- [x] APK téléphone 0.15.6-dev publié en prerelease ;
- [ ] validation téléphone Fab des trois axes, y compris double-clic ;
- [ ] vérifier absence de régression couleur + drag.

## GECKO-048 — prononciation Pierre + couleurs d’axes

### Réalisé
- [x] PierrePronunciationPolicy dédiée au bord Piper ;
- [x] église → eglize pour la voix ;
- [x] texte UI préservé ;
- [x] tests de prononciation ;
- [x] AxisGuideColor jaune / vert / rouge ;
- [x] ClassicAxisGuide porte la couleur ;
- [x] rendu semi-transparent ;
- [x] drag Classic conserve la couleur ;
- [x] Bee axisColors + rendu + drag ;
- [x] popup couleur après choix d’axe ;
- [x] Bee session schema 4 ;
- [x] anciennes données Bee sans couleur → rouge ;
- [x] correction CI #229 ;
- [x] CI branche verte : run #232, 28/09/2026 ;
- [x] artefact CI : GeckoDoku-v0.15.5-dev-phone, artifact 10984668170.

### Reste
- [ ] validation téléphone Fab : Pierre prononce correctement église ;
- [ ] validation téléphone Fab : affichage reste église ;
- [ ] validation téléphone Fab : jaune / vert / rouge visibles ;
- [ ] validation téléphone Fab : drag conserve la couleur ;
- [ ] validation téléphone Fab : session Bee restaure la couleur ;
- [ ] validation téléphone Fab : audio et axes GECKO-047 sans régression ;
- [ ] après validation : clore GECKO-048 et produire une release téléphone directe si demandée.

## GECKO-MEM-001 — restructuration des mémoires

- [x] snapshot froid sauvegarde.md — commit fd230d996adc0f3083ab25d1dbc4e583aaebd6de ;
- [x] documentation détaillée docs/GECKODOKU-FONCTIONNEMENT.md ;
- [x] structure discutée avec Fab : documentation → brain compact → brainmap cartographique ;
- [x] brain.md restructuré depuis la documentation ;
- [x] brainmap.md restructuré avec organigrammes ;
- [x] debughistorical.md condensé ;
- [x] sauvegarde.md laissé intact ;
- [ ] validation Fab de la nouvelle lecture Brain / Brainmap.
