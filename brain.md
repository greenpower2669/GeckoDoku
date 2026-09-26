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
- run #97 : GREEN complet pour v0.10.11-dev, tests + APK + AAB + artifact.

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
