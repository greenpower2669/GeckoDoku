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
