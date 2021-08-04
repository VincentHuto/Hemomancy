package com.huto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.PotionInit;
import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityBloodNeedle extends AbstractArrow {

	public EntityBloodNeedle(EntityType<? extends EntityBloodNeedle> type, Level worldIn) {
		super(type, worldIn);
	}

	public EntityBloodNeedle(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_needle.get(), x, y, z, worldIn);
	}

	public EntityBloodNeedle(Level worldIn, LivingEntity shooter) {
		super(EntityInit.blood_needle.get(), shooter, worldIn);
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			for (int i = 0; i < 2; i++) {
				level.addParticle(
						GlowParticleFactory.createData(new ParticleColor(255 * level.random.nextFloat(), 0, 0)),
						getX() + ParticleUtils.inRange(-0.1, 0.1), getY() + ParticleUtils.inRange(-0.1, 0.1),
						getZ() + ParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);

			}
		}
		if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 25) {
			this.level.broadcastEntityEvent(this, (byte) 0);
			this.remove(RemovalReason.KILLED);
		}

	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

	}

	@Override
	public void setSoundEvent(SoundEvent soundIn) {
		super.setSoundEvent(soundIn);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
	}

	@Override
	protected void onHitEntity(EntityHitResult p_213868_1_) {
		super.onHitEntity(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addEffect(new MobEffectInstance(PotionInit.blood_loss.get(), 1000, 2));

		}

	}

	@Override
	protected void doPostHurtEffects(LivingEntity living) {
		super.doPostHurtEffects(living);
		Entity entity = living;
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addEffect(new MobEffectInstance(PotionInit.blood_loss.get(), 1000, 2));

		}

	}

	@Override
	protected ItemStack getPickupItem() {
		return ItemStack.EMPTY;
	}

}
