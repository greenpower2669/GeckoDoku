# Mission 021 — Comparatif H1 avec Gecko X-Wing / H2 sans Gecko X-Wing

Date : 17/10/2026. Fab, après capture Android Atelier v7 (3 770 X-Wing observés, 111 explorés, zéro candidat), a proposé « une seule hypothèse », puis « deux hypothèse sans xwing », et a demandé « Voir les deux ».

Branche : `feature/classic-workshop-h1-xwing-h2-no-xwing-v8`, issue de `feature/classic-workshop-xwing-two-layer-v7`. Aucune fusion `main`, aucune Release / Play non autorisée.

## Plan expérimental A/B (grilles identiques)

Comparer les deux preuves sur la *même archive v7 de grilles UNIQUES présentant un Gecko X-Wing*. Cela garantit que les compteurs des deux méthodes partent du même échantillon sans effacer les résultats antérieurs. Limite scientifique : le groupe H2 sans X-Wing est actuellement sélectionné sur des grilles qui possèdent un motif X-Wing ; « sans X-Wing » désigne donc **le solveur sans la technique**, pas l'absence du motif sur le plateau. Tester un deuxième échantillonnage indépendant ultérieurement si souhaité.

- **Piste A « H1 + X-Wing »** : résolution humaine complète avec X-Wing autorisé, profondeur de preuve 1 et exactement une étape hypothétique, au moins une véritable élimination Gecko X-Wing dans la trace.
- **Piste B « H2 sans X-Wing »** : résolution avec `SolverRules.NO_X_WING` sous profondeur hypothétique strictement 2 (vraie imbrication), puis contrôle indépendant qu'une preuve H1 sans X-Wing ne résout pas la grille. Aucune élimination X-Wing autorisée dans sa trace.
- **Toutes les pistes** : unicité mathématique vérifiée séparément ; absence de budget temps/nœuds arbitraire ; arrêt Android thermique/manuelle/batterie => fiche non explorée conservée, pas de faux rejet ; pas de validation automatique.

## Implémentation

- `WorkshopComparisonPolicy` définit les deux pistes (tests négatifs sur X-Wing absent, H1 multiples, H2 non imbriquées ou H1 suffisant).
- `PuzzleGenerator.proveArchivedComparison` calcule le verdict de laboratoire par puzzle, en conservant les sous-arbres de preuve acquis.
- `WorkshopXWingQueueStore` ajoute un champ JSON facultatif `laboratory` par fiche v7 ; tous les anciens fichiers et champs v7 restent valides et inchangés hors ajout des résultats A/B.
- `InfernalWorkshopService` intercale génération par retrait et deux évaluations alternées H1/H2, sans mettre fin aux recherches v6/v7.
- `MainActivity` propose un menu « Comparer H1 + X-Wing / H2 sans X-Wing », des compteurs distincts et la prévisualisation jouable des grilles retenues sous statut NON homologué.
- `InfernalWorkshopPolicy` et `InfernalWorkshopStore` d'origine sont volontairement inchangés : **une H1 ou une H2 sans X-Wing n'entre jamais dans Infernal sans nouvelle validation/autorisation**.

## Contrôles et limites

- [x] Tests unitaires de politique A/B écrits avant le code, premier test RED constaté (classes manquantes).
- [ ] Run final GitHub Actions vert : tests JVM, APK, assets Pierre et 240p.
- [ ] Recette Samsung : reprise/upgrade sans perte des ~3 770 pistes préexistantes ; deux tableaux de compteurs ; affichage et ouverture manuelle des résultats.
- [ ] Évaluer latence UI sur plusieurs milliers de fiches JSON ; surveiller CPU/température.
- [ ] Vérifier détection et vérité des preuves à grande taille avec cas contrôlés ; aucune grille prometteuse n'est garantie a priori.
- [ ] Si résultats H2 sans X-Wing rares, ouvrir plus tard une source de grilles uniques hors banque X-Wing afin d'éviter un biais d'échantillonnage.
- [ ] Pas de publication directe de l'APK / merge / Play sans ordre explicite supplémentaire.
