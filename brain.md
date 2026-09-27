# GeckoDoku — brain.md
## MÉMOIRE FONCTIONNELLE DURABLE / SOURCE DE VÉRITÉ

Ce fichier décrit **ce que GeckoDoku doit continuer à savoir faire**.

Il ne doit jamais devenir un simple résumé du dernier bug ou du dernier ordre de mission.

Rôle des fichiers FAB Copilot :
- `ordres-de-mission.md` : travail immédiat demandé maintenant ;
- `brain.md` : comportements actifs, contrats fonctionnels et invariants durables ;
- `brainmap.md` : architecture, relations et causalités ;
- `debughistorical.md` : bugs, incompréhensions, causes, essais et corrections passées ;
- `todo.md` : travail réellement restant.

Une hypothèse technique temporaire ne devient jamais une règle du brain si les tests téléphone la contredisent.

---

# 1. JEU — CONTRAT DE BASE

## Tailles
Les grilles prises en charge vont de **5×5 à 12×12**.

## Règles Gecko
Les geckos confirmés imposent les exclusions du jeu :
- même ligne ;
- même colonne ;
- même région ;
- contact voisin, diagonales comprises.

Les geckos donnés sont verrouillés.

## Gestes / marques joueur
Fonctions actives :
- croix manuelle ;
- pose/retrait de Gecko ;
- hypothèse par appui long ;
- cycle `NONE → GHOST_GECKO → ALERT_GECKO → NONE` ;
- marqueurs personnalisés ;
- croix automatiques dérivées des Gecko confirmés ;
- comptage des erreurs sur mauvais Gecko.

Une action normale du joueur ne doit jamais casser les sous-systèmes audio/Prof/média qui n'ont pas à l'être.

---

# 2. GÉNÉRATION ET VALIDITÉ DES GRILLES

Le générateur doit préserver :
- solution unique ;
- validation par `countSolutions(..., 2) == 1` ;
- grille logiquement analysable avant acceptation ;
- taille 5..12 ;
- trace de résolution attachée au puzzle ;
- génération de secours sûre si la recherche parfaite échoue.

Une grille ne doit pas être acceptée seulement parce qu'elle possède une solution : elle doit aussi appartenir au profil logique supporté.

---

# 3. SOLVEUR HUMAIN ACTIF

`HumanSolver` est la référence des déductions déterministes.

Techniques actives :
- Gecko donné ;
- single ligne ;
- single colonne ;
- single région ;
- région verrouillée ligne/colonne ;
- projection de zone par voisinage ;
- Gecko X-Wing.

Chaque `SolveStep` peut conserver :
- cellule forcée ;
- exclusions ;
- cellules sources ;
- régions sources ;
- axe ;
- état confirmé/exclu avant l'étape.

Le solveur doit expliquer une déduction, pas seulement annoncer une réponse.

---

# 4. HYPOTHÈSES LOGIQUES

Quand les techniques déterministes sont épuisées :
- `Mission Impossible` autorise une hypothèse logique bornée ;
- `Infernal` autorise jusqu'à deux niveaux / une hypothèse dans l'hypothèse.

`HypothesisSolver` doit démontrer une contradiction avant de forcer l'autre branche.

Ce mécanisme n'est pas un brute-force caché.

---

# 5. DIFFICULTÉS ACTIVES

Les 8 niveaux actifs sont :
1. Découverte ;
2. Facile ;
3. Réflexion ;
4. Difficile ;
5. Expert ;
6. Démentiel ;
7. Mission Impossible ;
8. Infernal.

Le classement est fondé sur les techniques réellement nécessaires :
- singles ;
- logique de région ;
- projection ;
- X-Wing ;
- hypothèses ;
- profondeur d'hypothèse.

Profils majeurs :
- Expert : X-Wing requis ;
- Démentiel : X-Wing + projection requis ;
- Mission Impossible : une hypothèse bornée ;
- Infernal : plusieurs hypothèses ou profondeur 2.

---

# 6. PROF GECKO — CONTRAT PÉDAGOGIQUE

## Source logique
`ProfessorGecko` utilise :
1. la trace du solveur si elle correspond encore à l'état joueur ;
2. sinon une nouvelle déduction `HumanSolver` ;
3. sinon une étape `HypothesisSolver` autorisée par le niveau.

Le Prof ne doit jamais inventer un coup.

## Explication
Le Prof doit pouvoir montrer :
- la zone / ligne / colonne / cellules sources ;
- la technique utilisée ;
- l'explication ;
- l'action concrète attendue.

## Application par le Prof
Le Prof peut appliquer **une étape logique à la fois**.

Garde-fou :
- un Gecko forcé par le Prof est vérifié contre la solution unique avant mutation ;
- une divergence interne doit être traitée comme problème de debug et non comme faute du joueur.

Le Prof ne doit jamais résoudre silencieusement toute la grille d'un seul coup.

---

# 7. BULLE ET INTERFACE PROF

La bulle Prof est un **overlay**, pas un élément qui redimensionne le plateau.

Invariants :
- ouverture de la bulle ≠ changement de taille de la grille ;
- contrôles principaux restent disponibles ;
- portrait Prof n'est pas dessiné dans la bulle ;
- portrait/vidéo Prof vivent dans le host du bouton Prof ;
- fermeture de la bulle ne signifie pas arrêt automatique de la voix.

Le bouton Prof reste accessible avec les barres système Android grâce aux insets.

---

# 8. VOIX DE PIERRE

## Voix principale
Pierre utilise prioritairement le moteur local Sherpa/Piper :
- modèle UPMC Medium ;
- locuteur Pierre `sid=1`.

Android TTS reste un fallback.

## Priorités de parole
Origines actives :
- `PROF_BUTTON` ;
- `AMBIENT` ;
- `STATS` ;
- `ENCOURAGEMENT` ;
- `END_GAME`.

Règles :
- PROF_BUTTON peut remplacer une phrase existante lors d'une nouvelle demande explicite ;
- END_GAME suit sa logique spéciale ;
- AMBIENT, STATS et ENCOURAGEMENT ne préemptent jamais Pierre déjà en train de parler ;
- tap, double tap, croix, Gecko, animation ou changement visuel normal ne coupent pas Pierre ;
- arrêts explicites autorisés : nouvelle grille, fin de partie, FX OFF selon contexte, pause/destruction/quitter Activity.

Les arrêts transport voix doivent être tracés avec raison + appelant.

---

# 9. PROF_PARLE / PROF_ACTIONS

Assets :
- `prof/Prof.png` : portrait normal/fallback ;
- `prof/Prof_actions.mp4` : animation locale muette/non parlante ;
- `prof/ProfParle.mp4` : Pierre visuellement en train de parler.

Priorité locale dans le slot Prof :

`PROF_SPEECH > PROF_ACTION`

