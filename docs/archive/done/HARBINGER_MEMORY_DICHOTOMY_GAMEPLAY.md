# Harbinger Memory Dichotomy: Gameplay Implications

> **Status:** Superseded design history. Current mechanics and terminology are recorded in [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) and [MNEMONIC_POWER_OVERHAUL_IMPLEMENTATION_PLAN.md](MNEMONIC_POWER_OVERHAUL_IMPLEMENTATION_PLAN.md).
>
> **Authority boundary:** [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) remains the implementation authority. [CURRENT_MORPHLING_STATS_AND_ABILITIES.md](CURRENT_MORPHLING_STATS_AND_ABILITIES.md) remains the current Morphling mechanics reference.
>
> **Companion documents:** [HARBINGER_MEMORY_DICHOTOMY_LORE.md](HARBINGER_MEMORY_DICHOTOMY_LORE.md) covers the lore implications. [HARBINGER_MEMORY_DICHOTOMY_IMPLEMENTABLE_PIECES.md](HARBINGER_MEMORY_DICHOTOMY_IMPLEMENTABLE_PIECES.md) splits this design into implementation-ready powers, dialogue, assignments, paths, crafting work, rites, and creatures.

## 1. Desired Player Experience

Give early Hemomancy useful body-level powers before the player reaches the deeper manipulation systems, then let those early habits feed into enzymes, tendencies, Morphlings, Memory Weaving, puppetry, Blood Structures, Cardinal Rites, and the final Harbinger response.

The player should feel the progression physically:

1. Drink a tonic or tincture and watch one body section change when it is needed.
2. Spend blood and strain that section by relying on the change.
3. Learn which enzymes and tendencies make certain adaptations easier.
4. Use Morphlings to reinforce the bodily abilities they care about.
5. Move into deliberate Hematic Memories that project learned patterns outward.
6. Build persistent Blood Structures and perform event-driven Cardinal Rites.
7. Reach the end knowing the final choice was foreshadowed by how every earlier system treated body, memory, and connection.

This should guide play without forcing a class choice. A player can use Muscle Memories, manipulations, Morphlings, puppets, structures, and rites together.

## 2. The Soft Gameplay Axis

| Physical / incorporated emphasis | Willed / relational emphasis |
|---|---|
| Muscle Memories | Deliberate Hematic or Willed Memories |
| Tonics, tinctures, and enzyme acclimation | Somatic Loom and Mnemonic Reliquary |
| Morphlings | Puppets and Commandeered Wills |
| Blood Formations and Blood Structures | Event-driven Cardinal Rites |
| Alchemist and Artificer guidance | Mnemonist and Voyager guidance |
| Buffing the body | Imposing a pattern |

This table describes synergy, not ownership. Existing manipulations can remain hybrids, and NPCs can teach systems outside their strongest emphasis.

The axis describes the player's dominant verb, not the metaphysical purity of every system. Structures persist; rites unfold. Either can still contain both bodily and willed elements.

## 3. Muscle Memory System

### What It Is

A Muscle Memory is a learned, action-triggered bodily adaptation. Tinctures add stored reserve, the existing memory radial activates it, and reserve drains while active. Its useful behavior spends blood and strains the vascular section doing the work.

It is not just another potion effect. The player should be able to tell that their body changed because a remembered action was required.

The document uses **tincture** as the working mechanical term. The final content may use tonic and tincture as separate strengths or choose one name consistently.

### Core Flow

1. Prepare, acquire, or discover an early tincture.
2. Drink a tincture dose to permanently teach its Muscle Memory and add stored reserve.
3. Equip it automatically into a free early slot or arrange it later with the Mnemonic Reliquary.
4. Select and activate it through the existing memory radial.
5. Its reserve drains while active and the relevant body section gains the black-red, Greed-like hematic skin treatment.
6. Perform its trigger, such as attacking, mining, sprinting, entering darkness, taking damage, or becoming hungry.
7. The triggered benefit spends a small amount of stored blood.
8. Successful use adds a small amount of aligned blood tendency.
9. Repeated use strains the associated vascular section.
10. Deactivate it to preserve reserve, or drink later doses to refill that reserve.

