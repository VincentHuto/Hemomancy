# Networking and client projections

[Stage 7](stage-7-networking/README.md) traces representative packet authority, menu validation, recipient scope, and client cleanup from the current gameplay source. Use it with the registration index before changing directions or moving handlers.

## Current paths

- [PacketHandler](../../src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java) is the NeoForge payload registration hub. It registers serverbound actions and clientbound snapshots with `TYPE`, `STREAM_CODEC`, and handlers.
- The inspected registration method has 42 `playToServer`, 98 `playToClient`, and 34 `playBidirectional` calls. These are registration counts, not a claim that every registered class needs consolidation.
- [Packet registration index](packet-registration-index.md) records all 174 entries with their registration line, so a feature audit can start from every payload in the hub.
- Client projections are split among classes such as [ActiveRiteClientData](../../src/main/java/com/vincenthuto/hemomancy/client/data/ActiveRiteClientData.java), [ChamberOfWillClientData](../../src/main/java/com/vincenthuto/hemomancy/client/data/ChamberOfWillClientData.java), and [BloodBallClientData](../../src/main/java/com/vincenthuto/hemomancy/client/data/BloodBallClientData.java). Feature renderers consume these projections.
- Player attachment sync is covered separately in [Player state and sync](player-state-and-sync.md). Block-entity update packets are covered in [Block entities and menus](block-entities-and-menus.md).

## Overlap and target contract

Classify each payload as **action**, **snapshot**, **delta**, or **visual cue**. For each, record sender, receiver, authority, validation, scope (player/tracking/dimension/nearby), and the client state it replaces or mutates. The server validates actions; a client projection is disposable and must clear on world exit and rebuild from a snapshot. A visual cue needs an expiry or bounded lifetime.

The packet shapes already vary. [KnownManipulationClientPacket](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/KnownManipulationClientPacket.java) is an empty request that responds with skills and known manipulations. [StartCentrifugeButtonPacket](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/StartCentrifugeButtonPacket.java) is an empty server action tied to the current menu. [ChangeSelectedManipPacket](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/ChangeSelectedManipPacket.java) encodes a byte and float but handles a server-side selection. [PacketSyncUnstainedProgress](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/unstained/PacketSyncUnstainedProgress.java) is a larger snapshot; [PacketSyncFungalProjection](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncFungalProjection.java) is a small visual-state snapshot. Several older manipulation/equipment packets are registered with `playBidirectional` in `PacketHandler`; inspect actual callers and handlers before narrowing their registration direction.

The generic `PacketHandler.sendToPlayer` method has dialogue-specific behavior: it queues whispers and decorates/clears dialogue context before sending [OpenDialoguePacket](../../src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java). Do not treat this helper as a transparent transport operation when reorganizing packet dispatch.

| Representative payload | Current registration and shape | Validation / cleanup question |
| --- | --- | --- |
| [StartCentrifugeButtonPacket](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/StartCentrifugeButtonPacket.java) | `playBidirectional`; empty payload; gameplay handler checks current menu type then calls station startup on the server. | The packet handler does not call menu `stillValid`, and station startup does not check menu distance. See the [Stage 7 comparison](stage-7-networking/README.md) before sharing a menu-action sequence. |
| [ChangeSelectedManipPacket](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/ChangeSelectedManipPacket.java) | Server action; codec reads/writes a leading byte and a float, while selection logic uses server equipped-manipulation state. | Confirm senders and protocol compatibility before removing fields; keep selected-manip sync and weapon-form application together. |
| [PacketSyncUnstainedProgress](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/unstained/PacketSyncUnstainedProgress.java) | Clientbound progress snapshot. | Identify all login/respawn/update senders and whether the snapshot replaces or merges client state. |
| [PacketSyncFungalProjection](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncFungalProjection.java) | Clientbound visual projection. | Bound its lifetime and clear it on world change or feature end. |
| [OpenDialoguePacket](../../src/main/java/com/vincenthuto/hemomancy/common/network/dialogue/OpenDialoguePacket.java) | Clientbound dialogue open; `sendToPlayer` may transform it. | Preserve pending-whisper order, NPC context, and displayed tree. |

Central registration is useful, but do not create a second packet registry. A thin grouped registration method per feature family could make `PacketHandler` easier to navigate if it retains one obvious entry point and NeoForge 1.21 payload semantics.

## First migration slice

1. Build a packet table directly from `PacketHandler`: ID, registration direction, class, payload kind, handler side, sender validation, recipient scope, and client holder. Start with the manipulation and machine-action groups.
2. For menu actions, use one server-side validation sequence: current menu type, `stillValid`, permitted action, then mutation. Check the Centrifuge and Scar Station actions before sharing a helper.
3. Standardize clear/rebuild behavior for two client holders with the same lifecycle. Keep their payload codecs and domain logic local.
4. Group packet registrations by feature family without changing IDs or directions. Remove encoded fields only after confirming they carry no behavior and preserving protocol compatibility as required.
5. Check the feature with two players and a late tracker. A single-player client can hide stale or mis-scoped projections.

**Avoid:** mirroring all server state to all players, treating a visual cue as durable state, or using the old `SimpleChannel` API.
