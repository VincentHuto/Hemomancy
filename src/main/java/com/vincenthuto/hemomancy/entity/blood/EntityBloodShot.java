package com.vincenthuto.hemomancy.entity.blood;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityBloodShot extends AbstractArrow {

	public EntityBloodShot(EntityType<? extends EntityBloodShot> type, Level worldIn) {
		super(type, worldIn);
	}

	public EntityBloodShot(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_shot.get(), x, y, z, worldIn);
	}

	LivingEntity shooter;

	public EntityBloodShot(Level worldIn, LivingEntity shooter) {
		super(EntityInit.blood_shot.get(), shooter, worldIn);
		this.shooter = shooter;
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@SuppressWarnings("unused")
	@Override
	public void tick() {
		super.tick();
		if (shooter != null) {
			if (shooter instanceof Player) {
				Player player = (Player) shooter;
//				IBloodTendency tend = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
//						.orElseThrow(NullPointerException::new);
				ParticleColor color = ParticleColor.BLOOD;
				if (level.isClientSide) {
					level.addParticle(BloodCellParticleFactory.createData(color),
							getX() + HLParticleUtils.inRange(-0.05, 0.05),
							getY() + HLParticleUtils.inRange(-0.05, 0.05),
							getZ() + HLParticleUtils.inRange(-0.05, 0.05), 0, 0.0, 0.1);

				}
				if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 100) {
					this.level.broadcastEntityEvent(this, (byte) 0);
					this.remove(RemovalReason.KILLED);
				}
			}
		}

	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);

	}

	@Override
	protected void onHitEntity(EntityHitResult p_213868_1_) {
		super.onHitEntity(p_213868_1_);
		// Entity entity = p_213868_1_.getEntity();
		/*
		 * if (entity instanceof LivingEntity) { ((LivingEntity)
		 * entity).addPotionEffect(new MobEffectInstance(PotionInit.blood_loss.get(),
		 * 1000, 2));
		 * 
		 * }
		 */

	}

	@Override
	protected void doPostHurtEffects(LivingEntity living) {
		super.doPostHurtEffects(living);

	}

	@Override
	protected ItemStack getPickupItem() {
		return ItemStack.EMPTY;
	}

}
