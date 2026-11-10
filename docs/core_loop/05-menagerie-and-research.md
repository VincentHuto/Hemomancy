# Track 5: Menagerie and research

**Goal:** the mad anatomist's loop. Find a creature, learn what its blood is, bring it home alive, keep it, and draw
from it without killing it. This is what Temper (Track 4) is refilled from, and it gives the Microscope a job.

**Depends on:** Track 2 (Blood Binding as a Shaping) and Track 4.

## What exists

- **Specimen Jar.** `SpecimenJarBlockItem.interactLivingEntity` → `SpecimenJarData.captureEntity` stores
  `entity.save()` in `CUSTOM_DATA.Specimen` and discards the entity. The tag `specimen_jar_capturable` holds 32 small
  mod creatures and no vanilla mob. No ticker, no blood, no health, no owner. The renderer rebuilds a client entity
  from the saved data with a single shared cache. Filled jars stack to 64.
- **Sampling.** `BloodVialItem.onLeftClickEntity` and `LivingSyringeItem.fillVialFromTarget`. It costs the creature
  nothing, has no cooldown and no limit. A sample records only the entity type.
- **Research.** The Microscope is an item (`HematicMicroscopeItem`, `MicroscopeExamination`, 40 ticks). It marks the
  stack identified and bumps a count in `ClinicalBloodProgress.examinedSources`. It unlocks two Liber entries and some
  lesson gates. A creature's schools act whether or not anyone has examined it, and every tooltip shows them.
- **Control.** Blood Binding stops movement. Hematic Impressment (`common/manipulation/HematicCommandManager`) is the
  best base: the mob follows, its owner is remembered, its flags are restored after. It lasts 300 to 600 ticks, costs
  500 mL, allows one per caster, up to 80 health, no bosses, and makes the mob fight for the caster.
  `SurrogateTethers` shows how to keep a binder's UUID on an entity attachment.
- **Limits.** `HematicAcclimationRules`: per player per creature type, absorption falls to ×0.5 at 750 mL, ×0.25 at
  1,250 and zero at 1,500, recovering at 0.25 mL per tick.
- **Unused.** `SanguineFertilityEffect` and its potion are registered and nothing applies them.

## Target design

### 1. Leading a creature home

- Holding Blood Binding's Shaping on a creature at or below a third of its health (or any passive creature) for three
  seconds **leashes** it: it turns placid, follows the binder, and stops despawning. The leash costs blood upkeep
  (`BloodAccounting.payUpkeep`, a new `Source`), scaled by the creature's max health. Stop paying and it breaks free,
  hostile creatures angry.
- Built on Impressment's follow and flag-restore code, with the state on an entity attachment (Track 0 moves it off
  the static maps) so it survives unload and relog.
- Led creatures at once (`CL-13`): one at D3, two at D5, three at D7.
- Not leashable: bosses, players, owned or tamed entities, `BoundPuppeteerSummon`, Wills, NPCs, Saint companions, the
  Warden. One `MenagerieRules.canKeep(entity)` (pure, tested) answers for leash, jar and vat alike.

### 2. Vats

A creature kept as a block does not despawn, wander, path, or load an AI. Two sizes:

- **Specimen Jar** (exists): small creatures. Gains a ticker and the rules below. Filled jars stop stacking.
- **Specimen Vat** (new): two blocks tall, for creatures up to about 2 blocks high and 40 health; a modelled
  set-piece block from a generator in `tools/model_export/`, hematic iron and glass, no copper.

Both hold the saved entity and a **Vigor** value, 0 to 100.

- Vigor falls slowly on its own and by a step with each draw.
- Vigor rises when the vat is fed blood: by Projection, or by owner draw through a Suture (Track 3). A vat is a
  living vessel, so the blood does not clot; it holds only a small buffer, the Cradle's 800.
- **Absorbing from a vat** gives Temper in the creature's schools and a little blood. The amount scales with Vigor
  and is cut by the same Acclimation rule as wild creatures, so ten cows are not ten times one cow.
- At zero Vigor the creature dies and the vat holds a husk (bone meal, a ruinous enzyme, a sour lesson).
- Hostile creatures in a vat with low Vigor rattle the glass. Below 10, a strong one can break out.

In the terms of rule 2: a vat stores no blood. It converts the keeper's blood into Temper, at a loss, through an
animal, at a rate the animal sets. The cap is upkeep and attention, not a number (`CL-13`).

### 3. Sampling costs something

- A draw takes health from the creature (a vial 2, a syringe 1) and sets a per-creature cooldown held on an entity
  attachment. A creature in a vat pays in Vigor instead.
