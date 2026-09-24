# Phlegethontic Nether

The Phlegethontic Basin is a Nether biome of dark arterial ichor, scorched scab banks, venous stone, basalt, and blackstone. Its reservoirs have connected, jagged shorelines with broad finger-like projections and coves that reach roughly six to twelve blocks beyond or inside the underlying bowl. Every solid face touching ichor is blood-scorched scab. Across the exposed bank, scab breaks gradually into blackstone, then blackstone dithers into venous stone instead of forming hard material rings. Banks rise and fall by zero to two blocks and submerged floors vary by one block in either direction while the ichor surface remains level. Irregular reservoirs still connect through curved channels and raised courses with eroded arches. The terrain is procedural and follows the local biome boundary and protected terrain. Rounded, irregular vaults clear space above the water and taper into the surrounding cavern. Worldgen changes affect new chunks; already generated cutouts are not rebuilt automatically.

Buried ichor veins also occur outside the Basin. Their cores and scab shells remain between five and twenty blocks above the Nether floor. They can pass through compatible Nether biomes containing natural netherrack, basalt, or blackstone. Ores, structures, block entities, portals, and unsupported foreign blocks interrupt and seal their paths.

## Escharian Overgrowth

Escharian Scyphus is a gatherable, shallow cup plant. One block space holds one to five Scyphus: placing another Scyphus onto an existing group increases its count, and breaking the group returns that many items. Counts one through four use the four authored small-cup arrangements; count five uses the original large cup. It attaches to any sturdy block face except Escharian Overgrowth Rim, opens away from its support, and can be waterlogged. Removing the supporting block drops the full group and leaves its water behind.

In newly generated Basin terrain, colonies replace 10–30 exposed natural stone blocks with dark Escharian Overgrowth. The shallow patches follow steps and wall/ceiling corners, including exposed cells that meet along an edge at inward corners. Scyphus decorates 50–70% of their backing cells, with each occupied position independently using a count from one through five. Wall and ceiling patches have no separate pale rim blocks.

About one in eight feature attempts first searches for a ground pile, with other eligible habitats as fallback. These solid, irregular mounds are 3–4 blocks across and 2–3 blocks tall, with tapering dark tiers and a single-block-high pale boundary at ground level. Scyphus covers every available exposed top and side position around the dark centre blocks, with multiple cups per block. No cups attach to the pale rim. Plant projections add to the solid mound dimensions.

Every colony has an infested venous stone foundation replacing the terrain beneath or behind it. An approximately two-block-wide infested band follows the adjacent floor, wall, or ceiling, interlacing with regular venous stone over another two blocks. The outer edge is irregular and leaves some original terrain visible. These replacements preserve the existing stone contour and skip ores, fluids, decorations, and protected structures.

The feature retains its rarity filter, Basin restriction, and ichor proximity. Centre and rim blocks remain Bombardier habitat; the decorative plant has no movement collision and is not solid mob support. Existing chunks retain their previous growth. The large multiblock cup generator is removed; old configured-feature data keeps supported habitat settings and uses the new patch defaults.

## Phlegethontic Bombardier appearance

The Bombardier has mottled charcoal-brown armor, rusty-red markings across its wing covers, dark red abdominal joints, and worn bone-colored wing rims, nozzle, and limb tips. The texture repaint preserves its existing model and spray animation.

## Ichor and its heartbeat

Ichor uses 16-pixel animation frames with dark red pools, black clots, and sparse fatty foam. Still surfaces churn slowly; flowing surfaces carry streaks downward.

Ichor deals three points of heat damage at ten-tick intervals and briefly ignites exposed entities. Fire Resistance protects against heat; it does not prevent Blood Loss I. Bloodless creatures and existing effect immunities retain their protections. Contact does not activate blood magic.

Every eight seconds, a thirty-tick heartbeat strengthens gentle currents and the guardians' attacks. Fluid levels stay stable. You can swim against the capped current. Ordinary buckets cannot collect ichor. Extraction, fever, draughts, and Course of Phlegethon remain future content.

## Excoriated

The Excoriated has an exposed equine body, a lowered horse head, a long human torso, shared ribs and lungs, and a bow grown into its left arm. It has 52 health and remains near its home shore. It can spawn on illuminated, fully supported shores in the Basin; it never naturally spawns in Peaceful. Persistent sentinels are selected during terrain generation and do not return after being killed.

A Recall Barb deals four projectile damage and can tether a victim for four seconds. The first second measures the victim. Active blood-magic users above half blood volume are drawn toward nearby deeper ichor; lower-volume targets are drawn toward the guardian. Other targets use health ratio. Only one tether can hold a victim at a time.

Hold Sneak for one second to tear free. Active players pay one percent of maximum blood volume, capped at 100 mL, when enough blood is available. Otherwise, severing costs one damage. Tethers end when their owner or victim dies, unloads, changes dimension, or moves too far away, and are cleared on reload.

Elevated targets receive a two-shot piercing volley. At close range the guardian rears before releasing a short scalding breath. Bleeding targets can be remembered briefly through obstructions, but arrows still require line of sight and collide with blocks. After five seconds away from ichor, the guardian visibly clots and slows; after ten seconds it begins to take damage. Returning to ichor reverses the penalty.

Initial loot uses bones and existing sanguine formations. There is no new progression gate or extraction recipe.

## Development and acceptance

The editable Blockbench model is `src/main/resources/assets/hemomancy/models/entity/bbmodel/excoriated.bbmodel`. It uses the approved Orcadian horseman: 58 rotated cuboids, a fused forearm bow, and a 256×256 box-UV atlas. At rest, its thin arms extend down the flanks and its fused bow trails near the ground. The limbs retract into the aiming pose over the first eight draw ticks and lower again after firing. Native Java model parts drive walking, wading, aiming, drawing, firing, rearing, breath, clotting, and death poses. Tethers attach to the interpolated, animated bow transform.

See the [implementation checklist](../docs/phlegethontic-nether-worldgen/2026-09-12-phlegethontic-nether-worldgen.md), [validation report](../docs/phlegethontic-nether-worldgen/VALIDATION.md), and [testing guide](../docs/TESTING.md) for measured results and remaining acceptance checks.
