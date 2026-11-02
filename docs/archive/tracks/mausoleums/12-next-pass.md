# Saint Mausoleum rework — pass 1 summary and next-pass cleanup

Date: 2026-10-29. Sources: the eleven ledgers `.superpowers/sdd/2026-10-28-mausoleum-rework-01` … `-11`
(`progress.md` in each; ledger 01 covers Tracks 0 and 1, the rest one track each). All of it is in the working tree,
uncommitted. This file replaces reading the eleven ledgers before starting the next pass; go to a ledger only for a
contract table or a coordinate it lists.

Read [00-README.md](00-README.md) first for the shared rules (lang keys, `HemoParticleData`, 16 px, generator-authored
models, review client before "done", no commits). They still apply.

---

## 1. What pass 1 built

| Track | Result |
| --- | --- |
| 0 Review scaffold | `HemoJigsawStructure.assembleOnSurface`; dev-only `/hemo test mausoleum <saint> [seed]`; `MausoleumAssemblyGameTests` (four seeds x four Saints, every mandatory piece); `MausoleumClientReview` + `runMausoleumReviewClient` (`-Pmausoleum=<saint>`, flat or `natural` mode); stops in `src/gameTest/resources/mausoleum/review-stops.json`, shots in `build/mausoleum-review/<saint>/`. Stops take optional `degree`, `action`, `block`, `face`, `lead`, `door`. |
| 1 Placement, outer halls | Entrance sits flush (`start_height` 0, lifted onto the highest ground under the 17x17 entrance, refused when the site rises more than 4 within 14 blocks: `MausoleumEntranceRules`). `trialroom` / offering gate / trial altar gone. New descent (buries everything after it 17 blocks down) and loculi gallery (6 bays, 22 loculi, 3 barrels, iron-door treasure room with a pressure plate inside, inscriptions written into the template). |
| 2 Consecrated ward | Seal block, rules, tests and GameTests removed. `threshold` hall with an invisible `ward_anchor` BE (`WardAnchors` index, `MausoleumWardRules`, D8 gate). Turned-back push, strikes for break/place/explosion/piston/threshold/burn through `WardStrikePacket` (protocol 20). Client `MausoleumWardRenderer` (`mausoleum_ward` shader, Abocipher runes), strike flashes, `HallowedBurnLayer` for an Apotheos. |
| 3 Legacy-dungeon layout | Spiral fold-back layout, 19 pieces, lowest box -39, reach 80: top → stairs → antechamber → descent → gallery → two-piece spiral → threshold → nave → three wings (level, upper, crypt; each approach → hall → focus) and the chamber under the entrance. Generator split into `tools/worldgen/mausoleum/{kit,trials,pieces,build}.py` + `themes/<saint>.py`; `build.py` lays out every approach combination and fails on overlap. `ChapelRules` marks (facing-turned), Downfall gate start, Litany stone inscription. Armor-stand congregation gone. |
| 4 Sarcophagus, chamber | 2 x 4 block tomb on 23 fillers, cut at z 32 into foot + head sections baked through a sectioned model; half-scale item model. Chamber 37 x 17 x 27: processional, pillar ranks, 15 x 15 boss floor, dais, apse behind a triumphal arch; `CHAMBER_SOCKETS` + `chamber_sockets` theme hook. `runSarcophagusReviewClient`. |
| 5 Ring door | `ring_door` is now a 5 x 5 controller multiblock with a BE: baked hematic-iron frame, venous slab that sinks, three rings that turn into line node by node, channel fills as `BloodStreams` ribbons, seam pulse, hyphae cracks and burst fragments when forced. Projection pays 300 per ring stage through `BloodAccounting`. `SectionedBakedModel` shared with the tomb. |
| 6 Set pieces | Eleven generator-authored blocks: `frozen_devotee`, `witness_stone`, `litany_stone`, `offering_stand`, `pulse_pylon`, `spawn_mark`, `containment_anchor_block`, `rot_pyre`, `sealed_niche`, `opened_niche`, `reliquary_sconce`. The Chapel focus drives witness `lit`, seal `due`, pylon `charge`, stand stock. Loculus fronts, wall lamps → sconces. No entities in any template. |
| 7 Architectural kit | `kit.py`: corridor, hall, barrel/cross vaults, archivolt arches, flights, stair shaft, niche, dais, speckle, floor grid, `finish`. `checks.py` runs on every template (no 4 x 4 flat, keystone on every doorway ≤ 5 wide, vault stairs over 6+ ceilings, real block states) and fails the generator. Entrance rebuilt by the kit (`entrance.py`). Bone brick family added. `everyPlacedStateIsARealState` (JVM) + registry twin (GameTest). |
| 8 Hemorath | Hematic iron plating family, polished venous stone wall, `blood_gutter` (multipart, wet by depth), `venous_basin`, remodelled `blood_basin`, crimson sconce flame. Theme hooks `HALLS`, `motif`, `CRYPT_DROP`. His Downfall is a descending hall to the chamber. `SarcophagusPalettes` + `RetexturedBakedModel` retexture the tomb per Saint. `MausoleumAmbience` (heartbeat, drip). |
| 9 Seraphae | Healed quartz bricks, healed calcite, radiant healed quartz, `bone_outgrowth`. Anchor `snapped`, pylon `style=anchor`. Growth by depth, anchors chained to vaults, grilles for wall lights, swollen pillars, the Still Ward as her Repose. Fight wakes the chamber's four nearest whole anchor blocks (`SeraphaeAnchorRules`) and restores them. Choir + anchor-tick ambience. |
| 10 Putriciel | `packed_ash`, charred planks family, `charred_log`, `charred_bookshelf`, `sodden_mud_bricks`, `spore_shelf`. Pyre fire is the Flammeus flame drawn from a BE (`PyreFlames`); sconce `flame=smoulder`, pylon `style=pyre` with purge ring, `litany_stone` `charred`, `spawn_mark` `style=ash`. Burned/overgrown bands by piece, Burned Library, Purge Corridors, Ash Garden, rotted chamber lit only by pyres and shelves. Fight alternates pyres and shelves (`PutricielChamberLightingRules`). Crackle/creak/flare ambience. |
| 11 Velorum | Devotee pose `striding` + `head` turn, `witness_devotee`. Rimed deepslate brick family, `rime_lantern`, `hanging_icicle`, `frozen_drip`, dark lacquer planks family. Stand `style=bowl`, pylon `style=frost`. Frost by depth, the Procession, the Rule (his Litany), the Cell Row (his Repose), inverted-nave chamber. Ice creak + muffled own footsteps. |

