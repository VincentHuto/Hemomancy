# Vanilla particle sweep and texel density: ready-to-execute plan

Audit date 2026-11-11. Read-only: no build, test or client was run, and no source file was changed. Another job was editing
source while this was measured, so line numbers can drift by a few lines; search for the quoted call if a line has moved.

Part 1 plans the vanilla particle sweep (A-33, AN-12). Part 2 re-measures texel density across the whole mod. Each part
ends with batches a later build session can take one at a time.

---

## Part 1. The vanilla particle sweep

### 1.1 What the ratchet counts

`VanillaParticleRatchetTest` matches `ParticleTypes.`, `DustParticleOptions`, `DustColorTransitionOptions`,
`BlockParticleOption` and `ItemParticleOption` anywhere in a file, imports included. The allow-list
(`src/test/resources/architecture/vanilla-particle-allow-list.txt`) pins 73 files with a total of 189.

Without imports and with each source line counted once, there are 157 matching lines. Three of those lines only split a
constant across two lines (two `DustParticleOptions` constants in `PeacockSpiderEntity`, one in
`HematicArmatureBlockEntity`). That leaves **154 call sites in 73 files**:

| Outcome | Call sites |
| --- | --- |
| Straight replacement with Hemomancy particles | 133 |
| Vanilla is the honest choice (stays, with its reason in the allow-list) | 19 |
| Dead code (commented out, delete the comment) | 2 |

When a file's last vanilla call leaves, its import goes too, and its allow-list row is deleted. A file that keeps an
honest call gets its count lowered and its reason rewritten from "pending the vanilla particle sweep (A-33)" to the
honest reason.

### 1.2 The mod's own particle vocabulary

All factories live in `common/particle/HemoParticleData` and are safe on either side (server `sendParticles` or client
`addParticle`). Sizes below are the quad half-extent in blocks after the first tick: a quad is twice that wide. A
1/16-block face pixel is a half-extent of about 0.031. Unless noted, the particle's starting velocity is doubled by its
constructor, it shrinks and fades linearly over its life, and it ignores block collisions.

| Factory (type) | Client class and look | Parameters, size, life |
| --- | --- | --- |
| `glow(color)` (HutosLib `glow`) | `ParticleGlow`, additive full-bright soft dot, slow roll | alpha 1.0, half-size 0.035 (about one face pixel), life about 68 ticks with random extra ageing. The standard fine mote. |
| `darkGlow(color)` (HutosLib `dark_glow`) | `ParticleDarkGlow`, the same dot on the dark-glow render type, reads as a stain or shadow mote | alpha 1.0, half-size 0.035, life about 68. Clots, smoke, voids. |
| `emberSpark(color, scale, alpha, life)` (HutosLib `ember`) | `ParticleGlow` with explicit numbers | Scale is the half-size, so `0.031F` is a 1/16-pixel mote and `0.2F` to `0.6F` is a flare. Life is halved by the particle. Use this for every cursor, trace and spark. |
| `ember(color, s, a, l)` | same type | Hands scale and alpha to HutosLib swapped. Old callers are tuned around it; new code should call `emberSpark`. |
| `bloodCell(color)` (`blood_cell`) | `BloodCellParticle`, red-cell sprite on the dark-glow render type | alpha 2.0, half-size 0.025, life about 25. Passing a vertical speed at or below `BloodCellParticleFactory.FEED_SPIRAL_MARKER` switches to the feed-spiral variant (half-size 0.18, 14 ticks). Blood spray, drips, bursts. |
| `absorbedBloodCell(color)` (`absorbed_blood_cell`) | `AbsorbedBloodCellParticle`, a cell that rides an absorption stream into a palm (`AbsorptionStreamRider`) | alpha 2.0, small, 10 to 40 ticks. Blood drawn toward something. |
| `sporiticSpore(color)` (`sporitic_spore`) | `ParticleGlow` with the hit-glow sprite, rising 0.0125 a tick | alpha 1.15, half-size 0.38 (a soft spore puff about 0.75 block wide), life about 24. The established replacement for `CRIMSON_SPORE` (`FungalGillBlock`, `RenderingPoolEvents` and `GardensNetwork` use colour (150, 40, 58)). |
| `serpent(color)` (`serpent`) | `SerpentParticle`, curling serpent sprite, additive | alpha 3.0, half-size 0.15, life about 53. |
| `bloodClaw(color)` (`blood_claw`) | `BloodClawParticle`, claw-mark sprite, dark render | alpha 2.0, half-size 0.2, life 25. |
| `hitGlow(color)` (`hit_glow`) | `ParticleGlow` with the hit sprite | alpha 3.0, half-size 1.25 (a 2.5-block flash), life about 37. Focal flashes only, never cursors or traces. |
| `bloodAvatarHit(color)` (`blood_avatar_hit`) | same size as hit glow, avatar-hit sprite | alpha 3.0, half-size 1.25. |
| `hermitEdgeGlow(color)` (`hermit_edge_glow`) | `HermitEdgeGlowParticle`, tiny additive edge spark | alpha 0.86, half-size 0.016, 12 to 18 ticks. Hairline edge light. |
| `willAbsorptionGlow(color)` and `willAbsorptionGlowPulse(color)` (`will_absorption_glow`) | `WillAbsorptionGlowParticle`, dark-render glow that travels inward along its velocity (pulse variant travels outward); the raw `WillAbsorptionGlowParticleData(r, g, b, pulse)` constructor also exists | alpha 0.86, half-size 0.105, 18 to 26 ticks. Something being drawn in or released. |
| `daemonDiffuseGlow(scale)` (`daemon_diffuse_glow`) | `DaemonDiffuseGlowParticle`, broad low-opacity dark glow that grows 35% | scale 0.02 to 1.0, 24 to 32 ticks. Void silhouettes, dark smoke, banishment. No colour argument. |
| `abocipher()` (`abocipher`) | `AbocipherParticle`, one of 26 rune glyphs that swims and writhes | half-size 0.08 to 0.12, 95 to 140 ticks, full-bright translucent. Does not home on a target. |
| `ParticleInit.lethean_drip.get()` (not in `HemoParticleData`) | `LetheanDripParticle`, pale still-water drop (vanilla drip sprite tinted (0.96, 0.96, 1.0)) | half-size 0.035 to 0.05, gravity 0.012, 18 to 30 ticks. The Unstained water drop. Worth adding a `HemoParticleData.letheanDrip()` factory in the first batch. |

Larger pieces of the toolkit that a call site can hand off to instead of spawning particles itself:

- `ManipulationParticles.accent(level, temper, at, direction)` and `impact(...)` send a `ManipulationAccentPacket`, the
  temper-coloured cast accent and impact geometry. Use it for anything that is a blood cast landing.
- `ParticleBurstPacket` kinds (`AVATAR_HIT`, `BLOOD_FLASK`, `BLOOD_CLAW`, `LIVING_TOOL_BREAK`, `MONOLITH_SHATTER`,
  `MONOLITH_SHARD_PULSE`, `POME_PULSE`) for client-side bursts a server event triggers.
- HutosLib `ColorLightningData` (`lightning_bolt` type) and `BoltParticleData` for arcs.
- `HLParticleUtils.spawnPoof(level, pos, options)` accepts any `ParticleOptions`, so its vanilla arguments can be swapped
  in place.

Colour sources: `EnumBloodTemper.X.getColor()` for anything that belongs to a temper (Animus (255, 0, 0), Flammeus
(255, 100, 0), Ductilis (255, 255, 0), Lux (255, 255, 255), Mortem (0, 58, 0), Congeatio (0, 100, 255), Ferric
(53, 53, 53), Tenebris (70, 0, 110)). The palette below reuses colours already in the code; the mod has no shared
palette class, so each batch declares the few it needs as private constants, as the codebase already does.

