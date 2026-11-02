# Phlegethontic Bombardier Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a neutral, territorial, Flammeus-aligned Nether bombardier beetle that lives on Escharian Overgrowth in the Phlegethontic Basin and attacks provokers with a telegraphed abdominal flame sweep.

**Architecture:** Implement the creature as a server-authoritative `PathfinderMob` that also implements `NeutralMob`. Keep habitat resolution, connected-outcropping identity, cone targeting, and the behaviour state machine in focused helpers so they can be tested without a renderer. Synchronize only presentation state and attack aim; clients animate and emit visuals from that state. The creature may inspect Escharian Overgrowth, stand on it, graze it cosmetically, and use it for faster chemical recharge, but it must never modify the block or its state.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge 21.1.219, the repository's existing entity-registration and animation/rendering stack, JUnit, NeoForge GameTest, Gradle.

**Spec:** Creature contract is restated in this plan under “Authoritative Behaviour Contract.” Environmental dependency: `docs/superpowers/specs/2026-09-14-escharian-overgrowth-connected-growth-design.md`.

## Global Constraints

- Entity ID: `hemomancy:phlegethontic_bombardier`.
- Habitat block ID: `hemomancy:escharian_overgrowth`.
- Natural spawning is restricted to `hemomancy:phlegethontic_basin`; there is no generic-Nether fallback.
- Escharian Overgrowth remains one block ID with neighbour-derived six-direction connectivity and deterministic visual forms.
- Do not add a BlockItem, drops, loot rewards, recipes, trades, composting, fuel, potting, right-click behaviour, damage, redstone/fluid interaction, random ticking, bonemeal response, cultivation, spreading, decay, regeneration, or extra growth block IDs to Escharian Overgrowth.
- The Bombardier must never place, destroy, replace, age, consume, or change block states while resting, grazing, recharging, retaliating, or firing.
- The creature is neutral until it or a colony member on the same connected outcropping is harmed. Proximity alone may wake it and trigger a warning display, but never an attack.
- The flame originates from the raised abdominal nozzle, not the mouth.
- The flame attack is a short sweeping cone with a readable wind-up, an aim-lock window, discrete damage pulses, line-of-sight checks, and a substantial cooldown.
- The sprayed fire must reuse the visual language of the currently implemented Flammeus player manipulations: a pinkish-red flame body with the same black-tipped flame motifs. Do not render it as ordinary orange, yellow, or vanilla Nether fire.
- Camouflage is visual pose and texture matching only; never apply invisibility or suppress the entity outline when targeted.
- Do not add new reagent items, armour ingredients, manipulation unlocks, spawn eggs, or progression gates in this implementation.
- Reuse the repository's existing animation library and registration patterns; do not add a new rendering or animation dependency.
- All damage, target selection, anger, charge, and habitat decisions are server-authoritative. Client code must not enter dedicated-server classloading paths.

---

## Authoritative Behaviour Contract

### Identity and balance constants

| Property | Value |
| --- | ---: |
| Max health | 28 |
| Armour | 8 |
| Movement speed | 0.22 |
| Follow range | 20 blocks |
| Knockback resistance | 0.45 |
| Experience reward | 5 |
| Natural group size | 1–3 |
| Biome spawn weight | 8 |
| Habitat search | 12 blocks horizontal, 6 vertical |
| Territory leash | 14 blocks from outcropping anchor |
| Colony alert radius | 18 blocks |
| Persistent anger | random 20–39 seconds |
| Camouflage delay | 80 idle ticks on valid Overgrowth support |
| Passive warning distance | 5 blocks |
| Flame engagement range | 3–10 blocks |
| Wind-up | 30 ticks |
| Aim tracking | first 22 wind-up ticks |
| Aim lock | final 8 wind-up ticks |
| Flame duration | 16 ticks |
| Horizontal sweep | −25° to +25° around locked aim |
| Cone half-angle | 12° around the instantaneous sweep ray |
| Damage pulses | flame ticks 0, 5, 10, and 15 |
| Damage per pulse | 2 fire damage |
| Ignition | 4 seconds per successful pulse, refreshed rather than stacked |
| Recharge on Overgrowth | 120 ticks |
| Recharge away from Overgrowth | 240 ticks |

