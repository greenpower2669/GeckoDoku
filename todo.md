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
- [ ] Téléphone : confirmer animations Gecko revenues.
- [ ] Téléphone : confirmer Pierre Piper revenu (plus de fallback `engine=android`).
- [ ] Clean install online : confirmer que le score global Facile réapparaît avant toute nouvelle partie.
- [ ] Partie online : une seule entrée serveur.
- [ ] Partie offline : pending conservé.
- [ ] Kill/redémarrage offline : pending survit.
- [ ] Retour réseau : envoi automatique.
- [ ] Retry/duplicate : une seule entrée globale.
- [ ] Vérifier au moins un score réel par mode éligible et ses catégories.
- [ ] Merge `main` / Release uniquement sur ordre explicite de Fab.

## Produit hors mission à préserver

Ne pas modifier pour ce lot : gameplay, étoiles, aides, statistiques personnelles, progression, gestes/géométrie, logique Prof, médias source.
