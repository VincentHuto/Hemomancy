package com.vincenthuto.hemomancy.client.render.tile.shared;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.data.LidlessPassageClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberProxyView;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberRoomShape;
import com.vincenthuto.hemomancy.common.tile.shared.ChamberGateBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * The corridor's end and the corridor over it: the third eye, and the ribs that arch over the walk to the arbor. The
 * ribs are the Monolith's black living surface, smooth tubes that swell at the root and thin toward the crown, each
 * leaning and wandering a little its own way. They are drawn, not built, so a room that grows only redraws them.
 */
public class ChamberGateRenderer implements BlockEntityRenderer<ChamberGateBlockEntity> {
	private static final int RIB_SPACING = 3;
	private static final float SPRING_HEIGHT = 3.0F;
	private static final float ROOT_DEPTH = 1.6F;
	private static final int ALONG = 52;
	private static final int AROUND = 8;
	private static final int FLOOR_OFFSET = -1;
	/** Baked once per room size; the room's radius never passes the manager's maximum. */
	private static final float[][] RIB_MESHES = new float[ChamberOfWillManager.MAX_ROOM_RADIUS + 1][];

	public ChamberGateRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(ChamberGateBlockEntity gate) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96;
	}

	@Override
	public AABB getRenderBoundingBox(ChamberGateBlockEntity gate) {
		int radius = gate.radius();
		double side = ChamberRoomShape.halfWidth(radius) + 3.0D;
		double height = SPRING_HEIGHT + ChamberRoomShape.halfWidth(radius) + 4.0D;
		return new AABB(gate.getBlockPos()).inflate(side, height, 1.0D)
				.expandTowards(0.0D, 0.0D, -ChamberRoomShape.length(radius) - 1.0D);
	}

	@Override
	public void render(ChamberGateBlockEntity gate, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		if (gate.getLevel() == null) return;
		float time = AnimationTime.gameTime(gate.getLevel(), partialTick);
		float seed = LidlessDoorRenderer.seed(gate.getBlockPos());

		int radius = gate.radius();
		drawRibs(pose, buffers, radius, time, seed);

		var local = LidlessPassageClientState.local();
		if (local != null && local.door().equals(gate.getBlockPos())) return;
		// An open eye is a window the other way: the overworld around the husk, drawn by the proxy view.
		float opening = gate.opening(partialTick);
		int live = 0;
		if (gate.outside() != null && opening > 0.02F) {
			ChamberProxyView.requestGate(gate);
			live = ChamberProxyView.textureFor(gate.getBlockPos());
		}
		pose.pushPose();
		pose.translate(0.5D, 0.0D, 0.5D);
		// The pose faces the portal's front, north, along +Z.
		pose.mulPose(Axis.YP.rotationDegrees(180.0F));
		LidlessPortalPanel.draw(pose, buffers, time, seed, Direction.NORTH, opening, false, gate.theme(), 1.0F, live);
		pose.popPose();
	}

	/** The corridor's ribs, with the pose at the gate block's corner. The husk's live eye draws them too. */
	public static void drawRibs(PoseStack pose, MultiBufferSource buffers, int radius, float time, float seed) {
		radius = Math.clamp(radius, ChamberOfWillManager.BASE_ROOM_RADIUS, ChamberOfWillManager.MAX_ROOM_RADIUS);
		if (RIB_MESHES[radius] == null) RIB_MESHES[radius] = bakeRibs(radius);
		float[] mesh = RIB_MESHES[radius];
		VertexConsumer surface = buffers.getBuffer(HemoRenderTypes.monolithEntitySurface(time / 20.0F, seed, 0.45F, 0.88F, 7.5F));
		pose.pushPose();
		pose.translate(0.5D, FLOOR_OFFSET + 1.0D, 0.5D);
		Matrix4f matrix = pose.last().pose();
		PoseStack.Pose last = pose.last();
		for (int i = 0; i < mesh.length; i += 8) {
			surface.addVertex(matrix, mesh[i], mesh[i + 1], mesh[i + 2]).setColor(0xFF0D0505).setUv(mesh[i + 3], mesh[i + 4])
					.setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(last, mesh[i + 5], mesh[i + 6], mesh[i + 7]);
		}
		pose.popPose();
	}

	/** Every rib of a room of this radius, relative to the gate: vertex x, y, z, u, v, nx, ny, nz, as quads. */
	private static float[] bakeRibs(int radius) {
		java.util.ArrayList<float[]> out = new java.util.ArrayList<>();
		// Just beyond the walkway, so each leg's root grips the floor's edge.
		float span = ChamberRoomShape.halfWidth(radius) + 0.85F;
		int length = ChamberRoomShape.length(radius);
		for (int back = 0; back <= length; back += RIB_SPACING) {
			java.util.Random random = new java.util.Random(radius * 31L + back);
			rib(out, random, span, -back, back == 0 ? 1.5F : 1.0F);
		}
		float[] mesh = new float[out.size() * 8];
		for (int i = 0; i < out.size(); i++) System.arraycopy(out.get(i), 0, mesh, i * 8, 8);
		return mesh;
	}

	private static void rib(java.util.List<float[]> out, java.util.Random random, float span, float z, float weight) {
		float leanPhase = random.nextFloat() * 6.28F;
		float swayPhase = random.nextFloat() * 6.28F;
		float lumpPhase = random.nextFloat() * 6.28F;
		float crown = 0.92F + random.nextFloat() * 0.14F;
		float[][] centres = new float[ALONG + 1][];
		float[] widths = new float[ALONG + 1];
		float legs = SPRING_HEIGHT + ROOT_DEPTH;
		float arc = (float) Math.PI * span;
		float total = legs * 2.0F + arc;
		for (int i = 0; i <= ALONG; i++) {
			float t = i / (float) ALONG;
			float along = t * total;
			float x;
			float y;
			if (along < legs) {
				x = -span;
				y = along - ROOT_DEPTH;
			} else if (along > total - legs) {
				x = span;
				y = total - along - ROOT_DEPTH;
			} else {
				float angle = (float) Math.PI * (1.0F - (along - legs) / arc);
				x = (float) Math.cos(angle) * span;
				y = SPRING_HEIGHT + (float) Math.sin(angle) * span * crown;
			}
			// The rib wanders off its plane and in and out of its arc, slowly, as grown things do.
			float wander = (float) Math.sin(t * 6.3F + leanPhase) * 0.14F + (float) Math.sin(t * 15.0F + swayPhase) * 0.04F;
			float inward = (float) Math.sin(t * 9.0F + swayPhase) * 0.07F;
			float crownWard = 1.0F - Math.abs(t - 0.5F) * 2.0F;
			float scale = x == 0.0F ? 1.0F : 1.0F - inward / Math.max(0.5F, Math.abs(x));
			centres[i] = new float[] { x * scale, y, z + wander };
			float root = (float) Math.pow(Math.clamp(1.0F - y / 1.6F, 0.0F, 1.0F), 2.0F) * 0.17F;
			// Under the floor the root narrows away to nothing, hanging into the dark.
			float hanging = y < 0.0F ? Math.max(0.05F, 1.0F + y / ROOT_DEPTH) : 1.0F;
			float lumps = 1.0F + 0.13F * (float) Math.sin(t * 27.0F + lumpPhase) + 0.07F * (float) Math.sin(t * 61.0F + lumpPhase);
			widths[i] = (0.2F - 0.09F * crownWard + root) * lumps * weight * hanging;
		}
		for (int i = 0; i < ALONG; i++) {
			float[][] ringA = ring(centres, widths, i);
			float[][] ringB = ring(centres, widths, i + 1);
			for (int a = 0; a < AROUND; a++) {
				int next = (a + 1) % AROUND;
				float u0 = a / (float) AROUND;
				float u1 = (a + 1) / (float) AROUND;
				float v0 = i * 0.5F;
				float v1 = (i + 1) * 0.5F;
				out.add(vertex(ringA[a], u0, v0));
				out.add(vertex(ringA[next], u1, v0));
				out.add(vertex(ringB[next], u1, v1));
				out.add(vertex(ringB[a], u0, v1));
			}
		}
	}

	/** One cross-section: position and normal of each point around the rib. */
	private static float[][] ring(float[][] centres, float[] widths, int index) {
		float[] c = centres[index];
		float[] prev = centres[Math.max(0, index - 1)];
		float[] next = centres[Math.min(ALONG, index + 1)];
		float tx = next[0] - prev[0];
		float ty = next[1] - prev[1];
		float tz = next[2] - prev[2];
		float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
		tx /= tl;
		ty /= tl;
		tz /= tl;
		// One side of the section stays pointing along the corridor, so the tube does not twist.
		float sx = ty;
		float sy = -tx;
		float sz = 0.0F;
		float sl = (float) Math.sqrt(sx * sx + sy * sy);
		sx /= sl;
		sy /= sl;
		float bx = sy * tz - sz * ty;
		float by = sz * tx - sx * tz;
		float bz = sx * ty - sy * tx;
		float[][] ring = new float[AROUND][];
		for (int a = 0; a < AROUND; a++) {
			float angle = a * 6.2831855F / AROUND;
			float cos = (float) Math.cos(angle);
			float sin = (float) Math.sin(angle);
			float nx = sx * cos + bx * sin;
			float ny = sy * cos + by * sin;
			float nz = sz * cos + bz * sin;
			float w = widths[index];
			ring[a] = new float[] { c[0] + nx * w, c[1] + ny * w, c[2] + nz * w, nx, ny, nz };
		}
		return ring;
	}

	private static float[] vertex(float[] point, float u, float v) {
		return new float[] { point[0], point[1], point[2], u, v, point[3], point[4], point[5] };
	}
}
