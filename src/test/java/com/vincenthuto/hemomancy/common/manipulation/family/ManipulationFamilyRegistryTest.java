package com.vincenthuto.hemomancy.common.manipulation.family;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ManipulationFamilyRegistryTest {
	@Test
	void masteryUsesCumulativeThresholdsAndCapsEfficiencyAtStageFour() {
		ManipLevel level = new ManipLevel(0, 9);
		assertFalse(level.tryLevelUp());
		level.setXp(10);
		assertTrue(level.tryLevelUp());
		level.setXp(34);
		assertFalse(level.tryLevelUp());
		level.setXp(35);
		assertTrue(level.tryLevelUp());
		level.setXp(85);
		assertTrue(level.tryLevelUp());
		level.setXp(185);
		assertTrue(level.tryLevelUp());

		assertEquals(4, level.getCurrentLevel());
		assertEquals(0.8D, level.getCostMultiplier());
		assertEquals(0.8D, level.getCooldownMultiplier());
		assertFalse(level.tryLevelUp());
	}

	@Test
	void declaresTheSevenApprovedFamiliesAndTwentyForms() {
		assertEquals(7, ManipulationFamilyRegistry.families().size());
		assertEquals(20, ManipulationFamilyRegistry.families().stream()
				.mapToInt(family -> family.forms().size()).sum());
		assertEquals("blood_shot", ManipulationFamilyRegistry.family("sanguine_halo")
				.orElseThrow().baselineId());
		assertEquals(4, ManipulationFamilyRegistry.form("sanguine_halo")
				.orElseThrow().requiredLevel());
	}

	@Test
	void everyFormHasItsOwnSixteenPixelOverlay() throws Exception {
		Path root = Path.of("src/main/resources/assets/hemomancy/textures/item/memories");
		var signatures = new HashSet<String>();
		for (ManipulationFormDefinition form : ManipulationFamilyRegistry.families().stream()
				.flatMap(family -> family.forms().stream()).toList()) {
			var image = ImageIO.read(root.resolve("memory_" + form.id() + "_overlay.png").toFile());
			assertNotNull(image, form.id());
			assertEquals(16, image.getWidth(), form.id());
			assertEquals(16, image.getHeight(), form.id());
			int[] pixels = image.getRGB(0, 0, 16, 16, null, 0, 16);
			assertTrue(Arrays.stream(pixels).anyMatch(pixel -> pixel >>> 24 != 0), form.id());
			assertTrue(signatures.add(Arrays.toString(pixels)), form.id() + " reuses another form overlay");
		}
	}

	@Test
	void normalizingAFamilySharesItsHighestSavedMastery() {
		BloodManipulation baseline = manipulation("blood_shot");
		BloodManipulation form = manipulation("guided_blood_shot");
		var known = new LinkedHashMap<BloodManipulation, ManipLevel>();
		known.put(baseline, new ManipLevel(1, 14));
		known.put(form, new ManipLevel(3, 90));

		assertTrue(ManipulationFamilyRegistry.normalizeKnown(known));
		assertSame(known.get(baseline), known.get(form));
		assertEquals(3, known.get(baseline).getCurrentLevel());
		assertEquals(90, known.get(baseline).getXp());
	}

	private static BloodManipulation manipulation(String name) {
		return new BloodManipulation(name, 10, 0, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);
	}
}
