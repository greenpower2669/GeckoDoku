# GeckoDoku 🦎

Jeu de logique Android accessible, local et sans publicité.

## v0.4.0-dev — Professeur Gecko
- Chaque grille conserve la trace complète du solveur humain validateur.
- Chaque étape mémorise :
  - la technique utilisée ;
  - les cases sources ;
  - les zones sources ;
  - l'axe concerné ;
  - les cases forcées ou éliminées ;
  - l'état logique juste avant l'étape.
- Professeur Gecko utilise cette trace pré-calculée en priorité pour répondre instantanément.
- Si le joueur suit un autre chemin valide, le Prof recalcule seulement la prochaine déduction depuis l'état courant.
- Aide progressive en trois pressions :
  1. où regarder ;
  2. pourquoi la déduction est vraie ;
  3. quelle action faire.
- Surlignage ambre des preuves ; cible rouge seulement au niveau 3.
- Le Prof n'applique jamais l'action à la place du joueur.
- Si aucune déduction sûre n'est trouvée avec l'état courant, il le dit au lieu d'inventer une réponse.

## Logique v0.3 conservée
- tailles 5×5 à 12×12 ;
- 12 couleurs de zones ;
- projection de zone ;
- Gecko X-Wing lignes, colonnes et paires de zones ;
- difficulté mesurée par les techniques réellement indispensables ;
- clic simple = croix, vrai double-clic = gecko ;
- stats locales et FX audio.

## Build
Android API 36, JDK 17, Android Gradle Plugin 9.4.0, Gradle 9.6.0.
GitHub Actions produit GeckoDoku-v0.4.0-dev.apk.
