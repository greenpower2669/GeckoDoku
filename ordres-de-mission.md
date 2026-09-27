# GECKODOKU — ORDRE DE MISSION COMPLET
## GECKO-038 — UX SUDOKU TACTILE, PAVÉ TOUJOURS VISIBLE, APPUI LONG ET CANDIDATS VIVANTS

### STATUT

**MISSION DOCUMENTÉE — NE PAS CODER AVANT GO EXPLICITE DE FAB.**

Dépôt :
`greenpower2669/GeckoDoku`

Branche impérative :
`gecko-038-sudoku-mode`

HEAD documentaire au moment de la rédaction :
`929889b075a89be170fbad756dd7c5d4a32af50f`

IMPORTANT :
- continuer directement sur cette branche ;
- ne pas créer de nouvelle branche ;
- ne pas repartir de main ;
- ne pas fusionner main ;
- avant tout code, relire le HEAD réel ;
- si le HEAD a avancé, continuer sur le nouveau HEAD de cette même branche ;
- maintenir `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md` et ce fichier synchronisés à chaque intervention.

---

# 1 — CONTEXTE TÉLÉPHONE

Sur la candidate Sudoku actuelle, Fab observe sur téléphone :
- grille Sudoku 9×9 correctement visible ;
- Notes visible ;
- Effacer visible ;
- Undo / Redo visibles ;
- bouton Difficulté `FACILE` visible sur une ligne entière ;
- pavé 1–9 non visible ;
- sélecteur 3 états non visible dans la zone utile.

Le bouton Difficulté consomme inutilement une ligne entière alors que la difficulté est déjà affichée sous le titre.

La mission doit corriger le problème de disposition **sans supposer que supprimer ce bouton suffit à lui seul**.

---

# 2 — OBJECTIF GLOBAL

Améliorer le mode Sudoku téléphone avec quatre objectifs :

1. garantir que le sélecteur de style et le pavé 1–9 restent toujours visibles ;
2. déplacer la difficulté dans ⚙️ Réglages ;
3. ajouter une saisie locale par appui long sur une case ;
4. faire des candidats 1–9 un élément vivant de la grille, utilisable aussi par Prof Gecko.

---

# 3 — DIFFICULTÉ DANS ⚙️

Retirer le gros bouton Difficulté de l'écran principal Sudoku.

Ajouter / intégrer dans Réglages :

```text
Mode de jeu : Sudoku
Difficulté : Facile
Son
Animations
Journal vidéo
...
```

Le résumé sous le titre conserve l'information :

`Sudoku 9×9 • Facile • N cases données`

Ne pas dupliquer inutilement cette information par un gros bouton permanent.

Une modification de difficulté doit avoir un comportement explicite vis-à-vis de la partie en cours. Ne pas remplacer silencieusement la grille sans politique/test.

---

# 4 — GÉOMÉTRIE : PAVÉ ET SÉLECTEUR TOUJOURS VISIBLES

Le vrai bug à éviter est le chevauchement / masquage par la grille flottante.

La disposition cible :

```text
Titre / résumé
Grille 9×9
Sélecteur 3 états
Pavé 1–9
Notes / Effacer / Undo / Redo
Nouvelle / ! / ⚙️
Rejouer
Prof Gecko
```

Le plateau ne doit jamais recouvrir :
- sélecteur ;
- pavé 1–9 ;
- Notes ;
- Effacer ;
- Undo / Redo.

Préférer une policy/géométrie testable à des offsets magiques spécifiques à un téléphone.

Aucun reflow provoqué par une bulle, popup, animation ou Prof.

---

# 5 — SÉLECTEUR 3 ÉTATS À PRÉSERVER

Conserver le contrat existant :

- gauche = Classic ;
- milieu = Gecko N/B ;
- droite = Gecko couleur ;
- état ciblé très grand ;
- autres états petits ;
- glissement sans relever le doigt ;
- preview live de la vraie grille ;
- haptique discret au changement de cran ;
- relâchement = validation/persistance ;
- CANCEL = retour au style mémorisé ;
- aucun bouton OK.

