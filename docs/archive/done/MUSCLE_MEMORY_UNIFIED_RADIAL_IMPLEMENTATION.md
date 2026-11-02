# Muscle Memory Unified Radial Implementation

## Outcome

Muscle Memories and conventional manipulation memories now use one selection vocabulary, one radial, one shared memorization capacity, and the existing Mnemonic Reliquary and Dendritic Distributor. The lore documents use **Willed Memory** as a working name for the latter, but this implementation does not require that name to be final. This avoids a second menu and keeps the early physical system connected to later memory play.

The implementation retains the five vascular sections. The eight tendencies define eight Muscle Memories, but several memories may compete for the same section. A player can learn all eight; section assignment controls what can be active together, while shared memorization slots control what appears in the field radial.

## Implemented Pieces

### Tincture preparation and reserve

- The Ghastly Alembic has a dedicated third recipe input for a Bloody Flask or Bloody Jug. The main ingredient, consumed enzyme catalyst, and blood vessel are committed together only when the output can complete.
- Every one of the eight Muscle Memories has a flask recipe and a jug recipe.
- A flask tincture contains three five-minute doses. A jug contains six.
- Drinking a dose permanently teaches an unknown Muscle Memory and adds five minutes to its stored reserve, up to a thirty-minute cap.
- Reserve drains only while that memory is active. Learning remains after reserve exhaustion and death; prepared reserve and active state do not survive death.
- Empty tincture vessels return as Cured Clay Flasks or Cured Clay Jugs.

### Shared radial and activation

- Equipped Muscle Memories appear in the existing manipulation radial outside the fixed Blood Absorption and Blood Projection controls.
- Selecting an entry and pressing the manipulation-use control toggles that Muscle Memory on or off.
- One Muscle Memory may be active per vascular section. Activating another memory assigned to the same section replaces the former section assignment.
- Before the Reliquary is learned, newly drunk tinctures auto-equip into the first free shared memory slot. Once capacity is full, new knowledge remains learned and charged but cannot enter the field radial until the loadout is rearranged.
- The radial uses tincture icons for Muscle Memories. Empty reserve is grayscale; active reserve pulses; armed overexertion pulses faster; vascular damage adds progressively denser cracking; a dead section receives the strongest broken treatment and cannot activate.
- The Vascular Status display remains the detailed body-state view. No second full-screen Muscle Memory menu was added.

### Overexertion

- Sneak-use while an active Muscle Memory is selected arms its next valid trigger for ten seconds.
- The armed state is single-use and does not fire on an invalid action.
- A valid overexerted trigger attempts to pay twice the normal blood and two-and-a-half times the normal vascular strain.
- If the enhanced payment is unavailable, the action falls back to the ordinary trigger instead of silently deleting the prepared use.
- Each of the eight memories has an enhanced response rather than a universal flat stat multiplier.

### Mnemonic Reliquary

- The Reliquary now displays known Muscle Memories beside conventional memories and equips both into the same capacity.
- Tincture icons are grouped by their primary tendency and identify their vascular section in the tooltip.
- The recipe is now Third Degree.
- Filling all available memory slots produces the Alchemist-to-Mnemonist referral message.
- At Third Degree, asking the Mnemonist about the Reliquary teaches its craft. Completing the existing Woven Vessel handoff also teaches it as a fallback for players who never filled their slots or never used tinctures.
- Opening an already placed Reliquary teaches the recipe state, preserving access for existing worlds.
- Server-side Blood Structure validation rejects Reliquary construction until this lesson is recorded.

### Dendritic Distributor

- Saved patterns preserve mixed manipulation and Muscle Memory storage keys.
- The selected radial entry is restored whether it is a conventional memory or a Muscle Memory.
- Applying a pattern deactivates currently running Muscle Memories that are absent from the restored pattern.
- The preview uses tincture icons for Muscle Memories and calls the contents remembered powers rather than manipulations.

## Canonical Runtime Rules

1. Learned is permanent knowledge.
2. Reserve is stored usable tincture duration.
3. Equipped means the power occupies one shared radial slot.
4. Active means its assigned vascular section is currently spending reserve.
5. Triggered means a useful action has paid blood, applied strain, and produced the power's response.
6. Armed means the next valid trigger may overexert.

These states are separate. Drinking no longer immediately applies a long passive effect, and selecting a radial icon does not spend blood until the body performs that memory's meaningful action.

## Compatibility and Protected Boundaries

- Legacy manipulation slot strings remain valid. Muscle Memories use the namespaced storage form `muscle_memory:<id>`.
- Legacy timed priming data migrates into reserve on load.
- Blood Absorption, Blood Projection, and the fixed sickle entry retain their existing slot exemptions.
- The five vascular sections remain the only injury authority; tendency count does not create three additional body sections.
- Morphling resonance still modifies cost and strain through the shared trigger service.
- The Dendritic Distributor retains its existing Degree 5, blood, and XP costs.
- Consecrated Structures remain the name for existing structure/rite hybrids. No Hematic Relay or separate relay mechanic is introduced.

## Verification Gates

- Automated: state serialization, legacy migration, shared slot replacement, dose handling, vessel volume, radial presentation, overexertion payment, mixed loadout round-trip, compilation, and resource validation.
- Live client: verify radial spacing at multiple GUI scales, icon pulse readability, click/hold controls, Reliquary drag-and-drop, Distributor mixed previews, first-person/third-person body overlays, jug silhouettes, and combat/mining feel.
- Balance playtest: validate reserve consumption, bottle carry pressure, overexertion costs, Second Pulse survivability, Predatory Eyes target volume, and morphling resonance.
