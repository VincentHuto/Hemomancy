# Player state, persistence, and client sync

## Current paths

The complete storage/access split is indexed in [Capability and attachment storage](capability-storage-index.md). The explicit login/dimension/respawn/tracking subscriptions are compared in [Capability lifecycle and observer sync](capability-lifecycle-and-observer-sync.md); the first dispatch pair and packet contract are in the [Stage 2 snapshot audit](stage-2-player-snapshots/README.md). Damage and tick subscribers are compared in [Entity and player event phases](entity-and-player-event-phases.md).

- [HemoAttachmentTypes](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java) declares serializable player attachments with `copyOnDeath`; [HemoCapabilityKeys](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityKeys.java) and [HemoCapabilityRegistrar](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityRegistrar.java) expose capability interfaces; [HemoCapabilityAccess](../../src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java) is the read path.
- Sync ownership is distributed: [UnstainedProgressEvents](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/unstained/UnstainedProgressEvents.java), [KnownStillArtEvents](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/unstained/stillart/KnownStillArtEvents.java), and [LiberKnowledgeEvents](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/LiberKnowledgeEvents.java) each handle lifecycle sync. [ChamberVisitEvents](../../src/main/java/com/vincenthuto/hemomancy/common/event/ChamberVisitEvents.java) includes login, clone, and respawn hooks.
- [PacketHandler](../../src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java) registers payloads; many client projections live under `client/data/`.

## Overlap and target contract

For each state, document six facts together: **owner**, **attachment ID**, **serialization**, **death behavior**, **server mutation point**, and **sync recipients/triggers**. `copyOnDeath` covers attachment copying; it does not replace a client snapshot after login, respawn, dimension change, or tracking. Conversely, a packet is not the persistence owner.

Retain `HemoCapabilityAccess` as the public read boundary. The blood-volume attachment is present even before blood magic is active; `isActive()` is the opt-in check. Harbinger and Unstained reset/exclusion rules must remain explicit.

## First migration slice

1. Use the Stage 1 storage and lifecycle ledger, then the Stage 2 tendency/vascular packet map, to identify matching owner-only sends.
2. Centralize lifecycle **dispatch** first for tendency and vascular login/dimension/respawn snapshots, while state-specific classes still build their own payloads. Do not change saved NBT shape in this slice.
3. Make mutation methods responsible for one well-defined sync request; remove ad hoc duplicate sends only after finding all callers.
4. Check save/reload, death/respawn, dimension change, and a second client observing relevant state. Client fields should be rebuilt from an authoritative snapshot.

**Avoid:** a second generic player data container beside the attachments, or silently changing attachment registry names.
