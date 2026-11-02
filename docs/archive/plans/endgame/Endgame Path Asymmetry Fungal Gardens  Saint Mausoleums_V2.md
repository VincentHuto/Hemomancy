# Endgame Path Asymmetry: Fungal Gardens & Saint Mausoleums (V2)

Oct 27, 2026 · @Vi · V2. V1 is kept alongside this file. Companion: `Fungal Gardens Purpose & Mausoleum Structure_V2.md`.

## Changes from V1

- Silentium is a D8 title equal to Apotheos. It is reached by absorbing the Memory of Vesper, not by the fight itself.
- Only Apotheos players enter the Gardens physically. Silentium players enter only through possession. The Overworld Fungal Gardens biome is a separate place and stays open to everyone.
- A Silentium's surrogate no longer walks into the Gardens as itself. Its traits are cast into the Gardens' warped facsimile of that creature.
- Apotheos players never eat. Their hunger is pinned at 17 everywhere, and the morphic tonic is the only exception. The "food tastes of ash" layer is gone.
- The sporocarp respawn and the corpse husks are replaced by the heart-node sanctum, a private Fane-like site that holds the respawn.
- Hallowed ground is consecrated, not holy. Its rejection of the hive is the Saints' own residue lashing out.
- Saint fights are D8. Outer mausoleum halls open from D3 to D4.
- Focus is renamed **Concentration** so it doesn't collide with the Cardinal Focus block or the Hematic Focus skill.
- Beeswax is replaced by honeycomb as wax tier 1.
- The implementation notes now follow the architecture rules.

## Overview

Each D8 path has a home domain it enters cleanly. It also has a costly, dangerous way to trespass into the other path's domain. Neither path is locked out of anything: the choice decides where you belong, not what you may see.

### Degrees

Every D7 player is an **Archon**. D7 is a full stage with its own content, not a stepping stone. After the first Spine projection the Archon chooses a path, and each path ends at an equal D8 rank.

| D8 title | How a player ascends |
| --- | --- |
| Apotheos | Chooses to accept, then performs the Rite of Apotheosis |
| Silentium (colloquially "silent archon") | Severs the Qliphoth tree (the Rite of Pruning), defeats Vesper, then absorbs the Memory of Vesper through the Iron Brazier |

- Both paths eat all nine pomes before the choice, and the ninth pome grants the Spine. The pomes don't separate the paths; what each does afterwards does.
- Both ascend by taking something in. The Apotheos lets the hivemind in. The Silentium takes Vesper's memory by force, after cutting their own antenna to the hive.
- In code, D8 is a shared degree check plus the path in `archon_path`: `NONE`, `SILENT_PENDING`, `SILENTIUM`, `APOTHEOS_PENDING`, `APOTHEOS`.

### Home domains and trespass

| Path | Home domain | Trespass into | How | Cost of trespass |
| --- | --- | --- | --- | --- |
| Apotheos | Fungal Gardens (enter at will) | Saint Mausoleums | Hyphae shatter the sealed door | Enraged Saint, fragmented reward, hallowed ground burns them |
| Silentium | Saint Mausoleums (ring door, proper awakening) | Fungal Gardens | Possession rite through a surrogate | Concentration counter, ejection, Mind Broken |

Mausoleum exploration, lore and outer-hall loot stay open to every path. Only the inner Chapels and the Saint awakening need D8.

### Who reaches the Gardens dimension

| Player | Access |
| --- | --- |
| Apotheos (D8) | Physical travel at will through the Fungal Spine or a Fungal Podium |
| Silentium (D8) | Possession rite only, never physically |
| Archon (D7), path unchosen or pending | Only the first, forced Spine projection (the revelation) |
| Unstained, unaligned, below D7 | Never. A far-future Unstained invasion rite stays parked. |

The Overworld Fungal Gardens biome is a different place. Anyone can walk it, and the D2 survey sends players there. The Apotheos food rules below still apply inside it.

## Apotheos in the Fungal Gardens

The Gardens treat an Apotheos body as part of the organism. They treat a possessing Silentium as a foreign will lodged in one of their own facsimiles. The same systems run for both, but the outcome differs.

### Shared systems, split outcomes

| System | Apotheos (own body) | Silentium (possessed facsimile) |
| --- | --- | --- |
| Spore load | Fuel: powers manipulations, refills blood, emergency heal | Not tracked. Concentration is the only meter. |
| Mycelium network | Outlines every entity standing on mycelium | Footsteps alert the network, and hunter fungi converge |
| Terrain | Caps extend into bridges | Gills snap shut, puffballs burst |
| Tendency drift (Mortem/Ductilis) | Progress | None. The facsimile, not the player, carries the drift. |
| Being struck down | Regrows at their heart-node sanctum (see below) | Ejected, with Mind Broken |

