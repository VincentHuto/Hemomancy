package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingTorchRenderPlacementTest {
	@Test
	void authoredEmitterIsTheFlameTipRatherThanTheModelCenter() {
		assertEquals(27.5F / 16.0F, LivingTorchRenderPlacement.TIP_LOCAL_Y, 0.0001F);
		assertTrue(LivingTorchRenderPlacement.TIP_LOCAL_Y > 1.5F);
	}

	@Test
	void firstPersonTipUsesTheSameCustomModelTransformAsRendering() {
		PoseStack poseStack = new PoseStack();
		LivingTorchRenderPlacement.applyCustomModelTransform(
				poseStack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
		Vec3 tip = LivingTorchRenderPlacement.tipFromCurrentPose(poseStack, Vec3.ZERO);
		assertTrue(tip.distanceTo(Vec3.ZERO) > 0.75D,
				"the transformed flame tip must not collapse to the camera/crosshair origin");
	}
}
