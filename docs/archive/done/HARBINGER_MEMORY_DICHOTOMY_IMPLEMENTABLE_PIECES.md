# Harbinger Memory Dichotomy: Implementable Pieces

> **Status:** Superseded implementation history. The active combined tracker is [MNEMONIC_POWER_OVERHAUL_IMPLEMENTATION_PLAN.md](MNEMONIC_POWER_OVERHAUL_IMPLEMENTATION_PLAN.md); implemented truth belongs in [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md).
>
> **Implementation authority:** Current Java and data remain authoritative for what already works. This document describes additions and extensions. It does not mark any proposed piece as implemented.
>
> **Execution rule:** Finish and verify one milestone before widening the system. The first milestone is deliberately small enough to test in a live client but complete enough to establish the contract used by everything after it.

## 1. Protected Design Boundaries

Every implementation piece below must preserve these rules:

- Muscle Memories and Willed Memories are complementary expressions of Hematic Memory. They are not classes, moral alignments, or mutually exclusive skill trees.
- Early behavior may guide the player toward an emphasis, but it must not create a hidden physical-versus-willed alignment score.
- No early choice may weaken or lock out Apotheosis or Severance. The irreversible opposition belongs to the final Harbinger response.
- Blood stores repeatable living instructions, not complete biographies. Ordinary memory content must not become unrestricted mind reading, resurrection, or universal reality editing.
- Learning a Muscle Memory is permanent. Tinctures add stored reserve, equipped memories share the existing radial capacity, and one active memory is allowed per vascular section.
- Active memories drain tincture reserve over time. Only a useful trigger spends blood and strains the body section that performs it.
- Automatic Muscle Memory costs must respect Sanguine Reserve protection.
- Morphling resonance improves efficiency and behavior. It must not multiply raw attributes until balance breaks.
- Puppets keep paying their existing Crossbar or Spindle cost. Owner blood is an added connection cost, not a replacement currency.
- Blood Formations are temporary forms, Blood Structures are persistent forms, and Cardinal Rites are timed events. Consecrated Structure is a descriptive label for hybrid Rite/Structure content, not a fourth runtime system.
- Existing Cardinal Rite, Blood Structure, Morphling, assignment, dialogue, and vascular systems should be extended through their current seams. Do not replace working systems with parallel frameworks.

## 2. Current Systems This Work Extends

This proposal is not greenfield. The current repository already contains most of the required foundations:

| Existing system | Current anchor | How this program uses it |
|---|---|---|
| Serializable player state | `HemoAttachmentTypes`, `HemoCapabilityKeys`, `HemoCapabilityRegistrar`, `HemoCapabilityAccess` | Stores known and primed Muscle Memories. |
| Blood economy | `BloodFlowLedger`, `BloodFlowContribution`, `BloodVolumeEvents` | Pays trigger and tether costs without bypassing reserve rules. |
| Vascular consequences | `EnumVeinSections`, `VascularSystemEvents` | Associates every body memory with Arms, Legs, Head, Heart, or Body. |
| Tendency growth | `BloodTendencyEvents` | Adds small aligned tendency shifts on successful use. |
| Enzymes and distillation | `EnzymeItem`, `DistillationRecipe`, Ghastly Alembic recipes | Produces tinctures and supplies their tendency language. |
| Morphlings | `IMorphling`, `MorphlingItem`, `EquippedMorphlingEvents` | Reuses preferred and secondary tendency relationships for resonance. |
| Puppetry | `MarionetteCrossbarItem`, `PuppeteersSpindleBlockEntity`, `BoundSummonBehavior` | Adds owner-blood upkeep and later tether counters. |
| Blood Structures | `BloodStructureRecipe`, `BloodStructureCraftingHelper`, blood-structure recipe JSON | Creates persistent works and Rite prerequisites. |
| Cardinal Rites | `CardinalRiteRecipe`, `CardinalRiteCeremonyDefinition`, `ActiveCardinalRite`, `CardinalRiteOrdealEngine` | Adds declared event verbs, consequences, and hybrid body-and-will objectives. |
| NPC guidance | Harbinger dialogue-tree classes, `DialogueEventHandler`, dialogue inquiry JSON | Delivers each NPC's interpretation without making them interchangeable. |
| Assignments | Existing assignment helpers, advancement criteria, Harbinger Assignment Ledger | Teaches the new systems through observable player actions. |
| Liber Sanguinium | `data/hemomancy/books/libersanguinium` | States the governing lore in the player's reference book. |

## 3. Delivery Order

| Milestone | Pieces | Player-visible result |
|---|---|---|
| **A. First bodily proof** | IP-01 through IP-05 | The player learns, primes, sees, uses, and is taught Sanguine Fists. |
| **B. Complete body loadout** | IP-06 through IP-08 | All five vascular sections have useful choices and enzyme alignment matters. |
| **C. Complementary playstyles** | IP-09 through IP-11 | Morphlings reinforce body memory while puppetry gains a blood connection and a counter. |
| **D. Form, structure, and event** | IP-12 through IP-15 | Crafting has a clear scale ladder and one Rite proves physical and willed play can cooperate. |
| **E. NPC and path framing** | IP-16 through IP-18 | Dialogue and assignments explain the dichotomy and reflect play without locking it. |
| **F. Ecology packages** | IP-20 through IP-25 | New creatures embody the physical and conceptual sides and have real progression uses. |

Milestone A is the recommended starting point. IP-01 through IP-05 form one vertical slice and should ship together. Later pieces are intentionally separable.

---

## Milestone A: First Bodily Proof

## IP-01. Muscle Memory Definition and Player State

**Type:** Foundation, persistent state, networking

**Delivers:** Permanent Muscle Memory knowledge, stored tincture reserve, a shared radial loadout, and one active memory for each vascular section.

### Runtime contract

Each Muscle Memory definition contains:

- Stable ID.
- Display name and description localization keys.
- Vascular section.
- Primary and optional secondary blood tendency.
- Primed duration.
- Trigger cooldown or cadence.
- Base blood cost.
- Base vascular strain.
- Client visual profile.

Player state contains:

- `known`: permanent set of learned memory IDs.
- `primedBySection`: at most one memory ID for each `EnumVeinSections` value.
- `expiresAt`: server game time for each primed section.
- No permanent physical-versus-willed lane score.

The first dose teaches the memory and adds reserve. Later doses add more reserve up to the cap. The radial controls activation, and only one active memory may occupy each vascular section. Death copies learned memories but clears reserve, activation, cooldowns, and armed overexertion. Legacy timed priming migrates into reserve during load and login.

### Code pieces

Create:

- `common/capability/player/harbinger/musclememory/MuscleMemoryDefinition.java`
- `common/capability/player/harbinger/musclememory/MuscleMemoryDefinitions.java`
- `common/capability/player/harbinger/musclememory/MuscleMemoryState.java`
- `common/capability/player/harbinger/musclememory/MuscleMemoryStateEvents.java`
- `common/network/capa/harbinger/MuscleMemoryStateServerPacket.java`

Extend:

- `HemoAttachmentTypes.java`
- `HemoCapabilityKeys.java`
- `HemoCapabilityRegistrar.java`
- `HemoCapabilityAccess.java`
- `PacketHandler.java`
- `VascularStatusScreen.java` with a read-only primed-memory name and remaining duration for each body section. This is the main loadout display; do not add a second full-screen Muscle Memory menu.

### Acceptance criteria

- A player can learn every registered memory.
- Priming another Arms memory replaces the current Arms memory but does not forget either one.
- A primed entry expires against server game time and cannot survive indefinitely through logout.
- Known state survives save/load and death; primed state survives save/load but clears on death.
- The server remains authoritative. Clients cannot grant knowledge, extend duration, or select unlearned memories.
- Tracking clients receive enough state to render another player's current primed section.
- The Vascular Status screen identifies every primed section and its remaining time without exposing stale client-only state.

### Verification

- Codec or serialization round-trip test.
- Replacement and expiry rule tests.
- Login, respawn, dimension change, and reconnect checks with two clients.

## IP-02. Shared Trigger, Cost, Strain, and Tendency Pipeline

**Type:** Gameplay foundation

**Delivers:** One service that every Muscle Memory uses when a meaningful action occurs.

### Trigger order

`MuscleMemoryActivationService.tryTrigger(...)` must perform this order on the server:

1. Confirm that the memory is known, primed in its own section, and not expired.
2. Reject duplicate, artificial, or cooldown-invalid triggers.
3. Compute tendency alignment and Morphling resonance through pure rule classes.
4. Calculate final blood cost, strain, duration, and effect strength within explicit caps.
5. Attempt a full blood payment through `BloodFlowLedger`.
6. If payment fails, apply no benefit, strain, tendency, or visual trigger.
7. Apply the gameplay benefit.
8. Apply section-specific vascular strain.
9. Shift the primary and secondary tendencies by the smaller Muscle Memory use amount.
10. Send one transient trigger packet for animation and feedback.

### Code pieces

Create:

- `MuscleMemoryActivationService.java`
- `MuscleMemoryTriggerRules.java`
- `MuscleMemoryEfficiencyRules.java`
- `MuscleMemoryTriggerResult.java`

Extend:

- `BloodFlowContribution.Category` with `MUSCLE_MEMORY`.
- `BloodFlowLedger.reserveProtectedCategory(...)` so automatic triggers honor Sanguine Reserve.
- `VascularSystemEvents` with an explicit-strain helper separate from manipulation strain.
- `BloodTendencyEvents` with a Muscle Memory use helper. Its default shift is `0.1`, one fifth of the current default manipulation-use shift.
- `HemoServerConfig.java` with `muscleMemoryTendencyShift`, default `0.1`, so server owners do not have to change manipulation or kill progression to tune this system.

### Initial shared tuning

- Primed duration: 6,000 ticks, or five minutes.
- Base trigger strain: `0.25` vascular health unless a power declares a different amount.
- Tendency shift: `0.1` total per accepted trigger, split `0.075` primary and `0.025` secondary when two tendencies are present.
- Identical event callbacks in the same server tick count as one trigger.
- Benefits require full payment. Partial payment never produces a partial benefit.

These are starting balance values, not lore constants. Keep them in one definition or configuration surface so later playtesting does not require edits across event handlers.

### Acceptance criteria

- Failed blood payment has no hidden side effects.
- Reserve protection stops an automatic trigger cleanly.
- One action cannot double-charge through two event callbacks.
- Tendency and strain only move after a benefit was actually applied.
- The pure rule layer can be tested without constructing a live level.

## IP-03. Sanguine Fists Power and Tincture

**Type:** Power, consumable, recipe

**Stable internal ID:** `hemomancy:sanguine_fists`

**Item ID:** `hemomancy:tincture_sanguine_fists`

**Section:** Arms

**Tendencies:** Fervent primary, Vivacious secondary

**Delivers:** The first complete Muscle Memory. A successful direct melee strike hardens the attacking hands, spends blood, strains the Arms, and gains a modest damage benefit.

### Initial behavior

- Drink one tincture dose to learn Sanguine Fists and add five minutes of stored reserve, then select and activate it through the shared radial.
- Trigger only on a valid direct melee hit dealt by the player.
- Exclude projectiles, thorns, reflected damage, sweeping secondary targets, fake-player automation, and attacks already canceled by another system.
- Spend `3` blood for the accepted hit.
- Add `2.0` damage before final mitigation.
- Apply `0.25` Arms strain.
- Use a four-tick per-target trigger guard so one swing cannot pay and apply twice through overlapping callbacks.
- When blood payment fails, the attack remains an ordinary attack and the overlay does not flash.

The first slice deliberately uses flat damage. Attack-speed modification can be considered later after the trigger, stacking, and animation behavior are proven. It must be audited against Blood Rush, weapons, Morphling attacks, and armor bonuses before inclusion.

### Content pieces

Create:

- `MuscleMemoryTinctureItem.java`
- Sanguine Fists registration in `ItemInit.java`.
- Item model, texture, name, tooltip, and guide text.
- Ghastly Alembic distillation recipe combining one Bloody Flask, one Sanguine Formation main ingredient, and one Fervent enzyme catalyst. Consume all three and produce one tincture in `200` ticks for `0.5` experience.
- Recipe advancement or assignment reward so the player is not expected to guess it.

The tincture returns one Cured Clay Flask after drinking unless the player is in Creative mode.

### Three-input Alembic support

The Alembic currently has a main recipe input and catalyst slot, but its existing flask slot is a reservoir-transfer slot. A Bloody Flask placed there is drained into the machine before it can act as a tincture ingredient. Do not overload that slot.

Add:

- `SLOT_TINCTURE_BLOOD = 5` to `GhastlyAlembicBlockEntity`, increasing `NUM_SLOTS` from five to six.
- A matching `TINCTURE_BLOOD_SLOT` in `GhastlyAlembicMenu` and a clearly marked recipe-blood slot in `GhastlyAlembicScreen`.
- A `blood_input` ingredient to `DistillationRecipe`, optional for old recipes and required by tinctures.
- A `consume_catalyst` recipe property, defaulting to `false` for compatibility and set to `true` on every tincture recipe.
- Codec and stream-codec support in `DistillationRecipeSerializer`.
- Three-stack recipe matching and atomic consumption in the Alembic's `canBurn` and `burn` paths.

The existing `SLOT_FLASK` and `SLOT_FLASK_OUTPUT` retain their reservoir-transfer behavior. Existing two-part distillation recipes omit `blood_input`, preserve their present catalyst consumption behavior, and load unchanged.

For a successful tincture craft, `burn` consumes exactly one main ingredient, one matching catalyst, and one matching blood input only after the result slot has accepted the complete output. A blocked output, missing input, removed heat source, or recipe mismatch consumes nothing.

Update shift-click routing, sided automation, container serialization, recipe lookup, progress reset, and JEI display for the new input. Old five-slot Alembic saves must load with the sixth slot empty.

Extend an existing combat event class or add `MuscleMemoryCombatEvents.java`; do not place combat logic in the item class.

