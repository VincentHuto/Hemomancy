package com.vincenthuto.hemomancy.client.model.morphling;

import com.vincenthuto.hemomancy.client.model.entity.morphling.FungalHeadAttachmentModel;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingPartSpec;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainModels;
import com.vincenthuto.hemomancy.client.model.morphling.MorphlingModelAttachment.AttachmentPoint;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Brined Court Track 3: every strain grows on the anchors its appearance table names, from the stage it names,
 * through generated models whose parts carry stages and limb tags; the Mycophant Tendril's fungal head survives.
 */
final class MorphlingMutationRegistryTest {
	/** Anchor and the first stage it shows, per strain (spec appearance tables; see 03-strain-visuals.md). */
	private static final Map<String, Map<AttachmentPoint, Integer>> EXPECTED = Map.of(
			"sunburst", Map.of(AttachmentPoint.BODY, 2),
			"holdfast", Map.of(AttachmentPoint.ARMS, 2, AttachmentPoint.LEGS, 4),
			// The appearance table also grows calf finlets from Developing and a brow crest at Primal.
			"mermaids_purse", Map.of(AttachmentPoint.BODY, 2, AttachmentPoint.ARMS, 2, AttachmentPoint.LEGS, 2,
					AttachmentPoint.HEAD, 5),
			"dead_mans_rope", Map.of(AttachmentPoint.ARMS, 2, AttachmentPoint.BODY, 3),
			"ink_cap", Map.of(AttachmentPoint.HEAD, 2, AttachmentPoint.BODY, 5),
			"hushbell", Map.of(AttachmentPoint.BODY, 2),
			"marrowplume", Map.of(AttachmentPoint.ARMS, 2, AttachmentPoint.LEGS, 4),
			"kinbud", Map.of(AttachmentPoint.BODY, 2, AttachmentPoint.HEAD, 2));

	@Test
	void everyStrainGrowsOnItsSpecAnchors() {
		assertEquals(MorphlingStrains.ALL.size(), EXPECTED.size());
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			Map<AttachmentPoint, Integer> anchors = new EnumMap<>(AttachmentPoint.class);
			for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.forStrain(strain.id())) {
				assertNull(anchors.put(anchor.point(), anchor.visibleFrom()), strain.id() + " anchors once per point");
			}
			assertEquals(EXPECTED.get(strain.id()), anchors, strain.id());
		}
	}

	@Test
	void headReplacementsAreThePrimalHoodAndPolypFace() {
		Set<String> replacing = new HashSet<>();
		for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.ALL) {
			if (anchor.hideAt() >= 0) {
				assertEquals(AttachmentPoint.HEAD, anchor.point(), anchor.strainId());
				assertEquals(5, anchor.hideAt(), anchor.strainId() + " replaces the head only at Primal");
				replacing.add(anchor.strainId());
			}
		}
		assertEquals(Set.of("ink_cap", "kinbud"), replacing);
	}

	@Test
	void limbPartsAreTaggedForJointedRigsAndStagesNeverRunAhead() {
		for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.ALL) {
			Map<String, MorphlingPartSpec> byName = new HashMap<>();
			boolean limb = anchor.point() == AttachmentPoint.ARMS || anchor.point() == AttachmentPoint.LEGS;
			for (MorphlingPartSpec part : anchor.parts()) {
				String label = anchor.strainId() + " " + anchor.point() + " " + part.name();
				if (limb) assertNotEquals(MorphlingPartSpec.Limb.NONE, part.limb(), label + " limb tag");
				else assertEquals(MorphlingPartSpec.Limb.NONE, part.limb(), label);
				assertTrue(part.stage() >= anchor.visibleFrom() && part.stage() <= 5, label);
				if (!part.parent().equals("root")) {
					MorphlingPartSpec parent = byName.get(part.parent());
					assertNotNull(parent, label + " parent comes first");
					assertTrue(part.stage() >= parent.stage(), label + " grows after its parent");
					assertEquals(parent.stage() < part.stage(), part.grows(), label + " grows in at its own stage");
				}
				byName.put(part.name(), part);
			}
		}
	}

	@Test
	void everyStageAfterTheTintGrowsSomething() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			Set<Integer> stages = new HashSet<>();
			for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.forStrain(strain.id())) {
				anchor.parts().forEach(part -> stages.add(part.stage()));
			}
			// Unfed and Fledgling are tint only (the row's tint colour); later stages grow parts.
			assertTrue(stages.containsAll(Set.of(4, 5)), strain.id() + " Apex and Primal grow");
			assertTrue(stages.contains(2) || stages.contains(3), strain.id() + " Developing or Mature grows");
			assertNotEquals(0, strain.tintColor() >>> 24, strain.id() + " tint has an opacity");
		}
	}

	@Test
	void texturesArePackedAtOnePixelPerModelUnit() throws Exception {
		for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.ALL) {
			String path = "assets/" + anchor.texture().getNamespace() + "/" + anchor.texture().getPath();
			assertTrue(ResourceReader.exists(path), path);
			BufferedImage image = ImageIO.read(ResourceReader.path(path).toFile());
			// The generator packs box UVs at one texel per unit into the smallest power-of-two atlas that fits.
			assertEquals(1, Integer.bitCount(image.getWidth()), path);
			assertEquals(1, Integer.bitCount(image.getHeight()), path);
			assertTrue(image.getWidth() <= 256 && image.getHeight() <= 256, path);
		}
	}

	@Test
	void theTendrilKeepsItsFungalHead() {
		assertEquals("morphling_fungal_head", FungalHeadAttachmentModel.LAYER_LOCATION.getModel().getPath());
		assertTrue(ResourceReader.exists("assets/hemomancy/textures/models/morphling/fungal_head.png"));
		assertTrue(ResourceReader.exists("assets/hemomancy/models/entity/bbmodel/morphling/FungalHeadAttachmentModel.bbmodel"));
	}
}
