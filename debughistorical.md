# Debug historical — GeckoDoku

## 2026-09-25 — Grille initiale non unique
Correction : validation d'unicité puis génération sécurisée.

## 2026-09-25 — Workflow APK absent
Correction : GitHub Actions ajouté.

## 2026-09-25 — Deux clics simples pouvaient créer un gecko
Correction v0.2 : intentions séparées + GestureDetector.

## 2026-09-25 — Unique ne signifie pas humainement résoluble
Correction : HumanSolver explicable.

## 2026-09-25 — Difficulté peu fidèle
Correction v0.3 : classement par techniques réellement indispensables.

## 2026-09-25 — Projection de zone
Ajout REGION_TOUCH_PROJECTION pour les zones à 2–4 candidats partageant une interdiction de voisinage.

## 2026-09-25 — Gecko X-Wing
Ajout lignes, colonnes et paires de zones.

## 2026-09-25 — Professeur absent alors que le solveur connaît le chemin
### Symptôme
Une grille peut être logiquement résoluble mais le joueur ne sait pas quelle déduction le solveur a utilisée. Exemple téléphone : 12×12 Difficile, projection requise ; une exclusion oubliée masquait la suite.
### Cause
SolveAnalysis gardait seulement technique + action, sans preuve visuelle ni état avant l'étape. L'UI ne consommait pas la trace.
### Correction v0.4
- SolveStep enrichi avec sources, zones, axe et préconditions ;
- Puzzle conserve solverTrace ;
- ProfessorGecko réutilise cette trace ;
- fallback nextStep sur l'état réel du joueur ;
- révélation progressive 1/3, 2/3, 3/3 ;
- surlignage pédagogique.
### Non-régression
Le Prof ne joue pas automatiquement. Si aucune déduction sûre n'est trouvée, il l'annonce.

## Vigilances v0.4
- vérifier que le cache choisit toujours une étape encore valide ;
- tester les chemins humains différents de la trace ;
- tester projection et X-Wing visuellement ;
- surveiller la lisibilité du surlignage sur 12×12 ;
- éviter que les explications deviennent trop longues sur petit écran.


## 2026-09-25 — Prof Gecko masqué par la barre Android
### Symptôme
Sur téléphone 12×12, le bouton Prof Gecko apparaît derrière la barre de navigation Android et n'est pas utilisable normalement. Le haut de l'interface empiète également sur la barre d'état.
### Cause
L'activité cible Android API 36 et l'interface programmatique n'appliquait aucun WindowInsets système.
### Correction v0.4.1
Application des insets systemBars haut, bas, gauche et droite au conteneur racine, en plus du padding de base. Compatibilité API 26–36.
### Non-régression
Prof Gecko doit rester entièrement visible et cliquable avec navigation Android par boutons comme par gestes.

## 2026-09-25 — Facile nécessitant deux X-Wing
### Symptôme
Test humain sur grille 12×12 affichée « Facile • zones 1 • trace 7 » : deux Gecko X-Wing ont été nécessaires pour terminer.
### Interprétation
Si ces deux X-Wing sont logiquement indispensables, le classement Facile est faux malgré l'analyse actuelle.
### Décision
Bug enregistré comme GECKO-012. Ne pas le mélanger au correctif UI urgent ; auditer ensuite DifficultyIndexer, solverTrace et la notion de technique réellement nécessaire.


## 2026-09-25 — Impossible de rejouer/conserver une bonne grille
### Besoin
Une grille appréciée devait pouvoir être rejouée à l'identique et conservée pour être testée de nouveau par le joueur ou une autre personne.
### Correction v0.5
Ajout d'un journal local persistant et de l'action Rejouer. Le journal stocke la définition originale et non la progression afin de garantir un redémarrage propre.

## 2026-09-25 — Stats sans réussite par difficulté
### Symptôme
L'application comptait les terminées par difficulté mais n'affichait pas le nombre de tentatives ni le taux de réussite par niveau.
### Correction v0.5
Exposition de started_diff + completed_diff et affichage completionRate pour chaque GameDifficulty.

## Vigilances v0.5
- journal toujours relisible après fermeture/réouverture de l'app ;
- supprimer une entrée sans affecter la partie courante ;
- vider le journal sans supprimer les stats ;
- rejouer doit conserver exactement la même définition de grille ;
- vérifier que le Prof reconstruit correctement sa trace au chargement ;
- vérifier que chaque replay/load compte bien comme nouvelle tentative ;
- GECKO-012 difficulté reste ouvert et non corrigé par cette version.