### Behaviour states

Use one enum with these stable serialized names:

```java
public enum BombardierState {
    IDLE,
    CAMOUFLAGED,
    WARNING,
    GRAZING,
    WINDUP,
    FIRING,
    COOLING
}
```

- `CAMOUFLAGED`: flattened resting pose on top of Escharian Overgrowth; no invisibility.
- `WARNING`: rises, exposes oxblood abdominal plates, faces a nearby player, and hisses; cannot deal damage.
- `GRAZING`: cosmetic mandible animation directed at nearby Overgrowth; no block callbacks or mutation.
- `WINDUP`: tracks the anger target for 22 ticks, locks yaw/pitch for 8 ticks.
- `FIRING`: sweeps the locked direction across 50° over 16 ticks and applies four server-side hit pulses.
- `COOLING`: cannot fire; attempts to return to a valid position above its outcropping and visibly vents.

### Outcropping identity

An outcropping is one six-directionally connected component of `hemomancy:escharian_overgrowth`. Resolve its stable identity by flood-filling at most 256 blocks and choosing the lexicographically smallest `BlockPos` by `(y, z, x)` as the component anchor. The generation contract caps normal organisms below this traversal ceiling. Re-resolve only when spawning, loading, or when the stored anchor is no longer an Overgrowth block; do not flood-fill every tick.

---

## File Map and Repository-Fit Rule

The source repository was not available while this plan was written, so existing registry class paths must be resolved from code rather than guessed. Before editing, run:

```bash
rg -n "DeferredRegister.*ENTITY_TYPES|registerEntityType|EntityType\.Builder" src/main/java
rg -n "SpawnPlacementRegisterEvent|registerSpawnPlacements|SpawnPlacements\.register" src/main/java
rg -n "GeoEntity|AnimatableInstanceCache|EntityModel<|AnimationState" src/main/java
rg -n "Fervent|Flammeus|tendenc|enzyme" src/main/java src/main/resources
rg -n "flame|Flame|particle|Particle" src/main/java src/main/resources/assets/hemomancy
rg -n "GameTest|HarbingerPilotGameTests|HemoTestScenarioCatalog" src/gameTest src/main/java src/test
```

Modify the matching existing registry/event files found by those searches. Do not create a parallel registration framework.

Create these focused types beneath the repository's existing entity package:

- `PhlegethonticBombardier.java` — synced data, persistence, attributes, goals, and high-level state transitions.
- `BombardierState.java` — stable behaviour enum.
- `BombardierHabitat.java` — support checks, nearest-growth search, component flood-fill, and component anchor.
- `BombardierFlameGeometry.java` — pure sweep direction and cone-membership mathematics.
- `BombardierFlameAttack.java` — server-side ray/line-of-sight selection and damage pulses.
- `PhlegethonticBombardierRenderer.java` and the renderer stack’s required model class — client-only presentation.

Create these resources:

- `src/main/resources/assets/hemomancy/textures/entity/phlegethontic_bombardier.png`
- `src/main/resources/assets/hemomancy/lang/en_us.json` entry `entity.hemomancy.phlegethontic_bombardier`
- The animation stack’s conventional model and animation files, named `phlegethontic_bombardier`.
- `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_phlegethontic_bombardier.json`
- `src/main/resources/data/hemomancy/loot_table/entities/phlegethontic_bombardier.json`

Create focused JVM and GameTest classes under the same package layout already used by existing tests:

- `BombardierFlameGeometryTest.java`
- `BombardierHabitatTest.java`
- `PhlegethonticBombardierGameTests.java`

---

### Task 1: Register the Entity and Its Stable State

**Files:**
- Create: `PhlegethonticBombardier.java`
- Create: `BombardierState.java`
- Modify: the existing entity type registry resolved by the repository-fit search
- Modify: the existing common event subscriber used for entity attributes
- Test: the existing registry/resource contract test class

