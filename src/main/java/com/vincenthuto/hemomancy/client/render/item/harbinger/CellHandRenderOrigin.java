package com.vincenthuto.hemomancy.client.render.item.harbinger;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CellHandRenderOrigin {
	public static final float PALM_OFFSET_Y = 10.0F / 16.0F;

	private CellHandRenderOrigin() {
	}

	public static Vec3 fromPose(Matrix4f pose, Vec3 cameraPosition, float localX, float localY, float localZ) {
		Vector3f renderPosition = pose.transformPosition(localX, localY, localZ, new Vector3f());
		return cameraPosition.add(renderPosition.x(), renderPosition.y(), renderPosition.z());
	}
}
