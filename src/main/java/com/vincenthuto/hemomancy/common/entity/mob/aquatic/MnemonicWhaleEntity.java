package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

public class MnemonicWhaleEntity extends WaterAnimal {
	private static final int SAMPLE_COOLDOWN_TICKS = 6000;
	private static final int AMBIENT_SHEDDING_CHECK_INTERVAL = 4800;
	private static final int MIN_SPAWN_DEPTH_BELOW_SEA_LEVEL = 9;
	private static final int SHALLOW_WATER_PUSH_DEPTH = 7;

	private int sampleCooldownTicks;

	public MnemonicWhaleEntity(EntityType<? extends MnemonicWhaleEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 38.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 0.55D, 80));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends MnemonicWhaleEntity> type, LevelAccessor level,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		return level.getFluidState(pos).is(FluidTags.WATER)
				&& level.getFluidState(pos.above()).is(FluidTags.WATER)
				&& level.getFluidState(pos.below()).is(FluidTags.WATER)
				&& pos.getY() <= level.getSeaLevel() - MIN_SPAWN_DEPTH_BELOW_SEA_LEVEL;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide()) {
			if (this.sampleCooldownTicks > 0) {
				this.sampleCooldownTicks--;
			} else if (this.isInWater() && this.random.nextInt(AMBIENT_SHEDDING_CHECK_INTERVAL) == 0) {
				this.spawnAtLocation(ItemInit.mnemonic_ambergris.get());
				this.sampleCooldownTicks = SAMPLE_COOLDOWN_TICKS;
			}
		}

		if (this.isInWater() && this.getY() > this.level().getSeaLevel() - SHALLOW_WATER_PUSH_DEPTH) {
			Vec3 current = this.getDeltaMovement();
			this.setDeltaMovement(current.x, Math.max(current.y - 0.02D, -0.05D), current.z);
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.GLASS_BOTTLE)) {
			return super.mobInteract(player, hand);
		}
		if (this.sampleCooldownTicks > 0) {
			this.level().playSound(null, this.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.NEUTRAL, 0.45F, 0.8F);
			return InteractionResult.sidedSuccess(this.level().isClientSide());
		}

		if (!this.level().isClientSide()) {
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			ItemStack sample = new ItemStack(ItemInit.mnemonic_ambergris.get());
			if (!player.addItem(sample)) {
				player.drop(sample, false);
			}
			this.level().playSound(null, this.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 0.7F, 0.9F);
			this.sampleCooldownTicks = SAMPLE_COOLDOWN_TICKS;
		}
		return InteractionResult.sidedSuccess(this.level().isClientSide());
	}

	@Override
	public int getMaxAirSupply() {
		return 6000;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_MNEMONIC_WHALE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_MNEMONIC_WHALE_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundInit.ENTITY_MNEMONIC_WHALE_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.45F;
	}

	@Override
	public float getVoicePitch() {
		return 0.65F + this.random.nextFloat() * 0.1F;
	}
}
