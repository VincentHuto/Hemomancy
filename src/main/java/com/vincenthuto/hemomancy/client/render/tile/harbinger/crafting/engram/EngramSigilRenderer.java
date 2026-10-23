package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomEffectRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.*;

/**
 * Draws a loom's engram on the ground around it (shaders/core/world/engram_sigil): the skin with its empty star, the
 * scars, valves and prisms sunk in it, the lanes of ichor the weaver has threaded, and over them each fragment's
 * pool, each lane's advancing head and the staff's mark. Everything is laid on the real ground by
 * {@link EngramSigilMesh}. The skin spreads out from the loom as the weave begins, and at the climax the lanes are
 * drawn up off the ground into the loom while the skin withdraws after them.
 */
public final class EngramSigilRenderer {
	/** Ticks the skin takes to spread from the loom to past the tips. */
	public static final float UNFURL_TICKS = 50F;
	private static final float UNDER_CAP_LIFT = 0.02F, LANE_LIFT = 0.032F, OVER_CAP_LIFT = 0.046F;
	private static final float LANE_HALF = 0.2F;
	/** Lanes are drawn in samples this long, so a ring arc bends and a lane follows the ground. */
	private static final double SAMPLE = 0.22D;
	/** Where the spokes enter the loom, as a share of the tip radius (the shader's rim). */
	private static final double RIM = 0.24D;
	private static final int CAP_FRAGMENT = 0, CAP_ACTIVE = 1, CAP_INTRUSION = 2, CAP_HUSK = 3, CAP_SCAR = 4,
			CAP_VALVE = 5, CAP_PRISM = 6, CAP_AIM = 7, CAP_AIM_REFUSED = 8, CAP_HEAD = 9, CAP_HOME = 10, CAP_GREY = 11;
	/** The eight tendencies' ichor, in wheel order; the shader's own palette. */
	private static final float[][] ICHOR = {
			{0.86F, 0.06F, 0.08F}, {1.00F, 0.40F, 0.06F}, {1.00F, 0.86F, 0.16F}, {1.00F, 0.97F, 0.86F},
			{0.22F, 0.62F, 0.16F}, {0.18F, 0.52F, 1.00F}, {0.58F, 0.60F, 0.64F}, {0.52F, 0.12F, 0.86F}};
	private static final float[] GREY = {0.72F, 0.70F, 0.76F};

	private EngramSigilRenderer() {
	}

	public static float[] ichor(int tendency) {
		return tendency < 0 || tendency >= ICHOR.length ? GREY : ICHOR[tendency];
	}

	/** How far the skin has spread (0 to 1) this frame. */
	public static float unfurl(SomaticLoomBlockEntity te, float partialTick) {
		float ticks = te.getWeaveTicks() + (te.isPaused() ? 0F : partialTick);
		float t = Mth.clamp(ticks / UNFURL_TICKS, 0F, 1F);
		return t * t * (3F - 2F * t);
	}

	/** The climax's progress (0 to 1), or 0 outside it. */
	public static float climax(SomaticLoomBlockEntity te, float partialTick) {
		return te.isInClimax() ? Mth.clamp((te.getClimaxTicks() + partialTick) / SomaticWeaveRules.CLIMAX_TICKS, 0F, 1F) : 0F;
	}

