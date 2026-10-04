# Player / Harbinger progression audit

Audit date: 2026-09-10. Minecraft 1.21.1, configured NeoForge 21.1.219. Base commit: `db11d6baee81d54f404cac53c65c637553ed4273`, with substantial existing local changes. Findings describe the working checkout, not that commit alone.

## Current disposition (2026-10-02)

The findings below are a historical 2026-09-10 snapshot, not the current backlog. H01 (explicit Loom selection), H02 (Alembic capacity), H03 (separate bottling output), H04 (syringe batch eligibility), H06 (explicit Monolith press-further), H07 (rack transfer clearing), and H11 (Apotheos rite contract) are implemented at their reported causes. H05 now has a personal shared-Fane member contribution route: a successful D5+ Blood Projection deposit at the bloodline's usable heart while inside its boundary. Membership and nearby observation grant no proof.

Scar Resonance now supplies its advertised bonus slots, both endings reach the ledger, the Liber maps Sanctified instead of the absent Adept rite, and distillation fixture recipes are isolated from release resources. See [the current repair evidence](validation/PRIORITY_FINDINGS_2026_10_02.md). Historical red tests and counts below must not be cited as current results; native connected-client recovery, natural Survival pacing and real multiplayer acceptance remain separate work.
## Historical assessment

The progression has a coherent identity: learn to spend your own blood, build a workshop that changes what blood can do, reshape your body, establish a covenant, then decide what ascension means. The chapter proofs are considerably stronger than eight ranks unlocked by accumulating materials. The machinery, NPC specializations, embodied powers and ritual staging support that identity.

The current implementation does not yet make that journey consistently dependable. The most urgent problems are recipe selection, material conservation, contradictory machine contracts, and ownership rules that conflict with cooperative progression. Those should be repaired before broad damage or resource nerfs. Otherwise balance measurements will describe players working around bugs.

Six new server regression probes reproduce broken or conflicting contracts. Two additional controls establish that ordinary reagent distillation and Alembic crystal growth actually work. The broader tests are not green. A client launched, but the available Windows capture API failed before a world could be entered. Consequently this is an implementation audit with server experiments and a source-derived waiting-time simulation, **not a completed live D0–D8 playthrough or a verified assessment of combat feel**.

Production behavior has not been changed by this audit. Deliverables are this report, the inventory script, and diagnostic GameTests.

## Evidence and reproduction

- [Inventory and simulation script](../tools/misc/progression_audit.py): `python tools/progression_audit.py`.
- [Eight server probes](../src/gameTest/java/com/vincenthuto/hemomancy/gametest/ProgressionAuditGameTests.java): `./gradlew.bat runGameTestServer --no-daemon`.
- JVM suite: `./gradlew.bat test --no-daemon`.
- Local evidence directory: `build/progression-audit/`. Logs are generated evidence and may be ignored by Git; the script and tests are the reproducible sources.

| Run | Result | Interpretation |
|---|---|---|
| JVM baseline | 1,899 tests; 58 failures; 1,841 passed | 18 failures involve missing source files, often old paths. The remaining 40 are not all established as stale. |
| Initial server baseline | 299 tests; 2 required failures | Formation pickup fixture and paralysis/recovery fixture failed. |
| Server run with first four probes | 303 tests; all 299 original required tests passed; four optional probes failed | Confirms the original failures vary across runs. It does not erase them. |
| Final server run | 307 tests; 3 required failures and 6 optional failures | Both new required controls passed. Six optional probes reproduced the audit findings. |
| Live client | Launched to title; capture failed twice, including after refreshing the window | No survival world, actual two-client session, visual guidance walkthrough or combat benchmark completed. |

Final required failures: `formationAcceptsAutoPickedUpOutput`, `paralysisExpiresAndRecoveryStartsAfterRemoval`, and `hostilePayloadsLeaveAlliedHealthStatusesAndMotionUntouched`. The first two exposed an occupied fixture position and a removed/dead fixture entity respectively; the third reported a missing `blood_aneurysm` hostile payload. These need independent diagnosis. Do not describe the suite or `alphaCheck` as passing.

The six optional tests deliberately assert the desired contract rather than approving the observed bug. They remain red until the underlying issue is resolved. Optional status keeps these newly documented failures separate from the existing required gate.

Inventory: 539 merged main/generated recipe IDs, including 94 memory weaves, 56 Cardinal Rites, 48 Armature upgrades, 44 distillations, 28 blood structures, 25 scar crafts, and eight each of incubation, fungal scar cultivation and enzyme fruiting. Source registration scans identify 54 block entities, 114 manipulations, 33 scars and 73 skill entries. These are syntactic inventory counts, not claims that every registration is a distinct, reachable Harbinger activity.

## Ranked findings and repair targets

Priority means impact on a normal journey: P0 blocks trustworthy selection of core progression output; P1 breaks an advertised contract or major progression relationship; P2 creates substantial confusion, exploitability or economic distortion.

