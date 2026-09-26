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


### AMENDEMENT GECKO-022-K — KEYCOLOR BLEU IMMÉDIAT
- **Demandeur / date :** Fab, 26/09/2026.
- **Commande :** le keycolor bleu n'est plus différé. Le bleu des médias sert dès maintenant à produire de la transparence.
- **Remplace :** la section GECKO-022 « Keycolor bleu — PAS MAINTENANT » pour le périmètre de ce lot.
- **Résultat demandé :** fond bleu supprimé au rendu, contours adoucis, réduction des franges bleues, traitement GPU compatible minSdk 26 ; si le shader ou le décodage échoue, fermer le média et conserver le jeu normal. Ne jamais toucher à la logique du puzzle.
- **Portée :** vidéos riches Gecko/Prof à fond bleu. `Prof.png` reste le portrait normal transparent ; `Prof_fb.png` reste une source bleue et ne remplace pas automatiquement le portrait normal.
- **Statut :** **Livré sur branche, à valider sur téléphone.** GitHub Actions run #15 (`36203684907`) : tests + APK + AAB réussis. Artefact `GeckoDoku-v0.9.0-dev-Android` id `10893390568`. Validation visuelle du détourage bleu et du placement reste humaine.


# GECKO-023 — VOIX D'ENCOURAGEMENT À CHAQUE NOUVEAU GECKO
**Demandeur / date :** Fab, 26/09/2026  
**Statut :** mission figée, **à implémenter ultérieurement**.  
**Source maître :** `assets/audio/encouragements/master/Voix_encouragements.mp3` — durée mesurée 14,441 s.

## 0. Intention
À chaque **nouveau gecko correctement trouvé par le joueur**, GeckoDoku joue une courte phrase française d'encouragement choisie de façon aléatoire. Cette voix est un retour positif purement sonore : elle ne modifie jamais la logique, le score, la difficulté, les statistiques ni l'état du puzzle.

Le fichier maître doit être conservé intact. L'implémentation devra produire des clips courts séparés afin d'éviter de faire des seek/timers fragiles dans un MP3 long pendant le gameplay.

## 1. Timecodes de découpe validés
Les fenêtres ci-dessous incluent une petite marge de sécurité autour de la parole afin de ne pas couper les consonnes ou les fins de mots.

| # | Fichier cible conseillé | Texte | IN | OUT |
|---|---|---|---:|---:|
| 01 | `encouragement_01_bravo.mp3` | Bravo. | 00:00.000 | 00:00.508 |
| 02 | `encouragement_02_super.mp3` | Super. | 00:01.023 | 00:01.518 |
| 03 | `encouragement_03_excellente.mp3` | Excellente. | 00:02.113 | 00:02.683 |
| 04 | `encouragement_04_genial.mp3` | Génial. | 00:03.108 | 00:03.717 |
| 05 | `encouragement_05_bien_joue.mp3` | Bien joué. | 00:04.246 | 00:04.859 |
| 06 | `encouragement_06_magnifique.mp3` | Magnifique. | 00:05.111 | 00:05.745 |
| 07 | `encouragement_07_continue.mp3` | Continue comme ça. | 00:06.327 | 00:07.169 |
| 08 | `encouragement_08_presque.mp3` | Tu y es presque. | 00:07.878 | 00:08.644 |
| 09 | `encouragement_09_tres_bien.mp3` | Très bien. | 00:09.019 | 00:09.468 |
| 10 | `encouragement_10_formidable.mp3` | Formidable. | 00:09.790 | 00:10.524 |
| 11 | `encouragement_11_quel_talent.mp3` | Quel talent. | 00:10.974 | 00:11.604 |
| 12 | `encouragement_12_impressionnant.mp3` | Impressionnant. | 00:11.991 | 00:12.741 |
| 13 | `encouragement_13_vraiment_bon.mp3` | Ça, c'était vraiment bon. | 00:13.152 | 00:14.441 |

**Important :** le clip 13 contient volontairement la pause entre « Ça » et « c'était vraiment bon ». Elle doit être conservée ; ne pas le redécouper en deux phrases.

