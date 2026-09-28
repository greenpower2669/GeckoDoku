# GECKODOKU — ORDRE DE MISSION ACTIF

## GECKO-046 — REPÈRES LOGIQUES, CROIX 3 ÉTATS, VICTOIRE VIVANTE ET BROUILLARD

Date : 2026-09-28  
Branche : `gecko-039-sudoku-tap-gecko-gomoku`

## ÉTAT VALIDÉ / AU VERT

GECKO-045 est considéré comme **validé par Fab**.

Ne pas régresser :

- fenêtre carrée Abeilles & Geckos ;
- navigation interne ;
- zoom / drag ;
- Abeilles réduites à 50 % ;
- keycolor VERT ;
- ailes bleues conservées ;
- plateau hexagonal ;
- zones / axes / solveur / générateur ;
- pédagogie du Prof ;
- Classic / Sudoku / Gomoku ;
- sauvegardes / stats / Hall / export-import.

La nouvelle mission porte sur les **repères visuels**, la **victoire animée** et le remplacement du **cercle noir de départage**.

---

# A — DOUBLE CLIC : PALETTE DE REPÈRES LOGIQUES

## 1 — DOUBLE CLIC CONTEXTUEL

Dans GeckoBeeDoku / Abeilles & Geckos, le double clic doit ouvrir une palette logique plus riche.

Le double clic doit permettre de choisir :

- placer / confirmer un Gecko ;
- placer / confirmer une Abeille ;
- poser un repère Gecko ;
- poser un repère Abeille ;
- poser une barre d'exclusion d'axe ;
- choisir / modifier une croix logique ;
- retirer un repère.

Ne pas supprimer les fonctions déjà accessibles par double clic : les intégrer à cette palette.

---

## 2 — REPÈRE GECKO

Créer un repère visuel Gecko :

- couleur dominante **VERTE** ;
- symbole Gecko ou marque claire ;
- léger décalage par rapport au centre / axe de la case pour ne pas masquer la pièce ;
- lisible même sans couleur grâce à une forme / symbole distinct.

Ce repère signifie :

> « ici je soupçonne / projette un Gecko »

Il ne valide pas automatiquement la solution.

---

## 3 — REPÈRE ABEILLE

Créer un repère visuel Abeille :

- couleur dominante **JAUNE** ;
- symbole Abeille ou marque claire ;
- légèrement excentré de l'axe / centre pour rester lisible ;
- distinct du repère Gecko même en vision dégradée.

Ce repère signifie :

> « ici je soupçonne / projette une Abeille »

Il ne valide pas automatiquement la solution.

---

## 4 — BARRES D'EXCLUSION D'AXE

Ajouter des **barres d'exclusion** utilisables comme repères.

Pour une case hexagonale, elles doivent pouvoir représenter les familles d'axes logiques :

- axe ↖↘ ;
- axe ↑↓ ;
- axe ↗↙.

Une barre d'exclusion signifie :

> « cet axe / cette direction est éliminé(e) pour ce raisonnement »

Les barres doivent être légèrement décalées / superposées sans masquer la case.

Prévoir une représentation visuelle distincte :

- trait / barre ;
- orientation correspondant réellement à l'axe ;
- contraste suffisant ;
- pas uniquement la couleur.

---

# B — CROIX À 3 ÉTATS

## 5 — TROIS ÉTATS DE CROIX

Les croix personnelles ne doivent plus être un simple état binaire.

Créer trois états :

### JAUNE
Hypothèse / doute / piste en cours.

### VERT
Déduction validée / exclusion confirmée par le raisonnement.

### ROUGE
Impossible / contradiction / exclusion forte.

Le joueur doit pouvoir faire évoluer une croix entre ces états.

Exemple de cycle possible :

`aucune → jaune → vert → rouge → aucune`

Le cycle exact peut être ajusté pour rester ergonomique.

---

## 6 — ACCESSIBILITÉ DES CROIX

Ne pas dépendre uniquement de la couleur.

Chaque état doit aussi avoir une différence perceptible par :

- épaisseur ;
- motif ;
- petit symbole ;
- style de trait ;
- animation légère éventuelle.

---

# C — PROF GECKO ET SOLVEUR : UTILISER LES MÊMES REPÈRES

## 7 — SOURCE UNIQUE DE LANGAGE VISUEL

Le Prof et le joueur doivent partager le même vocabulaire graphique.

Quand le Prof explique :

- un Gecko possible → repère Gecko vert ;
- une Abeille possible → repère Abeille jaune ;
- un axe impossible → barre d'exclusion orientée ;
- une hypothèse → croix jaune / marque hypothèse ;
- une déduction sûre → croix / contour vert ;
- une contradiction → croix rouge.

Le solveur doit produire les informations nécessaires au rendu.

Ne pas créer un système graphique séparé pour le Prof.

---

## 8 — EXPLICATION SYNCHRONISÉE

Quand le Prof dit par exemple :

> « Cet axe contient déjà un Gecko »

le plateau doit afficher en même temps :

- l'axe concerné ;
- la barre d'exclusion correspondante ;
- le Gecko déjà responsable de l'exclusion.

Quand il dit :

> « Il ne reste que cette Abeille »

afficher :

- candidats précédents ;
- exclusions ;
- repère Abeille jaune sur la conclusion ;
- éventuellement repère Gecko vert si le couple est déduit.

Le texte et les repères doivent raconter exactement le même raisonnement.

---

# D — VICTOIRE : MUSIQUE CLASSIC + TOUT LE MONDE S'ANIME

## 9 — MUSIQUE DE VICTOIRE

À la résolution d'une grille GeckoBeeDoku / Abeilles & Geckos :

