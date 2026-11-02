# Phlegethontic Nether: implementation and acceptance

Approved 2026-09-12. Implement inline in the current checkout. Preserve the conversation background and both concept images in this folder, unrelated dirty work, existing saves, dependencies, and progression contracts. No automatic commits or worktrees.

## Scope

A TerraBlender Phlegethontic Basin biome with organic basalt/blackstone/venous-stone aqueduct terrain; independent sealed low-Nether ichor veins; animated hazardous ichor; a fully modeled, river-bound Excoriated with Recall Barb and Arterial Sounding combat.

Fire Resistance blocks heat but not Blood Loss. Heartbeats change current forces, animation, and guardian attacks without changing fluid levels. Extraction, fever, draughts, and Course of Phlegethon are deferred.

## Implementation checklist

- [x] Shared ichor/scab registries, resources, contact rules, and pulse.
- [x] Seeded basin/channel/arch geometry and chunk-local materialization.
- [x] Nether climate mapping, biome, surface rules, restart configuration, and tags.
- [x] Seeded buried veins, shell preflight, Nether-wide injection, and containment.
- [x] Excoriated spawn rules, home/sentinel persistence, full combat, projectile, and tether.
- [x] Editable guardian model, textures, animations, projectile/tether rendering, and pulse visuals.
- [x] Behavioral JVM tests, codec/resource checks, explicitly registered dedicated GameTests.
- [x] Five-seed worldgen measurements, containment, second-region/foreign-biome compatibility.
- [x] Focused live-client visual inspection and two-player pulling/escape/cleanup checks; broader artistic acceptance remains below.
- [x] Focused tests, compileGameTestJava, runGameTestServer, alphaCheck, packaged build.
- [x] Reference documentation, wiki, testing guide, and evidence report.
- [x] Screenshot follow-up: replace rectangular overhead clearing with rounded overlapping vaults and tapered protected boundaries; add regressions and inspect a fresh client world.

## Measured acceptance still open

- [ ] Vein frequency target: measured 6.2 successful origins/100 chunks; target 3-5. Required origin activation remains 1/18.
- [ ] Materialized raised-course target: 9/16 layouts (56.25%); target at least 60%. Planned raised geometry occurs in 22/22 active layouts, but protected terrain and biome clipping remove some courses.
- [ ] Broader artistic review of cavern-visible arches, surviving connections, motion, and swimming feel across the five seeds.
- [ ] Controlled total generator overhead and wider third-party modpack acceptance. The second-region/foreign-biome fixture and feature-level timing checks passed.

Implementation is present and the automated gates pass. These measured tuning and broader acceptance gaps are not marked complete. See [VALIDATION.md](VALIDATION.md) for exact counts, captures, live multiplayer evidence, and limits.

## Terrain contracts

Register hemomancy:phlegethontic_basin and a RegionType.NETHER region hemomancy:phlegethontic_nether. Common configuration: enabled=true and weight=1 (1-100), requiring restart. Emit the five explicit vanilla Nether climate points: replace Basalt Deltas with the Basin and defer other slots to vanilla. Do not use addBiomeSimilar for Nether biomes: TerraBlender 4.1.0.8 searches only Overworld points.

Use pure seeded geometry, separate replacement/containment rules, and chunk-local feature writes. Each 128x128 macro cell has 2-4 noise-warped basins, radii 18-42, curved connecting channels with half-width 4-7, and an extra loop when there are four basins. Fluid levels start sea level +8 through +36, with descending spillways. Raised courses reach 28 blocks above their footing and have asymmetrical arches around 11 blocks apart. Preserve cavern headroom and bedrock. Overhead clearance is the union of rounded, noise-shaped basin and channel vaults, independent of fluid-column ownership. Taper carving before biome/protected boundaries and retain exact block-position biome checks; a flat twenty-four-block ceiling or a four-block biome cache recreates artificial cuts.

Scab lines fluid troughs two blocks thick. Basalt/blackstone support them with visible venous-stone bands. Blend outer terrain over 12 blocks; close fluid courses at biome borders and protected obstacles. Reconstruct neighboring layouts from coordinates and seed. Write only the active chunk, in solids/voids/fluid order. Check actual biome positions, not only the feature origin. Exclude structures by bounding box, ores, block entities, portals, and foreign blocks. Allow air replacement for constructed landforms. No jigsaws, templates, modular masonry, or replacement Nether generator.

Register basin terrain once in local_modifications. Buried veins are injected exactly once with neoforge:add_features into #c:is_nether at underground_decoration. Each origin chunk activates with probability 1/18; every receiving chunk reconstructs intersecting paths without an outer rarity filter. Paths are 32-72 blocks, radii 1-2, with two-block scab shells. Entire envelope stays 5-20 above dimension bottom. Replace only natural netherrack/basalt/blackstone. Preflight, seal shortened sections at obstacles, and never rely on unwritable planned neighbors as successful containment. Compatible foreign biomes participate where supported host stone exists.

## Fluid and guardian contracts

### Generation sequencing discovered during implementation

The underground-decoration feature registers one chunk-local vein job. It completes at the start of LIGHT, after neighboring FEATURES finish, and refreshes light sources before lighting. The queued job and protected structure bounds are chunk attachments, so unfinished protochunks can be saved safely. This preserves the single biome-modifier injection while preventing later neighboring vanilla decoration from cutting a vein's shell.