### Acceptance criteria

- Drinking an unknown tincture teaches and primes the memory.
- Drinking a known tincture refreshes it without duplicating knowledge.
- Sanguine Fists cannot distill unless the Bloody Flask, Sanguine Formation, and Fervent enzyme are all present in their correct recipe slots.
- A completed tincture consumes exactly one of each required input and does not drain its Bloody Flask into the Alembic reservoir.
- Existing flask filling, Bloody Flask draining, blood-gourd transfer, and two-part distillation recipes retain their current behavior.
- Shift-click and hopper automation route the recipe Bloody Flask to the new slot without stealing flasks intended for reservoir transfer.
- A real melee hit pays once and gains exactly one bonus.
- Insufficient blood, expired priming, a damaged reserve floor, and an unprimed state all fail safely.
- The bonus does not apply to projectile, thorns, puppet, or sweeping secondary damage.
- Sanguine Fists cannot trigger itself recursively from its added damage.

### Focused verification

- Automated tests for trigger eligibility, payment failure, duplicate prevention, and damage amount.
- Live combat checks with empty hand, sword, Blood Rush, an equipped Morphling, low blood, and Sanguine Reserve enabled.

## IP-04. Muscle Memory Body Overlay and Feedback

**Type:** Rendering, animation, sound

**Delivers:** The old black-red hardened-skin language returns as a body-section response rather than a permanent costume.

### Visual contract

- Reuse `assets/hemomancy/textures/entity/hardened_skin.png` and the existing blood-arm material language.
- An active memory shows the black-red body treatment on its assigned section; its stronger response window may intensify that treatment.
- A successful trigger produces the strong overlay and section-specific motion.
- Sanguine Fists contracts and pulses across the hands and forearms on impact.
- A failed trigger does not play the success animation.
- The overlay follows both wide and slim player models, armor visibility, invisibility, first-person hands, and third-person tracked players.
- Use authored Hemomancy effects where extra feedback is needed. Vanilla particles remain a deliberate fallback, not the default treatment.

### Code pieces

Create:

- `client/render/layer/player/MuscleMemoryOverlayLayer.java`
- `client/render/layer/player/MuscleMemoryOverlayModel.java`
- `client/event/MuscleMemoryFirstPersonRenderEvents.java`
- `client/state/MuscleMemoryClientState.java`
- `common/network/client/ClientboundMuscleMemoryTriggerPacket.java`

Extend:

- `LayerEvents.java` to register the layer for wide and slim skins.
- `PacketHandler.java` for transient trigger synchronization.

`CellHandLayer` and `BloodArmModel` are references for the visual language. They should not be overloaded with unrelated state checks that make held-item rendering dependent on Muscle Memories.

### Acceptance criteria

- Only Arms render for Sanguine Fists.
- First- and third-person trigger timing agree.
- Other players see the correct entity animate in multiplayer.
- Relogging clears stale transient animation while preserving legitimate primed state.
- The overlay does not render on an unrelated player because entity IDs were reused.
- Armor, invisibility, spectator mode, wide skins, and slim skins do not produce detached geometry.

### Required live checks

Automated state tests are not enough. Record a live client pass for first person, front third person, rear third person, slim model, armor, invisibility, and two-player tracking.

## IP-05. First Lesson: “The Body Answers”

**Type:** Dialogue, assignment, advancement, Liber entry

**Delivers:** The first power arrives as proof of the setting's memory rule instead of appearing as an unexplained potion buff.

### Assignment flow

**Giver:** Harbinger Alchemist

**Availability:** Degree 1, after the existing first bloodcraft introduction.

**Steps:**

1. Ask the Alchemist why an enzyme changes the same blood differently in different bodies.
2. Receive one Sanguine Fists tincture and its distillation recipe.
3. Drink it.
4. Land three paid Sanguine Fists triggers on living hostile targets.
5. Return after the Arms have accumulated at least `0.75` total Muscle Memory strain.

**Reward:** A second tincture, recipe permanence if the first dose was lost, and the Liber Sanguinium entry **The Body Answers**.

The assignment counts accepted Muscle Memory triggers, not generic damage. Creative-mode or command-granted advancement states may be supported for testing but must not be the normal completion route.

### Dialogue roles

- **Alchemist:** Explains that the tincture does not contain strength. It exposes an instruction the blood can repeat. Their blind spot is reducing the whole phenomenon to reagent and reaction.
- **Vicar:** Supplies one optional follow-up line: the body answering before the will is the Order's earliest proof that memory lives in blood.
- **Mnemonist:** Remains silent on the deeper distinction until later. Do not have every NPC explain the same thesis immediately.

### Data and code pieces

Extend:

- `HarbingerAlchemistDialogueTrees.java`
- `DialogueEventHandler.java`
- `HarbingerAssignmentLedgerScreen.java`
- advancement JSON under `data/hemomancy/advancement/hemomancy/`
- `data/hemomancy/books/libersanguinium/manipulations/chapter.json`
- `assets/hemomancy/lang/en_us.json`

Create:

- `MuscleMemoryAssignmentHelper.java`
- `data/hemomancy/books/libersanguinium/manipulations/pages/the_body_answers.json`
- Alchemist dialogue inquiry data for the tincture.

### Lore text requirements

The entry must state these four facts plainly:

1. Blood preserves instructions learned through living, not complete experiences.
2. A Muscle Memory repeats an instruction through the bearer's own body.
3. A tincture primes a known instruction; it does not permanently grant a potion effect.
4. Repetition spends blood and harms the vascular section performing it.

It must not introduce Willed Memory as a separate school or reveal the final path choice.

### Acceptance criteria

- A new player can discover the tincture without recipe lookup outside the game.
- Losing or drinking the issued dose cannot permanently break assignment progression.
- Ledger, dialogue, advancement, and Liber completion agree after reconnect.
- The Alchemist's wording and the Liber rule do not contradict each other.

---

## Milestone B: Complete Body Loadout

## IP-06. Arms and Legs Power Set

**Type:** Powers, tinctures, movement and mining hooks

### Laboring Arms

- **ID:** `hemomancy:laboring_arms`
- **Item:** `hemomancy:tincture_laboring_arms`
- **Section:** Arms
- **Tendencies:** Ferric primary, Neurotic secondary
- **Trigger:** Sustained eligible block breaking, charged at a controlled mining cadence rather than every tick.
- **Benefit:** `20%` mining-speed increase while a valid tool is already capable of harvesting the block.
- **Cost:** `1` blood and `0.10` Arms strain per twenty ticks of active eligible breaking.
- **Boundary:** Never raises harvest tier, supplies Silk Touch or Fortune, or makes an invalid tool valid.

### Coursing Legs

