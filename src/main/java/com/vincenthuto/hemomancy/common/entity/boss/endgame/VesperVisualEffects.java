package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Authored particle, tendril, and lightning vocabulary for both Vesper forms. */
final class VesperVisualEffects {
	static final ParticleColor BLOOD = new ParticleColor(224, 0, 24);
	static final ParticleColor DEEP_BLOOD = new ParticleColor(88, 0, 18);
	static final ParticleColor BLACK = new ParticleColor(3, 0, 4);
	static final ParticleColor ICE = new ParticleColor(92, 202, 245);
	static final ParticleColor WHITE = new ParticleColor(242, 247, 255);
	static final ParticleColor EMBER = new ParticleColor(255, 62, 18);
	/** The ordeal floor's blackstone, for torn-up flakes, and its lighter grit. */
	static final ParticleColor FLOOR_STONE = new ParticleColor(38, 33, 38);
	static final ParticleColor FLOOR_GRIT = new ParticleColor(74, 64, 70);

	private static final int BLOOD_CORE = 0xF8E00018;
	private static final int BLOOD_GLOW = 0xB86A0018;
	private static final int VOID_CORE = 0xF8040008;
	private static final int VOID_GLOW = 0xB865128F;
	private static final int ICE_CORE = 0xF0EAFBFF;
	private static final int ICE_GLOW = 0xA852BFE8;

	private VesperVisualEffects() { }

	static ParticleColor tendencyColor(EnumBloodTendency tendency) {
		return color(VesperVisualRules.tendencyColorRgb(tendency));
	}

