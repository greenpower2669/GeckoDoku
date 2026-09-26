# GeckoDoku — brain.md
## RÔLE DU FICHIER

`brain.md` est la **mémoire fonctionnelle durable et la source de vérité comportementale** de GeckoDoku.

Il ne décrit pas seulement le bug du jour. Il conserve :
- les comportements validés qui doivent survivre aux futures missions ;
- les contrats fonctionnels et règles utilisateur ;
- les invariants de non-régression ;
- les décisions d'architecture qui sont réellement nécessaires au comportement ;
- les distinctions de rôle entre assets et sous-systèmes.

Une hypothèse technique temporaire qui se révèle fausse doit être corrigée ici ; elle ne doit jamais devenir un dogme contre les preuves téléphone.

`ordres-de-mission.md` = travail immédiat.
`brain.md` = ce que le logiciel doit durablement rester.
`brainmap.md` = relations/architecture/causalité.
`debughistorical.md` = historique des erreurs, causes et corrections.
`todo.md` = travail restant.

## État fonctionnel durable — 26/09/2026

### Validé téléphone — gelé
- Intro 1 `IntroGeckoGD.mp4` : parfaite, son inclus.
- Intro 2 `Gecko_Intro.mp4` : parfaite, enchaînement correct.
- Icône launcher : validée.
- Médaillon près du titre : validé.

### Contrats animations/sprites
- Gecko gameplay : animation visible, audio embarqué muet.
- Keycolor bleu → vraie transparence ; rectangle noir interdit.
- Pierre ne doit pas être coupé par une action joueur normale.
- `ProfParle.mp4` suit exactement l'état réel de parole.
- Priorité locale du slot Prof : PROF_SPEECH > PROF_ACTION.
- Cette priorité ne touche jamais une animation Gecko.
- Plusieurs sessions vidéo doivent réellement coexister.
- Une fin de session ne stoppe jamais une autre.
- Une erreur ProfParle ponctuelle donne un fallback PNG pour la tentative courante, mais ne désactive jamais les tentatives futures.
- Les intros validées sont hors périmètre du correctif sprites sauf régression démontrée.

### Rendu vidéo
Le résultat fonctionnel est prioritaire sur l'hypothèse de Z-order.
La stratégie Surface/GL doit garantir simultanément :
1. alpha/chroma transparent réel ;
2. absence de rectangle noir ;
3. sessions indépendantes ;
4. coexistence des animations compatibles.

Le téléphone a montré un rectangle noir avec la stratégie `setZOrderMediaOverlay(true)`. L'ancienne interdiction absolue de `setZOrderOnTop(true)` est donc retirée : elle était une hypothèse technique, pas un besoin fonctionnel.

Conserver `GeckoDokuMediaTrace`.


<!-- GECKO-033-PROFPARLE-RETRY-GREEN-2026-09-26 -->
## Invariant durable — erreur vidéo ProfParle
Une erreur de `ProfParle.mp4` est **locale à la tentative courante** :
- Pierre continue via la voix ;
- PNG fallback possible ;
- le prochain speech peut retenter la vidéo ;
- un succès ultérieur efface l'état d'erreur diagnostic.

Jamais de latch permanent de désactivation.
