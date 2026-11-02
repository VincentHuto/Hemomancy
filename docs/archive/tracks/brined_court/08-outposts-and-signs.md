# Track 8: Inner-house outposts and signs

**Status:** DONE 2026-10-30 (unattended run). The six inner-house outposts (Tide Refuge, Wake nursery, Line Camp joined and parted, Screened Cleft, the Bell, Whale Fall) are templates from `tools/worldgen/brined_court/outposts.py` with nine generated set-piece blocks and a house marker each; the sign tier (Sunburst blooms, house markers, relay markers, messenger buoys, vent offerings) is one capped placed feature, `hemomancy:court_signs`; template validation, `court_worldgen_validation` sign caps (seeds 42, 1337, 8675309) and `brinedOutpostReviewClient` pass. **Track 9 has given every outpost its residents (2026-10-30), so the outposts can ship.** Left: the reviewer pass, and the open call on whether to widen the Wake nursery's bank rule (it still almost never generates; see "Calls made unattended").

Phase B. Depends on Track 6. Ships with at least placeholder residents from Track 9 (an empty outpost reads as abandoned, which the Court is not). Spec sections: "Three tiers of presence: Outposts, Signs", the six inner house texts.

## Goal

One small outpost per inner house, two or three buildings at most, each instantly telling you which house you've found by its shape. Then frequent small signs that say whose water you're in without placing a settlement.

## Shared approach

Same as Track 7: jigsaw or single-template structures built by `tools/worldgen/brined_court/` builders, set-piece blocks from `tools/model_export/`, biological light, no copper family, no torches. Each outpost is in `brined_court_outposts` and runs `CourtPlacementRules` first. Footprints stay small (under 32×32) so the corner-first screen is the whole check.

## The outposts (PROPOSED layouts on the spec's SETTLED concepts)

