# GeckoDoku 🦎

Jeu de logique Android accessible, mignon, local et sans publicité.

## v0.3.0-dev
- Tailles réglables : 5×5 à 12×12.
- 12 teintes pastel de zones pour conserver des régions distinctes sur les grandes grilles.
- Difficultés mesurées par les techniques réellement nécessaires :
  - Découverte : singles suffisants ;
  - Facile : au moins 1 déduction de zone nécessaire ;
  - Réflexion : 2 à 3 déductions de zone ;
  - Difficile : 4 déductions de zone ou davantage ;
  - Expert : impossible sans Gecko X-Wing ;
  - Démentiel : nécessite la combinaison Gecko X-Wing + projection de zone.
- Projection de zone : si les 2 à 4 positions restantes d'une zone rendent une case voisine impossible dans tous les cas, cette case est éliminée.
- Gecko X-Wing : paires de lignes/colonnes et paires de zones réservant deux axes.
- Génération locale, unicité vérifiée avec les geckos donnés et solveur humain explicable.
- Si le générateur ne trouve pas exactement le niveau demandé dans son budget de recherche, l'interface affiche honnêtement le niveau mesuré.
- Clic simple = croix ; vrai double-clic = gecko ; appui long = hypothèse.
- Stats locales uniquement sur le téléphone.
- FX audio légers et désactivables.

## Principe de difficulté
La taille et la difficulté sont indépendantes. La difficulté officielle n'est pas une note arbitraire : GeckoDoku retire virtuellement certaines familles de déductions et vérifie si la grille devient insoluble. Un niveau Expert doit donc réellement dépendre de Gecko X-Wing.

## Build
Android API 36, JDK 17, Android Gradle Plugin 9.4.0, Gradle 9.6.0.
GitHub Actions produit GeckoDoku-v0.3.0-dev.apk.
