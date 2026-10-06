# GeckoDoku — ordre de mission courant

## Statut

Mission active : `GECKO-HOF-SYNC-001` — synchronisation Hall of Fame global v1.

Référence de départ :
- branche canonique : `main`
- branche de travail : `feature/gecko-hof-sync-v1`
- base : `1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- version de départ : `0.15.43-dev`
- versionCode : `78`
- release de référence : `phone-0.15.43-dev-run-403`
- merge code précédent : `17c6de0186c745c15fc042971eb09b4fe19a6299`
- CI main précédente : #405 verte.

Le travail part du code de `main`, jamais d’un ancien ordre archivé.

## Contrat permanent FAB Copilot

1. Lire le dépôt avant toute modification.
2. Ne pas recoder depuis la mémoire.
3. Une demande de Fab remplace les anciennes consignes contradictoires sur le même sujet.
4. Synchroniser après chaque geste significatif :
   - `brain.md`
   - `brainmap.md`
   - `debughistorical.md`
   - `todo.md`
   - `ordres-de-mission.md`
5. Pas de merge `main` sans validation explicite de Fab.
6. Pas de release/prerelease sans validation explicite de Fab.
7. Ne jamais restaurer des médias supprimés uniquement parce qu'ils existent dans un ancien commit.
8. `sauvegarde.md` est archive froide et ne doit pas servir de vérité courante.
9. En cas de conflit d'identifiants GECKO, utiliser version + SHA + date.

## Mission GECKO-HOF-SYNC-001

Objectif : raccorder les quatre modes GeckoDoku au protocole public Hall of Fame global v1 déjà publié.

Contrat serveur canonique :
- hôte : `https://fab-hall-of-fame.gnrationsia.chatgpt.site`
- POST : `/api/v1/games/geckodoku/scores`
- sync : `/api/v1/games/geckodoku/sync`
- aucun header Authorization requis ;
- aucun secret serveur dans l’APK ;
- `runId` idempotent ;
- `seed` 64 bits transportée en chaîne décimale quand nécessaire ;
- champs spécifiques nullable ;
- `metadata` extensible.

Implémenter :
- payload global immuable ;
- persistance `pendingScores` avant réseau ;
- retry progressif et respect de `Retry-After` ;
- retrait uniquement après `accepted:true` ;
- cache/sync global par `scoreId` et curseur `nextCursor` ;
- reprise après redémarrage et retour réseau ;
- tests unitaires et build.

Préserver strictement :
- gameplay des quatre modes ;
- calcul d’étoiles ;
- statistiques et progression locales ;
- Hall of Fame local ;
- médias et géométrie ;
- aucune donnée personnelle ou matérielle supplémentaire.

Document de design :
`docs/superpowers/specs/2026-10-06-geckodoku-global-hof-sync-design.md`

Aucun merge `main` ni release sans ordre explicite de Fab.

## Baseline fonctionnelle à préserver

### Sudoku
- pavé persistant 2×2 ;
- simple/double clic ouvrent ou reciblent ;
- appui long = repères personnels ;
- drag + resize ;
- minimum ~140×160 dp ;
- Choix / Candidats / Hypothèse / Prévisu ;
- confirmation Oui/Non avant valeur définitive ;
- candidats distincts des hypothèses ;
- aide `?` interactive par zone, sans coût d'assistance ;
- hypothèses parent/enfant colorées avec rollback.

### Classic / Abeilles & Geckos
- hypothèses parent/enfant colorées ;
- aura de branche ;
- croix filles colorées ;
- prune descendants à suppression, contradiction ou changement de sous-branche ;
- sens interdit sur contradiction ;
- axes personnels et repères Prof séparés.

### Statistiques
- terminée = statée ;
- annulée 0 erreur = ignorée ;
- annulée avec erreur = statée ;
- erreurs par niveau ;
- tendances sur les deux dernières parties terminées ;
- graphes Stats + Hall of Fame ;
- Prof de début limité au niveau le plus difficile + précédent.

### Médias
- Gecko/Abeille runtime SpriteRGBA 240p ;
- ne pas réintroduire MP4 Gecko/Abeille supprimés ;
- Plante, Prof/Pierre et intros conservés ;
- médias décoratifs sans effet sur logique ou géométrie.

## Travail actuellement autorisé

- mission `GECKO-HOF-SYNC-001` selon le design approuvé ;
- corrections de bugs découvertes lors des tests téléphone de 0.15.43-dev ;
- mise à jour des cinq mémoires ;
- documentation pure.

Toute extension hors de ce périmètre nécessite un nouvel avenant ou ordre de Fab.
