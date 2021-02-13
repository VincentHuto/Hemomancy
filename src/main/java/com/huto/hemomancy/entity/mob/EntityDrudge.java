package com.huto.hemomancy.entity.mob;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.morphlings.ItemMorphling;
import com.huto.hemomancy.item.tool.ItemDrudgeElectrode;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.CreatureEntity;
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
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class EntityDrudge extends CreatureEntity {

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
				this.setDrudgeRole(this.rand.nextInt(5));
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

		if (!world.isRemote) {
			if (this.getDrudgeRole() == 3) {
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
						ItemEntity item = new ItemEntity(getEntityWorld(), getPosX(), getPosY(), getPosZ(),
								new ItemStack(
										world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock().asItem()));
						world.addEntity(item);
						world.setBlockState(this.getPosition().add(0, 1, 0), Blocks.AIR.getDefaultState(), 2);
						this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1, 1);
						timer = 0;
					}
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return super.getBoundingBox();
	}

	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MoveToBlockGoal(this, 1.5f, 10) {
			@Override
			protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
				if (this.creature instanceof EntityDrudge) {
					EntityDrudge drudge = (EntityDrudge) creature;
					if (drudge.getDrudgeRole() == 3) {
						if (worldIn.getBlockState(pos).getBlock() instanceof CropsBlock) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				} else {
					return false;

				}
			}
		});
		this.goalSelector.addGoal(3,
				new TemptGoal(this, 1.25D, Ingredient.fromItems(ItemInit.drudge_electrode.get()), false));
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

	// This is on right click with a certain item not neccisairly by player
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
		this.dataManager.register(ROLE, rand.nextInt(6));

	}

	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
			@Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);

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

}