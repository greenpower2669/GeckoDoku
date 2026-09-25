# Ordres de mission — GeckoDoku

## GECKO-001 — Base du jeu accessible
**État : Livré, à valider sur téléphone.**

Créer GeckoDoku, jeu Android de logique avec geckos, agréable pour une personne malvoyante :
- grosses cases et contours lisibles ;
- un gecko par ligne, colonne et zone ;
- les geckos ne se touchent pas, diagonales comprises ;
- un clic pose une croix de repère utilisateur ;
- une nouvelle découverte de gecko provoque le remplissage automatique des impossibilités autour, sur sa ligne, sa colonne et sa zone.

## GECKO-002 — Repères avancés
**État : Livré, à valider.**

- Appui long sur une case : deux niveaux de repère gecko, un presque invisible et un très visible/clignotant.
- Appui long hors grille : palette de repères personnels.
- Prévoir 10 repères, avec notamment point d'exclamation et gecko arc-en-ciel.
- Après sélection, la palette disparaît et le joueur place le repère sur la case voulue.

## GECKO-003 — Retours sonores utiles
**État : Livré en version légère, à enrichir.**

Mettre des FX sonores dès la première version car le retour audio aide à compenser la difficulté visuelle. Ils doivent rester désactivables.

## GECKO-004 — Habillage graphique/média futur
**État : Ordre futur conservé.**

Prévoir dès l'architecture la coexistence de deux systèmes :
1. rendu léger/procédural ;
2. rendu futur très riche utilisant des assets importés.

La couche riche pourra recevoir : belles images, animations, vidéos, voix, sons, FX et autres assets, sans réécrire le moteur logique. Début de cette phase prévu autour du 10 octobre 2026 ; cible autour du 30 octobre 2026.

## GECKO-005 — Difficulté objective et IA interne
**État : Préparé, non entraîné.**

Créer un index de difficulté objectif des grilles générées. À terme, entraîner un modèle interne léger sur les caractéristiques du chemin de résolution et les observations de jeu. La taille brute de la grille ne doit pas suffire à déterminer la difficulté.
