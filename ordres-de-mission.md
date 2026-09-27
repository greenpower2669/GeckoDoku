# ISSUE INTEMPORELLE — SYNCHRONISATION PIERRE ↔ BULLE PROF

STATUT : **TECHNIQUEMENT GREEN — VALIDATION TÉLÉPHONE FAB EN ATTENTE**

BRANCHE :
`gecko-038-sudoku-mode`

HEAD DE DÉPART :
`e854a6d8096a5b16227f38e0d3f998075e9e5fe6`

TDD :
- RED commit `defaf7692d1b12a09001b3411fcb3e7d3ce5bbe6`
- CI #155 : failure attendue
- GREEN code `2935bd8566d32650758937b1ece1693f2b5f5bd2`
- CI #156 : success complet, tests + APK + AAB + artifact

CORRECTION :
- `ProfessorSimpleSpeechCoordinator`
- `speakSimpleProfessorBubble()`
- AMBIENT fixe corrigé
- STATS corrigé
- moteur 309 réutilise le chemin commun
- rejet tardif protégé
- fermeture vraie fin + ~1 s
- Prof pédagogique inchangé
- priorités SpeechOrigin inchangées
- aucune modification GeckoBoardView / SudokuEngine / géométrie

INVARIANT INTEMPOREL :
**Pierre parle ⇔ ProfessorBubbleView affiche exactement le texte parlé.**
`status` n'est jamais le conteneur d'un dialogue du Prof.

RESTE :
- test téléphone Fab sur aide automatique ;
- test téléphone Fab sur suggestion Sauver ;
- test téléphone Fab sur narration Stats ;
- vérifier visuellement fermeture naturelle après fin de Pierre.

Aucun merge main.
Aucune release sans GO explicite de Fab.
