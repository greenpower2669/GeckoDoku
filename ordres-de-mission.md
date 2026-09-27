# GeckoDoku — GECKO-037
# ORDRE DE MISSION COMPLET
## Prof Gecko vivant : 309 phrases, mémoire 48 h, contexte joueur, humeur cachée et bulle synchronisée

Date : 27/09/2026
Dépôt : `greenpower2669/GeckoDoku`
Branche de travail actuelle vérifiée : `gecko-033-identity-prof-life`
Base comportementale validée : v0.10.15-dev / versionCode 26

---

# 0 — BUT GÉNÉRAL

Faire évoluer Prof Gecko / Pierre d’un système de phrases tirées presque au hasard vers un personnage qui semble :

- se souvenir de ce qu’il vient de dire ;
- observer réellement la manière de jouer ;
- adapter son ton au comportement du joueur ;
- devenir fier, taquin, pédagogue ou encourageant selon la situation ;
- éviter les répétitions ;
- ne jamais casser les explications sérieuses du Prof ;
- conserver son humour ;
- fermer naturellement sa bulle après une petite intervention.

Effet recherché :

> « Il a vu ce que je viens de faire et il réagit vraiment à ma partie. »

Règle de philosophie :

> « Taquiner le comportement, jamais rabaisser le joueur. »

Une bonne blague doit sembler déclenchée par ce qui vient réellement de se passer.

---

# 1 — SOURCE ACTUELLE / NON-RÉGRESSION

Le corpus historique se trouve notamment dans :

`app/src/main/java/com/greenpower2669/geckodoku/PierreSmallTalk.kt`

Il contient actuellement 100 phrases historiques.

**NE PAS LES SUPPRIMER.**

Objectif final :

- 100 historiques ;
- 200 nouvelles V2 ;
- 4 phrases FAB ;
- 5 nouvelles phrases TAQUIN ;
- total = **309 phrases**.

---

# 2 — MODÈLE DE DONNÉES DES PHRASES

Les 309 phrases ne doivent plus être une simple liste de String.

Créer une structure stable du type :

```kotlin
data class ProfessorPhrase(
    val id: String,
    val text: String,
    val category: PhraseCategory,
    val rarity: PhraseRarity = PhraseRarity.NORMAL,
    val moods: Set<ProfessorMood> = emptySet()
)
```

Chaque phrase doit avoir :
- un ID stable ;
- son texte ;
- une catégorie ;
- éventuellement une affinité avec certaines humeurs ;
- éventuellement un statut rare.

Ne jamais utiliser uniquement sa position dans une liste comme identité persistante.

---

# 3 — IDS DES 100 PHRASES HISTORIQUES

Attribuer définitivement :

`legacy_smalltalk_001`
…
`legacy_smalltalk_100`

Une fois publiés :
- ne jamais les renuméroter ;
- si une phrase disparaît plus tard, son ID n’est jamais recyclé.

---

# 4 — CATÉGORIES

Prévoir au minimum :

- GENERAL
- SUCCESS
- ERROR
- STREAK
- HESITATION
- ABSURD
- SELF
- SMART
- FINISH
- RETURN
- RARE
- FAB
- TAQUIN

Les catégories servent surtout à construire intelligemment les pools de sélection.

---

# 5 — MÉMOIRE INDIVIDUELLE 48 HEURES

Chaque phrase possède son historique :

`lastUsedAt[phraseId]`

Quand une phrase est réellement sélectionnée pour être prononcée :

`lastUsedAt[id] = now`

Règle :
- jamais utilisée → disponible immédiatement ;
- `now - lastUsedAt[id] < 48 h` → indisponible normalement ;
- à partir de 48 h → disponible.

---

# 6 — PERSISTANCE

La mémoire doit survivre à :
- fermeture application ;
- destruction Activity ;
- relance ;
- redémarrage Android ;
- nouvelle partie.

Utiliser un stockage léger cohérent avec le projet.

Pour quelques centaines de timestamps : ne pas introduire inutilement une grosse base SQL/Room.

---

# 7 — LAST PHRASE ID

Persister également :

`lastPhraseId`

Règle absolue :
une phrase ne doit jamais être prononcée deux fois consécutivement.

Cela reste vrai :
- après 48 h ;
- après fallback ;
- après pool épuisé ;
- après relance application.

---

# 8 — ALGORITHME NORMAL DE PIOCHE

Flux :

`événement joueur`
→ mise à jour contexte joueur
→ détermination humeur Prof
→ catégories compatibles
→ construction pool pondéré
→ retrait phrases <48 h
→ retrait `lastPhraseId`
→ tirage
→ enregistrement `lastUsedAt`
→ affichage bulle
→ Pierre parle.

Interdit de conserver un simple :

`Random.nextInt(allPhrases.size)`

---

# 9 — CONTEXTE JOUEUR

Créer un état interne léger :

```kotlin
data class ProfessorPlayerContext(
    val mastery: Int,
    val impulsivity: Int,
    val momentum: Int
)
```

Trois axes suffisent.

## MASTERY

Monte avec :
- bonnes déductions ;
- séries ;
- grilles terminées ;
- difficulté élevée ;
- jeu sans aide.

Une seule erreur ne doit pas l’effondrer.

## IMPULSIVITY

Monte surtout lorsque plusieurs erreurs sont :
- très rapides ;
- successives ;
- sans réflexion apparente ;
- répétitives.

