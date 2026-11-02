# Crimson Troupe implementation report

Implemented October 7, 2026, in the existing checkout. The accepted design is recorded in [the implementation plan](../../../superpowers/plans/2026-10-07-crimson-troupe-expansion.md).

## Delivered

- Seven professors teach personal preparation before ordinary Cardinal Ordeals. Existing learned shapes remain usable. School progress, practical mastery and guest payments persist independently of Harbinger Main progression and the two existing Circus endings.
- The Marrow Juggler retains `marrow_spitter` identities with a hunched robe, three masks, six arms and three spinning knives. Its three sequential daggers share the previous rounded projectile impact budget and retain Blood Loss; armor mitigates the separate strikes. The Hulk retains its stats with a Strongman body and synchronized strike windup. Cinder Bellows adds the gaunt lung-sac humanoid over a serpentine coil, its frame recipe, ordeal and telegraphed fire cone.
- Strongman, Beast Tamer, Threadkeeper and Retired Understudy faculty have dialogue, histories, disagreements, preparation advice and aftermath responses. Paired demonstrations use actual puppet bodies while bypassing combat, ownership, upkeep, trial credit and loot. New faculty stay outside the original finale cast.
- Five teaching camps cover every ordinary shape and include Threadkeepers. A 65×65 pavilion adds school rooms, rehearsal courts, loft, wardrobe, mess and quarters around the preserved arena. Abandoned stages supply evidence. The Waybill guides eligible instruction or an unfinished pavilion; placement checks the actual selected footprint.
- An Empty Ring, seven practicals, The Wrong Audience, The Missing Understudy and daily guest acts use server-observed facts. The Side ledger gives next actions. Full inventories retain exact unpaid Filaments. Captive deaths and the existing Succession/Liberation outcomes remain intact.
- Recipes, props, blood profiles, eight book entries, English text, mechanics/lore references and wiki documentation are updated. Three editable Blockbench models embed the runtime atlases.

## Verification

- **92 focused JVM tests passed**, including curriculum, eligibility, projectile budget, attack-memory provenance, model baking, UV coverage and Circus resource contracts. The broader legacy Puppeteering checks also passed.
- **15 focused mod-loaded GameTests passed**: real dialogue dispatch, degree/blood/proximity restrictions, ordeal gates and legacy knowledge, demonstration safety, reward overflow and clone preservation, Crossbar proof, Cinder ownership reload, all seven practical completions, the old Blood Shot versus three-dagger unarmored impact comparison, and actual template loading with preserved arena data.
- Asset checks confirm quarter-unit model geometry, painted UV islands, matching embedded textures, native 16px frame/prop art, complete teacher coverage and preservation of all **3,435 legacy blocks and block-entity NBT**. The three Blockbench exports pass converter checks. Runtime templates, textures and the Cinder ordeal are present in the assembled jar; editable `.bbmodel` sources follow the existing packaging exclusion.
- `compileJava`, `compileGameTestJava` and `assemble` completed. A fresh read-only review found five issues; targeting, replay provenance, projectile damage, client windup and visible juggling were corrected and rechecked. No Critical or Important finding remains from that review.

Logs are under `build/crimson-troupe-*.log`. Rebuild assets with `tools/circus/build_troupe_assets.py`, merge English text with `tools/circus/write_troupe_language.py`, and check authored assets with `tools/circus/verify_troupe_assets.py`.

## Cinder Bellows visual revision

The original flat fifteen-part model was replaced with forty connected joints and sixty-four quarter-grid cubes. Scarlet Serpent informed the overlapping ten-joint tail chain; existing humanoid puppets informed shoulder, elbow, wrist and hand attachment. The upper body curves into a tapered coil, with separate lung lobes beneath angled ribs, a charred skull, hinged jaw and long fingers. A repainted twelve-color atlas supplies soot, ventral scutes, exposed bone and ember accents. Every animation frame resets the rig before applying travel curvature and eased breathing poses.

Rebuild this model alone with `tools/circus/build_cinder_bellows.py`. The full Troupe builder delegates to the same authoring source. `tools/circus/preview_cinder_bellows.py --serve` serves an interactive textured comparison at `http://127.0.0.1:8766/preview.html` and saves PNG comparisons into `build/cinder-bellows-rework/`.

Nine focused JVM tests pass, covering joint chains, lung/jaw recovery, pose reset, propagating travel motion, grounded tail geometry, model baking and atlas coverage. `assemble`, authored-asset checks and Java-to-Blockbench geometry/texture comparison pass. Front, rear and angled authoring previews were inspected with idle, travel and breath poses. This does not establish native Minecraft appearance or animation acceptance. Gameplay, recipes and summon stats are unchanged by this revision. The focused build log is `build/cinder-bellows-rework/focused-tests.log`.

## Marrow Juggler visual revision (2026-10-08)

