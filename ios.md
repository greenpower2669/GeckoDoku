# GeckoDoku iOS / iPadOS 🍎🦎

## Statut

Document de réflexion uniquement.

Aucun portage iOS n'est commencé pour le moment et aucune modification du code Android n'est demandée par ce document.

## Faisabilité

Oui, GeckoDoku peut avoir une version iPhone et iPad.

Le projet actuel est une application Android native écrite en Kotlin. Une version iOS ne peut donc pas être obtenue en recompilant simplement le projet Android tel quel.

En revanche, une partie importante de GeckoDoku est constituée de logique métier séparée de l'interface Android, notamment :

- moteur du jeu ;
- génération des grilles ;
- solveur humain ;
- Gecko X-Wing ;
- hypothèses par contradiction ;
- calcul et classement de difficulté ;
- logique pédagogique du Professeur Gecko ;
- modèles de données et règles de validation.

Cette logique constitue une bonne base pour un portage Apple.

## Deux stratégies possibles

### 1. Application iOS séparée en Swift / SwiftUI

Créer un projet iOS spécifique, par exemple `GeckoDoku-iOS`, avec :

- interface en SwiftUI ;
- moteur de jeu porté depuis Kotlin ;
- affichage natif iPhone/iPad ;
- audio et vidéo adaptés avec les API Apple ;
- gestion des sauvegardes locales iOS ;
- adaptation de Professeur Gecko à l'environnement Apple.

Avantage : application Apple native et propre.

Inconvénient : le moteur existerait en deux versions et certaines corrections devraient être reportées sur Android et iOS.

### 2. Kotlin Multiplatform

Faire évoluer progressivement l'architecture afin de partager le cœur du jeu entre Android et iOS.

Le principe serait :

- garder l'application Android existante ;
- extraire progressivement le moteur commun dans un module Kotlin Multiplatform ;
- conserver une interface Android native ;
- créer une interface iOS en SwiftUI ;
- faire utiliser aux deux applications le même moteur de génération et de résolution.

Cette solution permettrait, à terme, qu'une correction du solveur ou du générateur bénéficie aux deux plateformes.

C'est probablement la stratégie la plus intéressante à long terme pour GeckoDoku.

## Éléments réutilisables

Peuvent être réutilisés ou adaptés facilement :

- règles du jeu ;
- moteur logique ;
- générateur ;
- solveurs ;
- système de difficulté ;
- textes du Professeur Gecko ;
- images ;
- icônes ;
- sons ;
- musiques ;
- vidéos ;
- données de grilles ;
- principes de statistiques et journal.

## Éléments spécifiques Android à remplacer sur iOS

Certaines parties actuelles reposent directement sur Android et devront avoir un équivalent Apple :

- `MainActivity` ;
- vues Android ;
- gestion des insets et barres système ;
- lecture audio Android ;
- lecture vidéo Android ;
- chroma-key vidéo ;
- overlays et vues personnalisées ;
- moteur de voix / lecture PCM ;
- stockage Android ;
- permissions et cycle de vie Android.

Côté Apple, les équivalents seraient notamment basés sur :

- SwiftUI ;
- AVFoundation ;
- APIs audio iOS ;
- stockage local Apple ;
- cycle de vie SwiftUI/iOS.

## Tester sur iPhone ou iPad

Le MacBook Air permet de développer et tester une version iOS.

Le circuit de test serait :

1. installer Xcode sur le Mac ;
2. ouvrir le projet iOS ;
3. connecter un iPhone ou un iPad ;
4. sélectionner l'appareil dans Xcode ;
5. signer l'application avec l'identifiant Apple ;
6. compiler ;
7. installer directement l'application sur l'appareil pour les essais.

Pour des tests personnels sur ses propres appareils, Xcode permet de commencer sans distribution publique.

## Équivalents APK / AAB côté Apple

Android :

- **APK 🦎** : installation directe et tests ;
- **AAB 📦** : publication Google Play.

Apple :

- application iOS signée installée via Xcode pour les tests locaux ;
- **IPA 🍎** : paquet iOS utilisé dans certains circuits de distribution ;
- **TestFlight** : distribution bêta à des testeurs ;
- **App Store** : distribution publique.

## Proposition d'organisation Git

Au moment où le développement iOS commencera, plusieurs structures seront possibles.

### Option A — dépôt séparé

- `GeckoDoku` : Android ;
- `GeckoDoku-iOS` : iPhone/iPad.

### Option B — dépôt multiplateforme

Un seul dépôt regroupant :

- moteur partagé Kotlin Multiplatform ;
- application Android ;
- application iOS ;
- assets communs ;
- documentation commune.

Cette deuxième organisation devient particulièrement intéressante si le cœur logique doit rester strictement identique sur Android et iOS.

## Recommandation actuelle

Ne rien modifier maintenant dans l'application Android uniquement pour préparer iOS.

Lorsque la version Android sera suffisamment stabilisée, commencer par identifier précisément les classes de logique pures qui peuvent être extraites sans dépendance Android.

Ensuite, créer un prototype iOS minimal permettant de vérifier :

- génération d'une grille ;
- affichage ;
- interaction ;
- validation ;
- appel du Professeur Gecko ;
- sauvegarde locale.

Après validation de ce prototype, décider définitivement entre un portage Swift séparé et Kotlin Multiplatform.

## Règle actuelle

Ce fichier est un document de préparation.

Il n'autorise aucune modification du code Android ni aucun démarrage automatique du portage iOS.
