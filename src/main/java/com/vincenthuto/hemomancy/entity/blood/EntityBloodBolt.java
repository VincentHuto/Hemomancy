package com.vincenthuto.hemomancy.entity.blood;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityBloodBolt extends AbstractArrow {
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(EntityBloodBolt.class,
			EntityDataSerializers.INT);
	private Potion potion = Potions.EMPTY;
	private final Set<MobEffectInstance> customPotionEffects = Sets.newHashSet();
	private boolean fixedColor;

	public EntityBloodBolt(EntityType<? extends EntityBloodBolt> type, Level worldIn) {
		super(type, worldIn);
	}

	public EntityBloodBolt(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_bolt.get(), x, y, z, worldIn);
	}

	public EntityBloodBolt(Level worldIn, LivingEntity shooter) {
		super(EntityInit.blood_bolt.get(), shooter, worldIn);
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public void setPotionEffect(ItemStack stack) {
		if (stack.getItem() == Items.TIPPED_ARROW) {
			this.potion = PotionUtils.getPotion(stack);
			Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(stack);
			if (!collection.isEmpty()) {
				for (MobEffectInstance effectinstance : collection) {
					this.customPotionEffects.add(new MobEffectInstance(effectinstance));
				}
			}

			int i = getCustomColor(stack);
			if (i == -1) {
				this.refreshColor();
			} else {
				this.setFixedColor(i);
			}
		} else if (stack.getItem() == Items.ARROW) {
			this.potion = Potions.EMPTY;
			this.customPotionEffects.clear();
			this.entityData.set(COLOR, -1);
		}

	}

	public static int getCustomColor(ItemStack p_191508_0_) {
		CompoundTag CompoundTag = p_191508_0_.getTag();
		return CompoundTag != null && CompoundTag.contains("CustomPotionColor", 99)
				? CompoundTag.getInt("CustomPotionColor")
				: -1;
	}

	private void refreshColor() {
		this.fixedColor = false;
		if (this.potion == Potions.EMPTY && this.customPotionEffects.isEmpty()) {
			this.entityData.set(COLOR, -1);
		} else {
			this.entityData.set(COLOR,
					PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.customPotionEffects)));
		}

	}

	public void addEffect(MobEffectInstance effect) {
		this.customPotionEffects.add(effect);
		this.getEntityData().set(COLOR,
				PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.customPotionEffects)));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(COLOR, -1);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			for (int i = 0; i < 2; i++) {
				level.addParticle(
						GlowParticleFactory.createData(new ParticleColor(255 * level.random.nextFloat(), 0, 0)),
						getX() + HLParticleUtils.inRange(-0.1, 0.1), getY() + HLParticleUtils.inRange(-0.1, 0.1),
						getZ() + HLParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);

			}
			if (this.inGround) {
				if (this.inGroundTime % 5 == 0) {
					this.spawnPotionParticles(1);
				}
			} else {
				this.spawnPotionParticles(2);
			}
		} else if (this.inGround && this.inGroundTime != 0 && !this.customPotionEffects.isEmpty()
				&& this.inGroundTime >= 600) {
			this.level.broadcastEntityEvent(this, (byte) 0);
			this.potion = Potions.EMPTY;
			this.customPotionEffects.clear();
			this.entityData.set(COLOR, -1);
		}

	}

	private void spawnPotionParticles(int particleCount) {
		int i = this.getColor();
		if (i != -1 && particleCount > 0) {
			double d0 = (i >> 16 & 255) / 255.0D;
			double d1 = (i >> 8 & 255) / 255.0D;
			double d2 = (i >> 0 & 255) / 255.0D;

			for (int j = 0; j < particleCount; ++j) {
				this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(),
						this.getRandomZ(0.5D), d0, d1, d2);
			}

		}
	}

	public int getColor() {
		return this.entityData.get(COLOR);
	}

	private void setFixedColor(int p_191507_1_) {
		this.fixedColor = true;
		this.entityData.set(COLOR, p_191507_1_);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (this.potion != Potions.EMPTY && this.potion != null) {
			compound.putString("Potion", Registry.POTION.getKey(this.potion).toString());
		}

		if (this.fixedColor) {
			compound.putInt("Color", this.getColor());
		}

		if (!this.customPotionEffects.isEmpty()) {
			ListTag listnbt = new ListTag();

			for (MobEffectInstance effectinstance : this.customPotionEffects) {
				listnbt.add(effectinstance.save(new CompoundTag()));
			}

			compound.put("CustomPotionEffects", listnbt);
		}

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Potion", 8)) {
			this.potion = PotionUtils.getPotion(compound);
		}

		for (MobEffectInstance effectinstance : PotionUtils.getCustomEffects(compound)) {
			this.addEffect(effectinstance);
		}

		if (compound.contains("Color", 99)) {
			this.setFixedColor(compound.getInt("Color"));
		} else {
			this.refreshColor();
		}

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
		if (this.customPotionEffects.isEmpty() && this.potion == Potions.EMPTY) {
			return new ItemStack(ItemInit.blood_bolt.get());
		} else {
			ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
			PotionUtils.setPotion(itemstack, this.potion);
			PotionUtils.setCustomEffects(itemstack, this.customPotionEffects);
			if (this.fixedColor) {
				itemstack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
			}

			return itemstack;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleEntityEvent(byte id) {
		if (id == 0) {
			int i = this.getColor();
			if (i != -1) {
				double d0 = (i >> 16 & 255) / 255.0D;
				double d1 = (i >> 8 & 255) / 255.0D;
				double d2 = (i >> 0 & 255) / 255.0D;

				for (int j = 0; j < 20; ++j) {
					this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(),
							this.getRandomZ(0.5D), d0, d1, d2);
				}
			}
		} else {
			super.handleEntityEvent(id);
		}

	}
}