Une erreur après une longue réflexion ≠ hasard.

## MOMENTUM

Monte avec :
- réussite ;
- streak ;
- belle déduction.

Descend avec :
- série d’erreurs ;
- blocage prolongé ;
- perte de rythme.

Revient naturellement vers le neutre.

---

# 10 — NE PAS JUGER SUR UN SEUL ÉVÉNEMENT

1 erreur ≠ joueur imprudent.

Exemple :
4 essais faux très rapides peuvent fortement augmenter `impulsivity`.

Mais :
1 erreur après 20 secondes de réflexion doit plutôt produire ENCOURAGING/PEDAGOGICAL, pas une grosse moquerie.

---

# 11 — HUMEUR DU PROF

L’humeur est un état de sélection de ton, pas une émotion humaine simulée.

Prévoir :

- NEUTRAL
- PROUD
- IMPRESSED
- TAQUIN
- PEDAGOGICAL
- ENCOURAGING
- CURIOUS

Elle reste principalement invisible.

Pas de grosse jauge “humeur du Prof”.

---

# 12 — CONTEXTE → HUMEUR

Exemples :

- réussite normale → NEUTRAL / PROUD ;
- belle série → IMPRESSED ;
- déduction complexe réussie → IMPRESSED + SMART ;
- plusieurs grilles faciles terminées très facilement → TAQUIN ;
- plusieurs essais faux extrêmement rapides → TAQUIN + PEDAGOGICAL ;
- erreur après réflexion → ENCOURAGING ;
- plusieurs erreurs en très difficile mais persévérance → ENCOURAGING / PROUD ;
- plusieurs demandes d’aide → PEDAGOGICAL.

Exemples de ton :
- « Bon… tu comptes vraiment continuer à terroriser les grilles faciles ? »
- « Ah, aujourd’hui on teste la méthode scientifique : cliquer partout et observer les dégâts ? »

---

# 13 — DIFFICULTÉ MODIFIE L’INTERPRÉTATION

Une erreur en difficulté élevée ne vaut pas une erreur en facile.

Facile + maîtrise élevée + victoire très rapide :
→ TAQUIN possible.

Infernal + erreurs + persévérance :
→ ENCOURAGING / PROUD.

---

# 14 — RETOUR AU NEUTRE

Le Prof ne garde pas rancune.

Après quelques actions normales :
- impulsivity redescend ;
- momentum revient vers 0 ;
- mood revient vers NEUTRAL.

Une mauvaise minute ne provoque pas 20 minutes de sarcasmes.

---

# 15 — HUMEUR = MÉMOIRE COURTE

Le cooldown phrases persiste 48 h.

L’humeur représente surtout la partie récente :
- session-scoped possible ;
- ou persistée légèrement avec forte décroissance temporelle.

Après longue absence :
`mood ≈ NEUTRAL`.

---

# 16 — HUMEUR = POIDS, PAS PHRASE DIRECTE

IMPRESSED :
- SMART fortement favorisé ;
- SUCCESS favorisé ;
- STREAK favorisé.

TAQUIN :
- TAQUIN favorisé ;
- ABSURD favorisé ;
- ERROR possible selon contexte.

PEDAGOGICAL :
- GENERAL ;
- HESITATION ;
- SUCCESS doux.

ENCOURAGING :
- SUCCESS ;
- GENERAL ;
- HESITATION.

Puis :
cooldown 48 h
+ lastPhraseId
+ tirage.

---

# 17 — PHRASES RARES

Catégorie RARE.

Probabilité maximale cible :
3 à 5 %.

Cooldown 48 h identique.

Une phrase rare doit rester réellement rare.

---

# 18 — FALLBACK SI POOL VIDE

Ne jamais bloquer Prof Gecko.

Ordre :
pool contextuel
→ pool compatible voisin
→ GENERAL
→ fallback forcé.

Exemples :
- SMART vide → SUCCESS → GENERAL ;
- HESITATION vide → GENERAL ;
- TAQUIN vide → ABSURD → GENERAL.

---

# 19 — FALLBACK FORCÉ OLDEST-FIRST

Si toutes les phrases compatibles sont sous cooldown :
- ne pas choisir totalement au hasard ;
- préférer les phrases utilisées depuis le plus longtemps ;
- trier par `lastUsedAt` ;
- prendre les plus anciennes ;
- éventuellement tirer parmi un petit sous-pool ancien.

Toujours exclure `lastPhraseId`.

---

# 20 — NE JAMAIS CLEAR L’HISTORIQUE GLOBAL

Pool vide ≠ `clear(allHistory)`.

Le fallback forcé ignore temporairement le cooldown uniquement pour ce tirage.

La mémoire globale reste intacte.

---

# 21 — BULLE POUR PETITES PHRASES

Petite phrase :
phrase
→ bulle Prof Gecko
→ Pierre prononce **exactement la même chaîne**.

La bulle et la voix utilisent la même source texte.

---

# 22 — FERMETURE AUTO BULLE

Pour les petites phrases uniquement :

Pierre commence
→ bulle visible
→ Pierre termine réellement
→ attendre 1 seconde
→ fermer la bulle.

Le délai commence après le vrai callback de fin de parole.

Aucune estimation par longueur de texte.

---

# 23 — TIMER OBSOLÈTE

Si une nouvelle phrase commence pendant cette seconde :
- annuler l’ancienne fermeture ;
- un callback d’une ancienne phrase ne ferme jamais une nouvelle bulle.

