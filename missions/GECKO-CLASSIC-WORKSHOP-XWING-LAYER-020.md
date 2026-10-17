# Mission 020 — Couche X-Wing persistante et exploration secondaire

Date : 17/10/2026 • ordre Fab : « rajoute cette couche^^ xwing détecté sauvegardé et une secondes couche explore justement grilles avec xwing ».

Branche : `feature/classic-workshop-xwing-two-layer-v7`, issue de `feature/classic-workshop-resumable-deletion-v6`. Aucun merge `main`, aucune Release, aucune signature Play sans validation explicite.

## Contrat

La construction historique par retrait des Gecko reste intacte. Au passage d'une grille **à solution unique**, chercher une élimination **Gecko X-Wing réellement observable** (immédiate ou après déductions humaines), l'archiver avec géométrie et indices de la figure ; aucune promotion Infernal automatique.

Une seconde couche reprend des fiches archivées `PENDING`, lance les mêmes démonstrations H2–H4, la vérification de solution unique et la comparaison sans X-Wing propres à la v6. Les candidats prouvés vont dans la bibliothèque habituelle sous statut `CANDIDATE` pour approbation manuelle ; les recherches achevées mais non admissibles passent `EXPLORED` ; les preuves réussies `PROMISING`. Une pause/erreur laisse `PENDING`. Archive non effacée. Déduplication par empreinte géométrie+indices.

La couche 1 conserve sa recherche générale ; lorsque la preuve d'une grille avec X-Wing est déléguée, elle poursuit les retraits sans attendre l'analyse approfondie. Le service alterne une passe d'exploration archivée et une exploration générale complète. Il n'existe aucun délai arbitraire mettant définitivement au rebut une grille.

## Vérifications

- [x] Détecteur X-Wing et distinction observable / indispensable.
- [x] Archive locale durable et dédupliquée des grilles détectées.
- [x] Seconde couche de preuve H2–H4 avec conservation des suspensions.
- [x] Compteurs distincts dans l'interface (repérées, en attente, explorées, prometteuses).
- [x] Tests unitaires de l'absence de promotion, interruption, observation géométrique.
- [ ] Vérifier compilation/tests sur la branche v7.
- [ ] Essais réels multi-tailles et longues durées Samsung : consommation, thermique, reprises.
- [ ] Vérifier que le détecteur couvre les X-Wing cachés dans des branches hypothétiques ; aujourd'hui il couvre les grilles initiales et la chaîne déductive déterministe, pas toutes les hypothèses.
- [ ] Évaluer le risque de blocage mutuel de progression pendant une preuve H4 particulièrement longue (service mono-fil) ; optimiser sans rejeter le travail de preuve.
