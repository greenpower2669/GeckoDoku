# GECKODOKU — ORDRE DE MISSION ACTIF

## GECKO-047 — BARRES D’AXES GLOBALES + AUDIO CAPTURABLE

Date : 2026-09-28  
Branche : `gecko-039-sudoku-tap-gecko-gomoku`

---

# ÉTAT DE RÉFÉRENCE

Les acquis précédents restent valides sauf indication contraire explicite dans cette mission.

Conserver notamment :

- Classic fonctionnel ;
- Sudoku fonctionnel ;
- Gomoku fonctionnel ;
- GeckoBeeDoku / Abeilles & Geckos fonctionnel ;
- viewport carré Bee ;
- zoom / drag ;
- Abeilles réduites ;
- keycolor vert ;
- solveurs / générateurs ;
- Prof Gecko ;
- sauvegardes / stats / Hall / export-import ;
- musique / animations déjà présentes tant qu’elles ne contredisent pas la mission audio.

La présente mission remplace les choix précédents concernant :

- la grosse palette double-clic ;
- les petites barres locales d’axe ;
- l’usage des grosses croix par le Prof / solveur.

---

# PARTIE A — REPÈRES D’AXES SIMPLIFIÉS

## 1 — DOUBLE CLIC : SEULEMENT 3 CHOIX VISUELS

Le double clic doit être simplifié.

Ne plus afficher une longue liste de commandes.

Afficher un petit sélecteur visuel à **3 choix** :

### VERT
Repère Gecko.

### JAUNE
Repère Abeille.

### ROUGE
Repère / exclusion d’axe.

Le but est que le double clic soit immédiat et lisible.

---

## 2 — CHOIX D’AXE DANS UNE SECONDE PETITE POPUP

Si le joueur choisit le repère ROUGE / AXE :

ouvrir une seconde petite popup demandant seulement l’axe.

### Classic
Proposer les axes pertinents du plateau carré :
- horizontal ;
- vertical.

### GeckoBeeDoku
Proposer les trois familles d’axes hexagonaux :
- ↖↘ ;
- ↑↓ ;
- ↗↙.

Ne pas mélanger toutes les commandes dans la première popup.

---

## 3 — UNE BARRE SEMI-TRANSPARENTE SUR TOUTE LA LONGUEUR

Une exclusion d’axe ne doit plus être une petite barre sur une seule case.

Elle doit devenir une **grande bande / barre semi-transparente** traversant toute la longueur utile du plateau dans l’axe choisi.

Caractéristiques :

- semi-transparente ;
- lisible mais non envahissante ;
- attachée au repère logique du plateau ;
- suit zoom / drag dans GeckoBeeDoku ;
- ne masque pas les pièces ;
- longueur couvrant l’axe entier visible / logique.

---

## 4 — BARRE DÉPLAÇABLE PAR DRAG

Une fois créée, la barre d’axe doit pouvoir être déplacée par drag.

Le joueur doit pouvoir :

1. poser une barre d’un axe donné ;
2. la saisir ;
3. la faire glisser parallèlement à elle-même ;
4. la déplacer sur la ligne / colonne / axe voulu.

Le drag de la barre doit être distinct du drag de caméra.

Prévoir une zone tactile suffisamment large pour être confortable.

---

## 5 — SUPPRESSION NATURELLE PAR SORTIE DU PLATEAU

Si le joueur fait glisser une barre complètement hors du plateau :

→ supprimer cette barre.

Pas besoin de bouton « supprimer ».

Cette règle doit être cohérente dans Classic et GeckoBeeDoku.

---

## 6 — REPÈRES GECKO / ABEILLE

Les repères de pièce restent simples :

### Gecko
- vert ;
- clairement identifiable ;
- discret.

### Abeille
- jaune ;
- clairement identifiable ;
- légèrement excentré pour ne pas se confondre avec le Gecko.

Ces repères sont des notes / projections du joueur, pas des placements validés.

---

# PARTIE B — PROF / SOLVEUR : SUPPRIMER LES CROIX MOCHES

