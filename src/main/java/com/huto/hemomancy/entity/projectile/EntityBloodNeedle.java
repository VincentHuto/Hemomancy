package com.huto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.PotionInit;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.hutoslib.client.particle.factory.GlowParticleFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityBloodNeedle extends AbstractArrowEntity {

	public EntityBloodNeedle(EntityType<? extends EntityBloodNeedle> type, World worldIn) {
		super(type, worldIn);
	}

	public EntityBloodNeedle(World worldIn, double x, double y, double z) {
		super(EntityInit.blood_needle.get(), x, y, z, worldIn);
	}

	public EntityBloodNeedle(World worldIn, LivingEntity shooter) {
		super(EntityInit.blood_needle.get(), shooter, worldIn);
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.world.isRemote) {
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