**Interfaces:**
- Produces: `EntityType<PhlegethonticBombardier>` registered as `hemomancy:phlegethontic_bombardier`.
- Produces: `BombardierState getBombardierState()` and `void setBombardierState(BombardierState state)`.
- Produces: `Optional<BlockPos> getTerritoryAnchor()` and `void setTerritoryAnchor(@Nullable BlockPos anchor)`.

- [ ] **Step 1: Inspect and record existing conventions**

Run the five repository-fit `rg` commands above. Use the exact package, deferred-register helper, attribute event, client registration, and animation system already present. Confirm Java 21, Minecraft 1.21.1, and NeoForge 21.1.219 from Gradle files before editing.

- [ ] **Step 2: Write the failing registry contract**

Add a JUnit assertion alongside existing registry contract tests:

```java
assertNotNull(BuiltInRegistries.ENTITY_TYPE.get(
    ResourceLocation.fromNamespaceAndPath("hemomancy", "phlegethontic_bombardier")
));
```

Run:

```powershell
./gradlew.bat test --tests "*Registry*"
```

Expected: failure because the entity type is absent.

- [ ] **Step 3: Register the entity and attributes**

Use `MobCategory.CREATURE`, a fire-immune entity type, and dimensions derived from the finished Blockbench model. Register these attributes:

```java
public static AttributeSupplier.Builder createAttributes() {
    return PathfinderMob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 28.0D)
        .add(Attributes.ARMOR, 8.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.22D)
        .add(Attributes.FOLLOW_RANGE, 20.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D);
}
```

Implement `NeutralMob`, `PersistentAngerTarget`, and `RemainingPersistentAngerTime`. Save/load territory anchor, anger target, anger time, chemical charge/cooldown, and state. If a saved transient combat state is `WINDUP` or `FIRING`, load it as `COOLING` to prevent relog attack duplication.

- [ ] **Step 4: Run focused and full JVM tests**

```powershell
./gradlew.bat test --tests "*Registry*"
./gradlew.bat test
```

Expected: both commands pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java src/test
git commit -m "feat: register phlegethontic bombardier"
```

---

### Task 2: Resolve Escharian Habitat and Stable Colony Identity

**Files:**
- Create: `BombardierHabitat.java`
- Create: `BombardierHabitatTest.java`
- Modify: `PhlegethonticBombardier.java`

**Interfaces:**
- Produces: `boolean isValidSupport(LevelReader level, BlockPos feetPos)`.
- Produces: `Optional<BlockPos> findNearestSupport(LevelReader level, BlockPos origin, int horizontalRange, int verticalRange)`.
- Produces: `Optional<BlockPos> resolveComponentAnchor(LevelReader level, BlockPos growthPos, int visitLimit)`.
- Produces: `boolean sharesOutcropping(PhlegethonticBombardier first, PhlegethonticBombardier second)`.

- [ ] **Step 1: Write failing habitat unit tests**

Cover these cases using the repository's existing fake-level or lightweight block-access fixture:

```java
@Test void rejectsOrdinaryNetherSupport() { /* netherrack below feet => false */ }
@Test void acceptsEscharianSupport() { /* overgrowth below feet => true */ }
@Test void connectedGrowthReturnsSameAnchorFromEveryEntryPoint() { /* six connected blocks */ }
@Test void diagonallyTouchingGrowthRemainsSeparate() { /* diagonal only => different anchors */ }
@Test void traversalStopsAtVisitLimit() { /* 257-block synthetic component => empty result */ }
```

Run:

```powershell
./gradlew.bat test --tests "*BombardierHabitatTest"
```

Expected: failure because `BombardierHabitat` does not exist.

- [ ] **Step 2: Implement bounded habitat search**

Use six-direction adjacency for component traversal. Search support positions in deterministic expanding distance order. A valid standing position has Escharian Overgrowth directly below, non-solid collision space at feet and head, and a stable floor collision shape. Never call `setBlock`, `destroyBlock`, `randomTick`, `useItemOn`, or any growth method.

- [ ] **Step 3: Bind and maintain territory**

On finalization of natural spawn, find support within `(12, 6)`, resolve its component anchor with a 256-block ceiling, and store the anchor. On load, retain a valid anchor. If it is missing or no longer points to Overgrowth, reacquire at most once every 100 ticks; discard natural-spawned entities that cannot acquire habitat during spawn validation.

- [ ] **Step 4: Run habitat and full JVM tests**

```powershell
./gradlew.bat test --tests "*BombardierHabitatTest"
./gradlew.bat test
```

Expected: all habitat cases and the full suite pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java src/test
git commit -m "feat: bind bombardiers to escharian outcroppings"
```

