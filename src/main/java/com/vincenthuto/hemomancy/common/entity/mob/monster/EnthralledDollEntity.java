package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EnthralledDollEntity extends Monster implements OwnableEntity {

	protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(EnthralledDollEntity.class,
			EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData
			.defineId(EnthralledDollEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	protected static final EntityDataAccessor<Boolean> DATA_SUMMONED_BY_PUPPETEER =
			SynchedEntityData.defineId(EnthralledDollEntity.class, EntityDataSerializers.BOOLEAN);

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	public EnthralledDollEntity(EntityType<? extends EnthralledDollEntity> type, Level worldIn) {
		super(type, worldIn);
		this.reassessTameGoals();

	}

	public EnthralledDollEntity(Level worldIn, LivingEntity owner) {
		super(EntityInit.enthralled_doll.get(), worldIn);
		setOwnerUUID(owner.getUUID());
		setSummonedByPuppeteer(owner instanceof BloodDrunkPuppeteerEntity);
		this.reassessTameGoals();
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
	      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));

		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));

	}

	public BloodDrunkPuppeteerEntity getPuppeteer() {
		UUID ownerId = getOwnerUUID();
		if (ownerId == null) {
			return null;
		}
		List<BloodDrunkPuppeteerEntity> owners = level().getEntitiesOfClass(BloodDrunkPuppeteerEntity.class,
				getBoundingBox().inflate(BloodDrunkPuppeteerTuning.DOLL_OWNER_SEARCH_RANGE));
		Optional<BloodDrunkPuppeteerEntity> owner = owners.stream()
				.filter(o -> o.getUUID().equals(ownerId) && o.isAlive())
				.findFirst();
		if (owner.isPresent()) {
			return owner.get();
		}
		return null;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide) {
			tickPuppeteerBond();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if (this.getOwnerUUID() != null) {
			pCompound.putUUID("Owner", this.getOwnerUUID());
		}
		pCompound.putBoolean("SummonedByPuppeteer", isSummonedByPuppeteer());

	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_FLAGS_ID, (byte) 0);
		builder.define(DATA_OWNERUUID_ID, Optional.empty());
		builder.define(DATA_SUMMONED_BY_PUPPETEER, false);

	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_ENTHRALLED_DOLL_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_ENTHRALLED_DOLL_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_ENTHRALLED_DOLL_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	public boolean mayInteract(Level pLevel, BlockPos pPos) {
		Entity entity = this.getOwner();
		if (entity instanceof Player) {
			return entity.mayInteract(pLevel, pPos);
		} else {
			return entity == null ;
		}
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);
		// entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.5f);

	}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		UUID uuid;
		if (pCompound.hasUUID("Owner")) {
			uuid = pCompound.getUUID("Owner");
		} else {
			String s = pCompound.getString("Owner");
			uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
		}

		if (uuid != null) {
			try {
				this.setOwnerUUID(uuid);
				this.setTame(true);
			} catch (Throwable throwable) {
				this.setTame(false);
			}
		}
		if (pCompound.contains("SummonedByPuppeteer")) {
			setSummonedByPuppeteer(pCompound.getBoolean("SummonedByPuppeteer"));
		}
	}

	public boolean isTame() {
		return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
	}

	public void setTame(boolean pTamed) {
		byte b0 = this.entityData.get(DATA_FLAGS_ID);
		if (pTamed) {
			this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | 4));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & -5));
		}

		this.reassessTameGoals();
	}

	protected void reassessTameGoals() {
	}

	@Override
	@Nullable
	public UUID getOwnerUUID() {
		return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID) null);
	}

	public void setOwnerUUID(@Nullable UUID pUuid) {
		this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(pUuid));
	}

	public boolean isOwnedBy(LivingEntity pEntity) {
		return pEntity == this.getOwner();
	}

	public boolean isSummonedByPuppeteer() {
		return this.entityData.get(DATA_SUMMONED_BY_PUPPETEER);
	}

	public void setSummonedByPuppeteer(boolean summonedByPuppeteer) {
		this.entityData.set(DATA_SUMMONED_BY_PUPPETEER, summonedByPuppeteer);
		if (summonedByPuppeteer) {
			this.xpReward = 0;
		}
	}

	@Override
	public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
		return !(target instanceof BloodDrunkPuppeteerEntity)
				&& !(target instanceof EnthralledDollEntity)
				&& super.canAttack(target);
	}

	@Override
	protected boolean shouldDropLoot() {
		return (BloodDrunkPuppeteerTuning.SUMMONED_DOLLS_DROP_LOOT || !isSummonedByPuppeteer())
				&& super.shouldDropLoot();
	}

	@Override
	protected int getBaseExperienceReward() {
		return isSummonedByPuppeteer() ? 0 : super.getBaseExperienceReward();
	}

	private void tickPuppeteerBond() {
		if (!isSummonedByPuppeteer()) {
			return;
		}
		BloodDrunkPuppeteerEntity puppeteer = getPuppeteer();
		if (puppeteer == null) {
			discard();
			return;
		}
		LivingEntity puppeteerTarget = puppeteer.getTarget();
		if (puppeteerTarget != null && canAttack(puppeteerTarget)) {
			setTarget(puppeteerTarget);
		}
		double distanceToPuppeteer = distanceToSqr(puppeteer);
		double teleportDistance = BloodDrunkPuppeteerTuning.DOLL_TELEPORT_DISTANCE;
		if (distanceToPuppeteer > teleportDistance * teleportDistance) {
			double[] offset = BloodDrunkPuppeteerTuning.dollSpawnOffset(getId());
			moveTo(puppeteer.getX() + offset[0], puppeteer.getY() + offset[1],
					puppeteer.getZ() + offset[2], getYRot(), getXRot());
			getNavigation().stop();
			return;
		}
		double followDistance = BloodDrunkPuppeteerTuning.DOLL_FOLLOW_START_DISTANCE;
		if ((getTarget() == null || !getTarget().isAlive())
				&& distanceToPuppeteer > followDistance * followDistance) {
			getNavigation().moveTo(puppeteer, BloodDrunkPuppeteerTuning.DOLL_FOLLOW_SPEED);
		}
	}
}
