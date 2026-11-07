package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Saint Putriciel's purifying fire in the world (visual pass 2), on the Flammeus {@code FLAME} material the pyres burn
 * (crimson body, pink-white heat, black soot tips; the shader owns the palette, so no fire texel is ever green or
 * yellow): bent tongues that lick rather than stand, rows of them along a front or a lane, rings of them, a column
 * over the body, soot rising off them, ember sparks thrown on arcs and the glow of a hot bed on the floor. Points are
 * camera-relative.
 */
final class PutricielFlameGeometry {
	/** Shapes the flame shader decodes: a tongue, smoke, a glow. */
	static final int TONGUE = 0, SMOKE = 1, GLOW = 2;
	static final int WHITE = 0xFFFFFF;

	private PutricielFlameGeometry() {
	}

	/** One tongue rooted at {@code root}, {@code height} tall, leaning by {@code lean}; it bends and licks as it burns. */
	static void tongue(PoseStack p, VertexConsumer v, Vec3 root, Vec3 lean, double height, double width, double time, int seed,
			float alpha) {
		if (height < 0.04D || alpha <= 0.003F) return;
		// a full body of flame and a hotter core licking up inside it on its own sway and lean, so a tongue reads as
		// fire with body rather than a spike
		ThermalGeometry.flameRibbon(p, v, ThermalGeometry.tongue(root, lean, height, time, seed), Vec3.ZERO, width * 1.75D, seed, alpha * 0.85F);
		double side = PutricielBloomRules.unit(seed, 1700) - 0.5D;
		Vec3 off = new Vec3(side * width * 0.8D, 0.0D, -side * width * 0.5D);
		ThermalGeometry.flameRibbon(p, v, ThermalGeometry.tongue(root.add(off), lean.scale(0.6D).add(off), height * 0.78D, time * 1.3D,
				seed + 101), Vec3.ZERO, width * 0.95D, seed + 101, alpha);
	}

	/**
	 * A row of tongues from {@code a} to {@code b} about every {@code spacing} blocks, each {@code height} tall times
	 * {@code profile(f)} at its place {@code f} (0 at a, 1 at b) and its own flicker; under it the bed glows.
	 */
	static void row(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, double spacing, double height, Profile profile, Vec3 lean,
			Vec3 right, Vec3 up, double time, int seed, float alpha) {
		double length = a.distanceTo(b);
		int count = Math.max(1, (int) Math.round(length / Math.max(0.1D, spacing)));
		for (int i = 0; i <= count; i++) {
			double f = count == 0 ? 0.0D : i / (double) count;
			double scale = profile.at(f);
			if (scale <= 0.02D) continue;
			double jitter = (PutricielBloomRules.unit(seed, 1200 + i) - 0.5D) * spacing * 0.6D;
			Vec3 along = b.subtract(a);
			Vec3 side = new Vec3(-along.z, 0.0D, along.x);
			side = side.lengthSqr() < 1.0E-6D ? Vec3.ZERO : side.normalize();
			Vec3 root = a.lerp(b, f).add(side.scale(jitter));
			double h = height * scale * PutricielBloomRules.flicker(time, seed, i) * (0.75D + 0.4D * PutricielBloomRules.unit(seed, 1220 + i));
			tongue(p, v, root, lean, h, Math.max(0.07D, h * 0.2D), time, seed + i * 7, alpha);
			if (i % 2 == 0) {
				LuxUmbraGeometry.ground(p, v, root.add(0.0D, 0.02D, 0.0D), Math.max(0.25D, h * 0.45D), GLOW, seed + i, WHITE,
						alpha * 0.4F * (float) Math.min(1.0D, scale));
			}
			if (i % 4 == 1 && h > 0.5D) smoke(p, v, root.add(0.0D, h * 1.3D, 0.0D), right, up, h * 0.35D, time, seed + i, alpha * 0.5F);
		}
	}

	/** How tall the tongues stand along a row, 0 to 1 at its place 0 to 1. */
	@FunctionalInterface
	interface Profile {
		double at(double f);

		Profile FLAT = f -> 1.0D;
	}