	static void glow(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, HemoParticleData.glow(color), center, count, xSpread, ySpread, zSpread, speed);
	}

	static void darkGlow(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, HemoParticleData.darkGlow(color), center, count, xSpread, ySpread, zSpread, speed);
	}

	static void bloodCells(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, HemoParticleData.bloodCell(color), center, count, xSpread, ySpread, zSpread, speed);
	}

	static void embers(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed, float scale, int life) {
		send(level, HemoParticleData.ember(color, scale, 0.92F, life), center, count,
				xSpread, ySpread, zSpread, speed);
	}

	static void spores(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, HemoParticleData.sporiticSpore(color), center, count,
				xSpread, ySpread, zSpread, speed);
	}

	static void telegraphLine(ServerLevel level, Vec3 start, Vec3 end, ParticleColor color) {
		BossTelegraphs.line(level, start, end, color);
	}

	static void telegraphRing(ServerLevel level, Vec3 center, double radius, ParticleColor color, int points) {
		BossTelegraphs.ring(level, center, radius, color, points);
	}

	static void ambient(Level level, Vec3 center, double width, double height, boolean crowned,
			EnumBloodTendency tendency) {
		ParticleColor color = crowned ? BLOOD : tendencyColor(tendency);
		for (int i = 0; i < 3; i++) {
			double x = center.x + (level.random.nextDouble() - 0.5D) * width;
			double y = center.y + level.random.nextDouble() * height;
			double z = center.z + (level.random.nextDouble() - 0.5D) * width;
			ParticleOptions particle;
			if (crowned && i == 0) particle = HemoParticleData.sporiticSpore(DEEP_BLOOD);
			else if (i == 0) particle = HemoParticleData.glow(color);
			else particle = i % 2 == 0 ? HemoParticleData.bloodCell(color)
					: HemoParticleData.darkGlow(crowned ? BLACK : color);
			level.addParticle(particle, x, y, z, 0.0D, 0.008D, 0.0D);
		}
	}

	static void tendril(ServerLevel level, Vec3 start, Vec3 end, boolean glacial, long seed) {
		int core = glacial ? ICE_CORE : BLOOD_CORE;
		int glow = glacial ? ICE_GLOW : BLOOD_GLOW;
		float range = (float) Math.max(2.0D, start.distanceTo(end) + 2.0D);
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(core, glow)
				.withRange(range)
				.withLifecycle(3, 5, 8)
				.withShape(14, glacial ? 1 : 2, glacial ? 0.055F : 0.08F, 0.05F)
				.withBranching(glacial ? 1 : 3, 1, 0.24F, glacial ? 0.55F : 0.85F)
				.withWrithe(glacial ? 0.04F : 0.12F, 0.06F, glacial ? 0.22F : 0.72F, 0.05F)
				.withBlendColors(glacial)
				.withFixedSeed(true, seed);
		TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end), config);
	}

	static void voidTendril(ServerLevel level, Vec3 start, Vec3 end, long seed) {
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(VOID_CORE, VOID_GLOW)
				.withRange((float) Math.max(2.0D, start.distanceTo(end) + 2.0D))
				.withLifecycle(3, 5, 9)
				.withShape(16, 2, 0.09F, 0.05F)
				.withBranching(4, 2, 0.3F, 1.0F)
				.withWrithe(0.16F, 0.07F, 0.9F, 0.08F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
		TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end), config);
	}

	static void lightning(ServerLevel level, Vec3 start, Vec3 end, boolean glacial, long seed) {
		int outer = glacial ? 0xD8409DD8 : 0xE806020A;
		int inner = glacial ? 0xFFF0FBFF : 0xFFB80B18;
		LightningTestConfig config = new LightningTestConfig(LightningTestConfig.Backend.BOLT,
				outer, outer, inner, (float) Math.max(4.0D, start.distanceTo(end) + 4.0D),
				0.0F, 0.0F, 0.0F, 48.0F, 1.25F, 8, 7, 0.22F, 0.055F,
				true, seed, false, 20);
		LightningTesterSpawner.spawn(level, start, end, config);
	}

	/** A ground wedge: radial spokes plus the outer arc, so the struck sector reads before the swing. */
	static void telegraphCone(ServerLevel level, Vec3 origin, Vec3 facing, double radius, double degrees,
			ParticleColor color) {
		BossTelegraphs.cone(level, origin, facing, radius, degrees, color);
	}

	/** A pulsing floor sigil marking where something is about to burst up. */
	static void spawnMarker(ServerLevel level, Vec3 point, ParticleColor color, int ticksLeft) {
		double radius = 0.35D + 0.05D * ticksLeft;
		telegraphRing(level, point, radius, color, 10);
		level.sendParticles(HemoParticleData.glow(color), point.x, point.y + 0.1D, point.z,
				1, 0.05D, 0.02D, 0.05D, 0.0D);
	}

	/** Thread strands lowering from above onto a landing point, the puppet-descent tell. */
	static void descendingStrings(ServerLevel level, Vec3 point, ParticleColor color, float progress) {
		double top = point.y + 6.0D;
		double bottom = top - (top - point.y) * Mth.clamp(progress, 0.0F, 1.0F);
		for (int strand = 0; strand < 3; strand++) {
			double angle = Mth.TWO_PI * strand / 3.0D;
			double x = point.x + Math.cos(angle) * 0.35D;
			double z = point.z + Math.sin(angle) * 0.35D;
			for (double y = top; y >= bottom; y -= 0.6D) {
				level.sendParticles(HemoParticleData.darkGlow(color), x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
			level.sendParticles(HemoParticleData.bloodCell(color), x, bottom, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
		}
		telegraphRing(level, point, 0.6D, color, 8);
	}

	/**
	 * Broken floor flung up where the mount tears through the ground: flakes and grit in the ordeal floor's own
	 * blackstone tones, drawn as cells and glow so they sit with the blood and void layers above them.
	 */
	static void floorChips(ServerLevel level, Vec3 center, int count, double spread) {
		send(level, HemoParticleData.bloodCell(FLOOR_STONE), center.add(0.0D, 0.2D, 0.0D), count,
				spread, 0.2D, spread, 0.16D);
		send(level, HemoParticleData.glow(FLOOR_GRIT), center.add(0.0D, 0.1D, 0.0D), Math.max(1, count / 2),
				spread, 0.1D, spread, 0.05D);
		send(level, HemoParticleData.bloodCell(DEEP_BLOOD), center.add(0.0D, 0.15D, 0.0D), Math.max(1, count / 3),
				spread, 0.1D, spread, 0.06D);
		send(level, HemoParticleData.darkGlow(BLACK), center.add(0.0D, 0.1D, 0.0D), count, spread, 0.15D, spread, 0.08D);
	}

	/** A jagged crack line along the floor plane, used for tunnelling and scuttle lanes. */
	static void floorFissure(ServerLevel level, Vec3 from, Vec3 to, ParticleColor color, long seed) {
		Vec3 delta = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
		double length = delta.length();
		if (length < 0.05D) return;
		Vec3 dir = delta.scale(1.0D / length);
		Vec3 side = new Vec3(-dir.z, 0.0D, dir.x);
		int points = Math.max(2, (int) Math.ceil(length / 0.4D));
		for (int i = 0; i <= points; i++) {
			double jitter = Math.sin(seed * 0.37D + i * 1.91D) * 0.18D;
			Vec3 point = from.add(dir.scale(length * i / points)).add(side.scale(jitter));
			level.sendParticles(i % 3 == 0 ? HemoParticleData.bloodCell(color) : HemoParticleData.darkGlow(BLACK),
					point.x, point.y + 0.05D, point.z, 1, 0.03D, 0.01D, 0.03D, 0.0D);
		}
	}

	private static ParticleColor color(int rgb) {
		return new ParticleColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
	}

	private static void send(ServerLevel level, ParticleOptions particle, Vec3 center, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		level.sendParticles(particle, center.x, center.y, center.z, count, xSpread, ySpread, zSpread, speed);
	}
}