| ID | Priority / evidence | Finding | Concrete repair and acceptance condition |
|---|---|---|---|
| H01 | P0, server + source | Blood Shot and Vital Effusion have identical Loom catalyst/enzyme requirements. Recipe iteration chooses a result the player cannot explicitly select. | Add an explicit affordable-recipe choice, or make recipes uniquely selectable. Demonstrate both outputs from the same stocked Loom without relying on recipe ordering. |
| H02 | P1, server | Alembic capacity is 2,000 blood; automatic flask bottling requires 2,500 and jug bottling 5,000. | Reconcile capacity and vessel requirements as one economy decision. Fill each supported vessel through normal input, with exact blood conservation. |
| H03 | P1, server | An empty bottling flask can replace a distillation reagent result with a Bloody Flask. | Separate reagent and bottling outputs. Devil's Tooth must still yield two Foul Paste with an empty flask present; bottling must pay its own blood cost. |
| H04 | P1, server | Consecrated Syringes are rejected at centrifuge startup, despite tooltip and completion logic advertising their processing. | Share sample validation between startup and completion. A balanced Saint pair must start, consume correctly and produce its intended residuum. |
| H05 | P1, source | Founding Fane certification is personal to the Progenitor; joining their bloodline does not satisfy a member's D6 chapter. | Give members a personal, witnessed contribution route to the chapter. Verify two players advance through one covenant without each being forced to establish a separate bloodline. |
| H06 | P1, server | A D7 player's second ordinary monolith interaction destroys it, independently of choosing to press further. | Bind destruction to the explicit consequential dialogue action. Ordinary teaching must remain repeatable; test two readers and the cornerstone handoff. |
| H07 | P2, server | Rack insertion places eight physical vials in the centrifuge while replacing the rack's eight samples with eight empty vials. | Transfer rather than duplicate containers. Eight input vials must remain eight across rack, machine, outputs and inventory. |
| H08 | P2, resource/source | Packaged `distillation/test` and `test_count` convert cheap blocks to Formations. | Decide whether these are authored survival recipes. Remove them from production resources if they are fixtures; otherwise rename, teach and price them intentionally. |
| H09 | P2, source/model | Free repeat sampling competes with a very expensive passive enzyme route. | Establish separate active and passive production targets before altering costs. Measure enzymes/minute, blood/enzyme and player actions per batch. |
| H10 | P2, source | NPC rank hints, chapter gates, machine details and recipe gates do not always describe the same next action. Seven duplicate English keys discard more informative ledger text. | Derive next-step hints from actual unmet chapter state, remove duplicate keys and verify every handoff in the ledger and NPC dialogue. |
| H11 | P2, source | Apotheos lists 7,000 blood, while the interactive rite uses anchor payments; the general recipe drain appears in the legacy execution branch. | Establish the intended total, display that same total, and assert exact blood spent through the interactive path. Do not rebalance from JSON alone. |
| H12 | P2, source/model | Nine random pome waits have a long tail, while notifications can open dialogue and the final communion restricts other rites. | Show growth state and the consequence before the last communion; use non-modal notifications. Test interrupted, offline and cooperative journeys. |

### Source anchors for the confirmed contracts

- Alembic capacity, burn/output handling and leak growth: [GhastlyAlembicBlockEntity](../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java), [AlembicVesselRules](../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/AlembicVesselRules.java).
- Sample acceptance, random yields and rack movement: [VialCentrifugeBlockEntity](../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java).
- First affordable matching recipe: [SomaticLoomBlockEntity](../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticLoomBlockEntity.java), especially `refreshRecipe`.
- Chapter gates and leader-only migration: [HarbingerChapterProgression](../src/main/java/com/vincenthuto/hemomancy/common/mission/shared/HarbingerChapterProgression.java).
- Fane certification and ritual completion: [HarbingerCardinalRiteEvents](../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java), especially `completeFoundingFane`.
- Monolith interaction counter: [SanguineMonolithBlock](../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineMonolithBlock.java).

## The actual journey, rank by rank

The table distinguishes a rank's proof from everything the player might do at that rank. Chapters are prerequisites for the *target* degree. Do not confuse the later optional Vein-Mason career lessons with the earlier chapter proof bearing the same name.

