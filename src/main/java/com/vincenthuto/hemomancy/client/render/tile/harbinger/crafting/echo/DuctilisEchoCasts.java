package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.NerveCommandRules;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.world.phys.Vec3;

/** Ductilis memories: nerve lightning, commands over bodies, the gaze, the thread, the ward and the circuit. */
final class DuctilisEchoCasts {
	private static final ParticleColor SIGNAL_GOLD = new ParticleColor(255, 214, 92);

	private DuctilisEchoCasts() {
	}

	static void registerAll() {
		EchoCasts.register("synaptic_jolt", DuctilisEchoCasts::jolt);
		EchoCasts.register("activation_potential", DuctilisEchoCasts::activationPotential);
		EchoCasts.register("synaptic_storm", DuctilisEchoCasts::storm);
		EchoCasts.register("hemolymphal_pulse", DuctilisEchoCasts::pulse);
		EchoCasts.register("conductive_mark", DuctilisEchoCasts::mark);
		EchoCasts.register("sanguine_ward", DuctilisEchoCasts::ward);
		EchoCasts.register("living_circuit", DuctilisEchoCasts::circuit);
		EchoCasts.register("hematic_rebuke", ctx -> command(ctx, NerveCommandRules.REBUKE));
		EchoCasts.register("hematic_impressment", ctx -> command(ctx, NerveCommandRules.IMPRESS));
		EchoCasts.register("sanguine_marionette", DuctilisEchoCasts::marionette);
		EchoCasts.register("sovereign_instinct", DuctilisEchoCasts::sovereign);
		EchoCasts.register("deadly_gaze", DuctilisEchoCasts::gaze);
		EchoCasts.register("thread_ripper", DuctilisEchoCasts::threadRipper);
		EchoCasts.register("conjure_crossbow", ctx -> AnimusEchoCasts.conjure(ctx, Form.FERRIC_CONJURE));
		EchoCasts.register("axonal_transduction", DuctilisEchoCasts::axonal);
	}

	private static void nervousImpact(EchoCast.EchoContext ctx, Vec3 at) {
		EchoFx.cue(Form.NERVE_HIT, at.add(0, ctx.scaled(0.6), 0), at, ctx.scaled(0.5), 12, 1);
	}

	private static void jolt(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 target = ctx.target();
		EchoFx.strike(ctx.hands(), target, ctx.figure());
		EchoFx.nerve(ctx.hands(), target, 10);
		nervousImpact(ctx, target);
		EchoFx.bolt(target, target.add(ctx.side().scale(ctx.scaled(0.3))).add(0, ctx.scaled(0.18), 0), 56F, 2F, 4, 5,
				0.05F * ctx.figure(), 0.012F * ctx.figure());
	}

	private static void activationPotential(EchoCast.EchoContext ctx) {
		Vec3[] targets = EchoMoves.fan(ctx, 3, 1.2);
		EchoFx.chargeThen(Form.LIGHTNING_CHARGE, ctx, ctx.target(), 20, () -> {
			EchoFx.activate(ctx);
			for (int i = 0; i < targets.length; i++) {
				Vec3 target = targets[i];
				EchoScheduler.later(i, () -> {
					EchoFx.bolt(ctx.hands(), target, 64F, 2F, 7, 7, 0.11F * ctx.figure(), 0.017F * ctx.figure());
					nervousImpact(ctx, target);
				});
			}
		});
	}

	/** The storm leaps hop by hop through three bodies. */
	private static void storm(EchoCast.EchoContext ctx) {
		Vec3[] hops = {ctx.target(), ctx.target().add(ctx.side().scale(ctx.scaled(1.4))),
				ctx.target().add(ctx.side().scale(ctx.scaled(-1.2))).add(ctx.forward().scale(ctx.scaled(0.8)))};
		EchoFx.chargeThen(Form.LIGHTNING_CHARGE, ctx, ctx.target(), 28, () -> {
			EchoFx.activate(ctx);
			Vec3 previous = ctx.hands();
			for (int i = 0; i < hops.length; i++) {
				Vec3 from = previous, to = hops[i];
				EchoScheduler.later(i * 3, () -> {
					EchoFx.strike(from, to, ctx.figure());
					EchoFx.nerve(from, to, 10);
					EchoFx.burst(Form.PARALYSIS, to, ctx.scaled(1.0), 30);
				});
				previous = to;
			}
		});
	}

