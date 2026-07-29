package com.vincenthuto.hemomancy.common.rite.sigil;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class AwakenedIchorianSigilBodyAnimationTest {
	@Test
	void bodyContinuesBreathingAndShiftingBetweenFlightPositions() {
		ResourceLocation reservoir = ResourceLocation.parse("hemomancy:reservoir");
		var first = AwakenedIchorianSigilBodyAnimation.pose(reservoir, 50.0F, 0.04F);
		var later = AwakenedIchorianSigilBodyAnimation.pose(reservoir, 61.0F, 0.04F);

		assertNotEquals(first, later);
		assertNotEquals(first.scaleY(), later.scaleY());
		assertNotEquals(first.offsetY(), later.offsetY());
	}

	@Test
	void differentSigilsDoNotMoveLikeOneRepeatedPuppet() {
		var reservoir = AwakenedIchorianSigilBodyAnimation.pose(
				ResourceLocation.parse("hemomancy:reservoir"), 80.0F, 0.06F);
		var lens = AwakenedIchorianSigilBodyAnimation.pose(
				ResourceLocation.parse("hemomancy:lens"), 80.0F, 0.06F);

		assertNotEquals(reservoir, lens);
	}

	@Test
	void secondaryMotionRemainsOrganicAndReadable() {
		for (String id : new String[] {"reservoir", "bastion", "suture", "shunt", "lens"}) {
			for (int age = 0; age < 240; age++) {
				var pose = AwakenedIchorianSigilBodyAnimation.pose(
						ResourceLocation.parse("hemomancy:" + id), age, 0.08F);
				assertTrue(Math.abs(pose.offsetX()) <= 0.12F);
				assertTrue(Math.abs(pose.offsetY()) <= 0.12F);
				assertTrue(Math.abs(pose.offsetZ()) <= 0.12F);
				assertTrue(pose.scaleX() >= 0.85F && pose.scaleX() <= 1.15F);
				assertTrue(pose.scaleY() >= 0.85F && pose.scaleY() <= 1.15F);
				assertTrue(pose.scaleZ() >= 0.85F && pose.scaleZ() <= 1.15F);
			}
		}
	}
}
