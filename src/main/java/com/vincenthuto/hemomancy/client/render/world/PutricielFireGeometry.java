package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.List;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.FOCUS;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.RIBBON;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.RIPPLE;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.SLASH;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.WISP;

/**
 * Putriciel's fire, on the Flammeus {@code FLAME} material (crimson body, pink-white heat, black soot tips): the lit
 * censers and the core's heat in the interval, the Pyre Heart's core card, and the focal moment, the Offering, to the
 * Hematic Riposte standard (docs/MANIPULATION_VISUALS.md): back to front a broad translucent heat shell rising off the
 * palm, three fine flame threads spiralling up from it (the sinew), the five nodule knots, the palm's flame (the
 * business end), and on acceptance a recoil trail down the forearm and the new nodule swelling with an overshoot.
 * Also the fire bursts and the ending's last cleansing. It draws only with the fire stage or the ending (the caller
 * gates it), so the rot never shows a flame.
 */
final class PutricielFireGeometry {
	/** The hot core, pink-white. */
	static final int CORE = 0xFFD2C4;
	static final int TINT = 0xFFFFFF;

	private PutricielFireGeometry() {
	}

	static void body(PoseStack p, VertexConsumer fire, VertexConsumer rot, PutricielOfferingGeometry.Body b) {
		var m = b.motion();
		float age = b.age();
		float ember = m.look().ember();
		if (ember <= .01F) return;
		for (int i = 0; i < m.censers().length; i++) {
			Vec3 censer = m.censers()[i];
			double flicker = .06 + .03 * Math.sin(age * .5 + i * 2.1);
			LuxUmbraGeometry.card(p, fire, censer.add(0, flicker, 0), b.right(), b.up(), .05, .09, FOCUS, b.seed() + i, CORE, .6F * ember);
			LuxUmbraGeometry.card(p, fire, censer.add(0, .2 + flicker, 0), b.right(), b.up(), .07, .16, WISP, b.seed() + i, TINT, .3F * ember);
		}
		LuxUmbraGeometry.card(p, fire, m.core(), b.right(), b.up(), .12, .12, FOCUS, b.seed(), CORE, .18F * ember);
		PutricielMove move = PutricielMove.byId(m.moveId());
		float t = m.moveTime();
		if (move == PutricielMove.PYRE_HEART && t >= 0) {
			float flare = HemoEasing.window(t, 10, 28, 36, 60);
			LuxUmbraGeometry.card(p, fire, m.core(), b.right(), b.up(), .25 + .1 * flare, .25 + .1 * flare, FOCUS, b.seed(), CORE, .55F * flare);
		}
		if (move == PutricielMove.FULL_PURGE && t >= 0) {
			float white = HemoEasing.window(t, 0, 30, 90, 108);
			LuxUmbraGeometry.card(p, fire, m.core(), b.right(), b.up(), .22, .22, FOCUS, b.seed(), 0xFFF4F0, .6F * white);
		}
		if (m.openingId() == PutricielOpenings.OFFERING_ID || m.sinceAccepted() < PutricielAbsolutionRules.RECOIL_TICKS + 6) {
			offering(p, fire, rot, b, m.openingTime());
		}
		if (m.endingTime() >= 0) ending(p, fire, b, m.endingTime());
	}

