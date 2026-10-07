# GeckoDoku — TODO actif

Mission active `GECKO-PLAY-SIGNING-001` sur `feature/play-upload-signing-v1`.

Mission précédente `GECKO-HOF-SYNC-001` : close, mergée et publiée.

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


## GECKO-PLAY-SIGNING-001 — Google Play / AAB

- [x] Créer la branche `feature/play-upload-signing-v1` depuis `main@908df36a4e26e79d714967ff9fb7948759cdd9ee`.
- [x] Préparer workflow one-shot de génération clé d'upload + certificat.
- [x] Préparer workflow AAB Release signé.
- [x] Préserver Pierre + sprites 240p dans la chaîne AAB.
- [x] Interdire l'écrasement silencieux d'une clé existante.
- [x] Ne jamais écrire le secret ou le JKS clair dans Git.
- [ ] Fab : créer le secret GitHub `ANDROID_UPLOAD_STORE_PASSWORD` (24+ caractères, idéalement généré par gestionnaire de mots de passe).
- [ ] Déclencher la génération one-shot par commit `[generate-upload-key]`.
- [ ] Vérifier le run et le SHA-256 du certificat public.
- [ ] Construire l'AAB signé par commit `[play-aab]`.
- [ ] Vérifier tests, packaging média et signature du bundle.
- [ ] Fab : importer l'AAB dans Play Console et laisser Google générer/protéger la clé de signature Play.
- [ ] Aucun merge `main` avant ordre explicite de Fab.
