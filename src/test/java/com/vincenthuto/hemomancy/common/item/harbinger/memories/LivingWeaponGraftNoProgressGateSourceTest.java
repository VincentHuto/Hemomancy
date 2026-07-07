package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingWeaponGraftNoProgressGateSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponGraftNoProgressGateSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		for (String path : new String[] {
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponMemoryUnlocks.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRite.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRecipeUnlockEvents.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftItem.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponGraftData.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponForm.java" }) {
			String source = read(path);
			assertDoesNotContain(path + " must not gate base forms on LivingStaffProgress", source, "LivingStaffProgress");
			assertDoesNotContain(path + " must not gate base forms on ILivingStaffProgress", source, "ILivingStaffProgress");
			assertDoesNotContain(path + " must not introduce base form progress checks", source, "hasForm(");
		}
		assertNoPath("src/main/java/com/vincenthuto/hemomancy/client/screen/LivingArsenalScreen.java");
		assertNoPath("src/main/java/com/vincenthuto/hemomancy/client/screen/StaffFormUnlockScreen.java");
	}

	private static String read(String path) throws IOException {
		Path absolute = ROOT.resolve(path);
		if (!Files.exists(absolute)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(absolute).replace("\r\n", "\n");
	}

	private static void assertNoPath(String path) {
		if (Files.exists(ROOT.resolve(path))) {
			throw new AssertionError("unexpected new UI path exists: " + path);
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (contained '" + unexpected + "')");
		}
	}
}