Utiliser token/generation/speech id.

---

# 24 — BULLES PÉDAGOGIQUES NON AUTO-FERMÉES

La fermeture auto concerne :
- small talk ;
- humour ;
- encouragement simple ;
- phrase taquine ;
- réaction contextuelle courte.

Elle ne concerne pas :
- indice de résolution ;
- explication de technique ;
- Gecko X-Wing ;
- hypothèse ;
- double hypothèse ;
- instruction demandant une action ;
- étape nécessitant une nouvelle pression Prof ;
- texte pédagogique devant rester lisible.

---

# 25 — PROF NON INTERROMPU

Une petite phrase aléatoire ne coupe jamais Pierre en pleine phrase.

Pendant parole prioritaire :
small talk → ignoré ou reporté.

Réutiliser l’état/priorité/génération existants plutôt que créer un second système concurrent.

---

# 26 — EXCEPTIONS AUX INTERRUPTIONS

Conserver :
- joueur réappuie explicitement sur Prof ;
- fin de partie garde sa priorité prévue ;
- aucune animation décorative ne stoppe Pierre.

---

# 27 — PROF_PARLE

Préserver :

`assets/prof/ProfParle.mp4`

La présente mission ne refond pas la vidéo Prof.

Elle modifie principalement :
- catalogue ;
- sélection ;
- contexte ;
- humeur ;
- historique ;
- bulle.

Préserver le pipeline vidéo validé v0.10.15-dev.

---

# 28 — ARCHITECTURE

Éviter d’empiler tout dans `MainActivity.kt`.

Séparer au minimum :
- ProfessorPhrase
- ProfessorPhraseCatalog
- ProfessorPhraseHistory
- ProfessorPhraseSelector
- ProfessorPlayerContext
- ProfessorMoodPolicy

---

# 29 — HORLOGE TESTABLE

Ne pas utiliser `System.currentTimeMillis()` partout.

Injecter une source de temps.

Production :
`System.currentTimeMillis()`

Tests :
FakeClock.

Scénarios :
- +1 min ;
- +2 h ;
- +47 h 59 ;
- +48 h ;
- +3 jours.

---

# 30 — HASARD TESTABLE

Injecter le random.

Production :
Random.

Tests :
FakeRandom.

Aucun test ne doit être aléatoirement rouge.

---

# 31 — DÉDUPLICATION CORPUS

Tests obligatoires :
- IDs uniques ;
- textes non vides ;
- IDs non vides ;
- catégories valides ;
- aucun doublon exact après normalisation.

Normalisation :
- lowercase ;
- trim ;
- espaces multiples ;
- ponctuation.

---

# 32 — PHRASES TROP PROCHES

Ajouter un diagnostic simple :
- tokens communs ;
- Jaccard ;
- Levenshtein ;
- ratio simple.

Warning suffisant pour cas ambigus.

Exemple à détecter :
“Très bien, continue comme ça.”
vs
“Super, continue comme ça.”

---

# 33 — CORPUS V2 : 200 NOUVELLES PHRASES

## GENERAL — 40

prof_general_001 — Attention, cette grille commence à comprendre que vous êtes dangereux.
prof_general_002 — Je viens d’ouvrir officiellement la séance extraordinaire du conseil des chiffres.
prof_general_003 — Cette case a demandé un avocat. C’est rarement bon signe.
prof_general_004 — J’ai vérifié le règlement : réfléchir est encore parfaitement autorisé.
prof_general_005 — La grille fait la fière, mais je vois bien qu’elle commence à douter.
prof_general_006 — Nous avons neuf chiffres et beaucoup trop de suspects.
prof_general_007 — J’annonce l’ouverture des négociations avec la colonne de gauche.
prof_general_008 — Aucun chiffre ne sort d’ici avant d’avoir montré ses papiers.
prof_general_009 — Le tableau est calme. Beaucoup trop calme.
prof_general_010 — J’ai mis mon air professionnel. La situation est donc sous contrôle.
prof_general_011 — Aujourd’hui encore, aucun chiffre ne sera placé sur simple rumeur.
prof_general_012 — Je propose une stratégie révolutionnaire : éviter les bêtises.
prof_general_013 — Il y a une solution. Elle a juste décidé de se faire désirer.
prof_general_014 — Ne vous fiez pas à cette petite case innocente. Elles savent très bien jouer la comédie.
prof_general_015 — J’ai convoqué les candidats. Certains vont devoir rendre leur badge.
prof_general_016 — Cette grille possède manifestement un service juridique très performant.
prof_general_017 — Voilà une situation qui réclame mon expression numéro quatre : professeur préoccupé mais élégant.
prof_general_018 — Nous entrons dans une zone à haute concentration de logique.
prof_general_019 — Je vais noter que tout se passe exactement selon mon plan. Quel plan ? Secret professionnel.
prof_general_020 — Quelques chiffres commencent déjà à préparer leurs valises.
prof_general_021 — Cette grille ne sait pas encore que nous sommes deux contre elle.
prof_general_022 — Je vous laisse le cerveau, je m’occupe de l’autorité pédagogique.
prof_general_023 — Encore quelques déductions et nous pourrons facturer le déménagement aux chiffres.
prof_general_024 — J’entends presque cette case dire : ce n’est pas moi.
prof_general_025 — L’enquête progresse. Personne ne quitte la ligne.
prof_general_026 — J’aurais bien sorti ma règle de professeur, mais elle ne résout absolument rien.
prof_general_027 — Chaque candidat a droit à la présomption d’innocence. Pas longtemps.
prof_general_028 — Cette grille vient officiellement de passer du statut mystérieux à légèrement suspect.
prof_general_029 — J’ai lu le dossier. Il manque juste la solution, détail regrettable.
prof_general_030 — La logique est prête. Le professeur aussi. La grille beaucoup moins.
prof_general_031 — J’ai demandé une réunion aux chiffres. Ils ont refusé de répondre.
prof_general_032 — Voilà précisément le genre de moment où un professeur regarde très fort son élève.
prof_general_033 — Ce dossier avance. Je vais prévenir le ministère des cases carrées.
prof_general_034 — Je surveille particulièrement le 7. Aucune raison précise, mais il le sait.
prof_general_035 — La grille tente une stratégie d’intimidation par multiplication des cases vides.
prof_general_036 — Rien d’alarmant : seulement plusieurs dizaines de petits problèmes parfaitement organisés.
prof_general_037 — J’ai apporté mon expertise et un air convaincu. Nous sommes équipés.
prof_general_038 — Je sens une avancée imminente. Ou une excellente occasion de faire semblant.
prof_general_039 — Nous allons résoudre ceci avec méthode, dignité et éventuellement quelques grimaces.
prof_general_040 — Je rappelle que le hasard n’a pas été invité à ce cours.

