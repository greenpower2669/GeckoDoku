# Ordres de mission — GeckoDoku

## GECKO-001 — Base du jeu accessible
**État : En validation téléphone v0.2.0-dev.**

Créer GeckoDoku avec grosses cases, contours lisibles, un gecko par ligne/colonne/zone et interdiction de contact. Les exclusions automatiques doivent se remplir après un gecko confirmé.

## GECKO-002 — Repères avancés
**État : Implémenté, à revalider.**

- Appui long case : gecko hypothèse discret → gecko alerte clignotant → aucun.
- Appui long hors grille : palette de 10 repères personnels.
- Inclure !, ?, cible et gecko arc-en-ciel.
- Les repères personnels ne modifient jamais la logique.

## GECKO-003 — Retours sonores utiles
**État : Implémenté en FX légers.**

FX dès la première version, désactivables, pour confirmer les actions sans dépendre uniquement de la vue.

## GECKO-004 — Habillage graphique/média futur
**État : Ordre futur conservé.**

Prévoir deux systèmes interchangeables : rendu léger/procédural et rendu assets riches. Futurs beaux sons, voix, images, animations, vidéos et FX sans réécrire le moteur. Début visé autour du 10 octobre 2026 ; cible autour du 30 octobre 2026.

## GECKO-005 — Difficulté objective et IA interne
**État : Première base logique implémentée ; IA entraînée future.**

- Séparer taille de grille et difficulté.
- Utiliser un solveur humain explicable pour mesurer le raisonnement requis.
- Monter jusqu'au X-Wing pour le mode Expert.
- Garder DifficultyModel interchangeable pour une future IA locale entraînée.
- Ne pas confondre unicité mathématique et résolubilité humaine.

## GECKO-006 — APK de test
**État : v0.1 construit avec succès ; v0.2 en build après commit.**

Produire des APK correctement nommés et les valider sur téléphone.

## GECKO-007 — Correction des gestes
**État : Implémenté v0.2.0-dev, à tester.**

Le comportement v0.1 était incorrect : deux clics simples successifs pouvaient transformer une croix en gecko. Désormais :
- clic simple confirmé = croix uniquement ;
- vrai double-clic GestureDetector = gecko uniquement ;
- appui long = hypothèse.

## GECKO-008 — Taille, difficulté et stats locales
**État : Implémenté v0.2.0-dev, à tester.**

- tailles 5×5 à 8×8 ;
- difficultés Découverte / Facile / Réflexion / Expert ;
- geckos donnés selon le niveau ;
- génération validée unique ;
- statistiques locales : lancées, terminées, réussite, erreurs, temps moyen, répartition taille/difficulté ;
- aucune donnée de stats envoyée sur Internet.
