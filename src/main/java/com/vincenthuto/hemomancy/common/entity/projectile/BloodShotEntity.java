package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.manipulation.TendencyDamageCarrier;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.particles.ParticleTypes;
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
		if (orbitOwnerId != null) {
			this.noPhysics = true;
			this.setNoGravity(true);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide && orbitOwnerId != null) tickOrbit();
		else if (!level().isClientSide && homingTargetId != null && homingTicks-- > 0) steerTowardTarget();
		if (this.level().isClientSide) {
			for (int i = 0; i < 2; i++) {
				level().addParticle(
						GlowParticleFactory.createData(new ParticleColor(255 * level().random.nextFloat(), 0, 0)),
						getX() + HLParticleUtils.inRange(-0.1, 0.1), getY() + HLParticleUtils.inRange(-0.1, 0.1),
						getZ() + HLParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);

			}
		}
		if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 25) {
			this.level().broadcastEntityEvent(this, (byte) 0);
			this.remove(RemovalReason.KILLED);
		}

	}

	public void setHomingTarget(@Nullable LivingEntity target, int ticks) {
		this.homingTargetId = target != null ? target.getUUID() : null;
		this.homingTicks = Math.max(0, ticks);
	}

	public void setMortar(boolean mortar) {
		this.mortar = mortar;
	}

	public void configureOrbit(LivingEntity owner, int index) {
		this.orbitOwnerId = owner.getUUID();
		this.orbitIndex = Math.max(0, Math.min(index, 4));
		this.noPhysics = true;
		this.setNoGravity(true);
	}

	public boolean isOrbitingFor(UUID ownerId) {
		return ownerId != null && ownerId.equals(orbitOwnerId);
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
		double angle = tickCount * 0.08D + orbitIndex * Math.PI * 2.0D / 5.0D;
		setPos(owner.getX() + Math.cos(angle) * 1.25D, owner.getY() + 1.55D + Math.sin(angle * 2) * 0.12D,
				owner.getZ() + Math.sin(angle) * 1.25D);
		setDeltaMovement(Vec3.ZERO);
		if (tickCount < orbitIndex * 10 + 1 || (tickCount - orbitIndex * 10 - 1) % 50 != 0) return;
		LivingEntity target = level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(16),
				candidate -> candidate != owner && candidate.isAlive() && !owner.isAlliedTo(candidate)
						&& owner.hasLineOfSight(candidate)).stream()
				.min(Comparator.comparingDouble(owner::distanceToSqr)).orElse(null);
		if (target == null) return;
		Vec3 direction = target.getEyePosition().subtract(position()).normalize();
		orbitOwnerId = null;
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
			target.hurt(server.damageSources().magic(), Math.max(4.0F, damage));
		}
		server.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY(), getZ(), 60, 2.0D, 1.0D, 2.0D, 0.03D);
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