Le correctif de géométrie doit rendre ce sélecteur réellement accessible sur téléphone.

---

# 6 — PAVÉ 1–9 À PRÉSERVER

Le pavé principal reste une méthode de saisie essentielle et accessible.

Il doit rester visible même si une saisie par appui long est ajoutée.

Appui simple :
- sélectionner case ;
- toucher 1–9 sur le pavé ;
- Notes / Effacer / Undo / Redo comme actuellement.

L'appui long est un accélérateur, pas une dépendance.

---

# 7 — APPUI LONG SUR UNE CASE SUDOKU

Sur une case Sudoku jouable, un appui long ouvre une palette tactile locale.

Cette palette doit permettre :
- valeur 1–9 ;
- mode / action candidats ;
- candidat 1–9 ;
- effacer selon contexte.

Elle doit rester simple, lisible et tactile.

Ne pas y placer les réglages globaux.

Positionnement intelligent :
- ne pas sortir de l'écran ;
- préférer le côté offrant de l'espace ;
- ne pas recouvrir inutilement la case ou la zone d'intérêt ;
- rester overlay ;
- ne jamais déplacer la grille.

---

# 8 — CANDIDATS DANS LES CASES

Chaque case vide peut afficher de 0 à 9 mini-candidats.

Disposition fixe :

```text
1 2 3
4 5 6
7 8 9
```

Les positions ne dépendent pas de l'ordre d'ajout.

Les candidats restent strictement à l'intérieur de la case.

Ils peuvent être petits/serrés, mais doivent rester identifiables.

Une valeur principale dans la case remplace l'affichage des candidats de cette case.

---

# 9 — CANDIDATS DANS LE STYLE VISUEL COURANT

Contrat :

**les mini-candidats utilisent le même VisualStyle que les valeurs principales.**

Classic :
- chiffres classiques.

Gecko N/B :
- mini Gecko/chiffres N/B issus de la planche canonique.

Gecko couleur :
- mini Gecko/chiffres colorés issus de la planche canonique.

Assets canoniques :
- `assets/gecko/PlancheGeckoDeNombreNB.png`
- `assets/gecko/PlancheGeckoDeNombreColored.png`

Interdiction :
- conversion ;
- recompression ;
- réexport ;
- remplacement.

Les découpes se font au rendu.

---

# 10 — PROF GECKO UTILISE LES MÊMES CANDIDATS

Prof Gecko doit pouvoir placer / montrer / retirer les mini-candidats dans la grille lors d'une explication, comme le mode historique sait poser ses repères pédagogiques.

Il n'existe pas deux systèmes visibles de candidats.

Joueur et Prof utilisent :
- mêmes positions ;
- mêmes chiffres/geckos ;
- même style ;
- même renderer.

Le Prof peut par exemple :
- montrer les candidats possibles d'une case ;
- supprimer visuellement des candidats impossibles ;
- mettre en évidence une possibilité ;
- expliquer une déduction avant de donner une réponse.

---

# 11 — MODÈLE DE DONNÉES

Ne jamais transformer les candidats en simples décorations.

Ils doivent rester liés à l'état Sudoku.

Concept :

```text
SudokuCell
 ├── value
 └── candidates 1..9
          ↓
renderer selon VisualStyle
```

Si le Prof nécessite une provenance interne, utiliser une métadonnée/policy claire sans créer une seconde grille concurrente.

---

# 12 — UNDO / REDO

Le joueur doit pouvoir annuler/refaire :
- ajout candidat ;
- retrait candidat ;
- effacement candidats ;
- saisie de valeur ;
- conséquences existantes du placement d'une valeur.

Pour le Prof, décider explicitement avant implémentation si ses marques sont :
- purement pédagogiques/transitoires ;
ou
- de vraies modifications de l'état candidat.

Ne pas polluer silencieusement l'historique Undo du joueur.

