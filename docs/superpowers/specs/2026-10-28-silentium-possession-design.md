# Silentium Possession (Section 4a) — Design

Date: 2026-10-28. Source: `docs/Endgame Path Asymmetry …_V2.md`, "Silentium possession rite", "Concentration" and "Ejection and Mind Broken". Written during the user's overnight run, so every choice below is a ruling made on the user's behalf. Section 4b covers the Abhorrent Thought gaze and grab, adipocere, and the Visitation rite.

## Goal

A Silentium casts the **Rite of Possession** over a stilled surrogate. Their will walks the Gardens in the surrogate's facsimile while a shadow echo waits at the rite. Concentration is the only meter. A clean return costs nothing; a snapped one leaves them **Mind Broken**.

## Decisions

### D1. The rite
- **`cardinal_rite/possession_rite`:**
  - purpose `bond`, floor `communion_lesser`, D8;
  - blood cost 400;
  - no brazier signature.
- **Refused unless the caster:**
  - is a Silentium;
  - is not Mind Broken;
  - is not already possessing;
  - and a **surrogate** stands in the rite space: a vanilla bat, enderman, blaze, iron golem, horse, donkey, wolf, spider or armor stand (exact vanilla types; facsimiles never count).
- **Offerings** are items lying loose inside the rite space at completion. They are consumed, one of each kind:
  - Mnemonic Ambergris: Concentration regenerates, 1 per 100 ticks.
  - Hematic Iron Block: a shell of 3 free hits that never returns.
  - Totem of Undying: once per possession, prevents a Mind Broken ejection and refills Concentration.
- **Why loose items:** the shared brazier matcher demands an exact signature and fails on extras, so optional brazier offerings would mean changing every rite's matcher.

### D2. Possession
- **The surrogate** (nearest in the rite space) is **stilled**: no AI, invulnerable, held in place for the whole visit.
- **The echo.** A `possession_echo` stays at the caster's position: a shadowy, translucent figure in the caster's skin. It has no hitbox and no collision, can't be hurt, and is visible to everyone.
- **The caster's inventory** is saved into the possession attachment and cleared, except for the donkey form, which keeps its inventory ("inventory access").
  - The armor-stand form instead puts on the stand's gear, taken off the stand.
  - The caster receives a **Thread of Will** (`thread_of_will`) in the first slot. It can't be dropped or stored.
- **Travel.** The caster is carried to the Gardens dimension (the same safe-landing search as Spine travel) and takes the form. Its return point is stored in the attachment.
- **Forms:**

| Form | Concentration | Body | Thread of Will (right-click) |
| --- | --- | --- | --- |
| Bat | 3 | flight | echolocation: nearby entities glow for 5 s (100-tick cooldown) |
| Enderman | 6 | touching water drains 1 Concentration per second | teleport up to 24 blocks along the gaze (40) |
| Blaze | 6 | flight; touching water drains 1 per second | small fireball (15) |
| Iron golem | 14 | speed −40%, jump −40%, +1.5 attack knockback | ground slam: 4 damage and knockback in 4 blocks (60) |
| Horse | 10 | speed +20%, jump +60% | leap forward (40) |
| Donkey | 8 | speed −15%, keeps its inventory | — |
| Wolf | 6 | speed +30% | lunge (30) |
| Spider | 6 | climbs walls | — |
| Armor stand | 8 | wears the stand's gear | — |

- **Rendering.** Every client sees the possessed player drawn as their form's facsimile (the 2a renderers). The armor-stand form is drawn as the player wearing the stand's gear; the fungal mannequin is still deferred.

### D3. Concentration
- **Damage never touches health.** Each hit costs 1 Concentration, or 2 if the hit deals 6 or more.
  - The shell absorbs hits first.
  - At 0, an unused totem refills Concentration instead; otherwise the caster is ejected **Mind Broken**.
  - A lethal event during possession is cancelled and ejects too.
- **No regeneration** without Ambergris.
- **HUD.** A Concentration layer shows pips, with the shell as pale pips.

### D4. Return and ejection
- **Clean return.** Hold the Thread of Will while sneaking for 2 seconds. Logging out also returns the caster cleanly.
  - The caster goes back to the echo.
  - The echo dissolves.
  - The surrogate is released unharmed.
  - The saved inventory comes back. Anything picked up in the Gardens comes back too; overflow drops at the echo.
  - The armor-stand form leaves the stand's gear in the Gardens on a fresh armor stand where it stood.
- **Ejection.** Concentration reaching zero, leaving the dimension or dying ends the visit.
  - The return is the same, except the surrogate dies (the organism keeps what it took).
  - The caster gets **Mind Broken** for 6000 ticks.
- **Crash recovery.** A player who logs in with an active possession is returned cleanly.

### D5. Mind Broken
- **`hemomancy:mind_broken`** is a harmful effect that milk and totems do not cure (`fillEffectCures` is left empty).
- **While it lasts:**
  - attack damage −2;
  - Will ambush chance ×2;
  - a fungal whisper fires on application, when one is due;
  - the possession rite is refused, which makes it the rite's cooldown.

### D6. Networking
- One clientbound payload, `PacketSyncPossession(entityId, form, concentration, max, shell)`. It is sent to the player and to everyone tracking them on change, and on start-tracking.
- PROTOCOL_VERSION 19.

## Out of scope (4b and later)
- The Abhorrent Thought gaze drain and grab ejection (grab gives a longer Mind Broken).
- Adipocere as an offering (+3 max Concentration).
- The Visitation rite.
- Chimerization.
- The fungal mannequin.
- The echo shader.
- Gardens digesting left gear.