	/** The Offering: shell, sinew, knots, business end, motion. Seeded flicker per element. */
	private static void offering(PoseStack p, VertexConsumer fire, VertexConsumer rot, PutricielOfferingGeometry.Body b,
			float openingTime) {
		var m = b.motion();
		float age = b.age();
		float open = openingTime < 0 ? 0 : HemoEasing.ramp(openingTime, 0, 6);
		Vec3 palm = m.palm().add(0, .08, 0);
		// shell: a broad translucent heat wisp rising off the palm, with wisp cards trailing upward
		LuxUmbraGeometry.card(p, fire, palm.add(0, .42, 0), b.right(), b.up(), .3, .56, WISP, b.seed(), TINT, .3F * open);
		for (int i = 0; i < 3; i++) {
			double s = (age * .03 + i / 3.0) % 1;
			LuxUmbraGeometry.card(p, fire, palm.add(Math.sin(i * 2.1 + age * .1) * .08, .3 + s * .9, 0), b.right(), b.up(),
					.08, .16, WISP, b.seed() + i, TINT, (float) Math.sin(s * Math.PI) * .26F * open);
		}
		// sinew: three fine threads spiralling up, tapering 0.035 to 0.018, pulsing pow(max(0, sin(t * 0.6)), 4)
		for (int strand = 0; strand < 3; strand++) {
			List<Vec3> points = new ArrayList<>();
			for (int k = 0; k <= 6; k++) {
				double h = k / 6.0;
				double a = strand * Math.PI * 2 / 3 + h * 3.2 + age * .12;
				points.add(palm.add(Math.cos(a) * .09 * (1 - h * .5), h * .75, Math.sin(a) * .09 * (1 - h * .5)));
			}
			float pulse = (float) Math.pow(Math.max(0, Math.sin(age * .6 + strand * 2.1)), 4);
			LuxUmbraGeometry.ribbonPath(p, fire, points, b.camera(), .035, .018, RIBBON, b.seed() + strand, CORE, (.3F + .2F * pulse) * open);
		}
		// knots: the five nodules over the palm, lit ones pink-white
		int lit = m.absolutions();
		for (int i = 0; i < PutricielAbsolutionRules.NEEDED; i++) {
			double a = i * Math.PI * 2 / PutricielAbsolutionRules.NEEDED + age * .01;
			Vec3 nodule = palm.add(Math.cos(a) * .3, .34 + Math.sin(age * .08 + i) * .02, Math.sin(a) * .3);
			if (i < lit) {
				float swell = i == lit - 1 ? overshoot(m.sinceAccepted()) : 1.0F;
				LuxUmbraGeometry.card(p, fire, nodule, b.right(), b.up(), .07 * swell, .07 * swell, FOCUS, b.seed() + i, CORE, .95F * Math.max(open, .5F));
			} else {
				LuxUmbraGeometry.card(p, rot, nodule, b.right(), b.up(), .055, .055, SLASH, b.seed() + i, PutricielRotGeometry.CREAM, .6F * open);
			}
		}
		// business end: the palm's own flame
		LuxUmbraGeometry.card(p, fire, palm, b.right(), b.up(), .11, .15, FOCUS, b.seed(), CORE, .65F * open);
		// motion: the recoil trail down the forearm on acceptance
		float recoil = HemoEasing.pulse(m.sinceAccepted(), 2, PutricielAbsolutionRules.RECOIL_TICKS);
		if (recoil > .01F) {
			Vec3 elbow = palm.lerp(m.core(), .5).add(0, -.1, 0);
			LuxUmbraGeometry.ribbonPath(p, fire, List.of(palm, elbow, m.core()), b.camera(), .03, .01, RIBBON, b.seed(), CORE, .55F * recoil);
			for (int i = 0; i < 4; i++) {
				double a = i * 1.57 + b.seed();
				double s = HemoEasing.easeOutCubic(m.sinceAccepted() / 10.0);
				LuxUmbraGeometry.card(p, fire, palm.add(Math.cos(a) * .3 * s, .1 + s * .3, Math.sin(a) * .3 * s), b.right(), b.up(),
						.025, .025, FOCUS, b.seed() + i, CORE, .8F * recoil);
			}
		}
	}

	/** The new nodule's swell: a sine overshoot over 8 ticks after an acceptance. */
	static float overshoot(float since) {
		if (since < 0 || since > 8) return 1.0F;
		return 1.0F + (float) Math.sin(since / 8.0 * Math.PI) * .6F;
	}

