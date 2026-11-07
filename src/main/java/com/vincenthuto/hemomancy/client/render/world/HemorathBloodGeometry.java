package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Hemorath's blood with body (visual pass 2), in the Animus material the cast outpouring and Hematic Riposte
 * draw with: streams that swell with travelling boluses inside a soft shell, a glossy dark body with a lit rim along
 * its top and two sinews twisting through it, drops shedding off their arc; drains of beading drops that leave a body
 * and run to him; crowns of spurts where blood breaks out of a well or the floor; and the standing wall of blood the
 * Marrow Rupture rolls out at its jump height. White base tint with the two accents CORE and CLOT; the body's darker
 * tint is the material's own colour stepped down. Points are relative to the pose; {@code camera} is the eye in the
 * same space.
 */
final class HemorathBloodGeometry {
	static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
	/** The material stepped down to a glossy dark body. */
	static final int BODY = 0xB04A52;
	private static final Vec3 UP = new Vec3(0, 1, 0);
	private static final int SAMPLES = 18;

	private HemorathBloodGeometry() {
	}

	/**
	 * A stream of blood from {@code from} to {@code to}, arching {@code lift} high, reached {@code reach} of the way,
	 * {@code width} its half-width at rest. Boluses travel it toward {@code to} at {@code speed} blocks a tick.
	 */
	static void stream(PoseStack p, VertexConsumer v, Vec3 from, Vec3 to, Vec3 camera, Vec3 right, Vec3 up, double time,
			int seed, double reach, double lift, double width, double speed, float alpha) {
		if (alpha <= 0.0F || reach <= 0.0D || from.distanceToSqr(to) < 1e-4) return;
		double length = from.distanceTo(to);
		List<Vec3> spine = new ArrayList<>(SAMPLES + 1);
		double[] body = new double[SAMPLES + 1], shell = new double[SAMPLES + 1], sinew = new double[SAMPLES + 1];
		double[] swell = new double[SAMPLES + 1];
		for (int i = 0; i <= SAMPLES; i++) {
			double t = reach * i / SAMPLES;
			spine.add(from.lerp(to, t).add(0.0D, Math.sin(t * Math.PI) * lift, 0.0D));
			double b = HemorathFissureRules.bolus(t * length, time, speed, 1.6D);
			swell[i] = b;
			// thin where it leaves the well, full along its run, narrowing to a lip at its running head
			double ends = Math.min(1.0D, 0.45D + t * 3.0D) * Math.min(1.0D, 0.35D + (reach - t) * length / 0.6D);
			body[i] = width * (0.62D + 0.55D * b) * ends;
			shell[i] = body[i] * 2.4D;
			sinew[i] = body[i] * 0.28D;
		}
		Vec3 axis = to.subtract(from).normalize();
		Vec3 side = axis.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		Vec3 bend = axis.cross(side).normalize();
		if (bend.y < 0.0D) bend = bend.scale(-1.0D);

		// 1. shell: a soft sheath and wisps trailing back along the flow
		ribbonPath(p, v, spine, camera, shell, RIBBON, seed, TINT, alpha * 0.32F);
		for (int i = 2; i < spine.size(); i += 3) {
			double flicker = 0.85D + 0.25D * Math.sin(time * 0.9D + i * 1.7D + seed);
			double w = shell[i] * 1.1D * flicker;
			card(p, v, spine.get(i).subtract(axis.scale(0.15D)), right, up, w, w * 1.2D, WISP, seed + i, TINT, alpha * 0.3F);
		}
		// 2. body: glossy dark and solid (the slash sprite, as Riposte's thick base), its shine line lit along the top
		ribbonPath(p, v, spine, camera, body, SLASH, seed + 1, BODY, alpha);
		double[] deep = new double[body.length];
		for (int i = 0; i < body.length; i++) deep[i] = body[i] * 0.6D;
		ribbonPath(p, v, spine, camera, deep, SLASH, seed + 7, CLOT, alpha * 0.85F);
		List<Vec3> rim = new ArrayList<>(spine.size());
		double[] rimWidth = new double[spine.size()];
		for (int i = 0; i < spine.size(); i++) {
			rim.add(spine.get(i).add(bend.scale(body[i] * 0.55D)));
			rimWidth[i] = body[i] * 0.22D;
		}
		ribbonPath(p, v, rim, camera, rimWidth, RIBBON, seed + 2, CORE, alpha * 0.55F);
		// 3. sinew: two strands twisting through the body, one clot-dark
		for (int strand = 0; strand < 2; strand++) {
			List<Vec3> points = new ArrayList<>(spine.size());
			for (int i = 0; i < spine.size(); i++) {
				double a = i * 0.9D - time * 0.25D + strand * Math.PI;
				double r = body[i] * 0.55D;
				points.add(spine.get(i).add(side.scale(Math.cos(a) * r)).add(bend.scale(Math.sin(a) * r)));
			}
			ribbonPath(p, v, points, camera, sinew, RIBBON, seed + 3 + strand, strand == 0 ? TINT : CLOT, alpha * 0.8F);
		}
		// 4. knots: the boluses' hearts, the source, and the running head
		for (int i = 1; i < spine.size() - 1; i++) {
			if (swell[i] > 0.8D && swell[i] >= swell[i - 1] && swell[i] >= swell[i + 1]) {
				card(p, v, spine.get(i), right, up, body[i] * 1.15D, body[i] * 1.15D, FOCUS, seed + 10 + i, TINT, alpha * 0.4F);
			}
		}
		card(p, v, from, right, up, width * 1.6D, width * 1.4D, FOCUS, seed + 5, CORE, alpha * 0.4F);
		Vec3 head = spine.get(spine.size() - 1);
		if (reach < 0.999D) card(p, v, head, right, up, width * 1.9D, width * 1.9D, FOCUS, seed + 6, TINT, alpha);
		// 5. drops shedding off the high part of the arc
		for (int k = 0; k < 4; k++) {
			double at = 0.25D + 0.5D * HemorathFissureRules.unit(seed, 3000 + k);
			if (at > reach) continue;
			double phase = (time / 12.0D + HemorathFissureRules.unit(seed, 3010 + k)) % 1.0D;
			Vec3 top = from.lerp(to, at).add(0.0D, Math.sin(at * Math.PI) * lift - width, 0.0D);
			double fall = phase * phase * (top.y - Math.min(from.y, to.y) + 0.3D);
			Vec3 drop = top.add(0.0D, -fall, 0.0D);
			ribbonPath(p, v, List.of(drop.add(0.0D, Math.min(0.35D, fall * 0.5D + 0.05D), 0.0D), drop), camera, width * 0.35D, width * 0.15D,
					RIBBON, seed + 20 + k, TINT, alpha * (float) (0.8D * (1.0D - phase)));
			card(p, v, drop, right, up, width * 0.45D, width * 0.55D, FOCUS, seed + 24 + k, TINT, alpha * (float) (1.0D - phase));
		}
	}