## 7 — PAS DE GROSSES CROIX DE PROF

Retirer les grosses croix graphiques actuellement utilisées par le Prof / solveur pour montrer les exclusions.

Fab les trouve trop lourdes visuellement.

Le Prof peut conserver :
- surbrillances ;
- contours ;
- repères Gecko / Abeille ;
- projections ;
- barres d’axes globales.

Mais éviter les grandes croix répétées sur la grille.

---

## 8 — TESTER LES BARRES GLOBALES POUR LES EXPLICATIONS

Quand le solveur / Prof explique une exclusion d’axe :

préférer une **barre globale semi-transparente** correspondant à l’axe concerné.

Exemple :

« Cette zone couleur A n’a plus que deux positions, toutes les deux sur ces deux colonnes.
La zone couleur B possède exactement les mêmes deux colonnes possibles.
Ces deux colonnes sont donc réservées à A et B, dans un ordre ou dans l’autre.
Par projection, les autres candidats sur ces colonnes sont exclus. »

Le Prof doit montrer :
- les deux zones concernées ;
- les quatre positions possibles si nécessaire ;
- les deux axes réservés ;
- les barres globales correspondant aux axes ;
- les exclusions qui en découlent.

Ne pas appeler cela X-Wing si la structure à 4 positions / 2×2 n’est pas réellement établie.

Si ce n’est qu’une projection :
dire explicitement **projection**.

---

## 9 — CROIX JOUEUR

Les croix personnelles déjà présentes peuvent rester pour le moment si elles servent au joueur.

Mais :

- elles ne doivent plus être le langage principal du Prof ;
- elles ne doivent pas encombrer le double clic ;
- elles pourront être réévaluées après test téléphone.

---

# PARTIE C — AUDIO : RENDRE PIERRE ET TOUS LES SONS CAPTURABLES

## 10 — CONTEXTE DE TEST RÉEL

Test effectué sur téléphone Samsung avec :

- AZ Screen Recorder ;
- enregistreur d’écran Samsung natif ;
- mode « Sons multimédia ».

Résultat :

- certains anciens sons GeckoDoku, pourtant devenus inaudibles dans le jeu, apparaissent encore dans l’enregistrement ;
- Pierre est audible par Fab dans le casque / téléphone ;
- MAIS Pierre n’est pas présent dans la vidéo enregistrée.

Conclusion de travail :

le problème est probablement dans le routage / la politique audio de GeckoDoku, pas dans le screen recorder.

---

## 11 — OBJECTIF AUDIO ABSOLU

Obtenir :

### Ce que Fab entend dans GeckoDoku
=
### ce que l’enregistreur Samsung capture

pour tous les sons normaux du jeu.

Et inversement :

### Son supprimé / arrêté
=
### absent à l’oreille ET absent de l’enregistrement.

---

# PARTIE D — MANIFEST ANDROID

## 12 — AUTORISER LA CAPTURE DE LECTURE AUDIO

Auditer `AndroidManifest.xml`.

Dans `<application>`, vérifier / ajouter explicitement :

`android:allowAudioPlaybackCapture="true"`

Ne pas ajouter `RECORD_AUDIO` pour résoudre ce problème.

GeckoDoku ne doit pas enregistrer le micro.

Il doit uniquement autoriser Android à capturer sa propre lecture audio.

---

# PARTIE E — POLITIQUE GLOBALE DE CAPTURE

## 13 — ANDROID 10+ / API 29+

Auditer l’application d’une politique globale de capture.

Sur API 29+ utiliser, lorsque pertinent :

`AudioManager.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)`

Protéger par version Android.

Ne pas casser les versions plus anciennes.

Au démarrage, logger :

- version Android ;
- politique demandée ;
- politique appliquée ;
- éventuelle impossibilité / exception.

---

# PARTIE F — PIERRE / TTS

## 14 — AUDIT PRIORITAIRE DU CHEMIN PIERRE

Identifier précisément le moteur utilisé par Pierre et son chemin audio réel.

Auditer :

