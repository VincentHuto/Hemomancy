# Runtime stability and controls

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Restore dedicated-server safety, awakened weapon rendering and trustworthy controls. Preserve authored particle effects and weapon transitions; removing effects or glint is not the intended fix.

## Tasks

<a id="run-01"></a>

### RUN-01: Remove server-side client particle class loading

**Priority:** Critical. **Evidence:** Confirmed defect.

**Observed basis:** Three live failures: persistent learned-memory brazier aura crashed the dedicated server; Projection ignition disconnected its actor; Cardinal helix startup crashed both clients.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [RIT-02](04-crafting-rites-and-armor.md#rit-02) (acceptance), [MEM-01](05-weaving-scars-and-teachings.md#mem-01) (acceptance), [MEM-03](05-weaving-scars-and-teachings.md#mem-03) (acceptance), [COM-02](06-morphlings-and-puppets.md#com-02) (acceptance), [COM-03](06-morphlings-and-puppets.md#com-03) (acceptance), [FAN-01](07-fanes-recruitment-and-persistence.md#fan-01) (acceptance), [FAN-03](07-fanes-recruitment-and-persistence.md#fan-03) (acceptance), [VAL-03](09-validation-and-execution.md#val-03) (implementation), [VAL-04](09-validation-and-execution.md#val-04) (acceptance)

**Recommended code work:** Trace IronBrazierBlockEntity, BloodProjectionItem and HarbingerCardinalRiteEvents, including referenced helpers and static initialization. Keep authoritative effects on the server; send registered particle data or existing effect payloads to tracking clients. Construct ParticleProvider/render objects only on the client. Check HutosLib effect helpers at their call sites rather than substituting a catch-and-ignore wrapper. Retain the authored look and intended audience/range.

**Recommended gameplay behavior:** Players should be able to leave an offering burning, ignite it, and begin a rite with a nearby observer without a disconnect. Preserve equivalent caster and observer feedback.

**Acceptance:** Dedicated server plus two clients: run ignition, idle persistent aura, chunk unload/reload, reconnect, and Cardinal startup separately. Check both views and server survival. Reopen each preserved crash save on the fixed build. Add focused dedicated regression coverage; integrated-server success alone cannot close this task.

**Current code/resource entry points:** [IronBrazierBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/rite/IronBrazierBlockEntity.java), [BloodProjectionItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java), [HarbingerCardinalRiteEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java), [PacketHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java)

<a id="run-02"></a>

### RUN-02: Make awakened Staff morph rendering safe

**Priority:** Critical. **Evidence:** Confirmed defect.

**Observed basis:** After the real Vesper graft, cycling Staff to Blade crashed with Duplicate delegates in the foil render path. A no-glint test override isolated the failure.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [COM-02](06-morphlings-and-puppets.md#com-02) (acceptance), [END-04](08-qliphoth-and-endings.md#end-04) (acceptance), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Correct the morph buffer adapter in LivingStaffMorphRenderer so base and foil passes do not resolve to the same delegate passed twice to VertexMultiConsumer. Use the existing item-render pipeline for distinct supported layers; do not permanently disable awakening glint. Preserve held-item transforms, morph timing, data components and armor-layer rendering.

**Recommended gameplay behavior:** The reward should strengthen the Staff experience without making its everyday form cycle unsafe.

**Acceptance:** Use the preserved awakened save with its test no-glint override removed in a disposable copy. Cycle Staff, Blade and Sickle repeatedly, left/right hands, first/third person, ordinary and awakened items. Include an enchanted case if supported. No crash, duplicate geometry or missing foil; server inventory remains unchanged by rendering.

**Current code/resource entry points:** [LivingStaffMorphRenderer.java](../../src/main/java/com/vincenthuto/hemomancy/client/render/item/harbinger/LivingStaffMorphRenderer.java), [LivingStaffMorphClientEvents.java](../../src/main/java/com/vincenthuto/hemomancy/client/event/LivingStaffMorphClientEvents.java), [LivingStaffItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java)

<a id="run-03"></a>

### RUN-03: Resolve control conflicts and validate native held menus

**Priority:** High. **Evidence:** Confirmed conflicts; held-menu coverage gap.

**Observed basis:** Default F conflicted with offhand swapping; C crafting/cycling conflicted, and cycling with an empty hand got stuck at a weapon form. The driver could not hold the GLFW radial/wheel open; that is not an established game bug.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [MEM-04](05-weaving-scars-and-teachings.md#mem-04) (acceptance), [COM-03](06-morphlings-and-puppets.md#com-03) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Audit the registered bindings and their event consumption/contexts. Assign nonconflicting defaults without overwriting existing user options. Separate selection from execution: selecting a weapon form must not prevent selecting a later entry when the Staff is absent. Keep deliberate Thelemic selection in the shared radial; do not silently broaden C semantics. Test native hold/release with supported native input or an explicit manual checklist, rather than marking driver emulation as a pass.

**Recommended gameplay behavior:** Show current bindings in tutorial text. State that loose vials use attack and the syringe uses item use. Display why a selected action cannot execute without trapping selection.

**Acceptance:** Fresh options and migrated custom bindings; formation with an occupied main/offhand; crafting versus cycling; empty-hand and Staff-held cycling; actual Z radial selecting a Thelemic Memory and Crossbar wheel issuing a command. If native input remains unavailable, retain that acceptance item as unverified.

**Current code/resource entry points:** [BloodCraftingKeyPressPacket.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodCraftingKeyPressPacket.java), [RadialChooseManipScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java), [CrossbarRadialScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/summon/CrossbarRadialScreen.java), [UseManipKeyPacket.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseManipKeyPacket.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