| Name used below | RGB | Where it already lives |
| --- | --- | --- |
| BLOOD | (150, 14, 26) | several files |
| CLOT | (70, 6, 14) | `SpawnMarkBlock` |
| CRIMSON_BRIGHT | (214, 30, 46) | several files |
| CRIMSON_SPORE | (150, 40, 58) | `FungalGillBlock`, `GardensNetwork` |
| BONE | (232, 218, 192) | `CircusEffects` |
| PALE_SPORE | (214, 196, 150) | `RenderingPoolEvents` |
| FLAME, FLAME_CORE | (238, 98, 30), (255, 196, 120) | `CircusEffects` |
| STILL_WHITE, STILL_SILVER | (244, 246, 248), (201, 209, 217) | `StillArtInit` |
| PALE_GLOW | (214, 232, 236) | `PaleConsecrationEvents` |
| FROST_FINE | (236, 246, 255) | `ChapelFocusBlockEntity` |
| VERDIGRIS | (86, 160, 132), proposed | new; Unstained only |

Rules applied below: cursors, traces and ambient emitters are fine motes (`emberSpark` at about 0.031, or `glow`) on
1/16-pixel scale and never `hitGlow`; focal moments (rite completions, boss and summon arrivals, item transformations)
are bold, layered and dense; overlays (the Circus perception stage) stay subtle and sparse; blood-side colours never
use copper, brass, bronze or silver tones (the copper, verdigris and silver colours appear only on Unstained rows).

### 1.3 Call sites and replacements, by batch

Column "Keep" marks an honest vanilla call. Lines are as read on 2026-11-11.

#### Batch A. Harbinger blocks and stations (11 files, 19 sites, 4 stay vanilla). Size M.

| File | Line | Vanilla | Moment | Replacement |
| --- | --- | --- | --- | --- |
| `common/block/harbinger/crafting/EnzymaticScriptoriumBlock` | 109 | `ENCHANT` | bookshelf power flowing into the Scriptorium (`animateTick`) | Delete the line. The `WillAbsorptionGlowParticleData(0.35, 0.015, 0.025, false)` on the next line already flies the same path inward; the glyph role it played can be added later with one `abocipher()` per tick rising from the desk, if the review wants letters. |
| `common/block/harbinger/crafting/GhastlyAlembicBlock` | 175 | `DRIPPING_LAVA` | distillate dripping from the alembic spout | `bloodCell(CLOT)` with velocity (0, -0.01, 0): a dark drop that falls a few pixels and fades. |
| `common/block/harbinger/functional/HematicSutureNodeBlock` | 61 | red `DustParticleOptions` 0.55 | ambient seep above a suture node | `emberSpark(BLOOD, 0.031F, 0.8F, 30)` rising 0.01: a fine mote, since this is an ambient trace. |
| `common/block/harbinger/rite/EngramBlock` | 87 | `SMOKE` via `spawnPoof` | the engram bites a careless hand (player hurt) | `spawnPoof(level, pos, HemoParticleData.bloodCell(BLOOD))` plus 4 `darkGlow(CLOT)`: a small blood spit, not smoke. |
| `common/block/shared/MnemonicCandleBlock` | 97 | `SMOKE` | the lit candle's wick smoke | **Keep.** It is the candle's own smoke above a flame, like a vanilla candle or campfire. |
| `common/tile/harbinger/crafting/HematicArmatureBlockEntity` | 66-67 (constant), used at 232 | red Dust 2.4 `CRIMSON_FLASH` | upgrade lands: 48 flashes around the player | Focal: 1 `hitGlow(CRIMSON_BRIGHT)` at chest height, then 24 `bloodCell(BLOOD)` with outward speed 0.06 and 12 `darkGlow(CLOT)`. |
| same | 224 | `FLAME` | upgrade: fire in the bowl | 14 `emberSpark(FLAME, 0.05F, 1F, 20)` rising 0.02 with a 0.14 spread; the bowl's fire reads as blood-lit embers. |
| same | 227 | `SMOKE` | upgrade: smoke over the bowl | 6 `darkGlow(CLOT)` rising 0.01. |
| same | 235 | `CRIMSON_SPORE` | upgrade: spores around the player | 26 `sporiticSpore(CRIMSON_SPORE)`, same spread. |
| same | 246 | `FLAME` | wind-up: bowl flicker that grows with progress | 2 `emberSpark(FLAME, 0.031F, 0.9F, 14)` with the existing `spread`. |
| same | 249 | `CRIMSON_SPORE` | wind-up: spores gathering on the player | 2 `willAbsorptionGlow(CRIMSON_SPORE)` aimed from the bowl to the player, so it reads as drawn in. |
| `common/tile/harbinger/crafting/MorphlingIncubatorBlockEntity` | 146 | `MYCELIUM` via `spawnPoof` | crafting tick puff | `spawnPoof(..., sporiticSpore(PALE_SPORE))`. |
| same | 149 | `REVERSE_PORTAL` via `spawnPoof` | crafting finishes, a Morphling is born | Focal: `spawnPoof(..., bloodCell(BLOOD))` plus 8 `serpent(CRIMSON_SPORE)` rising; this is a birth, make it wet and alive. |
| `common/tile/harbinger/crafting/MycelialCrucibleBlockEntity` | 193 | `MYCELIUM` via `spawnPoof` | crafting spore burst every 10 ticks | `spawnPoof(..., sporiticSpore(PALE_SPORE))`. |
| same | 197 | `REVERSE_PORTAL` via `spawnPoof` | crafting finishes | `spawnPoof(..., willAbsorptionGlowPulse(CRIMSON_SPORE))` plus 6 `sporiticSpore(CRIMSON_SPORE)`. |
| `client/render/tile/harbinger/crafting/MorphlingIncubatorRenderer` | 104 | `ITEM` crumbs | input stack being consumed | **Keep.** Item crumbs of the stack itself. Move the spawn out of `render` (it runs per frame, so it is frame-rate dependent) into the block entity's client tick while there. |
| same | 131 | `MYCELIUM` | drifting mote over the incubated item, spawned every frame in `renderIncubatedItem` | `emberSpark(PALE_SPORE, 0.031F, 0.6F, 30)`, and move it to the client tick at about one per 4 ticks. |
| `common/tile/harbinger/functional/SanguineMonolithBlockEntity` | 490 | `BLOCK` debris | the monolith rising out of the ground it breaks | **Keep.** Debris of the ground block. |
| `common/tile/harbinger/functional/VigilArchiveBlockEntity` | 81 | `SCULK_SOUL` | a vanilla sculk shrieker fixture fails its shriek | **Keep.** It is a vanilla shrieker using its own vocabulary. |

Review: `scriptoriumReviewClient` covers the Scriptorium and `monolithHubReviewClient` the Monolith.
`FungalStationClientReview` (gameTest, property `hemomancy.fungalStationReview`) already places a stocked Incubator,
Crucibles, a Mycelial Lantern and a Puppeteer's Spindle, but has no row in `gradle/review-clients.gradle`; add a
`fungalStationReviewClient` row. The Armature, Alembic, Suture Node and Engram need a small new station scene.

#### Batch B. Harbinger rites (3 files, 3 sites, 2 stay vanilla). Size S.

| File | Line | Vanilla | Moment | Replacement |
| --- | --- | --- | --- | --- |
| `client/render/world/CardinalFloorConsecrationRenderer` | 258 | `BLOCK` | dig dust as the floor is consecrated | **Keep.** Debris of the floor block it consecrates. |
| `client/render/world/rite/CardinalRiteOfferingDissolveRenderer` | 166 | `ITEM` | the dissolving offering shatters | **Keep.** Crumbs of the offering item. |
| `common/event/worldevent/ChamberOfWillEvents` | 210 | `REVERSE_PORTAL` | Chamber of Will theme change around its owner | 8 `willAbsorptionGlowPulse(BLOOD)` (outward), beside the 12 `bloodCell` it already sends. |