- moteur TTS / Sherpa / Piper ;
- AudioTrack éventuel ;
- MediaPlayer éventuel ;
- AudioAttributes ;
- Usage ;
- ContentType ;
- stream ;
- policy de capture.

Pierre doit être considéré comme :

- contenu = SPEECH ;
- usage = MEDIA ;
- capture = ALLOW_CAPTURE_BY_ALL.

Cible conceptuelle API 29+ :

`AudioAttributes.Builder()`
`.setUsage(AudioAttributes.USAGE_MEDIA)`
`.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)`
`.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)`
`.build()`

Sur API plus ancienne :
appliquer les attributs disponibles sans appeler une API inexistante.

---

## 15 — INTERDICTIONS POUR PIERRE

Vérifier que Pierre n’utilise pas accidentellement :

- `USAGE_ASSISTANT` ;
- `USAGE_VOICE_COMMUNICATION` ;
- `ALLOW_CAPTURE_BY_NONE` ;
- un stream système non capturable ;
- un AudioTrack créé avec des attributs incompatibles avec AudioPlaybackCapture.

Pierre doit rester une voix parlée, mais passer par un chemin média capturable.

---

# PARTIE G — MP3 / MUSIQUES / EFFETS

## 16 — AUDIT DE TOUS LES LECTEURS

Auditer au minimum :

- MediaPlayer ;
- Media3 / ExoPlayer si présents ;
- SoundPool ;
- AudioTrack ;
- lecteurs vidéo ;
- Sherpa / Piper ;
- WebView audio si présent ;
- autres moteurs internes.

Pour chaque source audible par le joueur :

- préférer `USAGE_GAME` ou `USAGE_MEDIA` selon le rôle ;
- autoriser `ALLOW_CAPTURE_BY_ALL` sur API 29+ ;
- ne pas modifier volume / priorité sans nécessité.

---

# PARTIE H — ANCIENS SONS FANTÔMES

## 17 — NE PLUS CONFONDRE MUTE ET STOP

Le test montre qu’un ancien son « supprimé » du jeu reste capturable.

Auditer la différence entre :

### réellement arrêté
- stop ;
- pause si reprise nécessaire ;
- release si fin définitive.

### seulement masqué
- setVolume(0,0) ;
- mute logiciel ;
- piste à volume zéro ;
- routage alternatif ;
- lecteur toujours actif.

Un son qui ne doit plus exister ne doit plus continuer à jouer silencieusement.

---

## 18 — RECHERCHER LES ANCIENS ENCOURAGEMENTS

Chercher notamment :

- anciens encouragements audio remplacés par Pierre ;
- anciennes voix ;
- anciens jingles ;
- sons associés à anciennes animations ;
- lecteurs conservés après changement de média.

Identifier précisément la source fantôme entendue dans les captures écran.

Documenter :
- asset ;
- lecteur ;
- déclencheur ;
- raison pour laquelle il continuait ;
- correction.

---

# PARTIE I — VIDÉOS

## 19 — PISTES AUDIO DES VIDÉOS

Auditer les vidéos qui contiennent du son.

Leur audio doit :

- rester audible normalement ;
- être capturable ;
- ne pas réveiller un ancien son ;
- ne pas couper Pierre ;
- conserver les règles actuelles de coexistence visuelle.

Cette mission concerne le routage audio.

Ne pas réécrire la logique vidéo si elle n’est pas en cause.

---

# PARTIE J — LOGS AUDIO

## 20 — LOGS DE DIAGNOSTIC LISIBLES

Ajouter temporairement des logs homogènes.

Exemples :

`[AUDIO] source=PIERRE usage=MEDIA content=SPEECH capture=ALLOW_ALL`

`[AUDIO] source=MUSIC usage=GAME capture=ALLOW_ALL`

`[AUDIO] source=VIDEO usage=MEDIA capture=ALLOW_ALL`

Lors d’un arrêt :

`[AUDIO] STOP source=...`

Lors d’une libération :

`[AUDIO] RELEASE source=...`

Le but est de voir immédiatement si un vieux lecteur reste vivant.

