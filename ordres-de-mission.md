# GECKODOKU — ORDRE DE MISSION ACTIF

Date : 2026-09-27

## 0. STATUT VALIDÉ — GOMOKU

Validation téléphone Fab :

- titre corrigé ;
- mode Gomoku contre Prof Gecko validé ;
- mode humain contre humain validé ;
- animations vidéo validées ;
- les animations suivent correctement le zoom ;
- les animations suivent correctement les déplacements / drag ;
- rendu jugé parfait.

RÈGLE :

**Ne plus modifier le Gomoku dans cette mission**, sauf régression directement provoquée par un changement transversal.

Le Gomoku devient une référence stable à préserver.

---

## 1. CLASSIC — RÉORGANISER LE LAYOUT, PAS LA GRILLE LOGIQUE

Constat téléphone :

- la grille Classic n'est pas correctement centrée horizontalement ;
- elle pourrait être sensiblement plus grande ;
- trop de place est consommée par les commandes sous la grille ;
- Sauver et Journal n'ont pas besoin d'être visibles en permanence.

Objectif :

- ne modifier ni les règles, ni le moteur, ni les zones, ni les gestes de la grille ;
- agrandir la grille au maximum de l'espace réellement disponible ;
- centrer proprement le carré de jeu ;
- supprimer toute impression de grille collée à gauche ;
- éviter les offsets magiques.

### Réorganisation demandée

Déplacer dans l'engrenage / Réglages :

- ⭐ Sauver ;
- 📚 Journal.

Ils restent entièrement accessibles, mais ne prennent plus une ligne permanente de boutons.

Réorganiser les commandes principales de façon compacte, par exemple :

- Taille + Difficulté ;
- Nouvelle + Rejouer ;
- Stats + ! + ⚙️ ;
- Prof Gecko.

Le but est de récupérer de la hauteur **et** de permettre au conteneur de grille d'exploiter toute la largeur utile.

La grille doit être calculée comme le plus grand carré lisible possible dans l'espace disponible, puis centrée horizontalement.

---

## 2. GÉNÉRATION PAR DIFFICULTÉ — NE PAS ABANDONNER AVANT D'AVOIR LE NIVEAU DEMANDÉ

Lorsque le joueur demande explicitement une difficulté :

- le générateur / solveur continue à générer et évaluer des grilles ;
- il ne retombe pas silencieusement sur une difficulté voisine ;
- il ne présente la grille que lorsqu'elle correspond réellement au niveau demandé.

Le calcul doit tourner en tâche de fond afin de ne pas bloquer l'interface Android.

Pendant la recherche :

- afficher un état clair du type « Recherche d'une grille Facile… » ;
- conserver une interface réactive ;
- permettre au joueur d'annuler ou de choisir un autre niveau.

La recherche continue **jusqu'à ce que la difficulté demandée soit trouvée**, sauf annulation explicite de l'utilisateur.

Ne jamais annoncer « Facile », « Difficile », etc. si l'évaluation réelle du solveur ne correspond pas.

---

## 3. HISTORIQUE — L'UTILISATEUR DOIT POUVOIR LE VIDER

Ajouter une commande explicite pour vider l'historique.

La suppression doit demander confirmation avant effacement.

Elle doit viser les données d'historique / résultats terminés, sans détruire par accident :

- les paramètres ;
- les sauvegardes de parties ;
- le nom du joueur ;
- les préférences d'accessibilité.

Si plusieurs historiques existent, présenter clairement ce qui sera effacé.

---

## 4. STATISTIQUES PAR NIVEAU

Les statistiques doivent être consultables globalement **et par niveau de difficulté**.

Pour chaque difficulté, conserver au minimum les informations pertinentes déjà disponibles, ainsi que :

- parties commencées ;
- parties terminées ;
- réussites ;
- temps si déjà suivi ;
- usage du Prof / aides ;
- meilleure note en étoiles ;
- distribution des étoiles si utile.

Ne pas mélanger artificiellement des difficultés différentes dans un seul résultat de performance.

---

## 5. SYSTÈME D'ÉTOILES — QUALITÉ DE RÉSOLUTION

Une partie terminée entièrement par le joueur, sans aide, vaut :

**★★★★★ — 5 étoiles.**

Principe fondamental :

