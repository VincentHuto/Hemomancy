# Registered mob drop catalogue

Source audit: 2026-10-06, after the first mob-drop consolidation pass. Registry IDs remain authoritative. This catalogue distinguishes authored loot, biological samples and live harvests; a source audit is not natural-spawn or runtime acceptance.

Coverage: 129 `EntityInit` registrations. Mob ancestry is followed through local classes; non-mob registrations are listed separately. Each mob entry includes its entity source and ancestors, complete authored loot pools, blood profile, authored spawn packs and interaction/drop hooks. Global modifiers and source limitations follow the roster.

## Shared material decisions

| Material | Preserved ID | Added sources |
| --- | --- | --- |
| Chromatophores | `cuttlefish_chromatophores` | Comb jelly, Luminal Cicada |
| Enthralling Filament | `puppeteering_thread` | Peacock Spider, Hemojelly |
| Barbed Splinter | `calcified_blood_spine` | Excoriated, Venous Strider, Vampire Bat |
| Chitinous Husk | `chitinous_husk` | Chiton, Bombardier; Peacock Spider 10% |
| Venous Pinion | `venous_pinion` | Choir Keeper |
| Ganglion Cluster | `ganglion_cluster` | Siphonophore |

Display changes: **Cuttlefish Chromatophores → Chromatophores**, **Puppeteering Thread → Enthralling Filament**. Barbed Splinter was already the current display name for `calcified_blood_spine`; Chitinous Husk, Venous Pinion and Ganglion Cluster retain their names.

Existing named sources and unrelated drops remain. New ordinary material pools are player-kill gated, 1–2 items plus 0–1 per Looting level. Spider husks are one item at 10%, without Looting. Foods have ordinary unrestricted death loot and burning conversion. Queen’s Physogastrism (`queens_physogastrism`), Telson, Toxicognath, Fargone Proboscis and Naeglerophaeon Ganglion retain their existing probabilities and roles. No additional mob assignments were adopted.

## Live collection and rewards

- Chalybeate Snail: knap a living retracted snail with the accepted tool; 1 Sclerite, 18% chance of a second, with the existing cooldown and tool wear. Death scrap remains separate.
- Mnemonic Whale: bottle collection and natural ambergris shedding share their existing waiting period. Each whale lethal bite on eligible squid/cuttle prey yields exactly one ambergris independently.
- Verdigris Moth: brushing and natural shedding supply Virid Salis Trail with their existing cooldowns.
- Hematic Burrower: escape digging can shed coal/raw copper/raw iron; this is not a death pool.
- Choir Keeper: successful Enderman hunting can supply an Ender Pearl independently of the new player-kill Pinion pool.
- Hagfish: feeding interaction is a behavior path; it does not make its empty death table a material source.
- Will: school-specific enzyme and occasional Faded Memory rewards are authored in its class; the configured-form path and fallback path have different conditions.
- All eligible living mobs also use shared syringe sampling and data-driven Specimen Jar capture. A jar preserves an entity rather than yielding a new ordinary material. NPC gifts, trades, advancement rewards and boss-progression callbacks are separate from death loot; source hooks below identify those boundaries.

## Mob roster

### `unstained_zealot`

