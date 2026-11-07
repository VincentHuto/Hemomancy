package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Putriciel's rot in the air, with body (visual pass 2), in their spore material
 * ({@link PutricielRenderTypes#SPORE}): billowing puffs lit from above, bursts of them rolling out, a spore curtain
 * standing round a wave's front, the exhale of a breath, the wake a swung censer leaves (a pale shell of spores round a
 * dark rot-smoke core with glints shed off it), thick ooze lashing along a reach or thrown up in a splash, and spore
 * glints. Never a strand: ooze is a rounded body at least a tenth of a block across, and clouds are puffs. Points are
 * camera-relative.
 */
final class PutricielSporeGeometry {
	/** Shapes the spore shader decodes. */
	static final int PUFF = 0, OOZE = 1, MOTE = 2, CURTAIN = 3;
	/** An ash flake shed off the burning body or a burnt crust (revision 2026-11-06): grey only, no hue. */
	static final int ASH = 4;

	private PutricielSporeGeometry() {
	}

	/** The spore state as the shader reads it: how far it has thinned, how dark it is, how much it glows. */
	static int state(float thin, float dark, float glow) {
		return PutricielGroundGeometry.color(thin, dark, glow);
	}

	/** One camera-facing card of {@code shape}. */
	static void card(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double width, double height, int shape, int seed,
			int state, float alpha) {
		if (alpha <= 0.003F || width <= 0.0D || height <= 0.0D) return;
		Vec3 x = right.scale(width), y = up.scale(height);
		Vec3 a = at.subtract(x).subtract(y), b = at.add(x).subtract(y), c = at.add(x).add(y), d = at.subtract(x).add(y);
		MaterialQuads.quad(v, p.last().pose(), a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z, d.x, d.y, d.z, shape,
				MaterialQuads.seedTile(seed), state, alpha, 0.0D, 1.0D, false);
	}

	/** A billow of spores {@code size} across at {@code at}: thinned {@code thin}, dark {@code dark}, glowing {@code glow}. */
	static void puff(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double size, float thin, float dark, float glow,
			float alpha, int seed) {
		if (thin >= 0.995F) return;
		card(p, v, at, right, up, size, size * 0.86D, PUFF, seed, state(thin, dark, glow), alpha);
	}

	/**
	 * An ash flake {@code size} across: charcoal at {@code dark} 1, ash grey at 0, curling as it {@code thin}s. The
	 * spore shader draws it in greys only, so it never carries the rot's hue or the fire's.
	 */
	static void ash(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double size, float dark, float thin, float alpha, int seed) {
		if (thin >= 0.995F) return;
		card(p, v, at, right, up, size, size * (0.6D + 0.3D * PutricielBloomRules.unit(seed, 3)), ASH, seed, state(thin, dark, 0.0F), alpha);
	}

	/** A spore glint. */
	static void mote(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double size, float glow, float alpha, int seed) {
		card(p, v, at, right, up, size, size, MOTE, seed, state(0.0F, 0.0F, glow), alpha);
	}

	/**
	 * A burst of spores at {@code at}, {@code age} ticks after it: puffs rolling out and up and thinning, a few
	 * glints thrown higher, and a darker heart at the start. {@code reach} scales it.
	 */
	static void burst(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, float age, double reach, int count, float dark,
			float alpha, int seed) {
		if (age < 0.0F || age >= PutricielBloomRules.PUFF_LIFE || alpha <= 0.0F) return;
		if (age < 4.0F) {
			puff(p, v, at.add(0.0D, reach * 0.2D, 0.0D), right, up, reach * (0.35D + age * 0.1D), age / 4.0F, 0.7F, 0.2F,
					alpha * (1.0F - age / 4.0F), seed + 99);
		}
		for (int i = 0; i < count; i++) {
			double[] puff = PutricielBloomRules.puff(seed, i, age, reach);
			if (puff == null) continue;
			puff(p, v, at.add(puff[0], puff[1], puff[2]), right, up, puff[3], (float) puff[4], dark * (i % 3 == 0 ? 1.0F : 0.4F),
					0.35F, alpha, seed + i);
		}
		for (int i = 0; i < 3; i++) {
			double t = age / PutricielBloomRules.PUFF_LIFE;
			double a = PutricielBloomRules.unit(seed, 800 + i) * Math.PI * 2.0D;
			Vec3 glint = at.add(Math.cos(a) * reach * 0.7D * t, reach * (0.4D + 0.9D * t), Math.sin(a) * reach * 0.7D * t);
			mote(p, v, glint, right, up, 0.05D, 0.8F, alpha * (float) (1.0D - t), seed + 40 + i);
		}
	}

	/**
	 * A standing curtain of spores round {@code centre} at {@code radius}, {@code height} tall, its crest billowing and
	 * puffs rolling at its foot. {@code thin} frays it.
	 */
	static void curtain(PoseStack p, VertexConsumer v, Vec3 centre, double radius, double height, Vec3 camera, Vec3 right, Vec3 up,
			double time, float thin, float alpha, int seed) {
		if (radius <= 0.2D || alpha <= 0.003F) return;
		int segments = (int) Math.max(14, Math.min(64, radius * 5.0D));
		int st = state(thin, 0.15F, 0.45F);
		for (int i = 0; i < segments; i++) {
			double a0 = i * Math.PI * 2.0D / segments, a1 = (i + 1) * Math.PI * 2.0D / segments;
			double h0 = height * (0.85D + 0.15D * Math.sin(a0 * 4.0D + time * 0.21D + seed));
			double h1 = height * (0.85D + 0.15D * Math.sin(a1 * 4.0D + time * 0.21D + seed));
			Vec3 b0 = centre.add(Math.cos(a0) * radius, 0.0D, Math.sin(a0) * radius);
			Vec3 b1 = centre.add(Math.cos(a1) * radius, 0.0D, Math.sin(a1) * radius);
			MaterialQuads.quad(v, p.last().pose(), b0.x, b0.y, b0.z, b1.x, b1.y, b1.z, b1.x, b1.y + h1, b1.z, b0.x, b0.y + h0, b0.z,
					CURTAIN, MaterialQuads.seedTile(seed + i), st, alpha, 0.0D, 1.0D, false);
			if (i % 2 == 0) {
				Vec3 out = new Vec3(Math.cos(a0), 0.0D, Math.sin(a0));
				double roll = (time * 0.05D + i * 0.37D) % 1.0D;
				puff(p, v, b0.add(out.scale(0.25D * roll)).add(0.0D, 0.3D + 0.2D * roll, 0.0D), right, up, 0.45D + 0.2D * roll,
						Math.max(thin, (float) roll * 0.6F), 0.25F, 0.3F, alpha * 0.8F, seed + 200 + i);
				puff(p, v, b0.add(0.0D, h0 * 0.95D, 0.0D), right, up, 0.35D, Math.max(thin, 0.2F), 0.0F, 0.5F, alpha * 0.6F,
						seed + 300 + i);
			}
		}
	}

	/**
	 * The wake behind a swung censer along {@code path} (oldest point first): a pale shell of spores round a dark core
	 * of rot smoke, puffs shed off its length and settling, glints off the newest part. {@code fray} spreads it, lifts
	 * it and thins it as it goes.
	 */
	static void wake(PoseStack p, VertexConsumer v, List<Vec3> path, Vec3 camera, Vec3 right, Vec3 up, double time, double width,
			float fray, float alpha, int seed) {
		int n = path.size();
		if (n < 2 || alpha <= 0.003F) return;
		for (int i = 0; i < n; i++) {
			double f = i / (double) (n - 1);
			Vec3 at = path.get(i).add(0.0D, fray * 0.35D * (1.0D - f * 0.5D), 0.0D);
			double w = width * (0.45D + 0.75D * f) * (1.0D + fray * 0.7D);
			float thin = Math.min(0.95F, (float) (1.0D - f) * 0.55F + fray * 0.6F);
			puff(p, v, at, right, up, w * 1.5D, thin, 0.1F, 0.4F, alpha * 0.8F, seed + i * 3);
			if (i % 2 == 0) puff(p, v, at, right, up, w * 0.8D, thin, 0.85F, 0.1F, alpha, seed + i * 3 + 1);
		}
		// the ooze the censer slings: a short glossy smear just behind it
		if (fray < 0.5F) {
			List<Vec3> smear = path.subList(Math.max(0, n - 5), n);
			double[] widths = new double[smear.size()];
			for (int i = 0; i < widths.length; i++) widths[i] = width * 0.35D * (0.3D + 0.7D * i / Math.max(1, widths.length - 1));
			LuxUmbraGeometry.ribbonPath(p, v, smear, camera, widths, OOZE, seed + 7, state(fray, 0.3F, 0.2F), alpha * (1.0F - fray * 2.0F));
		}
		for (int k = 0; k < 4; k++) {
			double phase = (time / 10.0D + PutricielBloomRules.unit(seed, 900 + k)) % 1.0D;
			Vec3 from = path.get(Math.min(n - 1, (int) ((0.4D + 0.6D * PutricielBloomRules.unit(seed, 910 + k)) * (n - 1))));
			Vec3 glint = from.add(0.0D, -0.6D * phase * phase, 0.0D);
			mote(p, v, glint, right, up, 0.045D, 0.7F, alpha * (float) (1.0D - phase), seed + 60 + k);
		}
	}

	/**
	 * A reach of rot ooze along {@code spine} (from the palm to its running head): a pale shell of spores round a
	 * rounded, glossy body tapering to the head, a dark core, a cream scum line along the top, a knot at the head and
	 * drops falling off the high part.
	 */
	static void reach(PoseStack p, VertexConsumer v, List<Vec3> spine, Vec3 camera, Vec3 right, Vec3 up, double time, double width,
			float alpha, int seed) {
		int n = spine.size();
		if (n < 2 || alpha <= 0.003F) return;
		double[] body = new double[n], core = new double[n], scum = new double[n];
		for (int i = 0; i < n; i++) {
			double f = i / (double) (n - 1);
			// full from the palm, swelling in travelling boluses, pinching to the head
			double bolus = 0.85D + 0.25D * Math.sin(f * 9.0D - time * 0.6D + seed);
			body[i] = width * (1.0D - 0.55D * f) * bolus * Math.min(1.0D, (1.0D - f) * 5.0D + 0.35D);
			core[i] = body[i] * 0.55D;
			scum[i] = body[i] * 0.22D;
		}
		for (int i = 1; i < n; i += 2) {
			double w = body[i] * 2.6D;
			puff(p, v, spine.get(i), right, up, w, 0.35F, 0.2F, 0.4F, alpha * 0.45F, seed + 50 + i);
		}
		LuxUmbraGeometry.ribbonPath(p, v, spine, camera, body, OOZE, seed, state(0.0F, 0.2F, 0.3F), alpha);
		LuxUmbraGeometry.ribbonPath(p, v, spine, camera, core, OOZE, seed + 1, state(0.0F, 1.0F, 0.0F), alpha * 0.85F);
		List<Vec3> top = new ArrayList<>(n);
		for (int i = 0; i < n; i++) top.add(spine.get(i).add(0.0D, body[i] * 0.5D, 0.0D));
		LuxUmbraGeometry.ribbonPath(p, v, top, camera, scum, OOZE, seed + 2, state(0.0F, 0.0F, 1.0F), alpha * 0.7F);
		Vec3 head = spine.get(n - 1);
		puff(p, v, head, right, up, width * 1.4D, 0.2F, 0.9F, 0.2F, alpha, seed + 3);
		mote(p, v, head, right, up, width * 0.8D, 0.9F, alpha * 0.8F, seed + 4);
		for (int k = 0; k < 3; k++) {
			double at = 0.2D + 0.55D * PutricielBloomRules.unit(seed, 950 + k);
			double phase = (time / 9.0D + PutricielBloomRules.unit(seed, 960 + k)) % 1.0D;
			Vec3 from = spine.get((int) Math.round(at * (n - 1)));
			double fall = phase * phase * 1.1D;
			Vec3 drop = from.add(0.0D, -fall, 0.0D);
			LuxUmbraGeometry.ribbonPath(p, v, List.of(drop.add(0.0D, Math.min(0.3D, fall * 0.5D + 0.06D), 0.0D), drop), camera,
					width * 0.25D, width * 0.4D, OOZE, seed + 20 + k, state(0.0F, 0.4F, 0.2F), alpha * (float) (1.0D - phase));
		}
	}

	/**
	 * Rot ooze thrown up at {@code at}, {@code age} ticks after the impact: thick gouts that leap and fall back in
	 * arcs, glints off their heads, and a ring of puffs rolling out round them.
	 */
	static void splash(PoseStack p, VertexConsumer v, Vec3 at, Vec3 camera, Vec3 right, Vec3 up, float age, double height,
			double spread, float alpha, int seed) {
		if (age < 0.0F || age > 14.0F || alpha <= 0.003F) return;
		double t = age / 14.0D;
		float fade = alpha * (float) (1.0D - t * t);
		for (int i = 0; i < 7; i++) {
			double a = i * 2.39996D + seed;
			Vec3 out = new Vec3(Math.cos(a), 0.0D, Math.sin(a));
			double h = height * (0.6D + 0.5D * PutricielBloomRules.unit(seed, 1000 + i));
			double reach = spread * (0.5D + 0.5D * PutricielBloomRules.unit(seed, 1010 + i));
			double head = Math.min(1.0D, t * 1.4D), tail = Math.max(0.0D, head - 0.22D);
			Vec3 top = at.add(out.scale(reach * head)).add(0.0D, PutricielBloomRules.arc(head, h), 0.0D);
			Vec3 mid = at.add(out.scale(reach * (head + tail) * 0.5D)).add(0.0D, PutricielBloomRules.arc((head + tail) * 0.5D, h), 0.0D);
			Vec3 back = at.add(out.scale(reach * tail)).add(0.0D, PutricielBloomRules.arc(tail, h), 0.0D);
			LuxUmbraGeometry.ribbonPath(p, v, List.of(back, mid, top), camera, 0.04D, 0.09D, OOZE, seed + i, state(0.0F, 0.3F, 0.2F), fade);
			mote(p, v, top, right, up, 0.06D, 0.6F, fade * 0.7F, seed + 20 + i);
		}
		double ring = spread * HemoEasing.easeOutCubic((float) Math.min(1.0D, t * 1.8D));
		for (int i = 0; i < 8; i++) {
			double a = i * Math.PI / 4.0D + seed * 0.3D;
			puff(p, v, at.add(Math.cos(a) * ring, 0.2D + 0.15D * t, Math.sin(a) * ring), right, up, 0.3D + 0.35D * t, (float) t,
					0.3F, 0.3F, alpha * 0.8F, seed + 40 + i);
		}
	}

	/**
	 * A cloud of spores drifting through a cone of {@code radius} and {@code degrees} from {@code origin} along
	 * {@code forward} ({x, z}), puffs at seeded spots in it, low to the floor, turning slowly. {@code thin} frays it.
	 */
	static void coneCloud(PoseStack p, VertexConsumer v, Vec3 origin, double[] forward, double radius, double degrees, Vec3 right,
			Vec3 up, double time, int count, float thin, float alpha, int seed) {
		if (alpha <= 0.003F) return;
		for (int i = 0; i < count; i++) {
			double u = PutricielBloomRules.unit(seed, 1100 + i), w = PutricielBloomRules.unit(seed, 1120 + i);
			double bearing = Math.toRadians((u - 0.5D) * degrees * 0.9D + Math.sin(time * 0.02D + i) * 4.0D);
			double distance = radius * (0.2D + 0.75D * Math.sqrt(w));
			double fx = forward[0] * Math.cos(bearing) - forward[1] * Math.sin(bearing);
			double fz = forward[1] * Math.cos(bearing) + forward[0] * Math.sin(bearing);
			double rise = 0.35D + 1.1D * PutricielBloomRules.unit(seed, 1140 + i) + 0.1D * Math.sin(time * 0.05D + i * 1.3D);
			Vec3 at = origin.add(fx * distance, rise, fz * distance);
			double size = 0.55D + 0.6D * distance / radius;
			puff(p, v, at, right, up, size, Math.min(0.95F, thin + (float) (0.15D * PutricielBloomRules.unit(seed, 1160 + i))),
					i % 4 == 0 ? 0.7F : 0.15F, 0.35F, alpha, seed + i);
		}
	}
}
