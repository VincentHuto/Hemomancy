package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.lux.RadiantWakeRules;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.UmbralRendRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.world.phys.Vec3;

/** Lux and Tenebris memories: light flows, verdicts, beacons and sutures; umbral tears, veils, wells and rends. */
final class LuxUmbraEchoCasts {
	private static final ParticleColor UMBRA = new ParticleColor(96, 40, 140);

	private LuxUmbraEchoCasts() {
	}

	static void registerAll() {
		EchoCasts.register("conjure_spear", ctx -> AnimusEchoCasts.conjure(ctx, Form.FERRIC_CONJURE));
		EchoCasts.register("hematic_flare", LuxUmbraEchoCasts::flare);
		EchoCasts.register("prismatic_reproof", LuxUmbraEchoCasts::reproof);
		EchoCasts.register("hematic_beacon", LuxUmbraEchoCasts::beacon);
		EchoCasts.register("lumen_suture", LuxUmbraEchoCasts::suture);
		EchoCasts.register("white_verdict", LuxUmbraEchoCasts::verdict);
		EchoCasts.register("radiant_wake", LuxUmbraEchoCasts::radiantWake);
		EchoCasts.register("unclosing_eye", LuxUmbraEchoCasts::unclosingEye);

		EchoCasts.register("conjure_claws", ctx -> {
			EchoFx.activate(ctx);
			EchoFx.claw(ctx.ahead(0.9), ctx.forward(), UMBRA, false, 0.7F * ctx.figure());
		});
		EchoCasts.register("umbral_step", ctx -> step(ctx, 1));
		EchoCasts.register("umbral_reversal", ctx -> step(ctx, -1));
		EchoCasts.register("void_shroud", ctx -> veil(ctx, 18));
		EchoCasts.register("penumbral_drift", ctx -> veil(ctx, 40));
		EchoCasts.register("gloam_laceration", LuxUmbraEchoCasts::laceration);
		EchoCasts.register("blood_eclipse", LuxUmbraEchoCasts::eclipse);
		EchoCasts.register("black_veil_covenant", LuxUmbraEchoCasts::blackVeil);
		EchoCasts.register("eclipse_well", LuxUmbraEchoCasts::eclipseWell);
		EchoCasts.register("umbral_rend", LuxUmbraEchoCasts::rend);
	}

	//  Lux

	private static void flare(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 target = ctx.target();
		EchoFx.luxRelease(ctx.hands(), target);
		EchoScheduler.later(3, () -> {
			EchoFx.burst(Form.FLARE, target, ctx.scaled(1.1), 18);
			EchoFx.impact(ctx, target, ctx.hands());
		});
	}

	/** Three verdict rays thrown across a cone. */
	private static void reproof(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 eyes = ctx.feet().add(0, ctx.scaled(1.62), 0);
		Vec3[] struck = EchoMoves.fan(ctx, 3, 1.3);
		for (int i = 0; i < struck.length; i++) {
			Vec3 to = struck[i];
			EchoScheduler.later(i * 2, () -> {
				EchoFx.cue(Form.VERDICT, eyes, to, ctx.scaled(0.25), 12, 1);
				EchoFx.impact(ctx, to, eyes);
			});
		}
	}

	/** A beacon of light planted before the echo, its zone pulsing. */
	private static void beacon(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 centre = ctx.targetFeet();
		Vec3 crown = centre.add(0, ctx.scaled(1.35), 0);
		EchoFx.luxRelease(ctx.hands(), crown);
		EchoFx.keyed(Form.BEACON, centre, centre, ctx.scaled(2.2), 60, 1);
		EchoFx.accent(EnumBloodTendency.LUX, null, crown, Vec3.ZERO, false);
	}

	/** A suture of light stitched from the echo into an ally, mending them. */
	private static void suture(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 ally = ctx.feet().add(ctx.side().scale(ctx.scaled(1.4)));
		EchoFx.flow(false, ctx.chest(), ally.add(0, ctx.scaled(1.0), 0),
				HemomancyTendrilEffects.sutureConfig(ctx.scaled(1.4) + 4, EchoFx.nextSeed()));
		EchoScheduler.later(6, () -> {
			EchoFx.burst(Form.LUX_MENDING, ally, ctx.scaled(0.5), 28);
			EchoFx.accent(EnumBloodTendency.LUX, null, ally.add(0, ctx.scaled(1.0), 0), Vec3.ZERO, false);
		});
	}

	private static void verdict(EchoCast.EchoContext ctx) {
		Vec3 eyes = ctx.feet().add(0, ctx.scaled(1.62), 0), end = ctx.ahead(4.5);
		EchoFx.chargeThen(Form.VERDICT_CHARGE, ctx, end, 30, () -> {
			EchoFx.activate(ctx);
			EchoFx.cue(Form.WHITE_VERDICT, eyes, end, ctx.scaled(0.8), 40, 1);
		});
	}

