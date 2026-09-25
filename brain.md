# Brain — GeckoDoku

## Contrat produit
GeckoDoku est un jeu de logique Android accessible, mignon et utilisable hors ligne. L'accessibilité visuelle est prioritaire, avec retours audio utiles.

## Règles cœur
- Une grille est découpée en zones.
- Chaque ligne contient exactement un gecko.
- Chaque colonne contient exactement un gecko.
- Chaque zone contient exactement un gecko.
- Deux geckos ne peuvent pas se toucher, diagonales comprises.
- La grille 5×5 de démonstration possède une solution unique vérifiée par énumération exhaustive.
- La future génération doit également vérifier l'unicité.

## Gestes v0.1
- Tap case vide : croix manuelle.
- Second tap sur cette croix : tentative de gecko confirmé.
- Tap gecko confirmé : retrait.
- Appui long case : hypothèse discrète → alerte clignotante → aucun.
- Appui long hors grille : palette de 10 repères personnels.
- Les repères personnels n'altèrent jamais la solution.

## Auto-repères
Un gecko confirmé produit les exclusions automatiques de sa ligne, sa colonne, sa zone et ses cases voisines interdites.

## Audio
L'audio est séparé du moteur. La v0.1 utilise ToneGenerator : croix, blocage, gecko, erreur, hypothèse, repère et fin. Aucun fichier audio lourd. FX désactivables.

## Architecture média
Le moteur logique ne dépend pas des médias. Les futurs sons, voix, images, animations, vidéos et FX riches doivent pouvoir remplacer les implémentations légères sans modifier GameEngine.

## Difficulté
La v0.1 expose un index heuristique 0–100. Ce n'est pas encore une IA entraînée. DifficultyModel est le point de remplacement futur.

## Accessibilité
Traits de zones épais, grande grille, retours visuels + sonores, grandes commandes, clignotement seulement sur un repère explicitement choisi, pas de publicité.

## Validation
La logique d'unicité de la démo est vérifiée. Build APK et test physique restent non confirmés.
