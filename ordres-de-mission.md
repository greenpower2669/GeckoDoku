# GeckoDoku — ordre de mission courant

## Statut

Aucune mission de développement nouvelle n'est ouverte après le merge GECKO-048.

Référence :
- branche : `main`
- version : `0.15.43-dev`
- versionCode : `78`
- release : `phone-0.15.43-dev-run-403`
- merge code : `17c6de0186c745c15fc042971eb09b4fe19a6299`
- CI main : #405 verte.

Le prochain travail doit partir du code de `main`, pas des anciens ordres archivés.

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

## Travail actuellement autorisé sans nouveau contrat

Uniquement :
- corrections de bugs découvertes lors des tests téléphone de 0.15.43-dev ;
- mise à jour de ces cinq mémoires ;
- documentation pure.

Toute nouvelle fonctionnalité doit être ajoutée ici comme nouvelle mission avant développement.