Auditer le comportement du mode 1 avant de choisir.

---

# 13 — PROF / BULLE : INVARIANT INTEMPOREL

Ne pas régresser l'issue validée téléphone.

Invariant :

**Toute phrase effectivement prononcée par Pierre apparaît avec exactement le même texte dans ProfessorBubbleView.**

La palette d'appui long et les candidats ne doivent pas :
- interrompre Pierre ;
- fermer sa bulle ;
- changer les priorités SpeechOrigin ;
- provoquer de reflow.

---

# 14 — TDD AVANT / AVEC LE CODE

Ajouter des garde-fous au minimum pour :

### Géométrie
1. le sélecteur possède une zone réservée ;
2. le pavé 1–9 possède une zone réservée ;
3. la grille ne recouvre jamais ces zones ;
4. aucune bulle/popup n'altère BoardGeometry.

### Réglages
5. Difficulté existe dans Settings en Sudoku ;
6. le gros bouton Difficulté n'est pas affiché dans l'écran Sudoku ;
7. GeckoDoku historique conserve ses contrôles prévus.

### Long press
8. appui long sur case jouable → palette locale ;
9. appui long sur given → comportement sûr/non destructif ;
10. ACTION_CANCEL ne laisse pas de popup fantôme ;
11. popup positionnée dans l'écran.

### Candidats
12. 1..9 occupent des positions déterministes 3×3 ;
13. candidats ne débordent pas de la case ;
14. Classic → candidat Classic ;
15. Gecko NB → candidat NB ;
16. Gecko Color → candidat Color ;
17. changer VisualStyle ne change pas le set de candidats ;
18. valeur principale masque les candidats de sa case.

### Prof
19. Prof et joueur ciblent le même système candidat ;
20. hint pédagogique peut montrer des candidats sans contourner le moteur ;
21. comportement Undo du Prof explicitement testé selon la policy retenue.

### Non-régression
22. moteur Sudoku existant toujours GREEN ;
23. GeckoBoardView historique inchangé sauf nécessité prouvée ;
24. invariant Pierre ↔ bulle toujours GREEN ;
25. clavier physique toujours fonctionnel.

---

# 15 — AUDIT AVANT CODE

Avant modification :
- relire HEAD réel ;
- relever CI de référence ;
- inspecter `MainActivity` ;
- inspecter `positionFloatingBoard()` ;
- inspecter les vues Sudoku ;
- inspecter l'ordre des `addView` et z-index ;
- inspecter la hauteur réellement réservée par le root/controls ;
- inspecter SettingsMenuPolicy ;
- inspecter SudokuGameEngine notes/candidates ;
- inspecter SudokuHintEngine ;
- inspecter le mécanisme de repères pédagogiques du mode GeckoDoku historique ;
- déterminer ce qui peut être réutilisé sans couplage.

---

# 16 — PHASES DE CODE FUTURES

## Phase A — RED géométrie
Écrire les contrats de visibilité avant correction.

## Phase B — Difficulté
Déplacer vers ⚙️ et libérer la ligne.

## Phase C — Layout
Réserver réellement les zones selector + pad.

CI GREEN obligatoire avant suite.

## Phase D — Long press
Ajouter une policy pure de présentation/positionnement de palette.

## Phase E — Candidats visuels
Adapter le renderer aux 3 VisualStyle.

## Phase F — Prof
Faire utiliser au Prof le même système candidat.

## Phase G — Undo / Redo
Valider tous les scénarios.

## Phase H — CI / APK / AAB
Tests + assembleDebug + bundle selon workflow.

## Phase I — Téléphone Fab
Valider perceptuellement.

---

# 17 — CRITÈRES DE SUCCÈS

La mission sera réussie si :

