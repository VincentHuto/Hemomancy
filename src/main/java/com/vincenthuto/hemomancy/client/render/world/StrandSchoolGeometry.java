package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The school shapes the second wave of strands needs (core-loop round two P5), drawn from the mod's own toolkit so each
 * strand reads as its school at the Riposte bar: Ductilis arcs that re-jag every other tick, Lux split into prism rays,
 * Congeatio's cruor shards riding a jet, and Ferric's blood-iron flechettes and ground spikes (iron in the solid pass,
 * through {@link ForgedNeedleGeometry} and {@link FerricDuctilisGeometry}). Every position is relative to the pose.
 */
final class StrandSchoolGeometry {
	/** Prism light: white, gold, crimson and a pale rose, cycled across the rays. Never silver. */
	private static final int[] PRISM = {WHITE, GOLD, CRIMSON, 0xFFB8C8};
	private static final double GOLDEN = 2.39996;

	private StrandSchoolGeometry() {
	}

	/** A small seeded random in [-1, 1] for {@code i} at {@code flick}: the same on every frame of one flick. */
	private static double jitter(int seed, long flick, int i) {
		long h = seed * 0x9E3779B97F4A7C15L ^ flick * 0xC2B2AE3D27D4EB4FL ^ i * 0x165667B19E3779F9L;
		h ^= h >>> 29;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 32;
		return ((h & 0xFFFF) / 32767.5D) - 1.0D;
	}

	/**
	 * A Ductilis arc from {@code from} to {@code to}: a jagged path whose kinks jump to new places every other tick, a
	 * nerve-gold glow round a white-hot core, and a forked spur off its middle.
	 */
	static void arc(PoseStack p, VertexConsumer v, Vec3 from, Vec3 to, Vec3 eye, double width, int seed, double time,
			float alpha) {
		Vec3 span = to.subtract(from);
		double length = span.length();
		if (length < 0.05D || alpha <= 0.003F) return;
		Vec3 axis = span.scale(1.0D / length);
		Vec3 side = VisceralGeometry.side(axis), lift = axis.cross(side).normalize();
		long flick = (long) Math.floor(time * 0.5D);
		int kinks = Math.max(4, Math.min(12, (int) Math.round(length * 3.0D)));
		double amplitude = Math.min(0.35D, 0.08D + length * 0.05D);
		List<Vec3> path = new ArrayList<>(kinks + 1);
		for (int i = 0; i <= kinks; i++) {
			double t = i / (double) kinks;
			double taper = Math.sin(Math.PI * t);
			path.add(from.add(span.scale(t)).add(side.scale(jitter(seed, flick, i) * amplitude * taper))
					.add(lift.scale(jitter(seed + 7, flick, i) * amplitude * taper)));
		}
		ribbonPath(p, v, path, eye, width * 2.6D, WISP, seed, FerricDuctilisGeometry.NERVE, alpha * 0.45F);
		ribbonPath(p, v, path, eye, width, RIBBON, seed + 1, FerricDuctilisGeometry.NERVE, alpha);
		ribbonPath(p, v, path, eye, width * 0.35D, RIBBON, seed + 2, WHITE, alpha);
		// A fork off the arc's middle, re-thrown with each flick.
		Vec3 root = path.get(kinks / 2);
		Vec3 fork = root.add(axis.scale(length * 0.2D)).add(side.scale(jitter(seed + 3, flick, 0) * length * 0.25D))
				.add(lift.scale(jitter(seed + 5, flick, 1) * length * 0.2D));
		ribbonPath(p, v, List.of(root, root.lerp(fork, 0.5D).add(lift.scale(amplitude * 0.5D)), fork), eye, width * 0.55D,
				RIBBON, seed + 4, FerricDuctilisGeometry.NERVE, alpha * 0.85F);
	}