Donc :
- si Prof_actions tourne et Pierre commence réellement à parler, Prof_actions peut être stoppé localement ;
- ProfParle démarre ;
- aucune animation Gecko concurrente n'est stoppée ;
- ProfParle s'arrête lorsque Pierre cesse réellement de parler ;
- retour au portrait normal ensuite.

Une erreur ponctuelle de ProfParle :
- peut provoquer le fallback PNG pour cette tentative ;
- ne doit jamais condamner les phrases suivantes ;
- la phrase suivante peut retenter ProfParle ;
- un START réussi efface l'état d'erreur diagnostic.

---

# 10. PROF AMBIANT / VIE DU PROF

Politiques actives :
- aide après environ 90 s sans action de plateau ;
- suggestion de sauvegarde après environ 10 min ou 25 actions ;
- petite phrase calme après délai aléatoire de 4 à 8 min ;
- intervalle minimal entre interventions ambiantes ;
- aucune intervention ambiante si une parole prioritaire ou un état bloquant est actif.

Le Prof doit rester vivant sans devenir envahissant.

---

# 11. JOURNAL / REJOUER / SAUVEGARDER

Fonctions actives :
- `↺ Rejouer` remet la définition courante à zéro ;
- `⭐ Sauver` enregistre la définition originale de la grille ;
- `📚 Journal` liste les grilles conservées ;
- chargement journal = même définition, progression remise à zéro ;
- suppression d'une entrée ;
- vidage du journal.

Le journal conserve notamment :
- id ;
- taille ;
- difficulté ;
- seed ;
- régions ;
- solution ;
- givens.

Le journal ne doit pas enregistrer accidentellement une progression joueur comme nouvelle définition canonique.

---

# 12. STATISTIQUES JOUEUR

Les statistiques locales actives comprennent :
- parties lancées ;
- parties terminées ;
- réussites assistées par Prof ;
- erreurs ;
- temps cumulé ;
- temps moyen ;
- taux de réussite ;
- statistiques par taille ;
- statistiques par difficulté.

Pierre peut narrer les statistiques locales.

Les réussites avec Prof sont distinguées sans effacer les statistiques normales.

---

# 13. ENCOURAGEMENTS ET AUDIO

Deux sources d'encouragement coexistent :
- clips enregistrés ;
- phrases de Pierre.

Le choix est alterné/aléatoire selon la policy dédiée.

Garde-fous :
- pas d'encouragement pour un Gecko placé par le Prof ;
- anti-répétition ;
- contexte spécial quand il reste 1 ou 2 Gecko ;
- FX OFF doit rester silencieux pour les effets concernés.

---

# 14. CÉLÉBRATION DE FIN

La fin de partie conserve :
- message de réussite ;
- statistiques de fin ;
- `VictoryCelebrationView` procédurale ;
- confettis / feux d'artifice gradués selon difficulté ;
- effets sonores associés ;
- musique de célébration indépendante de la durée de l'overlay visuel.

La célébration ne doit pas redimensionner la grille ni casser les contrôles après fermeture.

---

# 15. IDENTITÉ VISUELLE VALIDÉE

Validé téléphone :
- icône launcher depuis `IconGeckoGD.png` ;
- médaillon rond GeckoDoku à gauche du titre ;
- micro-animation du médaillon sans effet sur le layout du plateau.

Ces éléments sont gelés sauf nouvel ordre explicite.

---

# 16. INTROS VALIDÉES

Séquence canonique :

`IntroGeckoGD.mp4` → `Gecko_Intro.mp4` → jeu

Validation téléphone explicite :
- Intro 1 : parfaite ;
- son Intro 1 : correct ;
- Intro 2 : parfaite ;
- enchaînement : correct.

Pendant Intro 1 :
- Prof non présenté prématurément.

Le comportement du skip reste explicite et ne doit pas recréer/redimensionner la grille.

Ces intros sont **gelées** sauf régression démontrée ou nouvel ordre.

---

# 17. ANIMATIONS GECKO

Assets canoniques :
- `gecko/Gecko_tr.png` : sprite statique canonique ;
- `Gecko_apparition.mp4` ;
- `Gecko_disparition.mp4` ;
- `Gecko_actions_plusieurs.mp4`.

Contrats :
- animations Gecko visibles ;
- audio embarqué des animations gameplay toujours muet ;
- mute ≠ stop ;
- mute ≠ skip ;
- mute ≠ hide ;
- animation sur la bonne case ;
- aucune modification de la géométrie de grille ;
- une animation Gecko ne coupe jamais Pierre ;
- une animation Gecko ne coupe jamais le player Prof.

---

# 18. RENDU CHROMA / TRANSPARENCE

Le keycolor bleu est transformé en transparence par :
- `MediaPlayer` ;
- `SurfaceTexture` externe OES ;
- OpenGL ES ;
- shader chroma-key bleu ;
- despill ;
- EGL avec alpha ;
- holder translucide ;
- clear alpha=0.

État candidat v0.10.11-dev :
- `ZOrderOnTop=true` ;
- `MediaOverlay=false`.

Cette stratégie a remplacé le MediaOverlay qui produisait des rectangles noirs sur téléphone.

Le contrat fonctionnel durable n'est pas « utiliser telle API » mais :
- vraie transparence ;
- aucun rectangle noir ;
- coexistence réelle ;
- aucune destruction croisée des players.

---

# 19. COEXISTENCE DES MÉDIAS

Architecture active :
- plusieurs sessions `RichMediaOverlayView` ;
- chaque session possède son propre `ChromaKeyVideoView` / `MediaPlayer` ;
- fin/erreur/stop d'une session ne ferme pas les autres ;
- une session Gecko peut coexister avec Pierre / ProfParle ;
- pas de mutex global « une seule vidéo à la fois ».

Exception locale :
- dans le slot Prof uniquement, PROF_SPEECH peut remplacer PROF_ACTION.

Les intros restent séquentielles entre elles pour raison narrative, pas parce que l'architecture impose un lecteur unique.

---

# 20. HABILLAGE ANIMÉ

`Anim` :
- est ON par défaut sans préférence utilisateur antérieure ;
- peut être désactivé explicitement ;
- état persistant ;
- une erreur vidéo ne doit jamais forcer globalement Anim OFF.

Les logs `LOAD_ENABLED` / `WRITE_ENABLED` servent à détecter toute modification involontaire.

---

# 21. DIAGNOSTIC MÉDIA

Tag :
`GeckoDokuMediaTrace`.

Doivent rester traçables :
- instance/layer logique ;
- PLAY_REQUEST ;
- START ;
- STOP ;
- COMPLETE ;
- ERROR ;
- SET_MUTED ;
- état Surface ;
- policy Surface ;
- requêtes ProfParle ;
- retry ProfParle ;
- origines/arrêts voix.

Les logs sont une aide au diagnostic, jamais un remplacement des tests téléphone.

---

# 22. PREUVES DE BUILD RÉCENTES