Low-Nether ore candidates are recorded in a compact chunk bit mask before their replacement checks. Veins reserve those cells even if another vanilla feature has already changed the host stone. Forward/reverse generation exposed that checking the final ore block alone was insufficient: basalt and ore features can compete for the same cell. The hooks do not change ore random draws or placement behavior. All river and vein block writes remain inside the owning chunk.

Fluid sources receive explicit scheduled ticks after protochunk placement. The geometry uses the Nether chunk generator's sea level (32), since the level-wide sea-level accessor returns 63. Retaining banks use adjacent reservoir elevations to close spillways without changing source levels during heartbeats. Primary connections receive raised supports; channel cores take precedence through overlapping bowls. Closed lower courses extend their retaining walls to the highest adjacent bank, and arch spacing uses an approximately eleven-block period.

Ichor density 1500, viscosity 5000, temperature 1300, light 7, slope distance 2, level decrease 2, and no renewable sources or ordinary bucket collection. Animated dark arterial-red fluid with sparse fatty foam and dark clots; scab is dark blood-scorched netherrack. Provide all fluid textures, overlay, block/item models, loot, tags, language, and creative entries. The requested texture refinement uses 16×16 frames for both fluids: four still keyframes in a slow forward/backward churn and sixteen downward-flow frames, plus a matching 16×16 overlay. Generated artwork, prompts, exports, and validation are preserved under `materials/ichor-16px/`.

Contact: 3 heat damage per 10 ticks, brief ignition, and Blood Loss I refreshed for 60 ticks independently of thermal immunity. Respect bloodless/effect-immune entities. Deduplicate each entity's contact processing. Do not activate blood magic. Guardians ignore ichor damage and currents. Pulse every 160 ticks for 30 ticks; double bounded current force, using channel/path direction. Animate from world time with interpolation and bounded client sampling; do not tick or scan unloaded terrain.

Guardian anatomy follows the reference: exposed equine muscle, lowered horse head, human torso, shared ribs/lungs, left arm grown into the bow, right arm drawing the arterial string. Native model/renderer, editable Blockbench source, animated walk/wade/aim/draw/fire/rear/breath/clot/death, matching barbed projectile and interpolated tether.

Attributes: health 52, speed .28, melee 8, range 40, armor 6, knockback resistance .45, dimensions 1.55x2.7. Natural spawn weight 4, group 1-2, Basin-only shores with ichor within 8 horizontal/4 vertical blocks. Validate full footprint/collision, permit ichor-lit shores, forbid Peaceful. One persistent sentinel candidate per macro cell, spawned only by its owning chunk on a valid largest-basin shore. Save home anchor and identity; no resurrection on reload.

Patrol within 32 blocks of home, on banks and in shallow ichor. Prioritize emerging/elevated players, ignore deeply submerged passive targets, retain retaliation. Recall Barb: 24-block firing range, 60-tick interval (40 during pulse), 4 damage, at most 80-tick tether. First 20 ticks measure blood. Active player blood ratio >.5 pulls toward deeper ichor; lower blood pulls toward guardian; otherwise use health ratio. Targets over four blocks above the river receive two-shot volleys. Within three blocks, rear for 20 ticks and breathe a scalding cone, cooldown 100. Bleeding targets retain short awareness through obstructions, but shots require line of sight. Away from ichor for 100 ticks: clot and slow; after 200: gradual damage. Return reverses penalty.

Only one tether per victim. Pull using bounded collision-respecting velocity, not teleportation. Sneak for 20 ticks to tear free, costing active players 1% maximum blood (cap 100), otherwise 1 damage. Show a localized hint. Clear links on death/disconnect/dimension change/excess distance/owner unload/reload. Server owns combat; entity data synchronizes visual states. Include spawn egg, suitable existing sounds, bones/sanguine-formation loot, and language.

## Validation

Behavioral JUnit coverage: actual climate mappings and codecs; negative coordinates; connected/overlapping layouts; chunk ownership/order; obstacle, cave, foreign block, ore, and biome-edge sealing; damage cadence and immunity; pulse; spawn footprint; sentinel uniqueness; tether/escape/collision/home/reload. Runtime tests belong in src/gameTest/java and must be explicitly discovered through DevTestHooks.

Run five fresh isolated Nether seeds. Record locate distances, basins/channels/arches, vein origins, sentinels, seams, fluid escapes, and timings. Require zero exposed buried cores, out-of-radius writes, duplicate sentinels, or visible seams. Tuning targets: 3-5 successful origins/100 chunks; raised courses in >=60% active layouts; added median/p95 generation <5/15 ms on the same machine. Test a second Nether region and foreign-namespace biome with supported and protected host materials. Live client checks cover fluid motion, anatomy, arm-bow animation, tether placement; two players verify pulling and cleanup. Distinguish automated evidence from any outstanding live/compatibility checks.

## Baseline

Before implementation, compileJava and compileGameTestJava passed. FerventChitiniteNetherPlacementResourceTest passed when run as its standalone main; it is not a JUnit test. Initial unrelated dirty paths were recorded in the task transcript.