	/** A nerve pulse rolling out from the echo, twelve bolts thrown in every direction. */
	private static void pulse(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 centre = ctx.chest();
		EchoFx.burst(Form.NERVE_PULSE, ctx.feet(), ctx.scaled(3.0), 32);
		for (int i = 0; i < 12; i++) {
			double yaw = i * Math.PI * 2 / 12, pitch = (i % 3 - 1) * 0.5;
			Vec3 dir = new Vec3(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch), Math.sin(yaw) * Math.cos(pitch));
			EchoFx.bolt(centre.add(dir.scale(ctx.scaled(0.24))), centre.add(dir.scale(ctx.scaled(1.6))), 40F, 1.8F, 8, 5,
					0.09F * ctx.figure(), 0.012F * ctx.figure());
		}
	}

	/** Five short arcs ring the target, then the mark settles on it. */
	private static void mark(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 centre = ctx.target();
		for (int i = 0; i < 5; i++) {
			double a = i * Math.PI * 2 / 5;
			Vec3 start = centre.add(Math.cos(a) * ctx.scaled(0.52), ctx.scaled(-0.25 + (i % 2) * 0.24), Math.sin(a) * ctx.scaled(0.52));
			Vec3 end = centre.add(Math.cos(a + 0.95) * ctx.scaled(0.52), ctx.scaled(0.25 - (i % 2) * 0.24), Math.sin(a + 0.95) * ctx.scaled(0.52));
			EchoFx.bolt(start, end, 44F, 1.6F, 5, 5, 0.055F * ctx.figure(), 0.011F * ctx.figure());
		}
		EchoFx.burst(Form.MARK, centre.subtract(0, ctx.scaled(0.6), 0), ctx.scaled(0.6), 40);
	}

	/** The ward settles over the echo, six arcs running its rim. */
	private static void ward(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.WARD, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 25, 4);
		Vec3 centre = ctx.feet().add(0, ctx.scaled(1.0), 0);
		for (int i = 0; i < 6; i++) {
			double a = i * Math.PI / 3, b = a + Math.PI * 0.38;
			EchoFx.bolt(centre.add(Math.cos(a) * ctx.scaled(0.72), ctx.scaled(-0.25 + (i % 2) * 0.5), Math.sin(a) * ctx.scaled(0.72)),
					centre.add(Math.cos(b) * ctx.scaled(0.72), ctx.scaled(0.25 - (i % 2) * 0.5), Math.sin(b) * ctx.scaled(0.72)),
					36F, 1.4F, 8, 5, 0.08F * ctx.figure(), 0.012F * ctx.figure());
		}
	}

	/** A living circuit closed through an ally beside the echo and out into a foe. */
	private static void circuit(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 ally = ctx.feet().add(ctx.side().scale(ctx.scaled(1.2)));
		EchoFx.nerve(ctx.chest(), ally.add(0, ctx.scaled(1.0), 0), 25);
		EchoFx.burst(Form.CIRCUIT, ally, ctx.scaled(1.0), 40);
		EchoScheduler.later(10, () -> {
			EchoFx.strike(ally.add(0, ctx.scaled(1.0), 0), ctx.target(), ctx.figure());
			EchoFx.burst(Form.MARK, ctx.targetFeet(), ctx.scaled(1.0), 30);
		});
	}

	/** A command bites into a body's nerves: rebuke strikes and holds it, impressment leashes it. */
	private static void command(EchoCast.EchoContext ctx, int mode) {
		EchoFx.activate(ctx);
		Vec3 body = ctx.targetFeet();
		if (mode == NerveCommandRules.REBUKE) {
			EchoFx.strike(ctx.hands(), ctx.target(), ctx.figure());
			nervousImpact(ctx, ctx.target());
		}
		EchoFx.cue(Form.NERVE_COMMAND, body, ctx.hands(), ctx.scaled(1.8), 30, mode);
	}

	/** Strings drawn from the echo's hands to a body, which is then sent at a foe. */
	private static void marionette(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 body = ctx.targetFeet();
		EchoFx.blood(com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket.Style.LIVING, ctx.hands(),
				body.add(0, ctx.scaled(1.2), 0), 30);
		EchoScheduler.later(12, () -> EchoFx.cue(Form.MARIONETTE_ORDER, body,
				body.add(ctx.side().scale(ctx.scaled(1.6))), ctx.scaled(1.0), 20, -1));
	}

	/** Bodies that would surround the echo are turned: a command crown, then lashes. */
	private static void sovereign(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.COMMAND, ctx.feet(), ctx.feet(), ctx.scaled(1.8), 25, 3);
		Vec3[] bodies = EchoMoves.ring(ctx, ctx.feet().add(0, ctx.scaled(0.8), 0), 2.2, 4, 0.3);
		for (int i = 0; i < bodies.length; i++) {
			Vec3 body = bodies[i];
			EchoScheduler.later(6 + i * 2, () -> EchoFx.cue(Form.SOVEREIGN_LASH, ctx.chest(), body, ctx.scaled(1.0), 14, 1));
		}
	}

	/** The gaze charges, grips the target back to the echo's eyes, and slams it down. */
	private static void gaze(EchoCast.EchoContext ctx) {
		Vec3 target = ctx.target(), eyes = ctx.feet().add(0, ctx.scaled(1.62), 0);
		EchoFx.chargeThen(Form.GAZE_CHARGE, ctx, target, 24, () -> {
			EchoFx.activate(ctx);
			EchoFx.cue(Form.GAZE_GRIP, ctx.targetFeet(), eyes, ctx.scaled(1.0), 14, 2);
			EchoScheduler.later(12, () -> EchoFx.burst(Form.GAZE_SLAM, ctx.targetFeet(), ctx.scaled(1.2), 16));
		});
	}

	private static void threadRipper(EchoCast.EchoContext ctx) {
		Vec3 target = ctx.target();
		EchoFx.chargeThen(Form.THREAD_CHARGE, ctx, target, 20, () -> {
			EchoFx.activate(ctx);
			EchoFx.cue(Form.THREAD, ctx.feet().add(0, ctx.scaled(1.62), 0), target, ctx.scaled(1.0), 24, 1);
			EchoFx.impact(ctx, target, ctx.hands());
		});
	}

	/**
	 * The echo dissolves into a signal and runs the warp: a gold path into the floor ahead and back up, an arc
	 * trailing it. Player position is never touched.
	 */
	private static void axonal(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 down = ctx.feet(), away = ctx.targetFeet(), up = ctx.target();
		Vec3[] path = {ctx.chest(), down, down.lerp(away, 0.5), away, up};
		for (int i = 0; i < path.length - 1; i++) {
			Vec3 from = path[i], to = path[i + 1];
			EchoScheduler.later(i * 2, () -> {
				for (int k = 0; k <= 6; k++)
					EchoFx.particles(HemoParticleData.glow(SIGNAL_GOLD), from.lerp(to, k / 6.0), 2, ctx.scaled(0.08), Vec3.ZERO);
				EchoFx.bolt(to, from.lerp(to, 0.4), 62F, 2.1F, 6, 6, 0.06F * ctx.figure(), 0.01F * ctx.figure());
			});
		}
		EchoScheduler.later(9, () -> EchoFx.glow(SIGNAL_GOLD, up, 10, ctx.scaled(0.4)));
	}
}
