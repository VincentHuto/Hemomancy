# Stained Priestess tendril form: design

Date: 2026-10-16. This replaces the box-built wings and thorn head from
`2026-10-15-annetta-encounter-models-design.md` §4. The user asked for the whole winged form to be redone from
actual writhing tendrils, in the style of the HutosLib tendrils, the Cardinal Rite renders and the loom renders,
and wants it to look perfect.

## Intent

Phase 2 is Annetta's corpse being worn by the infection. What holds her up and gives her wings must be alive: tendrils
that never stop moving, not rigid cubes. The silhouette stays as agreed: headless body a little taller than a
player, wings about five blocks each side, a thorn-and-tentacle head with three glowing yellow eyes, and the
red-and-blue lancea in her hand.

## Form

- **Body (model, kept):** her headless corpse in blood-soaked robe tatters, with dead-grey limbs and no copper. The
  scripted box rig stays only for the torso, arms, legs, tatters and the neck stump. The wing and thorn-head cubes
  are removed.
- **Wings (procedural):** each wing grows from a wound between the shoulder blades:
  - **Arm:** a thick vessel-bundle tendril. It is the wing's leading edge and carries the pose.
  - **Fingers:** six writhing tendrils fanned from points along the arm like a bat's wing, each about 2.5–3.2 blocks
    long. Travelling curvature waves run root to tip, so the tips curl and whip. The fingers never stay still.
  - **Webbing:** translucent veined membrane stretched between neighbouring fingers over their inner ~70%, torn at the
    trailing edge, with a slow pulse.
  - **Drips:** two or three thin tendrils hanging from the arm that sway under gravity.
  - **Poses:** FOLDED (swept back and down along her body), SPREAD (about five blocks per side, slow beat with the
    fingers lagging the arm), SHIELD (wrapped forward round her). Pose changes blend over about six ticks.
- **Head (procedural):** a knot of about 14 short tendrils boiling out of the neck stump and curling back in, so the
  cluster stays head-sized and head-shaped. Thorns and bone tips sit at the ends. Three almond eyes face forward,
  a large centre eye and two smaller ones tilted ±22°, glowing yellow with a dark slit. A faint glow surrounds them.
- **Body tendrils (procedural):** tendrils spiralling round each arm, spilling from the neck stump down the chest,
  bursting from the back wound between the wings, and two or three trailing from the hem to the floor.

## Look

- Tendril cores are lit, textured tubes (eight sides) using a painted vessel strip: dark venous red, wet highlights,
  vessel striations, and infected yellow-orange nodes. The texture runs along the length.
- Every tendril also gets a glow pass in `HLRenderTypeInit.TENDRIL_GLOW` with a pulse travelling outward. The
  pulse is red-to-orange on the wings and yellow at the eyes.
- Webbing uses a translucent, double-sided textured surface: vein network, ragged holes, alpha falling off toward
  the torn edge.
- Palette is unchanged: blood, vessel, infected ichor, fungal grey and bone. No copper, no brass. Base tint is
  white, and the textures carry the hue.

## Behaviour hooks (visual only)

The visuals read the existing synced state and never gate gameplay:

| Trigger | Visual response |
|---|---|
| Walking | Wings fold |
| Standing | Wings spread and beat |
| `VISUAL_PRESSURE_BLOOM` | Wings snap fully open; glow surges |
| `VISUAL_LUNGE` | Fingers lash forward |
| `VISUAL_LANCE` | Wings flare back |
| Hurt | Everything spasms |
| Death | Tendrils go slack and droop |

The review override `StainedPriestessModel.reviewWingPose` still forces a pose.

## Structure

- `PriestessTendrilRules` (pure, no Minecraft types) owns the pose angle tables, pose blending, wing span
  constants, finger fan layout and the curvature-wave integrator that returns centre-line points. It is
  JVM-tested.
- `StainedPriestessTendrilLayer` (`RenderLayer`) walks the model's bone chain (root → body or neck stump or arm)
  and draws each tendril in bone space, as `MnemonistThreadLayer` does.
- `TendrilTube` (client helper) parallel-transports frames from centre points and radii, then emits:
  - a lit textured core into `RenderType.entityCutoutNoCull`
  - glow quads into `TENDRIL_GLOW`
  - webbing strips between two tubes into `RenderType.entityTranslucent`
- Textures are painted by `tools/model_export/stained_priestess.py`: `stained_priestess_tendril.png`,
  `stained_priestess_webbing.png` and `stained_priestess_eye.png`.

## Acceptance

- Review-client shots of FOLDED, SPREAD and SHIELD poses plus a head close-up show writhing, organic tendrils
  with no box wings, at about five blocks per side when spread.
- Rules tests:
  - the SPREAD span is between 4.5 and 5.5 blocks
  - SHIELD tips end in front of the body and FOLDED tips behind it
  - waves keep roots pinned
  - blending is continuous
- A source test confirms the layer is registered and the box wing and thorn parts are gone.
- `AnnettaModelSourceTest` is updated, and the Annetta tests stay green.
