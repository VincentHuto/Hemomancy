# Annetta encounter models — design

Date: 2026-10-15. Sub-project 1 of 3 for the Annetta Knowles encounter rework.

1. **Models and textures (this document).**
2. Harbinger route choreography: Phase 1 combat, the tick-burst death and rise, Phase 2 flight AI.
3. Unstained route: Latent Infection combat and the purged Annetta as an ally.

Sub-projects 2 and 3 get their own design documents. This one produces the bodies, props, textures and base
animations they will drive, and leaves the encounter's gameplay behaviour as it is.

## Intent

Annetta is one of the mod's tragic figures: a high Unstained cleric who never knew she carried an enormous infection
(LORE_REFERENCE §11). All three of her entities currently use stock `HumanoidModel` boxes with placeholder textures
(`annetta_knowles.png` is a flat block with a red slash). The encounter needs bodies that tell her story at a glance:
the hidden tell, the purification, the thing that comes out of her, and what she becomes when it wins.

## The five looks

### 1. Annetta, found cowering (`AnnettaKnowlesEntity`, `COWERING`)

Almost entirely human, player-sized (existing 0.6 × 1.8 hitbox). The only tells are **long red hair** and **gold
eyes**. Ordinary Unstained lose their pigment (albino or bald, grey-blue eyes), so an observant player should sense
she is wrong before anything happens. Nothing else infected shows: no veins, no growths.

