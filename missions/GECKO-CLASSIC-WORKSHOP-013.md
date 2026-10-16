# Mission 013 — Atelier de grilles difficiles GeckoDoku

Ordre Fab du 16 octobre 2026 : créer un moteur de génération en arrière-plan, X-Wing minimum et deux branches dépendantes ; collecter et exporter pour audit, tester puis promouvoir manuellement au niveau Infernal.

Branche : feature/classic-infernal-workshop-v1 (base feature/infernal-proof-search-v2). Ne pas merger main ni publier de Release ou AAB.

## Implémentation
- `PuzzleGenerator.generateWorkshopCandidate` : solutions légales, suppression de givens contrôlée par `hasExactlyOneSolution` (compteur complet indépendant), `HypothesisSolver` à profondeur 3, trace X-Wing observée, profondeur imbriquée ≥2, réanalyse sans X-Wing et profondeur1.
- `HumanSolver` : `SolverRules.NO_X_WING` ; `HypothesisSolver` : transmettre les règles sur tous les nœuds.
- `InfernalWorkshopWorker` : WorkManager pour recherches courtes autonomes, periodic 15 min lorsque branché et batterie non faible, demande immédiate batteryNotLow, annulation coopérative et compteur d'essais. Android peut décaler/arrêter les exécutions.
- `InfernalWorkshopStore` : record JSON atomique par empreinte SHA-256, trace complète et solution dans fichier privé ; statuts CANDIDATE/TESTING/VALIDATED/REJECTED ; dédoublonnage ; ZIP manifeste + grids/id.json, limites de lecture, export sans effacement, import qui ne restaure jamais une validation distante.
- `MainActivity` : réglages Classic > Atelier ; démarre/pause/relance, montre 60 récents, teste, permet notes, exige un test avant validation, export/import ZIP via sélecteur Android. Les preview évitent stats/Hall public. En Infernal, priorité aux grilles validées du même format (H2/H3 affichés explicitement), sinon génération existante sans downgrade silencieux.
- `SettingsMenuPolicy` Classic uniquement, WorkManager ajouté aux dépendances, AndroidX activé.

## Contrôle et limites
L'objectif est une bibliothèque de candidats pour revue humaine. `X-Wing` observé dans la trace et échec dans modèle sans X-Wing borné ne prouvent pas que toute stratégie logiquement possible nécessite X-Wing. La version d'Android et le chargeur peuvent différer l'exécution. Les archives incluent les solutions intégrales (privées). Ne pas confondre validation JVM/Android avec génération réelle de grille certifiée. Mesurer le taux de réussite sur plusieurs graines et tailles avant déclaration fonctionnelle complète.

## Recette attendue
Lancer générateur branché et débatterie OK ; laisser une exécution ; vérifier nombre d'essais, création candidate ; lancer test sans stats/Hall ; noter et valider ; lancer Infernal pour prise en compte des validées ; exporter ZIP, importer et vérifier déduplication/remise à l'état candidat. Aucun envoi Play.

### Amélioration de rendement et traçabilité
La recherche de profondeur 2 est tentée avant profondeur 3. Une recherche sans X-Wing non concluante faute de budget ne supprime plus le candidat : les flags `xWingExclusionModelComplete` et `shallowExclusionModelComplete` indiquent l'incomplétude dans JSON/export/UI. Le modèle sans X-Wing qui résout la grille est éliminatoire. L'atelier ne présente jamais une preuve incomplète comme preuve mathématique d'indispensabilité.

## Preuves GitHub CI
- Run #545 `38894030798` sur code fcc114e4 : GREEN, tests et assemblePhone, Pierre et sprites 240p vérifiés.
- Artifact Actions `12043056881` ; APK téléphone `GeckoDoku-v0.15.45-dev.apk` SHA256 `1e49ba40e09e316d6857a12df33bda533cb5fa9c2c3aa0e4fd9892f9f0162e78` (dans ZIP artifact), pas de Release.
- CI ne prouve pas le taux réel de grilles générées ; tester sur Samsung et exporter le premier lot avant de déclarer fonctionnel.