| Stage | Main checkpoint / loop | Peak | Pain and recommended adjustment |
|---|---|---|---|
| Before D1 | Find a Hermit, receive the initial direction, acquire the offering and perform initiation. | The first rite uses the player's body and a place in the world, establishing the mod's identity immediately. | Finding the Hermit is the first unmeasured risk. A fixture spawning one proves none of the search experience. Measure fresh-world discovery distance/time and make the first clue recoverable if the NPC is lost. |
| D1 → D2 | First Bloodcraft, then the Votary rite. Learn blood shaping and early supplies. | A concrete crafted proof gives purpose to the first workshop. | The Hermit's broad D1 advice does not foreground the mandatory First Bloodcraft handoff as clearly as the Vicar. Name the NPC, exact current objective and missing requirement in the same conversation. |
| D2 → D3 | Alchemist's First Separation. Sample creatures, understand balanced centrifuge slots, obtain the assigned output. | Sampling makes the environment a source of capabilities rather than generic ore. | Loose vial and syringe use different clicks; rack transfer duplicates containers; unrestricted repeated sampling makes creature variety largely an initial discovery exercise. Teach one balanced pair before introducing a full rack. |
| D3 → D4 | Mnemonist's Woven Vessel. Blank memory, enzymes and catalyst become a usable manipulation. | A deliberate first weave makes learning a power feel authored. Puppetry also starts offering a different play style. | The core recipe-selection ambiguity can undermine the chapter's lesson. Bulk enzyme deposits make it worse. Resolve selection before judging the weaving minigame. |
| D4 → D5 | Vein-Mason chapter proof through the first Effigy loadout, then Illuminatus. Expand bodily and equipment choices. | The player starts choosing a build rather than collecting a linear power list. | Several similarly named scar, strain, loadout and teaching concepts arrive close together. Explicitly separate crafting a scar, equipping a loadout and demonstrating a teaching. Show tradeoffs before committing ingredients. |
| D5 → D6 | Bloodline and Founding Fane: A Covenant Written in Place. Incubation and advanced equipment deepen the base. | A home becomes a persistent institution, with NPC relationships and shared resources. | This is the sharpest solo/co-op contract conflict. A leader succeeds while a new member can remain chapter-locked. Role-specific contribution objectives would preserve personal achievement without fragmenting the group. |
| D6 → D7 | Living Covenant: Chamber return, bound Covenant Throne, completed Covenant Vigil; then Archon rite. | The base, covenant and otherworldly experience converge into one proof. | The Vicar's generic rank hint underspecifies this sequence. The ledger already has relevant states; surface the earliest unmet one through the NPC. Expensive structures and helper acquisition need preparation guidance. |
| D7 → D8 or Silent Archon | Monolith, Qliphoth growth/communion, then Apotheos or refusal/pruning and the Vesper ordeal. | A real thematic fork is a stronger ending than another stat tier. | Ordinary conversation can prematurely destroy the teacher. Random waits and last-communion restrictions can obscure the decision. Silent Archon remains degree 7; it is an alternate resolution, not an unimplemented D8 increment. |
| After ascension | Primal morphlings, high-tier powers, covenant use and repeat play. | Bodily transformations and persistent institutions can make the ending tangible. | Power rewards need situations worth using them in. A finished unlock is not evidence of a satisfying postgame. Repeatable challenges and covenant goals are a content opportunity, not something demonstrated by these tests. |

The Hermit's initiation instructions already contain useful specifics: minimum health, the offering, remaining within the ring, and the distinction from later anchored rites. Preserve this precision. The overall issue is inconsistent handoff quality, not an absence of dialogue.

## Cardinal Rites: staging works, accounting needs alignment

The interactive anchor mechanic is a recognizable progression language. Early forgiving practice followed by longer rites, larger layouts and helpers can work well. The escalation should test preparation and execution rather than uncertainty about what the machine expects.

| Target rank | Required prior degree | Anchors | Anchor blood at 50 each | Authored duration |
|---|---:|---:|---:|---:|
| D1 | 0 | 0 | 0 | 20 seconds |
| D2 | 1 | 4 | 200 | 20 seconds |
| D3 | 2 | 4 | 200 | 60 seconds |
| D4 | 3 | 4 | 200 | 60 seconds |
| D5 | 4 | 4 | 200 | 180 seconds |
| D6 | 5 | 8 | 400 | 180 seconds |
| D7 | 6 | 12 | 600 | 180 seconds |
| D8 | 7 | 32 | 1,600 | 180 seconds |

These are authored times and anchor payments, not observed completion times or full material bills. The rank rites alone account for 68 anchors / 3,400 blood in that payment model. Helper requirements, construction, offerings, retries and other rites are additional. D7 requires a helper; Apotheos currently does not. Apotheos also has a separate failure-policy exception, so treating it as a simple extension of D7 would miss actual behavior.

The 12-to-32 anchor jump is a large increase in setup and spatial management without a longer authored duration. Keep the scale if it serves the climax, but provide a layout preview, clear missing-anchor feedback and a preflight summary. Check solo movement paths and interaction timing live before changing the count.

The JSON `bloodCost` is not a reliable summary of the interactive path. Apotheos advertises 7,000 while anchors imply 1,600; the general `getBloodCost()` drain occurs in the legacy branch. A dedicated interactive accounting test must settle the intended behavior. Earlier rites with zero JSON cost are not therefore free.

Failure policies become less forgiving later. Players need to know what can be lost before starting, and retries should not silently require rediscovering helpers or reconstructing unrelated state. An existing harness supplying a sworn helper demonstrates rite execution, not the natural recruitment journey.

## Blood, materials and crafting trees

### Early extraction and the centrifuge

Loose vials sample by attacking/interacting through the vial's left-click entity path; Living Syringes use right-click, with rack loading and ejection behavior. Neither normal sampling path charges source health or applies a source depletion/cooldown. This makes sampling approachable, but removes an ongoing reason to maintain a diverse collection after the desired tags are found.

The centrifuge pairs are `(2,6)`, `(3,7)`, `(4,8)`, `(9,5)` in its inventory indexing. Startup has useful distinct failure feedback for balance, validity and output fit. Keep that feedback and expose the corresponding physical pair clearly in the GUI.

A spin takes 200 ticks / 10 seconds and generates 250 blood per completed spin, not per vial. For a standard tagged creature with maximum health 10, each sample yields a uniformly selected 1–4 units of its relevant output: mean 2.5. Two such samples therefore average five units per spin, while eight average twenty. Those figures exclude sampling/loading time and assume a relevant entity tag. Standard samples also have the auxiliary iron-powder chance.