- Class/category: `UnstainedZealotEntity` / `CREATURE`; ancestry: `UnstainedZealotEntity` → `PathfinderMob`.
- Source: [UnstainedZealotEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedZealotEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [UnstainedZealotEntity:76](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedZealotEntity.java:76) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `unstained_guardian`

- Class/category: `UnstainedGuardianEntity` / `CREATURE`; ancestry: `UnstainedGuardianEntity` → `PathfinderMob`.
- Source: [UnstainedGuardianEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedGuardianEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [UnstainedGuardianEntity:81](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedGuardianEntity.java:81) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `unstained_acolyte`

- Class/category: `UnstainedAcolyteEntity` / `CREATURE`; ancestry: `UnstainedAcolyteEntity` → `PathfinderMob`.
- Source: [UnstainedAcolyteEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedAcolyteEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [UnstainedAcolyteEntity:83](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedAcolyteEntity.java:83) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `unstained_scout`

- Class/category: `UnstainedScoutEntity` / `CREATURE`; ancestry: `UnstainedScoutEntity` → `PathfinderMob`.
- Source: [UnstainedScoutEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedScoutEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [UnstainedScoutEntity:86](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedScoutEntity.java:86) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `spectral_companion`

- Class/category: `PaleIntercessionEntity` / `MISC`; ancestry: `PaleIntercessionEntity` → `PathfinderMob`.
- Source: [PaleIntercessionEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/PaleIntercessionEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `phantasmal_echo`

- Class/category: `PhantasmalEchoEntity` / `MISC`; ancestry: `PhantasmalEchoEntity` → `PathfinderMob`.
- Source: [PhantasmalEchoEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/PhantasmalEchoEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `harbinger_hermit`

- Class/category: `HarbingerHermitEntity` / `CREATURE`; ancestry: `HarbingerHermitEntity` → `PathfinderMob`.
- Source: [HarbingerHermitEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerHermitEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerHermitEntity:373](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerHermitEntity.java:373) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `harbinger_alchemist`

- Class/category: `HarbingerAlchemistEntity` / `CREATURE`; ancestry: `HarbingerAlchemistEntity` → `com`.
- Source: [HarbingerAlchemistEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:hemolytic_solution","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerAlchemistEntity:96](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java:96) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `harbinger_artificer`

- Class/category: `HarbingerArtificerEntity` / `CREATURE`; ancestry: `HarbingerArtificerEntity` → `com`.
- Source: [HarbingerArtificerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerArtificerEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerArtificerEntity:73](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerArtificerEntity.java:73) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `harbinger_cicatrix_anchorite`

- Class/category: `HarbingerCicatrixAnchoriteEntity` / `CREATURE`; ancestry: `HarbingerCicatrixAnchoriteEntity` → `com`.
- Source: [HarbingerCicatrixAnchoriteEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerCicatrixAnchoriteEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerCicatrixAnchoriteEntity:73](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerCicatrixAnchoriteEntity.java:73) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `harbinger_mnemonist`

- Class/category: `HarbingerMnemonistEntity` / `CREATURE`; ancestry: `HarbingerMnemonistEntity` → `com`.
- Source: [HarbingerMnemonistEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerMnemonistEntity:80](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java:80) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `harbinger_vicar`

- Class/category: `HarbingerVicarEntity` / `CREATURE`; ancestry: `HarbingerVicarEntity` → `com`.
- Source: [HarbingerVicarEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerVicarEntity:193](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java:193) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [HarbingerVicarEntity:285](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java:285) `spawnAtLocation(ledger);`.

### `harbinger_voyager`

- Class/category: `HarbingerVoyagerEntity` / `CREATURE`; ancestry: `HarbingerVoyagerEntity` → `PathfinderMob`.
- Source: [HarbingerVoyagerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVoyagerEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerVoyagerEntity:72](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVoyagerEntity.java:72) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `harbinger_votary_wayfarer`

- Class/category: `HarbingerVotaryWayfarerEntity` / `CREATURE`; ancestry: `HarbingerVotaryWayfarerEntity` → `PathfinderMob`.
- Source: [HarbingerVotaryWayfarerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVotaryWayfarerEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [HarbingerVotaryWayfarerEntity:72](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVotaryWayfarerEntity.java:72) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `circus_fire_eater`

- Class/category: `CircusFireEaterEntity` / `CREATURE`; ancestry: `CircusFireEaterEntity` → `CircusPerformerEntity` → `PathfinderMob`.
- Source: [CircusFireEaterEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusFireEaterEntity.java).
- Source: [CircusPerformerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [CircusPerformerEntity:270](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java:270) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `circus_stilt_walker`

- Class/category: `CircusStiltWalkerEntity` / `CREATURE`; ancestry: `CircusStiltWalkerEntity` → `CircusPerformerEntity` → `PathfinderMob`.
- Source: [CircusStiltWalkerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusStiltWalkerEntity.java).
- Source: [CircusPerformerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [CircusPerformerEntity:270](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java:270) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `circus_acrobat`

- Class/category: `CircusAcrobatEntity` / `CREATURE`; ancestry: `CircusAcrobatEntity` → `CircusPerformerEntity` → `PathfinderMob`.
- Source: [CircusAcrobatEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusAcrobatEntity.java).
- Source: [CircusPerformerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [CircusPerformerEntity:270](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java:270) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `circus_knife_thrower`

- Class/category: `CircusKnifeThrowerEntity` / `CREATURE`; ancestry: `CircusKnifeThrowerEntity` → `CircusPerformerEntity` → `PathfinderMob`.
- Source: [CircusKnifeThrowerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusKnifeThrowerEntity.java).
- Source: [CircusPerformerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [CircusPerformerEntity:270](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusPerformerEntity.java:270) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `circus_ringmaster`

- Class/category: `CircusRingmasterEntity` / `CREATURE`; ancestry: `CircusRingmasterEntity` → `PathfinderMob`.
- Source: [CircusRingmasterEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusRingmasterEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [CircusRingmasterEntity:304](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusRingmasterEntity.java:304) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `peacock_spider`

- Class/category: `PeacockSpiderEntity` / `CREATURE`; ancestry: `PeacockSpiderEntity` → `Spider`.
- Source: [PeacockSpiderEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/PeacockSpiderEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:puppeteering_thread","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:chitinous_husk"}],"conditions":[{"condition":"minecraft:killed_by_player"},{"condition":"minecraft:random_chance","chance":0.1}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_peacock_spider`: #hemomancy:peacock_spider_spawnlist (weight 5, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `vampire_bat`

- Class/category: `VampireBatEntity` / `AMBIENT`; ancestry: `VampireBatEntity` → `Bat`.
- Source: [VampireBatEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VampireBatEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:calcified_blood_spine","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_vampire_bat`: #hemomancy:vampire_bat_spawnlist (weight 4, 8–20).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `hematic_construct`

- Class/category: `HematicConstructEntity` / `MONSTER`; ancestry: `HematicConstructEntity` → `Monster`.
- Source: [HematicConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/HematicConstructEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:hematic_iron_scrap","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":3.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","weight":1,"name":"hemomancy:crude_memory_deadly_gaze"},{"type":"minecraft:empty","weight":9}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["ferric"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `hemorath`

- Class/category: `HemorathEntity` / `MONSTER`; ancestry: `HemorathEntity` → `SaintTrialEntity` → `Monster`.
- Source: [HemorathEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/hemorath/HemorathEntity.java).
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Encounter reward: `SaintTrialEntity` rolls `loot_table/entities/hemorath.json` once when the encounter ends (overload/exsanguination or health loss), and returns the Saint to its restored sarcophagus. Vanilla death loot does not roll a second time. The table gives one `hallowed_residuum_hemorath` and 1–2 `sanguine_quintessence`. It replaces the former `hollow_vessel.json` (Hemorath's old working title), which held only the quintessence pool.
- Interaction/drop hooks: the shared Saint reward roll and tomb return live in `SaintTrialEntity`; no Saint keeps its own residuum drop.

### `seraphae`

- Class/category: `SeraphaeEntity` / `MONSTER`; ancestry: `SeraphaeEntity` → `SaintTrialEntity` → `Monster`.
- Source: [SeraphaeEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/seraphae/SeraphaeEntity.java).
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Encounter reward: `SaintTrialEntity` rolls `loot_table/entities/seraphae.json` once when the encounter ends (full containment), and returns the Saint to its restored sarcophagus. Vanilla death loot does not roll a second time. The table gives one `hallowed_residuum_seraphae` and 1–2 `sanguine_quintessence`.
- Interaction/drop hooks: the shared Saint reward roll and tomb return live in `SaintTrialEntity`; no Saint keeps its own residuum drop.

### `seraphae_fragment`

- Class/category: `SeraphaeFragmentEntity` / `MONSTER`; ancestry: `SeraphaeFragmentEntity` → `Monster`.
- Source: [SeraphaeFragmentEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/seraphae/SeraphaeFragmentEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `annetta_knowles`

- Class/category: `AnnettaKnowlesEntity` / `MONSTER`; ancestry: `AnnettaKnowlesEntity` → `Monster`.
- Source: [AnnettaKnowlesEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/AnnettaKnowlesEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [AnnettaKnowlesEntity:159](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/AnnettaKnowlesEntity.java:159) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `stained_priestess`

- Class/category: `StainedPriestessEntity` / `MONSTER`; ancestry: `StainedPriestessEntity` → `Monster`.
- Source: [StainedPriestessEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/StainedPriestessEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [StainedPriestessEntity:243](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/StainedPriestessEntity.java:243) `protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {`; [StainedPriestessEntity:244](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/StainedPriestessEntity.java:244) `super.dropCustomDeathLoot(level, source, recentlyHit);`; [StainedPriestessEntity:245](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/StainedPriestessEntity.java:245) `this.spawnAtLocation(new ItemStack(ItemInit.annettas_sanguis_lancea.get()));`; [StainedPriestessEntity:246](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/StainedPriestessEntity.java:246) `this.spawnAtLocation(new ItemStack(ItemInit.hematic_iron_scrap.get(), 4));`.

### `latent_annetta_infection`

- Class/category: `LatentAnnettaInfectionEntity` / `MONSTER`; ancestry: `LatentAnnettaInfectionEntity` → `Monster`.
- Source: [LatentAnnettaInfectionEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [LatentAnnettaInfectionEntity:217](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java:217) `protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {`; [LatentAnnettaInfectionEntity:218](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java:218) `super.dropCustomDeathLoot(level, source, recentlyHit);`; [LatentAnnettaInfectionEntity:219](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java:219) `this.spawnAtLocation(new ItemStack(ItemInit.annettas_absolution_dagger.get()));`; [LatentAnnettaInfectionEntity:220](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java:220) `this.spawnAtLocation(new ItemStack(ItemInit.pale_silver_ingot.get(), 3));`.

### `putriciel`

- Class/category: `PutricielEntity` / `MONSTER`; ancestry: `PutricielEntity` → `SaintTrialEntity` → `Monster`.
- Source: [PutricielEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/putriciel/PutricielEntity.java).
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Encounter reward: `SaintTrialEntity` rolls `loot_table/entities/putriciel.json` once when the encounter ends (the fifth absolution), and returns the Saint to its restored sarcophagus. Vanilla death loot does not roll a second time. The table gives one `hallowed_residuum_putriciel` and 1–2 `sanguine_quintessence`. It replaces the former direct residuum drop in `PutricielEntity`.
- Interaction/drop hooks: the shared Saint reward roll and tomb return live in `SaintTrialEntity`; no Saint keeps its own residuum drop.

### `velorum`

- Class/category: `VelorumEntity` / `MONSTER`; ancestry: `VelorumEntity` → `SaintTrialEntity` → `Monster`.
- Source: [VelorumEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/velorum/VelorumEntity.java).
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Encounter reward: `SaintTrialEntity` rolls `loot_table/entities/velorum.json` once when the encounter ends (defeat; he reverts to dormancy), and returns the Saint to its restored sarcophagus. Vanilla death loot does not roll a second time. The table gives one `hallowed_residuum_velorum` and 1–2 `sanguine_quintessence`. It replaces the former direct residuum drop in `VelorumEntity.dropCustomDeathLoot`.
- Interaction/drop hooks: the shared Saint reward roll and tomb return live in `SaintTrialEntity`; no Saint keeps its own residuum drop.

### `vesper_crowned_refusal`

- Class/category: `VesperTheCrownedRefusalEntity` / `MONSTER`; ancestry: `VesperTheCrownedRefusalEntity` → `Monster`.
- Source: [VesperTheCrownedRefusalEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [VesperTheCrownedRefusalEntity:227](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java:227) `protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {`.

### `vesper_evening_star`

- Class/category: `VesperTheEveningStarEntity` / `MONSTER`; ancestry: `VesperTheEveningStarEntity` → `Monster`.
- Source: [VesperTheEveningStarEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:memory_of_vesper"}]}`
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [VesperTheEveningStarEntity:119](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java:119) `protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {`.

### `mycophant`

- Class/category: `MycophantEntity` / `MONSTER`; ancestry: `MycophantEntity` → `Monster`.
- Source: [MycophantEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/MycophantEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:mycophant_tendril"}]}`
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [MycophantEntity:62](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/MycophantEntity.java:62) `protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {`; [MycophantEntity:63](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/MycophantEntity.java:63) `if (this.encounterOwner == null) super.dropCustomDeathLoot(level, damageSource, recentlyHit);`.

### `leech`

- Class/category: `LeechEntity` / `CREATURE`; ancestry: `LeechEntity` → `Animal`.
- Source: [LeechEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/LeechEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:swollen_leech","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `bloodlicker`

- Class/category: `BloodlickerEntity` / `MONSTER`; ancestry: `BloodlickerEntity` → `LeechEntity` → `Animal`.
- Source: [BloodlickerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/BloodlickerEntity.java).
- Source: [LeechEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/LeechEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `bog_revenant`

- Class/category: `BogRevenantEntity` / `MONSTER`; ancestry: `BogRevenantEntity` → `Monster`.
- Source: [BogRevenantEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BogRevenantEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"minecraft:rotten_flesh","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:vivianite_cluster"}],"conditions":[{"condition":"minecraft:killed_by_player"},{"condition":"minecraft:random_chance","chance":0.12}]}`
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `add_bog_revenant`: #hemomancy:bog_body_spawnlist (weight 8, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `fargone`

- Class/category: `FargoneEntity` / `MONSTER`; ancestry: `FargoneEntity` → `Monster`.
- Source: [FargoneEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/FargoneEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:fargone_proboscis","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `drifting_mycelium` (weight 8, 1–2); `fungal_gardens` (weight 10, 8–8); `hyphal_spires` (weight 10, 1–3); `sporecrown_thicket` (weight 6, 1–3).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `desiccant`

- Class/category: `DesiccantEntity` / `MONSTER`; ancestry: `DesiccantEntity` → `Monster`.
- Source: [DesiccantEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/DesiccantEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:telson","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["flammeus"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_desiccant`: #hemomancy:desiccant_spawnlist (weight 6, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `thirster`

- Class/category: `ThirsterEntity` / `MONSTER`; ancestry: `ThirsterEntity` → `Monster`.
- Source: [ThirsterEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/ThirsterEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","weight":3,"name":"hemomancy:bloody_flask"},{"type":"minecraft:empty","weight":7}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 3: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","weight":1,"name":"hemomancy:crude_memory_blood_shot"},{"type":"minecraft:item","weight":1,"name":"hemomancy:crude_memory_blood_rush"},{"type":"minecraft:empty","weight":18}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `fungal_isles` (weight 10, 1–4).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `abhorrent_thought`

- Class/category: `AbhorrentThoughtEntity` / `MONSTER`; ancestry: `AbhorrentThoughtEntity` → `Monster`.
- Source: [AbhorrentThoughtEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/AbhorrentThoughtEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `drifting_mycelium` (weight 15, 1–3); `fungal_gardens` (weight 10, 8–8); `fungal_isles` (weight 8, 1–3); `hemorrhagic_plateau` (weight 6, 1–2); `hyphal_spires` (weight 12, 1–4).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `erythromycelium_eruptus`

- Class/category: `ErythromyceliumEruptusEntity` / `MONSTER`; ancestry: `ErythromyceliumEruptusEntity` → `Monster`.
- Source: [ErythromyceliumEruptusEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/ErythromyceliumEruptusEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:spore_sac","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `mycelial_depths` (weight 8, 1–2); `sporecrown_thicket` (weight 15, 1–3).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `blood_drunk_puppeteer`

- Class/category: `BloodDrunkPuppeteerEntity` / `MONSTER`; ancestry: `BloodDrunkPuppeteerEntity` → `Monster`.
- Source: [BloodDrunkPuppeteerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:puppeteering_thread","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_blood_drunk_puppeteer`: #hemomancy:blood_drunk_puppeteer_spawnlist (weight 24, 1–1); `hemorrhagic_plateau` (weight 3, 1–1); `sporecrown_thicket` (weight 4, 1–1).
- Interaction/drop hooks: [BloodDrunkPuppeteerEntity:134](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java:134) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [BloodDrunkPuppeteerEntity:139](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java:139) `return super.mobInteract(player, hand);`.

### `excoriated`

- Class/category: `ExcoriatedEntity` / `MONSTER`; ancestry: `ExcoriatedEntity` → `Monster`.
- Source: [ExcoriatedEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/ExcoriatedEntity.java).
- Death pool 1: `{"rolls":{"type":"minecraft:uniform","min":1,"max":3},"entries":[{"type":"minecraft:item","name":"minecraft:bone"}]}`
- Death pool 2: `{"rolls":{"type":"minecraft:uniform","min":1,"max":2},"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation"}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 3: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:gore_wound_harpoon"}],"conditions":[{"condition":"minecraft:killed_by_player"},{"condition":"minecraft:random_chance","chance":0.25}]}`
- Death pool 4: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:calcified_blood_spine","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["flammeus"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `phlegethontic_basin` (weight 4, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `phlegethontic_bombardier`

- Class/category: `PhlegethonticBombardier` / `MONSTER`; ancestry: `PhlegethonticBombardier` → `PathfinderMob`.
- Source: [PhlegethonticBombardier](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/PhlegethonticBombardier.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:chitinous_husk","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["flammeus"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `add_phlegethontic_bombardier`: hemomancy:phlegethontic_basin (weight 8, 1–3).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `will`

- Class/category: `WillEntity` / `MONSTER`; ancestry: `WillEntity` → `Monster`.
- Source: [WillEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java).
- Death loot: **empty authored table**.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [WillEntity:613](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java:613) `spawnAtLocation(EnumBloodTendency.getRepEnzyme(loot.school()));`; [WillEntity:615](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java:615) `spawnAtLocation(ItemInit.faded_memory.get());`; [WillEntity:626](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java:626) `if (random.nextFloat() < 0.5F) spawnAtLocation(EnumBloodTendency.getRepEnzyme(getSchool()));`; [WillEntity:627](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java:627) `if (random.nextFloat() < 0.15F) spawnAtLocation(ItemInit.faded_memory.get());`; [WillEntity:631](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java:631) `public InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `blood_thrall`

- Class/category: `BloodThrallEntity` / `CREATURE`; ancestry: `BloodThrallEntity` → `PathfinderMob`.
- Source: [BloodThrallEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodThrallEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [BloodThrallEntity:285](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodThrallEntity.java:285) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [BloodThrallEntity:332](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodThrallEntity.java:332) `return super.mobInteract(player, hand);`.

### `veinwing_vulture`

- Class/category: `VeinwingVultureEntity` / `CREATURE`; ancestry: `VeinwingVultureEntity` → `Vex`.
- Source: [VeinwingVultureEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/VeinwingVultureEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `marrow_spitter`

- Class/category: `MarrowSpitterEntity` / `CREATURE`; ancestry: `MarrowSpitterEntity` → `Skeleton`.
- Source: [MarrowSpitterEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MarrowSpitterEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `gorebound_hulk`

- Class/category: `GoreboundHulkEntity` / `CREATURE`; ancestry: `GoreboundHulkEntity` → `GroundPuppetEntity` → `Zombie`.
- Source: [GoreboundHulkEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/GoreboundHulkEntity.java).
- Source: [GroundPuppetEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/GroundPuppetEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `mnemonist_puppet`

- Class/category: `MnemonistPuppetEntity` / `CREATURE`; ancestry: `MnemonistPuppetEntity` → `GroundPuppetEntity` → `Zombie`.
- Source: [MnemonistPuppetEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MnemonistPuppetEntity.java).
- Source: [GroundPuppetEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/GroundPuppetEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `scarlet_mummer`

- Class/category: `ScarletMummerEntity` / `CREATURE`; ancestry: `ScarletMummerEntity` → `GroundPuppetEntity` → `Zombie`.
- Source: [ScarletMummerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/ScarletMummerEntity.java).
- Source: [GroundPuppetEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/GroundPuppetEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `sanguine_hound`

- Class/category: `SanguineHoundEntity` / `CREATURE`; ancestry: `SanguineHoundEntity` → `Wolf`.
- Source: [SanguineHoundEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/SanguineHoundEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [SanguineHoundEntity:193](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/SanguineHoundEntity.java:193) `public InteractionResult mobInteract(Player player, InteractionHand hand) {`.

### `drudge`

- Class/category: `DrudgeEntity` / `CREATURE`; ancestry: `DrudgeEntity` → `PathfinderMob`.
- Source: [DrudgeEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/DrudgeEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [DrudgeEntity:334](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/DrudgeEntity.java:334) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [DrudgeEntity:394](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/DrudgeEntity.java:394) `return super.mobInteract(player, hand);`; [DrudgeEntity:423](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/DrudgeEntity.java:423) `spawnAtLocation(new net.minecraft.world.item.ItemStack(bmi));`.

### `enthralled_doll`

- Class/category: `EnthralledDollEntity` / `MONSTER`; ancestry: `EnthralledDollEntity` → `Monster`.
- Source: [EnthralledDollEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/EnthralledDollEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:bleeding_bulb","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: [EnthralledDollEntity:239](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/EnthralledDollEntity.java:239) `protected boolean shouldDropLoot() {`; [EnthralledDollEntity:241](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/EnthralledDollEntity.java:241) `&& super.shouldDropLoot();`.

### `chiton`

- Class/category: `ChitonEntity` / `valueOf`; ancestry: `ChitonEntity` → `PelagicAnimal` → `WaterAnimal`.
- Source: [ChitonEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/ChitonEntity.java).
- Source: [PelagicAnimal](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicAnimal.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:chitinous_husk","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["ferric"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `rockpool_shore` (weight 26, 2–4).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `pyrosome`

- Class/category: `PyrosomeEntity` / `valueOf`; ancestry: `PyrosomeEntity` → `PelagicAnimal` → `WaterAnimal`.
- Source: [PyrosomeEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PyrosomeEntity.java).
- Source: [PelagicAnimal](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicAnimal.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":["animus"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `pelagic_ocean` (weight 18, 1–2); `twilight_ocean` (weight 2, 1–1).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `pelagic_herring`

- Class/category: `PelagicHerringEntity` / `WATER_AMBIENT`; ancestry: `PelagicHerringEntity` → `AbstractSchoolingFish`.
- Source: [PelagicHerringEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicHerringEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:raw_pelagic_herring","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":1}},{"function":"minecraft:furnace_smelt","conditions":[{"condition":"minecraft:entity_properties","entity":"this","predicate":{"flags":{"is_on_fire":true}}}]},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}]}`
- Blood profile: `{"tendencies":["ductilis"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `pelagic_ocean` (weight 45, 6–10); `twilight_ocean` (weight 3, 6–10).
- Interaction/drop hooks: [PelagicHerringEntity:23](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicHerringEntity.java:23) `@Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [PelagicHerringEntity:24](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicHerringEntity.java:24) `return player.getItemInHand(hand).is(Items.WATER_BUCKET) ? InteractionResult.PASS : super.mobInteract(player, hand);`.

### `ice_fish`

- Class/category: `IceFishEntity` / `WATER_AMBIENT`; ancestry: `IceFishEntity` → `AbstractSchoolingFish`.
- Source: [IceFishEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/IceFishEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:cleansing_hemolymph","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false}]}]}`
- Blood profile: `{"tendencies":[],"properties":["hemomancy:blood_properties/aquatic"],"requires_living_syringe":false}`
- Spawn packs: `add_ice_fish`: #hemomancy:ice_fish_spawnlist (weight 12, 3–5).
- Interaction/drop hooks: [IceFishEntity:47](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/IceFishEntity.java:47) `@Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [IceFishEntity:50](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/IceFishEntity.java:50) `return super.mobInteract(player, hand);`.

### `siphonophore`

- Class/category: `SiphonophoreEntity` / `WATER_CREATURE`; ancestry: `SiphonophoreEntity` → `PelagicAnimal` → `WaterAnimal`.
- Source: [SiphonophoreEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/SiphonophoreEntity.java).
- Source: [PelagicAnimal](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicAnimal.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:ganglion_cluster","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["ductilis"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `midnight_ocean` (weight 2, 1–1); `twilight_ocean` (weight 18, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `bloody_belly_comb_jelly`

- Class/category: `BloodyBellyCombJellyEntity` / `WATER_AMBIENT`; ancestry: `BloodyBellyCombJellyEntity` → `PelagicAnimal` → `WaterAnimal`.
- Source: [BloodyBellyCombJellyEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/BloodyBellyCombJellyEntity.java).
- Source: [PelagicAnimal](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicAnimal.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:cuttlefish_chromatophores","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["tenebris"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `carrion_depths` (weight 2, 1–1); `midnight_ocean` (weight 24, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `hagfish`

- Class/category: `HagfishEntity` / `valueOf`; ancestry: `HagfishEntity` → `PelagicAnimal` → `WaterAnimal`.
- Source: [HagfishEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/HagfishEntity.java).
- Source: [PelagicAnimal](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicAnimal.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":["mortem"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `carrion_depths` (weight 30, 2–4); `hydrothermal_depths` (weight 2, 1–2).
- Interaction/drop hooks: [HagfishEntity:52](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/HagfishEntity.java:52) `@Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [HagfishEntity:54](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/HagfishEntity.java:54) `if (!stack.is(Items.ROTTEN_FLESH) || feedingTicks > 0) return super.mobInteract(player, hand);`.

### `vampire_squid`

- Class/category: `VampireSquidEntity` / `WATER_AMBIENT`; ancestry: `VampireSquidEntity` → `PelagicAnimal` → `WaterAnimal`.
- Source: [VampireSquidEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/VampireSquidEntity.java).
- Source: [PelagicAnimal](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PelagicAnimal.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:calcified_blood_spine","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["tenebris"],"properties":["hemomancy:blood_properties/aquatic"],"requires_living_syringe":false}`
- Spawn packs: `carrion_depths` (weight 24, 1–2); `hydrothermal_depths` (weight 24, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `barbed_urchin`

- Class/category: `BarbedUrchinEntity` / `WATER_AMBIENT`; ancestry: `BarbedUrchinEntity` → `AbstractFish`.
- Source: [BarbedUrchinEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/BarbedUrchinEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:calcified_blood_spine","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `drifting_mycelium` (weight 1, 1–2); `erythrocoral_reef` (weight 22, 2–4); `fungal_gardens` (weight 1, 1–2); `fungal_isles` (weight 1, 1–3); `hyphal_spires` (weight 1, 1–2); `sporecrown_thicket` (weight 1, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `chalybeate_snail`

- Class/category: `ChalybeateSnailEntity` / `valueOf`; ancestry: `ChalybeateSnailEntity` → `WaterAnimal`.
- Source: [ChalybeateSnailEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/ChalybeateSnailEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:hematic_iron_scrap","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0.0,"max":1.0},"add":false}]}],"conditions":[{"condition":"minecraft:killed_by_player"},{"condition":"minecraft:random_chance","chance":0.25}]}`
- Blood profile: `{"tendencies":["ferric"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `carrion_depths` (weight 2, 1–2); `hydrothermal_depths` (weight 32, 2–4).
- Interaction/drop hooks: [ChalybeateSnailEntity:130](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/ChalybeateSnailEntity.java:130) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [ChalybeateSnailEntity:133](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/ChalybeateSnailEntity.java:133) `return super.mobInteract(player, hand);`.

### `blood_lantern_jelly`

- Class/category: `BloodLanternJellyEntity` / `WATER_AMBIENT`; ancestry: `BloodLanternJellyEntity` → `PathfinderMob`.
- Source: [BloodLanternJellyEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/BloodLanternJellyEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":["animus"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `carrion_depths` (weight 1, 1–1); `erythrocoral_reef` (weight 3, 1–2); `midnight_ocean` (weight 28, 2–4); `twilight_ocean` (weight 3, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `mnemonic_whale`

- Class/category: `MnemonicWhaleEntity` / `WATER_CREATURE`; ancestry: `MnemonicWhaleEntity` → `WaterAnimal`.
- Source: [MnemonicWhaleEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/MnemonicWhaleEntity.java).
- Death loot: **empty authored table**.
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `erythrocoral_reef` (weight 1, 1–1); `pelagic_ocean` (weight 12, 1–1); `twilight_ocean` (weight 1, 1–1).
- Interaction/drop hooks: [MnemonicWhaleEntity:102](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/MnemonicWhaleEntity.java:102) `this.spawnAtLocation(ItemInit.mnemonic_ambergris.get());`; [MnemonicWhaleEntity:139](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/MnemonicWhaleEntity.java:139) `this.spawnAtLocation(new ItemStack(ItemInit.mnemonic_ambergris.get()), this.getBbHeight() + 0.1F);`; [MnemonicWhaleEntity:145](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/MnemonicWhaleEntity.java:145) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [MnemonicWhaleEntity:148](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/MnemonicWhaleEntity.java:148) `return super.mobInteract(player, hand);`.

### `brined_votary`

- Class/category: `BrinedVotaryEntity` / `MONSTER`; ancestry: `BrinedVotaryEntity` → `Monster`.
- Source: [BrinedVotaryEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/BrinedVotaryEntity.java).
- Death pool 1: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:iron_nugget","weight":3,"functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0,"max":2},"add":false}]},{"type":"minecraft:item","name":"hemomancy:hematic_iron_scrap","weight":1},{"type":"minecraft:empty","weight":6}]}`
- Blood profile: `{"tendencies":["mortem"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `prism_cuttle`

- Class/category: `PrismCuttleEntity` / `WATER_CREATURE`; ancestry: `PrismCuttleEntity` → `WaterAnimal`.
- Source: [PrismCuttleEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/PrismCuttleEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:cuttlefish_chromatophores","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:glow_ink_sac","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}]}`
- Blood profile: `{"tendencies":["ductilis"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_prism_cuttle`: #hemomancy:prism_cuttle_spawnlist (weight 14, 1–3); `erythrocoral_reef` (weight 2, 1–2); `midnight_ocean` (weight 1, 1–1); `pelagic_ocean` (weight 2, 1–1); `twilight_ocean` (weight 28, 1–3).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `lump_of_thought`

- Class/category: `LumpOfThoughtEntity` / `MONSTER`; ancestry: `LumpOfThoughtEntity` → `Monster`.
- Source: [LumpOfThoughtEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/LumpOfThoughtEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `fungal_isles` (weight 5, 1–2); `hyphal_spires` (weight 8, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `morphling_polyp`

- Class/category: `MorphlingPolypEntity` / `MONSTER`; ancestry: `MorphlingPolypEntity` → `Monster`.
- Source: [MorphlingPolypEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MorphlingPolypEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:morphling_polyp"}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_morphling_polyp`: #hemomancy:morphling_polyp_spawnlist (weight 3, 1–1).
- Interaction/drop hooks: [MorphlingPolypEntity:236](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MorphlingPolypEntity.java:236) `protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {`; [MorphlingPolypEntity:237](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MorphlingPolypEntity.java:237) `super.dropCustomDeathLoot(level, source, recentlyHit);`; [MorphlingPolypEntity:257](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MorphlingPolypEntity.java:257) `this.spawnAtLocation(new ItemStack(item));`.

### `fungling`

- Class/category: `FunglingEntity` / `CREATURE`; ancestry: `FunglingEntity` → `PathfinderMob`.
- Source: [FunglingEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/FunglingEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:spore_sac","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":[],"properties":["hemomancy:fungal"],"requires_living_syringe":false}`
- Spawn packs: `drifting_mycelium` (weight 8, 2–4); `fungal_isles` (weight 8, 2–4); `sporecrown_thicket` (weight 12, 2–5).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `tooth_pecks`

- Class/category: `ToothPecksEntity` / `MONSTER`; ancestry: `ToothPecksEntity` → `PathfinderMob`.
- Source: [ToothPecksEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/ToothPecksEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"minecraft:bone_meal","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":3.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:bleeding_bulb","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `hemolymphopoda`

- Class/category: `HemolymphopodaEntity` / `AMBIENT`; ancestry: `HemolymphopodaEntity` → `PathfinderMob`.
- Source: [HemolymphopodaEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/HemolymphopodaEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:cleansing_hemolymph","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false}]}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_hemolymphopoda`: #hemomancy:hemolymphopoda_spawnlist (weight 30, 2–5); `mycelial_depths` (weight 20, 3–8); `rockpool_shore` (weight 30, 2–5).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `lantern_tick`

- Class/category: `LanternTickEntity` / `MONSTER`; ancestry: `LanternTickEntity` → `PathfinderMob`.
- Source: [LanternTickEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/LanternTickEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:lantern_tick_helmet"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_lantern_tick`: #hemomancy:lantern_tick_spawnlist (weight 6, 1–1).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `myelin_borer`

- Class/category: `MyelinBorerEntity` / `MONSTER`; ancestry: `MyelinBorerEntity` → `Monster`.
- Source: [MyelinBorerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:ganglion_cluster","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false}]}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `cortical_drift` (weight 8, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `mortarbound`

- Class/category: `MortarboundEntity` / `MONSTER`; ancestry: `MortarboundEntity` → `Monster`.
- Source: [MortarboundEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/MortarboundEntity.java).
- Death pool 1: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:sculk_vein","conditions":[{"condition":"minecraft:random_chance","chance":0.25}]}]}`
- Blood profile: `{"tendencies":[],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_mortarbound`: #hemomancy:mortarbound_spawnlist (weight 2, 1–1).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `naeglerophaeon`

- Class/category: `NaeglerophaeonEntity` / `MONSTER`; ancestry: `NaeglerophaeonEntity` → `Monster`.
- Source: [NaeglerophaeonEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/NaeglerophaeonEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:naeglerophaeon_ganglion"}]}`
- Blood profile: `{"tendencies":["ductilis"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `chitinite`

- Class/category: `ChitiniteEntity` / `CREATURE`; ancestry: `ChitiniteEntity` → `PathfinderMob`.
- Source: [ChitiniteEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/ChitiniteEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:chitinous_husk","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["ferric"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `add_chitinite_cave`: #hemomancy:chitinite_cave_spawnlist (weight 10, 1–3); `sporecrown_thicket` (weight 8, 2–4).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `fervent_chitinite`

- Class/category: `FerventChitiniteEntity` / `CREATURE`; ancestry: `FerventChitiniteEntity` → `PathfinderMob`.
- Source: [FerventChitiniteEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/FerventChitiniteEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:blood_crystal"}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:fervent_husk","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":2.0,"max":4.0},"add":false}]}]}`
- Blood profile: `{"tendencies":["ferric","flammeus"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `add_fervent_chitinite_cave`: #hemomancy:fervent_chitinite_nether_spawnlist (weight 2, 1–1).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `venom_rib_centipede`

- Class/category: `VenomRibCentipedeEntity` / `MONSTER`; ancestry: `VenomRibCentipedeEntity` → `PathfinderMob`.
- Source: [VenomRibCentipedeEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/VenomRibCentipedeEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:toxicognath","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:spider_eye","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0.0,"max":2.0}}]}]}`
- Blood profile: `{"tendencies":["ferric"],"properties":[],"requires_living_syringe":true}`
- Spawn packs: `add_venom_rib_centipede`: #hemomancy:venom_rib_centipede_spawnlist (weight 4, 1–1).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `chthonian`

- Class/category: `ChthonianEntity` / `MONSTER`; ancestry: `ChthonianEntity` → `Spider`.
- Source: [ChthonianEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/ChthonianEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:chitinous_husk","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `drifting_mycelium` (weight 6, 1–2); `sporecrown_thicket` (weight 8, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `chthonian_queen`

- Class/category: `ChthonianQueenEntity` / `MONSTER`; ancestry: `ChthonianQueenEntity` → `Spider`.
- Source: [ChthonianQueenEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/ChthonianQueenEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:chitinous_husk","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":2.0,"max":4.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":2.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:queens_physogastrism","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":1.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 3: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","weight":2,"name":"hemomancy:ferric_enzyme"},{"type":"minecraft:empty","weight":3}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `crimson_doe`

- Class/category: `CrimsonDoeEntity` / `CREATURE`; ancestry: `CrimsonDoeEntity` → `PathfinderMob`.
- Source: [CrimsonDoeEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/CrimsonDoeEntity.java).
- Death pool 1: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:bleeding_bulb","functions":[{"function":"minecraft:set_count","count":{"min":1,"max":2,"type":"minecraft:uniform"}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1}}]}]}`
- Death pool 2: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"min":0,"max":1,"type":"minecraft:uniform"}}]}]}`
- Death pool 3: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:raw_venison","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":3}},{"function":"minecraft:furnace_smelt","conditions":[{"condition":"minecraft:entity_properties","entity":"this","predicate":{"flags":{"is_on_fire":true}}}]},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}]}`
- Blood profile: `{"tendencies":["animus"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_crimson_doe`: #hemomancy:crimson_doe_spawnlist (weight 12, 1–3).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `verdigris_moth`

- Class/category: `VerdigrisMothEntity` / `AMBIENT`; ancestry: `VerdigrisMothEntity` → `PathfinderMob`.
- Source: [VerdigrisMothEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VerdigrisMothEntity.java).
- Death loot: **empty authored table**.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_verdigris_moth`: #hemomancy:verdigris_moth_spawnlist (weight 9, 1–2).
- Interaction/drop hooks: [VerdigrisMothEntity:132](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VerdigrisMothEntity.java:132) `protected InteractionResult mobInteract(Player player, InteractionHand hand) {`; [VerdigrisMothEntity:135](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VerdigrisMothEntity.java:135) `return super.mobInteract(player, hand);`; [VerdigrisMothEntity:153](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VerdigrisMothEntity.java:153) `this.spawnAtLocation(new ItemStack(BlockInit.virid_salis_trail.get()));`.

### `choir_keeper`

- Class/category: `ChoirKeeperEntity` / `AMBIENT`; ancestry: `ChoirKeeperEntity` → `PathfinderMob`.
- Source: [ChoirKeeperEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/ChoirKeeperEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:venous_pinion","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: `add_choir_keeper`: #hemomancy:choir_keeper_spawnlist (weight 7, 1–1).
- Interaction/drop hooks: [ChoirKeeperEntity:259](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/ChoirKeeperEntity.java:259) `this.spawnAtLocation(Items.ENDER_PEARL);`.

### `osteophage`

- Class/category: `OsteophageEntity` / `AMBIENT`; ancestry: `OsteophageEntity` → `PathfinderMob`.
- Source: [OsteophageEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/OsteophageEntity.java).
- Death pool 1: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:venous_pinion","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["mortem"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_osteophage`: #hemomancy:osteophage_spawnlist (weight 2, 1–1).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `luminal_cicada`

- Class/category: `LuminalCicadaEntity` / `AMBIENT`; ancestry: `LuminalCicadaEntity` → `PathfinderMob`.
- Source: [LuminalCicadaEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/LuminalCicadaEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:cuttlefish_chromatophores","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["lux"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_luminal_cicada`: #hemomancy:luminal_cicada_spawnlist (weight 7, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `hematic_burrower`

- Class/category: `HematicBurrowerEntity` / `CREATURE`; ancestry: `HematicBurrowerEntity` → `PathfinderMob`.
- Source: [HematicBurrowerEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/HematicBurrowerEntity.java).
- Death pool 1: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:clay_ball","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0.0,"max":2.0}}]}]}`
- Blood profile: `{"tendencies":["ferric"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_hematic_burrower`: #hemomancy:hematic_burrower_spawnlist (weight 18, 1–2).
- Interaction/drop hooks: [HematicBurrowerEntity:115](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/HematicBurrowerEntity.java:115) `this.spawnAtLocation(new ItemStack(item));`.

### `scarlet_serpent`

- Class/category: `ScarletSerpentEntity` / `CREATURE`; ancestry: `ScarletSerpentEntity` → `PathfinderMob`.
- Source: [ScarletSerpentEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/ScarletSerpentEntity.java).
- Death pool 1: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:serpent_scale","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1.0,"max":2.0},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1.0}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["ductilis","flammeus"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_scarlet_serpent`: #hemomancy:scarlet_serpent_spawnlist (weight 8, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `hemojelly`

- Class/category: `HemojellyEntity` / `WATER_CREATURE`; ancestry: `HemojellyEntity` → `WaterAnimal`.
- Source: [HemojellyEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/aquatic/HemojellyEntity.java).
- Death pool 1: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:vivacious_enzyme","functions":[{"function":"minecraft:set_count","count":{"min":1,"max":2,"type":"minecraft:uniform"}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1}}]}]}`
- Death pool 2: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:puppeteering_thread","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["animus"],"properties":["hemomancy:blood_properties/aquatic"],"requires_living_syringe":false}`
- Spawn packs: `add_hemojelly`: #hemomancy:hemojelly_spawnlist (weight 4, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `venous_strider`

- Class/category: `VenousStriderEntity` / `CREATURE`; ancestry: `VenousStriderEntity` → `PathfinderMob`.
- Source: [VenousStriderEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VenousStriderEntity.java).
- Death pool 1: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:vivacious_spores","functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3,"type":"minecraft:uniform"}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1}}]}]}`
- Death pool 2: `{"rolls":1,"entries":[{"type":"minecraft:item","name":"hemomancy:sanguine_formation","functions":[{"function":"minecraft:set_count","count":{"min":0,"max":1,"type":"minecraft:uniform"}}]}]}`
- Death pool 3: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:venous_pinion","functions":[{"function":"minecraft:set_count","count":{"min":1,"max":2,"type":"minecraft:uniform"},"add":false},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0.0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Death pool 4: `{"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":"hemomancy:calcified_blood_spine","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":1,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}],"conditions":[{"condition":"minecraft:killed_by_player"}]}`
- Blood profile: `{"tendencies":["animus"],"properties":[],"requires_living_syringe":false}`
- Spawn packs: `add_venous_strider`: #hemomancy:venous_strider_spawnlist (weight 10, 1–3); `hemorrhagic_plateau` (weight 5, 1–2).
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `blood_cloud`

- Class/category: `CloudEntityBlood` / `MISC`; ancestry: `CloudEntityBlood` → `BloodConstructEntity` → `PathfinderMob`.
- Source: [CloudEntityBlood](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/CloudEntityBlood.java).
- Source: [BloodConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodConstructEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `iron_pillar`

- Class/category: `EntityIronPillar` / `MISC`; ancestry: `EntityIronPillar` → `FerricConstructEntity` → `BloodConstructEntity` → `PathfinderMob`.
- Source: [EntityIronPillar](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/EntityIronPillar.java).
- Source: [FerricConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/FerricConstructEntity.java).
- Source: [BloodConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodConstructEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `iron_wall`

- Class/category: `EntityIronWall` / `MISC`; ancestry: `EntityIronWall` → `FerricConstructEntity` → `BloodConstructEntity` → `PathfinderMob`.
- Source: [EntityIronWall](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/EntityIronWall.java).
- Source: [FerricConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/FerricConstructEntity.java).
- Source: [BloodConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodConstructEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `iron_spike`

- Class/category: `EntityIronSpike` / `MISC`; ancestry: `EntityIronSpike` → `FerricConstructEntity` → `BloodConstructEntity` → `PathfinderMob`.
- Source: [EntityIronSpike](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/EntityIronSpike.java).
- Source: [FerricConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/FerricConstructEntity.java).
- Source: [BloodConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodConstructEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

### `wretched_will`

- Class/category: `EntityWretchedWill` / `MISC`; ancestry: `EntityWretchedWill` → `BloodConstructEntity` → `PathfinderMob`.
- Source: [EntityWretchedWill](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/EntityWretchedWill.java).
- Source: [BloodConstructEntity](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodConstructEntity.java).
- Death loot: **no authored table at the default entity path**; custom/inherited hooks below may own rewards. No table is invented in this pass.
- Blood profile: **no explicit definition**; `BloodProfileData` returns the empty profile (no tendencies/properties, no living-syringe requirement).
- Spawn packs: No biome pack found; consult entity/controller source for ritual, structure, event or summon creation..
- Interaction/drop hooks: No authored interaction/drop hook found in the local ancestry. Shared sampling/capture still applies where eligible..

## Empty and missing death tables

**Empty authored tables:** `unstained_zealot`, `unstained_guardian`, `unstained_acolyte`, `unstained_scout`, `circus_fire_eater`, `circus_stilt_walker`, `circus_acrobat`, `circus_knife_thrower`, `will`, `pyrosome`, `hagfish`, `blood_lantern_jelly`, `mnemonic_whale`, `verdigris_moth`.

**Missing default tables:** `spectral_companion`, `phantasmal_echo`, `harbinger_hermit`, `harbinger_artificer`, `harbinger_cicatrix_anchorite`, `harbinger_mnemonist`, `harbinger_voyager`, `harbinger_votary_wayfarer`, `circus_ringmaster`, `annetta_knowles`, `stained_priestess`, `latent_annetta_infection`, `vesper_crowned_refusal`, `bloodlicker`, `veinwing_vulture`, `marrow_spitter`, `gorebound_hulk`, `mnemonist_puppet`, `scarlet_mummer`, `sanguine_hound`, `drudge`, `blood_cloud`, `iron_pillar`, `iron_wall`, `iron_spike`, `wretched_will`.

Controlled Peacock Spiders carrying `CircusTrialOwner` and Vampire Bats with `ManifestedAggregate`/`CircusSwarmOwner` suppress death loot, so the new wild tables do not reward circus actors. Their wild registry IDs are shared with the controlled forms.

Missing tables for summons, projections, trial actors and utility entities are not automatically defects or permission to add farmable rewards. NPC/saint/quest actors require checking their source-owned delivery rules. No new drops were assigned to those classes.

## Spawn-tag expansion

- `abyssal_siphon_spawnlist`: `{"replace":false,"values":["minecraft:deep_dark"]}`
- `bleeding_heart_spawnlist`: `{"replace":false,"values":["minecraft:cherry_grove"]}`
- `blood_drunk_puppeteer_spawnlist`: `{"replace":false,"values":["minecraft:dark_forest","minecraft:swamp","minecraft:mangrove_swamp","minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga","hemomancy:sporecrown_thicket","hemomancy:hemorrhagic_plateau"]}`
- `bog_body_spawnlist`: `{"replace":false,"values":["minecraft:swamp","minecraft:mangrove_swamp"]}`
- `bog_iron_clump_spawnlist`: `{"replace":false,"values":["minecraft:swamp","minecraft:mangrove_swamp"]}`
- `chitinite_cave_spawnlist`: `{"replace":false,"values":["minecraft:lush_caves","minecraft:dripstone_caves","minecraft:deep_dark"]}`
- `choir_keeper_spawnlist`: `{"replace":false,"values":["minecraft:end_highlands","minecraft:end_midlands","minecraft:end_barrens","minecraft:small_end_islands"]}`
- `crimson_doe_spawnlist`: `{"replace":false,"values":["minecraft:plains","minecraft:sunflower_plains","minecraft:forest","minecraft:birch_forest","minecraft:old_growth_birch_forest","minecraft:flower_forest","minecraft:cherry_grove","minecraft:meadow","hemomancy:fungal_gardens"]}`
- `cruor_fiend_spawnlist`: `{"replace":false,"values":["minecraft:nether_wastes","minecraft:crimson_forest","minecraft:soul_sand_valley","minecraft:warped_forest","minecraft:basalt_deltas"]}`
- `deep_ocean_vent_spawnlist`: `{"replace":false,"values":["hemomancy:hydrothermal_depths"]}`
- `desiccant_spawnlist`: `{"replace":false,"values":["minecraft:desert","minecraft:badlands","minecraft:eroded_badlands","minecraft:wooded_badlands","minecraft:savanna","minecraft:savanna_plateau","minecraft:windswept_savanna"]}`
- `dessicant_spawnlist`: `{"replace":false,"values":["minecraft:desert","minecraft:badlands","minecraft:eroded_badlands","minecraft:wooded_badlands"]}`
- `devils_tooth_spawnlist`: `{"replace":false,"values":["minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga"]}`
- `earthen_vein_spawnlist`: `{"replace":false,"values":["hemomancy:fungal_gardens"]}`
- `fervent_chitinite_nether_spawnlist`: `{"replace":false,"values":["minecraft:basalt_deltas"]}`
- `frozen_clot_spawnlist`: `{"replace":false,"values":["minecraft:snowy_plains","minecraft:ice_spikes","minecraft:frozen_peaks","minecraft:jagged_peaks","minecraft:snowy_slopes","minecraft:grove","minecraft:snowy_taiga"]}`
- `ghost_pipe_spawnlist`: `{"replace":false,"values":["#minecraft:is_forest","#minecraft:is_taiga","minecraft:dark_forest","minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga","minecraft:old_growth_birch_forest"]}`
- `hematic_burrower_spawnlist`: `{"replace":false,"values":["#minecraft:is_forest","minecraft:dark_forest","minecraft:old_growth_birch_forest","minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga"]}`
- `hemojelly_spawnlist`: `{"replace":false,"values":["minecraft:warm_ocean","minecraft:lukewarm_ocean","minecraft:ocean","minecraft:deep_lukewarm_ocean","minecraft:deep_ocean","hemomancy:rockpool_shore","hemomancy:pelagic_ocean","hemomancy:erythrocoral_reef"]}`
- `hemolymphopoda_spawnlist`: `{"replace":false,"values":["minecraft:beach","minecraft:snowy_beach","minecraft:stony_shore","minecraft:river","minecraft:frozen_river","minecraft:swamp","minecraft:mangrove_swamp","minecraft:mushroom_fields","minecraft:lush_caves","minecraft:dripstone_caves","#c:is_swamp"]}`
- `ice_fish_spawnlist`: `{"replace":false,"values":["minecraft:cold_ocean","minecraft:deep_cold_ocean","minecraft:frozen_ocean","minecraft:deep_frozen_ocean","minecraft:frozen_river","minecraft:ice_spikes"]}`
- `lantern_tick_spawnlist`: `{"replace":false,"values":["minecraft:dripstone_caves","minecraft:lush_caves","minecraft:deep_dark"]}`
- `lethean_poppy_spawnlist`: `{"replace":false,"values":["#minecraft:is_taiga","minecraft:snowy_plains","minecraft:ice_spikes","minecraft:snowy_taiga","minecraft:snowy_beach","minecraft:frozen_river","minecraft:snowy_slopes","minecraft:frozen_peaks","minecraft:jagged_peaks","minecraft:grove"]}`
- `luminal_cicada_spawnlist`: `{"replace":false,"values":["minecraft:dark_forest","minecraft:lush_caves"]}`
- `morphling_polyp_spawnlist`: `{"replace":false,"values":["#minecraft:is_overworld","minecraft:plains","minecraft:desert","minecraft:ocean","minecraft:lush_caves"]}`
- `mortarbound_spawnlist`: `{"replace":false,"values":["minecraft:deep_dark"]}`
- `myelin_borer_spawnlist`: `{"replace":false,"values":["minecraft:dripstone_caves","minecraft:lush_caves","minecraft:deep_dark"]}`
- `osteophage_spawnlist`: `{"replace":false,"values":["minecraft:soul_sand_valley"]}`
- `peacock_spider_spawnlist`: `{"replace":false,"values":["minecraft:forest","minecraft:birch_forest","minecraft:flower_forest","minecraft:meadow"]}`
- `prism_cuttle_spawnlist`: `{"replace":false,"values":["minecraft:warm_ocean","minecraft:lukewarm_ocean","minecraft:deep_lukewarm_ocean"]}`
- `rafflesia_spawnlist`: `{"replace":false,"values":["minecraft:jungle","minecraft:sparse_jungle","minecraft:bamboo_jungle","minecraft:dark_forest"]}`
- `sarcodes_spawnlist`: `{"replace":false,"values":["#minecraft:is_forest","minecraft:dark_forest","minecraft:taiga","minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga"]}`
- `scarlet_serpent_spawnlist`: `{"replace":false,"values":["minecraft:desert","minecraft:badlands","minecraft:eroded_badlands","minecraft:wooded_badlands","minecraft:swamp","minecraft:mangrove_swamp","minecraft:jungle","minecraft:sparse_jungle","minecraft:bamboo_jungle"]}`
- `silver_bells_tower_spawnlist`: `{"replace":false,"values":[]}`
- `stink_horn_spawnlist`: `{"replace":false,"values":["#c:is_swamp","#c:is_spooky"]}`
- `synapse_hound_spawnlist`: `{"replace":false,"values":["minecraft:savanna","minecraft:savanna_plateau","minecraft:windswept_savanna","minecraft:plains","minecraft:sunflower_plains","minecraft:windswept_hills","minecraft:windswept_gravelly_hills"]}`
- `termite_mound_spawnlist`: `{"replace":false,"values":["minecraft:savanna","minecraft:savanna_plateau","minecraft:windswept_savanna"]}`
- `tooth_geode_spawnlist`: `{"replace":false,"values":["minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga","minecraft:taiga","minecraft:snowy_taiga","minecraft:dark_forest","minecraft:forest","minecraft:birch_forest","minecraft:old_growth_birch_forest"]}`
- `vampire_bat_spawnlist`: `{"replace":false,"values":["minecraft:lush_caves","minecraft:dripstone_caves"]}`
- `venom_rib_centipede_spawnlist`: `{"replace":false,"values":["#minecraft:is_forest","#minecraft:is_jungle","minecraft:swamp","minecraft:mangrove_swamp","minecraft:dark_forest","minecraft:lush_caves"]}`
- `venous_strider_spawnlist`: `{"replace":false,"values":["minecraft:mushroom_fields","minecraft:swamp","minecraft:mangrove_swamp","hemomancy:fungal_gardens","hemomancy:fungal_isles"]}`
- `verdigris_moth_spawnlist`: `{"replace":false,"values":["minecraft:forest","minecraft:birch_forest","minecraft:old_growth_birch_forest","minecraft:dark_forest","minecraft:flower_forest","minecraft:taiga","minecraft:old_growth_pine_taiga","minecraft:old_growth_spruce_taiga","minecraft:snowy_taiga","minecraft:grove","minecraft:snowy_plains","minecraft:ice_spikes"]}`
- `void_drinker_spawnlist`: `{"replace":false,"values":["minecraft:the_end","minecraft:end_highlands","minecraft:end_midlands","minecraft:end_barrens","minecraft:small_end_islands"]}`

## Global loot modifiers

`bloody_jug_drop_candidates` expands to Blood Drunk Puppeteer, Crimson Doe, Hemojelly, Hemolymphopoda, Thirster and Venous Strider; these have a separate 2% player-kill Bloody Jug chance.

- `ancient_recordings`: `{"type":"hemomancy:ancient_recording","conditions":[{"condition":"neoforge:loot_table_id","loot_table_id":"minecraft:chests/ancient_city"},{"condition":"minecraft:random_chance","chance":0.12}]}`
- `bloody_jugs_from_blood_mobs`: `{"type":"hemomancy:add_item","conditions":[{"condition":"minecraft:killed_by_player"},{"condition":"minecraft:random_chance","chance":0.02},{"condition":"minecraft:entity_properties","entity":"this","predicate":{"type":"#hemomancy:bloody_jug_drop_candidates"}}],"item":"hemomancy:bloody_jug"}`
- `blood_rocks_from_venous_stone`: `{"type":"hemomancy:add_item","conditions":[{"condition":"minecraft:random_chance","chance":0.025},{"condition":"minecraft:block_state_property","block":"hemomancy:venous_stone"},{"condition":"minecraft:survives_explosion"}],"item":"hemomancy:blood_rock"}`
- `gourd_seeds_from_grass`: `{"type":"hemomancy:add_item","conditions":[{"condition":"minecraft:random_chance","chance":0.05},{"condition":"block_state_property","block":"minecraft:short_grass"}],"item":"hemomancy:gourd_seeds"}`
- `gourd_seeds_from_tall_grass`: `{"type":"hemomancy:add_item","conditions":[{"condition":"minecraft:random_chance","chance":0.05},{"condition":"block_state_property","block":"minecraft:tall_grass"}],"item":"hemomancy:gourd_seeds"}`

## Non-mob registrations

| Registry ID | Class | Category |
| --- | --- | --- |
| `covenant_throne_seat` | `CovenantThroneSeatEntity` | `MISC` |
| `hematic_armature_restraint` | `ArmatureRestraintEntity` | `MISC` |
| `unsettled_ichor` | `UnsettledIchorEntity` | `MISC` |
| `awakened_ichorian_sigil` | `AwakenedIchorianSigilEntity` | `MISC` |
| `humanity_sprite` | `HumanitySpriteEntity` | `MISC` |
| `circus_carousel` | `CircusCarouselEntity` | `MISC` |
| `containment_anchor` | `ContainmentAnchorEntity` | `MISC` |
| `recall_barb` | `RecallBarbEntity` | `MISC` |
| `gore_wound_harpoon` | `GoreWoundHarpoonEntity` | `MISC` |
| `will_anchor` | `WillAnchorEntity` | `MISC` |
| `arbor_of_will` | `ArborOfWillEntity` | `MISC` |
| `flying_charm` | `EntityFlyingCharm` | `MISC` |
| `directed_blood_orb` | `DirectedBloodOrbEntity` | `MISC` |
| `tracking_blood_orb` | `TrackingBloodOrbEntity` | `MISC` |
| `blood_cloud_carrier` | `BloodCloudCarrierEntity` | `MISC` |
| `tracking_snake` | `TrackingSerpentEntity` | `MISC` |
| `constrictor_cord_projectile` | `ConstrictorCordEntity` | `MISC` |
| `blood_chum_projectile` | `BloodChumEntity` | `MISC` |
| `tracking_pests` | `TrackingPestsEntity` | `MISC` |
| `morphling_polyp_item` | `EntityMorphlingPolypItem` | `MISC` |
| `qliphoth_seed_item` | `EntityQliphothSeedItem` | `MISC` |
| `blood_bolt` | `BloodBoltEntity` | `MISC` |
| `living_sickle_hook` | `LivingSickleHookEntity` | `MISC` |
| `living_flail_head` | `LivingFlailHeadProjectileEntity` | `MISC` |
| `vesper_scute_projectile` | `VesperScuteProjectileEntity` | `MISC` |
| `veinwing_feather` | `VeinwingFeatherEntity` | `MISC` |
| `circus_knife` | `CircusKnifeProjectileEntity` | `MISC` |
| `blood_needle` | `BloodNeedleEntity` | `MISC` |
| `blood_bullet` | `BloodBulletEntity` | `MISC` |
| `blood_shot` | `BloodShotEntity` | `MISC` |
| `dark_arrow` | `DarkArrowEntity` | `MISC` |
| `sanguis_lancea` | `SanguisLanceaEntity` | `MISC` |
| `hemolytic_vial_projectile` | `HemolyticVialEntity` | `MISC` |

## Compatibility, consumers and deferred work

Display-only renames preserve saved stacks, recipes, tags and integration IDs. For the complete direct recipe consumer list:

- `chitinous_husk`: `barbed_blade.json`, `barbed_shield.json`, `chitinite_arm_banner.json`, `chitinite_block.json`, `chitinite_mace.json`, `chitinite_shield.json`, `hemolymphopoda_headpiece.json`, `sclerotic_oleum.json`, `spider_husk_effigy.json`, `tendon_line.json`, `vein_spider.json`, `distillation/chitinous_husk.json`, `living_weapon_graft/claws.json`, `memory_weaving/memory_sanguine_ward.json`.
- `cuttlefish_chromatophores`: `chromatic_sublimate.json`, `rubricators_quill.json`, `cardinal_rite/eightfold_script.json`.
- `calcified_blood_spine`: `aculeate_vitriol.json`, `marrow_spitter_carriage.json`, `cardinal_rite/monolithic_armature.json`, `living_weapon_graft/spear.json`.
- `puppeteering_thread`: `chromatic_sublimate.json`, `constrictor_cord.json`, `covenant_mantle.json`, `hematic_suture_needle.json`, `hematic_suture_node.json`, `marionette_crossbar.json`, `marrow_spitter_carriage.json`, `memory_thread.json`, `mnemonic_candle.json`, `mnemonist_cradle.json`, `monolith_imbued_cloth.json`, `puppeteers_spindle.json`, `puppeteers_wool.json`, `sanguine_hound_bridle.json`, `scarlet_gorget.json`, `tendon_line.json`, `veinwing_harness.json`, `vein_spider.json`, `cardinal_rite/covenant_vigil.json`, `cardinal_rite/sanctified_rite.json`, `distillation/puppeteering_thread.json`, `living_weapon_graft/crossbow.json`, `memory_weaving/blood_thrall_effigy.json`.
- `ganglion_cluster`: `ganglion_cluster_to_synaptic_node.json`, `synaptic_step.json`.
- `venous_pinion`: `veinwing_harness.json`, `venous_strider_sabatons.json`.
- `queens_physogastrism`: `sclerotic_oleum.json`.
- `telson`: `aculeate_vitriol.json`.
- `toxicognath`: `aculeate_vitriol.json`.
- `fargone_proboscis`: `armature_upgrade/blood_lust_to_edacious_blood_lust_boots.json`, `armature_upgrade/blood_lust_to_edacious_blood_lust_chestplate.json`, `armature_upgrade/blood_lust_to_edacious_blood_lust_helm.json`, `armature_upgrade/blood_lust_to_edacious_blood_lust_leggings.json`.
- `naeglerophaeon_ganglion`: `synaptic_step.json`.
- `chalybeate_sclerite`: `ferric_spores_from_chalybeate_sclerite.json`, `master_cam_kit.json`, `sclerite_block.json`, `sclerotic_oleum.json`, `cardinal_rite/enduring_pattern.json`, `distillation/chalybeate_sclerite.json`, `living_weapon_graft/axe.json`.

Deferred: shared venom/Bloodworms, furnace ash catalogue/conversions, cold blood-source expansion, Sclerite costs and Choir Keeper visual redesign. This audit deliberately records missing/empty tables without assigning unapproved material families. Habitat packs describe configured spawning, not measured population. Inherited vanilla loot is not assumed for mod registry IDs. Sampling definitions and hooks are source evidence; actual availability still depends on gameplay eligibility.

## Validation and remaining acceptance

The first continuation passed 14 focused JVM cases, nine dedicated `mob_drop_validation` GameTests, compilation, resource processing and assembly. Loaded cases exercise the 11 ordinary sources with player/non-player loot contexts, burning food conversion, cooking/nutrition, controlled circus deaths, marine spawn packs and removal of the old Depths spawn, naturally ticking feeding/release/cooldown, and interrupted capture save/load. Red runs proved the missing data, land movement, and controlled-actor farming before their fixes. Actor tick/loading guards prevent stationary fixtures from passing without running behavior.

Native screenshots in the copied disposable `Mob Drop Review 20261006` world show surface bell placement, submerged articulated tendrils and prey capture, plus the yellow/white Siphonophore feeding bolt. The initial bolt was too brief for reliable presentation; its visual lifetime is now 12 ticks with a small readable width, without changing damage or cooldown. Food sprites were drawn as original transparent 16×16 clusters and inspected enlarged with nearest-neighbor sampling. Client review fixtures are authored acceptance scenes, not proof of naturally observed population, multiplayer or long-term ecology.

Reproduction: `./gradlew.bat test --tests '*MobDropConsolidationTest' --tests '*PelagicResourcesTest' --tests '*IceFishResourcesTest' --tests '*OsteophageResourcesTest' --tests '*FerventChitiniteLootResourceTest' runMobDropGameTestServer assemble --console=plain`. Evidence: `build/mob-drop-final-validation.log`, `build/mob-drop-zap-final-review.log`, `build/pelagic-client/screenshots/ecology-42-hemojelly_surface-lit.png`, `ecology-43-hemojelly_capture-lit.png`, and `ecology-44-siphonophore_zap-active.png` in the same screenshot directory.

A separate broader ecological run failed its existing Pyrosome-cap control; that suite is not reported green. The consolidation suite is isolated from it. Full alpha, multiplayer, natural abundance and ordinary Survival acceptance were not run.