TDD sprites :
- run #94 : RED attendu ProfParle retry ;
- run #95 : RED isolé transparence, 43 tests / 1 échec ;
- run #96 : GREEN complet ;
- run #97 : GREEN complet pour v0.10.11-dev, tests + APK + AAB + artifact ;
- run #103 : RED attendu GECKO-034 première frame + grille flottante ;
- run #104 : GREEN complet du correctif GECKO-034 ;
- run #105 : GREEN complet v0.10.12-dev, tests + APK + AAB + artifact.

La CI valide la cohérence logicielle.
Le téléphone de Fab reste l'autorité finale pour le rendu, le son et l'ergonomie.

---

# 23. NON-RÉGRESSION ABSOLUE

Toute future mission doit préserver sauf ordre explicite :
- règles de grille ;
- solution unique ;
- solveur humain ;
- difficultés ;
- hypothèses logiques ;
- Prof pédagogique ;
- journal/rejouer ;
- statistiques ;
- bulle sans reflow ;
- voix Pierre ;
- encouragements ;
- célébration ;
- icône/médaillon ;
- intros validées ;
- grille immuable sous overlays ;
- Gecko gameplay visible mais muet ;
- parole Pierre non coupée par action normale ;
- coexistence réelle des médias ;
- transparence chroma sans rectangle noir.

En cas de conflit entre une hypothèse technique et un comportement téléphone validé, **le comportement validé gagne**.


<!-- GECKO-BOARD-FLOATING-IMMUTABLE-INVARIANT-2026-09-26 -->
# 24. GRILLE FLOTTANTE ET GÉOMÉTRIQUEMENT IMMUABLE

La grille GeckoDoku est un **repère visuel stable**.

Invariant permanent :

- le rectangle du plateau reste identique pendant toute interaction qui ne change pas explicitement de taille de grille ;
- bulles, vidéos, sprites, overlays, animations, Prof, célébrations et médias ne participent jamais au calcul de taille du plateau ;
- aucun média ne doit pousser, tirer, recentrer, agrandir ou réduire la grille ;
- aucune apparition/disparition d'un objet visuel relatif ne doit provoquer de reflow ;
- aucun `View.GONE` ne doit être utilisé sur un élément dont la suppression du flux modifierait la géométrie du plateau ;
- préférer des overlays indépendants et des changements `alpha/visibility` sans recalcul du layout ;
- aucun changement temporaire de `layout_weight`, marges, padding ou dimensions du parent de la grille pour afficher un média ;
- la grille doit rester visuellement “flottante” dans son espace dédié, indépendante des couches décoratives placées au-dessus.

Contrat géométrique :

`boardRectBefore == boardRectDuringOverlay == boardRectDuringVideo == boardRectAfterVideo`

Ce principe s'applique notamment à :
- bulle Prof ;
- Prof.png / Prof_actions / ProfParle ;
- Gecko apparition/disparition/actions longues ;
- intro ;
- célébration ;
- overlays pédagogiques ;
- futur gate de première frame.

Si une solution technique corrige un média mais fait bouger ou redimensionner la grille sur certains formats d'écran, cette solution est invalide.


<!-- GECKO-034-RED-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## Invariant de présentation média
Une animation non-intro ne doit devenir visible qu'après consommation/rendu d'une première frame réelle. `onPrepared` ou `MediaPlayer.start()` seuls ne suffisent pas.

## Invariant de grille flottante
L'espace de layout peut conserver une ancre, mais la vue de grille réelle doit être indépendante des variations relatives des textes/contrôles. Tant que la fenêtre ne change pas, son rectangle reste figé.


<!-- GECKO-034-GREEN-FIRST-FRAME-FLOATING-BOARD-2026-09-26 -->
## Implémentation durable GECKO-034
La vraie grille est une couche flottante de screenRoot. Le LinearLayout ne contient plus que son anchor de réservation. Son rectangle est figé tant que la fenêtre ne change pas.

Les vidéos non-intro sont révélées par alpha après draw OpenGL d'une frame fraîche ; prepared/start seuls ne suffisent jamais.


<!-- GECKO-034-V01012-VALIDATED-2026-09-26 -->
## GECKO-034 VALIDÉ TÉLÉPHONE — v0.10.12-dev

Fab valide le correctif GECKO-034 sur téléphone.

Acquis durables supplémentaires :
- plus de flash noir parasite au démarrage des animations concernées ;
- les médias non-intro restent cachés jusqu'à la première frame réellement rendue ;
- la révélation se fait sans délai artificiel ;
- la grille réelle est flottante dans `screenRoot` ;
- son rectangle reste immuable tant que la fenêtre ne change pas ;
- textes, overlays, Prof, sprites et variations relatives du layout ne doivent plus déplacer la grille ;
- les intros validées restent inchangées.

Ces comportements sont désormais des invariants de non-régression au même titre que les autres acquis du brain.


<!-- GECKO-035-RED-2026-09-26 -->
## Garde-fou en cours GECKO-035
La priorité durable est déjà définie : Pierre est fonctionnel, la vidéo est décorative. Le cycle TDD doit empêcher toute future dépendance où une panne vidéo pourrait couper ou condamner la voix.


<!-- GECKO-035-GREEN-POLICIES-2026-09-26 -->
## Contrat Pierre prioritaire
La policy GECKO-035 affirme qu'une panne vidéo ne peut jamais autoriser l'arrêt de Pierre. Timeout de pré-roll prévu : 900 ms, puis voix avec PNG.


<!-- GECKO-035-GREEN-LOG-2026-09-26 -->
## Journal média durable
Le diagnostic média possède deux sorties : Logcat GeckoDokuMediaTrace et fichier interne geckodoku-media.log, borné à 256 KiB. Une erreur du journal ne doit jamais perturber le jeu.


<!-- GECKO-035-GREEN-SPEECH-GATE-2026-09-26 -->
## Admission des paroles
Le bouton ! utilise QUICK_TALK et ne préempte pas sauvagement une phrase active. La préparation vidéo ne doit commencer que si la requête voix est admissible.


<!-- GECKO-035-GREEN-FIRST-FRAME-HOLD-2026-09-26 -->
## Pré-roll parlant
La première frame de ProfParle peut être rendue hors écran puis tenue en pause. Sa révélation/reprise est indépendante du démarrage du player et peut donc être alignée sur le vrai départ audio.


<!-- GECKO-035-GREEN-RUNTIME-2026-09-26 -->
## GECKO-035 runtime
Toutes les demandes Pierre de MainActivity passent par un wrapper visuel commun. Le média est préparé avant la voix, mais n'a aucun pouvoir de blocage : erreur ou timeout → PNG + voix. Le bouton ! est non préemptif. Les réglages Son/Animations sont regroupés sous ⚙️ avec accès au journal média.


<!-- GECKO-035-V01013-CANDIDATE-2026-09-26 -->
## Référence candidate GECKO-035
v0.10.13-dev porte le pré-roll ProfParle avant voix, fallback PNG sans punir Pierre, ! quick talk, ⚙️ Son/Animations/Journal et journal média persistant.