## 2026-09-25 — Niveaux au-delà du déterministe
### Besoin
Certaines fins de grille peuvent rester à deux possibilités après toutes les techniques, X-Wing compris. Le joueur souhaite tester les hypothèses plutôt que déclarer la grille insoluble.
### Correction v0.6
Ajout HypothesisSolver borné. Il ne brute-force pas la solution : il ne branche que sur des paires de deux candidats et exige une contradiction démontrable avant de forcer l'autre choix.
Mission Impossible = une hypothèse.
Infernal = deux hypothèses ou profondeur 2.
### Vigilance
Valider humainement que les contradictions et les textes du Prof sont naturels. Refuser les grilles nécessitant plus de profondeur.

## 2026-09-25 — Prof sans bulle visuelle
### Besoin
Le texte d'indice devait ressembler à une parole du Prof en plus des repères sur la grille.
### Correction v0.6
ProfessorBubbleView, bulle BD accessible et temporaire, masquée dès que le joueur agit.

## 2026-09-25 — Fin de partie trop sobre
### Besoin
Récompense visuelle proportionnelle à la difficulté.
### Correction v0.6
VictoryCelebrationView procédural, sans asset lourd. Confettis et feux d'artifice augmentent avec le niveau, sans stroboscope. Toucher ferme l'overlay.

## Vigilances v0.6
- génération Mission Impossible / Infernal peut être plus coûteuse, surtout 12×12 ;
- tester que le Prof retrouve une hypothèse sur un état réellement bloqué ;
- vérifier qu'aucune branche n'est rejetée sans contradiction ;
- vérifier lisibilité de la bulle sur petit écran ;
- vérifier overlay de victoire avec barres Android ;
- GECKO-012 reste ouvert : classification Facile/X-Wing à auditer séparément.


## 2026-09-25 — Prof montre mais ne joue pas
### Besoin
Le joueur souhaite pouvoir demander au Prof de poursuivre réellement la résolution, une étape à la fois, au lieu de devoir recopier chaque exclusion/gecko indiqué.
### Correction v0.7
Ajout GameEngine.applyProfessorStep. Chaque pression applique une seule déduction déterministe puis s'arrête.
### Hypothèses
Une bifurcation à deux candidats utilise deux phases : deux geckos fantômes d'abord, contradiction et mutation seulement à la pression suivante. L'overlay fantôme ne touche pas aux hypothèses personnelles.
### Garde-fou
Un gecko forcé est contrôlé contre la solution unique avant toute mutation. Une divergence devient une alerte de debug et non une erreur joueur.

## 2026-09-25 — Réussites assistées indifférenciées
Correction v0.7 : stats globales et par difficulté conservent le nombre de grilles terminées avec Prof, tout en gardant le taux de réussite standard.


## 2026-09-25 — Bulle Prof écrase la grille
### Symptôme
Capture téléphone v0.7 : en 12×12, la grande bulle BD apparaît entre l'en-tête et le plateau et force la grille à devenir minuscule.
### Cause
ProfessorBubbleView était un enfant normal du LinearLayout vertical.
### Correction v0.8
La bulle est sortie du flux de layout et dessinée en overlay dans screenRoot. Les commandes secondaires sont temporairement masquées pour dégager l'espace visuel ; le bouton Prof reste toujours accessible. Une croix × ferme la bulle.
### Non-régression
La taille du plateau ne doit plus diminuer à cause de la longueur du texte du Prof.

## 2026-09-25 — Célébration sans FX dédiés
### Besoin
Les feux d'artifice et confettis doivent être accompagnés de sons cohérents et gradués.
### Correction v0.8
VictoryCelebrationView émet un callback par salve ; ToneFxFeedback synchronise lancement/explosion et accent final. Les callbacks différés sont annulables.


## 2026-09-26 — Risque de mélanger les rôles des nouveaux médias
### Constat
Plusieurs médias Gecko et Prof ont été ajoutés successivement. Une interprétation trop rapide avait commencé à confondre « banque d'actions », apparition, disparition, intro et portrait Prof.
### Décision GECKO-022
Figer un catalogue à rôle unique par fichier. Aucun fichier ne doit être réutilisé pour un autre rôle sans nouvel ordre de mission.
### Décision vidéos 30 s
Ne pas découper Gecko_actions_plusieurs.mp4 ni Prof_actions.mp4 maintenant. Les lire occasionnellement en totalité, 0→EOF, sans boucle.
### Décision rendu
L'habillage riche doit toujours être un overlay désactivable au-dessus du rendu normal. Aucune dépendance du moteur logique.
### Décision assets
Sortir les médias de la racine du dépôt et les ranger sous assets/gecko et assets/prof sans réencodage.
