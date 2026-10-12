package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.client.render.world.BlackVeilRenderer;
import com.vincenthuto.hemomancy.client.render.world.BloodFlowEffects;
import com.vincenthuto.hemomancy.client.render.world.ClawSlashRenderer;
import com.vincenthuto.hemomancy.client.render.world.FerricDuctilisEffects;
import com.vincenthuto.hemomancy.client.render.world.LuxUmbraEffects;
import com.vincenthuto.hemomancy.client.render.world.ManipulationAmbientParticles;
import com.vincenthuto.hemomancy.client.render.world.ManipulationVisualRenderer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationAccentPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationFlowPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.network.particle.NerveLinkPacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestBoltFactory;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Client-only cast replay. Each helper feeds a locally built packet into the same public client entry point the
 * live manipulation's server cue reaches, so an echo cast is the real geometry, sized and aimed by its caller.
 * Nothing here is sent anywhere.
 */
final class EchoFx {
	/** Ductilis bolts, as DuctilisLightningEffects colours them. */
	private static final int BOLT_OUTER = 0xE8FFE84A, BOLT_INNER = 0xFFFFFFFF;
	private static long seed = 0x5EED_10011L;

	private EchoFx() {
	}

	static long nextSeed() {
		seed = seed * 6364136223846793005L + 1442695040888963407L;
		return seed;
	}

	//  World cues (ManipulationVisualRenderer)

	static void cue(Form form, Vec3 from, Vec3 to, double radius, int ticks, int count) {
		ManipulationVisualRenderer.accept(new ManipulationVisualPacket(form, -1, from, to, (float) radius, ticks, count));
	}

	static void cue(Form form, Vec3 from, Vec3 to, double radius, int ticks) {
		cue(form, from, to, radius, ticks, 1);
	}

	static void burst(Form form, Vec3 at, double radius, int ticks) {
		cue(form, at, at, radius, ticks, 1);
	}

	/**
	 * Forms keyed by position (WELL, BEACON, ORE, CRUOR_SURFACE) refresh a live cue instead of replaying it, so a
	 * looped echo sends ticks = 0 first to retire the last loop's cue, then plays fresh.
	 */
	static void keyed(Form form, Vec3 from, Vec3 to, double radius, int ticks, int count) {
		cue(form, from, to, radius, 0, count);
		cue(form, from, to, radius, ticks, count);
	}

	/** A charge form gathering at the hands toward `aim`, `progress` 0..1, held for `ticks`. */
	static void charge(Form form, Vec3 hands, Vec3 aim, float progress, int ticks) {
		cue(form, hands, aim, progress, ticks, 1);
	}

	/** A charge that builds over `ticks` and releases into `then`. */
	static void chargeThen(Form form, EchoCast.EchoContext ctx, Vec3 aim, int ticks, Runnable then) {
		for (int t = 0; t <= ticks; t += 4) {
			float progress = Math.min(1f, t / (float) ticks);
			EchoScheduler.later(t, () -> charge(form, ctx.hands(), aim, progress, 6));
		}
		EchoScheduler.later(ticks, () -> {
			charge(form, ctx.hands(), aim, 1f, 0);
			then.run();
		});
	}

	//  Accents (ManipulationAmbientParticles)

	static void accent(EnumBloodTendency school, @Nullable EnumBloodTendency secondary, Vec3 at, Vec3 direction, boolean impact) {
		ManipulationAmbientParticles.accept(new ManipulationAccentPacket(school, secondary, at, direction, -1, impact));
	}

	/** The activation accent every live cast throws from the caster's chest. */
	static void activate(EchoCast.EchoContext ctx) {
		var manip = ctx.manipulation();
		accent(manip.getTend(), manip.getSecondaryTend(), ctx.chest(), Vec3.ZERO, false);
	}

	/** The impact accent a live hit throws at its target, along the blow. */
	static void impact(EchoCast.EchoContext ctx, Vec3 at, Vec3 from) {
		var manip = ctx.manipulation();
		Vec3 dir = at.subtract(from);
		accent(manip.getTend(), manip.getSecondaryTend(), at, dir.lengthSqr() < 1.0E-6D ? Vec3.ZERO : dir.normalize(), true);
	}