The free-repeat source plus rack duplication undermines both the biological sampling fantasy and container crafting. Fix container conservation first. Then decide whether active sampling should retain unlimited accessibility, trade source health, or use a short source recovery period. Any limitation should preserve a viable first pair and avoid making early progression dependent on rare mobs.

Startup and completion independently calculate random output. Near-full output slots can therefore be assessed against a different roll from the eventual result. This is a source-level consistency concern; it was not separately reproduced by a new probe. Store the accepted roll or validate a conservative maximum, and test blocked-output recovery without losing the sample.

Saint samples are a distinct confirmed problem: a Consecrated Syringe extends `Item`, while startup accepts `BloodVialItem`. The special completion branch cannot make an invalid startup succeed. This breaks the advertised Saint-reagent path; it does not prove that all late progression is blocked, because some high-tier materials have other sources.

### Ghastly Alembic, reagents and crystals

The Alembic has a strong dual purpose: process reagents while turning accumulated blood into a small physical growing field. Both ordinary distillation and growth passed server controls. Its bottling behavior is the broken part, and that break spreads into every downstream reagent recipe.

The Devil's Tooth control produced six Foul Paste and 300 blood over three recipe batches without a flask. Adding the empty flask instead produced a Bloody Flask in the reagent result slot. This is not merely an unclear tooltip. It changes the recipe product and can obstruct subsequent processing because that slot no longer matches the expected reagent.

The leak/growth rules are also more specific than the teaching suggests:

- Default leak interval: 200 ticks / 10 seconds; cost: 50 blood per successful event.
- It checks the surrounding 3×3 floor one block below, excluding the center. Supported floor materials include the accepted venous-stone variants and bone block.
- A crystal is placed at age 0, then grows to age 3. Four successful events cost 200 blood for one mature crystal.
- With one valid floor location, the controlled server test reached maturity after four payments, within 801 game ticks. This is approximately 40 simulated seconds, not a stopwatch live-client observation.
- With eight valid empty locations, placement takes priority over growth. The first mature crystal takes eleven successful events, approximately 110 seconds; all eight take 32 events / 320 seconds and 1,600 blood, assuming no interruptions or harvesting.
- Harvesting the first mature crystal repeatedly can redirect subsequent events toward that newly empty location. This is a scheduling consequence worth explaining or changing if even growth was intended.
- Leakage itself does not require active heating. Stored blood can continue supporting crystals after distillation stops.

The Alchemist's hint about venous stone and yellowed bone conveys atmosphere but not placement, block identity or age. Add a concrete follow-up: the eight surrounding floor positions, accepted blocks, visible maturation and the cost per growth event. Configuration commentary referring to dripstone does not match this implementation.

Normal bottling cannot reach its threshold because the reservoir caps at 2,000. Reagent crafting also eventually fills that reservoir unless the player withdraws blood or grows crystals. Teach reservoir saturation as a reason a valid reagent recipe can stop. Fix capacity and output routing together, then tune costs against the intended role of flasks and jugs.

Two packaged distillation recipes merit an explicit content decision: `test` turns cobblestone into one Formation; `test_count` turns venous stone into four. Each is a ten-second recipe with blood generation. If these were test fixtures, they bypass part of the intended Formation economy in survival. Their presence is confirmed; their intended authorship is not inferred from the filename alone.

### Loom and enzyme economy

The Loom holds eight enzyme reservoirs, up to 64 each. A blank memory and catalysts identify candidate recipes; stored quantities are tested as minimum requirements. The current first-match selection means richer storage can make the machine less controllable.

The exact collision is Blood Shot versus Vital Effusion: one Bleeding Bulb and one Animus enzyme, with different authored blood costs. Additional families share catalysts but use increasing enzyme requirements, such as Blood Binding / Lingering Binding / Chain Binding / Binding Lattice and several bulb-based projectile recipes. Higher stored totals satisfy lower recipes as well. This is a structural selection problem, not one mistaken resource file.

Add an explicit output choice among valid recipes and display affordability, degree/use restrictions, catalysts and remaining enzymes. Avoid selecting the highest-cost recipe automatically: that would still make stocking the machine silently change intent. The in-world weaving interaction can remain the act that completes the selected recipe.

The first weaving challenge has a useful five-second drift grace and requires active orb control. Repeating a deliberate interaction for 94 recipes may become laborious. First fix deterministic selection, then measure time and failure rate on first acquisition versus repeat crafting. An eventual convenience option should preserve the memorable first weave.

Mycelial Lantern enzyme fruiting takes 2,400 ticks / two minutes and 600 blood per enzyme at the inspected rate, following a 2,500-blood structure investment. That is weak against free repeated sampling averaging five enzymes from a pair in ten machine-seconds. Passive convenience has value, but the resource and throughput disparity needs an explicit target. Improving passive fruiting may be more appropriate than merely restricting sampling.

### Material graph and costs

The main dependency graph makes thematic sense: basic iron and blood shaping support sampling and processing; samples and reagents support memory weaving; workshop outputs feed bodily and equipment specialization; institutions require larger investments. Problems occur where output contracts or rank teaching interrupt that graph.

A selected structure spine totals roughly 9,025 blood: Liber 100, Hematic Iron Block 50, staff 150, centrifuge 150, Alembic 100, Loom 150, Reliquary 75, Scar Station 100, Effigy 150 and Covenant Throne 8,000. This is **not** the full playthrough price. It excludes direct ingredients, optional structures, rites, replacement items and ordinary power use. It shows how strongly the Throne changes the scale of investment.

