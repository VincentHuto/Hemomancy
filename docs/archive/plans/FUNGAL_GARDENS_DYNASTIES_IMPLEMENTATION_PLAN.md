# Fungal Gardens Dynasties and Castle Worldgen Plan

## Purpose

This plan turns the dynasty concept into player-visible Fungal Gardens content.

The main implementation is static worldgen: generate flesh-and-hyphae dynasty castles in several visible stages of reabsorption, populate them with Fargones, and use models, roles, and environmental details to make the relationship between Fargones, Thirsters, and Bloodlickers readable.

The hivemind's internal competition, the reasons dynasties eventually disappear, and the meaning of each castle state remain lore expressed through the environment. They do not need a persistent simulation in the first implementation.

## Settled Lore to Show Through the World

### Fargones

- Fargones are intelligent, cultivated sample gatherers created by the hivemind.
- They belong to dynasties with inherited traits, household teaching, and competing methods.
- Their competition is for useful worlds, specimens, and blood, not sabotage inside the Fungal Gardens.
- A castle is a dynasty's home, laboratory, nursery, receiving hall, and launching point for scouts.
- Fargones should feel like individuals within a lineage, not identical ambient mobs.

### Thirsters

- Thirsters are failed Harbingers whose first attempt to absorb and control blood broke their ability to regulate the appetite.
- They are husks caught in feeding, bloodletting, frenzy, and decay.
- They retain intermittent awareness and want peace, but cannot hold onto control.
- Their relationship to Fargones is historical and thematic: both are blood-shaped products of the wider fungal organism, but a Thirster is a failure left wandering while a Fargone is a deliberately cultivated success.

### Bloodlickers

- Bloodlickers are specialized collection organisms used to sample Cardinal Rites.
- They gather blood and information, then return to the dynasty that sent them.
- Their relationship to Fargones should read as subordinate and functional: Fargones are the intelligent dynasty members, while Bloodlickers are expendable collection bodies handled by the household.
- Existing rite behavior remains the primary live gameplay use. Castle appearances can establish where Bloodlickers come from without requiring a new persistent return system.

### Reabsorption

- Every dynasty is eventually reclaimed by the hivemind.
- A successful lineage becomes specialized, then is eventually pulled apart so its useful traits can be recovered and recombined.
- A castle can therefore be found intact, partially absorbed, or nearly dissolved.
- These are static generated states. They communicate a dynasty's history without requiring the world to transform the structure during play.

## Implementation Scope

### In scope

- Authored dynasty castle structures generated in the Fungal Gardens.
- Several static structure variants showing different stages of reabsorption.
- Fargone population and spawn roles tied to those structures.
- A small set of Fargone model, texture, or accessory variations.
- Environmental evidence connecting Fargones to Thirsters and Bloodlickers.
- Castle rooms and props that communicate scouting, specimen gathering, lineage, and reabsorption.
- Reusing existing Cardinal Rite Bloodlicker behavior and existing mob registrations where possible.

### Out of scope for the first pass

- Server-level hivemind or dynasty simulation.
- Persistent infection stages on arbitrary dimensions.
- Spore foothold progression and dynamically grown passages.
- Dynamic castle contraction or runtime reabsorption.
- A faction relationship system or inter-dynasty AI.
- New long-distance travel rules for Fargones or Bloodlickers.
- Making the player manage dynasty favor, specialization, or reclamation timers.

## Castle Structure Set

Begin with one dynasty architectural language and four static variants. The variants can share a palette and major silhouette while changing their room availability, growth, and damage.

### 1. Favored dynasty

This is the intact, active castle.

- Full towers and connected halls.
- Living flesh under a maintained crust and hyphae framework.
- Bright blood vessels feeding the central chambers.
- Active specimen gardens and nursery spaces.
- Fargone scouts, attendants, and guards present.
- Bloodlicker receiving chamber intact and visibly used.

### 2. Mature dynasty

This castle is still productive but more specialized and crowded.

- Additional annexes and specialized laboratories.
- Older rooms sealed behind newer growth.
- More glass, crust, bone, and preserved specimens.
- More differentiated Fargone roles and model variants.
- Evidence that the household has been repeatedly sending scouts out into other worlds.

### 3. Partially reclaimed dynasty

This shows reclamation beginning while the house still has inhabitants.

- Outer towers folded into the ground.
- Walls split open where hyphae and flesh are being pulled inward.
- Some rooms remain furnished while adjacent rooms have collapsed into biomass.
- Fargones concentrated around the remaining central keep.
- Empty or damaged Bloodlicker receiving channels.
- A few specimen chambers still maintained because the dynasty is trying to remain useful.

### 4. Nearly dissolved dynasty

This is an old house reduced to a readable ruin.

- Mostly buried foundations and exposed glass chambers.
- Hardened crust ridges where walls used to stand.
- A central remnant still attached to the fungal ground.
- Very few Fargones, ideally an elder, attendant, or isolated guard.
- Remains of nursery, scout, and receiving areas visible in the ruin.
- The structure should look reclaimed rather than conventionally destroyed.

The intended final presentation is authored templates placed through normal Fungal Gardens structure generation. The first implementation slice uses a registered static worldgen feature to establish the castle silhouettes and population without waiting for binary template assets. The stage is selected at generation time. No structure needs to change state after placement in the first pass. The feature can later be replaced by authored templates while preserving the same stage and population contract.

## Castle Rooms and Environmental Storytelling

