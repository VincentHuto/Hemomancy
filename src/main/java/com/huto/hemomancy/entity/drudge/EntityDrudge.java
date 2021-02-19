package com.huto.hemomancy.entity.drudge;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.tool.ItemDrudgeElectrode;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class EntityDrudge extends TameableEntity {

	private final Inventory drudgeInventory = new Inventory(1);
	public String roleTitle = "";
	private static final DataParameter<Integer> ROLE = EntityDataManager.createKey(EntityDrudge.class,
			DataSerializers.VARINT);
	private static final DataParameter<Integer> RANK = EntityDataManager.createKey(EntityDrudge.class,
			DataSerializers.VARINT);
	public static final Map<Integer, ResourceLocation> TEXTURE_BY_ID = Util.make(Maps.newHashMap(), (p_213410_0_) -> {
		p_213410_0_.put(0, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_red.png"));
		p_213410_0_.put(1, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_blue.png"));
		p_213410_0_.put(2, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_purple.png"));
		p_213410_0_.put(3, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_green.png"));
		p_213410_0_.put(4, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_yellow.png"));
		p_213410_0_.put(5, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_grey.png"));
		p_213410_0_.put(6, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_brown.png"));

	});

	public EntityDrudge(EntityType<? extends EntityDrudge> type, World worldIn) {
		super(type, worldIn);
	}

	public EnumDrudgeRoles getRoleTitle() {
		switch (this.getDrudgeRole()) {
		case 0:
			return EnumDrudgeRoles.ATTACK;
		case 1:
			return EnumDrudgeRoles.SEA;
		case 2:
			return EnumDrudgeRoles.PET;
		case 3:
			return EnumDrudgeRoles.HARVEST;
		case 4:
			return EnumDrudgeRoles.COLLECTOR;
		case 5:
			return EnumDrudgeRoles.BASE;
		case 6:
			return EnumDrudgeRoles.PLACER;
		default:
			return EnumDrudgeRoles.NOROLE;
		}
	}

	@Override
	public boolean canPickUpLoot() {
		return getRoleTitle() == EnumDrudgeRoles.COLLECTOR;
	}

	@Override
	public boolean canPickUpItem(ItemStack itemstackIn) {
		// TODO Auto-generated method stub
		return getRoleTitle() == EnumDrudgeRoles.COLLECTOR;
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
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("Inventory", this.getDrudgeInventory().write());
		compound.putInt("Role", this.getDrudgeRole());
		compound.putInt("Rank", this.getDrudgeRank());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.getDrudgeInventory().read(compound.getList("Inventory", 10));
		this.setDrudgeRole(compound.getInt("Role"));
		this.setDrudgeRank(compound.getInt("Rank"));
	}

	public Inventory getDrudgeInventory() {
		return this.drudgeInventory;
	}

	public boolean func_230293_i_(ItemStack p_230293_1_) {
		return (this.getDrudgeInventory().func_233541_b_(p_230293_1_));
	}

	public ResourceLocation getDrudgeRoleReLoc() {
		return TEXTURE_BY_ID.getOrDefault(this.getDrudgeRole(), TEXTURE_BY_ID.get(0));
	}

	public int getDrudgeRank() {
		return this.dataManager.get(RANK);
	}

	public void setDrudgeRank(int type) {
		this.dataManager.set(RANK, type);
	}

	public int getDrudgeRole() {
		return this.dataManager.get(ROLE);
	}

	public void setDrudgeRole(int type) {
		if (type <= 0 || type >= 7) {
			type = this.rand.nextInt(8);
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
				this.setDrudgeRole(this.rand.nextInt(8));
			}
		}
		return super.applyPlayerInteraction(player, vec, hand);
	}

	@Override
	public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
	}

	protected void registerGoals() {
		this.goalSelector.addGoal(0, new DrudgeEmptyToChestGoal(this, 2.5D, 25));
		this.goalSelector.addGoal(0, new DrudgeCollectItemGoal(this, 2.5D, 25));
		this.goalSelector.addGoal(0, new DrudgeHarvestCropGoal(this, 2.5D, 25));
		this.targetSelector.addGoal(0, new DrudgeNearestAttackableTargetGoal<>(this, VillagerEntity.class, false));
		this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 2.5D, true));

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
		return !(typeIn == EntityInit.drudge.get() || typeIn == EntityType.PLAYER);
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

	// This is on right click with a certain item not neccisairly by player
	@Override
	public ActionResultType func_230254_b_(PlayerEntity p_230254_1_, Hand p_230254_2_) {
		ItemStack itemstack = p_230254_1_.getHeldItem(p_230254_2_);
		Item item = itemstack.getItem();
		if (item == ItemInit.drudge_submission_device.get() && !this.isTamed()) {
			if (!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, p_230254_1_)) {
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
		this.dataManager.register(ROLE, 0);
		this.dataManager.register(RANK, 0);

	}

	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
			@Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.setDrudgeRole(this.rand.nextInt(8));
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