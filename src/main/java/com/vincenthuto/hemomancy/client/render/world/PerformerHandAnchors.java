package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Where each Circus performer's right hand was drawn this frame, so control threads leave the visible fist through
 * walking swings, act poses, flips and spins. Entities render before the thread stage, so the anchor is always the
 * current frame's. It is stored as an offset from the interpolated entity position: a performer culled from view
 * keeps its last offset instead of snapping to a guessed hand.
 */
public final class PerformerHandAnchors {
	private static final Map<Entity, Vec3> OFFSETS = new WeakHashMap<>();

	private PerformerHandAnchors() {
	}

	public static void record(Entity entity, Vec3 worldOffset) {
		OFFSETS.put(entity, worldOffset);
	}

	/** The drawn hand in world space, or null before the performer has been rendered once. */
	public static Vec3 anchor(Entity entity, float partialTick) {
		Vec3 offset = OFFSETS.get(entity);
		if (offset == null) {
			return null;
		}
		return new Vec3(Mth.lerp(partialTick, entity.xOld, entity.getX()) + offset.x,
				Mth.lerp(partialTick, entity.yOld, entity.getY()) + offset.y,
				Mth.lerp(partialTick, entity.zOld, entity.getZ()) + offset.z);
	}

	/** Bottom centre of an arm's own cubes, in model pixels; props on child parts are ignored. */
	public static Vector3f gripOf(ModelPart arm) {
		float[] bounds = { Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
				Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY };
		arm.visit(new PoseStack(), (pose, path, index, cube) -> {
			if (!path.isEmpty()) return;
			bounds[0] = Math.min(bounds[0], cube.minX);
			bounds[1] = Math.max(bounds[1], cube.maxX);
			bounds[2] = Math.max(bounds[2], cube.maxY);
			bounds[3] = Math.min(bounds[3], cube.minZ);
			bounds[4] = Math.max(bounds[4], cube.maxZ);
		});
		if (bounds[0] > bounds[1]) {
			return new Vector3f(-1.0F, 10.0F, 0.0F); // a vanilla arm, for parts without their own cubes
		}
		return new Vector3f((bounds[0] + bounds[1]) * 0.5F, bounds[2], (bounds[3] + bounds[4]) * 0.5F);
	}

	/**
	 * Converts a hand position from the render frame into a world-axis offset from the entity root. The entity root
	 * matrix is the pose the dispatcher handed the renderer, so its linear part is whatever base rotation the frame
	 * carries; inverting it keeps the offset independent of the camera.
	 */
	public static Vec3 worldOffset(Matrix4f entityRoot, Vector3f handInFrame) {
		Vector3f delta = handInFrame.sub(entityRoot.transformPosition(new Vector3f()), new Vector3f());
		entityRoot.get3x3(new Matrix3f()).invert().transform(delta);
		return new Vec3(delta.x, delta.y, delta.z);
	}
}
