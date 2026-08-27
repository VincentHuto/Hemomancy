package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.shared.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSanguineFormationProjection;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class SanguineFormationProjectionHandler {
	public static final TagKey<Block> PROJECTOR_TAG = TagKey.create(Registries.BLOCK,
			Hemomancy.rloc("sanguine_formation_projectors"));
	private static final double SYNC_RANGE = 64.0D;
	private static final int FORMING_VISIBLE_TICKS = 6;
	private static final int FINAL_VISIBLE_TICKS = 10;
	private static final Map<FormationKey, FormationProgress> ACTIVE_FORMATIONS = new HashMap<>();

	private SanguineFormationProjectionHandler() {
	}

	public static double tryProjectAtLookTarget(Level level, LivingEntity user, double maxAmount) {
		if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player) || maxAmount <= 0.0D) {
			return 0.0D;
		}
		HitResult trace = SanguineProjectionTargeting.pick(serverLevel, player,
				SanguineProjectionTargeting.PROJECTION_REACH, true);
		if (!(trace instanceof BlockHitResult hit) || trace.getType() != HitResult.Type.BLOCK) {
			return 0.0D;
		}

		BlockPos pos = hit.getBlockPos();
		BlockState state = serverLevel.getBlockState(pos);
		if (!isEligibleTarget(serverLevel, pos, state)) {
			return 0.0D;
		}

		IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (bloodVolume == null || !bloodVolume.isActive()) {
			return 0.0D;
		}

		boolean attuned = state.is(PROJECTOR_TAG);
		FormationKey key = new FormationKey(serverLevel.dimension(), pos.immutable(), hit.getDirection());
		long gameTime = serverLevel.getGameTime();
		FormationProgress progress = ACTIVE_FORMATIONS.get(key);
		if (progress != null && SanguineFormationProjectionRules.isExpired(gameTime, progress.lastFedGameTime)) {
			sendProjectionSync(serverLevel, key, 0.0F, FINAL_VISIBLE_TICKS,
					PacketSanguineFormationProjection.State.FAILURE, attuned);
			ACTIVE_FORMATIONS.remove(key);
			progress = null;
		}

		double currentProgress = progress == null ? 0.0D : progress.bloodFed;
		double feedAmount = SanguineFormationProjectionRules.feedAmount(currentProgress, attuned,
				bloodVolume.getBloodVolume(), maxAmount);
		if (feedAmount <= 0.0D) {
			return 0.0D;
		}

		if (progress == null) {
			progress = new FormationProgress();
			ACTIVE_FORMATIONS.put(key, progress);
		}
		if (!bloodVolume.drain(feedAmount)) {
			return 0.0D;
		}
		bloodVolume.addBloodSpend(feedAmount);
		progress.bloodFed += feedAmount;
		progress.lastFedGameTime = gameTime;
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(bloodVolume));

		double cost = SanguineFormationProjectionRules.bloodCost(attuned);
		float normalized = (float) Math.min(1.0D, progress.bloodFed / cost);
		sendProjectionSync(serverLevel, key, normalized, FORMING_VISIBLE_TICKS,
				PacketSanguineFormationProjection.State.FORMING, attuned);

		if (progress.bloodFed >= cost) {
			completeProjection(serverLevel, player, key, attuned);
		}
		return feedAmount;
	}

	public static void tick(ServerLevel level) {
		long gameTime = level.getGameTime();
		Iterator<Map.Entry<FormationKey, FormationProgress>> iterator = ACTIVE_FORMATIONS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<FormationKey, FormationProgress> entry = iterator.next();
			FormationKey key = entry.getKey();
			if (!key.dimension.equals(level.dimension())) {
				continue;
			}
			if (SanguineFormationProjectionRules.isExpired(gameTime, entry.getValue().lastFedGameTime)) {
				sendProjectionSync(level, key, 0.0F, FINAL_VISIBLE_TICKS,
						PacketSanguineFormationProjection.State.FAILURE, false);
				iterator.remove();
			}
		}
	}

	public static void clear() {
		ACTIVE_FORMATIONS.clear();
	}

	private static boolean isEligibleTarget(ServerLevel level, BlockPos pos, BlockState state) {
		if (state.isAir() || state.canBeReplaced() || state.getBlock() instanceof BlockBloodEndpoint) {
			return false;
		}
		if (!state.getFluidState().isEmpty()) {
			return false;
		}
		if (state.getCollisionShape(level, pos).isEmpty()) {
			return false;
		}
		return level.getBlockEntity(pos) == null;
	}

	private static void completeProjection(ServerLevel level, ServerPlayer player, FormationKey key, boolean attuned) {
		int skillLevel = SkillPointHelper.getSanguineCrystallizationLevel(player);
		double collapseChance = SanguineFormationProjectionRules.collapseChance(attuned, skillLevel);
		boolean collapsed = collapseChance > 0.0D && level.random.nextDouble() < collapseChance;
		ACTIVE_FORMATIONS.remove(key);

		if (collapsed) {
			level.playSound(null, key.pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.55F);
			spawnFailureFizz(level, faceCenter(key.pos, key.face));
			sendProjectionSync(level, key, 1.0F, FINAL_VISIBLE_TICKS,
					PacketSanguineFormationProjection.State.FAILURE, attuned);
			return;
		}

		Vec3 spawnPos = faceCenter(key.pos, key.face).add(0.0D, 0.05D, 0.0D);
		ItemEntity item = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z,
				new ItemStack(ItemInit.sanguine_formation.get()));
		Vec3 facePush = Vec3.atLowerCornerOf(key.face.getNormal()).scale(0.08D);
		item.setDeltaMovement(facePush.x, 0.12D + Math.max(0.0D, facePush.y), facePush.z);
		item.setDefaultPickUpDelay();
		level.addFreshEntity(item);
		spawnSuccessBurst(level, spawnPos);
		level.playSound(null, key.pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 0.7F, 0.8F);
		sendProjectionSync(level, key, 1.0F, FINAL_VISIBLE_TICKS,
				PacketSanguineFormationProjection.State.COMPLETE, attuned);
	}

	private static void spawnSuccessBurst(ServerLevel level, Vec3 center) {
		level.sendParticles(new ColorParticleData(new ParticleColor(185, 0, 0)),
				center.x, center.y, center.z, 18, 0.18D, 0.18D, 0.18D, 0.08D);
		level.sendParticles(new ColorParticleData(new ParticleColor(95, 0, 0)),
				center.x, center.y, center.z, 8, 0.10D, 0.10D, 0.10D, 0.04D);
	}

	private static void spawnFailureFizz(ServerLevel level, Vec3 center) {
		level.sendParticles(new ColorParticleData(new ParticleColor(70, 0, 0)),
				center.x, center.y, center.z, 14, 0.16D, 0.16D, 0.16D, 0.018D);
		level.sendParticles(new ColorParticleData(new ParticleColor(24, 0, 0)),
				center.x, center.y, center.z, 6, 0.08D, 0.08D, 0.08D, 0.010D);
	}

	private static Vec3 faceCenter(BlockPos pos, Direction face) {
		return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.58D));
	}

	private static void sendProjectionSync(ServerLevel level, FormationKey key, float progress, int visibleTicks,
			PacketSanguineFormationProjection.State state, boolean attuned) {
		PacketDistributor.sendToPlayersNear(level, null, key.pos.getX() + 0.5D, key.pos.getY() + 0.5D,
				key.pos.getZ() + 0.5D, SYNC_RANGE,
				new PacketSanguineFormationProjection(key.pos, key.face, progress, visibleTicks, state, attuned,
						key.pos.asLong() ^ ((long) key.face.get3DDataValue() << 56)));
	}

	private record FormationKey(ResourceKey<Level> dimension, BlockPos pos, Direction face) {
	}

	private static final class FormationProgress {
		private double bloodFed;
		private long lastFedGameTime;
	}
}