	/** The wake unfurls standing, then glides, flaring at a nearby foe. */
	private static void radiantWake(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 feet = ctx.feet();
		EchoFx.cue(Form.RADIANT_WAKE, feet, feet, ctx.scaled(1.0), RadiantWakeRules.CUE_TICKS, RadiantWakeRules.STANDING);
		EchoScheduler.later(RadiantWakeRules.CUE_TICKS, () -> {
			EchoFx.cue(Form.RADIANT_WAKE, feet, feet, ctx.scaled(1.0), 0, RadiantWakeRules.GLIDING);
			EchoFx.cue(Form.RADIANT_WAKE, feet, feet, ctx.scaled(1.0), RadiantWakeRules.CUE_TICKS, RadiantWakeRules.GLIDING);
			EchoFx.burst(Form.FLARE, ctx.target(), ctx.scaled(0.6), 14);
		});
	}

	private static void unclosingEye(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.EYE, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 60, 1);
		EchoFx.accent(EnumBloodTendency.LUX, null, ctx.feet().add(0, ctx.scaled(2.5), 0), Vec3.ZERO, false);
	}

	//  Tenebris

	/** The echo tears out of place and arrives a short way off: forward for a step, back for a reversal. */
	private static void step(EchoCast.EchoContext ctx, int direction) {
		Vec3 from = ctx.feet(), to = from.add(ctx.side().scale(ctx.scaled(1.8) * direction));
		EchoFx.burst(Form.TELEPORT, from, ctx.scaled(1.0), 18);
		EchoFx.accent(EnumBloodTendency.TENEBRIS, null, from.add(0, ctx.scaled(1.0), 0), Vec3.ZERO, false);
		EchoScheduler.later(4, () -> {
			EchoFx.burst(Form.UMBRA_ARRIVAL, to, ctx.scaled(1.0), 22);
			EchoFx.accent(EnumBloodTendency.TENEBRIS, null, to.add(0, ctx.scaled(1.0), 0), Vec3.ZERO, false);
		});
	}

	private static void veil(EchoCast.EchoContext ctx, int ticks) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.VEIL, ctx.feet(), ctx.feet(), ctx.scaled(1.0), ticks, 1);
		EchoFx.accent(EnumBloodTendency.TENEBRIS, null, ctx.chest(), Vec3.ZERO, false);
	}

	private static void laceration(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 centre = ctx.target();
		EchoFx.cue(Form.UMBRA_SLASH, centre, centre.add(ctx.forward()), ctx.scaled(1.0), 18, (int) (EchoFx.nextSeed() & 0xFF) % 360);
		EchoFx.claw(centre, ctx.forward(), UMBRA, true, 0.6F * ctx.figure());
		EchoFx.impact(ctx, centre, ctx.hands());
	}

	/** Umbra flows lash from the echo through a cone of three. */
	private static void eclipse(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3[] struck = EchoMoves.fan(ctx, 3, 1.4);
		for (int i = 0; i < struck.length; i++) {
			Vec3 to = struck[i];
			EchoScheduler.later(i * 2, () -> {
				EchoFx.flow(true, ctx.chest(), to, HemomancyTendrilEffects.voidConfig(ctx.chest().distanceTo(to) + 5, EchoFx.nextSeed()));
				EchoFx.impact(ctx, to, ctx.chest());
			});
		}
	}

	/** A small covenant of black veil drawn around the echo, its surface threaded with umbra. */
	private static void blackVeil(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 centre = ctx.feet().add(0, 0.18, 0);
		float radius = (float) ctx.scaled(2.0);
		EchoFx.veil(centre, radius, 60);
		for (Vec3 rim : EchoMoves.ring(ctx, centre, 2.0, 4, 0.4))
			EchoFx.flow(true, centre, rim, HemomancyTendrilEffects.voidSurfaceConfig(radius + 4, EchoFx.nextSeed()));
		EchoFx.accent(EnumBloodTendency.TENEBRIS, null, centre.add(0, 0.4, 0), Vec3.ZERO, false);
	}

	private static void eclipseWell(EchoCast.EchoContext ctx) {
		Vec3 centre = ctx.targetFeet();
		EchoFx.chargeThen(Form.WELL_CHARGE, ctx, centre, 30, () -> {
			EchoFx.activate(ctx);
			EchoFx.umbraRelease(ctx.hands(), centre);
			EchoFx.keyed(Form.WELL, centre, centre, ctx.scaled(2.2), 60, 1);
		});
	}

	/** Obscured bodies are rent open toward the echo's eyes. */
	private static void rend(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 eyes = ctx.feet().add(0, ctx.scaled(1.62), 0);
		Vec3[] struck = EchoMoves.fan(ctx, 2, 0.9);
		for (int i = 0; i < struck.length; i++) {
			Vec3 body = struck[i].subtract(0, ctx.scaled(0.2), 0);
			int tilt = i * 2 % UmbralRendRules.TILTS;
			boolean mirrored = i % 2 == 1;
			EchoScheduler.later(i * 3, () -> {
				EchoFx.umbraRelease(eyes, body);
				EchoFx.cue(Form.UMBRAL_REND, body, eyes, ctx.scaled(1.0), UmbralRendRules.VISUAL_TICKS,
						UmbralRendRules.encode(tilt, mirrored));
			});
		}
	}
}
