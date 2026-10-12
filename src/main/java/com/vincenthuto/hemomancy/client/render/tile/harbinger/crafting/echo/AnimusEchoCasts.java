package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteRules;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.world.phys.Vec3;

/** Animus memories, each echoing its live cues: blood shots, bindings, riposte, avatar, clouds, rush, coronation. */
final class AnimusEchoCasts {
	private static final ParticleColor ANIMUS_RED = new ParticleColor(214, 18, 48);

	private AnimusEchoCasts() {
	}

	static void registerAll() {
		EchoCasts.register("blood_shot", ctx -> bloodShot(ctx, 0));
		EchoCasts.register("guided_blood_shot", ctx -> bloodShot(ctx, 1));
		EchoCasts.register("hematic_mortar", AnimusEchoCasts::mortar);
		EchoCasts.register("sanguine_halo", AnimusEchoCasts::halo);
		EchoCasts.register("blood_binding", ctx -> binding(ctx, 0, 30));
		EchoCasts.register("lingering_blood_binding", ctx -> binding(ctx, 0, 60));
		EchoCasts.register("chain_blood_binding", ctx -> binding(ctx, 2, 24));
		EchoCasts.register("blood_lattice", ctx -> binding(ctx, 3, 40));
		EchoCasts.register("hematic_riposte", AnimusEchoCasts::riposte);
		EchoCasts.register("summon_avatar", ctx -> avatar(ctx, 1));
		EchoCasts.register("summon_avatar_arms", ctx -> avatar(ctx, 2));
		EchoCasts.register("summon_avatar_armor", ctx -> avatar(ctx, 3));
		EchoCasts.register("summon_avatar_legs", ctx -> avatar(ctx, 4));
		EchoCasts.register("summon_avatar_complete", ctx -> avatar(ctx, 5));
		EchoCasts.register("blood_cloud", ctx -> cloud(ctx, 1.25, 1));
		EchoCasts.register("expansive_blood_cloud", ctx -> cloud(ctx, 2.4, 2));
		EchoCasts.register("pursuing_blood_cloud", ctx -> cloud(ctx, 2.0, 3));
		EchoCasts.register("sanguine_tempest", ctx -> cloud(ctx, 2.2, 4));
		EchoCasts.register("blood_rush", AnimusEchoCasts::rush);
		EchoCasts.register("blood_aneurysm", AnimusEchoCasts::aneurysm);
		EchoCasts.register("vital_effusion", AnimusEchoCasts::vitalEffusion);
		EchoCasts.register("conjure_blade", ctx -> conjure(ctx, Form.ANIMUS_CONJURE));
		EchoCasts.register("crimson_coronation", AnimusEchoCasts::coronation);
	}

	/** A blood shot (`form` 0 plain, 1 guided) from the hands to the target, splashing where it strikes. */
	private static void bloodShot(EchoCast.EchoContext ctx, int form) {
		EchoFx.activate(ctx);
		Vec3 from = ctx.hands(), to = ctx.target();
		EchoMoves.shot(ctx, from, to, 6, shot -> {
			if (form == 1) shot.setHomingTarget(null, 0);
		}, () -> {
			EchoFx.cue(Form.BLOOD_SPLASH, to, to.add(to.subtract(from).normalize()), ctx.scaled(0.9), 16, 1);
			EchoFx.impact(ctx, to, from);
		});
	}

	private static void mortar(EchoCast.EchoContext ctx) {
		Vec3 aim = ctx.target();
		EchoFx.chargeThen(Form.MORTAR_CHARGE, ctx, aim, 20, () -> {
			EchoFx.activate(ctx);
			Vec3 from = ctx.hands(), to = ctx.targetFeet();
			EchoMoves.shot(ctx, from, to, 10, shot -> shot.setMortar(true),
					() -> EchoFx.cue(Form.BLOOD_SPLASH, to, to.add(0, 1, 0), ctx.scaled(2.0), 16, 3));
		});
	}

	/** Five shots ringing the echo, then loosed one after another at the target. */
	private static void halo(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3[] ring = EchoMoves.ring(ctx, ctx.chest().add(0, ctx.scaled(0.4), 0), 0.9, 5, 0);
		for (int i = 0; i < ring.length; i++) {
			Vec3 from = ring[i];
			Vec3 to = ctx.target().add(ctx.side().scale(ctx.scaled((i - 2) * 0.3)));
			EchoScheduler.later(i * 4, () -> EchoMoves.shot(ctx, from, to, 6, shot -> {}, () -> {
				EchoFx.cue(Form.BLOOD_SPLASH, to, to.add(to.subtract(from).normalize()), ctx.scaled(0.7), 16, 1);
				EchoFx.impact(ctx, to, from);
			}));
		}
	}

	/** A tearing blood flow from the hands to the target, then chained on to `chain` more points. */
	private static void binding(EchoCast.EchoContext ctx, int chain, int ticks) {
		EchoFx.activate(ctx);
		Vec3 target = ctx.target();
		EchoFx.blood(BloodFlowPacket.Style.TEAR, ctx.hands(), target, ticks);
		Vec3[] onward = EchoMoves.ring(ctx, ctx.targetFeet().add(0, ctx.scaled(1.0), 0), 1.4, Math.max(chain, 1), 0.6);
		Vec3 previous = target;
		for (int i = 0; i < chain; i++) {
			Vec3 from = chain == 3 ? target : previous, to = onward[i];
			EchoScheduler.later(4 + i * 4, () -> EchoFx.blood(BloodFlowPacket.Style.TEAR, from, to, ticks));
			previous = to;
		}
	}

