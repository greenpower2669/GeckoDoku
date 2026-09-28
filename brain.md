# GeckoDoku — brain

> Contrat fonctionnel courant, compact et reconstructible.
> Documentation détaillée : docs/GECKODOKU-FONCTIONNEMENT.md.
> Cartographie technique : brainmap.md.
> Archive froide avant restructuration : sauvegarde.md — ne pas lire par défaut.

## État de référence

- Branche : gecko-039-sudoku-tap-gecko-gomoku
- Version : 0.15.5-dev / versionCode 40
- GECKO-047 : VALIDÉ FAB — grandes barres d’axes, drag/suppression hors plateau, Prof utilisant les axes, audio Android/Pierre capturable.
- GECKO-048 : CODE + CI VERTE, validation téléphone encore attendue — prononciation Pierre et couleurs d’axes.
- Titre visible dans tous les modes : GeckoDoku 🦎.

## 1 — Contrat transversal

GeckoDoku regroupe quatre jeux dans une interface Android commune :
1. GeckoDoku Classic — puzzle logique carré.
2. Sudoku — Sudoku 9×9 avec candidats, repères et Prof.
3. Gomoku — plateau 19×19, contre Prof ou humain contre humain.
4. Abeilles & Geckos — puzzle hexagonal avec zones, axes Q/R/S et appariement Gecko ↔ Abeille.

Commun à tous les modes :
- Prof Gecko = présence pédagogique ;
- Pierre = voix locale du Prof ;
- difficultés quand pertinentes ;
- statistiques, étoiles, profil joueur et Hall of Fame ;
- son et animations optionnels ;
- export/import utilisateur ;
- overlays pédagogiques/médias sans effet sur la logique ni la taille logique du plateau.

Les préférences de mode, difficulté, taille Classic, type de match Gomoku et style Sudoku sont locales et persistantes.

## 2 — Difficultés

Gamme commune :
Découverte → Facile → Réflexion → Difficile → Expert → Démentiel → Mission Impossible → Infernal.

Classic et Abeilles & Geckos :
- le niveau demandé est une vraie contrainte de génération ;
- aucun fallback silencieux vers un autre niveau ;
- la recherche peut continuer jusqu’à trouver un puzzle conforme ;
- elle reste annulable.

Gomoku contre Prof :
- la difficulté règle profondeur, largeur d’exploration, pièges, rayon tactique et fiabilité défensive.

Gomoku humain contre humain :
- pas de difficulté IA ; sélecteur masqué.

## 3 — GeckoDoku Classic

### Règle
Grille 5×5 à 12×12, avec N zones pour N×N.

Une solution respecte :
- exactement un Gecko par ligne ;
- exactement un Gecko par colonne ;
- exactement un Gecko par zone ;
- aucun contact entre Geckos horizontal, vertical ou diagonal ;
- givens verrouillés ;
- solution unique.

Un Gecko confirmé exclut automatiquement sa ligne, sa colonne, sa zone et les cases voisines.

### Interaction
- tap simple : croix manuelle ;
- double-clic : palette Gecko / Hypothèse / Axe ;
- appui long : évolution de l’hypothèse ;
- repères personnels indépendants disponibles.

### Axes joueur
Double-clic → Axe → Horizontal/Vertical → Jaune/Vert/Rouge → pose.

Une barre :
- est globale sur l’axe choisi ;
- reste semi-transparente ;
- conserve type et couleur pendant le drag ;
- disparaît hors plateau.

### Solveur / Prof
La vérité pédagogique vient du solveur logique, jamais d’un coup inventé.
Le solveur couvre singles, règles de région, projections, Gecko X-Wing et hypothèses bornées par contradiction pour les hauts niveaux.
Le Prof travaille depuis l’état réel et ne mutera la grille que par une étape logique permise.
Ses repères pédagogiques sont séparés des repères personnels du joueur.

## 4 — Sudoku

Sudoku 9×9, givens verrouillés.

Interaction :
- tap simple : sélection / marqueur Gecko selon contexte ;
- double-clic : repères personnels ;
- appui long : palette de saisie.

Palette :
- chiffres 1–9 ;
- candidats ;
- marqueur Gecko ;
- effacement.

Un chiffre incorrect est refusé et compte comme erreur.
Un chiffre valide retire cette valeur des candidats de ses pairs ligne/colonne/bloc.

Historique local :
- Annuler ;
- Refaire.

Styles :
- chiffres classiques ;
- Gecko noir et blanc ;
- Gecko couleur.

Prof Sudoku :
- appui normal = explication / prochaine étape ;
- appui long = intervention directe possible ;
- les explications peuvent montrer candidats, projections et raisonnement ;
- l’intervention directe coûte davantage en assistance.

## 5 — Gomoku

Plateau 19×19. Cinq pièces alignées horizontalement, verticalement ou diagonalement gagnent.

Navigation :
- tap = jouer ;
- drag = déplacer la vue ;
- pincement = zoom.

Vue initiale autour de 12×12 cellules ; zoom minimal autour de 5×5.

Contre Prof Gecko :
- humain = vert ;
- Pierre joue l’autre camp ;
- après le coup humain, le Prof joue son tour ;
- le bouton Prof conseille pendant le tour humain ;
- un appui long peut appliquer le coup conseillé pour l’humain puis lancer le tour Prof.

