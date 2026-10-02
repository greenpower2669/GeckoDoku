# GeckoDoku — FAB Copilot debug historical

> Historique condensé uniquement.
> L'archive exhaustive antérieure reste dans `sauvegarde.md`.
> Les anciens numéros GECKO ont parfois été réutilisés : version, SHA et date priment.

## Socle multimode

Le projet a évolué d'un GeckoDoku Classic vers quatre modes :
Classic, Sudoku, Gomoku, Abeilles & Geckos.

Les invariants conservés :
- moteurs logiques séparés du rendu ;
- Prof pédagogique ;
- médias décoratifs non bloquants ;
- statistiques locales ;
- overlays sans reflow du plateau.

## Pierre / capture audio

Ancien problème :
Pierre n'était pas toujours capturable par l'enregistrement écran.

Correction :
- routage Android MEDIA ;
- contenu SPEECH ;
- capture autorisée ;
- fallback Android TTS conservé.

Prononciation :
`PierrePronunciationPolicy` permet des corrections vocales sans modifier l'affichage.
Exemple canonique : `église` → `eglize` pour la synthèse seulement.

## Axes Classic / Abeilles & Geckos

Évolution :
- axes joueurs devenus globaux et déplaçables ;
- jaune/vert/rouge ;
- suppression hors plateau ;
- Bee persiste la couleur ;
- géométrie Bee centralisée :
  Q +60° ↖↘, S -60° ↙↗, R 0° ←→.

## Pipeline sprites 240p

Problème :
génération progressive et banques multiples 60/120/240/... coûtaient du temps et de la mémoire.

Solution :
- banques préconstruites ;
- runtime SpriteRGBA ;
- cible unique 240p ;
- suppression des banques inférieures/obsolètes ;
- suppression des MP4 Gecko/Abeille devenus inutiles ;
- Plante, Prof/Pierre et intros conservés.

CI de la migration : branche GECKO-043, version 0.15.38-dev, run #395 vert.

## Hypothèses colorées

Ajout d'un modèle parent/enfant commun aux raisonnements d'hypothèse :
- couleurs jaune, vert, rouge, violet, bleu, orange ;
- aura sur branche active ;
- croix filles colorées ;
- suppression parent → descendants supprimés ;
- contradiction → sens interdit + rollback descendants ;
- changement de sous-branche → ancienne descendance retirée.

Le Sudoku a ensuite adopté la même logique.

## Pavé Sudoku 2×2

Version 0.15.40-dev :
- zones Choix / Candidats / Hypothèse / Prévisu ;
- prévalidation Oui/Non ;
- candidats distincts des hypothèses ;
- suppression des anciens libellés H1/H2.

Version 0.15.41-dev :
- pavé redimensionnable ;
- statistiques temporelles refaites.

Version 0.15.42-dev :
- pavé persistant ;
- simple + double clic ouvrent/reciblent ;
- appui long garde les repères personnels ;
- aide `?` interactive par zone ;
- popup non modal pour laisser les clics extérieurs atteindre la grille.
CI finale : #402 verte.

Version 0.15.43-dev / code 78 :
- minimum resize 280×320 → 140×160 dp ;
- bandeau et poignée de resize réduits ;
- release téléphone produite.
CI #403 verte.

## Statistiques temporelles

Ancien problème :
une partie était comptée dès son lancement, donc les annulations polluaient les stats.

Nouvelle règle :
- terminée → enregistrée ;
- annulée à 0 erreur → ignorée ;
- annulée avec erreur → enregistrée comme abandon.

Ajouts :
- erreurs par niveau ;
- événements temporels ;
- comparaison des deux dernières parties terminées ;
- tendances temps/étoiles ;
- graphe par niveau ;
- lien Stats ↔ Hall of Fame ;
- narration Prof limitée au niveau le plus difficile + précédent.

CI : version 0.15.41-dev, run #399 vert.

## Merge final main — 2 octobre 2026

Situation :
`main` possédait 8 commits absents de la branche GECKO-048.

Mesure de sécurité :
- pas de fast-forward forcé ;
- PR #2 ;
- réconciliation à deux parents ;
- code/médias récents conservés ;
- anciens médias supprimés non réintroduits ;
- documents iOS et petits fichiers utiles de main préservés.

Réconciliation :
- commit `a08b0b436b8523487bb3aae11c4405351df687ca`
- CI #404 verte.

Merge :
- commit code `17c6de0186c745c15fc042971eb09b4fe19a6299`
- CI main #405 verte.

Release :
- `phone-0.15.43-dev-run-403`.

## Règle de diagnostic actuelle

Avant de rouvrir un ancien bug :
1. lire le code de `main` ;
2. vérifier la version/SHA concernée ;
3. consulter ce fichier ;
4. n'ouvrir `sauvegarde.md` que si un détail historique précis manque.