Review: `riteFloorReviewClient` for the two kept renderers. The Chamber theme change has no scene; check it inside
`lidlessReviewClient` or the Chamber Gateway flow by hand.

#### Batch C. Unstained (17 files, 49 sites, 2 stay vanilla). Size L.

Unstained colours may use silver, copper and verdigris. The Unstained still-water look is pale glows plus Lethean drops;
the verdigris look is a green-teal glow low to the ground.

| File | Line | Vanilla | Moment | Replacement |
| --- | --- | --- | --- | --- |
| `common/rite/unstained/UnstainedCardinalRiteEvents` | 293 | `END_ROD` 30 | Lethean Baptism completes on the caster | Focal: 30 `glow(STILL_WHITE)` rising 0.05 plus 20 `lethean_drip` falling from 2 blocks up. |
| same | 316 | `END_ROD` 50 | Silver Veil completes | 50 `glow(STILL_SILVER)` in a 1-block shell plus 12 `emberSpark(STILL_WHITE, 0.2F, 0.8F, 30)` flares. |
| same | 353 | `END_ROD` 80 | Clarity Ascension (blood magic leaves the caster) | 80 `glow(STILL_WHITE)` rising fast (0.08) plus 30 `darkGlow(CLOT)` falling away below the caster: the blood leaving. |
| same | 388 | `END_ROD` 120 | Closed Vein completes over its radius | 120 `glow(PALE_GLOW)` across the ring, rising 0.015. |
| same | 399 | `SCRAPE` 120 | Antiseptic Ground takes hold | 120 `emberSpark(VERDIGRIS, 0.031F, 0.9F, 40)` at ground level across the radius: a fine verdigris film. |
| same | 421 | `SNOWFLAKE` 140 | Glass Lungs completes | 140 `glow(FROST_FINE)` drifting down slowly. |
| same | 441 | `WAX_OFF` 200 | Moon-Washed Copper completes | 200 `glow(STILL_SILVER)` at night or `glow(VERDIGRIS)` by day, rising in a column. |
| same | 486 | `END_ROD` 100 | Lethean Judgment | 100 `glow(STILL_WHITE)` plus 30 `lethean_drip`. |
| same | 564 | `END_ROD` 150 | Silver Dawn | 150 `glow(STILL_SILVER)` rising 0.02 plus 1 `hitGlow(STILL_WHITE)` at the centre (focal flash, not a cursor). |
| same | 567 | `SCRAPE` 80 | Silver Dawn ground film | 80 `emberSpark(VERDIGRIS, 0.031F, 0.8F, 40)` at ground level. |
| same | 598 | `END_ROD` 60 | Still Waters | 60 `lethean_drip` plus 30 `glow(PALE_GLOW)`, very slow (0.005). |
| same | 629 | `END_ROD` 80 | Pale Consecration | 80 `glow(PALE_GLOW)`. |
| same | 632 | `SCRAPE` 40 | Pale Consecration ground film | 40 `emberSpark(VERDIGRIS, 0.031F, 0.8F, 40)`. |
| same | 685 | `END_ROD` 120 | Silthmere Remembrance | 120 `glow(STILL_WHITE)` in a tall column. |
| same | 720 | `END_ROD` 200 | Lethe Covenant | 200 `glow(STILL_WHITE)` plus 40 `lethean_drip`; this is a capstone, so bold. |
| same | 743 | `END_ROD` 120 | Lethean Tide around the caster | 120 `glow(PALE_GLOW)` over a 7-block radius. |
| same | 786 | `END_ROD` 160 | Pale Vigil | 160 `glow(STILL_SILVER)`. |
| same | 789 | `SCRAPE` 80 | Pale Vigil ground film | 80 `emberSpark(VERDIGRIS, 0.031F, 0.8F, 40)`. |
| same | 842 | `END_ROD` 300 | Lethean Font | 300 `glow(STILL_WHITE)` plus 60 `lethean_drip`, and 1 `hitGlow(STILL_WHITE)`. |
| same | 845 | `SCRAPE` 150 | Lethean Font ground film | 150 `emberSpark(VERDIGRIS, 0.031F, 0.8F, 40)`. |
| `common/block/harbinger/functional/UnstainedPodiumBlock` | 220 | `HAPPY_VILLAGER` | purity reading on the podium | 20 `glow(VERDIGRIS)` rising. |
| same | 228 | `END_ROD` | clarity reading | 30 `glow(STILL_WHITE)` rising. |
| `common/block/unstained/functional/AltarOfCleansingBlock` | 341 | `END_ROD` 40 | blessing at the altar | 40 `glow(STILL_WHITE)`. |
| same | 344 | `HAPPY_VILLAGER` 20 | blessing, second layer | 20 `lethean_drip` from 1.5 blocks up. |
| same | 352 | `HAPPY_VILLAGER` 15 | purity reading | 15 `glow(VERDIGRIS)`. |
| same | 360 | `END_ROD` 20 | clarity reading | 20 `glow(STILL_WHITE)`. |
| `common/block/unstained/functional/PaleSilverBellsBlock` | 233 | `END_ROD` 32 | the bells are rung | 32 `glow(STILL_SILVER)` in an outward ring (horizontal speed 0.03). |
| same | 236 | `HAPPY_VILLAGER` 16 | ringing, second layer | 16 `emberSpark(STILL_WHITE, 0.031F, 1F, 24)`. |
| `common/block/unstained/crafting/PallidRetortBlock` | 119 | `DRIPPING_LAVA` | retort drip | `lethean_drip` at the spout. |
| same | 127 | `END_ROD` | retort vapour | `glow(PALE_GLOW)` rising 0.015. |
| `common/block/unstained/crafting/StillwaterCondenserBlock` | 85 | `END_ROD` | condenser surface mote | `emberSpark(STILL_WHITE, 0.031F, 0.8F, 30)` rising 0.008. |
| `common/block/unstained/crafting/VerdigrisLatticeBlock` | 80 | `WAX_OFF` | motes orbiting the lattice | `emberSpark(VERDIGRIS, 0.031F, 0.9F, 30)` with the same tangential velocity. |
| same | 85 | `END_ROD` | crown mote | `glow(STILL_WHITE)`. |
| `common/block/unstained/ViridSalisTrailBlock` | 412 | Dust in the trail's power colour | powered trail trace, redstone-dust style | `emberSpark(new ParticleColor(rgb), 0.031F, 0.9F, 20)` at the same point: a trace, so a fine mote. |
| `common/effect/SilverWardEffect` | 33 | `END_ROD` | ward shimmer on the entity | `glow(STILL_SILVER)`, same count. |
| `common/effect/VerdigrisAuraEffect` | 53 | `SCRAPE` | aura mote circling the entity | `emberSpark(VERDIGRIS, 0.031F, 0.9F, 24)`. |
| `common/entity/mob/animal/VerdigrisMothEntity` | 104 | `END_ROD` | ambient wing mote | `emberSpark(VERDIGRIS, 0.031F, 0.7F, 30)`. |
| same | 148 | `SCRAPE` 8 | the moth sheds Virid Salis | 8 `emberSpark(VERDIGRIS, 0.04F, 0.9F, 24)` falling. |
| same | 151 | `END_ROD` 3 | shed, second layer | 3 `glow(STILL_WHITE)`. |
| `common/event/ConsecrationHandler` | 109 | `SCRAPE` 12 | a block is consecrated | 12 `emberSpark(VERDIGRIS, 0.04F, 0.9F, 24)` over the block. |
| same | 112 | `END_ROD` 5 | consecration, second layer | 5 `glow(STILL_WHITE)`. |
| `common/event/CopperBrushingHandler` | 103 | `SCRAPE` | a copper block is brushed clean | **Keep.** `SCRAPE` is vanilla's own copper-scraping particle on a vanilla copper block. |
| `common/event/ViridSalisTrailHandler` | 98 | `SCRAPE` | a mob stepping on the trail is hurt | 4 `emberSpark(VERDIGRIS, 0.031F, 0.9F, 20)` at its feet. |
| `common/event/WhiteHumorPurificationEvents` | 96 | `END_ROD` | white humour working on a dropped item | `glow(STILL_WHITE)`, same count. |
| same | 131 | `POOF` 16 | the item transforms | Focal: 16 `glow(STILL_WHITE)` outward plus 8 `lethean_drip`. |
| same | 134 | `END_ROD` 12 | transform, second layer | 12 `emberSpark(STILL_SILVER, 0.2F, 0.8F, 20)` flares. |
| same | 243 | `SCRAPE` | a copper absorber block tarnishes | **Keep.** Copper surface change on a copper block, vanilla's own particle for it. |
| `common/item/unstained/LetheanChaliceItem` | 66 | `SPLASH` | drinking from the chalice | 24 `lethean_drip` spreading from the mouth. |
| `common/item/unstained/PaleSilverBellItem` | 64 | `END_ROD` 36 | the hand bell rung | 36 `glow(STILL_SILVER)` in an outward ring. |

