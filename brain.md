# GeckoDoku — FAB Copilot brain

> Mémoire fonctionnelle courte et canonique.
> Pour l'historique ancien : `sauvegarde.md`.
> Pour les détails de fonctionnement : `docs/GECKODOKU-FONCTIONNEMENT.md`.
> En cas de contradiction, le code de `main`, la version, le SHA et la date priment sur les anciens numéros GECKO.

## Référence courante

- Branche canonique : `main`
- Branche active : `feature/gecko-hof-sync-v1`
- Mission active : `GECKO-HOF-SYNC-001`
- Base de mission : `1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- Version de départ : `0.15.43-dev`
- versionCode : `78`
- Release téléphone de référence : `phone-0.15.43-dev-run-403`
- Merge GECKO-048 → main : `17c6de0186c745c15fc042971eb09b4fe19a6299`
- CI finale main précédente : #405 verte
- Aucun ancien média supprimé ne doit être réintroduit.

## Règles de travail FAB Copilot

- Ne jamais recoder depuis une vieille mémoire si le code courant peut être lu.
- Après modification significative, synchroniser : `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md`, `ordres-de-mission.md`.
- `ordres-de-mission.md` contient le contrat actif, pas l'historique.
- Pas de merge `main` ni release sans validation explicite de Fab.
- `sauvegarde.md` est une archive froide : ne pas la charger par défaut.
- Les anciens identifiants GECKO peuvent avoir été réutilisés ; utiliser version + SHA + date pour lever toute ambiguïté.

## Hall of Fame global — mission active

Protocole public GeckoDoku v1 :
- hôte : `https://fab-hall-of-fame.gnrationsia.chatgpt.site` ;
- POST `/api/v1/games/geckodoku/scores` ;
- GET `/api/v1/games/geckodoku/sync` ;
- aucun secret/API key dans l’APK ;
- score déclaré avec `runId` idempotent ;
- champs propres aux modes nullable ;
- `seed` 64 bits en chaîne décimale pour éviter tout arrondi ;
- `metadata` extensible.

Architecture prévue :
`GlobalScorePayload → PendingScoreStore → GeckoDokuHallApiClient → GlobalScoreSyncCoordinator`.

Règle fondamentale : le payload est persisté avant réseau et ne doit jamais être recalculé au retry. Retrait de pending uniquement après `accepted:true`.

Le flux `/sync` est séparé des stats/progression locales : fusion globale par `scoreId`, curseur opaque `nextCursor`, aucun résultat distant ne crée une victoire personnelle.

## Produit

GeckoDoku regroupe quatre modes :
1. GeckoDoku Classic.
2. Sudoku.
3. Gomoku.
4. Abeilles & Geckos.

Éléments communs :
- Prof Gecko / voix Pierre locale ;
- difficultés selon le mode ;
- étoiles, erreurs, statistiques et Hall of Fame ;
- préférences locales ;
- export/import ;
- animations optionnelles ;
- overlays sans modifier la géométrie logique du plateau.

## GeckoDoku Classic

Règles principales :
- un Gecko par ligne, colonne et zone ;
- aucun contact horizontal, vertical ou diagonal ;
- givens verrouillés ;
- solution unique ;
- confirmation d'un Gecko produit ses exclusions automatiques.

Repères joueur :
- croix manuelles ;
- hypothèses colorées ;
- axes personnels ;
- repères personnels indépendants du Prof.

Hypothèses :
- couleurs de branches : jaune, vert, rouge, violet, bleu, orange ;
- une hypothèse active possède une aura de sa couleur ;
- les croix déduites dans cette branche prennent la même couleur ;
- les sous-hypothèses sont des enfants de la branche courante ;
- suppression d'une hypothèse parent → suppression automatique de ses croix filles + perte d'aura des descendants ;
- contradiction → branche signalée en sens interdit, descendants invalidés, croix/aura correspondantes retirées ;
- explorer une autre sous-branche supprime les descendants devenus hors branche.

## Sudoku

Grille 9×9, givens verrouillés.

Gestes actuels :
- simple clic : ouvre ou recible le pavé de saisie ;
- double clic : ouvre ou recible aussi le pavé ;
- appui long : repères personnels.

Pavé flottant persistant :
- quatre zones : `Choix`, `Candidats`, `Hypothèse`, `Prévisu` ;
- reste ouvert jusqu'à fermeture explicite par sa croix ou changement de contexte ;
- toucher une autre case recible le même pavé ;
- déplaçable par drag ;
- redimensionnable jusqu'à environ `140×160 dp` ;
- poignée de resize compacte ;
- passage tactile extérieur vers la grille sur Android récent.