Final state at Track 11: JVM 4566 green; GameTests 1139 with only the two standing failures (§3.7); review client
218 shots for Velorum, and every Saint shot in its own theme track (Hemorath 80 stops, Seraphae 86, Putriciel 82).

---

## 2. Rulings that bind the next pass

Grouped by area. Ledger and ruling number in brackets.

### User rulings
- The armor-stand congregation is gone; figures are blocks (`frozen_devotee`). [03 R8]
- Putriciel's fire is the Flammeus look: crimson body, pink-white heat, black soot tips. Never green or yellow in any
  fire texel (`PutricielFireResourceTest`). Only the overgrown bands' spore glow is green. [10 R1]
- After the user's "try again" on Putriciel's first stills: no bone-brick trims (they read as a sandstone temple).
  Trims, bases and floors are mossy stone brick; bone only on keystones, capitals and seals. [10 R2]

### Placement and layout
- `max_distance_from_center` stays 116 (vanilla caps at 128 including beard padding), so the complex folds back under
  itself instead of reaching 160. [03 R1]
- Entrance placement lives in two places: the JSON (`start_height` 0) and `MausoleumStructure` (lift + site refusal).
  Some steep sites no longer generate. [01 R6, natural re-run note]
- Chain order: antechamber → descent → gallery → spiral → threshold. The threshold is 33 down. [01 R3, 03 R2]
- Wing slots: 0 level, 1 upper (from the nave gallery), 2 crypt. Marks live only in each wing's fixed hall and focus
  room. [03 R3, R4]
- The chamber is the lowest room. A theme may set `CRYPT_DROP` (at least 1) to give its own hall the rest of the drop.
  [08 R4]
- `MAUSOLEUM_FOOTPRINT_RADIUS` 80; ring-door search 64 and Chapel→door lookup ±16 high. [03 R9, R10]
- Inscriptions are template data (`InscriptionId` NBT); `MausoleumStructure.afterPlace` no longer searches for them.
  The disabled legacy `mausoleum` structure loses its two inscriptions if re-enabled. [01 R4]

### Tooling and tests
- `/hemo test mausoleum` is dev-only in `HemoTestCommands`, not a production `HemoCommand` branch. [01 R1]
- The assembly GameTest lives in `registration_validation`. [01 R2]
- Acceptance is checked by `checks.py`, not eyeballed; `kit.finish` must end every template and raises on a flat with
  no variant. [07 R2, R3]