Review: no review client stages Unstained rites, stations or ecology. Write one (`unstainedParticleReviewClient`) that
places the Altar, Podium, Bells, Retort, Condenser, Lattice and a Virid Salis trail, and fires one rite completion of
each visual family (Baptism, Silver Dawn, Glass Lungs, Moon-Washed Copper). Unstained rites are slated for their own
ritual system later, so this batch only swaps particles; it does not redesign the rite visuals.

#### Batch D1. Mobs, NPCs and bosses (17 files, 29 sites, 8 stay vanilla). Size M.

| File | Line | Vanilla | Moment | Replacement |
| --- | --- | --- | --- | --- |
| `common/entity/boss/endgame/VesperPhaseTwoCombat` | 803 | `BLOCK` | axe slam debris | **Keep.** Debris of the ground block. |
| `common/entity/mob/animal/ChoirKeeperEntity` | 232 | `ITEM` chorus fruit | planting chorus fruit | **Keep.** Crumbs of a vanilla item. |
| same | 263 | `PORTAL` 14 | it swallows an Eye of Ender and coughs up a pearl | **Keep.** Ender vocabulary for a vanilla ender item. |
| same | 265 | `END_ROD` 5 | same moment, sparkle | 5 `emberSpark(BONE, 0.031F, 1F, 20)`. |
| `common/entity/mob/animal/HematicBurrowerEntity` | 99 | `BLOCK` | digging | **Keep.** Ground debris. |
| `common/entity/mob/animal/LuminalCicadaEntity` | 127 | `FLASH` | defensive flash | 1 `hitGlow(new ParticleColor(255, 240, 210))`: a real focal flash. |
| same | 129 | `END_ROD` 18 | flash afterglow | 18 `emberSpark(same colour, 0.031F, 1F, 24)` spreading. |
| `common/entity/mob/animal/OsteophageEntity` | 209 | `ITEM` bone | gnawing a bone | **Keep.** Crumbs of a vanilla item. |
| `common/entity/mob/animal/PeacockSpiderEntity` | 25-28, used at 68 | cyan and magenta Dust | courtship fan display, 9 dots in an arc | `glow(new ParticleColor(15, 184, 209))` and `glow(new ParticleColor(224, 31, 140))`: fine motes in the same arc. |
| `common/entity/mob/aquatic/BloodLanternJellyEntity` | 79 | `CRIMSON_SPORE` | ambient glow from the bell | `emberSpark(CRIMSON_BRIGHT, 0.031F, 0.8F, 30)` rising 0.01. |
| `common/entity/mob/aquatic/ChalybeateSnailEntity` | 164 | `SCRAPE` 10 | sclerites harvested off the shell | 10 `emberSpark(new ParticleColor(120, 118, 124), 0.031F, 1F, 16)`: grey iron sparks, no copper. |
| same | 167 | `CRIMSON_SPORE` 5 | harvest, blood | 5 `bloodCell(BLOOD)`. |
| `common/entity/mob/aquatic/PrismCuttleEntity` | 182, 184 | magenta and cyan Dust 1.2 | colour flash | 18 `emberSpark(magenta, 0.06F, 1F, 20)` and 18 `emberSpark(cyan, 0.06F, 1F, 20)`; a display, bolder than a trace. |
| `common/entity/mob/arthropod/ChewWoodGoal` | 261 | `BLOCK` | chewing wood | **Keep.** Debris of the chewed block. |
| `common/entity/mob/arthropod/MyelinBorerWorkEffects` | 24 | `BLOCK` | boring into a block | **Keep.** Already paired with a `glow`. |
| `common/entity/mob/monster/MortarboundEntity` | 145, 151 | `SQUID_INK` | emerging or retreating, dark seep | `darkGlow(CLOT)` falling 0.015. |
| same | 146, 149 | `SCULK_SOUL` | soul wisp rising from the body | `daemonDiffuseGlow(0.18F)` rising 0.01. |
| `common/entity/mob/monster/will/WillEntity` | 557 | `POOF` 24 | a projected Will is banished | Focal: 24 `willAbsorptionGlowPulse(BLOOD)` over the body. |
| same | 559 | `CRIMSON_SPORE` 18 | banishment, second layer | 18 `bloodCell(BLOOD)` scattering. |
| `common/entity/npc/DrudgeEntity` | 455 | red Dust | drudge bursts on death | 12 `bloodCell(BLOOD)` with the same velocities. |
| `common/entity/npc/circus/CircusStiltWalkerEntity` | 108 | `BLOCK` | stilt strike on the ground | **Keep.** Ground debris. |
| `common/mission/circus/CircusDemonstrations` | 60 | `FLAME` | Cinder Bellows fire-breath demonstration | 4 `emberSpark(CircusEffects.FLAME, 0.06F, 1F, 16)` plus 1 `emberSpark(CircusEffects.FLAME_CORE, 0.1F, 1F, 10)`; or call the existing `CinderBreathFlames` look through a public bridge if one exists. |
| `client/screen/overlay/CircusPerceptionOverlay` | 57 | `CRIMSON_SPORE` | Disturbed stage ambient | Overlay, subtle: `sporiticSpore(CRIMSON_SPORE)` at the same interval. |
| same | 58 | `REVERSE_PORTAL` | Acclimating stage, drifting toward the player | `willAbsorptionGlow(BONE)` with the same inward velocity. |
| same | 59 | `END_ROD` | Attuned stage, rising | `emberSpark(BONE, 0.031F, 0.7F, 30)` rising 0.025. |

Review: `vesperReviewClient` (kept debris), `circusReviewClient` (stilt walker, demonstrations, perception overlay),
`pelagicReviewClient` with `-PpelagicEcologyReview=true` for the jelly, snail and cuttle and `-PosteophageReview=true`
for the Osteophage. The Choir Keeper, Cicada, Peacock Spider, Burrower, Mortarbound, Will and Drudge have no scene;
write one ecology scene or check them by hand.

#### Batch D2. Summons and projectiles (10 files, 32 sites, 3 stay vanilla). Size M.

