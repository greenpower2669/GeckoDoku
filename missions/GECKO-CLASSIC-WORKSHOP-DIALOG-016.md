# Mission 016 — Corriger les commandes invisibles de l'atelier

## Constat utilisateur du 16 octobre 2026
Capture Samsung Android 16 : popup « Atelier Infernal • 0 grilles », compteur 0, recherche arrêtée/inactive, aucun dernier signal, **bouton FERMER seul**, aucun bouton DÉMARRER, Journal vidéo sans SEARCH_START. Ne pas accuser le solveur : aucune tentative n'a commencé.

## Cause et correction
`MainActivity.showInfernalWorkshop()`, `showInfernalWorkshopStats()` et `showInfernalWorkshopEntry()` mélangeaient `AlertDialog.Builder.setMessage` et `setItems` dans la même fenêtre. Le panneau Android affichait le texte mais masquait toutes les actions. Correction : séparer commande et informations. Menu Atelier visible en `setItems` uniquement ; premier choix DÉMARRER/ARRÊTER, deuxième COMPTEURS EN DIRECT ; ce dernier ouvre `showInfernalWorkshopLiveCounters()` en `setMessage` seul et rafraîchit à 1,5 seconde, boutons retour aux commandes et arrêt. Huit tailles accessibles dans un menu items-only. Les fiches candidat ont une liste actions test/notes/validation/rejet/réinitialisation et un bouton Détails qui affiche une fenêtre purement textuelle.

## Non-régression
`InfernalWorkshopDialogVisibilityTest.kt` fait quatre tests source pour détecter les mélanges, garantit présence du texte DÉMARRER, des menus de taille et candidat, ainsi que l'écran séparé de statistiques en direct.

## Recette exigée
Installer APK issu du build GREEN après ce commit sur Samsung. Ouvrir GeckoDoku classique > Réglages > Atelier : **DÉMARRER visible sans défilement caché**, toucher, voir notification Android et RECHERCHE ACTIVE, voir LANCÉES >0 (même si zéro grille), TERMINEES >0, bascule 5..12 ; Journal vidéo SERVICE_ACTIVE puis SEARCH_START/SEARCH_END ; bouton ARRÊTER fonctionne. Voir Bilan 5..12, et pour chaque candidat les commandes Tester/Valider. Sans cette recette, compilations vertes ne prouvent pas le correctif UI.

Ne pas fusionner main, ne pas publier Play ni Release stable.
