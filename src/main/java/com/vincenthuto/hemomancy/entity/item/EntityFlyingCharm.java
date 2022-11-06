package com.vincenthuto.hemomancy.entity.item;

import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityFlyingCharm extends Entity implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData
			.defineId(EntityFlyingCharm.class, EntityDataSerializers.ITEM_STACK);
	protected static float lerpRotation(float p_37274_, float p_37275_) {
		while (p_37275_ - p_37274_ < -180.0F) {
			p_37274_ -= 360.0F;
		}

		while (p_37275_ - p_37274_ >= 180.0F) {
			p_37274_ += 360.0F;
		}

		return Mth.lerp(0.2F, p_37274_, p_37275_);
	}
	private double tx;
	private double ty;
	private double tz;

	private int life;

	public EntityFlyingCharm(EntityType<? extends EntityFlyingCharm> p_36957_, Level p_36958_) {
		super(p_36957_, p_36958_);
	}

	public EntityFlyingCharm(Level p_36960_, double p_36961_, double p_36962_, double p_36963_) {
		this(EntityInit.flying_charm.get(), p_36960_);
		this.setPos(p_36961_, p_36962_, p_36963_);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		ItemStack itemstack = this.getItemRaw();
		if (!itemstack.isEmpty()) {
			pCompound.put("Item", itemstack.save(new CompoundTag()));
		}

	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}

	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	public ItemStack getItem() {
		ItemStack itemstack = this.getItemRaw();
		return itemstack.isEmpty() ? new ItemStack(ItemInit.charm_of_vascularium.get()) : itemstack;
	}

	private ItemStack getItemRaw() {
		return this.getEntityData().get(DATA_ITEM_STACK);
	}

	/**
	 * Returns true if it's possible to attack this entity with an item.
	 */
	@Override
	public boolean isAttackable() {
		return false;
	}

	/**
	 * Updates the entity motion clientside, called by packets from the server
	 */
	@Override
	public void lerpMotion(double pX, double pY, double pZ) {
		this.setDeltaMovement(pX, pY, pZ);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			double d0 = Math.sqrt(pX * pX + pZ * pZ);
			this.setYRot((float) (Mth.atan2(pX, pZ) * (180F / (float) Math.PI)));
			this.setXRot((float) (Mth.atan2(pY, d0) * (180F / (float) Math.PI)));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		ItemStack itemstack = ItemStack.of(pCompound.getCompound("Item"));
		this.setItem(itemstack);
	}

	public void setItem(ItemStack p_36973_) {
		if (!p_36973_.is(ItemInit.charm_of_vascularium.get()) || p_36973_.hasTag()) {
			this.getEntityData().set(DATA_ITEM_STACK, Util.make(p_36973_.copy(), (p_36978_) -> {
				p_36978_.setCount(1);
			}));
		}

	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@Override
	public boolean shouldRenderAtSqrDistance(double pDistance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}

		d0 *= 64.0D;
		return pDistance < d0 * d0;
	}

	public void signalTo(BlockPos pPos) {
		double d0 = pPos.getX();
		int i = pPos.getY();
		double d1 = pPos.getZ();
		double d2 = d0 - this.getX();
		double d3 = d1 - this.getZ();
		double d4 = Math.sqrt(d2 * d2 + d3 * d3);
		if (d4 > 12.0D) {
			this.tx = this.getX() + d2 / d4 * 12.0D;
			this.tz = this.getZ() + d3 / d4 * 12.0D;
			this.ty = this.getY() + 8.0D;
		} else {
			this.tx = d0;
			this.ty = i;
			this.tz = d1;
		}

		this.life = 0;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void tick() {
		super.tick();
		Vec3 vec3 = this.getDeltaMovement();
		double d0 = this.getX() + vec3.x;
		double d1 = this.getY() + vec3.y;
		double d2 = this.getZ() + vec3.z;
		double d3 = vec3.horizontalDistance();
		this.setXRot(lerpRotation(this.xRotO, (float) (Mth.atan2(vec3.y, d3) * (180F / (float) Math.PI))));
		this.setYRot(lerpRotation(this.yRotO, (float) (Mth.atan2(vec3.x, vec3.z) * (180F / (float) Math.PI))));
		if (!this.level.isClientSide) {
			double d4 = this.tx - d0;
			double d5 = this.tz - d2;
			float f = (float) Math.sqrt(d4 * d4 + d5 * d5);
			float f1 = (float) Mth.atan2(d5, d4);
			double d6 = Mth.lerp(0.0025D, d3, f);
			double d7 = vec3.y;
			if (f < 1.0F) {
				d6 *= 0.8D;
				d7 *= 0.8D;
			}

			int j = this.getY() < this.ty ? 1 : -1;
			vec3 = new Vec3(Math.cos(f1) * d6, d7 + (j - d7) * 0.015F,
					Math.sin(f1) * d6);
			this.setDeltaMovement(vec3);
		}

		if (this.isInWater()) {
			for (int i = 0; i < 4; ++i) {
				this.level.addParticle(ParticleTypes.BUBBLE, d0 - vec3.x * 0.25D, d1 - vec3.y * 0.25D,
						d2 - vec3.z * 0.25D, vec3.x, vec3.y, vec3.z);
			}
		} else {
			this.level.addParticle(GlowParticleFactory.createData(ParticleColor.BLOOD),
					d0 - vec3.x * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, d1 - vec3.y * 0.25D - 0.5D,
					d2 - vec3.z * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, vec3.x, vec3.y, vec3.z);
		}

		if (!this.level.isClientSide) {
			this.setPos(d0, d1, d2);
			++this.life;
			if (this.life > 180 && !this.level.isClientSide) {
				this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
				this.discard();
				this.level.levelEvent(2003, this.blockPosition(), 0);
			}
		} else {
			this.setPosRaw(d0, d1, d2);
		}

	}
}