| File | Line | Vanilla | Moment | Replacement |
| --- | --- | --- | --- | --- |
| `common/entity/summon/EntityIronPillar` | 92 | `SQUID_INK` | pillar erupts (first 2 ticks) | Focal: `darkGlow(CLOT)` plus `emberSpark(EnumBloodTemper.FERRIC.getColor() lightened to (120, 118, 124), 0.06F, 1F, 16)` sparks. |
| same | 97 | `REDSTONE` Dust | blood seep at the base while it stands | `bloodCell(BLOOD)`. |
| same | 99 | `ASH` | iron flakes rising | `emberSpark((120, 118, 124), 0.031F, 0.8F, 20)`. |
| same | 104 | `SQUID_INK` | lifetime ends | `darkGlow(CLOT)` puff. |
| same | 197 | `SQUID_INK` | death dissolve | `darkGlow(CLOT)` puff plus 6 `bloodCell(BLOOD)`. |
| `common/entity/summon/EntityIronSpike` | 58, 64, 66, 71, 96 | same five as the Pillar | same moments | Same replacements. Fold the three Ferric constructs onto one shared helper (`FerricConstructEffects`) so the five calls live once, not three times. |
| `common/entity/summon/EntityIronWall` | 59, 64, 66, 71, 90 | same five | same moments | Same. |
| `common/entity/summon/PhantasmalEchoEntity` | 120 | `REVERSE_PORTAL` | ambient shimmer on the echo | `darkGlow(EnumBloodTemper.TENEBRIS.getColor())`. |
| same | 129 | `SMOKE` | ambient wisp | `daemonDiffuseGlow(0.12F)` rising 0.01. |
| same | 260 | `SMOKE` 80 | the echo bursts into a smoke cloud | Focal: 40 `daemonDiffuseGlow(0.6F)` plus 30 `darkGlow(TENEBRIS)` outward. |
| same | 262 | `REVERSE_PORTAL` 48 | burst, second layer | 24 `willAbsorptionGlowPulse(TENEBRIS)`. |
| same | 278 | `REVERSE_PORTAL` 24 | quiet dissolve | 24 `willAbsorptionGlow(TENEBRIS)` (inward, a quiet fold). |
| `common/entity/summon/BloodThrallEntity` | 209 | red Dust sized by carry | blood the thrall carries, dripping | `bloodCell(BLOOD)` falling 0.01. |
| same | 349 | red Dust 1.2 | death splatter | 20 `bloodCell(BLOOD)` plus 6 `darkGlow(CLOT)`. |
| `common/entity/projectile/DirectedBloodOrbEntity` | 109 | `ASH` | orb trail | Trace: `emberSpark(CLOT, 0.031F, 0.8F, 14)`. |
| same | 111 | `REDSTONE` | orb trail | `bloodCell(BLOOD)` (the `glow` above already gives the bright core). |
| `common/entity/projectile/HemolyticVialEntity` | 105 | `SPLASH` 70 | vial shatters | 70 `lethean_drip` in the existing spherical burst, or `bloodCell((200, 235, 255))` if the vial is not meant to read as Lethean. |
| same | 106 | `SNOWFLAKE` 40 | shatter, second layer | 40 `emberSpark(FROST_FINE, 0.031F, 1F, 20)`. |
| `common/entity/projectile/BloodBoltEntity` | 182, 322 | `ENTITY_EFFECT` coloured by the potion | a tipped bolt's potion swirl | **Keep.** It tells the player which vanilla potion the bolt carries, the same cue tipped arrows give. |
| `common/entity/item/EntityFlyingCharm` | 193 | `BUBBLE` | charm travelling under water | **Keep.** Water. |
| `common/entity/item/EntityMorphlingPolypItem` | 49 | `REVERSE_PORTAL` | ambient shimmer on the dropped polyp | `emberSpark(CRIMSON_SPORE, 0.031F, 0.7F, 30)`. |
| same | 90 | `POOF` | polyp consumed | 6 `sporiticSpore(CRIMSON_SPORE)`. |
| same | 92 | `SMOKE` | consumed, second layer | 4 `darkGlow(CLOT)`. |

Review: no client stages Ferric constructs, the Phantasmal Echo or the Thrall. `outpouringReviewClient` drives casts and
may reach the Ferric casts through its casting scenes; check it first, otherwise write a summon scene.
`morphlingStrainReviewClient` covers the polyp.

#### Batch E. Items, armour abilities, effects and events (15 files, 22 sites, 2 dead). Size M.

| File | Line | Vanilla | Moment | Replacement |
| --- | --- | --- | --- | --- |
| `client/event/LayerEvents` | 441 | `SOUL` tracking emitter | the Curved Horn breaks (totem-style activation) | `createTrackingEmitter(player, HemoParticleData.bloodCell(BLOOD), 30)`; the emitter accepts any `ParticleOptions`. |
| `client/render/item/SporiticThuribleRenderHelper` | 118 | `FLAME` | burning spore inside the swinging thurible | `emberSpark((214, 112, 28), 0.031F, 1F, 12)` rising 0.004: a fine ember, the thurible's coal. |
| `common/armor/ability/EdaciousBloodburstArmorAbilityHandler` | 39 | `CRIMSON_SPORE` 80 | Bloodburst fires its needles | Focal: 40 `bloodCell(BLOOD)` outward plus 1 `hitGlow(CRIMSON_BRIGHT)`. |
| `common/armor/ability/PhanstmalBloodlustArmorAbilityHandler` | 43 | `REVERSE_PORTAL` 90 | Masquerade of the Forgotten | 40 `willAbsorptionGlowPulse(TENEBRIS)`. |
| same | 46 | `SMOKE` 45 | masquerade smoke | 20 `daemonDiffuseGlow(0.5F)`. |
| same | 87 | `REVERSE_PORTAL` 32 | Distorted Presence blinks the wearer | 16 `willAbsorptionGlow(TENEBRIS)` at the attacker. |
| `common/armor/ability/SheolicBastionBloodlustArmorAbilityHandler` | 164 | `CRIMSON_SPORE` 28 | Crimson Retribution sets the attacker alight | 20 `emberSpark(EnumBloodTemper.FLAMMEUS.getColor(), 0.05F, 1F, 20)` plus 8 `bloodCell(BLOOD)`. |
| `common/armor/ability/SilentArchonArmorAbilityHandler` | 283 | `SMOKE` 12 every 5 ticks | physical damage negated | 6 `daemonDiffuseGlow(0.25F)`; keep it light, it repeats. |
| `common/effect/ElytraEffect` | 81 | `MYCELIUM` | wind streaming past the flying player | `emberSpark(PALE_SPORE, 0.031F, 0.6F, 14)` with the same velocity: a trace. |
| `common/effect/NeuralOverloadEffect` | 51 | `ELECTRIC_SPARK` | overload crackle | `hermitEdgeGlow((236, 214, 255))` at the same count; for a stronger read, one short `ColorLightningData` arc every second. |
| `common/event/HematicSalvageEvents` | 109 | `DAMAGE_INDICATOR` | scrap breaks off damaged iron armour | 4 `emberSpark((120, 118, 124), 0.031F, 1F, 16)` iron sparks. |
| `common/event/MorphicNectarEvents` | 103 | `WITCH` | nectar working on a dropped Morphling | `sporiticSpore(CRIMSON_SPORE)`, same count. |
| same | 116 | `TOTEM_OF_UNDYING` | the Morphling mutates | Focal: 1 `hitGlow(CRIMSON_BRIGHT)` plus 16 `serpent(CRIMSON_SPORE)` outward. |
| same | 160 | `MYCELIUM` | primalizing in progress | `sporiticSpore(PALE_SPORE)`. |
| same | 169 | `TOTEM_OF_UNDYING` | primalized | Focal: as line 116, plus 12 `bloodCell(BLOOD)`. |
| `common/event/PossessionEvents` | 115 | `TOTEM_OF_UNDYING` | a possession totem is spent when a hit breaks it | Focal: 1 `hitGlow(BONE)` plus 20 `bloodCell(BLOOD)` and 10 `darkGlow(CLOT)`. |
| same | 128 | `TOTEM_OF_UNDYING` | totem spent against the Abhorrent Gaze grab | Same as line 115. |
| `common/init/BiomeInit` | 109 | `CRIMSON_SPORE` ambient biome particle | ambient air of one Hemomancy biome | `sporiticSpore(CRIMSON_SPORE)` through `AmbientParticleSettings`. Check first that the biome is built by code at runtime and not serialized to datapack JSON through the particle codec; `HemoParticleType` carries codecs, so it should serialize. Low priority. |
| `common/item/harbinger/armor/AbstractSpecialBloodLustArmorItem` | 46 | `REDSTONE` in a comment | none (commented out) | Delete the commented block. |
| `common/item/harbinger/armor/BloodLustArmorItem` | 60 | `REDSTONE` in a comment | none | Delete the commented block. |
| `common/item/harbinger/tool/living/LivingSicklePruning` | 61 | `SQUID_INK` 30 | the Living Sickle cuts a Qliphoth trunk | Focal: 20 `darkGlow(CLOT)` plus 20 `bloodCell(BLOOD)` sprayed up the trunk. |
| same | 63 | `CRIMSON_SPORE` 45 | trunk cut, spores | 30 `sporiticSpore(CRIMSON_SPORE)`. |

