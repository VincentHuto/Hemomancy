# Rite merge stages 2–4: one Liber map, special floors, rite spaces

Date: 2026-10-18. Status: the user asked to finish the three agreed follow-ups to the Blood Structure / Cardinal Rite merge and said "just do it", so every design call below is the builder's call unless stated. Follows `2026-10-14-rite-purposes-and-working-crafts-design.md` (sub-project 1).

Harbinger only. Unstained rites, structures and their Liber tabs are untouched (see the deferred Unstained ritual system).

## Stage 2 — one view of rites and crafts, grouped by purpose

### Liber (Harbinger progress screen)

- The **Rites** and **Crafting** tabs become one tab, **Rites & Crafts**. It shows every Harbinger rite, Working craft, floorless Blood Structure, ritual floor and Ichorian sigil on one radial map.
- Families are grouped by **purpose group**, in this order round the map:

| Group | Colour source | Families |
|---|---|---|
| Floorless | Blood red | Floorless Crafts (the eight floorless structures) |
| Self | Threshold floor glow | Order, Body/Will, Threshold Floors |
| World | Dominion floor glow | Domain/World, Fane, Qliphoth, Puppetry, Trials, Dominion Floors |
| Making | Working floor glow | Vessel, Apparatus, Stations, Ritual Infrastructure, Constructs/Effigies, Station Upgrades, Working Floors |
| Bond | Communion floor glow | Bloodline, Ancestral, Communion Floors |
| Reference | Muted | Ichorian Sigils |

  Families stay contiguous within their group. Inside a group they keep the existing earliest-degree ordering. A special floor joins the floors family of its purpose.
- The old `Qliphoth/Forbidden` family splits by purpose: Bloom goes to World/Qliphoth and Ancestral Communion to Bond/Ancestral. `Bloodline/Fane` splits into World/Fane (Founding Fane, Dominion, Succession, Restoration) and Bond/Bloodline. Exsanguination (making) joins Vessel.
- Family labels are tinted with their group colour, and a thin arc in that colour runs beyond the rim across each group's sectors. The family filter control cycles purpose groups instead of single families.
- Selecting a rite, floor or sigil opens the rite inspector. Selecting a floorless structure or Working craft opens the crafting inspector (subject on its bed). Shift+right-click imprints a blank Mnemonic Blueprint for either.
- A data test pins that every rite in a family has that family's purpose, and that floorless crafts sit only in Floorless.

### JEI

`BloodStructureRecipeCategory` takes an optional purpose. There are five categories:

| Category | Contents | Icon |
|---|---|---|
| Blood Structures (floorless) | the floorless tier, as today | Sanguine Formation |
| Threshold Rites | every Harbinger `self` rite | Hematic Iron Block |
| Dominion Rites | every Harbinger `world` rite except Succession/Restoration (kept in their own category) | Chiseled Hematic Iron Block |
| Working Rites & Crafts | every Harbinger `making` rite (Working crafts, vessels, Horn, station upgrades, Exsanguination) | Bone Block |
| Communion Rites | every Harbinger `bond` rite | Blood Wood Planks |

A rite's preview shows its Working subject, else its required structure, else its floor. The footer names its floor. The steps tooltip is worded per purpose. The Cardinal Focus and Living Staff are catalysts for the four rite categories.

## Stage 3 — special floors

Three special floor styles exist, grand tier only. Each rite marked `"special": true` moves onto its own floor:

| Rite | Purpose | Floor | Laid pattern (9×9, Focus centred) |
|---|---|---|---|
| Rite of Apotheos | self | `apotheos_grand` | **The Hush.** Polished venous stone. Chiseled polished venous stone rings at Chebyshev distance 2 and 4, and hematic iron blocks on the Focus's four sides. Rings close in on silence. |
| Bloom of the Qliphoth | world | `qliphoth_grand` | **The inverted tree.** Calcified hyphae ground. Nine erythrocytic mycelium nodes (one per husk) on three pillars, joined by hyphae block paths into the Focus. |
| Rite of Ancestral Communion | bond | `ancestral_grand` | **The lineage.** Polished venous stone bricks. A blood wood plank border, a mnemonic ambergris ring at distance 2, and ambergris threads along the diagonals. |

