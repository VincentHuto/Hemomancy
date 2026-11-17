package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;

public final class LivingWeaponGraftNoProgressGateSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponGraftNoProgressGateSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		for (String path : new String[] {
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponMemoryUnlocks.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRecipeUnlockEvents.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftItem.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponGraftData.java",
				"src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponForm.java" }) {
			String source = read(path);
			assertNotContains(path + " must not gate base forms on LivingStaffProgress", source, "LivingStaffProgress");
			assertNotContains(path + " must not gate base forms on ILivingStaffProgress", source, "ILivingStaffProgress");
			assertNotContains(path + " must not introduce base form progress checks", source, "hasForm(");
		}
		String rite = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRite.java");
		assertContains("graft rite keeps vesper progress access special-cased", rite,
				"offering.is(ItemInit.memory_of_vesper.get())");
		assertContains("graft rite uses existing vesper staff progress", rite, "ILivingStaffProgress");
		assertNotContains("graft rite must not introduce base form progress checks", rite, "hasForm(");
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
}
