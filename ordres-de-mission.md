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


## GECKO-022 — HABILLAGE RICHE SUPERPOSÉ / CONTRAT MÉDIA
**État : ordre de mission figé le 26/09/2026. Organisation des assets à appliquer immédiatement. Intégration runtime à réaliser ensuite sans modifier la logique du jeu.**

### 0. Règle absolue
L'habillage riche est une couche purement visuelle et sonore AU-DESSUS du jeu existant.

Il est interdit à cette couche :
- de remplacer GameEngine ;
- de modifier Puzzle, HumanSolver, HypothesisSolver, DifficultyIndexer ou les règles ;
- de changer une case, une croix, un gecko, une hypothèse ou une statistique par elle-même ;
- de rendre le jeu inutilisable si un média est absent, illisible ou désactivé ;
- de devenir obligatoire pour comprendre une déduction.

Le rendu procédural actuel reste la vérité visuelle de secours et doit toujours fonctionner seul.

### 1. Interrupteur général
Ajouter un interrupteur utilisateur explicite « Animations riches » / « Habillage animé » :
- état mémorisé localement ;
- ON : les médias peuvent être joués ;
- OFF : aucun média riche n'est lancé ;
- OFF ne change aucune logique, aucun score, aucune difficulté ni aucune sauvegarde ;
- la bascule doit être possible sans redémarrer l'application ;
- si OFF est activé pendant une vidéo, arrêter proprement la vidéo et revenir immédiatement au jeu normal.

Les FX sonores du jeu restent un réglage séparé.
Le son embarqué dans les vidéos riches doit respecter le réglage FX :
- Animations ON + FX ON : vidéo + son embarqué ;
- Animations ON + FX OFF : vidéo muette ;
- Animations OFF : aucune vidéo, donc aucun son vidéo.

### 2. Principe de superposition
Tous les médias riches sont affichés dans une couche overlay dédiée au-dessus de screenRoot / du rendu normal.

Ordre recommandé des couches, du fond vers l'avant :
1. écran de jeu normal ;
2. surlignages logiques / Prof ;
3. habillage média riche ;
4. célébration de victoire si active ;
5. contrôles de sécurité indispensables : fermeture × / skip.

Une animation ne doit pas provoquer de reflow du layout :
- la grille ne rétrécit pas ;
- les boutons ne changent pas de taille ;
- le média n'est jamais ajouté comme enfant mesuré dans le LinearLayout de la grille.

### 3. Politique actuelle des vidéos longues — IMPORTANT
Pour cette phase, NE PAS découper les vidéos de 30 secondes.
NE PAS utiliser de timecodes internes.
NE PAS sélectionner une expression particulière dans la vidéo.

Les deux vidéos longues sont toujours lues :
- depuis t = 0 ;
- jusqu'à leur fin naturelle (~30,07 s) ;
- une seule fois ;
- sans boucle.

Une fermeture volontaire × peut interrompre la lecture si l'utilisateur le souhaite, mais le programme ne doit jamais tronquer automatiquement le clip.

Cette règle reste en vigueur tant qu'un futur ordre de mission « découpage clips / catalogue d'actions » n'a pas été validé.

### 4. Catalogue immuable des assets et rôle UNIQUE
Les fichiers doivent être rangés sous le dossier racine `assets/` avec deux sous-dossiers.

#### assets/gecko/
**Gecko_Intro.mp4**
- rôle UNIQUE : ouverture / intro de l'application ;
- jamais utilisé comme apparition de case ;
- jamais utilisé comme animation ambiante ;
- lecture complète ;
- audio embarqué autorisé si FX ON ;
- afficher par-dessus un texte stylisé « GeckoDoku » ;
- texte lisible, contrasté, indépendant du contenu de la vidéo ;
- si Animations riches OFF : démarrage direct du jeu, sans intro vidéo.

**Gecko_apparition.mp4**
- rôle UNIQUE : apparition visuelle d'un gecko confirmé ;
- déclenché seulement après qu'un gecko est réellement confirmé par GameEngine ;
- ne décide jamais si le gecko est correct ;
- lecture complète du fichier ;
- overlay au premier plan ;
- à la fin, le gecko procédural/normal déjà confirmé reste visible.