---

### Task 3: Implement Neutral Territorial and Colony Retaliation AI

**Files:**
- Modify: `PhlegethonticBombardier.java`
- Test: `PhlegethonticBombardierGameTests.java`

**Interfaces:**
- Consumes: `BombardierHabitat.sharesOutcropping(...)`.
- Produces: persistent neutral anger and same-outcropping alert behaviour.

- [ ] **Step 1: Add failing GameTests**

Create two connected Overgrowth clusters separated by air, two Bombardiers on the first, and one on the second. Assert:

```java
gameTestHelper.assertTrue(first.getTarget() == null, "unprovoked bombardier must remain neutral");
// Hurt first with the fixture player, then advance two server ticks.
gameTestHelper.assertTrue(second.getTarget() == player, "same-outcropping colony member must retaliate");
gameTestHelper.assertTrue(third.getTarget() == null, "different outcropping must remain neutral");
```

Run:

```powershell
./gradlew.bat runGameTestServer
```

Expected: failure because goals and colony filtering are absent.

- [ ] **Step 2: Add goals in stable priority order**

Use this behaviour order, adapting constructor signatures to the mappings already used by the repository:

```java
goalSelector.addGoal(0, new FloatGoal(this));
goalSelector.addGoal(1, new BombardierFlameGoal(this));
goalSelector.addGoal(2, new ReturnToOutcroppingGoal(this, 1.15D, 14.0F));
goalSelector.addGoal(3, new EscharRestGoal(this));
goalSelector.addGoal(4, new EscharGrazeGoal(this));
goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
goalSelector.addGoal(7, new RandomLookAroundGoal(this));

targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
targetSelector.addGoal(2, new ResetUniversalAngerTargetGoal<>(this, true));
```

Do not rely on vanilla `setAlertOthers()` alone. Override or replace the alert step so only Bombardiers within 18 blocks whose stored component anchors match receive the target.

- [ ] **Step 3: Implement passive warning without aggression**

When a non-creative, non-spectator player enters five blocks of a camouflaged Bombardier, wake it into `WARNING`, face the player, and play one warning sound. Do not set a target, anger timer, or damage source. After the player leaves for 40 ticks, return to `IDLE` and later camouflage normally.

- [ ] **Step 4: Run GameTests and JVM tests**

```powershell
./gradlew.bat runGameTestServer
./gradlew.bat test
```

Expected: an unprovoked player remains unharmed; same-outcropping members retaliate; the separate colony remains neutral.

- [ ] **Step 5: Commit**

```bash
git add src/main/java src/gameTest
git commit -m "feat: add bombardier territorial retaliation"
```

---

### Task 4: Implement Pure Flame Sweep Geometry

**Files:**
- Create: `BombardierFlameGeometry.java`
- Create: `BombardierFlameGeometryTest.java`

**Interfaces:**
- Produces: `Vec3 sweepDirection(Vec3 lockedDirection, int flameTick, int flameDuration, double sweepDegrees)`.
- Produces: `boolean insideCone(Vec3 nozzle, Vec3 direction, Vec3 targetCenter, double maxRange, double halfAngleDegrees)`.

- [ ] **Step 1: Write failing geometry tests**

```java
@Test void sweepBeginsAtNegativeTwentyFiveDegrees() { /* assert yaw within 0.001 */ }
@Test void sweepCrossesLockedAimAtMiddleTick() { /* assert normalized dot product */ }
@Test void sweepEndsAtPositiveTwentyFiveDegrees() { /* assert yaw within 0.001 */ }
@Test void acceptsTargetInsideRangeAndCone() { /* true */ }
@Test void rejectsTargetBehindNozzle() { /* false */ }
@Test void rejectsTargetBeyondTenBlocks() { /* false */ }
@Test void rejectsTargetOutsideTwelveDegreeHalfAngle() { /* false */ }
```

