# Ordres de mission — GeckoDoku

## GECKO-001 — Base du jeu accessible
**État : En validation téléphone.**
Un gecko par ligne, colonne et zone ; aucun contact, diagonales comprises ; grosses cases, contours de zones lisibles et exclusions automatiques après confirmation.

## GECKO-002 — Repères avancés
**État : Implémenté, à revalider.**
Appui long case : gecko hypothèse discret → alerte clignotante → aucun. Appui long hors grille : palette de 10 repères personnels, sans effet sur la logique.

## GECKO-003 — Retours sonores
**État : Implémenté en FX légers.**
Retours audio désactivables pour confirmer les actions sans dépendre uniquement de la vue.

## GECKO-004 — Habillage graphique/média futur
**État : Ordre futur conservé.**
Préserver rendu léger et futurs assets riches : images, animations, vidéos, voix, sons et FX sans modifier le moteur. Début visé autour du 10 octobre 2026 ; cible autour du 30 octobre 2026.

## GECKO-005 — Difficulté logique objective
**État : v0.3 implémentée, à valider humainement.**

La difficulté doit être déterminée par la technique minimale indispensable :
- Découverte : singles suffisants ;
- Facile : au moins 1 déduction de zone indispensable ;
- Réflexion : 2 à 3 déductions de zone ;
- Difficile : 4 ou davantage ;
- Expert : la grille ne se résout pas sans Gecko X-Wing ;
- Démentiel : la grille exige à la fois Gecko X-Wing et projection de zone.

Les techniques peuvent être combinées. Le futur modèle IA local pourra prédire rapidement le niveau, mais le solveur logique reste le juge explicable.

## GECKO-006 — APK
**État : v0.3 à construire après commit.**
APK correctement nommé, testable sur téléphone, sans médias lourds intégrés.

## GECKO-007 — Gestes
**État : Corrigé depuis v0.2.**
Clic simple confirmé = croix uniquement. Vrai double-clic = gecko uniquement. Appui long = hypothèse.

## GECKO-008 — Tailles, couleurs et stats
**État : Étendu v0.3.**
- tailles 5×5 à 12×12, soit 8 tailles au lieu des 4 initiales ;
- palette de 12 couleurs de zones ;
- statistiques locales conservées ;
- difficulté stockée selon le niveau réellement mesuré.

## GECKO-009 — Déductions Gecko spécifiques
**État : Implémenté v0.3, à valider.**
Projection de zone : lorsqu'une zone sans gecko n'a plus que 2 à 4 cases candidates, toute case extérieure qui toucherait chacune de ces possibilités est impossible.
Gecko X-Wing : deux lignes avec les mêmes deux colonnes candidates, deux colonnes avec les mêmes deux lignes, ou deux zones enfermées dans exactement deux lignes/colonnes réservent ces axes et éliminent les autres candidats.
