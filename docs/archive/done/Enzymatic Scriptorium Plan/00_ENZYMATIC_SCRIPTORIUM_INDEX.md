# Enzymatic Scriptorium — Design Packet

> **Status:** Consolidated design packet from the September 2026 Enzymatic Scriptorium discussion.  
> **Target:** Hemomancy, Minecraft 1.21.1 / NeoForge.  
> **Purpose:** Separate the feature into Codex-friendly documents covering concept, base implementation, curses/balance, Overexertion, and the D5/D7 Cardinal Rite upgrades.

## Documents

1. `01_ENZYMATIC_SCRIPTORIUM_CONCEPT.md` — fantasy, progression, visual identity, eight enzyme tubes, red-glint provenance, and relationship to vanilla enchanting.
2. `02_ENZYMATIC_SCRIPTORIUM_BASE_IMPLEMENTATION.md` — ordinary enzyme-biased enchanting, affinity data, station inventory/model behavior, item provenance, compatibility, and implementation architecture.
3. `03_HEMATIC_CURSES_AND_BALANCE.md` — Scriptorial Strain, Hematic Curse rules, candidate curses, removal, and balance guardrails.
4. `04_OVEREXERTION_SYSTEM.md` — D5 `+1` and D7 `+2` limit breaking, Excess Load, guaranteed instability, and use-triggered backlash.
5. `05_SCRIPTORIUM_UPGRADE_RITES.md` — D5/D7 upgrades inside the existing Cardinal Rite framework, including offerings, eight floor orbs, writing tendrils, visual changes, and culmination.

## Settled high-level decisions

- The station is named **Enzymatic Scriptorium**.
- It resembles a recognizable enchanting table modified by Harbinger alchemy.
- The model permanently exposes **eight glass enzyme tubes/reservoirs**, one for each tendency.
- Normal Scriptorium operation biases legal enchantments rather than directly selecting them.
- The abandoned **Hematic Graft** concept is not part of this design.
- The dangerous counterpart to better enchanting is a dedicated family of **Hematic Curses**.
- Curse risk rises with both how aggressively the process is biased and how powerful the resulting enchantment package actually is.
- Every item enchanted through the Scriptorium uses Hemomancy's existing **red enchantment glint** instead of vanilla purple glint.
- Station progression stops at **D7** for now:
  - **D3:** Enzymatic Scriptorium — biasing, normal enchantment limits.
  - **D5:** Consecrated Enzymatic Scriptorium — Overexertion I, normal max `+1`.
  - **D7:** Monolithic Enzymatic Scriptorium — Overexertion II, normal max `+2`.
- **D8 / Apotheos currently has no Scriptorium-specific behavior.**
- D5 and D7 upgrades use the existing **Cardinal Rite** system rather than a parallel ritual controller.
- Rite-specific behavior is implemented through narrow exceptions/hooks keyed by the Scriptorium rite IDs.
- The player does **not** project directly into the tiny tubes on the block model during upgrade rites.
- Upgrade rites instead create **eight large glowing virtual orbs** on the ritual floor. Filling an orb writes a tendency-colored blood tendril across the floor into the matching physical tube.
- Upgrade rites currently have **no bespoke failure state**. Special objectives wait for completion; inactive/wrong targets simply reject input.

## System boundary

The Scriptorium remains an **enchanting system**.

Its verbs are:

```text
BIAS
Make legal enchantments more or less likely.

INSCRIBE
Create an enchanted item through Hemomantic chemistry.

CURSE
Attach the biological/magical price of forcing that inscription.

OVEREXERT
Exceed an enchantment's ordinary maximum at increasing structural and bodily cost.
```

It does **not** grant new active combat abilities comparable to Manipulations.
