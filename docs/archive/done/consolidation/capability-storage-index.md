# Capability and attachment storage index

The current [attachment registry](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java) has 34 entries: 27 player records marked `copyOnDeath`, one living-entity school-combat record, two worldgen records, three block-entity records, and one item-stack record. [HemoCapabilityRegistrar](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityRegistrar.java) exposes 18 of those player records through NeoForge `EntityCapability` keys. This index distinguishes stored data from the capability interface that reads it; the latter is not another persistence store.

## Player attachments

All entries in this table are serialized and marked `copyOnDeath` in `HemoAttachmentTypes`. “Capability interface” means the registrar maps a player `EntityCapability` key to that same attachment. “Direct” means callers use `getData` or a helper rather than a registered player capability key.

| Access form | Attachment IDs |
| --- | --- |
| Capability interface, blood/progression | `blood_volume`, `blood_tendency`, `vascular_system`, `initiatory_degree`, `unstained_progress`, `white_humor_volume`, `skill_progress` |
| Capability interface, loadout/body | `equipped_morphling`, `known_manipulations`, `living_staff_progress`, `specimen_bestiary`, `known_still_arts`, `known_summons`, `visceral_organs`, `scars`, `harbinger_equipment` |
| Capability interface, knowledge | `liber_knowledge`, `ichorian_knowledge` |
| Direct attachment, progression/knowledge | `antecedent`, `clinical_blood`, `advanced_brewing`, `resonant_forge`, `dialogue_knowledge` |
| Direct attachment, runtime guardrails/memory | `power_guardrails`, `will_ambush_state`, `last_death_memory`, `muscle_memory` |

The direct records still persist and copy on death; lack of a capability key is not missing storage. [HemoCapabilityAccess](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java) already offers several concrete direct-attachment accessors alongside interface-based `getCapability` accessors. A consolidation should make that distinction visible in naming/documentation rather than wrap each record in a second container.

| Direct attachment | Confirmed mutation or lifecycle owner |
| --- | --- |
| `antecedent` | [AntecedentKnowledge](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/AntecedentKnowledge.java) records evidence, captures a memo, and sends login/dimension/respawn snapshots. |
| `clinical_blood` | [ClinicalBloodKnowledge](../../src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/ClinicalBloodKnowledge.java) updates lessons and reconciles recipes at login before syncing. |
| `power_guardrails` | [BodyIdiomEvents](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/BodyIdiomEvents.java) clears transient values at respawn and sends state plus visuals. |
| `last_death_memory` | [HematicMemoryToolEvents](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memory/HematicMemoryToolEvents.java) records death context and sends lifecycle snapshots. |
| `muscle_memory` | [MuscleMemoryEvents](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/musclememory/MuscleMemoryEvents.java) owns priming, combat triggers, lifecycle migration, and observer sync. |

The other direct records remain in the inventory above; this table names the owners whose event hooks were verified in the Stage 1 lifecycle pass. A direct `getData` call in current gameplay source is part of the current contract even where repository guidance recommends routing reads and writes through `HemoCapabilityAccess`.

Some `HemoCapabilityAccess` methods accept a broad `Entity` argument even though the matching entity capability is registered for `EntityType.PLAYER` only. The `SCHOOL_COMBAT` attachment is the genuine cross-entity state. A caller that passes a mob to a player capability accessor gets an empty capability; do not interpret the overload as proof that all living entities own that state.

[WhiteHumorVolumeEvents](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/unstained/white_humor/WhiteHumorVolumeEvents.java) is currently empty. The player attachment and capability key exist and [PaleHumorFlaskItem](../../src/main/java/com/vincenthuto/hemomancy/common/item/unstained/PaleHumorFlaskItem.java) reads them, while the Retort uses the block-entity attachment. No player white-humor lifecycle sync is present in that empty event class. Check whether any client UI actually needs a player snapshot before creating one.

## Other attachment hosts and registered capabilities

| Host | Attachment IDs and policy | Separate capability surface |
| --- | --- | --- |
| Living entity | `school_combat`, serialized, no death copy | Read directly through `HemoCapabilityAccess.getSchoolCombat`; [SchoolStates](../../src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolStates.java) is the domain API. |
| Worldgen attachment hosts | `phlegethontic_pending_vein`, `phlegethontic_ore_reservations`, serialized | Internal worldgen state; outside player lifecycle dispatch. |
| Block entity | `block_blood_volume`, `block_blood_tendency`, `block_white_humor_volume`, serialized | [Registrar](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityRegistrar.java) separately exposes item handlers for the cabinet, Alembic, Retort, and Mycelial Lantern. |
| Item stack | `item_blood_volume`, serialized | The registrar also exposes `ITEM_BLOOD_VOLUME` for gourd/horn/rib items and `Capabilities.ItemHandler.ITEM` for jar/staff/folio/syringe handlers. These item capabilities are not player attachment sync. |

## Cleanup implication

The shared seam is **host and lifecycle classification**, not a generic capability serializer. Put each gameplay state on a ledger with host, storage ID, capability key if any, mutation owner, saved format, clone/death rule, and recipient of each sync. Then use [capability lifecycle and observer sync](capability-lifecycle-and-observer-sync.md) to consolidate repeated event dispatch. Preserve all IDs and the distinction between player, other living entity, block entity, and item stack.