Run:

```powershell
./gradlew.bat test --tests "*BombardierFlameGeometryTest"
```

Expected: failure because the geometry helper is absent.

- [ ] **Step 2: Implement deterministic geometry**

Interpolate sweep angle linearly from −25° to +25° over ticks `0..15`. Rotate the normalized locked direction around world Y. Cone inclusion must compare squared distance first and then use a dot-product threshold:

```java
double minDot = Math.cos(Math.toRadians(halfAngleDegrees));
return targetCenter.subtract(nozzle).normalize().dot(direction.normalize()) >= minDot;
```

Return `false` for zero-length vectors and non-finite inputs.

- [ ] **Step 3: Run focused and full JVM tests**

```powershell
./gradlew.bat test --tests "*BombardierFlameGeometryTest"
./gradlew.bat test
```

Expected: all geometry tests and the full suite pass.

- [ ] **Step 4: Commit**

```bash
git add src/main/java src/test
git commit -m "feat: add bombardier flame sweep geometry"
```

---

### Task 5: Implement the Server-Authoritative Abdominal Flame Attack

**Files:**
- Create: `BombardierFlameAttack.java`
- Modify: `PhlegethonticBombardier.java`
- Test: `PhlegethonticBombardierGameTests.java`

**Interfaces:**
- Consumes: `BombardierFlameGeometry.sweepDirection(...)` and `insideCone(...)`.
- Produces: `void tickServer(PhlegethonticBombardier attacker)`.
- Produces synced presentation fields: state, state tick, locked yaw, and locked pitch.

- [ ] **Step 1: Add failing combat GameTests**

Assert each observable contract separately:

```java
gameTestHelper.assertTrue(target.getHealth() == initialHealth, "wind-up cannot deal damage");
gameTestHelper.assertTrue(frontTarget.getHealth() < initialHealth, "front target must be hit");
gameTestHelper.assertTrue(backTarget.getHealth() == initialHealth, "rear target must not be hit");
gameTestHelper.assertTrue(blockedTarget.getHealth() == initialHealth, "solid cover must block flame");
gameTestHelper.assertTrue(attacker.getBombardierState() == BombardierState.COOLING,
    "attack must enter cooling");
```

Run the focused GameTest class and confirm failure.

- [ ] **Step 2: Implement wind-up and aim lock**

The mob may begin an attack only when angry, charged, on the ground, not submerged, and the target is 3–10 blocks away. During wind-up ticks `0..21`, face the target. At tick 22, snapshot normalized direction from abdominal nozzle to target centre into synced yaw/pitch. Do not update it during the final eight wind-up ticks.

- [ ] **Step 3: Implement four damage pulses**

On flame ticks `0, 5, 10, 15`, query living entities in a ten-block inflated AABB, exclude allies and other Bombardiers from the same outcropping, filter through pure cone geometry, then perform a clip/raycast from nozzle to target centre. Apply `2.0F` fire damage and set fire for four seconds only when unobstructed. One entity can be damaged at most once per pulse.

The nozzle position must be derived from the entity's body position and model-scale constants, not eye position. Keep the offset in one named method so it can be tuned against the model:

```java
public Vec3 getAbdominalNozzlePosition(float partialTick) {
    return position().add(getLookAngle().scale(-0.65D)).add(0.0D, 1.05D, 0.0D);
}
```

Adjust the two numeric offsets once after the final model is integrated; do not scatter duplicate offsets through renderer and combat code.

- [ ] **Step 4: Enter cooling and prevent terrain griefing**

After tick 15, set chemical charge to zero and state to `COOLING`. The attack must not place fire, Crimson Flame, fluid, particles as blocks, or any other block state. Visual particles are entities/packets only.

- [ ] **Step 5: Run combat tests**

```powershell
./gradlew.bat runGameTestServer
./gradlew.bat test
```

