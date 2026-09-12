package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.TendencyDamageCarrier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BloodNeedleEntity extends AbstractArrow implements CombatWeaponCarrierProjectile, TendencyDamageCarrier {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> CORONATION_SWORD =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BloodNeedleEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> PIERCE_STYLE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BloodNeedleEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BYTE);

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CORONATION_SWORD, false);
        builder.define(PIERCE_STYLE, (byte)0);
    }

    public boolean isCoronationSword() { return entityData.get(CORONATION_SWORD); }
    public boolean isLanceNeedle() { return entityData.get(PIERCE_STYLE) >= 3; }
    public float swordOpacity() { return Math.max(0, 1 - inGroundTime / 25F); }
    public void setCoronationSword(boolean sword) { entityData.set(CORONATION_SWORD, sword); }

    @Override
    protected void onHit(net.minecraft.world.phys.HitResult hit) {
        super.onHit(hit);
        if (isCoronationSword() && level() instanceof net.minecraft.server.level.ServerLevel server) {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.burst(server,
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.ANIMUS_IMPACT,
                    hit.getLocation(), hit.getLocation().add(getDeltaMovement().normalize()), 1, 16);
        }
    }
	private ItemStack combatWeaponItem = ItemStack.EMPTY;
	private boolean bloodburstNeedle = false;
	private byte configuredPierceLevel;
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
		this(EntityInit.blood_needle.get(), worldIn, shooter, firedFromWeapon);
	}

	protected BloodNeedleEntity(EntityType<? extends BloodNeedleEntity> type, Level worldIn, LivingEntity shooter,
			@Nullable ItemStack firedFromWeapon) {
		super(type, shooter, worldIn, new ItemStack(Items.ARROW),
				firedFromWeapon != null && !firedFromWeapon.isEmpty() ? firedFromWeapon : null);
		this.pickup = Pickup.DISALLOWED;
		this.combatWeaponItem = copyCombatWeapon(firedFromWeapon);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
        compound.putBoolean("CoronationSword", isCoronationSword());
		if (!this.combatWeaponItem.isEmpty()) {
			compound.put("CombatWeapon", this.combatWeaponItem.save(this.registryAccess()));
		}
		compound.putBoolean("BloodburstNeedle", this.bloodburstNeedle);
		compound.putByte("ConfiguredPierceLevel", configuredPierceLevel);
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
        if (com.vincenthuto.hemomancy.common.damage.SchoolDamage.projectileContext(this) == null)
            living.addEffect(new MobEffectInstance(EffectInit.blood_loss, 1000, 2));
		com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles.impact(
				living, damageTendency, secondaryDamageTendency, getDeltaMovement());
		Entity entity = living;
		if (entity instanceof LivingEntity) {
			applyBloodburstEffects((LivingEntity) entity);

		}

	}

	@Nonnull

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
        setCoronationSword(compound.getBoolean("CoronationSword"));
		this.combatWeaponItem = compound.contains("CombatWeapon", 10)
				? ItemStack.parseOptional(this.registryAccess(), compound.getCompound("CombatWeapon"))
				: ItemStack.EMPTY;
		this.bloodburstNeedle = compound.getBoolean("BloodburstNeedle");
		configurePiercing(compound.getByte("ConfiguredPierceLevel"));
		this.damageTendency = readDamageTendency(compound);
		this.secondaryDamageTendency = readTendency(compound, "SecondaryDamageTendency");
	}

	public void setBloodburstNeedle(boolean bloodburstNeedle) {
		this.bloodburstNeedle = bloodburstNeedle;
	}

	public void configurePiercing(byte pierceLevel) {
		this.configuredPierceLevel = pierceLevel;
        entityData.set(PIERCE_STYLE,pierceLevel);
	}

	@Override
	public byte getPierceLevel() {
		return (byte) Math.max(super.getPierceLevel(), configuredPierceLevel);
	}

	@Override
	public void tick() {
		super.tick();
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