	/** A ring of tongues round {@code centre} at {@code radius}, about every {@code spacing} blocks, leaning outward. */
	static void ring(PoseStack p, VertexConsumer v, Vec3 centre, double radius, double spacing, double height, double outward,
			Vec3 right, Vec3 up, double time, int seed, float alpha) {
		if (radius <= 0.05D || alpha <= 0.003F) return;
		int count = (int) Math.max(8, Math.min(90, radius * Math.PI * 2.0D / Math.max(0.15D, spacing)));
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2.0D / count + seed * 0.37D;
			Vec3 out = new Vec3(Math.cos(a), 0.0D, Math.sin(a));
			Vec3 root = centre.add(out.scale(radius));
			double h = height * PutricielBloomRules.flicker(time, seed, i) * (0.8D + 0.35D * PutricielBloomRules.unit(seed, 1300 + i));
			tongue(p, v, root, out.scale(outward), h, Math.max(0.07D, h * 0.22D), time, seed + i * 5, alpha);
			if (i % 3 == 0) {
				LuxUmbraGeometry.ground(p, v, root.add(0.0D, 0.02D, 0.0D), Math.max(0.3D, h * 0.5D), GLOW, seed + i, WHITE, alpha * 0.35F);
			}
			if (i % 6 == 2 && h > 0.45D) smoke(p, v, root.add(0.0D, h * 1.35D, 0.0D), right, up, h * 0.4D, time, seed + i, alpha * 0.45F);
		}
	}

	/**
	 * A column of fire over {@code base}: a ring of tall tongues leaning in round a taller heart, its glow on the
	 * floor, soot rising off its top.
	 */
	static void column(PoseStack p, VertexConsumer v, Vec3 base, double radius, double height, Vec3 right, Vec3 up, double time,
			int seed, float alpha) {
		if (height < 0.05D || alpha <= 0.003F) return;
		tongue(p, v, base, Vec3.ZERO, height * 1.1D, Math.max(0.12D, height * 0.16D), time, seed, alpha);
		int count = 7;
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2.0D / count + seed;
			Vec3 out = new Vec3(Math.cos(a), 0.0D, Math.sin(a));
			double h = height * (0.65D + 0.3D * PutricielBloomRules.unit(seed, 1400 + i)) * PutricielBloomRules.flicker(time, seed, i);
			tongue(p, v, base.add(out.scale(radius)), out.scale(-0.12D), h, Math.max(0.1D, h * 0.18D), time, seed + 10 + i, alpha * 0.92F);
		}
		LuxUmbraGeometry.ground(p, v, base.add(0.0D, 0.03D, 0.0D), radius * 2.4D + 0.4D, GLOW, seed + 3, WHITE, alpha * 0.55F);
		smoke(p, v, base.add(0.0D, height * 1.45D, 0.0D), right, up, radius + height * 0.25D, time, seed + 4, alpha * 0.55F);
	}

	/** Soot lifting off a fire: a smoke card that rises and widens on its own slow loop. */
	static void smoke(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double size, double time, int seed, float alpha) {
		if (alpha <= 0.003F || size <= 0.02D) return;
		double phase = (time / 26.0D + PutricielBloomRules.unit(seed, 1500)) % 1.0D;
		double s = size * (0.8D + 0.7D * phase);
		LuxUmbraGeometry.card(p, v, at.add(0.0D, phase * size * 1.6D, 0.0D), right, up, s, s * 1.2D, SMOKE, seed, WHITE,
				alpha * (float) Math.sin(phase * Math.PI));
	}

	/**
	 * Ember sparks thrown from {@code at} along {@code direction} (or all round with a zero direction), {@code age}
	 * ticks after: hot points on falling arcs, each with a short streak behind it.
	 */
	static void sparks(PoseStack p, VertexConsumer v, Vec3 at, Vec3 direction, Vec3 right, Vec3 up, float age, double reach, int count,
			int seed, float alpha) {
		if (age < 0.0F || age > 14.0F || alpha <= 0.003F) return;
		Vec3 flat = new Vec3(direction.x, 0.0D, direction.z);
		boolean round = flat.lengthSqr() < 1.0E-6D;
		flat = round ? new Vec3(1, 0, 0) : flat.normalize();
		Vec3 side = new Vec3(-flat.z, 0.0D, flat.x);
		float fade = alpha * (1.0F - age / 14.0F);
		for (int i = 0; i < count; i++) {
			double u = PutricielBloomRules.unit(seed, 1600 + i);
			Vec3 dir = round ? new Vec3(Math.cos(u * Math.PI * 2.0D), 0.0D, Math.sin(u * Math.PI * 2.0D))
					: flat.add(side.scale((u - 0.5D) * 1.4D)).normalize();
			double speed = reach * (0.05D + 0.06D * PutricielBloomRules.unit(seed, 1620 + i));
			double rise = 0.14D + 0.14D * PutricielBloomRules.unit(seed, 1640 + i);
			Vec3 head = flight(at, dir, speed, rise, age), tail = flight(at, dir, speed, rise, Math.max(0.0F, age - 1.2F));
			LuxUmbraGeometry.ribbonPath(p, v, List.of(tail, head), Vec3.ZERO, 0.012D, 0.03D, GLOW, seed + i, WHITE, fade);
			LuxUmbraGeometry.card(p, v, head, right, up, 0.045D, 0.045D, GLOW, seed + 20 + i, WHITE, fade);
		}
	}

	/** The glow of a hot bed on the floor at {@code at}, {@code radius} across. */
	static void bed(PoseStack p, VertexConsumer v, Vec3 at, double radius, int seed, float alpha) {
		LuxUmbraGeometry.ground(p, v, at.add(0.0D, 0.025D, 0.0D), radius, GLOW, seed, WHITE, alpha);
	}

	/** A flash of heat at {@code at}: a pink-white glow swelling and gone in a few ticks. */
	static void flash(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, float age, double size, int seed, float alpha) {
		if (age < 0.0F || age > 6.0F) return;
		float f = HemoEasing.pulse(age, 1.0F, 6.0F);
		LuxUmbraGeometry.card(p, v, at, right, up, size * (0.6D + 0.6D * f), size * (0.6D + 0.6D * f), GLOW, seed, WHITE, alpha * f);
	}

	/**
	 * A tongue burning off a point on the body (revision 2026-11-06, R1.7): {@code length} along {@code direction} (a
	 * unit vector at most 70 degrees from up, already leaned by the anchor's motion), {@code width} its half-width,
	 * {@code heat} 0 to 1 how hot its core burns. The outer flame is the full crimson body; the core licks up inside it
	 * on its own sway, narrower and shorter as the heat drops, so a guttering tongue goes to its soot tip, and at full
	 * heat (the locked beat, the flare) a pink-white glow sits in its root.
	 */
	static void anchored(PoseStack p, VertexConsumer v, Vec3 root, Vec3 direction, double length, double width, float heat, Vec3 right,
			Vec3 up, double time, int seed, float alpha) {
		if (length < 0.03D || alpha <= 0.003F) return;
		double rise = Math.max(0.3D, direction.y);
		// the spine climbs rise x length and leans the rest, so the tip lies along the direction at the tongue's length
		Vec3 lean = new Vec3(direction.x / rise, 0.0D, direction.z / rise);
		double height = length * rise;
		ThermalGeometry.flameRibbon(p, v, ThermalGeometry.tongue(root, lean, height, time, seed), Vec3.ZERO, width * 1.6D, seed, alpha * 0.85F);
		float hot = HemoEasing.clamp01(heat);
		double side = PutricielBloomRules.unit(seed, 1700) - 0.5D;
		Vec3 off = new Vec3(side * width * 0.7D, 0.0D, -side * width * 0.45D);
		ThermalGeometry.flameRibbon(p, v, ThermalGeometry.tongue(root.add(off), lean.scale(0.7D).add(off), height * (0.55D + 0.3D * hot),
				time * 1.3D, seed + 101), Vec3.ZERO, width * (0.55D + 0.45D * hot), seed + 101, alpha * (0.45F + 0.55F * hot));
		if (hot > 0.9F) glow(p, v, root.add(direction.scale(length * 0.12D)), right, up, width * 1.6D, seed + 7, alpha * (hot - 0.9F) * 6.0F);
	}

	/**
	 * A tear-off: a tongue ripped off a fast-moving anchor {@code age} ticks ago, carried on at most of the anchor's
	 * {@code velocity} (blocks a tick), rising and shrinking to nothing over {@link #TEAR_LIFE} ticks.
	 */
	static void tearOff(PoseStack p, VertexConsumer v, Vec3 from, Vec3 velocity, float age, double size, Vec3 right, Vec3 up, double time,
			int seed, float alpha) {
		if (age < 0.0F || age >= TEAR_LIFE || alpha <= 0.003F) return;
		float life = age / TEAR_LIFE;
		Vec3 at = from.add(velocity.scale(age * 0.7D)).add(0.0D, 0.07D * age, 0.0D);
		Vec3 back = velocity.lengthSqr() < 1.0E-6D ? new Vec3(0, 1, 0) : velocity.scale(-1.0D).normalize().add(0.0D, 1.2D, 0.0D).normalize();
		anchored(p, v, at, back, size * (1.0D - life), Math.max(0.03D, size * 0.22D * (1.0D - life * 0.6D)), 0.6F * (1.0F - life), right, up,
				time, seed, alpha * (1.0F - life * life));
	}

	/** Ticks a tear-off lasts. */
	static final float TEAR_LIFE = 5.0F;

	/** A camera-facing glow: the coals a flame rises from, an ember in a crack, the light under the hood. */
	static void glow(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double size, int seed, float alpha) {
		if (size <= 0.01D || alpha <= 0.003F) return;
		LuxUmbraGeometry.card(p, v, at, right, up, size, size, GLOW, seed, WHITE, Math.min(1.0F, alpha));
	}

	private static Vec3 flight(Vec3 at, Vec3 dir, double speed, double rise, float age) {
		return at.add(dir.scale(speed * age)).add(0.0D, rise * age - 0.025D * age * age, 0.0D);
	}
}
