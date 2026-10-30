package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.PossessionSceneClientState;
import com.vincenthuto.hemomancy.client.data.PossessionSceneClientState.Running;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.PossessionTendrils.Ring;
import com.vincenthuto.hemomancy.client.render.world.PossessionTendrils.Strand;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteVisualRules;
import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHaze;
import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Scene;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.*;

/**
 * The world side of possession's cinematics, for every scene {@link PossessionSceneClientState} is running: the floor's
 * hyphae tendrils, the rite's daemon as haze (it no longer has an entity once the rite is done), the goo a Gardens body
 * leaves, and the one-shot spores, cells, shatters and sounds on the scene's beats. Everything is timed from the
 * {@code PossessionSceneRules} tick table, so it lands with the server's steps and the camera's shots.
 */
public final class PossessionSceneRenderer {
	private static final ResourceLocation HYPHAE = Hemomancy.rloc("textures/entity/boss/endgame/mycophant_hyphae.png");
	private static final RenderType HYPHAE_CORE = RenderType.entityCutoutNoCull(HYPHAE);
	/** The rite floor's own tissue: dark crimson, the blood side of the Qliphoth. */
	private static final RenderType RITE_CORE = RenderType.entityCutoutNoCull(
			Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_tendril.png"));
	private static final int DAEMON_TINT = 0xB01420;
	private static final ParticleColor SPORE = new ParticleColor(214, 150, 52);
	private static final ParticleColor SPORE_PALE = new ParticleColor(236, 196, 110);
	private static final ParticleColor CELL = new ParticleColor(180, 10, 30);
	private static final ParticleColor CLOT = new ParticleColor(122, 16, 32);
	private static final ParticleColor SHADOW = new ParticleColor(58, 6, 14);
	private static final float AMBER_R = 1.0F, AMBER_G = 0.66F, AMBER_B = 0.16F;

	// The rings each scene raises, around the framed body (sizes in blocks, thickness in pixels); widths and heights
	// that depend on the body are filled in per frame.
	private static final int DEPARTURE_COUNT = 14;
	private static final int ARRIVAL_COUNT = 11;
	private static final int ERUPTION_COUNT = 12;
	private static final int LEAK_COUNT = 10;
	private static final int HYPHAE_COUNT = 14;

	/** The daemon at one moment of a scene: its centre, size, the shader's inputs and how far it has flowed. */
	private record DaemonPose(Vec3 centre, double scale, float emergence, float strength, float recall, float stream,
			float flow) {
	}

	private PossessionSceneRenderer() {
	}

	// ------------------------------------------------------------------ per tick: beats, spores and sounds

	public static void tick() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		for (Running running : PossessionSceneClientState.running()) {
			if (running.age() > running.scene().length()) continue;
			switch (running.scene()) {
				case DEPARTURE -> tickDeparture(level, running);
				case ARRIVAL -> tickArrival(level, running);
				case BROKEN_GARDENS -> tickBrokenGardens(level, running);
				case CLEAN_GARDENS -> tickCleanGardens(level, running);
				case BROKEN_RITE -> tickBrokenRite(level, running);
				case CLEAN_RITE -> tickCleanRite(level, running);
			}
		}
	}