Other large authored structure costs include Mycelial Lantern 2,500, standalone Consecrated Bloodwell 5,000 and Sanguine Vigil 6,500. Founding Fane completion manifests its own heart Bloodwell; do not incorrectly add a separately crafted 5,000-blood well as a universal founding prerequisite. Sanguine Quintessence also has a D3 Exsanguination route; its presence in founding materials is not proof of a D8 circular dependency.

Base maximum blood is 5,000, but structures can use staged interactions and capacity has upgrades. A cost above base capacity is not by itself an impossible recipe. What matters is whether the actual interaction permits staged payment and communicates progress.

Base passive recovery is approximately one blood per second. Damage/kill income and other effects change that. Multiple passive systems share circulation limits, while some skill, consumable and manipulation income follows different rules. Do not add every advertised regeneration number into a theoretical unlimited total. Test a complete equipped build, including armor, scars, morphling, cradle and covenant, when judging sustain.

## Scars, Cicatrix Anchorite and bodily progression

The system has real structure: 25 scar crafts, eight fungal cultivation recipes, scar patterns/loadouts, and an Anchorite curriculum connecting strain, diagnosis, treatment and demonstrated use. It is not an empty collection of item registrations.

The stat tradeoffs are meaningful. The heart-oriented sequence trades capacity and later movement for health: +2 health/−100 capacity; +4 health/−200 capacity with movement loss; +6 health/−400 capacity with a larger movement loss. The fire-oriented sequence trades armor for attack, with additional upper-tier effects and upkeep. These are better foundations than every higher tier being an unconditional replacement.

The player-facing problem is vocabulary and sequencing. Cerebral scars, fungal scars, afflictions from strain, patterns, loadouts and Noetic routing are related but not interchangeable. The teaching should answer, in order: what happened to my body; how to treat it; which scar changes the tradeoff; how to equip it; and how to verify that the lesson counted.

Noetic routing uses the best matching cerebral tier for 5/10/15% strain reduction rather than adding every matching scar. State this explicitly in the preview. Otherwise a player can invest in multiple matching scars expecting a benefit the system intentionally does not provide.

The later Vein-Mason assignment observes the transition into the relevant strain condition. A player who already has that condition before qualifying may not satisfy a transition-based objective by remaining injured. This needs a targeted recovery/re-entry test and a clear acceptance rule; it is not one of the six newly reproduced failures. Prefer recognizing the current condition where the lesson is diagnosis, and demand a fresh demonstration only where the lesson actually concerns causing it.

The D6 teaching sequence combines counsel, a matching cast, a different active scar set and another matching cast. This can teach adaptation, but requires immediate feedback about which cast qualifies and which loadout change counted. Rejecting a mechanically excluded cast without explaining the category turns a lesson into trial and error.

Recommendation: retain the cost-bearing bodily choices and authored teaching. Add a before/after stat and strain preview, explicit lesson progress, and one clear recovery path. Validate scar swaps, death, reload and old-save migration before declaring the full career complete. Source: [VeinMasonAssignments](../src/main/java/com/vincenthuto/hemomancy/common/mission/cicatrix_anchorite/VeinMasonAssignments.java), [Anchorite dialogue](../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerCicatrixAnchoriteDialogueTrees.java), [ScarInit](../src/main/java/com/vincenthuto/hemomancy/common/init/ScarInit.java).

## Morphlings versus puppeteering

These should be compared as different ways to inhabit combat. Morphlings change the player's body, movement and survival decisions; puppets add external actors whose usefulness depends on commands, reach, terrain and AI. Raw damage is insufficient to decide which is stronger or more fun.

### Current morphling roster

Eight strains are currently registered for this acquisition path. Older Pests, Tick, Urchin and Chitinite classes should not be counted as four additional current choices merely because source files remain.

| Strain | Acquisition distinction | Gameplay promise | Balance / enjoyment concern |
|---|---|---|---|
| Deadman's Purse | Swollen Leech, rotten flesh | Banking blood, emergency transfusion and later covenant support | Strong universal utility may displace narrower strains unless storage and rescue are legible. |
| Gravecap | Infected material, puffball, rotten flesh | Regeneration, spores, ally support and healing zones | Broad healing/utility is attractive in both solo and groups; compare actual prevented damage and healing, not tooltip counts. |
| Witch's Ear | Phantom membrane, echo shard, rotten flesh | Revelation, gliding and darkness-dependent effects | Echo-shard acquisition is a major exploration gate compared with cheap alternatives. Reward must justify the journey. |
| Lumenlace | Ink and glow ink, rotten flesh | Camouflage, blindness and emergency survival | A ten-minute reprieve cooldown makes the exceptional moment hard to learn by experimentation. Show readiness clearly. |
| Bootlace | String, spider eye, rotten flesh | Climbing, fall protection, webs and grapple/root utility | Cheap, broadly useful mobility is a likely favorite; terrain and input reliability will decide whether it feels excellent or frustrating. |
| Irontooth | Raw iron, bone meal, rotten flesh | Underground sensing, defensive bulwark and shockwave | Underground value depends on the actual Y restriction and discoverable feedback. Avoid opaque inactivity above its range. |
| Emberfang | Serpent scale, rotten flesh | Venom, a timed hit sequence, ambush and hothead tradeoffs | Likely the most deliberate offensive loop. Test hit recognition, cooldown cues and risk rather than reducing it to passive DPS. |
| Winter Shroud | Iron nugget, poisonous potato, rotten flesh | Cold protection, freezing and later molting/cryptobiosis | Niche usefulness and an awkward crop drop can make acquisition feel arbitrary. It needs visible wins outside a narrow cold encounter. |