- **ID:** `hemomancy:coursing_legs`
- **Item:** `hemomancy:tincture_coursing_legs`
- **Section:** Legs
- **Tendencies:** Neurotic primary, Fervent secondary
- **Trigger:** Sprint distance, a jump that receives the bonus, or fall-control intervention.
- **Benefit:** `10%` ground movement, `0.15` additional jump velocity, and up to `20%` fall-distance reduction.
- **Cost:** `1` blood per sixteen sprinted blocks, `2` per enhanced jump, or `1` for each two points of prevented fall damage.
- **Strain:** `0.10` per sprint payment, `0.20` per jump, or `0.20` per fall intervention.
- **Boundary:** Does not stack step height in the first version. It must be audited against Bootlace and travel equipment before any step-height bonus is added.

### Hushed Gait

- **ID:** `hemomancy:hushed_gait`
- **Item:** `hemomancy:tincture_hushed_gait`
- **Section:** Legs
- **Tendencies:** Umbral primary, Neurotic secondary
- **Trigger:** Walking or swimming at light level `7` or lower when the player's next eligible movement signal would be emitted.
- **Benefit:** Suppresses the player's `STEP` and `SWIM` game events and lowers their corresponding footstep or movement sound while the trigger remains valid.
- **Cost:** `1` blood and `0.10` Legs strain per eight blocks of signal-suppressed movement.
- **Boundary:** Sprinting, jumping, landing, attacking, taking damage, using a block, opening a container, firing a projectile, and breaking or placing a block remain detectable. It does not grant Invisibility, reduce visual detection range, erase an existing mob target, suppress another entity, or duplicate Umbral Step teleportation.

Hushed Gait is the bodily precursor to more deliberate Umbral absence: the feet and lower body learn not to announce motion, while later Willed Memories can impose darkness, reversal, concealment, or displacement beyond the body.

### Implementation notes

- Laboring Arms and Sanguine Fists replace one another because both occupy Arms.
- Movement distance must be server-measured. Client movement packets cannot directly award benefits or tendency.
- Block-breaking benefits need an explicit eligible-tool check and fake-player exclusion.
- Hushed Gait must intercept only the player's eligible movement game events. Do not cancel unrelated world events globally or silence sounds for players who are not using it.

### Acceptance criteria

- Mining never bypasses tool tier.
- Starting and stopping a block does not generate repeated free charges.
- Sprint cost is distance-based and cannot be avoided by alternating sprint input.
- Jump and fall hooks cannot both charge for the same unmodified motion.
- Hushed Gait permits controlled movement past Sculk listeners but still exposes careless actions, sprinting, jumping, combat, interaction, and block work.

## IP-07. Head, Heart, and Body Power Set

**Type:** Powers, tinctures, survival hooks

### Predatory Eyes

- **ID:** `hemomancy:predatory_eyes`
- **Item:** `hemomancy:tincture_predatory_eyes`
- **Section:** Head
- **Tendencies:** Incandescent primary, Umbral secondary
- **Trigger:** Entering qualifying darkness or acquiring a moving hostile target in that darkness.
- **Benefit:** Night vision while the darkness condition is met and an eight-block hostile-motion outline for two seconds after acquisition.
- **Cost:** `2` blood every ten seconds while the darkness benefit is actively needed; `1` for a new target acquisition.
- **Boundary:** Does not hear through blocks, reveal loot, expose invisible entities without another valid reveal, or duplicate Witch's Ear sound detection.

### Second Pulse

- **ID:** `hemomancy:second_pulse`
- **Item:** `hemomancy:tincture_second_pulse`
- **Section:** Heart
- **Tendencies:** Vivacious primary, Ruinous secondary
- **Trigger:** Taking damage that would leave the player below `25%` health.
- **Benefit:** Absorbs up to `2` points of that incoming damage and grants two seconds of modest regeneration.
- **Cost:** `12` blood, `1.0` Heart strain, and a thirty-second cooldown.
- **Boundary:** Cannot activate on blood-cost damage, starvation, void damage, or its own consequence. It cannot create blood or trigger another emergency-healing loop.

### Enduring Viscera

- **ID:** `hemomancy:enduring_viscera`
- **Item:** `hemomancy:tincture_enduring_viscera`
- **Section:** Body
- **Tendencies:** Frigid primary, Ruinous secondary
- **Trigger:** Hunger or saturation consumption while below the full-food threshold.
- **Benefit:** Prevents `20%` of eligible exhaustion accumulation.
- **Cost:** `2` blood for each full exhaustion point prevented and `0.20` Body strain.
- **Boundary:** Does not raise maximum hunger, feed the player, erase starvation damage, or grant general poison immunity in the first version.

### Carrion Metabolism

- **ID:** `hemomancy:carrion_metabolism`
- **Item:** `hemomancy:tincture_carrion_metabolism`
- **Section:** Body
- **Tendencies:** Ruinous primary, Vivacious secondary
- **Trigger:** Finishing a food in the `hemomancy:muscle_memory/carrion_foods` item tag while the player can legitimately eat.
- **Initial tag:** Rotten Flesh, Spider Eye, Poisonous Potato, Pufferfish, and Raw Chicken.
- **Benefit:** Prevents only the Hunger, Poison, and Nausea effects supplied directly by the consumed tagged food and adds `1.0` saturation after the food's ordinary nutrition is applied.
- **Cost:** `4` blood and `0.25` Body strain per consumed item.
- **Boundary:** Does not cleanse an existing effect, prevent poison or hunger from another source, make non-food items edible, bypass normal eating rules, raise maximum hunger, or duplicate Winter Shroud's poison immunity.

Carrion Metabolism treats decay as substrate. It is not healing from death or feeding from kills, which keeps it separate from Gravecap, Deadman's Purse, Blood Absorption, and Ruinous manipulations that harm or dissolve external targets.

### Acceptance criteria

- Each power has a visible, meaningful trigger rather than a hidden constant attribute.
- Predatory Eyes and Witch's Ear remain distinguishable.
- Second Pulse cannot recurse or convert blood drain into healing profit.
- Enduring Viscera still requires food and cannot hold the hunger bar forever without blood loss and Body consequences.
- Carrion Metabolism and Enduring Viscera replace one another in the Body slot; their survival benefits cannot operate together.
- Carrion Metabolism only suppresses effects traced to the consumed tagged stack and cannot become general poison immunity.

## IP-08. Enzyme Alignment and Tincture Catalog

**Type:** Alchemy progression, balance, documentation

**Delivers:** Enzyme acclimation matters before advanced Memory Weaving without creating another permanent capability.

### Alignment rule

Reuse the player's current blood tendencies and each memory's primary and secondary tendencies:

| Relationship | Starting effect |
|---|---|
| Primary tendency is dominant or strongly represented | `10%` lower blood cost, rounded only after the full calculation. |
| Secondary tendency is dominant or strongly represented | `5%` lower blood cost. |
| Both are represented | Apply the better discount, not both. |
| Neither is represented | Base cost and strain. No punishment multiplier. |

This is a soft benefit. A Ferric player can still use Predatory Eyes, and an Incandescent player can still use Laboring Arms.

The existing enzyme mastery and tendency systems remain authoritative. Do not add a second hidden “enzyme acclimation” meter. Tincture recipes, NPC teaching, and the current tendency display are enough to make the connection legible.

### Catalog work

