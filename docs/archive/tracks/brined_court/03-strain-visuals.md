# Track 3: Strain visuals

**Status:** DONE 2026-10-30 (unattended run). Generated multi-anchor attachments for all eight strains (16 anchors from `tools/model_export/morphling_strains.py`) with stage growth, limb tags and motion answering the synced active bits; world effects on the shared toolkit; redrawn HUD sprites; `morphlingStrainReviewClient` with contact sheets. Left: Track 2's three placeholder effect icons, the polyp appendage geometry (Track 1 placeholder) and in-play art tuning. See "Handover".

Phase A. Depends on Track 1's table. Reads Track 2's synced active state (landed 2026-10-30: the bit layout is in `02-strain-powers.md`, "Synced active state"; strands ride the same packet, not their own payload). Spec sections: "Silhouette and palette", each strain's "Appearance" table, "Implementation notes: new render geometry".

## Goal

Each strain changes the player stage by stage as the spec's appearance tables describe, reads apart from the other seven at a glance, and moves the way the palette table says (fronds open with light, veins pulse, mane ripples like flame, bell breathes). Icons and HUD art match. The same attachments later dress Court NPCs (Track 9), so build them to be reused.

## What exists

- `client/model/morphling/MorphlingMutationRegistry.init()` maps each strain item to one `MorphlingVisualMutation` (tint, pulse, swirl, emissive, `attach(...)`).
- `MorphlingVisualMutation.Builder.attach()` stores **one** `modelAttachment`. The spec needs two anchors for six strains.
- `MorphlingModelAttachment.AttachmentPoint {HEAD, BODY, ARMS, LEGS}` with `visibleFrom`, `growthScale`, `hideAttachedPartAt`.
- `client/render/layer/player/MorphlingMutationLayer.java` and `MorphlingPlayerPartVisibility.java` draw and hide parts.
- 12 Java attachment models `client/model/entity/summon/Morphling*AttachmentModel.java` (bat head, fungal head, cuttlefish head, spider body, centipede body, leech arm, mole arm, serpent leg, plus unused chitinite, urchin, pests, tick), with `.bbmodel` sources in `assets/hemomancy/models/entity/bbmodel/morphling/` and layers registered in `client/event/LayerEvents.java`.
- No per-strain model generator exists. `tools/model_export/morphling_incubator.py` covers the incubator only.
- Equipped Morphling sync: `SyncEquippedMorphlingPacket`, an `ALWAYS` row in `PlayerStateSync`, sent to the owner and trackers from `EquippedMorphlingEvents` (≈424, ≈430).

## Work

### 1. Several attachments per strain

Change `MorphlingVisualMutation` to hold a list of `MorphlingModelAttachment`s, each with its own point, `visibleFrom` stage and `hideAttachedPartAt`. `MorphlingPlayerPartVisibility` hides the union. Keep the builder's `attach(...)` as "add one". Anchors per the spec:

| Strain | Anchors |
| --- | --- |
| Sunburst | BODY |
| Holdfast | ARMS, LEGS from Apex |
| Mermaid's Purse | BODY, ARMS finlets from Developing |
| Dead Man's Rope | BODY, ARMS |
| Ink Cap | HEAD, BODY at Primal |
| Hushbell | BODY |
| Marrowplume | ARMS, LEGS from Apex |
| Kinbud | BODY (spine), HEAD (nape and crown) |

Primal head replacements (Ink Cap's hood, Kinbud's polyp face) use `hideAttachedPartAt` on HEAD; check they also hide the hat layer and any helmet the player wears, or decide that a helmet wins (PROPOSED: helmet wins below Primal, the replacement wins at Primal and the helmet renders over it scaled).

### 2. A generator for the strain models

