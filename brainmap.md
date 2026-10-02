# GeckoDoku — FAB Copilot brainmap

> Carte technique courte de l'état courant.
> Référence : `main`, version `0.15.43-dev`, code 78.

## Architecture générale

```text
MainActivity
├─ GameMode
│  ├─ Classic → GameEngine / PuzzleGenerator / HumanSolver
│  ├─ Sudoku → SudokuGameEngine / SudokuSolver / SudokuHintEngine
│  ├─ Gomoku → GomokuGameEngine / GomokuAi
│  └─ BeeGecko → BeeGeckoGameEngine / générateur / solveur
├─ Prof Gecko
│  └─ ProfessorSpeech → PierrePiperSpeechEngine
│     → PierrePronunciationPolicy → VoicePcmPlayer
├─ Stats
│  └─ PlayerStatsStore → PlayerStatsTrendView → HallOfFameStore
└─ Rich media
   ├─ SpriteBankFactory / SpriteFrameCache
   ├─ AliveMascotOverlay
   └─ ChromaKey vidéo pour médias encore conservés
```

## Classic

```text
PuzzleGenerator
→ unicité + contraintes
→ HumanSolver / HypothesisSolver
→ GameEngine
→ GeckoBoardView
→ ProfessorGecko
```

Branches d'hypothèses :
```text
hypothèse parent (couleur + aura)
→ croix filles de même couleur
→ sous-hypothèse enfant
   ├─ continuation
   ├─ changement de sous-branche → prune descendants
   └─ contradiction → sens interdit + prune descendants/croix/aura
```

## Sudoku

Entrée tactile :
```text
simple clic ─┐
double clic ─┼→ SudokuGesturePolicy → OPEN_INPUT_PALETTE
appui long ──┘                         └→ OPEN_PERSONAL_MARKERS
```

Pavé persistant :
```text
SudokuQuickPaletteView
├─ Choix → Prévisu → Oui/Non → SudokuGameEngine
├─ Candidats → notes de la case
├─ Hypothèse → HypothesisBranchTrace
└─ Prévisu
   └─ ? → aide documentaire Prof
```

Popup :
- `PopupWindow` non focusable ;
- Android Q+ : `setTouchModal(false)` ;
- reciblage sans fermeture ;
- drag ;
- resize min ~140×160 dp.

Hypothèses Sudoku :
```text
SudokuGameEngine.cycleHypothesis
→ trace parent/enfant
→ couleur de branche
→ visualisation aura/croix
→ contradiction / prune / rollback
```

## Stats

```text
recordStart
→ tentative active temporaire
   ├─ recordComplete → event terminé
   └─ nouveau départ / abandon
      ├─ 0 erreur → suppression silencieuse
      └─ ≥1 erreur → event abandon avec erreurs
```

```text
PlayerStatEvent[]
→ statsForDifficulty
→ dernières 2 parties terminées
   ├─ tendance temps
   └─ tendance étoiles
→ PlayerStatsTrendView
→ Stats / Hall of Fame
```

Prof au démarrage :
```text
professorLevels()
→ niveau le plus difficile staté
→ niveau précédent éventuel
→ PlayerStatsNarration
→ uniquement tendances vitesse/étoiles
```

## Abeilles & Geckos

```text
BeeGeckoAxisGeometry
├─ Q +60° ↖↘
├─ S -60° ↙↗
└─ R 0°   ←→
```

Même trace de branches d'hypothèses que Classic :
parent → enfants → prune à suppression, contradiction ou changement de sous-branche.

Axes personnels :
choix Q/S/R → couleur jaune/vert/rouge → rendu → drag → persistance.

## Prof / voix

```text
texte UI correct
→ ProfessorSpeech
→ PierrePiperSpeechEngine
→ PierrePronunciationPolicy
→ Sherpa/Piper
→ AudioTrack / sortie Android
```

Exemple :
`église` affiché → `eglize` envoyé à Pierre.

## Sprites / médias

```text
APK
└─ sprites/banks/240p
   → SpriteBankFactory
   → séquences prêtes en mémoire
   → SpriteFrameCache LRU
   → SpriteRGBA runtime
```

Gecko/Abeille gameplay : SpriteRGBA 240p.
Plante, Prof/Pierre et intros : médias conservés selon leur pipeline dédié.

## Persistance

- préférences de mode ;
- stats + events temporels ;
- Hall of Fame ;
- profils ;
- sessions Bee ;
- export/import utilisateur.

## Références Git

- release : `phone-0.15.43-dev-run-403`
- merge code main : `17c6de0186c745c15fc042971eb09b4fe19a6299`
- CI merge/reconciliation : #404 verte
- CI main : #405 verte
