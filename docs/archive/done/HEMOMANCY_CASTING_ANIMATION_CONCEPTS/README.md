# Hemomancy Blood-Manipulation Casting Concepts

**Current contract (2026-10-19):** [Casting animations v2](../../superpowers/specs/2026-10-19-casting-animations-v2-design.md) now governs player movement, timing and the Still Art lane; it supersedes the movement revision below.
This package remains the material and silhouette reference for each tendency; see [current behavior](../../../wiki/Casting-Animations.md).

**Movement revision (superseded):** The implemented tracks now use restrained chest/shoulder-height gestures, small first-person rotations and no casting-driven torso pitch. Every charged/continuous presentation settles once and holds until release or cancellation. Earlier references to repeated pumping, hauling, shuddering or twitch loops are superseded for the player's body; the surrounding blood anatomy may still animate. See [current behavior](../../../wiki/Casting-Animations.md).

This package collects the current discussion material and blood-first concept art for tendency-specific player casting animations.

## Package contents

- [`CASTING_ANIMATION_LANGUAGE.md`](CASTING_ANIMATION_LANGUAGE.md) — complete shared animation-language discussion.
- [`PARTICLE_AND_RANK_STANDARDS.md`](PARTICLE_AND_RANK_STANDARDS.md) — shared rank escalation, blood-material rules, particle vocabulary, and provisional performance guidance.
- [`tendencies/`](tendencies/) — one animation and particle brief for each canonical tendency.
- [`images/`](images/) — eight five-rank concept sheets.

## Reading the concept sheets

Every image progresses from left to right:

1. **Humilis** — Degree 0
2. **Mediocritas** — Degree 1
3. **Summa** — Degree 3
4. **Magister** — Degree 5
5. **Perfectus** — Degree 6

All five ranks are implemented presentation targets. The active registry already contains advanced powers such as Crimson Coronation, White Verdict, Funeral Bell and Absolute Stillness. Rank selects choreography; the manipulation's existing cast type independently controls quick, charged, continuous or passive playback.

## Canonical visual rule

> Every tendency is blood magic first. The tendency changes the behavior, state, or presentation of blood; it does not replace blood with an ordinary element.

Across all eight tendencies:

- Crimson, burgundy, black-red, vessels, droplets, clots, and circulatory anatomy remain dominant.
- Tendency coloration is a secondary accent.
- Particle effects must preserve a visible material relationship to blood.
- The base animation must remain legible without particles.
- The ordinary player rig is limited to head, torso, two rigid arms, and two rigid legs.

## Tendency index

| Tendency | Aspect | Animation brief | Concept sheet |
|---|---|---|---|
| Animus | Life and circulation | [`01_ANIMUS.md`](tendencies/01_ANIMUS.md) | [`01_animus_rank_progression.png`](images/01_animus_rank_progression.png) |
| Flammeus | Fire and violent metabolism | [`02_FLAMMEUS.md`](tendencies/02_FLAMMEUS.md) | [`02_flammeus_rank_progression.png`](images/02_flammeus_rank_progression.png) |
| Ductilis | Nerves and conduction | [`03_DUCTILIS.md`](tendencies/03_DUCTILIS.md) | [`03_ductilis_rank_progression.png`](images/03_ductilis_rank_progression.png) |
| Lux | Light and revelation | [`04_LUX.md`](tendencies/04_LUX.md) | [`04_lux_rank_progression.png`](images/04_lux_rank_progression.png) |
| Mortem | Death and extraction | [`05_MORTEM.md`](tendencies/05_MORTEM.md) | [`05_mortem_rank_progression.png`](images/05_mortem_rank_progression.png) |
| Congeatio | Cold and arrest | [`06_CONGEATIO.md`](tendencies/06_CONGEATIO.md) | [`06_congeatio_rank_progression.png`](images/06_congeatio_rank_progression.png) |
| Ferric | Iron and weight | [`07_FERRIC.md`](tendencies/07_FERRIC.md) | [`07_ferric_rank_progression.png`](images/07_ferric_rank_progression.png) |
| Tenebris | Shadow and displacement | [`08_TENEBRIS.md`](tendencies/08_TENEBRIS.md) | [`08_tenebris_rank_progression.png`](images/08_tenebris_rank_progression.png) |

## Status

The shared system now contains 40 authored school/rank presentations, separate first-person arm tracks, purpose endings, cancellation and bespoke overrides. It extends the existing player model and render layers without a new animation library. The concept sheets remain material references; their extra joints are not part of the player rig.

See [implemented behavior, preview and validation](../../../wiki/Casting-Animations.md). Use `/hemocastpreview lux perfectus area hold` to inspect a presentation locally without spending blood or applying gameplay. The original storyboards below describe visual intent; quick gameplay always executes before its decorative release animation.