## SUCCESS — 30

prof_success_001 — Validé ! Tampon officiel du Prof Gecko.
prof_success_002 — Cette réponse vient d’obtenir son permis de séjour dans la grille.
prof_success_003 — Très bien ! Le tribunal des chiffres confirme.
prof_success_004 — Accepté à l’unanimité, y compris par moi.
prof_success_005 — Magnifique. Même mon stylo imaginaire vient de signer.
prof_success_006 — Exact ! La case a cessé toute résistance.
prof_success_007 — Cette déduction mérite au moins une petite musique triomphale.
prof_success_008 — Autorisation accordée : ce chiffre peut rester.
prof_success_009 — Décision impeccable. Je transmets au service des félicitations.
prof_success_010 — Celui-là est tellement juste qu’il pourrait servir de témoin.
prof_success_011 — Adopté ! Aucun recours possible.
prof_success_012 — Joli travail. Le dossier vient de perdre une page entière de complications.
prof_success_013 — Confirmation officielle : votre cerveau fonctionne très bien aujourd’hui.
prof_success_014 — Voilà un chiffre qui a rempli correctement tous ses formulaires.
prof_success_015 — Très bon placement. Je n’ai aucune objection pédagogique.
prof_success_016 — Accepté ! Je range immédiatement mon tampon « douteux ».
prof_success_017 — Ce coup-là passe directement en commission sans débat.
prof_success_018 — Résultat net, sans bavure et sans réunion supplémentaire.
prof_success_019 — Très réussi. Même les cases voisines ont l’air convaincues.
prof_success_020 — Décision approuvée par le Haut Conseil du Sudoku.
prof_success_021 — Celui-là peut prendre possession des lieux.
prof_success_022 — Voilà ce que j’appelle un chiffre administrativement irréprochable.
prof_success_023 — Très bien ! Une ambiguïté vient de perdre son emploi.
prof_success_024 — C’est signé, classé et rangé.
prof_success_025 — Excellent. Je retire cette affaire du tableau des suspects.
prof_success_026 — Votre déduction vient d’être promue.
prof_success_027 — Autorisation exceptionnelle de sourire : c’était parfaitement juste.
prof_success_028 — Bien joué. La grille vient de perdre une bataille réglementaire.
prof_success_029 — Je confirme avec toute la gravité de mon poste : c’est bon.
prof_success_030 — Une réponse comme celle-là mérite presque un bureau avec une plaque sur la porte.

## ERROR — 25

prof_error_001 — Refusé par la commission. Le dossier était pourtant joliment présenté.
prof_error_002 — Ah. Ce chiffre vient d’être expulsé pour fausse déclaration.
prof_error_003 — Négatif. Il n’avait même pas rempli le formulaire B-7.
prof_error_004 — Ce candidat est recalé avant même l’entretien.
prof_error_005 — Petite erreur. Je détruis les preuves et nous continuons.
prof_error_006 — Non recevable. La défense peut se rasseoir.
prof_error_007 — Ce chiffre était très sûr de lui. C’est souvent mauvais signe.
prof_error_008 — Refus administratif immédiat.
prof_error_009 — Je crains que ce candidat n’ait falsifié son CV.
prof_error_010 — Verdict : innocent de toute possibilité.
prof_error_011 — Ce chiffre vient d’être prié de quitter les locaux.
prof_error_012 — Mauvaise porte. Il cherchait probablement la grille d’à côté.
prof_error_013 — Celui-là n’a absolument rien à faire ici, malgré son costume.
prof_error_014 — Non homologué par les autorités compétentes.
prof_error_015 — Petit incident diplomatique entre vous et cette case.
prof_error_016 — Requête rejetée. Vous pouvez déposer un nouvel essai immédiatement.
prof_error_017 — Ce chiffre s’était inventé une carrière dans cette case.
prof_error_018 — Faux départ. Le professeur n’a rien vu. Officiellement.
prof_error_019 — Ce candidat est éliminé de l’émission.
prof_error_020 — Mauvaise réponse, excellente tentative d’infiltration.
prof_error_021 — Le comité vient de dire non avec beaucoup de sérieux.
prof_error_022 — Celui-ci repart avec ses affaires dans un petit carton.
prof_error_023 — Je classe ceci dans la catégorie : idée intéressante mais témoin peu fiable.
prof_error_024 — La grille proteste. Pour une fois, elle a raison.
prof_error_025 — Erreur constatée. Sanction : exactement zéro minute de retenue.

