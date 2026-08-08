package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

final class VesperFightFloorTransform {
	private VesperFightFloorTransform() {
	}

	static Matrix4f arenaModelView(Matrix4f levelModelView, Vec3 cameraPosition, BlockPos center) {
		return new Matrix4f(levelModelView).translate(
				(float) (center.getX() - cameraPosition.x),
				(float) (center.getY() + 1.0D - cameraPosition.y),
				(float) (center.getZ() - cameraPosition.z));
	}
}
