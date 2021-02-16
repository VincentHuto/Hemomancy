package com.huto.hemomancy.entity.mob;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.morphlings.ItemMorphling;
import com.huto.hemomancy.item.tool.ItemDrudgeElectrode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BreakBlockGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class EntityDrudge extends TameableEntity {

	private final Inventory drudgeInventory = new Inventory(8);

	private static final DataParameter<Integer> ROLE = EntityDataManager.createKey(EntityDrudge.class,
			DataSerializers.VARINT);
	public static final Map<Integer, ResourceLocation> TEXTURE_BY_ID = Util.make(Maps.newHashMap(), (p_213410_0_) -> {
		p_213410_0_.put(0, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge.png"));
		p_213410_0_.put(1, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_blue.png"));
		p_213410_0_.put(2, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_purple.png"));
		p_213410_0_.put(3, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_green.png"));
		p_213410_0_.put(4, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_gold.png"));
		p_213410_0_.put(5, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_grey.png"));
	});

	public EntityDrudge(EntityType<? extends EntityDrudge> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	public EntityDrudge func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		EntityDrudge wolfentity = EntityInit.drudge.get().create(p_241840_1_);
		UUID uuid = this.getOwnerId();
		if (uuid != null) {
			wolfentity.setOwnerId(uuid);
			wolfentity.setTamed(true);
		}

		return wolfentity;
	}

	@Override
	protected void func_233657_b_(EquipmentSlotType p_233657_1_, ItemStack p_233657_2_) {
		super.func_233657_b_(p_233657_1_, p_233657_2_);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		this.drudgeInventory.read(compound.getList("Inventory", 10));

	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("Inventory", this.drudgeInventory.write());

	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.drudgeInventory.read(compound.getList("Inventory", 10));
	}

	public Inventory getDrudgeInventory() {
		return this.drudgeInventory;
	}

	public boolean func_230293_i_(ItemStack p_230293_1_) {
		return (this.getDrudgeInventory().func_233541_b_(p_230293_1_));
	}

	public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
		if (super.replaceItemInInventory(inventorySlot, itemStackIn)) {
			return true;
		} else {
			int i = inventorySlot - 300;
			if (i >= 0 && i < this.drudgeInventory.getSizeInventory()) {
				this.drudgeInventory.setInventorySlotContents(i, itemStackIn);
				return true;
			} else {
				return false;
			}
		}
	}

	// Defaults if the textures arnt found
	public ResourceLocation getDrudgeRoleReLoc() {
		return TEXTURE_BY_ID.getOrDefault(this.getDrudgeRole(), TEXTURE_BY_ID.get(0));
	}

	public int getDrudgeRole() {
		return this.dataManager.get(ROLE);
	}

	public void setDrudgeRole(int type) {
		if (type <= 0 || type >= 6) {
			type = this.rand.nextInt(7);
		}

		this.dataManager.set(ROLE, type);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return false;
	}

	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
		if (!player.world.isRemote) {
			if (player.getHeldItem(hand).getItem() instanceof ItemDrudgeElectrode) {
				player.sendStatusMessage(new StringTextComponent("Switching Roles").mergeStyle(TextFormatting.RED),
						true);
				this.setDrudgeRole(this.rand.nextInt(7));
			}
		}
		return super.applyPlayerInteraction(player, vec, hand);
	}

	@Override
	public boolean canPickUpItem(ItemStack itemstackIn) {
		if (itemstackIn.getItem() instanceof ItemMorphling) {
			return true;
		} else {
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
		
		/*
		 * Iterable<BlockPos> radiusPositions =
		 * BlockPos.getAllInBoxMutable(this.getPosition().add(1.0f, 1.0f, 1.0f),
		 * this.getPosition().add(-1.0f, -1, -1.0f));
		 * 
		 * if (!world.isRemote) { if (this.getDrudgeRole() == 3) {
		 * 
		 * for (BlockPos currentPos : radiusPositions) { if
		 * (world.getBlockState(currentPos).getBlock() instanceof CropsBlock) {
		 * CropsBlock crop = (CropsBlock) world.getBlockState(currentPos).getBlock(); if
		 * (world.getBlockState(currentPos).get(crop.getAgeProperty()) >=
		 * crop.getMaxAge()) {
		 * 
		 * // Block crop = world.getBlockState(this.getPosition().add(0, 1,
		 * 0)).getBlock(); if (timer <= 100) { timer++; if (timer % 15 == 0) {
		 * this.playSound(SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, 1, 1); } }
		 * 
		 * if (timer > 100) { world.destroyBlock(currentPos, true);
		 * this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1, 1); timer = 0; } }
		 * 
		 * } }
		 * 
		 * } }
		 */
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return super.getBoundingBox();
	}

	protected void registerGoals() {
		this.goalSelector.addGoal(0, new AttackCropGoal(this, 2.5D, 25));
		 this.targetSelector.addGoal(1, new DrudgeNearestAttackableTargetGoal<>(this, VillagerEntity.class, false));
		 this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 5.0D, true)); 
		/*
		 * this.targetSelector.addGoal(1, new DrudgeNearestAttackableTargetGoal<>(this,
		 * VillagerEntity.class, false)); this.goalSelector.addGoal(1, new
		 * MeleeAttackGoal(this, 5.0D, true)); this.targetSelector.addGoal(2, new
		 * DrudgeNearestAttackableTargetGoal<>(this, VillagerEntity.class, false));
		 * this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 5.0D, true));
		 */

	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 2.0D)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double) 0.15F)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D);

	}

	@Override
	public boolean canAttack(EntityType<?> typeIn) {
		if (typeIn == EntityInit.drudge.get() || typeIn == EntityType.PLAYER) {
			return false;
		} else {
			return true;
		}
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

	@Override
	public boolean canPickUpLoot() {
		return true;
	}

	@Override
	protected void updateEquipmentIfNeeded(ItemEntity itemEntity) {
		ItemStack itemstack = itemEntity.getItem();
		if (this.func_230293_i_(itemstack)) {
			Inventory inventory = this.getDrudgeInventory();
			boolean flag = inventory.func_233541_b_(itemstack);
			if (!flag) {
				return;
			}

			this.triggerItemPickupTrigger(itemEntity);
			this.onItemPickup(itemEntity, itemstack.getCount());
			ItemStack itemstack1 = inventory.addItem(itemstack);
			if (itemstack1.isEmpty()) {
				itemEntity.remove();
			} else {
				itemstack.setCount(itemstack1.getCount());
			}
			inventory.markDirty();
		}
	}

	@Override
	public void onItemPickup(Entity entityIn, int quantity) {
		super.onItemPickup(entityIn, quantity);
	}

	// This is on right click with a certain item not neccisairly by player
	@Override
	public ActionResultType func_230254_b_(PlayerEntity p_230254_1_, Hand p_230254_2_) {
		ItemStack itemstack = p_230254_1_.getHeldItem(p_230254_2_);
		Item item = itemstack.getItem();
		if (item == ItemInit.drudge_electrode.get() && !this.isTamed()) {
			if (this.rand.nextInt(3) == 0
					&& !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, p_230254_1_)) {
				this.setTamedBy(p_230254_1_);
				this.navigator.clearPath();
				this.setAttackTarget((LivingEntity) null);
				this.func_233687_w_(true);
				this.world.setEntityState(this, (byte) 7);
			} else {
				this.world.setEntityState(this, (byte) 6);
			}

			return ActionResultType.SUCCESS;
		}

		return super.func_230254_b_(p_230254_1_, p_230254_2_);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(ROLE, rand.nextInt(6));

	}

	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
			@Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.setDrudgeRole(this.rand.nextInt(7));
		World world = worldIn.getWorld();
		if (world instanceof ServerWorld && ((ServerWorld) world).func_241112_a_()
				.getStructureStart(this.getPosition(), true, Structure.SWAMP_HUT).isValid()) {
			this.setDrudgeRole(1);
			this.enablePersistence();
		}

		return spawnDataIn;

	}

	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return this.isChild() ? sizeIn.height * 0.1F : 1F;
	}

	public class DrudgeNearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
		protected final Class<T> targetClass;
		protected final int targetChance;
		protected LivingEntity nearestTarget;
		/**
		 * This filter is applied to the Entity search. Only matching entities will be
		 * targeted.
		 */
		protected EntityPredicate targetEntitySelector;

		public DrudgeNearestAttackableTargetGoal(MobEntity goalOwnerIn, Class<T> targetClassIn, boolean checkSight) {
			this(goalOwnerIn, targetClassIn, checkSight, false);
		}

		public DrudgeNearestAttackableTargetGoal(MobEntity goalOwnerIn, Class<T> targetClassIn, boolean checkSight,
				boolean nearbyOnlyIn) {
			this(goalOwnerIn, targetClassIn, 10, checkSight, nearbyOnlyIn, (Predicate<LivingEntity>) null);
		}

		public DrudgeNearestAttackableTargetGoal(MobEntity goalOwnerIn, Class<T> targetClassIn, int targetChanceIn,
				boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
			super(goalOwnerIn, checkSight, nearbyOnlyIn);
			this.targetClass = targetClassIn;
			this.targetChance = targetChanceIn;
			this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
			this.targetEntitySelector = (new EntityPredicate()).setDistance(this.getTargetDistance())
					.setCustomPredicate(targetPredicate);
		}

		/**
		 * Returns whether execution should begin. You can also read and cache any state
		 * necessary for execution in this method as well.
		 */
		public boolean shouldExecute() {

			if (this.goalOwner instanceof EntityDrudge) {
				EntityDrudge drudge = (EntityDrudge) goalOwner;
				if (this.targetChance > 0 && this.goalOwner.getRNG().nextInt(this.targetChance) != 0) {
					return false;
				} else {
					this.findNearestTarget();
					return this.nearestTarget != null;
				}
			} else {
				return false;

			}

		}

		protected AxisAlignedBB getTargetableArea(double targetDistance) {
			return this.goalOwner.getBoundingBox().grow(targetDistance, 14.0D, targetDistance);
		}

		protected void findNearestTarget() {
			if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
				this.nearestTarget = this.goalOwner.world.func_225318_b(this.targetClass, this.targetEntitySelector,
						this.goalOwner, this.goalOwner.getPosX(), this.goalOwner.getPosYEye(), this.goalOwner.getPosZ(),
						this.getTargetableArea(this.getTargetDistance()));
			} else {
				this.nearestTarget = this.goalOwner.world.getClosestPlayer(this.targetEntitySelector, this.goalOwner,
						this.goalOwner.getPosX(), this.goalOwner.getPosYEye(), this.goalOwner.getPosZ());
			}

		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			this.goalOwner.setAttackTarget(this.nearestTarget);
			super.startExecuting();
		}

		public void setNearestTarget(@Nullable LivingEntity target) {
			this.nearestTarget = target;
		}
	}

	class AttackCropGoal extends BreakBlockGoal {
		AttackCropGoal(CreatureEntity creatureIn, double speed, int yMax) {
			super(Blocks.AIR, creatureIn, speed, yMax);
			this.entity = creature;
		}

		private final MobEntity entity;
		private int breakingTime;
		@SuppressWarnings("serial")
		List<Block> harvestableBlocks = new ArrayList<Block>() {
			{
				add(Blocks.POTATOES);
				add(Blocks.WHEAT);
				add(Blocks.BEETROOTS);
				add(Blocks.CARROTS);
			}
		};

		@Nullable
		private BlockPos findTarget(BlockPos pos, IBlockReader worldIn) {
			BlockPos[] ablockpos = new BlockPos[] { pos, pos.up(), pos.up().up(), pos.south(), pos.south().south(),
					pos.south().up(), pos.south().south().up(), pos.south().down(), pos.south().south().down(),
					pos.east(), pos.east().east(), pos.east().up(), pos.east().east().up(), pos.east().down(),
					pos.east().east().down(), pos.west(), pos.west().west(), pos.west().up(), pos.west().west().up(),
					pos.west().down(), pos.west().west().down(), pos.north(), pos.north().north(), pos.north().up(),
					pos.north().north().up(), pos.north().down(), pos.north().north().down() };

			for (BlockPos blockpos : ablockpos) {
				for (Block harv : harvestableBlocks) {
					if (worldIn.getBlockState(blockpos).isIn(harv)) {
						return blockpos;
					}
				}
			}

			return null;
		}

		@Override
		public void tick() {
			super.tick();
			World world = entity.world;
			BlockPos blockpos = this.entity.getPosition();
			BlockPos blockpos1 = this.findTarget(blockpos, world);
			Random random = this.entity.getRNG();
			if ((blockpos1 != null)) {
				if (world.getBlockState(blockpos1).getBlock() instanceof IGrowable) {
					if (world.getBlockState(blockpos1).getBlock() instanceof CropsBlock) {
						CropsBlock crop = (CropsBlock) world.getBlockState(blockpos1).getBlock();
						if (world.getBlockState(blockpos1).getProperties().contains(CropsBlock.AGE)) {
							if (world.getBlockState(blockpos1).get(CropsBlock.AGE) >= crop.getMaxAge()) {

								if (this.breakingTime > 0) {
									Vector3d vector3d = this.entity.getMotion();
									this.entity.setMotion(vector3d.x, 0.3D, vector3d.z);
									if (!world.isRemote) {
										double d0 = 0.08D;
										((ServerWorld) world)
												.spawnParticle(
														new ItemParticleData(ParticleTypes.ITEM,
																new ItemStack(world.getBlockState(blockpos1).getBlock()
																		.asItem())),
														(double) blockpos1.getX() + 0.5D,
														(double) blockpos1.getY() + 0.7D,
														(double) blockpos1.getZ() + 0.5D, 3,
														((double) random.nextFloat() - 0.5D) * 0.08D,
														((double) random.nextFloat() - 0.5D) * 0.08D,
														((double) random.nextFloat() - 0.5D) * 0.08D, (double) 0.15F);
									}
								}

								if (this.breakingTime % 2 == 0) {
									Vector3d vector3d1 = this.entity.getMotion();
									this.entity.setMotion(vector3d1.x, -0.3D, vector3d1.z);
									if (this.breakingTime % 6 == 0) {
										this.playBreakingSound(world, this.destinationBlock);
									}
								}

								if (this.breakingTime > 60) {
									world.destroyBlock(blockpos1, true);
									if (!world.isRemote) {
										for (int i = 0; i < 20; ++i) {
											double d3 = random.nextGaussian() * 0.02D;
											double d1 = random.nextGaussian() * 0.02D;
											double d2 = random.nextGaussian() * 0.02D;
											((ServerWorld) world).spawnParticle(ParticleTypes.POOF,
													(double) blockpos1.getX() + 0.5D, (double) blockpos1.getY(),
													(double) blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double) 0.15F);
										}

										this.playBrokenSound(world, blockpos1);
									}
								}

								++this.breakingTime;
							}
						}
					}
				}
			}

		}

		@Override
		protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
			pos = pos.add(0.5, 0, 0.5);
			if (this.creature instanceof EntityDrudge) {
				if (world.getBlockState(pos).getBlock() instanceof IGrowable) {
					if (world.getBlockState(pos).getBlock() instanceof CropsBlock) {
						CropsBlock crop = (CropsBlock) world.getBlockState(pos).getBlock();
						if (world.getBlockState(pos).getProperties().contains(CropsBlock.AGE)) {
							if (world.getBlockState(pos).get(CropsBlock.AGE) == crop.getMaxAge()) {
								return true;
							} else {
								return false;
							}
						}
					}
				}
			}
			return false;

		}

		@Override
		public boolean shouldExecute() {
			if (this.runDelay > 0) {
				--this.runDelay;
				return false;
			} else if (this.func_220729_m()) {
				this.runDelay = 20;
				return true;
			} else {
				this.runDelay = this.getRunDelay(this.creature);
				return false;
			}
		}

		private boolean func_220729_m() {
			return this.destinationBlock != null && this.shouldMoveTo(this.creature.world, this.destinationBlock) ? true
					: this.searchForDestination();
		}

		public void playBreakingSound(IWorld worldIn, BlockPos pos) {
			worldIn.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE,
					0.5F, 0.9F + EntityDrudge.this.rand.nextFloat() * 0.2F);
		}

		public void playBrokenSound(World worldIn, BlockPos pos) {
			worldIn.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F,
					0.9F + worldIn.rand.nextFloat() * 0.2F);
		}

		public double getTargetDistanceSq() {
			return 1.14D;
		}
	}

}