# GeckoDoku — brainmap.md
## Carte active — 26/09/2026

VALIDÉ / GELÉ
├── IntroGeckoGD.mp4 + son ✅
├── Gecko_Intro.mp4 après Intro 1 ✅
├── launcher icon ✅
└── médaillon titre ✅

CHANTIER ACTIF : SPRITES / VIDÉOS
│
├── Gecko gameplay
│   ├── apparition.mp4 → visible + muet
│   ├── disparition.mp4 → visible + muet
│   └── actions_plusieurs.mp4 → visible + muet
│
├── Prof slot
│   ├── Prof.png = fallback
│   ├── Prof_actions.mp4 = animation muette
│   └── ProfParle.mp4 = parole réelle
│       └── priorité locale sur Prof_actions
│
├── Coexistence
│   ├── ProfParle + Gecko = OUI
│   ├── plusieurs Gecko = OUI si déclenchés
│   └── fin session A ≠ stop session B
│
└── Rendu commun suspect
    ├── ChromaKeyVideoView
    ├── OpenGL renderer
    ├── Surface / SurfaceTexture
    ├── Z-order / MediaOverlay
    ├── alpha / blending
    └── symptôme : rectangle noir

ProfParle failure path suspect :
onError
→ professorSpeechVideoFailed=true
→ tentative suivante bloquée
→ PNG malgré Pierre actif

À corriger après RED/logs, sans toucher aux intros validées.


<!-- GECKO-033-SPRITES-RED-TRANSPARENCY-RETRY-2026-09-26 -->
## RED courant
Rendu transparent
├── résultat requis : alpha réel / pas de noir
├── MediaOverlay actuel : suspect téléphone
└── stratégie OnTop : à retester avec multi-sessions indépendantes

ProfParle
├── erreur tentative N → PNG fallback
└── tentative N+1 → DOIT pouvoir retenter ProfParle


<!-- GECKO-033-PROFPARLE-RETRY-GREEN-2026-09-26 -->
## ProfParle retry
attempt N ERROR
→ flag diagnostic=true
→ PNG fallback
→ speech N+1
→ StartPolicy ignore l'ancien échec
→ retry ProfParle
→ START réussi
→ flag diagnostic=false