**plus le joueur utilise d'aide, moins il obtient d'étoiles.**

Les aides doivent être réellement comptabilisées, notamment selon les fonctions disponibles :

- conseil / indice du Prof ;
- explication demandée ;
- coup ou étape joué par le Prof à la place du joueur ;
- aide directe équivalente.

Le calcul doit être déterministe et documenté.

Ne pas punir un simple affichage automatique ou une animation : seules les aides réellement demandées par le joueur comptent.

La politique précise peut pondérer les aides, mais elle doit respecter cet ordre :

- aucune aide → 5 étoiles ;
- aide légère → moins de 5 ;
- aide directe / plusieurs aides → note encore plus basse ;
- résolution très assistée → note minimale.

La note obtenue est enregistrée avec la partie terminée.

---

## 6. HALL OF FAME

Créer / compléter un Hall of Fame lisible.

Il doit montrer au minimum :

- nom du joueur ;
- mode ;
- niveau de difficulté ;
- étoiles obtenues ;
- date / ordre de réalisation si déjà disponible ;
- temps ou score si le projet le suit déjà.

Les étoiles doivent être visibles directement dans le Hall of Fame, par exemple :

★★★★★
★★★★☆
★★★☆☆

Le Hall of Fame doit pouvoir être filtré ou lu par niveau afin de comparer des performances comparables.

---

## 7. NOM DU JOUEUR

Nom par défaut :

**GeckoTétu**

Le joueur doit pouvoir modifier ce nom.

Le nom choisi doit être utilisé dans :

- Hall of Fame ;
- statistiques si un nom est affiché ;
- nouvelles entrées de résultats.

Le changement de nom ne doit pas forcément réécrire rétroactivement les anciennes entrées si elles stockent déjà un nom ; ce comportement doit être explicite et stable.

Le nom est un paramètre persistant.

---

## 8. EXPORT / IMPORT COMPLET

Dans les Réglages, ajouter des fonctions claires :

- Exporter mes données ;
- Importer mes données.

L'export doit permettre de sauvegarder dans un fichier portable et versionné **toutes les données utilisateur utiles** :

- paramètres ;
- préférences ;
- nom du joueur ;
- statistiques ;
- Hall of Fame ;
- historique ;
- sauvegardes de parties ;
- progression ;
- réglages son / animations / accessibilité ;
- choix de modes et difficultés persistants ;
- toute autre donnée réellement persistante de GeckoDoku.

Ne pas exporter les caches temporaires inutiles.

### Import

Avant import :

- valider le format ;
- valider la version ;
- refuser proprement un fichier corrompu ;
- éviter toute perte partielle en cas d'échec.

Prévoir une stratégie sûre :

- import transactionnel ;
- ou sauvegarde automatique de l'état courant avant remplacement.

Si des versions de schéma existent, prévoir une migration explicite.

L'import doit restaurer les données cohérentes sans casser les parties sauvegardées.

---

## 8 bis. GOMOKU — PROF GECKO RESTE TEIGNEUX À TOUS LES NIVEAUX

Nouvelle règle produit validée par Fab pour la 0.13 :

- dans le Gomoku, Prof Gecko garde toujours son caractère compétitif, taquin et un peu méchant ;
- cette personnalité ne doit jamais être adoucie automatiquement par la difficulté ;
- **le mode Découverte n'est pas une exception** ;
- ses conseils restent utiles et exacts, mais leur formulation conserve son mordant ;
- ses réactions de début de partie, conseil, coup joué, victoire, défaite, match nul et petites interventions doivent rester cohérentes avec ce personnage ;
- la difficulté ne doit modifier que la force de jeu / profondeur stratégique, jamais transformer Prof Gecko en professeur gentil.

Le ton reste drôle et familial : piquant, jamais insultant ni humiliant.

---

## 9. TESTS OBLIGATOIRES

### Classic layout
- grille centrée ;
- grille plus grande quand l'espace le permet ;
- aucune zone coupée ;
- aucune régression tactile ;
- Sauver et Journal accessibles depuis ⚙️.

### Difficulté
- demander chaque difficulté ;
- vérifier que la grille réellement générée correspond ;
- vérifier que le générateur continue tant qu'il n'a pas trouvé ;
- annulation utilisateur propre ;
- interface non bloquée.

