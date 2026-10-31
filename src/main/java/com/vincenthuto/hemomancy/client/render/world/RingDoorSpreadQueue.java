package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * The ring door's opening spread is translucent and writes no depth, and the door's slab and rings are drawn from a
 * buffer flushed at the end of the entity pass. Drawn from the door's renderer, the spread is flushed as soon as
 * anything else asks for a buffer and the slab then paints over it. So the renderer only queues each door here, with
 * its pose, and the spread is drawn after the translucent blocks, over a slab already down.
 */
public final class RingDoorSpreadQueue {
	/** More doors than this queued without a flush means frames are being skipped; start over. */
	static final int LIMIT = 16;
	private static final List<Spread> PENDING = new ArrayList<>();

	/**
	 * One door's spread: the door's pose (its frame, in door pixels over 16), the shader's inputs and the two layers it
	 * covers: the slab face {@code slab} (x0, y0, x1, y1, z) and the well's front {@code well}, likewise.
	 */
	public record Spread(PoseStack.Pose pose, float time, float progress, float seed, float mode, Vec3 origin,
			Vec3 anchor, Vec3 right, float wellX, float wellY, float wellRadius, float reach, float[] slab, float[] well,
			int light) {
	}

	private RingDoorSpreadQueue() {
	}

	public static void add(Spread spread) {
		if (PENDING.size() >= LIMIT) PENDING.clear();
		PENDING.add(spread);
	}

	public static void clear() {
		PENDING.clear();
	}

	/** Draws every queued door, each in its own batch so its uniforms never reach another. */
	public static void render(PoseStack poseStack) {
		if (PENDING.isEmpty()) return;
		List<Spread> spreads = List.copyOf(PENDING);
		PENDING.clear();
		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		for (Spread spread : spreads) {
			RenderType type = HemoRenderTypes.ringDoorOpening(spread.time(), spread.progress(), spread.seed(),
					spread.mode(), spread.origin(), spread.anchor(), spread.right(), spread.wellX(), spread.wellY(),
					spread.wellRadius(), spread.reach());
			poseStack.pushPose();
			poseStack.last().pose().mul(spread.pose().pose());
			poseStack.last().normal().mul(spread.pose().normal());
			VertexConsumer out = buffers.getBuffer(type);
			face(out, poseStack.last(), spread.slab(), 0, spread.light());
			face(out, poseStack.last(), spread.well(), 1, spread.light());
			poseStack.popPose();
			buffers.endBatch(type);
		}
	}

	/** One flat layer facing the nave in door pixels, {@code layer} riding in red for the shader. */
	private static void face(VertexConsumer out, PoseStack.Pose pose, float[] box, float layer, int light) {
		Matrix4f matrix = pose.pose();
		float s = 1 / 16F;
		float[][] corners = {{box[0], box[1]}, {box[2], box[1]}, {box[2], box[3]}, {box[0], box[3]}};
		for (float[] c : corners)
			out.addVertex(matrix, c[0] * s, c[1] * s, box[4] * s).setColor(layer, 0F, 0F, 1F).setUv(0, 0)
					.setLight(light).setNormal(pose, 0, 0, -1);
	}
}