## STREAK — 20

prof_streak_001 — Encore juste ! Nous allons devoir renforcer la sécurité de la grille.
prof_streak_002 — Nouvelle réussite. Le service des erreurs demande votre photo.
prof_streak_003 — Vous enchaînez tellement bien que je vais demander une chaise.
prof_streak_004 — Encore validé ! La direction commence à s’inquiéter.
prof_streak_005 — Nouvelle bonne réponse. J’ouvre officiellement un dossier à votre nom.
prof_streak_006 — Cette série devient statistiquement agaçante pour la grille.
prof_streak_007 — Encore une ! Les candidats restants commencent à parler.
prof_streak_008 — Vous empilez les bonnes décisions comme des dossiers sur mon bureau.
prof_streak_009 — Je vais bientôt manquer de tampons « approuvé ».
prof_streak_010 — Encore exact. Le suspense devient franchement à sens unique.
prof_streak_011 — Belle série. J’ai demandé des renforts au département des compliments.
prof_streak_012 — Vous venez encore de réduire le chômage des cases vides.
prof_streak_013 — Nouvelle réussite. C’est une véritable opération de nettoyage.
prof_streak_014 — La grille réclame maintenant un temps mort.
prof_streak_015 — Vous êtes lancé. J’évite de me mettre devant.
prof_streak_016 — Encore bon ! Le comité n’arrive plus à suivre les dossiers.
prof_streak_017 — Votre série vient officiellement d’obtenir le statut de phénomène.
prof_streak_018 — Nouvelle validation. Je commence à soupçonner un abonnement premium à la logique.
prof_streak_019 — Toujours juste. Mon rôle devient essentiellement cérémoniel.
prof_streak_020 — Encore une réussite. Je vais devoir inventer de nouveaux diplômes.

## HESITATION — 15

prof_hesitate_001 — Cette case a l’air de vouloir vous faire signer quelque chose. Méfiance.
prof_hesitate_002 — Plusieurs candidats se présentent. Nous allons examiner leurs dossiers un par un.
prof_hesitate_003 — Voilà un joli embouteillage de possibilités.
prof_hesitate_004 — Cette situation mérite une enquête plutôt qu’un lancer de pièce.
prof_hesitate_005 — Je vois au moins deux suspects qui transpirent.
prof_hesitate_006 — Lorsque tout semble possible, quelqu’un ment forcément.
prof_hesitate_007 — Commençons par chercher celui qui a le moins bon alibi.
prof_hesitate_008 — Le prochain indice pourrait venir d’une autre zone du dossier.
prof_hesitate_009 — Ne signez rien pour l’instant, les candidats négocient encore.
prof_hesitate_010 — Cette affaire manque de preuves. Allons en chercher ailleurs.
prof_hesitate_011 — Plusieurs réponses font les innocentes. C’est précisément le problème.
prof_hesitate_012 — Nous avons besoin d’un témoin supplémentaire avant de conclure.
prof_hesitate_013 — Je recommande une petite tournée des lignes voisines.
prof_hesitate_014 — Cette case ne donnera rien sous la pression. Interrogeons ses voisines.
prof_hesitate_015 — Le dossier n’est pas mûr. Gardons-le ouvert.

## ABSURD — 15

prof_absurd_001 — J’ai nommé le 3 responsable des ressources humaines. Grave erreur.
prof_absurd_002 — La colonne cinq vient de déposer une demande de congés.
prof_absurd_003 — Le 8 exige désormais qu’on l’appelle Monsieur Huit.
prof_absurd_004 — J’ai reçu une lettre de protestation signée par trois cases vides.
prof_absurd_005 — Le conseil d’administration des chiffres s’est encore réuni sans moi.
prof_absurd_006 — J’ai mis un 2 en retenue. Il prétend avoir un mot de ses parents.
prof_absurd_007 — Selon le règlement intérieur, les 6 doivent arrêter de se faire passer pour des 9.
prof_absurd_008 — La photocopieuse refuse toujours de reproduire les solutions.
prof_absurd_009 — J’ai tenté de négocier avec la grille. Elle demande une augmentation.
prof_absurd_010 — Le 1 se plaint encore qu’on ne le prend pas assez au sérieux.
prof_absurd_011 — J’ai convoqué le 5. Il est arrivé avec un avocat et deux témoins.
prof_absurd_012 — La colonne deux souhaite changer de bureau.
prof_absurd_013 — Un chiffre a disparu du registre. J’accuse l’administration.
prof_absurd_014 — J’avais préparé un PowerPoint de quatre-vingt-sept diapositives. Vous avez de la chance.
prof_absurd_015 — J’interdis formellement aux cases de comploter pendant les heures de cours.

## SELF — 15

