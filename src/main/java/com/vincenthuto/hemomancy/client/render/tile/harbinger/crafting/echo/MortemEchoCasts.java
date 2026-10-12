package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.mortem.FruitingContagionRules;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import net.minecraft.world.phys.Vec3;

/** Mortem memories: tearing and extracting flows, rot, debt, the funeral bell and the bloom. */
final class MortemEchoCasts {
	private MortemEchoCasts() {
	}

	static void registerAll() {
		EchoCasts.register("conjure_axe", ctx -> AnimusEchoCasts.conjure(ctx, Form.MORTEM_CONJURE));
		EchoCasts.register("conjure_sickle", ctx -> AnimusEchoCasts.conjure(ctx, Form.MORTEM_CONJURE));
		EchoCasts.register("hemorrhage", MortemEchoCasts::hemorrhage);
		EchoCasts.register("lignum_mortis", ctx -> lignum(ctx, 3, 0.0));
		EchoCasts.register("canopy_mortis", ctx -> lignum(ctx, 4, 1.4));
		EchoCasts.register("worked_lignum", ctx -> lignum(ctx, 5, 0.6));
		EchoCasts.register("exsanguinate", MortemEchoCasts::exsanguinate);
		EchoCasts.register("insatiable_hunger", MortemEchoCasts::hunger);
		EchoCasts.register("grave_debt", MortemEchoCasts::graveDebt);
		EchoCasts.register("blackhearted", MortemEchoCasts::blackhearted);
		EchoCasts.register("funeral_bell", MortemEchoCasts::funeralBell);
		EchoCasts.register("carrion_communion", MortemEchoCasts::communion);
		EchoCasts.register("fruiting_contagion", MortemEchoCasts::contagion);
		EchoCasts.register("crimson_tithe", MortemEchoCasts::tithe);
		EchoCasts.register("bloom_of_rot", MortemEchoCasts::bloomOfRot);
	}

	private static void hemorrhage(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 target = ctx.target();
		EchoFx.blood(BloodFlowPacket.Style.TEAR, ctx.hands(), target, 18);
		EchoScheduler.later(6, () -> EchoFx.burst(Form.MORTEM_BURST, target, ctx.scaled(0.6), 18));
	}

	/** Lignum crawls blood into the floor and lifts it: flows to `count` points, raised by `lift` for the canopy. */
	private static void lignum(EchoCast.EchoContext ctx, int count, double lift) {
		EchoFx.activate(ctx);
		Vec3 base = ctx.targetFeet().add(0, ctx.scaled(lift), 0);
		for (int i = 0; i < count; i++) {
			double t = count == 1 ? 0 : i / (double) (count - 1) * 2 - 1;
			Vec3 point = base.add(ctx.side().scale(ctx.scaled(1.2 * t)));
			EchoScheduler.later(i * 3, () -> {
				EchoFx.blood(BloodFlowPacket.Style.LIGNUM, ctx.hands(), point, 10);
				EchoFx.accent(EnumBloodTendency.MORTEM, null, point, new Vec3(0, -0.15, 0), false);
			});
		}
	}

	private static void exsanguinate(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 target = ctx.target();
		EchoFx.impact(ctx, target, ctx.hands());
		EchoScheduler.later(4, () -> {
			EchoFx.blood(BloodFlowPacket.Style.EXTRACTION, target, ctx.chest(), 18);
			EchoFx.burst(Form.MORTEM_BURST, target, ctx.scaled(0.7), 24);
		});
	}

	private static void hunger(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 target = ctx.target();
		EchoFx.impact(ctx, target, ctx.hands());
		EchoFx.burst(Form.HUNGER, target, ctx.scaled(1.0), 24);
		EchoScheduler.later(14, () -> EchoFx.burst(Form.HUNGER_COLLAPSE, target, ctx.scaled(1.0), 18));
	}

	private static void graveDebt(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 target = ctx.target();
		EchoFx.burst(Form.GRAVE, target, ctx.scaled(1.0), 24);
		EchoScheduler.later(16, () -> {
			EchoFx.burst(Form.MORTEM_BURST, target, ctx.scaled(1.6), 24);
			EchoFx.blood(BloodFlowPacket.Style.EXTRACTION, target, ctx.chest(), 18);
			EchoFx.burst(Form.GRAVE_REFUND, ctx.chest(), ctx.scaled(1.0), 20);
		});
	}

	/** The black heart swells with necrotic saturation, then ruptures. */
	private static void blackhearted(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.BLACK_HEART, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 24, 1);
		EchoScheduler.later(18, () -> EchoFx.cue(Form.BLACKHEART_RUPTURE, ctx.feet().add(0, ctx.scaled(0.8), 0), ctx.feet(),
				ctx.scaled(1.2), 24, 1));
	}

	private static void funeralBell(EchoCast.EchoContext ctx) {
		EchoFx.chargeThen(Form.BELL_CHARGE, ctx, ctx.target(), 28, () -> {
			EchoFx.activate(ctx);
			Vec3 feet = ctx.feet();
			EchoFx.burst(Form.BELL, feet, ctx.scaled(2.6), 40);
			for (Vec3 tolled : EchoMoves.ring(ctx, feet.add(0, ctx.scaled(0.5), 0), 2.6, 3, 0.4))
				EchoScheduler.later(8, () -> EchoFx.cue(Form.MORTEM_BURST, tolled, feet, ctx.scaled(1.1), 20, 1));
		});
	}

	/** Afflicted bodies bleed into the echo along communion flows. */
	private static void communion(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		for (Vec3 source : EchoMoves.fan(ctx, 2, 1.1)) {
			EchoFx.blood(BloodFlowPacket.Style.COMMUNION, source, ctx.chest(), 24);
			EchoFx.burst(Form.COMMUNION, source, ctx.scaled(0.6), 22);
		}
	}

	/** A carrier fruits, then spreads its rot to a neighbour. */
	private static void contagion(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 carrier = ctx.targetFeet();
		Vec3 neighbour = carrier.add(ctx.side().scale(ctx.scaled(1.6)));
		EchoFx.cue(Form.CONTAGION, carrier, carrier, ctx.scaled(1.8), FruitingContagionRules.VISUAL_TICKS, FruitingContagionRules.FRUITING);
		EchoScheduler.later(FruitingContagionRules.BURST_END, () -> {
			EchoFx.cue(Form.CONTAGION, carrier, neighbour, ctx.scaled(1.8), FruitingContagionRules.VISUAL_TICKS, FruitingContagionRules.SPREADING);
			EchoFx.burst(Form.ROT_INFECTION, neighbour, ctx.scaled(1.0), 30);
		});
	}

	/** Blood borrowed against the body, then the debt collected. */
	private static void tithe(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.DEBT, ctx.feet(), ctx.feet(), ctx.scaled(2.0), 30, 3);
		EchoScheduler.later(24, () -> EchoFx.burst(Form.TITHE_COLLECT, ctx.feet(), ctx.scaled(1.0), 24));
	}

	private static void bloomOfRot(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.burst(Form.BLOOM, ctx.feet(), ctx.scaled(3.0), 36);
		EchoFx.burst(Form.ROT_INFECTION, ctx.feet(), ctx.scaled(1.0), 30);
	}
}