**Gecko_disparition.mp4**
- rôle UNIQUE : disparition visuelle d'un gecko retiré ;
- déclenché seulement après le retrait effectif dans GameEngine ;
- lecture complète ;
- à la fin, la case reste dans son état logique normal.

**Gecko_actions_plusieurs.mp4**
- rôle UNIQUE pour cette phase : grande séquence d'actions mignonnes occasionnelle ;
- durée ~30,07 s ;
- toujours lue en totalité ;
- aucune extraction d'actions individuelles maintenant ;
- aucun timecode ;
- aucun découpage ;
- déclenchement occasionnel et aléatoire avec cooldown ;
- ne doit jamais se déclencher pendant une célébration, une autre vidéo, l'intro ou une étape critique du Prof ;
- ne doit pas changer l'état du jeu.

#### assets/prof/
**Prof.png**
- rôle UNIQUE : portrait normal du Prof ;
- image de référence utilisable dans la bulle / interface ;
- aucun keycolor nécessaire si la transparence est exploitable.

**Prof_fb.png**
- rôle UNIQUE : source Prof sur fond bleu destinée au futur keycolor / tests ;
- ne pas substituer automatiquement à Prof.png ;
- ne pas traiter le bleu maintenant sauf ordre séparé.

**Prof_actions.mp4**
- rôle UNIQUE pour cette phase : animation longue occasionnelle du Prof ;
- durée ~30,07 s ;
- toujours lue en totalité ;
- aucun découpage interne maintenant ;
- ne doit être déclenchée que dans un contexte Prof ;
- ne doit jamais retarder ou modifier une déduction ;
- la logique du Prof doit pouvoir continuer même si cette vidéo est désactivée ou absente.

### 5. Intro GeckoDoku
Au lancement d'une session d'application :
- si Habillage animé OFF : afficher immédiatement l'écran normal ;
- si ON : jouer Gecko_Intro.mp4 en overlay ;
- conserver le son original seulement si FX ON ;
- afficher un titre stylisé « GeckoDoku » au-dessus de la vidéo ;
- le titre doit être dessiné par l'application, pas brûlé dans le média ;
- prévoir un bouton « Passer » ou × accessible ;
- la fin ou le skip révèle exactement l'écran normal déjà prêt sous l'overlay ;
- ne pas générer une nouvelle grille à la fin de l'intro : la grille doit déjà exister.

### 6. Apparition / disparition dans les cases
Objectif final : l'animation se place visuellement sur la case concernée, au-dessus du rendu normal.

Séquence apparition :
1. GameEngine confirme le gecko ;
2. état logique immédiatement valide ;
3. rendu normal disponible dessous ;
4. Gecko_apparition.mp4 est joué en overlay sur la zone de la case ;
5. fin vidéo ;
6. overlay retiré ;
7. le gecko normal demeure.

Séquence disparition :
1. GameEngine retire le gecko ;
2. état logique immédiatement valide ;
3. Gecko_disparition.mp4 est joué au-dessus de l'ancienne case ;
4. fin ;
5. overlay retiré ;
6. la case normale demeure sans gecko.

Si la vidéo ne peut pas être lue :
- aucune erreur logique ;
- aucun rollback ;
- afficher simplement le rendu procédural normal.

### 7. Actions longues occasionnelles — phase actuelle
Tant que les vidéos ne sont pas découpées :
- Gecko_actions_plusieurs.mp4 et Prof_actions.mp4 sont des séquences longues événementielles ;
- une seule vidéo riche à la fois ;
- jamais de boucle ;
- jamais deux vidéos superposées ;
- lecture complète 0→fin ;
- priorité faible : elles cèdent la place à la victoire, à l'intro et aux besoins pédagogiques.

Déclenchement Gecko_actions_plusieurs :
- uniquement pendant une partie active ;
- après une action logique valide ou après une période calme ;
- probabilité faible ;
- cooldown minimal recommandé : 180 secondes ;
- pas plus d'une séquence longue pendant ce cooldown ;
- ne pas déclencher si une bulle Prof est ouverte, si une hypothèse Prof attend sa seconde étape ou si la grille vient d'être terminée.

