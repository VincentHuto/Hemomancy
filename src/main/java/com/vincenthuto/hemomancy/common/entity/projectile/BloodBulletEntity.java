package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BloodBulletEntity extends AbstractArrow implements CombatWeaponCarrierProjectile {
	private ItemStack combatWeaponItem = ItemStack.EMPTY;

	public BloodBulletEntity(EntityType<? extends BloodBulletEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public BloodBulletEntity(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_bullet.get(), x, y, z, worldIn, ItemStack.EMPTY, (ItemStack) null);
	}

	public BloodBulletEntity(Level worldIn, LivingEntity shooter) {
		this(worldIn, shooter, null);
	}

	public BloodBulletEntity(Level worldIn, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
		super(EntityInit.blood_bullet.get(), shooter, worldIn, ItemStack.EMPTY,
				firedFromWeapon != null && !firedFromWeapon.isEmpty() ? firedFromWeapon : null);
		this.combatWeaponItem = copyCombatWeapon(firedFromWeapon);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (!this.combatWeaponItem.isEmpty()) {
			compound.put("CombatWeapon", this.combatWeaponItem.save(this.registryAccess()));
		}

	}

	@Override
	public ItemStack getCombatWeaponItem() {
		return this.combatWeaponItem;
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
		return ItemStack.EMPTY;
	}

	@Override
	protected void onHit(@Nonnull HitResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
//			if (!level().isClientSide) {
//				this.level().explode(this, this.getX(), this.getY() + this.getBbHeight() / 16.0F, this.getZ(), 1.0F,
//						ExplosionInteraction.NONE);
//			}
			// this.remove(RemovalReason.KILLED);
			break;
		}
		case ENTITY: {
			if (!level().isClientSide) {
				this.level().explode(this, this.getX(), this.getY() + this.getBbHeight() / 16.0F, this.getZ(), 4.0F,
						ExplosionInteraction.NONE);
			}
			this.remove(RemovalReason.KILLED);
			break;
		}
		default: {
			this.remove(RemovalReason.KILLED);
			break;
		}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult p_213868_1_) {
		super.onHitEntity(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addEffect(new MobEffectInstance(EffectInit.blood_loss, 1000, 2));

		}

	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.combatWeaponItem = compound.contains("CombatWeapon", 10)
				? ItemStack.parseOptional(this.registryAccess(), compound.getCompound("CombatWeapon"))
				: ItemStack.EMPTY;
	}

	@Override
	public void setSoundEvent(SoundEvent soundIn) {
		super.setSoundEvent(soundIn);
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
				level().addParticle(
						BloodCellParticleFactory.createData(new ParticleColor(255 * level().random.nextFloat(), 0, 0)),
						getX() + HLParticleUtils.inRange(-0.1, 0.1), getY() + HLParticleUtils.inRange(-0.1, 0.1),
						getZ() + HLParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);


				Random rand = new Random();
				Vector3 endVec = Vector3.fromEntityCenter(this).add(rand.nextDouble() - rand.nextDouble(),
						rand.nextDouble() - rand.nextDouble(), rand.nextDouble() - rand.nextDouble());
				Vec3 speedVec = new Vec3(endVec.x, endVec.y, endVec.z);

			HLPacketHandler.sendLightningSpawn(this.position().add(0.5, 0.5, 0.5), speedVec, 64.0f,
						this.level().dimension(), ParticleColor.RED, 3, 10, 9, 1.2f);
			}
		}

		if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 50) {
			this.level().broadcastEntityEvent(this, (byte) 0);
			this.remove(RemovalReason.KILLED);
		}

	}

	private static ItemStack copyCombatWeapon(@Nullable ItemStack weaponStack) {
		return weaponStack != null && !weaponStack.isEmpty() ? weaponStack.copy() : ItemStack.EMPTY;
	}

}