	/** A Ductilis spray: arcs thrown across the cone from the palm, each re-jagging, each ending in a spark. */
	static void arcSpray(PoseStack p, VertexConsumer v, Vec3 look, Vec3 across, Vec3 lift, double spread, double reach,
			Vec3 eye, Vec3 right, Vec3 up, int seed, double time, float alpha) {
		long flick = (long) Math.floor(time * 0.5D);
		for (int i = 0; i < 7; i++) {
			double angle = i * GOLDEN + jitter(seed, flick, i) * 0.6D;
			double r = spread * Math.sqrt((i + 0.5D) / 7.0D);
			Vec3 dir = look.add(across.scale(Math.cos(angle) * r)).add(lift.scale(Math.sin(angle) * r)).normalize();
			Vec3 end = dir.scale(reach * (0.7D + 0.3D * (0.5D + 0.5D * jitter(seed + 11, flick, i))));
			arc(p, v, dir.scale(0.15D), end, eye, 0.055D, seed + i * 5, time, alpha);
			card(p, v, end, right, up, 0.18D, 0.18D, FOCUS, seed + 40 + i, FerricDuctilisGeometry.NERVE, alpha * 0.8F);
			card(p, v, end, right, up, 0.07D, 0.07D, FOCUS, seed + 50 + i, WHITE, alpha);
		}
		card(p, v, look.scale(0.25D), right, up, 0.22D, 0.22D, FOCUS, seed + 60, FerricDuctilisGeometry.NERVE, alpha * 0.9F);
	}

	/** A Lux spray: light split at the palm into prism rays fanned across the cone, each ending in a soft flare. */
	static void prismSpray(PoseStack p, VertexConsumer v, Vec3 look, Vec3 across, Vec3 lift, double spread, double reach,
			Vec3 eye, Vec3 right, Vec3 up, int seed, double time, float alpha) {
		for (int i = 0; i < 8; i++) {
			double angle = i * GOLDEN + Math.sin(time * 0.05D + i) * 0.25D;
			double r = spread * Math.sqrt((i + 0.5D) / 8.0D);
			Vec3 dir = look.add(across.scale(Math.cos(angle) * r)).add(lift.scale(Math.sin(angle) * r)).normalize();
			double length = reach * (0.8D + 0.2D * Math.sin(time * 0.6D + i * 1.7D));
			Vec3 end = dir.scale(length);
			int color = PRISM[i % PRISM.length];
			ribbon(p, v, dir.scale(0.12D), end, eye, 0.11D, WHITE_BEAM, seed + i, color, alpha, 0, 1);
			ribbon(p, v, dir.scale(0.12D), end, eye, 0.034D, RIBBON, seed + 10 + i, WHITE, alpha, 0, 1);
			ribbon(p, v, dir.scale(0.12D), end, eye, 0.34D, WISP, seed + 20 + i, color, alpha * 0.4F, 0, 1);
			card(p, v, end, right, up, 0.26D, 0.26D, FOCUS, seed + 30 + i, color, alpha * 0.7F);
		}
		// The prism itself: a hot white knot at the palm the light splits from.
		card(p, v, look.scale(0.2D), right, up, 0.3D, 0.3D, FOCUS, seed + 60, WHITE, alpha);
		card(p, v, look.scale(0.2D), right, up, 0.55D, 0.55D, WISP, seed + 61, GOLD, alpha * 0.5F);
	}

	/**
	 * Cruor shards riding a jet from the palm to {@code end}: glass flakes spinning down the line, and a cruor spearhead
	 * driven into the contact.
	 */
	static void cruorShards(PoseStack p, VertexConsumer glass, Vec3 end, double pressure, int seed, double time, float alpha) {
		double length = end.length();
		if (length < 0.2D) return;
		Vec3 axis = end.scale(1.0D / length);
		int count = Math.max(3, Math.min(10, (int) Math.round(length * 1.2D)));
		for (int k = 0; k < count; k++) {
			double run = (time * 0.09D + k / (double) count) % 1.0D;
			Vec3 at = end.scale(run);
			p.pushPose();
			p.translate(at.x, at.y, at.z);
			p.mulPose(Axis.YP.rotation((float) (seed + k * 1.7D + time * 0.3D)));
			p.mulPose(Axis.ZP.rotation((float) (k * 0.8D + time * 0.21D)));
			ThermalGeometry.shard(p, glass, 0.06D + 0.05D * pressure, seed + k, alpha * (float) (0.6D + 0.4D * Math.sin(Math.PI * run)));
			p.popPose();
		}
		// The spearhead: a long shard laid along the jet, set into what it strikes.
		p.pushPose();
		p.translate(end.x - axis.x * 0.2D, end.y - axis.y * 0.2D, end.z - axis.z * 0.2D);
		float yaw = (float) Math.atan2(axis.x, axis.z);
		float pitch = (float) Math.asin(Math.max(-1.0D, Math.min(1.0D, axis.y)));
		p.mulPose(Axis.YP.rotation(yaw));
		p.mulPose(Axis.XP.rotation(-pitch + (float) Math.PI / 2));
		ThermalGeometry.shard(p, glass, 0.18D + 0.12D * pressure, seed + 40, alpha);
		p.popPose();
	}