Déclenchement Prof_actions :
- uniquement après interaction avec Prof Gecko ;
- probabilité faible ;
- cooldown recommandé partagé ou séparé d'au moins 180 secondes ;
- ne jamais empêcher l'affichage immédiat du texte logique ;
- si la vidéo doit être montrée, elle vient en habillage complémentaire, jamais comme prérequis avant la réponse du Prof.

Les valeurs de probabilité restent des constantes réglables. Valeur initiale conseillée pour tests : 10 à 15 % après un événement éligible, avec cooldown 180 s.

### 8. Concurrence / priorités
Priorité haute vers basse :
1. cohérence GameEngine ;
2. célébration de victoire ;
3. Professeur / explication logique ;
4. intro au démarrage ;
5. apparition / disparition courte liée à une case ;
6. actions longues mignonnes aléatoires.

Si une priorité supérieure apparaît :
- ne jamais empiler une seconde vidéo ;
- arrêter ou différer l'animation basse priorité selon son type ;
- conserver l'état du jeu.

Les actions longues ne doivent jamais démarrer :
- pendant VictoryCelebrationView ;
- pendant une autre vidéo ;
- pendant un dialogue système ;
- pendant le chargement d'une nouvelle grille ;
- pendant la sauvegarde/relecture du journal ;
- pendant une étape d'hypothèse Prof en attente.

### 9. Son
Les fichiers vidéo conservent leur bande son d'origine.
Le lecteur média reçoit le volume selon FX :
- FX ON → volume média normal ;
- FX OFF → volume 0, sans empêcher la vidéo.

Le son des vidéos n'est pas mélangé artificiellement aux ToneFxFeedback.
Éviter de lancer un ToneGenerator simultané inutilement lors de l'apparition/disparition si la vidéo possède déjà son propre effet.

### 10. Keycolor bleu — PAS MAINTENANT
Les fonds bleus sont conservés comme sources de travail.
Aucun keycolor/chroma-key n'est implémenté dans cette phase.

Futur ordre séparé :
- seuil de bleu ;
- bord doux ;
- suppression des franges bleues ;
- tests GPU / performances ;
- fallback si shader indisponible.

Il est interdit de bricoler un détourage approximatif dans le moteur logique.

### 11. Robustesse
Le système média doit être optionnel :
- asset absent → log + fallback ;
- décodage vidéo impossible → fallback ;
- Activity pause → pause/stop propre ;
- Activity destroy → libération MediaPlayer/VideoView ;
- changement de grille → aucune ancienne vidéo ne doit survivre ;
- désactivation de l'habillage → arrêt immédiat et nettoyage ;
- aucune fuite de Surface/MediaPlayer.

### 12. Accessibilité
- la vidéo ne remplace jamais une information textuelle ;
- intro skippable ;
- × / Passer avec grande cible tactile ;
- pas de flash stroboscopique ajouté ;
- le texte « GeckoDoku » reste lisible ;
- TalkBack doit pouvoir annoncer « Passer l'animation » ;
- l'utilisateur doit pouvoir jouer avec Habillage animé OFF sans perdre aucune fonction.

### 13. Architecture cible
Créer plus tard une couche dédiée, par exemple :
- RichMediaSettings ;
- RichMediaOverlayView ;
- AssetMediaCatalog ;
- RichMediaScheduler.

MainActivity ne doit pas accumuler les timecodes ou noms de fichiers en dur.
AssetMediaCatalog est la seule source de vérité des chemins.

### 14. Critères d'acceptation
Le lot sera considéré valide lorsque :
- le jeu fonctionne identiquement avec Habillage animé OFF ;
- ON ajoute uniquement du visuel/son ;
- les médias sont en overlay sans réduire la grille ;
- intro complète fonctionne ;
- apparition/disparition complètes fonctionnent ;
- les deux vidéos longues sont jouées entières et rarement ;
- aucune longue vidéo n'est découpée ;
- une seule vidéo est active à la fois ;
- audio vidéo suit FX ;
- les médias manquants ne cassent jamais le jeu ;
- tous les assets sont rangés dans `assets/`.
