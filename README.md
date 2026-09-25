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


## v0.4.1-dev — Correctif barres système
- L'interface applique désormais les insets Android haut, bas, gauche et droite.
- Le bouton Prof Gecko reste au-dessus de la barre de navigation Android, y compris sur téléphones avec navigation par boutons ou gestes.
- Le titre n'est plus placé sous la barre d'état.
- Bug logique enregistré séparément : une grille affichée Facile a nécessité deux Gecko X-Wing lors du test humain. Le classement sera audité après ce correctif UI prioritaire.


## v0.5.0-dev — Rejouer, journal et statistiques par difficulté
- ↺ Rejouer remet exactement la grille courante à zéro.
- ⭐ Sauver conserve la définition de la grille dans un journal local.
- 📚 Journal permet de recharger une grille sauvegardée pour la refaire.
- Suppression d'une grille du journal.
- Vider tout le journal avec confirmation.
- La sauvegarde journal contient la grille originale, pas la progression : chaque joueur repart d'une grille propre.
- Une grille rechargée reconstruit sa trace Professeur Gecko.
- Le même identifiant n'est pas dupliqué ; le resauvegarder le remonte dans le journal.
- Les statistiques affichent désormais le taux de réussite séparé pour chaque difficulté : terminées / lancées.
- Nouvelle, Rejouer et Chargement journal comptent comme de nouvelles tentatives.
- Tout reste local sur le téléphone.
- GECKO-012 reste ouvert : le classement de difficulté doit encore être audité sur le cas 12×12 Facile ayant nécessité deux X-Wing.


## v0.6.0-dev — Prof BD, hypothèses et célébrations
- Deux niveaux ajoutés : Mission Impossible et Infernal.
- Mission Impossible : les techniques déterministes sont épuisées et une hypothèse par contradiction est nécessaire.
- Infernal : deux hypothèses sont nécessaires ou une contradiction demande une profondeur d'hypothèse 2.
- Les hypothèses ne sont pas du hasard : le moteur cherche une ligne, colonne ou zone avec exactement deux candidats, teste les branches et n'accepte une conclusion que si une branche conduit à une contradiction logique.
- Les étapes d'hypothèse sont enregistrées dans solverTrace et expliquées par Prof Gecko.
- Prof Gecko affiche désormais son texte dans une bulle BD lisible, en plus des surlignages de cases.
- La progression pédagogique reste : 1/3 où regarder, 2/3 pourquoi, 3/3 action.
- À la victoire, un overlay stylisé affiche confettis + feux d'artifice sans stroboscope.
- La célébration devient plus riche avec la difficulté.
- Mission Impossible affiche « MISSION ACCOMPLIE ! ».
- Infernal affiche « INFERNAL VAINCU ! » avec la célébration maximale.
- L'animation est courte, progressive et peut être interrompue en touchant l'écran.
- Les grilles Mission Impossible / Infernal sauvegardées reconstruisent leur trace d'hypothèses au chargement.