<!-- GECKO-035-CI115-GREEN-2026-09-26 -->
## Preuve technique GECKO-035
Run #115 GREEN complet pour v0.10.13-dev. Cette preuve valide la cohérence logicielle/build ; la synchro perceptuelle, l'ergonomie ⚙️/! et les scénarios de panne vidéo restent à valider sur téléphone par Fab.


<!-- GECKO-035-CONSOLIDATED-2026-09-27 -->
# GECKO-035 — PIERRE PRIORITAIRE / RÉGLAGES / JOURNAL MÉDIA

État logiciel consolidé après CI #115 GREEN sur v0.10.13-dev (versionCode 24).

## Pierre reste prioritaire sur la vidéo
Principe durable :

**Pierre parle toujours. ProfParle l'accompagne si possible, mais la vidéo ne commande jamais la voix.**

Le flux de parole est désormais conçu ainsi :
- vérifier que la requête de parole est admissible ;
- préparer `ProfParle.mp4` caché ;
- attendre une première frame réellement rendue ;
- tenir cette frame cachée ;
- lancer Pierre ;
- au vrai `SPEAK_STARTED`, révéler la frame tenue et reprendre la vidéo ;
- en cas d'erreur vidéo ou de timeout de préparation, conserver `Prof.png` et lancer quand même Pierre.

Timeout de sécurité actuel du pré-roll : environ 900 ms.

Une panne vidéo ne doit jamais :
- appeler `ProfessorSpeech.stop()` ;
- empêcher une phrase de Pierre ;
- condamner les phrases suivantes ;
- désactiver définitivement ProfParle.

## Habillage de toutes les paroles du Prof
Les origines de parole du Prof passent par le même contrat visuel :
- PROF_BUTTON ;
- AMBIENT ;
- STATS ;
- ENCOURAGEMENT ;
- END_GAME ;
- QUICK_TALK.

Les encouragements enregistrés demandent également une animation de parole du Prof lorsque les animations sont actives.

La priorité locale reste :
`PROF_SPEECH > PROF_ACTION`.

Aucune animation Gecko ne doit être stoppée par cette priorité locale.

## Bouton !
Le bouton `!` permet de faire parler volontairement le Prof avec les 100 phrases de `PierreSmallTalk`.

Règles durables :
- sélection avec anti-répétition immédiate ;
- origine `QUICK_TALK` ;
- ne préempte pas sauvagement une phrase déjà active ;
- utilise le même contrat ProfParle / fallback PNG ;
- ne modifie pas la géométrie de la grille.

## Menu ⚙️
Les contrôles Son et Animations ont quitté la barre principale.

Le menu `⚙️` regroupe :
- Son ON/OFF ;
- Animations ON/OFF ;
- Journal vidéo.

L'ouverture/fermeture de ce menu ne doit jamais provoquer de reflow de la grille flottante.

## Journal média persistant
Le diagnostic média conserve deux sorties :
1. Logcat avec le tag `GeckoDokuMediaTrace` ;
2. fichier interne persistant `geckodoku-media.log`.

Le fichier :
- est borné à environ 256 KiB ;
- est consultable dans l'application ;
- est sélectionnable/copiant ;
- peut être vidé avec confirmation ;
- ne doit jamais perturber le jeu si lecture/écriture échoue.

Les événements média principaux restent traçables : PLAY_REQUEST, START, VIDEO_RENDERING_START_SIGNAL, VIDEO_FIRST_FRAME, VIDEO_VISIBLE, STOP, COMPLETE, ERROR, EXCEPTION, VIDEO_ABORT_BEFORE_FIRST_FRAME, erreurs ProfParle et événements de parole.

## Preuves techniques
Cycle GECKO-035 :
- #108 : RED attendu sur les nouveaux contrats absents ;
- #114 : GREEN complet après câblage runtime ;
- #115 : GREEN complet final sur v0.10.13-dev, avec tests, APK, AAB et artifact.

Cette preuve CI valide le contrat logiciel et le build. Les observations perceptuelles téléphone restent toujours l'autorité finale pour synchro audio/vidéo et rendu.


<!-- GECKO-036-PNG-CONTINUITY-MISSION-2026-09-27 -->
## Nouveau contrat visuel Prof — à implémenter
Fab impose une continuité visuelle stricte entre le portrait statique et ProfParle :
`Prof.png` doit rester visible pendant toute la préparation cachée de `ProfParle.mp4` et ne doit être masqué qu'après réussite réelle de la révélation de la première frame.

En cas de timeout, erreur ou reveal impossible, le PNG reste visible.
Cette exigence ne change pas la priorité de Pierre et ne doit pas modifier la géométrie de la grille.


<!-- GECKO-036-QUICK-TALK-BUBBLE-2026-09-27 -->
## Contrat durable — QUICK_TALK dans la bulle
Les 100 phrases déclenchées par `!` appartiennent à la bulle de dialogue du Prof, pas à la ligne d'état au-dessus de la grille.

Une pression sur `!` doit :
- choisir une phrase `PierreSmallTalk` ;
- l'afficher dans `ProfessorBubbleView` ;
- la faire prononcer une seule fois avec `SpeechOrigin.QUICK_TALK` ;
- utiliser ProfParle/fallback selon le contrat média courant.

L'affichage de la bulle et le déclenchement vocal doivent être séparables afin d'éviter toute double parole. La bulle reste un overlay sans effet sur la géométrie du plateau.


<!-- GECKO-036-RED-2026-09-27 -->
## Garde-fou GECKO-036 en cours
La continuité visuelle PNG→vidéo, l'identité de frame par playback et la séparation bulle/voix QUICK_TALK sont maintenant protégées par un cycle TDD dédié.


<!-- GECKO-036-GREEN-POLICIES-2026-09-27 -->
## GECKO-036 policies
Le portrait possède désormais un contrat explicite de continuité ; une frame vidéo n'est admissible qu'après START + VIDEO_RENDERING_START du playback courant ; QUICK_TALK possède une présentation distincte de son transport vocal.


<!-- GECKO-036-GREEN-RUNTIME-2026-09-27 -->
## GECKO-036 runtime
Continuité PNG→vidéo : portrait visible avant stop/restart vidéo, pendant préparation et frame tenue ; masquage seulement après révélation réelle. Les frames sont associées à une génération de playback et ne sont admissibles qu'après START + VIDEO_RENDERING_START. QUICK_TALK sépare affichage de bulle et transport vocal.


<!-- GECKO-036-V01014-CANDIDATE-2026-09-27 -->
## Référence candidate GECKO-036
v0.10.14-dev porte la continuité Prof.png→ProfParle, le rejet des frames résiduelles par génération de playback et QUICK_TALK dans la bulle sans double parole.


<!-- GECKO-036-CI123-GREEN-2026-09-27 -->
## Preuve technique GECKO-036
Run #123 GREEN complet pour v0.10.14-dev. Le contrat logiciel/build est validé ; le téléphone de Fab reste l'autorité finale pour continuité PNG→vidéo, disparition du -38 et affichage QUICK_TALK dans la bulle.