- utiliser **la même musique de victoire que le mode Classic** ;
- ne pas créer une musique concurrente si celle du Classic existe déjà ;
- conserver le comportement audio déjà validé.

---

## 10 — ANIMATION DE TOUS LES GECKOS ET ABEILLES

À la victoire :

- activer / animer **tous les Geckos** de la grille ;
- activer / animer **toutes les Abeilles** de la grille ;
- pas uniquement la dernière pièce posée.

L'effet doit donner une vraie sensation de plateau vivant.

### Contraintes

- rester dans le viewport carré ;
- respecter le clipping ;
- conserver les positions logiques ;
- éviter de masquer tout le plateau ;
- ne pas lancer des dizaines de vidéos lourdes simultanément si cela détruit les performances.

Si nécessaire, utiliser :

- une animation légère par sprite ;
- des décalages de phase ;
- une vague / séquence rapide ;
- ou un système de batch.

L'objectif visuel prime, mais l'APK doit rester fluide.

---

# E — CLASSIC + GECKOBEEDOKU : REMPLACER LE CERCLE NOIR PAR DU BROUILLARD

## 11 — CERCLE NOIR DE DÉPARTAGE

Dans Classic et GeckoBeeDoku, le système actuel utilise un **cercle noir** pour signaler le choix / départage de la grille.

Fab préfère une représentation plus organique.

Le cercle noir doit être remplacé par :

### un nuage de fumée / brouillard

Caractéristiques :

- transparent ;
- doux ;
- animé ;
- diffus ;
- pas opaque ;
- pas agressif ;
- évoque un petit brouillard qui flotte sur la zone concernée.

---

## 12 — BROUILLARD = INDICATEUR, PAS MASQUE

Le brouillard ne doit jamais empêcher la lecture de la grille.

Il doit :

- rester semi-transparent ;
- laisser visibles couleurs, chiffres / pièces et contours ;
- avoir un mouvement lent / vivant ;
- être attaché à la zone logique concernée ;
- suivre correctement zoom / déplacement quand le mode est zoomable.

---

## 13 — MÊME MÉTAPHORE DANS CLASSIC ET GECKOBEEDOKU

Le même concept visuel doit être utilisé dans les deux modes :

- Classic ;
- GeckoBeeDoku / Abeilles & Geckos.

Éviter deux systèmes différents pour la même notion de départage.

---

# F — PERSISTANCE

## 14 — REPÈRES JOUEUR

Les nouveaux repères doivent être sauvegardés avec la partie :

- repère Gecko ;
- repère Abeille ;
- barres d'axe ;
- croix jaune / verte / rouge.

Fermer / rouvrir l'application doit les restaurer.

L'export / import doit aussi les conserver.

---

# G — TESTS

## 15 — TESTS OBLIGATOIRES

Ajouter des tests pour :

- cycle croix 3 états ;
- persistance des trois états ;
- repère Gecko ;
- repère Abeille ;
- barres d'axe ;
- orientation correcte des trois axes ;
- Prof utilisant les mêmes structures de repères ;
- sauvegarde / reprise ;
- victoire : déclenchement musique Classic ;
- victoire : animation de tous les Geckos ;
- victoire : animation de toutes les Abeilles ;
- brouillard présent à la place du cercle noir ;
- brouillard semi-transparent ;
- non-régression Classic / Sudoku / Gomoku / GeckoBeeDoku.

---

# H — CRITÈRES DE VALIDATION TÉLÉPHONE

La mission est terminée seulement si Fab valide :

- [ ] double clic ouvre la palette logique complète ;
- [ ] repère Gecko vert lisible ;
- [ ] repère Abeille jaune lisible et légèrement excentré ;
- [ ] barres d'exclusion d'axe faciles à comprendre ;
- [ ] croix jaune / verte / rouge pratiques ;
- [ ] Prof utilise réellement ces repères dans ses explications ;
- [ ] musique de victoire Classic dans GeckoBeeDoku ;
- [ ] tous les Geckos s'animent à la victoire ;
- [ ] toutes les Abeilles s'animent à la victoire ;
- [ ] le cercle noir a disparu ;
- [ ] brouillard transparent animé agréable ;
- [ ] brouillard utilisé dans Classic ET GeckoBeeDoku ;
- [ ] repères sauvegardés ;
- [ ] aucune régression.

---

# PRINCIPE DIRECTEUR

Les repères du joueur et ceux du Prof doivent parler **le même langage visuel**.

Le joueur doit pouvoir poser exactement les mêmes types d'indices que ceux qu'il voit dans une démonstration du Prof.

Et lors de la victoire :

**tout le plateau prend vie.**
---

## GECKO-046 — implémentation 0.15.3-dev

Version cible : **0.15.3-dev** / versionCode **38**.

Implémenté dans la passe en cours :

- palette logique enrichie sur double clic ;
- repère Gecko vert ;
- repère Abeille jaune, décalé du centre ;
- barres d'exclusion pour les trois axes hexagonaux ;
- croix à trois états : jaune → vert → rouge → aucune ;
- le solveur/Prof produit les mêmes structures visuelles que le joueur ;
- sauvegarde des cross states et des nouveaux repères logiques ;
- migration de session Bee schema 2 → 3 ;
- musique de victoire Classic réutilisée pour GeckoBeeDoku ;
- animation de victoire légère sur toutes les pièces Gecko/Abeille ;
- animation de victoire ajoutée aux Geckos Classic ;
- cercle de départ remplacé par un brouillard animé transparent dans Classic et GeckoBeeDoku ;
- tests unitaires des nouveaux marqueurs / croix / brouillard.

Ne pas déclarer la mission terminée avant CI GREEN et test téléphone Fab.
