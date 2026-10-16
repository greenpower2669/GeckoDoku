# Atelier GeckoDoku — Mission 017 — Sans X-Wing obligatoire

**Ordre du 16 octobre 2026 :** après 1194 recherches moteur actif sans candidats, Fab demande de retirer le X-Wing des équations et d'essayer.

## Contrat de l'expérience
- Les tailles restent 5×5 à 12×12, rotation continue, même géométrie et 5..12 couleurs/régions/Gecko.
- **Aucun X-Wing obligatoire**, y compris pour 8×8 à 12×12. Le compteur `xWingCount` reste un simple renseignement éventuel de la trace, jamais une condition d'acceptation.
- Supprimer toute seconde résolution de rejet `rules = NO_X_WING` du moteur atelier. Le solveur normal reste inchangé et peut utiliser le X-Wing s'il le rencontre.
- **Toujours exiger** solution unique validée indépendamment et H2-H4 imbriquées effectivement prouvées ; ne pas déclasser une H1 en H2.
- Bibliothèque de candidats/ZIP inchangée, validations manuelles obligatoires, pas de stats Hall pour les tests.
- Comptage A/B séparé à partir de la version v4 : `attempts_no_xwing_v4`, `saved_no_xwing_v4` ; compteurs historiques conservés.
- Statistiques atelier : taille 5..12 sans obligation X-Wing, distinguer anciens essais et nouveaux essais.
- Tester JVM, APK Android, Pierre/sprites 240p, puis rendement sur Samsung. CI GREEN ≠ grille réelle déjà générée.
- Branche `feature/classic-workshop-no-xwing-v4`. Pas de merge main, stable Release, AAB ou Google Play.

## Suite si zéro candidat
Après quelques centaines de recherches terminées avec 0 candidat enregistré, comparer le taux par taille et instrumenter les raisons de rejet (unicité, profondeur H2, temps imparti, etc.). Ne pas conclure que le X-Wing seul était responsable sans expérience.
