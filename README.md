# GeckoDoku 🦎

Jeu de logique Android accessible, mignon et local.

## v0.2.0-dev
- Tailles réglables : 5×5, 6×6, 7×7, 8×8.
- Difficultés : Découverte, Facile, Réflexion, Expert.
- Clic simple : poser/enlever une croix.
- Vrai double-clic : poser/enlever un gecko.
- Appui long : gecko hypothèse discret puis repère fort clignotant.
- Gecko confirmé : exclusions automatiques sur ligne, colonne, zone et voisinage.
- Geckos donnés au départ selon difficulté, verrouillés et entourés.
- Générateur local de régions + validation d'une solution unique.
- Solveur humain : singles ligne/colonne/zone, interactions ligne-zone, X-Wing.
- Index 0–100 basé sur le chemin logique, pas seulement la taille.
- Stats du joueur stockées uniquement sur le téléphone.
- FX audio légers sans asset et désactivables.
- Architecture prête pour futurs sons, voix, images, animations, FX et vidéos.

## Philosophie difficulté
Une grille proposée dans un mode normal doit être résoluble par les techniques autorisées pour ce niveau. Le solveur exhaustif garantit l'unicité ; le solveur humain garantit un chemin logique sans guessing selon le niveau.

## Build
Android API 36, JDK 17, Android Gradle Plugin 9.4.0, Gradle 9.6.0.
GitHub Actions produit GeckoDoku-v0.2.0-dev.apk.