- [ ] le pavé 1–9 est toujours visible ;
- [ ] le sélecteur 3 états est toujours visible ;
- [ ] le bouton Difficulté principal a disparu en Sudoku ;
- [ ] la difficulté est accessible dans ⚙️ ;
- [ ] l'appui long sur case ouvre une palette locale confortable ;
- [ ] le joueur peut saisir une valeur via cette palette ;
- [ ] le joueur peut ajouter/retirer des candidats ;
- [ ] jusqu'à 9 candidats apparaissent dans une case en 3×3 ;
- [ ] les candidats suivent Classic / Gecko NB / Gecko Color ;
- [ ] les PNG canoniques restent inchangés ;
- [ ] Prof Gecko peut utiliser ces mêmes candidats pédagogiquement ;
- [ ] Undo / Redo reste cohérent ;
- [ ] aucun overlay ne déplace la grille ;
- [ ] aucun overlay ne bloque les touches de manière inattendue ;
- [ ] GeckoDoku historique reste sans régression ;
- [ ] Pierre ↔ bulle reste conforme ;
- [ ] CI GREEN ;
- [ ] APK/AAB GREEN ;
- [ ] validation téléphone Fab.

---

# 18 — INTERDICTIONS

- ne pas créer une autre branche ;
- ne pas repartir de main ;
- ne pas fusionner main ;
- ne pas réexporter les planches Gecko ;
- ne pas supprimer le pavé sous prétexte que le long press existe ;
- ne pas dupliquer les candidats joueur/Prof visuellement ;
- ne pas modifier GeckoBoardView pour corriger un problème purement Sudoku sans preuve ;
- ne pas publier de release sans GO explicite de Fab.

---

# 19 — AUTORITÉ FINALE

La CI prouve la cohérence technique.

**Le téléphone de Fab décide du confort réel.**


<!-- GECKO-038-TACTILE-RED-2026-09-27 -->
## ÉTAT D'EXÉCUTION
GO reçu. HEAD de départ `baac7df072786bad770b1b8236aada091390744f`. Phase actuelle : RED des contrats purs avant modification runtime. La policy Prof retenue suit le précédent du mode 1 : overlay pédagogique transitoire utilisant le même renderer, sans altérer l'historique joueur.


<!-- GECKO-038-TACTILE-GREEN-CORE-2026-09-27 -->
Bloc core posé. Étape suivante : câblage runtime, puis CI. Ne pas toucher GeckoBoardView.


<!-- GECKO-038-TACTILE-INPUT-SETTINGS-2026-09-27 -->
Sous-bloc input/settings posé ; prochaine étape renderer candidats puis Activity.


<!-- GECKO-038-TACTILE-STYLED-CANDIDATES-2026-09-27 -->
Rendu candidats terminé ; reste câblage Activity et validation CI.


<!-- GECKO-038-TACTILE-MAIN-WIRING-2026-09-27 -->
Câblage runtime terminé. Étape courante : CI puis corrections ciblées uniquement si nécessaire. Pas de nouvelles fonctions avant GREEN.


<!-- GECKO-038-TACTILE-RECT-IMPORT-2026-09-27 -->
Correction syntaxique/import uniquement ; attente CI suivante.


<!-- GECKO-038-TACTILE-GEOMETRY-RESET-2026-09-27 -->
#161 compilation corrigée : Rect + reset multi-mode. Attente CI courante.


<!-- GECKO-038-TACTILE-CANDIDATE-0111-2026-09-27 -->
Candidate 0.11.1-dev/code29 préparée. Ne plus ajouter de fonctionnalité avant CI candidate et test téléphone Fab.


<!-- GECKO-038-TACTILE-CI164-GREEN-2026-09-27 -->
STATUT : CODE GREEN / ATTENTE TEST TÉLÉPHONE FAB.
Ne plus ajouter de fonctionnalité avant retour téléphone sur 0.11.1-dev. Corriger seulement les défauts réellement observés, dans la couche Sudoku concernée. Aucun merge main, aucune release sans GO explicite.

<!-- GECKO-038-GRID-READABILITY-REVISION-2026-09-27 -->
# RÉVISION PRIORITAIRE DE LA MISSION — LISIBILITÉ GRILLE

