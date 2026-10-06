# GeckoDoku — FAB Copilot debug historical

> Historique condensé. La vérité courante reste `brain.md` + `ordres-de-mission.md`.

## Baseline avant HOF

- base : `main@1b66d3fc6ad4bfa06bf939cd5ee743fe767a4675`
- version `0.15.43-dev`, code 78
- téléphone validé avant HOF : `phone-0.15.43-dev-run-403`
- quatre modes, Pierre, médias 240p, stats et Hall local déjà fonctionnels.

## GECKO-HOF-SYNC-001 — 06/10/2026

Implémenté en TDD : payload 4 modes, pending crash-safe, client ACK, backoff, cache `/sync`, mono-worker, reprise réseau, runtime Android, mapping des complétions et timestamp local/global unique.

Invariants : pending avant réseau, retry exact, erreurs permanentes BLOCKED, `/sync` transactionnel, seed décimale 64 bits, aucun secret APK, aucune donnée globale injectée dans stats/progression.

## Régressions téléphone du premier APK de validation

Fab a fourni capture + journal.

### Médias

Symptômes : Gecko invisibles/empreintes vides et Pierre muet.

Preuves journal :
- `SpriteBankFactory ... SPRITE_BANK_ERROR ... FileNotFoundException` sur `gecko/alive/stay*.mp4`, `Gecko_apparition.mp4`, etc. ;
- `ProfessorSpeech ... SPEAK_STARTED ... engine=android`.

Cause : l’APK de validation HOF avait été construit via une chaîne légère `assembleDebug` sans le packaging téléphone complet. Le code gameplay n’était pas en cause.

Correction build :
- Piper UPMC Medium téléchargé avec checksum canonique ;
- ancienne Release technique `PackageSprites` constatée absente ;
- banque 240p restaurée depuis l’APK téléphone validé `phone-0.15.43-dev-run-403`, SHA connu `5b4f38049c3c7e8d115b78bef573784084ddec85cd4774d620dd0d76a4ba7a94` ;
- `assemblePhone` ;
- contrôle APK : modèle Pierre présent, index sprites présent, bank-manifest 240p présent, 1 305 fichiers 240p, entrées Gecko signalées par le journal présentes.

### Hall après réinstallation

Symptôme : un score global Facile existait mais, après désinstallation/réinstallation, l’UI disait « aucune partie terminée » ; une nouvelle partie recréait ensuite 1 résultat.

Cause : `/sync` alimentait bien `GlobalScoreCacheStore`, mais `showHallOfFame()` lisait seulement `HallOfFameStore.entries()` local. La désinstallation effaçait donc la seule source affichée.

Correction TDD :
- `GlobalHallProjectionTest` RED ;
- `GlobalHallProjection` convertit les entrées cache valides en `HallOfFameEntry` ;
- `HallOfFameStore.entries()` fusionne local + cache global en lecture seule ;
- même complétion locale/globale dédoublonnée grâce au `completedAt` commun ;
- entrées invalides/inachevées ignorées ;
- tests HOF GREEN.

## État actuel

- code fonctionnel : `1e54fe8156bc04de2470456423ec62f125856083`
- build téléphone complet : run `37526965060` GREEN
- artifact : `GeckoDoku-HOF-v1-phone-validation`
- SHA-256 : `c5232d721c22d4d7c1eefd35f53411ebf03a0d1f70ade1cc145efef1019b63bf`
- workflow temporaire de build supprimé après génération.

Reste : validation réelle animations/Pierre, restauration Hall après clean install, puis scénarios offline/retry. Aucun merge/release avant ordre de Fab.
