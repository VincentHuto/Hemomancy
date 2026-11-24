package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AwakenedIchorianSigilBodyAnimationTest {
	private static final List<String> KNOWN = List.of(
			"reservoir", "bastion", "hematic_lattice", "mnemonic",
			"suture", "shunt", "seal", "cage", "lens");

	@Test
	void everyHoveringFormKeepsBreathing() {
		for (String id : KNOWN) {
			ResourceLocation sigil = ResourceLocation.parse("hemomancy:" + id);
			var first = AwakenedIchorianSigilBodyAnimation.pose(sigil, 50.0F);
			var later = AwakenedIchorianSigilBodyAnimation.pose(sigil, 61.0F);

			assertNotEquals(first, later, id + " froze at rest");
			assertNotEquals(first.scaleY(), later.scaleY(), id + " stopped breathing");
			assertNotEquals(first.offsetY(), later.offsetY(), id + " stopped drifting");
		}
	}

	@Test
	void differentSigilsDoNotMoveLikeOneRepeatedPuppet() {
		var reservoir = AwakenedIchorianSigilBodyAnimation.pose(
				ResourceLocation.parse("hemomancy:reservoir"), 80.0F);
		var lens = AwakenedIchorianSigilBodyAnimation.pose(
				ResourceLocation.parse("hemomancy:lens"), 80.0F);

		assertNotEquals(reservoir, lens);
	}

	@Test
	void theShuntIsLivelierThanTheSeal() {
		assertTrue(offsetRange("shunt") > offsetRange("seal"),
				"the shunt's quickness and the seal's stillness carry into the idle");
	}

	@Test
	void secondaryMotionRemainsOrganicAndReadable() {
		for (String id : KNOWN) {
			for (int age = 0; age < 240; age++) {
				var pose = AwakenedIchorianSigilBodyAnimation.pose(
						ResourceLocation.parse("hemomancy:" + id), age);
				assertTrue(Math.abs(pose.offsetX()) <= 0.12F);
				assertTrue(Math.abs(pose.offsetY()) <= 0.12F);
				assertTrue(Math.abs(pose.offsetZ()) <= 0.12F);
				assertTrue(pose.scaleX() >= 0.85F && pose.scaleX() <= 1.15F);
				assertTrue(pose.scaleY() >= 0.85F && pose.scaleY() <= 1.15F);
				assertTrue(pose.scaleZ() >= 0.85F && pose.scaleZ() <= 1.15F);
			}
		}
	}

	private static float offsetRange(String id) {
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;
		for (int age = 0; age < 200; age++) {
			float x = AwakenedIchorianSigilBodyAnimation.pose(
					ResourceLocation.parse("hemomancy:" + id), age).offsetX();
			min = Math.min(min, x);
			max = Math.max(max, x);
		}
		return max - min;
	}
}