Ordinary fed upkeep is 0.5 blood every 60 ticks: ten blood/minute, before skill changes. The current registered strains use the same ordinary upkeep rather than a tiered per-strain base charge. With puppeteering interference it becomes 12.5/minute. Base passive blood recovery can cover that in isolation; starvation and complete builds need separate accounting.

Bond thresholds of 50/100/200 blood correspond to roughly 5/10/20 minutes at ordinary ten-per-minute upkeep if continuously credited. Reducing upkeep can also slow bond progress because credit follows blood actually spent. If an efficiency skill unintentionally makes nurturing feel slower, separate bond time from resource consumption or explain the tradeoff. Wild-bound maturity is capped by `WildMorphlingRules` until the appropriate exception; show that cap before the player invests further feeding.

Incubation and feeding provide a growth arc; Primalization is a D8 reward with qualitatively distinct effects. Those are peak opportunities. The risk is a large amount of maturation followed by too little meaningful content after the most interesting form unlocks.

### Puppet roster and resource comparison

Values below are current base definitions, before skills, buffs, interference and AI constraints. Damage is a per-attack parameter, **not measured DPS**.

| Puppet | Degree gate | HP | Damage | Speed | Summon thread | Upkeep thread/min | Full 256-thread reserve after summoning |
|---|---:|---:|---:|---:|---:|---:|---:|
| Veinwing | 3 | 14 | 4 | .36 | 28 | 18 | 12.7 min |
| Marrow Spitter | 3 | 22 | 5 | .24 | 38 | 12 | 18.2 min |
| Gorebound Hulk | 4 | 55 | 9 | .18 | 56 | 8 | 25.0 min |
| Scarlet Mummer | 4 | 24 | 3 | .30 | 48 | 14 | 14.9 min |
| Sanguine Hound | 4 | 30 | 6 | .36 | 44 | 15 | 14.1 min |
| Ringmaster Pattern | 4 plus succession unlock | 28 | 1 | .28 | 52 | 14 | 14.6 min |
| Mnemonist Puppet | 5 | 26 | 3 | .26 | 64 | 16 | 12.0 min |

Reserve duration assumes one puppet, no replenishment and uninterrupted base upkeep. It is not a prediction of survival in combat. [PuppeteerSummonDefinitions](../src/main/java/com/vincenthuto/hemomancy/common/summon/PuppeteerSummonDefinitions.java) is the source of the base table.

The Hulk has the most HP and base damage while charging the least upkeep. Slowness and melee access may justify this, but it is the clearest candidate for sustained-combat dominance. Test narrow passages, fast targets, vertical terrain and open arenas before deciding to raise its upkeep. Veinwing mobility and Mummer/Ringmaster utility need appropriate encounters to show their value.

Combined use imposes three penalties: puppet range falls from 16 to 12, thread upkeep is multiplied by 1.5 with rounding, and ordinary morphling upkeep rises by 25%. These are substantial and should appear together when equipping both systems. Otherwise the player experiences a worse summon without understanding that the new body caused it.

Sanguine Spinning at D5 converts ten blood/second into one thread/second: 600 blood/minute against roughly 60/minute base passive recovery. This is a costly emergency/conversion channel, not free sustain. Eight thread per physical item corresponds to 80 blood through spinning. The crossbar and Spindle should make this comparison visible.

The Circus branch adds acclimation, an encounter progression and a consequential succession/liberation choice. It offers a better explanation for special puppet identity than a generic recipe upgrade. Its complete encounter flow, shared-site ownership and choice persistence were not live-played here.

**Fun assessment:** Bootlace movement, Emberfang timing, bodily emergency effects and differentiated puppet roles have convincing mechanical hooks. Satisfaction remains unverified because movement feel, hit feedback, summon pathfinding, command latency and fight duration were not observed live. A fair benchmark must use equivalent total investment, test solo and combined loadouts, and count player damage taken, command actions, kills, resource spend and failures of reach—not just damage dealt.

## NPCs, conversations and cooperative institutions

NPC specialization generally makes sense: Hermit introduces the path; Vicar directs rank and covenant responsibilities; Alchemist teaches processing; Mnemonist teaches memories; Artificer teaches equipment; Anchorite teaches bodily adaptation. Their individual assignments give the progression a social structure worth preserving.

The missing connective tissue is a consistent answer to “what do I do next, and why did that not count?” A useful guidance contract is: current rank and chapter; exact unmet condition; responsible NPC or station; one actionable next step; and a way to repeat instructions. The ledger already tracks more than some generic rank hints admit. Reuse that state rather than authoring a second, drifting approximation.

