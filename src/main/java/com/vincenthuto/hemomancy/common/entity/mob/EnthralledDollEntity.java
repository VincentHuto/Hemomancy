package com.vincenthuto.hemomancy.common.entity.mob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class EnthralledDollEntity extends Monster implements OwnableEntity {

	protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class,
			EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData
			.defineId(TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID);

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
		List<BloodDrunkPuppeteerEntity> owners = level().getNearbyEntities(BloodDrunkPuppeteerEntity.class,
				TargetingConditions.DEFAULT, this, getBoundingBox().inflate(12.0D, 6.0D, 12.0D));
		Optional<BloodDrunkPuppeteerEntity> owner = owners.stream().filter(o -> o.getUUID().equals(getOwnerUUID()))
				.findFirst();
		if (owner.isPresent()) {
			return owner.get();
		}
		return null;
	}

	@Override
	public void tick() {
		super.tick();
		if(this.getPuppeteer() == null) {
			this.kill();
		}

	}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if (this.getOwnerUUID() != null) {
			pCompound.putUUID("Owner", this.getOwnerUUID());
		}

	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte) 0);
		this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());

	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		Entity entity = this.getOwner();
		return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WOLF_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WOLF_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WOLF_HURT;
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
			return entity == null || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, entity);
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
}