Humain contre humain :
- deux camps humains ;
- Prof conseiller uniquement.

Pédagogie :
- ligne étudiée ;
- menaces ;
- coup conseillé ;
- projection de suite.

## 6 — Abeilles & Geckos

Plateau hexagonal :
- Q ↖↘
- S ↑↓
- R ↗↙

Carte panoramique/zoomable et recentrable.

Règle :
- exactement 1 Gecko et 1 Abeille par zone ;
- la paire de zone est voisine ;
- chaque Abeille de solution touche exactement un Gecko de solution ;
- chaque Gecko de solution touche exactement une Abeille de solution ;
- pour chaque type de pièce, au plus une pièce sur une même ligne Q, R ou S.

Interaction :
- tap simple = cycle croix jaune hypothèse → verte sûre → rouge impossible → retrait ;
- double-clic = Gecko / Abeille / Axe ;
- appui long = repère personnel.

Axes :
Axe → Q/S/R → Jaune/Vert/Rouge.
Ils sont globaux, draggables, supprimables hors plateau et persistés.

Persistance :
- schema 4 avec couleur ;
- anciennes données sans couleur → rouge par défaut.

Prof :
- appui normal = indice ;
- appui long = application possible ;
- repères Prof indépendants des couleurs personnelles.

## 7 — Prof Gecko / Pierre

Le personnage UI est Prof Gecko. La voix locale est Pierre.

Contrat :
- explication cohérente avec l’état réel ;
- conseil ≠ coup direct sauf action explicitement prévue ;
- parole pédagogique prioritaire sur animation décorative ;
- paroles automatiques non comptées comme aide demandée ;
- bulles/animations sans reflow du plateau.

Flux vocal :
texte logique → ProfessorSpeech → PierrePiperSpeechEngine → PierrePronunciationPolicy → Sherpa/Piper → VoicePcmPlayer.

Le texte affiché reste orthographiquement correct.
La couche de prononciation ne transforme que le texte vocal.

GECKO-048 :
- mot entier église → eglize pour Pierre ;
- texte UI inchangé.

Pierre dispose aussi du bouton !, de phrases d’ambiance, d’un contexte joueur et d’une mémoire anti-répétition.

## 8 — Médias et audio

Les vidéos/animations sont décoratives :
- aucune influence sur moteur, solution, difficulté, stats ou validation ;
- pas de reflow du plateau ;
- échec média = jeu toujours fonctionnel ;
- désactivables.

Vidéo muted :
- la piste audio est réellement désélectionnée.

Audio Pierre validé GECKO-047 :
- Android MEDIA ;
- contenu SPEECH ;
- capture de lecture autorisée ;
- manifeste compatible capture.

Journal vidéo = diagnostic média.

## 9 — Étoiles, erreurs et assistance

Base : 5 étoiles sans aide.

Assistance :
- conseil = 1 point ;
- coup direct = 2 points.

Avant erreurs :
- 0 → 5 étoiles ;
- 1 → 4 ;
- 2–3 → 3 ;
- 4–5 → 2 ;
- plus → 1.

Chaque erreur retire 3 étoiles.
Minimum final : 1 étoile.
Animations et paroles automatiques ne comptent pas comme aide demandée.

## 10 — Données utilisateur

Statistiques locales, globales et par mode/difficulté :
- parties commencées ;
- terminées ;
- terminées avec Prof ;
- temps ;
- erreurs ;
- étoiles ;
- meilleurs résultats.

Profil / Hall of Fame :
- nom joueur modifiable ;
- Hall par mode/difficulté avec étoiles ;
- vider l’historique efface le Hall sans effacer stats agrégées, réglages ou journal Classic.

Journal Classic :
- sauvegarde définition de grille, pas progression ;
- charger = repartir d’une grille propre ;
- sauver, lister, charger, supprimer, vider.

Export/import :
- JSON via sélecteur Android ;
- validation avant application ;
- erreur d’import → restauration de l’état précédent.

## 11 — Invariants à ne pas casser

1. La logique de jeu ne dépend jamais des médias.
2. Prof et solveurs n’inventent jamais une preuve.
3. Les overlays ne redimensionnent pas les plateaux.
4. Les givens restent verrouillés.
5. Difficultés Classic/Bee sans fallback silencieux.
6. Couleurs/repères joueur séparés du rendu pédagogique Prof.
7. Import invalide ne détruit jamais l’état utilisateur.
8. brain.md garde le contrat fonctionnel ; brainmap.md porte les détails techniques.
9. sauvegarde.md est froide et n’est ouverte que sur besoin précis.

## 12 — Delta actif GECKO-048

Présent dans le code et couvert par CI :
- PierrePronunciationPolicy ;
- église → eglize au bord vocal seulement ;
- AxisGuideColor jaune / vert / rouge ;
- ClassicAxisGuide porte la couleur ;
- BeeGeckoLogicalMarks.axisColors ;
- drag conserve couleur ;
- Bee session schema 4, compat ancienne → rouge.

Reste à Fab :
validation téléphone de la prononciation, du rendu des couleurs, du drag/persistance et des non-régressions audio/axes.
