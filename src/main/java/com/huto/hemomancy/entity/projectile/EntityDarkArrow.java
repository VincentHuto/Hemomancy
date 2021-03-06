package com.huto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.init.EntityInit;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.hutoslib.client.particle.factory.DarkGlowParticleFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityDarkArrow extends AbstractArrowEntity {

	public EntityDarkArrow(EntityType<? extends EntityDarkArrow> type, World worldIn) {
		super(type, worldIn);
	}

	public EntityDarkArrow(World worldIn, double x, double y, double z) {
		super(EntityInit.dark_arrow.get(), x, y, z, worldIn);
	}

	LivingEntity shooter;

	public EntityDarkArrow(World worldIn, LivingEntity shooter) {
		super(EntityInit.dark_arrow.get(), shooter, worldIn);
		this.shooter = shooter;
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	protected void registerData() {
		super.registerData();
	}

	@SuppressWarnings("unused")
	public void tick() {
		super.tick();
		if (shooter instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) shooter;
			IBloodTendency tend = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(NullPointerException::new);
			ParticleColor color = ParticleColor.BLACK;
			ServerWorld sWorld = (ServerWorld) world;

			sWorld.spawnParticle(DarkGlowParticleFactory.createData(color), getPosX() + ParticleUtils.inRange(-0.05, 0.05),
					getPosY() + ParticleUtils.inRange(-0.05, 0.05), getPosZ() + ParticleUtils.inRange(-0.05, 0.05), 20,
					0.005, 0.3, 0.005, 0.001d);

			sWorld.spawnParticle(DarkGlowParticleFactory.createData(color), getPosX() + ParticleUtils.inRange(-0.05, 0.05),
					getPosY() + ParticleUtils.inRange(-0.05, 0.05), getPosZ() + ParticleUtils.inRange(-0.05, 0.05), 20,
					0.005, -0.3, 0.0005, 0.001d);

			if (this.inGround && this.timeInGround != 0 && this.timeInGround >= 100) {
				this.world.setEntityState(this, (byte) 0);
				remove();
			}
		}

	}

	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);

	}

	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

	}

	@Override
	protected void onEntityHit(EntityRayTraceResult p_213868_1_) {
		super.onEntityHit(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			//((LivingEntity) entity).addPotionEffect(new EffectInstance(PotionInit.blood_loss.get(), 1000, 2));

		}

	}

	protected void arrowHit(LivingEntity living) {
		super.arrowHit(living);

	}

	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY;
	}

}
