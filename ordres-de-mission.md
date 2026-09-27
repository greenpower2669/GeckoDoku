# ISSUE INTEMPORELLE — SYNCHRONISATION PIERRE ↔ BULLE PROF

STATUT : MISSION ACTIVE SUR `gecko-038-sudoku-mode`.

HEAD DE DÉPART RÉEL :
`e854a6d8096a5b16227f38e0d3f998075e9e5fe6`

Objectif :
- corriger `speakProfessorAmbient()` ;
- corriger `announcePlayerStats()` ;
- factoriser la parole simple du Prof ;
- conserver les SpeechOrigin et priorités ;
- ne pas toucher au Prof pédagogique ;
- aucun reflow de grille ;
- audit global de tous les chemins Pierre ;
- TDD RED → GREEN ;
- CI APK/AAB ;
- pas de merge main ;
- pas de release sans GO Fab.

Invariant final :
**Pierre parle ⇔ la bulle affiche exactement le texte parlé.**

Étape courante : RED du contrat pur `ProfessorSimpleSpeechCoordinator`.