Cette section **prime sur les sections antérieures incompatibles** de cet ordre de mission.

## 20 — CONSTAT TÉLÉPHONE 0.11.1-dev

La candidate 0.11.1-dev affiche désormais le pavé et le sélecteur, mais la grille est devenue trop petite.

Le correctif précédent a donc résolu le recouvrement au prix d'une perte de lisibilité.

Cette régression ergonomique doit être corrigée avant toute nouvelle fonction.

## 21 — CONTRAT DE LARGEUR DE GRILLE

La grille Sudoku est la priorité numéro 1 de l'écran.

Règle impérative :

**GRILLE = LARGEUR UTILE MAXIMALE DE L'ÉCRAN.**

Marge horizontale volontaire :
- gauche : **3 px maximum** ;
- droite : **3 px maximum**.

Donc, hors inset Android obligatoire :

```text
boardWidth = usefulScreenWidth - 6px maximum
boardHeight = boardWidth
```

Interdit :
- padding esthétique supplémentaire ;
- marge latérale importante ;
- réduction de grille pour faire tenir un gros pavé ;
- réduction de grille pour faire tenir les trois gros boutons du selector.

Si l'espace vertical manque, **compacter les commandes**, pas la grille.

## 22 — PRIORITÉ DES COMMANDES

Les commandes doivent s'adapter à la grille.

Le pavé 1–9 permanent n'est plus une obligation de mise en page si l'appui long fournit la même saisie de façon confortable.

Le selector 3 états peut être rendu plus compact tout en conservant :
- les 3 états ;
- le glissement ;
- preview live ;
- validation au relâchement.

Les fonctions doivent rester accessibles, mais leur encombrement permanent n'est pas prioritaire sur la grille.

## 23 — MINI-CANDIDATS : NOIR IMPÉRATIF

Les mini-candidats affichés dans les cases doivent être :

- chiffres 1–9 ;
- positions fixes 3×3 ;
- **couleur noire** ;
- contraste maximal ;
- indépendants du VisualStyle principal.

Cette décision remplace l'ancienne exigence :
> mini-candidats Classic / Gecko N/B / Gecko couleur.

Nouvel invariant :
> **valeur principale stylée ; candidats fonctionnels en noir.**

Même règle pour :
- candidats joueur ;
- candidats montrés par Prof Gecko.

Une mise en évidence pédagogique peut utiliser un fond, contour ou halo discret, mais le chiffre candidat reste noir.

## 24 — TDD À AJOUTER / MODIFIER

Avant ou avec le correctif :

### Géométrie
1. en mode Sudoku, largeur du plateau = largeur utile moins au plus 6 px ;
2. marge gauche <= 3 px hors inset ;
3. marge droite <= 3 px hors inset ;
4. grille reste carrée ;
5. GeckoDoku historique conserve sa géométrie indépendante ;
6. popup / bulle / Prof n'altèrent pas cette largeur.

### Candidats
7. chaque candidat 1..9 garde sa position 3×3 ;
8. candidat joueur utilise un glyphe noir ;
9. candidat Prof utilise le même glyphe noir ;
10. changement Classic → Gecko NB → Gecko Color ne change pas la couleur noire du candidat ;
11. valeur principale continue de suivre VisualStyle ;
12. valeur posée masque les candidats comme actuellement.

### UX
13. si tous les contrôles ne tiennent pas verticalement, la policy réduit/compacte les contrôles avant de réduire la grille ;
14. l'appui long reste accessible ;
15. aucune zone tactile critique n'est masquée.

## 25 — CRITÈRE DE SUCCÈS RÉVISÉ

La prochaine candidate est acceptable seulement si, sur le téléphone de Fab :