| Outpost | House | Site | Layout | Resident spots (Track 9) | What the player gets |
| --- | --- | --- | --- | --- | --- |
| **Tide Refuge** | Holding Stone | Rockpool Shore, an enclosed pocket pool | A sheltered pool behind an entrance braced by a Holdfast-rooted member (set piece: root masses cemented into the rock). One shelter room above the tide line; a safe passage cut through the stack. | 2 | Shelter in a storm; the first Court building most players see, since it's visible from the surface. |
| **Wake nursery** (rare) | Shared Wake | Pelagic Ocean, a seamount flank or bank | No buildings: a sheltered ground of grown nest-cups (bird's-nest-fungus purses) on the bank where young are raised. Low weight in the set. | 1–2 tenders; the school itself roams (Track 9) | Where to find the school if you can't catch it. |
| **Line Camp** | Woven Current | Twilight Ocean, across a gully | Pods (grown, lamp-lit with the Rope's violet-blue glow) hung on lines strung between the gully walls, linked by strands that can visibly part and rejoin (two layout variants, joined and parted, chosen per placement). | 2 | A camp you swim through; lines you can climb. |
| **Screened Cleft** | Veiled Fold | Midnight Ocean (warm/temperate surface), a canyon wall | A doorway into the canyon wall behind a hanging veil, almost invisible from outside. A host's receiving room inside; further rooms closed to the visitor. | 2 | Received by a host; one room, not the whole house. |
| **The Bell** | Still Bell | Midnight Ocean (cold/frozen surface) | One sealed diving bell on the floor with an air pocket inside: a deep rest stop. Grown, not forged: a sclerotium-shell bell with a weighted rim, no metal. Sealed sleepers hang in the bell's upper ring. | 1 awake, 2+ sleepers | Air and rest at depth. Waste and neglect have consequences (Track 11). |
| **Whale Fall** | Last Marrow | Carrion Depths, sediment fan or fossil bed | One great skeleton (set-piece bone, distinct from the static fossil ribs the terrain already places), homes inside the ribcage, rooted elders in the hollow bones with red feeding plumes. | 1–2 mobile, 1–2 rooted | Food and care at depth; Marrowplume shown at its fullest. |

Each outpost also gets one **house marker** at its edge (below) so the house is named in-world.

## Signs (PROPOSED)

Placed features, not structures. They are the "frequent" tier, so they must stay small and capped.

| Sign | Where | What it is |
| --- | --- | --- |
| Sunburst bloom | Water surface over reef and coast, rarer over open Pelagic Ocean | A floating patch of gold bloom (a lily-pad-like block family, 1–3 variants). Marimo's visible marker. Denser around the Lighthouse Garden. |
| House marker | One per outpost, plus rare lone markers inside each house's water | A carved stone or grown post with the house's mark (eight marks, one per house). Reads as a boundary: "you are in the Veiled Fold's water". |
| Relay marker | Along the layer boundaries | A post where messengers hand off between depths, with red current threads drifting near it. Set dressing only (L4). |
| Messenger buoy | Open water surface | A grown float with a trailing line. Set dressing only (L4). |
| Vent offering | Hydrothermal chimneys outside the Sanctum | A small offering cluster at a chimney's foot: the Kindling feed heads beyond their Seat. |

**Budget:** each sign family has a rarity and a per-region cap enforced in the feature's placement (a deterministic hash per 256×256 region, so placement is order-independent). Track 6's validation counts them.

New blocks: bloom (and its item, harvestable as a Sunburst-related food or catalyst, if Track 2 needs a catalyst), eight house markers (one block with a property, or eight blocks; PROPOSED one block, `house` property), relay marker, buoy. Each from a model generator.

## Review client

Extend `brinedSeatReviewClient` from Track 7 with an outposts pass (each outpost from approach, entrance, interior) and a signs pass (bloom field at the surface, a marker underwater). Or a separate `brinedOutpostReviewClient` if the Seat client is still in flight.

## Tests

- Template validation as in Track 7 (pools resolve, no forbidden blocks, the Bell's air pocket stays dry after reload, Line Camp variants both valid).
- Sign caps per region (unit test on the region hash, plus the validation run's counts).
- Registry and creative goldens.

## Docs

The Court section in `docs/HEMOMANCY_REFERENCE.md` lists outposts and signs. `docs/LORE_REFERENCE.md`: one line per outpost tying it to its house's belief.

## Open questions

- Not blocking: the Wake nursery is the spec's "at most a rare, small nursery ground". Confirm it's wanted at all (otherwise the Shared Wake exists only as the roaming school).
- Not blocking: the eight house marks need designs. They could come from the icon pack's strain silhouettes simplified to a glyph.

## What landed (2026-10-30)

**The outposts do not ship until Track 9 gives them residents.** An empty outpost reads as abandoned, which the Court is not; the resident marks below are where Track 9's members stand.

- **Generator:** `tools/worldgen/brined_court_outposts.py` runs `tools/worldgen/brined_court/outposts.py` (the six builders, on Track 7's `kit.py` canvas, which it imports without editing) and `outpost_build.py` (templates, pools, structure JSONs; refuses to write if `checks.py` finds a forbidden block or floodable air). The entry script is tracked (`.gitignore`, `AGENTS.md`). Track 6's `structure/brined_court/stub_marker.nbt` is deleted: every outpost has its template.
- **Templates and pools:** `structure/brined_court/tide_refuge/refuge`, `wake_nursery/nursery`, `line_camp/{joined,parted}` (both in one pool, weight 1 each, so each placement picks one), `screened_cleft/cleft`, `still_bell/bell`, `whale_fall/fall`. Every element ignores waterlogging; every structure JSON names `start_jigsaw_name: hemomancy:court_outpost_start`, size 1. Each template is square, odd-sized and under 32 x 32, centred on its marker column (`CourtSeatLayout.outpostBelow` / `outpostCentre`); footings reach down to the seabed under the corners. The Tide Refuge is pinned to the sea surface like the Garden (`CourtSeatLayout.start`, `REFUGE_DEPTH` 8).
- **What each looks like:** Tide Refuge: rock stack round a pocket pool, a seaward arch braced by Holdfast Root Masses on both jambs, a shelter room above the tide (bed, shroomlight hearth, dried kelp, barrel), a passage cut east to west. Wake nursery: a boulder-and-kelp ringed hollow with 13 Nest Cups in a spiral. Line Camp: its own gully walls, three Current Strand lines with Rope Pod lamps, two ribbed Current Tissue lantern pods (joined by two strands, or parted with loose strand ends), climbing lines up both walls. Screened Cleft: its own rock face, a one-wide cleft with a Fold Veil, a receiving room (bone posts, low table and seats, veils), two sealed doors to further rooms. The Bell: a Sclerotium Shell dome on eight basalt legs, swim-in under the rim, moon pool up to a dry ledge (bed, kelp, barrel), four body-length Bell Sleepers in the upper ring, a shroomlight crown. Whale Fall: spine, flukes, paired ribs into the mud, a calcite-and-bone skull hollowed into a home, two hollow jaw bones, Marrow Plumes, mud-brick homes under the ribs. Each carries one House Marker for its house at its edge.
- **Set-piece blocks** (structure/feature-only, itemless, no drops; one generator each in `tools/model_export/`, `court_outpost_kit.py` shared on top of Track 7's `court_kit.py`): `holdfast_root_mass` (facing), `nest_cup`, `rope_pod` (light 11), `current_strand` (axis, climbable via `minecraft:climbable`, light 3), `current_tissue` (cube, animated veins, light 6), `fold_veil` (facing), `sclerotium_shell` (cube), `bell_sleeper` (light 3, hangs 14 px into the cell below), `marrow_plume` (light 5); signs: `sunburst_bloom` (`variant` pad/pair/bloom, `turn` 0-3; light 6/9; a `WaterlilyBlock`), `house_marker` (`house` = `CourtHouse`, `facing`; eight glyph models on one atlas; light 3), `relay_marker` (light 4; red glow motes via `HemoParticleData.glow`), `messenger_buoy`. Classes `SunburstBloomBlock`, `CurrentStrandBlock`, `HouseMarkerBlock`, `RelayMarkerBlock` in `common/block/harbinger/decoration/`; the rest reuse `CourtFixtureBlock` / `CourtFacingFixtureBlock`. `CourtHouse` now implements `StringRepresentable`.
- **Signs:** `hemomancy:court_signs` (`common/worldgen/feature/court/CourtSignFeature`, rules in `CourtSignRules`), in the VEGETAL_DECORATION step of all seven Pelagic biome JSONs (and the dormant `PelagicBiomes`/`BiomeInit` bootstraps). Fixed slots per 256 x 256 region from a seeded region hash: blooms 6 patches of 3-7 (reef and coast; one slot in three over the open sea), lone house marker 1, relay marker 2 (floor within 3 of a layer boundary), messenger buoy 2 (open surface over a Pelagic floor at Y40 or lower), vent offering 3 (the tallest chimney's foot of the region's built vent fields, a Vent Offering Basin). Reads and writes only its own chunk; skips any chunk a Court structure references. `CourtStructure.afterPlace` adds the Lighthouse Garden's denser bloom ring (one open-water cell in four, r 17 to 23.5).
- **Resident marks (Track 9):** `hemomancy:court_resident/<role>`: refuge_keeper, holdfast_braced; wake_tender (2); line_tender, pod_keeper; cleft_host, cleft_veiled; bell_keeper, bell_sleeper (2 hooks); marrow_carer (2), marrow_rooted (2). `CourtResidentMarkers.read` reads them as it does the Seats'.
- **Tests:** `CourtOutpostTemplateTest` (7), `CourtSignRulesTest` (6); `CourtTemplates` is the shared NBT reader the Seat and outpost template tests use (`CourtSeatTemplateTest` moved onto it, same assertions). `court_worldgen_validation` gained `CourtOutpostValidation` (nearest outpost of each kind found: house marker, resident marks, the Bell's air pocket dry now, no fluid tick beside it and after read-back; sign caps per region in full chunks round the nearest Garden and Sanctum, signs kept out of Court sites, the feature places some). `CourtSeatValidation.dry` takes the template list. Registry golden: 13 blocks and the `court_signs` feature, configured and placed feature; itemless set updated in `RegistrationGameTests` and `BlockInit.ITEMLESS_BLOCKS`.
- **Review:** `brinedOutpostReviewClient` (`BrinedOutpostClientReview`, `-PbrinedOutpostSeed`, default 42), screenshots in `build/brined-outpost-review/screenshots/`.
- **Docs:** HR §29 "The outposts and signs (Track 8)", `LORE_REFERENCE.md` "Brined Court outposts and signs" (one line per outpost tied to its house's belief), `wiki/Brined-Court.md` Outposts and Signs.

## Calls made unattended

- **Wake nursery kept** (the plan builds it; the README lists it as not blocking). Its placement is left exactly as Track 6 validated it, which means it still almost never generates (no bank passed the floor screen on the review seeds). Widening its bank rule would change Track 6's validated sites, so it is left as an open tuning call for the user. The review client places it from its own pool on an open-ocean floor near the origin so the template is still captured.
- **Every outpost brings its own ground** where the plan names a terrain feature (the Line Camp's gully walls, the Screened Cleft's canyon face, the Tide Refuge's rock stack), the same call Track 7 made for the Garden's tide rock: the predicate guarantees the house's water and a level-enough floor, not a gully or a canyon wall.
- **The Tide Refuge is pinned to Y62** (Track 6 allows its floor from Y56 to Y68) so its pool and shelter always sit at the tide line; its rock stack reaches eight blocks down. Its air is all above the waterline.
- **The Screened Cleft is flooded** throughout (a dry receiving room would need an airlock vanilla lacks); the Bell is the deep outpost with air. The closed further rooms exist behind solid doors.
- **Bell sleepers:** four are `bell_sleeper` blocks (set dressing); two empty hooks carry `bell_sleeper` resident marks for Track 9's sleeping members, plus one awake `bell_keeper`.
- **Sunburst Bloom has no item** (Track 2 named no catalyst); it is itemless like every other Court set piece. Vent offerings reuse Track 7's Vent Offering Basin rather than a new block.
- **House marks** are 7 x 7 glyphs simplified from the strain silhouettes (sunburst, gripping knot, fin and tail, branching strands, hood, bell, stalked plume, flame), inlaid half-bright on a blackstone post capped in the house's own material.
- **Deep water is named by its floor.** Over deep columns the water above the Pelagic band keeps vanilla ocean biomes, so a buoy or an open-sea bloom asks the floor's biome whose sea it is; shore and reef read their surface.
- **Caps** (per 256 x 256 region): blooms 6 patches, lone house markers 1, relays 2, buoys 2, vent offerings 3. Starting values; the validation enforces them as caps, not counts.
- **No sign in a Court site:** the feature skips a chunk that references any `#hemomancy:brined_court` structure (references are fixed before features run, so this stays order-independent).
- **`CourtSeatCandidates.search` over the outpost set can predict a cell the set's `exclusion_zone` then refuses** (the predicate measures from real Seats, vanilla from the Seat set's potential cells). It affects nothing that ships (the Relay Chart searches Seats only); the review client confirms each predicted outpost against its real start. Noted for the Track 6 owner.

- **Review fixes (2026-10-30, user).** The Wake nursery's site rule is widened (see Track 6's calls) so it generates; the template and pool are unchanged. Every set piece the user named now has a creative item in the Brined Court section (Sunburst Bloom, Frond, Crust and Lamp, Petrified Follower, House Marker, Holdfast Root Mass, Fold Veil, Rope Pod, Current Tissue, Current Strand, Sclerotium Shell, Bell Sleeper, Marrow Plume, Kindled Tissue, Hydra Crown, Vent Offering Basin), each with an item model that parents its generated block model. The Sunburst Bloom's item places it on water like a lily pad (`PlaceOnWaterBlockItem`) and the bloom picks its variant and turn from its position; the House Marker is one item whose stack carries its house in the `block_state` component, listed once per house, named "House Marker of <house>" and drawn with that house's model through the `hemomancy:house` item property. Nest Cup, Relay Marker and Messenger Buoy stay item-less (not on the user's list). Blocks still drop nothing. None of these blocks has a survival rule beyond the bloom's water check, so each places anywhere from its item.

## Verification (2026-10-30)

- `python tools/worldgen/brined_court_outposts.py`: all seven templates pass `checks.py` (no forbidden block, no floodable air); the stub marker template is removed. Every set-piece generator in `tools/model_export/` (the thirteen new blocks) ran with `--preview`; previews checked by eye.
- `./gradlew.bat test`: 4,751 JVM tests, 0 failures, on the final tree (includes `CourtOutpostTemplateTest` 7, `CourtSignRulesTest` 6, `CourtSeatTemplateTest` 8 on the shared `CourtTemplates` reader, and the architecture guardrails).
- `./gradlew.bat runRegistrationGameTestServer`: all 47 required tests pass (registry golden with the 13 blocks and the `court_signs` feature; itemless set). Run before the last template, texture and sign-rule tweaks, none of which touch registries.
- `./gradlew.bat runCourtValidationServer -PcourtSeed=<seed> -PcourtRun=t8-final -PcourtOrder=forward` on the final tree: pass on 42, 1337 and 8675309; 42 reverse (`t8-reverse`, before the last sign-rule change) also passes. Outposts inspected: 42 Line Camp, Screened Cleft, Whale Fall; 1337 all five that generate (Tide Refuge, Line Camp, Screened Cleft, the Bell, Whale Fall); 8675309 Line Camp, Screened Cleft, the Bell, Whale Fall. The Bell's 380 air cells under the sea are dry, have no fluid tick beside them and read back dry (1337, 8675309). Signs in 914 full chunks round the nearest Garden and Sanctum, outside Court sites, worst region per family: 42 blooms 19, house marker 1, vent offering 2; 1337 blooms 9, house marker 1, buoys 2, vent offering 2; 8675309 blooms 10, relay 1, buoys 2, vent offering 2. All within the caps (blooms 42, house marker 1, relay 2, buoy 2, vent offering 3).
- `./gradlew.bat runBrinedOutpostReviewClient` (seed 42): 114 shots in `build/brined-outpost-review/screenshots/` (six outposts by day, by night and by night with night vision; the Wake nursery placed from its pool because none generates within range; the signs pass with the Garden's bloom ring, the sign gallery and natural bloom, buoy and relay). Reviewed by eye: the Tide Refuge's braced arch, pool, passage, shelter and cairn; the nursery's cups; the Line Camp's gully, lines and lantern pods; the Screened Cleft's face, veil and receiving room; the Bell's dome, legs, air pocket and sleepers; the Whale Fall's ribs, skull and jaws; every sign block. Two camera stops start close to geometry (the Line Camp's climbing-line stop, one Bell ring stop); camera tuning, not geometry.
- `python gradle/source-text-impact-map.py`: regenerated.
- Not run: the full `alphaCheck`, and the reviewer pass (a separate reviewer runs after this session).