- Register all eight tinctures through the same item class and definition registry, with one primary tendency represented exactly once.
- Add a three-input Ghastly Alembic recipe for each: one Bloody Flask, one authored main ingredient, and the definition's primary enzyme as a consumed catalyst.
- Use Sanguine Formation as the shared main ingredient for the initial eight simple tinctures. Later advanced tinctures may replace it with a specific organ, animal material, or adapted tissue while keeping the Bloody Flask and aligned enzyme requirements.
- Add one Alchemist inquiry entry per tincture.
- Add a Liber table that names the section, trigger, tendencies, cost behavior, and main risk.
- Add recipe-discovery advancement rewards in Degree order rather than exposing every formula at Degree 1.

### Acceptance criteria

- One definition drives item tooltip, server rules, and guide text identifiers.
- Every tincture recipe declares `blood_input` and `consume_catalyst: true`.
- The eight registered definitions have unique primary tendencies covering Vivacious, Fervent, Neurotic, Incandescent, Ruinous, Frigid, Ferric, and Umbral exactly once.
- Alignment never changes whether a memory is legal to use.
- A player can see why a cost changed without inspecting source code.
- Recipe and book data contain no orphaned tincture IDs.

---

## Milestone C: Complementary Playstyles

## IP-09. Morphling Resonance

**Type:** System integration, efficiency, behavior

**Delivers:** Existing Morphlings reinforce the body memories closest to their preferred tendencies.

### Resonance rule

Use the equipped Morphling's existing `getPreferredTendency()` and `getSecondaryTendency()` values:

| Match | Resonance weight | Allowed use |
|---|---:|---|
| Memory primary matches Morphling preferred | `1.50` | Improve efficiency, strain smoothing, cadence, or a bounded behavior. |
| Memory primary matches Morphling secondary, or memory secondary matches preferred | `1.25` | Smaller version of the same benefit. |
| No match | `1.00` | Memory still works at its base values. |

The weight is an input to capped formulas. It is not a direct `1.5x` attribute multiplier.

### First implementation

- Preferred resonance: `15%` lower blood cost and `15%` lower vascular strain.
- Secondary resonance: `8%` lower blood cost and `8%` lower vascular strain.
- Discounts combine multiplicatively with enzyme alignment and have a total floor of `70%` of base cost and strain.

Create `MorphlingMuscleMemoryResonanceRules.java` and keep it pure enough for table-driven tests.

### Later behavioral resonance, one at a time

- **Irontooth + Laboring Arms:** Broaden the eligible mining-block whitelist without changing harvest tier.
- **Witch's Ear + Predatory Eyes:** Add directional sound awareness while preserving the Eyes' visual role.
- **Bootlace + Coursing Legs:** Smooth a single large Legs strain spike after a hard landing.
- **Emberfang + Sanguine Fists:** Convert controlled movement into one capped strike bonus, with no recursive damage.

The remaining Morphlings receive their own body-memory behavior only after the overlap audit proves there is a distinct role. Do not manufacture eight symmetrical bonuses for the sake of a table.

### Acceptance criteria

- Existing Morphling feeding, maturity, upkeep, and passive abilities remain unchanged unless a specific resonance explicitly says otherwise.
- Only the equipped Morphling supplies resonance.
- A mismatch is neutral, not a penalty.
- Cost floors prevent zero-cost or negligible-strain loops.

## IP-10. Puppet Tether Blood Upkeep

**Type:** Puppetry economy, owner-state integration

**Delivers:** Puppetry becomes an explicitly blood-maintained relationship while Morphlings remain blood-incorporated bodies.

### Rule

- The Crossbar or Spindle continues paying every puppet body's existing thread charge.
- The owner pays one additional blood upkeep per active tether session, not once per puppet.
- Starting value: `1` blood every forty ticks while at least one valid commanded puppet remains actively tethered.
- The session cost uses `BloodFlowLedger` category `PUPPET_TETHER` and respects Sanguine Reserve.
- If blood payment fails, puppets lose active commands and fall back to their current safe idle or release behavior. They are not silently killed or deleted.
- Multiple puppets on the same owner and controller do not multiply the owner-blood session cost.

### Code pieces

Create owner-level `PuppetTetherUpkeepEvents.java` and `PuppetTetherBloodRules.java`. Do not put the blood drain independently inside each `BoundSummonBehavior`, because that would accidentally convert a session cost into per-body upkeep.

### Acceptance criteria

- One puppet and five puppets on the same active controller draw the same owner-blood session cost while their existing Crossbar costs still differ normally.
- Unloaded, dead, released, or invalid puppets do not keep a session alive.
- A player cannot evade upkeep by rapidly swapping the Crossbar between hands or inventory slots.
- Failed upkeep produces readable command loss and no permanent entity corruption.

## IP-11. Morphling Tether Gnaw

**Type:** Counterplay, entity behavior, PvP-safe interaction

**Delivers:** A mature Morphling can attack the blood relationship controlling a puppet without simply dealing more generic damage.

### Rule

- Eligible Morphlings may target a hostile puppet tether when their owner is attacked by that puppet or explicitly marks it.
- A completed gnaw pulse damages tether stability or adds Crossbar strain. It does not directly damage the puppet's ordinary health.
- Three completed pulses within ten seconds sever one ordinary tether.
- Boss, story, and protected NPC tethers must opt in. They are immune by default.
- Friendly, allied, or same-owner puppets are never automatically targeted.
- Severing a tether uses the current safe release behavior and does not delete the entity.

### Visuals

Extend `PuppeteerThreadRenderer` with a frayed, bitten, or interrupted segment using authored cell/tendril effects. Both controlling and defending players need an audible and visible warning before severance completes.

### Acceptance criteria

- The system cannot grief friendly summons through proximity alone.
- The controller has time to respond.
- Crossbar charge, thread health, and tether stability are not three names for the same hidden value; the UI must name the one actually being damaged.
- Disconnects and chunk unloads clear unfinished gnaw progress safely.

---

## Milestone D: Form, Structure, and Event

## IP-12. Crafting Scale Classification

**Type:** Terminology, guide data, recipe audit

**Delivers:** Players can tell what kind of blood work they are performing before more hybrid content is added.

### Canonical ladder

| Work | Meaning | Runtime expectation |
|---|---|---|
| **Blood Formation** | Temporary matter or tool projected from blood. | Exists briefly, is carried, or disappears when its purpose ends. |
| **Blood Structure** | A repeatable instruction stabilized as persistent matter. | Remains in the world and is produced through `BloodStructureRecipe`. |
| **Consecrated Structure** | A persistent structure manifested by a Rite or materially used through its existing optional structure requirement. | Uses current Structure and Cardinal Rite behavior. It has no separate runtime type. |
| **Cardinal Rite** | A memory executed through time, space, participants, and response. | Uses the active Rite engine and resolves success or failure. |

### Work