### Triggered Instead of Constant

The preparation reserve drains while active, but blood and vascular strain are paid only when the useful effect triggers.

Examples:

- Strengthened hands spend blood when an attack connects.
- Mining arms spend blood on eligible block breaks or timed mining pulses.
- Running legs spend blood at a controlled distance or cadence, not every tick.
- Jumping legs spend blood when they actually increase a jump.
- Altered eyes spend blood when entering qualifying darkness or acquiring a revealed target.
- A reinforced heart spends blood when it prevents or recovers damage.

This keeps the system readable and prevents a player from losing an entire reserve while standing still.

### Primed Loadout

Learning, reserve, equipping, and activation are separate states. Knowledge is permanent; the tincture remains relevant because each dose adds usable duration to that known instruction.

- Only one Muscle Memory can be active for each vascular section at a time.
- Priming a different memory for the same section replaces the current one instead of stacking both.
- A player can know every Muscle Memory and change preparations freely outside the immediate cost of brewing and drinking.
- Morphling resonance should strongly favor only two or three prepared memories at once, even when more sections are primed.

This creates a loadout without creating a class. The player chooses what their body is ready to remember now, not what it is permanently allowed to learn.

## 4. Initial Memory Families

Start with eight strong families, one primarily aligned to each blood tendency. Do not create one separate tincture for every vanilla attribute beyond this eightfold foundation.

| Family | Primary tendency | Section | Possible behavior | Main risk |
|---|---|---|---|---|
| **Sanguine Fists** | Fervent | Arms | Attack damage and forceful hand hardening | Invalidating weapons or stacking too strongly with Blood Rush |
| **Laboring Arms** | Ferric | Arms | Mining speed and controlled harvest support | Bypassing tool-tier progression |
| **Coursing Legs** | Neurotic | Legs | Speed, jump height, and fall control | Making ordinary travel and mobility gear irrelevant |
| **Hushed Gait** | Umbral | Legs | Suppress eligible footsteps and body-made vibrations while moving through darkness | Trivializing Sculk, stealth, or the need to control movement |
| **Predatory Eyes** | Incandescent | Head | Night vision, motion detection, and target highlighting | Duplicating Witch's Ear and Lux manipulations |
| **Second Pulse** | Vivacious | Heart | Emergency recovery and blood circulation | Passive healing and blood-income loops |
| **Enduring Viscera** | Frigid | Body | Hunger efficiency through controlled metabolism | Trivializing survival and food systems |
| **Carrion Metabolism** | Ruinous | Body | Safely extract nutrition from specifically tagged dangerous foods | Duplicating poison immunity or making all harmful consumption safe |

The exact names are still open. The categories and protected balance boundaries matter more than naming them immediately.

Mining tier should be handled with particular care. A temporary tier increase can bypass material progression and create confusing drops. Safer options include mining speed, reduced durability cost, or a narrow whitelist of Hemomancy materials that respond to reinforced arms.

Maximum hunger is also not a simple attribute change. It would require custom food-capacity and HUD handling, so hunger efficiency is the safer first version of Enduring Viscera.

Hushed Gait and Carrion Metabolism complete the eight-tendency set without becoming broad stealth or immunity powers. Hushed Gait suppresses only the bearer's eligible movement signals under controlled conditions. Carrion Metabolism changes the result of eating an authored carrion food; it does not cleanse existing effects or protect against poison from creatures, potions, clouds, or the environment.

## 5. Tinctures and Enzyme Acclimation

### Role of Tinctures

Tinctures are the early discovery and activation surface. The first mastered preparation teaches a bodily instruction before the player can deliberately weave a full Hematic Memory. Later doses prime that known instruction rather than forcing the player to rediscover it.

They use a three-part Ghastly Alembic recipe:

1. **Blood carrier:** one Bloody Flask in a new dedicated recipe slot.
2. **Main ingredient:** the bodily substrate in the existing main-input slot. The first simple set can use Sanguine Formation; advanced preparations can use specific organs, animal materials, or adapted tissues.
3. **Catalyst:** one aligned enzyme in the catalyst slot.

