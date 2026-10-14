# MISSION ASTRA / SOL — GECKO-HOF-PLAYER-ISOLATION-002
**Date :** 12 octobre 2026
**Dépôt :** `greenpower2669/GeckoDoku`
**Branche de travail :** `feature/sudoku-visual-reasoning-v1` (chantier complémentaire séparé de l'UI Sudoku)
**Origine :** retour de Fab : la progression présentée avec le score d'un autre utilisateur semble regrouper les statistiques de plusieurs pseudonymes comme s'il s'agissait d'une seule personne.
**État :** signalement utilisateur, à reproduire ; cause non encore prouvée. Implémentation non commencée.
**Portée :** les **quatre modes** : Classique, Sudoku, Gomoku, Abeilles & Geckos.

## Principe produit incontournable
- **Classement global** : peut afficher des scores de plusieurs joueurs/pseudonymes et de tous les modes, selon le filtre choisi.
- **Progression et statistiques personnelles** : doivent concerner **uniquement le profil/joueur identifié** et, pour les statistiques propres à un mode, **uniquement le mode filtré** ; aucune victoire, série, partie, rang, historique ou courbe appartenant à un autre profil ne doit y être additionnée.
- **Un pseudo n'est pas une identité unique** : deux personnes peuvent employer le même nom visible. Inversement, un utilisateur peut renommer son pseudo sans devenir automatiquement un autre joueur : cela dépend de l'identité de profil, pas de la chaîne affichée.
- Ne **jamais** interpréter plusieurs pseudonymes visibles comme un seul et même joueur parce que leurs scores apparaissent dans le Hall, partagent le téléphone, une même synchronisation, le même mode ou le même cache.
- La progression personnelle ne doit pas être reconstruite en sommant tous les résultats du serveur public.

## Audit obligatoire (ne pas coder par supposition)
1. Lire le code et les tests courants de `main` : `PlayerStatsStore`, `HallOfFameStore`, `GlobalHallProjection`, `GlobalScoreCacheStore`, publication et consommation des scores, sélecteurs/statistiques/écrans de progression. Localiser les sources réelles, sans supposer les chemins.
2. Reproduire **la confusion observée** avec au moins deux profils de pseudonymes différents et plusieurs parties, dont une partie par mode ; inspecter les filtres, agrégations, groupements, identifiants et états d'écran.
3. Examiner le contrat `GET /sync`, `POST /scores` et le backend Hall global associé **si le dépôt/service serveur est accessible** ; identifier l'origine de la fusion (API, cache, projection, store, UI). Ne pas conclure que le serveur est fautif sans preuve.
4. Contrôler l'effet des réinstallations, appareils différents, renommages, doublons `runId`, synchronisation asynchrone et anciens scores possiblement dépourvus d'identité fiable.
5. Restituer le diagnostic dans `debughistorical.md` avec chemins/lignes/tests et preuves.

## Contrat d'identité et de progression
- Définir une clé de **profil joueur stable** (par exemple `playerProfileId`) pour attribuer les complétions quand le modèle réel ne le fournit pas déjà ; générer/attribuer un identifiant par profil et le conserver selon le cycle de vie produit, **sans l'assimiler au pseudo ni à un identifiant d'appareil**.
- Ne pas créer de compte obligatoire ni inventer de continuité multi-appareils. Si la continuité est impossible avec l'architecture actuelle, la documenter clairement et solliciter validation avant une migration de contrat API.
- Pour les vues personnelles : sélectionner par identifiant de profil fiable, puis par mode si nécessaire ; rejeter/exclure de ces agrégations les scores globaux sans rattachement prouvé au profil courant.
- Pour le Hall global : conserver le filtrage et l'affichage multi-joueurs, avec le pseudo associé à **chaque entrée**, le mode et les métadonnées adéquates, sans importer leurs résultats dans la progression personnelle.
- Les valeurs affichées en courbes, séries, records, taux de réussite, badges et progressions sont calculées **exclusivement** à partir de l'ensemble autorisé pour le profil et le périmètre affiché.
- Préserver l'idempotence existante : même `runId` et même JSON lors des retries, complétion locale/globale dédoublonnée ; ne pas confondre `runId`, `scoreId`, `playerProfileId`, `displayName` et `deviceId`.
- Aucune suppression/réattribution silencieuse des scores historiques ambigus : les entrées au propriétaire indéterminable peuvent rester visibles au Hall global, mais **n'alimentent aucune progression personnelle** tant que la preuve d'association manque. Toute migration doit être explicite, réversible/testée.
- Les données d'identité publiques doivent être minimisées : pseudonyme pour affichage, identifiant opaque quand strictement requis, jamais secret/authentification dans l'APK.

## Recette obligatoire — tous les modes
Préparer des jeux d'essai déterministes avec :
- Profil A « GeckoTêtu » : parties Classique et Sudoku.
- Profil B « Yann » : parties Gomoku et Abeilles & Geckos.
- Profil C distinct, ayant **le même pseudo visible** que A : partie Classique.
- Profil A renommé volontairement : ne fusionner/séparer qu'en fonction de l'identité réelle, pas du texte du pseudo.
- Changement de mode, retour écran, redémarrage, perte/rétablissement du réseau, réinstallation si la persistence le permet.
Vérifier :
1. Hall global : toutes les entrées légitimes visibles et filtrables, pseudo d'origine sur chaque score.
2. Profil A : aucune partie de B ni de C dans ses statistiques ou courbes.
3. Profil B : aucun score de A ni C.
4. Pseudonymes identiques A/C : comptes/progressions isolés.
5. Chaque mode ne compte que ses propres événements dans les statistiques filtrées ; éventuelle vue « tous modes » agrège **seulement les résultats du même profil**.
6. Aucune rétrogression des statistiques locales et aucun doublon après synchronisation / retry / ACK.
7. Comportement sûr sur l'historique global dont l'identité d'origine est inconnue.

## Couplage avec la mission Sudoku (GECKO-SUDOKU-VISUAL-001)
Les deux missions sont **distinctes**, même branche demandée : une concerne les annotations/contrôles Sudoku ; l'autre concerne la partition des données Hall/progression pour quatre modes. Produire des commits séparés et des tests ciblés ; aucune refonte accessibilité ne doit masquer ou dégrader la correction d'identité.

## Livraison FAB Copilot
- Implémenter après audit ; pousser **sur la branche dédiée uniquement**.
- Vérifier tests JVM/instrumentés pertinents et build Android `phone` conservant Pierre et les sprites 240p si possible, sans inventer de preuve d'APK.
- Mettre à jour les cinq mémoires vivantes ; `todo.md` doit lister tout travail inachevé.
- **Pas de merge `main`, pas de Release, pas de publication, pas de modification de signature / applicationId sans ordre explicite de Fab.**

**Critère de succès :** le Hall reste réellement **global**, mais aucune personne ne voit les statistiques/progressions de plusieurs profils mélangées comme si elles appartenaient au même joueur, et cela fonctionne pour les quatre modes.

## Mise en œuvre — Hall comparatif / progression personnelle (14/10/2026)

Formulation canonique validée par Fab :
- **Hall of Fame = comparatif global entre joueurs.**
- **Progression = analyse strictement personnelle d'une identité joueur.**

Implémentation :
- suppression du `take(10)` dans l'écran de niveau : tous les scores filtrés restent consultables ;
- suppression de la troncature locale `MAX_ENTRIES=200` du Hall local ;
- ajout de `playerProfileId` nullable à `HallOfFameEntry`, rétrocompatible avec l'historique ;
- `PlayerProfileStore` expose un `activeProgressionId` opaque et stable : UUID dédié pour le profil historique, UUID existant réutilisé pour les profils locaux supplémentaires ;
- publication des nouveaux scores via `metadata.playerProfileId`, sans compte, appareil, email ou secret ;
- lecture du même champ depuis le cache global ;
- si un serveur ancien omet metadata, une copie locale strictement identique peut hydrater l'identité uniquement lorsqu'elle est non ambiguë ;
- deux profils connus différents ne sont jamais dédupliqués ensemble, même avec pseudo, score et date identiques ;
- les scores historiques sans identité prouvée restent au classement mais sont exclus de toute sous-progression ;
- nouvelle projection `HallPlayerProgressionProjection` groupée exclusivement par `playerProfileId`, jamais par pseudo ;
- écran Hall : classement global complet + sous-progressions séparées par joueur ; renommage conservé comme une seule progression via l'identifiant stable ;
- bouton local renommé conceptuellement en « Ma progression • <pseudo actif> » ;
- Pierre utilise uniquement `statsStore` du profil actif pour ses statistiques, préfixe la narration par le pseudo actif, coupe toute parole précédente puis recrée son contexte vivant et son état ambiant lors d'un changement/création de profil ;
- aucune narration Pierre n'est lancée pour la sous-progression d'un autre joueur.

Tests ajoutés/étendus :
- même pseudo + profils distincts => progressions distinctes ;
- renommage + même id => progression continue ;
- historique sans id => classement seulement ;
- plus de 10 scores => aucune troncature de progression ;
- metadata globale => profile id conservé ;
- fallback local exact => hydratation prouvée ;
- profils connus différents avec score visuellement identique => jamais fusionnés.

Historique : les anciennes lignes sans provenance fiable gardent `playerProfileId=null`. Aucune réattribution par pseudo.


### Validation CI finale HOF isolation
Run 38526555276 / #510 : GREEN. Commit code validé 29151fc63bd9f8b05d5ba188276f6cfa197e5998. Tests JVM + assemblePhone réussis, Pierre et banque sprites 240p vérifiés. Artefact GeckoDoku-v0.15.45-dev-phone ID 11876601009. APK SHA-256 eec808012b45e21a36a400672b1c50d7b2db41a27df9310db2df30eccef45981.
Recette téléphone/réseau encore requise pour confirmer le retour de metadata.playerProfileId via le serveur réel. Aucun merge/release/publication.
