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

### Preuve
Énumération de toutes les permutations 5×5 respectant ligne/colonne et non-contact, puis filtrage par régions : l'ancienne carte produisait plusieurs solutions.

### Correction
Nouvelle carte de régions conservant la solution (0,2,4,1,3). L'énumération exhaustive retourne exactement cette unique solution.

### Règle de non-régression
Toute grille générée ou intégrée devra passer un UniqueSolutionValidator avant d'être jouable.

### Toujours non vérifié
Compilation AGP 9.4.0 / Gradle 9.6.0, gestes téléphone, volume/confort FX et vitesse du clignotement.

### Difficulté
Le score actuel est heuristique, pas une IA entraînée.
