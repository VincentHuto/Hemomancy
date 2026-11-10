# Track 3: Living blood

**Goal:** blood is fuel only while something alive holds it. Inert containers clot. Machines keep a small working
buffer and drink from their owner. This is the rule that stops blood becoming RF, and it gives Sanguine Formations a
real source.

**Depends on:** Track 0. Independent of Tracks 1 and 2.

## Where blood sits today

Nothing decays anywhere. Every block reservoir is the same `HemoAttachmentTypes.BLOCK_BLOOD_VOLUME` (a plain
`BloodVolume`, default max 5,000).

| Holder | Max (mL) | Spends | Verdict |
| --- | --- | --- | --- |
| SSC | 30,000 | 500 per Drudge scan | Shrink to a working buffer |
| Gourdvine Tap | 10,000 | makes 0.10 to 0.50 per tick | Living. Shrink to 2,000 |
| Bloodsmith's Crucible | 8,000 | 3,000 to 6,000 per scar | Keep size; fill and fire |
| Armature | 8,000 | 250 to 2,000 one-off | Keep size; fill and fire |
| Alembic | 5,000 / 7,500 | tinctures 2,500 / 5,000; makes +100 per distillation | Keep size; fill and fire; stop making blood |
| Resonant Forge | 5,000 | reserved at `begin` | Keep size; fill and fire |
| Sanguine Vigil | 5,000 | 25 per hostile per second | Working buffer |
| Mycelial Lantern | 4,000 | 600 per enzyme | Working buffer (Track 7 re-houses it) |
| Incubator | 2,500 | about 100 per craft | Working buffer |
| Centrifuge | 2,000 | never; makes +250 per spin | Working buffer; stop making blood |
| Scriptorium | 2,000 | 100 to 300 | Working buffer |
| Somatic Loom, Throne | 5,000 (default) | never | Remove the tank |
| Sanguine Condenser | 300 / 750 / 1,500 | clots into Formations | Already the model |
| Morphling Cradle | 800 internal | owner draw within 24, bloodline ×1.5 | Already the model |
| Gourds (White, Red, Black, Horn, Rib) | 1,000 to 5,000 | worn | Living. One worn gourd counts, as today |
| Flask, Jug | 2,500 / 5,000 | carried | Inert bulk. Clot (`CL-6`) |
| Bloodline pool | 5,000 per player + 1,000 per NPC member | FANE links | Living kin. Stays |

A dedicated player can hold well over 100,000 mL outside a 5,000 mL body, and none of it ever spoils.

## What the code already gives

- **Two blocks already work the wanted way.** The Condenser turns projected blood into Formations on a timer. The
  Cradle keeps a small buffer and draws from its owner through `BloodAccounting.pay(owner, OWNER_DRAW)`, with a
  bloodline fallback at a worse rate.
- **Suture routing is an owner-draw system waiting to be the default.** `BloodRoutingSavedData` maps a position to
  `DirectBloodLinkData(owner, mode NEARBY|FANE, bloodlineEnabled, workingReserve=600)`. `BloodRoutingHelper` draws up
  to 80 mL per pass from the owner above a 50% floor, tops the target only to `min(600 + 80 × level, max)`, and does
  nothing when the owner is offline, dead, inactive, or out of range. It already skips unloaded chunks.
- **One choke point for every reservoir.** `IBloodReservoir.getBloodCapability()`,
  `HemoCapabilityAccess.getBloodVolume(BlockEntity)` and `takeProjectedBlood`.
- **`BloodAccounting.payUpkeep`** exists for recurring owner payments.

## Target design

### 1. Freshness on the container, settled lazily

`BlockBloodVolume extends BloodVolume` with `lastFedTick` and a `clotted` amount. Nothing ticks. Whenever the volume
is read through the choke points, it settles: `elapsed = gameTime - lastFedTick`; inside the fresh window nothing
happens; past it, a share of the fresh blood moves to `clotted`. Being fed by Projection or by owner draw resets the
stamp. Lazy settlement means unloaded chunks cost nothing and clot correctly when they load.

Starting numbers (`CL-5`): 5 minutes fresh, then 10% of what remains per minute, so a full tank is mostly clot after
half an hour away.

`IBloodReservoir` gains `isLivingVessel()` (default false). The Tap, the Cradle, vats (Track 5) and living veins
(Track 6) return true and never clot.

### 2. Clotted blood is Formations

Pull the Condenser's held-Formation and extraction code into a shared component, with the rules in a pure
`CoagulationRules` (yield per mL, collapse chance, stage bonus). Any reservoir with `clotted > 0` shows it and gives
Formations when emptied by hand or hopper. The Condenser stays the deliberate station: it clots at once and at the
best yield; a neglected tank clots slowly and at a poor one. Neglect is a loss, not a strategy.

Then close the cheap side doors:

- `BloodFormationKeyPressPacket` (100 mL buys 0 to 3 Formations from the body) is far cheaper than any container.
  Retire it or price it above the Condenser.
- The 8% drop on big hits at D1 to D3 stays (it teaches the item exists). Loot and the shapeless recipe stay.
- About 67 recipes consume Formations. Check the yield against them once the numbers are set.

### 3. Machines drink from their owner

- Every blood machine records its placer as owner and implements `IBloodRoutingTarget` (nothing does today, and
  `IBloodReservoir.receiveBlood` is never called).
