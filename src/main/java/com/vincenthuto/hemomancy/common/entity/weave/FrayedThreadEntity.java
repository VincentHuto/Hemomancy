package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * An intrusion's bite: the memory of a failed Harbinger torn loose when a Perfectus weaver threads a false fragment
 * home. It rises from the fragment's tip, winds up once in plain sight, strikes its weaver with its tendency's harm
 * and unravels. It cannot be hurt and never hunts anyone else; the blackout floor keeps it from killing.
 * <p>
 * Threads saved by the witnessed weave, which fought until struck slack, unravel as soon as they load.
 */
public class FrayedThreadEntity extends PathfinderMob {
	private static final EntityDataAccessor<Byte> TENDENCY = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> RANK = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> WINDUP = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FADED = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> UNRAVEL = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<BlockPos> LOOM = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BLOCK_POS);
	/** Ticks a spent intrusion takes to come apart. */
	public static final int UNRAVEL_TICKS = 24;
	private boolean intrusion;

	public FrayedThreadEntity(EntityType<? extends FrayedThreadEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
		noPhysics = true;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D);
	}

	/** Raises a false fragment's bite over its tip, `at` being the tip on the engram's floor. */
	public static FrayedThreadEntity spawnIntrusion(ServerLevel level, SomaticLoomBlockEntity loom, EnumBloodTendency tendency,
			Vec3 at, EnumManipulationRank rank) {
		FrayedThreadEntity thread = new FrayedThreadEntity(EntityInit.frayed_thread.get(), level);
		thread.intrusion = true;
		thread.entityData.set(TENDENCY, (byte) tendency.ordinal());
		thread.entityData.set(RANK, (byte) rank.ordinal());
		thread.entityData.set(LOOM, loom.getBlockPos());
		thread.entityData.set(WINDUP, SomaticWeaveRules.WINDUP_TICKS);
		thread.moveTo(at.x, at.y + 1.1D, at.z, level.random.nextFloat() * 360F, 0F);
		level.addFreshEntity(thread);
		level.sendParticles(HemoParticleData.darkGlow(tendency.getColor()), at.x, at.y + 0.6D, at.z, 18, 0.25D, 0.4D, 0.25D, 0.03D);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 0.7F, 0.6F);
		return thread;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TENDENCY, (byte) 0);
		builder.define(RANK, (byte) 0);
		builder.define(WINDUP, 0);
		builder.define(FADED, 0);
		builder.define(UNRAVEL, 0);
		builder.define(LOOM, BlockPos.ZERO);
	}

	public EnumBloodTendency tendency() { return EnumBloodTendency.values()[entityData.get(TENDENCY)]; }
	public EnumManipulationRank rank() { return EnumManipulationRank.values()[entityData.get(RANK)]; }
	public BlockPos loomPos() { return entityData.get(LOOM); }
	public int windup() { return entityData.get(WINDUP); }
	public boolean isFaded() { return entityData.get(FADED) > 0; }
	public void fade(int ticks) { entityData.set(FADED, ticks); }
	/** Ticks since this intrusion struck and began to come apart; 0 while it still winds up. */
	public int unravelling() { return entityData.get(UNRAVEL); }

	/** Whether this thread's loom is woven by `entity`: intrusions strike and harm only their weaver. */
	public boolean hunts(net.minecraft.world.entity.Entity entity) {
		return entity != null && level().getBlockEntity(loomPos()) instanceof SomaticLoomBlockEntity loom
				&& entity.getUUID().equals(loom.getWeaver());
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		setDeltaMovement(getDeltaMovement().scale(0.6D));
		if (!(level() instanceof ServerLevel server)) return;
		if (!intrusion || !server.isLoaded(loomPos()) || !(server.getBlockEntity(loomPos()) instanceof SomaticLoomBlockEntity loom)) {
			discard();
			return;
		}
		if (entityData.get(FADED) > 0) entityData.set(FADED, entityData.get(FADED) - 1);
		int unravel = unravelling();
		if (unravel > 0) {
			entityData.set(UNRAVEL, unravel + 1);
			setDeltaMovement(getDeltaMovement().add(0, 0.01D, 0));
			if (unravel >= UNRAVEL_TICKS) discard();
			return;
		}
		Player target = loom.weaverPlayer(server);
		if (target != null) getLookControl().setLookAt(target);
		int windup = windup();
		if (windup > 1) {
			entityData.set(WINDUP, windup - 1);
			return;
		}
		entityData.set(WINDUP, 0);
		if (target != null && target.isAlive()) ThreadAttacks.perform(this, target);
		entityData.set(UNRAVEL, 1);
	}

	/** An intrusion is memory, not flesh: nothing harms it. */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override public boolean isPushable() { return false; }
	@Override public boolean removeWhenFarAway(double distance) { return false; }
	@Override protected void registerGoals() {}
	@Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("Intrusion", intrusion);
		tag.putByte("Tendency", entityData.get(TENDENCY));
		tag.putByte("Rank", entityData.get(RANK));
		tag.putInt("Windup", windup());
		tag.putInt("Unravel", unravelling());
		tag.put("Loom", NbtUtils.writeBlockPos(loomPos()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		intrusion = tag.getBoolean("Intrusion");
		entityData.set(TENDENCY, tag.getByte("Tendency"));
		entityData.set(RANK, tag.getByte("Rank"));
		entityData.set(WINDUP, tag.getInt("Windup"));
		entityData.set(UNRAVEL, tag.getInt("Unravel"));
		NbtUtils.readBlockPos(tag, "Loom").ifPresent(pos -> entityData.set(LOOM, pos));
	}
}