Seven duplicate English keys overwrite more specific Artificer ledger completion/handoff descriptions with shorter text. This loses details such as equipping a fitting at the Scarlet Vanity and applying the Monolithic Cornerstone to a consecrated Armature. The inventory also found five missing literal translation keys, including jar/field-note guidance surfaces. These counts are from literal-call scanning, not a full proof that all dynamic translations exist.

At D5, the Vicar does have dedicated Founding Fane lore. The problem is that the general rank hint emphasizes the bloodline oath without reliably prioritizing the next founding action. At D6, generic preparation advice should resolve to Chamber return, Throne binding or Vigil according to the first unmet flag. Do not add duplicate quest systems to solve an information-routing problem.

Recruitment limits of one NPC per type and one per outpost make geographical relationships matter. They also increase travel and search burden when the player needs several professions. Explain those limits before the player relocates or invests in an outpost, and retain a way to locate the relevant unrecruited teacher.

The Fane problem is more serious than unclear text. `completeFoundingFane` requires the leader, and chapter migration likewise recognizes the leader's Fane. A member without an earlier personal certification cannot simply progress alongside that leader. Recommended design: the leader consecrates; members earn their own chapter by carrying out an owned service/contribution within that Fane. Do not automatically certify every nearby player, and do not require each friend to abandon the institution to prove they can found one.

For solo play, NPC helpers are a valid substitute for other players in the relevant late rites. That keeps the path conceptually solo-capable. The actual locate → recruit → position → retain helper loop still needs a survival walkthrough; a fixture that supplies the helper skips those possible failures.

A two-player verification should cover separate chapter flags, shared facilities, invitations, member departure/rejoin, leader offline, simultaneous station use, helper ownership, pome ownership and monolith interactions. None of those should be called verified by the fake-player probe alone.

## Monolith, observances, Qliphoth and the endgame fork

The monolith contains degree-sensitive dialogue and contributes to the late equipment/lore path. Its crafted structure requires D7, while ordinary block interaction accepts D5 and higher. Therefore D5/D6 access depends on an already existing monolith; a dialogue branch is not proof the player can craft its speaker at that degree. Some lower-degree authored text is unreachable through normal block access.

The confirmed destruction trigger counts ordinary D7 interactions on the shared block entity. Two conversations are enough, even without the deliberate press-further choice. This can consume a teacher while the player is still trying to understand it, and shared interaction state makes multiplayer more surprising. Bind the spectacle to explicit choice and preserve a means to recover essential instructions after it.

The specifically named Observances implementation belongs to the Unstained path. The inspected Harbinger monolith supplies lore and progression interactions, but not an equivalent complete repeatable observance curriculum. If monolith teaching is intended to become a continuing Harbinger practice, that is missing content to design and implement; it should not be reported as an existing full system that merely needs a few values changed.

Qliphoth ripening checks every 40 ticks with a 1-in-80 chance. One pending pome, including one claimed but not eaten, prevents another from ripening. Assuming immediate pickup and communion, nine successful ripenings average 24 minutes. A seeded 10,000-run simulation gave median 23.2 minutes, 90th percentile 34.5 minutes and 95th percentile 38.3 minutes. These are best-case mathematical waiting times; travel, inventory handling and hesitation add time.

The tree provides a reason to stay near the covenant, and temporary pome empowerment can encourage activity between harvests. However, pure randomness produces a long quiet tail. Add visible growth progress or bounded bad-luck protection if the intended climax should maintain momentum. Do not claim a twenty-four-minute average guarantees a twenty-four-minute experience.

Owner restrictions are intentional: another player cannot simply consume the owner's pome. Full-inventory feedback exists. Test death, disconnect, unloaded areas and a claimed-but-unconsumed item before asserting recovery is safe under every circumstance. Ripening notification currently can open dialogue for an online owner; a toast or ledger notification would avoid interrupting unrelated combat or conversations.

After the ninth communion, non-Apotheos rites are restricted. This is a consequential content lock, so preview it before the final consumption. The player should know which preparations remain useful and that refusal is a distinct route.

Pruning is not generic tree farming. The owner's D7 Silent-pending/communion state governs the Living Sickle interaction. The Vesper route has an actual staged ordeal and owner-bound completion, with a transition from Crowned Refusal to Evening Star. The completion yields Silent Archon while retaining degree 7. Apotheos is the D8 route. Present these as different resolutions, with their persistent consequences, rather than implying the Silent route forgot to award a rank.

Source: [QliphothBloomEvents](../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/QliphothBloomEvents.java). Chamber transitions and the Vesper fight remain a live-validation gap; a dedicated server without the relevant custom-dimension setup cannot establish the whole experience.

## Other system surfaces and coverage

The audit included an inventory beyond the named systems. The table records depth honestly: inventory/source inspection does not mean every interaction was played or individually tested.

