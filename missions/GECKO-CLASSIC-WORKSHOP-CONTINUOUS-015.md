# Mission 015 — Atelier GeckoDoku en continu, zéro essai corrigé

**Demande Fab, 16/10/2026 :** « Je veux une recherche en continue » et une vraie lecture du compteur, après constat d'absence d'essais sur toutes les tailles. Contrat : préserver H2–H4, X-Wing facultatif 5/6/7, obligatoire 8–12, grilles et ZIP existants. Branche de développement `feature/classic-workshop-continuous-fgs-v3`.

## Diagnostic

Le journal utilisateur montre activités média et interface, pas de recherches traçables. L'ancien WorkManager exigeait une batterie non faible ou le chargeur pour les jobs périodiques ; ses fenêtres de 25s étaient opportunistes. 0 au compteur = aucune tentative persistée, sans preuve d'une recherche effective. Il ne faut plus présenter `enabled=true` comme preuve d'une exécution.

## Implémentation

1. L'utilisateur presse DÉMARRER depuis Atelier : `Context.startForegroundService` crée `InfernalWorkshopService`. Type Android FGS `specialUse` (permission FOREGROUND_SERVICE_SPECIAL_USE + sous-type explicite), notification persistante et bouton Arrêter, permission POST_NOTIFICATIONS proposée à partir d'Android 13. L'arrêt par notification désactive le flag persisted.
2. Thread unique, lancement immédiat, parcours 5→6→…→12→5, 20 secondes maximum par taille plus 250ms entre essais, pas de besoin de chargeur. Les recherches très coûteuses peuvent terminer sans grille ; tentatives malgré tout comptées. Le service suspend son calcul en cas de chaleur sévère ou économie de batterie ; Android/Samsung peut aussi le suspendre ou l'arrêter.
3. Écriture persistante de compteurs au DÉBUT de la tentative : `attempts_v2_N` / total + heure + currentSize ; après fin : `completed_v3_N`, `saved_v3_N` ou `errors_v3_N`. Phase, dernier signal, date/erreur restent disponibles. Ancien WorkManager est annulé, classe Worker historique présente pour compatibilité des travaux déjà programmés.
4. UI popup Atelier avec compteur LANCÉES, TERMINÉES, ERREURS, ENREGISTRÉES, dernière activité, phase, taille courante/prochaine. Rafraîchissement automatique 1,5 secondes tant que popup ouverte ; détails par taille. Diagnostic persistant dans `MediaTrace` : SERVICE_ACTIVE, SEARCH_START, SEARCH_END, THERMAL_PAUSE, BATTERY_SAVER_PAUSE, ERROR, STOP_NOTIFICATION, SERVICE_STOPPED.
5. Aucun changement du moteur standard du jeu, de Pierre, ni Hall of Fame ; les candidats ont toujours une solution unique et leur validation reste manuelle. JSON/ZIP existants inchangés.

## Prudence

Un foreground service **n'est pas une garantie mathématique de fonctionnement infini** : Android et les restrictions Samsung peuvent interrompre l'application, la batterie peut se vider, le système peut imposer des quotas. Sur Android 14+, `specialUse` doit être déclaré et peut nécessiter justification/revue Play avant la publication. Cette solution est actuellement limitée à une branche/APK de test. Aucune Release, AAB, fusion main, Play.

## Recette Samsung

Activer Atelier → DÉMARRER RECHERCHE CONTINUE. Vérifier notification, moteur actif, total essais croissant en direct, taille passant de 5 à 12, terminé >0 sans candidats, absence erreurs ; le Journal vidéo doit fournir SEARCH_START/SEARCH_END. Masquer app et éteindre écran : vérifier progression après quelques minutes. Activer économie de batterie : phase pause. Arrêter avec notification, vérifier état et absence de nouvelle recherche. Exporter ZIP (solutions privées) si grille trouvée. Si 0 persiste, relever phase, dernière erreur et journal; ne pas déduire de l'absence de grilles que le thread ne tourne pas.

### CI atelier 015 — 16/10/2026
Run téléphone #557 `38929818514` GREEN sur commit `ad65fab01d05d0ec3751246d8d8446d6a64fabe7`. `BUILD SUCCESSFUL in 2m 33s`, tests JVM, `assemblePhone`, contrôles de Pierre et sprites 240p OK. Artefact Actions `GeckoDoku-v0.15.45-dev-phone` id `12058666334` ; SHA-256 APK `da540f8e19007d7d4fe932df13d30c00a09160958560adbe5f15b6d26dede8af`. Les anciennes CI #552, #554, #555 étaient aussi vertes. APK brut non publié : seul l'artefact ZIP Actions existe ; ne pas inventer un lien direct .apk ni créer de prérelease sans nouvelle demande. Aucun test Android 16 réel effectué. Aucun merge main, aucune Release/Play. Un foreground service est susceptible d'être interrompu par Android ; `specialUse` nécessite examen Play avant diffusion publique.
