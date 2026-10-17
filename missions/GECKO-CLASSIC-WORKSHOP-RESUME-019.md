# Mission 019 — Atelier Infernal : retrait, preuve sans chronomètre, reprise

Date : 17/10/2026. Autorisation Fab : « Ok, gogogo » après validation du contrat de recherche. Branche de travail : `feature/classic-workshop-resumable-deletion-v6`, issue de `feature/classic-workshop-forward-geckos-xwing-v5`. **Aucun merge main, aucune Release, aucun Play sans ordre distinct.**

## Contrat

- Partir d'une solution complète légale. Retirer un Gecko à la fois ; si l'unicité disparaît, le restaurer et explorer les autres retraits.
- Ne pas qualifier une recherche interrompue comme échec logique. Distinguer `PAUSED/INCOMPLETE` de `EXHAUSTED`.
- Conserver H2–H4 imbriquées ; X-Wing facultatif pour 5–7 et obligatoire pour 8–12. Une étape X-Wing seule n'est pas preuve de nécessité ; exiger la comparaison complète sans X-Wing.
- Les budgets de 20 s, 850 ms, 1,5 s, 2,5 s et plafonds de nœuds ne doivent pas causer d'abandon de preuve. Les pauses de protection système restent actives.
- À la reprise Android, reprendre l'état enregistré (taille, graine, géométrie, grille et position de retrait). Sauvegarder atomiquement, y compris lors d'une demande de pause.
- Ne pas altérer d'autres modes de jeu, données historiques, ou progression des profils.

## Réalisation / limites à vérifier

1. [ ] Contrat de retrait avec tests d'unicité et restauration.
2. [ ] Points de reprise durables pour solution, retrait et piste de démonstration.
3. [ ] Recherche logique complète H1 puis H2–H4, sans cutoff temporel ni nœud arbitraire.
4. [ ] Comparaison X-Wing et distinction INCONCLUSIVE vs échec mathématique.
5. [ ] Intégration service : reprendre taille active, pause thermique/batterie/manuelle, ne jamais signaler fin sur pause.
6. [ ] Tests JVM + build APK phone via GitHub Actions, contrôle Pierre et banque sprites 240p.
7. [ ] Essai réel Samsung (pause, arrêt brutal, écran éteint, chauffe), puis seulement demander autorisation de livraison APK direct.

**Ne jamais prétendre qu'un checkpoint à la seule frontière des retraits restaure une pile de démonstration profonde. Si la sérialisation des branches H2–H4 n'est pas implémentée, ce reste doit être explicitement déclaré dans le todo avant de conclure.**