	/**
	 * Draws relative to the loom's block, in world axes: `stack` must not be turned to the loom's facing. `dissolve` is
	 * how far a broken weave's engram has come apart (see {@link EngramDissolve}), or below 0 for a live one.
	 */
	public static void render(SomaticLoomBlockEntity te, float partialTick, PoseStack stack, MultiBufferSource buffers,
			float dissolve) {
		boolean dissolving = dissolve >= 0F;
		EngramDissolve.Snapshot snapshot = dissolving ? EngramDissolve.snapshot(te.getBlockPos()) : null;
		EngramBoard board = dissolving ? snapshot == null ? null : snapshot.board() : te.getEngram();
		Level level = te.getLevel();
		if (board == null || level == null || !dissolving && !te.isWeaving()) return;
		EngramSigilMesh mesh = EngramSigilMesh.of(te);
		float now = AnimationTime.gameTime(level, partialTick);
		boolean climaxing = !dissolving && te.isInClimax();
		float climax = dissolving ? 0F : climax(te, partialTick);
		float withdraw = Mth.clamp((climax - 0.55F) / 0.45F, 0F, 1F);
		float beat = SomaticLoomEffectRules.heartbeat(now, 1F);
		float light = worldLight(level, te, partialTick);
		float seed = Math.floorMod(te.getBlockPos().asLong() * 0x9E3779B97F4A7C15L, 1000L) * 0.1F;
		double radius = te.sigilRadius();
		EngramClientState.Aim aim = EngramClientState.aim();
		boolean aimed = !dissolving && aim != null && aim.loom().equals(te.getBlockPos()) && !climaxing;
		Matrix4f matrix = stack.last().pose();

		Params params = new Params(now, seed, (float) radius, dissolving ? snapshot.unfurl() : unfurl(te, partialTick), withdraw, beat,
				board.pressure() / (float) EngramWeaveRules.PRESSURE_LIMIT, board.keyed(), board.rotation(), te.sigilTurns(),
				!dissolving && te.isPaused() ? 1F : 0F, light, aimed ? (float) aim.sx() : 0F, aimed ? (float) aim.sz() : 0F,
				aimed ? 1F : 0F, dissolving ? dissolve : 0F);

		mesh.emitSkin(buffers.getBuffer(params.type(0)), matrix);
		if (withdraw >= 0.98F) return;
		// Nothing lies beyond the skin: caps and lanes appear as it reaches them and go as it withdraws.
		double reach = params.unfurl() * (1F - withdraw) * (1.14D * radius + 0.7D) - 0.4D;

		VertexConsumer under = buffers.getBuffer(params.type(2));
		for (int node = 0; node < VESSEL; node++) {
			EngramBoard.NodeKind kind = board.kind(node);
			if (kind == EngramBoard.NodeKind.OPEN || !within(node, radius, reach)) continue;
			int cap = kind == EngramBoard.NodeKind.SCAR ? CAP_SCAR : kind == EngramBoard.NodeKind.MEMBRANE ? CAP_VALVE : CAP_PRISM;
			cap(under, matrix, mesh, te, node, kind == EngramBoard.NodeKind.SCAR ? 0.6F : 0.55F, ichor(board.tint(node)), cap,
					UNDER_CAP_LIFT);
		}

		VertexConsumer lanes = buffers.getBuffer(params.type(1));
		for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
			int[] lane = board.lane(fragment);
			if (lane.length < 2 || !within(lane[0], radius, reach) && climax <= 0) continue;
			boolean home = climaxing || EngramWeaveRules.connected(board, fragment);
			lane(lanes, matrix, mesh, te, board, fragment, lane, home, climax);
		}

