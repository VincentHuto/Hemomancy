package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveSession;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * What a beat of the engram looks like where it happens. A lane threaded home flares round the loom's rim in its
 * ichor and sends a vessel up into the echo; a false lane tears loose in a black spray with a slash of its colour; a
 * refused step flinches red; pressure scarring flesh puffs necrotic soot; a recalled lie rises in smoke; a cut lane
 * sighs. Motes born on the ground start on the exact pixel of the block face beneath them. Hemomancy and HutosLib
 * particles only.
 */
public final class EngramBeatEffects {
	private static final ParticleColor SOOT = new ParticleColor(22, 4, 14);
	private static final ParticleColor ROT = new ParticleColor(52, 60, 30);
	private static final ParticleColor FLINCH = new ParticleColor(255, 46, 34);
	private static final ParticleColor BLOOD = new ParticleColor(150, 8, 18);
	private static final ParticleColor ASH = new ParticleColor(120, 110, 112);
	private static final ParticleColor CHAR = new ParticleColor(255, 92, 40);

	private EngramBeatEffects() {
	}

	public static void play(SomaticLoomBlockEntity te, EngramWeaveSession.Beat beat) {
		Level level = te.getLevel();
		EngramBoard board = te.getEngram();
		if (level == null || board == null) return;
		EngramSigilMesh mesh = EngramSigilMesh.of(te);
		RandomSource random = level.random;
		int fragment = beat.fragment();
		int colour = fragment >= 0 && fragment < board.fragmentCount() ? EngramWeaveRules.colour(board, fragment) : -1;
		if (colour < 0 && fragment >= 0 && fragment < board.fragmentCount()) colour = board.fragment(fragment).trueColour();
		ParticleColor ichor = EngramGlyphs.particleColour(colour);
		Vec3 at = nodeAt(te, mesh, beat.node(), 0.12D);
		switch (beat.event()) {
			case LAND -> {
				Vec3 heart = te.sigilCentre();
				double rim = te.sigilRadius() * 0.24D;
				for (int i = 0; i < 28; i++) {
					double angle = i / 28.0D * Math.PI * 2;
					Vec3 p = mesh.pixel(Math.sin(angle) * rim, -Math.cos(angle) * rim);
					Vec3 out = p.subtract(heart).multiply(1, 0, 1).normalize().scale(0.05D);
					level.addParticle(HemoParticleData.glow(ichor), p.x, p.y, p.z, out.x, 0.03D + random.nextDouble() * 0.03D, out.z);
				}
				for (int i = 0; i < 14; i++)
					level.addParticle(HemoParticleData.emberSpark(ichor, 0.08F, 0.9F, 22), at.x, at.y, at.z,
							(random.nextDouble() - 0.5D) * 0.12D, 0.06D + random.nextDouble() * 0.08D, (random.nextDouble() - 0.5D) * 0.12D);
				Vec3 echo = te.echoAnchor().add(0, 0.9D, 0);
				long seed = te.getBlockPos().asLong() * 131L + beat.serial();
				TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(heart.add(0, 0.2D, 0)),
						new TendrilAnchor.Point(echo), landConfig(ichor, seed), seed),
						Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
			}
			case SNAP -> {
				for (int i = 0; i < 26; i++)
					level.addParticle(HemoParticleData.darkGlow(SOOT), at.x, at.y + 0.3D, at.z, (random.nextDouble() - 0.5D) * 0.2D,
							random.nextDouble() * 0.12D, (random.nextDouble() - 0.5D) * 0.2D);
				for (int i = 0; i < 12; i++)
					level.addParticle(HemoParticleData.bloodCell(BLOOD), at.x, at.y + 0.4D, at.z, (random.nextDouble() - 0.5D) * 0.3D,
							0.12D + random.nextDouble() * 0.12D, (random.nextDouble() - 0.5D) * 0.3D);
				level.addParticle(HemoParticleData.bloodClaw(ichor), at.x, at.y + 0.6D, at.z, 0, 0, 0);
				for (int i = 0; i < 16; i++)
					level.addParticle(HemoParticleData.glow(FLINCH), at.x, at.y + 0.4D, at.z, random.nextGaussian() * 0.03D,
							random.nextDouble() * 0.04D, random.nextGaussian() * 0.03D);
				ring(te, mesh, beat.node(), FLINCH, 20, 0.45D, 0.02D);
			}
			case REFUSE -> ring(te, mesh, beat.node(), FLINCH, 16, 0.3D, 0.006D);
			case SCAR -> {
				for (int i = 0; i < 18; i++)
					level.addParticle(HemoParticleData.darkGlow(ROT), at.x, at.y, at.z, (random.nextDouble() - 0.5D) * 0.12D,
							random.nextDouble() * 0.08D, (random.nextDouble() - 0.5D) * 0.12D);
				for (int i = 0; i < 6; i++)
					level.addParticle(HemoParticleData.sporiticSpore(ROT), at.x, at.y + 0.2D, at.z, 0, 0.02D, 0);
			}
			case INTRUDE -> {
				for (int i = 0; i < 22; i++)
					level.addParticle(HemoParticleData.darkGlow(SOOT), at.x + (random.nextDouble() - 0.5D) * 0.4D,
							at.y + i * 0.06D, at.z + (random.nextDouble() - 0.5D) * 0.4D, 0, 0.03D, 0);
				for (int i = 0; i < 6; i++)
					level.addParticle(HemoParticleData.glow(ichor), at.x, at.y + 0.8D, at.z, 0, 0.02D, 0);
			}
			case CUT -> ring(te, mesh, beat.node(), ichor, 10, 0.18D, 0.004D);
		}
	}

	/**
	 * A broken engram coming apart, once a tick. As it begins each glyph crumbles into motes of its ichor and a little
	 * soot; while the lanes last they char and shed sparks; and ash lifts off the skin where it is rotting away, the
	 * rim first and then nearer the loom.
	 */
	public static void dissolving(SomaticLoomBlockEntity te, EngramBoard board, float progress, boolean first) {
		Level level = te.getLevel();
		if (level == null || board == null) return;
		EngramSigilMesh mesh = EngramSigilMesh.of(te);
		RandomSource random = level.random;
		double radius = te.sigilRadius();
		for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
			EngramBoard.Fragment f = board.fragment(fragment);
			boolean home = EngramWeaveRules.connected(board, fragment);
			ParticleColor ichor = f.intrusion() && f.premarked() ? ASH : EngramGlyphs.particleColour(EngramWeaveRules.colour(board, fragment));
			if (first) {
				Vec3 glyph = EngramGlyphs.glyphAt(te, mesh, f.node(), level.getGameTime(), fragment, home ? 1F : 0F);
				for (int i = 0; i < 12; i++)
					level.addParticle(HemoParticleData.glow(ichor), glyph.x + random.nextGaussian() * 0.08D, glyph.y + random.nextGaussian() * 0.08D,
							glyph.z + random.nextGaussian() * 0.08D, random.nextGaussian() * 0.012D, -0.004D - random.nextDouble() * 0.01D,
							random.nextGaussian() * 0.012D);
				for (int i = 0; i < 4; i++)
					level.addParticle(HemoParticleData.darkGlow(SOOT), glyph.x, glyph.y, glyph.z, random.nextGaussian() * 0.01D,
							0.01D + random.nextDouble() * 0.01D, random.nextGaussian() * 0.01D);
			}
			int[] lane = board.lane(fragment);
			if (progress < 0.6F && lane.length > 1 && random.nextInt(2) == 0) {
				int k = random.nextInt(lane.length - 1);
				if (lane[k + 1] >= EngramSigilGeometry.VESSEL) continue;
				double[] a = EngramSigilGeometry.unit(lane[k]), b = EngramSigilGeometry.unit(lane[k + 1]);
				double t = random.nextDouble();
				Vec3 at = mesh.pixel((a[0] + (b[0] - a[0]) * t) * radius, (a[1] + (b[1] - a[1]) * t) * radius);
				level.addParticle(HemoParticleData.emberSpark(random.nextBoolean() ? ichor : CHAR, 0.06F, 0.8F, 18), at.x, at.y, at.z,
						random.nextGaussian() * 0.006D, 0.02D + random.nextDouble() * 0.02D, random.nextGaussian() * 0.006D);
			}
		}
		// The skin rots away from its rim toward the loom: ash lifts where it is going now.
		double front = radius * 1.15D * (1D - progress);
		for (int i = 0; i < 4; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double r = Math.max(0.6D, front + random.nextGaussian() * 0.6D);
			Vec3 at = mesh.pixel(Math.sin(angle) * r, -Math.cos(angle) * r);
			level.addParticle(HemoParticleData.darkGlow(SOOT), at.x, at.y + 0.05D, at.z, random.nextGaussian() * 0.004D,
					0.012D + random.nextDouble() * 0.012D, random.nextGaussian() * 0.004D);
			if (random.nextInt(3) == 0)
				level.addParticle(HemoParticleData.glow(CHAR), at.x, at.y, at.z, 0, 0.008D, 0);
		}
	}

	/** A fragment heard: its glyph sheds a few motes of its memory. */
	public static void heard(SomaticLoomBlockEntity te, int fragment) {
		Level level = te.getLevel();
		EngramBoard board = te.getEngram();
		if (level == null || board == null || fragment < 0 || fragment >= board.fragmentCount()) return;
		EngramSigilMesh mesh = EngramSigilMesh.of(te);
		ParticleColor ichor = EngramGlyphs.particleColour(EngramWeaveRules.colour(board, fragment));
		Vec3 glyph = EngramGlyphs.glyphAt(te, mesh, board.fragment(fragment).node(), level.getGameTime(), fragment, 0F);
		for (int i = 0; i < 10; i++)
			level.addParticle(HemoParticleData.glow(ichor), glyph.x, glyph.y, glyph.z, (level.random.nextDouble() - 0.5D) * 0.08D,
					0.02D + level.random.nextDouble() * 0.04D, (level.random.nextDouble() - 0.5D) * 0.08D);
	}

	/** Fine motes on the ground pixels in a ring round a node (the loom's rim for the Vessel), starting outward. */
	private static void ring(SomaticLoomBlockEntity te, EngramSigilMesh mesh, int node, ParticleColor colour, int count,
			double radius, double speed) {
		double cx = 0, cz = 0;
		if (node >= 0 && node < EngramSigilGeometry.VESSEL) {
			double[] unit = EngramSigilGeometry.unit(node);
			cx = unit[0] * te.sigilRadius();
			cz = unit[1] * te.sigilRadius();
		} else {
			radius = te.sigilRadius() * 0.24D;
		}
		Vec3 centre = mesh.ground(cx, cz, 0);
		for (int i = 0; i < count; i++) {
			double angle = i / (double) count * Math.PI * 2;
			Vec3 p = mesh.pixel(cx + Math.cos(angle) * radius, cz + Math.sin(angle) * radius);
			Vec3 out = p.subtract(centre).multiply(1, 0, 1).normalize().scale(speed);
			te.getLevel().addParticle(HemoParticleData.glow(colour), p.x, p.y, p.z, out.x, 0.004D, out.z);
		}
	}

	private static Vec3 nodeAt(SomaticLoomBlockEntity te, EngramSigilMesh mesh, int node, double lift) {
		if (node < 0 || node >= EngramSigilGeometry.VESSEL) return te.sigilCentre().add(0, 0.4D, 0);
		double[] unit = EngramSigilGeometry.unit(node);
		return mesh.ground(unit[0] * te.sigilRadius(), unit[1] * te.sigilRadius(), lift);
	}

	private static TendrilEffectConfig landConfig(ParticleColor ichor, long seed) {
		int rgb = ichor.getColor() & 0xFFFFFF;
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0xF0000000 | rgb, 0xD0300610)
				.withBlendColors(true)
				.withLifecycle(12, 14, 14)
				.withShape(18, 1, 0.03F, 0.06F)
				.withBranching(1, 2, 0.12F, 0.4F)
				.withWrithe(0.05F, 0.12F, 0.08F, -0.2F)
				.withFixedSeed(true, seed);
	}
}
