# Blood Structure Offering Spawners Design

## Goal

Teach and display the real Blood Structure offering system wherever structures are generated for testing or debugging. The Harbinger journey must require the player to load and light offering braziers for the Vial Centrifuge, while the Debug Structure Spawner must render every recipe with its offerings already visible and active.

## Shared Offering Layout

Add a shared main-source helper that accepts a Blood Structure recipe, its placed structure bounds, and a placement origin. It expands every `BloodStructureOffering.count()` into one distinct offering slot because the crafting matcher requires a separate lit Iron Brazier per required unit.

The planner places slots immediately outside the structure footprint, alternating between the west, east, north, and south sides. Positions must:

- Remain within the production offering search distance.
- Never overlap structure blocks, another brazier, labels, or the fixture platform boundary.
- Have solid support beneath them.
- Be deterministic for the same bounds and offering list.

Each planned slot carries the `BloodStructureOffering` it satisfies and a representative stack from the ingredient. If an ingredient exposes no concrete matching stack, placement fails with the recipe id and offering index instead of silently creating an invalid display.

## Journey Mode

The Vial Centrifuge journey fixture uses the shared planner after placing the height-aware structure.

It places the required Iron Braziers with ritual phase zero and no inserted offering. For the current recipe this produces:

- One empty, unlit brazier for one Glass Bottle.
- One empty, unlit brazier for one Copper Ingot.

The fixture supplies the Glass Bottle and Copper Ingot. The already-supplied Blood Projection tool is the ignition tool: the player projects blood into each phase-zero brazier to light it. Fixture blood includes the structure's 150 mL cost plus `BrazierBlock.BLOOD_TO_LIGHT` for each required brazier. The player must insert each item and light both braziers before projecting the supplied structure with the Ferric Binder.

The checkpoint continues to require the machine-crafted statistic and the placed Vial Centrifuge, so preexisting outpost machines cannot satisfy it. Journey ownership includes the braziers and their block entities so reset and clear remove their offerings safely.

The action text explicitly instructs the player to load and light both braziers before projection.

## Debug Structure Spawner Mode

The Debug Structure Spawner passes each Blood Structure recipe's offerings to the shared planner. It places one Iron Brazier per offering unit, inserts the representative stack, and sets `BrazierBlock.RITUAL_PHASE` to phase 1, the normal lit state accepted by production matching.

Recipes without offerings produce no braziers. Cardinal Rite showcase placement remains unchanged because this offering list belongs to Blood Structure recipes.

Labels retain their existing positions. Showcase spacing accounts for the expanded brazier footprint so adjacent structures and labels cannot collide.

## Failure Handling

Journey preparation fails closed if a required brazier or support position is occupied, outside build limits, or cannot create an Iron Brazier block entity. It reports the exact position without modifying unrelated blocks.

Debug showcase placement skips only the invalid recipe's offerings, reports the recipe id and reason to the invoking player, and continues placing the remaining showcase entries.

## Testing

Automated coverage must prove:

- Offering counts expand into distinct planned slots.
- Planned positions are deterministic, unique, supported, outside structure bounds, and inside offering search range.
- The Vial Centrifuge produces two journey braziers that begin empty and unlit.
- The journey supplies one Glass Bottle, one Copper Ingot, and the ignition tool.
- The Debug Structure Spawner loads the matching representative item into each brazier and sets ritual phase 1.
- Recipes without offerings produce no offering braziers.
- Multiple offerings cannot collide with structure blocks or adjacent showcase entries.
- Journey cleanup removes owned brazier block entities and their contents.
- `alphaCheck`, release-jar isolation, and `git diff --check` remain green.
