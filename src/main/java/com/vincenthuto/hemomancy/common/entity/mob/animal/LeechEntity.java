package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.google.common.collect.Maps;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;

import javax.annotation.Nullable;
import java.util.Map;

public class LeechEntity extends Animal {

	private static final EntityDataAccessor<Integer> SLUG_TYPE = SynchedEntityData.defineId(LeechEntity.class,
			EntityDataSerializers.INT);
	public static final Map<Integer, ResourceLocation> TEXTURE_BY_ID = Util.make(Maps.newHashMap(), (p_213410_0_) -> {
		p_213410_0_.put(0, Hemomancy.rloc("textures/entity/leech/model_leech_grey.png"));
		p_213410_0_.put(1, Hemomancy.rloc("textures/entity/leech/model_leech_grey.png"));
		p_213410_0_.put(2, Hemomancy.rloc("textures/entity/leech/model_leech_brown.png"));
	});

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0D).add(Attributes.MOVEMENT_SPEED, 0.15F);
	}

	int timer = 0;

	public LeechEntity(EntityType<? extends LeechEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SLUG_TYPE, random.nextInt(3));
	}

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
			MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
		this.setLeechType(1);
		return spawnDataIn;

	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_LEECH_AMBIENT.get();
	}

	@Override
	public LeechEntity getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_) {
		LeechEntity catentity = EntityInit.leech.get().create(p_241840_1_);
		if (p_241840_2_ instanceof LeechEntity) {
			if (this.random.nextBoolean()) {
				catentity.setLeechType(this.getLeechType());
			} else {
				catentity.setLeechType(((LeechEntity) p_241840_2_).getLeechType());
			}

		}
		return catentity;

	}

	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(Items.SUGAR);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_LEECH_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_LEECH_HURT.get();
	}

	public int getLeechType() {
		return this.entityData.get(SLUG_TYPE);
	}

	public ResourceLocation getLeechTypeName() {
		return TEXTURE_BY_ID.getOrDefault(this.getLeechType(), TEXTURE_BY_ID.get(0));
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	@Override
	protected float getSoundVolume() {
		return 0.2F;
	}


	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
		this.goalSelector.addGoal(0, new MoveToBlockGoal(this, 1.5f, 10) {
			@Override
			protected boolean isValidTarget(LevelReader worldIn, BlockPos pos) {
				return worldIn.getBlockState(pos).getBlock() instanceof CropBlock;
			}
		});
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(Items.SUGAR), false));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.75D));

	}

	public void setLeechType(int type) {
		if (type <= 0 || type >= 3) {
			type = this.random.nextInt(4);
		}

		this.entityData.set(SLUG_TYPE, type);
	}

	@Override
	public void tick() {
		super.tick();

		if (!level().isClientSide) {
			if (level().getBlockState(this.blockPosition().offset(0, 1, 0)).getBlock() instanceof CropBlock) {
				if (timer <= 150) {
					timer++;
					if (timer % 15 == 0) {
						this.playSound(SoundEvents.CHORUS_FLOWER_DEATH, 1, 1);
					}
				}

				if (timer > 150) {
					level().setBlock(this.blockPosition().offset(0, 1, 0), Blocks.AIR.defaultBlockState(), 2);
					this.playSound(SoundEvents.PLAYER_BURP, 1, 1);
					timer = 0;
				}
			}
		}
	}
}