### Spore load

- Spore load gathers while an Apotheos stands on Gardens ground or mycelium, quickly in the dimension and slowly in the Overworld biome. It never gathers anywhere else.
- It replaces the hunger bar on the HUD.
- Hallowed ground drains it (see Saint Mausoleums).

### No eating

- Apotheos players don't eat, anywhere. Every food item is refused, and hunger is pinned at 17. That is high enough to sprint but below the natural-regen threshold of 18, so they get no free regen.
- The one exception is the **morphic tonic**, and only inside the Gardens dimension or the Overworld biome. It is made by throwing a bloody vessel into a morphic pool, using the existing `hemomancy:morphic_nectar` transmutation. It clears all harmful effects and may grant a beneficial one.
- Inside the dimension or the biome, potions and milk are refused too. The tonic is the only remedy there.

### Replacement healing loop

- Burn spore load for an instant heal.
- Drain colonized facsimiles.
- Sink into the mycelium to regrow. This is strong, but leaves them immobile.

### Heart-node sanctum

- An Apotheos can plant a heart-node in the Gardens. It grows into a private, Fane-like sanctum that belongs to them alone.
- It sets their spawn, and it grants a bonus while they stand inside it.
- It is the only place they can **molt** (see the companion doc).
- When an Apotheos dies, they regrow at the heart-node. Their inventory is held in a fruiting body at the death point instead of scattering, and it never despawns.
- This is not the Covenant Throne. The Throne stays the D6 respawn for bloodline progenitors after D5.

### Privileges

- Recover Blood Memory fragments from colonized remains. The network remembers who died in it.
- Rendering pools: break items down into alchemical compounds.
- Grow Morphling Cradle grafts and Somatic Loom materials.
- Walk the Archive freely, where the Saints' recollections are held (see the companion doc).

V1's "D4/D5 mission capstones staged in the Gardens" is dropped, because no player below D8 Apotheos enters physically.

### Threat that remains

An immune response (antibody entities) flags anything carrying too much autonomy or blood. An Apotheos who overcasts gets read as foreign.

## Silentium possession rite

A Silentium never travels to the Gardens in their own body. They force their will onto a piece of the organism, proving they are no longer governed by the hivemind, even subconsciously. The rite is an Invocation: repeatable and resource-gated.

### The echo

- On casting, a Dark Souls-style echo of the player stays at the rite site, visible to other players. It is semi-corporeal: shadowy, monolithic, rendered with the player's skin.
- The echo is not the player. It has no hitbox and no collision, and it can't be hurt. All the risk lives in the Concentration counter.
- On return, the Silentium manifests back into the echo and becomes corporeal again.

### Surrogates and their facsimiles

The player drags a surrogate into the rite space. The Gardens already hold warped facsimiles of these creatures: the hive's attempts to learn and recreate Earth life, gotten grossly wrong. The rite reads the surrogate and casts the Silentium's will into its facsimile. The surrogate decides the form, and the facsimile is what actually walks the Gardens.

| Surrogate | Gardens facsimile | Gives | Costs |
| --- | --- | --- | --- |
| Bat | Warped bat | Flight | Very low starting Concentration |
| Enderman | Warped enderman | Teleporting | Cannot touch liquids |
| Blaze | Warped blaze | Flight, fire projectiles (most fungal mobs are weak to fire) | Cannot touch liquids |
| Iron Golem | Warped golem | Very high Concentration pool, knockback | Very slow, poor jumping |
| Horse | Warped horse | High Concentration pool, strong jumping | No equipment |
| Donkey | Warped donkey | Inventory access | Slower movement |
| Wolf | Warped wolf | Extra speed | No equipment |
| Spider | Warped spider | Wall climbing | No equipment |
| Armor stand | Fungal mannequin wearing the stand's gear | Carries the stand's equipped gear into the Gardens | Gear is left behind in the Gardens on return |

- Every mob surrogate already pays by bringing no equipment. Only the strong ones (Enderman, Blaze, Iron Golem) carry an extra downside. Exact stats are still to be tuned.
- The facsimiles also roam the Archive as ordinary Gardens fauna. Possession borrows one; it doesn't create one.
- Facsimile abilities are hand-coded per mob, not data-driven. Supporting third-party mobs is out of scope for now (future API or addon territory).
- The surrogate stays stilled in the rite space for the whole visit. A clean return releases it unharmed. A Mind Broken ejection kills it, because the organism keeps what it took.
- Gear left behind sits on a colonized stand at the ejection point. It can be recovered on a later trip; left long enough, the Gardens digest it.

### Brazier offerings

Offerings placed in the rite's braziers before casting tune the possession:

| Offering | Effect |
| --- | --- |
| Mnemonic Ambergris | Passive Concentration regeneration (there is none by default) |
| Hematic Iron Block | A few free hits: a Concentration shell that breaks on the first hits taken and never returns |
| Totem of Undying | Once per possession, prevents a Mind Broken ejection and refills Concentration, like the totem's normal resurrection |
| Chimerization offering (item name TBD) | Bring a mob surrogate and an armor stand together, or fuse two mobs: the facsimile grows into a chimera with both sets of abilities |

#### Wax tiers

A new middle tier, Adipocere (corpse wax), sits between honeycomb and Mnemonic Ambergris.

| Tier | Material | Sources |
| --- | --- | --- |
| 1 | Honeycomb | Vanilla |
| 2 | Adipocere (corpse wax) | Found as loot, scraped from certain blocks or mobs, or distilled in the Ghastly Alembic from honeycomb, Ruinous Enzyme and a Bleeding Bulb (recipe tentative) |
| 3 | Mnemonic Ambergris | Existing |

Beyond Chimerization, offerings stay simple attribute tweaks.

### Concentration

- Concentration is a hit counter, separate from health. Taking damage ticks it down; at zero the Silentium is ejected.
- It does not regenerate by default, even standing still. Only a Mnemonic Ambergris offering unlocks regeneration.
- Abhorrent Thoughts drain Concentration rapidly by proximity, and a grab breaks it outright. They become hazards to route around, in the spirit of their winter-lantern / untouchable concepts.
- Their aura needs a readable edge (visual distortion, muffled sound) so drains never feel random.

### Ejection and Mind Broken

- Returning voluntarily is clean.
- Being snapped back inflicts **Mind Broken**:
  - Silentium powers are temporarily diminished.
  - Will ambushes come more often.
  - Whisper events trigger.
- A grab ejection is worse than running Concentration to zero.
- The rite cannot be cast again while Mind Broken, so the debuff doubles as the rite's cooldown. Milk must not clear it.

## Saint Mausoleums

Mausoleums are open to every path for exploration and outer-hall loot. Layout, Chapels and gifts are in the companion doc. This section covers only how each D8 path opens the Saint's chamber.

### Silentium: the ring door

- Each completed Chapel lights up and fills its node on the door.
- With every node lit, the Silentium projects into the door to connect them. The three concentric rings fill with blood in sequence, then the door falls away.
- Each ring draws from the Silentium's blood, so opening it is both a cost and a cutscene. The blood flows toward the chamber; at Hemorath's door it visibly runs to him, which makes the shared mechanic his signature.
- The Saint wakes properly and the fight runs as designed, with the full reward.

### Apotheos: the shattered door

- The Apotheos projects mycelial hyphae into the stone, and the door shatters. This can happen at any time, skipping every Chapel.
- **In gameplay terms** it plays like a Terraria-style enrage: the opening phase is skipped, pauses between attacks are shorter, and some phases appear that a normal fight never shows.
- **In lore terms** the enrage is the sum of what the player refused to see. The Saint fights with the power of every Chapel the player skipped. Skip the Downfall and it fights with its fall; skip the Repose and it never pauses.
- The reward is reduced: a fragment of Hallowed Residuum instead of a whole one.

Opened and shattered doors stay open permanently. Mausoleums generate throughout the world, so on a server where one has been pillaged, players find another.

### Hallowed ground

- Mausoleum grounds are hallowed: consecrated, not holy. Every Saint fell to the power of Hemomancy, and that fall was their downfall. Their residual energy lashes out against the hive's reach wherever they rest.
- The rejection is aimed at the hive's will, not at fungal matter. Fungal growth in the tombs and the Saints' own infection are untouched. What the ground refuses is a body the hive speaks through.
- Only Apotheos bodies are burned. Silentium players are tolerated, because they cut their antenna to the hive. Lower Harbingers in the outer halls are too faint a signal to provoke it.
- Inside, spore load drains continuously. Once it is empty, the burn moves to real health.
- Spore load is their emergency heal and food is unavailable, so an Apotheos enters on a clock with no safety net.
- Visuals: smoke and charring rising from the skin, hyphae blackening and curling.

## Implementation notes (NeoForge 1.21.1)

Player state lives in attachments: register them through `HemoAttachmentTypes`, read and write them through `HemoCapabilityAccess`, and sync to the owner through the `PlayerStateSync` table. The mod is not live, so no `PlayerDataMigrations` entry is needed for the path rename.