- `BloodSamplingRules` already refuses dead, invulnerable and living-syringe-only targets; the cost goes there.
- First Draws is unaffected: five loose-vial samples from at least three of six farm animals is well inside the cost.
  `FirstDrawsAssignment.recordCollection` and the `PendingDelivery` claims in `AGENTS.md` do not change.

### 4. Research has a point

- `ClinicalBloodProgress.examinedSources` becomes a set of creature types the player has examined, not a count.
- Until a type is examined, its blood is **unlabelled** for that player: tooltips, the vat screen, the sample and the
  Temper marks show "unread humour". The effect is the same; the knowledge is not (`CL-12`).
- Examining a sample under the Microscope reveals schools and properties, writes the Bestiary page, and from then on
  the vat screen shows what a draw will give.
- A second level: examining samples from the same type at three Vigor levels (or from three individuals) reveals
  its yield and its quirks. This is the "ordered collection" lesson, which already exists as
  `clinical_ordered_collection` at three sources.
- Identification moves from the item stack to the player; a traded sample is still unlabelled to someone who has
  not done the work.
- `SpecimenBestiary*` (jar-based today) becomes the creature bestiary for everything kept or examined.

### 5. Projection into animals

Plain Projection, no Shaping, into a passive animal:

- A small dose applies Sanguine Fertility: the animal enters love mode, with the usual breeding cooldown.
- The cost is **taint**: a flag on the animal. A tainted animal's next drops are tainted meat (a food with a blood
  benefit for a Harbinger and nausea for anyone else), and each further dose raises the chance it goes **feral**,
  hostile for a minute.
- Passive animals have no attack attribute, so feral needs a small custom goal with fixed damage rather than a
  swapped AI.
- Offspring of a tainted parent can carry a weak second school in their profile. This is the hook for breeding a
  better source later; do not build the breeding system in this track.

### 6. The Sanguine Vigil's job

Under-exposed today. It already drains hostiles in a radius of 20. Make it the menagerie's guard and tender: with a
catalyst slot it keeps nearby vats' Vigor from falling while the keeper is within Suture range.

## What changes, file by file

| File | Change |
| --- | --- |
| New `common/entity/menagerie/` (or under `capability/entity/`): `MenagerieRules`, `LeashState` attachment | Who can be kept; the leash. |
| `HematicCommandManager` | Shared follow and restore; placid mode. |
| `SpecimenJarData`, `SpecimenJarBlockEntity`, new `SpecimenVatBlock` / `BlockEntity` | Vigor, ticker, feeding, draw. |
| `SpecimenJarRenderer` | A cache per block entity. |
| `BloodSamplingRules`, `BloodVialItem`, `LivingSyringeItem` | Cost and cooldown. |
| `ClinicalBloodProgress`, `ClinicalBloodKnowledge`, `BloodSampleData` | Per-player known types. |
| Tooltips, Jade-style overlay, vat screen | Unlabelled until known. |
| `BloodProjectionItem` chain (plain branch) | The animal dose. |
| `SanguineFertilityEffect` | Routed. |

## Tests

- New: `MenagerieRulesTest`, `VatVigorRulesTest`, a `menagerie_validation` GameTest batch (leash and lose upkeep; jar
  a riding mob; vat feed, draw, starve, breakout; unlabelled until examined).
- Retune: `BloodProfileGameTests` (13), `HematicAcclimationGameTests`, `HematicMicroscopeGameTests` (12),
  `HematicCommandGameTests`, `ClinicalBloodProgressionGameTests`, `SpecimenJar*Test`.
- Must not change: `FirstDrawsProgressTest`, `SanguineMarionetteGameTests`.

## Hazards

- **Tone.** This is vivisection played straight. The lore reference keeps Harbingers morally grey, not cartoon
  villains; the husk, the rattling glass and the taint are the mod being honest about the cost. Villagers are not
  keepable in this track.
- **Entity duplication.** Saving an entity into a block and discarding it is a classic dupe and loss surface: chunk
  unload during capture, pistons, block breaking by explosion. The vat drops itself with its occupant, like the jar.
- **Performance.** Each vat renders an entity. Cap render distance and skip animation past 16 blocks.
- **Lag-farm temptation.** Vats must not produce drops, eggs or experience.

## Open questions

`CL-8`, `CL-12`, `CL-13`.

## Acceptance

- A player can weaken a blaze, lead it out of the Nether, vat it, feed it, and refill Flammeus and Lux from it
  indefinitely for a blood cost, without it despawning.
- Neglect kills it.
- The player does not know what a new creature's blood is until they have looked.