| Family | Included surfaces | Assessment / remaining gap |
|---|---|---|
| Blood supply and routing | Gourds, Gourdvine Tap, Bloodwell, Hematic Stakes, conduit, Suture Node, Earthen Vein, basin/pylon | These connect personal resource use to a workshop and Fane. Verify conservation and ownership across a complete network; do not balance every station as if it consumes a separate unlimited pool. |
| Equipment and loadouts | Armature, Scarlet Vanity, Effigy, Reliquary, Living weapons, Visceral Mirror | Meaningful equipment/body choices exist. Artificer handoffs lose useful text through duplicate keys. Mirror structure/use gates differ, and paid organ changes need clear previews and recovery guidance. Full equipment combinations were not combat-benchmarked. |
| Fungal workshop | Mycelial Crucible/Lantern, Fungal Podium, Implantation Pylon, Dendritic Distributor, fungal scars | Implementations route work through blocks, menus and packets. Empty block-entity tick methods are not proof a station is unfinished. Passive enzyme economics is the clearest numeric issue. Full cultivation and graft playthrough remains open. |
| Morphling support | Incubator, Cradle, Specimen Jar | More than an equipped passive item: there is acquisition, feeding and stationed support. Cradle action/upkeep costs differ from ordinary equipped upkeep; test combined sustain and ownership rather than reusing the ten-blood/minute figure. |
| Puppetry and labor | Spindle, crossbar, summon skills, Semi-Sentient Construct | Thread economy, combat roles and utility labor are distinct loops. Pathfinding and task reliability need live scenarios. Do not infer an empty feature from a manager-driven tile. |
| Ritual infrastructure | Cardinal Focus, Brazier, suspended crystals, filler blocks, emitters, trial altar | Spatial ritual identity is strong. Completion feedback, exact payment and failure recovery are the important integration boundaries. |
| Covenant and travel | Throne, Vigil, Warp Chair, Fane, monolith, Qliphoth, non-Euclidean/Chamber surfaces | Persistent ownership and chapter progression need coherent solo/co-op rules. Travel and custom-dimension persistence remain unverified live. |
| Discovery and flavor | Discovery Inscription, Dictation Table, Witness Organ, Mortal Display, idols, Scrying Podium | These can make discovery and bases feel inhabited. Witness recording has actual behavior; decorative or contextual roles are not inherently incomplete. Tutorial-critical information needs a repeatable home. |
| Adjacent paths | Unstained Podium, Altar of Cleansing, Pallid Retort, Stillwater Condenser, cleansing crystal surfaces, Saint Sarcophagus | Included in inventory and crossover dependency review. The full Unstained campaign was outside the Harbinger mock-route execution, so no whole-path balance claim is made. |

## Mock-route comparison and what remains to play

These are source-walked scenarios informed by the executed probes, not fictional transcripts presented as observed sessions.

| Route | Expected behavior from source | Evidence / next live checkpoint |
|---|---|---|
| New solo novice | Hermit discovery → initiation → Vicar proof → Alchemist sampling | First search duration, message readability and lost-NPC recovery remain unknown. Start with no granted progression. |
| Solo workshop learner | One valid pair → enzymes → ordinary Alembic reagent → crystal → first weave | Reagent/crystal controls passed; flask, rack and Loom problems reproduced. First meaningful stop is deterministic crafting, before broad combat tuning. |
| Solo specialist | Compare bodily mobility with early puppets; construct scar loadout | Definitions support distinct play styles. Needs controlled terrain/combat, identical investment, actual input and failure feedback. |
| Two friends sharing a base | One leader founds, both contribute and expect D6 eligibility | Source exposes member certification mismatch. Run two actual clients to confirm all UI, packet and persistence consequences after the contract is repaired. |
| Established D6 covenant | Return from Chamber, bind Throne, recruit/position helper, complete Vigil | Source and ledger contain the chain. Natural acquisition and custom-dimension transitions have not been completed here. |
| D7 communion | Teach → grow → nine pomes → informed Apotheos choice | Two-click monolith failure reproduced; pome wait modeled. Need live late-rite accounting and meaningful activity during growth. |
| D7 refusal | Teach → reach refusal state → prune → Vesper → Silent Archon | Actual route exists in code. Fight feel, death/retry and portal recovery remain open. |

A proper follow-up live run should stop at the first broken or disappointing checkpoint, record the exact inventory, degree, chapter flags and action, then reproduce it without granting the missing proof. Fixtures may accelerate passive waiting or place a known prerequisite for an isolated test; they cannot establish natural discoverability or end-to-end progression.

## Implementation sequence recommended by this audit

1. **Restore trustworthy crafting:** fix H01–H04 and H07 together with conservation and output-selection tests. Keep the successful reagent/crystal controls. Ensure full outputs, reloads and failed starts preserve inputs.
2. **Restore progression ownership and deliberate choices:** implement the member chapter route and explicit monolith destruction action. Cover simultaneous users and repeatable teaching.
3. **Make guidance reflect actual state:** repair language duplicates/missing keys, align Hermit/Vicar/Alchemist handoffs, describe Alembic geometry, show Loom output and explain the final communion restriction.
4. **Set resource targets using working systems:** compare active sampling, passive fruiting, thread conversion, combined upkeep and large structure payment. Resolve Apotheos's actual charge before tuning it.
5. **Run natural solo and two-player journeys:** discovery, inventories, travel, helpers, dimensions, death and reload. Then benchmark morphling/puppet roles and complete both endgame resolutions.
6. **Fill the content gaps demonstrated by play:** repeatable Harbinger observances if intended, meaningful covenant responsibilities for members, and encounters/activities that justify post-ascension powers.

The audit establishes real repair targets and a stronger progression model. It does not establish that all 539 recipes, 114 powers, dialogue branches or multiplayer combinations are balanced. The next useful milestone is a reliable novice-to-workshop route and a shared-Fane chapter contract, followed by live evidence through both endings.