prof_self_001 — J’ai choisi l’enseignement pour le prestige. Il semblerait qu’on m’ait mal renseigné.
prof_self_002 — Mon bureau officiel mesure exactement zéro centimètre carré.
prof_self_003 — J’ai une excellente mémoire, sauf concernant les choses que j’oublie.
prof_self_004 — J’ai demandé une augmentation. On m’a proposé davantage de grilles.
prof_self_005 — Mon contrat stipule que je dois paraître compétent en toutes circonstances.
prof_self_006 — J’utilise parfois des mots compliqués uniquement pour impressionner la direction.
prof_self_007 — Je possède un magnifique diplôme. Je ne sais plus très bien où je l’ai imprimé.
prof_self_008 — On me surnomme le professeur. C’était plus court que directeur général des cases.
prof_self_009 — J’ai développé une technique secrète : regarder ailleurs quand je me trompe.
prof_self_010 — J’ai déjà pensé à prendre ma retraite. Puis j’ai vu le prix des dossiers administratifs.
prof_self_011 — Je suis payé à la déduction. Enfin, c’est ce que j’aimerais croire.
prof_self_012 — J’ai un talent rare : savoir hocher la tête au moment approprié.
prof_self_013 — Ma spécialité universitaire ? Faire croire que j’avais prévu le résultat.
prof_self_014 — Je conserve tous mes grands discours dans un tiroir qui n’existe pas.
prof_self_015 — Mon autorité repose essentiellement sur ma voix et mon sérieux parfaitement théâtral.

## SMART — 10

prof_smart_001 — Oh ! Celle-là n’était pas évidente. Très belle lecture.
prof_smart_002 — Voilà une déduction qui mérite d’être inscrite au procès-verbal.
prof_smart_003 — Vous venez de contourner le piège sans même lui dire bonjour.
prof_smart_004 — Très élégant. Vous avez réduit plusieurs possibilités d’un seul raisonnement.
prof_smart_005 — Ce coup était discret, mais redoutablement efficace.
prof_smart_006 — Excellente exploitation des contraintes. Là, je valide très sérieusement.
prof_smart_007 — Vous avez trouvé la petite fissure dans tout le raisonnement.
prof_smart_008 — Belle anticipation. Vous aviez déjà deux étapes d’avance.
prof_smart_009 — Voilà exactement le genre de déduction qui simplifie soudain toute une zone.
prof_smart_010 — Très fort. Vous n’avez pas seulement trouvé une réponse, vous avez démonté le mécanisme.

## FINISH — 10

prof_finish_001 — Affaire classée ! Je ferme officiellement le dossier.
prof_finish_002 — Terminé. Tous les chiffres ont été interrogés et correctement rangés.
prof_finish_003 — Mission accomplie. La grille peut rendre son badge.
prof_finish_004 — Fin de l’enquête ! Aucun suspect ne court encore.
prof_finish_005 — Grille résolue. Le conseil pédagogique applaudit debout, en théorie.
prof_finish_006 — C’est terminé ! Je signe le certificat de victoire.
prof_finish_007 — Voilà. Cette grille appartient désormais aux archives.
prof_finish_008 — Dossier clos, preuves rangées, professeur satisfait.
prof_finish_009 — Victoire complète. Même le service des réclamations n’a rien trouvé.
prof_finish_010 — Travail terminé ! Je vous accorde officiellement le droit de regarder la grille avec fierté.

## RETURN — 10

prof_return_001 — Ah, vous revoilà. J’avais commencé à distribuer les rôles aux chiffres.
prof_return_002 — Retour au bureau ! Le dossier est resté exactement là où vous l’aviez laissé.
prof_return_003 — Vous tombez bien. La grille vient justement de nier toutes les accusations.
prof_return_004 — Reprise de séance. Tout le monde debout… non, finalement restez installé.
prof_return_005 — Vous voilà ! J’ai empêché les chiffres de toucher à quoi que ce soit.
prof_return_006 — Le cours reprend. J’ai même remis mon air officiel.
prof_return_007 — Content de vous revoir. J’avais presque commencé une réunion sans vous.
prof_return_008 — Retour aux affaires ! Les suspects sont toujours dans la salle.
prof_return_009 — Très bien, reprenons avant que la grille ne profite de notre absence.
prof_return_010 — Vous êtes de retour. Parfait, je peux arrêter de faire semblant de remplir des formulaires.

## RARE — 10

prof_rare_001 — Information confidentielle : je n’ai absolument aucune idée d’où j’ai rangé mon bureau.
prof_rare_002 — C’est historique. Le comité vient d’être d’accord du premier coup.
prof_rare_003 — Je viens de recevoir une médaille imaginaire. Elle est étonnamment lourde.
prof_rare_004 — Nous interrompons ce cours pour une annonce importante : tout va étonnamment bien.
prof_rare_005 — Quelqu’un a remplacé mon café par de la logique pure. Je vais déposer plainte.
prof_rare_006 — Pour une fois, le formulaire A-38 était dans le bon tiroir. Journée exceptionnelle.
prof_rare_007 — J’avais préparé un discours dramatique. La situation refuse de devenir dramatique.
prof_rare_008 — Le rectorat vient d’appeler. Ils n’ont rien compris non plus.
prof_rare_009 — Je déclare officiellement cette minute beaucoup trop efficace.
prof_rare_010 — Félicitations : vous venez de débloquer une phrase que même moi j’entends rarement.

---

# 34 — 4 PHRASES FAB