### Étoiles
- partie sans aide → 5 étoiles ;
- avec conseil → moins de 5 ;
- avec coup joué par Prof → baisse supplémentaire ;
- résultat conservé après redémarrage.

### Statistiques / Hall of Fame
- statistiques par niveau ;
- étoiles visibles ;
- nom GeckoTétu par défaut ;
- nom modifiable ;
- nouvelles entrées utilisent le nouveau nom ;
- vider historique fonctionne avec confirmation.

### Export / import
- export complet ;
- réinstallation / état vierge simulé ;
- import ;
- paramètres restaurés ;
- sauvegardes restaurées ;
- stats / Hall of Fame / étoiles restaurés ;
- nom restauré ;
- fichier invalide refusé proprement.

### Non-régression
- Gomoku reste inchangé et parfait ;
- Sudoku reste fonctionnel ;
- Classic conserve ses règles et seulement son layout évolue.

---

## 10. FIN DE MISSION

Quand tout est :

- codé ;
- testé ;
- CI GREEN ;
- validé téléphone ;

alors mettre à jour :

- brain.md ;
- brainmap.md ;
- debughistorical.md ;
- todo.md.

Ne conserver dans todo.md que les vrais restes.

Le Gomoku étant validé, ne pas rouvrir ses anciens bugs sans nouvelle observation réelle.

<!-- GECKO-041-V013-ACTIVE-2026-09-28 -->
## STATUT D'EXÉCUTION 0.13

Version cible : **0.13.0-dev** / versionCode **33**.

Fab invalide le statut documentaire précédent qui présentait GECKO-040 comme déjà livré.
La 0.13 devient la candidate réelle de cette mission.

Implémentation de cette passe :
- stats séparées par mode + difficulté tout en conservant les compteurs globaux ;
- Gomoku branché aux stats et au Hall of Fame lors d'une victoire contre Prof Gecko ;
- aides Gomoku comptabilisées dans les étoiles ;
- Hall of Fame présenté par mode puis difficulté ;
- Prof Gecko teigneux en Gomoku à toutes les difficultés, **Découverte comprise** ;
- tests et CI 0.13 avant validation téléphone.

<!-- GECKO-041-V013-ARTIFACT-NAMING-2026-09-28 -->
### Correctif packaging 0.13
La CI #209 a validé tests + build, mais l'artefact était encore nommé 0.12 à cause d'un nom figé dans build.yml.
Le workflow doit désormais dériver automatiquement le nom APK/AAB/artefact depuis versionName afin que la candidate 0.13 soit identifiable sans ambiguïté.

<!-- GECKO-041-V013-CI210-GREEN-2026-09-28 -->
## JALON TECHNIQUE 0.13 VALIDÉ

- Commit packaging : `cae4ed410314a3aa0e1b76a78ebd2b0dbf36eed8`
- CI : **#210 GREEN**
- Tests unitaires : GREEN
- Build APK : GREEN
- Build AAB : GREEN
- Artifact : **GeckoDoku-v0.13.0-dev-Android**
- Artifact id : `10944486757`
- Digest : `sha256:7b91aec1489ccb13556efae3d7b94466c0ec24dd967af3266d6b768499137371`

Reste uniquement la validation téléphone Fab avant nettoyage/clôture.
---

# GECKO-042 — ABEILLES & GECKOS — ORDRE DE MISSION ACTIF

Date : 2026-09-28
Version cible : **0.14.0-dev** / versionCode **34**.

## Correction de compréhension Gomoku à conserver

La note GECKO-041 « Prof teigneux » est **supersédée** par la précision Fab :
- le problème observé n'est pas le caractère du Prof ;
- en **Découverte**, le Prof Gomoku joue actuellement trop fort et gagne trop souvent ;
- la difficulté doit réellement piloter sa force de jeu ;
- le Prof ne doit détailler sa stratégie **que lorsque le joueur lui demande une explication** ;
- lorsqu'une explication est demandée, elle doit montrer la suite réellement anticipée, avec repères graphiques et séquence de coups, comme une explication d'échecs, et non une phrase vague du type « prépare la suite ».

Ce correctif Gomoku reste un chantier distinct ; ne pas le confondre avec le nouveau mode.

## Règle canonique du nouveau mode

**ABEILLES & GECKOS** est un puzzle logique sur grande grille hexagonale exploratoire.