<!-- GECKO-036-FRAME-SERIAL-RED-2026-09-27 -->
## Garde-fou SurfaceTexture
Une frame SurfaceTexture disponible ne doit jamais être supprimée en remettant un booléen de disponibilité à faux. La fraîcheur d'une frame doit être déterminée par identité/serial, tout en consommant toutes les frames reçues. Les intros hors gate ne doivent pas être soumises au mécanisme de première frame.


<!-- GECKO-036-FRAME-SERIAL-GREEN-2026-09-27 -->
## Invariant SurfaceTexture corrigé
Ne jamais jeter une notification de frame pour déterminer sa fraîcheur. Les frames sont toujours consommées via `updateTexImage()`; leur fraîcheur est déterminée par un serial monotone. Un gate de première frame n'est armé que pour un playback qui demande réellement `revealOnFirstFrame`. Les intros restent hors gate.


<!-- GECKO-036-V01015-CANDIDATE-2026-09-27 -->
## Référence candidate GECKO-036 corrigée
v0.10.15-dev remplace la purge de frame dangereuse de v0.10.14-dev par un compteur monotone produit/consommé. Les intros hors gate ne sont plus armées. Ce mécanisme doit préserver la fluidité INTRO et permettre à ProfParle d'obtenir une vraie première frame sans timeout artificiel.


<!-- GECKO-036-CI127-GREEN-2026-09-27 -->
## Preuve technique v0.10.15-dev
Run #127 GREEN complet. Le correctif serial SurfaceTexture compile et passe les tests. Le téléphone de Fab reste l'autorité pour confirmer la disparition du gel Intro/Prof.


<!-- GECKO-036-FINAL-VALIDATION-2026-09-27 -->
# GECKO-036 — VALIDATION TÉLÉPHONE FINALE / ÉTAT ACQUIS

Fab confirme le 27/09/2026 que, pour lui, **tout est corrigé** sur la candidate v0.10.15-dev (versionCode 26).

Cette validation téléphone clôt les régressions GECKO-036 et transforme les comportements suivants en acquis à préserver :

## Vidéos / SurfaceTexture
- Intro 1 ne doit plus rester figée après `VIDEO_RENDERING_START`.
- Prof_actions et ProfParle doivent continuer à recevoir et consommer les frames normalement.
- Il est interdit de jeter une frame disponible avec une simple remise à faux d'un drapeau de disponibilité.
- Toutes les frames SurfaceTexture disponibles sont consommées via `updateTexImage()`.
- La fraîcheur est déterminée par serial monotone produit/consommé.
- Une frame antérieure à la baseline peut être consommée mais ne valide jamais le nouveau playback.
- Une frame fraîche de la bonne génération valide le gate.
- Les intros avec `revealOnFirstFrame=false` restent hors du mécanisme de gate première frame.

## Prof / Pierre
- `Prof.png` reste visible pendant toute préparation cachée de ProfParle.
- Le PNG n'est masqué qu'après révélation réelle de la vidéo.
- Timeout, erreur ou échec de reveal conservent/restaurent le PNG.
- Une panne vidéo n'empêche jamais Pierre de parler.
- Pierre reste prioritaire sur l'habillage vidéo.

## QUICK_TALK / bulle
- Les 100 phrases du bouton `!` s'affichent dans la vraie bulle Prof.
- La phrase complète ne doit pas polluer la ligne de statut.
- Une pression sur `!` produit une seule parole `QUICK_TALK`.
- Affichage de bulle et transport vocal restent séparables pour éviter toute double parole.

## Interface / géométrie
- La grille reste flottante et géométriquement immuable.
- Les overlays, bulles, vidéos et transitions Prof ne doivent provoquer aucun reflow.
- Le menu ⚙️, le journal média, les contrôles Son/Animations et le bouton ! restent acquis.

## Référence technique validée
- version : `0.10.15-dev`
- versionCode : `26`
- CI #125 : RED attendu
- CI #126 : GREEN correctif frame serial
- CI #127 : GREEN candidate versionnée
- CI #128 : GREEN HEAD final
- validation téléphone Fab : **OK, tout corrigé**

Cette version devient la nouvelle base comportementale de référence pour les missions suivantes.


<!-- GECKO-037-LIVING-PROF-MISSION-2026-09-27 -->
# GECKO-037 — CIBLE DURABLE : PROF GECKO VIVANT

Nouvelle mission active : faire évoluer le small talk de Prof Gecko vers un système de 309 phrases identifiées, catégorisées et sélectionnées par contexte.

Principes à préserver :
- 100 phrases historiques conservées et dotées d'IDs stables `legacy_smalltalk_001..100` ;
- 209 nouvelles phrases définies par l'ordre de mission ;
- cooldown individuel persistant de 48 h ;
- `lastPhraseId` persistant, aucune répétition immédiate ;
- fallback oldest-first sans effacement global d'historique ;
- contexte léger joueur : mastery / impulsivity / momentum ;
- humeur interne de sélection : NEUTRAL / PROUD / IMPRESSED / TAQUIN / PEDAGOGICAL / ENCOURAGING / CURIOUS ;
- humeur courte et naturellement décroissante vers NEUTRAL ;
- difficulté de grille prise en compte pour interpréter une erreur ;
- une erreur réfléchie ne doit pas déclencher une moquerie injuste ;
- les phrases RARE restent rares ;
- la bulle et Pierre utilisent exactement le même texte ;
- petites phrases : fermeture 1 s après vraie fin de parole avec token/generation anti-callback obsolète ;
- bulles pédagogiques : jamais auto-fermées par ce mécanisme ;
- small talk : ne préempte jamais une parole prioritaire.

Philosophie durable :
**taquiner le comportement, jamais rabaisser le joueur.**


<!-- GECKO-037-REMOVE-RECORDED-ENCOURAGEMENTS-2026-09-27 -->
## Cible GECKO-037 — encouragements enregistrés retirés
Décision fonctionnelle : les encouragements vocaux enregistrés dans `Voix_encouragements.mp3` doivent disparaître. Le chemin cible est Pierre uniquement, puis intégration progressive au moteur contextuel des 309 phrases.

Ne pas supprimer les autres sons/musiques. Préserver l’origine `ENCOURAGEMENT`, les priorités de parole, ProfParle et les callbacks de fin utilisés par la célébration.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-GREEN-2026-09-27 -->
## Acquis GECKO-037 — encouragements Pierre uniquement
Le système enregistré `Voix_encouragements.mp3` n'est plus une source d'encouragement. Il ne doit pas être réintroduit comme source concurrente.

Contrat actuel :
`player success → playEncouragement → PierreEncouragements → speakWithProfessorVisual(ENCOURAGEMENT)`.

`onFinished` reste propagé afin de préserver la musique de célébration en fin de grille.

