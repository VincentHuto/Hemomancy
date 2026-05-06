package com.vincenthuto.hemomancy.common.entity.projectile;

import com.google.common.collect.Sets;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class BloodBoltEntity extends AbstractArrow {
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(BloodBoltEntity.class,
			EntityDataSerializers.INT);

	public static int getCustomColor(ItemStack p_191508_0_) {
		CustomData customData = p_191508_0_.get(DataComponents.CUSTOM_DATA);
		if (customData == null) return -1;
		CompoundTag CompoundTag = customData.copyTag();
		return CompoundTag.contains("CustomPotionColor", 99) ? CompoundTag.getInt("CustomPotionColor") : -1;
	}

	@Nullable
	private Holder<Potion> potion = null;
	private final Set<MobEffectInstance> customPotionEffects = Sets.newHashSet();

	private boolean fixedColor;

	public BloodBoltEntity(EntityType<? extends BloodBoltEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public BloodBoltEntity(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_bolt.get(), x, y, z, worldIn, new ItemStack(ItemInit.blood_bolt.get()), ItemStack.EMPTY);
	}

	public BloodBoltEntity(Level worldIn, LivingEntity shooter) {
		super(EntityInit.blood_bolt.get(), shooter, worldIn, new ItemStack(ItemInit.blood_bolt.get()), ItemStack.EMPTY);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		if (this.fixedColor) {
			compound.putInt("Color", this.getColor());
		}

		if (!this.customPotionEffects.isEmpty()) {
			ListTag listnbt = new ListTag();

			for (MobEffectInstance effectinstance : this.customPotionEffects) {
				listnbt.add(effectinstance.save());
			}

			compound.put("CustomPotionEffects", listnbt);
		}

	}

	public void addEffect(MobEffectInstance effect) {
		this.customPotionEffects.add(effect);
		this.getEntityData().set(COLOR,
				PotionContents.getColor(Stream.concat(
						potion != null ? potion.value().getEffects().stream() : Stream.empty(),
						this.customPotionEffects.stream()).toList()));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(COLOR, -1);
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

	public int getColor() {
		return this.entityData.get(COLOR);
	}

	@Override
	protected ItemStack getPickupItem() {
		if (this.customPotionEffects.isEmpty() && this.potion == null) {
			return new ItemStack(ItemInit.blood_bolt.get());
		} else {
			ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
			itemstack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
				new PotionContents(java.util.Optional.ofNullable(this.potion), java.util.Optional.empty(), new java.util.ArrayList<>(this.customPotionEffects)));
			if (this.fixedColor) {
				CompoundTag colorTag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
				colorTag.putInt("CustomPotionColor", this.getColor());
				itemstack.set(DataComponents.CUSTOM_DATA, CustomData.of(colorTag));
			}

			return itemstack;
		}
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(ItemInit.blood_bolt.get());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleEntityEvent(byte id) {
		if (id == 0) {
			int i = this.getColor();
			if (i != -1) {
				ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, i | 0xFF000000);
				for (int j = 0; j < 20; ++j) {
					this.level().addParticle(particle, this.getRandomX(0.5D), this.getRandomY(),
							this.getRandomZ(0.5D), 0, 0, 0);
				}
			}
		} else {
			super.handleEntityEvent(id);
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

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Potion", 8)) {
			String potionId = compound.getString("Potion");
			this.potion = potionId.isEmpty() ? null : BuiltInRegistries.POTION.getHolder(ResourceLocation.parse(potionId)).orElse(null);
		}

		if (compound.contains("custom_potion_effects", 9)) {
			ListTag listnbt2 = compound.getList("custom_potion_effects", 10);
			for (int i = 0; i < listnbt2.size(); ++i) {
				MobEffectInstance effect = MobEffectInstance.load(listnbt2.getCompound(i));
				if (effect != null) this.addEffect(effect);
			}
		}

		if (compound.contains("Color", 99)) {
			this.setFixedColor(compound.getInt("Color"));
		} else {
			this.refreshColor();
		}

	}

	private void refreshColor() {
		this.fixedColor = false;
		if (this.potion == null && this.customPotionEffects.isEmpty()) {
			this.entityData.set(COLOR, -1);
		} else {
			this.entityData.set(COLOR,
					PotionContents.getColor(Stream.concat(
							potion != null ? potion.value().getEffects().stream() : Stream.empty(),
							this.customPotionEffects.stream()).toList()));
		}

	}

	private void setFixedColor(int p_191507_1_) {
		this.fixedColor = true;
		this.entityData.set(COLOR, p_191507_1_);
	}

	public void setPotionEffect(ItemStack stack) {
		if (stack.getItem() == Items.TIPPED_ARROW) {
			PotionContents contents2 = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
			this.potion = contents2 != null && contents2.potion().isPresent() ? contents2.potion().get() : null;
			Collection<MobEffectInstance> collection = contents2 != null ? contents2.customEffects() : List.of();
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
			this.potion = null;
			this.customPotionEffects.clear();
			this.entityData.set(COLOR, -1);
		}

	}

	private void spawnPotionParticles(int particleCount) {
		int i = this.getColor();
		if (i != -1 && particleCount > 0) {
			ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, i | 0xFF000000);
			for (int j = 0; j < particleCount; ++j) {
				this.level().addParticle(particle, this.getRandomX(0.5D), this.getRandomY(),
						this.getRandomZ(0.5D), 0, 0, 0);
			}

		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			for (int i = 0; i < 2; i++) {
				level().addParticle(
						GlowParticleFactory.createData(new ParticleColor(255 * level().random.nextFloat(), 0, 0)),
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
			this.level().broadcastEntityEvent(this, (byte) 0);
			this.potion = null;
			this.customPotionEffects.clear();
			this.entityData.set(COLOR, -1);
		}

	}
}
