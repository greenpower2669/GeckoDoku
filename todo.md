# GeckoDoku — todo.md
## ÉTAT APRÈS v0.10.11-dev — 26/09/2026

### Validé / acquis
- [x] Moteur de jeu principal actif.
- [x] Génération à solution unique + validation logique.
- [x] HumanSolver actif.
- [x] Projection de zone active.
- [x] Gecko X-Wing actif.
- [x] Hypothèses Mission Impossible / Infernal actives.
- [x] 8 difficultés actives.
- [x] Prof pédagogique actif.
- [x] Journal / Sauver / Rejouer actifs.
- [x] Stats locales actives.
- [x] Bulle Prof overlay sans reflow.
- [x] Pierre + priorités de parole.
- [x] Encouragements enregistrés + Pierre.
- [x] Célébration.
- [x] Icône launcher validée téléphone.
- [x] Médaillon titre validé téléphone.
- [x] Intro 1 + son validés téléphone.
- [x] Intro 2 + enchaînement validés téléphone.
- [x] Multi-sessions média.
- [x] Retry ProfParle après erreur transitoire.
- [x] CI #97 GREEN complet v0.10.11-dev.

### Vigilances, pas missions actives
- [ ] Surveiller toute régression de transparence/chroma sur d'autres appareils.
- [ ] Surveiller l'état Anim ON/OFF via logs si le symptôme réapparaît.
- [ ] Surveiller que ProfParle reste synchronisé avec la parole réelle.
- [ ] Surveiller la coexistence Prof/Gecko sous forte concurrence média.

### Mission
Aucun ordre de mission actif actuellement.
`ordres-de-mission.md` doit rester vide jusqu'au prochain ordre explicite de Fab.


<!-- GECKO-034-FIRST-FRAME-GATE-2026-09-26 -->
## GECKO-034 — supprimer le flash noir de première frame
- [ ] RED : une nouvelle session vidéo démarre cachée.
- [ ] RED : `onPrepared` ne rend pas la vidéo visible.
- [ ] RED : appel de `start()` ne rend pas la vidéo visible.
- [ ] RED : première frame réellement rendue → vidéo visible.
- [ ] RED : erreur avant première frame → session fermée sans affichage noir.
- [ ] RED : complete avant première frame → jamais visible.
- [ ] RED : état de visibilité indépendant pour chaque session.
- [ ] Brancher le callback `MEDIA_INFO_VIDEO_RENDERING_START` ou équivalent fiable.
- [ ] Garder `ChromaKeyVideoView` / Surface caché tant que la première frame n'est pas rendue.
- [ ] Ajouter logs `VIDEO_VISIBILITY_ARMED`, `VIDEO_FIRST_FRAME`, `VIDEO_VISIBLE`, `VIDEO_ABORT_BEFORE_FIRST_FRAME`.
- [ ] Ne pas introduire de temporisation arbitraire.
- [ ] Ne pas toucher aux intros validées.
- [ ] Ne pas modifier ZOrderOnTop / alpha / shader chroma hors nécessité démontrée.
- [ ] Ne pas modifier la coexistence multi-sessions.
- [ ] Build APK/AAB après GREEN.
- [ ] Validation téléphone : aucun flash noir au démarrage Prof.
- [ ] Validation téléphone : aucun flash noir au démarrage Gecko.


<!-- GECKO-034-GRID-GEOMETRY-GUARD-2026-09-26 -->
## GECKO-034 — garde-fou géométrie grille
- [ ] RED : rectangle de la grille identique avant création de la session vidéo.
- [ ] RED : rectangle identique pendant vidéo cachée en attente de première frame.
- [ ] RED : rectangle identique lors du passage alpha 0 → 1.
- [ ] RED : rectangle identique après fin/erreur vidéo.
- [ ] Ne jamais utiliser `GONE` si cela peut provoquer un reflow.
- [ ] Préférer `alpha=0f` / couche overlay pour cacher la surface sans modifier le layout.
- [ ] Vérifier aucun changement de weight/marges/padding/taille du parent plateau.
- [ ] Vérifier petits/grands écrans et orientation/ratios différents.
- [ ] Validation téléphone : aucune grille qui flotte, saute, se redimensionne ou se décale pendant le lancement d'une animation.
