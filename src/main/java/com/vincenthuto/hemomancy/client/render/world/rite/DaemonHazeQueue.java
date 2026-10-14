package com.vincenthuto.hemomancy.client.render.world.rite;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The daemon's smoke volume writes no depth, so anything translucent drawn after it would paint over it. The entity
 * pass only queues each daemon here; each box is drawn after the clouds and every other rite visual.
 */
public final class DaemonHazeQueue {
	/** More daemons than this queued without a flush means frames are being skipped; start over. */
	static final int LIMIT = 64;
	private static final List<Draw> PENDING = new ArrayList<>();
	private static final List<StaffDraw> STAFF = new ArrayList<>();

	/** The haze curling off a planted staff while its daemon is abroad: its box centre and the shader's inputs. */
	public record StaffDraw(double x, double y, double z, float time, float seed, float emergence, float strength) {
	}

	/**
	 * One daemon: its centre, height (blocks), body and head yaw (radians; the head within half a turn of the body,
	 * so the shader can blend them), its tilt off upright (a level axis and an angle in radians), box half-sizes, the
	 * shader's inputs, and how far it has flowed (heights) and how far its tail streams (0 to 1).
	 */
	public record Draw(double x, double y, double z, float height, float yaw, float headYaw, float tiltX, float tiltZ,
			float tiltAngle, float halfWidth, float halfHeight,
			float time, float seed, float emergence, float strength, float swallow, float eyes, int tint, float flare,
			float trailX, float trailY, float trailZ, float recall, float flow, float stream) {
	}

	private DaemonHazeQueue() {
	}

	public static void add(Draw draw) {
		if (PENDING.size() >= LIMIT) PENDING.clear();
		PENDING.add(draw);
	}

	public static void addStaff(StaffDraw draw) {
		if (STAFF.size() >= LIMIT) STAFF.clear();
		STAFF.add(draw);
	}

	/** The queued staff hazes, emptying their queue. */
	public static List<StaffDraw> drainStaff() {
		List<StaffDraw> drained = List.copyOf(STAFF);
		STAFF.clear();
		return drained;
	}

	/** The queued daemons, emptying the queue. */
	public static List<Draw> drain() {
		List<Draw> drained = List.copyOf(PENDING);
		PENDING.clear();
		return drained;
	}

	public static void clear() {
		PENDING.clear();
		STAFF.clear();
	}

	/** Draws every queued daemon, each in its own batch so its uniforms never reach another. */
	public static void render(PoseStack poseStack) {
		List<Draw> draws = drain();
		List<StaffDraw> staffs = drainStaff();
		if (draws.isEmpty() && staffs.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		float[] staffBox = boxVertices(DaemonHaze.STAFF_HAZE_HALF_WIDTH, DaemonHaze.STAFF_HAZE_HALF_HEIGHT);
		for (StaffDraw staff : staffs) {
			poseStack.pushPose();
			poseStack.translate(staff.x() - camera.x, staff.y() - camera.y, staff.z() - camera.z);
			Matrix4f matrix = poseStack.last().pose();
			Vector3f eye = DaemonHaze.eyeInBox(RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewMatrix(),
					matrix);
			RenderType type = HemoRenderTypes.cardinalStaffHaze(staff, eye.x, eye.y, eye.z);
			emitBox(buffers.getBuffer(type), matrix, staffBox);
			poseStack.popPose();
			buffers.endBatch(type);
		}
		for (Draw draw : draws) {
			poseStack.pushPose();
			poseStack.translate(draw.x() - camera.x, draw.y() - camera.y, draw.z() - camera.z);
			Matrix4f matrix = poseStack.last().pose();
			// Rays start at the eye this frame is drawn from, view bobbing included, so the volume never bounces.
			Vector3f eye = DaemonHaze.eyeInBox(RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewMatrix(),
					matrix);
			RenderType type = HemoRenderTypes.cardinalDaemonHaze(draw, eye.x, eye.y, eye.z);
			emitBox(buffers.getBuffer(type), matrix, boxVertices(draw.halfWidth(), draw.halfHeight()));
			poseStack.popPose();
			buffers.endBatch(type);
		}
	}

	private static void emitBox(VertexConsumer consumer, Matrix4f matrix, float[] box) {
		for (int at = 0; at < box.length; at += 6) {
			consumer.addVertex(matrix, box[at], box[at + 1], box[at + 2]).setUv(box[at + 3], box[at + 4])
					.setColor((int) box[at + 5], 255, 255, 255);
		}
	}

	/**
	 * The box's 24 vertices as {x, y, z, u, v, red}, each face counter-clockwise seen from outside. UV carries the
	 * local x and z and red the height (0 at the bottom, 255 at the top), so the shader rebuilds the box-local point
	 * whatever the view transform.
	 */
	public static float[] boxVertices(double halfWidth, double halfHeight) {
		double[] extent = {halfWidth, halfHeight, halfWidth};
		// Per face: the outward normal, then two in-face axes whose cross product is that normal.
		int[][] faces = {
				{1, 0, 0, 0, 0, -1, 0, 1, 0}, {-1, 0, 0, 0, 0, 1, 0, 1, 0},
				{0, 0, 1, 1, 0, 0, 0, 1, 0}, {0, 0, -1, -1, 0, 0, 0, 1, 0},
				{0, 1, 0, 1, 0, 0, 0, 0, -1}, {0, -1, 0, 1, 0, 0, 0, 0, 1}};
		int[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
		float[] out = new float[24 * 6];
		int at = 0;
		for (int[] face : faces) {
			for (int[] corner : corners) {
				double[] p = new double[3];
				for (int axis = 0; axis < 3; axis++) {
					p[axis] = (face[axis] + corner[0] * face[3 + axis] + corner[1] * face[6 + axis]) * extent[axis];
				}
				out[at++] = (float) p[0];
				out[at++] = (float) p[1];
				out[at++] = (float) p[2];
				out[at++] = (float) p[0];
				out[at++] = (float) p[2];
				out[at++] = p[1] > 0.0D ? 255.0F : 0.0F;
			}
		}
		return out;
	}
}
