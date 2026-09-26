# GeckoDoku — GECKO-033
# MISSION ACTIVE — RESTAURATION DES ANIMATIONS / SPRITES VIDÉO

Date : 26/09/2026
État téléphone de référence : v0.10.10-dev
Branche : `gecko-033-identity-prof-life`

## 1. VALIDÉ — NE PLUS RETOUCHER

### Intro 1
`assets/gecko/IntroGeckoGD.mp4`

Validation Fab :
- affichage : OK ;
- son embarqué : OK ;
- chronologie : OK.

### Intro 2
`assets/gecko/Gecko_Intro.mp4`

Validation Fab :
- enchaînement après Intro 1 : OK ;
- affichage : OK ;
- chronologie : OK.

**Conclusion : Intro 1 + Intro 2 sont parfaites et sorties de la liste des bugs.**

Interdiction de modifier leur ordre, leur son, leur déclenchement ou leur logique de skip pendant le correctif sprites, sauf preuve qu'une modification est absolument nécessaire.

### Identité
Déjà validés précédemment :
- icône launcher ;
- médaillon GeckoDoku près du titre.

Ne pas les retoucher.

---

## 2. PROBLÈME ACTIF PRINCIPAL

Les animations / sprites vidéo sont actuellement **quasi KO sur téléphone**.

Les symptômes observés incluent :
- rectangle/fond noir derrière les vidéos du Prof ;
- rectangle/fond noir derrière les animations Gecko sur la grille ;
- animations Gecko qui ne retrouvent pas leur rendu antérieur fiable ;
- `ProfParle.mp4` qui peut ne pas apparaître alors que Pierre parle ;
- coexistence multi-vidéos encore visuellement incorrecte malgré le nouveau modèle multi-sessions.

Le correctif suivant doit porter exclusivement sur cette famille de problèmes.

---

## 3. CONTRAT DES ANIMATIONS GECKO

Conserver les assets et rôles historiques :
- `Gecko_apparition.mp4` ;
- `Gecko_disparition.mp4` ;
- `Gecko_actions_plusieurs.mp4`.

Règles :
- animation visible : OUI ;
- keycolor bleu correctement détouré : OUI ;
- fond noir : INTERDIT ;
- cadrage/mask sur la bonne case : OUI ;
- audio embarqué : TOUJOURS MUET pour les animations Gecko de gameplay ;
- une animation Gecko peut fonctionner pendant que Pierre parle ;
- une animation Gecko ne doit jamais couper Pierre ;
- une animation Gecko ne doit jamais arrêter `ProfParle.mp4` ;
- une fin de vidéo Gecko ne doit jamais arrêter une autre session vidéo.

Le silence doit être obtenu par le volume/player, jamais par suppression, skip ou non-lancement de l'animation.

---

## 4. CONTRAT DU PROF

Assets :
- portrait normal : `Prof.png` ;
- animation muette locale : `Prof_actions.mp4` ;
- animation de parole : `ProfParle.mp4`.

### Priorité locale autorisée
Dans le slot visuel du Prof uniquement :

`ProfParle.mp4` > `Prof_actions.mp4`.

Si `Prof_actions.mp4` tourne et que Pierre commence réellement à parler :
1. arrêter localement `Prof_actions.mp4` ;
2. lancer `ProfParle.mp4` immédiatement ;
3. ne toucher à aucune animation Gecko concurrente.

### ProfParle
`ProfParle.mp4` suit uniquement l'état réel de parole :
- Pierre START → ProfParle START ;
- Pierre continue → ProfParle continue / reboucle si nécessaire ;
- Pierre COMPLETE → ProfParle STOP → portrait normal ;
- interruption volontaire autorisée → cohérence avec nouvelle phrase.

Si Pierre parle alors que le Prof est déjà en PNG, `ProfParle.mp4` doit quand même démarrer.

### Échec vidéo
Le flag persistant `professorSpeechVideoFailed` est suspect :
- une erreur ponctuelle ne doit pas condamner `ProfParle.mp4` pour toute la session ;
- fallback immédiat = PNG ;
- phrase future = possibilité de retenter la vidéo ;
- logs obligatoires sur erreur et retry.

---

## 5. COEXISTENCE RÉELLE

Ne jamais revenir à un arbitre global « une seule vidéo ».

Architecture attendue :
- sessions vidéo indépendantes ;
- aucun `stopPlayback()` croisé entre instances ;
- aucune fin d'une vidéo qui retire ou masque une autre ;
- aucune visibilité `GONE/INVISIBLE` appliquée au mauvais player ;
- plusieurs animations autorisées si leurs rôles sont compatibles ;
- la seule exclusivité voulue est locale au slot Prof : PROF_SPEECH remplace PROF_ACTION.

Exemple valide :

Pierre START
→ ProfParle START
→ GeckoAppearance START
→ GeckoAppearance COMPLETE
→ Pierre COMPLETE
→ ProfParle STOP

---

## 6. PRIORITÉ TECHNIQUE — FOND NOIR / TRANSPARENCE

