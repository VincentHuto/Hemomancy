package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.TendencyDamageCarrier;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BloodNeedleEntity extends AbstractArrow implements CombatWeaponCarrierProjectile, TendencyDamageCarrier {
	private ItemStack combatWeaponItem = ItemStack.EMPTY;
	private boolean bloodburstNeedle = false;
	@Nullable
	private EnumBloodTendency damageTendency;
	@Nullable
	private EnumBloodTendency secondaryDamageTendency;

	public BloodNeedleEntity(EntityType<? extends BloodNeedleEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public BloodNeedleEntity(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_needle.get(), x, y, z, worldIn, new ItemStack(Items.ARROW), (ItemStack) null);
		this.pickup = Pickup.DISALLOWED;
	}

	public BloodNeedleEntity(Level worldIn, LivingEntity shooter) {
		this(worldIn, shooter, null);
	}

	public BloodNeedleEntity(Level worldIn, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
		super(EntityInit.blood_needle.get(), shooter, worldIn, new ItemStack(Items.ARROW),
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
		compound.putBoolean("BloodburstNeedle", this.bloodburstNeedle);
		if (damageTendency != null) compound.putString("DamageTendency", damageTendency.name());
		if (secondaryDamageTendency != null) {
			compound.putString("SecondaryDamageTendency", secondaryDamageTendency.name());
		}

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
			applyBloodburstEffects((LivingEntity) entity);

		}

	}

	@Nonnull

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	protected void onHitEntity(EntityHitResult p_213868_1_) {
		super.onHitEntity(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addEffect(new MobEffectInstance(EffectInit.blood_loss, 1000, 2));
			applyBloodburstEffects((LivingEntity) entity);

		}

	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.combatWeaponItem = compound.contains("CombatWeapon", 10)
				? ItemStack.parseOptional(this.registryAccess(), compound.getCompound("CombatWeapon"))
				: ItemStack.EMPTY;
		this.bloodburstNeedle = compound.getBoolean("BloodburstNeedle");
		this.damageTendency = readDamageTendency(compound);
		this.secondaryDamageTendency = readTendency(compound, "SecondaryDamageTendency");
	}

	public void setBloodburstNeedle(boolean bloodburstNeedle) {
		this.bloodburstNeedle = bloodburstNeedle;
	}

	@Override
	public void tick() {
		super.tick();
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

	private void applyBloodburstEffects(LivingEntity target) {
		if (!this.bloodburstNeedle) {
			return;
		}
		target.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER, 160, 1, false, true, true));
		target.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 100, 0, false, true, true));
	}

}