Add `tools/model_export/morphling_strains.py`: one generator, one function per strain, writing for each anchor the Java model class, the `.bbmodel` and a preview PNG with all stages side by side, at 16 px per block. Name the classes for the strains (`SunburstBodyModel`, `HoldfastArmModel`, …) under `client/model/entity/morphling/` (not `summon/`, which they aren't).

Shape targets (spec "Silhouette and palette"): fanned fronds over a lichen rosette; knotted root fists; a hybrid sailfish body with red purses (below); branching veins and trailing beaded strands; tall shaggy cap and veil; closed folded bell; red clubs with feathery crowns on white; flame-coloured mane on a pale polyp. Avoid lists in the spec's "Real-world references" table: no Siphonophore look on Rope, no Osedax on Marrowplume, no Tube Worm plumes on Kinbud, no armour plates or shells anywhere.

**Mermaid's Purse is a hybrid (SETTLED 2026-10-29, M3).** The item keeps the red leathery egg-case icon. The body grows both halves: cobalt-and-silver sailfish parts (finlets, sail, crescent tail, bill crest) and red leathery purse pouches at the flanks, ridged and horned like the icon's egg case, that swell with banked blood. The purses tie the body to the item at a glance.

**Limb tags for NPCs.** Court NPCs (Track 9) wear these attachments on the jointed Hemomancy rig, which has forearms and shins; the player model doesn't. Tag every `ARMS` and `LEGS` attachment part as upper-limb or lower-limb in the generator, so Track 9's anchor adapter can put forearm and calf growths on the right segment and they bend with the joint.

Each stage grows parts (`visibleFrom`) and scales them (`growthScale`) rather than swapping models, so a strain reads as one organism growing.

Retire the legacy animal models and their textures, **except** the Gravecap fungal head: the Mycophant Tendril reward uses it for its full-body fungalization. Move it to a neutral name (`FungalHeadAttachmentModel`) and keep the Tendril pointed at it.

### 3. Motion and synced state

Motion comes from `AnimationTime` (frozen-tick safe). Some motion answers server state the client can't infer:

| Strain | State the client needs |
| --- | --- |
| Sunburst | fronds open (Lantern Fronds, Bloom, Arbor), home (sunlight glow) |
| Holdfast | anchored (strands spread into the ground) |
| Mermaid's Purse | sail raised (sprint-swim), banked blood (pouch swell 0–1) |
| Dead Man's Rope | link targets (drawn by the link renderer) |
| Ink Cap | dissolved (Deliquesce) |
| Hushbell | shut (Fold Shut, Cryptobiosis) |
| Marrowplume | anchored in a Whale Fall |
| Kinbud | Second Head active and which shoulder; Kindled |

PROPOSED: add a small `visualFlags` int and one `visualLevel` byte to `SyncEquippedMorphlingPacket` (it already reaches trackers), set by Track 2 when state changes, not every tick. That re-lays-out a payload: bump the protocol and regenerate the registry file. Rope links get their own payload from Track 2.

### 4. World-space pieces

From `WorldEffectDispatcher`, using the existing tendril, glow, cell and mote toolkit:

- Dead Man's Rope link strands between partners (and to block anchors), with the bioluminescent pulse travelling along them.
- Sunburst Bloom and Marrowplume Whale Fall field patches (`MorphlingFieldEntity` renderer): a floating gold bloom mat; a pale bone-and-red ground stain with swaying plumes.
- Kinbud's Second Head as a body part, drawn with the player (an attachment with its own neck), not a separate entity renderer.
- Ink Cap's ink trail and Deliquesce dissolve; Hushbell's bell closing; Holdfast's grip strands into the ground. These are focal moments: read `docs/MANIPULATION_VISUALS.md` and build to the Riposte bar.

No vanilla particles. Ink is not vanilla squid ink.

### 5. Icons, HUD and jar

- Item icons arrive in Track 1 from `docs/archive/concept/output/brined-strain-icons-16x16/`. Re-check Marrowplume, the weakest native read per that README.
- Redraw the HUD overlay sprites (`textures/gui/morphling_overlay/`, static plus `anim/` and `anim2/` frames) for all eight, drawn by `EquippedMorphlingOverlay`. Reuse the icon pack's pixel-map approach: add the HUD frames to its `pixel-maps.json` or a sibling file so they regenerate from one exporter.
- The Morphling Jar's model branch reads the table (Track 1). Decide whether each strain gets a jar look; PROPOSED: tint only.
- The tint for Unfed and Fledgling per strain (spec appearance tables) goes in the table row's colour fields, read here.

## Review client

Add `morphlingStrainReviewClient` to `gradle/review-clients.gradle` (driver `MorphlingStrainClientReview` under `src/gameTest`, operator-inbox pattern). Scenes: each strain at each of the six stages, front, back and side, in air and underwater, by day and night; each active state from the table above; Rope links between two players and to a block; both field patches; Kinbud with one and two Second Heads; a helmeted player at Primal for the head replacements. Screenshots to `build/morphling-strain-review/screenshots`. Then a contact sheet of all eight at Apex side by side, the spec's "read apart" check.

## Tests

- `MorphlingMutationRegistryTest`: every strain in the table has a mutation, anchors match the spec table above, Gravecap's fungal head is still registered for the Tendril.
- Keep `MorphlingStaticSpriteTest` and `EquippedMorphlingOverlayTest` meaningful against the new sprites.
- Texel density: the generator asserts 16 px per block.

## Open questions

- Not blocking: should Morphling visuals show on other players by default, or respect a client setting? Today they show.

## Handover (written 2026-10-30, unattended run)

### What landed

- **Several attachments per strain.** `MorphlingVisualMutation.attachments` is a list; `Builder.attach` adds one. `MorphlingMutationLayer` draws each; `MorphlingPlayerPartVisibility` hides the union and reads the active bits (`MorphlingModelAttachment.hiddenPlayerParts(maturity, flags)`, `keepsHelmet`). `MorphlingMutationRegistry` builds every strain from its table row: the new `MorphlingStrain.tintColor()` (ARGB: the Unfed-Fledgling tint, alpha the overlay opacity) plus one `MorphlingStrainAttachment` per generated anchor.
- **Generator.** `tools/model_export/morphling_strains.py`, one function per strain, writes per anchor the Java model (`client/model/entity/morphling/<Strain><Anchor>Model.java`), atlas and glow atlas (`textures/models/morphling/<strain>_<anchor>[_glow].png`), the `.bbmodel` (`models/entity/bbmodel/morphling/`) and a six-stage preview (`build/morphling-strain-preview/<strain>_<anchor>.png`, plus `apex_contact.png`). It also writes `MorphlingStrainModels`, the anchor index (point, layer, factory, part table, texture, first stage, head-replacement stage, Primal scale, head-hiding bits). It asserts one texel per model unit (16 px per block), eighth-unit boxes, limb tags on every ARMS/LEGS part and parents growing before children. 16 anchors, 411 parts, 805 cubes.
- **Runtime model.** `MorphlingStrainModel` (base of the generated classes), `MorphlingPartSpec` (stage, limb `UPPER`/`LOWER`/`NONE`, motion, amplitude, speed, phase, active bits, glow group, grows), `MorphlingAnimContext` (maturity, bits, time, daylight; state motions settle per wearer), `MorphlingStrainMotion` (pure curves). Motions: SWAY, WAG, RIPPLE, FLAME, FROND, BREATHE, SAIL, POUCH, DRIP, GROW_ON, SPLAY_ON. Glow modes: SUNLIGHT, VEIN_PULSE (travelling group by group), SAIL_FLUSH, BREATH, FLAME. `drawLimbSegment(limb)` draws one segment for Track 9's jointed rig.
- **Anchors.** As the table above, plus Mermaid's Purse LEGS calf finlets (Developing) and a HEAD brow crest (Primal), which its appearance table describes. Ink Cap and Kinbud replace the head at Primal. Hushbell's Primal bell closes over the head (and hides it) on `FOLD_SHUT | CRYPTOBIOSIS`. Kinbud's Second Heads are BODY parts on their own necks: `SECOND_HEAD` on the right shoulder, `SECOND_HEAD_TWIN` on the left.
- **Helmets** (the PROPOSED rule): below Primal a worn helmet wins and HEAD growths are skipped; at Primal the replacement takes the head and the helmet still renders over it (not scaled).
- **World effects.** `MorphlingStrainWorldRenderer`, called from `WorldEffectDispatcher` (AFTER_PARTICLES, after the outpouring), draws on `OutpouringBrushes` (Lux, Ductilis, Umbra, Mortem): Rope strands to linked entities, block anchors and the Lifeline point, with beads and a running pulse; Rope's Primal train (along the walked path, or laid out behind a still host); the Bloom and Whale Fall field patches (drawn here; the entity keeps its `NoopRenderer`); Holdfast grip strands, Sunburst Arbor roots and Marrowplume Whale Fall stalks spreading from the feet; Ink Cap's Deliquesce dissolve; Hushbell's frost veil; Mermaid's Purse's wake with the sail up. `MorphlingStrainTrails` keeps the history and Lifeline point (cleared on logout in `ClientSessionEvents`). `MorphlingStrainClientEvents` sheds sparse toolkit accents (ink mist, blood beads, frost, Kindled flames, lux motes). No vanilla particles.
- **Retired:** the eleven first-attempt attachment models, their textures and `.bbmodel`s, and their `LayerEvents` rows. The fungal head is now `client/model/entity/morphling/FungalHeadAttachmentModel` (layer id unchanged), still drawn by `MycophantTendrilFungalizationLayer`; `java_model_to_bbmodel.mjs --set=morphling` lists only it.
- **HUD.** `docs/archive/concept/output/brined-strain-icons-16x16/export_hud_sprites.py` enlarges each icon map with EPX to 32x32, adds the feeding tendril to the vessel point (5, 16), writes `hud-pixel-maps.json` and exports the static sprites and the `anim/` and `anim2/` strips; `hud-preview.png` sits beside it.
- **Review.** `morphlingStrainReviewClient` (`MorphlingStrainClientReview`, operator inbox; `-PmorphlingStrainReviewOnly=a,b` filters scenes): 48 stage scenes (back, side, front) and 28 state scenes (plus a high back view), 256 shots in `build/morphling-strain-review/screenshots`. `tools/review/morphling_strain_contact.py` writes `apex_contact.png`, `stages_<strain>.png` and `states.png` there.
- **Tests.** `MorphlingMutationRegistryTest` (anchors and first stages per strain, head replacements, limb tags, stage order, atlases, the fungal head) and `MorphlingStrainMotionTest`. `MorphlingStaticSpriteTest`, `EquippedMorphlingOverlayTest` and `MorphlingPassiveEffectPresentationTest` pass on the new sprites. `EndgameBossRewardResourceTest` now pins `new FungalHeadAttachmentModel`.
- **Docs.** HR §16.5 rewritten (anchors table, motion, helmets, world effects, HUD, review client) and its status line; the icon pack README; the source-text impact map regenerated.

### Calls made unattended

- Mermaid's Purse grows LEGS (calf finlets) and HEAD (brow crest) anchors beyond the plan's BODY + ARMS row, because its appearance table puts finlets on the calves and a bill crest over the brow, which body-space geometry cannot follow. The test pins the four anchors.
- Rope's "several blocks" train is a world effect from position history (with a short beaded model train from the lower back), so it trails where the host walked.
- Rope strands, the Lifeline and the train draw on the Lux material (violet tint) with a Ductilis core; the Bloom on Lux, the Whale Fall on Mortem. The packet carries no Lifeline position, so the strand anchors where the client saw the host when the bit came on.
- Helmet rule as PROPOSED, except the helmet is not scaled over a Primal replacement (that needs an armour-pose hook).
- Growth: a part shows at 0.7 of its size at its first stage, 0.88 at the next, then full. The only whole-attachment Primal scale is Holdfast's 1.12 ("visibly broader silhouette").
- Jar: no change (it already reads the table; PROPOSED was tint only).
- Marrowplume's icon was re-checked at native size and kept; its HUD sprite comes from the same map.
- Unfed-Fledgling tints moved into the table row (`tintColor`). Marrowplume's tint is now bone white (was red), per its table. Kinbud's tint pulses pale instead of the old red swirl, which read as a red body in the review shots.
- Second Head sides: `SECOND_HEAD` right shoulder, `SECOND_HEAD_TWIN` left; Track 2's bits carry no side.
- Model previews and contact sheets are offline Python (Pillow + NumPy).
- Other players still see each other's mutations (the open question is unchanged).

### Verification

- `./gradlew.bat test`: 4,726 tests, 0 failures. `compileGameTestJava` clean.
- `runMorphlingStrainReviewClient` on a seed-42 world: `MORPHLING_STRAIN_REVIEW captured 256`, no render exceptions in the log; a filtered rerun (`kinbud-apex,holdfast-apex,second-head`, 14 shots) after enlarging the Second Heads and Holdfast's forearm clusters. Contact sheets built and inspected: the eight read apart at Apex from back, side and front; strands, train, field patches, roots, the sealed bell, the raised sail, the dissolve and the helmet scenes all show.
- No shader or render-type class was added or changed (strain models use vanilla entity render types; world effects use the existing toolkit types).

### Reviewer fix (2026-10-30)

- Deliquesce grants Invisibility, and both `MorphlingStrainWorldRenderer` and `MorphlingStrainClientEvents` skipped invisible wearers, so the dissolve and the ink trail never drew in play (the review scene set the bit without the effect). An invisible Ink Cap host mid-Deliquesce now still gets the dissolve and the ink trail, and nothing else. `./gradlew.bat test` rerun: 4,726 tests, 0 failures; the generator reruns byte-identical.

### Left for later

- Effect icons for `stung`, `pinned` and `morphling_sealed` are still Track 2's placeholder copies.
- Polyp appendage geometry per family (Track 1's `MorphlingPolypLayerModel` placeholder) was not in this plan's work list and is unchanged.
- Art tuning in play: Holdfast and Sunburst read smaller than the others at Mature; Rope strands are faint in full daylight; Ink Cap's cap still shows above its Primal hood.
- First-person hand rendering of arm growths (`EquippedMorphlingHandRenderer`) was not touched.