- Audit current Blood Structure recipes and Liber wording against the ladder.
- Add a player-facing category or subtitle where existing screens already expose recipe descriptions.
- Update `MnemonicBlueprintItem` descriptions so structure targets and Rite targets are never called the same kind of work.
- Add a short Liber entry, **Scaffold and Vessel**, explaining that Sanguine Formation gives an instruction material support while Blank Hematic Memory gives an instruction a receptive conscious vessel. Neither item belongs exclusively to one playstyle.
- Do not add a required classification field to every recipe unless a screen or rule actually consumes it. Documentation and recipe type already establish most of the distinction.

### Acceptance criteria

- No existing recipe changes behavior merely because its guide wording was clarified.
- Formation, Structure, Consecrated Structure, and Rite are used consistently in dialogue, assignments, the Liber, and tooltips.

## IP-13. Existing Consecrated Structure Classification

**Type:** Lore terminology, content audit, guide wording

**Delivers:** Existing hybrid Rite/Structure content receives one consistent name without adding another crafting or Rite subsystem.

### Current precedent

- The **Consecrated Bloodwell** is already manifested by the Rite of the Founding Fane. It is the clearest current Consecrated Structure.
- `CardinalRiteRecipe` already supports an optional `requiredStructure` and `consumeRequiredStructure` rule.
- Rites that materially depend on those structures already express the hybrid relationship mechanically. They do not need a new recipe type or activation layer.

### Work

1. Catalogue the Consecrated Bloodwell and every current Cardinal Rite with a non-null required structure.
2. Identify which required structures are meaningful ritual organs rather than incidental layout support.
3. Call those authored hybrid Rite/Structure works **Consecrated Structures** in the Liber, Rite hints, dialogue, and reference documentation where the distinction helps the player.
4. Keep the current `requiredStructure` and `consumeRequiredStructure` behavior unchanged.
5. Do not add a consecration registry, block flag, block-entity state, new Rite profile, generic routing function, or mandatory transformation step.

### Acceptance criteria

- The Consecrated Bloodwell is documented as the existing primary example.
- Current required-structure Rites behave exactly as they did before the terminology pass.
- “Consecrated Structure” always means a hybrid Rite/Structure work, not every decorative block placed near a Rite.
- No new item, block, recipe, packet, saved state, or Rite lifecycle is introduced by this piece.

## IP-14. Declared Cardinal Rite Event Verbs

**Type:** Rite data model, reusable objective language

**Delivers:** Rite authors can state what event the player must perform instead of expressing every ordeal as a wave fight or hardcoded special case.

### Data model

Add optional declared verbs to `CardinalRiteCeremonyDefinition`:

- `pursue`
- `defend`
- `respond`
- `endure`
- `bind`
- `sever`
- `witness`
- `sacrifice`
- `overcome`

Existing recipes deserialize with no new verb and retain current behavior. A verb only gains runtime behavior when its controller is implemented and referenced by a recipe.

Create:

- `CardinalRiteEventVerb.java`
- `CardinalRiteEventObjective.java`
- serializer validation for objective parameters.
- objective progress sync for the current Rite display.

### First supported verbs

Implement only `endure`, `bind`, and `overcome` for the first hybrid Rite. Leave the remaining enum values data-valid only after each has a concrete objective schema and test. Do not add switch branches that silently count an unimplemented verb as success.

### Acceptance criteria

- All existing Rite JSON loads unchanged.
- Unknown verb IDs fail data loading with a recipe-specific error.
- An unimplemented verb cannot be authored as a completable Rite.
- Objective progress persists and resumes under the same rules as the current active Rite.

## IP-15. Hybrid Rite and Failure Consequence

**Type:** Cardinal Rite, encounter, progression proof

**Delivers:** One Rite makes Muscle Memory and Willed Memory cooperate without declaring either one the correct path.

### Proposed Rite: The Joined Instruction

**Degree:** 3

**Required structure:** None. The Rite uses its normal Cardinal Focus, floor, anchors, offerings, and Living Staff. It does not create or require a new persistent machine.

**Objectives:**

1. **Endure:** Keep the Rite stable through one hostile wave.
2. **Bind:** Place and maintain a Blank Hematic Memory at its mnemonic anchor.
3. **Overcome:** Trigger Sanguine Fists on the marked hostile three times while standing inside the Arms circle.

**Success:** Unlocks the Mnemonist's Willed Memory explanation and the next existing Memory Weaving progression reward.

**Failure:** Applies `2.0` Arms strain and leaves a visible but temporary blood scar at the Arms anchor. It does not consume permanent knowledge or lock the player out of retrying.

### Engine use

- Reuse `CardinalRiteRecipe`, `ActiveCardinalRite`, the ordeal engine, existing anchors, waves, persistence, and failure profiles.
- Add only the objective tracking and `vascular_strain` failure consequence needed by this Rite.
- Do not rewrite existing Puppeteer trials or migrate every Rite to verbs in the same change.

### Acceptance criteria

- The Rite can be completed by a player with Sanguine Fists and a Blank Hematic Memory regardless of their dominant tendency or equipped Morphling.
- The marked-hit objective counts accepted paid triggers, not ordinary hits.
- Failure and reconnect behavior use the existing Rite lifecycle.
- The Rite does not create, transform, or silently classify a persistent structure.

---

## Milestone E: NPC and Path Framing

## IP-16. NPC Lens Dialogue Pass

**Type:** Dialogue, degree gates, item inquiry

**Delivers:** Each Harbinger NPC explains the same practice through a different lens and exposes a different blind spot.

### Dialogue packages

| NPC | Degree and trigger | What they contribute | What they get wrong or understate |
|---|---|---|---|
| **Alchemist** | Degree 1, first tincture | The body can be primed to repeat an instruction. | Treats memory as if reagent and reaction explain all of it. |
| **Artificer** | Degree 2, first persistent Blood Structure | A structure is an instruction held in matter. | Assumes stabilized form is closer to truth than a changing event. |
| **Mnemonist** | Degree 3, after The Joined Instruction | Willed Memory imposes a learned condition; it is not merely mental magic. | Risks treating a perfect imitation as the person or will it resembles. |
| **Voyager** | Degree 4, first external or spatial memory use | Some instructions only become legible when separated from their first body or place. | Romanticizes dissolution and underprices the loss of origin. |
| **Cicatrix Anchorite** | Degree 4, first clotted section caused by memory use | Wound, repetition, and adaptation can write the same lesson through different means. | Mistakes suffering for proof that the lesson was worthy. |
| **Vicar** | Degree 1 introduction and Degree 7 synthesis | Holds body, will, structure, Rite, and belonging within one Order. | Presents unity as if it resolves the costs borne by individuals. |

### Rules

- Each package is a short topic with player questions, not a six-part lecture.
- Dialogue unlock conditions reuse known advancements, assignment completion, current Degree, known memory IDs, and Rite completion.
- Do not add a new persistent lore-topic capability if `DialogueKnowledge` already covers the needed topic state.
- Item inquiry JSON should answer what a tincture is and who can explain it, not repeat its entire tooltip.
- Add one paired follow-up between Alchemist and Artificer, one between Mnemonist and Voyager, and one between Vicar and Cicatrix Anchorite. The pairs disagree about emphasis without treating one another as enemies or already recognizing two opposed endgame camps.

