# GeckoDoku — TODO actif

Mission `GECKO-HOF-SYNC-001` sur `feature/gecko-hof-sync-v1`.

## HOF global v1

- [x] Contrat serveur publié.
- [x] Payload 4 modes + seed 64 bits chaîne.
- [x] pending crash-safe avant réseau.
- [x] ACK idempotent + retry/backoff/Retry-After.
- [x] `/sync` + cache + curseur transactionnel.
- [x] reprise lancement/retour réseau.
- [x] timestamp local/global unique.
- [x] Hall affiché fusionne désormais cache global + local et dédoublonne.
- [x] Test restauration après réinstallation ajouté en TDD.
- [x] APK `phone` complet avec Pierre + sprites 240p construit et vérifié.
- [x] run build `37526965060` GREEN.
- [x] SHA APK `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf`.
- [x] Téléphone : animations Gecko revenues (SpriteRGBA/banque 240p visibles dans le journal de validation).
- [ ] Téléphone : confirmer Pierre Piper revenu (plus de fallback `engine=android`).
- [x] Clean install online : Hall global restauré après désinstallation/réinstallation ; 3 résultats visibles.
- [ ] Partie online : une seule entrée serveur.
- [ ] Partie offline : pending conservé.
- [ ] Kill/redémarrage offline : pending survit.
- [ ] Retour réseau : envoi automatique.
- [ ] Retry/duplicate : une seule entrée globale.
- [ ] Vérifier au moins un score réel par mode éligible et ses catégories.
- [x] Ordre explicite Fab reçu : merge `main` + Release.

## Publication

- [x] Cible : `0.15.44-dev`, code 79.
- [x] Corriger le workflow Release qui référençait la Release supprimée `PackageSprites`.
- [x] Utiliser l’APK validé `phone-0.15.43-dev-run-403` comme source contrôlée de la banque 240p.
- [x] Merge `main` : `c1a32bf4736894f87ac45f1c626f72a111cf1c2e`.
- [x] Run Release GREEN : `37531494180` / #432.
- [x] APK publié : `GeckoDoku-v0.15.44-dev.apk`.
- [x] Prerelease promue en Release validée : `phone-0.15.44-dev-run-432`.
- [x] SHA-256 final : `555cce892d573aa5d5bd794b78254ce315e789726a474f149546c11e096d108c`.

Les tests offline/restart/retry/doublons exhaustifs restent en suivi produit ; Fab a autorisé la publication sans attendre leur répétition complète.

## Produit hors mission à préserver

Ne pas modifier pour ce lot : gameplay, étoiles, aides, statistiques personnelles, progression, gestes/géométrie, logique Prof, médias source.


## Nouvelle mission GECKO-SUDOKU-VISUAL-001 (12/10/2026)

- [x] Concept validé par Fab et ordre de mission poussé sur branche dédiée.
- [ ] Auditer l'UI réelle des candidats, des hypothèses, du pavé flottant et de Pierre.
- [ ] Implémenter pavé bas-centre et priorité d'affichage temporaire du Prof.
- [ ] Ajouter 4 cases à cocher indépendantes dans la validation Sudoku.
- [ ] Projeter les mêmes candidats par ligne dès 2 occurrences, gérer 1 à 9.
- [ ] Marquer visuellement paires/triplets valides sans élimination abusive ; pulsation lente facultative.
- [ ] Cercler/lier des candidats et permettre le tracé manuel X-Wing.
- [ ] Tests de logique, accessibilité, non-régression et build phone vérifié (Pierre + 240p).
- [ ] Test Samsung de Fab, corrections et mise à jour des 5 mémoires vivantes.
- [ ] Attendre l'autorisation expresse pour merge main / Release / publication.

Mission détaillée : `missions/GECKO-SUDOKU-VISUAL-001.md`. **Le code n'est pas encore réalisé.**


## GECKO-HOF-PLAYER-ISOLATION-002 — séparation des joueurs, tous modes (12/10/2026)