- Chaque Gecko touche exactement **UNE** Abeille.
- Chaque Abeille touche exactement **UN** Gecko.
- Les couples sont exclusifs : **1 Gecko ↔ 1 Abeille**.
- « Toucher » signifie partager **un côté d'hexagone** ; un sommet ne compte jamais.
- Le solveur, le générateur, la validation et le Prof utilisent la même topologie hexagonale.
- Le plateau se navigue comme Gomoku : drag fluide, zoom avant/arrière, pinch, caméra bornée.

## Architecture et géométrie

1. Utiliser de vraies coordonnées hexagonales axiales/cube ; ne jamais utiliser les pixels comme logique.
2. Centraliser le voisinage dans une seule fonction canonique de six voisins.
3. Séparer strictement coordonnées logiques et conversion écran.
4. Geckos et Abeilles sont tous deux des objets logiques, jamais des bonus/décorations.
5. Toute association confirmée réserve simultanément le Gecko et l'Abeille et élimine les relations concurrentes.
6. Le solveur propage dans les deux directions Gecko→Abeille et Abeille→Gecko jusqu'à stabilisation.
7. Le générateur produit directement une solution complète avec autant de Geckos que d'Abeilles ; aucune Abeille ajoutée au hasard après coup.
8. Les grilles doivent viser une solution unique contrôlée et une difficulté issue du raisonnement, pas seulement de la taille.

## Grande carte exploratoire

9. Le plateau est volontairement plus grand que l'écran aux niveaux supérieurs.
10. Pan/drag à un doigt et pinch zoom à deux doigts doivent être fluides.
11. Le point visé doit rester stable pendant le zoom.
12. Un mouvement dépassant le seuil de drag ne doit jamais déclencher un faux tap.
13. La caméra doit empêcher de perdre totalement la carte hors écran.
14. Une commande discrète de recentrage doit être disponible dans ⚙️.
15. Les aides du Prof, traits, candidats et surbrillances utilisent les coordonnées de la carte et suivent parfaitement zoom + drag.
16. À faible zoom conserver une lecture globale ; à fort zoom conserver les pièces et aides lisibles.

## Raisonnement Prof Gecko — uniquement sur demande

17. Le Prof comprend le 1↔1, les réservations et les chaînes de propagation.
18. Lorsqu'il est demandé, son raisonnement visuel suit cet ordre : pièce de départ → six directions → candidats → impossibilités → réservés → dernière possibilité → déduction.
19. L'explication textuelle/orale décrit la même déduction que la projection graphique.
20. Les aides restent attachées à la grille pendant zoom et drag.
21. Une demande de conseil compte comme aide ; une association appliquée par le Prof compte comme aide directe pour les étoiles.

## Joueur, validation et solveur

22. Les états Gecko / Abeille / vide / marques doivent rester non ambigus.
23. Les repères personnels, lorsqu'ils sont branchés au mode, doivent être sauvegardés avec les coordonnées hexagonales.
24. Validation obligatoire : pièce sans partenaire, partage d'un partenaire, association non voisine, ou autre contrainte = invalide.
25. Le solveur conserve pour chaque Gecko les Abeilles candidates et réciproquement.
26. Une relation réservée disparaît des autres ensembles de candidats.
27. Les chaînes de type A forcé→B réservé→C perd B→C forcé doivent être gérées.
28. Une grille complète n'est valide que si toutes les pièces appartiennent exactement à un couple.

## Difficulté et génération

29. Prévoir plusieurs dimensions et densités selon le niveau.
30. La difficulté peut varier par nombre de couples, candidats, ambiguïtés, longueur de propagation et interactions de zones.
31. Difficile/Expert ne doivent pas être seulement « plus grand » : le raisonnement doit s'allonger.
32. La carte doit permettre de résoudre une région, naviguer vers une autre, puis revenir.
33. Les indications de zone non terminée/contradiction/récente restent discrètes ; pas de mini-carte surchargée.

## UI, accessibilité, performance

34. Le plateau reste l'élément principal ; fonctions secondaires dans ⚙️/menus.
35. Ne jamais dépendre uniquement de la couleur : Gecko/Abeille et états doivent rester reconnaissables par leur forme/sprite.
36. Garder tailles minimales, contraste, surbrillances et aides lisibles avec zoom.
37. Éviter tout recalcul global à chaque frame de drag ; rendre et recalculer seulement ce qui est nécessaire.
38. L'ambiance peut évoquer ruche/nature/feuillage, sans réduire la lisibilité.

