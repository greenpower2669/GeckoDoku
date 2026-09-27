# ISSUE INTEMPORELLE — SYNCHRONISATION PIERRE ↔ BULLE PROF

STATUT : CORRECTIF CODE APPLIQUÉ — ATTENTE CI GREEN.

BRANCHE : `gecko-038-sudoku-mode`
HEAD DE DÉPART : `e854a6d8096a5b16227f38e0d3f998075e9e5fe6`
RED : commit `defaf7692d1b12a09001b3411fcb3e7d3ce5bbe6`, CI #155 failure attendue.

Correction appliquée :
- abstraction pure `ProfessorSimpleSpeechCoordinator` ;
- chemin commun `speakSimpleProfessorBubble()` ;
- AMBIENT fixe corrigé ;
- STATS corrigé ;
- moteur 309 factorisé vers le même chemin ;
- refus tardif protégé ;
- Prof pédagogique inchangé ;
- politiques de priorité inchangées.

Étapes restantes :
1. obtenir CI GREEN ;
2. refaire audit exhaustif runtime ;
3. confirmer APK/AAB ;
4. rapporter à Fab ;
5. validation finale téléphone Fab.

Aucun merge main. Aucune release sans GO explicite.
