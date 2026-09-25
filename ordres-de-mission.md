# Ordres de mission — GeckoDoku

## GECKO-001 — Base du jeu accessible
**État : Livré, à valider sur téléphone.**

Créer GeckoDoku, jeu Android de logique avec geckos, agréable pour une personne malvoyante :
- grosses cases et contours lisibles ;
- un gecko par ligne, colonne et zone ;
- les geckos ne se touchent pas, diagonales comprises ;
- un clic pose une croix de repère utilisateur ;
- une nouvelle découverte de gecko provoque le remplissage automatique des impossibilités autour, sur sa ligne, sa colonne et sa zone.
La grille de démonstration doit elle-même être logiquement déterminée : la v0.1 utilise désormais une grille 5×5 à solution unique vérifiée par énumération exhaustive.

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

Prévoir dès l'architecture la coexistence de deux systèmes : rendu léger/procédural et rendu futur très riche utilisant des assets importés. Cette couche pourra recevoir belles images, animations, vidéos, voix, sons et FX sans réécrire le moteur logique. Début visé autour du 10 octobre 2026 ; cible autour du 30 octobre 2026.

## GECKO-005 — Difficulté objective et IA interne
**État : Préparé, non entraîné.**

Créer un index de difficulté objectif des grilles générées. À terme, entraîner un modèle interne léger sur les caractéristiques du chemin de résolution et les observations de jeu. La taille brute de la grille ne doit pas suffire à déterminer la difficulté.