- [x] Enregistrer le signalement Fab : le Hall/progression paraît confondre plusieurs pseudonymes en un seul joueur.
- [x] Pousser la mission détaillée `missions/GECKO-HOF-PLAYER-ISOLATION-002.md` sur la branche dédiée.
- [ ] Reproduire et localiser le mélange (API / cache / projection / PlayerStatsStore / UI).
- [ ] Auditer l'identité stable de profil, distincte du pseudo et du téléphone.
- [ ] Isoler les progressions personnelles par profil et par mode sans perdre le Hall global multi-joueurs.
- [ ] Traiter prudemment les anciens scores d'identité ambiguë ; ne pas les attribuer arbitrairement.
- [ ] Tests multi-pseudos (y compris pseudo identique), quatre modes, redémarrages, offline, retry et réinstallation.
- [ ] Build téléphone, revue de régression et tests Samsung ; demander feu vert avant merge/Release.

**Correction non codée à ce jour.**


## Avancement code commun — 12/10/2026

- [x] Pavé Sudoku bas-centre, largeur compacte et PopupWindow prioritaire.
- [x] Cases Lignes/Couples/Triplets/Entourer plus Pulsation douce facultative (OFF au démarrage).
- [x] Modèle `SudokuVisualReasoning.kt` : projection par ligne, groupes 2/3 dans unité, vérification des deux orientations X-Wing.
- [x] Cercles manuels entre notes identiques, sans modifier les notes.
- [x] Séparation de l'écran Hall global et des graphes personnels ; ne pas associer le graphique local à tous les pseudos du classement.
- [x] `GlobalHallProjection.merge` garde deux scores mondiaux distincts malgré des libellés identiques.
- [x] Profils de joueur locaux distincts du pseudo, espace de stats par profil, choix du profil dans Réglages, confirmation en cas de partie en cours.
- [x] Sauvegarde/import des statistiques pour les nouveaux profils.
- [x] Tests rédigés (visuels, collision d'identité et nom de stockage de profil), CI build `phone` activée sur branche, APK .sha256 prévu.
- [x] Obtenir le **run final GREEN** après corrections de compilation et test X-Wing ; vérifier artefact et SHA-256.
- [ ] Recette réelle sur Samsung : superposition Pierre/pavé, taille/cases, contraste, cinq cases, projection, cercles, animations.
- [ ] Recette multi-profils : Classique, Sudoku, Gomoku, Abeilles & Geckos, profils homonymes, import/export, réseau/retry/reinstallation.
- [ ] Auditer l'historique préexistant potentiellement mélangé : **provenance introuvable, pas de redistribution automatique** ; expliquer au joueur que les anciens événements restent sur le profil historique.
- [ ] Attendre accord de Fab pour merge `main`, Release, publication ; aucune livraison Play depuis la signature phone debug.


## Validation finale CI du lot 0.15.45-dev — 12/10/2026

- Workflow `Build GeckoDoku phone APK`, run [38314483379](https://github.com/greenpower2669/GeckoDoku/actions/runs/38314483379) : **GREEN**, `gradle :app:testDebugUnitTest :app:assemblePhone` → `BUILD SUCCESSFUL`.
- Étapes Pierre, banque sprites 240p (source 1 305 fichiers, modèle et manifest dans l'APK) et publication de l'artefact : SUCCESS.
- Artefact Actions : `GeckoDoku-v0.15.45-dev-phone` (ID `11775463777`) contenant `GeckoDoku-v0.15.45-dev.apk` et `GeckoDoku-v0.15.45-dev.apk.sha256` ; APK SHA-256 : `ddff9b30a3267573cbf0a1cecad98fa6c76ca7aee94f670533a2169e1701d586`.
- Cette version est le **build de test phone signé avec la clé debug existante**, PAS une publication Google Play. La signature Google Play n'est pas modifiée.
- PR brouillon #4 : https://github.com/greenpower2669/GeckoDoku/pull/4 ; ne pas merger ni publier avant accord explicite.
- **À faire** : recette physique Samsung par Fab (UI, Pierre, quatre modes, deux profils/homonymes, restauration des données). Les anciens résultats localement mélangés, sans identité d'origine, ne peuvent être reclassés automatiquement.


## Ajustement Samsung — Sudoku visuel (13/10/2026)
- [x] Réduire la hauteur du pavé Sudoku de 25 %.
- [x] Conserver son ancrage bas-centre, y compris après redimensionnement.
- [x] Faire dépasser le trait de projection légèrement au-delà du chiffre à chaque extrémité.
- [ ] Valider visuellement sur Samsung : hauteur, lisibilité des touches et rendu des projections.


## GECKO-PIERRE-WARMUP-003 — première phrase TTS (13/10/2026)

- [x] Charger Sherpa/Piper dès la création du moteur.
- [x] Générer une phrase d'amorce via le même modèle/speaker que Pierre.
- [x] Faire traverser l'amorce au vrai AudioTrack avec volume nul.
- [x] Remplacer l'AudioTrack STATIC recréé par phrase par un AudioTrack STREAM persistant.
- [x] Attendre la consommation réelle des frames via `playbackHeadPosition`, sans timer de fin arbitraire.
- [x] Sérialiser la première vraie phrase derrière l'amorce.
- [x] Conserver le fallback Android en cas d'échec Piper.
- [ ] Confirmer build/tests CI GREEN sur le commit final.
- [ ] Samsung : vérifier que l'amorce est inaudible et que le premier mot de la première vraie phrase n'est plus mangé/déformé.
- [ ] Samsung : vérifier arrêt/pause/reprise, vidéo Prof et absence de régression CPU/mémoire.
- [ ] Aucun merge main / Release avant ordre explicite de Fab.

Mission détaillée : `missions/GECKO-PIERRE-WARMUP-003.md`.


## Binaire / candidats réels — 14/10/2026

- [x] Diagnostiquer pourquoi Lignes/Couples/Triplets/Entourer semblaient inactifs : dépendance exclusive aux notes manuelles.
- [x] Calculer les candidats logiquement possibles depuis `SudokuSolver.candidateMask`, sans modifier les notes personnelles.
- [x] Utiliser ces candidats pour Lignes, Couples et Triplets.
- [x] Entourer accepte désormais un candidat possible même sans note manuelle préexistante.
- [x] Ajouter checkbox **Binaire**.
- [x] Binaire = exactement deux positions du même chiffre dans ligne/colonne/bloc ; plusieurs chiffres simultanés.
- [x] Fusionner le même couple s'il qualifie dans plusieurs unités pour éviter les cercles superposés.
- [x] Double cercle + pulsation lente Binaire ; aucun flash rapide.
- [x] Prévisu explique les options et Pierre lit l'explication.
- [x] Compacter bandeau haut/bas, titres et espaces visuels entre touches.
- [x] Ajouter tests candidats réels + Binaire.
- [x] Confirmer le run final #464 GREEN et relever artefact/SHA.
- [ ] Recette Samsung : visibilité réelle Lignes/Couples/Triplets/Binaire/Entourer, Prévisu, Pierre, taille du pavé.
- [ ] Aucun merge main / Release avant ordre explicite de Fab.


### Preuve CI finale Binaire / candidats réels
Run `38504109994` / #464 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11867790810`, APK SHA-256 `356cb9973995d453d6e82e865b959f4648502624705322502e23d711ec52cb5e`.


## Projection complète / pseudo / Pierre — 14/10/2026

- [x] Restaurer le choix direct du pseudo sans supprimer les profils séparés.
- [x] Ajouter une entrée distincte Profils locaux.
- [x] Renommer « Lignes » en « Projection ».
- [x] Projection sur ligne ET colonne.
- [x] Ne projeter un axe que si le chiffre a exactement deux positions candidates dans cet axe.
- [x] Étendre le trait sur toute la grille, semi-transparent et discret.
- [x] Garder un petit repère aux deux occurrences.
- [x] Simplifier les narrations Sudoku de Pierre et limiter le nombre d'étapes parlées.
- [x] Adoucir les repères visuels de raisonnement de Pierre.
- [x] Tests projection ligne/colonne et présence Pseudo + Profils.
- [x] Confirmer le run final #472 GREEN.
- [ ] Recette Samsung : pseudo, projection horizontale/verticale, lisibilité et rythme de Pierre.
- [ ] Aucun merge main / Release avant feu vert explicite.


### Preuve CI finale projection/pseudo/Pierre
Run `38519610024` / #472 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11873097206`, APK SHA-256 `562218c0d5cc2622e106a582bd5ba8a9bdec667fbec84fc72d0bedb3052ec4b7`.

## Repères humains vs aide surhumaine — 14/10/2026

- [x] Retirer les checkboxes automatiques du clavier.
- [x] Ajouter fenêtre flottante Repères humains, déplaçable et fermable.
- [x] Ajouter bouton `✎ Repères` pour la rouvrir.
- [x] Projection manuelle en croix ligne + colonne à travers un candidat choisi.
- [x] Couple manuel en deux taps plateau.
- [x] Triplet manuel en trois taps plateau.
- [x] Candidat manuel rendu comme petite pièce graphique, sans note logique.
- [x] Effacement des seuls repères graphiques.
- [x] Appui `?` puis clic plateau → confirmation aide surhumaine.
- [x] Ajouter phrase humoristique Pierre à la confirmation.
- [x] Créer fenêtre Aide surhumaine séparée, flottante/déplaçable/fermable.
- [x] Ajouter checkboxes auto Projection / Couples / Triplets / Candidats / Binaire / Pulsation.
- [x] Candidats auto rendu uniquement si checkbox Candidats active ; les autres aides ne montrent que leurs chiffres utiles.
- [x] Toutes aides auto OFF sur nouvelle grille.
- [x] Confirmer le run final GREEN du dernier commit.
- [ ] Recette Samsung : interaction `?` → plateau, deux fenêtres, drag/close, outils manuels, non-modification de la grille.
- [ ] Aucun merge main / Release avant validation Fab.


### Preuve CI finale repères humains / aide surhumaine
Run `38522492488` / #486 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11875030286`, APK SHA-256 `793a3878817fa04f9b2096d3ac032c8f235c98df50f6a7f56df0139dda062d15`.

## Hall global complet / progressions isolées — 14/10/2026

- [x] Supprimer la limite UI `.take(10)`.
- [x] Ne plus tronquer le stockage local du Hall à 200 scores.
- [x] Ajouter une identité opaque stable de progression par profil.
- [x] Préserver la même identité lors d'un renommage.
- [x] Publier l'identité dans `metadata.playerProfileId`.
- [x] Lire l'identité depuis le cache global.
- [x] Conserver les historiques sans identité au Hall mais hors progression.
- [x] Ne jamais fusionner deux profils connus différents au même pseudo.
- [x] Ajouter les sous-progressions par joueur dans le Hall.
- [x] Maintenir le Hall global comparatif avec les scores des autres joueurs.
- [x] Réinitialiser le contexte vivant/ambiant de Pierre lors d'un changement de profil.
- [x] Couper une parole Pierre appartenant à l'ancien profil avant bascule.
- [x] Narration de stats basée uniquement sur le `statsStore` actif et le pseudo actif.
- [x] Ajouter tests identité / renommage / historique ambigu / >10 scores.
- [x] Confirmer le run final du dernier commit de code GREEN + artefact APK.
- [ ] Recette téléphone : plusieurs profils, mêmes pseudos, renommage, Hall complet, sous-progressions, changement de profil pendant/après partie.
- [ ] Vérifier en vraie synchronisation serveur que `metadata.playerProfileId` revient bien via `/sync` pour les scores nouvellement publiés.
- [ ] Aucun merge main / Release / Play avant ordre explicite.


### Preuve CI finale Hall global / progression
Run 38526555276 / #510 : GREEN. Commit code validé 29151fc63bd9f8b05d5ba188276f6cfa197e5998. Tests JVM + assemblePhone réussis, Pierre et banque sprites 240p vérifiés. Artefact GeckoDoku-v0.15.45-dev-phone ID 11876601009. APK SHA-256 eec808012b45e21a36a400672b1c50d7b2db41a27df9310db2df30eccef45981.

## Fenêtre Repères compacte / calques Pierre — 14/10/2026

- [x] Calculer le placement initial Repères depuis la géométrie réelle du plateau.
- [x] Attendre le layout réel du plateau avant placement.
- [x] Réduire la fenêtre Repères à deux rangées utiles, ~78 dp.
- [x] Réduire la barre titre à « ✎ Repères humains ».
- [x] Supprimer les marges verticales inutiles entre titre et outils.
- [x] Mémoriser x/y et width/height de Repères.
- [x] Restaurer puis re-clamper la géométrie à l’ouverture suivante.
- [x] Suivre la géométrie runtime de Repères / Aide surhumaine / pavé Sudoku.
- [x] Pendant la parole de Pierre, sortir temporairement les PopupWindow de la zone visible sans les détruire.
- [x] À la fin de la parole, restaurer chaque fenêtre à sa géométrie précédente.
- [x] Ajouter tests purs du placement compact et du clamp.
- [x] Confirmer CI finale du commit code `423f1a1729024abdeaf524bcde24e08aed1a6516`.
- [ ] Recette Samsung : ouverture directe, déplacement mémorisé, Pierre devant puis retour exact des fenêtres.
- [ ] Aucun merge main / Release avant validation Fab.


### Preuve CI finale Repères / calques Pierre
Run `38537609942` / #522 : GREEN. Tests JVM + `assemblePhone` réussis, Pierre et banque sprites 240p vérifiés, artefact `GeckoDoku-v0.15.45-dev-phone` ID `11880727507`, APK SHA-256 `3f6dbba028b4bbb793ed16cbf3a9bd3eb237f36963c4e9fd8a292da21fed911c`.

## Zone Android utile — GECKO-SUDOKU-SYSTEM-BARS-005 (14/10/2026)

- [x] Auditer la capture Samsung et les calculs de `PopupWindow` dans `MainActivity.kt`.
- [x] Déduire limites root/decor/WindowInsets en coordonnées fenêtre sans double comptage.
- [x] Corriger placement, déplacement, resize de Repères / Aide surhumaine / pavé.
- [x] Re-clamper les PopupWindow visibles à changement d'insets ; préserver temporairement leur suspension Pierre.
- [x] Ajouter tests unitaires edge-to-edge / navigation / root déjà inset / rotation / clavier.
- [ ] CI `testDebugUnitTest + assemblePhone` GREEN du dernier code de la mission.
- [ ] Vérifier artefact APK : Pierre UPMC medium + sprites 240p et SHA-256.
- [ ] Recette Samsung : déplacer jusqu'au-dessus des trois touches Android sans débord, redimensionner, rotation/reprise, Pierre devant/restitution sans perte.
- [ ] Vérifier ruban horizontal Repères ; ne pas cacher le bouton Projection au premier affichage.
- [ ] Aucun merge main, Release ou Play sans validation de Fab.

Mission : `missions/GECKO-SUDOKU-SYSTEM-BARS-005.md`.

## GECKO-SUDOKU-MANUAL-PROJECTION-006 — 14/10/2026

- [x] Auditer la capture « Projection manuelle 1 » et les deux `drawLine` inconditionnels.
- [x] Ajouter politique testable axe unique selon alignement des marques du même chiffre ; défaut ligne, égalité ligne.
- [x] Modifier le rendu manuel afin d'interdire la croix.
- [x] Ajouter la croix discrète seulement pour une hypothèse réellement posée.
- [x] Modifier les messages du joueur sans annoncer de fausse croix.
- [x] Ajouter six tests JVM (ligne / colonne / isolé / chiffres distincts / égalité / hypothèse).
- [ ] Confirmer le nouveau run CI phone `testDebugUnitTest + assemblePhone` GREEN.
- [ ] Vérifier APK test embarquant Pierre UPMC et sprites 240p ; SHA-256.
- [ ] Recette Samsung avec repères 1 alignés sur ligne puis colonne, hypothèse active puis retirée ; confirmer aucune élimination, aucune régression autre outil.
- [ ] Terminer recette Android navigation-bounds mission 005.
- [ ] Sans merge main, Release, Play ni changement de signature sans feu vert.

Voir `missions/GECKO-SUDOKU-MANUAL-PROJECTION-006.md`.

## Deux pushs ergonomie Samsung — 007 + 008 (14/10/2026)

### PUSH 1 — Intro et fenêtre Repères
- [x] Empêcher l'affichage de Repères pendant la vidéo d'introduction, avant visibilité/mesure réelle du Sudoku ou pendant la parole de Pierre.
- [x] Réouvrir seulement une fois l'intro terminée ; préserver fermeture et position manuelle.
- [x] Tester la garde sur mode/intro/rideau/vidéo/grille/fermeture.
- [ ] Recette Samsung : intro en mode sauvegardé Sudoku, intro sautée, sans intro, Pierre parle, réouverture.

### PUSH 2 — Grille visible, pavé dessous
- [x] Calculer une zone clavier située sous la dernière rangée du plateau, en excluant la navigation Android.
- [x] Adapter la hauteur du pavé à cette zone, bornage du drag, du resize et du recalage.
- [x] Tests JVM portrait, redimensionnement et évolution du bas du plateau.
- [ ] Recette Samsung : 9 lignes visibles, cibles tactiles suffisamment grandes, bouton fermer, nouvelle hypothèse, Pierre devant puis restauration.
- [ ] Paysage / petit écran : si l'espace libre est trop faible, vérifier l'ergonomie et concevoir au besoin une disposition alternative ; ne pas annoncer validation sans ce test.

### Livraison
- [ ] Vérifier le run CI du dernier commit avec `testDebugUnitTest + assemblePhone`, assets Pierre / sprites 240p et SHA-256.
- [ ] Télécharger l'APK validé et l'extraire dans un répertoire temporaire.
- [ ] Aucun merge main, Release, Play sans nouvel ordre Fab.

Passations : `missions/GECKO-SUDOKU-INTRO-GATE-007.md` et `missions/GECKO-SUDOKU-KEYPAD-DOCK-008.md`.

### Preuve CI Samsung 007/008 — run #525
Validation finale après les missions 007 et 008 : run [#525](https://github.com/greenpower2669/GeckoDoku/actions/runs/38587086273), workflow 38587086273 **GREEN**, commit de build `eee5347f52602ccbc91d1359aeacb161c427d9f1`. `testDebugUnitTest + assemblePhone` réussis ; vérification Pierre Sherpa/Piper et banque sprites 240p réussie. Artefact `GeckoDoku-v0.15.45-dev-phone` ID `11905448115` ; APK `GeckoDoku-v0.15.45-dev.apk` SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`, contrôlé après extraction. La recette Samsung demeure nécessaire. Aucun merge main / Release / Play.

## Livraison directe APK #525 — prérelease TEMP (14/10/2026)

- [x] Accord Fab pour un `.apk` direct sur GitHub ; règle permanente consignée dans `ordres-de-mission.md`.
- [x] Intégrité vérifiée pour l'APK run #525 : `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`.
- [x] Premier workflow publication #526 : artifact téléchargé et SHA OK ; `gh release create` refusé par GitHub **403 Resource not accessible by integration**. Aucun asset publié.
- [ ] Nouvelle tentative via l'ancienne méthode prouvée (checkout + tag sur commit déclencheur), sans reconstruire Android.
- [ ] Confirmer la prérelease `temp-geckodoku-0.15.45-dev-run-525`, son asset APK direct et son checksum par lecture API GitHub avant toute annonce.
- [ ] Si refus 403 persiste : laisser blocage documenté et ne pas fabriquer d'URL directe.

## Preuve GitHub — APK direct #525 (14/10/2026)
Livraison APK brut run #525 : prérelease temporaire `temp-geckodoku-0.15.45-dev-run-525` créée et vérifiée par l'API GitHub. [Téléchargement direct .apk](https://github.com/greenpower2669/GeckoDoku/releases/download/temp-geckodoku-0.15.45-dev-run-525/GeckoDoku-v0.15.45-dev.apk), 299496421 octets, SHA-256 `23506e3a310401dcf4016c5544324913d9670bb54b70ce439da6b8356effcb51`. Workflow de publication #527 GREEN. La première tentative #526 (403 GitHub) a été corrigée en reprenant la méthode qui fonctionnait : checkout de la branche et création du tag sur le commit déclencheur. Aucun nouveau build Android, aucun merge main ou Release stable. Règle permanente de livraison d'APK direct enregistrée dans la section `Contrat FAB Copilot` d'`ordres-de-mission.md`.

## GECKO-SUDOKU-KEYPAD-OPEN-COMPACT-009 — taille réelle (14/10/2026)

- [x] Comparer captures 16:45 / 18:40 et analyser constructeur `PopupWindow` + dock.
- [x] Corriger : appliquer dimensions compactes à l'objet AVANT son ouverture, à droite sous les neuf lignes.
- [x] Recalculer sur réutilisation de pavé, changement des dimensions grille et insets ; conserver déplacements valides.
- [x] Ajouter tests JVM : portrait deuxième capture, divergence dimensions réelles vs modèle, préservation/recalage du clavier.
- [ ] CI phone `testDebugUnitTest :app:assemblePhone` GREEN du nouveau commit.
- [ ] APK phone et SHA, Pierre et sprites 240p validés.
- [ ] Vérification manuelle Samsung : entrée sans redimensionner, aucun chevauchement des 9 lignes, 4 zones tactiles, sélection d'une autre case, rotation, parole Pierre.
- [ ] Pas de merge main, Release stable, Play. Prérelease TEMP uniquement si APK direct demandé conformément au contrat FAB Copilot.

Mission : `missions/GECKO-SUDOKU-KEYPAD-OPEN-COMPACT-009.md`.

### CI mission 009 — test #528 puis nouvelle validation

- [x] #528 : compilateur Kotlin passé, 298/299 tests OK ; 1 test révèle double déduction du dégagement bas dans `compactKeypadBelowBoard`.
- [x] Correction ciblée : `room = dock.height - margin - maxOf(margin, bottomGap)`, conserve 416 px sans recouvrir la grille.
- [ ] Relancer CI complète après correction ; n'annoncer GREEN qu'avec réussite de tous les tests / build et présence assets.

## Mission 010 — merge + Release AAB signée via Secrets

- [x] Autorisation explicite Fab « merge release avec la secret key » le 14/10/2026.
- [x] Lire le protocole signature immuable sur `feature/play-upload-signing-v1` et le transposer sans copier de secret.
- [x] Créer `build-play-aab.yml` sur la branche Sudoku avec contrôle bloquant SHA-1 du nouveau certificat Play.
- [x] Publication Release de l'AAB signée possible uniquement sur main et seulement après succès signature/tests.
- [ ] Avant merge : vérifier les GitHub Secrets par le workflow, JKS reconstruit, empreinte SHA1 identique, tests et AAB signé.
- [ ] Merge PR #4 si GREEN ; ne jamais fusionner après mismatch.
- [ ] Signer AAB depuis main après merge, publier Release GitHub .aab + sha256 et contrôler les URLs réelles.
- [ ] Confirmer séparément l'acceptation Play Console et la sauvegarde externe du JKS.
- [ ] Écrire SHA-256, run et tags finaux dans les cinq mémoires.

## GECKO-PLAY-MERGE-SIGNED-RELEASE-010 — clôture

- [x] Protocole signature immuable sur main et workflow AAB depuis Secrets.
- [x] Match SHA-1 du certificat upload Google Play après rotation, depuis JKS reconstruit (sans lecture des secrets par l'assistant).
- [x] Pré-merge AAB signé / tests / Pierre / sprites GREEN (`38636402228`).
- [x] Merge PR #4 sur main (`f48dcd7`).
- [x] Rebuild AAB signé `main` GREEN (`38636963727`).
- [x] Release stable GitHub AAB code80 + sha256 publiée et vérifiée.
- [ ] À réaliser sur Play Console : déposer l'AAB et vérifier acceptation/rollout (aucun envoi effectué).
- [ ] Vérifier sauvegarde hors GitHub du keystore chiffré, et conservation séparée des mots de passe.

**Clôture validée 14/10/2026 — mission 010.** Feu vert de Fab « merge release avec la secret key ». PR #4 mergée sur `main`, SHA merge `f48dcd7d6898793ccd6d63b22c0bc20644d6966a`. Contrôle pré-merge AAB sur branche : workflow `38636402228` GREEN (certificat Upload Key conforme au Play SHA-1 `27:AD:62:56:96:53:F9:82:37:F6:92:F7:9B:EA:DB:B8:EB:2E:25:C7`, tests, Pierre et sprites 240p, `jarsigner -verify`). Build officiel depuis main : workflow `38636963727` GREEN, tests + `bundleRelease`, même contrôle certificat, `jar verified.`. GitHub Release **publique/stable** : https://github.com/greenpower2669/GeckoDoku/releases/tag/geckodoku-v0.15.45-dev-code80 ; AAB signé direct : https://github.com/greenpower2669/GeckoDoku/releases/download/geckodoku-v0.15.45-dev-code80/GeckoDoku-v0.15.45-dev-code80-signed.aab ; SHA-256 `08390c0e502380ee5d3b6e2e77e01d491ff2a9535c61524b9b5270eb92cd755c`. Application `com.greenpower2669.geckodoku`, versionName `0.15.45-dev`, versionCode `80`. L'APK téléphone précédent reste un build à signature test : NE PAS le présenter comme signé Play. Aucun envoi à Play Console réalisé ; l'acceptation de l'AAB par Google n'est pas encore démontrée. Aucune clé privée ni valeur de Secret dans Git.


## GECKO-INFERNAL-NESTED-011 — suite et qualité (15/10/2026)
- [x] Auditer les contrats Classic et le solveur d'hypothèses avant modification.
- [x] Inférer une contradiction prouvée à profondeur imbriquée au moins 3, sans accepter 2 hypothèses indépendantes.
- [x] Préserver Mission Impossible : une hypothèse de profondeur 1.
- [x] Séparer les issues contradiction / solution complète / branche indécise ; préserver profondeur des preuves intermédiaires.
- [x] Vérifier profil Infernal dans la génération exacte avec unicité indépendante et trace Professeur à profondeur 3.
- [x] Ajouter tests de frontière et activer CI de la branche.
- [ ] Confirmer la CI testDebugUnitTest + assemblePhone, Pierre et sprites, SHA/APK.
- [ ] Trouver et fixer un seed de grille à véritable profondeur 3 ; prouver unicité et trace complète par test end-to-end.
- [ ] Mesurer le temps de génération exacte Infernal par taille, préserver annulation ; refuser les faux Infernal.
- [ ] Valider un cycle H1 → H2 → H3 et les explications de Pierre sur Samsung.
- [ ] Étudier une narration complète de l'arbre interne (non enregistrée à ce stade), si demandée.
- [ ] Ne pas merger ni publier sans nouvelle autorisation explicite de Fab.

### Preuve CI mission 011 — 15/10/2026
- [x] CI #533 / 38800346305 GREEN : JVM tests, assemblePhone, Pierre, banque 240p.
- [x] Artefact phone ID 12003858392 ; APK test SHA-256 ce9b504397cfad14c733636ac2dd3016e4aa6e108d1acb9ee556921eb022e3f3.
- [ ] La ligne de checklist CI initiale est satisfaite, mais seed profondeur 3 et essais Samsung restent obligatoires.
