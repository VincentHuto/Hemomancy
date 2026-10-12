package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinRules;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.SearingLashRules;
import net.minecraft.world.phys.Vec3;

/** Flammeus and Congeatio memories: conjured flame, forge, ignition, updrafts, the lash; frost, bone, cruor, the hour. */
final class ThermalEchoCasts {
	private ThermalEchoCasts() {
	}

	static void registerAll() {
		EchoCasts.register("conjure_torch", ctx -> AnimusEchoCasts.conjure(ctx, Form.FLAME_CONJURE));
		EchoCasts.register("crimson_flame_conjuration", ThermalEchoCasts::flameConjuration);
		EchoCasts.register("pyretic_forge", ThermalEchoCasts::forge);
		EchoCasts.register("sanguine_ignition", ctx -> ignition(ctx, 2.4, false));
		EchoCasts.register("cauterizing_rebuke", ctx -> ignition(ctx, 2.0, true));
		EchoCasts.register("vitric_combustion", ThermalEchoCasts::vitric);
		EchoCasts.register("scalding_updraft", ctx -> updraft(ctx, 1.0, 1));
		EchoCasts.register("soaring_updraft", ctx -> updraft(ctx, 0.6, 2));
		EchoCasts.register("suspended_updraft", ctx -> updraft(ctx, 1.0, 3));
		EchoCasts.register("expulsive_updraft", ctx -> updraft(ctx, 2.0, 4));
		EchoCasts.register("furnace_veins", ctx -> aura(ctx, Form.FURNACE, 2.0));
		EchoCasts.register("phoenix_debt", ThermalEchoCasts::phoenix);
		EchoCasts.register("searing_lash", ThermalEchoCasts::lash);

		EchoCasts.register("conjure_flail", ctx -> AnimusEchoCasts.conjure(ctx, Form.FROST_CONJURE));
		EchoCasts.register("glacial_grasp", ThermalEchoCasts::glacialGrasp);
		EchoCasts.register("cryogenic_pulse", ThermalEchoCasts::cryogenicPulse);
		EchoCasts.register("glacial_rampart", ThermalEchoCasts::rampart);
		EchoCasts.register("osseous_bloom", ThermalEchoCasts::osseousBloom);
		EchoCasts.register("absolute_stillness", ctx -> aura(ctx, Form.STILLNESS, 2.0));
		EchoCasts.register("rimebound_sentence", ThermalEchoCasts::rimebound);
		EchoCasts.register("cruor_javelin", ThermalEchoCasts::javelin);
		EchoCasts.register("endless_hour", ThermalEchoCasts::endlessHour);
	}

	//  Flammeus

	private static void flameConjuration(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 fire = ctx.targetFeet();
		EchoFx.cue(Form.FLAME_CONJURE, fire, fire.add(0, ctx.scaled(0.5), 0), ctx.scaled(0.7), 30, 1);
	}

