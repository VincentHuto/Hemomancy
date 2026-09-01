package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvatarHeldItemTransformTest {
	@Test
	void oversizedItemKeepsTheVanillaHandAnchor() {
		PoseStack poseStack = new PoseStack();
		AvatarHeldItemTransform.apply(poseStack, HumanoidArm.RIGHT);
		Vector4f origin = poseStack.last().pose().transform(new Vector4f(0, 0, 0, 1));

		assertEquals(-0.0625F, origin.x, 0.0001F);
		assertEquals(0.625F, origin.y, 0.0001F);
		assertEquals(-0.125F, origin.z, 0.0001F);
		assertEquals(1.0F,
				poseStack.last().pose().transformDirection(new Vector3f(1, 0, 0)).length(), 0.0001F);
	}

	@Test
	void worldAttachedCopiesUseThirdPersonItemTransforms() {
		assertEquals(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
				AvatarHeldItemTransform.displayContext(HumanoidArm.RIGHT));
		assertEquals(ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
				AvatarHeldItemTransform.displayContext(HumanoidArm.LEFT));
	}
}
