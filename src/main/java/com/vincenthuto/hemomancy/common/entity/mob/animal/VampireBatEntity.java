package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.UUID;

public final class VampireBatEntity extends Bat {
	private static final EntityDataAccessor<Boolean> AGGREGATE = SynchedEntityData.defineId(
			VampireBatEntity.class, EntityDataSerializers.BOOLEAN);

	private static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData.defineId(
			VampireBatEntity.class, EntityDataSerializers.BOOLEAN);
	private static final ParticleColor SWARM_MOTE = new ParticleColor(150, 16, 28);

	/** Client only: the body's lean into its flight, eased so the model doesn't snap with each tick's movement. */
	private float flightPitch, flightPitchO, flightBank, flightBankO;

	public VampireBatEntity(EntityType<? extends VampireBatEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Bat.createAttributes();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(AGGREGATE, false);
		builder.define(HUNTING, false);
	}

	public void makeManifestedAggregate(UUID owner) {
		entityData.set(AGGREGATE, true);
		getPersistentData().putUUID("CircusSwarmOwner", owner);
		setResting(false);
		setPersistenceRequired();
	}

	public boolean isManifestedAggregate() {
		return entityData.get(AGGREGATE);
	}

	public boolean isHunting() {
		return entityData.get(HUNTING);
	}

	/** Radians, nose up. */
	public float flightPitch(float partialTick) {
		return Mth.lerp(partialTick, flightPitchO, flightPitch);
	}

	/** Radians, into the turn. */
	public float flightBank(float partialTick) {
		return Mth.lerp(partialTick, flightBankO, flightBank);
	}

	private void easeFlightLean() {
		flightPitchO = flightPitch;
		flightBankO = flightBank;
		double dx = getX() - xo, dz = getZ() - zo;
		float pitch = (float) Mth.atan2(getY() - yo, Math.sqrt(dx * dx + dz * dz));
		float turn = Mth.wrapDegrees(yBodyRot - yBodyRotO) * Mth.DEG_TO_RAD;
		flightPitch += (Mth.clamp(pitch, -0.7F, 0.6F) - flightPitch) * 0.15F;
		flightBank += (Mth.clamp(turn * 2.5F, -0.6F, 0.6F) - flightBank) * 0.12F;
	}

	public static boolean canSpawnHere(EntityType<VampireBatEntity> type, LevelAccessor level,
			MobSpawnType reason, BlockPos pos, RandomSource random) {
		return level.getMaxLocalRawBrightness(pos) <= 7
				&& level.getFluidState(pos).isEmpty()
				&& level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 20;
	}

	@Override
	protected boolean shouldDropLoot() {
		return !isManifestedAggregate() && !getPersistentData().hasUUID("CircusSwarmOwner") && super.shouldDropLoot();
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			easeFlightLean();
			if (isManifestedAggregate() && random.nextBoolean()) level().addParticle(HemoParticleData.bloodCell(SWARM_MOTE),
					getRandomX(2.5D), getRandomY(), getRandomZ(2.5D), 0.0D, 0.01D, 0.0D);
			return;
		}
		LivingEntity prey = isManifestedAggregate() ? aggregateOwner() : bleedingPrey();
		if (isManifestedAggregate() && prey == null) {
			discard();
			return;
		}
		entityData.set(HUNTING, prey != null);
		if (prey == null) return;
		setResting(false);
		Vec3 delta = prey.getEyePosition().subtract(position());
		if (delta.lengthSqr() > 0.01D) setDeltaMovement(getDeltaMovement().scale(0.75D)
				.add(delta.normalize().scale(isManifestedAggregate() ? 0.09D : 0.035D)));
		// The Bat AI steers at its own wander point; a hunter faces what it's flying at.
		float facing = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
		setYRot(facing);
		yBodyRot = facing;
		yHeadRot = facing;
		if (tickCount % (isManifestedAggregate() ? 25 : 60) == 0 && distanceToSqr(prey) < 2.25D)
			prey.hurt(damageSources().mobAttack(this), isManifestedAggregate() ? 2.0F : 1.0F);
	}

	private LivingEntity aggregateOwner() {
		if (!(level() instanceof ServerLevel server) || !getPersistentData().hasUUID("CircusSwarmOwner")) return null;
		ServerPlayer owner = server.getServer().getPlayerList().getPlayer(getPersistentData().getUUID("CircusSwarmOwner"));
		return owner != null && owner.isAlive() && owner.level() == level() ? owner : null;
	}

	private LivingEntity bleedingPrey() {
		return level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(8.0D),
				living -> living != this && living.isAlive() && living.getHealth() < living.getMaxHealth())
				.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("ManifestedAggregate", isManifestedAggregate());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(AGGREGATE, tag.getBoolean("ManifestedAggregate"));
	}
}