	private static void forge(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.FORGE, ctx.hands().subtract(0, ctx.scaled(0.4), 0), ctx.feet(), ctx.scaled(1.0), 24, 1);
	}

	private static void ignition(EchoCast.EchoContext ctx, double radius, boolean cauterize) {
		EchoFx.activate(ctx);
		EchoFx.burst(Form.IGNITION, ctx.feet(), ctx.scaled(radius), cauterize ? 14 : 18);
		if (cauterize) EchoFx.cue(Form.CAUTERIZE, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 24, 2);
	}

	private static void vitric(EchoCast.EchoContext ctx) {
		Vec3 blast = ctx.target();
		EchoFx.chargeThen(Form.GLASS_CHARGE, ctx, blast, 30, () -> {
			EchoFx.activate(ctx);
			EchoFx.burst(Form.GLASS, blast, ctx.scaled(2.0), 28);
			EchoFx.impact(ctx, blast, ctx.hands());
		});
	}

	/** A scalding column under the echo; the expulsive form throws a burst of flame outward. */
	private static void updraft(EchoCast.EchoContext ctx, double radius, int variant) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.UPDRAFT, ctx.feet(), ctx.feet(), ctx.scaled(radius), variant == 3 ? 25 : 16, variant);
		if (variant == 4) EchoFx.burst(Form.IGNITION, ctx.feet(), ctx.scaled(2.0), 18);
	}

	private static void aura(EchoCast.EchoContext ctx, Form form, double radius) {
		EchoFx.activate(ctx);
		EchoFx.cue(form, ctx.feet(), ctx.feet(), ctx.scaled(radius), 30, 1);
	}

	/** The phoenix waits, then its debt is called and the echo burns back up. */
	private static void phoenix(EchoCast.EchoContext ctx) {
		EchoFx.cue(Form.PHOENIX_READY, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 20, 1);
		EchoScheduler.later(16, () -> {
			EchoFx.activate(ctx);
			EchoFx.cue(Form.PHOENIX, ctx.feet(), ctx.feet(), ctx.scaled(1.6), 48, 1);
		});
	}

	private static void lash(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 hand = ctx.hands().add(ctx.forward().scale(ctx.scaled(0.4))).subtract(0, ctx.scaled(0.45), 0);
		Vec3 target = ctx.target();
		EchoFx.cue(Form.SEARING_LASH, hand, target, ctx.scaled(1.0), SearingLashRules.VISUAL_TICKS, SearingLashRules.encode(false));
		EchoScheduler.later(SearingLashRules.CRACK_END, () -> EchoFx.impact(ctx, target, hand));
	}

	//  Congeatio

	/** Frost runs from the floor ahead back toward the echo, and ice closes over the grasped spot. */
	private static void glacialGrasp(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 centre = ctx.targetFeet();
		EchoFx.cue(Form.ICE, centre.add(0, ctx.scaled(1.0), 0), centre, ctx.scaled(2.0), 24, 1);
		for (int i = -1; i <= 1; i++) {
			Vec3 frozen = centre.add(ctx.side().scale(ctx.scaled(i * 0.8)));
			EchoScheduler.later(2 + (i + 1) * 2, () -> EchoFx.cue(Form.FROST_ADVANCE, frozen, ctx.feet(), ctx.scaled(0.45), 36, 1));
		}
	}

	private static void cryogenicPulse(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.burst(Form.CRYOGENIC_PULSE, ctx.feet(), ctx.scaled(2.4), 28);
		for (Vec3 struck : EchoMoves.fan(ctx, 2, 1.2)) EchoFx.burst(Form.BONE, struck, ctx.scaled(0.5), 24);
	}

	/** A short wall of cruor ice rises across the loom's front, and later breaks. */
	private static void rampart(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 base = ctx.feet().add(ctx.forward().scale(ctx.scaled(1.6)));
		for (int i = -2; i <= 2; i++) {
			for (int h = 0; h < 2; h++) {
				Vec3 block = base.add(ctx.side().scale(ctx.scaled(i * 0.8))).add(0, ctx.scaled(h * 0.8), 0);
				int delay = (i + 2) * 2 + h * 3;
				EchoScheduler.later(delay, () -> EchoFx.cue(Form.CRUOR_FORM, block, block.add(0, ctx.scaled(0.4), 0), ctx.scaled(0.45), 12, 1));
				EchoScheduler.later(delay + 40, () -> EchoFx.burst(Form.CRUOR_BREAK, block, ctx.scaled(0.45), 8));
			}
		}
	}

	private static void osseousBloom(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3[] struck = EchoMoves.ring(ctx, ctx.feet().add(0, ctx.scaled(0.6), 0), 2.2, 3, 0.5);
		for (int i = 0; i < struck.length; i++) {
			Vec3 at = struck[i];
			EchoScheduler.later(i * 3, () -> EchoFx.burst(Form.BONE, at, ctx.scaled(0.7), 32));
		}
	}

	private static void rimebound(EchoCast.EchoContext ctx) {
		Vec3 target = ctx.target();
		EchoFx.chargeThen(Form.ICE_CHARGE, ctx, target, 28, () -> {
			EchoFx.activate(ctx);
			EchoFx.burst(Form.RIMEBOUND, ctx.targetFeet(), ctx.scaled(0.7), 60);
			EchoFx.impact(ctx, target, ctx.hands());
		});
	}

	private static void javelin(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 from = ctx.hands().add(ctx.forward().scale(ctx.scaled(0.4))).subtract(0, ctx.scaled(0.4), 0), to = ctx.target();
		EchoFx.cue(Form.CRUOR_JAVELIN, from, to, ctx.scaled(0.5), CruorJavelinRules.VISUAL_TICKS, 1);
		EchoScheduler.later(CruorJavelinRules.FLIGHT_END, () -> {
			EchoFx.burst(Form.FROZEN_VEINS, ctx.targetFeet(), ctx.scaled(0.8), 40);
			EchoFx.impact(ctx, to, from);
		});
	}

	/** Harm deferred into the hour gathers around the echo, then breaks all at once. */
	private static void endlessHour(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.HOUR, ctx.feet(), ctx.feet(), ctx.scaled(2.0), 40, 1);
		EchoScheduler.later(36, () -> {
			EchoFx.burst(Form.HOUR_BREAK, ctx.feet(), ctx.scaled(2.0), 24);
			EchoFx.accent(EnumBloodTendency.CONGEATIO, null, ctx.chest(), Vec3.ZERO, true);
		});
	}
}
