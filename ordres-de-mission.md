# Ordres de mission — GeckoDoku

## GECKO-001 — Base accessible
**État : En validation téléphone.**
Un gecko par ligne, colonne et zone ; aucun contact y compris diagonales ; exclusions automatiques, grosses cases et contours lisibles.

## GECKO-002 — Repères avancés
**État : Implémenté.**
Appui long : hypothèse discrète → forte → aucune. Palette de repères personnels indépendante de la logique.

## GECKO-003 — Retours sonores
**État : Implémenté.**
FX légers et désactivables pour confirmer les actions.

## GECKO-004 — Habillage futur
**État : Conservé.**
Préserver rendu léger et futurs assets riches. Début visé autour du 10 octobre 2026 ; cible autour du 30 octobre 2026.

## GECKO-005 — Difficulté logique objective
**État : v0.3 implémentée, validation humaine continue.**
Découverte : singles. Facile : au moins 1 déduction de zone. Réflexion : 2–3. Difficile : 4+. Expert : X-Wing indispensable. Démentiel : X-Wing + projection indispensables.

## GECKO-006 — APK
**État : v0.4 à construire.**
APK correctement nommé via GitHub Actions.

## GECKO-007 — Gestes
**État : Corrigé.**
Simple = croix. Vrai double-clic = gecko. Long = hypothèse.

## GECKO-008 — Tailles/couleurs/stats
**État : Implémenté.**
5×5 à 12×12, 12 couleurs, stats locales, difficulté mesurée.

## GECKO-009 — Déductions spécifiques
**État : Implémenté.**
Projection 2–4 candidats et Gecko X-Wing lignes/colonnes/zones.

## GECKO-010 — Professeur Gecko pédagogique
**État : Implémenté v0.4.0-dev, à tester.**

Chaque grille stocke la trace complète du solveur au moment de sa validation. Chaque SolveStep contient sa preuve et son état précédent.

Le Professeur :
1. cherche d'abord une étape compatible dans cette trace pré-calculée ;
2. si le joueur a suivi un autre chemin, recalcule uniquement la prochaine étape sûre ;
3. révèle progressivement :
   - niveau 1 : où regarder ;
   - niveau 2 : pourquoi ;
   - niveau 3 : action précise ;
4. surligne les sources puis les cibles ;
5. ne joue jamais à la place du joueur ;
6. refuse d'inventer une déduction si l'état courant n'en fournit pas.

Objectif : pédagogie pour le joueur ET outil de debug du solveur.


## GECKO-011 — Protection contre les barres système Android
**État : Corrigé v0.4.1-dev, à tester.**

Retour téléphone : le bouton Prof Gecko était dessiné derrière la barre de navigation Android et devenait pratiquement impossible à activer. La racine applique maintenant les system bars insets sur les quatre côtés. Le titre est également protégé de la barre d'état.

## GECKO-012 — Difficulté Facile sous-estimée
**État : Bug confirmé par test humain, correction logique à faire après GECKO-011.**

Cas observé : grille 12×12 affichée Facile, trace 7, mais résolution humaine nécessitant deux Gecko X-Wing pour terminer. Une grille nécessitant réellement X-Wing ne doit jamais être classée Facile. Conserver ce cas comme non-régression pour l'audit du DifficultyIndexer et de la trace.