Rules:

- Floor JSON gains an optional `"purpose"`. The four standard styles imply theirs. Any other style is a **special floor** and must declare a purpose.
- A Harbinger rite that names a special floor must be `"special": true`, and the floor's purpose must equal the rite's. Standard purpose/floor validation is otherwise unchanged. A data test pins the three assignments.
- Visual style: three new style indices in `CardinalRiteFloorVisualRules` (4 Apotheos, 5 Qliphoth, 6 Ancestral), each with a palette, and a motif in `cardinal_floor_membrane.fsh`:
  - *Apotheos*: an ash-pale membrane with faint rose vessels and a cold white glow. Concentric hush rings travel inward toward the Focus, and the heartbeat is muted.
  - *Qliphoth*: bruised violet-black with wine vessels and a spore-lilac glow. Nine husk nodes swell on the beat, root lines join them to the Focus, and the edge creeps further onto the ground.
  - *Ancestral*: dark amber with ochre vessels and a honey glow. Memory threads run along the diagonals, with bright flecks drifting outward along them.
- Every other consumer (Focus particles, boundary renderer, escalation floor, swallow) already resolves palettes by style index, so it follows automatically.
- Existing worlds: a D7 player who built a threshold/dominion/communion grand floor for these rites must build the special floor. Nothing in progress is lost (the floor is checked at activation).

## Stage 4 — rite spaces

### The space

- When a Harbinger rite **completes** on a formed floor, the cube above that floor becomes a **rite space**: x and z within the floor's half-width of the Focus, y from 1 to 2×half above it. This is the same N×N×N space braziers count in. The space records Focus position, dimension, owner, the rite's purpose, the floor id, the rite id and the creation tick.
- One space per Focus. A later completed rite on the same Focus replaces it, so the latest purpose wins.
- The space lasts as long as that same floor stays formed at that Focus. It is released when the Focus's periodic floor check finds a different floor or none, or when the Focus block is removed. It is not released on chunk unload.
- Stored in Overworld saved data (`hemomancy_rite_spaces`), like every rite zone. Synced to clients per dimension.
- Shown as sparse glow motes in the floor's palette tracing the cube's top rim and corner seams, within 32 blocks. They brighten while the player holds something rite-bound.

### Rite-bound things

Tags declare what only works inside a rite space: `hemomancy:rite_bound/self|world|making|bond|any` for **blocks**, **items** and **entity types**. A thing in a purpose tag needs a space of that purpose. `any` accepts every space.

- **Blocks:** can only be placed inside a matching space, and right-clicking one outside does nothing (it may sit outside if its floor broke later).
- **Items:** right-click use, use on a block and use on an entity are refused outside a matching space, with an action-bar line.
- **Entities:** a tagged entity outside a matching space unravels after a 5-second grace: a blood-cell burst, then it is discarded with no drops.
- Tooltips on tagged items and block items name the space they need.
- Pure rules (`RiteBoundRules`) decide admission. Events stay thin.
- **First member:** the **Sanguine Omen** block is `rite_bound/world`. Its portent reads a world-changing rite's ground. It is decorative, so gating it costs no progression. The tags are the extension point for future rite-born content. Nothing a player needs before their first completed rite may be rite-bound.

## Tests and docs

- JVM: purpose-group data test; merged-map spacing; special-floor purpose validation and roster; style palettes; rite-space geometry; `RiteBoundRules`; JEI partition source contract.
- GameTests: a completed rite consecrates a space that its floor's breaking releases; the Sanguine Omen is refused outside and placed inside.
- Docs: `HEMOMANCY_REFERENCE.md` §5.3 (floors, special floors, rite spaces) and §25.2; `AGENTS.md` contract line; `wiki` rite pages; `LORE_REFERENCE.md` lines for the three special floors.

## Out of scope

Unstained rites/tabs; per-floor ceremony mechanics; new rite-born item art; a balance pass.
