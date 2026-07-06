package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TendencyWeaponHelperSourceTest {
	private TendencyWeaponHelperSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/TendencyWeaponHelper.java"));
		assertContains("helper imports WillEntity", helper,
				"import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;");
		assertContains("Will targets use synced school", helper, "target instanceof WillEntity will");
		assertContains("Will school routes through pure helper", helper,
				"return isOpposingTendency(weaponTendency, will.getSchool());");
		assertContains("pure tendency opposition helper exists", helper,
				"public static boolean isOpposingTendency(@Nullable EnumBloodTendency weaponTendency,");
		assertContains("pure helper compares against opposing tendency", helper,
				"getOpposingTendency(weaponTendency) == targetTendency");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}
}
