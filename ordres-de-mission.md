# GeckoDoku — GECKO-035
# MISSION ACTIVE — PIERRE PRIORITAIRE, SYNCHRO PROF, RÉGLAGES ET JOURNAL VIDÉO

Date : 26/09/2026
Branche : `gecko-033-identity-prof-life`
Base validée : v0.10.12-dev

## Intention Fab

Pierre / Prof Gecko est une fonction principale du jeu.
La vidéo `ProfParle.mp4` n'est qu'un habillage visuel.

**Un problème vidéo ne doit jamais punir Pierre, le couper, le désactiver, ni empêcher ses phrases suivantes.**

Cette mission regroupe quatre évolutions liées :
1. synchronisation Pierre ↔ ProfParle ;
2. journal d'erreurs vidéo persistant et consultable ;
3. menu Réglages ⚙️ ;
4. bouton ! pour faire vivre les 100 phrases du Prof.

---

# 1. SYNCHRO PIERRE / PROF_PARLE

## Ordre cible

L'ordre actuel déclenche la vidéo après le démarrage réel de Pierre.

Nouvel ordre voulu :

`préparer ProfParle caché → première frame prête → lancer Pierre → révéler ProfParle au même instant`

Objectif :
- aucune bouche en retard sur la voix ;
- aucun flash noir ;
- aucune temporisation arbitraire ;
- si la vidéo n'est pas prête ou échoue, Pierre parle quand même.

## Règle fondamentale

La voix est **autoritaire**.
La vidéo est **optionnelle**.

Donc :
- vidéo OK → synchroniser vidéo + Pierre ;
- vidéo lente → attendre seulement dans une limite raisonnable prévue par la logique, sans bloquer indéfiniment la parole ;
- vidéo en erreur → fallback PNG et Pierre parle normalement ;
- vidéo crash en cours de phrase → Pierre continue jusqu'au bout ;
- erreur ancienne → n'empêche jamais une tentative vidéo future ;
- aucune erreur vidéo ne doit appeler `ProfessorSpeech.stop()`.

Le comportement déjà acquis où `previousAttemptFailed` ne bloque pas les phrases suivantes doit être conservé.

---

# 2. PROF_PARLE POUR TOUTES LES PAROLES DU PROF

Chaque parole audible associée au Prof doit avoir une animation visuelle cohérente.

Origines à couvrir :
- `PROF_BUTTON` ;
- `AMBIENT` ;
- `STATS` ;
- `ENCOURAGEMENT` ;
- `END_GAME` ;
- bouton manuel `!` des phrases de vie.

Règle :
- si Pierre parle → `ProfParle.mp4` doit être demandé ;
- si une voix d'encouragement enregistrée représentant le Prof est jouée → le Prof doit également paraître en train de parler ;
- aucune origine de parole du Prof ne doit rester avec un portrait figé par oubli de câblage ;
- `PROF_SPEECH > PROF_ACTION` reste la priorité locale ;
- aucune animation Gecko concurrente ne doit être stoppée.

---

# 3. BOUTON ! — 100 PHRASES DU PROF

Ajouter un bouton visible et simple :

`!`

Fonction :
- déclencher à la demande une phrase issue des 100 phrases de `PierreSmallTalk` ;
- choix aléatoire avec anti-répétition ;
- parole par Pierre ;
- animation `ProfParle.mp4` synchronisée ;
- si vidéo indisponible : Pierre parle quand même avec PNG fallback.

But :
faire vivre davantage le Prof sans attendre uniquement le timer ambiant.

Le bouton ne doit pas :
- déplacer/redimensionner la grille ;
- interrompre une phrase déjà prioritaire de façon sauvage ;
- casser les règles existantes de priorité de parole.

---

# 4. MENU RÉGLAGES ⚙️

Ajouter un bouton :

`⚙️`

Il ouvre un panneau / dialogue accessible contenant au minimum :

- Son : ON / OFF ;
- Animations : ON / OFF ;
- Consulter le journal vidéo.

Les contrôles Son et Animations doivent être retirés de la barre principale et regroupés dans ce menu afin de libérer de la place.