All three are consumed when a tincture finishes. The resulting tincture carries the Bloody Flask forward as its container and returns one Cured Clay Flask after drinking.

The new blood-carrier slot is not the Alembic's existing flask-transfer slot. That slot must remain responsible for moving Bloody Flasks into the internal reservoir and filling empty flasks from it. Keeping the recipe input separate prevents a tincture's Bloody Flask from being drained before recipe matching occurs.

They should:

- Be available early enough to matter during Degrees 1 and 2.
- Use recognizable organic materials and at least one tendency-linked input.
- Prime a Muscle Memory rather than apply a generic vanilla stat bundle.
- Record the first successful mastery permanently.
- Have enough duration for the player to use the behavior intentionally.
- Show the associated body section and blood cost in the tooltip.
- Clearly distinguish **unknown**, **known**, **primed**, and **currently triggered** states.
- Leave room for improved preparations without creating dozens of nearly identical bottles.
- Require all three authored inputs before distillation can progress; no ingredient may be consumed on an invalid or blocked output.

### Role of Enzymes

Enzymes determine affinity and efficiency. Early acclimation should matter before the player begins serious Memory Weaving.

An aligned enzyme can affect:

- Trigger blood cost.
- Vascular strain.
- Effect duration.
- Trigger cadence.
- Magnitude within a hard cap.
- Tendency gained through successful use.

This gives the Degree-2 separation and enzyme catalogue work a direct player-facing payoff. The player is not gathering colored reagents only because a later Loom recipe will need them.

### Tendency Growth

Muscle Memory use can add small tendency amounts, but it should not silently lock a player's build through ordinary movement.

Use should count only when the empowered behavior produces a real result. Sprinting in circles, jumping under a low ceiling, or swinging at air should not become the optimal tendency farm.

The gain should remain smaller than deliberate manipulation use so that early habits guide affinity without deciding it permanently.

## 6. Vascular Cost and Failure Pressure

The existing vascular system already associates manipulations with body sections. Muscle Memories should use the same section authority rather than inventing another injury meter.

That authority is currently broad: `ARMS` covers both arms and `LEGS` covers both legs. A visual can favor the active limb, but the mechanical strain still belongs to the shared section unless the vascular capability is deliberately expanded. This proposal does not require that expansion.

### Intended Pressure

- Arms that repeatedly harden for combat or mining become strained.
- Legs pushed past ordinary movement begin to clot or fail.
- Eyes held in an altered state become unreliable.
- A heart forced to maintain greater output becomes dangerous to overuse.
- Viscera stretched for survival begin to punish continued neglect.

### Avoiding a Failure Spiral

The thematic consequence is strong, but the failure state can become miserable if the same body section first grants a buff and then applies a severe opposite debuff.

Protect the loop with:

- Low per-trigger strain.
- Clear threshold warnings.
- Visible section health before severe penalties begin.
- A reserve floor that can prevent optional automatic triggers.
- Existing salves, poultices, rites, rest, food, and Morphling repair routes.
- A short grace window when a section crosses into a dangerous state.

The player should knowingly overwork a body part. They should not discover the system only after their legs stop working.

## 7. Greed-Like Body Overlay

Reuse the abandoned black-red body treatment as direct feedback for Muscle Memory activation and early Blood Absorption or Projection where it fits.

### Required Visual Behavior

- Only the relevant body section changes for a localized Muscle Memory.
- The overlay appears when the memory actually triggers, not simply because a tincture icon is active.
- The change can persist briefly between repeated triggers so it does not flicker every tick.
- Stronger activation can spread farther across the section without immediately becoming a full-body transformation.
- The appearance must remain readable with armor, Morphling attachments, first-person hands, and third-person multiplayer rendering.
- The treatment should remain Hemomancy's own red-black biological surface, not a direct recreation of another property's character design.

`HEART` and `BODY` are separate vascular sections but occupy the same player-model torso. Their treatments need distinct visual grammar, such as a concentrated chest-core change for `HEART` and a broader skin or rib treatment for `BODY`. Otherwise the overlay cannot tell the player which section is paying the cost.