	private static void tickDeparture(ClientLevel level, Running running) {
		int age = running.age();
		RandomSource random = level.random;
		Vec3 anchor = running.anchorPos(1.0F);
		float h = running.anchorHeight();
		Vec3 echo = running.echoPos();
		// The caster's body darkens into the echo, shedding shadow.
		if (age < DEPARTURE_FADE_END && age % 3 == 0) {
			level.addParticle(HemoParticleData.darkGlow(SHADOW), echo.x + (random.nextDouble() - 0.5D) * 0.6D,
					echo.y + random.nextDouble() * 1.8D, echo.z + (random.nextDouble() - 0.5D) * 0.6D, 0.0D, 0.03D, 0.0D);
		}
		if (age == DEPARTURE_DAEMON_START) {
			sound(level, daemonStart(running, echo.add(0.0D, 1.3D, 0.0D)), SoundInit.RITE_OFFERING_DRAWN.get(), 1.2F, 0.7F);
		}
		if (age > DEPARTURE_DAEMON_START && age < DEPARTURE_DAEMON_ARRIVES) {
			DaemonPose daemon = daemonAt(running, age, 1.0F);
			if (daemon != null) burst(level, HemoParticleData.daemonDiffuseGlow(0.35F), daemon.centre(), 1, 0.3D, 0.01D, 0.0D);
		}
		if (age == (DEPARTURE_DAEMON_START + DEPARTURE_DAEMON_ARRIVES) / 2) {
			// Passing through the echo.
			burst(level, HemoParticleData.darkGlow(SHADOW), echo.add(0.0D, 1.3D, 0.0D), 10, 0.3D, 0.05D, 0.0D);
		}
		if (age == DEPARTURE_DAEMON_ARRIVES) {
			sound(level, anchor, SoundInit.ENTITY_WILL_DISSOLVE.get(), 1.0F, 0.8F);
			burst(level, HemoParticleData.sporiticSpore(SPORE), anchor.add(0.0D, h * 0.6D, 0.0D), 20, 0.3D, 0.08D, 0.02D);
		}
		if (age > DEPARTURE_DAEMON_ARRIVES && age <= DEPARTURE_DISSOLVE_END) {
			// Body and daemon come apart into a rising spore cloud.
			float left = 1.0F - ramp(age, DEPARTURE_DAEMON_ARRIVES, DEPARTURE_DISSOLVE_END) * 0.6F;
			for (int i = 0; i < 4; i++) {
				Vec3 at = bodyPoint(random, anchor, running.anchorWidth(), h * left);
				level.addParticle(HemoParticleData.sporiticSpore(i == 0 ? SPORE_PALE : SPORE), at.x, at.y, at.z,
						(random.nextDouble() - 0.5D) * 0.03D, 0.02D + random.nextDouble() * 0.03D,
						(random.nextDouble() - 0.5D) * 0.03D);
			}
			burst(level, HemoParticleData.daemonDiffuseGlow(0.3F), anchor.add(0.0D, h * 0.6D * left, 0.0D), 1, 0.3D, 0.01D, 0.01D);
		}
		if (age == DEPARTURE_TENDRILS_RISE) {
			sound(level, anchor, SoundInit.RITE_SUBJECT_RISE.get(), 1.0F, 0.9F);
			ringMotes(level, anchor, 1.3D + running.anchorWidth(), 12);
		}
		if (age > DEPARTURE_DISSOLVE_END && age < DEPARTURE_TENDRILS_CLOSE) {
			// The spore mass hangs where the body was, the tendrils closing over it.
			Vec3 mass = anchor.add(0.0D, h * 0.45D, 0.0D);
			for (int i = 0; i < 3; i++) {
				Vec3 dir = scatter(random);
				Vec3 at = mass.add(dir.scale(0.5D));
				level.addParticle(HemoParticleData.sporiticSpore(SPORE), at.x, at.y, at.z, -dir.x * 0.03D,
						-dir.y * 0.03D, -dir.z * 0.03D);
			}
		}
		if (age == DEPARTURE_TENDRILS_CLOSE) {
			// The Quintessence in the Focus is spent as the tendrils take the mass.
			Vec3 medium = Vec3.atCenterOf(running.site()).add(0.0D, 0.6D, 0.0D);
			SanguineMonolithShatterRenderer.spawnShardPulse(medium, random);
			burst(level, HemoParticleData.bloodCell(CELL), medium, 18, 0.2D, 0.14D, 0.04D);
			burst(level, HemoParticleData.glow(CELL), medium, 8, 0.15D, 0.06D, 0.02D);
			sound(level, medium, SoundInit.RITE_ANCHOR_BREAK.get(), 1.0F, 0.8F);
			sound(level, anchor, SoundInit.RITE_SUBJECT_SINK.get(), 1.0F, 0.85F);
		}
		if (age > DEPARTURE_TENDRILS_CLOSE && age < DEPARTURE_BLACK) {
			for (int i = 0; i < 2; i++) {
				Vec3 at = bodyPoint(random, anchor, 1.2F, 0.4F);
				level.addParticle(HemoParticleData.sporiticSpore(SPORE), at.x, at.y, at.z,
						(anchor.x - at.x) * 0.08D, -0.02D, (anchor.z - at.z) * 0.08D);
			}
		}
	}

	private static void tickArrival(ClientLevel level, Running running) {
		int age = running.age();
		RandomSource random = level.random;
		Vec3 anchor = running.anchorPos(1.0F);
		float h = running.anchorHeight();
		if (age == 3) sound(level, anchor, SoundInit.ENTITY_MYCOPHANT_SUMMON.get(), 0.8F, 1.2F);
		if (age < ARRIVAL_SEIZE_END && age % 2 == 0) ringMotes(level, anchor, 1.1D + running.anchorWidth(), 1);
		if (age == ARRIVAL_SEIZE_END) sound(level, anchor, SoundInit.BLOOD_VIAL_INJECT.get(), 1.2F, 0.6F);
		if (age > ARRIVAL_SEIZE_END && age < ARRIVAL_INJECT_END) {
			// Spores and haze pour along the tendrils into the body.
			List<Strand> strands = new ArrayList<>();
			strands(strands, running, age, age);
			strands.removeIf(strand -> !strand.main());
			if (!strands.isEmpty()) {
				int last = PossessionTendrils.STATIONS - 1;
				for (int i = 0; i < 3; i++) {
					Strand strand = strands.get(random.nextInt(strands.size()));
					int station = 2 + random.nextInt(last - 4);
					double vx = (strand.x(last) - strand.x(station)) / 10.0D;
					double vy = (strand.y(last) - strand.y(station)) / 10.0D;
					double vz = (strand.z(last) - strand.z(station)) / 10.0D;
					ParticleOptions options = i == 0 && age % 2 == 0 ? HemoParticleData.daemonDiffuseGlow(0.25F)
							: HemoParticleData.sporiticSpore(SPORE);
					level.addParticle(options, anchor.x + strand.x(station), anchor.y + strand.y(station),
							anchor.z + strand.z(station), vx, vy, vz);
				}
			}
		}
		if (age == ARRIVAL_INJECT_END) sound(level, anchor, SoundInit.RITE_SUBJECT_SINK.get(), 0.9F, 1.3F);
		if (age == ARRIVAL_RELEASE_END) {
			burst(level, HemoParticleData.sporiticSpore(SPORE_PALE), anchor.add(0.0D, h * 0.6D, 0.0D), 8, 0.3D, 0.04D, 0.01D);
		}
	}

