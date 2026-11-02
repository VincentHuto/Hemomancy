# Final Bloodlust Armor Abilities Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add reusable final armor set abilities and activate them through the existing manipulation radial menu.

**Architecture:** Add a common armor ability registry with item-specific full-set detection and server-authoritative activation. The radial menu displays a conditional inner item using the active armor set helmet and sends only an ability id to the server.

**Tech Stack:** Java 21, NeoForge 1.21 payload networking, existing Hemomancy radial screen classes, existing source/resource test style.

---

## File Structure

- Create `src/main/java/com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbility.java`: public interface for display, validation, activation, cooldown, and blood cost.
- Create `src/main/java/com/vincenthuto/hemomancy/common/armor/ability/SimpleArmorSetAbility.java`: reusable implementation backed by required slot item suppliers and a server activation callback.
- Create `src/main/java/com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java`: registers final Bloodlust abilities, finds the active player ability, and performs authoritative activation.
- Create `src/main/java/com/vincenthuto/hemomancy/common/armor/ability/FinalBloodlustArmorAbilityHandler.java`: server event hooks for flight/passives/reactives/Bastion damage prevention.
- Create `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/ActivateArmorSetAbilityC2SPacket.java`: C2S packet with only the ability id.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java`: register the new packet.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java`: add the conditional inner-wheel armor ability wedge.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodNeedleEntity.java`: allow Bloodburst needles to add Hunger and Wither without changing ordinary blood needles.
- Modify `docs/HEMOMANCY_REFERENCE.md`: document final armor abilities and radial activation.
- Create `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/FinalBloodlustArmorAbilitySourceTest.java`: source-level regression test for registry, packet, radial integration, and ability ids.

## Tasks

### Task 1: Source Test

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/FinalBloodlustArmorAbilitySourceTest.java`

- [ ] **Step 1: Write the failing source test**

```java
package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FinalBloodlustArmorAbilitySourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private FinalBloodlustArmorAbilitySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String registry = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java"));
		String packet = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/ActivateArmorSetAbilityC2SPacket.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String radial = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java"));
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/FinalBloodlustArmorAbilityHandler.java"));

		assertContains("edacious id", registry, "edacious_bloodburst");
		assertContains("sheolic id", registry, "sheolic_bastion_stance");
		assertContains("phantasmal id", registry, "masquerade_of_the_forgotten");
		assertContains("edacious full set", registry, "ItemInit.edacious_blood_lust_helm");
		assertContains("sheolic full set", registry, "ItemInit.sheolic_blood_lust_helm");
		assertContains("phantasmal full set", registry, "ItemInit.phantasmal_blood_lust_helm");
		assertContains("packet carries resource id", packet, "ResourceLocation abilityId");
		assertContains("packet validates via registry", packet, "ArmorSetAbilityRegistry.tryActivate");
		assertContains("packet registered", packetHandler, "ActivateArmorSetAbilityC2SPacket.TYPE");
		assertContains("radial queries active ability", radial, "ArmorSetAbilityRegistry.getActiveAbility");
		assertContains("radial uses item icon", radial, "ItemStackRadialMenuItem");
		assertContains("radial sends activation packet", radial, "new ActivateArmorSetAbilityC2SPacket");
		assertContains("flight passive handled", handler, "updateEdaciousFlight");
		assertContains("bastion damage handled", handler, "isBastionActive");
		assertContains("phantasmal reactive handled", handler, "triggerDistortedPresence");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat compileTestJava`

Expected: PASS compile, then running the test class fails because the ability source files do not exist yet.

### Task 2: Ability Registry and Packet

**Files:**
- Create the three common ability classes.
- Create `ActivateArmorSetAbilityC2SPacket.java`.
- Modify `PacketHandler.java`.

- [ ] **Step 1: Implement the registry and packet**

Use the registry ids and item suppliers specified in the design. `tryActivate` must reject unknown ids, missing full sets, failed validation, insufficient blood, and active cooldown before calling `activate`.

- [ ] **Step 2: Register the packet**

Add `net.playToServer(ActivateArmorSetAbilityC2SPacket.TYPE, ActivateArmorSetAbilityC2SPacket.STREAM_CODEC, ActivateArmorSetAbilityC2SPacket::handle);`.

- [ ] **Step 3: Run the source test**

Run: `./gradlew.bat compileTestJava`

Expected: compile succeeds.

### Task 3: Radial Menu Integration

**Files:**
- Modify `RadialChooseManipScreen.java`.

- [ ] **Step 1: Add armor ability inner item creation**

After Blood Absorption and Blood Projection are added, call `ArmorSetAbilityRegistry.getActiveAbility(mc.player)` and append an `ItemStackRadialMenuItem` that sends `ActivateArmorSetAbilityC2SPacket`.

- [ ] **Step 2: Run the source test**

Run: `./gradlew.bat compileTestJava`

Expected: compile succeeds.

### Task 4: Ability Effects and Passives

**Files:**
- Modify `BloodNeedleEntity.java`.
- Create/finish `FinalBloodlustArmorAbilityHandler.java`.

- [ ] **Step 1: Implement Bloodburst**

Spawn radial BloodNeedle projectiles, tag them as Bloodburst needles, and apply Blood Loss, Hunger, and Wither on hit.

- [ ] **Step 2: Implement Bastion Stance**

Store a timed persistent-data marker, suppress movement, disable flight, zero incoming damage, and allow reactivation to cancel.

- [ ] **Step 3: Implement Masquerade of the Forgotten and Phantasmal Step passive**

Spawn short-lived Phantasmal Echo decoys through the armor radial ability, and make the existing Umbral Step manipulation ignore darkness and cooldown checks while the full Phantasmal Bloodlust set is worn.

- [ ] **Step 4: Implement passives/reactives**

Apply Edacious slow flight, Sheolic fall/fire/lava immunity and Crimson Retribution, and Phantasmal attacker displacement.

### Task 5: Documentation and Verification

**Files:**
- Modify `docs/HEMOMANCY_REFERENCE.md`.

- [ ] **Step 1: Document final armor abilities**

Add the radial activation rule, full-set requirement, and ability summaries.

- [ ] **Step 2: Run focused verification**

Run:

```powershell
./gradlew.bat compileJava compileTestJava
./gradlew.bat test --tests com.vincenthuto.hemomancy.common.item.harbinger.FinalBloodlustArmorAbilitySourceTest
```

Expected: both commands exit 0.

- [ ] **Step 3: Run existing related tests**

Run:

```powershell
./gradlew.bat test --tests com.vincenthuto.hemomancy.common.item.harbinger.FinalBloodLustArmorResourceTest --tests com.vincenthuto.hemomancy.common.item.harbinger.GrinningMaskRenameResourceTest --tests com.vincenthuto.hemomancy.common.recipe.ArmorSetProgressionResourceTest
```

Expected: command exits 0.
