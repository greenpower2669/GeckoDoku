# Debug historical — GeckoDoku

## 2026-09-25 — Initialisation
Le dépôt contenait uniquement un README. Aucun code ni mémoire FAB Copilot n'existait.

### Prévention
- Pas de médias binaires lourds dans l'initialisation.
- FX via ToneGenerator pour retour audio immédiat sans asset.
- Gecko dessiné procéduralement.
- Séparation moteur / rendu / audio / difficulté pour l'habillage futur.

## 2026-09-25 — Grille démo non unique détectée avant validation
### Symptôme
La première carte de régions 5×5 admettait plusieurs placements conformes aux règles alors que GameEngine utilisait une solution cachée unique pour accepter/refuser un gecko.

### Cause
La carte de régions initiale n'avait pas été vérifiée par solveur exhaustif.

### Correction
Nouvelle carte de régions conservant la solution (0,2,4,1,3). L'énumération exhaustive retourne exactement cette unique solution.

### Règle de non-régression
Toute grille générée ou intégrée devra passer un UniqueSolutionValidator avant d'être jouable.

## 2026-09-25 — Aucun GitHub Action visible pour l'APK
### Symptôme
Fab ne voyait aucune Action en cours après la demande d'APK.

### Cause confirmée
Le fichier .github/workflows/build.yml était absent : la tentative précédente de création du workflow avait été bloquée avant commit. Aucun build ne pouvait donc démarrer.

### Correction
Ajout d'un workflow minimal build APK déclenché sur push de main et manuellement. Il utilise JDK 17, Gradle 9.6.0, assembleDebug et upload-artifact.

### Toujours à vérifier
Résultat réel du workflow, compilation Android, installation téléphone, volume/confort FX et vitesse du clignotement.

### Difficulté
Le score actuel est heuristique, pas une IA entraînée.