Choix :
- le chiffre apparaît d'abord en Prévisu ;
- confirmation `Êtes-vous sûr ? Oui / Non` ;
- pendant confirmation, la croix de fermeture disparaît ;
- Non efface seulement la prévisu ;
- Oui applique la validation Sudoku normale ;
- le pavé reste ouvert après validation.

Candidats :
- indépendants des hypothèses ;
- plusieurs candidats peuvent coexister.

Hypothèses Sudoku :
- même logique parent/enfant que les autres modes ;
- couleurs jaune, vert, rouge, violet, bleu, orange ;
- suppression parent → descendants + croix filles supprimés ;
- contradiction → sens interdit + rollback descendant ;
- changement de sous-branche → nettoyage des descendants de l'ancienne branche.

Aide `?` :
- active un mode documentaire ;
- toucher Choix / Candidats / Hypothèse / Prévisu fait expliquer cette zone par Pierre ;
- cette aide documentaire ne compte pas comme assistance de résolution et ne retire pas d'étoile.

## Gomoku

- plateau exploratoire avec zoom/drag ;
- cinq alignés gagnent ;
- humain vs humain ou humain vs Prof ;
- difficulté du Prof ajuste sa force ;
- Prof peut expliquer menaces, ligne étudiée, coup conseillé et projection.

## Abeilles & Geckos

Plateau hexagonal compact.

Géométrie canonique :
- Q = +60° = ↖↘
- S = -60° = ↙↗
- R = 0° = ←→

Règles :
- exactement un Gecko et une Abeille par zone ;
- paire Gecko/Abeille voisine et exclusive 1↔1 ;
- contraintes d'axes Q/R/S.

Hypothèses :
- même modèle de branches colorées parent/enfant que Classic ;
- croix filles colorées ;
- aura sur hypothèses actives ;
- suppression/contradiction/changement de sous-branche nettoient descendants, croix et auras.

Axes joueur :
- Q/S/R ;
- jaune/vert/rouge ;
- drag ;
- suppression hors plateau ;
- persistance de la couleur.

## Prof Gecko / Pierre

- Pierre est la voix locale du Prof ;
- modèle Sherpa/Piper UPMC Medium, voix Pierre ;
- `PierrePronunciationPolicy` ne modifie que le texte vocal ;
- exemple : affichage `église`, synthèse `eglize` ;
- paroles pédagogiques prioritaires sur décorations ;
- aide documentaire `?` Sudoku ≠ assistance de résolution ;
- animations Prof et parole restent des couches séparées.

## Statistiques

Une tentative n'est plus comptée au simple lancement.

Règle :
- partie terminée → enregistrée ;
- partie annulée sans erreur → ignorée ;
- partie annulée avec erreur → enregistrée comme abandon.

Par niveau :
- nombre de parties statées ;
- terminées ;
- abandonnées avec erreur ;
- erreurs ;
- temps moyen ;
- étoiles moyennes / meilleur score ;
- tendance temps ;
- tendance étoiles.

Tendances :
- comparent les deux dernières parties terminées du niveau ;
- temps plus bas = plus rapide ;
- étoiles plus hautes = progression.

Prof au début :
- parle seulement du niveau le plus difficile ayant des données et, s'il existe, du niveau juste précédent ;
- signale uniquement les tendances utiles de vitesse et d'étoiles.

Menu Stats :
- chaque niveau est cliquable ;
- graphe temporel temps + étoiles ;
- Hall of Fame navigable par mode/niveau et relié aux mêmes stats.

## Médias / sprites

Runtime Gecko/Abeille :
- banques SpriteRGBA préconstruites en `240p` uniquement ;
- anciennes banques 60/120/180/360/480 supprimées/prunées ;
- MP4 Gecko/Abeille remplacés par les banques ne doivent pas revenir ;
- séquences logiques préchargées ; bitmaps bornés par cache LRU.

À conserver en vidéo :
- intros ;
- Plante ;
- Prof/Pierre ;
- médias explicitement encore utilisés.

Invariants :
- média décoratif ≠ logique du jeu ;
- échec média ne doit pas bloquer une partie ;
- pas de reflow de plateau dû aux overlays.

## État validation

Baseline précédente validée techniquement :
- release 0.15.43-dev produite ;
- CI #403 verte ;
- réconciliation main #404 verte ;
- main #405 verte ;
- merge main explicitement validé par Fab.

Mission HOF globale : design approuvé en conversation ; spec écrite, en attente de revue écrite avant plan d’implémentation.