prof_fab_001 — Un jour, je me suis assis sur un mur en pleine journée… et là, tapis dans l’ombre, ces petites créatures super mignonnes étaient en train de me regarder.
prof_fab_002 — Le gecko sur l’église est-il plus croyant que moi ?
prof_fab_003 — J’égoutte mes pâtes avec une grille. Et oui… je suis carré !
prof_fab_004 — Si les geckos pouvaient voler, eh bien ils se feraient attraper par ceux qui, eux, ne volent pas !

Ces phrases peuvent être rares ou semi-rares.

---

# 35 — 5 PHRASES TAQUINES

prof_taquin_001 — Même ma grand-mère peut mieux faire !
prof_taquin_002 — Là, même un gecko avec des moufles faisait mieux.
prof_taquin_003 — Je vais faire semblant de ne pas avoir vu ça.
prof_taquin_004 — Bon… on va appeler ça une expérience pédagogique.
prof_taquin_005 — Magnifique. Totalement faux, mais magnifique.

Éligibilité principale :
- impulsivity élevée ;
- ou joueur très à l’aise + petite erreur inhabituelle ;
- ou contexte clairement humoristique.

Une erreur réfléchie sur niveau difficile doit favoriser ENCOURAGING, pas TAQUIN.

---

# 36 — EXTENSIBILITÉ CONTEXTUELLE

Le système doit permettre plus tard des phrases comme :

- « Bon… tu comptes vraiment continuer à terroriser les grilles faciles ? »
- « Ah, aujourd’hui on teste la méthode scientifique : cliquer partout et observer les dégâts ? »
- « Celle-là était vicieuse. Ton raisonnement était pourtant intéressant. »

Ne jamais coder ce comportement par comparaison du texte.

Tout doit venir :
événement → contexte → humeur → catégories → selector.

---

# 37 — ÉVÉNEMENTS DE HAUT NIVEAU

Le contexte joueur doit pouvoir recevoir :

- CORRECT_MOVE
- WRONG_MOVE
- SMART_MOVE
- STREAK_STARTED
- STREAK_CONTINUED
- LONG_THINKING
- RAPID_WRONG_MOVE
- HINT_REQUESTED
- LEVEL_COMPLETED
- GAME_STARTED
- RETURN_AFTER_PAUSE

Rester simple et non invasif.

---

# 38 — DÉTECTION APPROXIMATIVE DE L’IMPULSIVITÉ

Utiliser uniquement les événements du jeu.

Plusieurs erreurs
+ intervalle très court
+ absence de réflexion intermédiaire
→ impulsivity monte.

Une seule erreur rapide fausse ne suffit jamais.

---

# 39 — PAS DE MOQUERIE EN BOUCLE

Même en humeur TAQUIN :
ne pas enchaîner TAQUIN/TAQUIN/TAQUIN/TAQUIN.

Après une phrase taquine :
- réduire temporairement son poids ;
- varier les catégories ;
- le cooldown 48 h reste actif.

---

# 40 — TESTS OBLIGATOIRES

## Corpus
- total = 309 ;
- IDs uniques ;
- aucun texte exact en double après normalisation ;
- catégories valides ;
- 100 historiques conservées ;
- 200 V2 ;
- 4 FAB ;
- 5 TAQUIN.

## Cooldown
- jamais utilisée → disponible ;
- 30 s → indisponible ;
- 47 h 59 → indisponible ;
- 48 h → disponible.

## Persistance
Phrase utilisée → recréer store → toujours sous cooldown.

## Last phrase
`lastPhraseId` ne ressort jamais immédiatement.

## Pool vide
Toujours trouver une phrase.

## Oldest-first
Fallback forcé préfère la phrase très ancienne à celle utilisée il y a quelques minutes.

## Mood
- série de réussites → IMPRESSED ;
- plusieurs erreurs très rapides → TAQUIN/PEDAGOGICAL ;
- erreur réfléchie → pas TAQUIN immédiat ;
- plusieurs erreurs sur difficulté forte → ENCOURAGING ;
- maîtrise forte + victoires faciles répétées → TAQUIN possible ;
- actions normales → retour progressif NEUTRAL.

## Bulle
- fin réelle Pierre + 1 s → fermeture ;
- timer ancien ne ferme jamais nouvelle bulle ;
- explication structurée Prof reste ouverte.

---

# 41 — LOGS

Ajouter sans spam frame :

`PROF_CONTEXT mastery=72 impulsivity=18 momentum=64 mood=IMPRESSED`

`PROF_PHRASE_POOL requested=SMART eligible=7`

`PROF_PHRASE_SELECTED id=prof_smart_004 category=SMART`

`PROF_PHRASE_FALLBACK requested=SMART fallback=SUCCESS`

`PROF_PHRASE_FORCED_OLDEST id=...`

`PROF_QUICK_BUBBLE_CLOSE_SCHEDULED delayMs=1000`

---

# 42 — NON-RÉGRESSION PROF EXISTANT

Conserver absolument :
- Prof Gecko résolution ;
- HumanSolver ;
- HypothesisSolver ;
- Gecko X-Wing ;
- ProfParle ;
- Pierre Piper ;
- fallback Android TTS ;
- animations ;
- écran ;
- musique ;
- célébration ;
- géométrie ;
- pipeline SurfaceTexture serial validé ;
- priorité Pierre ;
- PNG fallback ;
- menu ⚙️ / journal média ;
- bulle overlay ;
- grille immuable.

Cette mission ajoute de la vie.
Elle ne remplace pas l’intelligence pédagogique existante.