	/**
	 * A drain of blood out of a body at {@code from} toward {@code to}: beading drops that leave the wound, gather
	 * speed and run to him along a sagging path, each with its own tapering trail, a thread joining them and a pulse at
	 * the wound. {@code strength} 0 to 1.
	 */
	static void drain(PoseStack p, VertexConsumer v, Vec3 from, Vec3 to, Vec3 camera, Vec3 right, Vec3 up, double time,
			int seed, float strength) {
		if (strength <= 0.01F || from.distanceToSqr(to) < 0.25D) return;
		double length = from.distanceTo(to);
		Vec3 middle = from.lerp(to, 0.5D).add(0.0D, -0.25D - 0.04D * length, 0.0D);
		List<Vec3> thread = new ArrayList<>(13);
		for (int i = 0; i <= 12; i++) thread.add(quadratic(from, middle, to, i / 12.0D));
		ribbonPath(p, v, thread, camera, 0.012D, 0.02D, RIBBON, seed, CLOT, strength * 0.35F);
		int drops = 4 + (int) Math.min(6.0D, length / 2.0D);
		for (int i = 0; i < drops; i++) {
			double phase = (time * 0.035D + i / (double) drops + seed * 0.13D) % 1.0D;
			double t = HemoEasing.easeInCubic(phase);
			double back = Math.max(0.0D, t - 0.05D - 0.05D * t);
			Vec3 head = quadratic(from, middle, to, t), tail = quadratic(from, middle, to, back);
			float fade = (float) Math.min(1.0D, phase * 8.0D) * strength;
			double size = 0.05D + 0.035D * HemorathFissureRules.unit(seed, 3100 + i);
			ribbonPath(p, v, List.of(tail, tail.lerp(head, 0.5D), head), camera, size * 0.2D, size * 0.8D, RIBBON, seed + i, TINT,
					fade * 0.9F);
			card(p, v, head, right, up, size, size * 1.15D, FOCUS, seed + 40 + i, i % 3 == 0 ? CORE : TINT, fade);
		}
		float beat = (float) Math.pow(Math.max(0.0D, Math.sin(time * 0.35D + seed)), 4.0D);
		card(p, v, from, right, up, 0.1D + 0.06D * beat, 0.12D + 0.06D * beat, FOCUS, seed + 90, TINT, strength * (0.5F + 0.4F * beat));
		card(p, v, from, right, up, 0.26D, 0.26D, WISP, seed + 91, TINT, strength * 0.25F);
	}