## 2. Organisation des assets
- conserver `assets/audio/encouragements/master/Voix_encouragements.mp3` comme master ;
- créer les dérivés sous `assets/audio/encouragements/clips/` ;
- ne pas remplacer le master par les clips ;
- le catalogue des clips devient la seule source de vérité pour le runtime ;
- le découpage se fait une fois lors de la préparation des assets, pas en temps réel pendant la partie.

## 3. Déclenchement
Déclencher une voix uniquement quand le joueur vient de **confirmer correctement un nouveau gecko**.

Ne pas déclencher sur :
- une croix ;
- une hypothèse ;
- un gecko incorrect ;
- un gecko retiré ;
- un gecko posé automatiquement par le Prof ;
- le simple rechargement d'une grille ;
- une animation décorative.

Pour éviter de féliciter plusieurs fois la même découverte, mémoriser pendant la tentative les cellules déjà récompensées. Retirer puis remettre le même gecko ne doit pas relancer une nouvelle phrase. Cette mémoire temporaire est remise à zéro au lancement/rejeu d'une tentative.

## 4. Choix aléatoire
- choisir aléatoirement parmi les phrases éligibles ;
- **interdire deux fois de suite le même clip** ;
- les phrases génériques restent disponibles à tout moment ;
- `Tu y es presque` n'entre dans le tirage que lorsqu'il reste au maximum deux geckos à trouver et au moins un ;
- garder le mécanisme simple : pas de classement, pas de score caché et pas de système de récompense complexe.

## 5. Dernier gecko et victoire
Le dernier gecko est lui aussi une nouvelle découverte et doit recevoir une phrase.

Pour éviter un mélange sonore :
- la célébration visuelle peut démarrer immédiatement ;
- la courte phrase d'encouragement est jouée une fois ;
- tout futur son/morceau de victoire attend la fin de cette phrase avant de démarrer ;
- aucune superposition de deux voix ou bandes son fortes.

## 6. Réglages audio
- les voix suivent **FX ON/OFF** ;
- FX OFF = aucune voix d'encouragement ;
- `Habillage animé` ne contrôle pas ces voix : ce réglage reste réservé aux médias visuels/vidéos ;
- une désactivation FX en cours de phrase doit arrêter ou rendre muette proprement la voix ;
- aucune voix ne doit bloquer les gestes ni l'affichage.

## 7. Architecture cible
Créer une petite couche dédiée, par exemple :
- `EncouragementCatalog` : noms, texte, conditions ;
- `EncouragementSelector` : hasard + anti-répétition + règle « presque » ;
- `EncouragementPlayer` : lecture courte et libération propre ;
- MainActivity ne contient pas les timecodes en dur.

Le système reçoit uniquement un événement « gecko joueur nouvellement confirmé » après validation du GameEngine. Il ne doit jamais décider lui-même si un coup est correct.

## 8. Robustesse / accessibilité
- clip absent ou illisible → ignorer la voix, jeu inchangé ;
- aucune exception audio ne remonte au moteur ;
- pause/destroy → libération propre ;
- la phrase sonore ne remplace jamais le texte visuel d'encouragement ;
- aucune obligation d'écouter la voix pour comprendre ou résoudre la grille.

## 9. Tests exigés avant livraison
- test de sélection : pas de répétition immédiate ;
- test de contexte : « Tu y es presque » absent loin de la fin ;
- test de contexte : « Tu y es presque » autorisé à 1–2 geckos restants ;
- test : croix / erreur / retrait / Prof ne déclenchent rien ;
- test : même cellule retirée puis remise ne redéclenche pas ;
- test : chaque nouveau gecko joueur déclenche exactement une voix ;
- test : FX OFF = silence ;
- test : clip manquant = aucun impact sur le jeu ;
- test manuel téléphone : toutes les coupes commencent et finissent proprement, sans syllabe mangée.

## 10. Critères d'acceptation
- 13 clips propres issus du master ;
- timecodes conformes au tableau ;
- une phrase aléatoire par nouveau gecko joueur ;
- pas de répétition immédiate ;
- contexte « Tu y es presque » cohérent ;
- dernier gecko encouragé sans cacophonie avec la victoire ;
- FX contrôle les voix ;
- jeu et logique totalement indépendants de l'audio ;
- master original conservé.


