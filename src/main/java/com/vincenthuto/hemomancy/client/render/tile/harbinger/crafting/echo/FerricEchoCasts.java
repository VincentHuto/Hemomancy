package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ferric.LodestoneCollapseRules;
import net.minecraft.world.phys.Vec3;

/** Ferric memories: forged needles, iron in and around the body, constructs, the choir and the lodestone. */
final class FerricEchoCasts {
	private FerricEchoCasts() {
	}

	static void registerAll() {
		EchoCasts.register("blood_needle", ctx -> needles(ctx, Form.NEEDLE_CHARGE, 5, 0.5, false));
		EchoCasts.register("blood_needle_fan", ctx -> needles(ctx, Form.FAN_CHARGE, 9, 1.6, false));
		EchoCasts.register("blood_needle_lance", ctx -> needles(ctx, Form.LANCE_CHARGE, 3, 0.15, true));
		EchoCasts.register("conjure_staff", ctx -> conjured(ctx));
		EchoCasts.register("blood_absorption", ctx -> conjured(ctx));
		EchoCasts.register("blood_projection", ctx -> conjured(ctx));
		EchoCasts.register("sanguine_mending", FerricEchoCasts::mending);
		EchoCasts.register("vascular_dowsing", FerricEchoCasts::dowsing);
		EchoCasts.register("iron_retort", FerricEchoCasts::retort);
		EchoCasts.register("ironhearted", FerricEchoCasts::ironheart);
		EchoCasts.register("sanguine_magnetism", FerricEchoCasts::magnetism);
		EchoCasts.register("ferric_rampart", FerricEchoCasts::rampart);
		EchoCasts.register("ferric_spikes", FerricEchoCasts::spikes);
		EchoCasts.register("iron_choir", FerricEchoCasts::choir);
		EchoCasts.register("lodestone_collapse", FerricEchoCasts::lodestone);
	}

	/** Forged needles gather in the hands and fly: a spread volley, a wide fan, or three piercing lances. */
	private static void needles(EchoCast.EchoContext ctx, Form charge, int count, double spread, boolean lance) {
		EchoFx.chargeThen(charge, ctx, ctx.target(), lance ? 30 : 20, () -> {
			EchoFx.activate(ctx);
			Vec3[] aims = EchoMoves.fan(ctx, count, spread);
			for (int i = 0; i < aims.length; i++) {
				Vec3 to = lance ? aims[i].add(ctx.forward().scale(ctx.scaled(1.2))) : aims[i];
				Vec3 from = ctx.hands().add(ctx.side().scale(ctx.scaled((i - (count - 1) / 2.0) * 0.06)));
				EchoScheduler.later(lance ? i * 3 : i, () -> EchoMoves.needle(ctx, from, to, 5, needle -> needle.setForged(true),
						() -> EchoFx.cue(Form.BLOOD_SPLASH, to, to.add(to.subtract(from).normalize()), ctx.scaled(0.5), 16, 4)));
			}
		});
	}

	/** The Living Staff, Absorption and Projection are conjured into the hand. */
	private static void conjured(EchoCast.EchoContext ctx) {
		EchoFx.cue(Form.FERRIC_CONJURE, ctx.feet(), ctx.feet(), ctx.scaled(0.5), 20, 1);
	}

