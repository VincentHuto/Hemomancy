package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PhantasmalUmbralStepSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private PhantasmalUmbralStepSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ArmorSetHelper.java"));
		String umbralStep = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/manipulation/tenebris/UmbralStepManip.java"));
		String bloodManipulation = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java"));
		String useManipPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseManipKeyPacket.java"));

		assertContains("helper exposes full set check", helper, "hasFullPhantasmalBloodlust");
		assertContains("helper requires helmet", helper, "ItemInit.phantasmal_blood_lust_helm");
		assertContains("helper requires chest", helper, "ItemInit.phantasmal_blood_lust_chest");
		assertContains("helper requires legs", helper, "ItemInit.phantasmal_blood_lust_legs");
		assertContains("helper requires boots", helper, "ItemInit.phantasmal_blood_lust_boots");

		assertContains("umbral step uses helper", umbralStep, "ArmorSetHelper.hasFullPhantasmalBloodlust(player)");
		assertContains("umbral step keeps line of sight targeting", umbralStep, "world.clip(new ClipContext");
		assertContains("umbral step keeps configured range", umbralStep, "BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player)");
		assertContains("phantasmal bypasses light only", umbralStep,
				"if (!phantasmalStep && !BlackVeilCovenantManager.isDarkEnough");
		assertContains("umbral step exposes cooldown override", umbralStep, "public boolean ignoresCooldown(Player player)");
		assertContains("blood manipulation checks override before cooldown", bloodManipulation, "!ignoresCooldown(player) && isAnyManipOnCooldown(player)");
		assertContains("blood manipulation skips starting cooldown", bloodManipulation, "ignoresCooldown(player) ? 0L : startCooldown(player)");
		assertContains("unified use packet respects selected override", useManipPacket, "selManip.ignoresCooldown(player)");
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
