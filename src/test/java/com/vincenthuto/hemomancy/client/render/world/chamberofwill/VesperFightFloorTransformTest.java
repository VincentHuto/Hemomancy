package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VesperFightFloorTransformTest {
	@Test
	void arenaRetainsTheLevelCameraRotationWhileAnchoringToItsWorldCenter() {
		Matrix4f levelModelView = new Matrix4f().rotateZ((float) (Math.PI / 2.0D));
		BlockPos center = new BlockPos(100, 64, 200);
		Vec3 camera = new Vec3(90.0D, 63.0D, 195.0D);

		Matrix4f arenaModelView = VesperFightFloorTransform.arenaModelView(levelModelView, camera, center);
		Vector3f transformed = arenaModelView.transformPosition(new Vector3f(1.0F, 0.0F, 0.0F));

		assertEquals(-2.0F, transformed.x, 0.0001F);
		assertEquals(11.0F, transformed.y, 0.0001F);
		assertEquals(5.0F, transformed.z, 0.0001F);
	}
}