The sections should also move differently instead of relying only on location:

- **Arms:** tighten, plate over, or send a short contraction toward the hand when impact or labor triggers the memory.
- **Legs:** form tendon bands or running lines that pull with the stride.
- **Head:** spread around the eyes or temples in response to darkness, sound, or target acquisition.
- **Heart:** pulse outward from a concentrated chest core.
- **Body:** form ribs, membranes, or breathing expansions across the wider torso.

This overlay is functional information. It tells the player which part of the body is spending blood and taking strain.

## 8. Morphling Resonance

The current eight canonical Morphlings already provide healing, blood restoration, perception, knockback resistance, vascular repair, mining speed, movement speed, attack speed, and poison immunity. Muscle Memories cannot simply duplicate all of those bonuses as another independent layer.

### Recommended Relationship

Morphlings amplify or stabilize Muscle Memories rather than adding a second uncontrolled copy of the same stat.

Treat each Morphling's existing preferred and secondary tendency relationships as two resonance strengths:

- **Primary resonance weight:** approximately 1.5x.
- **Secondary resonance weight:** approximately 1.25x.

This interprets the proposed two Morphling associations through the two affinities each current strain already has. It does not require two new item variants for every Morphling.

These values should weight resonance efficiency, not multiply raw attack, movement, mining, healing, or maximum-health values. Resonance can instead affect:

- Reduced trigger blood cost.
- Reduced vascular strain.
- Longer active duration.
- Improved cadence.
- A capped increase to the actual benefit.

This lets a player reinforce their two most-used Muscle Memories or bring weaker secondary memories closer to the same level.

### Behavioral Resonance

The strongest pairings should sometimes change how a Muscle Memory behaves instead of only improving a number. Possible examples include:

- **Irontooth + Laboring Arms:** recognize a narrow set of resistant Hemomancy materials rather than granting a universal tool-tier increase.
- **Witch's Ear + Predatory Eyes:** briefly interpret nearby sound through an obstruction instead of only extending sight range.
- **Bootlace + a strained section:** smooth a dangerous strain spike into a smaller recoverable burden over a short period rather than deleting the cost.
- **Emberfang + Coursing Legs:** preserve enough momentum for the next empowered strike instead of separately stacking full movement and attack bonuses.

These interactions are more expensive to author and test than efficiency modifiers. Use them for defining primary resonances, then let ordinary secondary resonance rely on cost, strain, cadence, or duration.

### Existing Passive Overlap

Each Morphling needs an overlap audit before assigning resonance:

- **Deadman's Purse:** blood recovery can create cost-refund loops.
- **Gravecap:** healing can erase heart or body strain too cheaply.
- **Witch's Ear:** perception already overlaps sight memories.
- **Lumenlace:** resistance can overlap body hardening.
- **Bootlace:** vascular repair directly counters Muscle Memory strain.
- **Irontooth:** mining speed directly overlaps laboring arms.
- **Emberfang:** movement and attack speed overlap legs and fists.
- **Winter Shroud:** movement and poison immunity overlap legs and viscera.

The resonance design should deepen these identities while respecting global attribute and blood-income caps.

## 9. Morphlings and Puppetry as Competitive Systems

Morphlings concentrate living power inward. Puppets distribute the player's will outward. The intended design makes them compete over how much living extension one Harbinger can sustain, but current resource behavior does not yet create direct blood competition.

Morphling upkeep currently draws blood. Puppet bodies currently draw charge from their owner-attuned Crossbar, and Puppeteering Thread supplies that charge. The desired "puppets take blood away from Morphlings" relationship therefore needs an explicit bridge rather than being assumed to exist.

### Proposed Shared Pressure

- Keep Crossbar charge as the immediate puppet resource so the current tether and starvation rules remain legible.
- Recommended bridge: charge one small owner-blood upkeep per active Crossbar tether session, not per puppet. Crossbar charge should continue paying each body's existing upkeep.
- Make puppet use reduce the blood available for Morphling upkeep and Muscle Memory triggers only through that visible rule.
- Let heavy Muscle Memory or Morphling use make puppet preparation harder in return.
- Allow bloodline support to soften the shared pressure without removing it.

