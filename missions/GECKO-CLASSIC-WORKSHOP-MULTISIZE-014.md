# GeckoDoku — Atelier Classic multitailles H4 (014)

Ordre explicite de Fab, 16 octobre 2026 : corriger la génération qui restait cantonnée à une taille et adapter les exigences de preuve pour 5×5..12×12. Branche de travail : `feature/classic-workshop-multisize-h4-v2` depuis `feature/classic-infernal-workshop-v1`.

## Contrat

| Taille | Régions / couleurs / Gecko | X-Wing pour les candidats | Hypothèses |
|---|---:|---|---|
| 5×5 | 5 | facultatif | H2 à H4 |
| 6×6 | 6 | facultatif | H2 à H4 |
| 7×7 | 7 | facultatif | H2 à H4 |
| 8×8 à 12×12 | égales à N | au moins un X-Wing observé, nécessité à auditer | H2 à H4 |

Les tailles/couleurs Classic n'ont pas changé (palette 12 entrées). Un X-Wing absent dans la trace pour 5–7 ne bloque plus une grille H2+ ; pour 8–12 il reste requis dans la trace. La bibliothèque reste une bibliothèque de **candidats** avec solution unique et traces exportables, revue humaine et validation manuelle obligatoires avant leur emploi dans Infernal. Une H2 ne devient jamais une H4 par quatre suppositions indépendantes.

## Travail

- `InfernalWorkshopPolicy`: source unique pour rotation, contrat minimal et X-Wing.
- `InfernalWorkshopScheduler`: chaque tentative lit puis incrémente le curseur persistant sous verrou ; migration conservatrice de l'ancien état actif, pas d'effacement de bibliothèque, 8 compteurs indépendants et timestamps, demande manuelle ou WorkManager périodique selon contraintes Android ; pas d'exécution concurrente entre les deux jobs.
- `PuzzleGenerator`: analyse H2 puis H3/H4 tant que non résolu, X-Wing optionnel 5–7. Le modèle sans X-Wing est testé lorsqu'un X-Wing figure dans la trace, résultat borné/indécis conservé comme tel.
- `HypothesisSolver`: budget limité profondeur 4, annulation cohérente et report de recherche incomplète.
- `InfernalWorkshopStore`: le format et les clés des archives JSON restent compatibles avec l'atelier 013 (format 1) ; les conditions import/export/éligibilité respectent les tailles.
- `MainActivity`: réglages Atelier, démarrer rotation complète, compteur global, prochaine taille, bilan 8 tailles, filtres des candidats. Essais toujours exclus du Hall et des statistiques.
- `InfernalWorkshopRotationTest`: rotation 16 tentatives, X-Wing conditionnel, palette, profondeurs H2/H3/H4, validation manuelle.

## Limites / test réel

WorkManager ne calcule pas en continu et peut suspendre/reporter les lots. Les recherches H4 coûtent plus cher et peuvent n'aboutir à aucune grille dans les 25 secondes par lot. La CI vérifie code/tests et ressources téléphoniques, pas la production réelle d'un ensemble de grilles. Aucune publication Google Play, merge main ou Release sans feu vert distinct.
