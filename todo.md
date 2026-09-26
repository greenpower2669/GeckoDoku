# GeckoDoku — todo.md
## GECKO-033 — mission active sprites vidéo

### Validé téléphone
- [x] Intro 1 `IntroGeckoGD.mp4` parfaite.
- [x] Son Intro 1 correct.
- [x] Intro 2 `Gecko_Intro.mp4` parfaite.
- [x] Enchaînement Intro 1 → Intro 2 correct.
- [x] Icône launcher validée.
- [x] Médaillon titre validé.

### À ne pas retoucher
- [ ] Ne pas modifier les intros validées pendant le correctif sprites sans cause démontrée.

### Bugs actifs
- [ ] Corriger rectangle/fond noir derrière vidéo Prof.
- [ ] Corriger rectangle/fond noir derrière vidéo Gecko.
- [ ] Restaurer animations Gecko de grille fiables.
- [ ] Garder ces animations visibles mais audio embarqué muet.
- [ ] Garantir ProfParle lorsque Pierre parle, même depuis état PNG.
- [ ] Garantir PROF_SPEECH > PROF_ACTION localement.
- [ ] Ne jamais stopper une session Gecko lors de cette bascule Prof.
- [ ] Auditer puis supprimer le verrou permanent `professorSpeechVideoFailed` si confirmé.
- [ ] Autoriser retry ProfParle après erreur transitoire.
- [ ] Vérifier Anim ON/OFF via logs, sans modifier le réglage à cause d'une erreur vidéo.

### Diagnostic / TDD
- [ ] RED : PNG + Pierre START → ProfParle demandé.
- [ ] RED : ACTION + Pierre START → ACTION stop local + SPEECH start.
- [ ] RED : Gecko active pendant bascule Prof → Gecko reste active.
- [ ] RED : erreur ProfParle → retry possible au speech suivant.
- [ ] RED : fin session A ne stoppe pas session B.
- [ ] Auditer ChromaKeyVideoView / OpenGL blending / Surface / Z-order.
- [ ] Vérifier transparence avec deux vidéos simultanées.
- [ ] Conserver et exploiter GeckoDokuMediaTrace.

### Livraison
- [ ] Tests unitaires GREEN.
- [ ] Build Android GREEN.
- [ ] APK + AAB.
- [ ] Test téléphone Fab.


<!-- GECKO-033-SPRITES-RED-TRANSPARENCY-RETRY-2026-09-26 -->
## Cycle RED sprites
- [x] Réaffirmer le rôle durable de brain.md.
- [x] RED : composition transparente attendue au lieu du MediaOverlay noir observé.
- [x] RED : échec ProfParle précédent n'interdit pas le retry suivant.
- [ ] Observer RED CI pour les bonnes raisons.
- [ ] Corriger minimalement la composition Surface.
- [ ] Supprimer le latch permanent ProfParle via une politique de démarrage testée.
- [ ] Conserver coexistence multi-sessions.
- [ ] Build + APK/AAB + validation téléphone.


<!-- GECKO-033-PROFPARLE-RETRY-GREEN-2026-09-26 -->
## GREEN retry ProfParle
- [x] Ajouter ProfessorSpeechVideoStartPolicy.
- [x] Retirer l'échec précédent des critères de blocage.
- [x] Conserver le flag seulement pour diagnostic.
- [x] Réinitialiser le flag au prochain START réussi.
- [ ] Observer le prochain RED sur la stratégie de transparence.
- [ ] Corriger le rectangle noir.


<!-- GECKO-033-TRANSPARENT-SURFACE-GREEN-2026-09-26 -->
## GREEN transparence
- [x] RED #95 isolé : 43 tests, 1 échec surface.
- [x] Restaurer ZOrderOnTop pour la transparence.
- [x] Désactiver MediaOverlay par défaut.
- [x] Conserver alpha EGL / TRANSLUCENT / shader.
- [x] Conserver multi-sessions indépendantes.
- [x] Ajouter log SURFACE_POLICY.
- [ ] Vérifier suite complète GREEN.
- [ ] Bump version APK de test.
- [ ] Validation téléphone : fond noir Prof disparu.
- [ ] Validation téléphone : fond noir Gecko disparu.
- [ ] Validation téléphone : ProfParle retry fonctionnel.