	/** The last cleansing climbing the body, the man's face lit for a breath, then soot. */
	private static void ending(PoseStack p, VertexConsumer fire, PutricielOfferingGeometry.Body b, float t) {
		var m = b.motion();
		float burn = PutricielEndingRules.ember(t);
		for (int i = 0; i < 6; i++) {
			double s = (t * .04 + i / 6.0) % 1;
			Vec3 at = m.core().add(Math.sin(i * 1.7) * .3, -0.8 + s * 2.0, Math.cos(i * 1.7) * .2);
			LuxUmbraGeometry.card(p, fire, at, b.right(), b.up(), .08, .18, WISP, b.seed() + i, TINT, (float) Math.sin(s * Math.PI) * .4F * burn);
		}
		float face = PutricielEndingRules.face(t);
		LuxUmbraGeometry.card(p, fire, m.face(), b.right(), b.up(), .2, .25, FOCUS, b.seed(), CORE, .25F * face);
	}

	/** Fire bursts: impacts, the absolution's swell, the Ignition's ring, an ember's soot, a scorched cell. */
	static void burst(PoseStack p, VertexConsumer fire, VertexConsumer rot, Vec3 camera, Vec3 right, Vec3 up, int kind,
			double radius, float age, float fade, int seed) {
		switch (kind) {
			case PutricielVisualRules.FIRE_IMPACT -> {
				LuxUmbraGeometry.card(p, fire, Vec3.ZERO, right, up, .14, .14, FOCUS, seed, CORE, fade * .8F);
				LuxUmbraGeometry.ground(p, fire, new Vec3(0, -.9, 0), Math.min(radius, .6 + radius * .2), RIPPLE, seed, TINT, fade * .4F);
				for (int i = 0; i < 4; i++) {
					double a = i * 1.57 + seed;
					double s = HemoEasing.easeOutCubic(age / 8.0);
					LuxUmbraGeometry.card(p, fire, new Vec3(Math.cos(a) * .35 * s, .1 + s * .35, Math.sin(a) * .35 * s), right, up,
							.02, .02, FOCUS, seed + i, CORE, fade);
				}
			}
			case PutricielVisualRules.ABSOLVE -> {
				float swell = overshoot(age);
				LuxUmbraGeometry.card(p, fire, Vec3.ZERO, right, up, .12 * swell, .12 * swell, FOCUS, seed, CORE, fade);
				LuxUmbraGeometry.card(p, fire, new Vec3(0, .25, 0), right, up, .2, .4, WISP, seed, TINT, fade * .3F);
			}
			case PutricielVisualRules.IGNITE -> {
				// the purge's ring of fire is PutricielTelegraphPass's, drawn from the Ignition and the Full Purge
				// themselves so it keeps their waves' timing; the burst is only the heat leaving the body
				float heat = HemoEasing.pulse(age, 1.5F, 12.0F);
				LuxUmbraGeometry.card(p, fire, new Vec3(0, .9, 0), right, up, .22 + .2 * heat, .22 + .2 * heat, FOCUS, seed, CORE, fade * heat);
				for (int i = 0; i < 4; i++) {
					double a = i * 1.57 + seed;
					double s = HemoEasing.easeOutCubic(age / 10.0);
					LuxUmbraGeometry.card(p, fire, new Vec3(Math.cos(a) * .5 * s, .5 + s * .6, Math.sin(a) * .5 * s), right, up,
							.025, .025, FOCUS, seed + i, CORE, fade * .8F);
				}
			}
			case PutricielVisualRules.EMBER -> {
				LuxUmbraGeometry.card(p, fire, Vec3.ZERO, right, up, .1, .14, FOCUS, seed, CORE, fade * .5F);
				LuxUmbraGeometry.card(p, rot, new Vec3(0, .3, 0), right, up, .14, .2, SLASH, seed, PutricielRotGeometry.CLOT, fade * .4F);
			}
			case PutricielVisualRules.SCORCH -> LuxUmbraGeometry.ground(p, fire, new Vec3(0, -.45, 0), .6, RIPPLE, seed, TINT, fade * .35F);
			default -> {
			}
		}
	}
}