	private static void tickBrokenGardens(ClientLevel level, Running running) {
		int age = running.age();
		RandomSource random = level.random;
		Vec3 anchor = running.anchorPos(1.0F);
		float h = running.anchorHeight();
		if (age == BROKEN_BURST) {
			Vec3 chest = anchor.add(0.0D, h * 0.5D, 0.0D);
			sound(level, chest, SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_DEATH.get(), 1.2F, 0.9F);
			sound(level, chest, SoundInit.ENTITY_HEMOJELLY_DEATH.get(), 1.0F, 0.6F);
			burst(level, HemoParticleData.sporiticSpore(SPORE), chest, 36, 0.4D, 0.18D, 0.04D);
			burst(level, HemoParticleData.sporiticSpore(SPORE_PALE), chest, 10, 0.3D, 0.1D, 0.03D);
			burst(level, HemoParticleData.bloodCell(CELL), chest, 20, 0.3D, 0.22D, 0.05D);
			burst(level, HemoParticleData.glow(SPORE), chest, 6, 0.2D, 0.05D, 0.0D);
		}
		if (age > BROKEN_BURST && age < BROKEN_REABSORB_END) gooSpores(level, random, anchor, 2);
	}

	private static void tickCleanGardens(ClientLevel level, Running running) {
		int age = running.age();
		RandomSource random = level.random;
		Vec3 anchor = running.anchorPos(1.0F);
		float h = running.anchorHeight();
		if (age < CLEAN_STAMMER_END && age % 2 == 0) {
			Vec3 at = bodyPoint(random, anchor, running.anchorWidth(), h);
			level.addParticle(HemoParticleData.sporiticSpore(SPORE), at.x, at.y, at.z, 0.0D, 0.015D, 0.0D);
		}
		if (age == CLEAN_STAMMER_END) sound(level, anchor, SoundInit.RITE_SUBJECT_SINK.get(), 0.8F, 0.8F);
		if (age == CLEAN_COLLAPSE_END) {
			sound(level, anchor, SoundInit.ENTITY_HEMOJELLY_DEATH.get(), 1.0F, 0.7F);
			burst(level, HemoParticleData.sporiticSpore(SPORE), anchor.add(0.0D, 0.3D, 0.0D), 14, 0.4D, 0.06D, 0.02D);
			burst(level, HemoParticleData.bloodCell(CELL), anchor.add(0.0D, 0.2D, 0.0D), 8, 0.3D, 0.08D, 0.03D);
		}
		if (age > CLEAN_COLLAPSE_END && age % 2 == 0) gooSpores(level, random, anchor, 1);
	}

	private static void tickBrokenRite(ClientLevel level, Running running) {
		int age = running.age();
		RandomSource random = level.random;
		Vec3 anchor = running.anchorPos(1.0F);
		float h = running.anchorHeight();
		if (age == 8) sound(level, anchor, SoundInit.RITE_SUBJECT_RISE.get(), 1.1F, 1.0F);
		if (age > 8 && age < BROKEN_ERUPT_END) ringMotes(level, anchor, 1.2D + running.anchorWidth(), 1);
		if (age == BROKEN_BLAST) {
			// The tendrils burst open and fling the daemon out of the spores.
			Vec3 top = anchor.add(0.0D, h + 0.9D, 0.0D);
			SanguineMonolithShatterRenderer.spawnBurst(top, random);
			burst(level, HemoParticleData.sporiticSpore(SPORE), top, 40, 0.4D, 0.2D, 0.03D);
			burst(level, HemoParticleData.daemonDiffuseGlow(0.45F), top, 6, 0.3D, 0.05D, 0.0D);
			sound(level, top, SoundInit.RITE_BACKLASH.get(), 1.2F, 0.9F);
			sound(level, top, SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_DEATH.get(), 1.0F, 1.1F);
		}
		if (age > BROKEN_BLAST && age < BROKEN_SLAM) {
			DaemonPose daemon = daemonAt(running, age, 1.0F);
			if (daemon != null) burst(level, HemoParticleData.daemonDiffuseGlow(0.3F), daemon.centre(), 1, 0.3D, 0.01D, 0.0D);
		}
		if (age == BROKEN_BLAST + 6) sound(level, anchor, SoundInit.RITE_SUBJECT_SINK.get(), 0.9F, 1.0F);
		if (age == BROKEN_SLAM) {
			Vec3 chest = anchor.add(0.0D, h * 0.6D, 0.0D);
			sound(level, chest, SoundInit.RITE_SUBJECT_THUD.get(), 1.2F, 1.1F);
			burst(level, HemoParticleData.bloodCell(CELL), chest, 14, 0.25D, 0.16D, 0.02D);
			burst(level, HemoParticleData.darkGlow(SHADOW), chest, 10, 0.3D, 0.08D, 0.0D);
		}
	}