This keeps the existing thread-and-charge economy intact while making an active puppeteer maintain the connection with their own living blood. Charging once per tether session avoids multiplying both charge and blood upkeep across the same cohort. Do not also charge blood when feeding Puppeteering Thread into the Spindle, restoring charge, and calling a body; those are alternative pressure points, not a stack of taxes.

### Tether-Gnawing

Morphlings should be able to interact with puppet-host connections, but not through uncontrolled friendly sabotage.

Good uses include:

- A directed ability that damages hostile puppet tethers.
- A defense against an enemy Commandeered Will.
- A PvP or encounter counter to hostile puppeteers.
- A deliberate reclaim or dismissal action.
- A specific Morphling strain whose active ability consumes connection strength.

When a Morphling deliberately severs a hostile connection, it can absorb a bounded part of that connection as feeding, bond progress, or temporary Muscle Memory stability. The reward needs a hard cap so players cannot create disposable tethers and feed them back into the Morphling indefinitely.

The player's equipped Morphling should not passively unravel their own bodies just because both systems are active.

## 10. Blood Formations and Blood Structures

### Gameplay Identity

A Blood Structure is persistent construction. The player builds the correct arrangement, supplies its physical requirements, and uses Blood Projection to make the result exist.

Use a clear construction vocabulary where the content supports it:

Here, Sanguine Formation remains the prepared activation medium where authored. Blood Formation describes the temporary projected scaffold or arrangement it produces; it is not a second replacement item.

1. **Blood Formation:** a temporary projected shape or raw hematic scaffold.
2. **Blood Structure:** a stable result created from authored geometry, materials, offerings, and projection.
3. **Consecrated Structure:** a descriptive category for a persistent structure that is manifested by a Cardinal Rite or materially participates through the Rite's existing optional structure requirement. The Consecrated Bloodwell is the clearest current example.

This is not a new three-stage crafting lifecycle. Ordinary utility structures remain direct and repeatable. Consecrated Structure is simply the name for authored hybrid Rite/Structure content where construction and ceremony are already connected. It does not require a new recipe type, block state, Rite profile, or consecration mechanic.

Blood Structure definitions should continue to state:

- Blood cost.
- Required degree.
- Held activation item, including Sanguine Formation where authored.
- Any separate catalyst requirement.
- Target block.
- Authored 3D geometry.
- Offerings.
- Result.
- Progression gate.

### Intended Role

Blood Structures should be:

- Common enough to support ordinary progression and infrastructure.
- Repeatable after the player understands their plan.
- Focused on stations, equipment, routes, architecture, storage, and persistent utility.
- Allowed to grow more complex through Basic, Advanced, and Grand tiers.
- Closely tied to Blood Projection, Sanguine Formation, and later Blood Conduit flow.

They should not be treated as the lesser or boring half of the dichotomy. They create the organs through which the rest of Hemomancy operates.

## 11. Event-Driven Cardinal Rites

### Gameplay Identity

A Cardinal Rite creates a temporary authored ritual body. Building the floor and supplying the medium begins the work, but the player must participate in what the rite remembers doing. The event ends; its result may permanently change rank, knowledge, ownership, a relationship, or a structure.

Each major rite should have one defining event verb:

- **Pursue:** intercept or follow a moving manifestation.
- **Defend:** keep a medium, helper, or temporary organ alive.
- **Respond:** answer changing sigils or pulses.
- **Endure:** survive escalating pressure without abandoning the rite body.
- **Bind:** maintain several active connections.
- **Sever:** break a connection with Blood Absorption at the correct moment.
- **Witness:** identify or observe a revealed pattern without disrupting it.
- **Sacrifice:** choose what the rite consumes when a cost becomes due.
- **Overcome:** defeat the manifested entity or will.

### Reusable Rite Grammar

These verbs should be reusable event modules, not nine completely separate rite engines. A Rite definition should select one defining verb and only the supporting verbs it actually needs, then provide authored targets, timing, failure conditions, and consequences.