	/** The parry stance, a claw of blood across the blade, and the strike it returns. */
	private static void riposte(EchoCast.EchoContext ctx) {
		Vec3 feet = ctx.feet();
		EchoFx.cue(Form.RIPOSTE, feet, feet, ctx.scaled(1.0), 0, HematicRiposteRules.encodeSeed(1, 2));
		EchoFx.cue(Form.RIPOSTE, feet, feet, ctx.scaled(1.0), HematicRiposteRules.VISUAL_TICKS, HematicRiposteRules.encodeSeed(1, 2));
		EchoScheduler.later(6, () -> {
			Vec3 contact = ctx.ahead(0.9);
			EchoFx.cue(Form.RIPOSTE_STRIKE, contact, contact.add(ctx.forward()), ctx.scaled(1.0), 10, 1);
			EchoFx.claw(contact, ctx.forward(), ANIMUS_RED, false, 0.65F * ctx.figure());
			EchoFx.particles(HemoParticleData.bloodCell(ANIMUS_RED), contact, 6, ctx.scaled(0.3), Vec3.ZERO);
		});
	}

	/** The avatar takes shape in parts: arms, armour, legs, or the whole figure in conjured blood. */
	private static void avatar(EchoCast.EchoContext ctx, int part) {
		EchoFx.activate(ctx);
		Vec3 feet = ctx.feet();
		EchoFx.cue(Form.ANIMUS_CONJURE, feet, feet, ctx.scaled(part == 5 ? 1.0 : 0.6), 24, 1);
		double[] heights = switch (part) {
			case 2 -> new double[]{1.2, 1.3};
			case 3 -> new double[]{0.9, 1.3, 1.6};
			case 4 -> new double[]{0.3, 0.6};
			case 5 -> new double[]{0.3, 0.9, 1.4, 1.9};
			default -> new double[]{1.0};
		};
		for (int i = 0; i < heights.length; i++) {
			Vec3 at = feet.add(0, ctx.scaled(heights[i]), 0);
			EchoScheduler.later(i * 3, () -> {
				EchoFx.accent(EnumBloodTendency.ANIMUS, null, at, Vec3.ZERO, false);
				EchoFx.particles(HemoParticleData.bloodCell(ANIMUS_RED), at, 4, ctx.scaled(0.5), new Vec3(0, 0.01, 0));
			});
		}
	}

	/** A cloud of blood blooming over the target, its size and mode by variant. */
	private static void cloud(EchoCast.EchoContext ctx, double radius, int mode) {
		EchoFx.activate(ctx);
		Vec3 at = ctx.target();
		EchoFx.blood(BloodFlowPacket.Style.LIVING, ctx.hands(), at, 12);
		EchoScheduler.later(8, () -> EchoFx.cue(Form.CLOUD, at, at, ctx.scaled(radius), 50, mode));
	}

	private static void rush(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		EchoFx.cue(Form.RUSH, ctx.feet(), ctx.feet(), ctx.scaled(1.0), 20, 1);
		EchoFx.accent(EnumBloodTendency.ANIMUS, null, ctx.ahead(0.8), ctx.forward(), true);
	}

	private static void aneurysm(EchoCast.EchoContext ctx) {
		Vec3 target = ctx.target();
		EchoFx.chargeThen(Form.ANEURYSM_CHARGE, ctx, target, 24, () -> {
			EchoFx.activate(ctx);
			EchoFx.cue(Form.ANEURYSM_RUPTURE, target, ctx.chest(), ctx.scaled(2.4), 28, 1);
			EchoFx.impact(ctx, target, ctx.hands());
		});
	}

	/** Living blood coaxing growth from the floor: three sprouts before the echo. */
	private static void vitalEffusion(EchoCast.EchoContext ctx) {
		EchoFx.activate(ctx);
		Vec3 base = ctx.targetFeet();
		for (int i = -1; i <= 1; i++) {
			Vec3 ground = base.add(ctx.side().scale(ctx.scaled(i * 0.9)));
			EchoScheduler.later((i + 1) * 3, () -> EchoFx.cue(Form.GROWTH, ground, ground.add(0, ctx.scaled(0.5), 0), ctx.scaled(0.35), 24, 1));
		}
	}

	/** A living weapon form conjured in the echo's hands. */
	static void conjure(EchoCast.EchoContext ctx, Form form) {
		EchoFx.activate(ctx);
		EchoFx.cue(form, ctx.feet(), ctx.feet(), ctx.scaled(0.5), 20, 1);
	}

	/** The crown of swords gathers, then one sword is loosed at the target. */
	private static void coronation(EchoCast.EchoContext ctx) {
		Vec3 aim = ctx.target();
		EchoFx.chargeThen(Form.CROWN_CHARGE, ctx, aim, 28, () -> {
			EchoFx.activate(ctx);
			Vec3 feet = ctx.feet();
			EchoFx.cue(Form.CROWN, feet, feet, ctx.scaled(1.15), 0, 8);
			EchoFx.cue(Form.CROWN, feet, feet, ctx.scaled(1.15), 40, 8);
			EchoScheduler.later(12, () -> {
				Vec3 from = feet.add(0, ctx.scaled(3.05), 0), to = ctx.target();
				EchoMoves.needle(ctx, from, to, 6, needle -> needle.setCoronationSword(true), () -> {
					EchoFx.cue(Form.ANIMUS_IMPACT, to, to.add(to.subtract(from).normalize()), ctx.scaled(1.0), 16, 1);
					EchoFx.impact(ctx, to, from);
				});
			});
		});
	}
}