The Juggler now has fifty-two connected joints and ninety-four quarter-grid cubes. The existing Pinwheel-inspired direction is expressed through a hunched hood, three angled carved masks, six fanned bone arms and a split crimson robe. Shoulders, elbows, wrists and fingers form actual parent chains. Each knife is one articulated prop whose grip passes through the posed palm at its catch frames; the three pairs use staggered arcs. Separate robe panels, trailing strips and the sash sway after a full pose reset. The twelve-color atlas adds readable mask expressions, cloth folds, worn hems, ivory and iron accents.

`tools/circus/build_marrow_juggler.py` rebuilds only its Java model, runtime PNG and portable Blockbench export. The full Troupe builder delegates to this authoring source. `tools/circus/preview_marrow_juggler.py --serve` serves the actual textured hierarchy at `http://127.0.0.1:8767/preview.html`, with a saved comparison in `build/marrow-juggler-rework/`.

Thirteen focused JVM tests pass: four Juggler rig/catch/reset checks, four shared model/UV checks and five Bellows rig regression checks. Authored-asset validation and fresh Java-to-Blockbench geometry/texture comparison pass. `assemble` passes. Front, rear and angled source previews were inspected, including knife catches, airborne arcs and travel. Native Minecraft appearance remains unverified. Registry identity, stats, recipes, combat and worldgen are unchanged by this revision. Build logs are in `build/marrow-juggler-rework/`.

## Gorebound Hulk visual revision (2026-10-08)

The previous fourteen independent cube parts are replaced with forty connected joints and fifty-seven quarter-grid cubes. A taller skull and neck, sloped traps, separate pectorals and back muscles, a narrow waist and broad bound hands express the Strongman silhouette. Each arm contains an elbow, wrist, four curled fingers and a thumb; each leg contains a knee and boot. Bindings follow their forearms. The existing synchronized strike counter raises the fists through the actual limb chains, while the existing swing progress supplies the downward follow-through and recovery. Knee flexion and ankle compensation keep walking soles level. The twelve-color native atlas adds clotted muscle shading, stitched seams, ivory wraps, a skull mouth, cloth folds and black iron fittings.

`tools/circus/build_gorebound_hulk.py` rebuilds its Java model, runtime PNG and embedded Blockbench source. The full Troupe builder delegates to this same narrow authoring script. `tools/circus/preview_gorebound_hulk.py --serve` supplies the textured before/after comparison at `http://127.0.0.1:8768/preview.html`; comparisons and build logs are in `build/gorebound-hulk-rework/`.

Seventeen focused JVM tests pass: four Hulk checks cover hierarchy, raised fists with planted boots, knee/ankle motion and strike recovery without pose drift; four shared checks cover baking and UV/texture contracts, and nine retain Juggler/Bellows rig coverage. Authored-asset validation, fresh Java-to-Blockbench comparison and `assemble` pass. The packaged model class and PNG match their compiled/runtime sources. Front, rear, angled and side authoring views were inspected with idle, walking, wind-up and recovery poses. Native Minecraft presentation remains unverified. This revision changes no registry IDs, combat rules, summon costs, recipes or worldgen.

## Teaching camp visual revision (2026-10-08)

All five 25×9×25 teaching camps now have triangular A-frame tents with red-and-ivory canvas bands, ridge supports, flags and courtyard entrances. Separate tent decks meet a narrow boardwalk instead of a full rectangular platform. A worn oval yard, stepping stones and practice courts with clipped corners break up the ground plan. Hematic lantern posts, benches, storage and a bed furnish each site. Aerial rigging, knife targets, Strongman weights and masks, fire practice supplies, and the Understudy's library distinguish the five variants. Existing faculty NBT, teacher positions, Spindles, crafting tables, story records and fire-practice utilities are retained. Placement registration, spacing and pavilion templates are unchanged.

`tools/circus/build_teaching_camps.py` rebuilds only these five NBTs. Shared template serialization lives in `tools/circus/troupe_templates.py`; the full Troupe builder delegates to the narrow camp builder. `tools/circus/preview_teaching_camps.py --serve` reads the actual NBTs and cached Minecraft 1.21.1 block models/textures for an interactive comparison at `http://127.0.0.1:8769/preview.html`. Before templates and comparison captures are in `build/teaching-camp-rework/`. Mobs and entity-rendered blocks (Spindle, banners and beds) are omitted from the authoring preview.

Two focused JVM template tests pass after the peaked-roof check failed against the old flat tents. All fifteen focused Circus GameTests pass, including native template loading, peaked roof and ivory stripe states, clear entrances, teacher headroom and instruction blocks. All five variants were inspected in the textured authoring preview. `assemble` and authored-asset checks pass; all five packaged NBTs match the source bytes, and the extracted shared helper produces identical legacy room geometry. Logs are in `build/teaching-camp-rework/`. Native client presentation and fresh natural terrain placement remain unverified. Existing camps are not rebuilt; the revised templates affect newly generated sites. Lore and MnA compatibility need no update for this cosmetic revision.

