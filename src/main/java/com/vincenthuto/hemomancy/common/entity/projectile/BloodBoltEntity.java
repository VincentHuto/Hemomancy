package com.vincenthuto.hemomancy.common.entity.projectile;

import com.google.common.collect.Sets;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;
import com.vincenthuto.hutoslib.client.HlClientTickHandler;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.client.particle.data.BoltParticleData;
import com.vincenthuto.hutoslib.client.particle.data.BoltParticleData.FadeFunction;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
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
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class BloodBoltEntity extends AbstractArrow implements CombatWeaponCarrierProjectile {
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(BloodBoltEntity.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DUCTILIS_TRAIL = SynchedEntityData.defineId(BloodBoltEntity.class,
			EntityDataSerializers.BOOLEAN);

	public static int getCustomColor(ItemStack p_191508_0_) {
		CustomData customData = p_191508_0_.get(DataComponents.CUSTOM_DATA);
		if (customData == null) return -1;
		CompoundTag CompoundTag = customData.copyTag();
		return CompoundTag.contains("CustomPotionColor", 99) ? CompoundTag.getInt("CustomPotionColor") : -1;
	}

	@Nullable
	private Holder<Potion> potion = null;
	private final Set<MobEffectInstance> customPotionEffects = Sets.newHashSet();
	private ItemStack combatWeaponItem = ItemStack.EMPTY;

	private boolean fixedColor;

	public BloodBoltEntity(EntityType<? extends BloodBoltEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public BloodBoltEntity(Level worldIn, double x, double y, double z) {
		super(EntityInit.blood_bolt.get(), x, y, z, worldIn, new ItemStack(ItemInit.blood_bolt.get()), null);
	}

	public BloodBoltEntity(Level worldIn, LivingEntity shooter) {
		this(worldIn, shooter, null);
	}

	public BloodBoltEntity(Level worldIn, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
		super(EntityInit.blood_bolt.get(), shooter, worldIn, new ItemStack(ItemInit.blood_bolt.get()),
				firedFromWeapon != null && !firedFromWeapon.isEmpty() ? firedFromWeapon : null);
		this.combatWeaponItem = copyCombatWeapon(firedFromWeapon);
		this.entityData.set(DUCTILIS_TRAIL, isDuctilisWeapon(firedFromWeapon));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		if (!this.combatWeaponItem.isEmpty()) {
			compound.put("CombatWeapon", this.combatWeaponItem.save(this.registryAccess()));
		}
		compound.putBoolean("DuctilisTrail", this.entityData.get(DUCTILIS_TRAIL));

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

	@Override
	public ItemStack getCombatWeaponItem() {
		return this.combatWeaponItem;
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
		builder.define(DUCTILIS_TRAIL, false);
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
		if (this.isDuctilisProjectile()) {
			this.spawnDuctilisImpactArcs(p_213868_1_.getLocation(), p_213868_1_.getEntity());
		}
		super.onHitEntity(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).addEffect(new MobEffectInstance(EffectInit.blood_loss, 1000, 2));

		}

	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		if (this.isDuctilisProjectile()) {
			Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
			this.spawnDuctilisImpactArcs(result.getLocation().add(normal.scale(0.05D)), null);
		}
		super.onHitBlock(result);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.combatWeaponItem = compound.contains("CombatWeapon", 10)
				? ItemStack.parseOptional(this.registryAccess(), compound.getCompound("CombatWeapon"))
				: ItemStack.EMPTY;
		this.entityData.set(DUCTILIS_TRAIL,
				compound.contains("DuctilisTrail") ? compound.getBoolean("DuctilisTrail")
						: isDuctilisWeapon(this.combatWeaponItem));
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

	private static ItemStack copyCombatWeapon(@Nullable ItemStack weaponStack) {
		return weaponStack != null && !weaponStack.isEmpty() ? weaponStack.copy() : ItemStack.EMPTY;
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

	@OnlyIn(Dist.CLIENT)
	private void spawnDuctilisTrailArc() {
		Vec3 motion = this.getDeltaMovement();
		if (motion.lengthSqr() < 1.0E-4D) {
			return;
		}

		Vec3 current = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
		Vec3 trailStart = current.subtract(motion.normalize().scale(0.62D + this.level().random.nextDouble() * 0.24D))
				.add(randomSigned(0.08D), randomSigned(0.08D), randomSigned(0.08D));
		Vec3 trailEnd = current.add(randomSigned(0.16D), randomSigned(0.16D), randomSigned(0.16D));
		long seed = this.level().random.nextLong() ^ this.getId() ^ ((long) this.tickCount << 32);

		BoltRenderer.INSTANCE.add(new BoltParticleData(trailStart, trailEnd, seed, 0xE8FFE84A)
				.size(0.034F).lifespan(6).fade(FadeFunction.fade(0.3F)), HlClientTickHandler.partialTicks);
		BoltRenderer.INSTANCE.add(new BoltParticleData(trailStart, trailEnd, seed, 0xFFFFFFFF)
				.size(0.016F).lifespan(6).fade(FadeFunction.fade(0.3F)), HlClientTickHandler.partialTicks);
	}

	private boolean isDuctilisProjectile() {
		return this.entityData.get(DUCTILIS_TRAIL);
	}

	private void spawnDuctilisImpactArcs(Vec3 impact, @Nullable Entity hitEntity) {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		int arcCount = hitEntity != null ? 8 : 7;
		Vec3 motion = this.getDeltaMovement();
		Vec3 fallbackDirection = motion.lengthSqr() > 1.0E-4D ? motion.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
		for (int i = 0; i < arcCount; i++) {
			Vec3 end = hitEntity != null
					? entityImpactEndpoint(impact, hitEntity, i, arcCount)
					: blockImpactEndpoint(impact, fallbackDirection, i, arcCount);
			long seed = serverLevel.random.nextLong() ^ this.getId() ^ ((long) i << 32);
			LightningTesterSpawner.spawn(serverLevel, impact, end,
					new LightningTestConfig(LightningTestConfig.Backend.BOLT, 0xE8FFE84A, 0xE8FFE84A, 0xFFFFFFFF,
							12.0F, 0.0F, 0.0F, 0.0F, 44.0F, 1.8F, 8, 6, 0.12F, 0.045F, true, seed, false, 20));
		}
	}

	private Vec3 entityImpactEndpoint(Vec3 impact, Entity hitEntity, int index, int count) {
		double angle = index * Math.PI * 2.0D / count + this.level().random.nextDouble() * 0.35D;
		double radius = Math.max(0.35D, hitEntity.getBbWidth() * 0.65D) + this.level().random.nextDouble() * 0.45D;
		double yOffset = (this.level().random.nextDouble() - 0.35D) * Math.max(0.6D, hitEntity.getBbHeight() * 0.55D);
		return impact.add(Math.cos(angle) * radius, yOffset, Math.sin(angle) * radius);
	}

	private Vec3 blockImpactEndpoint(Vec3 impact, Vec3 fallbackDirection, int index, int count) {
		double angle = index * Math.PI * 2.0D / count + this.level().random.nextDouble() * 0.4D;
		double radius = 0.45D + this.level().random.nextDouble() * 0.55D;
		Vec3 tangent = new Vec3(Math.cos(angle) * radius, (this.level().random.nextDouble() - 0.5D) * 0.25D,
				Math.sin(angle) * radius);
		return impact.add(tangent).add(fallbackDirection.scale(0.08D + this.level().random.nextDouble() * 0.18D));
	}

	private double randomSigned(double amount) {
		return (this.level().random.nextDouble() * 2.0D - 1.0D) * amount;
	}

	private static boolean isDuctilisWeapon(@Nullable ItemStack weaponStack) {
		return weaponStack != null && TendencyWeaponHelper.getWeaponTendency(weaponStack)
				.filter(EnumBloodTendency.DUCTILIS::equals)
				.isPresent();
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
				if (this.tickCount % 2 == 0 && this.isDuctilisProjectile()) {
					this.spawnDuctilisTrailArc();
				}
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
