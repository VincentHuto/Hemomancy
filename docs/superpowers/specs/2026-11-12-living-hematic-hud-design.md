# Living Hematic HUD — design

Date: 2026-11-12. Status: agreed in brainstorming, awaiting plan.

## Why

The blood HUD should feel alive and adaptive to what the player actually has showing: the blood
volume vessel, an equipped reserve (gourd, curved horn, Hemorath rib) and the Temper pools. Today's
"adaptive hematic reliquary" chassis (`tools/gui/hematic_hud_art.py`, `temper_rack_art.py`, the
`temper_overlay/phial_*` ampoules and the `blood_hud/assembly_*` art) was a first attempt the owner
rejects outright. **This is net new.** Nothing from that chassis is a starting point: not its art, its
geometry, its vein routing or its phial shapes. Its art files are not even on disk under
`textures/gui/blood_hud/` at the moment (the generator was never run into the tree), so the current
build draws missing textures there.

The owner's draft image (a rough mock of a red-tendril and dark-iron border wrapping the vessel, the
gourd and coloured vials, in each combination) set the direction. The scale was wrong, and the
decisions below supersede it where they differ.

## Goals

1. A **living binding** grows around whatever HUD parts are showing and reshapes itself for every
   combination.
2. The **Temper vials are remade from scratch** as small medical vials that tip and **pour a constant
   trickle into the blood volume meter**.
3. The degree red (in the vessel frame and in the new binding) **beats like a slow heart**.
4. The layout **mirrors** to follow the client `location` setting, without flipping existing textures.
5. The equipped Morphling lives in a new **jar slot** above the reserve, inside the binding.
6. The existing vessel, degree-frame and reserve textures are used **unchanged**.

## Non-goals

- No change to any existing texture file: `vessel_back`, `base_degree_0..8`, `pomes_*`,
  `fill_apotheos_*`, `halo_apotheos`, `gourd_*`, `curved_horn_*`, `hemorath_rib_*`. Runtime layers may
  be drawn over them, but the files stay byte-identical.
- No new item or Scarlet Vanity slot. The owner floated a separate item to show the vials without the
  Vascularium Charm; that is a **follow-up project**. This redesign keeps today's gates: active blood,
  the Unstained block, the charm in slot 5, schools seen, and the three `powergen` display toggles.
- No gameplay, eligibility or synced-data change. Pure client presentation.
- No gourd-to-vessel tube, siphon or glass line. The owner tried one and removed it ("doesn't look
  professional ... too cluttered").

## Anti-goals (it fails even if it works)

- Anything that reads as the old chassis: wet-tissue mantles, buried ampoules, feeder-vein routing,
  heart-knots.
- A **rope or braid** look. The owner rejected it; the binding is the vessel's own material.
- A heavy **mantle**. The binding is thin.
- A **shield** or slab around the reserve. The binding stays form-fitting to the reserve's outline.
- A tight, busy lattice.
- A heartbeat that **brightens or glows**, or beats fast. It darkens, slowly.
- Vanilla particles anywhere in the HUD.

## What the player sees

### The binding (owner decisions)

- **Weight:** a thin binding, about 6 art pixels, hugging the outline of the parts it wraps. This is
  the "binding" weight from the mockups, not the 9 px "mantle".
- **Material:** the same material as the vessel's degree frame. Crossed **hematic iron** struts in the
  frame's own dark greys (roughly #0A0A0A to #60606A, no bright silver), with **red tendril** filling
  the cells between struts. Iron collars at intervals carry gem studs. Black 1 px outline.
- **Calm:** a loose lattice. The struts cross about every 18 px along the binding (not every 9 to 10).
  Collars come every ~44 px. A small tendril curl sprouts from about every third collar, and only
  where the binding runs red.
- **Red fills by degree:** like the degree frames, the binding's red climbs from the base of the
  assembly as degree rises. Below the red line the cells are dark recessed tissue (≈#140A0A) and the
  studs are grey. At Degree 7 it is fully red. (Apotheos: see my calls.)
- **Form-fitting and adaptive:** the binding is grown around the union silhouette of the visible
  parts, so every state gets its own outline.
- **The reserve graft (vessel + reserve):** the reserve sits low on the vessel's **inward** flank (the
  side facing the screen centre). The **lower** gap between vessel and reserve is filled with the
  binding's lattice: iron struts over dark recessed tissue, with red tendrils hugging the struts
  (filled by degree), not solid red. The binding **dips in above the reserve's shoulder**. The owner
  explicitly wants that dip kept, "it made it feel more hugged", so the fill must not extend up beside
  the reserve's upper half.

### The heartbeat (owner decisions)

- Applies to the **red pixels of the vessel's degree frame** (found at runtime from the frame texture,
  which stays unchanged) and to all **red in the binding**, including studs and tendrils.
