package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendon.TendonAnchor;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Every Tendon Line rooted in this dimension (the anchors are synced to the owner and the players tracking them): a
 * cord of living blood from the owner's drawn palm to the face it is rooted in. Slack, it sags and throbs slowly; while
 * the owner hauls on it with held Absorption it pulls straight and its pulse quickens. The cord is a body of three
 * layers (a soft sheath, the blood ribbon and a pale seam) with one sinew wound along its whole length, and a clot
 * where it is rooted. The cell hand pours no ordinary stream while the line claims it, so the cord is the only thing
 * between the hand and the face.
 */
public final class TendonLineRenderer {
	private static final int SEGMENTS = 16;
	/** The cord's width at the hand and at its root. */
	private static final double HAND_WIDTH = 0.10D;
	private static final double ROOT_WIDTH = 0.07D;
	private static final double SLACK_SAG = 0.22D;
	/** Slack and taut pulse speeds, and how far the pulse swells the cord. */
	private static final double SLACK_PULSE = 0.32D;
	private static final double TAUT_PULSE = 0.9D;
	private static final double PULSE_SWELL = 0.035D;
	/** The wound sinew's turns along the whole cord and its distance off the cord's middle. */
	private static final double SINEW_TURNS = 7.0D;
	private static final double SINEW_RADIUS = 0.045D;
	/** Lines further than this from the camera are not drawn. */
	private static final double DRAW_DISTANCE = 96.0D;
	private static final int TINT = 0xFFFFFF, SEAM = 0xFFD2C4, CLOT = 0x7A1020, SINEW = 0x5A0A14;

	private TendonLineRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		double time = AnimationTime.gameTime(mc.level, (double) partialTick);
		boolean begun = false;
		for (Player player : mc.level.players()) {
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			if (!anchor.isSetIn(mc.level.dimension())) continue;
			if (player.position().distanceToSqr(cam) > DRAW_DISTANCE * DRAW_DISTANCE) continue;
			if (!begun) {
				OutpouringBrushes.begin(time);
				begun = true;
			}
			drawCord(poseStack, mc, player, anchor, cam, time, partialTick);
		}
		if (begun) OutpouringBrushes.flush(mc.renderBuffers().bufferSource());
	}

	private static void drawCord(PoseStack poseStack, Minecraft mc, Player player, TendonAnchor anchor, Vec3 cam,
			double time, float partialTick) {
		Vec3 start = CellHandStreams.palm(player, player.getMainArm() == HumanoidArm.LEFT, partialTick);
		Vec3 end = anchor.target();
		int seed = player.getId() * 31 + anchor.pos().hashCode();
		boolean taut = VerbChannel.isChannelling(player, Verb.ABSORB);
		double sagDepth = taut ? 0.0D : SLACK_SAG;
		double pulseSpeed = taut ? TAUT_PULSE : SLACK_PULSE;

		List<Vec3> path = new ArrayList<>(SEGMENTS + 1);
		for (int i = 0; i <= SEGMENTS; i++) {
			float t = (float) i / SEGMENTS;
			double sag = -Math.sin(t * Math.PI) * sagDepth;
			// The throb dies out at both ends, so the cord stays on the palm and on its root.
			double pulse = Math.sin(time * pulseSpeed + t * 8.0D) * PULSE_SWELL * Math.sin(t * Math.PI);
			path.add(start.lerp(end, t).add(0.0D, sag + pulse, 0.0D).subtract(cam));
		}
		var flow = OutpouringBrushes.flow(OutpouringBrushes.Mat.ANIMUS);
		OutpouringBrushes.ribbonPath(poseStack, flow, path, HAND_WIDTH * 1.9D, ROOT_WIDTH * 1.9D, OutpouringBrushes.WISP,
				seed + 1, TINT, 0.45F);
		OutpouringBrushes.ribbonPath(poseStack, flow, path, HAND_WIDTH, ROOT_WIDTH, OutpouringBrushes.RIBBON, seed, TINT,
				1.0F);
		OutpouringBrushes.ribbonPath(poseStack, flow, path, HAND_WIDTH * 0.28D, ROOT_WIDTH * 0.28D,
				OutpouringBrushes.RIBBON, seed + 2, SEAM, taut ? 0.9F : 0.6F);
		OutpouringBrushes.ribbonPath(poseStack, flow, sinew(path, time, pulseSpeed), 0.024D, 0.018D,
				OutpouringBrushes.RIBBON, seed + 3, SINEW, 0.95F);

		// A clot where the line is rooted, so a cord seen along its length still shows where it holds.
		var camera = mc.gameRenderer.getMainCamera();
		Vec3 right = new Vec3(camera.getLeftVector()).scale(-1.0D);
		Vec3 up = new Vec3(camera.getUpVector());
		Vec3 root = end.subtract(cam);
		OutpouringBrushes.card(poseStack, flow, root, right, up, 0.26D, 0.26D, OutpouringBrushes.FOCUS, seed + 4, CLOT, 0.95F);
		OutpouringBrushes.card(poseStack, flow, root, right, up, 0.12D, 0.12D, OutpouringBrushes.FOCUS, seed + 5, SEAM,
				taut ? 0.85F : 0.5F);
	}

	/** One sinew wound round the whole cord, turning as the blood runs along it. */
	private static List<Vec3> sinew(List<Vec3> path, double time, double pulseSpeed) {
		int steps = SEGMENTS * 3;
		List<Vec3> wound = new ArrayList<>(steps + 1);
		Vec3 axis = path.getLast().subtract(path.getFirst());
		if (axis.lengthSqr() < 1.0E-8D) return path;
		axis = axis.normalize();
		Vec3 side = Math.abs(axis.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : axis.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
		Vec3 normal = side.cross(axis).normalize();
		for (int i = 0; i <= steps; i++) {
			double f = (double) i / steps * SEGMENTS;
			int a = Math.min(SEGMENTS - 1, (int) f);
			Vec3 point = path.get(a).lerp(path.get(a + 1), f - a);
			double t = (double) i / steps;
			double angle = t * SINEW_TURNS * Math.PI * 2.0D + time * pulseSpeed * 0.6D;
			double r = SINEW_RADIUS * Math.min(1.0D, Math.sin(t * Math.PI) * 4.0D);
			wound.add(point.add(side.scale(Math.cos(angle) * r)).add(normal.scale(Math.sin(angle) * r)));
		}
		return wound;
	}
}
