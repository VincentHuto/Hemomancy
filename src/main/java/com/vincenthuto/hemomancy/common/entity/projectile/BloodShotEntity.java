package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.manipulation.TendencyDamageCarrier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.UUID;

public class BloodShotEntity extends AbstractArrow implements CombatWeaponCarrierProjectile, TendencyDamageCarrier {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> VISUAL_FORM =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BloodShotEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ORBIT_SLOT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BloodShotEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Long> ORBIT_STARTED_AT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BloodShotEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.LONG);
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);builder.define(VISUAL_FORM,0);
        builder.define(ORBIT_SLOT,-1);
        builder.define(ORBIT_STARTED_AT,0L);
    }
    public int visualForm(){return entityData.get(VISUAL_FORM);}
	private ItemStack combatWeaponItem = ItemStack.EMPTY;
	@Nullable
	private EnumBloodTendency damageTendency;
	@Nullable
	private EnumBloodTendency secondaryDamageTendency;
	@Nullable
	private UUID homingTargetId;
	private int homingTicks;
	private boolean mortar;
	@Nullable
	private UUID orbitOwnerId;
	private int orbitIndex;

	public BloodShotEntity(EntityType<? extends BloodShotEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public BloodShotEntity(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_shot.get(), x, y, z, worldIn, new ItemStack(Items.ARROW), (ItemStack) null);
		this.pickup = Pickup.DISALLOWED;
	}

	public BloodShotEntity(Level worldIn, LivingEntity shooter) {
		this(worldIn, shooter, null);
	}

	public BloodShotEntity(Level worldIn, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
		super(EntityInit.blood_shot.get(), shooter, worldIn, new ItemStack(Items.ARROW),
				firedFromWeapon != null && !firedFromWeapon.isEmpty() ? firedFromWeapon : null);
		this.pickup = Pickup.DISALLOWED;
		this.combatWeaponItem = copyCombatWeapon(firedFromWeapon);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
        compound.putInt("VisualForm",visualForm());
		if (!this.combatWeaponItem.isEmpty()) {
			compound.put("CombatWeapon", this.combatWeaponItem.save(this.registryAccess()));
		}
		if (damageTendency != null) compound.putString("DamageTendency", damageTendency.name());
		if (secondaryDamageTendency != null) {
			compound.putString("SecondaryDamageTendency", secondaryDamageTendency.name());
		}
		if (homingTargetId != null) compound.putUUID("HomingTarget", homingTargetId);
		compound.putInt("HomingTicks", homingTicks);
		compound.putBoolean("Mortar", mortar);
		if (orbitOwnerId != null) compound.putUUID("OrbitOwner", orbitOwnerId);
		compound.putInt("OrbitIndex", orbitIndex);

	}

	@Override
	public ItemStack getCombatWeaponItem() {
		return this.combatWeaponItem;
	}

	@Override
	@Nullable
	public EnumBloodTendency getDamageTendency() {
		return damageTendency;
	}

	public void setDamageTendency(@Nullable EnumBloodTendency damageTendency) {
		this.damageTendency = damageTendency;
	}

	@Override
	@Nullable
	public EnumBloodTendency getSecondaryDamageTendency() {
		return secondaryDamageTendency;
	}

	public void setSecondaryDamageTendency(@Nullable EnumBloodTendency secondaryDamageTendency) {
		this.secondaryDamageTendency = secondaryDamageTendency;
	}


	@Override
	protected void doPostHurtEffects(LivingEntity living) {
		super.doPostHurtEffects(living);
		com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles.impact(
				living, damageTendency, secondaryDamageTendency, getDeltaMovement());
		Entity entity = living;
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addEffect(new MobEffectInstance(EffectInit.blood_loss, 1000, 2));

		}

	}

	@Nonnull

	@Override
	protected ItemStack getDefaultPickupItem() {
		// Vanilla 1.21.1 requires a non-empty internal pickup stack when saving arrows.
		// Blood Shots remain non-pickable and render as their own projectile entity.
		return new ItemStack(Items.ARROW);
	}

	@Override
	protected void onHitEntity(EntityHitResult p_213868_1_) {
		if (mortar) {
			explodeMortar();
			return;
		}
		super.onHitEntity(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addEffect(new MobEffectInstance(EffectInit.blood_loss, 1000, 2));

		}

	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		if (mortar) {
			explodeMortar();
			return;
		}
		super.onHitBlock(hit);
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		Entity owner = getOwner();
		if (owner instanceof Mob mob && owner instanceof BoundPuppeteerSummon bound
				&& target instanceof LivingEntity living) {
			return super.canHitEntity(target) && BoundSummonBehavior.canAttack(mob, bound, living);
		}
		return super.canHitEntity(target);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
        entityData.set(VISUAL_FORM,compound.getInt("VisualForm"));
		this.combatWeaponItem = compound.contains("CombatWeapon", 10)
				? ItemStack.parseOptional(this.registryAccess(), compound.getCompound("CombatWeapon"))
				: ItemStack.EMPTY;
		this.damageTendency = readDamageTendency(compound);
		this.secondaryDamageTendency = readTendency(compound, "SecondaryDamageTendency");
		this.homingTargetId = compound.hasUUID("HomingTarget") ? compound.getUUID("HomingTarget") : null;
		this.homingTicks = compound.getInt("HomingTicks");
		this.mortar = compound.getBoolean("Mortar");
		this.orbitOwnerId = compound.hasUUID("OrbitOwner") ? compound.getUUID("OrbitOwner") : null;
		this.orbitIndex = compound.getInt("OrbitIndex");
        if(!compound.contains("VisualForm")) entityData.set(VISUAL_FORM, orbitOwnerId!=null?3:mortar?2:homingTargetId!=null?1:0);
		if (orbitOwnerId != null) {
			entityData.set(ORBIT_SLOT,orbitIndex);
			entityData.set(ORBIT_STARTED_AT,level().getGameTime());
			this.noPhysics = true;
			this.setNoGravity(true);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide && orbitOwnerId != null) tickOrbit();
		else if (!level().isClientSide && homingTargetId != null && homingTicks-- > 0) steerTowardTarget();
		if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 25) {
			this.level().broadcastEntityEvent(this, (byte) 0);
			this.remove(RemovalReason.KILLED);
		}

	}

	public void setHomingTarget(@Nullable LivingEntity target, int ticks) {
        entityData.set(VISUAL_FORM,1);
		this.homingTargetId = target != null ? target.getUUID() : null;
		this.homingTicks = Math.max(0, ticks);
	}

	public void setMortar(boolean mortar) {
        entityData.set(VISUAL_FORM,mortar?2:0);
		this.mortar = mortar;
	}

	public void configureOrbit(LivingEntity owner, int index) {
        entityData.set(VISUAL_FORM,3);
		this.orbitOwnerId = owner.getUUID();
		this.orbitIndex = Math.max(0, Math.min(index, 4));
		setOwner(owner);
		entityData.set(ORBIT_SLOT,this.orbitIndex);
		entityData.set(ORBIT_STARTED_AT,level().getGameTime()-tickCount);
		this.noPhysics = true;
		this.setNoGravity(true);
	}

	public boolean isOrbitingFor(UUID ownerId) {
		return ownerId != null && ownerId.equals(orbitOwnerId);
	}

    public Vec3 visualPosition(float partial) {
        int slot=entityData.get(ORBIT_SLOT);
        Entity owner=getOwner();
        if(slot<0 || owner==null || !owner.isAlive())return getPosition(partial);
        double age=level().getGameTime()-entityData.get(ORBIT_STARTED_AT)-1+partial;
        return owner.getPosition(partial).add(BloodShotOrbit.offset(age,slot));
    }

	private void tickOrbit() {
		if (!(level() instanceof ServerLevel server) || tickCount > 200) {
			discard();
			return;
		}
		Entity entity = server.getEntity(orbitOwnerId);
		if (!(entity instanceof LivingEntity owner) || !owner.isAlive()) {
			discard();
			return;
		}
		setPos(owner.position().add(BloodShotOrbit.offset(tickCount,orbitIndex)));
		setDeltaMovement(Vec3.ZERO);
		if (tickCount < orbitIndex * 10 + 1 || (tickCount - orbitIndex * 10 - 1) % 50 != 0) return;
		LivingEntity target = level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(16),
				candidate -> candidate != owner && candidate.isAlive() && !owner.isAlliedTo(candidate)
						&& owner.hasLineOfSight(candidate)).stream()
				.min(Comparator.comparingDouble(owner::distanceToSqr)).orElse(null);
		if (target == null) return;
		Vec3 direction = target.getEyePosition().subtract(position()).normalize();
		orbitOwnerId = null;
		entityData.set(ORBIT_SLOT,-1);
		entityData.set(VISUAL_FORM,0);
		noPhysics = false;
		setNoGravity(false);
		shoot(direction.x, direction.y, direction.z, 4.5F, 0.5F);
	}

	private void steerTowardTarget() {
		if (!(level() instanceof ServerLevel server) || !(server.getEntity(homingTargetId) instanceof LivingEntity target)
				|| !target.isAlive()) {
			homingTargetId = null;
			return;
		}
		Vec3 motion = getDeltaMovement();
		double speed = motion.length();
		if (speed < 0.01D) return;
		Vec3 desired = target.getEyePosition().subtract(position()).normalize().scale(speed);
		setDeltaMovement(motion.scale(0.85D).add(desired.scale(0.15D)).normalize().scale(speed));
	}

	private void explodeMortar() {
		if (!(level() instanceof ServerLevel server)) return;
		LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
		for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, new AABB(position(), position()).inflate(4),
				candidate -> candidate.isAlive() && candidate != owner
						&& (owner == null || !owner.isAlliedTo(candidate)))) {
			double distance = Math.min(4.0D, target.position().distanceTo(position()));
			float damage = (float) (8.0D - distance);
			if (owner instanceof net.minecraft.world.entity.player.Player player && damageTendency != null) {
				damage *= TendencyAffinityRules.damageMultiplier(player, target, damageTendency, secondaryDamageTendency);
			}
			if (target.hurt(server.damageSources().magic(), Math.max(4.0F, damage)))
				com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles.impact(
						target, damageTendency, secondaryDamageTendency, target.position().subtract(position()));
		}
		com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.burst(server,
                com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.RUPTURE,position(),position(),2,24);
		server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
		discard();
	}

	private static ItemStack copyCombatWeapon(@Nullable ItemStack weaponStack) {
		return weaponStack != null && !weaponStack.isEmpty() ? weaponStack.copy() : ItemStack.EMPTY;
	}

	@Nullable
	private static EnumBloodTendency readDamageTendency(CompoundTag compound) {
		return readTendency(compound, "DamageTendency");
	}

	@Nullable
	private static EnumBloodTendency readTendency(CompoundTag compound, String key) {
		try {
			return compound.contains(key) ? EnumBloodTendency.valueOf(compound.getString(key)) : null;
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

}