---

# PARTIE K — TESTS AUDIO

## 21 — TEST A : SANS RECORDER

Vérifier :

- musique ;
- effets ;
- Pierre ;
- vidéos ;
- volume ;
- synchronisation.

Aucune régression audible.

---

## 22 — TEST B : ENREGISTREUR SAMSUNG

Mode :

« Sons multimédia ».

Faire parler Pierre.

Résultat attendu :

### Pierre est présent dans la vidéo enregistrée.

---

## 23 — TEST C : ANCIEN SON SUPPRIMÉ

Déclencher les anciennes situations concernées.

Résultat attendu :

le son supprimé est absent :

- à l’oreille ;
- dans la vidéo enregistrée.

---

## 24 — TEST D : PIERRE + VIDÉO / ANIMATION

Faire parler Pierre pendant les séquences habituelles.

Résultat attendu :

- aucune nouvelle coupure ;
- aucune régression visuelle ;
- voix capturable ;
- coexistence actuelle préservée.

---

# PARTIE L — NE PAS FAIRE

Ne pas :

- ajouter `RECORD_AUDIO` ;
- demander « afficher par-dessus les autres applications » comme pseudo-correctif ;
- enregistrer le micro nous-mêmes ;
- modifier artificiellement le volume de Pierre ;
- réintroduire les anciens encouragements ;
- casser la synchro Pierre / animation ;
- considérer le manifeste seul comme solution complète ;
- publier une release définitive sans validation Fab.

---

# PARTIE M — DOCUMENTATION FAB COPILOT

## 25 — FICHIERS VIVANTS

Toute intervention de code doit mettre à jour dans le même commit :

- `ordres-de-mission.md` ;
- `brain.md` ;
- `brainmap.md` ;
- `debughistorical.md` ;
- `todo.md`.

Documenter :

- cause trouvée ;
- lecteurs concernés ;
- AudioAttributes avant / après ;
- ancien son fantôme identifié ou non ;
- fichiers modifiés ;
- tests ;
- résultat CI ;
- APK/AAB généré.

---

# CRITÈRES DE VALIDATION

## Repères

- [ ] double clic réduit à 3 choix visuels ;
- [ ] choix ROUGE ouvre seulement le choix d’axe ;
- [ ] barre semi-transparente sur toute la longueur du plateau ;
- [ ] barre déplaçable par drag ;
- [ ] barre supprimée si glissée hors plateau ;
- [ ] Prof n’utilise plus les grosses croix ;
- [ ] Prof peut utiliser les barres globales pour ses projections ;
- [ ] Classic et GeckoBeeDoku cohérents.

## Audio

- [ ] `allowAudioPlaybackCapture=true` vérifié ;
- [ ] policy globale API 29+ vérifiée ;
- [ ] Pierre = MEDIA + SPEECH + ALLOW_ALL ;
- [ ] musique / effets / vidéo capturables ;
- [ ] aucun ancien son fantôme ;
- [ ] logs audio cohérents ;
- [ ] test Samsung : Pierre enregistré ;
- [ ] aucun changement de volume perçu ;
- [ ] aucune régression animation / vidéo / pédagogie.

---

# PRINCIPE DIRECTEUR

## VISUEL
**Double clic simple → couleur → axe si nécessaire → grande barre globale draggable.**

## AUDIO
**Ce que Fab entend = ce que Samsung enregistre.  
Ce qui est supprimé = réellement arrêté.**
---

## GECKO-047 — implémentation 0.15.4-dev

Version cible : **0.15.4-dev** / versionCode **39**.

### Repères / axes