	private static void tickCleanRite(ClientLevel level, Running running) {
		int age = running.age();
		RandomSource random = level.random;
		Vec3 anchor = running.anchorPos(1.0F);
		Vec3 gather = gatherPoint(running, 1.0F);
		if (age == 2) sound(level, anchor, SoundInit.RITE_SUBJECT_RISE.get(), 0.8F, 1.1F);
		if (age >= CLEAN_TENDRILS_RISE_END && age < CLEAN_REFORM_END) {
			// Spores leak off the tendril tips and drift up to gather over the echo.
			List<Strand> strands = new ArrayList<>();
			strands(strands, running, age, age);
			// The main strands' tips, not the branches': the spores leak toward the echo from the ring.
			strands.removeIf(strand -> !strand.main());
			int last = PossessionTendrils.STATIONS - 1;
			for (int i = 0; i < 2 && !strands.isEmpty(); i++) {
				Strand strand = strands.get(random.nextInt(strands.size()));
				Vec3 tip = anchor.add(strand.x(last), strand.y(last), strand.z(last));
				Vec3 drift = gather.subtract(tip).scale(1.0D / 14.0D);
				level.addParticle(HemoParticleData.sporiticSpore(i == 0 ? SPORE : SPORE_PALE), tip.x, tip.y, tip.z,
						drift.x, drift.y, drift.z);
			}
			if (age % 2 == 0) {
				float reform = ramp(age, CLEAN_TENDRILS_RISE_END, CLEAN_REFORM_END);
				burst(level, HemoParticleData.daemonDiffuseGlow(0.15F + 0.3F * reform), gather, 1, 0.4D, 0.01D, 0.0D);
			}
		}
		if (age == CLEAN_REFORM_END - 6) sound(level, gather, SoundInit.ENTITY_WILL_MATERIALIZE.get(), 1.0F, 0.9F);
		if (age == CLEAN_DRIFT_END - 4) sound(level, anchor, SoundInit.RITE_SUBJECT_SINK.get(), 0.7F, 1.1F);
		if (age == CLEAN_DRIFT_END) sound(level, anchor, SoundInit.ENTITY_WILL_DISSOLVE.get(), 0.7F, 1.2F);
	}

	// ------------------------------------------------------------------ per frame: tendrils, goo, haze

	public static void render(PoseStack poseStack, float partialTick) {
		Collection<Running> all = PossessionSceneClientState.running();
		if (all.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		float poolTime = (float) (AnimationTime.gameTime(level, (double) partialTick) % 24000.0D);
		List<Strand> strands = new ArrayList<>();
		for (Running running : all) {
			float t = running.time(partialTick);
			if (t > running.scene().length() + 8) continue;
			queueDaemon(running, t, partialTick);
			Vec3 anchor = running.anchorPos(partialTick);
			poseStack.pushPose();
			poseStack.translate(anchor.x - camera.x, anchor.y - camera.y, anchor.z - camera.z);
			strands.clear();
			strands(strands, running, t, t);
			if (!strands.isEmpty()) {
				int light = LevelRenderer.getLightColor(level, BlockPos.containing(anchor.x, anchor.y + 0.5D, anchor.z));
				PoseStack.Pose pose = poseStack.last();
				// Fetch each buffer as it is used: asking for another render type closes the last one.
				RenderType core = running.scene().atRite() ? RITE_CORE : HYPHAE_CORE;
				for (Strand strand : strands) PossessionTendrils.core(pose, buffers.getBuffer(core), strand, light);
				buffers.endBatch(core);
				for (Strand strand : strands) {
					PossessionTendrils.glow(pose, buffers.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), strand, AMBER_R,
							AMBER_G, AMBER_B);
				}
			}
			pool(running, t, poolTime, poseStack, buffers);
			halo(running, t, poseStack, buffers);
			poseStack.popPose();
		}
		buffers.endBatch(HLRenderTypeInit.TENDRIL_GLOW);
	}

	public static void clear() {
		// Nothing outlives the running scenes: the shards and particles retire on their own.
	}

