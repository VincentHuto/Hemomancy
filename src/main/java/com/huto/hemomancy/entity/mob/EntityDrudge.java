package com.huto.hemomancy.entity.mob;

import javax.annotation.Nullable;

import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.morphlings.ItemMorphling;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class EntityDrudge extends AnimalEntity {

	public EntityDrudge(EntityType<? extends EntityDrudge> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
		System.out.println("Interacted With");
		return super.applyPlayerInteraction(player, vec, hand);
	}

	@Override
	public boolean canBreed() {
		return false;
	}

	@Override
	public boolean canPickUpItem(ItemStack itemstackIn) {
		if(itemstackIn.getItem() instanceof ItemMorphling){
		 return true;
		}else {
			return false;
		}
	}

	
	@Override
	public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
		return false;
	}
	
	
	
	int timer = 0;

	@Override
	public void tick() {
		super.tick();

		if (!world.isRemote) {
			// System.out.println(world.getBlockState(this.getPosition().add(0, 1,
			// 0)).getBlock());
			if (world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock() instanceof CropsBlock) {
				// Block crop = world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
				if (timer <= 150) {
					timer++;
					if (timer % 15 == 0) {
						this.playSound(SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, 1, 1);
					}
				}

				if (timer > 150) {
					world.setBlockState(this.getPosition().add(0, 1, 0), Blocks.AIR.getDefaultState(), 2);
					this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1, 1);
					timer = 0;
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return super.getBoundingBox();
	}

	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
		this.goalSelector.addGoal(0, new MoveToBlockGoal(this, 1.5f, 10) {
			@Override
			protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
				if (worldIn.getBlockState(pos).getBlock() instanceof CropsBlock) {
					return true;
				} else {
					return false;
				}
			}
		});
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.fromItems(Items.SUGAR), false));
		this.goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(3, new LookRandomlyGoal(this));
		this.goalSelector.addGoal(4, new RandomWalkingGoal(this, 0.75D));

	}

	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 2.0D)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double) 0.15F);
	}

	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_BAT_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_BAT_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_BAT_DEATH;
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	protected float getSoundVolume() {
		return 0.2F;
	}

	public ActionResultType func_230254_b_(PlayerEntity p_230254_1_, Hand p_230254_2_) {
		ItemStack itemstack = p_230254_1_.getHeldItem(p_230254_2_);
		if (itemstack.getItem() == Items.BUCKET && !this.isChild()) {
			p_230254_1_.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
			ItemStack itemstack1 = DrinkHelper.fill(itemstack, p_230254_1_,
					ItemInit.living_will.get().getDefaultInstance());
			p_230254_1_.setHeldItem(p_230254_2_, itemstack1);
			this.remove();
			float f = (this.rand.nextFloat() - 0.5F) * 2.0F;
			float f1 = -1;
			float f2 = (this.rand.nextFloat() - 0.5F) * 2.0F;
			this.world.addParticle(RedstoneParticleData.REDSTONE_DUST, this.getPosX() + (double) f,
					this.getPosY() + 2.0D + (double) f1, this.getPosZ() + (double) f2, 0.0D, 0.0D, 0.0D);

			return ActionResultType.func_233537_a_(this.world.isRemote);
		} else {
			return super.func_230254_b_(p_230254_1_, p_230254_2_);
		}
	}

	@Override
	protected void registerData() {
		super.registerData();
	}

	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
			@Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.setEquipmentBasedOnDifficulty(difficultyIn);
		World world = worldIn.getWorld();
		if (world instanceof ServerWorld && ((ServerWorld) world).func_241112_a_()
				.getStructureStart(this.getPosition(), true, Structure.SWAMP_HUT).isValid()) {
			this.enablePersistence();
		}

		return spawnDataIn;

	}

	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return this.isChild() ? sizeIn.height * 0.1F : 1F;
	}

	@Override
	public EntityDrudge func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		EntityDrudge catentity = EntityInit.drudge.get().create(p_241840_1_);

		return catentity;

	}
}