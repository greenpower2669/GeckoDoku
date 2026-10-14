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
- [ ] Confirmer le run final du dernier commit de code GREEN + artefact APK.
- [ ] Recette téléphone : plusieurs profils, mêmes pseudos, renommage, Hall complet, sous-progressions, changement de profil pendant/après partie.
- [ ] Vérifier en vraie synchronisation serveur que `metadata.playerProfileId` revient bien via `/sync` pour les scores nouvellement publiés.
- [ ] Aucun merge main / Release / Play avant ordre explicite.