	private static void mending(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.MENDING, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 18, 1);
		EchoScheduler.later(18, () -> EchoFx.cue(Form.MENDING, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 18, 1));
	}

	/** Ore veins light up in the floor before the echo. */
	private static void dowsing(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		for (int i = -1; i <= 1; i++) {
			Vec3 vein = ctx.targetFeet().add(ctx.side().scale(ctx.scaled(i * 1.1))).subtract(0, ctx.scaled(0.6), 0);
			EchoFx.keyed(Form.ORE, vein, vein, ctx.scaled(0.5), 25, 1);
		}
	}

	private static void retort(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.RETORT, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 30, 1);
		EchoScheduler.later(14, () -> EchoFx.cue(Form.FERRIC_IMPACT, ctx.feet().add(0, ctx.scaled(0.6), 0),
				ctx.target(), ctx.scaled(0.5), 14, 1));
	}

	private static void ironheart(EchoCast.EchoContext ctx) {
		EchoFx.chargeThen(Form.IRON_CHARGE, ctx, ctx.target(), 24, () -> {
			EchoFx.activate(ctx);
			EchoFx.cue(Form.IRON_HEART, ctx.feet(), ctx.feet(), ctx.scaled(6.0), 30, 1);
		});
	}

	/** A magnetic pillar rises before the echo, draws in, and collapses. */
	private static void magnetism(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 anchor = ctx.targetFeet();
		EchoFx.burst(Form.FERRIC_CONJURE, anchor, ctx.scaled(0.8), 20);
		EchoFx.burst(Form.MAGNET, anchor, ctx.scaled(1.8), 25);
		EchoScheduler.later(22, () -> EchoFx.cue(Form.LODESTONE_COLLAPSE, anchor, anchor, ctx.scaled(2.0),
				LodestoneCollapseRules.VISUAL_TICKS, 41));
	}

	/** A short iron wall raised across the loom's front, section by section. */
	private static void rampart(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 base = ctx.feet().add(ctx.forward().scale(ctx.scaled(1.6)));
		for (int i = -2; i <= 2; i++) {
			Vec3 section = base.add(ctx.side().scale(ctx.scaled(i * 0.7)));
			EchoScheduler.later((i + 2) * 2, () -> {
				EchoFx.burst(Form.FERRIC_CONJURE, section, ctx.scaled(0.7), 24);
				EchoFx.cue(Form.FERRIC_IMPACT, section, section.add(0, ctx.scaled(1.4), 0), ctx.scaled(0.4), 14, 1);
			});
		}
	}

	/** Iron spikes burst from the floor around the target. */
	private static void spikes(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3[] spikes = EchoMoves.ring(ctx, ctx.targetFeet(), 0.9, 4, 0.2);
		for (int i = 0; i < spikes.length; i++) {
			Vec3 at = spikes[i];
			EchoScheduler.later(i * 2, () -> {
				EchoFx.cue(Form.FERRIC_IMPACT, at, at.add(0, ctx.scaled(1.6), 0), ctx.scaled(0.6), 16, 1);
				EchoFx.burst(Form.FERRIC_CONJURE, at, ctx.scaled(0.4), 18);
			});
		}
	}

	/** The choir of iron hangs over the echo and strikes three shots out of the air. */
	private static void choir(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.CHOIR, ctx.feet(), ctx.feet(), ctx.scaled(2.5), 30, 3);
		Vec3 crown = ctx.feet().add(0, ctx.scaled(1.1), 0);
		Vec3[] intercepts = EchoMoves.fan(ctx, 3, 1.2);
		for (int i = 0; i < intercepts.length; i++) {
			Vec3 at = intercepts[i].add(0, ctx.scaled(0.4), 0);
			EchoScheduler.later(6 + i * 5, () -> {
				EchoFx.cue(Form.FERRIC_IMPACT, crown, at, ctx.scaled(0.5), 12, 1);
				EchoFx.cue(Form.CHOIR, ctx.feet(), ctx.feet(), ctx.scaled(2.5), 24, 2);
			});
		}
	}

	private static void lodestone(EchoCast.EchoContext ctx) {
		Vec3 centre = ctx.targetFeet();
		EchoFx.chargeThen(Form.IRON_CHARGE, ctx, ctx.target(), 30, () -> {
			EchoFx.activate(ctx);
			EchoFx.cue(Form.LODESTONE_COLLAPSE, centre, centre, ctx.scaled(2.6), LodestoneCollapseRules.VISUAL_TICKS, 63);
			for (Vec3 drawn : EchoMoves.ring(ctx, centre.add(0, ctx.scaled(0.8), 0), 2.4, 3, 0.7))
				EchoScheduler.later(6, () -> EchoFx.cue(Form.FERRIC_IMPACT, drawn, centre, ctx.scaled(0.6), 14, 1));
		});
	}
}