`PierreEncouragements` est provisoire : GECKO-037 doit ensuite faire converger ces réactions vers le catalogue contextuel des 309 phrases.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-CI132-2026-09-27 -->
## Preuve technique — encouragements enregistrés
CI #132 GREEN complet après retrait de la source RECORDED et de `Voix_encouragements.mp3`. Le contrat actif est désormais Pierre uniquement pour les encouragements vocaux, en attendant leur migration vers le selector contextuel 309.


<!-- GECKO-037-FULL-RED-2026-09-27 -->
## GECKO-037 — filet TDD global
La mission « Prof vivant » est désormais protégée par un RED global couvrant catalogue, mémoire 48 h, persistance, sélection, contexte/mood et cycle de bulle. Aucun branchement runtime ne doit précéder le GREEN des briques pures.


<!-- GECKO-037-GREEN-MODEL-MOOD-BUBBLE-2026-09-27 -->
## GECKO-037 GREEN 1
Le contexte joueur est un état léger : mastery/impulsivity/momentum plus compteurs courts. Une erreur réfléchie ou en difficulté élevée est ENCOURAGING avant toute logique TAQUIN. Mood est une politique de ton et revient vers NEUTRAL. La fermeture auto des petites bulles est générationnelle et la pédagogie n'est jamais auto-fermée.


<!-- GECKO-037-GREEN-CATALOG-309-2026-09-27 -->
## Catalogue canonique GECKO-037
ProfessorPhraseCatalog est la source d'identité persistante : 309 entrées, IDs stables, catégories, rareté et affinités mood. PierreSmallTalk.lines n'est plus qu'une vue texte des 100 legacy afin de préserver les anciens tests/appels pendant migration.


<!-- GECKO-037-GREEN-HISTORY-SELECTOR-2026-09-27 -->
## Mémoire/sélection GECKO-037
Une phrase est identifiée par ID, marquée utilisée à la sélection et reste normalement exclue 48 h. lastPhraseId est persisté et toujours exclu. Si le pool est épuisé, l'historique n'est jamais vidé : le selector choisit la plus ancienne phrase compatible hors lastPhraseId. SharedPreferences suffit pour cette mémoire légère.


<!-- GECKO-037-RUNTIME-GREEN-2026-09-27 -->
## GECKO-037 runtime
Le moteur 309 est maintenant la source unique des petites paroles runtime : !, ambient, encouragements et réactions contextuelles. Les erreurs sont classées approximativement par délai depuis l'action précédente : >=15 s = réflexion longue, <=2,5 s = rapide ; une seule erreur rapide ne suffit pas à rendre le Prof taquin. Retour après pause >=60 s alimente RETURN. Les explications Prof restent sur leur chemin pédagogique séparé.


<!-- GECKO-037-RUNTIME-CLEANUP-2026-09-27 -->
## Non-régression sélection
MainActivity ne possède plus d'ancien selector par index pour le smalltalk. Le chemin runtime des petites phrases passe exclusivement par ProfessorLifeController → ProfessorPhraseSelector → ProfessorPhraseCatalog.


<!-- GECKO-037-V01016-CANDIDATE-2026-09-27 -->
## Référence candidate Prof vivant
v0.10.16-dev / code 27 est la première candidate intégrant GECKO-037. La base v0.10.15 vidéo reste inchangée ; GECKO-037 se superpose au catalogue/sélection/contexte/bulle.


<!-- GECKO-037-CI140-GREEN-2026-09-27 -->
# GECKO-037 — ÉTAT TECHNIQUE CANDIDATE GREEN

Référence candidate :
- version : `0.10.16-dev`
- versionCode : `27`
- RED global : CI #134 attendu
- cœur pur : CI #137 GREEN
- runtime : CI #138 GREEN
- candidate versionnée : CI #140 GREEN complet

État runtime définitif de la candidate :
- les encouragements enregistrés MP3 sont supprimés ;
- `PierreEncouragements` n'est plus la source runtime des encouragements ;
- `!`, smalltalk ambiant, encouragements et réactions contextuelles passent tous par :
  `ProfessorLifeController → ProfessorPhraseSelector → ProfessorPhraseCatalog`;
- le catalogue canonique comporte 309 phrases ;
- historique individuel 48 h et `lastPhraseId` sont persistés ;
- contexte/mood reste court et décroissant ;
- la difficulté protège notamment contre la moquerie injuste sur erreur réfléchie/difficile ;
- les petites bulles utilisent exactement le texte prononcé et ferment 1000 ms après la vraie completion, avec token anti-callback obsolète ;
- les bulles pédagogiques restent hors auto-close ;
- les priorités de `ProfessorSpeech` empêchent le smalltalk de préempter une parole prioritaire.

La validation téléphone de Fab reste l'autorité finale avant clôture de GECKO-037.


<!-- GECKO-037-TECHNICAL-CLOSE-2026-09-27 -->
# GECKO-037 — BASE TECHNIQUE CONSOLIDÉE

État de référence technique :
- version candidate : `0.10.16-dev` ;
- versionCode : `27` ;
- CI cœur : #137 GREEN ;
- CI runtime : #138 GREEN ;
- nettoyage ancien sélecteur : #139 GREEN ;
- CI candidate : #140 GREEN ;
- HEAD documentaire précédent : #141 GREEN.

## Prof Gecko vivant
Le moteur de petites phrases est désormais structuré autour de :
- 309 phrases ;
- 100 historiques conservées avec IDs stables `legacy_smalltalk_001..100` ;
- 200 V2 ;
- 4 FAB ;
- 5 TAQUIN ;
- catégories et rareté ;
- déduplication normalisée + diagnostic de proximité ;
- cooldown individuel persistant de 48 h ;
- `lastPhraseId` persistant ;
- anti-répétition immédiate ;
- fallback voisin / GENERAL / oldest-first ;
- catégorie RARE limitée à environ 4 % ;
- FAB semi-rares.

## Contexte joueur
Le contexte actif utilise :
- mastery ;
- impulsivity ;
- momentum ;
- difficulté courante ;
- événements haut niveau du jeu.

Le mood interne adapte les poids sans être exposé comme jauge :
- NEUTRAL ;
- PROUD ;
- IMPRESSED ;
- TAQUIN ;
- PEDAGOGICAL ;
- ENCOURAGING ;
- CURIOUS.

Règle durable :
**taquiner le comportement, jamais rabaisser le joueur.**

Une erreur réfléchie ou commise sur forte difficulté ne doit pas déclencher une moquerie injuste. Le mood décroît naturellement vers NEUTRAL.

## Runtime petites phrases
Les chemins :
- bouton `!` ;
- smalltalk ambiant ;
- encouragements ;
- réactions d’erreur ;
passent par le moteur contextuel 309.

La bulle affiche exactement le texte prononcé par Pierre.

Petites réactions :
- vraie fin Pierre ;
- attendre environ 1 s ;
- fermeture protégée par token/generation ;
- un callback ancien ne ferme jamais une nouvelle bulle.