## Persistance, progression et résultats

39. Statistiques séparées de Classic/Sudoku/Gomoku : parties, réussite, difficulté, temps, aides, étoiles.
40. Étoiles : jusqu'à 5 sans aide ; conseils et coups du Prof diminuent la note selon la politique existante.
41. Hall of Fame : joueur, mode, niveau, étoiles, temps/score pertinent.
42. Une partie en cours sauvegarde puzzle, couples, aides, temps, zoom et position caméra ; reprise exacte après redémarrage.
43. Export/import global inclut toutes les données persistantes propres au mode sans casser les autres modes.
44. Effacer l'historique ne doit jamais supprimer la sauvegarde active ni les paramètres.
45. Non-régression obligatoire : Classic, Sudoku, Gomoku, Prof, animations, zoom/drag, Hall, étoiles, stats, import/export, paramètres et nom joueur.

## Assets fournis par Fab sur main

Sources canoniques :
- `AbeilleTr.png` — portrait/sprite transparent de l'Abeille ;
- `Abeillefondvert.mp4` — animation Abeille sur fond vert destinée au chroma key.

Dans l'application, les copies runtime sont rangées sous `assets/abeille/` afin de ne pas mélanger racine Git et catalogue Android.

## Priorité d'implémentation

1. grille / coordonnées / voisinage hexagonal ;
2. caméra zoom + drag + recentrage ;
3. Gecko + Abeille ;
4. règle exclusive 1↔1 et validation ;
5. solveur + propagation ;
6. générateur à solution contrôlée ;
7. Prof + projection visuelle ;
8. persistance/reprise ;
9. stats/étoiles/Hall/export-import ;
10. équilibrage des difficultés, notes personnelles, exploration avancée ;
11. tests logiques, caméra, Prof et non-régression ;
12. validation téléphone Fab.

## État de la passe en cours

La 0.14 démarre par une **fondation jouable de matching hexagonal** : les Geckos et Abeilles sont placés par le puzzle, et le joueur confirme les couples adjacents exclusifs. Cette représentation permet de valider proprement le cœur 1↔1 avant d'ajouter les raffinements de saisie/notes.

Ne pas déclarer les 45 points terminés avant tests CI + validation téléphone.
### Avancement GECKO-042 — après CI #213

- moteur hexagonal, matching 1↔1, solveur, générateur, caméra, Prof, sauvegarde, stats/Hall/export-import : intégrés ;
- repères personnels hexagonaux : ajoutés et persistants ;
- recentrage global + accès à la prochaine zone non résolue : intégrés dans ⚙️ ;
- différenciation de secours Gecko/Abeille par forme/lettre si sprite indisponible ;
- CI #213 a validé la première fondation ; les ajouts postérieurs exigent une nouvelle CI.
### Packaging de test demandé par Fab

Pour les validations téléphone, privilégier désormais une GitHub Release/prerelease contenant directement l'APK, plutôt qu'un ZIP Actions contenant APK+AAB. La variante `phone` doit rester séparée de la vraie build `release` afin que la signature de test ne soit jamais confondue avec la future signature Google Play.
---

# GECKO-043 — CORRECTION CANONIQUE ABEILLES & GECKOS + GOMOKU + ÉTOILES

Date : 2026-09-28
Version cible : **0.15.0-dev** / versionCode **35**.

## A — Abeilles & Geckos : la conception de matching libre est abandonnée

Le mode n'est pas un jeu de reliage ni une grande carte Gomoku. Il reprend l'esprit du Classic : grille logique compacte, zones colorées, axes, élimination, solveur et difficulté mesurée.

