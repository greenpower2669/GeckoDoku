# GeckoDoku — synchronisation Hall of Fame global v1

Date : 2026-10-06
Mission : GECKO-HOF-SYNC-001
Branche : `feature/gecko-hof-sync-v1`
Base : `main` @ `1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`

## 1. Objectif

Ajouter à l’APK GeckoDoku une synchronisation fiable avec le Hall of Fame global déjà publié, sans modifier le gameplay, les statistiques locales, la progression, ni le Hall of Fame local.

Le contrat serveur de référence est `GeckoDoku — protocole global v1` publié par le site. L’APK ne doit embarquer aucun secret et ne doit envoyer aucune donnée personnelle ou matérielle autre que le pseudo public choisi.

## 2. Endpoints canoniques

Base publique : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`

- POST score : `/api/v1/games/geckodoku/scores`
- GET catégories : `/api/v1/games/geckodoku/categories`
- GET classement : `/api/v1/games/geckodoku/scores?...`
- GET sync : `/api/v1/games/geckodoku/sync?cursor=<cursor>&limit=100`

Aucun header `Authorization` n’est requis pour GeckoDoku. `GECKODOKU_HOF_API_KEY` n’est donc ni nécessaire ni autorisé dans l’APK.

## 3. Architecture Android

Ajouter quatre composants isolés :

1. `GlobalScorePayload`
   - représentation immuable du score global v1 ;
   - sérialisation JSON conforme au protocole ;
   - champs communs obligatoires et champs spécifiques nullable ;
   - `seed` sérialisée en chaîne décimale lorsqu’elle est renseignée.

2. `PendingScoreStore`
   - persistance locale de chaque payload avant toute tentative réseau ;
   - conservation du JSON exact et du `runId` ;
   - aucune reconstruction du score lors d’un retry ;
   - suppression seulement après ACK accepté.

3. `GeckoDokuHallApiClient`
   - client HTTP minimal ;
   - POST score ;
   - GET sync ;
   - aucune logique gameplay ;
   - limite de timeout raisonnable ;
   - aucune donnée sensible dans les logs.

4. `GlobalScoreSyncCoordinator`
   - orchestre queue, retry et synchronisation globale ;
   - un seul envoi actif à la fois ;
   - retry progressif ;
   - respect de `Retry-After` sur HTTP 429 ;
   - reprise au lancement de l’app et au retour du réseau.

Le réseau doit rester hors du thread UI.

## 4. Création du score

Le payload global est figé au moment exact où la partie terminée est déjà acceptée par la logique locale.

Ordre obligatoire :

1. calculer les valeurs finales locales ;
2. créer une seule fois un `runId` ;
3. construire le payload global ;
4. enregistrer le payload dans `pendingScores` ;
5. continuer le flux local normal ;
6. tenter l’envoi de manière asynchrone.

Un échec réseau ne doit jamais annuler une victoire locale, modifier les étoiles ou bloquer l’écran de fin.

## 5. Champs communs

Toujours envoyer :

- `schemaVersion = 1`
- `scoreVersion = 1`
- `runId`
- `playerName`
- `mode`
- `difficulty`
- `size`
- `completed = true`
- `stars`
- `elapsedSeconds`
- `mistakes`
- `assistancePoints`
- `usedProfessor`
- `completedAt`
- `appVersion`

Les champs facultatifs sont `puzzleId`, `seed`, `sudokuVisualStyle`, `gomokuMatchMode`, `gomokuWinner`, `gomokuDraw`, `gomokuMoveCount`, `beeGeckoRadius`, `beeGeckoPairCount`, `metadata`.

Lorsqu’une information n’existe pas dans le moteur courant, elle reste absente ou `null`. Ne rien inventer.

## 6. Adaptation par mode

### GECKODOKU

- `size` = côté du plateau ;
- champs spécifiques des autres modes à `null` ;
- `puzzleId` / `seed` uniquement si l’information existe réellement dans le modèle courant.

### SUDOKU

- `size = 9` ;
- `seed` = seed du `SudokuPuzzle`, encodée sans perte ;
- `sudokuVisualStyle` = valeur technique courante ;
- autres champs de mode à `null`.

### GOMOKU

- `size` = taille réelle du moteur ;
- `gomokuMatchMode` = `VS_PROFESSOR` ou `HUMAN_VS_HUMAN` ;
- `gomokuWinner` = `PLAYER`, `PROFESSOR` ou `null` ;
- `gomokuDraw` ;
- `gomokuMoveCount` depuis le snapshot final ;
- aucune seed inventée.

### BEES_GECKOS

- `size` = nombre de paires/régions, conformément aux stats actuelles ;
- `puzzleId` = identifiant du puzzle existant ;
- `seed` = seed du puzzle, en chaîne décimale ;
- `beeGeckoRadius` = rayon réel ;
- `beeGeckoPairCount` = nombre réel de paires, égal à `size`.

## 7. Persistance pendingScores

Le store doit être crash-safe au niveau applicatif : un score est d’abord écrit localement, puis seulement envoyé.

Chaque entrée persistée contient au minimum :

- payload JSON exact ;
- `runId` ;
- date d’ajout ;
- compteur de tentatives ;
- prochaine date de retry ;
- dernier code HTTP / dernière erreur non sensible si utile au diagnostic.

La taille de la file doit être bornée par une limite haute généreuse. Une entrée acceptée est retirée. Une entrée non acceptée n’est jamais supprimée silencieusement.

## 8. Politique HTTP

### Succès

- HTTP 201 + `accepted:true` : retirer de pending ;
- HTTP 200 + `accepted:true`, y compris `duplicate:true` : retirer de pending.

### Erreurs corrigibles / temporaires

- erreur réseau : conserver et retry ;
- HTTP 429 : conserver, respecter `Retry-After` ;
- HTTP 503 : conserver et retry ;
- autres 5xx : conserver et retry avec backoff borné.

### Erreurs permanentes ou à diagnostiquer

- HTTP 400 : conserver l’entrée mais la marquer bloquée pour diagnostic ; ne pas boucler agressivement ;
- HTTP 409 `RUN_ID_CONFLICT` : conserver et marquer bloquée ; ne jamais générer un nouveau runId pour masquer le conflit ;
- HTTP 413 / 415 : conserver et marquer bloquée ; journaliser la cause sans exposer de contenu sensible.

Le coordinateur ne doit pas dépasser les quotas du serveur.

## 9. Backoff et réseau

Politique proposée : 5 s, 15 s, 30 s, 60 s, puis plafond à 5 min pour les erreurs temporaires, avec remise à zéro après succès.

Le retour du réseau peut déclencher une tentative anticipée, sans lancer plusieurs workers concurrents.

L’intégration doit utiliser les primitives Android disponibles dans le projet sans introduire une grosse dépendance réseau si elle n’est pas nécessaire.

## 10. Synchronisation globale `/sync`

Ajouter un cache local séparé du Hall of Fame personnel.

Stocker :

- entrées globales indexées par `scoreId` ;
- curseur opaque `nextCursor` ;
- métadonnées serveur utiles (`sequence`, `receivedAt`).

Algorithme :

1. GET avec le curseur local ;
2. fusion idempotente des entrées par `scoreId` ;
3. réconciliation éventuelle de ses propres envois par `runId` ;
4. persistance des entrées et de `nextCursor` dans la même opération logique ;
5. si `hasMore:true`, continuer même si la page est vide ;
6. ne jamais avancer depuis un ACK POST ni depuis `highWatermark`.

Les scores globaux reçus ne doivent jamais alimenter les statistiques personnelles ni la progression locale.

## 11. Déclencheurs

Déclencher la vidange de pending :

- après ajout d’un score terminé ;
- au lancement/reprise de l’application ;
- au retour d’une connectivité exploitable ;
- après expiration du délai de backoff.

Le sync global peut être lancé après la vidange de pending et à l’ouverture du Hall global, sans bloquer l’interface.

## 12. Manifest et configuration

Ajouter la permission Android `INTERNET`.

Conserver l’URL publique dans une constante de configuration non secrète. Aucun secret GitHub ou serveur n’est injecté dans l’APK.

## 13. Journalisation

Logs autorisés :

- `runId` tronqué ou complet si nécessaire au diagnostic local ;
- état queue ;
- HTTP status ;
- nombre d’éléments synchronisés ;
- temporisation de retry.

Ne jamais logger : email, identifiant matériel, GPS, contenu personnel non nécessaire. Ne jamais ajouter ces données à `metadata`.

## 14. Tests automatisés

Tests unitaires requis :

1. payload commun conforme ;
2. payload GECKODOKU ;
3. payload SUDOKU ;
4. payload GOMOKU Prof ;
5. payload GOMOKU humain ;
6. payload BEES_GECKOS ;
7. `seed` `Long.MIN_VALUE` ;
8. `seed` `Long.MAX_VALUE` ;
9. persistance/relecture de pending ;
10. retry conserve exactement le même `runId` et les mêmes données sportives ;
11. ACK 201 retire ;
12. ACK 200 duplicate retire ;
13. 400 bloque sans supprimer ;
14. 409 bloque sans régénérer ;
15. 429 applique attente ;
16. 503 conserve ;
17. fusion `/sync` par `scoreId` ;
18. `nextCursor` n’avance qu’après persistance de la page.

Puis lancer les tests existants et le build Android approprié.

## 15. Validation téléphone

Scénarios obligatoires :

1. terminer une vraie partie en ligne → une seule entrée serveur ;
2. terminer hors ligne → score visible localement et présent dans pending ;
3. rétablir Internet → même `runId` envoyé puis retiré de pending ;
4. forcer un retry/doublon → aucune duplication serveur ;
5. redémarrer l’app avec pending non vide → reprise correcte ;
6. vérifier au moins un mode avec seed 64 bits ;
7. vérifier que le Hall global n’altère ni stats ni progression locales.

## 16. Hors périmètre

- aucune modification des règles de jeu ;
- aucune modification du calcul des étoiles ;
- aucun merge `main` ;
- aucune release ;
- aucune authentification utilisateur ;
- aucun secret dans l’APK ;
- aucune refonte de l’UI du Hall local dans ce lot.

## 17. Critères de fin

Le lot est techniquement prêt lorsque :

- tous les tests nouveaux et existants sont verts ;
- le build Android est vert ;
- aucune donnée n’est perdue en simulation offline/retry ;
- les cinq mémoires FAB Copilot sont synchronisées ;
- la branche est laissée prête pour validation téléphone ;
- aucune action de merge/release n’a été faite sans ordre explicite de Fab.