Les bulles pédagogiques structurées restent ouvertes.

## Encouragements
L’ancien MP3 `Voix_encouragements.mp3` et tout le chemin RECORDED ont été retirés.
Les encouragements passent par Pierre et le moteur contextuel.

## Non-régression
Préserver :
- Prof pédagogique ;
- HumanSolver / HypothesisSolver / Gecko X-Wing ;
- ProfParle / PNG fallback ;
- pipeline SurfaceTexture serial validé ;
- grille flottante immuable ;
- intros ;
- célébration ;
- priorité Pierre ;
- menu ⚙️ / journal média.

Cette consolidation est techniquement validée par CI.
La validation perceptuelle téléphone GECKO-037 reste une vigilance utilisateur, pas une mission de code active.

<!-- GECKO-038-DESIGN-SUDOKU-2026-09-27 -->
# GECKO-038 — SECOND MODE SUDOKU — SPÉCIFICATION FONCTIONNELLE

STATUT : **DOCUMENTATION / CONCEPTION UNIQUEMENT. AUCUN CODE AUTORISÉ PAR CETTE SECTION.**
Le développement ne commence que sur ordre explicite ultérieur de Fab.

## Contrat documentaire du projet

Le `brain.md` est la mémoire fonctionnelle canonique de GeckoDoku. Il doit permettre de retrouver l'intention du produit même sans relire le code :
- modes de jeu et règles ;
- interactions tactiles / clavier ;
- identité visuelle ;
- comportement de Pierre / Prof Gecko ;
- invariants d'accessibilité ;
- actifs canoniques ;
- décisions produit validées ;
- comportements à préserver ;
- liens avec les organigrammes de `brainmap.md`.

Le `brainmap.md` porte les organigrammes et flux.
Le `debughistorical.md` conserve les causes, régressions et décisions historiques.
Le `todo.md` conserve ce qui reste à faire.
Le `ordres-de-mission.md` décrit le workflow de la prochaine intervention autorisée.

Ces documents doivent pouvoir servir plus tard de base à une documentation utilisateur et développeur.

## Vision produit : une application, deux modes

GeckoDoku doit pouvoir accueillir un deuxième jeu sans dénaturer le jeu historique.

### Mode 1 — GECKODOKU
Mode historique actuel.
Ses règles, géométrie, gestes, rendu et comportements validés restent la référence de non-régression.

### Mode 2 — SUDOKU
Vrai Sudoku classique 9 × 9 :
- chiffres 1 à 9 ;
- chaque chiffre une fois par ligne ;
- chaque chiffre une fois par colonne ;
- chaque chiffre une fois par bloc 3 × 3 ;
- cases données verrouillées ;
- cases joueur modifiables ;
- mode notes / crayon ;
- effacement ;
- annuler / refaire ;
- Prof Gecko pédagogique adapté au Sudoku ;
- même personnalité vivante de Pierre que dans GeckoDoku.

Le moteur de règles Sudoku doit être distinct du moteur GeckoDoku. On partage l'application et les services communs, pas les règles internes.

## Séparation fondamentale : GameMode ≠ VisualStyle

Ne jamais confondre le jeu actif avec son habillage.

### GameMode
Conceptuellement :
- `GECKODOKU`
- `SUDOKU`

Le point d'entrée de mode peut être pensé comme `mode(GECKODOKU)` / `mode(SUDOKU)`, sans imposer ce nom exact au futur code.

### VisualStyle du Sudoku
Trois états indépendants :
- `CLASSIC_NUMBERS` : chiffres classiques ;
- `GECKO_NB` : chiffres-geckos stylisés noir/blanc ;
- `GECKO_COLORED` : chiffres-geckos colorés.

Ainsi le mode Sudoku reste le même jeu quelles que soient ses représentations visuelles.

## Assets canoniques des styles Gecko

Fab a ajouté sur `main` :
- `assets/gecko/PlancheGeckoDeNombreNB.png`
- `assets/gecko/PlancheGeckoDeNombreColored.png`

Ces deux fichiers ont été vérifiés présents sur `main` le 2026-09-27.

Règle permanente :
- utiliser ces PNG comme sources canoniques ;
- aucune conversion ;
- aucune recompression ;
- aucun réexport automatique ;
- aucun remplacement par une approximation générée ;
- privilégier l'utilisation de régions source / découpe logique au rendu plutôt que fabriquer dix nouveaux fichiers dérivés.

La branche `gecko-033-identity-prof-life` ne contient pas encore ces deux assets au moment de cette documentation. Une future mission d'implémentation devra d'abord les intégrer depuis la bonne base Git sans les modifier.

## Sélecteur magique à trois états

Le choix visuel du Sudoku se fait avec un unique sélecteur horizontal tactile à trois positions.

Positions :
- gauche : Classic ;
- milieu : Gecko NB ;
- droite : Gecko Coloré.

Comportement :
1. Le style actuellement ciblé est affiché **très grand**.
2. Les deux autres styles sont affichés plus petits.
3. Le joueur pose le doigt sur le sélecteur.
4. Sans relever le doigt, il peut glisser gauche ↔ milieu ↔ droite.
5. Dès que le doigt franchit une zone, le style de prévisualisation change immédiatement.
6. **La vraie grille change visuellement en direct pendant le glissement**, avant tout relâchement.
7. Le joueur peut comparer les trois styles en gardant le doigt posé.
8. Au relâchement, l'état courant est validé et mémorisé.
9. Un petit retour haptique par cran est souhaitable s'il reste discret et désactivable/cohérent avec les réglages FX.
10. Un tap direct sur un petit état peut également sélectionner cet état si cela ne complique pas le geste principal.

Il ne doit pas y avoir de bouton « OK » supplémentaire.

Concept :
`selectedStyle` = style mémorisé.
`previewStyle` = style suivi pendant le doigt posé.
Pendant MOVE : rendu sur `previewStyle`.
Au UP valide : `selectedStyle = previewStyle`.

## Stratégie anti-régression : couche visuelle on-top

Pour les zones qui risquent de mal réagir à une refonte du rendu historique, ne pas les réécrire.

Principe :
- conserver les hitboxes et événements tactiles historiques en dessous ;
- conserver le contenu métier en dessous ;
- ajouter une couche visuelle dédiée au-dessus quand c'est suffisant ;
- cette couche décorative doit être transparente aux touches / clics ;
- elle ne doit jamais modifier les coordonnées tactiles ;
- elle ne doit jamais devenir la donnée du jeu.

Le gecko affiché est une **représentation**, jamais l'état métier de la case.

Exemple :
- état métier Sudoku = valeur 7 ;
- style Classic → dessiner « 7 » ;
- style Gecko NB → dessiner la région 7 de la planche NB ;
- style Gecko Coloré → dessiner la région 7 de la planche colorée.

Changer de style ne change donc aucune valeur Sudoku.

## Cases vides et comportement historique

Le comportement historique GeckoDoku lié aux cases vides / geckos initiaux est spécifique au mode historique et doit être protégé.