### Rangement audio effectué — 26/09/2026
Sans code applicatif, les trois MP3 ont été classés par rôle, sans réencodage ni modification binaire :
- `assets/audio/intro/jungle intro GeckoD.mp3` ;
- `assets/audio/celebration/jungle cebration GeckoD.mp3` ;
- `assets/audio/encouragements/master/Voix_encouragements.mp3`.

Les futurs 13 clips GECKO-023 seront placés sous `assets/audio/encouragements/clips/`.
Cette réorganisation ne donne encore aucun rôle runtime aux deux morceaux jungle et n'active pas GECKO-023 : elle prépare uniquement les assets pendant que Fab teste la v0.9.


# GECKO-024 — GRILLE IMMUABLE, COMPOSITION PNG/VIDÉO ET AUDIO GAMEPLAY
**Demandeur / date :** Fab, 26/09/2026  
**Statut :** build v0.10.0-dev vert sur branche `gecko-024-grid-audio` (run #28), à valider sur téléphone avant clôture fonctionnelle.

## 0. Retour téléphone qui déclenche la mission
- les vidéos riches sont affichées tête en bas ;
- l'animation Gecko est trop générale/grande pour une animation de case ;
- toute animation de case doit être ancrée sur la case réelle sans faire bouger, redimensionner ou re-mesurer la grille ;
- pendant une animation de case, un cache carré blanc masque le Gecko/rendu normal sous-jacent ; PNG et vidéo sont dessinés au-dessus ;
- les éléments décoratifs peuvent visuellement dépasser d'une case, mais ce débordement reste un overlay et ne modifie jamais la géométrie du plateau.

## 1. Contrat géométrique absolu
La grille est une référence immuable pendant la partie :
- aucune animation ne change sa taille, sa position ou ses contraintes de layout ;
- les coordonnées d'une case viennent uniquement du rectangle réel calculé par `GeckoBoardView` ;
- un overlay de case réutilise exactement ce rectangle ;
- le cache de case et la vidéo sont des frères dans `screenRoot`, jamais des enfants participant à la mesure de la grille ;
- toute animation terminée/échouée retire son overlay sans demander de re-layout au plateau.

## 2. Composition d'une animation Gecko de case
Ordre arrière → avant :
1. grille normale déjà dessinée ;
2. cache carré **blanc** exactement borné à la case ciblée ;
3. PNG statique Gecko si disponible/nécessaire ;
4. vidéo Gecko transparente par keycolor ;
5. à la fin : suppression du cache + vidéo, rendu normal inchangé dessous.

Le cache blanc masque notamment le Gecko confirmé déjà présent pendant `Gecko_apparition.mp4`, afin d'éviter le double Gecko.

## 3. Orientation vidéo
Corriger la cause du rendu tête en bas. `SurfaceTexture.getTransformMatrix()` reste la transformation de vérité. Les UV d'entrée ne doivent pas pré-appliquer un second retournement vertical.

## 4. PNG statiques
- Prof hors animation : `assets/prof/Prof.png`, transparent, dans la présentation du Prof.
- Gecko hors animation : PNG transparent dédié attendu sous `assets/gecko/Gecko.png`.
- Tant que `Gecko.png` n'existe pas, le Gecko procédural existant reste le fallback exact : ne pas inventer un asset silencieusement.
- les animations restent temporaires ; à leur fin, le PNG/fallback normal est visible.

## 5. Intro
L'intro vidéo reste un overlay et ne touche pas à la grille. Un fond/cadre noir opaque est affiché derrière la vidéo d'intro pour garantir une composition propre ; titre GeckoDoku et bouton Passer restent au-dessus.

## 6. MP3 de gameplay
Rôles désormais explicites :
- `assets/audio/intro/jungle intro GeckoD.mp3` : joué quand l'utilisateur appuie sur **Nouvelle** et que la nouvelle grille est créée ;
- `assets/audio/celebration/jungle cebration GeckoD.mp3` : musique de victoire pendant la célébration ;
- ces deux sons suivent FX ON/OFF ;
- absence/erreur de lecture → fallback silencieux ou FX procéduraux, jeu inchangé ;
- pas de superposition incontrôlée : le joueur audio dédié arbitre les pistes.

## 7. Voix d'encouragement / voix du Prof
GECKO-023 est intégré à ce lot :
- découper le master en 13 clips déterministes sous `assets/audio/encouragements/clips/` ;
- un nouveau Gecko correct trouvé **par le joueur** déclenche une phrase aléatoire ;
- jamais deux fois le même clip à la suite ;
- même cellule retirée/replacée : pas de nouvelle récompense dans la tentative ;
- « Tu y es presque » uniquement à 1–2 Gecko restants ;
- le dernier Gecko est lui aussi encouragé ;
- les Gecko posés par Prof ne déclenchent pas ces encouragements.

Le texte pédagogique du Prof peut être vocalisé via Android TextToSpeech français quand FX est ON. TTS indisponible → texte/bulle inchangés, aucune erreur logique. Une fermeture de bulle ou FX OFF arrête la parole.

## 8. Animation du Prof
`Prof.png` reste visible statiquement dans la bulle. `Prof_actions.mp4` reste une animation décorative occasionnelle ; lorsqu'elle est jouée dans le contexte Prof, elle doit être localisée sur la zone portrait du Prof plutôt qu'en grande animation plein écran. La bulle texte reste immédiatement disponible et prioritaire.

## 9. Critères d'acceptation
- vidéos dans le bon sens ;
- grille strictement immuable pendant tous les médias ;
- animation apparition/disparition ancrée sur la case exacte ;
- cache blanc exact sous la vidéo de case ;
- aucun double Gecko visible pendant l'apparition ;
- intro sur fond noir propre ;
- Prof statique via PNG transparent ;
- musique Nouvelle et musique Victoire aux bons événements ;
- 13 encouragements découpés et sélectionnés selon GECKO-023 ;
- voix Prof TTS non bloquante avec fallback ;
- tests unitaires + APK/AAB verts avant fusion ;
- validation visuelle finale sur téléphone par Fab.


### Note d'implémentation audio GECKO-024
Le premier build téléphone utilise les 13 fenêtres temporelles canoniques directement dans le master via MediaPlayer.seekTo(..., SEEK_CLOSEST), disponible à partir d'Android 26. Cette solution permet le test immédiat mais ne clôt pas l'étape GECKO-023 de production/versionnage des 13 clips physiques, qui reste ouverte si la précision des attaques/fins n'est pas parfaite sur téléphone.


# GECKO-025 — GECKO_TR CANONIQUE + PROF ANIMÉ À CHAQUE INTERVENTION
**Demandeur / date :** Fab, 26/09/2026  
**Statut :** implémenté et CI verte sur branche dédiée (run #33) ; validation téléphone requise.

## 1. Sprite Gecko canonique
Fab confirme que `assets/gecko/Gecko_tr.png` est le PNG officiel déjà uploadé.
- `Gecko_tr.png` devient l'unique source statique Gecko hors animation ;
- le petit `Gecko.png` ajouté par l'assistant est un doublon erroné et doit être supprimé ;
- le runtime doit pointer exclusivement vers `gecko/Gecko_tr.png` ;
- fallback procédural conservé uniquement si l'asset canonique est illisible.

## 2. Prof davantage animé
Le Prof ne doit plus être animé seulement avec une probabilité faible.
À chaque ouverture/actualisation d'une intervention Prof :
- texte et voix sont immédiats ;
- si Habillage animé ON, aucun média plus prioritaire, aucune hypothèse Prof en attente et aucune célébration : lancer `Prof_actions.mp4` dans la zone portrait ;
- animation locale, grille immuable ;
- audio embarqué de la vidéo Prof muet pendant la voix TTS ;
- si la vidéo est déjà active, ne pas en empiler une seconde ;
- fermeture de bulle / animation OFF / pause → arrêt propre.

## 3. Critères
- aucun conflit entre deux PNG Gecko ;
- Gecko normal = Gecko_tr.png ;
- Prof s'anime à chaque intervention éligible, sans hasard/cooldown ;
- texte/voix ne sont jamais retardés ;
- tests + APK/AAB verts ;
- validation téléphone Fab.


# GECKO-026 — BULLE PROF FLOTTANTE + PROF VIVANT DANS SON BOUTON
**Demandeur / date :** Fab, 26/09/2026  
**Statut :** implémenté, CI branche verte (run #37) ; validation téléphone requise.

## 0. Bug observé sur téléphone
Quand la bulle Prof apparaît, le cadrage de la grille change. Cause identifiée dans le code :
`showProfessorBubble()` mettait `controlsPanel.visibility = GONE`. La grille utilise une hauteur pondérée (`weight=1`) dans le `LinearLayout`; masquer les contrôles lui rend donc de l'espace et provoque un nouveau calcul de taille/cadrage.

## 1. Contrat de fenêtre flottante
La bulle Prof doit être un overlay pur dans `screenRoot` :
- elle ne retire, masque, redimensionne ni remesure aucun contrôle du jeu ;
- `controlsPanel` reste visible pendant toute la bulle ;
- la grille garde exactement les mêmes dimensions avant, pendant et après l'ouverture/fermeture ;
- la bulle peut recouvrir visuellement une zone du jeu car elle flotte, mais elle n'entre jamais dans le flux de layout ;
- la bulle conserve une grande croix accessible et une élévation visuelle claire.

## 2. Prof retiré de l'intérieur de la bulle
La bulle devient calme et lisible :
- aucun PNG Prof dans la bulle ;
- aucune vidéo `Prof_actions.mp4` dans la bulle ;
- aucune animation visuelle Prof à l'intérieur ;
- uniquement titre « Prof Gecko », texte pédagogique et croix de fermeture ;
- la voix TTS reste associée au texte et ne dépend pas de l'animation.

## 3. Prof vivant dans son bouton
Le PNG transparent `assets/prof/Prof.png` est déplacé visuellement vers le bouton Prof :
- le bouton reste à hauteur fixe afin de ne jamais changer la grille ;
- le PNG se superpose dans le bouton et peut dépasser légèrement vers le haut ;
- les parents concernés autorisent ce petit débordement (`clipChildren=false`) ;
- le texte du bouton reste lisible à droite du PNG ;
- à chaque interaction avec Prof, le PNG réalise une petite animation locale courte (léger soulèvement/agrandissement), sans vidéo et sans impact layout ;
- les changements de libellé (« Tester l'hypothèse », « Étape suivante ») continuent à fonctionner.

## 4. Prof_actions.mp4
`Prof_actions.mp4` reste conservé comme asset historique/futur mais n'est plus lancé depuis la bulle dans GECKO-026. GECKO-025 est donc supersédé sur ce point précis par la demande de Fab.

## 5. Accessibilité
- le bouton conserve son texte et son rôle de bouton ;
- le PNG du Prof est décoratif pour TalkBack afin d'éviter une annonce en double ;
- la bulle texte reste entièrement accessible ;
- aucun changement de fonctionnalité si le PNG ne peut pas être décodé : bouton texte seul.

## 6. Critères d'acceptation
- ouverture/fermeture de la bulle : grille strictement immuable ;
- contrôles jamais masqués par changement de layout ;
- bulle réellement flottante au-dessus de l'écran ;
- aucun portrait/vidéo Prof dans la bulle ;
- Prof.png visible dans le bouton avec léger débordement ;
- petite animation du PNG lors d'une interaction Prof, sans re-layout ;
- TTS Prof inchangé ;
- tests + APK + AAB verts ;
- validation téléphone par Fab.


# GECKO-027 — PROF TOUJOURS AU PREMIER PLAN DU BOUTON
**Demandeur / date :** Fab, 26/09/2026  
**Statut :** implémenté, CI branche verte (run #41) ; validation téléphone requise.

## 0. Bug téléphone
Le bouton Prof est visuellement dessiné devant le PNG du Prof. Le PNG est bien ajouté après le bouton dans le `FrameLayout`, mais le `Button` Android possède sa propre élévation / animation d'état et peut repasser au-dessus dans l'ordre Z.

## 1. Contrat d'empilement
- le bouton reste la surface tactile et le support du texte ;
- sa surface visuelle reste derrière le Prof ;
- `Prof.png` est toujours au premier plan du host, y compris pendant son animation locale ;
- le bouton ne doit pas gagner de Z au press/click ;
- le portrait garde une élévation dédiée supérieure ;
- l'animation scale/translation du portrait ne doit jamais modifier sa priorité Z ;
- aucune modification de hauteur ou de layout : grille immuable.

## 2. Critères d'acceptation
- aucun morceau du bouton ne passe devant le corps du Prof ;
- Prof.png et sa micro-animation restent devant le bouton ;
- texte du bouton reste lisible ;
- clic du bouton fonctionne normalement ;
- pas de re-layout ;
- tests + APK/AAB verts ;
- validation visuelle téléphone Fab.


# GECKO-028 — CACHE DE CASE HARMONISÉ + VOIX PROF MASCULINE + ANIMATIONS D'ATTENTE
**Demandeur / date :** Fab, 26/09/2026
**Statut :** implémenté, CI branche verte (run #45) ; validation téléphone requise.

## 1. Cache des animations Gecko
Le cache derrière une animation Gecko ne doit plus être blanc. Il reprend exactement la couleur de région de la case ciblée. Le cache est centré et légèrement plus petit que la case (inset 4 % de chaque côté) afin de masquer le Gecko normal sans former un carré visuellement agressif. La vidéo reste ancrée sur le rectangle complet de la case. La grille reste immuable.

## 2. Voix du Prof
Prof Gecko étant un personnage masculin :
- préférer une voix TTS française explicitement masculine si le moteur installé en expose une ;
- la sélection examine nom et features du moteur, sans supposer qu'Android fournit toujours une métadonnée de genre standard ;
- si aucune voix masculine n'est identifiable, garder une voix française locale disponible et utiliser un pitch plus grave ;
- fallback silencieux/texte inchangé si TTS indisponible.

## 3. Prof plus vivant dans son bouton
Le PNG Prof reste au premier plan du bouton. Ajouter plusieurs micro-animations locales (rebond, inclinaison, petit hochement) :
- immédiatement à chaque clic Prof ;
- aussi après une période d'attente aléatoire entre 10 et 20 secondes ;
- nouveau délai replanifié après chaque animation ;
- aucune animation si l'Activity est en pause, si le portrait est absent/invisible ou si l'habillage animé est OFF ;
- aucun re-layout : uniquement scale / rotation / translation ;
- pas de vidéo dans la bulle.

## 4. Critères
- cache Gecko = couleur exacte de la région, jamais blanc arbitraire ;
- cache inset 4 %, vidéo toujours calée sur la case entière ;
- voix Prof masculine quand disponible, sinon voix française plus grave ;
- 3 micro-animations Prof disponibles ;
- clic Prof anime immédiatement ;
- attente 10–20 s déclenche une animation locale ;
- pause/destroy retire les callbacks ;
- tests + APK/AAB verts ;
- validation téléphone Fab.


# GECKO-029 — PROF PLUS VIVANT, GECKO PLUS FRÉQUENT, MUSIQUE DE VICTOIRE NON TRONQUÉE
**Demandeur / date :** Fab, 26/09/2026
**Statut :** implémentation en cours sur `gecko-029-more-life-full-celebration`.

## 1. Prof : idle 2–3 secondes
Le Prof doit paraître beaucoup plus vivant :
- délai d'inactivité entre deux micro-animations : 2 000 à 3 000 ms ;
- le timer reste actif même si la bulle Prof est ouverte ;
- la bulle ne bloque jamais l'animation du PNG dans le bouton ;
- clic Prof = animation immédiate + replanification du prochain idle ;
- Anim OFF / pause / destroy = aucun callback.

## 2. Gecko : animations longues plus fréquentes
Les actions longues Gecko restent complètes et non découpées mais deviennent nettement plus fréquentes :
- probabilité par événement éligible : 45 % ;
- cooldown partagé : 45 secondes ;
- toujours une seule vidéo riche à la fois ;
- pas pendant victoire / média actif / hypothèse Prof critique / partie terminée ;
- la grille et les coordonnées restent immuables.

## 3. Musique de félicitations
Le MP3 `audio/celebration/jungle cebration GeckoD.mp3` ne doit plus être arrêté par la fin de l'animation visuelle de célébration.
- fin de `VictoryCelebrationView` = arrêt des FX procéduraux uniquement ;
- le MP3 continue jusqu'à sa fin naturelle ;
- Nouvelle / FX OFF / pause / destroy peuvent toujours l'arrêter proprement ;
- absence ou erreur MP3 = jeu inchangé.

## 4. Critères
- idle Prof toujours 2–3 s ;
- fonctionne même bulle ouverte ;
- Gecko long action : 45 % / 45 s ;
- musique victoire non tronquée par la fin visuelle ;
- tests + APK/AAB verts ;
- validation téléphone Fab.