Review: `possessionReviewClient` (totems), `sickleStrokeReviewClient` (pruning), `morphlingStrainReviewClient` (nectar).
Armour abilities, the horn, the thurible and the effects have no scene; check them in a dev client.

### 1.4 Batch summary

| Batch | Files | Sites | Stay vanilla | Size | Review client |
| --- | --- | --- | --- | --- | --- |
| A. Harbinger blocks and stations | 11 | 19 | 4 | M | `scriptoriumReviewClient`, `monolithHubReviewClient`; add a row for the existing `FungalStationClientReview`; a station scene for Armature, Alembic, Suture Node, Engram must be written |
| B. Harbinger rites | 3 | 3 | 2 | S | `riteFloorReviewClient` |
| C. Unstained | 17 | 49 | 2 | L | none; must be written |
| D1. Mobs, NPCs, bosses | 17 | 29 | 8 | M | `vesperReviewClient`, `circusReviewClient`, `pelagicReviewClient` (ecology and osteophage flags); others by hand |
| D2. Summons and projectiles | 10 | 32 | 3 | M | `morphlingStrainReviewClient`; check `outpouringReviewClient` for Ferric casts; otherwise write a summon scene |
| E. Items, armour abilities, effects, events | 15 | 22 | 0 (2 dead) | M | `possessionReviewClient`, `sickleStrokeReviewClient`, `morphlingStrainReviewClient`; the rest by hand |
| Total | 73 | 154 | 19 | | |

After the sweep the allow-list keeps 17 files (19 honest sites plus their imports), each with an honest reason:
`CardinalFloorConsecrationRenderer`, `CardinalRiteOfferingDissolveRenderer`, `MorphlingIncubatorRenderer` (item crumbs),
`MnemonicCandleBlock` (its own smoke), `SanguineMonolithBlockEntity`, `VigilArchiveBlockEntity` (vanilla shrieker),
`CopperBrushingHandler` and `WhiteHumorPurificationEvents` (vanilla copper scrape), `VesperPhaseTwoCombat`,
`ChoirKeeperEntity` (chorus crumbs, ender eye), `HematicBurrowerEntity`, `OsteophageEntity`, `ChewWoodGoal`,
`MyelinBorerWorkEffects`, `CircusStiltWalkerEntity`, `BloodBoltEntity` (potion cue), `EntityFlyingCharm` (bubbles).
`ChoirKeeperEntity` and `BloodBoltEntity` keep two sites each; the exact counts, imports included, come out of the
edit.

Execution notes:
- Each batch should lower the counts in the allow-list in the same change, or `VanillaParticleRatchetTest` fails.
- Server-side calls already use `sendParticles` with `HemoParticleData` elsewhere, so no packet work is needed. A
  `sendParticles` speed argument is applied to every particle's random velocity; `ParticleGlow` doubles velocity again,
  so halve the speed figure when porting a vanilla call that looked right.
- `sporiticSpore` is about ten times the area of `CRIMSON_SPORE`. Halve counts when swapping one for the other, or the
  replacement will cloud the scene the owner asked to keep subtle.
- None of this changes payloads, so `PacketHandler.PROTOCOL_VERSION` stays.

---

## Part 2. Texel density

### 2.1 Method

The rule is 16 texels per block as drawn, after renderer scale. The four guide-book models are the 32 px exception and
are not listed.

- **Java models.** For every `LayerDefinition.create(mesh, W, H)` (217 layer methods), the script parsed every
  `addBox` with its `CubeDeformation` and computed the area-weighted ratio of UV area to drawn area. Box UV gives one
  texture unit per model unit, so density in the model is 16 times (PNG width over W) times that deformation factor.
  The texture was taken from the renderer that binds it, not from name matching, and confirmed by hand for every
  model below. Renderer `poseStack.scale` constants were resolved (for example `HemorathVisualRules.RENDER_SCALE`
  2.0, `PutricielVisualRules.SCALE` 2.0, `SeraphaeVisualRules.MODEL_SCALE` 2.0) and divided out.
- **JSON models.** All 1,895 block and item models under `assets/hemomancy/models`, with parents resolved through the
  NeoForge 21.1.251 resource jar. For each face, density is the square root of (UV span in px, both axes) over (face
  size in blocks), area-weighted across faces, with the real width and height of each texture (non-square textures
  and `.mcmeta` animation frames handled). A second "ideal" figure gives what the same geometry would read at exactly
  one texel per unit, with the one-texel minimum per face; a model is flagged when its measured figure is more than
  1.25 times its ideal, or under 12. This separates real over-resolution from the unavoidable inflation of sub-unit
  faces (detailed blocks read 17 to 19 ideal).
- **OBJ.** The five Lowtide Ruin OBJ pieces measure exactly 16. The six `neoforge:composite` OBJ models
  (`broken_mana_trapazahedron*`, `faction_horn_harbingers`, `grimoire_harbinger_*`) point at `item/mna/...` and
  `block/mna/...` textures that no longer exist; they are Mana and Artifice leftovers and look dead.
- **Item GUI shrink is not flagged.** A multi-block model drawn small in an inventory slot (Sanguine Monolith item at
  0.2, Covenant Throne item at 0.185, Puppeteer's Spindle, Fungal Implantation Pylon, Earthen Vein and Mason's Effigy
  items) is a fitting choice, not a density defect. Held, worn and in-world drawing is what is measured.

The old scripts `build/density_audit.py` and `density_audit2.py` were not reused; their name matching was the noise
the note warned about. The measuring scripts for this audit lived in a temp folder and are not committed.

### 2.2 Fixed since the last audit

- **Velorum** is now 16: `VelorumModel` 256x256 with `velorum.png` 256x256, drawn at 1. (It was 512 px on 2026-11-02.)
- The 2.83x and 1.41x figures a naive square-texture measurement gives for the fixture-atlas blocks (Mnemonic Candle,
  Reliquary Sconce, Witness Stone, Ring Door, Frozen Devotee, Saint Sarcophagus block and others) come from their
  non-square atlases. Measured with real widths and heights, they sit at 16 to 19. No action.
