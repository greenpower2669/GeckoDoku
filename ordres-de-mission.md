# GECKO-038 — SECOND MODE SUDOKU — ORDRE DE MISSION FUTUR

STATUT : **CONCEPTION VALIDÉE / CODE INTERDIT POUR L'INSTANT.**
Ce document prépare le workflow. Ne rien implémenter tant que Fab n'a pas écrit explicitement GO / code / lance la mission.

## Objectif

Ajouter un vrai mode Sudoku 9×9 dans GeckoDoku tout en garantissant que le mode GeckoDoku historique reste inchangé.

Architecture cible :
- GameMode : GECKODOKU ou SUDOKU ;
- VisualStyle Sudoku indépendant : CLASSIC_NUMBERS / GECKO_NB / GECKO_COLORED ;
- moteurs de règles séparés ;
- services Pierre / Prof vivant partagés ;
- rendu Gecko du Sudoku en couche visuelle lorsque cela réduit le risque de régression.

## Assets canoniques à intégrer sans transformation

Présents sur main :
- assets/gecko/PlancheGeckoDeNombreNB.png
- assets/gecko/PlancheGeckoDeNombreColored.png

Interdit :
- conversion ;
- recompression ;
- réexport ;
- remplacement ;
- génération d'assets dérivés sauf nouvelle autorisation explicite.

## Workflow obligatoire après GO

### PHASE 0 — Geler la référence
1. relever HEAD, versionName, versionCode ;
2. relever CI GREEN de référence ;
3. documenter les comportements téléphone GeckoDoku à préserver ;
4. vérifier les deux PNG source ;
5. aucun changement de code dans cette phase.

### PHASE 1 — Audit anti-régression
Identifier précisément :
- création / rendu des cases ;
- comportement des cases vides ;
- hitboxes ;
- listeners tactiles ;
- clavier éventuel ;
- moteur GeckoDoku ;
- Prof / speech / bulle ;
- animations superposées.

But : lister les points qui doivent rester historiques et ceux qui peuvent être routés par GameMode.

### PHASE 2 — RED avant architecture
Écrire des tests prouvant :
- mode par défaut = comportement GeckoDoku actuel ;
- aucune modification des règles historiques ;
- aucun double listener ;
- changement de mode ne modifie pas les données de l'autre mode ;
- VisualStyle ne modifie jamais les données Sudoku ;
- overlay visuel ne reçoit pas les touches.

Les RED doivent échouer avant implémentation.

### PHASE 3 — GameMode minimal
Introduire le concept GECKODOKU / SUDOKU avec **GECKODOKU uniquement actif fonctionnellement au départ**.

Cette étape ne doit pas encore créer le Sudoku.
Elle sert à prouver que l'abstraction peut être ajoutée sans changer le comportement historique.

Préférence :
bindings stables → routeur de mode → action spécifique.

Éviter :
unbind global → rebind global.

CI + non-régression obligatoire avant suite.

### PHASE 4 — SudokuEngine pur
Créer un moteur indépendant testable sans UI :
- 9×9 ;
- valeurs 1..9 ;
- lignes ;
- colonnes ;
- blocs 3×3 ;
- givens immuables ;
- validation ;
- notes/candidats ;
- état de victoire.

Ne réutiliser le moteur GeckoDoku que pour des utilitaires réellement génériques.

### PHASE 5 — Actions utilisateur communes
Définir des actions métier Sudoku uniques :
- sélectionner case ;
- saisir 1..9 ;
- note 1..9 ;
- effacer ;
- undo ;
- redo ;
- demander Prof.

Le tactile et le clavier doivent appeler ces mêmes actions.

### PHASE 6 — Renderer Sudoku
Créer le rendu Sudoku sans toucher à la géométrie GeckoDoku historique.

Protection :
- case vide Sudoku = vide ;
- pas de gecko historique injecté par erreur ;
- givens visuellement distincts ;
- valeurs joueur distinctes ;
- notes lisibles.

### PHASE 7 — Couche visuelle Gecko on-top
Lorsque possible :
- renderer métier dessous ;
- overlay Gecko dessus ;
- overlay click-through ;
- hitboxes inchangées ;
- aucune donnée stockée dans l'image.

Mapping :
- valeur Sudoku 1..9 → sourceRect dans la planche NB ou colorée ;
- Classic → chiffre natif.

### PHASE 8 — Sélecteur magique 3 états
Un seul contrôle horizontal :
- gauche Classic ;
- milieu Gecko NB ;
- droite Gecko Coloré.

DOWN :
- prendre le contrôle du preview.

MOVE :
- déterminer la zone ;
- agrandir fortement l'état ciblé ;
- réduire les deux autres ;
- changer immédiatement previewStyle ;
- redessiner la grille en direct ;
- haptique discret au franchissement de cran.

UP :
- valider selectedStyle = previewStyle ;
- persister ;
- aucun bouton OK.

Un ancien geste ne doit jamais valider un état ultérieur par callback obsolète.

### PHASE 9 — Prof Gecko partagé
Réutiliser :
- 309 phrases ;
- 48 h ;
- lastPhraseId ;
- mood ;
- Pierre ;
- bulle ;
- ProfParle.

Ajouter un adaptateur d'événements Sudoku au contexte joueur.

Ne pas encore prétendre expliquer une technique Sudoku non réellement implémentée.

### PHASE 10 — Pédagogie Sudoku
Implémenter progressivement des techniques humaines testables.
Ordre suggéré :
1. single nu ;
2. single caché ;
3. candidats verrouillés ;
4. paires / triplets ;
5. X-Wing ;
6. techniques supplémentaires seulement si nécessaires.

Chaque explication du Prof doit correspondre à une déduction réellement démontrable.

### PHASE 11 — Validation anti-régression
Avant release :
- tests unitaires ;
- CI ;
- APK/AAB ;
- lancement GeckoDoku historique ;
- gestes historiques ;
- cases vides historiques ;
- Prof ;
- Intro ;
- ProfParle ;
- célébration ;
- audio ;
- grille ;
- puis tests Sudoku.

### PHASE 12 — Validation téléphone Fab
Fab valide :
- GeckoDoku inchangé ;
- Sudoku jouable ;
- gros contrôles lisibles ;
- sélecteur 3 états naturel ;
- preview live sans latence gênante ;
- assets exactement conformes aux PNG fournis ;
- aucun touch bloqué par overlay ;
- aucune animation nuisible à la lisibilité.

## Règle d'arrêt

À la première régression du mode GeckoDoku historique :
- arrêter l'ajout de fonctionnalités ;
- renforcer logs/tests ;
- corriger la couche d'abstraction ;
- ne pas empiler d'autres changements.

## Principe directeur

**Ajouter le Sudoku autour de GeckoDoku ; ne jamais transformer le moteur historique pour le forcer à devenir un Sudoku.**