Contraintes :
- préférences persistantes inchangées ;
- pas de perte des valeurs actuelles ;
- aucune ouverture/fermeture du menu ne doit provoquer un reflow de la grille ;
- interface lisible, grosses zones tactiles.

---

# 5. JOURNAL VIDÉO PERSISTANT ET CONSULTABLE

Le Logcat `GeckoDokuMediaTrace` existe déjà mais n'est pas suffisant pour Fab sur téléphone.

Ajouter un journal persistant local dédié au diagnostic média.

Il doit enregistrer au minimum :
- timestamp lisible ;
- séquence ;
- source / logicalLayer ;
- événement ;
- asset ;
- message d'erreur ;
- codes MediaPlayer `what/extra` ;
- exception éventuelle ;
- état de première frame ;
- mode Prof courant ;
- origine de parole si pertinente.

Événements utiles :
- PLAY_REQUEST ;
- START ;
- VIDEO_RENDERING_START_SIGNAL ;
- VIDEO_FIRST_FRAME ;
- VIDEO_VISIBLE ;
- STOP ;
- COMPLETE ;
- ERROR ;
- EXCEPTION ;
- VIDEO_ABORT_BEFORE_FIRST_FRAME ;
- PROF_SPEECH_VIDEO_FAILED ;
- SPEAK_REQUEST ;
- SPEAK_STARTED ;
- SPEAK_STOP.

## Consultation

Depuis ⚙️ → `Journal vidéo` :
- affichage texte lisible ;
- plus récent facilement accessible ;
- possibilité de tout sélectionner/copier si simple à faire ;
- bouton `Vider le journal` avec confirmation ;
- ne jamais crasher si le fichier est absent ou corrompu.

Prévoir une rotation / limite de taille pour éviter une croissance infinie.

Logcat doit rester actif en parallèle :
le fichier persistant complète Logcat, il ne le remplace pas.

---

# 6. PIERRE NE DOIT JAMAIS ÊTRE PUNI PAR LA VIDÉO

Contrat à tester explicitement :

- erreur ProfParle avant première frame → Pierre parle ;
- erreur ProfParle après première frame → Pierre continue ;
- exception MediaPlayer → Pierre continue ;
- erreur shader / Surface → Pierre continue ;
- ancien `professorSpeechVideoFailed=true` → phrase suivante de Pierre toujours autorisée ;
- plusieurs erreurs vidéo successives → Pierre reste utilisable ;
- bouton Prof après erreur vidéo → Pierre répond ;
- bouton ! après erreur vidéo → Pierre répond ;
- encouragement Pierre après erreur vidéo → Pierre répond ;
- stats Pierre après erreur vidéo → Pierre répond.

La seule chose qui tombe en panne en cas de crash vidéo doit être **l'habillage vidéo de cette tentative**.

Fallback visuel :
`Prof.png`.

---

# 7. TDD / ORDRE DE TRAVAIL

Avant production :

RED ciblés pour :
- préparation vidéo avant démarrage voix ;
- première frame prête → démarrage Pierre + révélation coordonnée ;
- erreur vidéo avant voix → voix autorisée ;
- erreur vidéo pendant voix → voix non stoppée ;
- toutes les SpeechOrigin déclenchent la demande visuelle ProfParle ;
- encouragement enregistré déclenche également une animation de parole du Prof ;
- bouton ! choisit une des 100 phrases sans répétition immédiate ;
- menu ⚙️ expose Son / Anim / Journal ;
- journal persistant reçoit ERROR/EXCEPTION ;
- rotation/limite de taille ;
- vidage du journal ;
- aucune de ces UI ne modifie le rectangle de la grille.

Puis GREEN minimal.

---

# 8. NON-RÉGRESSIONS ABSOLUES

Ne pas casser :
- v0.10.12-dev validée ;
- grille flottante et géométriquement immuable ;
- gate première frame anti-flash noir ;
- intros validées ;
- Pierre UPMC Medium sid=1 ;
- multi-sessions vidéo ;
- Gecko gameplay visible et muet ;
- coexistence Gecko / Prof ;
- bulle Prof overlay ;
- solveur / moteur / génération / journal / stats ;
- encouragements existants ;
- retry ProfParle après erreur.

