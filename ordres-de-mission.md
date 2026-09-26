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
**Statut :** implémenté, CI branche verte (run #49) ; validation téléphone requise.

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


# GECKO-030 — RESTAURATION DES VRAIES ANIMATIONS PROF
**Demandeur / date :** Fab, 26/09/2026
**Statut :** implémenté, CI branche verte (run #53) ; validation téléphone requise.

## 0. Régression confirmée
Le fichier fourni à nouveau par Fab, `14126.mp4`, est exactement le même blob que `assets/prof/Prof_actions.mp4` dans Git :
- taille : 2 717 098 octets ;
- blob Git : `87c72def4f7c79b7c7c35ed254e3380ac5a7c0db` ;
- SHA-256 du fichier fourni : `12a186ff1984870265fb96891014cd0855cfd1e41499e33133e997e050545567` ;
- vidéo : H.264 640×640, 30 fps, 30,070 s + AAC.

Historique :
- GECKO-025 / commit `8aaa06f95dd626a654a95a4b284280b43705a47c` : le vrai `Prof_actions.mp4` était joué ;
- GECKO-026 / commit `f555102094cda6a21ed7812dae247748329ea62d` : le runtime vidéo Prof a été supprimé et remplacé par de simples transformations du PNG.
Cette substitution est la régression à corriger.

## 1. Emplacement restauré, sans réintroduire l'ancien défaut
Fab ne veut pas la vidéo dans la bulle pédagogique.
Le vrai `Prof_actions.mp4` doit jouer **dans la zone du bouton Prof**, devant le bouton, à l'emplacement du portrait :
- bouton et grille restent à géométrie fixe ;
- bulle reste calme : texte + croix + TTS, aucune vidéo ;
- `Prof.png` est l'état de repos ;
- pendant la vidéo, la vidéo chroma-key remplace visuellement le PNG ;
- fin/erreur → retour automatique au PNG ;
- le bouton reste cliquable.

## 2. Déclenchement
- clic Prof : lancer immédiatement la vraie vidéo si elle n'est pas déjà en lecture ;
- après 2–3 s d'inactivité : lancer la vraie vidéo ;
- bulle ouverte : animation toujours autorisée dans le bouton ;
- si la vidéo est déjà en cours : ne pas la relancer ni l'empiler ;
- Anim OFF / pause / destroy : arrêter proprement et restaurer le PNG.

## 3. Lecture exacte
- utiliser `assets/prof/Prof_actions.mp4` entier ;
- t=0 → EOF naturel (~30,070 s) ;
- aucun timecode, aucun découpage, aucune boucle interne ;
- audio embarqué muet, car le TTS Prof et les FX restent gérés séparément ;
- le bleu reste transparent via le même ChromaKeyVideoView corrigé.

## 4. Isolation
La vidéo Prof du bouton utilise son propre `ChromaKeyVideoView` local au `professorButtonHost`.
Elle ne doit pas monopoliser `RichMediaOverlayView`, afin que les animations de cases Gecko gardent leur système propre.
Aucune modification du moteur logique.

## 5. Fallback
Si le MP4 ou le shader échoue :
- revenir immédiatement à `Prof.png` ;
- garder le texte/TTS ;
- utiliser éventuellement la petite micro-animation PNG comme fallback visuel ;
- jeu inchangé.

## 6. Critères
- vraies animations de `14126.mp4 / Prof_actions.mp4` visibles ;
- vidéo devant le bouton, jamais dans la bulle ;
- lecture complète 30,070 s ;
- déclenchement clic + idle 2–3 s ;
- bulle ouverte n'empêche pas l'animation ;
- PNG restauré après fin/erreur/arrêt ;
- aucune modification de grille/layout ;
- tests + APK/AAB verts ;
- validation téléphone Fab.


# GECKO-031 — TEST A/B TTS LOCAL ANDROID vs PIPER LOW vs PIPER MEDIUM
**Demandeur / date :** Fab, 26/09/2026
**Statut :** implémenté sur branche expérimentale ; CI verte run #58 ; test téléphone Fabrice requis avant toute fusion/décision.

## 0. Règle de non-régression
Cette mission ne remplace pas la voix normale du Prof Gecko.
`ProfessorSpeech` et le TTS Android actuel restent la référence runtime du jeu.
Le test A/B est un écran temporaire séparé et optionnel.

Avant toute modification, l'existant, `ordres-de-mission.md`, `brain.md`, `brainmap.md`, `debughistorical.md` et `todo.md` ont été relus depuis le HEAD `57db0ee3d7f6ba57cca54d381531b6495b21aa18`.

## 1. Moteurs comparés
La même phrase de test est jouée successivement avec :
1. Android TTS actuel ;
2. Piper LOW ;
3. Piper MEDIUM.

Les deux voix Piper utilisent la même famille française `fr_FR-siwis` afin que la comparaison LOW/MEDIUM porte principalement sur la qualité et le poids, pas sur un changement de speaker.

## 2. Sherpa-ONNX / Piper
- version Sherpa-ONNX figée pour l'expérience : `v1.13.8` ;
- API Kotlin officielle : `OfflineTts`, `OfflineTtsVitsModelConfig`, `OfflineTtsModelConfig`, `OfflineTtsConfig` ;
- LOW : `vits-piper-fr_FR-siwis-low` ;
- MEDIUM : `vits-piper-fr_FR-siwis-medium` ;
- les archives officielles doivent être injectées dans les assets pendant la CI, sans commiter ~90 Mo de modèles dans le dépôt ;
- l'APK/AAB expérimental contient effectivement les deux modèles et fonctionne hors ligne à l'exécution.

## 3. API Kotlin unique
Créer une API commune de benchmark utilisée par les trois moteurs :
- même texte ;
- même callback de résultat ;
- même structure de métriques ;
- aucune branche UI spécifique au LOW vs MEDIUM.

LOW et MEDIUM utilisent la même implémentation Piper, paramétrée uniquement par un profil/catalogue.

## 4. RAM : un seul Piper à la fois
Le gestionnaire Piper ne conserve jamais LOW et MEDIUM simultanément en mémoire :
1. si un autre Piper est chargé, appeler `release()` ;
2. annuler/référencer null l'ancien ;
3. ensuite seulement créer le nouveau `OfflineTts`.

Le test doit verrouiller l'ordre **release ancien → create nouveau**.

## 5. Écran temporaire
Ajouter un bouton temporaire `🧪 Voix A/B` sans ajouter une nouvelle hauteur de contrôles.
Il ouvre un dialogue accessible affichant :
- la phrase test commune ;
- bouton `1. Android TTS` ;
- bouton `2. Piper LOW` ;
- bouton `3. Piper MEDIUM` ;
- résultats/erreurs sous chaque essai.

Le jeu normal continue de fonctionner si Sherpa ou les modèles sont indisponibles.

## 6. Mesures
Afficher si disponible :
- temps de génération ;
- temps de chargement du modèle Piper ;
- taille du modèle ONNX ;
- mémoire approximative du processus via PSS Android avant/après chargement/génération ;
- sample rate / durée audio générée pour Piper.

Les valeurs mémoire sont explicitement approximatives.

## 7. Modèles retenus
Références officielles :
- LOW ONNX ~28,1 Mo, 16 kHz ;
- MEDIUM ONNX ~63,2 Mo, 22,05 kHz.

## 8. Critères d'acceptation
- jeu normal et voix Prof inchangés hors écran expérimental ;
- même phrase pour les trois moteurs ;
- LOW et MEDIUM passent par exactement la même API Kotlin ;
- un seul OfflineTts Piper vivant à la fois ;
- passage LOW→MEDIUM et MEDIUM→LOW libère d'abord l'ancien ;
- modèles présents dans l'APK expérimental ;
- génération et lecture entièrement locales/offline ;
- métriques visibles ;
- fermeture/pause/destroy libèrent les ressources expérimentales ;
- tests unitaires + APK + AAB verts ;
- Fabrice teste sur téléphone et choisit ensuite LOW/MEDIUM/Android.


### Correctif CI GECKO-031 — dépendance Android native
Le premier GREEN run #57 a validé le téléchargement et les SHA-256 des deux modèles Piper, puis a échoué à la compilation pour deux causes isolées :
1. la coordonnée JitPack tirait simultanément `sherpa-onnx-jvm-v1.13.8.jar` et `sherpa-onnx-v1.13.8.aar`, créant des classes dupliquées ;
2. `Debug.getPss()` est typé `Long` sur le SDK utilisé alors que les métriques expérimentales utilisaient `Int`.

Décision :
- ne plus utiliser JitPack ;
- télécharger l'AAR Android officiel `sherpa-onnx-1.13.8.aar` depuis la release v1.13.8 ;
- vérifier son SHA-256 officiel `633c24321e06b1fe79feafa03ea16cbc0f8a286641e2da3559bac91bdb13bd96` ;
- l'injecter sous `app/libs/` pendant la CI ;
- stocker les métriques PSS en `Long`.


# GECKO-032 — PIERRE UPMC MEDIUM DEVIENT LA VOIX LOCALE DU PROF
**Demandeur / date :** Fab, 26/09/2026
**Statut :** livré sur `main`, CI finale #63 GREEN ; validation téléphone de Pierre et des annonces stats requise.

## 0. Décision après test A/B
Fab valide le principe Piper MEDIUM et demande la voix masculine Pierre.
Le modèle retenu est `fr_FR-upmc-medium`, multi-speaker :
- Jessica = sid 0 ;
- Pierre = sid 1.
Le modèle Siwis LOW et Siwis MEDIUM du laboratoire A/B sont retirés.

## 1. Voix runtime
- Prof Gecko utilise en priorité Piper UPMC Medium avec `sid = 1` ;
- synthèse locale/offline via Sherpa-ONNX v1.13.8 ;
- un seul modèle TTS embarqué ;
- chargement paresseux et réutilisation d'un seul `OfflineTts` ;
- fallback Android TTS uniquement si Piper ne peut pas charger/générer ;
- le bouton temporaire `🧪 Voix A/B` et tout le labo A/B sont supprimés.

## 2. Encouragements
Les encouragements audio existants restent intacts.
À chaque Gecko récompensé, choisir aléatoirement entre :
- un segment enregistré existant ;
- un encouragement parlé par Pierre.
Pierre dispose d'une liste complémentaire de formulations françaises, distinctes des 13 clips actuels.
Aucun remplacement des sons existants.

## 3. Stats au début d'une nouvelle grille
Quand l'utilisateur lance une nouvelle grille :
1. créer la grille et enregistrer le départ ;
2. jouer la musique d'ouverture ;
3. attendre la fin naturelle de cette musique ;
4. Pierre annonce un résumé court des statistiques locales du joueur.

Le résumé comprend au minimum :
- parties lancées ;
- parties terminées ;
- taux de réussite ;
- erreurs ;
- temps moyen si disponible.

Si la musique d'ouverture ne peut pas être jouée, l'annonce peut partir immédiatement.
Si les FX/sons sont désactivés, aucune annonce vocale automatique.

## 4. Taille
La CI n'embarque plus que :
- Sherpa-ONNX AAR officiel v1.13.8 ;
- `vits-piper-fr_FR-upmc-medium`.
LOW et Siwis MEDIUM sont absents de l'artefact final.

## 5. Critères
- `sid = 1` verrouillé pour Pierre ;
- UPMC Medium est l'unique modèle Piper embarqué ;
- bouton A/B supprimé ;
- Prof normal = Pierre local, fallback Android seulement sur erreur ;
- encouragements enregistrés + Pierre coexistent ;
- stats annoncées après la musique d'ouverture ;
- pas de chevauchement musique d'ouverture / annonce stats ;
- aucune régression grille, animations, victoire ou Prof ;
- tests + APK/AAB verts ;
- fichiers FAB Copilot synchronisés.


# GECKO-033 — NOUVELLE IDENTITÉ INTRO / ICÔNE + ANIMATION PROF PARLANTE
**Demandeur / date :** Fab, 26/09/2026
**Statut :** mission préparée uniquement. **Aucun code exécuté dans ce cycle.**

## 0. Assets déjà uploadés sur main
Les trois assets sont présents dans le dépôt et leurs rôles sont désormais figés :
- `assets/prof/ProfParle.mp4` — animation du Prof quand Pierre parle ;
- `assets/gecko/IntroGeckoGD.mp4` — nouvelle première séquence d'introduction ;
- `assets/gecko/IconGeckoGD.png` — identité graphique GeckoDoku / icône.

Ne pas renommer, réencoder ni déplacer ces fichiers sans nouvelle instruction de Fab.

## 1. ProfParle.mp4 — animation synchronisée avec Pierre
Quand `ProfessorSpeech` fait parler Pierre :
1. lancer `assets/prof/ProfParle.mp4` **juste avant** le début de la synthèse/lecture vocale ;
2. afficher cette vidéo dans la zone visuelle du Prof déjà prévue au premier plan, jamais dans la bulle pédagogique ;
3. la bulle reste calme : texte + croix + TTS, sans vidéo interne ;
4. la grille et les contrôles restent strictement immuables.

### Réutilisation pendant les ~30 secondes
`ProfParle.mp4` dure environ 30 secondes.
- si Pierre reparle alors que `ProfParle.mp4` est déjà en cours, **réutiliser la lecture en cours** ;
- ne pas redémarrer la vidéo à t=0 à chaque phrase ;
- ne pas empiler une seconde instance ;
- si la vidéo est terminée et que Pierre reparle plus tard, relancer depuis t=0 ;
- si la parole finit avant la vidéo, laisser l'animation aller à sa fin naturelle sauf priorité supérieure / arrêt Activity / Anim OFF ;
- si vidéo absente ou illisible, Pierre parle quand même et le PNG Prof reste le fallback.

L'audio embarqué éventuel de la vidéo doit rester muet : la voix vient de Pierre via Sherpa-ONNX/Piper.

## 2. IntroGeckoGD.mp4 — nouvelle première intro
La séquence d'ouverture devient :
1. `assets/gecko/IntroGeckoGD.mp4`
2. puis l'intro existante actuelle `assets/gecko/Gecko_Intro.mp4`
3. puis révélation du jeu déjà prêt derrière.

Règles :
- les deux vidéos sont des overlays, jamais des éléments de layout ;
- aucune grille ne doit être recréée entre les deux vidéos ;
- un skip/fermeture accessible doit pouvoir sortir proprement de la séquence ;
- Anim OFF conserve l'accès direct au jeu comme aujourd'hui ;
- ne pas découper ni réencoder `IntroGeckoGD.mp4` sans nouvelle mission.

## 3. IconGeckoGD.png — icône officielle et présence près du titre
`assets/gecko/IconGeckoGD.png` devient la source graphique à utiliser pour :
- l'icône de l'APK/application Android ;
- un petit visuel GeckoDoku placé **en haut de l'écran, près du titre**.

Règles UI :
- le visuel près du titre doit respecter les proportions du PNG ;
- il ne doit pas pousser ou redimensionner la grille ;
- garder un contraste lisible et une taille raisonnable ;
- l'accessibilité du titre reste prioritaire ;
- pour l'icône Android, générer les ressources launcher nécessaires à partir de cette source sans partager ni altérer l'original.

## 4. Non-régression
GECKO-033 ne doit pas casser les éléments validés précédemment :
- Pierre = UPMC Medium, `sid=1` ;
- encouragements enregistrés + encouragements Pierre ;
- annonce des stats après la musique d'ouverture ;
- vraie animation `Prof_actions.mp4` déjà restaurée pour les actions du Prof ;
- bulle Prof flottante ;
- Prof devant son bouton ;
- grille immuable ;
- animations Gecko et musique de victoire.

### Distinction impérative entre les deux vidéos Prof
- `Prof_actions.mp4` = actions/animations générales du Prof ;
- `ProfParle.mp4` = animation **spécifique à la parole de Pierre**.

Ne jamais confondre ni substituer les deux assets.

## 5. Critères d'acceptation futurs
- parler avec Pierre déclenche/reprend correctement `ProfParle.mp4` ;
- plusieurs phrases pendant la même fenêtre ~30 s réutilisent la vidéo en cours ;
- intro = `IntroGeckoGD.mp4` puis `Gecko_Intro.mp4` ;
- `IconGeckoGD.png` = icône APK/app + visuel près du titre ;
- aucun re-layout de grille ;
- assets manquants = fallback propre ;
- fichiers FAB Copilot synchronisés avec le code dans le même commit lors de l'implémentation ;
- tests + APK/AAB verts avant fusion.


<!-- GECKO-033-ADDENDUM-FAB-2026-09-26 -->
# GECKO-033 — ADDENDUM FAB : IDENTITÉ, AUDIO MUET DES GECKOS, PROF VIVANT ET 100 RÉPLIQUES
**Demandeur / date :** Fab, 26/09/2026  
**Statut :** conception/mission synchronisée uniquement. **Aucun code applicatif modifié dans ce cycle.**

## A. Contrat de synchronisation visuelle de l’icône
`assets/gecko/IconGeckoGD.png` reste l’unique source graphique de référence pour les deux usages :
1. **launcher Android** : générer les ressources mipmap/adaptive icon nécessaires à l’APK/AAB à partir de cette source ;
2. **dans l’application** : afficher la même identité graphique **à gauche du titre GeckoDoku**, dans un petit médaillon rond/détouré de type fenêtre florale.

Règles du médaillon :
- aspect rond, propre et détouré ;
- animation légère locale autorisée (respiration, très faible rotation/oscillation ou éclat discret) ;
- **ne jamais utiliser `IntroGeckoGD.mp4` pour animer ce médaillon** ;
- animation par transform/alpha seulement, sans reflow ;
- aucune modification du calcul, de la taille ou de la position de la grille ;
- launcher et médaillon doivent rester visuellement synchronisés car dérivés de la même source `IconGeckoGD.png`.

## B. Son des animations Gecko
Nouvelle décision Fab qui remplace la règle historique « son vidéo selon FX » pour les animations Gecko :
- **toute animation vidéo où un Gecko s’anime est muette** ;
- couper le son embarqué des vidéos Gecko au niveau du player, sans modifier/réencoder les fichiers source ;
- cela concerne au minimum les apparitions, disparitions et actions Gecko, ainsi que les séquences Gecko utilisées comme animation visuelle ;
- la musique générale du jeu, la musique d’ouverture et la musique de victoire restent des flux audio séparés et ne sont pas supprimées par cette règle ;
- l’état FX ne doit jamais réactiver le son embarqué d’une animation Gecko devenue contractuellement muette.

Objectif : conserver le visuel mignon sans la bande-son jugée gênante.

## C. ProfParle.mp4 synchronisé avec Pierre — sans confusion avec Prof_actions
Contrat impératif :
- `assets/prof/ProfParle.mp4` = **animation exclusive de parole de Pierre** ;
- `assets/prof/Prof_actions.mp4` = **animation générale/idle/action du Prof** ;
- jamais de substitution implicite entre les deux.

Quand une parole Pierre démarre réellement :
1. demander l’affichage de `ProfParle.mp4` juste avant le démarrage audio ;
2. afficher la vidéo dans le host visuel du Prof, devant le bouton et hors bulle ;
3. vidéo **toujours muette** ; la voix audible vient de Piper/Sherpa-ONNX UPMC Medium, Pierre `sid=1` ;
4. si `ProfParle.mp4` est déjà en cours, **réutiliser la lecture courante**, sans restart et sans seconde instance ;
5. si la lecture est terminée, une nouvelle parole peut repartir de t=0 ;
6. si le média échoue, la parole Pierre continue et `Prof.png` demeure le fallback ;
7. la bulle pédagogique et la grille ne changent jamais de géométrie à cause de cette vidéo.

Le contrôleur de parole doit être indépendant du contrôleur `Prof_actions.mp4` mais partager une politique de priorité empêchant deux vidéos Prof superposées.

## D. Intro
Chaîne officielle :
`IntroGeckoGD.mp4` → `Gecko_Intro.mp4` → jeu déjà prêt derrière.

- `IntroGeckoGD.mp4` est uniquement une intro plein écran/overlay ; il ne sert jamais à animer l’icône près du titre.
- conserver le skip accessible ;
- Anim OFF saute les intros ;
- aucune intro ne recrée la grille ni ne modifie son layout ;
- ne pas renommer, déplacer ni réencoder les nouveaux assets.

## E. Prof vivant : interventions contextuelles
Pierre peut parler sans clic direct du joueur, mais sans devenir envahissant.

### E1. Inactivité
Après une période d’inactivité suffisamment longue pendant une partie active :
- Pierre peut demander si le joueur souhaite de l’aide ;
- il rappelle explicitement qu’il peut **appuyer sur le bouton Prof** pour obtenir une aide ;
- une seule proposition par épisode d’inactivité ;
- toute action du joueur réarme le mécanisme après un nouveau délai ;
- ne jamais lancer ce rappel pendant intro, victoire, dialogue, chargement, sauvegarde, parole en cours ou étape Prof critique.

Valeur initiale de test recommandée : **90 s sans coup**, constante réglable après validation téléphone.

### E2. Partie longue / sauvegarde
Sur une partie devenue longue :
- Pierre peut proposer de la sauvegarder dans le journal ;
- ne jamais sauvegarder automatiquement ;
- ne proposer qu’une fois par tentative tant que le contexte n’a pas changé ;
- ne pas interrompre une déduction ou une célébration.

Valeur initiale de test recommandée : **10 min de partie active ou 25 actions joueur**, constante réglable.

### E3. Prendre des nouvelles
Pierre peut occasionnellement demander au joueur comment il va ou commenter calmement la partie.
- pas de répétition immédiate ;
- pas de parole spontanée pendant une autre parole ;
- priorité toujours inférieure aux messages logiques, stats, victoire et aide explicite.

### E4. Banalités Gecko
Prévoir un catalogue statique de **100 répliques** ci-dessous.
Sélection :
- aléatoire avec anti-répétition immédiate ;
- cooldown long et variable ;
- aucune banalité si le joueur vient d’appuyer sur Prof ou si une information utile doit être dite ;
- ces phrases sont du décor vivant et ne modifient jamais la partie.

Valeur initiale de test recommandée : **4 à 8 minutes entre deux banalités spontanées**, réinitialisée après toute parole Pierre.

## F. 100 répliques officielles de Pierre
001. « Parfois, la meilleure façon de voir une solution est simplement de regarder la grille autrement. »
002. « Un gecko ne se presse pas pour tenir au mur. Nous pouvons faire pareil avec cette grille. »
003. « Une case après l’autre. Les grandes énigmes aiment les petits pas. »
004. « Je reste là. Pas besoin de se presser. »
005. « Une pause peut être une technique de résolution tout à fait respectable. »
006. « Regarder longtemps n’est pas perdre du temps quand on cherche un détail. »
007. « Les grilles ont parfois l’air muettes, puis soudain elles deviennent très bavardes. »
008. « Je préfère une déduction lente et sûre à trois coups joués au hasard. »
009. « Il y a quelque chose d’apaisant dans une grille qui se clarifie progressivement. »
010. « Le calme est souvent un excellent outil logique. »
011. « Un bon raisonnement n’a pas besoin de faire beaucoup de bruit. »
012. « Le prochain indice est peut-être déjà sous vos yeux. »
013. « Une grille difficile n’est pas une grille hostile. Elle demande seulement une autre manière de regarder. »
014. « Les erreurs aiment la précipitation. Les geckos, beaucoup moins. »
015. « On peut rester immobile et pourtant avancer dans sa tête. »
016. « Il n’y a aucune honte à revenir sur une zone déjà observée. »
017. « Une bonne déduction ressemble souvent à une évidence… une fois qu’on l’a trouvée. »
018. « Je crois beaucoup aux secondes lectures. »
019. « Parfois, la solution commence exactement là où l’on avait cessé de regarder. »
020. « Nous avons tout notre temps. Enfin… le gecko sur le mur, lui, surveille peut-être déjà le prochain moustique. »
021. « Vous devez aimer les geckos pour passer autant de temps à les chercher. »
022. « Un gecko posé sur un mur donne toujours l’impression de connaître un passage secret. »
023. « Je pourrais regarder un gecko chasser près d’une lampe pendant des heures. »
024. « Il y a quelque chose de très sérieux dans la façon dont un gecko fixe un insecte. »
025. « Un gecko immobile peut soudain devenir extraordinairement rapide. Une grille fait parfois la même chose. »
026. « Le gecko a trouvé sa case. À nous de trouver les autres. »
027. « Je soupçonne les geckos d’être meilleurs au Sudoku qu’ils ne veulent bien l’avouer. »
028. « Si un gecko vous regarde, faites comme si vous aviez parfaitement compris la grille. »
029. « Un gecko qui grimpe au plafond manque franchement de respect à la gravité. »
030. « Je me demande si les geckos comptent les humains pour s’endormir. »
031. « Le soir, certains comptent les moutons. Moi, je compterais volontiers les geckos. »
032. « Un petit gecko sur un grand mur : voilà quelqu’un qui ne doute pas de son échelle. »
033. « Quand un gecko s’arrête net, j’ai toujours l’impression qu’il vient de résoudre quelque chose. »
034. « Près d’une lampe, un gecko et un papillon de nuit peuvent transformer un mur en documentaire animalier. »
035. « Une seule patte bien placée peut parfois suffire à garder l’équilibre. Une seule bonne déduction aussi. »
036. « Le gecko n’a pas besoin d’une échelle. C’est presque de la triche. »
037. « J’aime bien l’idée qu’un gecko puisse considérer un plafond comme un simple deuxième sol. »
038. « Si la grille avait des murs, le gecko les escaladerait probablement. »
039. « Les geckos ont une manière très personnelle de rappeler que le haut et le bas sont des conventions. »
040. « Je crois que cette grille gagnerait beaucoup à avoir un vrai gecko posé dans un coin. Mais restons raisonnables. »
041. « Les pattes des geckos portent des structures microscopiques appelées setae. »
042. « À l’extrémité des setae, des structures encore plus petites augmentent énormément la surface de contact. »
043. « Ces extrémités minuscules sont souvent appelées spatules ou spatulae. »
044. « L’adhérence sèche des geckos repose principalement sur des interactions de van der Waals. »
045. « Le gecko n’utilise donc ni ventouse ni colle pour tenir sur une vitre propre. »
046. « Ce qui paraît magique à notre échelle devient très physique quand on descend vers l’échelle nanométrique. »
047. « Les forces de van der Waals sont faibles individuellement, mais leur multitude peut produire une adhérence remarquable. »
048. « Le système adhésif du gecko fonctionne parce qu’un très grand nombre de petits contacts travaillent ensemble. »
049. « Pour se détacher, le gecko change notamment l’angle de ses structures adhésives, un peu comme lorsqu’on décolle un ruban. »
050. « L’adhérence des geckos est directionnelle : la façon dont la patte est orientée compte. »
051. « Les chercheurs étudient les geckos pour concevoir des adhésifs secs inspirés du vivant. »
052. « On appelle cela du biomimétisme : observer le vivant pour imaginer de nouvelles solutions techniques. »
053. « Le pied d’un gecko est un très bel exemple de structure hiérarchique, du visible jusqu’au nanométrique. »
054. « Ce n’est pas une force mystérieuse qui colle le gecko au mur, mais énormément de minuscules interactions bien organisées. »
055. « Quand des millions de contacts faibles coopèrent, le résultat peut devenir étonnamment fort. »
056. « Les geckos nous rappellent qu’à petite échelle, la physique peut devenir contre-intuitive. »
057. « Une surface qui semble parfaitement lisse pour nous possède encore beaucoup de détails à l’échelle d’un gecko. »
058. « Les pattes du gecko maximisent le contact avec la surface plutôt que de produire une colle. »
059. « La rapidité avec laquelle un gecko attache puis détache ses pattes est presque aussi fascinante que son adhérence. »
060. « Science et geckos font plutôt bon ménage, vous ne trouvez pas ? »
061. « Je me demande combien de chercheurs ont commencé une carrière entière simplement parce qu’un gecko refusait de tomber. »
062. « Le gecko est un excellent professeur de physique : il fait la démonstration avant de donner l’explication. »
063. « Une patte de gecko, c’est beaucoup de finesse pour résoudre un problème très simple : ne pas tomber. »
064. « Les meilleures inventions humaines commencent parfois par quelqu’un qui regarde attentivement un animal faire quelque chose d’impossible en apparence. »
065. « Je trouve rassurant que la physique puisse être aussi élégante sur le bout des doigts… ou plutôt des orteils. »
066. « Un gecko ne connaît probablement pas l’expression « forces de van der Waals ». Heureusement, elles fonctionnent quand même. »
067. « Voilà une belle leçon : comprendre un phénomène n’est pas nécessaire pour en profiter. Mais c’est encore plus amusant quand on le comprend. »
068. « Les geckos savent exploiter la matière à une échelle où notre intuition quotidienne devient assez mauvaise. »
069. « Chaque fois que je vois un gecko sur une vitre, j’ai envie d’imaginer tout ce qui se passe sous chacune de ses pattes. »
070. « On parle souvent de haute technologie. Les geckos pratiquent la nanotechnologie depuis bien avant nous. »
071. « Comment allez-vous ? La grille ne vous traite pas trop sévèrement ? »
072. « Je passe simplement prendre de vos nouvelles. Continuez à votre rythme. »
073. « Tout va bien de votre côté ? Je reste disponible si vous voulez un coup de main. »
074. « Vous avez déjà bien avancé. Gardez votre rythme. »
075. « Je vous laisse réfléchir, mais je ne suis jamais très loin. »
076. « Je vois que vous prenez le temps d’observer. C’est souvent une bonne stratégie. »
077. « Ne vous laissez pas intimider par les cases encore vides. »
078. « Vous pouvez très bien ne rien jouer pendant un moment et réfléchir. C’est toujours jouer. »
079. « J’espère que cette grille vous amuse autant qu’elle vous résiste. »
080. « Il y a encore du chemin, mais il est déjà plus court qu’au début. »
081. « Chaque gecko placé correctement change un peu toute la carte. »
082. « Je garde un œil sur la grille pendant que vous gardez un œil sur les geckos. »
083. « Vous avancez peut-être plus que vous ne le pensez. »
084. « Si vous sentez que vous tournez en rond, changez simplement de ligne, de colonne ou de zone. »
085. « Une grille longue peut devenir une petite histoire. Celle-ci a déjà quelques chapitres. »
086. « Le plus agréable, c’est souvent l’instant où plusieurs déductions s’enchaînent d’un seul coup. »
087. « Je vous promets de ne pas révéler la solution pendant que vous réfléchissez. »
088. « Je suis un professeur très patient. C’est pratique : je suis dans le téléphone. »
089. « Le gecko près du titre semble confiant. Je propose de lui faire confiance. »
090. « Encore quelques observations et cette grille pourrait bien commencer à céder. »
091. « Je me demande si un gecko choisirait toujours la case la plus chaude. »
092. « Un mur de garage, une lumière du soir et un gecko en chasse : il n’en faut parfois pas davantage pour faire un bon spectacle. »
093. « Quand un gecko bondit sur un papillon de nuit puis se raccroche, on comprend vite pourquoi ils sont si difficiles à quitter des yeux. »
094. « Certains regardent les oiseaux. D’autres les étoiles. Les geckos ont aussi leurs passionnés. »
095. « J’aime beaucoup leur mélange de tranquillité et de vitesse soudaine. »
096. « Un gecko peut rester parfaitement immobile juste avant une action fulgurante. C’est une assez bonne métaphore pour la réflexion. »
097. « Il faut une certaine élégance pour passer la soirée la tête en bas sans en faire toute une histoire. »
098. « Je me demande si les geckos trouvent nos sols terriblement limités. »
099. « Un gecko dans un garage doit avoir une opinion très précise sur l’éclairage nocturne. »
100. « Je crois qu’un papillon de nuit n’apprécierait pas autant que nous cette conversation sur les geckos. »

## G. Précision scientifique à respecter
Ne pas faire dire à Pierre que l’adhérence du gecko est directement « l’effet Casimir ». Le contrat scientifique retenu est : structures hiérarchiques de type setae/spatulae + très grande surface de contact + interactions de **van der Waals** comme mécanisme principal d’adhérence sèche. Les formulations de la banque ci-dessus suivent cette règle.

## H. RED TDD à préparer avant implémentation
Avant tout code de GECKO-033, écrire des tests RED couvrant au minimum :
- même source `IconGeckoGD.png` contractualisée pour launcher + médaillon in-app ;
- médaillon à gauche du titre sans changement de métrique/grille ;
- médaillon animé localement sans `IntroGeckoGD.mp4` ;
- toutes animations Gecko contractuellement muettes ;
- `ProfParle.mp4` demandé avant parole Pierre ;
- réutilisation de la lecture ProfParle déjà active ;
- aucune seconde instance ProfParle ;
- `Prof_actions.mp4` distinct de `ProfParle.mp4` ;
- ordre intro `IntroGeckoGD.mp4` puis `Gecko_Intro.mp4` ;
- idle help : proposition unique + rappel du bouton Prof ;
- partie longue : proposition de sauvegarde sans sauvegarde automatique ;
- catalogue contenant exactement 100 banalités ;
- anti-répétition des banalités ;
- aucune parole spontanée pendant contexte bloquant ;
- fallback média : logique, voix et grille intactes.

## I. Critères d’acceptation
GECKO-033 ne sera déclarée terminée qu’après :
- RED observé pour la bonne raison ;
- implémentation GREEN ;
- synchronisation `ordres-de-mission.md`, `brain.md`, `brainmap.md`, `debughistorical.md`, `todo.md` dans le même commit de code ;
- CI tests + APK + AAB verte ;
- APK/AAB correctement nommés avec l’icône GeckoDoku ;
- validation téléphone par Fab de l’icône, du médaillon, des deux intros, de la voix Pierre + ProfParle, du silence des animations Gecko et du comportement non envahissant des interventions spontanées.


<!-- GECKO-033-PROFPARLE-SPEECH-LIFECYCLE-2026-09-26 -->
## GECKO-033 — précision cycle de vie ProfParle
Décision Fab : `ProfParle.mp4` doit suivre **la durée réelle de la parole de Pierre**, et non continuer jusqu’à la fin naturelle de ses ~30 secondes.

Règle runtime cible :
1. juste avant le démarrage effectif d’une parole Sherpa/Piper de Pierre, lancer `ProfParle.mp4` depuis t=0 si elle n’est pas déjà active ;
2. si une nouvelle phrase s’enchaîne alors que Pierre parle encore et que `ProfParle.mp4` tourne, réutiliser la lecture en cours sans créer de seconde instance ;
3. dès que le moteur de parole signale la fin effective de la parole / plus aucun audio Sherpa en cours, **stopper immédiatement `ProfParle.mp4`** et restaurer `Prof.png` ;
4. si Pierre reparle après cet arrêt, relancer `ProfParle.mp4` depuis t=0 ;
5. si la parole est interrompue, annulée, mise en pause ou échoue, arrêter également `ProfParle.mp4` et revenir au fallback visuel ;
6. la vidéo reste muette ; seule la voix Sherpa/Piper est audible.

Conséquence : l’ancienne règle « laisser ProfParle aller jusqu’à sa fin naturelle après la fin de la voix » est **remplacée** par ce contrat piloté par l’état réel de `ProfessorSpeech`/Sherpa.


## GECKO-033 — RED TDD démarré sur branche isolée
Branche : `gecko-033-identity-prof-life`.
Le cycle RED ajoute uniquement les tests de contrat GECKO-033 et l’activation CI de cette branche. Les tests exigent les constantes assets, le mute Gecko, le cycle ProfParle ↔ parole réelle, l’identité du titre, le catalogue 100 phrases et les seuils du Prof ambiant avant toute implémentation.


GECKO-033 RED complémentaire : l’ordre `IntroGeckoGD.mp4` → `Gecko_Intro.mp4` est désormais verrouillé par test avant code.


## GECKO-033 — implémentation GREEN poussée, CI à confirmer
Le code GECKO-033 est branché sur `gecko-033-identity-prof-life` :
- callbacks réels début/fin de parole Pierre sur Piper/Sherpa et fallback Android ;
- `ProfParle.mp4` réservé à la parole, toujours muet, stoppé dès le silence et relancé depuis t=0 si Pierre reparle ; si une parole dépasse ~30 s, le clip repart tant que la voix est active ;
- `Prof_actions.mp4` reste l’animation action/idle et partage une seule vue vidéo avec arbitrage exclusif ;
- intro `IntroGeckoGD.mp4` → `Gecko_Intro.mp4`, skip interrompant la chaîne sans recréer la grille ;
- vidéos Gecko forcées muettes au niveau du player, indépendamment de FX ;
- `IconGeckoGD.png` affiché en médaillon rond superposé à gauche du titre et micro-animé par transform, sans participer au layout ;
- launcher Android adaptatif utilisant le blob exact de `IconGeckoGD.png` ;
- Prof ambiant : aide à 90 s d’inactivité, proposition de sauvegarde à 10 min ou 25 actions, catalogue de 100 banalités à 4–8 min avec anti-répétition ;
- version cible `0.10.9-dev`.

La mission reste en validation CI : aucune déclaration GREEN finale avant tests + APK + AAB.


<!-- GECKO-033-PHONE-FEEDBACK-INTRO-2026-09-26 -->
# GECKO-033 — RETOUR TÉLÉPHONE v0.10.9-dev : INTROS
**Retour Fab — 26/09/2026.**
**Statut : documentation uniquement. Aucun code ne doit être modifié avant la suite des tests téléphone de Fab.**

## Bug observé 1 — son de la première intro coupé
Dans l’APK v0.10.9-dev testée sur téléphone :
- `IntroGeckoGD.mp4` se lance mais son audio embarqué est coupé ;
- **Fab veut conserver le son de cette première intro**.

### Nouvelle règle audio prioritaire
La règle générale « vidéos Gecko muettes » reçoit l’exception explicite suivante :
- `IntroGeckoGD.mp4` = **SON EMBARQUÉ AUTORISÉ / À CONSERVER** lorsque FX/audio est activé ;
- animations Gecko de gameplay = restent muettes ;
- `ProfParle.mp4` = reste toujours muet ;
- les musiques générales/victoire restent indépendantes.

Cette exception remplace toute formulation précédente qui rendait l’intro 1 systématiquement muette.

## Bug observé 2 — seconde intro absente
Dans le même test téléphone :
- la première intro apparaît ;
- **`Gecko_Intro.mp4` ne se lance plus ensuite**.

### Contrat à restaurer
Séquence obligatoire :
`IntroGeckoGD.mp4` (avec son) → `Gecko_Intro.mp4` → jeu déjà prêt derrière.

La fin naturelle de l’intro 1 doit déclencher l’intro 2.
Le bouton Skip reste accessible et doit quitter proprement la séquence d’intro sans recréer la grille.

## Gel temporaire
Fab poursuit ses tests de v0.10.9-dev.
**Ne rien corriger/coder pour l’instant** : attendre les autres observations afin de regrouper proprement le prochain cycle RED → correction.


<!-- GECKO-033-PHONE-FEEDBACK-ICON-PROF-ARBITRATION-2026-09-26 -->
# GECKO-033 — RETOUR TÉLÉPHONE : ICÔNES VALIDÉES + CONFLIT PROF/ANIMATION À AUDITER
**Retour Fab — 26/09/2026.**
**Statut : documentation uniquement. Aucun code maintenant.**

## A. Missions icône — VALIDÉES SUR TÉLÉPHONE
Les deux objectifs icône de GECKO-033 sont considérés **accomplis et validés visuellement par Fab** :
1. **icône launcher Android** dérivée de `assets/gecko/IconGeckoGD.png` : validée ;
2. **icône/médaillon dans l’application**, à gauche du titre GeckoDoku, avec son habillage rond et sa micro-animation indépendante : validée.

Ces deux points sortent donc de la liste des anomalies à corriger. Ne pas les retoucher pendant le prochain correctif intro/Prof sauf nécessité démontrée, afin d’éviter une régression visuelle.

## B. Nouveau point d’enquête — Prof et animation semblent se couper
Observation téléphone :
- un comportement d’interruption est visible entre le Prof et une animation ;
- à ce stade, **ne pas conclure lequel interrompt lequel** ;
- hypothèses à vérifier plus tard :
  - `Prof_actions.mp4` interrompt une animation/introduction en cours ;
  - une animation/introduction interrompt l’état visuel du Prof ;
  - le passage vers `ProfParle.mp4` prend la priorité et stoppe `Prof_actions.mp4` ;
  - un host/player partagé provoque un arrêt involontaire lors d’un changement de média.

### Règle d’investigation
Au prochain cycle RED/correction :
- tracer les transitions `INTRO`, `PROF_ACTION`, `PROF_SPEECH`, `GECKO_ACTION` ;
- journaliser qui demande `play()`, `stop()` et à quel instant ;
- vérifier les priorités et la propriété du player ;
- ne pas modifier l’arbitrage avant d’avoir identifié **qui stoppe qui** ;
- préserver le contrat déjà validé : Pierre doit afficher `ProfParle.mp4` uniquement pendant sa parole réelle, et `Prof_actions.mp4` reste séparé.

## C. État de test
Fab continue la validation téléphone de v0.10.9-dev.
Le prochain correctif devra regrouper :
- audio de l’intro 1 à restaurer ;
- intro 2 à restaurer ;
- conflit Prof/animation à diagnostiquer ;
tout en préservant les deux missions icône déjà validées.


<!-- GECKO-033-PHONE-FEEDBACK-GRID-GECKO-ANIM-2026-09-26 -->
# GECKO-033 — RETOUR TÉLÉPHONE : RÉGRESSION ANIMATIONS GECKO SUR LA GRILLE
**Retour Fab — 26/09/2026.**
**Statut : documentation uniquement. Aucun code maintenant.**

## Symptôme observé
Dans v0.10.9-dev, les animations Gecko associées aux cases de la grille ont disparu / ne se déclenchent plus comme avant.

Ce n’était **pas** la demande de Fab.

La demande initiale était uniquement :
> conserver les animations Gecko, mais couper leur son embarqué.

## Contrat à restaurer
Les animations visuelles Gecko de gameplay doivent être **remises exactement dans leur fonctionnement visuel antérieur**, notamment :
- apparition d’un Gecko sur une case ;
- disparition/retrait d’un Gecko ;
- actions longues / petites animations Gecko déjà prévues sur la grille ;
- masque/cadrage/couleur de fond de case déjà validés ;
- déclenchements et probabilités déjà validés avant GECKO-033.

### Audio
Pour ces animations Gecko de gameplay :
- **vidéo visible : OUI** ;
- **animation complète : OUI** ;
- **audio embarqué de la vidéo : NON / toujours muet** ;
- ne pas supprimer, sauter ou remplacer l’animation pour obtenir le silence ;
- le mute doit être appliqué au player/audio uniquement.

### À ne pas confondre avec les intros
- `IntroGeckoGD.mp4` : exception, son embarqué à conserver lorsque l’audio est actif ;
- `Gecko_Intro.mp4` : doit revenir en seconde intro selon son contrat ;
- animations Gecko de grille : visibles et animées, mais muettes.

## Vigilance de régression
Le prochain correctif doit vérifier séparément :
1. média Gecko bien lancé ;
2. rendu vidéo bien visible au-dessus de la bonne case ;
3. piste audio effectivement à zéro ;
4. aucune interaction du mute avec `play()`, `onStarted`, `onCompletion` ou l’enchaînement des animations ;
5. aucun impact sur la géométrie de la grille.

**Ne pas coder avant la fin de la série de tests téléphone de Fab.**


<!-- GECKO-033-MEDIA-TRACE-PROF-AFTER-INTRO1-2026-09-26 -->
# GECKO-033 — DIAGNOSTIC MÉDIA + CHRONOLOGIE PROF / INTRO
**Décision Fab — 26/09/2026.**
Ce cycle ajoute uniquement des **logs de diagnostic média**. Il ne corrige pas encore les régressions observées.

## Chronologie cible à respecter au prochain correctif
1. `IntroGeckoGD.mp4` démarre en premier.
2. Pendant toute cette première intro, **le Prof ne doit pas apparaître** et aucune animation Prof ne doit se lancer.
3. `IntroGeckoGD.mp4` doit conserver **son audio embarqué** lorsque l’audio est activé.
4. À la transition vers la seconde intro, l’audio de l’intro 1 doit être terminé proprement et ne pas déborder.
5. Le Prof devient éligible **seulement après la fin de la première intro**, donc à partir du début de la seconde partie de l’intro / suite de l’expérience.
6. `Gecko_Intro.mp4` doit bien se lancer après `IntroGeckoGD.mp4`.
7. Si `Gecko_Intro.mp4` possède son propre audio, il doit rester indépendant de l’audio de l’intro 1 : aucun chevauchement involontaire.

## Logs à conserver même si la cause paraît trouvée
Tag Logcat unique : `GeckoDokuMediaTrace`.

Tracer :
- `PLAY_REQUEST`, `START`, `STOP`, `COMPLETE`, `ERROR`, `EXCEPTION`, `RELEASE` ;
- `SET_MUTED` ;
- acceptation overlay `PLAY_ACCEPT` ;
- refus overlay `PLAY_REJECT_BUSY` avec média déjà actif ;
- étape intro demandée/acceptée/refusée/terminée ;
- animation Gecko de case acceptée/refusée/skippée ;
- demande `Prof_actions.mp4` ;
- passage parole Pierre active/inactive ;
- demande `ProfParle.mp4` ;
- arrêt global du player Prof.

But : reconstruire exactement **qui lance quoi, qui stoppe quoi et quel média bloque lequel**.

Aucune correction fonctionnelle n’est incluse dans ce cycle de logs.


<!-- GECKO-033-PHONE-FEEDBACK-ANIM-OFF-UNEXPECTED-2026-09-26 -->
# GECKO-033 — RETOUR TÉLÉPHONE : HABILLAGE ANIMÉ PASSÉ SUR OFF SANS CAUSE IDENTIFIÉE
**Retour Fab — 26/09/2026.**
**Statut : documentation uniquement. Aucun code maintenant.**

## Symptôme
Pendant le test téléphone de v0.10.9-dev / branche GECKO-033, le bouton **Anim.** s’est retrouvé sur **OFF** sans que Fab ait identifié l’action qui l’a provoqué.

À ce stade, **ne pas affirmer la cause**.

Hypothèses à auditer :
- état `RichMediaSettings.enabled` persisté d’un précédent lancement ;
- valeur par défaut incorrecte au premier lancement / après mise à jour ;
- appui accidentel non remarqué ;
- code qui écrit `enabled = false` lors d’une erreur média, d’un conflit ou d’un cycle pause/reprise ;
- restauration d’état Android / SharedPreferences après crash ou redémarrage ;
- autre conflit indirect lié aux players vidéo.

## Contrat attendu
- **Anim. doit être ON par défaut** sur une installation / configuration neuve, sauf choix explicite de l’utilisateur ;
- une erreur de lecture vidéo, un conflit de player ou un média manquant **ne doit jamais désactiver globalement Anim.** ;
- `Anim. OFF` doit résulter uniquement d’une action utilisateur explicite ou d’un réglage persistant déjà choisi par lui ;
- le bouton doit toujours refléter l’état réel de `RichMediaSettings.enabled`.

## Diagnostic demandé
Au prochain audit :
1. tracer chaque écriture de `RichMediaSettings.enabled` avec ancienne/nouvelle valeur et origine ;
2. tracer la valeur chargée au démarrage ;
3. vérifier la valeur par défaut en absence de préférence enregistrée ;
4. vérifier pause/reprise, crash/restart, rotation/recréation Activity et mise à jour APK ;
5. confirmer qu’aucun chemin d’erreur média n’écrit `enabled = false`.

Ne pas corriger avant la fin de la série de tests téléphone de Fab.
<!-- GECKO-033-AUDIT-PROF-SPEECH-GECKO-FLOW-2026-09-26 -->
# GECKO-033 — AUDIT PROF / PAROLE / FLUX VIDÉO GECKO — SANS CODE
**Demandeur / date :** Fab, 26/09/2026  
**Statut :** audit statique approfondi uniquement. Aucun correctif runtime dans ce cycle.

## Retour téléphone affiné
Fab précise que l'animation Gecko sur sa case **finit bien par apparaître**. Il faut donc distinguer :
- `Gecko_apparition.mp4` ;
- `Gecko_disparition.mp4` ;
- `Gecko_actions_plusieurs.mp4` lancé éventuellement après l'apparition ;
- le flux vidéo Prof, séparé du flux overlay Gecko.

## Déduction importante : pas de file d'attente / retry overlay
`playCellAnimation()` refuse immédiatement la vidéo si `richMediaOverlay.isBusy`.  
`RichMediaOverlayView.play()` refuse également si `isBusy`.  
**Aucun mécanisme de queue/retry différé n'existe.**

Donc, si l'animation Gecko finit par être visible :
- elle n'a pas été « remise plus tard » après un conflit busy ;
- elle a été acceptée lors de sa demande ;
- le délai visible peut venir de la préparation asynchrone du `MediaPlayer` / SurfaceTexture avant les premières frames.

Cela affaiblit fortement l'hypothèse « une autre vidéo remplace puis rend plus tard l'animation Gecko ».

## Les trois chemins Gecko ne sont pas équivalents
### 1. Apparition
Après un Gecko confirmé par le joueur :
`handlePlayerGeckoConfirmed()`
→ `playCellAnimation(GECKO_APPEARANCE)`
→ à la FIN naturelle seulement : `maybePlayGeckoLongAction(cell)`.

Donc une seconde animation longue peut éventuellement suivre l'apparition.

### 2. Disparition
Après retrait manuel :
`handleDoubleTap()`
→ `clearProfessorSession()`
→ `professorSpeech.stop()`
→ `engine.toggleGecko()`
→ si GECKO_REMOVED : `playCellAnimation(GECKO_DISAPPEARANCE)`.

**Aucune action longue n'est chaînée après la disparition.**

### 3. Action longue Gecko
`Gecko_actions_plusieurs.mp4` n'est éligible qu'après la FIN de l'apparition d'un Gecko confirmé par le joueur, avec politique actuelle 45 % et cooldown 45 s.
Elle n'est pas appelée après une disparition.
Elle n'est pas appelée par le chemin `applyProfessorHint()`.

## Corrélation très forte avec la coupure de Pierre sur action joueur
Les taps manuels commencent par `clearProfessorSession()`.  
Celui-ci ferme la bulle via `closeProfessorBubble()`, qui appelle explicitement `professorSpeech.stop()`.

Chronologie possible et cohérente avec le retour Fab :
**Pierre parle → action joueur → professorSpeech.stop() → mutation grille → préparation vidéo asynchrone → animation apparition/disparition visible un peu après.**

Le fait que la vidéo arrive « après » renforce donc cette piste : la coupure de Pierre peut précéder volontairement le démarrage visuel, donnant l'impression que la vidéo l'a coupé alors que l'ordre logique est inverse.

## Cas Prof qui pose lui-même un Gecko
`applyProfessorHint()` :
1. `showProfessorBubble(message)` → `professorSpeech.speak(message)` ;
2. application logique ;
3. si une cellule Gecko est posée : `playCellAnimation(GECKO_APPEARANCE)`.

Ici, `playCellAnimation()` n'appelle pas `professorSpeech.stop()`.  
La vidéo Gecko est muette et utilise `richMediaOverlay`, distinct du `professorVideo`.

Si Pierre se coupe dans CE cas précis, il faut chercher :
- une deuxième demande `ProfessorSpeech.speak()` qui préempte la première ;
- une fermeture de bulle / `clearProfessorSession()` venant d'un autre événement ;
- pause/lifecycle ;
- ou comportement Android/OEM non visible dans le code statique.

## Prof : conflit confirmé mais VISUEL
`Prof_actions.mp4` et `ProfParle.mp4` partagent le même `professorVideo: ChromaKeyVideoView`.
Le passage ACTION → SPEECH appelle `stopPlayback()` puis démarre `ProfParle.mp4`.
C'est un arbitrage vidéo Prof volontaire.

Pierre, lui, sort par :
`ProfessorSpeech` → `PierrePiperSpeechEngine` → `VoicePcmPlayer` → `AudioTrack`.

Donc la piste « même canal son » n'est pas confirmée : le code montre surtout un **lecteur vidéo Prof partagé**, alors que la voix Pierre possède son lecteur PCM séparé.

## Autre source réelle de coupure : nouvelle parole
Chaque `ProfessorSpeech.speak()` :
- incrémente une génération ;
- stoppe Piper courant ;
- stoppe le fallback Android courant ;
- puis lance la nouvelle phrase.

Le fallback Android utilise `TextToSpeech.QUEUE_FLUSH`.
Donc une nouvelle demande de parole remplace explicitement la précédente.

Sources possibles : nouveau texte Prof, encouragement Pierre, banalité/aide ambiante, annonce stats.

## Matrice de test téléphone à conserver
1. Pierre parle → retrait manuel d'un Gecko → noter si la parole coupe AVANT `Gecko_disparition`.
2. Pierre parle → confirmation manuelle d'un Gecko → noter si la parole coupe AVANT `Gecko_apparition`.
3. Laisser `Gecko_apparition` finir → vérifier si `Gecko_actions_plusieurs` suit parfois ; ne pas confondre les deux.
4. Prof pose lui-même un Gecko → vérifier si Pierre continue pendant `Gecko_apparition`.
5. Prof parle → nouvelle pression Prof avant fin de phrase → vérifier si la nouvelle phrase préempte l'ancienne.
6. `Prof_actions.mp4` tourne → Pierre commence → vérifier remplacement VISUEL par `ProfParle.mp4`, sans coupure sonore.
7. Relever `GeckoDokuMediaTrace` autour de ces scénarios.

## Audit suivant avant correction
Ajouter si nécessaire une trace dédiée à la voix :
- `SPEAK_REQUEST(origin=...)`
- `SPEAK_STARTED`
- `SPEAK_STOP(reason=...)`
- `SPEAK_COMPLETED`
afin de corréler exactement la coupure audio avec les événements vidéo.

**Aucun code correctif avant nouvel ordre de Fab.**

<!-- GECKO-033-AUDIT-PRIORITY-ARBITRATION-2026-09-26 -->
## GECKO-033 — PRIORITÉ ABSOLUE À LA PAROLE DE PIERRE / ARBITRAGE MÉDIA — SANS CODE

### Contrat voix validé par Fab
Quand Pierre a commencé une phrase, sa voix doit aller jusqu'à sa fin naturelle.

Une parole Pierre ne peut être interrompue volontairement que par :
- une nouvelle pression explicite du joueur sur le bouton Prof ;
- la transition / l'encouragement final de fin de partie ;
- les arrêts techniques explicites indispensables : FX coupé par l'utilisateur ou Activity réellement quittée/pausée/détruite.

Une action normale sur la grille, une apparition/disparition Gecko, une animation longue, une banalité ambiante ou une annonce de statistiques ne doivent jamais appeler indirectement ou directement `professorSpeech.stop()` pendant la phrase.

### Correctif architectural à préparer
Séparer obligatoirement :
1. fermeture/nettoyage visuel de la bulle Prof ;
2. nettoyage de l'état pédagogique/hypothèse ;
3. interruption volontaire de la voix.

Aujourd'hui `clearProfessorSession()` appelle `closeProfessorBubble()`, qui appelle `professorSpeech.stop()`. Cette chaîne viole le nouveau contrat lorsqu'elle est déclenchée par tap/double-tap/appui long.

`ProfessorSpeech.speak()` préempte aussi toute phrase déjà active. Prévoir une politique par origine :
- PROF_BUTTON : remplacement autorisé ;
- END_GAME : transition autorisée ;
- AMBIENT / SMALL_TALK / STATS / encouragement non final : aucune préemption ; ignorer ou différer jusqu'à ce que Pierre soit libre.

### Piste Android audio
Conserver l'hypothèse Fab, mais ne pas « désactiver une propriété native » à l'aveugle : les lecteurs audités n'appellent actuellement aucun `requestAudioFocus()`. La coupure observée est donc d'abord à chercher dans nos `stop()` explicites et dans l'arbitrage des médias.

### Cause supplémentaire très crédible : Prof vidéo actif pendant l'intro
Au démarrage :
- l'idle Prof est planifié AVANT l'intro ;
- il devient éligible après ~2–3 s ;
- `runProfessorIdleAnimation()` ne bloque pas lorsque `richMediaOverlay.isBusy` ;
- `playProfessorButtonVideo()` peut donc lancer `Prof_actions.mp4` pendant `IntroGeckoGD.mp4`.

Prof et intro utilisent deux `ChromaKeyVideoView` distincts, donc deux `MediaPlayer` + deux `GLSurfaceView`. Chaque vue utilise `setZOrderOnTop(true)`. Deux surfaces « on top » simultanées peuvent provoquer un ordre visuel fragile et une concurrence de décodage.

Comme `Prof_actions.mp4` dure ~30 s, une animation Prof démarrée 2–3 s après une intro 1 d'environ 30 s peut encore être active quand l'intro 2 démarre. Cette chronologie correspond aux symptômes Prof trop tôt + intro 2 masquée/perturbée.

### Contrat intro à imposer
Pendant toute la séquence Intro 1 + Intro 2 :
- verrou exclusif `INTRO_ACTIVE` ;
- aucun Prof_actions, ProfParle, Gecko vidéo de case ou longue ;
- aucune parole ambiante/stats ;
- Prof visuellement inactif jusqu'à la fin de la séquence.

Les deux intros sont déjà séquencées sur le même `richMediaOverlay` : Intro 2 n'est appelée qu'après le callback de fin d'Intro 1, donc elle n'est pas volontairement lancée en arrière-plan par `IntroSequencePolicy`.

Attention : le bouton × appelle actuellement `stop()` sans callback de completion. Sauter Intro 1 arrête donc toute la chaîne au lieu de lancer Intro 2. Décider explicitement si × signifie « passer cette intro » ou « passer toutes les intros ».

### Arbitrage vidéo futur
Priorités proposées :
1. INTRO exclusive ;
2. ProfParle pendant Pierre ;
3. apparition/disparition de case ;
4. Prof_actions / Gecko_actions seulement quand tout est libre.

Si Pierre parle et qu'une animation de case est demandée : état logique immédiat, animation décorative différée si nécessaire, mais jamais de `professorSpeech.stop()` pour faire place à la vidéo.

### Traces avant correction
Ajouter :
- `SPEAK_REQUEST(origin, replaceAllowed)`
- `SPEAK_STARTED(origin)`
- `SPEAK_STOP(reason, caller)`
- `SPEAK_COMPLETED(origin)`
- état arbitre média `INTRO_ACTIVE / PROF_SPEECH_VIDEO / PROF_ACTION / CELL / LONG_ACTION`.

**Aucun correctif runtime avant nouvel ordre explicite de Fab.**



<!-- GECKO-033-COEXISTENCE-RED-2026-09-26 -->
## GECKO-033 — CONTRAT PRIORITAIRE COEXISTENCE RÉELLE
Le nouvel ordre Fab remplace explicitement l’ancienne piste documentaire « arbitre une seule vidéo / différer Gecko ». Cette piste est **annulée**.

Contrat prioritaire :
- une animation ne coupe pas Pierre ;
- Pierre ne verrouille pas globalement les animations ;
- `ProfParle.mp4` suit uniquement l’état réel de parole ;
- Gecko apparition/disparition/action longue peut coexister avec Pierre/ProfParle ;
- plusieurs lecteurs/surfaces doivent être indépendants, sans stop croisé ;
- `clearProfessorSession()` doit nettoyer l’état pédagogique sans arrêter la voix ;
- `closeProfessorBubble()` masque la bulle sans arrêter Pierre ;
- origines parole : PROF_BUTTON / AMBIENT / STATS / END_GAME ;
- PROF_BUTTON peut remplacer ; AMBIENT/STATS ne préemptent pas ;
- Intro 1 audible si FX ON ; Intro 1 → Intro 2 séquentiel ;
- Prof non éligible pendant Intro 1, mais l’architecture reste concurrente hors cette règle narrative ;
- le × est explicite : skip de la séquence d’intro entière, conformément au contrat d’accessibilité historique.