- The Humoral Barometer reads 24 because its dial faces are authored at `density=2` on purpose (fixture_atlas: "dials
  and other read-at-a-glance faces"). Intended; not listed.

### 2.3 Models off 16 by more than about 25 percent

Sorted by how often a player sees them: worn and held first, then mobs and blocks met in normal play, then rarer world
pieces, then item-only views. "As drawn" is texels per block after renderer scale; for held items it is texels per
item-space block, which is what a vanilla 16 px item uses. "Model" is the figure before renderer scale.

| # | Model | As drawn (model) | Texture | Generator | Fix |
| --- | --- | --- | --- | --- | --- |
| 1 | Living Staff head variants, item and planted models: Leech, Pests, Fungal, Serpent (`models/item/living_staff_*`) | 150-158, 128-130, 109-116, 109-114 | 16 px block textures (`venous_stone`, `chiseled_hematic_iron_block`, `leech_mouth`, `pest_wing`, `infected_cap`, `snake_head`) mapped whole onto tiny cubes | none (hand kitbash) | Rescale the UV layout: drop the explicit full-face `uv` arrays so each face takes its position-derived UV, which is exactly 16 per block on a tiling block texture. Mechanical, one script across the files. |
| 2 | Living Staff fitting variants (Barbed, Chitinite, Prismatic, Worn Vow, Assumed Limb, Crimson Vestment, Monolithic Frame) | 23-28 (ideal 17-18) | 32x32 fitting sprites | `tools/model_export/staff_fitting_sprites.py` | Regenerate the fitting sprites at 16x16 (density 1). |
| 3 | Living Blade hand (tame and unleashed) | 64 (32) | `entity/model_living_blade_hand.png` 128x128 on a 64x64 layout; `LivingBladeItemRenderer` scale 0.5 | not in repo | Repaint at 64x64 (model 16), then bake the 0.5 into the mesh or accept 32 in item space; drawing at 0.5 doubles any texture. |
| 4 | Living Axe hand | 49 (32) | `model_living_axe_hand.png` 128x128 on 64x64; renderer 0.65 | not in repo | As row 3. |
| 5 | Living Spear hand | 43-49 (32) | `model_living_spear_hand.png` 128x128 on 64x64; renderer 0.65 and 0.75 | not in repo | As row 3. |
| 6 | Kitbashed held weapons: Living Baghnakh, Barbed Blade, Living Crossbow (plus arrow, three pulling states, firework), Chitinite Mace, Living Blaster, Living Rifle | 212, 189, 145-154, 92 (block-folder copy 180), 110, 99 | full 16 px block textures (`hematic_iron_block`, `hematic_iron_blade`, `blood_wood_log`, `bold_chiseled_hematic_iron_block`, `sanguine_pane`, chitinite tones) on small cubes | none | Same UV rescale as row 1. The Baghnakh also carries a gui scale of 1.5, so it draws bigger than other items in a slot. |
| 7 | Fungal (Noctifly Agaric) and Pontiff elytras | 32 | `models/armor/noctifly_agaric.png` and `pontiff_elytra.png` 128x64 on vanilla `ElytraModel` 64x32 | not in repo | Downsample and clean up to 64x32. |
| 8 | Covenant Mantle armour | 32 | `covenant_mantle_layer_1/2.png` 256x128 on `CovenantLeaderArmorModel` 128x64 | not in repo | Repaint at 128x64. |
| 9 | Chalybeate Sclerite armour and the Venous Strider sabatons (share `ChalybeateFortressArmorModel`) | 32 | `chalybeate_sclerite_layer_1/2.png` and `venous_strider_layer_1.png` 256x128 on 128x64 | not in repo | Repaint at 128x64. The Chalybeate set was not on the old list. |
| 10 | Unstained weapons: Silthmere Glaive, Unstained Warhammer, Absolution Dagger | 41-62, 35-53, 26-42 (16) | 64x64, 64x128, 64x32, each 1 texel per unit | `tools/model_export/unstained_weapons.py` | The meshes are at density 1 but the item renderers draw them at 0.26 to 0.62. Bake the renderer scale into the generator's mesh (author at the drawn size) and set the renderers back near 1. |
| 11 | Living Syringe and the vial rack (chamber, wrist, vials, rack, all fill states) | 28-42 (ideal 17-21) | `living_syringe_materials.png` and `living_syringe_filled.png` 16x16 | `tools/model_export/living_syringe.py` | Regenerate with face-sized UVs at density 1; the generator currently maps more of the 16 px sheet onto each small face than its size. |
| 12 | Hemolymphopoda headpiece worn on the head | 40 (16) | `hemolymphopoda/model_hemolymphopoda.png` 128x128; `HemolymphopodaHeadpieceLayer` scale 0.4 | not in repo | The whole mob model is worn at 0.4. Give the headpiece its own small model at density 1, or accept it as a creature worn as a hat. |
| 13 | Lantern Tick helmet worn on the head | 29 (16) | `lantern_tick/model_lantern_tick.png` 64x64; `LanternTickHelmetLayer` 0.56 | not in repo | As row 12. |
| 14 | Sporitic Thurible (worn on the belt, held) | 22-36 (17) | `sporitic_thurible` 64x64; layer 0.78, held and gui 0.48 to 0.64 | not in repo | Bake scale into the mesh, or accept the worn 22. |
| 15 | Sanguis Lancea and Annetta's Sanguis Lancea | 21-40 held, 32 thrown (16) | 64x64; item renderer 0.4 to 0.75, thrown `SanguisLanceaRenderer` 0.5 | not in repo | Bake scale into the mesh. |
| 16 | Marionette Crossbar | 26-32 (16) | `marionette_crossbar_model` 64x64; renderer 0.5 to 0.62 | `tools/circus/build_marionette_crossbar.py` | Regenerate with the mesh at drawn size. |
| 17 | Living Flail | 23-25 (17.5) | `model_living_flail` 64x64; renderer 0.7 to 0.76 | not in repo | Minor; bake scale or accept. |
| 18 | Barbed Shield | 32 | `barbed_shield/model_barbed_shield.png` 128x128 on `BarbedShieldModel` 64x64 | not in repo | Repaint at 64x64. |
| 19 | Silent Archon leggings | 16 down, 32 across (aspect mismatch) | `silent_archon_layer_2.png` 256x128 on `SilentArchonArmorModel` 128x128 | not in repo | Layer 2 does not match the layout's aspect, so the UVs stretch. Confirm in a client which half is drawn, then repaint at 128x128 to the model's layout. |
| 20 | Cardinal Focus heart (`FloatingHeartModel` in `CardinalFocusRenderer`, seen in every Harbinger rite) | 33 (16) | `model_floating_heart` 64x64; renderer 0.48 | not in repo | Bake 0.48 into the mesh or paint at 32x32 with `texScale`. |
| 21 | Choir Keeper | 64 (16) | `entity/choir_keeper_java.png` 1024x512 on a 1024x512 layout; `ChoirKeeperRenderer` 0.25 | not in repo | The mob is authored at four times its size and drawn at a quarter. Bake 0.25 into the geometry and repaint at 256x128. |
| 22 | Mnemonic Whale | 8 | `mnemonic_whale/model_mnemonic_whale.png` 64x64 on 128x128 | not in repo (only `java_model_to_bbmodel.test.mjs` reads it) | Repaint at 128x128. |
| 23 | Ferric Rampart (Iron Wall summon) | 32 | `ferric_rampart/model_ferric_rampart.png` 256x256 on `IronWallModel` 128x128 | not in repo | Repaint at 128x128. |
| 24 | Morphling Polyp | 32 | `morphling_polyp/model_morphling_polyp.png` 256x256 on `MorphlingPolypModel` 128x128 (its layer texture is already 128) | not in repo | Repaint at 128x128. |
| 25 | Mason's Effigy (block entity in the world) | 29 (16) | `model_masons_effigy` 128x128; `MasonsEffigyRenderer` 0.55 | not in repo | Bake 0.55 into the mesh. |
| 26 | Blood Crystal, stages 1 to 3 and the item | 10.4, 8.3, 7.6 | `venous_stone`, `sanguine_tran_pane` 16x16 stretched over large faces | none | Rescale UVs to face size (position-derived UV). |
| 27 | Puffball Fungus, block and item | 4.0 | `dendritic_stem_2` 16x16 stretched across a 4-block-scale face | none | Rescale UVs; a full stem texture is spread over four times its size. |
| 28 | Hematic Microscope item, and its viewing model | 10.8, 44.5 | `blood_wood_log`, `blood_wood_log_top`, `blood_wood_planks` | `tools/oneoff/hematic_microscope.py` exists locally but is not in the repo | Rescale UVs to face size in both models. |
| 29 | Covenant Throne (block entity) | 8 | binds `entity/model_sanguine_monolith.png` 64x64 on `CovenantThroneModel` 128x128 | not in repo | It borrows the Monolith texture at half the layout's size, which also misplaces its UVs. Paint its own 128x128 texture. |
| 30 | Ghastly Alembic upgrade growths: Athanor and Condenser | 138, 69 | `hematic_iron_block`, `glass`, `model_ghastly_alembic` 128 on small cubes | not in repo (`ghastly_alembic.py` does not emit these two) | Rescale UVs to face size. |
| 31 | Podium family: Rune Mod Station, Scrying Podium, Unstained Podium, Fungal Podium (block and item) | 43.5, 39.2, 35.7, 31.4 | 16 px block textures (`hematic_iron_block`, `podium_top`, `quartz_bricks`, `hematic_iron_pillar`) on small parts | none | Rescale UVs to face size. |
| 32 | Phlebotomist's Cabinet specimens 0 to 17 (worldgen Harbinger outpost) | about 1,048 | `blood_fill_tiled.png` 256x256 mapped whole onto tiny faces | placed by `tools/worldgen/harbinger_sites/outpost.py`; model not generated | Point the fill faces at a 16 px fluid texture, or shrink their UV spans to face size over 16. |
| 33 | Phlebotomist's Field Case item | 114 | `blood_fill_tiled` 256, `black_wool`, `brown_terracotta` | none | As row 32. |
| 34 | Hydra Crown, block and item | 34.3 (ideal 17) | `hydra_crown.png` 128x128 | `tools/model_export/hydra_crown.py` | Regenerate at density 1. |
| 35 | Vacant Effigy, stages 0 to 5 and item | 28.6-29.1 (ideal 16) | `bog_flesh`, `bog_flesh_ripe`, `hematic_iron_block` 16x16 | none | Rescale UVs to face size. |
| 36 | Escharian Scyphus 1 to 4 | 32 (ideal 18) | `escharian_overgrowth_center` and `_rim` 16x16 | none | Rescale UVs to face size. |
| 37 | Wax, Ambergris and Corpse Wax cylinders (plain and recorded) | 25-27 (ideal 17-18) | 16x16 cylinder textures | `tools/model_export/cylinders.py` | Regenerate with face-sized UVs. |
| 38 | Resonant Forge wax cylinder (in the forge) | 10.5 | `wax_cylinder`, `hematic_iron_block` | none found | Rescale UVs; it stretches the wax texture. |
| 39 | Vial Centrifuge flywheel | 27.3 (ideal 18.5) | `bold_chiseled_hematic_iron_block`, `bone_block_side/top` | `tools/model_export/vial_centrifuge.py` | Regenerate the flywheel with face-sized UVs. |
| 40 | Mortal Display | 22.6 (ideal 16) | 16 px block textures | none | Rescale UVs. Borderline. |
| 41 | Rite Fragment Inscription | 21.3 (ideal 16) | `rite_hint` 16x16 | none | Rescale UVs. Borderline. |
| 42 | Semi-Sentient Construct | 20.9 (ideal 16.3) | `conscious_mass`, `hematic_iron_block`, `jar_bottom_side` | none | Borderline; fix with the podium batch. |
| 43 | Terrestrial Speculum item | 24.7 (ideal 18.3) | `terrestrial_speculum_materials.png` 8x8 | `tools/model_export/terrestrial_speculum.py` | Regenerate with face-sized UVs. |
| 44 | Saint Sarcophagus items (plain, Hemorath, Putriciel, Seraphae, Velorum) | 33 (block 16.5) | `saint_sarcophagus*.png` 128x256 | `tools/model_export/saint_sarcophagus.py` | The item model is the block at half size; draw the block model with a smaller display scale instead. Item only. |
| 45 | Pulse Pylon item | 34.1 (ideal 19) | `pulse_pylon.png` 128x64 | `tools/model_export/pulse_pylon.py` | As row 44. Item only. |
| 46 | Blood Gourd and Open Blood Gourd held item | 10 (20) | `blood_gourd/*.png` 32x32; `BloodGourdItemRenderer` scale 2.0 (worn on the belt it is 20, fine) | not in repo | Drop the item renderer's 2.0 or repaint; item only. |
| 47 | Sanguine Omen | 256 | `sanguine_omen.png` 256x256 on a full cube | none | On the WIP list. Repaint at 16x16. |

Three item sprites are not 16 px wide: `barbed_splinter` and `incandescent_enzyme` at 18, `hematic_condenser_kit` at
15. Resize to 16.

That is 47 entries covering about 130 model files, plus 3 sprites.

Three entity textures could not be measured because the bound texture is missing from the assets folder: `LeechRenderer`
binds `textures/entity/denizen/model_denizen.png`, and `BloodBulletRenderer` and `WillAnchorRenderer` bind
`textures/entity/wretched_will/modelwretchedwill.png`. If those entities can appear, they draw the missing-texture
checkerboard. Check whether the files were moved by the concurrent job before acting.

### 2.4 Fix batches

| Batch | Rows | What it is | Size |
| --- | --- | --- | --- |
| T1. Kitbashed JSON UV rescale | 1, 6, 26, 27, 28, 30, 31, 32, 33, 35, 36, 38, 40, 41, 42 | One script: for each listed model, remove explicit `uv` on faces whose texture is a 16 px tiling block texture (faces then take position-derived UVs), and for the 256 px fill faces set spans to face size over 16. Check each by eye; a few faces may need a manual offset. | M |
| T2. Downsample-and-repaint textures, layout unchanged | 7, 8, 9, 18, 19, 22, 23, 24, 29, 47 and the 3 sprites | Paint or clean up each PNG at its layout's size. A plain nearest downsample is a starting point, not the finish. | M to L |
| T3. Generator regenerations at density 1 | 2, 11, 34, 37, 39, 43, 44, 45, 16 | Rerun the tracked generators with density 1 and face-sized UVs; review renders come from each script's preview. | M |
| T4. Renderer scale baked into geometry | 3, 4, 5, 10, 12, 13, 14, 15, 17, 20, 21, 25, 46 | Model work: author the mesh at its drawn size so the renderer scale returns near 1; `unstained_weapons.py` covers row 10, the rest have no generator in the repo and need a Blockbench pass or a new generator. | L |

Review clients for density work: `bookModelReviewClient` is the model for a held-item review; `AnimalModelClientReview`
(`crimsonDoeReviewClient`, `venousStriderReviewClient`) and `pelagicReviewClient` show mobs; `riteFloorReviewClient`
shows the Cardinal Focus; `mausoleumReviewClient` shows sarcophagi, pylons and niches;
`harbingerSitesReviewClient` should show the Phlebotomist's Cabinet in the outpost; `staffMiningReviewClient` probably
shows the Living Staff (not confirmed). Armour, elytras and hand weapons have no dedicated review client.

### 2.5 What needs a client to settle

- Whether the Silent Archon leggings (row 19) visibly stretch, and which half of `silent_archon_layer_2.png` they show.
- The exact per-context renderer scale for the held items: the renderers pick a different scale for GUI, first person
  and third person, and the ranges above are bounds, not a per-view figure.
- Whether the UV rescale in batch T1 keeps each kitbashed model's intended look (for example a staff head that relied
  on the centre of a block texture).
- Whether the three missing entity textures are real gaps or a mid-edit state of the concurrent job.
- For Part 1, every replacement's count, size and speed: the numbers above are starting points read from the particle
  classes, and only a client shows whether a burst reads as bold or as noise.