Expected: telegraph, cone, rear exclusion, cover, and cooldown assertions pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/gameTest src/test
git commit -m "feat: add bombardier abdominal flame attack"
```

---

### Task 6: Implement Resting, Grazing, Retreat, and Chemical Recharge

**Files:**
- Modify: `PhlegethonticBombardier.java`
- Test: `PhlegethonticBombardierGameTests.java`

**Interfaces:**
- Consumes: `BombardierHabitat.findNearestSupport(...)`.
- Produces: deterministic state transitions for `CAMOUFLAGED`, `GRAZING`, and `COOLING`.

- [ ] **Step 1: Add failing lifecycle GameTests**

Cover:

- 80 peaceful ticks on Overgrowth enters `CAMOUFLAGED`.
- A player within five blocks wakes it into `WARNING` without creating anger.
- Grazing leaves every Overgrowth block and block state byte-for-byte unchanged.
- Cooling on Overgrowth completes after 120 ticks.
- Cooling away from Overgrowth completes after 240 ticks.
- Breaking the stored anchor causes bounded reacquisition rather than per-tick traversal.

- [ ] **Step 2: Implement camouflage and grazing**

Camouflage requires valid Overgrowth support, no anger target, no recent hurt, and no navigation path for 80 ticks. Grazing is a random idle action with a 200–400 tick interval, lasts 40 ticks, and only rotates the head/mandibles toward the supporting growth. It calls no method on the block.

- [ ] **Step 3: Implement retreat and recharge**

During `COOLING`, prefer a reachable standing position above the stored connected component. Recharge advances by two charge units per tick while supported by Overgrowth and one unit per tick otherwise, with a 240-unit full charge. Thus recharge takes 120 ticks on habitat and 240 away. Failed pathfinding must not freeze recharge or the state machine.

- [ ] **Step 4: Run lifecycle tests and full suite**

```powershell
./gradlew.bat runGameTestServer
./gradlew.bat test
```

Expected: all lifecycle assertions pass and no Overgrowth state changes occur.

- [ ] **Step 5: Commit**

```bash
git add src/main/java src/gameTest
git commit -m "feat: add bombardier eschar lifecycle"
```

---

### Task 7: Add Model, Texture, Animations, Sounds, and Particles

**Files:**
- Create: `PhlegethonticBombardierRenderer.java`
- Create: the existing renderer stack's required model class
- Create: `src/main/resources/assets/hemomancy/textures/entity/phlegethontic_bombardier.png`
- Create: animation-stack model and animation resources named `phlegethontic_bombardier`
- Modify: the existing client entity-renderer registration file
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`

**Interfaces:**
- Consumes: synced `BombardierState`, state tick, locked yaw, and locked pitch.
- Consumes: the existing pinkish-red, black-tipped Flammeus flame particle/sprite implementation used by player manipulations.
- Produces: readable silhouettes for rest, warning, grazing, wind-up, firing, and cooling.

- [ ] **Step 1: Build the Blockbench model to the approved silhouette**

Use a six-legged beetle body with a low, broad thorax; heavy basalt-like wing cases; and a segmented abdomen capable of curling over the back. Give the terminal abdominal segment an organic nozzle. Do not attach weapons, tanks, pipes, metal machinery, dragon anatomy, or a mouth flame emitter.

- [ ] **Step 2: Paint the camouflage-readable texture**

Use brown-black eschar, charred umber, oxblood seams, iron-dark limbs, and restrained pale curled margins. Match the Escharian surface closely enough that the resting creature can be overlooked, while preserving readable leg joints and silhouette. Emissive pixels, if the renderer stack supports them, are permitted only for exposed reservoirs during `WINDUP` and `FIRING`; the resting carapace and Overgrowth remain non-emissive.

- [ ] **Step 3: Author state animations**

Provide clips for:

```text
idle
walk
flatten_rest
wake_warning
graze
abdomen_windup
abdomen_fire
vent_cooldown
hurt
death
```

The firing clip must hold the head largely independent while the abdomen curls and aims. Use the synced locked aim to rotate the abdominal chain or its terminal aiming bone; never rotate the whole creature instantaneously after aim lock.

- [ ] **Step 4: Reuse the established Flammeus fire presentation**