	/** The tendrils a scene has up at scene time {@code t}; {@code time} drives their writhe. */
	private static void strands(List<Strand> out, Running running, float t, float time) {
		float h = running.anchorHeight();
		float w = running.anchorWidth();
		long seed = running.seed();
		switch (running.scene()) {
			case DEPARTURE -> {
				if (t < DEPARTURE_TENDRILS_RISE || t > DEPARTURE_BLACK + 2) return;
				// Up out of the floor and over the spore mass, then down into the ground with it.
				float growth = HemoEasing.easeOutCubic(ramp(t, DEPARTURE_TENDRILS_RISE, DEPARTURE_TENDRILS_CLOSE));
				float sink = HemoEasing.easeInCubic(ramp(t, DEPARTURE_TENDRILS_CLOSE, DEPARTURE_BLACK)) * (h * 1.3F + 1.2F);
				float flash = flash(t, DEPARTURE_TENDRILS_CLOSE, 6.0F);
				Ring ring = new Ring(DEPARTURE_COUNT, 1.3F + w, h * 1.1F + 0.6F, h * 0.45F, 0.12F, 0.8F,
						0.12F * (1.0F - 0.6F * growth), 1.3F, 0.3F);
				PossessionTendrils.ring(out, ring, seed, growth, sink, 0.22F + 0.3F * flash,
						((t - DEPARTURE_TENDRILS_RISE) * 0.08F) % 1.0F, time);
			}
			case ARRIVAL -> {
				if (t > ARRIVAL_RELEASE_END + 6) return;
				// They seize the facsimile at the sides, pulse while they fill it, then let go and sink.
				float release = HemoEasing.smoothstep(ramp(t, ARRIVAL_INJECT_END, ARRIVAL_RELEASE_END));
				float growth = HemoEasing.easeOutCubic(ramp(t, 2.0F, ARRIVAL_SEIZE_END)) * (1.0F - 0.35F * release);
				float sink = HemoEasing.easeInCubic(ramp(t, ARRIVAL_INJECT_END + 2, ARRIVAL_RELEASE_END + 6)) * (h + 0.8F);
				boolean injecting = t > ARRIVAL_SEIZE_END && t < ARRIVAL_INJECT_END;
				Ring ring = new Ring(ARRIVAL_COUNT, 1.1F + w, h * 0.7F + 0.3F, h * 0.55F, w * 0.5F + 0.05F + release * 0.6F,
						0.35F, 0.08F + 0.1F * release, 1.1F, 0.3F);
				PossessionTendrils.ring(out, ring, seed, growth, sink, injecting ? 0.4F : 0.18F,
						injecting ? ((t - ARRIVAL_SEIZE_END) * 0.09F) % 1.0F : -1.0F, time);
			}
			case BROKEN_GARDENS -> {
				if (t < BROKEN_BURST + 3 || t > BROKEN_REABSORB_END + 4) return;
				// Hyphae creep in over the ground to the spill and drink it back down.
				float growth = HemoEasing.smoothstep(ramp(t, BROKEN_BURST + 3, BROKEN_BURST + 14));
				float done = ramp(t, BROKEN_REABSORB_END - 4, BROKEN_REABSORB_END + 4);
				hyphae(out, seed, gooRadius(running, t), growth, done * 0.4F, 0.3F * (1.0F - done), t, time, 0.1F);
			}
			case CLEAN_GARDENS -> {
				int end = running.scene().length();
				if (t < CLEAN_COLLAPSE_END + 4 || t > end + 4) return;
				float growth = HemoEasing.smoothstep(ramp(t, CLEAN_COLLAPSE_END + 4, CLEAN_COLLAPSE_END + 18));
				float done = ramp(t, end - 4, end + 4);
				hyphae(out, seed, gooRadius(running, t), growth, done * 0.4F, 0.26F * (1.0F - done), t, time, 0.06F);
			}
			case BROKEN_RITE -> {
				if (t < 8.0F || t > BROKEN_REMANIFEST_END + 4) return;
				// A cage over the echo that bursts open at the blast, then goes back into the floor.
				float growth = HemoEasing.easeOutCubic(ramp(t, 8.0F, BROKEN_ERUPT_END));
				float open = HemoEasing.smoothstep(ramp(t, BROKEN_BLAST, BROKEN_BLAST + 4));
				float sink = HemoEasing.easeInCubic(ramp(t, BROKEN_BLAST + 6, BROKEN_REMANIFEST_END)) * (h + 2.4F);
				float radius = 1.2F + w;
				Ring ring = new Ring(ERUPTION_COUNT, radius, h + 0.8F, h + 0.9F + 0.7F * open,
						Mth.lerp(open, 0.15F, radius * 1.1F), 0.6F * (1.0F - open) + 0.2F, 0.1F + 0.25F * open, 1.3F, 0.3F);
				PossessionTendrils.ring(out, ring, seed, growth, sink, 0.2F + 0.5F * flash(t, BROKEN_BLAST, 6.0F),
						t < BROKEN_BLAST ? ((t - 8.0F) * 0.1F) % 1.0F : -1.0F, time);
			}
			case CLEAN_RITE -> {
				if (t < 2.0F || t > CLEAN_REMANIFEST_END + 4) return;
				// Standing round the echo, leaning in, leaking spores from their tips.
				float growth = HemoEasing.easeOutCubic(ramp(t, 2.0F, CLEAN_TENDRILS_RISE_END));
				float sink = HemoEasing.easeInCubic(ramp(t, CLEAN_DRIFT_END - 4, CLEAN_REMANIFEST_END)) * (h + 1.8F);
				boolean leaking = t >= CLEAN_TENDRILS_RISE_END && t < CLEAN_REFORM_END;
				Ring ring = new Ring(LEAK_COUNT, 1.3F + w, h * 0.9F + 0.4F, h + 0.4F, 0.75F, 0.25F, 0.1F, 1.1F, 0.25F);
				PossessionTendrils.ring(out, ring, seed, growth, sink, 0.2F + 0.06F * Mth.sin(t * 0.3F),
						leaking ? (t * 0.07F) % 1.0F : -1.0F, time);
			}
		}
	}

	/** Thin hyphae creeping over the ground from a wide ring to the goo's edge, a pulse carrying it outward. */
	private static void hyphae(List<Strand> out, long seed, float goo, float growth, float sink, float glow, float t,
			float time, float pulseSpeed) {
		Ring ring = new Ring(HYPHAE_COUNT, 2.6F, 0.3F, 0.05F, goo * 0.4F + 0.1F, 0.12F, 0.15F, 0.7F, 0.2F);
		PossessionTendrils.ring(out, ring, seed + 17L, growth, sink, glow, 1.0F - (t * pulseSpeed) % 1.0F, time);
	}