- `Theme` is a validated dict (every role present, family roles must be that family). Theme hooks: `HALLS`, `motif`,
  `CRYPT_DROP`, `chamber_sockets`, `nave_sockets`, `hall_sockets`. [07 R4, 08 R3]
- The kit writes stair shapes itself (jigsaw pieces place with a known shape). [07 R5]
- Preview rasterizer UVs are mirrored from FaceBakery; `game_uvs` flips them back for previews. [05 R6]
- Review door states are written through the BE's saved tag; no review-only API in production code. [05 R9]
- A review stop's `lead` must be at most `wait - 2`. [05 open]

### Ward
- The ward layer covers pieces past the threshold; outer halls are consecrated and strike but have no breathing
  layer. [02 R2]
- Strikes go to the actor with `sendToPlayer` and to bystanders with `sendNear` excluding the actor. Refusal sound is
  vanilla bell at low pitch. Client re-scans ward faces every 100 ticks per section. [02 R4, R6, R7]
- Only destroying explosions are refused; wind charges and keep-blocks creepers pass. Riders are dismounted before
  push-back. [02 final review]

### Tomb and ring door
- Tomb is centred on its main block (straddles three columns) so it lines up with every symmetric chamber, runner and
  door. [04 R1]
- Static frames bake into chunk models in sections (`SectionedBakedModel`); moving parts are drawn by the BER as
  `<part>` + `<part>_head`. [04 R2, 05 R5, R7]
- Ring door: id stays `hemomancy:ring_door`, no item. Closed every cell is full; open only the frame cells keep
  shape. Full 900 checked at the click, 300 paid per stage; a missing or short projector fails the projection and
  records nothing. `PROPER` written when the slab starts to sink, `FORCED` + skipped when it bursts. [05 R1–R4]
- Ring door visuals reuse `BloodStreams` and existing fissure/glow quads, no new art. [05 R8]

### Set pieces
- Trial pieces (witness, litany, stand, pylon, spawn mark) are unbreakable; dressing breaks and drops nothing. All
  itemless, `noLootTable`. [06 R4]
- Two-tall blocks use vanilla `half`; the pylon uses `part` with its BE on the upper part. [06 R5]
- The focus drives a set piece only when the block at the mark is that set piece. [06 R6]
- Per-Saint looks are `style` properties on the shared block (`pulse_pylon` venous|anchor|pyre|frost,
  `offering_stand` stand|bowl, `spawn_mark` venous|ash, sconce `flame` pale|crimson|smoulder), not new blocks.
  [08–11]
- Decorative basins are `venous_basin`; `blood_basin` stays the puzzle block and stands only in Hemorath's four chamber
  alcoves, never four within its 12-block scan. [08 R1, R6]

### Themes
- Every theme grades by depth, walked backwards from the chamber (Hemorath wetness, Seraphae GROWTH 0–4, Putriciel
  BAND 8→3, Velorum FROST 1–4). [08 R2, 09 R2, 10 R3, 11 R1]
- The threshold corridor and the chamber's 15 x 15 boss floor stay pinned open in every theme (`_pinned`). [09 R5,
  10 R4, 11 R6]
- Theme growth touches wall and vault roles only; trims, accents and bases stay so the architecture reads. [09 R5]
- Lamps per Saint: Hemorath crimson sconces; Seraphae grilles (diorite post + lantern behind); Putriciel smoulder in
  burned bands, spore shelves in overgrown ones, no chamber crown lamps; Velorum rime lanterns everywhere, sea lanterns
  only in the nave. [09 R3, 10 R5, R6, 11 R3]
- Warm block light turns pale ice tan underground; Velorum's ice is saturated cold blue. [11 R7]
- Velorum's ring-door track is polished deepslate; Putriciel's is packed mud. [11 R8, 05 open]
- New planks families stay out of planks/wooden tags (chew goals, Foul Paste, lignum). [11 Done]

### Fights
- Seraphae takes the four nearest whole anchor blocks within 8 of the tomb; the relic-niche anchor is snapped so it
  is never picked. [09 R1, R8]
- Putriciel's window: pyres out and shelves bloom; restored whenever Putriciel goes. [10 D]

---

## 3. What is still open

Each item says where it came from. Items a later track closed are listed in §4, not here.

### 3.1 Must fix before anything else — done 2026-10-29

- ~~The structure generator is not in version control.~~ `.gitignore` now tracks `tools/worldgen/mausoleum/` and
  `tools/worldgen/mausoleum_complexes.py`; AGENTS.md lists them and `RepositoryTextEncodingTest` scans them. A
  re-run reproduces every committed template (pools, structures and tags byte-identical; NBT identical once
  decompressed, since the gzip header carries a timestamp).
