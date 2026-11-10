package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TemperWeaponHelperSourceTest {
	private TemperWeaponHelperSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/TemperWeaponHelper.java"));
		assertContains("helper imports WillEntity", helper,
				"import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;");
		assertContains("Will targets use synced school", helper, "target instanceof WillEntity will");
		assertContains("Will school routes through pure helper", helper,
				"return isOpposingTemper(weaponTemper, will.getSchool());");
		assertContains("pure temper opposition helper exists", helper,
				"public static boolean isOpposingTemper(@Nullable EnumBloodTemper weaponTemper,");
		assertContains("pure helper compares against opposing temper", helper,
				"getOpposingTemper(weaponTemper) == targetTemper");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}
}