Register the renderer only in the client setup path. Locate the particle type, sprite set, colour constants, animation frames, and spawn helper already used by the new Flammeus player manipulations. Route the Bombardier spray through that shared implementation, extending the existing helper only when it cannot accept an origin and direction. The spray must have the same pinkish-red flame body and black-tipped motifs; do not make a second visually similar particle system and do not substitute vanilla orange fire particles.

Reuse vanilla or existing Hemomancy sounds in the first implementation: a low chitter for warning, a pressure hiss for wind-up, a fire-charge burst for firing, and an extinguish/steam sound for cooling. Emit the narrow server-visible Flammeus particle sweep from `getAbdominalNozzlePosition()`; particles must follow the same direction helper as damage.

- [ ] **Step 5: Verify client and dedicated-server classloading**

```powershell
./gradlew.bat runClient
./gradlew.bat runGameTestServer
```

Expected: model, texture, and every clip load without resource warnings; the dedicated server starts without loading renderer classes.

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/main/resources
git commit -m "feat: render and animate phlegethontic bombardier"
```

---

### Task 8: Add Natural Spawning, Tendency Data, Empty Loot, and Resource Contracts

**Files:**
- Create: `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_phlegethontic_bombardier.json`
- Create: `src/main/resources/data/hemomancy/loot_table/entities/phlegethontic_bombardier.json`
- Modify: the existing Flammeus/Fervent entity tendency data resolved by repository-fit search
- Modify: the existing spawn-placement registration event
- Test: the existing resource-shape contract test class

**Interfaces:**
- Produces: natural spawn restricted by biome and Escharian support.
- Produces: Flammeus/Fervent blood-analysis classification through the existing data schema.

- [ ] **Step 1: Add failing resource contract assertions**

Assert that:

- the biome modifier names only `hemomancy:phlegethontic_basin`;
- the spawner type is `hemomancy:phlegethontic_bombardier` with weight 8 and group size 1–3;
- the entity appears in the existing Flammeus/Fervent tendency data exactly once;
- its loot table has zero pools;
- no recipe or item tag references the entity.

Run the focused resource contract test and confirm failure.

- [ ] **Step 2: Add the biome modifier**

Use the NeoForge 1.21.1 add-spawns codec already used in the repository:

```json
{
  "type": "neoforge:add_spawns",
  "biomes": "hemomancy:phlegethontic_basin",
  "spawners": {
    "type": "hemomancy:phlegethontic_bombardier",
    "weight": 8,
    "minCount": 1,
    "maxCount": 3
  }
}
```

- [ ] **Step 3: Register the placement predicate**

Use `SpawnPlacementTypes.ON_GROUND` and `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES`. Accept only when the block directly below the candidate is Escharian Overgrowth, collision space is clear, difficulty is not Peaceful, and the current biome key is `hemomancy:phlegethontic_basin`. Do not accept Blood-Scorched Scab as direct support; the creature belongs to the outcropping rather than the basin generally.

- [ ] **Step 4: Add tendency and empty loot data**

Add the entity to the existing Flammeus/Fervent schema without inventing a second tendency system. Create an explicit empty entity loot table:

```json
{
  "type": "minecraft:entity",
  "pools": []
}
```

- [ ] **Step 5: Run resource, GameTest, and build gates**

```powershell
./gradlew.bat test
./gradlew.bat runGameTestServer
./gradlew.bat build --console=plain
```

Expected: all resources decode, natural spawn data loads, and the build passes without missing-registry or loot-table warnings.

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/main/resources src/test
git commit -m "feat: spawn bombardiers in phlegethontic basins"
```

---

### Task 9: Complete Integration, Regression, and Manual Behaviour Verification

**Files:**
- Modify: `PhlegethonticBombardierGameTests.java`
- Modify: `TESTING.md`
- Modify: `RELEASE_CHECKLIST.md`

**Interfaces:**
- Produces: a repeatable automated and manual acceptance gate for the complete feature.

- [ ] **Step 1: Add the final no-mutation regression GameTest**

Build a connected 45-block fixture matching the smallest approved Overgrowth organism. Snapshot every `BlockState`, provoke two Bombardiers, allow one full wind-up, firing sweep, retreat, graze, and recharge cycle, then compare the snapshot:

```java
before.forEach((pos, state) -> gameTestHelper.assertTrue(
    gameTestHelper.getLevel().getBlockState(gameTestHelper.absolutePos(pos)).equals(state),
    "bombardier lifecycle changed Escharian Overgrowth at " + pos
));
```

- [ ] **Step 2: Add a development scenario**

Add one stable scenario ID, `phlegethontic_bombardier_ecology`, to `HemoTestScenarioCatalog`. Setup creates one Overgrowth outcropping, two same-colony Bombardiers, a separate outcropping with one control Bombardier, and safe observation markers. Verification checks neutrality before provocation, same-colony anger after provocation, separate-colony neutrality, completed flame/cooling state, and unchanged Overgrowth states. Cleanup removes only fixture-owned entities and blocks.

- [ ] **Step 3: Document manual acceptance**

Add this checklist to `TESTING.md`:

1. Locate a naturally generated Phlegethontic Basin and confirm Bombardiers appear only on Escharian Overgrowth.
2. Approach a resting specimen; confirm it is discoverable on inspection, wakes, warns, and does not attack.
3. Strike one specimen; confirm only members of that connected outcropping retaliate.
4. Stand at the edge of the flame sweep, behind the creature, and behind solid cover; confirm cone and cover behaviour.
5. Observe retreat and verify recharge is visibly faster on Overgrowth.
6. Leave and reload during anger and during cooling; confirm anger persists and firing does not duplicate.
7. Confirm firing, grazing, and pathfinding never alter Escharian blocks or ignite terrain.
8. Compare the spray directly beside a new Flammeus player manipulation; confirm both use the same pinkish-red body, black-tipped flame motif, animation cadence, and material language, with no vanilla-orange fallback particles.
9. Inspect `latest.log` for missing models, textures, animations, particles, loot tables, biome modifiers, entity types, and client-only classloading errors.

- [ ] **Step 4: Run the complete project gates**

```powershell
./gradlew.bat test
./gradlew.bat runGameTestServer
./gradlew.bat alphaCheck --console=plain
./gradlew.bat build --console=plain
```

Expected: all four commands exit zero; GameTests report the Bombardier scenarios passed; runtime resource-log gate reports no missing or malformed resources.

- [ ] **Step 5: Perform the manual scenario**

```text
/hemo test setup phlegethontic_bombardier_ecology
/hemo test verify phlegethontic_bombardier_ecology
/hemo test clear
```

Record discovered balance adjustments separately. Do not change the authoritative constants during this task without adding or updating their tests.

- [ ] **Step 6: Commit**

```bash
git add src/gameTest TESTING.md RELEASE_CHECKLIST.md
git commit -m "test: cover phlegethontic bombardier ecology"
```

---

## Definition of Done

- `hemomancy:phlegethontic_bombardier` registers, saves, reloads, renders, animates, and runs on a dedicated server.
- Natural spawning occurs only in the Phlegethontic Basin and only with Escharian Overgrowth directly beneath the creature.
- Unprovoked players receive only a warning display; attacks require harm-based anger.
- Colony retaliation crosses only Bombardiers sharing the same six-connected Overgrowth component.
- The abdominal flame has a 30-tick telegraph, eight-tick aim lock, 16-tick sweep, four discrete damage pulses, cover checks, and no terrain placement.
- The flame spray reuses the currently implemented Flammeus manipulation effects and visibly matches their pinkish-red flames with black tips.
- Rest, grazing, retreat, and faster habitat recharge are visible and mechanically functional.
- Camouflage relies on pose and texture, not invisibility.
- The mob is recognized by the existing Flammeus/Fervent system.
- No new drops, recipes, items, spawn eggs, or progression gates exist.
- Escharian Overgrowth is unchanged throughout the complete lifecycle.
- JVM tests, focused GameTests, `alphaCheck`, and `build` all pass.

## Execution Handoff

Run this plan in an isolated worktree. Execute tasks sequentially because later tasks depend upon interfaces established earlier. The recommended mode is subagent-driven development with one fresh worker per task and a review gate between tasks; inline execution with checkpoints is also valid.
