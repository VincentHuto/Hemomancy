# Endgame Path Asymmetry: Fungal Gardens & Saint Mausoleums

Oct 27, 2026 · @Vi

## Overview

Each Degree 7 Harbinger path gets one home domain it enters cleanly, and a costly, dangerous way to trespass into the other path's domain. Neither path is locked out of anything; the choice decides where you belong, not what you may see.

**Degrees.** Every D7 player is an Archon. The two endings are both D8, each with its own title:

| D8 title | How a player ascends |
| --- | --- |
| Apotheos | Consumes the Qliphoth tree's pomes, then performs the Apotheosis Cardinal Rite |
| Silentium (colloquially "silent archon") | Defeats Vesper and absorbs him at the end of the fight |

Both ascend by consumption: the Apotheos lets the hivemind in, the Silentium takes Vesper's power by force. In code, D8 is a shared degree check plus a path flag (`NONE`, `APOTHEOS`, `SILENTIUM`). D7 is a full stage with its own content, not a stepping stone.

| Path | Home domain | Trespass into | How | Cost of trespass |
| --- | --- | --- | --- | --- |
| Apotheosis | Fungal Gardens (enter at will) | Saint Mausoleums | Hyphae shatter the sealed door | Enraged Saint, corrupted reward, hallowed ground burns them |
| Silentium | Saint Mausoleums (ring door, proper awakening) | Fungal Gardens | Possession rite through a surrogate | Focus counter, ejection, Mind Broken debuff |

Mausoleum exploration, lore and relic recovery stay open to every path. Only the Saint awakening is gated.

## Apotheosis in the Fungal Gardens

The Gardens treat an apotheosis player as part of the organism and everyone else as a host. The same systems run for everyone; only the outcome differs.

### Shared systems, split outcomes

| System | Apotheosis | Non-apotheosis |
| --- | --- | --- |
| Spore load | Fuel: powers manipulations, refills blood, emergency heal | Escalating stages: vision tint, hunger drain, aggro, colonization |
| Mycelium network | Outlines every entity standing on mycelium | Footsteps alert the network; hunter fungi converge |
| Terrain | Caps extend into bridges | Gills snap shut, puffballs burst |
| Tendency drift (Mortem/Ductilis) | Progress | Irreversible corruption; Unstained purity loss |
| Death | Regrow at a sporocarp; inventory held in a fruiting body | Corpse rises as a husk carrying their gear |

### No hunger, no eating

- Apotheosis players no longer need food. Hunger is pinned at 17: high enough to sprint, below the 18 natural-regen threshold, so they get no free regen.
- Everywhere else, food tastes of ash: effects apply, nutrition does not. Golden apples still work.
- Inside the Gardens dimension or the overworld Fungal Gardens biome, eating is impossible. The organism rejects it. Potions and milk are blocked too, deliberately. Any exception will be a custom crafted remedy (a morphic tonic, made by throwing a bloody jug into a morphic pool through the existing transmutation recipe) that clears all harmful effects and may also grant a beneficial one.
- The hunger bar is replaced by a spore-load bar.

### Replacement healing loop

- Burn spore load for an instant heal.
- Drain colonized hosts or husks.
- Sink into the mycelium to regrow: strong, but immobile.

### Privileges

- Recover Blood Memory fragments from colonized remains (the network remembers who died in it).
- Rendering pools: break items down into alchemical compounds.
- Grow Morphling Cradle grafts and Somatic Loom materials.
- D4/D5 mission capstones staged inside the Gardens.

### Threat that remains

An immune response (antibody entities) flags anything carrying too much autonomy or blood. An apotheosis player who overcasts gets read as foreign.

## Silentium possession rite

A Silentium never travels to the Gardens physically. They force their own will onto a piece of the organism, proving they are no longer governed by the hivemind, even subconsciously. The rite is an Invocation: repeatable and resource-gated.

### The echo

- On casting, a shadowy, monolithic echo of the player model stays at the rite site, visible to other players.
- The echo is not the player: no hitbox, no collision, invulnerable. All risk lives in the focus counter.
- On return, the Silentium manifests back into the echo and becomes corporeal again.

### Surrogates

The player drags a surrogate into the rite space. It decides what form they take in the Gardens.

| Surrogate | Gives | Costs |
| --- | --- | --- |
| Bat | Flight | Very low starting focus |
| Enderman | Teleporting | Cannot touch liquids |
| Blaze | Flight, fire projectiles (most fungal mobs are weak to fire) | Cannot touch liquids |
| Iron Golem | Very high focus pool, knockback | Very slow, poor jumping |
| Horse | High focus pool, strong jumping | No equipment |
| Donkey | Inventory access | Slower movement |
| Wolf | Extra speed | No equipment |
| Spider | Wall climbing | No equipment |
| Armor stand | Carries the stand's equipped gear into the Gardens | Gear is left behind in the Gardens on return |

Every mob surrogate already pays by bringing no equipment. Only strong ones (Enderman, Blaze, Iron Golem) carry an extra downside. Exact stats are still to be tuned.