	/** A width profile scaled by {@code by}: the pale edge under a dark lash, a thinner seam inside it. */
	static double[] scaled(double[] widths, double by) {
		double[] out = new double[widths.length];
		for (int i = 0; i < widths.length; i++) out[i] = widths[i] * by;
		return out;
	}

	// ── Iron, in the solid pass ──

	/** Blood-iron flechettes streaming down a Ferric jet from the palm to {@code end}, one lodged at the contact. */
	static void ironJet(PoseStack p, VertexConsumer iron, Vec3 end, double pressure, boolean lance, int seed, double time,
			float alpha) {
		double length = end.length();
		if (length < 0.3D) return;
		Vec3 axis = end.scale(1.0D / length);
		int count = Math.max(3, Math.min(9, (int) Math.round(length * 0.9D)));
		double size = lance ? 0.62D : 0.45D;
		for (int k = 0; k < count; k++) {
			double run = (time * 0.16D + k / (double) count) % 1.0D;
			Vec3 base = end.scale(run * 0.92D);
			ForgedNeedleGeometry.flechette(p, iron, base, axis, size, 0.045D + 0.02D * pressure, lance, seed + k * 3,
					alpha * (float) Math.min(1.0D, run * 6.0D));
		}
		ForgedNeedleGeometry.flechette(p, iron, end.subtract(axis.scale(size * 0.7D)), axis, size * 1.15D, 0.06D + 0.03D * pressure,
				true, seed + 50, alpha);
	}

	/** A fan of blood-iron flechettes thrown out across the spray's cone, running outward from the palm. */
	static void ironFan(PoseStack p, VertexConsumer iron, Vec3 look, Vec3 across, Vec3 lift, double spread, double reach,
			int seed, double time, float alpha) {
		for (int i = 0; i < 16; i++) {
			double angle = i * GOLDEN;
			double r = spread * Math.sqrt((i + 0.5D) / 16.0D);
			Vec3 dir = look.add(across.scale(Math.cos(angle) * r)).add(lift.scale(Math.sin(angle) * r)).normalize();
			double run = (time * 0.11D + i * 0.137D) % 1.0D;
			ForgedNeedleGeometry.flechette(p, iron, dir.scale(0.2D + reach * 0.85D * run), dir, 0.6D, 0.055D, false, seed + i * 3,
					alpha * (float) Math.min(1.0D, (1.0D - run) * 4.0D));
		}
	}

	/** Ferric's eruption: iron spikes driven up round the point, leaning out, as tall as the burst has grown. */
	static void ironSpikes(PoseStack p, VertexConsumer iron, double radius, int seed, float age, float grow, float fade) {
		int spikes = spikeCount(radius);
		for (int i = 0; i < spikes; i++) {
			double a = i * Math.PI * 2 / spikes + seed;
			double at = radius * (0.25D + 0.65D * ((i * 0.618D) % 1.0D));
			double height = (0.55D + 0.8D * ((i * 0.37D) % 1.0D)) * Math.min(1.0D, radius) * grow
					* (1.0D - 0.4D * Math.max(0, (age - 14) / 10.0D));
			if (height < 0.02D) continue;
			p.pushPose();
			p.translate(Math.cos(a) * at, -0.05D, Math.sin(a) * at);
			p.mulPose(Axis.YP.rotation((float) -a));
			p.mulPose(Axis.ZP.rotation((float) (-0.18D - 0.12D * ((i * 0.53D) % 1.0D))));
			FerricDuctilisGeometry.spike(p, iron, Math.max(0.35D, height * 2.4D), 0.11D + 0.07D * Math.min(1.0D, radius), seed + i, fade);
			p.popPose();
		}
	}

	/** Spikes in an eruption of {@code radius}: about three in a stain run's small cell, twelve in a full ring. */
	static int spikeCount(double radius) {
		return Math.max(3, Math.min(12, (int) Math.round(radius * 3.5D)));
	}
}
