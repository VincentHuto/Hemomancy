package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.FOCUS;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.RIBBON;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.RIPPLE;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.SLASH;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.WISP;

/**
 * Putriciel's rot on the body, on the {@code PUTRICIEL_ROT} material: drips off the sacs, motes rising, censer wisps,
 * the drips stretching off the palm in the Offering Grasp, and the rot bursts' accents. These are overlays on the body
 * and stay subtle: thin and partly clear (no strands: ribbons stay under 0.04 half-width and short), and nothing here
 * is red or fire. Every move's world geometry, telegraph and hazard, and every struck body, is
 * {@link PutricielTelegraphPass}'s (visual pass 2).
 */
final class PutricielRotGeometry {
	static final int TINT = 0xFFFFFF;
	/** The rot's dark clot: charcoal olive, never the Riposte's red clot. */
	static final int CLOT = 0x2A2A14;
	static final int CREAM = 0xE8D8A0;

	private PutricielRotGeometry() {
	}

	static void body(PoseStack p, VertexConsumer rot, PutricielOfferingGeometry.Body b) {
		var m = b.motion();
		float growth = m.look().growth();
		if (growth < .05F) return;
		float age = b.age();
		Vec3[] sacs = m.sacs();
		for (int i = 0; i < sacs.length; i++) {
			Vec3 sac = sacs[i];
			if (sac == null) continue;
			double drip = (age * .018 + i * .23) % 1;
			LuxUmbraGeometry.ribbonPath(p, rot, List.of(sac, sac.add(.02, -.2, 0), sac.add(.03, -.45 - drip * .25, 0)),
					b.camera(), .025 * growth, .006, RIBBON, b.seed() + i, TINT, (float) (1 - drip) * .45F * growth);
			for (int n = 0; n < 2; n++) {
				double t = (age * .012 + i * .27 + n / 2.0) % 1;
				double a = i * 2.4 + n * 1.7 + t * 1.4;
				Vec3 mote = sac.add(Math.cos(a) * t * .3, .1 + t * .4, Math.sin(a) * t * .3);
				float opacity = (float) Math.sin(t * Math.PI) * .22F * growth;
				LuxUmbraGeometry.card(p, rot, mote, b.right(), b.up(), .03, .05, SLASH, b.seed() + i + n, CREAM, opacity);
			}
		}
		Vec3[] censers = m.censers();
		boolean fire = b.fire();
		for (int i = 0; i < censers.length && !fire; i++) {
			Vec3 censer = censers[i];
			double drift = Math.sin(age * .08 + i) * .1;
			LuxUmbraGeometry.ribbonPath(p, rot, List.of(censer, censer.add(drift, -.3, .05), censer.add(-drift, -.65, .08)),
					b.camera(), .035, .01, WISP, b.seed() + i, TINT, .3F);
		}
		PutricielMove move = PutricielMove.byId(m.moveId());
		float t = m.moveTime();
		if (move == null || t < 0 || fire) return;
		// every move's world geometry (the censers' wakes, the Communion's spores, the Bloom's field, the swell's wave,
		// the drop's burst) is PutricielTelegraphPass's; only the palm's drips stay a body overlay
		if (move == PutricielMove.OFFERING_GRASP) {
			float stretch = HemoEasing.window(t, 4, 12, 22, 30);
			Vec3 palm = m.palm();
			for (int i = 0; i < 3; i++) {
				Vec3 end = palm.add((i - 1) * .06, -.35 - stretch * .6, 0);
				LuxUmbraGeometry.ribbonPath(p, rot, List.of(palm, palm.lerp(end, .5).add(0, 0, .04), end), b.camera(),
						.02, .005, RIBBON, b.seed() + i, TINT, .5F * stretch);
			}
		}
	}

	/** Rot impacts, the censer's burst, a broken Communion's scatter, and the rest's lifting spores. */
	static void burst(PoseStack p, VertexConsumer rot, Vec3 camera, Vec3 right, Vec3 up, int kind, double radius, float age,
			float fade, int seed) {
		switch (kind) {
			case PutricielVisualRules.ROT_IMPACT -> {
				for (double offset : new double[]{-.12, .12}) {
					LuxUmbraGeometry.card(p, rot, new Vec3(offset, 0, 0), right, up, .08, .22, SLASH, seed, TINT, fade * .7F);
				}
				for (int i = 0; i < 4; i++) {
					double a = i * 1.57 + seed;
					double s = HemoEasing.easeOutCubic(age / 10.0);
					LuxUmbraGeometry.card(p, rot, new Vec3(Math.cos(a) * .4 * s, .1 + s * .2, Math.sin(a) * .4 * s), right, up,
							.03, .03, FOCUS, seed + i, CREAM, fade * .6F);
				}
			}
			case PutricielVisualRules.CENSER_BURST -> {
				// the drop's burst itself is PutricielTelegraphPass's (from the move); this is only a glint of spores
				for (int i = 0; i < 4; i++) {
					double a = i * 1.57 + seed;
					double s = HemoEasing.easeOutCubic(age / 12.0);
					LuxUmbraGeometry.card(p, rot, new Vec3(Math.cos(a) * .5 * s, .2 + s * .5, Math.sin(a) * .5 * s), right, up,
							.03, .03, FOCUS, seed + i, CREAM, fade * .5F);
				}
			}
			case PutricielVisualRules.COMMUNION_BREAK -> {
				double s = HemoEasing.easeOutCubic(age / 14.0);
				LuxUmbraGeometry.ground(p, rot, new Vec3(0, -.25, 0), radius * (.3 + .7 * s), RIPPLE, seed, TINT, fade * .35F);
				for (int i = 0; i < 8; i++) {
					double a = i * Math.PI / 4 + seed;
					Vec3 head = new Vec3(Math.cos(a) * radius * s * .8, .2 + Math.sin(s * Math.PI) * .6, Math.sin(a) * radius * s * .8);
					LuxUmbraGeometry.card(p, rot, head, right, up, .06, .1, SLASH, seed + i, TINT, fade * .55F);
				}
			}
			case PutricielVisualRules.REST -> {
				// spores and fine short threads lifting from the kneeling body, all under 0.4 alpha
				double travel = HemoEasing.easeOutCubic(age / 30.0);
				for (int i = 0; i < 10; i++) {
					double a = i * 2.39996 + seed;
					Vec3 base = new Vec3(Math.cos(a) * .5, 0, Math.sin(a) * .5);
					Vec3 head = base.add(Math.cos(a) * .2, travel * 1.6, Math.sin(a) * .2);
					LuxUmbraGeometry.ribbonPath(p, rot, List.of(base.add(0, travel * 1.2, 0), head), camera, .012, .003,
							RIBBON, seed + i, TINT, fade * .35F);
					LuxUmbraGeometry.card(p, rot, head, right, up, .03, .03, FOCUS, seed + i, CREAM, fade * .35F);
				}
			}
			default -> LuxUmbraGeometry.card(p, rot, Vec3.ZERO, right, up, .1, .1, SLASH, seed, CLOT, fade * .4F);
		}
	}
}