	/** A crown of blood breaking out at {@code at}: spurts that leap and arc over, and a swirl on the ground under them. */
	static void crown(PoseStack p, VertexConsumer v, Vec3 at, Vec3 camera, Vec3 right, Vec3 up, double time, int seed,
			double height, double spread, float alpha) {
		if (alpha <= 0.01F) return;
		for (int i = 0; i < 7; i++) {
			double phase = (time / 14.0D + i / 7.0D + seed * 0.07D) % 1.0D;
			double a = i * 2.39996D + seed;
			Vec3 out = new Vec3(Math.cos(a), 0.0D, Math.sin(a));
			double h = height * (0.6D + 0.5D * HemorathFissureRules.unit(seed, 3200 + i));
			Vec3 root = at.add(out.scale(spread * 0.25D));
			Vec3 top = root.add(out.scale(spread * phase)).add(0.0D, h * 4.0D * phase * (1.0D - phase), 0.0D);
			Vec3 back = root.add(out.scale(spread * Math.max(0.0D, phase - 0.18D)))
					.add(0.0D, h * 4.0D * Math.max(0.0D, phase - 0.18D) * (1.0D - Math.max(0.0D, phase - 0.18D)), 0.0D);
			float fade = alpha * (float) (1.0D - phase * phase);
			ribbonPath(p, v, List.of(back, back.lerp(top, 0.5D), top), camera, 0.025D, 0.05D, SLASH, seed + i, TINT, fade);
			card(p, v, top, right, up, 0.06D, 0.07D, FOCUS, seed + 10 + i, i % 2 == 0 ? CORE : TINT, fade * 0.9F);
		}
		ground(p, v, at.add(0.0D, 0.03D, 0.0D), spread * 0.9D + 0.3D, POOL, seed, TINT, alpha * 0.6F);
	}