---

# 43 — FAB COPILOT

Toute intervention de code doit synchroniser dans le même cycle :
- `ordres-de-mission.md`
- `brain.md`
- `brainmap.md`
- `debughistorical.md`
- `todo.md`

Documenter :
- système 309 phrases ;
- IDs ;
- historique 48 h ;
- fallback oldest-first ;
- contexte joueur ;
- humeur ;
- anti-moquerie injuste ;
- bulle +1 s ;
- tests ;
- résultats build.

TDD RED avant production.

---

# 44 — CRITÈRES D’ACCEPTATION FINAUX

- [ ] 100 historiques toujours présents
- [ ] 200 nouvelles V2
- [ ] 4 FAB
- [ ] 5 TAQUIN
- [ ] total 309
- [ ] ID stable par phrase
- [ ] aucun doublon exact
- [ ] cooldown individuel 48 h
- [ ] cooldown persistant
- [ ] lastPhraseId persistant
- [ ] aucune répétition immédiate
- [ ] fallback oldest-first
- [ ] RARE réellement rare
- [ ] contexte joueur
- [ ] mastery
- [ ] impulsivity
- [ ] momentum
- [ ] mood adaptatif
- [ ] mood revient au neutre
- [ ] difficulté prise en compte
- [ ] erreur réfléchie ≠ moquerie injuste
- [ ] jeu impulsif peut déclencher TAQUIN
- [ ] réussite difficile peut déclencher IMPRESSED
- [ ] victoires faciles répétées peuvent permettre TAQUIN
- [ ] petites phrases dans bulle
- [ ] texte bulle = texte prononcé
- [ ] bulle simple ferme 1 s après vraie fin Pierre
- [ ] ancien timer n’éteint jamais nouvelle bulle
- [ ] bulles pédagogiques restent ouvertes
- [ ] small talk n’interrompt pas Prof
- [ ] ProfParle continue de fonctionner
- [ ] tests unitaires verts
- [ ] APK vert
- [ ] cinq fichiers FAB Copilot synchronisés

---

# 45 — PHILOSOPHIE FINALE

Prof Gecko ne doit plus être :

`un Random.nextInt() avec une voix`

Il doit devenir :

`un personnage qui observe + se souvient + adapte son ton + reste bienveillant + peut taquiner lorsque le contexte le justifie + sait redevenir sérieux lorsqu’il enseigne`

Règle finale :

> « Taquiner le comportement, jamais rabaisser le joueur. »

Et surtout :

> « Une bonne blague doit sembler avoir été déclenchée par ce qui vient réellement de se passer. »


<!-- GECKO-037-REMOVE-RECORDED-ENCOURAGEMENTS-2026-09-27 -->
# 46 — RETIRER LES ANCIENS ENCOURAGEMENTS ENREGISTRÉS

Décision Fab : supprimer l’ancien système d’encouragements provenant du fichier enregistré :

`assets/audio/encouragements/master/Voix_encouragements.mp3`

Ce système ne doit plus coexister avec Pierre / GECKO-037.

À retirer :
- source `RECORDED` ;
- alternance aléatoire RECORDED / PIERRE ;
- `EncouragementSelector` dédié aux 13 segments enregistrés ;
- `EncouragementSegment` ;
- `AssetAudioCatalog.ENCOURAGEMENT_MASTER` ;
- `AssetAudioCatalog.ENCOURAGEMENTS` ;
- lecture segmentée via `playVoiceSegment()` pour ces encouragements ;
- asset `Voix_encouragements.mp3` lui-même ;
- tests qui imposent encore la coexistence RECORDED + PIERRE.

Nouveau contrat immédiat :
- encouragement vocal simple = **Pierre uniquement** ;
- conserver `SpeechOrigin.ENCOURAGEMENT` et la priorité non préemptive existante ;
- conserver ProfParle / PNG fallback actuel ;
- conserver le callback `onFinished` nécessaire au déclenchement de la musique de célébration ;
- FX OFF conserve le comportement silencieux actuel si c’est le contrat déjà en place ;
- les autres MP3 (intro, célébration, musique) ne sont pas concernés.

À terme dans GECKO-037, les encouragements Pierre doivent être absorbés par le nouveau catalogue/context selector afin d’éviter deux moteurs de phrases concurrents.

TDD :
- RED : une policy d’encouragement ne propose plus qu’une livraison PIERRE ;
- GREEN : supprimer tout chemin enregistré et l’asset MP3 sans casser Pierre ni la célébration.


<!-- GECKO-037-RECORDED-ENCOURAGEMENTS-GREEN-2026-09-27 -->
## 46 — GREEN : encouragements enregistrés retirés

RED #131 : échec attendu sur `EncouragementDeliveryPolicy` / `EncouragementDelivery` absents.

Correction appliquée :
- encouragement delivery = PIERRE uniquement ;
- suppression du choix aléatoire RECORDED/PIERRE ;
- suppression du selector des 13 segments ;
- suppression de `EncouragementSegment`, `ENCOURAGEMENT_MASTER`, `ENCOURAGEMENTS` ;
- suppression du chemin `playVoiceSegment()` pour les encouragements ;
- suppression de `Voix_encouragements.mp3` du dépôt ;
- suppression des anciens tests imposant RECORDED ;
- conservation de `SpeechOrigin.ENCOURAGEMENT`, `onFinished`, ProfParle et priorité voix.
