package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvatarManifestationTransitionTest {
	@Test
	void summonHugsThePlayerThenBurstsOut() {
		AvatarManifestationTransition transition = new AvatarManifestationTransition();

		var start = transition.update("summon_avatar_complete", 100, 0.0F);
		var swimming = transition.update("summon_avatar_complete", 112, 0.0F);
		var burst = transition.update("summon_avatar_complete", 118, 0.0F);
		var complete = transition.update("summon_avatar_complete", 120, 0.0F);

		assertEquals(AvatarManifestationTransition.Phase.SUMMONING, start.phase());
		assertEquals(0.65F, start.emergenceScale(2.0F), 0.0001F);
		assertEquals(0.65F, swimming.emergenceScale(2.0F), 0.0001F);
		assertEquals(0.0F, swimming.presence(), 0.0001F);
		assertTrue(Math.abs(swimming.swimOffset()) > 0.01F);
		assertTrue(burst.emergenceScale(2.0F) > 1.0F);
		assertTrue(burst.presence() > 0.9F);
		assertEquals(AvatarManifestationTransition.Phase.ACTIVE, complete.phase());
		assertEquals(1.0F, complete.playerVisualScale(2.0F, 2.0F), 0.0001F);
		assertEquals(1.0F, complete.presence(), 0.0001F);
	}

	@Test
	void dismissRetainsTheLastFormWhileItMelts() {
		AvatarManifestationTransition transition = new AvatarManifestationTransition();
		transition.update("summon_avatar_complete", 40, 0.0F);
		transition.update("summon_avatar_complete", 60, 0.0F);

		var start = transition.update("", 70, 0.0F);
		var middle = transition.update("", 80, 0.0F);
		var complete = transition.update("", 90, 0.0F);

		assertEquals("summon_avatar_complete", start.form());
		assertEquals(AvatarManifestationTransition.Phase.DISMISSING, start.phase());
		assertEquals(1.0F, start.playerVisualScale(1.0F, 2.0F), 0.0001F);
		assertEquals(0.5F, middle.meltProgress(), 0.0001F);
		assertTrue(middle.renders());
		assertFalse(complete.renders());
		assertEquals("", complete.form());
	}

	@Test
	void changingTierSummonsOnlyTheNewSelectedForm() {
		AvatarManifestationTransition transition = new AvatarManifestationTransition();
		transition.update("summon_avatar_arms", 0, 0.0F);
		transition.update("summon_avatar_arms", 20, 0.0F);

		var changed = transition.update("summon_avatar_legs", 30, 0.0F);

		assertEquals("summon_avatar_legs", changed.form());
		assertEquals(AvatarManifestationTransition.Phase.SUMMONING, changed.phase());
	}

	@Test
	void compactSummonShellSitsLowerAroundThePlayer() {
		AvatarManifestationTransition transition = new AvatarManifestationTransition();
		transition.update("summon_avatar_complete", 100, 0.0F);
		var swimming = transition.update("summon_avatar_complete", 112, 0.0F);
		PoseStack poseStack = new PoseStack();

		BloodAvatarLayer.applyEmergencePose(poseStack, swimming, 2.0F);
		Vector4f origin = poseStack.last().pose().transform(new Vector4f(0, 0, 0, 1));

		assertEquals(0.63F, origin.y, 0.0001F);
	}
}
