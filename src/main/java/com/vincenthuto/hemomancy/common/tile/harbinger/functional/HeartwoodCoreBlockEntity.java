package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.HeartwoodCoreBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.HeartwoodCoreRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The feed meter of a Heartwood tree, and the tree's hunger. Every pulse it bleeds each mob touching one of
 * the tree's standing logs and each player near one, and feeds on them. Its log list is what it grew with, so wood
 * placed nearby is inert and a tree whose core is cut out is only timber.
 */
public class HeartwoodCoreBlockEntity extends HemoBlockEntity {
	private static final BloodAccounting.Source FEEDING = BloodAccounting.Source.of("heartwood_core_feeding", Category.WORLD, true, false);
	private static final ParticleColor BLOOD = new ParticleColor(190, 18, 34);
	private static final int REFRESH_BELOW_TICKS = 20;
	private double fed;
	private long[] treeLogs = new long[0];
	private Set<Long> treeLookup;

	public HeartwoodCoreBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.heartwood_core.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, HeartwoodCoreBlockEntity core) {
		if (level.getGameTime() % HeartwoodCoreRules.PULSE_TICKS == 0) core.pulse((ServerLevel) level);
	}

	public double fed() {
		return fed;
	}

	/** Records the logs the core grew with; only they bite. */
	public void bindTree(Collection<BlockPos> logs) {
		treeLogs = logs.stream().mapToLong(BlockPos::asLong).toArray();
		treeLookup = null;
		setChanged();
	}

	/** One pulse of the tree's hunger; the ticker calls it every {@link HeartwoodCoreRules#PULSE_TICKS}. */
	public void pulse(ServerLevel level) {
		AABB bounds = treeBounds();
		for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, bounds.inflate(1.0D), HeartwoodCoreBlockEntity::bleeds)) {
			if (touchesTree(level, mob)) bite(level, mob, HeartwoodCoreRules.MOB_FEED);
		}
		AABB reach = bounds.inflate(HeartwoodCoreRules.PLAYER_NEAR_BLOCKS);
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isSpectator() || player.isCreative() || !reach.contains(player.position())) continue;
			if (nearTree(level, player)) bite(level, player, HeartwoodCoreRules.PLAYER_FEED);
		}
	}

	private static boolean bleeds(LivingEntity entity) {
		return entity.isAlive() && !(entity instanceof Player) && !(entity instanceof ArmorStand)
				&& !HemoEntityPredicates.NOBLOOD.test(entity);
	}

	private void bite(ServerLevel level, LivingEntity victim, double feed) {
		MobEffectInstance bleeding = victim.getEffect(EffectInit.blood_loss);
		// Only near the end of the last dose: re-adding it every pulse would send the victim a packet every pulse.
		if (bleeding == null || bleeding.getDuration() < REFRESH_BELOW_TICKS) {
			victim.addEffect(new MobEffectInstance(EffectInit.blood_loss, HeartwoodCoreRules.BLOOD_LOSS_TICKS, 0));
		}
		Vec3 from = victim.getBoundingBox().getCenter();
		Vec3 to = Vec3.atCenterOf(worldPosition);
		for (double along : new double[] { 0.25D, 0.5D, 0.75D }) {
			Vec3 at = from.lerp(to, along);
			level.sendParticles(HemoParticleData.bloodCell(BLOOD), at.x, at.y, at.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
		}
		feed(level, feed);
	}

	private boolean touchesTree(ServerLevel level, LivingEntity entity) {
		AABB box = entity.getBoundingBox().inflate(0.1D);
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
			if (isStandingWood(level, pos)) return true;
		}
		return false;
	}

	private boolean nearTree(ServerLevel level, Player player) {
		double nearSqr = HeartwoodCoreRules.PLAYER_NEAR_BLOCKS * HeartwoodCoreRules.PLAYER_NEAR_BLOCKS;
		if (player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= nearSqr) return true;
		for (long packed : treeLogs) {
			BlockPos pos = BlockPos.of(packed);
			if (player.distanceToSqr(Vec3.atCenterOf(pos)) <= nearSqr && level.getBlockState(pos).is(BlockInit.HEARTWOOD.log().get())) return true;
		}
		return false;
	}

	private boolean isStandingWood(ServerLevel level, BlockPos pos) {
		if (pos.equals(worldPosition)) return true;
		if (treeLookup == null) {
			treeLookup = new HashSet<>();
			for (long packed : treeLogs) treeLookup.add(packed);
		}
		return treeLookup.contains(pos.asLong()) && level.getBlockState(pos).is(BlockInit.HEARTWOOD.log().get());
	}

	private AABB treeBounds() {
		AABB bounds = new AABB(worldPosition);
		for (long packed : treeLogs) bounds = bounds.minmax(new AABB(BlockPos.of(packed)));
		return bounds;
	}

	/** Takes what blood it has room for from a player projecting into it; returns the amount taken. */
	public double receiveProjectedBlood(ServerPlayer player, double offered) {
		if (!(level instanceof ServerLevel serverLevel)) return 0.0D;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume == null || !volume.isActive()) return 0.0D;
		double taken = HeartwoodCoreRules.accepted(fed, offered, volume.getBloodVolume());
		if (taken <= 0.0D || !BloodAccounting.pay(player, taken, FEEDING).satisfied()) return 0.0D;
		feed(serverLevel, taken);
		return taken;
	}

	/** Raises the meter; a new stage shows in the block's glow and light, with a throb of blood from the core. */
	public void feed(ServerLevel level, double amount) {
		int before = HeartwoodCoreRules.stage(fed);
		fed = HeartwoodCoreRules.fed(fed, amount);
		int stage = HeartwoodCoreRules.stage(fed);
		setChanged();
		if (stage == before) return;
		level.setBlock(worldPosition, getBlockState().setValue(HeartwoodCoreBlock.FED, stage), Block.UPDATE_ALL);
		Vec3 centre = Vec3.atCenterOf(worldPosition);
		level.sendParticles(HemoParticleData.bloodCell(BLOOD), centre.x, centre.y, centre.z, 12 + stage * 6, 0.45D, 0.45D, 0.45D, 0.02D);
		level.playSound(null, worldPosition, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 0.8F + stage * 0.2F, 0.7F + stage * 0.08F);
		BlockEntitySync.markAndSync(this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putDouble("Fed", fed);
		tag.putLongArray("TreeLogs", treeLogs);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		fed = HeartwoodCoreRules.fed(tag.getDouble("Fed"), 0.0D);
		treeLogs = tag.getLongArray("TreeLogs");
		treeLookup = null;
	}
}
