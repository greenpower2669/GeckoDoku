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


<!-- GECKO-040-CODE-CI-GREEN-2026-09-28 -->
## STATUT D'EXÉCUTION

La mission est codée sur la branche de travail et passe la CI.

Jalons :
- Phase 1 Classic + difficulté stricte : commit `4912ff7f20b44275180a4fb8dd7ebf6e4c67eab3`, CI #204 GREEN.
- Phase 2 étoiles + Hall of Fame + GeckoTétu : commit `b72f149022faddf0d90599fea6a93557df619b53`, CI #205 GREEN.
- Phase 3 export/import : commit `72838027541b188c64e24e6983e3aae55fe70b3a`, CI #206 GREEN.
- Artifact Android : `GeckoDoku-v0.12.0-dev-Android`, id `10943373231`, digest `sha256:d0e7c17cdf6e886cb6a8b4d2c9184533d6892b5af0247cd429da45f388a13d13`.

RESTE OUVERT :
validation téléphone Fab. Ne pas vider cet ordre avant validation perceptuelle.