	/** Blood flung from {@code at} along {@code direction}: drops on short arcs with trails, {@code age} ticks in. */
	static void spatter(PoseStack p, VertexConsumer v, Vec3 at, Vec3 direction, Vec3 camera, Vec3 right, Vec3 up, float age,
			int seed, double reach, float alpha) {
		if (age < 0.0F || age > 12.0F || alpha <= 0.0F) return;
		Vec3 flat = new Vec3(direction.x, 0.0D, direction.z);
		flat = flat.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : flat.normalize();
		Vec3 side = flat.cross(UP);
		float fade = alpha * (1.0F - age / 12.0F);
		for (int i = 0; i < 6; i++) {
			double spread = (HemorathFissureRules.unit(seed, 3300 + i) - 0.5D) * 1.2D;
			Vec3 dir = flat.add(side.scale(spread)).normalize();
			double speed = reach * (0.06D + 0.06D * HemorathFissureRules.unit(seed, 3310 + i));
			double up0 = 0.12D + 0.12D * HemorathFissureRules.unit(seed, 3320 + i);
			Vec3 head = flight(at, dir, speed, up0, age), tail = flight(at, dir, speed, up0, Math.max(0.0F, age - 1.5F));
			ribbonPath(p, v, List.of(tail, head), camera, 0.01D, 0.035D, RIBBON, seed + i, TINT, fade);
			card(p, v, head, right, up, 0.04D, 0.045D, FOCUS, seed + 8 + i, TINT, fade);
		}
	}

	/**
	 * The Marrow Rupture's wave: a sheet of blood standing round the origin at {@code radius}, up to {@code height}
	 * (its jump height, so the safe line is drawn), with a pale crest and spray off it.
	 */
	static void wall(PoseStack p, VertexConsumer v, double radius, double floorY, double height, Vec3 camera, Vec3 right,
			Vec3 up, double time, int seed, float alpha) {
		if (radius <= 0.2D || alpha <= 0.0F) return;
		int segments = (int) Math.max(16, Math.min(72, radius * 6.0D));
		List<Vec3> crest = new ArrayList<>(segments + 1);
		for (int i = 0; i < segments; i++) {
			double a0 = i * Math.PI * 2.0D / segments, a1 = (i + 1) * Math.PI * 2.0D / segments;
			double h0 = height * (0.86D + 0.14D * Math.sin(a0 * 5.0D + time * 0.4D + seed));
			double h1 = height * (0.86D + 0.14D * Math.sin(a1 * 5.0D + time * 0.4D + seed));
			Vec3 b0 = new Vec3(Math.cos(a0) * radius, floorY, Math.sin(a0) * radius);
			Vec3 b1 = new Vec3(Math.cos(a1) * radius, floorY, Math.sin(a1) * radius);
			surface(p, v, b0, b1, b1.add(0.0D, h1, 0.0D), b0.add(0.0D, h0, 0.0D), 9, seed + i, BODY, alpha * 0.5F);
			crest.add(b0.add(0.0D, h0, 0.0D));
			if (i % 3 == 0) {
				double phase = (time / 8.0D + i * 0.37D) % 1.0D;
				Vec3 out = new Vec3(Math.cos(a0), 0.0D, Math.sin(a0));
				Vec3 spray = b0.add(out.scale(0.25D * phase)).add(0.0D, h0 + 0.35D * phase * (1.2D - phase), 0.0D);
				card(p, v, spray, right, up, 0.045D, 0.05D, FOCUS, seed + 100 + i, TINT, alpha * (float) (1.0D - phase));
			}
		}
		crest.add(crest.get(0));
		ribbonPath(p, v, crest, camera, 0.03D, RIBBON, seed + 1, CORE, alpha * 0.8F);
		ribbonPath(p, v, crest, camera, 0.09D, WISP, seed + 2, TINT, alpha * 0.25F);
	}

	private static Vec3 flight(Vec3 at, Vec3 dir, double speed, double up, float age) {
		return at.add(dir.scale(speed * age)).add(0.0D, up * age - 0.03D * age * age, 0.0D);
	}

	private static Vec3 quadratic(Vec3 a, Vec3 b, Vec3 c, double t) {
		double u = 1.0D - t;
		return a.scale(u * u).add(b.scale(2.0D * u * t)).add(c.scale(t * t));
	}
}
