package com.vincenthuto.hemomancy.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The hunt's late stage: she is glimpsed. A client-only body stands in the Gardens' fog behind the player, facing them;
 * when they turn and see it, it holds their eye for a moment and sinks into the mycelium. Nothing is spawned on the
 * server and nothing can be hit. It replaces the old elder-guardian flash.
 */
public final class MycophantGlimpse {
	private static final int LIFETIME = 240;
	private static final int SEEN_HOLD = 8;
	private static final int SINK_TICKS = 26;
	private static final float SEEN_COS = Mth.cos(26.0F * Mth.DEG_TO_RAD);

	@Nullable private static MycophantEntity body;
	private static int age;
	private static int seenAt = -1;

	private MycophantGlimpse() {
	}

	public static void start(int seed) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) return;
		clear();
		Player player = minecraft.player;
		ClientLevel level = minecraft.level;
		float behind = player.getYRot() + 180.0F;
		for (int attempt = 0; attempt < 10; attempt++) {
			float spread = ((seed >>> attempt) & 7) / 7.0F - 0.5F;
			float yaw = behind + spread * (attempt < 5 ? 100.0F : 200.0F);
			double distance = 20.0D + ((seed >>> (attempt + 3)) & 7);
			double x = player.getX() - Mth.sin(yaw * Mth.DEG_TO_RAD) * distance;
			double z = player.getZ() + Mth.cos(yaw * Mth.DEG_TO_RAD) * distance;
			BlockPos ground = groundAt(level, BlockPos.containing(x, player.getY(), z));
			if (ground == null) continue;
			MycophantEntity glimpse = EntityInit.mycophant.get().create(level);
			if (glimpse == null) return;
			float face = (float) (Mth.atan2(player.getZ() - z, player.getX() - x) * Mth.RAD_TO_DEG) - 90.0F;
			glimpse.moveTo(x, ground.getY() + 1.0D, z, face, 0.0F);
			glimpse.setYBodyRot(face);
			glimpse.yBodyRotO = face;
			glimpse.setYHeadRot(face);
			glimpse.yHeadRotO = face;
			body = glimpse;
			age = 0;
			seenAt = -1;
			return;
		}
	}

	public static void clear() {
		body = null;
		seenAt = -1;
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		MycophantEntity glimpse = body;
		if (glimpse == null) return;
		if (minecraft.level == null || minecraft.player == null || glimpse.level() != minecraft.level) {
			clear();
			return;
		}
		age++;
		glimpse.tickCount++;
		if (seenAt < 0 && (sees(minecraft.player, glimpse) || age > LIFETIME)) {
			seenAt = age;
			minecraft.level.playLocalSound(glimpse.getX(), glimpse.getY() + 3.0D, glimpse.getZ(),
					SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 1.4F, 0.5F, false);
		}
		if (seenAt >= 0 && age - seenAt > SEEN_HOLD + SINK_TICKS) clear();
	}

	public static void render(PoseStack poseStack, Vec3 camera, float partial) {
		MycophantEntity glimpse = body;
		Minecraft minecraft = Minecraft.getInstance();
		if (glimpse == null || minecraft.level == null) return;
		float sink = seenAt < 0 ? 0.0F : Mth.clamp((age - seenAt - SEEN_HOLD + partial) / SINK_TICKS, 0.0F, 1.0F);
		double drop = sink * sink * 7.5D;
		MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
		int light = LevelRenderer.getLightColor(minecraft.level, glimpse.blockPosition().above(2));
		minecraft.getEntityRenderDispatcher().render(glimpse, glimpse.getX() - camera.x, glimpse.getY() - drop - camera.y,
				glimpse.getZ() - camera.z, glimpse.getYRot(), partial, poseStack, buffers, light);
		buffers.endBatch();
	}

	/** In the view cone and not behind a wall. */
	private static boolean sees(Player player, MycophantEntity glimpse) {
		Vec3 eye = player.getEyePosition();
		Vec3 target = glimpse.position().add(0.0D, 3.0D, 0.0D);
		Vec3 toward = target.subtract(eye);
		double distance = toward.length();
		if (distance < 1.0E-3D || player.getViewVector(1.0F).dot(toward.scale(1.0D / distance)) < SEEN_COS) return false;
		HitResult hit = player.level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceTo(eye) > distance - 2.5D;
	}

	/** Solid footing with room for her above it, searched around the player's height. */
	@Nullable
	private static BlockPos groundAt(ClientLevel level, BlockPos start) {
		for (int dy = 10; dy >= -14; dy--) {
			BlockPos floor = start.offset(0, dy, 0);
			if (!level.getBlockState(floor).isFaceSturdy(level, floor, net.minecraft.core.Direction.UP)) continue;
			boolean room = true;
			for (int up = 1; up <= 5 && room; up++) room = level.getBlockState(floor.above(up)).getCollisionShape(level, floor.above(up)).isEmpty();
			if (room) return floor;
		}
		return null;
	}
}
