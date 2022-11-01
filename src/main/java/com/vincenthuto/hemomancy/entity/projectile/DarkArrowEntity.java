package com.vincenthuto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.DarkGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

public class DarkArrowEntity extends AbstractArrow {

	public DarkArrowEntity(EntityType<? extends DarkArrowEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public DarkArrowEntity(Level worldIn, double x, double y, double z) {
		super(EntityInit.dark_arrow.get(), x, y, z, worldIn);
	}

	LivingEntity shooter;

	public DarkArrowEntity(Level worldIn, LivingEntity shooter) {
		super(EntityInit.dark_arrow.get(), shooter, worldIn);
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

	@Override
	@SuppressWarnings("unused")
	public void tick() {
		super.tick();
		if (shooter instanceof Player) {
			Player player = (Player) shooter;
			IBloodTendency tend = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(NullPointerException::new);
			ParticleColor color = ParticleColor.BLACK;
			ServerLevel sLevel = (ServerLevel) level;

			sLevel.sendParticles(DarkGlowParticleFactory.createData(color), getX() + HLParticleUtils.inRange(-0.05, 0.05),
					getY() + HLParticleUtils.inRange(-0.05, 0.05), getZ() + HLParticleUtils.inRange(-0.05, 0.05), 20, 0.005,
					0.3, 0.005, 0.001d);

			sLevel.sendParticles(DarkGlowParticleFactory.createData(color), getX() + HLParticleUtils.inRange(-0.05, 0.05),
					getY() + HLParticleUtils.inRange(-0.05, 0.05), getZ() + HLParticleUtils.inRange(-0.05, 0.05), 20, 0.005,
					-0.3, 0.0005, 0.001d);

			if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 100) {
				this.level.broadcastEntityEvent(this, (byte) 0);
				this.remove(RemovalReason.KILLED);
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
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			// ((LivingEntity) entity).addPotionEffect(new
			// MobEffectInstance(PotionInit.blood_loss.get(), 1000, 2));

		}

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
