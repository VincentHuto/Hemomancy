package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
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
	private static final byte BITE_EVENT = 64;
	private static final int BITE_TICKS = 6;
	private static final ParticleColor BITE_BLOOD = new ParticleColor(170, 14, 24);
	/** Client only: ticks left of the jaw snapping shut on a bite. */
	private int biteTicks;
	/** Server only: the wet stain this bat has scented and is flying to drink (core-loop Track 10), or null. */
	private BlockPos scent;
	/** How far a bat scents a wet stain, and how often it looks. */
	private static final int SCENT_RADIUS = 10;
	private static final int SCENT_CHECK_TICKS = 20;
	/**
	 * Server only: one of the Ringmaster's roosting bats in his finale (2026-11-18). It hangs upside down in his rigging
	 * until called; then {@code FinaleSwarm} flies it (a swoop that bites and steals blood, the flight back to him and
	 * home to its roost). Its own hunting, scavenging and bat wandering stay off.
	 */
	private boolean finaleRoost;

	/** 1 as the jaw snaps shut on a bite, falling to 0 over a few ticks. */
	public float biteSnap(float partialTick) {
		return biteTicks <= 0 ? 0.0F : Math.max(0.0F, biteTicks - partialTick) / BITE_TICKS;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == BITE_EVENT) {
			biteTicks = BITE_TICKS;
			for (int i = 0; i < 5; i++) {
				level().addParticle(HemoParticleData.bloodCell(BITE_BLOOD), getX(), getEyeY() - 0.1D, getZ(),
						(random.nextDouble() - 0.5D) * 0.08D, 0.02D, (random.nextDouble() - 0.5D) * 0.08D);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	public VampireBatEntity(EntityType<? extends VampireBatEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Bat.createAttributes().add(Attributes.ATTACK_DAMAGE, 1.0D);
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

	/** Makes this a roosting bat of the Ringmaster's finale, flown by his swarm rather than by itself. */
	public void markFinaleRoost() {
		finaleRoost = true;
		noPhysics = true;
		setNoGravity(true);
		setPersistenceRequired();
	}

	public boolean isFinaleRoost() {
		return finaleRoost;
	}

	/** The jaw snaps shut with a spray of blood: a bite seen and heard. */
	public void biteFlourish() {
		level().broadcastEntityEvent(this, BITE_EVENT);
		playSound(SoundEvents.BAT_AMBIENT, 0.6F, 1.5F);
	}

	public boolean isManifestedAggregate() {
		return entityData.get(AGGREGATE);
	}

	/** A manifested aggregate bites for twice the attribute. */
	private float biteDamage() {
		float bite = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
		return isManifestedAggregate() ? bite * 2.0F : bite;
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
		return 8;
	}

	@Override
	protected boolean shouldDropLoot() {
		return !isManifestedAggregate() && !finaleRoost && !getPersistentData().hasUUID("CircusSwarmOwner") && super.shouldDropLoot();
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			if (biteTicks > 0) biteTicks--;
			easeFlightLean();
			if (isManifestedAggregate() && random.nextBoolean()) level().addParticle(HemoParticleData.bloodCell(SWARM_MOTE),
					getRandomX(2.5D), getRandomY(), getRandomZ(2.5D), 0.0D, 0.01D, 0.0D);
			return;
		}
		if (finaleRoost) {
			// Flown by his swarm: nothing of its own moves it.
			noPhysics = true;
			setDeltaMovement(Vec3.ZERO);
			return;
		}
		LivingEntity prey = isManifestedAggregate() ? aggregateOwner() : bleedingPrey();
		if (isManifestedAggregate() && prey == null) {
			discard();
			return;
		}
		if (prey == null && !isManifestedAggregate() && scavenge()) {
			entityData.set(HUNTING, true);
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
		if (tickCount % (isManifestedAggregate() ? 25 : 60) == 0 && distanceToSqr(prey) < 2.25D
				&& prey.hurt(damageSources().mobAttack(this), biteDamage())) {
			level().broadcastEntityEvent(this, BITE_EVENT);
			playSound(SoundEvents.BAT_AMBIENT, 0.35F, 1.7F);
		}
	}

	/**
	 * With no bleeding prey near, a bat follows the scent of a wet blood stain and drinks it up, the world cleaning
	 * what a Harbinger left (core-loop Track 10). Returns whether it is scavenging this tick.
	 */
	private boolean scavenge() {
		if (!(level() instanceof ServerLevel server)) return false;
		if (scent != null && !isWetStain(server, scent)) scent = null;
		if (scent == null && (tickCount + getId()) % SCENT_CHECK_TICKS == 0) {
			scent = com.vincenthuto.hemomancy.common.world.answer.WorldAnswer.wetStains(server, blockPosition(), SCENT_RADIUS)
					.stream().min(Comparator.comparingDouble(pos -> pos.distToCenterSqr(position()))).orElse(null);
		}
		if (scent == null) return false;
		setResting(false);
		Vec3 target = Vec3.atCenterOf(scent);
		Vec3 delta = target.subtract(position());
		if (delta.lengthSqr() < 1.2D) {
			// The bat drinks the whole cell: every face of it comes off the count.
			var drunk = server.getBlockState(scent);
			server.removeBlock(scent, false);
			for (net.minecraft.core.Direction face : net.minecraft.core.Direction.values()) {
				if (net.minecraft.world.level.block.MultifaceBlock.hasFace(drunk, face)) {
					com.vincenthuto.hemomancy.common.world.answer.WorldAnswer.stainCleaned(server, scent);
				}
			}
			server.broadcastEntityEvent(this, BITE_EVENT);
			playSound(SoundEvents.BAT_AMBIENT, 0.35F, 1.4F);
			scent = null;
			return true;
		}
		setDeltaMovement(getDeltaMovement().scale(0.75D).add(delta.normalize().scale(0.05D)));
		float facing = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
		setYRot(facing);
		yBodyRot = facing;
		yHeadRot = facing;
		return true;
	}

	private static boolean isWetStain(ServerLevel level, BlockPos pos) {
		var state = level.getBlockState(pos);
		return state.getBlock() instanceof com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock
				&& state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock.AGE)
				== com.vincenthuto.hemomancy.common.world.ground.GroundRunRules.WET;
	}

	/** While a scent leads it, the bat neither wanders to the vanilla bat's random points nor roosts. */
	@Override
	protected void customServerAiStep() {
		if (scent != null || finaleRoost) return;
		super.customServerAiStep();
	}

	/** The stain this bat is flying to drink, or null (tests). */
	public BlockPos scent() {
		return scent;
	}

	private LivingEntity aggregateOwner() {
		if (!(level() instanceof ServerLevel server) || !getPersistentData().hasUUID("CircusSwarmOwner")) return null;
		ServerPlayer owner = server.getServer().getPlayerList().getPlayer(getPersistentData().getUUID("CircusSwarmOwner"));
		return owner != null && owner.isAlive() && owner.level() == level() ? owner : null;
	}

	private LivingEntity bleedingPrey() {
		return level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(8.0D),
				living -> living != this && !(living instanceof VampireBatEntity) && living.isAlive()
						&& living.getHealth() < living.getMaxHealth())
				.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("ManifestedAggregate", isManifestedAggregate());
		if (finaleRoost) tag.putBoolean("FinaleRoost", true);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(AGGREGATE, tag.getBoolean("ManifestedAggregate"));
		if (tag.getBoolean("FinaleRoost")) markFinaleRoost();
	}
}