The shared Rite lifecycle still needs to own:

- Activation and participant capture.
- Event state and synchronization.
- Death, logout, disconnect, and dimension-change behavior.
- Cancellation and cleanup.
- Failure, partial completion, and refund rules.
- Completion and any persistent change left behind.

This keeps each ceremony authored without rebuilding persistence and recovery for every new Rite.

### Avoiding Repeated Wave Arenas

Event-driven does not mean every rite spawns enemies in circles.

- Puppetry can manifest the learned body as the ordeal.
- Vascular work can ask the player to route or answer pulses through damaged sections.
- Chamber work can test navigation, memory, or safe return.
- Covenant rites can depend on maintained participant relationships.
- Severance rites can require controlled Blood Absorption rather than damage.
- A witness rite can fail if the player attacks what they were meant to understand.

The event must express the rite's meaning.

### Failure with Consequences

Major Rite failure does not always need to reset the floor as though nothing happened. A defining Rite can instead:

- Scar or temporarily disable part of its ritual structure.
- Corrupt one of its offerings.
- Leave behind a temporary hostile Blood Formation.
- Strain the participant's relevant vascular section.
- Preserve limited progress while changing the next attempt.

Use these consequences sparingly and show them before recommitment. Routine rites should still fail cleanly, and no permanent punishment should make blind experimentation the wrong way to learn a new event.

### Complexity by Degree

Keep the existing progressive vocabulary:

- Early rites teach floors, media, and simple anchors.
- Midgame rites add the Living Staff, offerings, support sigils, and denser boundaries.
- Specialized puppet-learning ordeals can appear before Degree 5 through their authored Dominion Lesser progression.
- Degree-5 rites can introduce ordeals and bloodline help as part of the wider general rite vocabulary.
- Grand rites can combine stations, domes, multiple rings, helpers, and long ceremony profiles.

Degree remains a ceiling. A late utility rite may remain simple when complexity would add nothing.

### Frequency

- Blood Structures should handle routine repeated creation.
- Cardinal Rites should handle advancement, consecration, transformation, binding, major utility, and learned ordeals.
- Combined works should be reserved for major milestones where the player first constructs a body and then performs the event that gives it identity.

Do not make players repeat a long ceremony every time they need an ordinary material.

### Early Hybrid Proof

Before either side resembles a separate path, one early encounter should require a bodily action to complete a willed result. For example, reinforced arms could hold a Rite's temporary heart in place while the player imposes a binding instruction on it.

The exact encounter can change, but its job should remain fixed:

- The Muscle Memory performs a physical verb such as endure, pursue, climb, restrain, or hold.
- Completing that verb feeds a Willed Memory, Structure, or Rite consequence.
- The player sees that bodily adaptation and imposed meaning are stages of one practice.

This should appear before Morphling and puppetry specialization becomes strong enough to look like a class split.

## 12. NPC Guidance and Soft Specialization

### Alchemist and Artificer

Their assignments can preferentially reward:

- Tincture recipes.
- Enzyme efficiency.
- Muscle Memory discovery.
- Morphling resonance.
- Sanguine Formation economy.
- Blood Structure plans.
- Living gear and bodily grafts.

### Mnemonist and Voyager

Their assignments can preferentially reward:

- Crude and refined Hematic Memories.
- Manipulation slot control.
- Mnemonic Reliquary use.
- Puppetry materials and ordeals.
- Mnemonic Ambergris and strange memory sources.
- Chamber of Will interpretation.
- Conceptual or perception-driven fauna discoveries.

### Vicar and Cicatrix Anchorite

The Vicar keeps degree progression, doctrine, and shared ritual grammar from becoming career-locked.

The Anchorite provides deliberate crossover rewards because scars use physical injury to change mental and manipulation behavior.

Optional vocation work should improve access or efficiency. It should not become a hidden prerequisite for degree progression or either final response.

## 13. Fauna Additions

Do not add six biome-filler mobs at once. Add creatures that each teach a distinct verb, provide a needed material or memory source, and reinforce one side of the axis.

### Priority 1: Cold Land/Water Animal