### Acceptance criteria

- No two NPCs use identical explanatory text.
- The governing lore rule remains consistent across all six.
- Dialogue can be encountered out of the ideal order without claiming the player completed an event they have not completed.

## IP-17. Assignment and Quest Route

**Type:** Progression routing, optional tutorial chain

**Delivers:** The player is guided from body memory to Willed Memory, Structures, and Rites through actions rather than exposition.

### Route

1. **The Body Answers** — Alchemist, Degree 1. Learn and trigger Sanguine Fists.
2. **Two Instructions, One Limb** — Artificer, Degree 2. Learn Laboring Arms, intentionally replace Sanguine Fists in the Arms slot, then use the primed Arms while shaping a supplied Sanguine Formation into a temporary brace. This proves that knowledge and priming are separate and gives Degree 2 a small body-and-projection encounter before full Willed Memory teaching.
3. **What Blood Insists Upon** — Mnemonist, Degree 3. Bring a Blank Hematic Memory and complete The Joined Instruction.
4. **A Connection Must Be Fed** — Voyager or Puppeteer routing, Degree 4. Maintain one puppet tether and one equipped Morphling long enough to observe their different costs.
5. **Structure Within the Rite** — Artificer and Vicar, after the player's first existing required-structure Rite or the Rite of the Founding Fane. Unlock the Consecrated Structure terminology and identify the structure that participated. This is a lore acknowledgement, not another activation step.

### Progression policy

- Steps 1 and 2 teach core controls and may gate their specific recipes.
- Steps 3 through 5 are guidance and unlock lore, blueprints, or convenience rewards. They do not gate the main Degree ladder unless their underlying systems already belong to that Degree.
- A player may use both Morphlings and puppets. The quest never asks them to renounce one.
- Failure is recoverable. Lost issued items have recipe or dialogue recovery paths.

### Acceptance criteria

- Every objective is based on a server-observed event.
- Ledger text says exactly what action is missing.
- Repeating, replacing, losing, or crafting an item cannot soft-lock a step.
- Completing the route creates no permanent path selection.

## IP-18. Path Foreshadowing Without a Path Meter

**Type:** Conditional dialogue, endgame reflection

**Delivers:** The final Harbinger decision feels prepared by earlier play without being preselected by it.

### Implementation

Do not create `physicalAffinity`, `willedAffinity`, Hivemind favor, or Silent favor values.

At Degree 7, assemble reflection lines from concrete existing facts:

- Known and frequently primed Muscle Memories.
- Equipped and matured Morphlings.
- Known puppets or completed puppeteer ordeals.
- Consecrated structures and completed Cardinal Rites.
- Relevant advancements and NPC assignments.

The reflection can say what the player practiced. It cannot say what ending they ought to choose or change the availability, cost, or power of either response.

### Endgame lore contract

- **Apotheosis / Hivemind:** “To One from All.” The self persists through incorporation into a greater living whole, but sole ownership of body and memory is surrendered.
- **Severance / Silent Archon:** “From One to None.” The self becomes unreachable to the greater will, but loses the belonging, propagation, and shared continuity that came with it.
- A Severant cannot be willed into the Hivemind or physically absorbed by it.
- Neither response is secretly the physical lane or the willed lane. Both answer the same body-and-will problem differently.

### Acceptance criteria

- The same save can still choose either final response after every optional assignment.
- Reflection text does not expose hidden scoring because no hidden scoring exists.
- A player who ignored one subsystem receives a shorter truthful reflection rather than fallback claims.

## IP-19. Puppet Agency Classification

**Type:** Lore audit, dialogue, bestiary and ordeal text

**Delivers:** Puppetry content states what kind of self, if any, exists in the controlled body. This makes the later Hivemind question understandable without turning every puppet into the same moral case.

### Canonical agency states

| State | Meaning | Required player-facing cue |
|---|---|---|
| **Empty body** | A constructed vessel has no prior self and only executes supplied behavior. | Description names it as a vessel or shape, never a captive person. |
| **Recorded behavior** | A body repeats an extracted pattern but does not contain the source person's continuous self. | Dialogue calls it a record, echo, or imitation. |
| **Puppet** | An external body receives the Harbinger's current will through an attuned tether. | Crossbar or ordeal text names the active connection. |
| **Commandeered Will** | An existing will remains present but suppressed, redirected, or contested. | Encounter and dialogue acknowledge resistance and the possibility of release. |
| **Incorporated self** | A living identity has joined a greater organism and persists under changed ownership. | Reserved for Hivemind-facing content; never used as a synonym for an ordinary puppet. |

### Work

- Inventory current summon, puppet, Crossbar, Spindle, ordeal, and Commandeered Will descriptions.
- Assign one agency state to each existing case based on current mechanics and canon.
- Correct ambiguous dialogue, inquiry JSON, Liber text, bestiary text, and tooltips. Do not change mechanics just to make every case fit the same state.
- Add one Mnemonist question and one Vicar question that contrast imitation, suppression, and incorporation.
- Add an explicit safe-release cue anywhere IP-10 or IP-11 can sever an existing will's tether.

### Acceptance criteria

- No existing body is accidentally described as both empty and suppressed.
- A recorded behavior is not presented as proof that the original person survives inside it.
- Commandeered Wills retain the ethical distinction that someone is still present.
- The audit does not retcon ordinary puppet bodies into incorporated Hivemind members.

---

## Milestone F: Ecology Packages

These pieces are implementable content packages, but they should start only after their reagent or memory use is accepted. A creature without a progression job becomes disconnected content.

## IP-20. Cold Semi-Aquatic Animal

**Type:** Animal, reagent source, environment lesson

**Working role:** A cold-coast animal that dives beneath ice, slows its metabolism, and resurfaces in groups.

**Progression use:** Source for Enduring Viscera or Second Pulse reagent material and an Alchemist observation assignment.

**Behavior package:**

- Spawns on cold shores, frozen rivers, and water beneath surface ice.
- Breathes air but remains submerged longer than ordinary land animals.
- Retreats into water when harmed and clusters around safe breathing holes.
- Is neutral and breedable or renewable. The body-memory reagent must not require exterminating a rare animal.

**Required content:** Entity, renderer, authored model and animation, spawn placement, biome tags, breeding or renewal rule, loot/reagent, sound, egg, localization, Liber ecology entry, assignment hook, and multiplayer save/load tests.

## IP-21. Nether Wall Predator

**Type:** Hostile creature, physical adaptation encounter

**Working role:** A long wall-clinging sore or hanging mouth that detects heat and blood, anchors itself to Nether walls or ceilings, and pulls prey into its reach.

**Progression use:** Supplies a Fervent or Ferric reagent used by Sanguine Fists, Laboring Arms, or a physical Blood Structure.

**Behavior package:**

- Validates a wall or ceiling anchor before spawning.
- Remains attached while stalking and exposes a vulnerable body only while feeding or pulling.
- Uses a telegraphed tendril pull with a breakable line, not unavoidable displacement.
- Falls or retreats when its anchor is destroyed.
- Cannot pull through fully occluding terrain or outside loaded chunks.