- The ladder is the existing one, made the default path instead of an add-on:
  - **Before Degree 3:** fill by hand with Projection. Fresh for five minutes. "Fill and fire".
  - **Degree 3, Suture:** a sutured machine draws from its owner within 16 blocks through
    `BloodAccounting.payUpkeep(owner, amount, source, ROUTE_INTERVAL_TICKS, false)`, keeping its working buffer
    topped and fresh.
  - **Degree 5, Fane:** the same from anywhere in the dimension, with the bloodline fallback.
- Deliberate Projection still fills faster than the Suture and is how the four large ritual tanks are charged.
- Owner offline, dead, or out of range: the machine stalls and its buffer clots (`CL-7`). A stalled machine says why
  on its screen and through the Vascular Status Gauge.

### 4. Sizes

- **Working buffer** (Incubator, Scriptorium, Vigil, Lantern, SSC, Centrifuge): the routing `workingReserve`, 600,
  plus upgrades. Enough for a few operations.
- **Ritual tank** (Crucible, Alembic, Armature, Forge): size unchanged, because one operation needs thousands. The
  window is what limits them: fill, then use.
- **No tank** (Loom, Throne): they never spend it.
- **Tap:** 2,000, living. It is the one renewable source, so its rate is the lever, not its size.
- **Free blood removed:** Centrifuge +250 per spin and Alembic +100 per distillation. Rib Gourd's +0.25 per tick is
  worn and living; keep.

### 5. Carried blood

- **Gourds** are grown, worn and alive. No change, and they are the answer to "I need more than my body holds".
- **Flask and Jug** clot (`CL-6`): a data component stamp settled when the item is used. They become how blood is
  carried to a job inside five minutes, or how a batch of Formations is made away from a Condenser.
- **Vitality Chalice** (undecided in the cleanup audit): a small hand basin, 250 mL, same rule.
- **Concentrated Blood** and tinctures are processed products, not blood. They do not clot.

### 6. Ownership (`CL-8`)

`BlockBloodInteractions.tryAbsorbFromReservoir` checks only `canProvideBlood()`. Thralls and Condenser collection have
no owner check either. Add one rule in one place: owner and bloodline may draw; others only if a server config allows.

## What changes, file by file

| File | Change |
| --- | --- |
| New `common/tile/BlockBloodVolume`, `CoagulationRules`, shared held-Formations component | The rule. |
| `HemoAttachmentTypes.BLOCK_BLOOD_VOLUME`, `HemoCapabilityAccess.getBloodVolume(BlockEntity)`, `IBloodReservoir` | Type change, settle on read, `isLivingVessel`. |
| Each holder in the table | Size, owner field, `IBloodRoutingTarget`. |
| `BloodRoutingHelper`, `BloodRoutingRules`, `BloodRoutingEvents` | Owner draw as upkeep; stamp the target fresh. |
| `SanguineCondenserBlockEntity` | Uses the shared component. |
| `CentrifugeBlockEntity`, `AlembicBlockEntity` | Stop minting blood. |
| `SomaticLoomBlockEntity` | Its cached `volume` goes with the tank. |
| Flask, Jug, Chalice items | Stamp component. |
| Station screens, Vascular Status Gauge | Fresh and clotted shown as two parts of one bar. |

## Tests

- New: `CoagulationRulesTest`, `BlockBloodVolumeTest` (lazy settlement over long gaps), a `living_blood_validation`
  GameTest batch (fill, wait, clot, extract; owner near, far, offline; living vessel never clots).
- Counts to re-pin in `BloodAccountingGuardrailTest` (every DRAINS and SUBTRACTS site listed there).
- Rewrite or retune: `ReservoirGateSourceTest`, `BloodReservoirContractSourceTest`, `BloodRoutingRulesTest`,
  `SanguineCondenserRulesTest`, `SanguineCondenserGameTests` (12),
  `HardeningGameTests.routingTickLeavesUnloadedLinksAlone`, `StationUpgradeActivationGameTests`.
- Playthrough pins that fill a tank and come back later: `PlaythroughNeophyteGameTests` (the 500 mL proof absorbs
  from an Alembic filled to 2,000), `PlaythroughVotaryGameTests`, `PlaythroughArchonGameTests` near line 898. Inside
  the fresh window these still pass; check each for a wait.

## Progression this touches

Listed in full in Track 11. The ones to settle before building: the D1 to D2 500 mL absorption proof; Alembic
tinctures; the Pallid Vessel (blood in a White Gourd, safe because gourds are living); Concentrated Blood and sleep;
Drudges and the SSC (their craft is withheld, `E-4`); Cardinal Rite anchors, which hold blood for a rite's length and
should count as living for that time.

## Hazards

- **Station upgrades.** `docs/STATION_UPGRADES.md` requires in-place state and escrow recovery. Capacity upgrades
  (Alembic 7,500) keep their meaning; buffer upgrades become "more operations per top-up".
- **Rites must not clot mid-rite.** An active rite's anchors and any `RiteOfferingEscrow` are exempt for the rite's
  duration.
- **Multiplayer.** Offline owners stall bases. That is intended, but a bloodline member within range should be able to
  stand in.
- **Feel.** Five minutes can read as a punishment timer. The screens should show freshness as a colour draining from
  the blood, not a countdown.

## Open questions

`CL-5`, `CL-6`, `CL-7`, `CL-8`.

## Acceptance

- No inert container holds usable blood indefinitely; a test walks every `IBloodReservoir` and asserts it either
  clots or declares itself living.
- A sutured machine with its owner nearby runs without hand feeding; with the owner gone it stalls and clots.
- Clotted blood comes out as Formations through one shared path.
- The total fresh blood a player can keep outside their body and worn gourd, an hour after last tending it, is zero.