Le fond noir apparaît sur au moins :
- vidéo Prof ;
- vidéo Gecko de grille.

Donc traiter en priorité le chemin commun de rendu :
- `ChromaKeyVideoView` ;
- renderer OpenGL ;
- Surface / SurfaceTexture ;
- `GLSurfaceView` ;
- Z-order ;
- alpha/transparence ;
- `setZOrderMediaOverlay` / ancien `setZOrderOnTop` ;
- composition de plusieurs surfaces ;
- couleur de clear du renderer ;
- blending OpenGL ;
- ordre des layers Android.

Objectif :
**retrouver le keycolor bleu détouré avec un vrai fond transparent, sans rectangle noir, y compris avec plusieurs vidéos simultanées.**

Ne pas reencoder les MP4 pour cacher le problème.

---

## 7. LOGS À CONSERVER / RENFORCER

Tag : `GeckoDokuMediaTrace`.

Vidéo :
- instance unique ;
- logicalLayer ;
- asset ;
- PLAY_REQUEST ;
- PREPARED ;
- START ;
- STOP ;
- COMPLETE ;
- ERROR ;
- Surface créée/détruite ;
- mode Z-order ;
- nombre de sessions actives.

Prof :
- `PROF_SPEECH_STATE` ;
- `PROF_SPEECH_VIDEO_REQUEST` ;
- valeur `professorSpeechVideoFailed` ;
- transition ACTION → SPEECH ;
- retry après erreur.

Voix :
- SPEAK_REQUEST(origin) ;
- SPEAK_STARTED(origin) ;
- SPEAK_STOP(reason, caller) ;
- SPEAK_COMPLETED(origin).

---

## 8. TESTS À AJOUTER AVANT CORRECTION

RED ciblés au minimum :
1. Prof PNG + Pierre START → ProfParle demandé.
2. Prof ACTION + Pierre START → ACTION stoppée localement + ProfParle demandé.
3. Gecko active + ACTION→SPEECH Prof → Gecko reste active.
4. erreur ponctuelle ProfParle → fallback PNG mais prochain speech autorise un retry.
5. fin Gecko A → Gecko/Prof B reste actif.
6. plusieurs sessions vidéo indépendantes.
7. politique de surface ne réintroduit pas de `setZOrderOnTop(true)` global.
8. animations Gecko restent visibles tout en étant muettes.

Les intros validées ne doivent pas être utilisées comme terrain d'expérimentation dans ces tests.

---

## 9. MÉTHODE FAB COPILOT

Avant code :
1. reproduire / identifier précisément le défaut ;
2. RED ciblé ;
3. correctif minimal ;
4. logs conservés ;
5. tests ;
6. build APK/AAB ;
7. validation téléphone Fab.

À chaque intervention code, synchroniser dans le même commit :
- `ordres-de-mission.md` ;
- `brain.md` ;
- `brainmap.md` ;
- `debughistorical.md` ;
- `todo.md`.

Ne pas toucher aux intros validées pour faire passer artificiellement le correctif.

---

## CONTRAT COURT

**Intro 1 et Intro 2 sont VALIDÉES et gelées. Le chantier actif est maintenant la restauration des animations/sprites vidéo : transparence correcte, aucun fond noir, ProfParle fiable, Gecko visible mais muet, et vraie coexistence des lecteurs.**


<!-- GECKO-033-SPRITES-RED-TRANSPARENCY-RETRY-2026-09-26 -->
## Cycle RED — transparence + retry ProfParle
Nouvelles preuves téléphone :
- Intro 1/2 restent gelées et ne sont pas concernées.
- Les rectangles noirs touchent Prof et Gecko, donc le rendu transparent commun est prioritaire.
- Le changement vers `setZOrderMediaOverlay(true)` est désormais suspect : l'ancien `setZOrderOnTop(true)` produisait le chroma transparent avant la refonte multi-sessions.
- Cette règle est technique, pas fonctionnelle : la coexistence reste obligatoire même si la stratégie de composition doit être corrigée.

RED ajouté :
1. une surface chroma transparente doit choisir la composition permettant réellement l'alpha, et non le MediaOverlay opaque constaté sur téléphone ;
2. un échec précédent de `ProfParle.mp4` ne doit jamais interdire la tentative de la phrase suivante.


<!-- GECKO-033-PROFPARLE-RETRY-GREEN-2026-09-26 -->
## GREEN 1 — ProfParle retry après erreur
Le flag `professorSpeechVideoFailed` reste conservé uniquement comme information de diagnostic de la tentative précédente.

Il n'est plus autorisé à bloquer une phrase future.
Une nouvelle policy `ProfessorSpeechVideoStartPolicy` autorise le lancement selon les conditions courantes :
- animations activées ;
- vidéo Prof autorisée ;
- vue prête ;
- Pierre réellement en train de parler.

Une erreur précédente n'est pas un critère de refus.
Au démarrage réussi suivant, le flag est remis à false.
