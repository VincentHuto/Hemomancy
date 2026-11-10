# Track 4: Temper and catalysts

**Goal:** give tendency a spendable side. Alignment stays as who the Harbinger is. Temper is what they are carrying
right now: a small pool per school that Shapings and casts burn, refilled from living creatures or from a catalyst.
This is the second cost beside raw blood, and it is the reason to sample, keep and study creatures (Track 5).

**Depends on:** Track 1 for the Shaping charge point. Works without Track 3.

## Tendency today

- `common/capability/player/harbinger/tendency/BloodTendency`: a `tendency` map (no cap, can go negative) and a
  `transientAlignment` map that only scars write. Only the base map is saved. Sync sends effective values, so the
  client cannot tell base from scar.
- **It is never spent.** It is read by the alignment gate (Humilis 0 to 10, Mediocritas 0 to 15, avatar 50), by
  Dynamic Use in `ManipulationCostLedger` (cost ÷ up to 1.3 in the strongest school), by damage affinity
  (1.25 + 0.75 × align/100 against opposing-profile targets), by `LignumMortisRules.maxRadius`, by the Saint
  sarcophagus, by the Vein Mason's scar lesson, by `WillAmbushDirector.schoolFor` and by `MoltingService`.
- **It is gained** from casts (+0.5 primary, +0.25 secondary), from kills (base 1.0, with property multipliers up to
  ×10 for bosses), from Thelemic memories, and from Wills. **Absorbing blood from a living creature gives none.**
- A realistic early player sits at 25 to 75 in a main school. The Rite of Sanguine Attunement at D2 resets base to 0.

## What the code already gives

- **Every creature already has schools.** `EntityBloodProfile` / `BloodProfileData`: `tendencies`, `properties`,
  `requires_living_syringe`. Example, the blaze: Flammeus and Lux, flying, nether native, living syringe only.
- **Catalysts already exist under other names.** One enzyme per school (`EnumBloodTendency.getRepEnzyme`: vivacious
  Animus, ruinous Mortem, neurotic Ductilis, ferric Ferric, fervent Flammeus, frigid Congeatio, incandescent Lux,
  umbral Tenebris), one spore per school, and a vanilla item per school in `recipe/rendering` (blaze powder, bone,
  glowstone dust, ink sac, packed ice, rotten flesh, spider eye, hematic iron scrap).
- **An off-hand catalyst precedent:** the Sporitic Thurible.
- **A bounded per-school store precedent:** the Somatic Loom's `storedEnzymes` (1 enzyme = 3 spans, 192 per school).
- **One place every R cast pays:** `tryPerformAction`, between the alignment gate (about line 532) and
  `BloodAccounting.pay(CAST)` (about 549). `ManipulationCostLedger.collect` already has a `Category.TENDENCY` that
  shows in Scrying diagnostics.
- **Scars carry what is needed:** `assignedTendency`, `tier`, `deepenAmount`, `forEachActiveCerebralScar`; three tiers
  for each of the eight schools; 1 slot at D4, 2 at D5, 4 at D6.

## Target design

### 1. Two things, clearly split

| | Alignment (exists) | Temper (new) |
| --- | --- | --- |
| Meaning | What you have done | What you are carrying |
| Changes | Slowly, by casting and killing | Quickly, by drawing and spending |
| Spent | Never | By every Shaping pulse and cast of that school |
| Used for | Gates, affinity, lessons, Saints | Full-strength, normal-cost effects |
| On death | Kept | Half lost (`CL-11`) |

Temper lives in a new attachment, `BloodTemper`, in the same package, with its own `PlayerStateSync.Entry` and dirty
flag. `BloodAccounting` stays blood-only.

### 2. Capacity comes from alignment

`cap(school) = 20 + alignment(school) × 0.6`, clamped to 20..80. A Harbinger can carry a little of any school and a
lot of their own. This is how identity and the spendable pool stay tied together without one replacing the other.

### 3. Filling it

- **Absorbing from a living creature** gives Temper in that creature's schools, in proportion to the mL drawn. This
  closes the "absorption gives no tendency" gap and is the main source.
- **A catalyst** dissolves into the cast: an enzyme gives 10 (its existing amount), the matching vanilla item gives 3,
  taken from the off hand first and then the inventory. Coal and charcoal join fervent. This is the early-game path
  and the "I have no blaze" path: crude, and it eats items.
- **A vat or bound creature** (Track 5) is the renewable path: raw blood in, Temper out, through a living animal.
- Kills no longer need to be the way to chase a school. They still move alignment.

### 4. Spending it: one rule