Règles canoniques :
- plateau régulier de cases hexagonales, centré et adapté à l'écran ;
- zones colorées utilisant le langage visuel du Classic ;
- chaque zone contient exactement **1 Gecko + 1 Abeille** ;
- trois familles d'axes logiques hexagonaux, vues comme six directions opposées deux à deux ;
- pour une même famille de pièce, une ligne déjà occupée exclut les autres cases de cette ligne ;
- une Abeille et son Gecko partenaire sont **voisins directs**, donc partagent un côté ;
- le partenaire exclusif est celui de la même zone : 1 zone = 1 Gecko + 1 Abeille adjacents = 1 couple logique ;
- aucun lien longue distance ; une liaison graphique éventuelle reste locale entre deux voisins et uniquement pour l'explication ;
- le solveur combine zone + axes + type + voisinage + propagation ;
- le générateur part d'une solution complète, retire des données, vérifie unicité + solveur + difficulté réelle ;
- le générateur exact continue à chercher tant que la difficulté demandée n'est pas réellement obtenue.

Adaptation géométrique : dans un plateau hexagonal fini, les axes supplémentaires peuvent comporter plus de lignes que le nombre de zones. La règle d'axe canonique est donc **au plus une pièce de chaque type par ligne**. Les zones imposent le nombre total de Geckos/Abeilles. Cela conserve exactement la déduction demandée : « cette ligne possède déjà son Gecko/Abeille, donc les autres cases de la ligne sont exclues » sans imposer artificiellement qu'une pièce existe sur chaque ligne extérieure.

Gestes :
- tap simple = croix / exclusion, comme l'esprit Classic ;
- double tap = choisir Gecko ou Abeille sur la case ;
- appui long = repère personnel ;
- zoom/drag restent disponibles mais la vue initiale doit ajuster et centrer toute la grille.

Prof :
- jamais de réponse sèche ;
- sur demande seulement, il déroule la chaîne : zone analysée → candidats → exclusions d'axes/zone/voisinage → dernière possibilité → conclusion ;
- code visuel : bleu = analysé, orange = hypothèse, rouge = éliminé, vert = certain ;
- couleur toujours doublée d'un symbole/contour ;
- bulle au-dessus du plateau en z-order, jamais masquée par le board.

## B — Gomoku

Deux corrections obligatoires :
- difficulté réelle : Découverte/Facile voient moins loin et peuvent choisir un coup cohérent non optimal ; niveaux hauts augmentent profondeur, défense, pièges et doubles menaces ;
- pédagogie uniquement lorsque le joueur demande le Prof : montrer ligne, cases projetées, menace, blocage et suite anticipée. Les tours automatiques du Prof ne doivent pas réciter une explication générique.

Le précédent comportement « Prof teigneux partout » est supprimé : le ton redevient professeur/taquin, jamais hostile.

## C — étoiles

Règle globale : **1 erreur joueur = −3 étoiles**. Une aide coûte moins qu'une erreur afin de pousser l'ordre : réfléchir > demander une aide > tester au hasard. Le minimum affiché reste 1 étoile.

## Validation

- Abeilles & Geckos ressemble visuellement au Classic ;
- zones visibles, 1 Gecko + 1 Abeille par zone ;
- axes hexagonaux actifs ;
- partenaire local uniquement ;
- pas de trait longue distance ;
- solveur + unicité + génération exacte par difficulté ;
- Prof multi-étapes + projection graphique ;
- HUD / Prof / boutons jamais recouverts ;
- Gomoku réellement différencié par niveau + explications projetées sur demande ;
- une erreur enlève exactement 3 étoiles.
### GECKO-043 — implémentation Gomoku

Politique de force retenue :
- Découverte : profondeur 1, fenêtre de choix parmi plusieurs bons coups, défense pondérée et perception tactique limitée de façon déterministe ;
- Facile : même cohérence mais meilleure perception ;
- Réflexion : profondeur 2, choix resserré ;
- Difficile et au-delà : victoires/blocages immédiats toujours vus, pièges activés et choix optimal dans la fenêtre calculée ;
- Expert et niveaux supérieurs augmentent profondeur, largeur et poids défensif.

Le niveau faible n'est pas aléatoire ni absurde : il évalue de vrais coups mais peut ne pas voir une tactique immédiate ou choisir le 2e/3e bon candidat. Le comportement est déterministe pour rendre les tests reproductibles.

Lors d'un tour automatique du Prof, aucune explication stratégique n'est récitée. Lorsqu'une aide est demandée, la même décision produit une `GomokuReasoningTrace` contenant : type attaque/défense/piège, ligne concernée, extrémités menacées, case choisie et suite projetée. Le plateau affiche alors ligne bleue, menaces rouges, coup certain vert et projections orange numérotées.