		VertexConsumer over = buffers.getBuffer(params.type(3));
		int active = dissolving ? -1 : te.engramActive();
		for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
			EngramBoard.Fragment f = board.fragment(fragment);
			if (!within(f.node(), radius, reach)) continue;
			boolean home = climaxing || EngramWeaveRules.connected(board, fragment);
			int colour = EngramWeaveRules.colour(board, fragment);
			int cap;
			if (f.intrusion()) cap = f.premarked() ? CAP_HUSK : CAP_INTRUSION;
			else if (home) cap = CAP_HOME;
			else if (fragment == active) cap = CAP_ACTIVE;
			else if (colour < 0) cap = CAP_GREY;
			else cap = CAP_FRAGMENT;
			if (fragment == active && f.intrusion() && !f.premarked()) cap = CAP_ACTIVE;
			float size = 0.7F * (1F - withdraw);
			cap(over, matrix, mesh, te, f.node(), size, cap == CAP_HUSK ? GREY : ichor(colour < 0 ? -1 : colour), cap, OVER_CAP_LIFT);
			int[] lane = board.lane(fragment);
			if (lane.length >= 2 && !EngramWeaveRules.landed(board, fragment) && lane[lane.length - 1] != VESSEL)
				cap(over, matrix, mesh, te, lane[lane.length - 1], 0.36F, ichor(colour < 0 ? -1 : colour), CAP_HEAD, OVER_CAP_LIFT);
		}
		if (aimed && aim.node() >= 0 && aim.node() < VESSEL) {
			boolean taken = aim.step() == EngramWeaveRules.Step.OK || aim.step() == EngramWeaveRules.Step.RETRACTED
					|| aim.step() == EngramWeaveRules.Step.IDLE;
			cap(over, matrix, mesh, te, aim.node(), 0.78F, GREY, taken ? CAP_AIM : CAP_AIM_REFUSED, OVER_CAP_LIFT + 0.004F);
		}
	}

	private static boolean within(int node, double radius, double reach) {
		double[] unit = unit(node);
		return Math.hypot(unit[0], unit[1]) * radius <= reach;
	}

	/** One engram draw's shared uniforms; each layer gets its own render type. */
	private record Params(float now, float seed, float radius, float unfurl, float withdraw, float beat, float pressure,
			boolean keyed, int rotation, int turns, float dim, float light, float aimX, float aimY, float aimZ, float dissolve) {
		net.minecraft.client.renderer.RenderType type(int layer) {
			return HemoRenderTypes.engramSigil(new HemoRenderTypes.EngramSigilParams(layer, now, seed, radius, unfurl, withdraw,
					beat, pressure, keyed, rotation, turns, dim, light, aimX, aimY, aimZ, dissolve));
		}
	}

	/** A node's cap lying on the ground: a square across `size` blocks either way, its kind carried in alpha. */
	private static void cap(VertexConsumer vc, Matrix4f matrix, EngramSigilMesh mesh, SomaticLoomBlockEntity te, int node,
			float size, float[] tint, int kind, float lift) {
		double radius = te.sigilRadius();
		double[] unit = unit(node);
		double sx = unit[0] * radius, sz = unit[1] * radius;
		int alpha = kind * 16 + 8;
		capVertex(vc, matrix, mesh, te, sx - size, sz - size, -1, -1, tint, alpha, lift);
		capVertex(vc, matrix, mesh, te, sx - size, sz + size, -1, 1, tint, alpha, lift);
		capVertex(vc, matrix, mesh, te, sx + size, sz + size, 1, 1, tint, alpha, lift);
		capVertex(vc, matrix, mesh, te, sx + size, sz - size, 1, -1, tint, alpha, lift);
	}

	private static void capVertex(VertexConsumer vc, Matrix4f matrix, EngramSigilMesh mesh, SomaticLoomBlockEntity te,
			double sx, double sz, float u, float v, float[] tint, int alpha, float lift) {
		Vec3 at = mesh.ground(sx, sz, lift).subtract(Vec3.atLowerCornerOf(te.getBlockPos()));
		vc.addVertex(matrix, (float) at.x, (float) at.y, (float) at.z).setUv(u, v)
				.setColor(Math.round(tint[0] * 255), Math.round(tint[1] * 255), Math.round(tint[2] * 255), alpha);
	}

	/**
	 * A lane of ichor along its nodes: straight along the star's lines and spokes, bending along the rim between two
	 * tips. A grey lane runs grey until its prism dyes it. At the climax it is drawn up off the ground into the loom.
	 */
	private static void lane(VertexConsumer vc, Matrix4f matrix, EngramSigilMesh mesh, SomaticLoomBlockEntity te,
			EngramBoard board, int fragment, int[] lane, boolean home, float climax) {
		double radius = te.sigilRadius();
		List<double[]> points = new ArrayList<>();
		EngramBoard.Fragment f = board.fragment(fragment);
		int colour = f.grey() ? -1 : f.colour();
		for (int k = 0; k + 1 < lane.length; k++) {
			int a = lane[k], b = lane[k + 1];
			if (f.grey() && board.kind(a) == EngramBoard.NodeKind.PRISM && board.tint(a) == f.trueColour()) colour = f.trueColour();
			sampleEdge(points, a, b, radius, k == 0, colour);
		}
		double along = 0;
		Vec3 loom = Vec3.atLowerCornerOf(te.getBlockPos());
		Vec3 lifted = te.sigilCentre().add(0, 1.4D, 0);
		float drawUp = climax * climax;
		for (int i = 0; i + 1 < points.size(); i++) {
			double[] p0 = points.get(i), p1 = points.get(i + 1);
			double dx = p1[0] - p0[0], dz = p1[1] - p0[1];
			double length = Math.sqrt(dx * dx + dz * dz);
			if (length < 1.0E-5) continue;
			double nx = -dz / length * LANE_HALF, nz = dx / length * LANE_HALF;
			float[] c = ichor((int) p1[2]);
			int alpha = home ? 255 : 160;
			Vec3 a0 = up(mesh.ground(p0[0] + nx, p0[1] + nz, LANE_LIFT), lifted, drawUp), a1 = up(mesh.ground(p0[0] - nx, p0[1] - nz, LANE_LIFT), lifted, drawUp);
			Vec3 b1 = up(mesh.ground(p1[0] - nx, p1[1] - nz, LANE_LIFT), lifted, drawUp), b0 = up(mesh.ground(p1[0] + nx, p1[1] + nz, LANE_LIFT), lifted, drawUp);
			laneVertex(vc, matrix, a0.subtract(loom), along, 1, c, alpha);
			laneVertex(vc, matrix, a1.subtract(loom), along, -1, c, alpha);
			laneVertex(vc, matrix, b1.subtract(loom), along + length, -1, c, alpha);
			laneVertex(vc, matrix, b0.subtract(loom), along + length, 1, c, alpha);
			along += length;
		}
	}

	/** At the climax a lane rises off the ground and is drawn in toward the loom's heart. */
	private static Vec3 up(Vec3 at, Vec3 heart, float drawUp) {
		return drawUp <= 0 ? at : at.lerp(heart, drawUp * 0.55D).add(0, drawUp * 0.9D, 0);
	}

	private static void sampleEdge(List<double[]> out, int a, int b, double radius, boolean first, int colour) {
		double[] ua = unit(a), ub = unit(b);
		boolean rim = isTip(a) && isTip(b);
		if (b == VESSEL) ub = new double[]{ua[0] / INNER * RIM, ua[1] / INNER * RIM};
		if (a == VESSEL) ua = new double[]{ub[0] / INNER * RIM, ub[1] / INNER * RIM};
		double length = rim ? Math.PI / 4 * radius : Math.hypot(ub[0] - ua[0], ub[1] - ua[1]) * radius;
		int steps = Math.max(1, (int) Math.ceil(length / SAMPLE));
		double startAngle = Math.atan2(ua[0], -ua[1]), endAngle = Math.atan2(ub[0], -ub[1]);
		double turn = Math.IEEEremainder(endAngle - startAngle, Math.PI * 2);
		for (int s = first ? 0 : 1; s <= steps; s++) {
			double t = s / (double) steps;
			double x, z;
			if (rim) {
				double angle = startAngle + turn * t;
				x = Math.sin(angle) * radius;
				z = -Math.cos(angle) * radius;
			} else {
				x = Mth.lerp(t, ua[0], ub[0]) * radius;
				z = Mth.lerp(t, ua[1], ub[1]) * radius;
			}
			out.add(new double[]{x, z, colour});
		}
	}

	private static final double INNER = 0.41421D;

	private static void laneVertex(VertexConsumer vc, Matrix4f matrix, Vec3 at, double along, float across, float[] c, int alpha) {
		vc.addVertex(matrix, (float) at.x, (float) at.y, (float) at.z).setUv((float) along, across)
				.setColor(Math.round(c[0] * 255), Math.round(c[1] * 255), Math.round(c[2] * 255), alpha);
	}

	/** 0 in darkness to 1 in full light at the loom: the brighter of block light and the day's sky light. */
	private static float worldLight(Level level, SomaticLoomBlockEntity te, float partialTick) {
		int packed = LevelRenderer.getLightColor(level, te.getBlockPos().above());
		float sky = LightTexture.sky(packed) / 15.0F;
		if (level instanceof ClientLevel client) sky *= client.getSkyDarken(partialTick);
		return Math.max(LightTexture.block(packed) / 15.0F, sky);
	}

	public static void clear() {
		EngramSigilMesh.clear();
	}
}
