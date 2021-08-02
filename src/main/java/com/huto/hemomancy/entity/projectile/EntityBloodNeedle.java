package com.huto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.PotionInit;
import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			for (int i = 0; i < 2; i++) {
				world.addParticle(GlowParticleFactory.createData(new ParticleColor(255 * world.rand.nextFloat(), 0, 0)),
						getPosX() + ParticleUtils.inRange(-0.1, 0.1), getPosY() + ParticleUtils.inRange(-0.1, 0.1),
						getPosZ() + ParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);

			}
		}
		if (this.inGround && this.timeInGround != 0 && this.timeInGround >= 25) {
			this.world.setEntityState(this, (byte) 0);
			this.remove();
		}

	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);

	}

	@Override
	public void setHitSound(SoundEvent soundIn) {
		super.setHitSound(soundIn);
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
	}

	@Override
	protected void onEntityHit(EntityRayTraceResult p_213868_1_) {
		super.onEntityHit(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addPotionEffect(new EffectInstance(PotionInit.blood_loss.get(), 1000, 2));

		}

	}

	@Override
	protected void arrowHit(LivingEntity living) {
		super.arrowHit(living);
		Entity entity = living.getEntity();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addPotionEffect(new EffectInstance(PotionInit.blood_loss.get(), 1000, 2));

		}

	}

	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY;
	}

}