Required niche:

- Moves between land and water.
- Expresses insulation, circulation control, breath, or cold endurance.
- Provides a route into a cold, body, or survival-focused Muscle Memory.
- Has a nonlethal observation, sampling, or harvesting interaction where possible.

### Priority 2: Nether Wall-Sucker or Hanging Mouth

Required niche:

- Lives on walls or ceilings rather than behaving like another ground melee mob.
- Uses adhesion, tendon tension, ambush, pulling, or hanging movement.
- Supports a Fervent, mobility, grip, or body-hardening adaptation.
- Has a strong silhouette visible before it attacks.

This is the strongest physical monster concept because its body immediately explains what the player might learn from it.

### Priority 3: Sculk or End Conceptual Creature

Required niche:

- Interacts with sound, gaze, targeting, repeated actions, remembered position, or severed intent.
- Teaches its concept through behavior before the player reads a tooltip.
- Supports Willed Memory, puppetry, Mnemonist, or Voyager progression.
- Is more than a source of another elemental reagent.

The Mnemonic Whale and Mnemonic Ambergris already cover passive sampling from a strange memory-bearing animal. This creature should add an active perceptual or conceptual encounter rather than duplicate that acquisition loop.

### Later Flying Animals

- **Desert vulture:** carrion detection, thermal movement, hunger efficiency.
- **Jungle/lush-cave bat:** echolocation and perceptual crossover, with care not to duplicate Witch's Ear.
- **Carrion bees:** swarm distribution and collective appetite, with care not to make Hivemind symbolism too literal or duplicate vanilla bees.

Each should wait until it owns a behavior that the priority creatures do not already provide.

## 14. Progression Placement

| Degree | Proposed addition | Existing systems reinforced |
|---|---|---|
| **1** | First tinctures permanently teach and temporarily prime simple Muscle Memories | Blood Absorption, Blood Projection, Sanguine Formation, vascular system |
| **2** | Enzyme acclimation changes efficiency; wild fauna and Morphlings preview adaptation; one small hybrid encounter joins bodily action to a willed result | Vial Centrifuge, Ghastly Alembic, tendencies, Living Bestiary |
| **3** | Deliberate transition into full Hematic Memory weaving | Somatic Loom, Blank Hematic Memory, Mnemonist, early puppet ordeals |
| **4** | Scars deliberately bridge bodily and willed practices | Cicatrix Anchorite, Cerebral Scarring, Brotherhood systems |
| **5** | Cultivated Morphling resonance, stronger puppetry, Grand Blood Structures, and broader ordeal profiles | Incubator, Crossbar, Blood Conduits, Founding Fane |
| **6** | Outward covenant and inward Chamber become parallel spatial systems | Bloodlines, Founding Fane, Chamber of Will |
| **7/8** | Systems converge on incorporation or sealed refusal | Qliphoth, Vesper, Mycophant, Apotheosis, Silent Archon |

This placement is guidance. It should be checked against the current chapter load because Degrees 2, 3, and especially 5 already carry substantial content.

## 15. Balance and Usability Risks

### Duplicate Buff Sources

Muscle Memories overlap vanilla potions, armor, tools, scars, manipulations, Morphlings, Fanes, and Blood Moon bonuses. Every proposed stat needs a stacking audit and a hard global cap.

### Hidden Blood Consumption

Automatic triggers can drain blood without an intentional cast. Show recent trigger cost, respect a reserve floor, and let the player disable or remove the tincture state.

### Vascular Failure Spirals

Localized strain is the point, but recovery must remain accessible. Test repeated mining, travel, combat, and boss use rather than only isolated triggers.

### Morphling Feedback Loops

Blood-restoring and vascular-repair Morphlings can erase the Muscle Memory costs that are supposed to balance them. Cost and recovery must run through the existing global guardrails.

### Rite Authoring Cost

Every unique event needs persistence, cancellation, disconnect, death, dimension-change, multiplayer, failure, and refund rules. Start with a few defining rites rather than rewriting every rite at once.

### Content Overload

