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