| Feature | Approach |
| --- | --- |
| Silentium at D8 | Memory of Vesper's Blood Absorption completion advances to 8 through `DegreeProgression`. That currently forces `APOTHEOS` at 8, so it needs to take the path. Rename `SILENT_ARCHON` to `SILENTIUM` (about 89 references, including the Silent Archon armor and chamber effects). Change `HarbingerPathPermissions.isProvenSilentArchon` from `degree == 7` to D8. Give degree 8 a per-path lang title. |
| Physical travel gate | `FungalGardenTravelHelper.handleTravelUse` and `FungalPodiumBlock` require `APOTHEOS`. The first forced projection keeps its current gate. |
| Food refusal | Cancel `LivingEntityUseItemEvent.Start` for `FOOD` items on Apotheos players. Allow a `hemomancy:apotheos_consumable` tag (the tonic) inside the dimension or biome. Potions and milk are refused there too. |
| Hunger pin | Hold food at 17 on the player tick. Reconcile this with morphling upkeep (`EquippedMorphlingEvents`) and the food snapshot in `ChamberVisitService`. |
| Spore load, Concentration | One attachment each, synced through `PlayerStateSync` |
| HUD | Cancel the vanilla `FOOD_LEVEL` layer in `RenderGuiLayerEvent.Pre`, as `FungalProjectionClientEvents` already does for the hotbar. Draw the spore and Concentration bars as layers in `GuiLayerRegistrar`, never in a `RenderGuiEvent` handler. |
| Possession | Build on `ChamberVisitService`: the body stays behind, the inventory is snapshotted, the visit-safety rules apply, and `PendingDelivery` defers during observational visits. Reuse `FungalProjectionEvents`' damage cancel and interaction lock, routing damage into Concentration through `LivingIncomingDamageEvent`. |
| Return point | Stored in an attachment, not `getPersistentData` (the old fungal return keys are allow-listed legacy). It survives unloaded chunks, disconnects and crashes. |
| Possession rite | A Harbinger Cardinal Rite with the `bond` purpose. Mark it `"special": true` only if no Communion floor fits. A new "Invocation" purpose would ripple through the purpose data tests and the recipe map groups. The surrogate must stand in the rite space, and offerings follow the shared Brazier lifecycle. |
| Mind Broken | A `MobEffect` with no cures (milk does nothing), or a `PlayerCooldowns` entry. Hook it into the `WillAmbushDirector` chance multipliers and `FungalWhisperEvents`. |
| Echo | A cosmetic entity with no hitbox, rendered with the player skin through a cached shadow render type. Shared noise goes through the shader includes. |
| Facsimiles | One entity per surrogate, also spawning as Gardens fauna. Possession abilities are hand-coded per mob. |
| Ring door | Block entity plus BER, with node state in the block entity and `BlockEntitySync.markAndSync`. Blood is paid through `BloodAccounting.pay` with a named `Source`. |
| Hallowed ground | A mausoleum bounding-box check that drains spore load, then health |
| Abhorrent Thought | Rename the misspelled registry id `abhorent_thought` to `abhorrent_thought` (plus lang, assets and biome spawns), then rework it into the drain-aura and grab hazard |
| Network | Every new payload bumps `PacketHandler.PROTOCOL_VERSION` and rewrites `payload-registry.txt` |

New registrations:
- **Entities:** echo, nine facsimiles, antibodies, hunter fungi
- **Blocks:** ring door, heart-node, colonized stand, rendering pool, fruiting-body inventory cache
- **Items:** adipocere, morphic tonic, chimerization offering, Hallowed Residuum fragments
- **Other:** the Mind Broken effect, two attachments, two HUD layers, the possession rite recipe

## Canon changes this design makes

These lines in `docs/LORE_REFERENCE.md` (LR) and `docs/HEMOMANCY_REFERENCE.md` (HR) need rewriting when the design is adopted:

- **LR:34, LR:352, HR:1150-1154, `wiki/Harbinger-Path.md:537`:** Silent stays at D7. It should become Silentium at D8, equal to Apotheos.
- **LR:268:** the Vicar's line "the eighth degree is silence". It now reads straight rather than ironically, so keep it or retire it.
- **LR:131:** "not physically transported" holds only for the first projection and the Silentium. Apotheos travel is physical.
- **HR:864:** the Spine travel gate.
- **HR:868:** the dimension key there is wrong. It is `fungal_gardens`, not `the_fungal_gardens`.

## Open questions

- [ ] Morphic tonic: which beneficial effect, if any, it grants, and which bloody vessel it uses (there is a `bloody_flask` but no "jug")
- [ ] Chimerization: the offering item's name and recipe
- [ ] Surrogate stats: exact Concentration pools, speeds and ability tuning
- [ ] Silentium D8 powers: what Mind Broken diminishes, and what the Silentium gains over a D7 Archon
- [ ] Spore load: gain rates, burn costs, and what the sanctum bonus is