Each Shaping pulse and each R cast costs Temper in its primary school (pulse 0.5, Humilis 1, Mediocritas 2, higher
ranks 3 to 5; starting numbers). Then:

1. Pay from the Temper pool.
2. If the pool is short, dissolve a catalyst.
3. If there is none, the effect runs **dry**: blood cost × 1.5 and strength × 0.75 for Shapings; × 1.25 and × 0.9 for
   combat casts (`CL-10`).

Nothing is ever refused for lack of Temper. The alignment gate still decides what can be attempted at all. The dry
multiplier is one more line in `ManipulationCostLedger.collect` under `Category.TENDENCY`.

Paths that do not charge Temper, declared rather than accidental: Drudge actions, `EntityCast`, the Crawling Choir
echo, Still Arts, Thelemic memories, weapon-form melee.

### 5. Scars stabilise

An active scar of a school cuts that school's Temper burn: tier 1 by 25%, tier 2 by 50%, tier 3 by 75%, and sets a
floor of `5 × tier` that never burns. A scarred specialist rarely thinks about their own school and still needs
creatures or catalysts for the others. This also answers what scars are for beyond alignment (`F-13` on upkeep is
then mostly moot).

### 6. Showing it

- Eight small marks beside the blood bar (a layer in `GuiLayerRegistrar`), lit by how full each school is. Only
  schools the player has ever held show.
- The Scrying diagnostics line for `Category.TENDENCY` reads "dry", "catalyst" or "tempered".
- Sync must send base alignment, scar alignment and Temper separately; today the client only gets the sum.

## Content this needs

- **Thin schools.** Lux is the scarcest (witch, guardian on kill). Flammeus has no Overworld vanilla source. Ferric
  needs golems or raids. The creeper's profile is Ductilis and explosive, though the user named it as a Flammeus
  source. Review all profiles with two aims: every school has at least one Overworld creature that can be kept, and
  the obvious guesses are right (creeper gains Flammeus as a second school).
- **A catalyst table**, one data file per school listing items and their worth, replacing the scattered mappings. The
  rendering recipes, spores and enzymes read from it.
- **Tendency-flavoured food** is a cheap extra source: a cooked dish that gives a little Temper (ties to Track 10).

## What changes, file by file

| File | Change |
| --- | --- |
| New `BloodTemper`, `TemperRules` (pure), `TemperCatalysts` (data) | The pool, the maths, the table. |
| `HemoAttachmentTypes`, `HemoCapabilityKeys`, `HemoCapabilityRegistrar`, `HemoCapabilityAccess`, `PlayerStateSync` | Registration and sync. |
| `BloodManipulation.tryPerformAction`, `tryPerformVerbPulse` | The charge. |
| `ManipulationCostLedger` | The dry line. |
| `BloodAbsorptionItem.absorbFromTarget` | Temper from living targets. |
| `ScarInit` / scar events | Burn reduction and floor. |
| `BloodTendency` sync | Split base from scar. |
| `GuiLayerRegistrar` | The marks. |

## Tests

- New: `TemperRulesTest` (cap, fill, pay order, dry multipliers, scar reduction), `TemperCatalystDataTest`, a
  `temper_validation` GameTest batch.
- Retune: `ManipulationBalancePass2CastGameTests`, `BloodManipulationSecondaryTendencyTest`,
  `TendencyAffinityRulesTest`, `PlayerStateSyncTest`, `RenderingRecipeResourceTest`,
  `EnzymeReagentEconomyResourceTest`.
- Unchanged and must stay so: `BloodTendencyTransientAlignmentTest`, `MoltingRulesTest`, `EntityBloodProfileTest`.

## Hazards

- **Combat chore risk.** If every fight needs a stocked pool, casting becomes bookkeeping. That is why dry is mild in
  combat and why scars exist. Tune from the playthrough suite's D5 strain scenarios.
- **Early game.** With 1 to 7 Humilis manipulations per school and no creatures kept, a new player lives on catalysts
  and dry casts. The First Bloodcraft lessons should show the catalyst once, with an item the teacher hands over.
- **Double counting.** Kill shift and absorption can both reward the same creature today (Track 0).
- **The Attunement reset** zeroes alignment, so caps drop to 20. Say so in the rite's text.

## Open questions

`CL-9`, `CL-10`, `CL-11`. Also: `F-13` (scar upkeep) and `F-12` look answered by this track; confirm when it lands.

## Acceptance

- A cast with Temper costs and hits as today. A cast without it, and without a catalyst, costs more and hits softer,
  and the HUD says why.
- Absorbing from a blaze fills Flammeus and Lux. Coal in the off hand covers a Pyretic Forge pulse.
- A tier 3 scar holds its school steady through a long fight.
