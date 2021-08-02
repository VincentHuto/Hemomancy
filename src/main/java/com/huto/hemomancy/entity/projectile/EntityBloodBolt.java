package com.huto.hemomancy.entity.projectile;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.PotionInit;
import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityBloodBolt extends AbstractArrow {
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.createKey(EntityBloodBolt.class,
			DataSerializers.VARINT);
	private Potion potion = Potions.EMPTY;
	private final Set<EffectInstance> customPotionEffects = Sets.newHashSet();
	private boolean fixedColor;

	public EntityBloodBolt(EntityType<? extends EntityBloodBolt> type, World worldIn) {
		super(type, worldIn);
	}

	public EntityBloodBolt(World worldIn, double x, double y, double z) {
		super(EntityInit.blood_bolt.get(), x, y, z, worldIn);
	}

	public EntityBloodBolt(World worldIn, LivingEntity shooter) {
		super(EntityInit.blood_bolt.get(), shooter, worldIn);
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public void setPotionEffect(ItemStack stack) {
		if (stack.getItem() == Items.TIPPED_ARROW) {
			this.potion = PotionUtils.getPotionFromItem(stack);
			Collection<EffectInstance> collection = PotionUtils.getFullEffectsFromItem(stack);
			if (!collection.isEmpty()) {
				for (EffectInstance effectinstance : collection) {
					this.customPotionEffects.add(new EffectInstance(effectinstance));
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
			this.dataManager.set(COLOR, -1);
		}

	}

	public static int getCustomColor(ItemStack p_191508_0_) {
		CompoundNBT compoundnbt = p_191508_0_.getTag();
		return compoundnbt != null && compoundnbt.contains("CustomPotionColor", 99)
				? compoundnbt.getInt("CustomPotionColor")
				: -1;
	}

	private void refreshColor() {
		this.fixedColor = false;
		if (this.potion == Potions.EMPTY && this.customPotionEffects.isEmpty()) {
			this.dataManager.set(COLOR, -1);
		} else {
			this.dataManager.set(COLOR, PotionUtils
					.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects)));
		}

	}

	public void addEffect(EffectInstance effect) {
		this.customPotionEffects.add(effect);
		this.getDataManager().set(COLOR, PotionUtils
				.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects)));
	}

	protected void registerData() {
		super.registerData();
		this.dataManager.register(COLOR, -1);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			for (int i = 0; i < 2; i++) {
				world.addParticle(GlowParticleFactory.createData(new ParticleColor(255 * world.rand.nextFloat(), 0, 0)),
						getPosX() + ParticleUtils.inRange(-0.1, 0.1), getPosY() + ParticleUtils.inRange(-0.1, 0.1),
						getPosZ() + ParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);

			}
			if (this.inGround) {
				if (this.timeInGround % 5 == 0) {
					this.spawnPotionParticles(1);
				}
			} else {
				this.spawnPotionParticles(2);
			}
		} else if (this.inGround && this.timeInGround != 0 && !this.customPotionEffects.isEmpty()
				&& this.timeInGround >= 600) {
			this.world.setEntityState(this, (byte) 0);
			this.potion = Potions.EMPTY;
			this.customPotionEffects.clear();
			this.dataManager.set(COLOR, -1);
		}

	}

	private void spawnPotionParticles(int particleCount) {
		int i = this.getColor();
		if (i != -1 && particleCount > 0) {
			double d0 = (double) (i >> 16 & 255) / 255.0D;
			double d1 = (double) (i >> 8 & 255) / 255.0D;
			double d2 = (double) (i >> 0 & 255) / 255.0D;

			for (int j = 0; j < particleCount; ++j) {
				this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getPosXRandom(0.5D), this.getPosYRandom(),
						this.getPosZRandom(0.5D), d0, d1, d2);
			}

		}
	}

	public int getColor() {
		return this.dataManager.get(COLOR);
	}

	private void setFixedColor(int p_191507_1_) {
		this.fixedColor = true;
		this.dataManager.set(COLOR, p_191507_1_);
	}

	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		if (this.potion != Potions.EMPTY && this.potion != null) {
			compound.putString("Potion", Registry.POTION.getKey(this.potion).toString());
		}

		if (this.fixedColor) {
			compound.putInt("Color", this.getColor());
		}

		if (!this.customPotionEffects.isEmpty()) {
			ListNBT listnbt = new ListNBT();

			for (EffectInstance effectinstance : this.customPotionEffects) {
				listnbt.add(effectinstance.write(new CompoundNBT()));
			}

			compound.put("CustomPotionEffects", listnbt);
		}

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("Potion", 8)) {
			this.potion = PotionUtils.getPotionTypeFromNBT(compound);
		}

		for (EffectInstance effectinstance : PotionUtils.getFullEffectsFromTag(compound)) {
			this.addEffect(effectinstance);
		}

		if (compound.contains("Color", 99)) {
			this.setFixedColor(compound.getInt("Color"));
		} else {
			this.refreshColor();
		}

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

	protected ItemStack getArrowStack() {
		if (this.customPotionEffects.isEmpty() && this.potion == Potions.EMPTY) {
			return new ItemStack(ItemInit.blood_bolt.get());
		} else {
			ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
			PotionUtils.addPotionToItemStack(itemstack, this.potion);
			PotionUtils.appendEffects(itemstack, this.customPotionEffects);
			if (this.fixedColor) {
				itemstack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
			}

			return itemstack;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 0) {
			int i = this.getColor();
			if (i != -1) {
				double d0 = (double) (i >> 16 & 255) / 255.0D;
				double d1 = (double) (i >> 8 & 255) / 255.0D;
				double d2 = (double) (i >> 0 & 255) / 255.0D;

				for (int j = 0; j < 20; ++j) {
					this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getPosXRandom(0.5D), this.getPosYRandom(),
							this.getPosZRandom(0.5D), d0, d1, d2);
				}
			}
		} else {
			super.handleStatusUpdate(id);
		}

	}
}
