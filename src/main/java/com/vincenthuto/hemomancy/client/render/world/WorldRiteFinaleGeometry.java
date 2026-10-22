package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A world rite's strike. Where the daemon slams into the ground, jagged cracks
 * tear out from the impact: a wide black char, a blazing red seam and a hot
 * core line, glowing then cooling to black. Where it strikes the sky, a red
 * bloom flashes, a shock ring spreads across the sky and the daemon's streak
 * fades below it.
 */
final class WorldRiteFinaleGeometry {
	private static final int CHAR = 0x140204;
	private static final int BLAZE = 0xFF2A14;
	private static final int HEAT = 0xFFC8A0;
	private static final int TINT = 0xFFFFFF;
	private static final ParticleColor EMBER = new ParticleColor(255, 60, 30);
	private static final ParticleColor SOOT = new ParticleColor(24, 4, 6);
	private static final ParticleColor BLOOD = new ParticleColor(150, 6, 16);
	private static final Set<Long> BURST = new HashSet<>();
	private static final RandomSource RANDOM = RandomSource.create();

	private WorldRiteFinaleGeometry() {
	}

	static void collect(PoseStack poses, LuxUmbraBatch batch, Vec3 camera, float partialTick) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		double time = level.getGameTime() + partialTick;
		for (CardinalRiteEventClientState.RiteEvent event : CardinalRiteEventClientState.all()) {
			if (event.points().isEmpty()) continue;
			float age = event.age(partialTick);
			if (event.kind() == CardinalRiteEventKind.GROUND_SLAMMED && age < WorldRiteFinaleRules.LIFETIME_TICKS) {
				cracks(poses, batch, camera, event, age, time);
			} else if (event.kind() == CardinalRiteEventKind.SKY_STRUCK && age < WorldRiteFinaleRules.FLASH_TICKS) {
				sky(poses, batch, camera, event, age);
			}
		}
	}

	private static void cracks(PoseStack poses, LuxUmbraBatch batch, Vec3 camera, CardinalRiteEventClientState.RiteEvent event,
			float age, double time) {
		Vec3 impact = event.points().getFirst();
		if (impact.distanceToSqr(camera) > 128.0D * 128.0D) return;
		float reach = WorldRiteFinaleRules.crackReach(age);
		float blaze = WorldRiteFinaleRules.blaze(age);
		float charred = WorldRiteFinaleRules.charred(age);
		poses.pushPose();
		poses.translate(impact.x - camera.x, impact.y - camera.y, impact.z - camera.z);
		Vec3 local = camera.subtract(impact);
		VertexConsumer black = batch.vertices(LuxUmbraBatch.Material.MORTEM);
		VertexConsumer molten = batch.vertices(LuxUmbraBatch.Material.MOLTEN);
		VertexConsumer blood = batch.vertices(LuxUmbraBatch.Material.ANIMUS);
		// The crater: scorched black with a red glow welling in it while it is hot.
		LuxUmbraGeometry.ground(poses, black, new Vec3(0, 0.012D, 0), 1.8D * reach, LuxUmbraGeometry.POOL,
				event.index(), CHAR, charred * 0.85F);
		LuxUmbraGeometry.ground(poses, blood, new Vec3(0, 0.02D, 0), 2.6D * reach, LuxUmbraGeometry.POOL,
				event.index() + 7, BLAZE, blaze * 0.55F);
		List<List<Vec3>> cracks = WorldRiteFinaleRules.cracks(event.index(), reachOf(event));
		for (int c = 0; c < cracks.size(); c++) {
			List<Vec3> crack = torn(cracks.get(c), reach);
			if (crack.size() < 2) continue;
			float flicker = 0.85F + 0.15F * (float) Math.sin(time * 1.3D + c * 1.7D + event.index());
			seam(poses, black, crack, 0.24D, 0.08D, 0.014D, event.index() + c, CHAR, charred * 0.9F);
			seam(poses, molten, crack, 0.12D, 0.035D, 0.022D, event.index() + c + 31, BLAZE, blaze * flicker);
			seam(poses, molten, crack, 0.04D, 0.012D, 0.028D, event.index() + c + 61, HEAT, blaze * blaze * flicker * 0.85F);
		}
		poses.popPose();
	}

	/** A crack lying flat on the ground, as strips joined end to end and narrowing toward its tip. */
	private static void seam(PoseStack poses, VertexConsumer vertices, List<Vec3> crack, double startWidth,
			double endWidth, double lift, int seed, int color, float alpha) {
		if (alpha <= 0.0F) return;
		for (int i = 0; i + 1 < crack.size(); i++) {
			Vec3 from = crack.get(i);
			Vec3 to = crack.get(i + 1);
			Vec3 along = to.subtract(from);
			double length = along.length();
			if (length < 1.0E-4D) continue;
			along = along.scale(1.0D / length);
			Vec3 side = new Vec3(-along.z, 0.0D, along.x);
			double width = startWidth + (endWidth - startWidth) * (i + 0.5D) / (crack.size() - 1);
			Vec3 middle = from.add(to).scale(0.5D).add(0.0D, lift, 0.0D);
			LuxUmbraGeometry.card(poses, vertices, middle, along, side, length / 2.0D + width * 0.5D, width,
					LuxUmbraGeometry.RIBBON, seed + i, color, alpha);
		}
	}

	/** The part of a crack that has torn open so far. */
	private static List<Vec3> torn(List<Vec3> crack, float reach) {
		double span = reach * (crack.size() - 1);
		List<Vec3> points = new ArrayList<>(crack.size());
		for (int i = 0; i < crack.size(); i++) {
			if (i <= span) {
				points.add(crack.get(i));
			} else {
				int last = (int) Math.floor(span);
				points.add(crack.get(last).lerp(crack.get(Math.min(last + 1, crack.size() - 1)), span - last));
				break;
			}
		}
		return points;
	}

	private static double reachOf(CardinalRiteEventClientState.RiteEvent event) {
		try {
			return Math.max(3.0D, Float.parseFloat(event.style()));
		} catch (NumberFormatException ignored) {
			return 6.0D;
		}
	}

	private static void sky(PoseStack poses, LuxUmbraBatch batch, Vec3 camera, CardinalRiteEventClientState.RiteEvent event,
			float age) {
		Vec3 strike = event.points().getFirst();
		float flash = WorldRiteFinaleRules.skyFlash(age);
		var view = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 right = new Vec3(view.getLeftVector()).scale(-1.0D);
		Vec3 up = new Vec3(view.getUpVector());
		poses.pushPose();
		poses.translate(strike.x - camera.x, strike.y - camera.y, strike.z - camera.z);
		Vec3 local = camera.subtract(strike);
		VertexConsumer blood = batch.vertices(LuxUmbraBatch.Material.ANIMUS);
		VertexConsumer molten = batch.vertices(LuxUmbraBatch.Material.MOLTEN);
		double swell = 1.0D + 0.6D * (1.0D - flash);
		// The bloom where the daemon broke against the sky, and its hot heart.
		LuxUmbraGeometry.card(poses, molten, Vec3.ZERO, right, up, 30.0D * swell, 30.0D * swell, LuxUmbraGeometry.FOCUS,
				event.index(), BLAZE, flash * 0.65F);
		LuxUmbraGeometry.card(poses, blood, Vec3.ZERO, right, up, 16.0D * swell, 16.0D * swell, LuxUmbraGeometry.FOCUS,
				event.index() + 1, TINT, flash);
		LuxUmbraGeometry.card(poses, molten, Vec3.ZERO, right, up, 5.0D, 5.0D, LuxUmbraGeometry.FOCUS,
				event.index() + 2, HEAT, flash);
		// The shock ring spreading across the sky.
		double radius = WorldRiteFinaleRules.ringRadius(age);
		float ringFade = (1.0F - HemoEasing.clamp01(age / WorldRiteFinaleRules.FLASH_TICKS));
		List<Vec3> ring = new ArrayList<>(49);
		for (int i = 0; i <= 48; i++) {
			double angle = i / 48.0D * Math.PI * 2.0D;
			ring.add(new Vec3(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius));
		}
		LuxUmbraGeometry.ribbonPath(poses, blood, ring, local, 1.4D, LuxUmbraGeometry.RIBBON, event.index() + 3, BLAZE,
				ringFade * ringFade * 0.8F);
		// The streak the daemon tore on its way up.
		LuxUmbraGeometry.ribbonPath(poses, blood, List.of(new Vec3(0, -40.0D, 0), new Vec3(0, -12.0D, 0), Vec3.ZERO),
				local, 0.25D, LuxUmbraGeometry.RIBBON, event.index() + 4, TINT, flash * 0.8F);
		poses.popPose();
	}

	/** Embers and soot off the cracks while they are hot, and a burst of debris at the slam. */
	static void tick() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			BURST.clear();
			return;
		}
		Set<Long> live = new HashSet<>();
		for (CardinalRiteEventClientState.RiteEvent event : CardinalRiteEventClientState.all()) {
			if (event.kind() != CardinalRiteEventKind.GROUND_SLAMMED || event.points().isEmpty()) continue;
			float age = event.age(0.0F);
			if (age >= WorldRiteFinaleRules.COOL_END_TICKS) continue;
			long key = event.center().asLong() * 31L + event.receivedTick();
			live.add(key);
			Vec3 impact = event.points().getFirst();
			List<List<Vec3>> cracks = WorldRiteFinaleRules.cracks(event.index(), reachOf(event));
			if (BURST.add(key)) {
				for (int i = 0; i < 40; i++) {
					double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
					double speed = 0.15D + RANDOM.nextDouble() * 0.25D;
					level.addParticle(HemoParticleData.bloodCell(i % 2 == 0 ? BLOOD : SOOT), impact.x, impact.y + 0.1D,
							impact.z, Math.cos(angle) * speed, 0.2D + RANDOM.nextDouble() * 0.3D, Math.sin(angle) * speed);
				}
				level.addParticle(HemoParticleData.hitGlow(EMBER), impact.x, impact.y + 0.3D, impact.z, 0.0D, 0.0D, 0.0D);
			}
			float blaze = WorldRiteFinaleRules.blaze(age);
			for (int i = 0; i < 3; i++) {
				if (RANDOM.nextFloat() > blaze) continue;
				List<Vec3> crack = cracks.get(RANDOM.nextInt(cracks.size()));
				Vec3 at = impact.add(crack.get(RANDOM.nextInt(crack.size())));
				level.addParticle(HemoParticleData.darkGlow(RANDOM.nextFloat() < 0.6F ? EMBER : SOOT), at.x, at.y + 0.05D,
						at.z, 0.0D, 0.03D + RANDOM.nextDouble() * 0.04D, 0.0D);
			}
		}
		BURST.retainAll(live);
	}
}