Implémenté :
- Classic : double clic simplifié à 3 choix : Gecko / Hypothèse / Axe ;
- GeckoBeeDoku : double clic simplifié à 3 choix : Gecko / Abeille / Axe ;
- choix Axe dans une seconde petite popup ;
- Classic : axes horizontal / vertical ;
- GeckoBeeDoku : axes Q / R / S ;
- grandes bandes semi-transparentes sur toute la ligne logique ;
- drag direct des bandes ;
- sortie complète du plateau = suppression ;
- drag d'une bande prioritaire sur le drag caméra ;
- Prof Classic : détection des axes réservés et affichage par grandes bandes ;
- Prof GeckoBeeDoku : mêmes logicalMarkers mais rendus en bandes globales ;
- grosses croix Prof supprimées dans GeckoBeeDoku ; les exclusions restent indiquées par contours / bandes ;
- texte X-Wing / projection Classic rendu plus explicite : 4 positions, axes réservés, puis exclusions.

### Audio

Cause concrète trouvée pour Pierre :
- `VoicePcmPlayer` utilisait `USAGE_ASSISTANCE_ACCESSIBILITY`.
- Pierre est maintenant routé en `USAGE_MEDIA + CONTENT_TYPE_SPEECH`.
- sur API 29+, `ALLOW_CAPTURE_BY_ALL` est appliqué.

Manifeste :
- `android:allowAudioPlaybackCapture="true"` ajouté.

Policy globale :
- `AudioManager.setAllowedCapturePolicy(ALLOW_CAPTURE_BY_ALL)` sur API 29+ ;
- log au démarrage.

Android TTS fallback :
- MEDIA + SPEECH + ALLOW_ALL.

Musique / MP3 :
- MediaPlayer reçoit des AudioAttributes MEDIA capturables.

Vidéo :
- MediaPlayer reçoit MEDIA + MOVIE + ALLOW_ALL ;
- correction importante des vidéos muettes : on ne se contente plus de `setVolume(0,0)` ;
- leurs pistes audio sont explicitement désélectionnées quand la vidéo est muette ;
- cela cible directement le symptôme des anciens sons « inaudibles mais capturés ».

Logs ajoutés :
- [AUDIO] APP ;
- [AUDIO] PIERRE ;
- [AUDIO] PIERRE_FALLBACK ;
- [AUDIO] MUSIC ;
- [AUDIO] VIDEO ;
- STOP / RELEASE.

Tests ajoutés :
- policy axes Classic ;
- X-Wing → deux axes réservés ;
- vidéos Gecko/Bee restent hard-muted.

Ne pas déclarer la correction audio téléphone validée avant test Samsung réel.
---

## GECKO-047 — JALON TECHNIQUE 0.15.4-dev

CI #227 GREEN sur le commit fonctionnel `5ea050d2a9f87c813fa9ceb6f6a95c35f643ffa1`.

Validation technique obtenue :
- double clic simplifié ;
- grandes barres d’axes draggable Classic + GeckoBeeDoku ;
- suppression hors plateau ;
- Prof sans grosses croix comme langage principal ;
- pédagogie X-Wing/projection clarifiée ;
- manifest AudioPlaybackCapture ;
- policy globale ALLOW_CAPTURE_BY_ALL API 29+ ;
- vrai chemin Pierre PCM corrigé de ACCESSIBILITY vers MEDIA/SPEECH ;
- fallback Android TTS corrigé ;
- musique / vidéo avec AudioAttributes capturables ;
- vidéos muettes : piste audio désélectionnée, pas seulement volume 0 ;
- logs START/STOP/RELEASE.

Le dernier nettoyage retire de l’interface Bee la légende obsolète « rouge × » du Prof et décrit désormais les grandes barres d’axes.

La correction AUDIO reste à valider sur téléphone Samsung réel :
Pierre doit apparaître dans l’enregistrement « Sons multimédia » et l’ancien son fantôme doit avoir disparu.
### Publication GECKO-047

CI #228 GREEN, y compris publication prerelease téléphone.

Release :
`phone-0.15.4-dev-run-228`

APK :
`GeckoDoku-v0.15.4-dev.apk`

SHA-256 :
`a879911c7abc141bf34ded2fde31fc94c94791c690da8077fbc9b71b307080f2`

Reste uniquement la validation téléphone Fab :
- Pierre présent dans l'enregistrement Samsung ;
- ancien son fantôme absent ;
- barres d'axes agréables et supprimables par sortie du plateau.
