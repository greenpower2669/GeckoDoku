# TODO ACTIF — GECKO-040

## Validé / fermé
- [x] Gomoku titre corrigé.
- [x] Gomoku animations suivent zoom.
- [x] Gomoku animations suivent drag/pan.
- [x] Gomoku validé téléphone Fab : parfait.

## Classic layout
- [ ] Déplacer Sauver dans ⚙️.
- [ ] Déplacer Journal dans ⚙️.
- [ ] Réorganiser Nouvelle / Rejouer / Stats / ! / ⚙️ pour libérer de l'espace.
- [ ] Calculer le plus grand carré de grille possible.
- [ ] Centrer horizontalement la grille.
- [ ] Ne modifier aucune règle logique de la grille.

## Difficulté stricte
- [ ] Générer en boucle jusqu'à obtenir exactement la difficulté demandée.
- [ ] Exécuter la recherche hors thread UI.
- [ ] Afficher un état de recherche.
- [ ] Permettre Annuler / changer de difficulté.
- [ ] Interdire tout fallback silencieux vers un niveau voisin.

## Historique / stats / étoiles
- [ ] Ajouter Vider l'historique avec confirmation.
- [ ] Ajouter statistiques par difficulté.
- [ ] Ajouter suivi des aides demandées.
- [ ] 5 étoiles si résolution sans aide.
- [ ] Réduire les étoiles selon le niveau d'assistance.
- [ ] Persister les étoiles avec le résultat.
- [ ] Afficher les étoiles dans le Hall of Fame.

## Profil joueur
- [ ] Nom par défaut : GeckoTétu.
- [ ] Permettre modification du nom.
- [ ] Utiliser le nom dans les nouvelles entrées Hall of Fame / stats pertinentes.

## Export / import
- [ ] Export versionné de tous les paramètres et données persistantes utiles.
- [ ] Inclure sauvegardes, progression, stats, historique, Hall of Fame, profil.
- [ ] Import avec validation de format/version.
- [ ] Import transactionnel ou sauvegarde préalable.
- [ ] Prévoir migration de schéma.
- [ ] Tests aller-retour export → import.

## Validation
- [ ] Tests unitaires.
- [ ] CI GREEN.
- [ ] APK/AAB candidate.
- [ ] Test téléphone Fab.


<!-- GECKO-040-PHASE1-2026-09-28 -->
- [x] Sauver + Journal dans ⚙️.
- [x] Commandes Classic compactées sur 2 lignes.
- [x] Grille Classic centrée structurellement.
- [x] Recherche stricte de difficulté hors UI, annulable.
- [ ] CI GREEN phase 1.
- [ ] Étoiles + Hall of Fame + GeckoTétu + historique.
- [ ] Export/import complet.


<!-- GECKO-040-PHASE2-TODO-2026-09-28 -->
- [x] Nom défaut GeckoTétu + modification.
- [x] 5 étoiles sans aide, baisse selon assistance.
- [x] Stats étoiles par difficulté.
- [x] Hall of Fame avec nom/niveau/étoiles/temps.
- [x] Vider historique Hall of Fame avec confirmation.
- [x] Persistance du son dans le profil.
- [ ] CI GREEN phase 2.
- [ ] Export/import complet phase 3.


<!-- GECKO-040-PHASE3-TODO-2026-09-28 -->
- [x] Export complet JSON.
- [x] Import validé + rollback.
- [x] Stats, journal, paramètres, profil, Hall of Fame inclus.
- [x] Taille/difficulté rendues persistantes.
- [ ] CI GREEN final.
- [ ] APK/AAB candidate.
- [ ] Test téléphone Fab.