---

# 9. CRITÈRES TÉLÉPHONE

Fab doit pouvoir confirmer :
- Pierre et ProfParle démarrent visuellement ensemble ;
- aucune phrase de Pierre ne disparaît à cause d'un bug vidéo ;
- toutes les prises de parole du Prof sont animées ;
- le bouton ! fait parler le Prof avec les phrases en stock ;
- ⚙️ contient Son, Animations, Journal vidéo ;
- les anciens boutons Son/Animations ont quitté la barre principale ;
- le journal vidéo est consultable directement sur téléphone ;
- la grille ne bouge pas d'un pixel pendant toutes ces actions.

## Contrat court

**Pierre parle toujours. La vidéo l'accompagne si elle peut. Elle ne commande jamais la voix.**


<!-- GECKO-035-RED-2026-09-26 -->
## TDD RED ouvert
Tests de contrat ajoutés avant production :
- erreur/timeout vidéo → Pierre démarre avec PNG, jamais stoppé ;
- toutes les `SpeechOrigin` demandent l'habillage parlant ;
- encouragement enregistré demande aussi l'habillage parlant ;
- ⚙️ contient Son / Animations / Journal média sans reflow ;
- ! choisit parmi les 100 phrases avec anti-répétition ;
- journal persistant lisible, borné et vidable.


<!-- GECKO-035-GREEN-POLICIES-2026-09-26 -->
## GREEN étape 1 — policies
Policies pures ajoutées : priorité voix/fallback PNG, habillage de toutes les paroles, contenu ⚙️ et sélection ! sans reflow.


<!-- GECKO-035-GREEN-LOG-2026-09-26 -->
## GREEN étape 2 — journal média
MediaTrace conserve Logcat et écrit aussi dans geckodoku-media.log. Le fichier est borné à 256 KiB, lisible et vidable sans jamais affecter le jeu en cas d'erreur I/O.


<!-- GECKO-035-GREEN-SPEECH-GATE-2026-09-26 -->
## GREEN étape 3 — admission voix
QUICK_TALK est une origine non préemptive. ProfessorSpeech expose canAccept(origin) afin de refuser proprement une demande basse priorité avant de préparer ProfParle.


<!-- GECKO-035-GREEN-FIRST-FRAME-HOLD-2026-09-26 -->
## GREEN étape 4 — première frame tenue
ChromaKeyVideoView peut maintenant préparer une frame fraîche, la garder cachée et mettre le MediaPlayer en pause. revealHeldFirstFrame() révèle alpha=1 et reprend la lecture. AssetAudioPlayer expose onStarted pour synchroniser les encouragements enregistrés.


<!-- GECKO-035-GREEN-RUNTIME-2026-09-26 -->
## GREEN étape 5 — runtime/UI
MainActivity câble désormais pré-roll caché → frame tenue → demande voix → reveal au SPEAK_STARTED. Timeout/erreur repassent sur Prof.png et lancent quand même la voix. ! utilise les 100 phrases en QUICK_TALK. ⚙️ contient Son/Animations/Journal ; les anciens boutons Son/Anim quittent la barre. Journal consultable, sélectionnable, copiable et vidable. Encouragement enregistré anime aussi ProfParle et nettoie le visuel sur erreur audio.


<!-- GECKO-035-V01013-CANDIDATE-2026-09-26 -->
## Candidate téléphone v0.10.13-dev
Après GREEN #114 du runtime GECKO-035 : versionCode 24 / versionName 0.10.13-dev. CI finale versionnée requise avant livraison.


<!-- GECKO-035-CI115-GREEN-2026-09-26 -->
## Preuve CI finale
Run #115 : SUCCESS complet sur v0.10.13-dev (versionCode 24), avec tests, assembleDebug, bundleDebug et upload artifact. Mission reste ouverte jusqu'à validation téléphone Fab.