- ~~Commit the pass.~~ Committed as `2ff58cc50`. The overnight endgame run and the eleven tracks share their
  registration, lang, frozen-fixture and reference files, so they could not be split by track and went in one commit.

### 3.2 Proof owed: seen only in stills, by tests, or not at all

The review client takes stills. None of these have been watched in motion or heard:

- Apotheos hallowed-ground burn layer (needs an Apotheos review player). [02]
- `SCENE_SAINT_RISE` from the processional (needs a relic-woken Saint). [04]
- Every trial completed solo in a client: Beholding, Litany, Downfall, Repose, per Saint. [03]
- Set pieces mid-trial: witness `lit`, seal `due`, pylon charge wind-up, Velorum's witness head turn and eye glints.
  [06, 11]
- Ring door: Hemorath's runoff seen from the chamber side. [05]
- Seraphae's anchors waking (flare, block clear and restore), radiant glow layer, outgrowth facings. [09]
- Putriciel's purge ring over the Ash Garden, the pyre/shelf switch and `lightShift`, pyre licks. [10]
- Velorum's frost pulse particles and charge ring. [11]
- All ambience is unheard: Hemorath heartbeat/drip, Seraphae choir (beacon hum at pitch 0.6; fallback
  `ambient.warped_forest.loop`) and anchor tick, Putriciel crackle/creak (the `mud.step` creak is doubtful)/flare,
  Velorum creak (glass/amethyst at low pitch is a guess) and muffled steps. [08–11]
- Natural worldgen: only one natural run (Velorum, seed 20261028), and none after the entrance check widened to 14
  blocks. Run natural mode for all four Saints and note how often steep sites now refuse. [01]

Suggested approach: one played review session per Saint (or a scripted review with a video capture), working down
this list, before any new art.

### 3.3 Lighting and readability

- The ward overlay dims every inner stop. [10]
- Hemorath's litany hall is very dark; the candidate on a stand needed a block-light floor of 9 to read. [06]
- Putriciel's and Velorum's chambers are dim at rest by design (pyres/shelves; nave is Velorum's only bright room).
  Judge in play, not in stills. [10 R5, 11]
- The tomb reads small and dim from the chamber door at 30 blocks, and the processional walls hide the pillar ranks.
  Consider widening the portal. [04]
- Seraphae's grilles: ambient occlusion makes the diorite slat nearly black, and the lantern shows on both sides. The
  Still Ward's calcite ring is low contrast under the ward overlay. [09]
- Velorum's still circle reads as a squared ring in the 7-wide Cell Row. [11]

### 3.4 Art and model gaps

- The pavilion's stairwell newels read stubby in `pavilion_hall`. [07]
- Velorum's pavilion roof is only partly rimed (stepped courses cover each other's tops). [11]
- Sarcophagus coals and offering-bowl blood stay red for every Saint. Velorum at least needs its own. [11]
- Hemorath: Beholding steles "carved with his students" (a `witness_stone` carving variant) not made; Litany "rations
  on iron trays" has no tray model. [08]
- The ring door's hand stream starts from a fixed offset below the projector's eyes, not the rendered hand. [05]
- Prostrate devotees' upper cell has no outline (breaks through the lower). [06]

### 3.5 Content owed to other sections

- Chapel memory content: Hemorath's students sealing the vessel; Seraphae's "four touches, four growths". [08, 09]
- Section 8: Hemorath's swelling can draw on the four wells (`blood_basin` at chamber x 15/19, z zc±12); Velorum's
  "congregation thaws" needs a devotee mob; Velorum's "safe cells" in the Cell Row are not live (pulses reach the whole
  hall). [08, 11 R5]

### 3.6 Robustness and code debt — done 2026-10-29

- ~~Seraphae never re-places a released anchor.~~ Every 100 ticks she counts her standing anchors and wakes the
  missing ones (`SeraphaeAnchorRules.placements(..., standing)`: dormant blocks first, then the ring slots after those
  standing). She only does this while every chunk of her arena has its entities loaded, so an anchor that is merely
  unloaded is never doubled. The anchor's own orphan check is unchanged: it still lets go when it cannot see her,
  and she now takes it back. JVM `aRefillWakesOnlyWhatIsMissing`.