- **Rhythm:** lub-dub (strong beat, softer beat, rest), as in the mockup.
- **Speed:** **25 bpm** at full blood.
- **Effect:** on each beat the red **darkens** toward deep blood (≈#280006, peak ≈45% mix) and eases
  back. It never brightens or glows.
- The beat travels slowly: up the vessel frame's veins from the base, and along the binding.
- One shared heart drives the vessel frame and the binding in sync.

### The Temper vials (owner decisions)

- **Eight new medical vials**, one per school in enum order, **four on each flank** of the vessel
  ("flank pour"). They are not the old 12×18 ampoules: a small glass vial (~7×15 art px) with a
  crimped metal cap and lip, a dark glass outline and a pale paper label band.
- Each vial is **tipped about 50°**, mouth down and toward the vessel, and is seated on the binding.
- The school-coloured liquid **settles under gravity** in the tilted glass (the fill takes the lowest
  pixels in screen space), with a lighter surface line. School colours come from
  `TemperPhials.schoolColour` (dark schools are already lifted to a readable floor there).
- **Trickle into the vessel, all the time:** each vial that holds Temper pours a thin 1 px stream from
  its mouth, arcing over the frame rim into the glass. The stream runs down the inner wall to the
  blood surface and blooms a small fading colour plume into the blood. Brighter beads travel down the
  stream so it reads as moving. A vial whose pour point lies below the blood surface pours straight
  into the liquid.
- **Vials only trickle into the vessel.** With the vessel hidden there is no trickle.

### Every state

The three bits are vessel (`showBloodVolumeHud`), reserve (`showGourdHud` + reserve equipped) and
Temper (`showTemperHud` + any school seen). The reserve has three shapes (gourd, curved horn, rib).

| Showing | Layout |
|---|---|
| Vessel | Vessel wrapped by the binding. |
| Reserve | The reserve alone, binding hugging its outline. |
| Temper | **Vials only:** a short vertical iron-and-tendril spine in the binding's material, the binding wrapped around it, four vials clamped down each side. No trickle; the vials just hold their levels. |
| Vessel + reserve | Reserve grafted low on the inward flank, lower gap filled, dip kept above its shoulder. |
| Vessel + Temper | Vessel and binding, vials on both flanks trickling in. |
| Reserve + Temper | The reserve takes the centre with a **form-fitting** binding. Each vial's neck is seated on the binding itself, so the rack follows the reserve's curve (tucking in at the waist, out at the lobes). **No trickle.** |
| All three | Vessel + graft + vials trickling into the vessel. Vials on the graft side sit on the binding where the graft is. |

Each reserve shape (gourd, horn, rib) gets its own fitted outline in every state that shows it, on
each side. The Morphling jar (below) is a fourth optional part that the binding also wraps, so
every row above also exists with the jar added.

### The Morphling jar (owner decision)

- A new **jar slot** sits **above the reserve** on the inward flank. It is new art that resembles the
  Morphling Jar item (`textures/item/morphling_jar*.png`), with glass, a lid and a base in the
  binding's hematic iron. The equipped Morphling renders **inside** it. This replaces today's
  attached sprite clinging to the vessel wall
  (`BloodVolumeOverlay.getVesselEdgeX`/`getVesselCenterY`), which would otherwise collide with the
  inward vials and the graft.
- The jar is a fourth part the binding wraps: it gets its own form-fitting outline and joins the
  vessel's binding the same way the reserve does. *My calls:* keep a small dip between the jar and the
  reserve below it, matching the reserve's dip, and never fill the jar's sides solid.
- *My calls:* the jar shows whenever a Morphling is equipped and `MORPHLING_HUD_MODE` isn't `OFF`
  (the HUD's existing gates still apply). `ATTACHED` draws the 32 px gripping sprite inside the jar;
  `LEGACY_ICON` draws the item icon inside it. With no reserve, the jar keeps the same upper inward
  spot. With the vessel hidden, it sits above the reserve, or on top of the vials' spine when
  there's no reserve either. The bond meter and the distracted status keep their current readouts,
  repositioned next to the jar.
- Inward vials on the jar's side seat on the binding around the jar the same way they do on the
  graft.

### Orientation (owner decision)

- `powergen.location` (0 top-left, 1 top-right, 2 bottom-left, 3 bottom-right) already picks the
  corner. For the right-hand corners the **layout, binding, graft, jar position, vials and streams
  mirror** so the graft and jar always face the screen centre. There is no vertical flip; the vessel
  stays upright.
- **The existing textures are never flipped.** The degree frames keep their lighting, and the horn
  and rib keep their shape and curl direction. Because the reserve outlines are asymmetric, every
  binding, graft fill and dip that hugs a reserve (or the vessel frame) must be fitted **per side**:
  mirroring the fitted art is wrong. New art (vials, jar, lattice) may be authored once and mirrored,
  as long as its lighting reads correctly on both sides. The degree-red heartbeat mask comes from the
  unflipped frame, so it never mirrors.
- Text (degree title, mL readouts) never renders mirrored. Hover hit-testing for vial tooltips uses
  the mirrored geometry.

### Kept as is

- The vessel liquid fill, meniscus, bubbles, Apotheos fill frames and halo, Pome sockets (texture or
  item mode), the open-reserve halo and the reserve liquid fills (`GourdHudFillPixels`,
  `SpecialGourdHudFillPixels`).
- Degree title and mL labels under the vessel; the reserve's mL label.
- The Temper hover tooltip (`TemperMarksOverlay` stays the HUD's sole pool reader and tooltip owner;
  only its drawing changes).
- The Morphling bond meter and distracted-status readouts (moved next to the new jar).
- Corner anchoring and scale-down-to-fit behaviour (`BloodHudLayout.place`).

## My calls (unreviewed; the owner agreed to the general shape)

- **Dry vial** (below a Humilis cast cost, as `dry` is defined today): no trickle. It shows its cracked
  or dull state through the new vial art.
- **Full vial:** a faint lift on the glass, kept as subtle as the heartbeat. No bright halo.
- **Unseen school:** no vial. Its seat on the binding shows a closed, healed socket.
- **Apotheos (Degree 8):** the binding's red becomes the gold-orange of `base_degree_8`, and the beat
  darkens it toward deep amber (≈#461600).
- **Heart rate falls back to blood level:** 25 bpm at full, rising to about 45 bpm and slightly fainter
  near empty. The owner approved the 25 bpm mock that included this behaviour but did not rule on it
  separately.
- **Red line source:** the binding's red climbs to the same fraction of the assembly's height that
  the frame's red covers at that degree (degree/7 of the binding's vertical extent; full at 7).

## Constraints from the codebase

- **HUD drawing** stays in the existing `hemomancy:bloodvolume` layer registered in
  `client/registry/GuiLayerRegistrar`. No `RenderGuiEvent` handlers (`HudAndWorldDrawingGuardrailTest`).
- **Animation time** comes from `client/render/AnimationTime` (`AnimationTimeGuardrailTest`). Today's
  overlay uses `EquippedMorphlingOverlayPlacement.animationTimeSeconds`; the builder should check
  whether that already routes through `AnimationTime` and fix it if not.
- **No vanilla particles** (`VanillaParticleRatchetTest`); everything here is drawn pixels anyway.
- **Player-facing text** stays as `en_us.json` keys (`HardCodedEnglishGuardrailTest`). Existing
  tooltip keys under `screen.hemomancy.temper_rack.*` can be reused.
- **Pixel grid (my call):** today the vessel's 64×128 textures are stretched into a 76×126 art box
  (`BloodVolumeOverlay.OVERLAY_W/H`), while the reserves blit 1:1. The new build **draws the vessel
  1:1 at 64×128**, keeping `OVERLAY_SCALE` 0.6, so the vessel, reserves, binding, vials and jar share
  one pixel grid. That is the grid the owner approved the mockups on (binding 6 px, struts every
  ~18 px, collars every ~44 px, vials ~7×15, vessel:gourd width 2:1). Everything hard-wired to 76×126
  moves with it: the fill masks (`VesselFillMask` sampling), `BloodVesselShape`, `POME_POINTS`, the
  label offsets and the Apotheos fill frames. The texture files stay unchanged; only their draw size
  changes.
- **Performance:** today's fills re-upload `DynamicTexture`s at up to 60 Hz. The new per-pixel work
  (degree-red reveal, beat, streams, vial liquid) must not add per-frame texture uploads per vial.
  Precompute masks and paths per layout and draw with fills or blits.
- **Tests and docs to update:** `BloodHudLayoutTest` (geometry), `TemperGuardrailTest` (names
  `TemperMarksOverlay.java`), `docs/architecture/evidence/source-text-impact-map.csv` (regenerate with
  `python gradle/source-text-impact-map.py`), and the "Showing it" paragraph of
  `docs/HEMOMANCY_REFERENCE.md` (around line 650), which describes the rejected chassis and must be
  rewritten.
- **Removed assets:** the old generator scripts, the `temper_overlay/phial_*` textures and any
  `blood_hud/` art move to the untracked `removed_assets/` folder, not deleted outright (owner
  preference). `temper_overlay/sigil*.png` belong to other screens; check users before touching them.
- **Not released:** no save or config migration is needed.

## Left to the builder

- **How the binding is produced.** Either a pixel-authored Python generator under `tools/gui/` that
  bakes one fitted binding per state × reserve shape (with separate iron and red-capable layers plus
  a fill-order channel), or runtime generation from the parts' alpha masks. The mockups' algorithm is
  a working reference: a union silhouette, a chamfer distance field, a traced and smoothed contour at
  the binding's mid-radius, then the lattice rasterised along arc length and normal. The owner cares
  about the look, not the method; a baked generator gives the most pixel control and matches how the
  repo authors HUD art.
- Exact coordinates, vial art polish, stream curves, collar spacing within the "calm" rule, how the
  beat is implemented (per-pixel or banded), and the vial seat positions per reserve shape.
- How `BloodHudLayout` is restructured (or replaced) to describe the new states, the mirroring and
  the vial hit boxes.
- A review client (a row in `gradle/review-clients.gradle`) or `/hemo` fixture that shows each state,
  each reserve shape, with and without the jar, a few degrees, Apotheos, low blood, and both sides
  for screenshots. The
  owner expects visuals verified in a launched client.

## Mockups

Browser mockups made during brainstorming are kept locally (gitignored) in
`docs/superpowers/mockups/living-hud/`, with the real textures beside them:

- `vial-pour.html`: the flank pour the owner picked (option A).
- `heartbeat-v3.html`: the approved heartbeat (25 bpm, darkening).
- `border-dip.html`: the approved binding material, weight and calm, and the graft with its dip
  (ignore its glass tube, since removed).
- `states-fit.html`: full, reserve + vials (form-fitting, vials seated on the binding) and vials-only.
  Its reserve state still shows a trickle; the owner removed that afterwards.

The mockups are for intent, not pixel truth. Their vial and stream art is rough.