- [ ] grille quasiment pleine largeur ;
- [ ] <= 3 px de marge volontaire à gauche ;
- [ ] <= 3 px de marge volontaire à droite ;
- [ ] grille clairement plus grande que sur 0.11.1-dev ;
- [ ] mini-candidats noirs lisibles ;
- [ ] long press toujours fonctionnel ;
- [ ] accès aux styles toujours fonctionnel ;
- [ ] accès aux chiffres toujours fonctionnel, permanent ou contextuel ;
- [ ] Prof candidats fonctionnel ;
- [ ] aucune régression GeckoDoku ;
- [ ] invariant Pierre ↔ bulle intact ;
- [ ] CI APK/AAB GREEN.

## 26 — PRINCIPE DIRECTEUR

**La grille ne s'adapte plus aux boutons. Les boutons s'adaptent à la grille.**

<!-- GECKO-038-PROF-PLAY-GESTURE-2026-09-27 -->
# 27 — PROF GECKO : EXPLIQUER PUIS JOUER

Cette section complète la mission actuelle sans ajouter de nouveau bouton permanent.

## 27.1 Gestes
Premier appui court : explique seulement et crée un pending.
Deuxième appui court : si ce pending est toujours valide, joue le chiffre démontré.
Appui long sur Prof : recalcule et joue directement la prochaine déduction humaine sûre.
Appui long sur une case : reste la palette locale valeur/candidats/effacer.

## 27.2 Pending Prof
Le pending contient au minimum : révision/fingerprint de grille, cellule, chiffre, technique, candidats éventuels.
Il doit être invalidé ou revalidé après saisie joueur, candidat, effacer, Undo, Redo, Nouvelle, Rejouer, difficulté, changement de mode ou remplacement de puzzle.
Interdit : appliquer un pending obsolète.

## 27.3 Provenance
Ajouter une provenance PLAYER / PROFESSOR.
Un coup Prof utilise le même moteur Sudoku, est Undoable/Redoable, ne compte pas comme erreur et ne doit pas être crédité comme réussite autonome du joueur.

## 27.4 Candidats Prof
Avant application : overlay transitoire, petits chiffres noirs, aucune mutation des notes joueur, aucun Undo.
Après application : nettoyer l'overlay ; la vraie valeur suit le VisualStyle principal.

## 27.5 Pierre / bulle
Préserver l'invariant intemporel : tout texte prononcé par Pierre apparaît dans ProfessorBubbleView.

## 27.6 TDD obligatoire
1. premier tap explique et crée un pending sans changer la valeur ;
2. second tap même état applique exactement cellule/chiffre ;
3. second tap après mutation ne joue jamais un pending obsolète ;
4. long press recalcule et applique une déduction sûre ;
5. aucun hint sûr = aucune mutation ;
6. origin du coup auto = PROFESSOR ;
7. coup Prof Undoable ;
8. Redo restaure le coup Prof ;
9. pas de CORRECT_MOVE joueur artificiel ;
10. candidats Prof transitoires absents de Undo ;
11. overlay nettoyé après application ;
12. New/Replay/Difficulty/Mode invalident le pending ;
13. invariant Pierre↔bulle reste GREEN ;
14. long press case reste la palette locale.

## 27.7 Critère téléphone
tap Prof → explique et montre le candidat ;
tap Prof à nouveau → pose ce chiffre ;
long press Prof → joue directement la prochaine déduction sûre ;
Undo permet de revenir en arrière.

Aucun nouveau bouton permanent ne doit réduire la grille.
Le téléphone de Fab reste l'autorité finale.


<!-- GECKO-038-FULLWIDTH-PROF-RED-2026-09-27 -->
## ÉTAT EXÉCUTION
GO code reçu. Phase RED lancée sur HEAD `7b02a7379f09b39b858106408553d9e486df9de4`. Aucun correctif runtime avant preuve RED. Les quatre contrats testés sont : largeur réelle 3 px, candidats noirs, Prof explique/joue, provenance Undo/Redo.


<!-- GECKO-038-GREEN-POLICIES-2026-09-27 -->
Bloc policies GREEN posé. Suite : moteur provenance, renderer, puis Activity.