	/** The goo pool's radius at scene time {@code t}, or 0 when there is none. */
	private static float gooRadius(Running running, float t) {
		return switch (running.scene()) {
			case BROKEN_GARDENS -> {
				if (t < BROKEN_BURST) yield 0.0F;
				float spread = HemoEasing.easeOutCubic(ramp(t, BROKEN_BURST, BROKEN_BURST + 5));
				float shrink = HemoEasing.smoothstep(ramp(t, BROKEN_BURST + 6, BROKEN_REABSORB_END));
				yield 1.5F * spread * (1.0F - 0.92F * shrink);
			}
			case CLEAN_GARDENS -> {
				if (t < CLEAN_COLLAPSE_END - 2) yield 0.0F;
				float spread = HemoEasing.easeOutCubic(ramp(t, CLEAN_COLLAPSE_END - 2, CLEAN_COLLAPSE_END + 8));
				float shrink = HemoEasing.smoothstep(ramp(t, CLEAN_COLLAPSE_END + 8, running.scene().length() + 4));
				yield 1.3F * spread * (1.0F - 0.8F * shrink);
			}
			default -> 0.0F;
		};
	}

	/** The amber-veined blood the body leaves, the infected pool the Spawn Mother's nectar uses, under a warm glow. */
	private static void pool(Running running, float t, float poolTime, PoseStack poseStack,
			MultiBufferSource.BufferSource buffers) {
		float radius = gooRadius(running, t);
		if (radius < 0.05F) return;
		int end = running.scene() == Scene.BROKEN_GARDENS ? BROKEN_REABSORB_END : running.scene().length() + 2;
		float presence = 1.0F - ramp(t, end - 3, end + 3);
		if (presence <= 0.0F) return;
		float wobble = 1.0F + 0.03F * Mth.sin(t * 0.21F);
		RenderType type = HemoRenderTypes.infectedPool(poolTime, Math.floorMod(running.seed(), 1000L));
		BloodPoolSurface.draw(poseStack.last().pose(), buffers.getBuffer(type), 0.0F, 0.02F, 0.0F, radius * 1.25F * wobble,
				0, 90, 0, Math.round(236 * presence));
		buffers.endBatch(type);
		float beat = 0.5F + 0.5F * Mth.sin(t * 0.18F);
		PossessionTendrils.floorHalo(poseStack, buffers.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), 0.028F, radius * 2.2F,
				AMBER_R, AMBER_G, AMBER_B, (0.2F + 0.1F * beat) * presence);
	}

	/** The amber glow on the floor where the ring grips it. */
	private static void halo(Running running, float t, PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
		float w = running.anchorWidth();
		float size;
		float alpha;
		switch (running.scene()) {
			case DEPARTURE -> {
				size = 2.0F * (1.3F + w) + 1.0F;
				alpha = 0.3F * ramp(t, DEPARTURE_TENDRILS_RISE - 4, DEPARTURE_TENDRILS_RISE + 6)
						* (1.0F - ramp(t, DEPARTURE_TENDRILS_CLOSE + 2, DEPARTURE_BLACK)) + 0.25F * flash(t, DEPARTURE_TENDRILS_CLOSE, 8.0F);
			}
			case ARRIVAL -> {
				size = 2.0F * (1.1F + w) + 0.8F;
				float pour = t > ARRIVAL_SEIZE_END && t < ARRIVAL_INJECT_END ? 0.1F + 0.06F * Mth.sin(t * 0.5F) : 0.0F;
				alpha = (0.25F * ramp(t, 0.0F, 10.0F) + pour) * (1.0F - ramp(t, ARRIVAL_RELEASE_END, ARRIVAL_RELEASE_END + 6));
			}
			case BROKEN_RITE -> {
				size = 2.0F * (1.2F + w) + 1.0F;
				alpha = 0.28F * ramp(t, 6.0F, BROKEN_ERUPT_END) * (1.0F - ramp(t, BROKEN_SLAM, BROKEN_REMANIFEST_END))
						+ 0.4F * flash(t, BROKEN_BLAST, 8.0F);
			}
			case CLEAN_RITE -> {
				size = 2.0F * (1.3F + w) + 0.8F;
				alpha = 0.22F * ramp(t, 2.0F, CLEAN_TENDRILS_RISE_END) * (1.0F - ramp(t, CLEAN_DRIFT_END, CLEAN_REMANIFEST_END));
			}
			default -> {
				return;
			}
		}
		PossessionTendrils.floorHalo(poseStack, buffers.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), 0.03F, size, AMBER_R,
				AMBER_G, AMBER_B, alpha);
	}

	// ------------------------------------------------------------------ the daemon

	private static void queueDaemon(Running running, float t, float partialTick) {
		DaemonPose now = daemonAt(running, t, partialTick);
		if (now == null || now.scale() <= 0.01D || now.strength() <= 0.002F) return;
		DaemonPose before = daemonAt(running, t - 1.0F, partialTick);
		Vec3 motion = before == null ? Vec3.ZERO : now.centre().subtract(before.centre());
		double speed = motion.length();
		// Upright while it hangs, swinging over to lie along its path, head first, as it streams.
		double crownX = 0.0D, crownY = 1.0D, crownZ = 0.0D;
		if (speed > 1.0E-4D) {
			crownX = motion.x / speed * now.stream();
			crownY = (1.0D - now.stream()) + motion.y / speed * now.stream();
			crownZ = motion.z / speed * now.stream();
			double length = Math.sqrt(crownX * crownX + crownY * crownY + crownZ * crownZ);
			if (length < 1.0E-4D) {
				crownX = 0.0D;
				crownY = 1.0D;
				crownZ = 0.0D;
			} else {
				crownX /= length;
				crownY /= length;
				crownZ /= length;
			}
		}
		float yaw = Math.hypot(motion.x, motion.z) > 1.0E-3D ? (float) Math.atan2(-motion.x, motion.z)
				: running.anchorYaw() * Mth.DEG_TO_RAD;
		float[] tilt = DaemonHaze.tilt(crownX, crownY, crownZ, yaw);
		float[] trail = DaemonHaze.trail(motion.x, motion.y, motion.z);
		double[] box = DaemonHaze.box(now.scale(), tilt[3], now.stream());
		double height = CardinalRiteVisualRules.DAEMON_HEIGHT * now.scale();
		float seed = Math.floorMod(running.seed(), 113L);
		Vec3 c = now.centre();
		DaemonHazeQueue.add(new DaemonHazeQueue.Draw(c.x, c.y, c.z, (float) height, yaw, yaw, tilt[0], tilt[2], tilt[3],
				(float) box[0], (float) box[1], t + seed * 7.0F, seed, now.emergence(), now.strength(), 0.0F,
				DaemonHaze.eyesOpen(now.emergence()), DAEMON_TINT, 0.0F, trail[0], trail[1], trail[2], now.recall(),
				now.flow(), now.stream()));
	}

	/** Where and how the daemon is at scene time {@code t}, or null while it is not there. */
	@Nullable
	private static DaemonPose daemonAt(Running running, float t, float partialTick) {
		float h = running.anchorHeight();
		Vec3 anchor = running.anchorPos(partialTick);
		switch (running.scene()) {
			case DEPARTURE -> {
				// It hangs where the rite left it, then flies an arc through the echo's chest into the surrogate.
				Vec3 through = running.echoPos().add(0.0D, 1.3D, 0.0D);
				Vec3 start = daemonStart(running, through);
				Vec3 end = anchor.add(0.0D, h * 0.6D, 0.0D);
				if (t < DEPARTURE_DAEMON_START) return new DaemonPose(start, 0.5D, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F);
				if (t < DEPARTURE_DAEMON_ARRIVES) {
					float u = (float) HemoEasing.easeInOutCubic(ramp(t, DEPARTURE_DAEMON_START, DEPARTURE_DAEMON_ARRIVES));
					double scale = Mth.lerp(u, 0.5D, 0.3D);
					double path = start.distanceTo(through) + through.distanceTo(end);
					float stream = HemoEasing.smoothstep(HemoEasing.clamp01(Mth.sin(u * Mth.PI) * 1.6F));
					return new DaemonPose(arc(start, through, end, u), scale, 1.0F, 1.0F, 0.0F, stream,
							(float) (u * path / (CardinalRiteVisualRules.DAEMON_HEIGHT * scale)));
				}
				if (t < DEPARTURE_DISSOLVE_END) {
					float x = ramp(t, DEPARTURE_DAEMON_ARRIVES, DEPARTURE_DISSOLVE_END);
					return new DaemonPose(end.add(0.0D, -0.25D * x * h, 0.0D), 0.3D * (1.0D - 0.3D * x), 1.0F - x,
							1.0F - HemoEasing.smoothstep(x), x, 0.0F, 0.0F);
				}
				return null;
			}
			case BROKEN_RITE -> {
				// Flung up out of the opened cage, then down into the echo's chest.
				if (t < BROKEN_BLAST) return null;
				Vec3 top = anchor.add(0.0D, h + 0.9D, 0.0D);
				Vec3 end = anchor.add(0.0D, h * 0.55D, 0.0D);
				float side = (running.seed() & 255) / 255.0F * Mth.TWO_PI;
				Vec3 apex = top.add(Mth.cos(side) * 1.2D, 1.6D, Mth.sin(side) * 1.2D);
				if (t < BROKEN_SLAM) {
					float u = ramp(t, BROKEN_BLAST, BROKEN_SLAM);
					float emergence = 0.5F + 0.5F * HemoEasing.smoothstep(ramp(t, BROKEN_BLAST, BROKEN_BLAST + 4));
					return new DaemonPose(quadratic(top, apex, end, u), 0.4D, emergence, 1.0F, 0.0F, 0.8F,
							(float) (u * 4.0D / (CardinalRiteVisualRules.DAEMON_HEIGHT * 0.4D)));
				}
				if (t < BROKEN_SLAM + 6) {
					float x = ramp(t, BROKEN_SLAM, BROKEN_SLAM + 6);
					return new DaemonPose(end, 0.4D * (1.0D - 0.4D * x), 1.0F - x, 1.0F - x, x, 0.0F, 0.0F);
				}
				return null;
			}
			case CLEAN_RITE -> {
				// Re-formed out of the gathering spores over the echo, then drifting down into it.
				if (t < CLEAN_TENDRILS_RISE_END) return null;
				Vec3 gather = gatherPoint(running, partialTick);
				Vec3 end = anchor.add(0.0D, h * 0.55D, 0.0D);
				if (t < CLEAN_REFORM_END) {
					float x = ramp(t, CLEAN_TENDRILS_RISE_END, CLEAN_REFORM_END);
					return new DaemonPose(gather, 0.4D, x, 0.25F + 0.75F * x, 0.0F, 0.0F, 0.0F);
				}
				if (t < CLEAN_DRIFT_END) {
					float u = HemoEasing.smoothstep(ramp(t, CLEAN_REFORM_END, CLEAN_DRIFT_END));
					return new DaemonPose(gather.lerp(end, u), Mth.lerp(u, 0.4D, 0.32D), 1.0F, 1.0F, 0.0F,
							0.3F * Mth.sin(u * Mth.PI), 0.0F);
				}
				if (t < CLEAN_DRIFT_END + 8) {
					float x = ramp(t, CLEAN_DRIFT_END, CLEAN_DRIFT_END + 8);
					return new DaemonPose(end, 0.32D * (1.0D - 0.3D * x), 1.0F - x, 1.0F - HemoEasing.smoothstep(x), x,
							0.0F, 0.0F);
				}
				return null;
			}
			default -> {
				return null;
			}
		}
	}

	/** Where the rite left the daemon; over the echo if that was never sent or is far off. */
	private static Vec3 daemonStart(Running running, Vec3 echoChest) {
		Vec3 daemon = running.daemon();
		return daemon == null || daemon.distanceToSqr(echoChest) > 24.0D * 24.0D ? echoChest.add(0.0D, 2.2D, 0.0D)
				: daemon;
	}

	/** Where the leaked spores gather and the daemon re-forms, over the echo's head. */
	private static Vec3 gatherPoint(Running running, float partialTick) {
		return running.anchorPos(partialTick).add(0.0D, running.anchorHeight() + 1.4D, 0.0D);
	}

	/** A quadratic curve from {@code start} through {@code through} at its middle to {@code end}. */
	private static Vec3 arc(Vec3 start, Vec3 through, Vec3 end, float u) {
		Vec3 control = through.scale(2.0D).subtract(start.add(end).scale(0.5D));
		return quadratic(start, control, end, u);
	}

	private static Vec3 quadratic(Vec3 start, Vec3 control, Vec3 end, float u) {
		double a = 1.0D - u;
		return start.scale(a * a).add(control.scale(2.0D * a * u)).add(end.scale((double) u * u));
	}

	// ------------------------------------------------------------------ shared pieces

	private static float ramp(float t, float start, float end) {
		return HemoEasing.clamp01((t - start) / (end - start));
	}

	/** A flash at {@code at}, fading over {@code length} ticks. */
	private static float flash(float t, float at, float length) {
		return t < at ? 0.0F : Math.max(0.0F, 1.0F - (t - at) / length);
	}

	private static void sound(ClientLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
		level.playLocalSound(at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch, false);
	}

	private static void burst(ClientLevel level, ParticleOptions options, Vec3 at, int count, double spread,
			double speed, double lift) {
		RandomSource random = level.random;
		for (int i = 0; i < count; i++) {
			Vec3 dir = scatter(random);
			double s = speed * (0.5D + random.nextDouble());
			level.addParticle(options, at.x + dir.x * spread, at.y + dir.y * spread, at.z + dir.z * spread, dir.x * s,
					dir.y * s + lift, dir.z * s);
		}
	}

	/** Dark clot motes nosing up out of the floor round a ring, where tendrils are about to break through. */
	private static void ringMotes(ClientLevel level, Vec3 centre, double radius, int count) {
		RandomSource random = level.random;
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double r = radius * (0.8D + 0.4D * random.nextDouble());
			level.addParticle(HemoParticleData.darkGlow(CLOT), centre.x + Math.cos(angle) * r, centre.y + 0.08D,
					centre.z + Math.sin(angle) * r, 0.0D, 0.03D + random.nextDouble() * 0.03D, 0.0D);
		}
	}

	/** Spores lifting off the goo and settling back toward its middle. */
	private static void gooSpores(ClientLevel level, RandomSource random, Vec3 centre, int count) {
		for (int i = 0; i < count; i++) {
			Vec3 at = bodyPoint(random, centre, 2.0F, 0.3F);
			level.addParticle(HemoParticleData.sporiticSpore(SPORE), at.x, at.y, at.z, (centre.x - at.x) * 0.04D,
					0.01D, (centre.z - at.z) * 0.04D);
		}
	}

	private static Vec3 bodyPoint(RandomSource random, Vec3 feet, float width, float height) {
		return feet.add((random.nextDouble() - 0.5D) * width, random.nextDouble() * height,
				(random.nextDouble() - 0.5D) * width);
	}

	private static Vec3 scatter(RandomSource random) {
		double x = random.nextGaussian(), y = random.nextGaussian(), z = random.nextGaussian();
		double length = Math.sqrt(x * x + y * y + z * z);
		return length < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(x / length, y / length, z / length);
	}
}
