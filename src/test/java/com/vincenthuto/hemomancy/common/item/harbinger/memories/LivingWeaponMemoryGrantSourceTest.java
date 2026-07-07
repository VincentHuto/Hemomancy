package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingWeaponMemoryGrantSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponMemoryGrantSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulationGrantHelper.java");
		String memoryItem = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java");
		String unlocks = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponMemoryUnlocks.java");

		assertContains("helper exposes memory grant result", helper, "record MemoryGrantResult");
		assertContains("helper exposes status enum", helper, "enum MemoryGrantStatus");
		assertContains("helper validates active blood", helper, "NO_ACTIVE_BLOOD");
		assertContains("helper validates rank gate", helper, "RANK_TOO_LOW");
		assertContains("helper validates already known", helper, "ALREADY_KNOWN");
		assertContains("helper grants and syncs player memory", helper, "grantMemory(ServerPlayer player");
		assertContains("helper syncs known manipulations", helper, "new KnownManipulationServerPacket(known)");
		assertContains("helper preserves open-slot auto equip", helper, "ManipulationEquipHelper.equipNameIfPossible");

		assertContains("memory item delegates grant logic", memoryItem, "KnownManipulationGrantHelper.grantMemory");
		assertContains("memory item consumes only successful grant", memoryItem, "result.success()");
		assertContains("memory item keeps retired memory guard", memoryItem, "isRetiredMemoryItem()");

		assertContains("living weapon unlock helper exists", unlocks, "final class LivingWeaponMemoryUnlocks");
		assertContains("unlock helper delegates blade", unlocks, "ManipulationInit.conjure_blade");
		assertContains("unlock helper delegates axe", unlocks, "ManipulationInit.conjure_axe");
		assertContains("unlock helper delegates spear", unlocks, "ManipulationInit.conjure_spear");
		assertContains("unlock helper delegates claws", unlocks, "ManipulationInit.conjure_claws");
		assertContains("unlock helper delegates crossbow", unlocks, "ManipulationInit.conjure_crossbow");
		assertContains("unlock helper delegates torch", unlocks, "ManipulationInit.conjure_torch");
		assertContains("unlock helper delegates flail", unlocks, "ManipulationInit.conjure_flail");
		assertContains("unlock helper uses shared grant", unlocks, "KnownManipulationGrantHelper.grantMemory");
	}

	private static String read(String path) throws IOException {
		Path absolute = ROOT.resolve(path);
		if (!Files.exists(absolute)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(absolute).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
