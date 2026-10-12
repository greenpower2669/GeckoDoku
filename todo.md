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
