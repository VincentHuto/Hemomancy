# Harbinger Voyager NPCs V1 Design

## Summary

Active Harbinger voyager vessels should feel like neutral research expeditions rather than combat ships or merchant boats. V1 adds the social heart of those vessels: one guaranteed captain-scholar NPC and a rare junior Votary companion who is traveling to deepen their knowledge.

This design is intentionally dialogue-first. It creates atmosphere, faction texture, and future hooks without adding trade, quests, reputation, active ship AI, or progression rewards.

## Goals

- Give every active Harbinger vessel a clear resident authority figure.
- Keep Harbingers morally gray: curious, taboo, dangerous by reputation, but not simple villains.
- Tie active vessels back to deep-ocean vents, Erythrocoral reefs, and Harbinger Voyager Wrecks.
- Use NPC dialogue to make active ships feel inhabited before adding mechanics.
- Provide a clean future path for trade, rumors, expedition tasks, and faction tension.

## Non-Goals

- No trade table in V1.
- No quests, sample turn-ins, or reward state in V1.
- No active ship combat, boarding behavior, sailing AI, or faction reputation.
- No unique progression unlocks.
- No ordinary biome spawning for either NPC.

## NPCs

### Harbinger Voyager

The Harbinger Voyager is the captain-scholar and research lead of an active vessel.

Suggested id: `hemomancy:harbinger_voyager`

Role:
- Always spawns on each active Harbinger voyager vessel.
- Neutral dialogue NPC by default.
- Speaks with the confidence of a field captain and the curiosity of a natural philosopher.
- Frames ocean travel as covenant fieldwork: dangerous, sacred, practical, and unresolved.

Dialogue themes:
- Erythrocoral reefs as living ecologies, not corruption.
- Deep-ocean vents as mineral heat-scars and Ferric study sites.
- Chalybeate Snails as living material partners rather than simple resources.
- Sunken Voyager Wrecks as covenant tragedy and failed research.
- Brined Votaries as drowned remnants of duty, not a hostile faction.
- Active vessels as continuing research expeditions that have learned from older losses.

Player reactivity:
- Degree-aware dialogue should follow the existing Harbinger NPC pattern.
- Uninitiated players receive guarded but not immediately hostile explanations.
- Harbinger players receive more direct covenant language.
- Higher-degree Harbingers may hear more explicit concerns about fungal memory, reef behavior, and the risks of studying living hemomantic ecologies.
- Purifying or Clarity-bearing players can receive cooler, wary lines consistent with existing Harbinger NPC behavior, but V1 does not need combat escalation unless the shared NPC base demands it.

Held-item inquiry candidates:
- `hemomancy:salt_stained_voyager_log`
- `hemomancy:erythrocoral_fragment`
- `hemomancy:chalybeate_sclerite`
- Optional later: maps, specimen jars, reef blocks, field notes.

### Harbinger Votary Wayfarer

The Votary Wayfarer is a junior Harbinger tagging along to learn from the expedition.

Suggested id: `hemomancy:harbinger_votary_wayfarer`

Role:
- Spawns on active vessels only on a 1-in-5 roll.
- Neutral dialogue NPC.
- More personal, uncertain, and observant than the Voyager.
- Adds life to active vessels by showing that Harbinger expeditions include learners, not just authorities.

Dialogue themes:
- Wonder and anxiety about the sea.
- First-hand learning about reefs, vents, and wrecks.
- Trying to understand the difference between devotion, courage, curiosity, and obsession.
- Quiet discomfort around the wrecks and Brined Votaries.
- Respect for the Voyager without making the Voyager feel like a simple superior officer.

Player reactivity:
- Low-degree Harbingers are treated almost as peers.
- Higher-degree Harbingers may make the Votary nervous or reverent.
- Uninitiated players get simple explanations and cautious curiosity.
- Purifying or Clarity-bearing players get wary, brief dialogue rather than hostility in V1.

## Active Vessel Spawn Rule

The future active vessel structure should handle these NPCs in its `afterPlace()` hook:

- Spawn exactly one persistent `hemomancy:harbinger_voyager`.
- Roll `random.nextInt(5) == 0` to spawn one persistent `hemomancy:harbinger_votary_wayfarer`.
- Both NPCs should be structure-only and should not receive ordinary biome spawn placement.
- Both should spawn at stable deck/interior markers or at validated floor positions within the vessel bounding box.

## Dialogue Architecture

Use the existing dialogue-tree style used by Harbinger Vicar, Alchemist, and Mnemonist NPCs:

- Add dedicated dialogue tree factories, likely `HarbingerVoyagerDialogueTrees` and `HarbingerVotaryWayfarerDialogueTrees`.
- Use translatable language keys for all displayed lines.
- Use item inquiry JSON where possible for held-item questions.
- Keep any future event ids absent in V1 unless a concrete interaction needs them.

V1 dialogue should be reusable when trade or rumor systems arrive later. Lines should hint at ongoing research rather than promising mechanics that do not exist.

## Visual Direction

The Voyager should read as an ocean-field scholar: Harbinger robes or coat language adapted for wet decks, travel gear, chart case, diving or survey details, restrained hematic accents.

The Votary Wayfarer should look less authoritative: simpler Harbinger clothing, field satchel or sample straps, lighter gear, and an apprentice/research-pilgrim silhouette.

Both can initially follow existing Harbinger NPC renderer/model patterns if a unique model pass is too large for the active-vessel structure slice, but dedicated Blockbench sources are preferred before final polish.

## Testing Expectations

When implemented:
- Build with `./gradlew.bat build`.
- Verify `/locate structure` for active vessels once that structure exists.
- Confirm every active vessel spawns one Voyager.
- Confirm repeated generation shows the Votary Wayfarer about 1-in-5 times.
- Confirm neither NPC has ordinary biome spawn placement.
- Confirm dialogue opens and respects degree/purification/clarity branches.
- Confirm held-item inquiry works for the salt-stained log, Erythrocoral Fragment, and Chalybeate Sclerite.
- Confirm no old Forge imports, `SimpleChannel`, or MnA/Curios core imports are introduced.
