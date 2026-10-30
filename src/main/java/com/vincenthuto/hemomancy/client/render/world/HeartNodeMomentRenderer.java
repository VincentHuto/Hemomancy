package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.HeartNodeBeat;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.HeartNodeParts;
import com.vincenthuto.hemomancy.client.render.world.PossessionTendrils.Ring;
import com.vincenthuto.hemomancy.client.render.world.PossessionTendrils.Strand;
import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeMomentPacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.common.world.HeartNodeMoment;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The heart-node's moments ({@link HeartNodeMoment}) as nearby clients see them. Planting: roots punch out of the node
 * as Gardens tendrils (the possession scenes' tendril brush), a ring of spores bursts over the ground and the heart
 * beats hard three times (read by the heart's renderer), while the spread's growth front starts from the node on its
 * own clock. Withering: the server has already removed the node, so this keeps a ghost of its last frame (the
 * withered body of its stage and a greying heart) collapsing and sinking into the ground, the spread recedes with it
 * ({@link HeartNodeClientState#wither}) and grey spores rise. A cache opened: its ghost splits its cap open about the
 * hinges and sinks away. Nothing here touches the world; the moments only show what the server did.
 */
public final class HeartNodeMomentRenderer {
	private static final ResourceLocation HYPHAE = Hemomancy.rloc("textures/entity/boss/endgame/mycophant_hyphae.png");
	private static final RenderType ROOT_CORE = RenderType.entityCutoutNoCull(HYPHAE);
	private static final float CRIMSON_R = 0.92F, CRIMSON_G = 0.16F, CRIMSON_B = 0.22F;
	private static final ParticleColor SPORE = new ParticleColor(228, 214, 200);
	private static final ParticleColor SPORE_RED = new ParticleColor(190, 40, 58);
	private static final ParticleColor ASH = new ParticleColor(132, 126, 120);
	private static final ParticleColor CELL = new ParticleColor(170, 16, 34);

	/** How long each moment runs, in ticks. */
	public static final int PLANT_TICKS = 36;
	public static final int WITHER_TICKS = 26;
	public static final int CACHE_TICKS = 24;
	/** When the planting's spore ring bursts, and the withering's ash rises. */
	private static final int SPORE_BURST_AT = 6;
	private static final int ASH_AT = 3;
	/** The roots: seven tendrils from the node out over the ground, reaching about three blocks. */
	private static final Ring ROOTS = new Ring(7, 0.25F, 0.12F, 0.02F, 2.9F, 0.28F, 0.07F, 1.25F, 0.28F);
	/** Opening lids swing this far, and a ghost sinks this far into the ground at its end. */
	private static final float LID_OPEN_DEGREES = 78.0F;
	private static final float SINK = 0.7F;

	private static final List<Running> RUNNING = new ArrayList<>();

	private record Running(BlockPos pos, HeartNodeMoment moment, int stage, long start, long seed) {
		int length() {
			return switch (moment) {
				case PLANTED -> PLANT_TICKS;
				case WITHERED -> WITHER_TICKS;
				case CACHE_OPENED -> CACHE_TICKS;
			};
		}

		float age(ClientLevel level, float partialTick) {
			// In double: a long-lived world's clock has lost its fractions as a float.
			return (float) (AnimationTime.gameTime(level, (double) partialTick) - start);
		}
	}

	private HeartNodeMomentRenderer() {
	}

	/** A moment the server announced: it starts now, with its sounds. */
	public static void start(HeartNodeMomentPacket packet) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		long now = level.getGameTime();
		BlockPos pos = packet.pos();
		RUNNING.removeIf(running -> running.pos().equals(pos) && running.moment() == packet.moment());
		RUNNING.add(new Running(pos, packet.moment(), packet.stage(), now, pos.asLong() * 31L + now));
		switch (packet.moment()) {
			case PLANTED -> {
				// A wet, deep thump and a soft fungal crackle.
				level.playLocalSound(pos, SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 1.3F, 0.45F, false);
				level.playLocalSound(pos, SoundEvents.WART_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 0.6F, false);
			}
			case WITHERED -> {
				HeartNodeClientState.wither(pos, now);
				level.playLocalSound(pos, SoundEvents.WART_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 0.5F, false);
				level.playLocalSound(pos, SoundEvents.FUNGUS_BREAK, SoundSource.BLOCKS, 0.7F, 0.45F, false);
			}
			case CACHE_OPENED -> level.playLocalSound(pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 0.55F,
					false);
		}
	}

	/**
	 * Ticks into the planting at {@code pos}, or -1 when none is running there; the heart's renderer reads it for the
	 * three hard beats.
	 */
	public static float plantingAge(BlockPos pos, float partialTick) {
		if (RUNNING.isEmpty()) return -1.0F;
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return -1.0F;
		for (Running running : RUNNING) {
			if (running.moment() == HeartNodeMoment.PLANTED && running.pos().equals(pos)) {
				return running.age(level, partialTick);
			}
		}
		return -1.0F;
	}

	/** Moments running now; for the review client. */
	public static int running() {
		return RUNNING.size();
	}

	/** Once per client tick: the moments' one-shot spores and cells, and retiring finished moments. */
	public static void tick() {
		if (RUNNING.isEmpty()) return;
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			RUNNING.clear();
			return;
		}
		long now = level.getGameTime();
		RUNNING.removeIf(running -> now - running.start() > running.length() || running.start() > now);
		for (Running running : RUNNING) {
			long age = now - running.start();
			switch (running.moment()) {
				case PLANTED -> {
					if (age == SPORE_BURST_AT) sporeRing(level, running.pos());
					if (age >= HeartNodeBeat.PLANT_FIRST && age < HeartNodeBeat.PLANT_FIRST
							+ HeartNodeBeat.PLANT_GAP * HeartNodeBeat.PLANT_BEATS
							&& (age - (long) HeartNodeBeat.PLANT_FIRST) % (long) HeartNodeBeat.PLANT_GAP == 0) {
						heartCells(level, running.pos());
						level.playLocalSound(running.pos(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 0.9F,
								0.7F, false);
					}
				}
				case WITHERED -> {
					if (age == ASH_AT) ash(level, running.pos(), 14);
					if (age == WITHER_TICKS / 2) ash(level, running.pos(), 6);
				}
				case CACHE_OPENED -> {
					if (age == 2) cacheSpores(level, running.pos());
				}
			}
		}
	}

	public static void render(PoseStack poseStack, float partialTick) {
		if (RUNNING.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		float time = AnimationTime.gameTime(level, partialTick);
		List<Strand> strands = new ArrayList<>();
		boolean glowing = false;
		for (Running running : RUNNING) {
			float age = running.age(level, partialTick);
			if (age > running.length()) continue;
			BlockPos pos = running.pos();
			int light = LevelRenderer.getLightColor(level, pos);
			poseStack.pushPose();
			poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
			switch (running.moment()) {
				case PLANTED -> glowing |= roots(running, age, time, light, poseStack, buffers, strands);
				case WITHERED -> witherGhost(running, age, light, poseStack, buffers);
				case CACHE_OPENED -> cacheGhost(age, light, poseStack, buffers);
			}
			poseStack.popPose();
		}
		buffers.endBatch(HeartNodeParts.CUTOUT);
		if (glowing) buffers.endBatch(HLRenderTypeInit.TENDRIL_GLOW);
	}

	public static void clear() {
		RUNNING.clear();
	}

	// ------------------------------------------------------------------ planting

	/** The roots punching out over the ground, and a crimson glow where they leave the node; true if it drew glow. */
	private static boolean roots(Running running, float age, float time, int light, PoseStack poseStack,
			MultiBufferSource.BufferSource buffers, List<Strand> strands) {
		float growth = HemoEasing.easeOutCubic(ramp(age, 0.0F, 9.0F));
		float sink = HemoEasing.easeInCubic(ramp(age, 20.0F, PLANT_TICKS)) * 0.9F;
		float fade = 1.0F - ramp(age, 22.0F, PLANT_TICKS);
		float beat = HeartNodeBeat.plantingBeats(age);
		strands.clear();
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		PossessionTendrils.ring(strands, ROOTS, running.seed(), growth, sink, (0.25F + 0.35F * beat) * fade,
				Math.min(1.0F, age / 12.0F), time);
		PoseStack.Pose pose = poseStack.last();
		for (Strand strand : strands) PossessionTendrils.core(pose, buffers.getBuffer(ROOT_CORE), strand, light);
		buffers.endBatch(ROOT_CORE);
		for (Strand strand : strands) {
			PossessionTendrils.glow(pose, buffers.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), strand, CRIMSON_R, CRIMSON_G,
					CRIMSON_B);
		}
		PossessionTendrils.floorHalo(poseStack, buffers.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), 0.03F,
				1.2F + 4.6F * growth, CRIMSON_R, CRIMSON_G, CRIMSON_B, (0.18F + 0.3F * beat) * fade);
		poseStack.popPose();
		return true;
	}

	private static void sporeRing(ClientLevel level, BlockPos pos) {
		RandomSource random = level.random;
		int count = 28;
		for (int i = 0; i < count; i++) {
			double angle = (i + random.nextDouble() * 0.6D) * Math.PI * 2.0D / count;
			double speed = 0.11D + random.nextDouble() * 0.05D;
			level.addParticle(HemoParticleData.sporiticSpore(i % 3 == 0 ? SPORE_RED : SPORE),
					pos.getX() + 0.5D + Math.cos(angle) * 0.35D, pos.getY() + 0.12D,
					pos.getZ() + 0.5D + Math.sin(angle) * 0.35D, Math.cos(angle) * speed, 0.015D,
					Math.sin(angle) * speed);
		}
	}

	private static void heartCells(ClientLevel level, BlockPos pos) {
		RandomSource random = level.random;
		for (int i = 0; i < 4; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			level.addParticle(HemoParticleData.bloodCell(CELL), pos.getX() + HeartNodeParts.HEART_X,
					pos.getY() + HeartNodeParts.HEART_Y + 0.1D, pos.getZ() + HeartNodeParts.HEART_Z,
					Math.cos(angle) * 0.04D, 0.05D + random.nextDouble() * 0.03D, Math.sin(angle) * 0.04D);
		}
		level.addParticle(HemoParticleData.glow(SPORE_RED), pos.getX() + HeartNodeParts.HEART_X,
				pos.getY() + HeartNodeParts.HEART_Y, pos.getZ() + HeartNodeParts.HEART_Z, 0.0D, 0.0D, 0.0D);
	}

	// ------------------------------------------------------------------ withering

	/** The node's last frame: its stage's withered body sinking away, the heart greying and collapsing inward. */
	private static void witherGhost(Running running, float age, int light, PoseStack poseStack,
			MultiBufferSource.BufferSource buffers) {
		float collapse = HemoEasing.easeInCubic(ramp(age, 2.0F, 20.0F));
		poseStack.translate(0.0F, -SINK * HemoEasing.easeInCubic(ramp(age, 14.0F, WITHER_TICKS)), 0.0F);
		HeartNodeParts.draw(poseStack, buffers, HeartNodeParts.witheredStage(running.stage()), light);
		float scale = HeartNodeBeat.restingScale(running.stage()) * (1.0F - 0.82F * collapse);
		if (scale <= 0.02F) return;
		poseStack.pushPose();
		HeartNodeParts.scaleAbout(poseStack, HeartNodeParts.HEART_X, HeartNodeParts.HEART_Y, HeartNodeParts.HEART_Z,
				scale * (1.0F + 0.15F * collapse), scale, scale * (1.0F + 0.15F * collapse));
		// A last flush of red, then grey.
		HeartNodeParts.draw(poseStack, buffers, age < 3.0F ? HeartNodeParts.HEART : HeartNodeParts.HEART_WITHERED,
				light);
		poseStack.popPose();
	}

	private static void ash(ClientLevel level, BlockPos pos, int count) {
		RandomSource random = level.random;
		for (int i = 0; i < count; i++) {
			level.addParticle(HemoParticleData.sporiticSpore(ASH), pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D,
					pos.getY() + 0.25D + random.nextDouble() * 0.3D, pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D,
					(random.nextDouble() - 0.5D) * 0.02D, 0.03D + random.nextDouble() * 0.03D,
					(random.nextDouble() - 0.5D) * 0.02D);
		}
	}

	// ------------------------------------------------------------------ cache

	/** The cache's cap splitting open about its hinges, then the whole fungus sinking away. */
	private static void cacheGhost(float age, int light, PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
		float open = HemoEasing.easeOutCubic(ramp(age, 0.0F, 8.0F)) * LID_OPEN_DEGREES;
		poseStack.translate(0.0F, -SINK * HemoEasing.easeInCubic(ramp(age, 10.0F, CACHE_TICKS)), 0.0F);
		HeartNodeParts.draw(poseStack, buffers, HeartNodeParts.CACHE_BODY, light);
		lid(poseStack, buffers, HeartNodeParts.LID_WEST, HeartNodeParts.LID_HINGE_WEST, open, light);
		lid(poseStack, buffers, HeartNodeParts.LID_EAST, HeartNodeParts.LID_HINGE_EAST, -open, light);
	}

	private static void lid(PoseStack poseStack, MultiBufferSource buffers,
			net.minecraft.client.resources.model.ModelResourceLocation model, float hingeX, float degrees, int light) {
		poseStack.pushPose();
		poseStack.translate(hingeX, HeartNodeParts.LID_HINGE_Y, 0.5F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(degrees));
		poseStack.translate(-hingeX, -HeartNodeParts.LID_HINGE_Y, -0.5F);
		HeartNodeParts.draw(poseStack, buffers, model, light);
		poseStack.popPose();
	}

	private static void cacheSpores(ClientLevel level, BlockPos pos) {
		RandomSource random = level.random;
		for (int i = 0; i < 10; i++) {
			level.addParticle(HemoParticleData.sporiticSpore(i % 2 == 0 ? SPORE : SPORE_RED),
					pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D, pos.getY() + 0.6D,
					pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D, (random.nextDouble() - 0.5D) * 0.05D,
					0.04D + random.nextDouble() * 0.03D, (random.nextDouble() - 0.5D) * 0.05D);
		}
	}

	private static float ramp(float value, float start, float end) {
		return HemoEasing.clamp01((value - start) / (end - start));
	}
}
