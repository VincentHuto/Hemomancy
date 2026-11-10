# Track 10: The daily loop and the world's answer

**Goal:** tie blood to the things every Minecraft player already does (eat, sleep, watch the sky), and make the world
respond to how much blood the player spills. This is the "part of Minecraft, not bolted on" track. The first half is
small and can ship early.

**Depends on:** Track 3 and Track 6 for the second half. The first half depends on nothing.

## What exists

- **Regen** is 1 mL per second, flat. Food gives no blood. Sleep gives none. Kills pay
  `25 × max(1, maxHealth / 20)`.
- **Cooked Hemomancy foods** exist and do nothing beyond hunger.
- **Blood Moon** is a 1-in-7 roll each night. Moon phase is never read.
- **Will ambushes** (`WillAmbushDirector`) pick a school from the player's tendency and are not tied to blood spent.
- **Vampire bats** live in the Nether since 2026-11-02 and hunt (`isHunting`).
- **Barometer and Ossuary Clock** are isolated: built, modelled, connected to nothing.
- **`BloodFlowLedger`** already records every payment by source and category.
- **No infection spread** in the Overworld. No village tie at all.

## Part one: the body

### Food and sleep (`CL-18`)

- **Regen scales with the body.** A pure `BloodRegenRules`: base 1 mL per second, × 0.5 when hunger is below 6, × 1.5
  when the food bar is full and saturated, zero while starving. The Harbinger who eats well recovers faster. This is
  the cheapest possible vanilla tie and it makes every food in the game matter to the mod.
- **Meat is blood.** Raw and cooked meats give a one-off amount on eating, through one food hook, scaled by the
  food's nutrition. Hemomancy's cooked foods give more and gain their job here. Tainted meat (Track 5) gives the
  most to a Harbinger.
- **Sleep restores.** Waking from a full night's sleep sets a recovery bonus for the next few minutes (not an instant
  refill, so the bed is not a blood potion). Concentrated Blood plus sleep (a D2 to D3 gate) keeps its meaning.
- **Not hunger for hunger's sake.** Casting does not drain the food bar. One resource pays for magic; the body only
  sets how fast it comes back.

### A day in the loop

What the tracks add up to for a mid-game Harbinger:

1. Wake. Regen is up. Check the vats; top up Vigor (Track 5).
2. Draw the day's Temper from the creatures the plan needs (Track 4).
3. Tend the field: gourds and strains on fed ground, pour the basin, collect last night's Formations (Tracks 3, 7).
4. Go out with the Staff: mine by projection, light caves with gouges, sample what is new (Tracks 6, 8).
5. Come back, examine samples, fill and fire a station while the blood is fresh (Tracks 3, 5).
6. Watch the sky.

Every step uses the two verbs. None of it is a machine running alone.

## Part two: the world answers

### Blood spilled is counted

A level saved data, `BloodSpilledLedger`, keeps a decaying total per region (a few chunks square) fed from
`BloodFlowLedger` categories that mean blood left in the world: projection onto ground, stains, chum, basin clotting,
kills. It decays over days. One pure `WorldAnswerRules` reads it.

### What reads it

- **The Blood Moon.** Replace the flat roll: chance by moon phase (highest at the full moon, none at the new moon),
  raised by the region's spilled total. A Harbinger who has been busy brings it on. The Barometer shows the region's
  pressure; the Ossuary Clock shows the phase and the nights until the next likely Moon. Both instruments finally
  read something.
- **Wills.** `WillAmbushDirector` weighs the region's total and the player's recent spend, so heavy working draws
  attention where it happened. The school still comes from tendency.
- **Scavengers.** Wet stains (Track 6) and chum pools (Track 7) attract: vampire bats underground and at night, and
  the sea's hunters to strong pools. Small, local, and a reason to clean up with Absorption.
- **The ground remembers.** In a region with a high total for long enough, grass near old stains turns to the mod's
  own ground cover, slowly and only on blocks the player stained. This is the infection the lore describes, driven by
  the player's own hand, and it is reversible. Keep it cosmetic and slow in this track; a spreading biome is its own
  design.

### What the player can do about it

- Absorb stains instead of leaving them.
- Clot spare blood on purpose (a Condenser spill does not count).
- A ward block that lowers a region's count: a use for `suspended_vivianite` if the gouge catalysts do not take it.
- Or lean in: a high count is also when rare things come.

## Deferred: the Parish (`CL-19`)

Villages are the largest untouched tie to vanilla and the easiest to get wrong in tone. Villagers who bleed for
trade, who fear or revere a Harbinger, who call the Unstained, who sicken near a stained region: all of it belongs
in its own design after Stage C, once the loop above is proven. Nothing in this track depends on it. Villagers are
not keepable in vats (Track 5) for the same reason.

## Bestiary

Track 5 turns `SpecimenBestiary*` into the creature bestiary. This track adds the world's side to it: a page per
answer (Moon, Wills, scavengers, the ground) that fills in as the player causes each one.

## What changes, file by file

| File | Change |
| --- | --- |
| New `BloodRegenRules` (pure); the regen tick in `BloodVolumeEvents` | Hunger and sleep. |
| New food hook (`LivingEntityUseItemEvent.Finish`), a data table of meats | Meat is blood. |
| New `common/world/answer/BloodSpilledLedger` (saved data), `WorldAnswerRules` (pure) | The count. |
| `BloodFlowLedger` | Feeds the ledger by category. |
| Blood Moon roll | Phase and count. |
| `WillAmbushDirector` | Reads the count. |
| Vampire bat spawn and target rules | Reads stains. |
| Barometer, Ossuary Clock block entities and renderers | Read phase and pressure. |

## Tests

- New: `BloodRegenRulesTest`, `WorldAnswerRulesTest`, a `daily_loop_validation` GameTest batch (regen by hunger; meat
  pays once; sleep bonus; ledger decays; Moon chance by phase).
- Retune: the five regen JVM tests, Blood Moon tests, `WillAmbush*` tests, and every playthrough scenario that waits
  for blood to regenerate (`PlaythroughClock`).
- Guardrail: all blood gained still ends in `BloodVolumeEvents.syncVolume`.

## Hazards

- **Playthrough timing.** Many scenarios assume 1 mL per second. Give the GameTest player a fixed hunger state so
  the suite stays deterministic.
- **Punishing the core fantasy.** If projection always summons trouble, players stop projecting. The count must rise
  from waste (blood left on the ground), not from use (blood put to work).
- **Blood Moon frequency.** Tying it to phase makes it at most one window in eight nights. Check that against
  everything gated on a Blood Moon.
- **Spread.** Anything that changes terrain on its own needs an off switch in the server config.

## Open questions

`CL-18`, `CL-19`.

## Acceptance

- A well-fed Harbinger regenerates visibly faster than a starving one, and steak is worth eating for more than
  hunger.
- The Ossuary Clock is worth building.
- A night of careless projection near home has a consequence the player can see coming and can prevent.
