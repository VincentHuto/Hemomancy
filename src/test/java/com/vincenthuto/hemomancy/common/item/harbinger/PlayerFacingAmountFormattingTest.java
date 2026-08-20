package com.vincenthuto.hemomancy.common.item.harbinger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PlayerFacingAmountFormattingTest {
	@Test
	void widenedFloatCostsDoNotLeakIntoTooltips() throws Exception {
		Method trimCost = MuscleMemoryTinctureItem.class.getDeclaredMethod("trimCost", double.class);
		trimCost.setAccessible(true);

		assertEquals("0.3", trimCost.invoke(null, 0.30000001192092896D));
		assertEquals("12", trimCost.invoke(null, 12D));
	}

	@Test
	void bloodMessagesUseTheExistingBoundedFormatter() throws Exception {
		String events = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java"));
		String radial = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java"));

		assertTrue(events.contains("BloodGourdItem.formatBloodAmount(volume.getBloodVolume())"));
		assertFalse(events.contains("ChatFormatting.GOLD + volume.getBloodVolume()"));
		assertTrue(radial.contains("BloodGourdItem.formatBloodAmount(volCap.getBloodVolume())"));
		assertTrue(radial.contains("BloodGourdItem.formatBloodAmount(bloodVolume.getBloodVolume())"));
	}
}