## Faculty visual revision (2026-10-09)

The Strongman, Beast Tamer, Threadkeeper and Retired Understudy previously rendered with the vanilla zombie model and flat 64×64 placeholders. Each now has its own `HumanoidModel` on a shared `CircusFacultyModel` base. The base keeps authored shoulder pivots attached through swings and applies the troupe's shared DOWNED pose (`CircusDownedPose`): seated on the ground, torso folded forward about the hips, with the head and arms carried on the posed torso. Costume props are child parts posed by act state:

- **Threadkeeper:** eyeshade, spectacles, sleeve garters and a hand-held ledger. A belt spindle turns quickly during inspection, while the head bows over the ledger.
- **Retired Understudy:** stooped, in faded harlequin with a shawl, and walks with a planted cane. The half-mask stays pushed up on the brow and comes down over the face for the act.
- **Strongman:** broad, bald, with a moustache and a striped one-strap leotard. A barbell appears only for the act and is pressed overhead from the fists.
- **Beast Tamer:** frogged crimson coat with tails, epaulettes, fur mantle and tall boots. The belt coil becomes a hand whip with a three-segment lash during the act.

Each figure has two 128×128 variants that differ only in accents, plus an editable `.bbmodel` that embeds both. The `.bbmodel` follows the Fire Eater's mirrored-X Blockbench convention.

`tools/circus/build_faculty.py` rebuilds the four Java models, atlases and Blockbench files together; the full Troupe builder delegates to it and no longer writes the placeholders. `tools/circus/preview_faculty.py [out.png]` renders rest views and act poses using ModelPart's own box-UV and ZYX joint conventions.

Six focused JVM tests pass: four `CircusFacultyResourceTest` cases and the two `CircusTextureUvTest` checks. Together they cover groups, Java parts, UV bounds and island ownership, paint inside UVs, related variants, scale, the single head cube and painted eyes. The full JVM run reproduces only the eight unrelated failures listed below. Native Minecraft appearance remains unverified. Registry IDs, behaviour, dialogue and worldgen are unchanged.

## Performer control threads (2026-10-09)

`PuppeteerThreadRenderer` now accepts any living `CircusPerformerEntity` as a controller. Teaching stage bodies (owned through `CircusDemonstrations`) and performer-owned Enthralled Dolls draw the shared Animus thread from the performer's drawn right hand.

`CircusPerformerRenderer` records that hand every frame. It runs `StringHandLayer` after the model is posed, so the anchor follows walking swings, act poses, the Acrobat flip and the Stilt Walker spin.

The grip is the bottom centre of the right arm's own cubes, ignoring props on child parts. The anchor is stored in `PerformerHandAnchors` as a world-axis offset from the interpolated entity position, recovered by inverting the frame's base rotation.

`PuppeteerThreadEndpointRules.performerHandEndpoint` (a third-person right hand along the body yaw) is only the fallback for a performer that has never been rendered.

All control threads now round shut at both ends. `PuppeteerThreadEndpointRules.threadRadius` closes about three segments at the controller and two at the body, so neither end shows the hollow hexagonal tube. This covers player, Vesper, Blood Drunk and performer threads. Stage bodies previously linked to their teacher only in server `PersistentData`. Their synced owner is now the teacher, and older saved bodies are backfilled on their next stage tick. The cancelled stage tick still bypasses ownership, upkeep and combat, and `canAttack` already excludes stage bodies in both directions. As a side effect, owned stage bodies no longer despawn in Peaceful. The Carousel's bound captives remain unthreaded passengers.

`PerformerHandAnchorsTest` (grip from baked arms and offset recovery under a rotated frame), the endpoint test and `CircusPerformerThreadSourceTest` pass with all focused Circus, puppet-summon and Vesper thread suites (30 suites). Native appearance remains unverified.

## Broader checkout failures

The latest full JVM run executed **2,501 tests with eight failures** outside the Troupe changes: Bombardier spawn/loot expectations, Hemojelly blood-profile membership, missing armor textures, the existing acolyte encoding error, two tests reading the existing encoding of `docs/TESTING.md`, an archived Morphling rename reference, and an armor-reagent localization expectation.

The combined server run executed **798 tests with four failures**: two centrifuge output/container checks, Hemojelly prey ticking, and the Pyrosome water-creature-cap control. All Troupe tests passed within that run and in the isolated run. The resource-log gate is blocked by the combined runtime result. Packaged-resource validation also rejects the checkout's existing untracked archive at `textures/item/old/sanguine formation/` because its paths contain spaces. These unrelated files were preserved.

## Acceptance still required

Live-client presentation, fresh natural terrain placement and frequency, a complete natural-Survival apprenticeship and both endings, multiplayer isolation, and a cold server restart have not been accepted here. Focused fixtures and template checks do not establish those outcomes. Newly generated sites use the expanded template; old placed pavilions retain their legacy pieces.