- ~~Junctions left behind by the entrance lift.~~ `MausoleumStructure` lays the complex out into a scratch builder, then
  moves each piece and raises its `JigsawJunction` ground heights by the same amount, so the beardifier blends
  toward the real floor.
- ~~Low vault over the descent's top landing.~~ The descent's vault springs at `DESCENT_SPRING` 12 (was 10); the room
  is 16 tall, cornice, pilasters and lamps move with it, and a third bone band runs at 9. The landing's edges now
  have four courses of headroom. Generator checks and the layout solver pass for every Saint. Needs a look in the
  review client (`descent_top`).
- Gallery ledge in `kit.hall()`: no action needed. `checks.py` runs on every template, so the first template that
  uses the ledge gets checked automatically.
- The "reference only" entrance NBTs in `structure/mausoleum/` are live pieces of the disabled legacy `mausoleum`
  structure (`worldgen/structure/mausoleum.json` and its pools). Deleting them means retiring that structure, so it
  moves to §3.8.

### 3.7 Test debt

- ~~Two GameTests failing since Track 4~~ — fixed 2026-10-29; `alphaCheck` passed with JVM 4567/0 and all 1139
  required GameTests.
  - `centrifugeJourneySpawnsCompleteReadyStructure` ran in a 1x1x1 vanilla template but built its fixture 14 blocks
    out and four high, so it landed in whatever neighbouring test's barrier wall the GameTest grid put there. It now
    runs in the 32x12x24 `hemomancy:empty` template with its origin at (14, 5, 14). The other 56 `EMPTY_TEMPLATE`
    journey fixtures have the same exposure and only pass because of where they fall in the grid. Move them too if one
    starts failing the same way.
  - `everyScriptoriumOperationKeepsItsOutcome` recorded the menu's raw enchantment registry ids, which shift when a
    dev mod on the run classpath adds or drops an enchantment (62 → 61 for Unbreaking, 53 → 52 for Silk Touch).
    It now records the enchantment's name. The golden was rewritten after checking that each old id mapped to exactly
    one name and that no other part of any line changed.
- Flaky: `livingfalseomencannottimeoutintosuccess` (False Omens deadline), `strongerpreyneedsmultiplebitesbeforereward`
  (timed out once).
- Track 10 had no final fresh reviewer pass. Run one over Putriciel's Java (lighting rules, `PyreFlames`, spore shelf).

### 3.8 Calls for the user

- Hematic iron plating in `ductilis_conductors` / `laboring_arms_resistant`? [08]
- Charred planks in any planks/wooden tag? (Lacquer stays out, per 11.) [10]
- Hemorath's heartbeat quickens toward the pit (70 → 45 ticks, pitch drops). Keep, or slow it? [08]
- Sound picks in §3.2 (choir, creak, ice creak) once heard.
- Should `/hemo test mausoleum` get a production command, or stay dev-only? [01 R1]
- Retire the disabled legacy `mausoleum` structure (its JSON, pools and the ten NBTs in `structure/mausoleum/`)? [07 R7]

---

## 4. Closed by a later track

Opens from early ledgers that later tracks resolved. Recheck in the §3.2 session, but do not reopen as work.

- Seal plates and bone blocks standing in for niche blocks (01) → Track 6 loculus fronts.
- Trial set pieces as block stand-ins and plain halls (03) → Tracks 6 and 7.
- Velorum's gallery monotone (01) and the flat Seraphae/Velorum colour fields (07) → Track 9 growth and Track 11
  frost-by-depth palettes. Confirm in play.
- Only Hemorath's chamber and ring door shot (04, 05) → every Saint shot in Track 6 (249 shots) and its theme track.
- Empty relic and votive apse niches (06) → each theme fills them (Hemorath reliquary niches, Seraphae snapped anchor,
  Putriciel shelves, Velorum kneeling devotee and rime lanterns).
- `--saint` tomb palettes refused (04 R5) → `SarcophagusPalettes`, all four Saints have one.
- Natural entrance cut on the hill (01) → entrance check samples 14 blocks; still owed a natural re-run (§3.2).

---

## 5. Suggested order for pass 2

1. ~~§3.1: track the generator, commit pass 1.~~ Done.
2. ~~§3.7: clear the two standing GameTest failures.~~ Done; `alphaCheck` is green.
3. ~~§3.6 robustness fixes.~~ Done; the raised descent vault still needs a look in the review client.
4. §3.2: one played session per Saint; log what fails and what reads badly.
5. §3.3 and §3.4 from that log, per Saint, in the theme file that owns it.
6. §3.5 hands to Section 8 and the Chapel memory work; §3.8 waits on the user.
