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


## GECKO-013 — Rejouer / Favoris / Journal local
**État : Implémenté v0.5.0-dev, à tester.**

- ↺ Rejouer réinitialise exactement la grille courante : mêmes zones, mêmes givens, même difficulté, mais aucun coup joueur.
- ⭐ Sauver ajoute la définition immuable de la grille au journal local.
- 📚 Journal liste les grilles sauvegardées avec taille, difficulté et date.
- Une grille du journal peut être rejouée proprement.
- Suppression individuelle disponible.
- Vider tout le journal disponible avec confirmation.
- Une resauvegarde du même id ne crée pas de doublon.
- La trace Professeur Gecko est reconstruite au chargement.
- Le journal permet notamment de faire tester les mêmes grilles à d'autres joueurs sur le même appareil.
- Export/import inter-appareils reste une évolution future distincte.

## GECKO-014 — Taux de réussite par difficulté
**État : Implémenté v0.5.0-dev, à tester.**

Pour chaque difficulté mesurée, conserver parties lancées et parties terminées, puis afficher le pourcentage de réussite. Nouvelle grille, Rejouer et Chargement journal sont chacun une tentative distincte.


## GECKO-015 — Célébration de victoire stylisée
**État : Implémenté v0.6.0-dev, à tester.**

À chaque grille terminée, afficher une célébration procédurale : confettis, salves colorées et message de félicitations. L'intensité augmente avec GameDifficulty. Aucun stroboscope ni flash plein écran agressif. Animation courte et interrompable par toucher.

Découverte/Facile : célébration douce.
Réflexion/Difficile : davantage de particules.
Expert/Démentiel : plusieurs salves de feu d'artifice.
Mission Impossible : « MISSION ACCOMPLIE ! ».
Infernal : « INFERNAL VAINCU ! », célébration maximale.

## GECKO-016 — Bulle BD du Professeur Gecko
**État : Implémenté v0.6.0-dev, à tester.**

Ajouter une bulle de bande dessinée dédiée, avec en-tête Prof Gecko et gros texte lisible. La bulle complète les surlignages et ne les remplace pas. Elle reprend les trois niveaux pédagogiques : où regarder, pourquoi, action.

## GECKO-017 — Mission Impossible / Infernal
**État : Implémenté v0.6.0-dev, à valider humainement.**

Après Démentiel :
- Mission Impossible : toutes les techniques déterministes, y compris Gecko X-Wing, bloquent ; une unique hypothèse par contradiction permet de poursuivre et terminer.
- Infernal : Mission Impossible ne suffit pas ; deux hypothèses sont nécessaires ou une preuve demande une profondeur de branchement 2.

Le moteur ne lit pas solutionCols pour choisir l'hypothèse. Il cherche un axe ou une zone réduit à exactement deux candidats, suppose chaque possibilité et déroule les conséquences. Une branche n'est rejetée que si une contradiction structurelle est démontrée. Prof Gecko doit pouvoir expliquer le candidat rejeté et le candidat forcé.

Au-delà de deux hypothèses/profondeur 2, la grille est considérée hors contrat actuel et ne doit pas être proposée comme résoluble par ces niveaux.


## GECKO-018 — Professeur Gecko actif pas à pas
**État : Implémenté v0.7.0-dev, à tester.**

Le comportement GECKO-010 « ne joue jamais » est remplacé par un mode professeur actif mais contrôlé par le joueur :
- chaque pression sur Prof Gecko applique une seule prochaine déduction sûre ;
- une exclusion logique devient une ou plusieurs croix réelles ;
- un gecko forcé devient un gecko réel et déclenche ses exclusions automatiques ;
- le Prof s'arrête après chaque étape et attend une nouvelle pression ;
- le joueur peut reprendre la main à tout moment.

Pour une étape d'hypothèse :
1. première pression : afficher exactement les deux candidats sous forme de geckos semi-transparents ;
2. aucune mutation logique à ce stade ;
3. pression suivante : expliquer la contradiction, barrer le candidat rejeté et confirmer le survivant ;
4. les fantômes disparaissent.

Les geckos fantômes du Prof ne doivent pas modifier les hypothèses personnelles de l'utilisateur.

Un garde-fou compare uniquement la conclusion finale d'un gecko forcé à la solution unique avant mutation. Cette vérification ne choisit pas le coup ; elle empêche seulement un bug du solveur de corrompre la partie.

## GECKO-019 — Statistiques avec aide du Prof
**État : Implémenté v0.7.0-dev, à tester.**

Toute utilisation du Prof pendant une tentative marque la partie comme assistée. Si la grille est terminée, incrémenter « terminée avec Prof » globalement et pour la difficulté correspondante. Cela ne compte ni comme erreur ni comme échec.


## GECKO-020 — Bulle Prof flottante
**État : Implémenté v0.8.0-dev, à tester.**

Retour téléphone v0.7 : la bulle Prof occupait une hauteur réelle dans le LinearLayout et réduisait fortement la grille, particulièrement en 12×12.

Correction :
- ProfessorBubbleView devient un overlay dans un FrameLayout racine ;
- positionnement juste au-dessus du bouton Prof Gecko ;
- aucune hauteur n'est retirée à la grille ;
- pendant l'affichage, masquer temporairement les trois rangées de commandes secondaires ;
- conserver le bouton Prof visible ;
- ajouter une cible × de fermeture directement dessinée dans la bulle ;
- fermer la bulle restaure les commandes sans modifier la grille, les overlays logiques ou une hypothèse Prof en attente.

## GECKO-021 — FX de célébration synchronisés
**État : Implémenté v0.8.0-dev, à tester.**

Chaque salve de VictoryCelebrationView émet un événement sonore au même instant. ToneFxFeedback produit un petit lancement puis une explosion stylisée ; les niveaux élevés reçoivent davantage de salves et une accentuation finale.

Le bouton FX contrôle aussi ces sons. Toucher la célébration pour la fermer ou désactiver FX annule les sons différés restants.