Each variant should use a subset of the same readable room language:

- **Scout hall:** launch points, observation slits, world maps or biological diagrams, and hanging travel membranes.
- **Receiving hall:** Bloodlicker channels, swollen collection niches, drain basins, and glass containers.
- **Specimen gallery:** preserved organisms, blood samples, cages, and experimental growths.
- **Nursery:** developing Fargone bodies, shed membranes, lineage markers, and protected warm tissue.
- **Dynastic court:** a central chamber showing that Fargones have hierarchy, inherited roles, and household identity.
- **Reclamation wound:** a section where the structure has folded inward, exposing the material being recycled.

The structures should make the player infer the lore from what is physically present. A room full of Bloodlicker receiving sacs and Fargone observation platforms communicates more than a new runtime statistic would.

## Fargone Population and Variations

### Population roles

Use the existing Fargone entity as the base population, with structure-specific placement and small behavior/pose differences where practical:

- **Scout:** positioned near exits, upper walls, observation slits, and launch chambers.
- **Attendant:** placed around specimen rooms and receiving chambers.
- **Guard:** positioned at entrances, central halls, and valuable chambers.
- **Brood or lineage keeper:** placed in nursery rooms and mature dynasty interiors.
- **Elder or dynast representative:** rare, placed in the court or deepest surviving chamber, especially in mature and partially reclaimed structures.

These roles do not need separate AI factions. Spawn position, count, equipment or held props, idle pose, and model variation can carry most of the distinction.

### Model and texture variation

Add a small controlled set of Fargone appearances rather than making every Fargone unique:

- base scout;
- pale or glass-marked specimen attendant;
- darker crust-armored guard;
- swollen or ornamented dynasty elder;
- worn, partially fungalized variant for reclaimed castles.

Variations can be implemented through alternate textures, model layers, small geometry changes, or a synchronized entity variant value. The renderer should select the appearance deterministically from the entity's saved variant so it remains stable after reload.

The variants should communicate occupation and age while preserving the recognizable Fargone silhouette. They should not become entirely new mobs.

## Relationships Between the Existing Creatures

The player should understand the relationship through placement, props, and encounter context:

- Fargones occupy the castle as its thinking population.
- Bloodlickers are found in receiving halls, collection niches, or rite-related displays, showing that the household sends and receives them.
- Thirsters should not populate dynasty castles as normal residents. Their presence belongs in abandoned outskirts, old infection sites, or external areas where a failed Harbinger was left behind.
- A reclaimed castle can contain visual traces of former Fargones without turning them into Thirsters: collapsed nursery sacs, empty feeding stations, preserved shells, or a single remaining Fargone guarding a dying court.
- Bloodlicker rite encounters can use existing spawn behavior while the castles establish their origin and purpose.
- Loot and specimen props can connect the three creatures without adding a new universal data system.

## Existing Code Seams

The first implementation should build on the current project structure:

- Existing Fungal Gardens biome and dimension worldgen provide the placement context.
- Existing structure JSON and structure-manager patterns provide the authored-template path.
- `FargoneEntity`, `FargoneModel`, and `FargoneRenderer` are the primary model/population integration points.
- `BloodlickerEntity` already has stored-blood state and is already used by `CardinalRiteOrdealEngine`.
- `ThirsterEntity`, `ThirsterModel`, and `ThirsterRenderer` can receive visual and ambient adjustments without becoming part of castle generation.
- Existing loot and specimen systems can be reused for castle containers and displays.

## First Buildable Slice

The first implementation should prove the visual idea with one structure family:

1. Add one favored dynasty castle template.
2. Add one partially reclaimed version of the same castle.
3. Add one nearly dissolved remnant.
4. Register them for rare generation in the Fungal Gardens.
5. Populate each state with different Fargone counts and role placements.
6. Add at least three deterministic Fargone visual variants.
7. Build a receiving chamber that visually establishes the Bloodlicker relationship.
8. Place Thirster traces or an external Thirster encounter near the oldest/reclaimed variant rather than inside the court.
9. Reuse the existing Cardinal Rite Bloodlicker wave unchanged while documenting the castle as its dynastic origin.

This gives the player a complete visual read of the concept without requiring a new simulation layer.

## Verification Plan

- Validate structure registration, placement, spacing, biome targeting, and generation frequency.
- Validate that all structure variants load and that their templates do not contain missing blocks or entities.
- Validate Fargone spawn counts and role placement in each structure state.
- Validate deterministic model/texture variant persistence after save and reload.
- Inspect enlarged screenshots of each castle state for readable silhouette and material distinction.
- Check that the partially reclaimed and nearly dissolved states look biologically absorbed rather than conventionally destroyed.
- Verify that Thirsters remain visually and spatially distinct from Fargones and are not implied to be reclaimed Fargones.
- Run the existing Cardinal Rite tests for Bloodlicker siphoning separately from the new structure/worldgen checks.

## Boundaries to Preserve

- The first pass is static worldbuilding expressed through generated structures and populated spaces.
- Do not introduce persistent hivemind, foothold, passage, dynasty-favor, or dynamic-reabsorption systems as prerequisites for the castles.
- Reclamation state is selected when a structure generates; the castle does not need to transform during play yet.
- Fargone variation should strengthen individuality while keeping one recognizable species.
- Bloodlickers remain tied to collection and Cardinal Rite sampling.
- Thirsters remain failed absorbers with intermittent awareness, separate from the Fargone lineage.