**Required content:** Attachment navigation, anchor persistence, renderer, authored hanging and feeding animations, tendril effect, sounds, loot, spawn tags, advancement, and live latency testing.

## IP-22. Sculk or End Conceptual Creature

**Type:** Hostile or neutral conceptual creature, Willed Memory lesson

**Working role:** A creature whose threat is a repeated condition, false signal, copied action, or imposed perception rather than another claw-and-bite animal.

**Progression use:** Supplies a Willed Memory reagent, Mnemonist assignment target, or Rite objective. It must not merely drop a generic crafting ingredient.

**Behavior package:**

- Chooses one clear conceptual verb: copy, silence, misdirect, or repeat.
- Teaches that verb through readable environmental behavior before combat punishment begins.
- Interacts with sound or spatial memory if Sculk-based, or location and displacement if End-based.
- Has a reliable counter that uses existing Hemomancy knowledge, such as breaking a copied pattern, severing a relation, or refusing a false target.
- Never rewrites player inventory, permanent advancement state, or actual memories.

Choose Sculk or End when the exact verb is selected. Do not build both versions of the same role.

## IP-23. Desert Vulture

**Type:** Flying animal, scavenging ecology, survival lesson

**Progression use:** Teaches carrion detection, thermal movement, and hunger economy after Enduring Viscera already has a stable role.

**Behavior package:**

- Circles above desert carrion, recently killed entities, or authored carcass markers rather than acting as another always-hostile bird.
- Uses daytime thermals to gain height and descends when a valid feeding opportunity appears.
- Leads an observant player toward carrion but competes for a bounded reagent if ignored.
- Supplies a renewable or scavenged component. Its progression use must not require clearing every bird from a biome.
- Does not reveal arbitrary mobs, chests, structures, or players through terrain.

**Required content:** Flying navigation, thermal condition rules, carrion marker contract, model and authored flight/feeding animation, spawns, loot or collection interaction, sounds, bestiary entry, and Alchemist assignment.

## IP-24. Jungle and Lush-Cave Vampire Bat

**Type:** Flying animal, perceptual crossover encounter

**Progression use:** Provides an early bridge from bodily sense to deliberate perception without duplicating Witch's Ear.

**Behavior package:**

- Roosts in lush caves and dense jungle cover, then hunts wounded animals or exposed blood sources.
- Emits readable echolocation pulses that reveal its own position and nearby moving geometry for a moment.
- The player can bait or redirect it with a controlled blood source.
- Its reagent supports Predatory Eyes acquisition or a perception-focused Willed Memory, but never grants sound-through-wall awareness by itself.
- Remains distinct from vanilla bats through feeding, roosting, flock response, and the visible pulse mechanic.

**Required content:** Roost points, flock limits, echolocation effect, blood-lure interaction, model and authored animation, spawn tags, reagent, bestiary entry, and Mnemonist or Alchemist inquiry.

## IP-25. Carrion Bees

**Type:** Swarm animal, distributed appetite ecology

**Progression use:** Shows one appetite distributed across many bodies without declaring the swarm a literal Hivemind or an automatic Apotheosis symbol.

**Behavior package:**

- Builds a distinct carrion comb around eligible organic remains rather than replacing vanilla flower pollination.
- Individual bees carry food traces back to the comb and change the comb's stored state collectively.
- A disturbed swarm defends the comb, but ordinary harvesting has a pacification or smoke-like counter.
- The comb supplies a bounded alchemical binder for Blood Structures or group-oriented Rites.
- Swarm membership does not imply each bee shares a complete mind, memory, or player-controlling will.

**Required content:** Bee variant or dedicated entity decision, comb block and stored state, collection loop, renderer/assets, spawn and colony caps, loot, bestiary entry, and Vicar commentary about collective behavior versus incorporated identity.

---

## 4. Cross-Cutting Test Matrix

Every piece does not need every test, but each milestone must cover the applicable row before it is called complete.

| Area | Required evidence |
|---|---|
| Persistence | Save/load, death copy, reconnect, dimension change, and expired-state cleanup. |
| Authority | Client packets cannot learn, prime, pay, complete assignments, or progress Rites. |
| Blood | Full payment, low blood, reserve floor, concurrent upkeep, and no partial-benefit path. |
| Vascular | Correct section, exact strain, fortification interaction, clotted/dead behavior, and healing recovery. |
| Tendency | Only accepted triggers shift tendency; primary and secondary split is correct. |
| Combat | No recursion, sweeping duplication, projectile leakage, fake-player use, or armor/Morphling stacking failure. |
| Movement/mining | Server-observed cadence, tool eligibility, distance accounting, and chunk/teleport abuse. |
| Multiplayer | Self and tracking clients agree; no stale entity visual or cross-player assignment progress. |
| Rendering | First person, wide/slim third person, armor, invisibility, spectator, and animation reset. |
| Data | All recipes, advancements, book pages, item inquiries, models, loot, tags, and localization load without orphaned IDs. |
| Progression | Issued-item loss is recoverable; out-of-order completion does not soft-lock; neither ending is gated. |
| Existing systems | Focused regression for Blood Rush, Morphling upkeep, puppet charge, Blood Structures, and current Cardinal Rites. |

## 5. Recommended First Implementation Batch

Begin with IP-01 through IP-05 only:

1. Add Muscle Memory definition and attachment state.
2. Add the shared trigger/payment/strain pipeline.
3. Add Sanguine Fists and its tincture recipe.
4. Add the Arms overlay in first and third person.
5. Add The Body Answers assignment, dialogue, and Liber entry.

That batch proves the entire early-game loop:

> The player is told what the tincture means, learns a bodily instruction, primes it, sees the body answer, pays blood, damages the Arms through repeated use, gains aligned tendency, and returns to an NPC who interprets what happened.

Do not start Morphling resonance, puppetry changes, event verbs, or new fauna until that loop survives automated checks and a live client pass. Those systems depend on the trigger contract being stable.

## 6. Explicitly Deferred Decisions

The following are intentionally outside the first implementation batch, but their owning pieces are identified above:

- Attack-speed support belongs to IP-03 after Blood Rush and weapon stacking tests.
- Step height belongs to IP-06 after Bootlace overlap testing.
- Poison and breath effects belong to IP-07 after Enduring Viscera's hunger role is proven.
- Individual Morphling behavior bonuses belong to IP-09 after generic resonance works.
- Per-tether counterplay belongs to IP-11 after the owner-blood session cost is stable.
- Consecrated Structure remains a classification under IP-13. No dedicated consecration lifecycle is scheduled; future authored Rite/Structure combinations can use the label when the existing mechanics already connect them.
- More Rite verbs and failure profiles belong after IP-15 proves objective persistence and recovery.
- Puppet agency terminology belongs to IP-19 and should be settled before new puppeteer ordeals are authored.
- Creature names, final silhouettes, and authored assets belong to IP-20 through IP-25, after the exact ecological and progression role is locked.

These are deferred to prevent scope collapse, not abandoned. Each has a specific dependency and implementation home.
