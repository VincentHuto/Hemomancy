package com.vincenthuto.hemomancy.entity.mob;

import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.init.EntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class EntityEnthralledDoll extends Monster {

	LivingEntity owner;

	public EntityEnthralledDoll(EntityType<? extends EntityEnthralledDoll> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityEnthralledDoll(Level worldIn, LivingEntity owner) {
		super(EntityInit.enthralled_doll.get(), worldIn);
		this.owner = owner;

	}

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private boolean leftOwner;

	@Nullable
	public Entity getOwner() {
		if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
			return this.cachedOwner;
		} else if (this.ownerUUID != null && this.level instanceof ServerLevel) {
			this.cachedOwner = ((ServerLevel) this.level).getEntity(this.ownerUUID);
			return this.cachedOwner;
		} else {
			return null;
		}
	}

	public void setOwner(@Nullable Entity pEntity) {
		if (pEntity != null) {
			this.ownerUUID = pEntity.getUUID();
			this.cachedOwner = pEntity;
		}

	}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if (this.ownerUUID != null) {
			pCompound.putUUID("Owner", this.ownerUUID);
		}

		if (this.leftOwner) {
			pCompound.putBoolean("LeftOwner", true);
		}
	}

	protected boolean ownedBy(Entity p_150172_) {
		return p_150172_.getUUID().equals(this.ownerUUID);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		if (pCompound.hasUUID("Owner")) {
			this.ownerUUID = pCompound.getUUID("Owner");
		}

		this.leftOwner = pCompound.getBoolean("LeftOwner");
	}

	@Override
	public void tick() {
		if (!this.leftOwner) {
			this.leftOwner = this.checkLeftOwner();
		}
		super.tick();
	}

	private boolean checkLeftOwner() {
		Entity entity = this.getOwner();
		if (entity != null) {
			for (Entity entity1 : this.level.getEntities(this,
					this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (p_37272_) -> {
						return !p_37272_.isSpectator() && p_37272_.isPickable();
					})) {
				if (entity1.getRootVehicle() == entity.getRootVehicle()) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
			MobSpawnType pReason, SpawnGroupData pSpawnData, CompoundTag pDataTag) {
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);
		// entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.5f);

	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
		/*
		 * if (!(entityIn instanceof EntityDerangedBeast || entityIn instanceof
		 * EntityBeastFromBeyond)) {
		 * entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.5f); }
		 */
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));

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
	public Packet<?> getAddEntityPacket() {
		Entity entity = this.getOwner();
		return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
		super.recreateFromPacket(pPacket);
		Entity entity = this.level.getEntity(pPacket.getData());
		if (entity != null) {
			this.setOwner(entity);
		}

	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
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
}