Early Hemomancy already teaches blood storage, projection, assignments, machines, tendencies, and manipulations. Tinctures must simplify that transition, not add another full-screen progression system. Known and primed states should reuse an existing inventory, capability, or progression surface where practical.

### Consumable and Loadout Fatigue

If every useful action needs a different short-duration bottle, the system becomes inventory work instead of bodily adaptation. Keep durations long enough for a deliberate activity, preserve learned memories permanently, replace conflicting memories cleanly by vascular section, and avoid balancing the system around drinking every available tincture before combat.

## 16. Recommended Delivery Order

1. Define the Muscle Memory contract, permanent knowledge state, primed state, and coexistence with current manipulation strain.
2. Implement one memory for each vascular section using temporary test effects and one-memory-per-section replacement.
3. Add blood cost, reserve-floor behavior, and vascular strain.
4. Add the localized body overlay, section-specific motion, and multiplayer-visible trigger state.
5. Add early tincture recipes, permanent first mastery, and enzyme efficiency.
6. Build one early hybrid encounter that turns a Muscle Memory action into a Willed Memory or Rite result.
7. Audit all eight current Morphlings before adding resonance.
8. Add capped primary and secondary resonance as efficiency weights.
9. Add only a few defining behavioral resonances after the numerical loop is stable.
10. Catalogue current Blood Formations, Blood Structures, consecrated Structures, and Cardinal Rites by gameplay purpose without changing behavior yet.
11. Convert a small number of suitable Cardinal Rites to the shared event-verb grammar and test their failure consequences.
12. Add fauna only after its memory, reagent, encounter, or progression use is already defined.

This order proves the core bodily loop before expanding it into every adjacent system.

## 17. Playtest Questions

- Does a new player understand why the body overlay appeared?
- Can the player identify which action spent blood?
- Does a tincture remain useful long enough to justify carrying it?
- Can the player distinguish an unknown memory from a known, primed, or actively triggered one?
- Does one-memory-per-section replacement create a useful loadout without becoming preparation busywork?
- Can a player accidentally empty their blood reserve through normal travel?
- Does vascular strain create a meaningful limit or only forced downtime?
- Do early enzyme choices feel useful without locking later tendencies?
- Can the player tell which bonus came from a Muscle Memory and which came from a Morphling?
- Does Morphling resonance encourage a build without making non-resonant memories worthless?
- Does a behavioral resonance feel like a relationship between systems rather than another hidden stat multiplier?
- Do puppets and Morphlings compete for blood in an interesting way?
- Does a Cardinal Rite's event teach what that rite means?
- Does the early hybrid encounter prove that bodily and willed practices work together?
- Does a failed major Rite leave a readable consequence without making experimentation feel punished?
- Are repeated Blood Structures convenient once learned?
- Do the final choices feel foreshadowed without feeling predetermined?

## 18. Protected Gameplay Boundaries

- Do not create two mutually exclusive early-game classes.
- Do not rename Hematic Memory into a mental-only category.
- Do not duplicate every Morphling passive as an uncapped Muscle Memory.
- Treat 1.5x and 1.25x as resonance weights, not raw stat multipliers.
- Keep learned Muscle Memories permanent even though tincture priming expires.
- Do not allow multiple Muscle Memories for the same vascular section to stack.
- Do not let automatic triggers drain below a clearly defined safe reserve without explicit player control.
- Do not use a separate injury system when the vascular sections already provide the correct authority.
- Do not let friendly Morphlings automatically destroy the player's own puppet tethers.
- Do not turn every Cardinal Rite into a wave arena.
- Do not give every Cardinal Rite a separate lifecycle implementation when shared event verbs and state rules can express it.
- Do not make permanent Rite punishment the default failure response.
- Do not make long Cardinal Rites the routine method for ordinary crafting.
- Do not make optional NPC vocations hidden degree requirements.
- Do not make a physical-leaning build automatically choose Apotheosis.
- Do not make a willed-leaning build automatically choose Silent Archon.
- Keep Silent Archon at Degree 7 and Apotheosis as the Degree-8 path.
- Treat all values, names, recipes, mobs, and delivery stages in this document as proposed until implemented and verified.
