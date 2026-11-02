# Annetta, Putriciel, and Velorum Telegraph Pass

## Goal

Add the same kind of readable boss-fight polish used for Hemorath and Seraphae to Annetta's encounter family, Putriciel, and Velorum. This slice focuses on synced attack states, model poses, render pulses, and particle telegraphs. It should make existing mechanics easier to understand without adding major new balance rules or bespoke trial chambers.

## Scope

This pass covers:

- `AnnettaKnowlesEntity` Phase One, Cowering, Cured Support, and Resolved readability.
- `StainedPriestessEntity` blood-lance, lunge, and pressure-bloom tells.
- `LatentAnnettaInfectionEntity` infection bloom and pressure spike tells.
- `PutricielEntity` absolution window, rot nova, and absolution-hit urgency.
- `VelorumEntity` martyrdom, frost nova, veil of darkness, and silence drain tells.
- Existing client models/renderers for those bosses.

This pass does not add new arenas, new loot, new route requirements, or large new mechanics. It also does not introduce GeckoLib animations; it uses the existing vanilla model/render path.

## Design

### Shared Pattern

Each boss gets lightweight synced visual state data where the current code does not already expose enough state to the client. The server sets the visual state at the start and impact of existing attacks. The client model and renderer use that state to pose the entity and pulse scale/glow. The server also emits clear telegraph particles during windups or periodic warning ticks.

The visual states should decay automatically after a short tick count. Impact states should be brief, while windup states last for the existing attack delay or warning interval.

### Annetta Encounter Family

Annetta's visuals should keep her tragedy legible:

- Cowering: small, guarded posture; no combat boss bar.
- Phase One: silver/teal Unstained aura corrupted by hair, nails, and blood pressure cues.
- Silver Aura: pale ring around Annetta before the effect applies to blood-active players.
- Hemolytic Vial: throw posture and brief splash cue.
- Hair-and-Nails Slash: short forward/area warning using crit/sweep-like particles before the hit.
- Cured Support: calmer pale glow and healing/support cue around Annetta and nearby Unstained players.
- Resolved: non-hostile softened posture.

Stained Priestess should read as the same person overrun by crystallized blood:

- Blood Lances: lance-charge pose and red line/projectile cue.
- Lunge: crouched coil before the dash and a brief streak after movement.
- Blood Pressure Bloom: expanding red floor ring before damage.

Latent Infection should read as externalized infection, not Annetta herself:

- Infection Bloom: fungal/spore ring before the confusion/slowness pulse.
- Pressure Spike: sharper soul-fire pressure cue with a visible radius.

### Putriciel

Putriciel's existing mechanic is the absolution window. The pass makes that unmistakable:

- A synced timer or visual state marks when absolution is open.
- The model and renderer brighten/pulse during the window.
- Rot Nova gets a clear expanding rot-fire floor ring before the arena pulse.
- Each successful absolution hit increases visual urgency toward trial completion.

The damage clamping and five-hit victory condition stay as implemented.

### Velorum

Velorum should feel cold, silent, and rhythm-based:

- Martyrdom resistance gets a synced visual pulse that decays with the resistance window.
- Frost Nova gets an expanding snowflake/pale floor ring before root damage.
- Veil of Darkness gets a distinct dark cloud tell around the boss.
- Silence Drain at low HP draws visible soul-fire/dark tethers from blood-active players to Velorum.

The existing martyrdom resistance, frost nova, darkness effects, and low-HP blood drain remain the core mechanics.

## Implementation Notes

- Prefer existing entity classes over new goal classes unless an attack needs a clean helper method.
- Add `EntityDataAccessor<Integer>` visual state/tick fields where needed.
- Keep helper methods small: `setVisualState`, `tickVisualState`, and attack-specific particle helpers.
- Model changes should be simple poses using existing `HumanoidModel` parts.
- Renderer changes should be limited to scale/color/glow pulses and existing render layers.
- Use particles already available in vanilla/NeoForge; no new texture assets are required.

## Testing

Run `./gradlew.bat build`.

Manual checks after build:

- Annetta Harbinger route: Cowering to Phase One, silver aura, vial, slash, mutation into Stained Priestess.
- Stained Priestess: lance, lunge, pressure bloom, reward behavior.
- Unstained route: Cured Support plus Latent Infection bloom/spike, resolution on infection death.
- Putriciel: absolution window visibility, rot nova tell, trial completion after required hits.
- Velorum: martyrdom pulse, frost nova tell, veil tell, low-HP silence drain tethers.

## Non-Goals

- No bespoke arenas or structure placement changes.
- No new boss rewards.
- No large mechanical rebalance.
- No GeckoLib conversion.
- No changes to faction route gating.