Le mode Sudoku ne doit pas hériter accidentellement d'un gecko de case vide provenant du renderer GeckoDoku.

Le futur routeur de mode doit décider explicitement :
- GECKODOKU → comportement historique inchangé ;
- SUDOKU → rendu Sudoku propre.

Une case vide Sudoku reste fonctionnellement vide. Toute décoration éventuelle future devra être explicitement décidée et ne jamais masquer l'état vide.

## Routage des événements

Pour limiter les régressions, éviter une stratégie qui détruit puis recrée les listeners à chaque changement de mode.

Préférence architecturale :
- bindings tactiles / clavier installés de manière stable ;
- événement reçu ;
- lecture du `GameMode` ;
- routage vers l'action autorisée par ce mode seulement.

Objectif :
- pas de double bind ;
- pas de listener fantôme ;
- pas d'événement oublié après bascule ;
- surface tactile stable.

Seules les actions réellement différentes sont redirigées.

## Contrôles Sudoku envisagés

Tactile principal :
- toucher une case → sélectionner ;
- gros pavé 1–9 → saisir la valeur ;
- bouton Notes / crayon → bascule des candidats ;
- Effacer ;
- Annuler / Refaire ;
- Prof Gecko.

Clavier physique complémentaire :
- 1…9 → mêmes actions que les boutons tactiles ;
- flèches → déplacement de sélection ;
- Suppr / Retour arrière → effacer ;
- raccourci Notes et Prof possible si confirmé ultérieurement.

Règle : tactile et clavier appellent les **mêmes actions métier**, pas deux implémentations.

## Lisibilité et geckos vivants

Le Sudoku doit rester lisible avant d'être décoratif.

Dans les styles Gecko :
- le visuel peut être mignon et vivant ;
- animations courtes, locales et non envahissantes ;
- ne pas animer toute la grille en permanence ;
- les notes restent des petits chiffres classiques ;
- une bonne saisie peut déclencher une courte réaction Gecko ;
- une erreur peut déclencher une réaction visuelle légère ;
- sélection d'un même chiffre peut animer / souligner brièvement les représentations correspondantes ;
- Pierre peut être regardé par un gecko lors d'une intervention, à condition de ne pas gêner la lecture ;
- la célébration de fin peut être plus riche.

Ces animations sont de l'habillage et ne doivent pas toucher au moteur Sudoku.

## Prof Gecko / Pierre partagé entre les deux modes

Le système vivant GECKO-037 reste un service commun :
- catalogue 309 ;
- mémoire 48 h ;
- mood / contexte ;
- voix Pierre ;
- bulle synchronisée ;
- animations Prof ;
- règles de priorité de parole.

Le **contexte joueur** peut être alimenté par le Sudoku, mais la pédagogie Sudoku devra être spécifique à ses techniques.

À terme, le Prof Sudoku pourra expliquer des techniques humaines :
- single nu ;
- single caché ;
- candidats verrouillés ;
- paires / triplets ;
- X-Wing ;
- autres techniques seulement si réellement implémentées et explicables.

Ne pas utiliser un solveur opaque pour prétendre expliquer une déduction humaine.

## Difficulté Sudoku

La difficulté doit idéalement refléter les techniques nécessaires à la résolution, pas uniquement le nombre de cases données.

Cette règle est une intention fonctionnelle à respecter lors de la future conception du générateur / solveur Sudoku.

## Invariants de non-régression

L'ajout du Sudoku ne doit pas modifier le comportement validé de GeckoDoku.

À protéger explicitement :
- géométrie de la grille historique ;
- gestes historiques ;
- logique GeckoDoku ;
- animations GeckoDoku ;
- Pierre / ProfParle ;
- moteur 309 phrases ;
- priorités audio ;
- intros ;
- célébration ;
- accessibilité ;
- réglages existants ;
- journal média ;
- pipeline vidéo validé.

Principe directeur :
**on ajoute le Sudoku autour de l'architecture existante ; on ne tord pas le moteur GeckoDoku pour en faire un Sudoku.**

## Évolutivité

Le routeur de mode doit permettre plus tard un troisième mode sans devoir réécrire toute l'UI.

Même principe pour les styles visuels : ajouter un quatrième style ne doit pas modifier les règles Sudoku.

Cette séparation GameMode / VisualStyle est un contrat architectural durable.



<!-- GECKO-038-RED-START-2026-09-27 -->
## GECKO-038 — lancement autorisé
Fab donne le GO explicite. Branche isolée `gecko-038-sudoku-mode`. Première étape : RED de contrat pur avant implémentation. Aucun merge main ni release autorisé.


<!-- GECKO-038-CORE-GREEN-2026-09-27 -->
## GECKO-038 — cœur Sudoku
RED CI #145 confirmé. Cœur pur ajouté : GameMode séparé du VisualStyle, SudokuPuzzle 9×9, moteur de saisie/notes/undo-redo, solveur de comptage, générateur conservant une solution unique et hints humains single nu/caché. La couche UI n'est pas encore câblée à ce commit.


<!-- GECKO-038-CORE-COMPILE-FIX-2026-09-27 -->
Correction cœur : collection des notes typée par son contrat `MutableSet<Int>` pour permettre la restauration undo/redo indépendamment de l'implémentation concrète du Set.


<!-- GECKO-038-UI-SURFACE-2026-09-27 -->
## GECKO-038 — surface Sudoku isolée
Après CI #147 GREEN, la couche UI Sudoku est ajoutée en classes séparées : grille/hitboxes Sudoku, overlay de valeurs click-through et sélecteur 3 positions. GeckoBoardView reste inchangé. Les planches PNG canoniques sont copiées par blob Git identique depuis main, sans conversion.


<!-- GECKO-038-ASSET-CATALOG-2026-09-27 -->
Les deux planches Sudoku Gecko sont référencées par AssetMediaCatalog avec leurs chemins canoniques exacts. Aucun traitement du fichier source.


<!-- GECKO-038-MAIN-WIRING-2026-09-27 -->
## GECKO-038 — câblage fonctionnel Sudoku
MainActivity route maintenant le GameMode sans remplacer GeckoBoardView. L'état GeckoDoku historique et l'état Sudoku coexistent ; basculer de mode change la visibilité et le routage, pas les listeners historiques. Contrôles Sudoku : case tactile, pavé 1..9, Notes, Effacer, Undo/Redo ; clavier 1..9, flèches, Suppr, N, P. Le style est prévisualisé pendant le glissement et persisté au relâchement. Prof Sudoku utilise les hints humains disponibles et le moteur vivant commun.


<!-- GECKO-038-CI150-CONTRACT-2026-09-27 -->
CI #150 confirme la compilation complète du câblage Sudoku. Seul l'ancien contrat Réglages (liste exacte de 3 entrées) échoue : GAME_MODE devient une quatrième entrée, tout en conservant Son/Animations/Journal et affectsBoardLayout=false.
