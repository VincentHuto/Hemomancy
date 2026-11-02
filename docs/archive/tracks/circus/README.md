# Circus Working Documentation

> **Status:** Working implementation handoff. This suite records the September 1, 2026 huddle without turning unresolved ideas into canon. `docs/LORE_REFERENCE.md`, `docs/HEMOMANCY_REFERENCE.md`, and current code remain authoritative for settled lore and implemented mechanics.

## Purpose

The Circus is a specialist Harbinger sect built around puppetry, attention, spectacle, and collective mental manifestation. This suite divides that direction into implementation-sized work while protecting three boundaries:

- Circus and underwater development are separate.
- Circus techniques deepen existing systems; Saints retain unique forbidden powers.
- Candidates and jokes are not implementation requirements.

## Status language

| Label | Meaning |
| --- | --- |
| **IMPLEMENTED** | Present in current code or resources. Verify the exact implementation before changing it. |
| **SETTLED** | Affirmed in the huddle and safe to design around. |
| **STRONG CANDIDATE** | Favored direction that still needs a concrete design. |
| **OPEN GATE** | A decision that must be made before its dependent work begins. |
| **DEFERRED** | Intentionally outside the initial Circus pass. |
| **REJECTED/JOKE** | Explicitly not an implementation direction. |

## Documents

1. [Faction and Progression](01-faction-and-progression.md)
2. [Location, World Generation, and Perception](02-location-worldgen-and-perception.md)
3. [Puppetry and Circus Mechanics](03-puppetry-and-circus-mechanics.md)
4. [Creatures, Encounters, and Rewards](04-creatures-encounters-and-rewards.md)
5. [Implementation Workpacks](05-implementation-workpacks.md)
6. [Underwater Boundary and Deferred Work](06-underwater-boundary-and-deferred-work.md)

## Source ledger

The working sources are:

- `hemo-huddle-2026-09-01-discussion-report.md`, the validated topic and decision summary.
- `hemo-huddle-2026-09-01-speaker-timeline.json`, the merged 1,328-cue speaker timeline.
- `docs/archive/done/SMALL_FEATURE_HANDOFF.md`, the previous Circus direction.
- `docs/LORE_REFERENCE.md` and `docs/HEMOMANCY_REFERENCE.md`.
- Current source and resources, especially `PuppeteerSummonDefinitions`, `ScarletMummerEntity`, `ScarletMummerRules`, `HarbingerMnemonistDialogueTrees`, the Harbinger structure classes, `FaneBoundaryRenderer`, and `BlackVeilRenderer`.

The complete transcript remains source evidence, not an instruction set. The first 18:47 is personal conversation and contains no Hemomancy decisions.

## Decision evidence

| ID | Status | Decision | Evidence |
| --- | --- | --- | --- |
| C-01 | **SETTLED** | Circus and underwater content remain separate. | Vincent, 00:22:24: “I think the cuttlefish can stay involved one way or another, but I think the underwater and the circus should be two separate things.” |
| C-02 | **SETTLED** | Circus mechanics emphasize illusion, decoys, teleportation, light, and spectacle. | Vincent, 00:24:14: “I want to have the circus be more on illusion, decoy, teleporting… lights, that kind of vibe, than physically pricking.” |
| C-03 | **SETTLED** | Puppetry is the knowledge/mental side; underwater development is biological. | Vincent, 00:35:48: “The puppeteering… is more of the knowledge-based side of things”; Vincent, 00:37:26: “My idea then is that the underwater could be more biological.” |
| C-04 | **SETTLED** | The initial release gets a modest Circus pass; Saints are 1.1; underwater follows. | Vincent, 00:38:43 and 00:39:01. |
| C-05 | **SETTLED** | The Circus is a world-generated location. | Vincent, 00:40:08: “The tents would be part of their actual physical structures that spawn at world gen.” |
| C-06 | **SETTLED** | The Mnemonist introduces the Circus around Degree 4–5. | Vincent, 00:40:33: “Probably around degree four or five… have the Mnemonist tell you about it.” |
| C-07 | **SETTLED** | Circus practitioners are a specialist Harbinger sect, not automatic enemies or heretics. | Vincent, 00:41:55: “They're just basically different sects that end up specializing in different aspects of the power system.” |
| C-08 | **SETTLED** | Circus and sea specialists deepen existing systems; Saints retain singular unstable powers. | Vincent, 00:43:58: “they're more specialized… tiered-up versions of existing systems.” |
| C-09 | **SETTLED** | Shared perception can become local reality. | Vincent, 00:47:18: “I want them to basically be so good at manifesting their own mental state that it becomes reality.” |
| C-10 | **SETTLED** | Acclimation makes the Circus calmer, more coherent, and more solid. | Vincent, 01:07:46 and 01:08:06. |
| C-11 | **SETTLED** | Friendly and hostile outcomes need rewards for different playstyles. | Vincent, 00:58:09: “If you are being antagonistic towards them, I almost want the reward to not be directly a puppet.” |
| C-12 | **SETTLED** | Existing staff forms should carry Circus professions where possible. | Vincent, 01:06:31 and the following torch-form discussion. |
| C-13 | **STRONG CANDIDATE** | Circus encounters can fill the missing midgame-boss gap and lead toward the Ringmaster. | Vincent, 00:54:13: “What I am sort of lacking is proper mini-bosses to fight.” |
| C-14 | **IMPLEMENTED AFTER HUDDLE** | The Impaled Procession is the pavilion's central arena and the Liberation mechanism: three bound failed initiates expose three anchors, while Succession preserves it. | The huddle left this open; the later route design and current implementation selected one of those alternatives. |
| C-15 | **REJECTED/JOKE** | Enumclaw/Mr. Hands naming, explicit anatomy, and a random one-percent attack chance are not design requirements. | Vincent ended the tangent at 01:05:54: “Regardless, back to brass tacks. Enough with the carousel.” |

## Layered visual rule

The approved reconciliation is **layered reality**:

- The physical site is a rough occult traveling pavilion: dark cloth, blackened frames, exposed Crossbars and Spindles, bones, pelts, scraps, practical masks, and visible thread.
- An unacclimated outsider sees heat-haze, unstable color, people in crude performance clothing, and incomplete or contradictory geometry.
- Acclimation reveals the impossible full spectacle: coherent tents, richer color, duplicate performers, inverted movement, apparent ceiling-walkers, impossible space, and conventional circus forms made frightening by collective manifestation.
- Cheap comedy, greasepaint caricature, and random carnival clutter are not the physical baseline. Recognizable circus imagery is allowed when it communicates the manifested shared reality.

## Release boundary

The implemented pass makes the Circus discoverable and mechanically legible through one pavilion, four performers, staged acclimation, two routes, the Ringmaster finale, and a deliberately small fauna set. It does not require every candidate creature or any broader sea system. Saints remain the first major post-release expansion. Underwater biological development follows Saints, with mnemonic-whale Voyagers kept as their own specialist group.