**Clothing (high cleric's vestments):**
- Flowing white robes with faint pale-silver thread. Clergy, not a Guardian: no plate.
- A short white mantle/stole across the shoulders, closed by a patinated **consecrated copper crescent clasp** (Our
  Lady).
- A verdigris censer chain or rosary at the belt; consecrated copper fittings at cuffs and collar.
- A high collar that half-hides the neck.
- Cowering: robes rumpled, hem dirtied. Pose: huddled, knees drawn, arms over head/face.

Copper and verdigris are allowed on Annetta in every look where she is still Unstained (cowering, Phase 1, purged
ally). This is a deliberate exception to the no-copper rule for blood-side models; it does not apply to Phase 2 or
the Latent Infection.

### 2. Annetta, purged ally (`AnnettaKnowlesEntity`, `CURED_SUPPORT` / `RESOLVED`)

After the Draught of Still Mercy, she physically becomes one of her order: **hair cropped short and white, eyes
blue**. Same vestments, cleaned and squared. Pose: upright fighting stance in `CURED_SUPPORT`, calm standing in
`RESOLVED`. She carries **only her dagger** (`annettas_absolution_dagger`), no shield: she is still weakened. The
stance should read a little guarded and tired rather than fully battle-ready. **She heals the player** (already
implemented in `tickCuredSupport`: +3 HP, Regeneration, Blood Loss removed every 60 ticks). The model needs a
healing pose for it: off hand raised and open toward the player, dagger hand held back and guarded. That pose plays
on `VISUAL_CURED_SUPPORT`. Sub-project 3 may refine the healing behaviour and its visuals.

### 3. Annetta, Harbinger Phase 1 (`AnnettaKnowlesEntity`, `PHASE_ONE`)

Same as the cowering look (red hair, gold eyes, full vestments) plus:
- **Absolution Dagger** in hand (existing item texture `annettas_absolution_dagger.png` is the reference).
- A **cracked Unstained shield** on the off arm: white lacquer split through the middle, verdigris copper rim and boss.
- A **Tooth Peck tick** latched to the side of her neck at an angle, where the high collar has been torn aside. It is a
  swollen tick-form of the Tooth Peck (her canonical creature, LORE §11; existing `ToothPecksModel` is the
  reference for its head and mouthparts). Its abdomen must be able to **swell** smoothly from small to grotesque over
  the fight (driven by a 0–1 value supplied later by sub-project 2), with infected yellow-orange showing through the
  stretched skin as it grows.

The head must be hideable on its own (sub-project 2's tick-burst takes her head off).

### 4. The Stained Priestess, Harbinger Phase 2 (`StainedPriestessEntity`)

What rises after the infection takes her.
- **Body:** Annetta's body, headless, slightly taller than a player (~2.2 blocks). The white robes survive only as
  torn, blood-soaked tatters. **No copper**: it is eaten away or overgrown.
- **Head:** from the neck stump, a ball of writhing thorns and tentacles roughly shaped like a head, with **three
  glowing yellow eyes** (a larger centre eye and two smaller tilted flanking eyes, like
  `SanguineMonolithRenderer.renderEyeOverlay`). Eyes render emissive/full-bright.
- **Wings:** crude wings of writhing tendrils and blood vessels torn out of her back, about **5 blocks span each**
  (10+ blocks tip to tip) when fully spread. They must fold (behind her, for walking/landing), spread (flight), and
  wrap forward around her (shield).
- **Spear:** a Sanguis Lancea of **red and blue intertwined** strands. The existing `AnnettasSanguisLanceaModel` /
  `model_annettas_sanguis_lancea.png` is already red-and-blue and is what drops from this fight; the held spear
  should be that same weapon, upgraded if needed so held and dropped versions match.
- Hitbox covers the body only, not the wings.

### 5. The Latent Infection (`LatentAnnettaInfectionEntity`)

**Revised mid-build (user reference images, 2026-10-15):** skinnier and closer to the supplied concept: a gaunt
frame, a pointed bone cowl arching over an empty face with teeth down its rim into a toothed chest maw around a red
core, ropes of red hair under the hood, a ragged skirt of hanging vessel strands over the serpent tail, vertebra rings
on the whip arm, long black hooked claws; crimson mottled with ivory. The two claw arms and the spear are kept from
the original description below (Claude's call; the reference shows one large claw).

What the Draught forces out of her on the Unstained route. Towering: about **5 blocks tall**, a little under the
Mycophant (4 × 6 hitbox, 2× render scale).
- **Only very vaguely humanoid.**
- **Lower half:** no legs. A long, almost **serpentine tail-like appendage**, trailing writhing mycelium and roots.
- **Head:** a writhing mass of tendrils in place of a head.
- **Three arms:**
  - Far left: a mass of **coiled blood vessels** that can uncoil, extend and whip around.
  - Two on the right side: **skinny, three-clawed with one thumb**, both gripping one large, gnarled, Sanguis
    Lancea–like spear (all infection: no blue).
- **Material:** entirely fungus and blood vessels, with hints of bone, spores and teeth showing through.
- Palette: infected yellow-orange and blood red over fungal greys/off-whites. No copper.

## Sizes

| Entity | Hitbox (w × h) | Visual |
|---|---|---|
| Annetta (all human looks) | 0.6 × 1.8 (unchanged) | player-sized |
| Stained Priestess | ~0.9 × 2.2 (from 0.9 × 2.6) | body ~2.2 tall; wings ~5 blocks each |
| Latent Infection | 1.6 × 4.5 (from 1.0 × 2.2; narrowed from 2.5 after the skinnier rework so it paths through doorways) | ~5 blocks tall |

Hitbox numbers are the builder's call within these bounds; the visual sizes are the user's.

## How it gets made

Follow the repo's existing scripted-model pipeline (e.g. `tools/model_export/vestment_of_the_final_molt.py`,
`unstained_armor.py`):
- One Python script per entity under `tools/model_export/` emits the Java model class and the painted texture, with
  a `--preview` flag that renders a preview PNG.
- Regenerate Blockbench projects with `tools/model_export/java_model_to_bbmodel.mjs` into
  `src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/` (the three existing `.bbmodel` files there are
  replaced).
- Models are vanilla `HierarchicalModel` animated procedurally in `setupAnim`, like `MycophantModel` (keyframe
  `AnimationDefinition`s, as Vesper uses, are allowed where a pose sequence needs them). Not GeckoLib.
- Variants of the human Annetta (hair length/colour, eye colour, tick, shield, dagger, head) are part visibility and
  texture swaps on one model, chosen by the renderer from the entity's existing encounter state. One texture per
  variant (or overlay layers) is the builder's call.

## Animations in scope

Base locomotion and idles only, plus rigging that later sub-projects drive:
- Annetta: cower, idle, walk, fighting-stance idle; tick swell parameter; head hide.
- Priestess: idle, walk, fly (wing beat), glide; wing fold/spread/shield-wrap poses; head tendril writhe; eye glow.
- Latent Infection: idle (tail and head writhing), slither/move, whip-arm coil/uncoil pose.

Out of scope here (sub-projects 2 and 3): attack animations, dagger-into-neck, the collapse and rise, swoops, wind
blast, fluid pools, the tick-burst effects, any AI or combat change.

## Goals

- Five distinct, readable looks matching the descriptions above.
- Each renders in game with no missing-texture or z-fighting, at the stated sizes.
- The rigging exposes everything sub-projects 2 and 3 need (head hide, tick swell, wing poses, whip arm, held items).

## Non-goals

- No change to encounter states, AI, damage, drops or progression.
- No new entities. The tick is a model part on Annetta, not a separate mob (sub-project 2 may revisit).

## Anti-goals

- Annetta must not look infected in her cowering/Phase 1 looks beyond hair and eyes (plus the tick in Phase 1).
- No brass, bronze or silver accents on the Priestess or the Latent Infection; no copper there either.
- Not God/Satan framing: the Priestess is a corpse driven by infection, not a demon.
- Do not let generic vanilla particles stand in for model detail. Any ambient effect added uses the
  Hemomancy/HutosLib custom suite (AGENTS.md, `docs/MANIPULATION_VISUALS.md`).

## Acceptance

- Preview PNGs for each look, shown to the user.
- In-game screenshots of all five looks (review-client or `runClient`), with no resource errors in the log.
- `./gradlew.bat build` passes; existing `AnnettaCanonSourceTest` stays green (update it if it asserts the old model
  shape).
- Docs: `HEMOMANCY_REFERENCE.md` and `LORE_REFERENCE.md` §11 updated for the new looks, sizes, and the Phase 1
  dagger/shield/tick identity (replacing the hair-and-nails Phase 1 note).

## Calls made while writing this up (Claude's, not the user's)

- **Existing visual-state poses are ported**, so the encounter stays playable between sub-projects: Annetta's
  SILVER_AURA, VIAL_THROW, CURED_SUPPORT; the Priestess's LANCE, LUNGE, PRESSURE_BLOOM; the Latent Infection's
  INFECTION_BLOOM, PRESSURE_SPIKE become rough keyframed or procedural poses on the new models. KERATIN_SLASH
  becomes a dagger slash pose (the hair-and-nails identity is retired). Sub-projects 2/3 replace them.
- **Tick swell** is derived client-side from Phase 1 health fraction until sub-project 2 supplies its real driver; no
  new synced data in this project. **Head hide** is a model input that defaults to visible.
- **Phase 1 holds her personal dagger:** the Phase 1 equip swaps the plain `absolution_dagger` for
  `annettas_absolution_dagger` so the held-item layer draws her own. This is the one equipment change allowed here.
- **Arena fit:** the Latent Infection's spawn is nudged to the nearest open space that fits its new hitbox inside the
  broken church. Wider arena changes (and Priestess flight room) belong to sub-projects 2 and 3.
- **Held items never drop:** any item equipped for rendering (Annetta's dagger, the Priestess's lancea) gets a zero
  equipment drop chance; the encounter's existing reward drops are unchanged.
- **Flight/wing screenshots** come from a review-client or debug pose override, not from a real flight state.

## Left to the builder

- Exact cube layouts, texture resolution (2× painted, like recent models, is the default), UV packing.
- Whether variants are separate textures or overlay layers.
- Exact hitbox numbers within the table's bounds and render scale.
- Whether the renderers move off `HumanoidMobRenderer` (they will need to for `HierarchicalModel`).