- Mob abilities are hand-coded per mob, not data-driven. Third-party mob support is out of scope for now (future API/addon territory).
- Gear left behind sits on a colonized stand at the ejection point. It can be recovered on a later trip; left long enough, the Gardens digest it.

### Brazier offerings

Offerings in the rite's braziers tune the possession before casting:

| Offering | Effect |
| --- | --- |
| Mnemonic Ambergris | Passive focus regeneration (there is none by default) |
| Hematic iron | A few free hits: a focus shell that breaks on the first hits taken and never returns |
| Totem of Undying | Once per possession, a Mind Broken ejection is prevented and focus refills, like the totem's normal resurrection |
| Chimerization offering (item name TBD) | Bring a mob surrogate and an armor stand together, or fuse two mobs for both sets of abilities |

#### Wax tiers

A new middle tier, Corpse Wax, sits between Beeswax and Mnemonic Ambergris.

| Tier | Material | Sources |
| --- | --- | --- |
| 1 | Beeswax | Existing |
| 2 | Adipocere (corpse wax) | Found as loot; scraped from certain blocks or mobs; or crafted in a ghastly alembic from Beeswax, Mortem enzyme and a bleeding bulb (recipe tentative) |
| 3 | Mnemonic Ambergris | Existing |

Beyond Chimerization, offerings stay simple attribute tweaks.

### Focus

- Focus is a hit/concentration counter, separate from health. Taking damage ticks it down; at zero the Silentium is ejected.
- Abhorrent Thoughts drain focus rapidly by proximity and break it outright on a grab. They become hazards to route around, in the spirit of their winter-lantern / untouchable concepts.
- Their aura needs a readable edge (visual distortion, muffled sound) so drains never feel random.

Focus does not regenerate by default, even standing still. Regeneration is unlocked only by tuning the rite (Mnemonic Ambergris).

### Ejection and Mind Broken

- Returning voluntarily is clean.
- Being snapped back inflicts Mind Broken: Silentium powers temporarily diminished, the existing rogue wills invade more often, and whisper events trigger.
- A grab ejection is worse than running focus to zero.
- The rite cannot be cast again while Mind Broken, so the debuff doubles as the rite's cooldown.

## Saint Mausoleums

Mausoleums are open to every path for exploration, lore and relics. The Saint's chamber sits behind a sealed door, and how a player opens it decides the fight they get.

### Silentium: the ring door

- The Silentium projects into the door. Its three concentric rings fill with blood in sequence, then the door falls away.
- Each ring draws from the blood pool, so opening it is both a cost and a cutscene.
- The Saint wakes properly and the fight runs as designed, with the full reward.

### Apotheosis: the shattered door

- The apotheosis player projects mycelial hyphae into the stone. The door shatters.
- The fight starts immediately, against an enraged Saint (Terraria-style): opening phase skipped, shorter pauses between attacks, phases a normal fight never shows.
- Reward is reduced: a corrupted or partial relic, fragmented Saint memory.

Opened and shattered doors stay open permanently. Mausoleums spawn throughout the world, so on a server where one has been pillaged, players find another.

### Hallowed ground

- Mausoleum grounds are hallowed: consecrated ground rejecting the foreign apotheosis infection, not an antiseptic or medical cleansing (that register belongs to the Unstained).
- Inside, spore load drains continuously. Once it is empty, the burn moves to real health.
- Since spores are their emergency heal and food is unavailable, apotheosis players enter on a clock with no safety net.
- Visuals: smoke and charring rising from the skin, hyphae blackening and curling.

## Implementation notes (NeoForge 1.21.1)

Everything runs on capability flags and existing currencies (blood integers, spore load, tendencies). New registrations are limited to the husk, the antibody mobs, the echo entity and the door block.

| Feature | Approach |
| --- | --- |
| Block eating | Cancel `LivingEntityUseItemEvent.Start` for items with the `FOOD` data component, gated on the apotheosis flag (plus a dimension check for the Gardens-only rule) |
| Hunger pin | Hold food level at 17 each player tick, or mixin `FoodData.tick` |
| Hunger bar swap | `RenderGuiLayerEvent.Pre` on `VanillaGuiLayers.FOOD_LEVEL`, draw the spore-load bar instead |
| Possession model | Cancel `RenderPlayerEvent.Pre`, render the surrogate's model in place of the player |
| Focus counter | Intercept `LivingIncomingDamageEvent`, cancel damage, decrement focus on a capability; same HUD layer swap |
| Echo | Cosmetic entity, no hitbox, rendered with the player skin through a shadow shader |
| Return point | Stored on the player capability, not only the echo entity, to survive unloaded chunks, disconnects and crashes |
| Mob surrogate abilities | Hand-coded per supported mob |
| Ring door | Block entity + BER animating three rings filling in sequence, drawing from the blood pool |

## Open questions

- [ ] Morphic tonic: which beneficial effect, if any, it grants
- [ ] Chimerization: the offering item's name and recipe
- [ ] Surrogate stats: exact focus pools, speeds and ability tuning