	//  Flows and links

	static void luxRelease(Vec3 from, Vec3 to) {
		release(from, to, false);
	}

	static void umbraRelease(Vec3 from, Vec3 to) {
		release(from, to, true);
	}

	private static void release(Vec3 from, Vec3 to, boolean umbra) {
		flow(umbra, from, to, HemomancyTendrilEffects.sutureConfig(from.distanceTo(to) + 4, nextSeed())
				.withLifecycle(2, 4, 6).withShape(12, 1, .055f, .2f));
	}

	static void flow(boolean umbra, Vec3 from, Vec3 to, TendrilEffectConfig config) {
		LuxUmbraEffects.accept(new ManipulationFlowPacket(umbra, new TendrilAnchor.Point(from), new TendrilAnchor.Point(to), config));
	}

	/** A blood flow between two points, configured as BloodFlowVisuals configures the live one. */
	static void blood(BloodFlowPacket.Style style, Vec3 from, Vec3 to, int ticks) {
		long key = nextSeed();
		var config = TendrilEffectConfig.defaults().withRange((float) (from.distanceTo(to) + 4))
				.withLifecycle(2, ticks, 8).withShape(12, 1, style == BloodFlowPacket.Style.COMMUNION ? .12f : .085f, .2f)
				.withBranching(0, 0, 0, 0).withWrithe(.08f, .05f, .4f, .04f).withFixedSeed(true, key).clamped();
		BloodFlowEffects.accept(new BloodFlowPacket(key, -1, style, new TendrilAnchor.Point(from), new TendrilAnchor.Point(to),
				config, false));
	}

	static void nerve(Vec3 from, Vec3 to, int ticks) {
		FerricDuctilisEffects.accept(new NerveLinkPacket(new TendrilAnchor.Point(from), new TendrilAnchor.Point(to), ticks));
	}

	//  Lightning (HutosLib BoltRenderer, as PacketSpawnLightningTest draws the BOLT backend)

	static void bolt(Vec3 from, Vec3 to, float ticksPerMeter, float speed, int maxAge, int fract, float maxOffset, float size) {
		long boltSeed = nextSeed();
		var config = new LightningTestConfig(LightningTestConfig.Backend.BOLT, BOLT_OUTER, BOLT_OUTER, BOLT_INNER,
				(float) Math.max(8.0D, from.distanceTo(to) + 4.0D), 0.0F, 0.0F, 0.0F, ticksPerMeter, speed, maxAge, fract,
				maxOffset, size, true, boltSeed, false, 20).clamped();
		BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(from, to, boltSeed, config.outerColor(), config.size(), config), 0.0F);
		BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(from, to, boltSeed, config.innerColor(),
				Math.max(0.01F, config.size() * 0.45F), config), 0.0F);
	}

	/** A jolt-weight bolt, struck again three ticks later as live Ductilis bolts are. */
	static void strike(Vec3 from, Vec3 to, float figure) {
		bolt(from, to, 72.0F, 2.4F, 6, 8, 0.09F * figure, 0.014F * figure);
		EchoScheduler.later(3, () -> bolt(from, to, 62.0F, 2.1F, 5, 7, 0.08F * figure, 0.012F * figure));
	}

	//  Other renderers

	static void claw(Vec3 centre, Vec3 forward, ParticleColor colour, boolean ambush, float scale) {
		ClawSlashRenderer.spawn(centre, forward, colour, ambush, scale, (int) nextSeed());
	}

	static void veil(Vec3 centre, float radius, int ticks) {
		BlackVeilRenderer.addVeil(centre, radius, ticks, (int) nextSeed());
	}

	static void particles(ParticleOptions particle, Vec3 at, int count, double spread, Vec3 drift) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		for (int i = 0; i < count; i++)
			level.addParticle(particle, at.x + (level.random.nextDouble() - 0.5D) * spread,
					at.y + (level.random.nextDouble() - 0.5D) * spread, at.z + (level.random.nextDouble() - 0.5D) * spread,
					drift.x, drift.y, drift.z);
	}

	static void glow(ParticleColor colour, Vec3 at, int count, double spread) {
		particles(HemoParticleData.glow(colour), at, count, spread, Vec3.ZERO);
	}
}
